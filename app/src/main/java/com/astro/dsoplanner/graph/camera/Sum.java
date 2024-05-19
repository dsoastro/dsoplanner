package com.astro.dsoplanner.graph.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Process;
import android.util.Log;

import com.astro.dsoplanner.Logg;

import java.util.concurrent.Callable;

class Sum implements Callable<Void> {
    short[] total;
    byte[] data;
    int offset;
    int N;
    Manager.ImageType imageType;
    int[] pixels;

    public Sum(short[] total, byte[] data, int[] pixels, int offset, int n, Manager.ImageType imageType) {
        this.total = total;
        this.data = data;
        this.offset = offset;
        N = n;
        this.imageType = imageType;
        this.pixels = pixels;
    }


    public Void call() {
        Process.setThreadPriority(Prefs.THREAD_PRIORITY);
        if (imageType == Manager.ImageType.YUV) {
            int size = data.length;
            for (int i = offset; i < size; i += N) {
                int v = data[i] & 0xFF;
                total[i] += v;

            }
            return null;
        } else { //JPEG
            int size = pixels.length;

            for (int j = offset; j < size; j += N) {
                int color_p = pixels[j];
                int blue_p = color_p & 0xff;
                int green_p = (color_p & 0xff00) >> 8;
                int red_p = (color_p & 0xff0000) >> 16;

                total[j] += (blue_p + green_p + red_p) / 3;
            }
            return null;

        }
    }
}
