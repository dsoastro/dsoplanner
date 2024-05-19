package com.astro.dsoplanner.scopedrivers;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Locale;

import android.util.Log;

import com.astro.dsoplanner.AstroTools.RaDecRec;
import com.astro.dsoplanner.Logr;


import com.astro.dsoplanner.AstroTools;


public class IOptronDriver implements TelescopeDriver {

    private static final String US_ASCII = "US-ASCII";


    private static final String TAG = "IOptron";
    String messages = "";

    private PrintStream mPs = null;
    private Double mRA, mDEC;
    private boolean cancelFlag = false;

    int type; //short or long data
    final int timeout = 5000;

    public int getEpoch() {
        return TelescopeDriver.EPOCH_CURRENT;
    }

    public synchronized void addMessage(byte[] b, int length) {
        messages += new String(b, 0, length);
    }

    private void clearMessages() {
        messages = ""; //new ArrayList<String>();
    }

    public int getDriver() {
        return TelescopeDriver.IOptron;
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
        return messages.length(); //.size();
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
        mRA = AstroTools.normalise24(ra);
        mDEC = AstroTools.normalise(dec); //if dec is negative it is converted to 360 +dec
    }


    public synchronized RaDecRec getPosition() {

        String ack = "";
        Logr.d(TAG, "getPosition, start");
        clearMessages();

        command(":GEP#");

        //return from scope sTTTTTTTT TTTTTTTTTnn#
        //example +0540000008100000011#
        ack = waitForChars(21, timeout);
        Logr.d(TAG, "getPosition, ack=" + ack + " " + ack.length());
        if (ack == null)
            return null;

        char s = ack.charAt(0);
        int sign = 1;
        if (s == '+') {
            sign = 1;
        } else if (s == '-') {
            sign = -1;
        } else {
            CommunicationManager.error("Wrong sign: " + s);
            return null;
        }
        String decStr = ack.substring(1, 9);
        int idec = 0;
        try {
            idec = Integer.parseInt(decStr);
        } catch (Exception e) {
            CommunicationManager.error("Wrong dec: " + decStr);
            return null;
        }

        String raStr = ack.substring(9, 18);
        int ira = 0;
        try {
            ira = Integer.parseInt(raStr);
        } catch (Exception e) {
            CommunicationManager.error("Wrong ra: " + raStr);
            return null;
        }
        double ra = ira / 100. / 3600. * 24. / 360.;
        double dec = idec / 100. / 3600 * sign;
        return new RaDecRec(ra, dec);


    }


    public synchronized void slewToPosition() {

        clearMessages();

        //set RA :SRATTTTTTTTT#
        int sec = (int) (mRA * 360 / 24 * 3600 * 100);
        String secStr = String.format(Locale.US, "%09d", sec);
        String msg = ":SRA" + secStr + "#";
        command(msg);
        String ack = waitForChars(1, timeout);
        Logr.d(TAG, "set ra, ack=" + ack + " " + (ack == null ? 0 : ack.length()));
        if (!"1".equals(ack)) {
            CommunicationManager.error("Wrong RA set response: " + ack);
            return;
        }
        clearMessages();

        //set DEC :SdsTTTTTTTT#
        sec = (int) (mDEC * 3600 * 100);
        secStr = String.format(Locale.US, "%09d", sec);
        msg = ":Sds" + secStr + "#";
        command(msg);
        ack = waitForChars(1, timeout);
        Logr.d(TAG, "set dec, ack=" + ack + " " + (ack == null ? 0 : ack.length()));
        if (!"1".equals(ack)) {
            CommunicationManager.error("Wrong DEC set response: " + ack);
            return;
        }
        clearMessages();

        command(":MS1#");
        ack = waitForChars(1, timeout);
        Logr.d(TAG, "MS1, ack=" + ack + " " + (ack == null ? 0 : ack.length()));
        if (!"1".equals(ack)) {
            CommunicationManager.error("Mount motion error response: " + ack);
            return;
        }
        clearMessages();
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
