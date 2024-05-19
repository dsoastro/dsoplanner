package com.astro.dsoplanner.matrix;

import android.content.Context;

import com.astro.dsoplanner.pushcamera.P3;

public class DVector extends Line {
    public DVector(double x, double y, double z) {
        super(x, y, z);
    }

    public DVector(Line a) {
        super(a.x, a.y, a.z);
    }

    public DVector(P3 p3) {
        super(p3.x, p3.y, p3.z);
    }

    public double timesVector(DVector v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public DVector timesValue(double d) {
        return new DVector(d * x, d * y, d * z);
    }

    public boolean isValid() {
        int xcomp = Double.valueOf(x).compareTo(Double.NaN);
        int ycomp = Double.valueOf(y).compareTo(Double.NaN);
        int zcomp = Double.valueOf(z).compareTo(Double.NaN);


        return (xcomp != 0 && ycomp != 0 && zcomp != 0);
    }

    public DVector minusVector(DVector v) {
        return new DVector(this.x - v.x, this.y - v.y, this.z - v.z);
    }

    public DVector plusVector(DVector v) {
        return new DVector(this.x + v.x, this.y + v.y, this.z + v.z);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public void normalise() {
        double mod = Math.sqrt(x * x + y * y + z * z);
        x = x / mod;
        y = y / mod;
        z = z / mod;

    }

    public double distance(DVector v) {
        return Math.sqrt((x - v.x) * (x - v.x) + (y - v.y) * (y - v.y) + (z - v.z) * (z - v.z));
    }

    public static DVector restore(Context context, String key) {
        Line a = Line.restore(context, key);
        if (a != null)
            return new DVector(a);
        else
            return null;

    }

    public static void clearSharedPrefs(Context context, String key) {
        Line.clearSharedPrefs(context, key);
    }

}
