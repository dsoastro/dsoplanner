package com.astro.dsoplanner.scopedrivers;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.AstroTools.RaDecRec;

public class MeadeDriver implements TelescopeDriver {
    private static final String OBJECT_UNREACHABLE = "Object unreachable";
    private static final String CAN_T_SLEW_COMMAND_IGNORED = "Can't slew, command ignored";
    private static final String CAN_T_SET_DEC_DATA_INVALID = "Can't set Dec, data invalid";
    private static final String CAN_T_SET_DEC_COMMAND_IGNORED = "Can't set Dec, command ignored";
    private static final String CAN_T_SET_RA_DATA_INVALID = "Can't set RA, data invalid";
    private static final String CAN_T_SET_RA_COMMAND_IGNORED = "Can't set RA, command ignored";
    private static final String US_ASCII = "US-ASCII";


    private static final String TAG = MeadeDriver.class.getSimpleName();
    String messages = "";

    private PrintStream mPs = null;
    private Double mRA, mDEC;
    private boolean cancelFlag = false;

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
        return TelescopeDriver.Meade;
    }

    private String getMessage(int sz) {
        if (messages.length() >= sz) {
            String r = messages.substring(0, sz);
            messages = messages.substring(sz);
            Log.d(TAG, "got reply:" + r);
            return r;
        } else
            return null;
    }

    private String getMessage() {
        String r = messages;
        messages = "";
        return r;
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
        mRA = ra;
        mDEC = dec;
    }

    public synchronized RaDecRec getPosition() {
        final int timeout = 5000;
        String ack = null;

        clearMessages();

        command("GR");
        ack = waitForCharsFlex(8, timeout);
        if (ack == null) return null;
        mRA = AstroTools.getRaDecValue(ack); // Get Telescope RA
        //		Returns: HH:MM.T#  or  HH:MM:SS# (last one in this case)

        command("GD");
        ack = waitForCharsFlex(7, timeout);
        if (ack == null) return null;
        mDEC = AstroTools.getRaDecValue(ack); //Get Telescope Dec
        //		Returns: sDD*MM# or sDD*MM SS#
        if (mRA == null || mDEC == null) return null;
        else return new RaDecRec(mRA, mDEC);

    }

    public synchronized void slewToPosition() {
        final int timeout = 5000;
        clearMessages();

        //Meade Controllers implementation

        //Might be necessary but must be confirmed
        //:U# Toggle between low/hi precision positions
        //		Low   - RA displays and messages HH:MM.T  sDD*MM
        //		High   - Dec/Az/El displays and messages  HH:MM:SS  sDD*MM:SS
        //		Returns Nothing
        String cs = null;

        command("Q"); // Halt all current slewing Returns:Nothing


        command("Sr" + getIDeg(mRA) + ":" + getFMin(mRA));//":" + getISec(ra), true);
        cs = waitForChars(1, timeout);
        if (cs == null) CommunicationManager.error(CAN_T_SET_RA_COMMAND_IGNORED);
        else if (cs.charAt(0) == '0') CommunicationManager.error(CAN_T_SET_RA_DATA_INVALID);
        Log.d(TAG, "cs=" + cs);
        //       Set target object RA to HH:MM.T or HH:MM:SS depending on the current precision setting.
        //		 Returns:	0 Invalid;	1 Valid

        command("Sd" + getIDeg(mDEC) + "*" + getFMin(mDEC));
        cs = waitForChars(1, timeout);
        if (cs == null) CommunicationManager.error(CAN_T_SET_DEC_COMMAND_IGNORED);
        else if (cs.charAt(0) == '0') CommunicationManager.error(CAN_T_SET_DEC_DATA_INVALID);
        Log.d(TAG, "cs=" + cs);
        //       Set target object declination to sDD*MM or sDD*MM:SS depending on the current precision setting
        //		 Returns:	0 Invalid;	1 Valid
        command("MS");
        cs = waitForChars(1, timeout);
        if (cs == null) CommunicationManager.error(CAN_T_SLEW_COMMAND_IGNORED);
        else if (cs.charAt(0) != '0') CommunicationManager.error(OBJECT_UNREACHABLE);
        Log.d(TAG, "cs=" + cs);
        //		Slew to Target Object
        //		Returns:0  Slew is Possible 
        //		1<string># Object Below Horizon w/string message
        //		2<string># Object Below Higher w/string message

    }

    private void command(CharSequence s) {
        mPs.print(":" + s + "#");
        mPs.flush();
        Log.d(TAG, "[:" + s + "#] sent");
    }

    private String waitForChars(int nChars, int timeout) {
        String ans = null;
        try {
            while (getMessageCount() < nChars && !cancelFlag) {
                this.wait(timeout);
                Log.d(TAG, "nchars " + getMessageCount());
            }
        } catch (InterruptedException e) {
            Log.d(TAG, "Interrupted excptn");
            return null;
        }
        ans = getMessage(nChars);
        Log.d(TAG, "ans " + ans);
        return ans;
    }

    /**
     * this function clear messages each time as chars are flexible!
     *
     * @param nChars  min required number of chars
     * @param timeout
     * @return
     */
    private String waitForCharsFlex(int nChars, int timeout) {
        String ans = null;
        try {
            while (getMessageCount() < nChars && !cancelFlag) {
                this.wait(timeout);
                Log.d(TAG, "nchars " + getMessageCount());
            }
        } catch (InterruptedException e) {
            Log.d(TAG, "Interrupted excptn");
            return null;
        }
        ans = getMessage();
        Log.d(TAG, "ans " + ans);
        return ans;
    }


    private String getIDeg(double x) {
        return String.format("%02d", (int) x);
    }

    private String getFMin(double x) {
        if (x < 0) x = -x;
        return String.format("%02.1f", (x - (int) x) * 60.0);
    }
}
