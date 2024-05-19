package com.astro.dsoplanner.pushcamera;

import android.content.Context;
import android.content.SharedPreferences;

import com.astro.dsoplanner.SettingsActivity;

public class P {
    public double x, y;

    public P(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public P minus(P p) {
        return new P(x - p.x, y - p.y);
    }

    @Override
    public String toString() {
        return "P{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    public void saveToSharedPrefs(Context context, String key) {
        SharedPreferences.Editor editor = SettingsActivity.getSharedPreferences(context).edit();
        editor.putString(key + "_x", String.valueOf(x));
        editor.putString(key + "_y", String.valueOf(y));
        editor.commit();
    }


    public static P restoreFromSharedPrefs(Context context, String key) {

        Double x = P4.getDoubleFromSharedPrefs(context, key + "_x");
        Double y = P4.getDoubleFromSharedPrefs(context, key + "_y");

        if (x != null && y != null)
            return new P(x, y);
        else
            return null;

    }

    public void clearSharedPrefs(Context context, String key) {
        SettingsActivity.removeSharedPreferencesKey(key + "_x", context);
        SettingsActivity.removeSharedPreferencesKey(key + "_y", context);
    }
}
