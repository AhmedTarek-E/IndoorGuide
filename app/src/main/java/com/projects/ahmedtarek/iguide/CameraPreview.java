package com.projects.ahmedtarek.iguide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.hardware.Camera;
import android.util.Log;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

import static java.lang.System.currentTimeMillis;

/**
 * Created by Ahmed Tarek on 3/23/2016.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    Camera mCamera;
    int width;
    int height;
    int init;
    int end;
    int l;
    int h;
    int w;
    int lumaSize;
    int bufferLength;
    long startTime;
    long previousTime;
    long nowTime;
    boolean startPreviewCallback = false;
    int countDone = 0;
    int count = 0;
    int numberTaken = 120;
    long[] timesBetweenRawImages = new long[numberTaken-1];
    String TAG = "New Try";
    SurfaceHolder mHolder;
    String ACTION_DONE = "newtry.DONE_ACTION";
    String ACTION_DECODE = "newtry.DECODE_ACTION";
    Context mContext;
    Activity activity;
    int[] framesWhiteCount = new int[numberTaken];
    boolean flag = true;
    int minEx;
    long startCalcTime;
    long endCalcTime;
    long currentFrameTime;
    long previousFrameTime;

    public CameraPreview(Activity activity, Context context, Camera camera) {
        super(context);
        mContext = context;
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.activity = activity;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                Camera.Parameters p = mCamera.getParameters();
                p.setAutoExposureLock(false);
                mCamera.setParameters(p);
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                p.setAutoExposureLock(true);
                mCamera.setParameters(p);
            }
        }).start();
*/

        if (startPreviewCallback) {
            if (count == 0) {
                startTime = currentTimeMillis();
                previousFrameTime = currentFrameTime;
            } else if (count < numberTaken) {
                currentFrameTime = currentTimeMillis();
                timesBetweenRawImages[count - 1] = currentFrameTime - previousFrameTime;
                previousFrameTime = currentFrameTime;
            }
            if (count < numberTaken) {
                int x = count;
                if (x == 0) {
                    startCalcTime = currentTimeMillis();
                }
                try {
                    framesWhiteCount[x] = Processes.calculateWhiteCount(data, init, end, width, l);
                    //framesWhiteCount[x] = Processes.calculateWhiteCount(data);
                    if (x == 0) {
                        endCalcTime = currentTimeMillis();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                count++;
                if (count == numberTaken) {
                    nowTime = currentTimeMillis();
                }
            } else if (count == numberTaken) {
                mContext.sendBroadcast(new Intent(ACTION_DECODE).putExtra("frames", framesWhiteCount)
                        .putExtra("calctime", endCalcTime - startCalcTime).putExtra("times", timesBetweenRawImages));
                stopCapture();
            }
        }
        camera.addCallbackBuffer(data);
    }

    public long getTotalTime() {
        return nowTime - startTime;
    }

    public double getFPS() {
        return ((double) numberTaken/(double) getTotalTime())*1000;
    }

    public void startCapture() {
        startPreviewCallback = true;
    }

    public void stopCapture() {
        startPreviewCallback = false;
        count = 0;
        countDone = 0;

    }

    public void setNumberTaken(int numberTaken) {
        this.numberTaken = numberTaken;
        framesWhiteCount = new int[numberTaken];
        timesBetweenRawImages = new long[numberTaken-1];
    }

    public int getNumberTaken() {
        return numberTaken;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallbackWithBuffer(this);
        } catch (IOException e) {
            Log.e(TAG, "Error setPreviewDisplay");
        }
        startCameraPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        stopCameraPreview();
        try {
            mCamera.setPreviewDisplay(mHolder);
        } catch (IOException e) {
            Log.e(TAG, "Error setPreviewDisplay");
        }
        startCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            stopCameraPreview();
        } catch (Exception e) {
            Log.d(TAG, "No camera to stop preview or can't stop");
        }
        releaseCamera();
    }

    private void releaseCamera () {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void stopCameraPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public void startCameraPreview() {
        Camera.Parameters p = mCamera.getParameters();

        this.height = 720;
        this.width = 1280;
        l = height*7/11;
        h = (height-l)/2;
        w = (width-l)/2;

        init = ((h)*width) + w;
        end = ((h+l)*width) + w+l;
        p.setPreviewSize(this.width,this.height);
        Camera.Size size = p.getPreviewSize();
        lumaSize = size.width*size.height;
        List<Camera.Size> s = p.getSupportedPreviewSizes();
        List<int[]> fpsRange = p.getSupportedPreviewFpsRange();
        p.setPreviewFpsRange(fpsRange.get(0)[1], fpsRange.get(0)[1]);
        minEx = p.getMinExposureCompensation();
        p.setExposureCompensation(p.getMaxExposureCompensation()); //exposure value
        if (p.isAutoExposureLockSupported())
            p.setAutoExposureLock(true);
        if (p.isAutoWhiteBalanceLockSupported())
            p.setAutoWhiteBalanceLock(true);
        mCamera.setDisplayOrientation(90);

        mCamera.setParameters(p);
        bufferLength = size.height*size.width*3/2;
        for (int i = 0;i < 10;i++ ) {
            mCamera.addCallbackBuffer(new byte[bufferLength]);
        }
        //mCamera.setPreviewCallbackWithBuffer(this);
        mCamera.setPreviewCallbackWithBuffer(this);
        mCamera.startPreview();
    }
}
