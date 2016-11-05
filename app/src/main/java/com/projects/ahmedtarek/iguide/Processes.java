package com.projects.ahmedtarek.iguide;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Ahmed Tarek on 4/19/2016.
 */
public abstract class Processes {
    int[] whites;

    public static int[] decodeData(List<byte[]> images, Context context) {
        String ACTION_DONE = "newtry.DONE_ACTION";
        int[] output = new int[7];
        int[] framesWhiteCount = new int[images.size()];
        for (int i = 0 ; i < images.size() ; i++) {
            framesWhiteCount[i] = calculateWhiteCount(images.get(i));
        }

        int[] syncPoints = findSynchronizationPoints(framesWhiteCount);
        context.sendBroadcast(new Intent(ACTION_DONE).putExtra("whitecount", framesWhiteCount)
                .putExtra("syncpoints", syncPoints));

        List<double[]> averages = new ArrayList<>(syncPoints.length);
        for (int i = 0 ; i < syncPoints.length ; i++) {
            if (i != syncPoints.length-1) {
                averages.add(getAverages(framesWhiteCount,syncPoints[i],syncPoints[i+1]));
            } else {
                averages.add(getAverages(framesWhiteCount,syncPoints[i]));
            }
        }
        //TODO Error Correction
        double[] average = averages.get(0);
        for (int i = 0 ; i < output.length ; i++) {
            if (average[i*2] > average[(i*2)+1]) {
                output[i] = 1;
            } else {
                output[i] = 0;
            }
        }
        return output;
    }

    public static int[] decodeData(int[] framesWhiteCount, Context context, long calcTime) {
        String ACTION_DONE = "newtry.DONE_ACTION";
        int[] output = new int[7];
        int[] syncPoints = findSynchronizationPoints(framesWhiteCount);

        //.putExtra("times",timesBetweenFrames)
        List<double[]> averages = new ArrayList<>(syncPoints.length);
        for (int i = 0 ; i < syncPoints.length ; i++) {
            if (i != syncPoints.length-1) {
                averages.add(getAverages(framesWhiteCount,syncPoints[i],syncPoints[i+1]));
            } else {
                averages.add(getAverages(framesWhiteCount,syncPoints[i]));
            }
        }
        //TODO Error Correction
        double[] average = averages.get(0);
        for (int i = 0 ; i < output.length ; i++) {
            if (average[i*2] > average[(i*2)+1]) {
                output[i] = 1;
            } else {
                output[i] = 0;
            }
        }
        context.sendBroadcast(new Intent(ACTION_DONE).putExtra("whitecount", framesWhiteCount)
                .putExtra("syncpoints", syncPoints).putExtra("calculation", calcTime)
                .putExtra("output", output));
        return output;
    }

    public static double[] getAverages(int[] frames, int initSync, int nextSync) {
        int length = (nextSync-initSync)/2;
        double[] averages;
        if (length % 2 == 0) {
            averages = new double[length];
        } else {
            averages = new double[length-1];
        }
        int k = 0;
        for (int i = initSync+1 ; i < nextSync-2 ; i += 2 ) {
            if ((k == length-1) & (length % 2 == 1)) {
                break;
            } else {
                averages[k] = (frames[i]+frames[i+1])/2;
            }
            k++;
        }
        return averages;
    }

    public static double[] getAverages(int[] frames, int initSync) {
        int length = (frames.length - initSync)/2;
        double[] averages;
        if (length % 2 == 0) {
            averages = new double[length];
        } else {
            averages = new double[length-1];
        }
        int k = 0;
        for (int i = initSync+1 ; i < frames.length-1 ; i += 2 ) {
            if ((k == length-1) & (length % 2 == 1)) {
                break;
            } else {
                averages[k] = (frames[i]+frames[i+1])/2;
            }
            k++;
        }
        return averages;
    }

    public static int[] findSynchronizationPoints(int[] frames) {
        int[] syncPoints = new int[frames.length/60];
        for (int i = 0 ; i < frames.length/60 ; i++){
            int init = i*60;
            int max = 0;
            int index = 0;
            for (int j = init ; j < init+60 ; j++) {
                if (max < frames[j]) {
                    max = frames[j];
                    index = j;
                }
            }
            if (frames[index-1] > frames[index+1]) {
                syncPoints[i] = index;
            } else {
                syncPoints[i] = index+1;
            }
        }
        return syncPoints;
    }

    public static int calculateWhiteCount(byte[] img) {
        int lumaSize = img.length * 2/3;
        int whiteCount = 0;
        int compare = ((byte) 250) & (0xFF);
        for (int i = 0; i < lumaSize; i++) {
            whiteCount = (img[i] & 0xFF) > compare ? whiteCount+1 : whiteCount;
        }
        return whiteCount;
    }

    public static int calculateWhiteCount(byte[] img, int init, int end, int width, int l) throws Exception {
        int lumaSize = img.length * 2/3;
        if (end > lumaSize) {
            throw new Exception("Wrong crop size");
        }
        int whiteCount = 0;
        int compare = ((byte) 250) & (0xFF);

        /*for (int i = init; i <= end; i++) {
            int b = img[i] & 0xFF;
            if (b > compare) {
                whiteCount++;
            }
        }*/

        for (int i = 0 ; i < l ; i++) {
            for (int j = init + i*width ; j < init + (i*width) + l ; j++) {
                whiteCount = (img[j] & 0xFF) > compare ? whiteCount+1 : whiteCount;
            }
        }
        return whiteCount;
    }



}
