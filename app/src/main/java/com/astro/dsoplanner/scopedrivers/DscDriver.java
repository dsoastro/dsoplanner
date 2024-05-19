package com.astro.dsoplanner.scopedrivers;

import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.AstroTools.RaDecRec;
import com.astro.dsoplanner.Logr;


import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Locale;


public class DscDriver implements TelescopeDriverDsc {

    private static final String US_ASCII = "US-ASCII";


    private static final String TAG = "DSC";
    String messages = "";

    private PrintStream mPs = null;
    private Double mRA, mDEC;
    private boolean cancelFlag = false;

    int type; //short or long data
    final int timeout = 5000;
    int azResolution;
    int altResolution;

    public DscDriver(int azResolution, int altResolution) {
        this.azResolution = azResolution;
        this.altResolution = altResolution;
    }

    public int getEpoch() {
        return TelescopeDriver.EPOCH_2000;
    }

    public synchronized void addMessage(byte[] b, int length) {
        messages += new String(b, 0, length);
    }

    private void clearMessages() {
        messages = "";
    }

    public int getDriver() {
        return TelescopeDriver.DSC;
    }

    private String getMessage() {
        String tmp = messages;
        messages = "";
        return tmp;
    }

    private String getMessage(int sz) {
        if (messages.length() >= sz) {
            String r = messages.substring(0, sz);
            messages = messages.substring(sz);
            return r;
        } else
            return null;
    }


    private int getMessageCount() {
        return messages.length();
    }

    public synchronized void setOutputStream(OutputStream out) {
        try {
            mPs = new PrintStream(out, true, US_ASCII);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            mPs = null;
        }
    }

    public synchronized void cancel() {
        cancelFlag = true;
        this.notifyAll();
    }

    public synchronized void setPosition(double ra, double dec) {

    }

    public synchronized void sendResolution() {
        String message = "R" + Math.abs(azResolution) + "\t" + Math.abs(altResolution) + "\r";
        command(message);
    }


    public synchronized RaDecRec getPosition() {

        String ack = "";
        Logr.d(TAG, "getPosition, start");
        clearMessages();

        command("Q");

        //return from scope +04512<tab>-01297
        ack = waitForChars(13, timeout);

        if (ack == null) {
            Logr.d(TAG, "getPosition, ack=null");
            return null;
        } else
            Logr.d(TAG, "getPosition, ack=" + ack + " " + ack.length());

        String[] arr = ack.split("\t");
        if (arr.length != 2) {
            CommunicationManager.error("Wrong answer: " + ack);
            return null;
        }
        int iaz = 0;
        try {
            iaz = Integer.parseInt(arr[0]);
        } catch (Exception e) {
            CommunicationManager.error("Wrong az: " + arr[0]);
            return null;
        }

        int ialt = 0;
        try {
            ialt = Integer.parseInt(arr[1]);
        } catch (Exception e) {
            CommunicationManager.error("Wrong alt: " + arr[1]);
            return null;
        }

        double az = iaz * 360. / azResolution;
        double alt = ialt * 360. / altResolution;
        Log.d(TAG, "az=" + az + " alt=" + alt);
        return new RaDecRec(az, alt);


    }


    public synchronized void slewToPosition() {

    }

    private void command(CharSequence s) {
        mPs.print(s);
        mPs.flush();
        Logr.d(TAG, s + " sent");
    }

    private String waitForChars(int timeout) {
        String ans = null;
        int i = 0;

        try {
            do {
                this.wait(timeout);
            }
            while ((ans = getMessage()) == null && i++ < 1);

        } catch (InterruptedException e) {
            Logr.d(TAG, "Interrupted excptn");
            return null;
        }

        Logr.d(TAG, "ans " + ans);
        return ans;
    }

    private void logBytes(String msg) {
        for (int i = 0; i < msg.length(); i++) {
            Log.d(TAG, "char at " + i + " " + (int) (msg.charAt(i)));
        }
    }

    private String waitForChars(int nChars, int timeout) {
        String ans = null;
        int i = 0;
        long timeStart = Calendar.getInstance().getTimeInMillis();
        long timeNow = 0;
        try {
            do {
                this.wait(500);
                timeNow = Calendar.getInstance().getTimeInMillis();
                Logr.d(TAG, "wait for chars");
            }
            while (getMessageCount() < nChars && (timeNow - timeStart) < timeout);
        } catch (InterruptedException e) {
            Logr.d(TAG, "Interrupted excptn");
            return null;
        }
        Logr.d(TAG, "messages=" + messages + " size=" + messages.length());
        ans = getMessage(nChars);

        Logr.d(TAG, "ans " + ans);
        return ans;
    }
}
