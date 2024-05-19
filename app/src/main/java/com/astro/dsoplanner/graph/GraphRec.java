package com.astro.dsoplanner.graph;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;

import com.astro.dsoplanner.Constants;


import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.Exportable;
import com.astro.dsoplanner.base.Point;

public class GraphRec {
    private static final String G_SET_CENTERED = "gSetCentered";
    private static final String G_SELECTED_CON = "gSelectedCon";
    private static final String GMIRROR = "gmirror";
    private static final String GORANGLE = "gorangle";
    private static final String GOBJ_ID = "gobjId";
    private static final String GOBJ = "gobj";
    private static final String GC = "gc";
    private static final String GAZ_C = "gazC";
    private static final String GALT_C = "galtC";
    public static final String G_FOV = "gFOV";

    public static final int NO_CONSTELLATION_SELECTED = 0;

    int FOV; //spinner position
    double altCenter;
    double azCenter;
    Calendar c;
    Point obj;
    public int selected_con = NO_CONSTELLATION_SELECTED;
    public boolean set_centered = false;

    public GraphRec(int FOV, double azCenter, double altCenter, Calendar c, Point obj,
                    int selected_con, boolean set_centered) {
        this(FOV, azCenter, altCenter, c, obj);
        this.selected_con = selected_con;
        this.set_centered = set_centered;
    }

    public GraphRec(int FOV, double azCenter, double altCenter, Calendar c, Point obj) {
        this.FOV = FOV;
        this.altCenter = altCenter;
        this.azCenter = azCenter;
        this.c = c;
        this.obj = obj;
    }

    /**
     * create graph rec from shared prefs (where it was saved before)
     *
     * @param context
     */
    public GraphRec(Context context) {
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(context);
        int dzl = prefs.getInt(Constants.CURRENT_ZOOM_LEVEL, Constants.DEFAULT_ZOOM_LEVEL);
        FOV = prefs.getInt(G_FOV, dzl);
        altCenter = prefs.getFloat(GALT_C, 45);
        azCenter = prefs.getFloat(GAZ_C, 180);
        long cmillis = prefs.getLong(GC, 0);
        c = Calendar.getInstance();
        if (cmillis != 0) {
            c.setTimeInMillis(cmillis);
        }

        selected_con = prefs.getInt(G_SELECTED_CON, NO_CONSTELLATION_SELECTED);
        set_centered = prefs.getBoolean(G_SET_CENTERED, false);

        Exportable e = SettingsActivity.getExportableFromSharedPreference(GOBJ, context);
        if (e instanceof Point) {
            obj = (Point) e;
        }
    }

    public void save(Context context) {
        SharedPreferences.Editor editor = SettingsActivity.getSharedPreferences(context).edit();
        editor.putInt(G_FOV, FOV);
        editor.putFloat(GALT_C, (float) altCenter);
        editor.putFloat(GAZ_C, (float) azCenter);
        editor.putLong(GC, c.getTimeInMillis());
        if (obj instanceof Exportable) {
            //This is ok as contour objects are put into bundles without lists when passed between activities

            SettingsActivity.putSharedPreferences(GOBJ, obj, context);
        }
        editor.putInt(G_SELECTED_CON, selected_con);
        editor.putBoolean(G_SET_CENTERED, set_centered);
        editor.commit();
    }
}