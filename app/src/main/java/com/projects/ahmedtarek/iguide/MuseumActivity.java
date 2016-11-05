package com.projects.ahmedtarek.iguide;

import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MuseumActivity extends AppCompatActivity {
    private TextView paintBody;
    private TextView paintTitle;
    private ImageView imageView;
    private final String TAG = "iGuide";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_museum);

        initialize();
        int ID = getIntent().getIntExtra("ID", 0);
        new HttpGetTask().execute(ID);
    }

    void initialize() {
        paintBody = (TextView) findViewById(R.id.body);
        paintTitle = (TextView) findViewById(R.id.paint_title);
        imageView = (ImageView) findViewById(R.id.painting);
    }

    private static Drawable loadImageFromURL(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "Painting");
            return d;
        } catch (IOException e) {
            return null;
        }
    }

    public class LoadImageTask extends AsyncTask<String, Void, Drawable> {

        @Override
        protected Drawable doInBackground(String... params) {
            Drawable painting = loadImageFromURL(params[0]);
            return painting;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            imageView.setImageDrawable(drawable);
        }
    }

    public class HttpGetTask extends AsyncTask<Integer, Void, List<String>> {

        AndroidHttpClient client = AndroidHttpClient.newInstance("");

        @Override
        protected List<String> doInBackground(Integer... params) {
            final String URL = "http://www.inav.netau.net/json/" + params[0] +".JS";
            HttpGet httpGetRequest = new HttpGet(URL);
            JSONResponseHandler responseHandler = new JSONResponseHandler();
            try {
                return client.execute(httpGetRequest, responseHandler);
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            if (null != client) {
                client.close();
                if (strings != null) {
                    if (strings.size() == 3) {
                        String title = strings.get(0);
                        String body = strings.get(1);
                        updateLayout(title, body);
                        new LoadImageTask().execute(strings.get(2));
                    }
                } else {
                    Log.d(TAG, "There is no JSON Data");
                    Toast.makeText(getApplicationContext(), "Failed to fetch data from the server", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void updateLayout(String title, String body) {
        paintTitle.setText(title);
        paintBody.setText(body);
    }

    private class JSONResponseHandler implements ResponseHandler<List<String>> {

        private static final String TITLE_TAG = "title";
        private static final String DATA_TAG = "data";
        private static final String URL_TAG = "url";

        @Override
        public List<String> handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
            List<String> result = new ArrayList<>();
            String JSONResponse = new BasicResponseHandler().handleResponse(httpResponse);
            StringBuilder stringBuilder = new StringBuilder(JSONResponse);
            stringBuilder.delete(0, JSONResponse.indexOf("[")-1);

            try {
                JSONArray responseObject = (JSONArray) new JSONTokener(stringBuilder.toString()).nextValue();
                JSONObject jsonObject = responseObject.getJSONObject(0);
                result.add(jsonObject.getString(TITLE_TAG));
                result.add(jsonObject.getString(DATA_TAG));
                result.add(jsonObject.getString(URL_TAG));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return result;
        }
    }
}
