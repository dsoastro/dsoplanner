package com.astro.dsoplanner.scopedrivers;

import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.graph.DscRec;
import com.astro.dsoplanner.graph.XY;
import com.astro.dsoplanner.matrix.Axis;
import com.astro.dsoplanner.matrix.DMatrix;
import com.astro.dsoplanner.matrix.DVector;
import com.astro.dsoplanner.util.Holder2;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.Locale;

public class DscDriverStub implements TelescopeDriver {
    private static final String TAG = "DscDriverStub";
    String messages = "";

    XY star1_eq = new XY(21.7364444732666, 9.875); //Enif
    XY star2_eq = new XY(0.13980555534362793, 29.090557098388672); //Alpheratz
    XY star3_eq = new XY(19.846389770507812, 8.86833381652832); //Altair

    int pos = 0;

    public synchronized void addMessage(byte[] b, int length) {
        messages += new String(b, 0, length);
    }

    private void clearMessages() {
        messages = "";
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

    public void setPosition(double ra, double dec) {

    }

    public void setOutputStream(OutputStream out) {

    }

    public void slewToPosition() {

    }

    public void cancel() {

    }

    public int getEpoch() {
        return TelescopeDriver.EPOCH_2000;
    }

    public int getDriver() {
        return TelescopeDriver.DSC;
    }

    public AstroTools.RaDecRec getPosition() {
        if (pos >= 3) {
            pos = 0;
            return null;
        }


        XY star_eq = null;
        switch (pos) {
            case 0:
                star_eq = star1_eq;
                break;
            case 1:
                star_eq = star2_eq;
                break;
            case 2:
                star_eq = star3_eq;
                break;
            default:
                star_eq = star1_eq;
        }
        pos++;

        Point p = AstroTools.precession(new Point(star_eq.x, star_eq.y), AstroTools.getDefaultTime(Global.getAppContext()));
        double lat = SettingsActivity.getLattitude();

        double lst = AstroTools.sdTime(Calendar.getInstance());
        Holder2<Double, Double> h = AstroTools.getAzAlt(lst, lat, p.ra, p.dec);
        double az = -h.x;
        double alt = h.y;
        DVector lmn = DscRec.calc_lmn(new XY(az, alt));
        DMatrix matrixX = new DMatrix(Axis.X, 0.2);
        DMatrix matrixY = new DMatrix(Axis.Y, 0.3);
        DMatrix matrixZ = new DMatrix(Axis.Z, 0.4);
        DVector lmn_ = matrixX.timesMatrix(matrixY).timesMatrix(matrixZ).timesVector(lmn);
        XY xy = DscRec.calc_az_alt(lmn_);
        alt = xy.y;
        az = -xy.x;
        String azStr = String.format(Locale.US, "%+06d", (int) (az * 4000. / 360));
        String altStr = String.format(Locale.US, "%+06d", (int) (alt * 4000. / 360));
        Log.d(TAG, "az=" + az + " alt=" + alt);
        Log.d(TAG, azStr + " " + altStr);
        return new AstroTools.RaDecRec(az, alt);
    }
}
