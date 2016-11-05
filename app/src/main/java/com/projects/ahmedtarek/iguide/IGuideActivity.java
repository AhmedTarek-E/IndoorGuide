package com.projects.ahmedtarek.iguide;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

public class IGuideActivity extends AppCompatActivity {

    CameraHandlerThread mThread;
    Camera mCamera;
    CameraPreview cameraPreview;
    FrameLayout preview;
    boolean longClick = false;
    EditText numberOfPhotos;
    String ACTION_DECODE = "newtry.DECODE_ACTION";
    TextView outputTextView;
    String TAG = "INAV";

    BroadcastReceiver decodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            final int[] whiteCounts = intent.getIntArrayExtra("frames");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    cameraPreview.stopCameraPreview();
                    int[] output = Processes.decodeData(whiteCounts, getApplicationContext(),
                            intent.getLongExtra("calctime",0));
                    StringBuilder builder = new StringBuilder();
                    for (int i : output) {
                        builder.append(i);
                    }
                    Log.d(TAG, builder.toString());
                    Intent mapIntent = new Intent(IGuideActivity.this, MuseumActivity.class);
                    mapIntent.putExtra("ID", getID(output));
                    startActivity(mapIntent);
                }
            }).start();
        }
    };

    private int getID(int[] output) {
        int ID = 0;
        int length = output.length;
        for (int i = 0 ; i < length ; i++) {
            ID += (int) (Math.pow(2, i) * output[length - i - 1]);
        }
        return ID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iguide);
        mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
        cameraPreview = new CameraPreview(this, this, mCamera);
        preview = (FrameLayout) findViewById(R.id.preview);
        preview.addView(cameraPreview);
        numberOfPhotos = (EditText) findViewById(R.id.editText);
        outputTextView = new TextView(this);
        outputTextView.setWidth(FrameLayout.LayoutParams.MATCH_PARENT);
        outputTextView.setHeight(FrameLayout.LayoutParams.MATCH_PARENT);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!longClick) {
                    cameraPreview.startCapture();
                } else {
                    setNumberTaken();
                }
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                numberOfPhotos.setCursorVisible(true);
                numberOfPhotos.setVisibility(View.VISIBLE);
                longClick = true;
                return true;
            }
        });
    }

    void setNumberTaken() {
        cameraPreview.setNumberTaken(Integer.parseInt(numberOfPhotos.getText().toString()));
        numberOfPhotos.setVisibility(View.INVISIBLE);
        longClick = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraPreview == null) {
            mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
            cameraPreview = new CameraPreview(this, this, mCamera);
            preview.addView(cameraPreview);
        }
        registerReceiver(decodeReceiver, new IntentFilter(ACTION_DECODE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(decodeReceiver);
        if (cameraPreview == null) {
            mCamera = getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
            cameraPreview = new CameraPreview(this, this, mCamera);
            preview.addView(cameraPreview);
        }
        if (mCamera != null) {
            cameraPreview.stopCameraPreview();
        }
        preview.removeAllViews();
        cameraPreview = null;
        releaseCamera();
    }

    private void releaseCamera () {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private static class CameraHandlerThread extends HandlerThread {
        android.os.Handler mHandler = null;
        Camera c = null;

        public CameraHandlerThread() {
            super("CameraHandlerThread");
            //setPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
            start();
            mHandler = new android.os.Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }

        Camera openCamera(final int cam) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        c = Camera.open(cam); // attempt to get a Camera instance
                    }
                    catch (Exception e){
                    }

                    notifyCameraOpened();
                }
            });

            try {
                wait();
            } catch (InterruptedException e) {
                Log.i("ANOTHER THREAD", "wait was interrupted");
            }
            return c;
        }
    }
    /** A safe way to get an instance of the Camera object. */
    public Camera getCameraInstance(int cam){
        Camera camera = null;
        if (mThread == null) {
            mThread = new CameraHandlerThread();
        }

        synchronized (mThread) {
            camera = mThread.openCamera(cam);
        }
        return camera;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_iguide, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
