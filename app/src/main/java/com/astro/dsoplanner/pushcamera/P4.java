package com.astro.dsoplanner.pushcamera;

import android.content.Context;
import android.content.SharedPreferences;

import com.astro.dsoplanner.SettingsActivity;

public class P4 {
    public double x, y, z, t;

    public P4(double x, double y, double z, double t) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
        this.t = t;
    }

    @Override
    public String toString() {
        return "P4 [x=" + x + ", y=" + y + ", z=" + z + ", t=" + t + "]";
    }

    public void saveToSharedPrefs(Context context, String key) {
        SharedPreferences.Editor editor = SettingsActivity.getSharedPreferences(context).edit();
        editor.putString(key + "_x", String.valueOf(x));
        editor.putString(key + "_y", String.valueOf(y));
        editor.putString(key + "_z", String.valueOf(z));
        editor.putString(key + "_t", String.valueOf(t));
        editor.commit();
    }

    /**
     * use string representation of double for more precision
     *
     * @param context
     * @param key
     * @return
     */
    public static Double getDoubleFromSharedPrefs(Context context, String key) {
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(context);
        if (prefs.contains(key)) {
            double d;
            try {
                d = Double.parseDouble(prefs.getString(key, ""));
            } catch (Exception e) {
                return null;
            }
            return d;
        } else
            return null;
    }

    public static P4 restoreFromSharedPrefs(Context context, String key) {
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(context);

        Double x = getDoubleFromSharedPrefs(context, key + "_x");
        Double y = getDoubleFromSharedPrefs(context, key + "_y");
        Double z = getDoubleFromSharedPrefs(context, key + "_z");
        Double t = getDoubleFromSharedPrefs(context, key + "_t");
        if (x != null && y != null && z != null && t != null)
            return new P4(x, y, z, t);
        else
            return null;

    }

    public void clearSharedPrefs(Context context, String key) {
        SettingsActivity.removeSharedPreferencesKey(key + "_x", context);
        SettingsActivity.removeSharedPreferencesKey(key + "_y", context);
        SettingsActivity.removeSharedPreferencesKey(key + "_z", context);
        SettingsActivity.removeSharedPreferencesKey(key + "_t", context);
    }

}
