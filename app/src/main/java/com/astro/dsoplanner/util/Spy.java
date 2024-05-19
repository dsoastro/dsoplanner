package com.astro.dsoplanner.util;

import android.util.Log;

import static java.lang.System.nanoTime;

public class Spy {
    private static final String TAG = Spy.class.getSimpleName();
    private static long t0 = nanoTime();

    public static void t(String s) {
        long t = nanoTime();
        long dt = (t - t0) / 1000000;
        t0 = t;
        Log.d(TAG, s + "\n-------------------------------------------------------------------------\n" + dt);
    }
}
