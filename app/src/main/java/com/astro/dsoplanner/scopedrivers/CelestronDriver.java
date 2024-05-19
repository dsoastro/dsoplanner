package com.astro.dsoplanner.scopedrivers;

import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.AstroTools.RaDecRec;


import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class CelestronDriver implements TelescopeDriver {

    private static final String US_ASCII = "US-ASCII";


    public static final int SHORT_DATA = 1;
    public static final int LONG_DATA = 2;
    private static final String TAG = CelestronDriver.class.getSimpleName();
    String messages = "";

    private PrintStream mPs = null;
    private Double mRA, mDEC;
    private boolean cancelFlag = false;

    int type; //short or long data
    final int timeout = 2000;

    /**
     * @param type short or long data
     */
    public CelestronDriver(int type) {
        this.type = type;
    }

    public int getDriver() {
        return TelescopeDriver.Celestron;
    }

    public int getEpoch() {
        return TelescopeDriver.EPOCH_2000;
    }

    public synchronized void addMessage(byte[] b, int length) {
        messages += new String(b, 0, length);
    }

    private void clearMessages() {
        messages = ""; //new ArrayList<String>();
    }

    private String getMessage() {
        int index = messages.indexOf("#");
        if (index > -1) {
            try {
                String s = messages.substring(0, index);
                messages = messages.substring(index + 1);
                return s;
            } catch (Exception e) {//just in case
                return null;
            }
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
        switch (type) {
            case SHORT_DATA:
                return getPositionShort();
            case LONG_DATA:
                return getPositionLong();
        }
        return null;
    }

    private synchronized RaDecRec getPositionShort() {

        String ack = "";
        clearMessages();

        command("E");

        //return from scope 34AB,12CE#
        ack = waitForChars(timeout);
        if (ack == null)
            return null;
        String[] sarr = ack.split(",");
        if (sarr.length < 2)
            return null;
        Integer ra = getInt(sarr[0]);
        Integer dec = getInt(sarr[1]);
        Log.d(TAG, "ra str=" + sarr[0] + "ra=" + ra);
        Log.d(TAG, "dec str=" + sarr[1] + "dec=" + dec);
        if (ra == null || dec == null)
            return null;
        double rad = ra / 65536f * 24f;
        double decd = dec / 65536f * 360f;
        return new RaDecRec(rad, decd);


    }

    private synchronized RaDecRec getPositionLong() {

        String ack = "";
        clearMessages();

        command("e");

        //return from scope 12AB0500,40000500#
        ack = waitForChars(timeout);
        if (ack == null)
            return null;
        String[] sarr = ack.split(",");
        if (sarr.length < 2)
            return null;
        Long ra = getLong(sarr[0]);
        Long dec = getLong(sarr[1]);
        Log.d(TAG, "ra str=" + sarr[0] + "ra=" + ra);
        Log.d(TAG, "dec str=" + sarr[1] + "dec=" + dec);
        if (ra == null || dec == null)
            return null;
        double rad = ra / 4294967296. * 24.;
        double decd = dec / 4294967296. * 360.;
        return new RaDecRec(rad, decd);


    }

    public synchronized void slewToPosition() {
        switch (type) {
            case SHORT_DATA:
                slewToPositionShort();
                break;
            case LONG_DATA:
                slewToPositionLong();
                break;
        }
        String cs = waitForChars(timeout);
        Log.d(TAG, "cs=" + cs);
        if (cs == null) {
            CommunicationManager.error(CommunicationManager.NO_RESPONSE_FROM_SCOPE);
        }


    }

    private synchronized void slewToPositionShort() {
        clearMessages();
        int dec = (int) (mDEC / 360 * 65536);
        int ra = (int) (mRA / 24. * 65536);

        String mess = "R" + String.format(Locale.US, "%04X,%04X", ra, dec);
        command(mess);
    }

    public synchronized void slewToPositionLong() {

        clearMessages();
        long dec = (long) (mDEC / 360 * 4294967296.);
        long ra = (long) (mRA / 24. * 4294967296.);

        String mess = "r" + String.format(Locale.US, "%08X,%08X", ra, dec);
        command(mess);

    }

    private void command(CharSequence s) {
        mPs.print(s);
        mPs.flush();
        Log.d(TAG, s + " sent");
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
            Log.d(TAG, "Interrupted excptn");
            return null;
        }
        Log.d(TAG, "ans " + ans);
        return ans;
    }


    /**
     * @param hex - hex string to be converted to Integer
     * @return
     */
    private Integer getInt(String hex) {
        int value = 0;
        try {
            value = Integer.parseInt(hex, 16);
        } catch (Exception e) {
            return null;
        }
        return value;
    }

    private Long getLong(String hex) {
        long value = 0;
        try {
            value = Long.parseLong(hex, 16);
        } catch (Exception e) {
            return null;
        }
        return value;
    }
}
