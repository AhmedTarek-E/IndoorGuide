package com.projects.ahmedtarek.iguide;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

public class MuseumActivity extends AppCompatActivity {
    private TextView paintBody;
    private TextView paintTitle;
    private ImageView imageView;
    private final String TAG = "iGuide";
    private DatabaseReference mDatabase;
    private Map<String, String> dataObject = null;
    private android.os.Handler mHandler;
    private final String TITLE_COLUMN = "title";
    private final String DATA_COLUMN = "data";


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_museum);
        mHandler = new android.os.Handler(getMainLooper());
        mDatabase = FirebaseDatabase.getInstance().getReference();
        initialize();
        final int ID = getIntent().getIntExtra("ID", 0);
        final String imageName = "pics/" + ID + ".jpg";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReferenceFromUrl("gs://indoorguide-80788.appspot.com")
                .child(imageName)
                .getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(MuseumActivity.this).load(uri).into(imageView);
                    }
                });
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = (Iterator<DataSnapshot>) dataSnapshot.getChildren().iterator();
                DataSnapshot snapshot = null;

                for (int i = 0 ; i < ID ; i++) {
                    if (!iterator.hasNext()) {
                        Toast.makeText(MuseumActivity.this, "Wrong ID", Toast.LENGTH_SHORT).show();
                        snapshot = null;
                        break;
                    }
                    snapshot = iterator.next();
                }
                if (snapshot != null) {
                    final Post post = snapshot.getValue(Post.class);
                    dataObject = post.toMap();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateLayout();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void initialize() {
        paintBody = (TextView) findViewById(R.id.body);
        paintTitle = (TextView) findViewById(R.id.paint_title);
        imageView = (ImageView) findViewById(R.id.painting);
    }


    private void updateLayout() {
        String title = dataObject.get(TITLE_COLUMN);
        String body = dataObject.get(DATA_COLUMN);

        paintTitle.setText(title);
        paintBody.setText(body);
    }


}
