package com.astro.dsoplanner;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.sql.Timestamp;


public class Logg {
    static String path;

    public static void setFile(String mpath) {
        path = mpath;
    }

    public static String getFile() {
        return path;
    }

    public static void d(String TAG, String s) {
        Log.d(TAG, s);
        BufferedOutputStream bos;
        PrintStream out;
        if (path != null) {
            File f = new File(path);
            try {
                bos = new BufferedOutputStream(new FileOutputStream(f, true));
                out = new PrintStream(bos);
                out.print(new Timestamp(System.currentTimeMillis()));
                out.println(" " + TAG + " " + s);
                out.close();
            } catch (Exception ex) {
            }
        }
    }

    public static String get() {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } catch (Exception e) {
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
        }
        return null;
    }
}
