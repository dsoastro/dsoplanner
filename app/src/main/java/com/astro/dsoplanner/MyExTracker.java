package com.astro.dsoplanner;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Timestamp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

//helper class for saving uncaught exceptions
public class MyExTracker {

    private static final String ERROR_TXT = "error.txt";
    private static final String PLAIN_TEXT = "plain/text";
    private static final String SEND_ERROR_LOG_TO_DEVELOPERS = "Send error log to developers...";
    private static final String CRASH_LOG = "Crash log";
    private static final String DSOPLANNER_GMAIL_COM = "dsoplanner@gmail.com";

    public static void saveInfo(File f, String s, Exception e) {
        BufferedOutputStream bos;
        PrintStream out;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(f, true));
            out = new PrintStream(bos);
            out.println(new Timestamp(System.currentTimeMillis()));
            out.println(s);
            if (e != null) e.printStackTrace(out);
            out.close();
        } catch (Exception ex) {
        }

    }

    public static void saveInfo2(File f, String s, Exception e) {
        BufferedOutputStream bos;
        PrintStream out;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(f, true));
            out = new PrintStream(bos);
            out.print(new Timestamp(System.currentTimeMillis()));
            out.println(" " + s);
            if (e != null) e.printStackTrace(out);
            out.close();
        } catch (Exception ex) {
        }

    }

    static void saveReport(Throwable t) {//saving report to both memory and card
        BufferedOutputStream bos;
        PrintStream out;
        try {
            File f = new File(Global.getAppContext().getDir("", Context.MODE_PRIVATE), ERROR_TXT);
            bos = new BufferedOutputStream(new FileOutputStream(f, true));
            out = new PrintStream(bos);
            out.println(new Timestamp(System.currentTimeMillis()));
            t.printStackTrace(out);
            out.close();
        } catch (Exception e) {
        }
    }

    static void sendReport(Activity a) {

        //sending report file if there is one in memory, deleting it afterwards
        boolean readOk = true;
        StringBuilder sb = new StringBuilder();
        sb.append(a.getString(R.string.app_version_name));
        sb.append(" ");
        sb.append(a.getPackageName());
        sb.append("\n");
        File fint = new File(Global.getAppContext().getDir("", Context.MODE_PRIVATE), ERROR_TXT);
        try {

            FileInputStream fin = new FileInputStream(fint);
            BufferedReader in = new BufferedReader(new InputStreamReader(fin));
            String s = null;

            while ((s = in.readLine()) != null) {
                sb.append(s);
                sb.append("\n ");
            }
            in.close();

        } catch (Exception e) {
            readOk = false;
        }

        if (readOk) {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType(PLAIN_TEXT);
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{DSOPLANNER_GMAIL_COM});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, CRASH_LOG);
            emailIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
            try {//if there is no one to handle the intent
                a.startActivity(Intent.createChooser(emailIntent, SEND_ERROR_LOG_TO_DEVELOPERS));
            } catch (Exception e) {
            }
            fint.delete();
        }
    }
}
