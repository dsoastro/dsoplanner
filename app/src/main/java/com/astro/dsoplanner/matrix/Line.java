package com.astro.dsoplanner.matrix;

import android.content.Context;
import android.content.SharedPreferences;

import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.pushcamera.P4;

public class Line {
    public double x, y, z;

    public Line() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Line(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double timesVector(DVector v) {
        return (x * v.x + y * v.y + z * v.z);
    }

    public Line minus(Line line) {
        return new Line(x - line.x, y - line.y, z - line.z);
    }

    @Override
    public String toString() {
        return String.format("%.5f", x) + " " + String.format("%.5f", y) + " " + String.format("%.5f", z);
    }

    void print() {
        System.out.println(toString());
    }

    public void save(Context context, String key) {
        SharedPreferences.Editor editor = SettingsActivity.getSharedPreferences(context).edit();
        editor.putString(key + "_x", String.valueOf(x));
        editor.putString(key + "_y", String.valueOf(y));
        editor.putString(key + "_z", String.valueOf(z));
        editor.commit();
    }

    public static Line restore(Context context, String key) {
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(context);
        Double x = P4.getDoubleFromSharedPrefs(context, key + "_x");
        Double y = P4.getDoubleFromSharedPrefs(context, key + "_y");
        Double z = P4.getDoubleFromSharedPrefs(context, key + "_z");

        if (x != null && y != null && z != null)
            return new Line(x, y, z);
        else
            return null;
    }

    public static void clearSharedPrefs(Context context, String key) {
        SettingsActivity.removeSharedPreferencesKey(key + "_x", context);
        SettingsActivity.removeSharedPreferencesKey(key + "_y", context);
        SettingsActivity.removeSharedPreferencesKey(key + "_z", context);

    }
}
