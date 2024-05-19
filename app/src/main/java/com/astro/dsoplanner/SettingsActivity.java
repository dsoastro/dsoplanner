package com.astro.dsoplanner;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.Manifest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.net.Uri;

import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.Comet;
import com.astro.dsoplanner.base.ContourObject;
import com.astro.dsoplanner.base.Exportable;
import com.astro.dsoplanner.base.MinorPlanet;
import com.astro.dsoplanner.base.ObjectInfo;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.database.DbManager;
import com.astro.dsoplanner.database.TelescopeDatabase;
import com.astro.dsoplanner.download.DSSdownloadable;
import com.astro.dsoplanner.graph.CuV;
import com.astro.dsoplanner.graph.GraphActivity;
import com.astro.dsoplanner.graph.GraphRec;
import com.astro.dsoplanner.graph.StarBoldness.FovMap;
import com.astro.dsoplanner.graph.UserHorizon;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListCollectionFiller;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.InfoListImpl;
import com.astro.dsoplanner.infolist.InfoListLoader;
import com.astro.dsoplanner.infolist.InfoListLoaderImp;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListSaverImp;
import com.astro.dsoplanner.infolist.ListHolder;

import java.util.UUID;

public class SettingsActivity extends ParentPreferenceActivity
        implements OnSharedPreferenceChangeListener {
    public static final String FILE = "file";
    private static final boolean DEFAULT_STAR_CHART_AUTO_UPDATE = false;
    public static final float MAX_LABEL_SCALE = 3;
    public static final float MIN_LABEL_SCALE = 0;
    public static final float DEFAULT_LABEL_SCALE = 1;

    private static final String MAG22 = "; mag2<=";
    private static final String SEP2 = "; sep<=";
    private static final String SEP = "; sep>=";
    private static final String ALT2 = "alt>";
    private static final String CON = "con=";
    private static final String VIS2 = "vis>=";
    private static final String MAG3 = "mag<=";
    private static final String A2 = "a>=";
    public static final String UGCLAYER = "ugclayer";
    public static final String NGCICLAYER = "ngciclayer";
    private static final String D_DSSZOOM = "d_dsszoom";
    private static final String INVSKY = "invsky";
    private static final String SKINFRONT = "skinfront";
    private static final String AUTONIGHTMODE = "autonightmode";
    private static final String OBJ_ID = "objId";
    private static final String OBJ = "obj";
    private static final String CENTER_OBJECT = "center_object";
    private static final String PER_FOV_FLIPPERS_ON = "per_fov_flippers_on";
    public static final String PGC = "pgc";
    public static final String UCAC4 = "ucac4";
    private static final String UCAC2 = "ucac2";
    public static final String TYCHO = "tycho";
    private static final String DEFAULT2 = "default";
    private static final String EYEPIECES_LIST = "Eyepieces list";
    private static final String USED_IF_NO_TELESCOPES_DEFINED_BY_USER = "Used if no telescopes defined by user";
    private static final String TELESCOPE_ID = "telescope_id";
    public static final String FLICK_BRIGHT = "flickBright";
    private static final String D_BATTERYWARNING = "d_batterywarning";
    private static final String TRANS_BACK = "transBack";
    private static final String ANTIALIASING = "antialiasing";
    private static final String VIS = "&(vis>";
    private static final String ALT = "(alt>";
    private static final String MAG = "&(mag<";
    private static final String M_0 = "&(m>0";
    private static final String A = "(a>";
    private static final String CUSTOM2 = "=custom";
    private static final String COMET2 = "=comet";
    private static final String DS2 = "=ds";
    private static final String STAR2 = "=star";
    private static final String PLANET2 = "=mplanet";
    private static final String SNR = "=snr";
    private static final String PN = "=pn";
    private static final String OCNEB = "=ocneb";
    private static final String OC = "=oc";
    private static final String NEB = "=neb";
    private static final String HIIRGN = "=hiirgn";
    private static final String GXYCLD = "=gxycld";
    private static final String GX = "=gx";
    private static final String GC = "=gc";
    private static final String INT_KEYBOARD = "intKeyboard";
    private static final String ADVANCED_SEARCH = "advanced_search";
    private static final String ACTIVE_SEARCH_REQUEST = "activeSearchRequest";
    private static final String D_DISMISS_WARNINGS = "d_dismissWarnings";
    private static final String BT_GOTO = "bt_goto";
    private static final String GYRO = "gyro";
    private static final String D_TR_W = "d_trW";
    private static final String D_TR_A = "d_trA";
    private static final String D_TR5 = "d_tr5";
    private static final String D_TR4 = "d_tr4";
    private static final String D_TR3 = "d_tr3";
    private static final String D_TR2 = "d_tr2";
    private static final String D_TR1 = "d_tr1";
    private static final String D_LIGHT_SENSOR_LIMIT = "d_lightSensorLimit";
    private static final String NAG_SCREEN_ON = "nagScreenOn";
    private static final String BOLDNESS_DATA_BMP = "boldnessDataBMP";
    private static final String BOLDNESS_DATA = "boldnessData";
    private static final String CHARTFLIPPER_DATA = "chartFlipperData";
    private static final String D_DSO_ZOOM = "d_dso__zoom";
    private static final String D_DSO_SCALE = "d_dso__scale";
    private static final String DSO_SHAPE = "dso__shape";
    private static final String D_AS_COREECTANGLE = "d_as_coreectangle";
    private static final String AS_NONADIR = "as_nonadir";
    private static final String D_AS_ALPHA = "d_as_alpha";
    private static final String D_AS_MAXDST = "d_as_maxdst";
    private static final String D_AS_NATTEMTS = "d_as_nattemts";
    private static final String NICE_STARS = "niceStars";
    private static final String ICOLOR_OBJECT = "icolor_Object";
    public static final String ICOLOR_USER_OBJECT = "icolor_UserObject";
    public static final String ICOLOR_TEXT = "icolor_Text";
    public static final String ICOLOR_USER_HORIZON = "icolor_User_Horizon";
    public static final String ICOLOR_TELRAD = "icolor_Telrad";
    public static final String ICOLOR_CONSTELLATIONS = "icolor_Constellations";
    public static final String ICOLOR_CROSS_MARKER = "icolor_CrossMarker";
    public static final String ICOLOR_EYEPIECES = "icolor_Eyepieces";
    public static final String ICOLOR_EP_CROSS = "icolor_EpCross";
    public static final String ICOLOR_LABELS = "icolor_Labels";
    public static final String ICOLOR_HORIZON = "icolor_Horizon";
    public static final String ICOLOR_GRID = "icolor_Grid";
    private static final String ICOLOR_DSS_IMAGE = "icolor_DSS_Image";
    public static final String COLOR_TEXT = "color_Text";
    private static final String COLOR_USER_HORIZON = "color_User_Horizon";
    private static final String COLOR_DSS_IMAGE = "color_DSS_Image";
    private static final String COLOR_TELRAD = "color_Telrad";
    private static final String COLOR_CONSTELLATIONS = "color_Constellations";
    private static final String COLOR_CROSS_MARKER = "color_CrossMarker";
    private static final String COLOR_EYEPIECES = "color_Eyepieces";
    private static final String COLOR_EP_CROSS = "color_EpCross";
    private static final String COLOR_LABELS = "color_Labels";
    private static final String COLOR_HORIZON = "color_Horizon";
    private static final String COLOR_GRID = "color_Grid";
    public static final String COLOR_USER_OBJECT = "color_UserObject";
    private static final String COLOR_OBJECT = "color_Object";
    private static final String DIMLIGHT = "dimlight";
    private static final String AUTO_ROTATION = "autoRotation";
    private static final String AUTO_SKY = "autoSky";
    private static final String D_AUTO_TIME_PERIOD = "d_autoTimePeriod";
    private static final String AUTO_TIME = "autoTime";
    private static final String DSS_CONTOUR_ON = "DSS_contour_on";
    private static final String DS_SON = "DSSon";
    private static final String D_DSSBRIGHTNESS = "d_dssbrightness";
    private static final String D_LM = "d_LM";
    private static final String CUSTOM = "CUSTOM";
    private static final String COMET = "COMET";
    private static final String DS = "DS";
    private static final String STAR = "STAR";
    private static final String PLANET = "PLANET";
    private static final String D_DISTANCE = "d_distance";
    private static final String T_OBJECT = "t_object";
    private static final String NEARBY = "nearby";
    public static final String D_MAX_MAG = "d_max_mag";
    public static final String D_DETECTION_LIMIT = "d_detection_limit";
    public static final String FILTER_PREFERENCE = "filter_preference";
    public static final String D_MIN_ALT = "d_min_alt";
    public static final String D_DIMENSION = "d_dimension";
    private static final String D_TR = "d_tr";
    public static final String OPT_NIGHT_MODE = "night_mode";
    private static final String OPT_GC = "GC";
    private static final boolean OPT_GC_DEF = true;
    private static final String OPT_Gxy = "Gxy";
    private static final boolean OPT_Gxy_DEF = true;


    private static final String OPT_GxyCld = "GxyCld";
    private static final boolean OPT_GxyCld_DEF = false;
    private static final String OPT_HIIRgn = "HIIRgn";
    private static final boolean OPT_HIIRgn_DEF = false;
    private static final String OPT_Neb = "Neb";
    private static final boolean OPT_Neb_DEF = false;
    private static final String OPT_OC = "OC";
    private static final boolean OPT_OC_DEF = false;
    private static final String OPT_OCNeb = "OCNeb";
    private static final String OPT_PN = "PN";
    private static final boolean OPT_PN_DEF = false;
    private static final String OPT_SNR = "SNR";
    private static final boolean OPT_SNR_DEF = false;
    private static final String OPT_dimension = D_DIMENSION;
    private static final String OPT_CAL_AZALT = "CalAzAlt"; //Show Az and Height values using Calibration

    static final String OPT_lattitude = "d_lattitude";
    static final String OPT_longitude = "d_longitude";

    static final String OPT_auto_location = "auto_location";
    static final String OPT_Messier = "Messier";
    static final String OPT_Caldwell = "Caldwell";
    static final String OPT_Hershell = "Hershell";
    static final String OPT_ONYX_SKIN = "onyxskin";


    private static final String TAG = SettingsActivity.class.getSimpleName();
    public static final float MIN_BRIGHTNESS = 0.04f;
    public static final float NORM_BRIGHTNESS = -1f;   // automatic brightness

    public static final int MAX_EYEPIECES = 5; //max number of eyepieces to accept

    public static boolean redrawRequired = false;//redraw of DSO selection list

    private static int niceStars = 0;//use the static variable to speed up star drawing
    public static SharedPreferences defaultprefs;

    private static boolean dso_ShowShape = true; //Show DSO real size
    private static float dso_Scale = 1.f; //Scale DSO symbol when not in real size mode
    private static double dso_Zoom = 5.f; //at which zoom to show DSO real size

    private static int uhor_Width = 5; //width of user horizon line
    private static float[] telrad = {0.5f, 2f, 4f, 0.5f, 4f, 8f, 16f}; //telrad parameters array (radiuses in degrees, slant angle, 2 more rings for QuInsight)
    private static TelescopeRecord mRec; //currently selected telescope parameters

    private static Boolean dirtyFlag = false;
    private static List<EyepiecesRecord> curEPs = new ArrayList<EyepiecesRecord>();
    private static int curEPsSize = 0;
    private static boolean mNightGuard;
    private static float mBrightness = NORM_BRIGHTNESS; //automatic by default
    private static double mDSSZoom = -1; //minimal zoom to show DSS patches -1 will trigger first read
    private static boolean mBatteryIndicatorOn;
    private static int mBatteryLow;

    private static final Object dirtySync = new Object();
    private static int dssBrightness = 0xff; //DSS tint layer brightness set up in graph

    private static long valid_until = 0;
    private static long current_time = 0;

    public static boolean isCameraPermissionValid(Context context) {
        return context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
    }

    Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int querycat = getIntent().getIntExtra(SettingsSearchActivity.QUERYCAT2, 0);

            String dbname = getIntent().getStringExtra(SettingsSearchActivity.DBNAME);
            if (dbname == null) dbname = "";
            SettingsActivity.this.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(SettingsActivity.this);
            updateMinDim();
            updateMinAlt();
            updateFilter();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.contains(D_TR))
            updateTelrad();
        if (key.equals(D_DIMENSION))
            updateMinDim();
        if (key.equals(D_MIN_ALT))
            updateMinAlt();

        if (key.equals(FILTER_PREFERENCE))
            updateFilter();
        if (key.equals(D_DETECTION_LIMIT))
            updateFilter();
        if (key.equals(D_MAX_MAG))
            updateFilter();
        if (key.equals(NEARBY))
            updateNearby();
        if (key.equals(T_OBJECT))
            updateNearby();
        if (key.equals(D_DISTANCE))
            updateNearby();
    }

    private void updateMinDim() {
        Preference ep = getPreferenceScreen().findPreference(D_DIMENSION);
        double dim = getDimension();
        ep.setSummary(String.format(Locale.US, "%.1f", dim) + (char) 39);
    }

    private void updateMinAlt() {
        Preference p = getPreferenceScreen().findPreference(D_MIN_ALT);
        double alt = getMinAlt();
        p.setSummary(String.format(Locale.US, "%.1f", alt) + '\u00B0');
    }


    private void updateNearby() {

        Preference p1 = getPreferenceScreen().findPreference(T_OBJECT);
        Preference p2 = getPreferenceScreen().findPreference(D_DISTANCE);


        boolean on = getNearbyMode();
        if (!on) {
            p1.setEnabled(false);
            p2.setEnabled(false);
        } else {
            p1.setEnabled(true);
            p2.setEnabled(true);
        }
        Point p = getNearbyObject();
        if (p != null)
            p1.setSummary(getNearbyObjectName(p));
        else
            p1.setSummary(R.string.object_not_selected);

        double dist = getNearbyDistance();
        if (dist != 0) {
            p2.setSummary(String.format(Locale.US, "%.1f", dist) + (char) 39);
        } else
            p2.setSummary("");

    }

    private static String getNearbyObjectName(Point p) {

        String s = "";
        if (p instanceof ObjectInfo)
            s = ((ObjectInfo) p).getLongName();
        if (p == null) s = "";
        return s;
    }

    private void updateFilter() {
        ListPreference ep = (ListPreference) getPreferenceScreen().findPreference(FILTER_PREFERENCE);
        Preference p1 = getPreferenceScreen().findPreference(D_DETECTION_LIMIT);
        Preference p3 = getPreferenceScreen().findPreference(D_MAX_MAG);

        String s = "";
        int filter = getFilter();
        switch (filter) {
            case 0:
                s = getString(R.string.visibility_filter_objects_with_visibility_below_detection_limit_are_rejected);
                p1.setEnabled(true);
                p3.setEnabled(false);
                break;
            case 1:
                s = getString(R.string.maximum_magnitude_filter_objects_with_magnitude_higher_than_the_maximum_set_are_rejected);
                p1.setEnabled(false);
                p3.setEnabled(true);
                break;
            case 2:
                s = getString(R.string.no_filter);
                p1.setEnabled(false);
                p3.setEnabled(false);
                break;
        }
        ep.setSummary(s);

        double dt = getDetectionLimit();
        p1.setSummary(String.format(Locale.US, "%.1f", dt));

        double mm = getMaxMag();
        p3.setSummary(String.format(Locale.US, "%.1f", mm));
    }


    public static boolean getNightMode() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(OPT_NIGHT_MODE, false);
    }

    public static boolean getDarkSkin() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(OPT_ONYX_SKIN, false);
    }

    public static boolean getNearbyMode() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(NEARBY, false);
    }

    public static Point getNearbyObject() {
        String s = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(T_OBJECT, "");
        return DetailsActivity.Search(s, Global.getAppContext());
    }

    public static double getNearbyDistance() {
        double f = 60;
        String s = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_DISTANCE, "60");
        try {
            f = Double.valueOf(s);
        } catch (NumberFormatException e) {
            Log.d(TAG, "NumberFormatException");
        }
        return f;
    }

    public static boolean getGC(Context context, int where) {
        String pref = context.getString(R.string.type_gc);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_gc2);
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(pref, true);
    }

    public static boolean getGxy(Context context, int where) {
        String pref = context.getString(R.string.type_gxy);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_gxy2);
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(pref, true);
    }

    public static boolean getGxyCld(Context context, int where) {
        String pref = context.getString(R.string.type_gxycld);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_gxycld2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }

    public static boolean getHIIRgn(Context context, int where) {
        String pref = context.getString(R.string.type_hiirgn);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_hiirgn2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }

    public static boolean getNeb(Context context, int where) {
        String pref = context.getString(R.string.type_neb);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_neb2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }

    public static boolean getOC(Context context, int where) {
        String pref = context.getString(R.string.type_oc);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_oc2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }

    public static boolean getOCNeb(Context context, int where) {
        String pref = context.getString(R.string.type_ocneb);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_ocneb2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }

    public static boolean getPN(Context context, int where) {
        String pref = context.getString(R.string.type_pn);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_pn2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }


    public static boolean getSNR(Context context, int where) {
        String pref = context.getString(R.string.type_snr);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_snr2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }

    public static boolean getMinorPlanet(Context context, int where) {
        String pref = context.getString(R.string.type_planet);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_planet2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }

    public static boolean getStar(Context context, int where) {
        String pref = context.getString(R.string.type_star);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_star2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }

    public static boolean getDS(Context context, int where) {
        String pref = context.getString(R.string.type_ds);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_ds2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }

    public static boolean getComet(Context context, int where) {
        String pref = context.getString(R.string.type_comet);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_comet2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }

    public static boolean getCustom(Context context, int where) {
        String pref = context.getString(R.string.type_custom);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_custom2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }

    public static boolean getCG(Context context, int where) {
        String pref = context.getString(R.string.type_cg);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_cg2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }

    public static boolean getDN(Context context, int where) {
        String pref = context.getString(R.string.type_dn);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_dn2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }

    public static boolean getAST(Context context, int where) {
        String pref = context.getString(R.string.type_ast);
        if (where == SEARCH_NEARBY)
            pref = context.getString(R.string.type_ast2);
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(pref, false);
    }


    public static boolean isNgcicSelected() {
        Context context = Global.getAppContext();
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getBoolean(context.getString(R.string.select_catalog_ngcic), true);

    }

    public static boolean isNgcicNearbySelected() {
        Context context = Global.getAppContext();
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getBoolean(context.getString(R.string.select_catalog_ngcic2), true);

    }

    public static boolean getMessier() {
        Context context = Global.getAppContext();
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.select_catalog_messier), true);
    }

    public static boolean getCaldwell() {
        Context context = Global.getAppContext();
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.select_catalog_caldwell), true);

    }


    public static boolean getHershell() {
        Context context = Global.getAppContext();
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.select_catalog_hershell), true);

    }

    public static boolean getHershellNearby() {
        Context context = Global.getAppContext();
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.select_catalog_hershell2), true);

    }

    //need to provide for digit input only! otherwise provide for exception handling
    public static double getDimension() {
        float f = 0;
        String s = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(OPT_dimension, "0");
        try {
            f = Float.valueOf(s);
        } catch (NumberFormatException e) {
            Log.d(TAG, "NumberFormatException");
        }
        return f;
    }

    public static double getMinSeparation(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        double sepmin = AstroTools.getDouble(sh.getString(context.getString(R.string.basic_search_min_sep), "0"), 0, 0, 10000);
        return sepmin;
    }

    public static double getMaxSeparation(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        double sepmax = AstroTools.getDouble(sh.getString(context.getString(R.string.basic_search_max_sep), "1000"), 1000, 0, 10000);
        return sepmax;
    }

    public static double getMaxMag2(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        double mag2 = AstroTools.getDouble(sh.getString(context.getString(R.string.basic_search_max_mag2), "20"), 20, 0, 30);
        return mag2;
    }


    public static boolean getAutoLoc() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(OPT_auto_location, true);
    }

    //Observer coordinates
    public static void setLocation(double lon, double lat) {
        SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).edit();
        e.putString(OPT_lattitude, String.valueOf(lat));
        e.putString(OPT_longitude, String.valueOf(lon));
        e.commit();
    }

    public static double getLattitude() {
        float lat = SettingsActivity.getSharedPreferences(Global.getAppContext()).getFloat(Constants.LATITUDE, -181);
        if (lat < -180)
            lat = 34;//la
        return lat;
    }

    public static double getLongitude() {
        float lon = SettingsActivity.getSharedPreferences(Global.getAppContext()).getFloat(Constants.LONGITUDE, -181);
        if (lon < -180)
            lon = -118;//la
        return lon;
    }

    public static double getMinAlt() {
        float f = 0;
        String s = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_MIN_ALT, "20");
        try {
            f = Float.valueOf(s);
        } catch (NumberFormatException e) {
            Log.d(TAG, "NumberFormatException");
        }
        return f;
    }

    private static double limit_mag = 6;

    public static void updateLM() {

        String s = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(Global.getAppContext().getString(R.string.d_lm), "6");

        limit_mag = AstroTools.getDouble(s, 6, 0, 8);
    }

    public static double getLM() {
        return limit_mag;
    }

    public static double getDetectionLimit() {
        float f = 0;
        String s = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_DETECTION_LIMIT, "0");

        return AstroTools.getDouble(s, 0, 0, 5);
    }

    public static int getFilter() {
        String s = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(FILTER_PREFERENCE, "0");
        return AstroTools.getInteger(s, 0, 0, 2);
    }

    public static double getMaxMag() {
        float f = 12;
        String s = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_MAX_MAG, "12");
        try {
            f = Float.valueOf(s);
        } catch (NumberFormatException e) {
            Log.d(TAG, "NumberFormatException");
        }
        return f;
    }

    public static boolean isCalibrationEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(OPT_CAL_AZALT, false);
    }

    /**
     * set the local static variable to the user value (called from CustomView)
     */
    public static void setDSSbrightness() {
        dssBrightness = (100 - AstroTools.getInteger(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_DSSBRIGHTNESS, "0"), 0, 0, 100)) * 255 / 100; //(0 - 0xff multiplier)
    }

    public static int getDSSbrightness() {
        return dssBrightness;
    }

    public static boolean isDSSon() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(DS_SON, true);
    }

    public static void setDSSon(boolean on) {
        PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).edit()
                .putBoolean(DS_SON, on).commit();
    }

    public static boolean areDSScontoursOn() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(DSS_CONTOUR_ON, true);
    }

    public static void setDSScontours(boolean status, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(DSS_CONTOUR_ON, status).commit();
    }

    //Realtiome mode
    public static boolean isAutoTimeUpdating() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(AUTO_TIME, true);
    }

    public static long getAutoTimeUpdatePeriod() {
        return AstroTools.getInteger(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_AUTO_TIME_PERIOD, "30"), 30, 1, 100000);
    }

    public static void setAutoTimeUpdating(boolean v) {
        PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).edit()
                .putBoolean(AUTO_TIME, v).commit();
    }

    //Sky direction tracking
    private static boolean is_auto_sky_on = false;

    public static boolean isAutoSkyOn() {
        return is_auto_sky_on;
    }

    public static void initAutoSkyFlag(Context context) {
        is_auto_sky_on = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.autosky), false);
    }

    public static void setAutoSkyFlag(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).
                edit().putBoolean(context.getString(R.string.autosky), value).
                commit();
        is_auto_sky_on = value;
    }

    private static boolean is_auto_rotation = false;

    public static boolean isAutoRotationOn() {
        return is_auto_rotation;
    }

    public static void initAutoRotationFlag(Context context) {
        is_auto_rotation = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.autorotation), false);
    }

    public static void setAutoRotationFlag(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).
                edit().putBoolean(context.getString(R.string.autorotation), value).
                commit();
        is_auto_rotation = value;
    }

    public static float getDimmer() {
        if (PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(DIMLIGHT, false))
            return MIN_BRIGHTNESS;
        else
            return getBrightness();
    }

    public static float getBrightness() {
        return mBrightness;
    }

    public static void updateBrightness(float v) {
        mBrightness = v;
    }

    public static boolean isFlickBrightnessEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(FLICK_BRIGHT, false);
    }

    public static int DEFAULT_COLOR_CON_BOUNDARY = 0x70d900ff;
    public static int DEFAULT_COLOR_MILKY_WAY = 0xB0fdff00;
    public static int DEFAULT_ICOLOR_CON_BOUNDARY = 0xa0ff5d00;
    public static int DEFAULT_ICOLOR_MILKY_WAY = 0xc5000000;

    //Initialize color settings with defaults
    protected static void setDefaultColors(boolean overwrite, Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext());

        setIntIfNotSet(p, COLOR_OBJECT, 0xB0f0f0ff, overwrite);
        setIntIfNotSet(p, COLOR_USER_OBJECT, 0x7000ff00, overwrite);
        setIntIfNotSet(p, COLOR_GRID, 0x4000ffff, overwrite);
        setIntIfNotSet(p, COLOR_HORIZON, 0x8000ffff, overwrite);
        setIntIfNotSet(p, COLOR_LABELS, 0x8000ffff, overwrite);
        setIntIfNotSet(p, COLOR_EYEPIECES, 0x60ff0000, overwrite);//red
        setIntIfNotSet(p, COLOR_EP_CROSS, 0x30ff0000, overwrite);//red
        setIntIfNotSet(p, COLOR_CROSS_MARKER, 0x90ff0000, overwrite);//red
        setIntIfNotSet(p, COLOR_CONSTELLATIONS, 0x7000ff00, overwrite);//green
        setIntIfNotSet(p, COLOR_TELRAD, 0x40ff4100, overwrite);//yellish red
        setIntIfNotSet(p, COLOR_USER_HORIZON, 0x8000ffff, overwrite);
        setIntIfNotSet(p, COLOR_CONSTELLATIONS, 0x7000ff00, overwrite);//green
        setIntIfNotSet(p, COLOR_TEXT, 0x80ffffff, overwrite);
        setIntIfNotSet(p, COLOR_DSS_IMAGE, 0xffffffff, overwrite);
        setIntIfNotSet(p, context.getString(R.string.color_con_boundary), DEFAULT_COLOR_CON_BOUNDARY, overwrite);
        setIntIfNotSet(p, context.getString(R.string.color_milky_way), DEFAULT_COLOR_MILKY_WAY, overwrite);


        setIntIfNotSet(p, ICOLOR_OBJECT, 0xa0ff00ff, overwrite);
        setIntIfNotSet(p, ICOLOR_USER_OBJECT, 0xa0ff00ff, overwrite);
        setIntIfNotSet(p, ICOLOR_GRID, 0xc033ccff, overwrite);
        setIntIfNotSet(p, ICOLOR_HORIZON, 0xc00000ff, overwrite);
        setIntIfNotSet(p, ICOLOR_LABELS, 0xd0f0c000, overwrite);
        setIntIfNotSet(p, ICOLOR_EYEPIECES, 0xa0df0000, overwrite);//red
        setIntIfNotSet(p, ICOLOR_EP_CROSS, 0x30ff0000, overwrite);//red
        setIntIfNotSet(p, ICOLOR_CROSS_MARKER, 0xa0ff0000, overwrite);//red
        setIntIfNotSet(p, ICOLOR_CONSTELLATIONS, 0xa0008f00, overwrite);//green
        setIntIfNotSet(p, ICOLOR_TELRAD, 0x90ffb000, overwrite);//yellish red
        setIntIfNotSet(p, ICOLOR_USER_HORIZON, 0xc00090ff, overwrite);
        setIntIfNotSet(p, ICOLOR_CONSTELLATIONS, 0xa0ff00ff, overwrite);//green
        setIntIfNotSet(p, ICOLOR_TEXT, 0xd0ff0000, overwrite);
        setIntIfNotSet(p, ICOLOR_DSS_IMAGE, 0xffffffff, overwrite);
        setIntIfNotSet(p, "i" + context.getString(R.string.color_con_boundary), DEFAULT_ICOLOR_CON_BOUNDARY, overwrite);
        setIntIfNotSet(p, "i" + context.getString(R.string.color_milky_way), DEFAULT_ICOLOR_MILKY_WAY, overwrite);


    }

    public static void setDefaultRedModeIntensity(Context context) {
        class Rec {
            int id;
            int def;

            public Rec(int id, int def) {
                super();
                this.id = id;
                this.def = def;
            }

        }

        Rec[] values = {
                new Rec(R.string.ncolor_star, CuV.N_STAR_DEF_INTENSITY),
                new Rec(R.string.ncolor_planet, CuV.N_PLANET_DEF_INTENSITY),
                new Rec(R.string.ncolor_object, CuV.N_OBJECT_DEF_INTENSITY),
                new Rec(R.string.ncolor_grid, CuV.N_GRID_DEF_INTENSITY),
                new Rec(R.string.ncolor_horizon, CuV.N_HOR_DEF_INTENSITY),
                new Rec(R.string.ncolor_labels, CuV.N_LABELS_DEF_INTENSITY),
                new Rec(R.string.ncolor_eyepieces, CuV.N_EP_DEF_INTENSITY),
                new Rec(R.string.ncolor_crossmarker, CuV.N_CROSS_MARKER_DEF_INTENSITY),
                new Rec(R.string.ncolor_constellations, CuV.N_CONSTELLATIONS_DEF_INTENSITY),
                new Rec(R.string.ncolor_telrad, CuV.N_TELRAD_DEF_INTENSITY),
                new Rec(R.string.ncolor_user_horizon, CuV.N_UHOR_DEF_INTENSITY),
                new Rec(R.string.ncolor_con_boundary, CuV.N_CON_BOUNDARY_DEF_INTENSITY),
                new Rec(R.string.ncolor_milky_way, CuV.N_MILKY_WAY_DEF_INTENSITY),
                new Rec(R.string.ncolor_ep_cross, CuV.N_EP_CROSS_DEF_INTENSITY),
                new Rec(R.string.ncolor_eyepieces, CuV.N_EYEPIECES_DEF_INTENSITY)};

        Editor p = PreferenceManager.getDefaultSharedPreferences(context).edit();

        for (Rec rec : values) {
            p.putString(context.getString(rec.id), "" + rec.def);
        }
        p.commit();
    }

    private static void setIntIfNotSet(SharedPreferences p, String key, int v, boolean overwrite) {
        if (!p.contains(key) || overwrite) {
            SharedPreferences.Editor e = p.edit();
            e.putInt(key, v);
            e.commit();
        }
    }


    /**
     * updating static variable
     */
    public static void setStarMode() {
        niceStars = 0;
    }

    public static int getStarMode() {
        return niceStars;
    }

    //----------------------------------------------------------------------
    //Switches the skin from day to night,
    //  process settings for Dimmer and fullscreen mode
    public static boolean setDayNightSky(Activity t) {
        //Global.context = t;

        boolean nightmode = getNightMode();
        if (nightmode) {
            t.setTheme(R.style.SkinSky_Night);
        } else {
            t.setTheme(R.style.SkinSky_Day);
        }
        if (isFullScreen(t)) {
            t.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            t.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        //Screen Dimmer
        WindowManager.LayoutParams lp = t.getWindow().getAttributes();
        lp.screenBrightness = SettingsActivity.getDimmer();
        t.getWindow().setAttributes(lp);

        return nightmode;
    }

    private static boolean isDayFullScreen(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(context.getString(R.string.full_screen_on), false);
    }

    private static boolean isNightFullScreen(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(context.getString(R.string.full_screen_on_night), true);
    }

    /**
     * No activity title
     *
     * @return
     */
    public static boolean isFullScreen(Context context) {
        boolean f;
        if (getNightMode()) {
            f = isNightFullScreen(context);
        } else {
            f = isDayFullScreen(context);
        }
        return f;
    }

    /**
     * Fix lists background in Preferences
     *
     * @param t
     */
    public static void fixListBackground(Activity t) {
        if (getNightMode() || getDarkSkin()) {
            View v = t.findViewById(android.R.id.list);
            if (v != null) v.setBackgroundColor(0xff000000);
        }
    }


    public static void hideNavBarForView(final View decorView) {
        if (SettingsActivity.isHideNavBarSupported()) {
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {

                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_IMMERSIVE
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
                    }
                }
            });
            decorView.setSystemUiVisibility(

                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);

        }

    }

    private static void hideNavBarForActivity(Activity t) {
        if (SettingsActivity.isHideNavBarSupported()) {
            t.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            //should be here as well, full screen not working otherwise

            View decorView = t.getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE


                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);

        }

    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI(Activity t) {
        View decorView = t.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    public static void changeNavBarBackground(Window window, Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
            window.setFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS, WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setNavigationBarColor(context.getResources().getColor(R.color.black));
        }

    }

    public static boolean isHideNavBarSupported() {
        return ((Build.VERSION.SDK_INT >= 24)); // 7.0
    }

    /**
     * Set the GUI theme
     *
     * @param t deprecated as not used
     * @return
     */
    public static boolean setDayNightList(Activity t) {

        boolean nightmode = getNightMode();
        boolean hideNavBar = isHideNavBar(t);
        Log.d(TAG, "nightmode=" + nightmode);
        if (nightmode) {
            t.setTheme(R.style.SkinList_Night);
            boolean no_status_bar = isNightFullScreen(t);
            if (no_status_bar)
                t.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            if (!hideNavBar)
                changeNavBarBackground(t.getWindow(), t.getApplicationContext());

        }
        //DAY
        else if (getDarkSkin()) {
            t.setTheme(R.style.SkinList_Onyx);
            if (isDayFullScreen(t)) {
                t.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            if (!hideNavBar)
                changeNavBarBackground(t.getWindow(), t.getApplicationContext());


        } else { //light skin
            t.setTheme(R.style.SkinList_Day);
            if (isDayFullScreen(t)) {
                t.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
        if (hideNavBar) {
            hideNavBarForActivity(t);
        }
        //SAND: Dimmer
        WindowManager.LayoutParams lp = t.getWindow().getAttributes();
        lp.screenBrightness = SettingsActivity.getDimmer();
        t.getWindow().setAttributes(lp);

        SettingsActivity.nightGuardReset();

        return nightmode;
    }

    /**
     * Figure if the device is a tablet
     *
     * @return
     */
    public static boolean isTablet() {
        try {
            // Compute screen size
            DisplayMetrics dm = Global.getAppContext().getResources().getDisplayMetrics();
            float screenWidth = dm.widthPixels / dm.xdpi;
            float screenHeight = dm.heightPixels / dm.ydpi;
            double size = Math.sqrt(Math.pow(screenWidth, 2) +
                    Math.pow(screenHeight, 2));
            // Tablet devices should have a screen size greater than 6 inches
            return size >= 6;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to compute screen size");
            return false;
        }

    }

    public static int getASnattempts() {
        return getInt(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_AS_NATTEMTS, "3"), 3, 1, 10);
    }

    public static double getASmaxdst() {
        return getDouble(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_AS_MAXDST, "0.2"), 0.2, 0.1, 0.5);
    }

    public static double getASalpha() {
        return getDouble(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_AS_ALPHA, "0.1"), 0.1, 0.01, 1);
    }

    public static boolean getANadir() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(AS_NONADIR, true);
    }

    public static double getAScorangle() {
        return getDouble(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_AS_COREECTANGLE, "0.0"), 0.0, 0., 360.);
    }

    private static float dso_min_dim_scale_factor;

    public static void setDSOsettings() {
        dso_ShowShape = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(DSO_SHAPE, true);
        dso_Scale = getFloat(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_DSO_SCALE, "1.0"), 1, 0.1f, 10.f);
        dso_Zoom = getFloat(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_DSO_ZOOM, "90"), 5, 0.01f, 100f);
        dso_min_dim_scale_factor = getFloat(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(Global.getAppContext().getString(R.string.d_dso__dim), "1"), 1, 0f, 100f);

    }

    public static boolean dso_ShowShape() {
        return dso_ShowShape;
    }

    public static float dso_Scale() {
        return dso_Scale;
    }

    public static double dso_GetMinZoom() {
        return dso_Zoom;
    }

    public static float dso_GetDimScaleFactor() {
        return dso_min_dim_scale_factor;
    }

    public static int getInt(String s, int def, int min, int max) {
        int i = def;
        try {
            i = Integer.parseInt(s);
        } catch (Exception e) {
        }
        if (i < min) return min;
        if (i > max) return max;
        return i;
    }

    public static long getLong(String s, long def, long min, long max) {
        long i = def;
        try {
            i = Long.parseLong(s);
        } catch (Exception e) {
        }
        if (i < min) return min;
        if (i > max) return max;
        return i;
    }

    public static float getFloat(String s, float def, float min, float max) {
        float f = def;
        try {
            f = Float.parseFloat(s);
        } catch (Exception e) {
        }
        if (f < min) return min;
        if (f > max) return max;
        return f;
    }

    public static double getDouble(String s, double def, double min, double max) {
        double d = def;
        try {
            d = Double.parseDouble(s);
        } catch (Exception e) {
        }
        if (d < min) return min;
        if (d > max) return max;
        return d;
    }

    public static void saveBoldnessData(ArrayList<FovMap> fovmap) {
        String propName = BOLDNESS_DATA;
        if (getStarMode() == 1) propName = BOLDNESS_DATA_BMP;
        //make the long string
        String format = "%f %f %f;";
        StringBuilder s = new StringBuilder();
        for (FovMap i : fovmap) {
            s.append(String.format(Locale.US, format, i.fov, i.minm, i.maxm));
        }
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).edit();
        editor.putString(propName, s.toString());
        editor.commit();
        Log.d(TAG, "boldness=" + s);
    }

    public static String getBoldnessData() {
        String propName = BOLDNESS_DATA;
        if (getStarMode() == 1) propName = BOLDNESS_DATA_BMP;
        String bs = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(propName, "");
        if (bs.isEmpty()) return null;
        return bs;
    }

    public static void saveChartFlipperData(String s) {
        String propName = CHARTFLIPPER_DATA;
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(Global.getAppContext()).edit();
        editor.putString(propName, s);
        editor.commit();
        Log.d(TAG, "chartFlipper=" + s);
    }

    public static String getChartFlipperData() {
        String propName = CHARTFLIPPER_DATA;
        String bs = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(propName, "");
        if (bs.equals("")) return null;
        return bs;
    }

    public static boolean getNagScreenOn() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(NAG_SCREEN_ON, true);
    }


    public static void forceNightMode(boolean b) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).edit();
        editor.putBoolean(OPT_NIGHT_MODE, b);
        editor.commit();
    }

    //User Horizon
    public static void setUHor() {
        UserHorizon.init(Global.getAppContext());
        setUHorWidth();
    }

    public static String getUHorFile() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(Global.getAppContext().getString(R.string.t_uhor_name), "");
    }

    public static boolean getUHorFill() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(Global.getAppContext().getString(R.string.uhorfill), false);
    }

    public static void setUHorUri(String uri) {
        PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).edit().putString(Global.getAppContext().getString(R.string.t_uhor_name), uri).commit();
    }

    public static float getUHorStep() {
        return getFloat(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString("d_uhor__step", "5"), 5, 1, 45);
    }

    public static int getUHorWidth() {
        return uhor_Width;
    }

    public static void setUHorWidth() {
        int i = AstroTools.getInteger(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString("d_uhor__width", "5"), 5, 1, 30);
        uhor_Width = i;
    }

    //Telrad settings are in the static array
    public static void updateTelrad() {
        telrad[0] = getFloat(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_TR1, "0.5"), 0.5f, 0.01f, 60f);
        telrad[1] = getFloat(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_TR2, "2"), 2f, 0.01f, 60f);
        telrad[2] = getFloat(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_TR3, "4"), 4f, 0.01f, 60f);
        telrad[3] = getFloat(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_TR_A, "0.5"), 0f, -360f, 360f);
        telrad[4] = getFloat(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_TR_W, "4"), 4f, 0f, 15f);
        telrad[5] = getFloat(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_TR4, "8"), 8f, 0.01f, 60f);
        telrad[6] = getFloat(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_TR5, "16"), 16f, 0.01f, 60f);
    }


    public static float getTr(int i) {
        return telrad[i];
    }

    public static boolean getGyroPreference() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(GYRO, false);
    }


    public static boolean isGoToEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(BT_GOTO, false);
    }

    public static int DismissWarningsDelay() {
        return AstroTools.getInteger(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_DISMISS_WARNINGS, "3000"), 3000, 1, 100000);
    }

    public static SearchRequestItem getSearchRequestItem() {
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(Global.getAppContext());//Global.getAppContext().getSharedPreferences(com.astro.dsoplanner.Constants.PREFS,Context.MODE_PRIVATE);

        int asr = prefs.getInt(Constants.SRA_ACTIVE_SEARCH_REQUEST, -1);
        Log.d(TAG, "asr=" + asr);
        if (asr > -1) {
            InfoList iL = ListHolder.getListHolder().get(InfoList.SREQUEST_LIST);
            SearchRequestItem item = (SearchRequestItem) iL.get(asr);
            return item;
        }
        return null;
    }


    public static String getBasicSearchNameStartWith(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.basic_search_name_start), "");
    }

    public static boolean getNoRedKeyboard() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(INT_KEYBOARD, false);
    }

    public static String getBasicSearchConSummary(Context context) {
        String checkstr = SettingsActivity.getStringFromSharedPreferences(context, Constants.LIST_SELECTOR_CHECKS, "");
        String summary = "";
        if ("".equals(checkstr)) {
            summary = context.getString(R.string.all_constellations);
        } else {
            boolean[] checks = SettingsActivity.retrieveBooleanArrFromString(checkstr);

            for (int i = 0; i < checks.length; i++) {
                if (checks[i]) {
                    summary = summary + Constants.constellations[i + 1] + ", ";
                }
            }
            if (!"".equals(summary))
                summary = summary.substring(0, summary.length() - 2);
            else
                summary = context.getString(R.string.all_constellations);


        }
        return summary;
    }

    /**
     * @param context
     * @param catalogs - selected catalogs for depicting double star info
     * @return
     */
    public static String createSearchQuery(Context context, List<Integer> catalogs) {
        double f = SettingsActivity.getDimension();

        String s = A2 + String.format(Locale.US, "%.1f", f) + "; ";
        int filter = SettingsActivity.getFilter();
        switch (filter) {
            case 1:
                double maxmag = SettingsActivity.getMaxMag();
                s = s + MAG3 + String.format(Locale.US, "%.1f", maxmag) + "; ";
                break;
            case 0:
                double DL = SettingsActivity.getDetectionLimit();
                s = s + VIS2 + String.format(Locale.US, "%.1f", DL) + "; ";
                break;
        }

        String cons = SettingsActivity.getBasicSearchConSummary(context);
        if (!cons.equals(context.getString(R.string.all_constellations))) {
            s = s + CON + cons + "; ";
        }


        double minAlt = SettingsActivity.getMinAlt();
        s = s + ALT2 + String.format(Locale.US, "%.1f", minAlt);

        String start_with = SettingsActivity.getBasicSearchNameStartWith(context);
        if (!"".equals(start_with)) {
            s = s + ";" + start_with + "*";
        }
        boolean double_star = false;
        for (int cat : catalogs) {
            if (cat == AstroCatalog.HAAS || cat == AstroCatalog.WDS) {
                double_star = true;
                break;
            }
        }

        if (double_star) {
            double sepmin = SettingsActivity.getMinSeparation(context);
            double sepmax = SettingsActivity.getMaxSeparation(context);
            double mag2 = SettingsActivity.getMaxMag2(context);


            s = s + SEP + String.format(Locale.US, "%.1f", sepmin) + SEP2 + String.format(Locale.US, "%.1f", sepmax) +
                    MAG22 + String.format(Locale.US, "%.1f", mag2);
        }
        return s;

    }


    public static String createLocalSearchQuery() {
        int cat = -1;
        int filter = SettingsActivity.getFilter();


        double minAlt = SettingsActivity.getMinAlt();
        String s1 = ALT + String.format(Locale.US, "%.1f", minAlt) + ")";

        switch (filter) {
            case 0:
                if (cat != AstroCatalog.COMET_CATALOG) {
                    double DL = SettingsActivity.getDetectionLimit();
                    s1 = s1 + VIS + String.format(Locale.US, "%.1f", DL) + ")";
                }
                break;
        }
        return s1;
    }

    public static void setAntialiasing() {
        Global.antialiasing = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(ANTIALIASING, true);
    }

    public static boolean getTrans() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(TRANS_BACK, false);
    }

    //Revert the current value of a boolean checkbox
    public static void toggle(String name) {
        //get
        boolean v = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(name, false);
        //set invert
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).edit();
        editor.putBoolean(name, !v);
        editor.commit();
    }

    public static boolean isEpOn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.epson), true);
    }

    public static boolean isQuinsightOn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.quinsighton), false);
    }

    //Warning message for launching not red functions
    // Will return true if launced for the first time, false for the second time
    // Use nightGuardReset() at the beginning of affected aps
    public static boolean nightGuard(Context context) {
        if (getNightMode()) {
            if (!mNightGuard) { //first time here
                mNightGuard = true;
                InputDialog.message(context, R.string.nightmode_warning_click_again, 0).show();
                return true; //prevent going through
            }
        }
        return false; //allow to go through
    }

    public static void nightGuardReset() {
        mNightGuard = false;
    }

    public static int getBatteryLow() {
        return mBatteryLow;
    }

    public static void setBatteryLow() {
        mBatteryLow = AstroTools.getInteger(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(D_BATTERYWARNING, "20"), 20, 0, 100);
    }


    //Telescope -----------------------------------------------------------------------------
    //
    //Init the telescope data at the start of the app
    public static void initTelescope(Context context) {
        //load telescopes list
        class Filler implements InfoListFiller {
            List<TelescopeRecord> list = new ArrayList<TelescopeRecord>();

            public Filler() {
                TelescopeDatabase db = new TelescopeDatabase();
                db.open();
                list = db.search();
                db.close();
            }

            public Iterator<TelescopeRecord> getIterator() {
                return list.iterator();
            }
        }

        //retrieving database data
        InfoList list = ListHolder.getListHolder().get(InfoList.TELESCOPE_LIST);
        list.removeAll();
        list.fill(new Filler());

        //init selected telescope
        int scope_id = -1;
        String s = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(TELESCOPE_ID, "e");
        if (s.contentEquals("e")) //no such setting defined yet
            scope_id = -1;
        else
            scope_id = AstroTools.getInteger(s, -1, -1, 1000);

        TelescopeRecord r, r0;

        if (list.getCount() > 0) { //list of telescopes is not empty, search for scope id
            Iterator<TelescopeRecord> it = list.iterator();
            r0 = it.next(); //save first item
            if (scope_id != -1) { //some scope id is in the prefs, locate it
                for (r = r0; r.id != scope_id && it.hasNext(); r = it.next()) ;
                if (r != null && r.id != scope_id) //not found
                    r = r0; //use first in the list
            } else //user did not select any scope yet, select the first one automatically
                r = r0;
        } else //list is empty, use default scope
            r = new TelescopeRecord(-1, context.getString(R.string.default2), 180, 1800, 75., -1, USED_IF_NO_TELESCOPES_DEFINED_BY_USER);

        setCurrentTelescope(r);
    }


    /**
     * Set current telescope record data after selection
     *
     * @param rec
     */
    public static void setCurrentTelescope(TelescopeRecord rec) {
        mRec = new TelescopeRecord(rec.getBundle());

        updateEyepieces();

        //Save id to the prefs
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).edit();
        editor.putString(TELESCOPE_ID, "" + mRec.id);
        editor.commit();
    }

    public static TelescopeRecord getCurrentTelescope() {
        return mRec;
    }

    //PRIVATE: Get attached eyepieces into Settings.curEPs
    public static void updateEyepieces() {
        class Filler implements InfoListFiller {
            List<EyepiecesRecord> list = new ArrayList<EyepiecesRecord>();

            public Filler() {
                EyepiecesDatabase db = new EyepiecesDatabase();
                db.open();
                list = db.search();
                db.close();
            }

            public Iterator<EyepiecesRecord> getIterator() {
                return list.iterator();
            }
        }

        InfoList list = new InfoListImpl(EYEPIECES_LIST, EyepiecesRecord.class);
        list.removeAll();
        list.fill(new Filler());

        //resetting the local selected EPs cache
        curEPs.clear();
        Iterator<EyepiecesRecord> it = list.iterator();
        EyepiecesRecord r, r0 = null;
        while (it.hasNext()) {
            r = it.next();
            if (r0 == null) r0 = r; //save the first one
            for (String s : r.getEp_id().split("[,]")) {
                if (s.length() == 0) break;
                if (AstroTools.getInteger(s, -2, -2, 10000) == mRec.ep_id) { //-2 - impossible number
                    curEPs.add(r); //save to list of current eps
                }
            }
        }
        curEPsSize = curEPs.size();

        if (curEPsSize == 0) {//no yeiepieces assigned to this telescope
            if (r0 != null) //some EPs are in the database, use the first one
                curEPs.add(r0);
            else //no EP list at all
                curEPs.add(new EyepiecesRecord(-1, DEFAULT2, 26., 60., "" + mRec.ep_id, ""));
            curEPsSize = 1;
        }

    }

    public static int getEPsNumber() {
        return curEPsSize;
    }

    public static double getEpFocus(int i) {
        double f = 33.0 / (i + 1.0);
        if (curEPsSize > i)
            f = curEPs.get(i).getFocus();
        return f;
    }

    /**
     * checks for CCD as CCD is saved with large focus
     *
     * @param focus
     * @return
     */
    public static boolean isCCD(double focus) {
        return (focus > EyepiecesListActivity.OFFSET / 2);
    }

    public static double getEpFOV(int i) {
        double d = 1.0 / (i + 1.0);
        if (curEPsSize > i) {
            EyepiecesRecord ep = curEPs.get(i);
            d = ep.getAfov() * ep.getFocus() / mRec.focus;
        }
        return d;
    }

    public static EyepiecesRecord getEpRecord(int i) {
        if (i < curEPs.size())
            return curEPs.get(i);
        else
            return null;
    }

    public static String getTName() {
        return mRec.name;
    }

    public static double getTFocus() {
        return mRec.focus;
    }

    public static double getTAperture() {
        return mRec.aperture;
    }

    public static double getTPassthr() {
        return mRec.pass;
    }

    public static int getTId() {
        return mRec.id;
    }

    public static int getTEPid() { //no scope selected, default used
        return mRec.ep_id;
    }

    public static boolean getTychoStatus() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).
                getBoolean(TYCHO, true);
    }

    private static boolean visibleStatus = false;

    /**
     * show visible status in settings/layer options
     */
    public static boolean showVisibleStatus() {
        return visibleStatus;
    }

    /**
     * show visible status in settings/layer options
     */
    public static void updateVisibleStatus() {
        visibleStatus = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).
                getBoolean(Global.getAppContext().getString(R.string.layer_vis), false);

    }


    private static double visibleThreshold = 0;

    public static double getLayerVisibilityThreshold() {
        return visibleThreshold;
    }

    public static void updateLayerVisibilityThreshold() {
        String s = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).getString(Global.getAppContext().getString(R.string.layer_vis_threshold), "1");
        Log.d(TAG, "s=" + s);
        visibleThreshold = AstroTools.getDouble(s, 0, 0, 5);
        Log.d(TAG, "vis_thresh=" + visibleThreshold);
    }

    public static boolean getUcac2Status() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).
                getBoolean(UCAC2, false);
    }

    public static boolean getUcac4Status() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).
                getBoolean(UCAC4, true);
    }

    public static boolean getPgcStatus() {
        Context context = Global.getAppContext();
        return PreferenceManager.getDefaultSharedPreferences(context).
                getBoolean(context.getString(R.string.pgclayer), true);
    }

    public static boolean getUgcStatus() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).
                getBoolean(UGCLAYER, true);
    }

    public static boolean getNgcIcStatus() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).
                getBoolean(NGCICLAYER, true);
    }

    public static boolean getCenterObjectStatus() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).
                getBoolean(CENTER_OBJECT, false);
    }

    public static boolean getIsPerFovFlipperOn() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).
                getBoolean(PER_FOV_FLIPPERS_ON, false);
    }


    /**
     * @param context
     * @return shared preferences used by the application
     */
    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
    }


    public static Map<String, ?> getAllSharedPreferences(Context context) {
        return context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE).getAll();
    }

    /**
     * use this as String is put into sh prefs in hex format
     *
     * @param context
     * @param key
     * @param defValue
     * @return
     */
    public static String getStringFromSharedPreferences(Context context, String key, String defValue) {
        SharedPreferences sh = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        String s = sh.getString(key, defValue);
        if (defValue == null || defValue.equals(s))
            return defValue;
        return getStringFromHexRepresentation(s);
    }

    public static boolean getBooleanFromSharedPreferences(Context context, String key, boolean defValue) {
        SharedPreferences sh = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        return sh.getBoolean(key, defValue);


    }

    /**
     * Saves application shared preferences of Integer, String, Boolean, Float, Double, Long, AstroObject
     * for String retrieval use getStringFromSharedPreferences!!!
     */
    public static void putSharedPreferences(String key, Object value, Context context) {
        SharedPreferences.Editor sh = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE).edit();
        if (value instanceof Integer) {
            sh.putInt(key, (Integer) value).commit();
        } else if (value instanceof String) {

            sh.putString(key, getHexStringRepresentation((String) value)).commit();
        } else if (value instanceof Boolean) {
            sh.putBoolean(key, (Boolean) value).commit();
        } else if (value instanceof Float) {
            sh.putFloat(key, (Float) value).commit();
        } else if (value instanceof Double) {
            Double d = (Double) value;
            sh.putFloat(key, d.floatValue()).commit();
        } else if (value instanceof Long) {
            sh.putLong(key, (Long) value).commit();
            Log.d(TAG, "long put");
        } else if (value instanceof Exportable) {

            byte[] arr;
            if (value instanceof ContourObject) {
                arr = ((ContourObject) value).getShortByteRepresentation();
            } else
                arr = ((Exportable) value).getByteRepresentation();
            StringBuilder sb = new StringBuilder();
            for (byte b : arr) {
                sb.append((char) b);
            }

            ShItem sh1 = new ShItem(key + OBJ, arr);
            ShItem sh2 = new ShItem(key + OBJ_ID, ((Exportable) value).getClassTypeId());
            putShPref(sh1);
            putShPref(sh2);
            new Prefs(context).saveList(InfoList.PREFERENCE_LIST);

        }

        sh = null;

    }

    public static void removeSharedPreferencesKey(String key, Context context) {
        SharedPreferences.Editor sh = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE).edit();
        sh.remove(key).commit();
    }

    public static String getHexStringRepresentation(String s) {
        byte[] arr = s.getBytes();

        String result = "";
        for (byte b : arr) {
            int b1 = b;
            if (b < 0) b1 = b + 256;
            String r = Integer.toHexString(b1);
            if (r.length() == 1) r = "0" + r;
            result += r;
        }
        return result;
    }

    public static String getStringFromHexRepresentation(String s) {
        try {
            char[] arr = s.toCharArray();
            String result = "";
            String num = "";

            byte[] buf = new byte[arr.length / 2];
            boolean first = true;
            int i = 0;
            for (char c : arr) {
                if (first) {
                    num = "" + c;
                    first = false;
                } else {
                    num += c;
                    buf[i++] = (byte) Integer.parseInt(num, 16);
                    first = true;

                }
            }
            return new String(buf);
        } catch (Exception e) {
            return "";
        }
    }

    public static String convertBooleanArrToString(boolean[] a) {
        String s = "";
        for (int i = 0; i < a.length; i++) {
            if (a[i])
                s = s + "1";
            else
                s = s + "0";
        }
        return s;
    }

    public static boolean[] retrieveBooleanArrFromString(String s) {
        boolean[] a = new boolean[s.length()];

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '1')
                a[i] = true;
            else
                a[i] = false;
        }
        return a;
    }

    private static void putShPref(ShItem sh) {
        InfoList ilist = ListHolder.getListHolder().get(InfoList.PREFERENCE_LIST);
        boolean replaced = false;
        for (int i = 0; i < ilist.getCount(); i++) {
            ShItem item = (ShItem) ilist.get(i);
            if (item.name.equals(sh.name)) {//replace
                InfoListImpl ilistimpl = (InfoListImpl) ilist;
                ilistimpl.set(i, sh);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            //need to add
            InfoListCollectionFiller filler = new InfoListCollectionFiller(Arrays.asList(new Object[]{sh}));
            ilist.fill(filler);
        }

    }

    private static ShItem getShPref(String name) {
        InfoList ilist = ListHolder.getListHolder().get(InfoList.PREFERENCE_LIST);
        for (Object o : ilist) {
            ShItem item = (ShItem) o;
            if (item.name.equals(name)) {
                return item;
            }
        }
        return null;
    }

    public static AstroObject getObjectFromSharedPreferences(String key, Context context) {
        SharedPreferences sh = SettingsActivity.getSharedPreferences(context);
        String objStr = sh.getString(key + OBJ, "");
        if (!"".equals(objStr) && objStr != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (char c : objStr.toCharArray()) {
                out.write((byte) c);
            }
            byte[] arr = out.toByteArray();
            int classTypeId = sh.getInt(key + OBJ_ID, 0);
            if (classTypeId != 0) {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(arr));
                Object obj = ObjectInflater.getInflater().inflate(classTypeId, in);
                if (obj instanceof AstroObject)
                    return (AstroObject) obj;
            }

        }
        return null;
    }

    public static AstroObject getObjectFromSharedPreferencesNew(String key, Context context) {
        Exportable e = getExportableFromSharedPreference(key, context);
        if (e instanceof AstroObject)
            return (AstroObject) e;
        else
            return null;
    }

    public static Exportable getExportableFromSharedPreference(String key, Context context) {
        ShItem item = getShPref(key + OBJ);
        if (item == null) return null;
        if (item.type != ShItem.BYTE) return null;
        if (item.bytevalue == null) return null;

        ShItem item1 = getShPref(key + OBJ_ID);
        if (item1 == null) return null;

        int classTypeId = 0;
        if (item1.type == ShItem.INT)
            classTypeId = item1.ivalue;

        if (classTypeId != 0) {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(item.bytevalue));
            Object obj = ObjectInflater.getInflater().inflate(classTypeId, in);
            if (obj instanceof Exportable)
                return (Exportable) obj;
        }


        return null;
    }

    /**
     * use it for short lists only!
     *
     * @param key
     * @param list
     * @param context
     */
    public static void putInfoListIntoDefSharedPrefs(String key, InfoList list, Context context) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InfoListSaver saver = new InfoListSaverImp(out);
        boolean result = list.save(saver);
        StringBuilder sb = new StringBuilder();
        //String s="";
        for (byte b : out.toByteArray()) {
            sb.append((char) b);
        }
        PreferenceManager.getDefaultSharedPreferences(context).
                edit().putString(key, sb.toString()).commit();


    }


    public static void getInfoListFromDefSharedPrefs(String key, InfoList list, Context context) {
        String listStr = PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
        if ("".equals(listStr))
            return;
        Log.d(TAG, "listStr=" + listStr + "str length=" + listStr.length());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (char c : listStr.toCharArray()) {
            out.write((byte) c);
        }
        byte[] arr = out.toByteArray();
        InfoListLoader loader = new InfoListLoaderImp(new ByteArrayInputStream(arr));
        list.load(loader);
    }

    public static boolean isAutoNightModeOn() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(AUTONIGHTMODE, false);
    }

    public static boolean isHideNavBar(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.hide_nav_bar), false);
    }

    //SKINS --------------------------------------------------------------------
    public static boolean getSkinMap() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(SKINFRONT, false);
    }

    public static boolean getInverseSky() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(INVSKY, false);
    }

    public static double getDSSZoom() {
        return mDSSZoom;
    }

    public static void setDSSZoom() {
        mDSSZoom = AstroTools.getDouble(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).getString(D_DSSZOOM, "5.0"), 5.0, 0, 30);

    }


    public static synchronized void setCurrentTime(long time) {
        current_time = time;
    }


    static long ucac4_offset = -1;

    public static synchronized long getUcac4Offset() {
        if (ucac4_offset == -1) {
            ucac4_offset = SettingsActivity.getSharedPreferences(Global.getAppContext()).
                    getLong(Constants.UCAC4_START_POS, -1);
        }
        return ucac4_offset;
    }

    static long pgc_offset = -1;

    /**
     * offset in exp patch
     *
     * @return
     */
    public static synchronized long getPgcOffset() {
        if (pgc_offset == -1) {
            pgc_offset = SettingsActivity.getSharedPreferences(Global.getAppContext()).
                    getLong(Constants.PGC_START_POS_PATCH, -1);
        }
        return pgc_offset;
    }


    public static boolean getHorizonFillStatus() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).
                getBoolean(Global.getAppContext().getString(R.string.horiz_fill), true);
    }

    private static boolean star_label = false;
    private static boolean object_label = false;
    private static boolean object_label_layer = false;
    private static boolean double_star_sep = true;

    public static void initLabels() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext());
        star_label = sh.getBoolean(Global.getAppContext().getString(R.string.bright_star_label), true);
        object_label = sh.getBoolean(Global.getAppContext().getString(R.string.object_label), true);
        object_label_layer = sh.getBoolean(Global.getAppContext().getString(R.string.object_label_layer), false);
        double_star_sep = sh.getBoolean(Global.getAppContext().getString(R.string.show_double_separation), true);
    }

    public static boolean isStarLabelOn() {
        return star_label;
    }

    /**
     * label for object from observation list
     *
     * @return
     */
    public static boolean isObsObjectLabelOn() {
        return object_label;
    }

    /**
     * label for object from layer
     *
     * @return
     */
    public static boolean isLayerObjectLabelOn() {
        return object_label_layer;
    }

    public static boolean isDoubleSepOn() {
        return double_star_sep;
    }

    private static boolean layer_gc = true;
    private static boolean layer_gx = true;
    private static boolean layer_gcld = false;
    private static boolean layer_hiirgn = false;
    private static boolean layer_neb = true;
    private static boolean layer_oc = true;
    private static boolean layer_ocneb = false;
    private static boolean layer_pn = true;
    private static boolean layer_snr = true;

    private static boolean layer_ast = false;
    private static boolean layer_cg = false;
    private static boolean layer_dn = false;
    private static boolean layer_star = false;

    private static boolean layer_zero_mag = true;//include zero mag objects to search

    /**
     * graph ngcic selection
     *
     * @return
     */
    public static boolean isGCon() {
        return layer_gc;
    }

    public static boolean isGxOn() {
        return layer_gx;
    }

    public static boolean isGxyCldOn() {
        return layer_gcld;
    }

    public static boolean isHiiOn() {
        return layer_hiirgn;
    }

    public static boolean isNebOn() {
        return layer_neb;
    }

    public static boolean isOCon() {
        return layer_oc;
    }

    public static boolean isOCNebOn() {
        return layer_ocneb;
    }

    public static boolean isPNon() {
        return layer_pn;
    }

    public static boolean isSNRon() {
        return layer_snr;
    }

    public static boolean isAstOn() {
        return layer_ast;
    }

    public static boolean isCGon() {
        return layer_cg;
    }

    public static boolean isDNon() {
        return layer_dn;
    }

    public static boolean isStarOn() {
        return layer_star;
    }

    /**
     * zero mag preference value
     *
     * @return
     */
    public static boolean isZeroMagOn() {
        return layer_zero_mag;
    }

    /**
     * init graph search for ngcic layer
     */
    public static void initGraphSelection() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext());
        layer_gc = sh.getBoolean(Global.getAppContext().getString(R.string.graph_gc), true);
        layer_gx = sh.getBoolean(Global.getAppContext().getString(R.string.graph_gx), true);
        layer_gcld = sh.getBoolean(Global.getAppContext().getString(R.string.graph_gxycld), false);
        layer_hiirgn = sh.getBoolean(Global.getAppContext().getString(R.string.graph_hii), false);
        layer_neb = sh.getBoolean(Global.getAppContext().getString(R.string.graph_neb), true);
        layer_oc = sh.getBoolean(Global.getAppContext().getString(R.string.graph_oc), true);
        layer_ocneb = sh.getBoolean(Global.getAppContext().getString(R.string.graph_ocneb), false);
        layer_pn = sh.getBoolean(Global.getAppContext().getString(R.string.graph_pn), true);
        layer_snr = sh.getBoolean(Global.getAppContext().getString(R.string.graph_snr), true);
        layer_zero_mag = sh.getBoolean(Global.getAppContext().getString(R.string.graph_zero_mag), true);
        layer_ast = sh.getBoolean(Global.getAppContext().getString(R.string.graph_ast), false);
        layer_cg = sh.getBoolean(Global.getAppContext().getString(R.string.graph_cg), false);
        layer_dn = sh.getBoolean(Global.getAppContext().getString(R.string.graph_dn), false);
        layer_star = sh.getBoolean(Global.getAppContext().getString(R.string.graph_star), false);


    }

    private static int rotation;

    /**
     * for quick processing sets current screen rotation to static var
     *
     * @param a
     */
    public static void setScreenRotation(Activity a) {
        rotation = a.getWindowManager().getDefaultDisplay().getRotation();

    }

    /**
     * @return current screen rotation relative to natural orientation
     */
    public static int getRotationAngle() {
        switch (rotation) {
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }

    public static final int ALTAZ_GRID = 0;
    public static final int EQ_GRID = 1;
    public static final int NO_GRID = 2;

    public static int getGridType() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext());
        String type = sh.getString(Global.getAppContext().getString(R.string.grid_preference), "0");
        return AstroTools.getInteger(type, 0, 0, 2);

    }

    public static boolean isGridLabelOn() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext());
        return sh.getBoolean(Global.getAppContext().getString(R.string.grid_label), true);
    }

    public static boolean areOtherLabelsOn() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext());
        return sh.getBoolean(Global.getAppContext().getString(R.string.other_label), true);

    }


    private static long mpsize = 0;

    /**
     * Minor planet size
     *
     * @param size
     */
    public static void setMPsize(long size) {
        mpsize = size;
    }

    public static long getMPsize() {
        return mpsize;
    }

    public static double getRealTimeFOV() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext());
        return getDouble(sh.getString(Global.getAppContext().getString(R.string.d_auto_time_fov), ""), 0, 0, 90);


    }

    public static double getNoMagFOV() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext());
        return getDouble(sh.getString(Global.getAppContext().getString(R.string.graph_zero_mag_fov), "10"), 0, 0, 90);


    }


    public static double getEpFOV() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext());
        return getDouble(sh.getString(Global.getAppContext().getString(R.string.fov_eps), ""), 20, 0, 90);

    }

    private static boolean removingDuplicates = true;

    public static void updateRemovingDuplicates() {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext());
        removingDuplicates = sh.getBoolean(Global.getAppContext().getString(R.string.duplicates_search), true);

    }

    public static boolean isRemovingDuplicates() {
        return removingDuplicates;
    }

    /**
     * @param context
     * @return set of user dbs that are selected
     */
    public static Set<Integer> getCatalogSelectionPrefs(Context context, int where) {
        String pref = Constants.CATALOG_SELECTION_PREF;
        if (where == SEARCH_NEARBY)
            pref = Constants.CATALOG_SELECTION_PREF_SEARCH_NEARBY;

        Set<Integer> set = new HashSet<Integer>();
        String s = SettingsActivity.getStringFromSharedPreferences(context, pref, "");
        if ("".equals(s)) return set;
        String[] arr = s.split(";");
        for (String sa : arr) {
            try {
                int j = Integer.parseInt(sa);
                set.add(j);
            } catch (Exception e) {
                continue;
            }
        }
        return set;
    }

    /**
     * @param context
     * @param set     - set of user dbs that are selected
     */
    public static void saveCatalogSelectionPrefs(Context context, Set<Integer> set, int where) {
        String pref = Constants.CATALOG_SELECTION_PREF;
        if (where == SEARCH_NEARBY)
            pref = Constants.CATALOG_SELECTION_PREF_SEARCH_NEARBY;

        String s = "";
        for (int i : set) {
            s += i + ";";
        }
        if (s.length() > 0) s = s.substring(0, s.length() - 1);
        putSharedPreferences(pref, s, context);
    }

    public static final int DSO_SELECTION = 1;
    public static final int SEARCH_NEARBY = 2;

    /**
     * @return keys for determining which catalog is selected
     * @where DSO_SELECTION or SEARCH_NEARBY
     */
    public static int[] getCatalogKeys(int where) {
        int[] keys = new int[]{R.string.select_catalog_messier, R.string.select_catalog_caldwell, R.string.select_catalog_hershell, R.string.select_catalog_ngcic,
                R.string.select_catalog_sac, R.string.select_catalog_ugc, R.string.select_catalog_pgc, R.string.select_catalog_ldn, R.string.select_catalog_lbn, R.string.select_catalog_barnard,
                R.string.select_catalog_sh2, R.string.select_catalog_pk, R.string.select_catalog_abell, R.string.select_catalog_hickson, R.string.select_catalog_bright_ds, R.string.select_catalog_wds,
                R.string.select_catalog_comet, R.string.select_catalog_planet};
        int[] keys2 = new int[]{R.string.select_catalog_messier2, R.string.select_catalog_caldwell2, R.string.select_catalog_hershell2, R.string.select_catalog_ngcic2,
                R.string.select_catalog_sac2, R.string.select_catalog_ugc2, R.string.select_catalog_pgc2, R.string.select_catalog_ldn2, R.string.select_catalog_lbn2, R.string.select_catalog_barnard2,
                R.string.select_catalog_sh22, R.string.select_catalog_pk2, R.string.select_catalog_abell2, R.string.select_catalog_hickson2, R.string.select_catalog_bright_ds2, R.string.select_catalog_wds2,
                R.string.select_catalog_comet2, R.string.select_catalog_planet2};

        switch (where) {
            case DSO_SELECTION:
                return keys;
            case SEARCH_NEARBY:
                return keys2;
        }
        return keys;
    }

    /**
     * obj types for dso selection or SEARCH NEARBY
     *
     * @return
     */
    public static int[] getObjTypesKeys(int where) {
        int[] obj_types_keys = new int[]{R.string.type_comet, R.string.type_custom, R.string.type_ds, R.string.type_gc, R.string.type_gxy,
                R.string.type_gxycld, R.string.type_hiirgn, R.string.type_neb, R.string.type_oc, R.string.type_ocneb, R.string.type_planet,
                R.string.type_pn, R.string.type_snr, R.string.type_star, R.string.type_cg, R.string.type_ast, R.string.type_dn};
        int[] obj_types_keys2 = new int[]{R.string.type_comet2, R.string.type_custom2, R.string.type_ds2, R.string.type_gc2, R.string.type_gxy2,
                R.string.type_gxycld2, R.string.type_hiirgn2, R.string.type_neb2, R.string.type_oc2, R.string.type_ocneb2, R.string.type_planet2,
                R.string.type_pn2, R.string.type_snr2, R.string.type_star2, R.string.type_cg2, R.string.type_ast2, R.string.type_dn2};
        switch (where) {
            case DSO_SELECTION:
                return obj_types_keys;
            case SEARCH_NEARBY:
                return obj_types_keys2;
        }
        return obj_types_keys;
    }

    /**
     * obj types for star chart object layer
     *
     * @return
     */
    public static int[] getGraphObjTypesKeys() {
        int[] keys = new int[]{R.string.graph_ast, R.string.graph_cg, R.string.graph_dn, R.string.graph_gc, R.string.graph_gx, R.string.graph_gxycld, R.string.graph_hii, R.string.graph_neb, R.string.graph_oc, R.string.graph_ocneb, R.string.graph_pn, R.string.graph_snr, R.string.graph_star};
        return keys;
    }

    /**
     * @param context
     * @return the list of selected internal catalog names
     */
    public static List<String> getSelectedCatalogsNames(Context context, int where) {
        List<Integer> list = getSelectedInternalCatalogs(context, where);
        List<String> list2 = new ArrayList<String>();
        for (Integer i : list) {
            String name = DbManager.getDbName(i);
            if (name != null)
                list2.add(name);
        }
        return list2;
    }

    /**
     * @param context
     * @return the list of selected internal catalogs for dso selection / search nearby
     */
    public static List<Integer> getSelectedInternalCatalogs(Context context, int where) {
        int[] keys = SettingsActivity.getCatalogKeys(where);
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        List<Integer> list = new ArrayList<Integer>();
        for (int key : keys) {
            boolean defvalue = false;
            if (key == R.string.select_catalog_caldwell || key == R.string.select_catalog_hershell || key == R.string.select_catalog_messier ||
                    key == R.string.select_catalog_ngcic || key == R.string.select_catalog_caldwell2 || key == R.string.select_catalog_hershell2 || key == R.string.select_catalog_messier2 || key == R.string.select_catalog_ngcic2) {
                defvalue = true;
            }
            boolean res = sh.getBoolean(context.getString(key), defvalue);
            if (res) {
                int cat = SettingsActivity.getCatalogFromKey(key);
                list.add(cat);
            }
        }
        return list;

    }


    /**
     * @param key xml key representing catalog in setting search
     * @return catalog number associated with this key
     */
    public static int getCatalogFromKey(int key) {
        switch (key) {
            case R.string.select_catalog_messier2:
            case R.string.select_catalog_messier:
                return AstroCatalog.MESSIER;
            case R.string.select_catalog_caldwell2:
            case R.string.select_catalog_caldwell:
                return AstroCatalog.CALDWELL;
            case R.string.select_catalog_hershell2:
            case R.string.select_catalog_hershell:
                return AstroCatalog.HERSHEL;
            case R.string.select_catalog_ngcic2:
            case R.string.select_catalog_ngcic:
                return AstroCatalog.NGCIC_CATALOG;
            case R.string.select_catalog_sac2:
            case R.string.select_catalog_sac:
                return AstroCatalog.SAC;
            case R.string.select_catalog_ugc2:
            case R.string.select_catalog_ugc:
                return AstroCatalog.UGC;
            case R.string.select_catalog_ldn2:
            case R.string.select_catalog_ldn:
                return AstroCatalog.DNLYNDS;
            case R.string.select_catalog_lbn2:
            case R.string.select_catalog_lbn:
                return AstroCatalog.BNLYNDS;
            case R.string.select_catalog_barnard2:
            case R.string.select_catalog_barnard:
                return AstroCatalog.DNBARNARD;
            case R.string.select_catalog_bright_ds:
            case R.string.select_catalog_bright_ds2:
                return AstroCatalog.HAAS;
            case R.string.select_catalog_wds2:
            case R.string.select_catalog_wds:
                return AstroCatalog.WDS;
            case R.string.select_catalog_comet2:
            case R.string.select_catalog_comet:
                return AstroCatalog.COMET_CATALOG;
            case R.string.select_catalog_planet2:
            case R.string.select_catalog_planet:
                return AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG;
            case R.string.select_catalog_abell2:
            case R.string.select_catalog_abell:
                return AstroCatalog.ABELL;
            case R.string.select_catalog_hickson2:
            case R.string.select_catalog_hickson:
                return AstroCatalog.HCG;
            case R.string.select_catalog_pk2:
            case R.string.select_catalog_pk:
                return AstroCatalog.PK;
            case R.string.select_catalog_pgc2:
            case R.string.select_catalog_pgc:
                return AstroCatalog.PGC;
            case R.string.select_catalog_sh22:
            case R.string.select_catalog_sh2:
                return AstroCatalog.SH2;
        }
        return -1;
    }

    /**
     * @return the list of selected object types for dso selection and search nearby search
     */
    public static List<String> getSelectedTypes(Context context, int where) {
        List<String> list = new ArrayList<String>();

        if (SettingsActivity.getAST(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.AST));
        }
        if (SettingsActivity.getCG(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.CG));
        }
        if (SettingsActivity.getComet(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.Comet));
        }
        if (SettingsActivity.getDN(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.DN));
        }
        if (SettingsActivity.getGC(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.GC));
        }
        if (SettingsActivity.getGxy(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.Gxy));

        }
        if (SettingsActivity.getGxyCld(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.GxyCld));

        }
        if (SettingsActivity.getHIIRgn(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.HIIRgn));

        }
        if (SettingsActivity.getMinorPlanet(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.MINOR_PLANET));
        }

        if (SettingsActivity.getNeb(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.Neb));

        }
        if (SettingsActivity.getOC(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.OC));

        }
        if (SettingsActivity.getOCNeb(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.OCNeb));

        }
        if (SettingsActivity.getPN(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.PN));

        }
        if (SettingsActivity.getSNR(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.SNR));

        }
        if (SettingsActivity.getStar(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.Star));

        }
        if (SettingsActivity.getDS(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.DoubleStar));
        }
        if (SettingsActivity.getCustom(context, where)) {
            list.add(AstroObject.getTypeString(AstroObject.Custom));

        }
        return list;
    }

    public static String getObjTypesSummary(Context context, int where) {
        List<String> list = SettingsActivity.getSelectedTypes(context, where);
        String summary = "";
        for (String s : list) {
            summary = summary + s + " ";
        }
        if (!"".equals(summary))
            summary = summary.substring(0, summary.length() - 1);
        if ("".equals(summary)) {
            summary = context.getString(R.string.no_object_types_selected);
        }
        return summary;
    }

    public static String getSelectedCatalogsSummary(Context context, int where) {
        List<String> list = SettingsActivity.getSelectedCatalogsNames(context, where);
        Set<Integer> set = getCatalogSelectionPrefs(context, where);
        for (Integer i : set) {
            String name = DbManager.getDbName(i);
            if (name != null)
                list.add(name);
        }

        String summary = "";
        for (String s : list) {
            summary = summary + s + " ";
        }
        if (!"".equals(summary))
            summary = summary.substring(0, summary.length() - 1);
        if ("".equals(summary)) {
            summary = context.getString(R.string.no_catalog);
        }
        return summary;
    }


    public static int getEmptyRule() {
        Context context = Global.getAppContext();
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        String valuestr = sh.getString(context.getString(R.string.absent_data), "0");
        int emptyRule = SettingsActivity.getInt(valuestr, 0, 0, 3);
        return emptyRule;
    }

    public static boolean isConBoundaryOn(Context context) {

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getBoolean(context.getString(R.string.con_boundaries1), false);
    }

    public static boolean isMilkyWayOn(Context context) {

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getBoolean(context.getString(R.string.milky_way), false);
    }

    public static final int PRIMARY_SEARCH = 0;
    public static final int ADVANVCED_SEARCH = 1;

    public static int getSearchType() {
        Context context = Global.getAppContext();
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        String valuestr = sh.getString(context.getString(R.string.select_search_type2), "0");
        int type = SettingsActivity.getInt(valuestr, 0, 0, 1);
        return type;
    }

    public static boolean areButtonsInRedMode(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getBoolean(context.getString(R.string.floating_back_menu_buttons), true);
    }

    public static boolean areButtonsInDayMode(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getBoolean(context.getString(R.string.floating_back_menu_buttons_day), true);
    }

    public static FloatingActionButton getNightModeBackButton(final Activity activity, int height) {
        Log.d(TAG, "h=" + height);
        FloatingActionButton btn = new FloatingActionButton.Builder(activity)
                .withDrawable(activity.getResources().getDrawable(R.drawable.ram_back))
                .withButtonColor(0x30ff0000)
                .withGravity(Gravity.BOTTOM | Gravity.LEFT)
                .withMargins(5, 0, 0, Math.max(height / 6, FloatingActionButton.FLOATING_BUTTON_SIZE))
                .create();
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //onBackPressed();
                activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));

            }
        });
        return btn;
    }


    private static final int DARK_ON_GRAY_COLOR = 0x30ffffff;
    private static final int WHITE_ON_GRAY_COLOR = 0x30000000;

    /**
     * @param activity
     * @param height
     * @param color    true for dark on gray, false for white on gray
     * @return
     */
    public static FloatingActionButton getDayModeBackButton(final Activity activity, int height, boolean color) {
        Log.d(TAG, "h=" + height);
        int image = color ? R.drawable.am_back : R.drawable.am_back_white;
        int buttonColor = color ? DARK_ON_GRAY_COLOR : WHITE_ON_GRAY_COLOR;

        FloatingActionButton btn = new FloatingActionButton.Builder(activity)
                .withDrawable(activity.getResources().getDrawable(image))
                .withButtonColor(buttonColor)
                .withGravity(Gravity.BOTTOM | Gravity.LEFT)
                .withMargins(5, 0, 0, Math.max(height / 6, FloatingActionButton.FLOATING_BUTTON_SIZE))
                .create();
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));

            }
        });
        return btn;
    }

    public static FloatingActionButton getNightModeMenuButton(final Activity activity, int height) {

        FloatingActionButton fabButtonMenu = new FloatingActionButton.Builder(activity)
                .withDrawable(activity.getResources().getDrawable(R.drawable.ram_btn_menu))
                .withButtonColor(0x30ff0000)
                .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
                .withMargins(0, 0, 5, Math.max(height / 6, FloatingActionButton.FLOATING_BUTTON_SIZE))
                .create();

        fabButtonMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
                activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU));


            }
        });
        return fabButtonMenu;
    }

    /**
     * @param activity
     * @param height
     * @param color    true for dark on gray, false for white on gray
     * @return
     */
    public static FloatingActionButton getDayModeMenuButton(final Activity activity, int height, boolean color) {
        int image = color ? R.drawable.am_btn_menu : R.drawable.am_btn_menu_white;
        int buttonColor = color ? DARK_ON_GRAY_COLOR : WHITE_ON_GRAY_COLOR;
        FloatingActionButton fabButtonMenu = new FloatingActionButton.Builder(activity)
                .withDrawable(activity.getResources().getDrawable(image))
                .withButtonColor(buttonColor)
                .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
                .withMargins(0, 0, 5, Math.max(height / 6, FloatingActionButton.FLOATING_BUTTON_SIZE))
                .create();

        fabButtonMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
                activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU));


            }
        });
        return fabButtonMenu;
    }

    public static final int LANGUAGE_ENGLISH = 0;
    public static final int LANGUAGE_RUSSIAN = 1;

    public static void setFileDialogPath(String path, Context context) {
        File f = new File(path);
        if (!f.isDirectory()) {
            path = f.getParent();
        }
        SettingsActivity.putSharedPreferences(Constants.FILE_DIALOG_PATH, path, context);

    }

    /**
     * returns Global.exportImportPath if not set
     *
     * @return
     */
    public static String getFileDialogPath(Context context) {
        String path = SettingsActivity.getStringFromSharedPreferences(context,
                Constants.FILE_DIALOG_PATH, Global.exportImportPath);
        File f = new File(path);

        if (!f.canRead())
            path = Global.exportImportPath;

        else {

            if (!f.isDirectory()) {
                path = f.getParent();
                File f2 = new File(path);
                if (!f2.canRead())
                    path = Global.exportImportPath;
            }
        }
        return path;
    }

    public static void setFileDialogUri(Uri uri, Context context) {
        SettingsActivity.putSharedPreferences(Constants.FILE_DIALOG_URI, uri.toString(), context);
    }

    public static Uri getFileDialogUri(Context context) {
        String uris = SettingsActivity.getStringFromSharedPreferences(context,
                Constants.FILE_DIALOG_URI, null);
        if (uris == null) {
            return null;
        }
        return Uri.parse(uris);
    }

    public static boolean isEyepiecesLabels(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getBoolean(context.getString(R.string.ep_labels_info), false);
    }

    public static boolean isFOVcolumn(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getBoolean(context.getString(R.string.fov_column), true);
    }

    private static final int MAX_VIBRATION_MILLIS = 500;
    private static final int DEFAULT_VIBRATION_MILLIS = 25;
    private static final String D_VIB_STRENGTH = "d_vib_strength";

    public static int getVibrationLength(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);

        return SettingsActivity.getInt(sh.getString(D_VIB_STRENGTH, ""), DEFAULT_VIBRATION_MILLIS, 0, MAX_VIBRATION_MILLIS);

    }

    private static float label_scale = DEFAULT_LABEL_SCALE;

    public static void initLabelsScale(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);

        label_scale = SettingsActivity.getFloat(sh.getString(context.getString(R.string.d_scale_labels), ""), DEFAULT_LABEL_SCALE, MIN_LABEL_SCALE, MAX_LABEL_SCALE);

    }

    public static float getLabelsScale() {
        return label_scale;
    }

    /**
     * same def values should be in settings_bt.xml
     *
     * @param context
     * @return
     */
    public static boolean isScopeGoStarChartAutoUpdate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getBoolean(context.getString(R.string.star_chart_auto_update), DEFAULT_STAR_CHART_AUTO_UPDATE);
    }

    /**
     * same def values should be in settings_bt.xml
     *
     * @param context
     * @return
     */
    public static double getScopeGoStarChartAutoTimeUpdatePeriod(Context context) {
        return AstroTools.getDouble(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext())
                .getString(context.getString(R.string.d_autoScopeGoTimePeriod), "1"), 1, 0.01, 100000);
    }


    public static String getCometUpdateUrl(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getString(context.getString(R.string.url_comet_update), Comet.DOWNLOAD_URL);
    }

    public static void setCometUpdateUrl(Context context, String url) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        String out = context.getString(R.string.url_comet_update);
        sh.edit().putString(out, url).commit();

    }

    public static String getDSSdownloadUrl(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        String out = context.getString(R.string.url_dss_download);
        Log.d(TAG, "out=" + out);
        return sh.getString(out, DSSdownloadable.HTTP_ARCHIVE_STSCI_EDU_CGI_BIN_DSS_SEARCH_V_POSS2UKSTU_RED_R);
    }

    public static void setDSSdownloadUrl(Context context, String url) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        String out = context.getString(R.string.url_dss_download);
        sh.edit().putString(out, url).commit();
    }

    public static String getDefaultMinorPlanetUpdateUrl(Context context) {
        String packageName = context.getPackageName();
        packageName = packageName.replace(".", "/");
        String aURL = URLupdateTask.DOWNLOAD_URL_BASE + packageName + "/" + MinorPlanet.DOWNLOAD_FILE_NAME;
        return aURL;


    }

    public static String getMinorPlanetUpdateUrl(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        String url = getDefaultMinorPlanetUpdateUrl(context);
        return sh.getString(context.getString(R.string.url_minor_planet_update), url);
    }

    public static void setMinorPlanetUpdateUrl(Context context, String url) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        String out = context.getString(R.string.url_minor_planet_update);
        sh.edit().putString(out, url).commit();

    }

    /**
     * set default value for url_minor_planet_update InputPreference
     */
    public static void initMinorPlanetUpdateUrlInputPref(Context context) {
        boolean def_url_changed = SettingsActivity.getSharedPreferences(context).getBoolean(Constants.MP_CHANGE_DEF_UPDATE_URL, true);
        if (def_url_changed)
            SettingsActivity.putSharedPreferences(Constants.MP_CHANGE_DEF_UPDATE_URL, false, context);

        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        String field = context.getString(R.string.url_minor_planet_update);
        String out = sh.getString(field, "");
        if ("".equals(out) || def_url_changed) {
            String url = getDefaultMinorPlanetUpdateUrl(context);
            sh.edit().putString(field, url).commit();
        }
    }

    public static String getAlternativeExpansionFileFolder(Context context) {
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        return sh.getString(context.getString(R.string.alternative_data_location), "");
    }

    /**
     * should run in non-UI thread
     *
     * @param context
     * @return
     */
    public static int getMinorPlanetUpdateNumberOnServer(Context context) {
        String url = URLupdateTask.getUpdateUrl(context);
        String result = AstroTools.downloadTextViaHttps(url);
        if (result == null)
            return -1;
        String[] a = result.split("\n");
        if (a.length < 4)
            return -1;
        return SettingsActivity.getInt(a[3], -1, -1, 10000);
    }

    /**
     * for keeping the same zoom level when transferring to version
     * with more scales in Graph
     */
    public static void updateZoomLevelIfNeeded(Context context) {
        SharedPreferences sh = getSharedPreferences(context);
        int first_time = sh.getInt(Constants.GRAPH_UPDATE_ZOOM_LEVEL, -1);
        if (first_time == -1) {
            Log.d(TAG, "update zoom level, first time");
            putSharedPreferences(Constants.GRAPH_UPDATE_ZOOM_LEVEL, 1, context);
            int dzl = sh.getInt(GraphRec.G_FOV, -1);
            if (dzl != -1) { //exists already
                Log.d(TAG, "exists already" + dzl);
                dzl += 6;
                int len_spin = GraphActivity.spinArr.length;
                Log.d(TAG, "len_spin" + len_spin);
                if (dzl >= 0 && dzl < len_spin) {
                    Log.d(TAG, "g fov updated" + dzl);
                    sh.edit().putInt(GraphRec.G_FOV, dzl).commit();
                }
            }
        }
    }


    /**
     * Persist pure uri, not file paths
     *
     * @param uri
     * @param context
     */
    public static void persistUriIfNeeded(Uri uri, Context context) {
        String authority = uri.getAuthority();
        if (FILE.equals(authority))
            return;
        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
        try {
            context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
        } catch (Exception e) {

        }
        return;
    }

    /**
     * @param context
     * @return if flag for compulsory recopying these databases is set
     */
    public static boolean isResetNgcicCometMp(Context context) {
        return SettingsActivity.getSharedPreferences(context).
                getBoolean(Constants.RESET_NGCIC_COMET_MINOR_PLANET_DBS_FLAG, false);

    }

    /**
     * set flag for compulsory recopying these databases
     *
     * @param context
     */
    public static void setResetNgcicCometMpFlag(Context context, boolean value) {
        SettingsActivity.putSharedPreferences(Constants.RESET_NGCIC_COMET_MINOR_PLANET_DBS_FLAG, value, context);
    }

    public static boolean isFreeSpaceAvailable(Context context, File pathDir, long size) {
        //https://developer.android.com/training/data-storage/app-specific#java
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                StorageManager storageManager = context.getSystemService(StorageManager.class);
                UUID appSpecificInternalDirUuid = storageManager.getUuidForPath(pathDir);
                long availableBytes = storageManager.getAllocatableBytes(appSpecificInternalDirUuid);
                Log.d(TAG, "availableBytes=" + availableBytes);
                if (availableBytes >= size) {
                    storageManager.allocateBytes(appSpecificInternalDirUuid, size);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {

            }
            return true;
        } else {
            long freespace = AstroTools.getSdCardAvailableSpace();
            Log.d(TAG, "freespace=" + freespace);
            if (freespace >= size)
                return true;
            else
                return false;

        }
    }


    public static boolean isPushCameraOn(Context context) {
        return getBooleanFromSharedPreferences(context,
                Constants.PUSH_CAMERA_MODE, false);
    }

    public static void setPushCamera(Context context, boolean status) {
        SettingsActivity.putSharedPreferences(Constants.PUSH_CAMERA_MODE, status, context);
    }

    public static boolean isPushCameraMirrorOn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pushcam_mirror), false);
    }

    public static int getNumberOfStackedImages(Context context) {
        String s = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.d_number_of_stacked_images), "25");

        return AstroTools.getInteger(s, 25, 1, 100);
    }

    public static boolean isPushCameraAnyAlignObject(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_any_align), false);
    }


    public static boolean isPushCamGyroAltValidation(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_check_alt_for_hashes), true);
    }

    /**
     * Experimental
     */
    public static boolean isPushCamDiskCache(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_save_sd), false);
    }

    /**
     * Experimental. Use JPEG for stacking else YUV
     *
     * @param context
     * @return
     */
    public static boolean isPushCamJPEGforStacking(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_use_jpeg), false);
    }

    /**
     * Experimental. Number of jpeg batches
     */
    public static int getNumberOfJpegDivisions(Context context) {
        String s = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.push_cam_jpeg_div), "1");

        return AstroTools.getInteger(s, 1, 1, 4);
    }


    public static boolean isRemoveBrightAreas(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_remove_bright_objects), true);
    }

    public static int getImageCaptureDelay(Context context) {
        String s = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.d_image_capture_delay), "2000");

        return AstroTools.getInteger(s, 2000, 0, 100000);

    }

    /**
     * is Sensor.Gyroscope used for azimuth calculation
     */


    public static boolean isSensorHighSpeed(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_sensor_speed), false);
    }


    public static boolean isCameraApi(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_camera_api), true);
    }

    public void setCameraApi(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).
                edit().putBoolean(context.getString(R.string.push_cam_camera_api), value).
                commit();
    }

    public static boolean isTemplatePreview(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_mode_preview), false);
    }

    public static boolean isTemplateManual(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_mode_manual), false);
    }

    public static boolean isTemplateVideo(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_mode_video), false);
    }

    public static boolean isTemplatePhoto(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_mode_photo), true);
    }

    public static boolean isCamera2Control(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_control), false);
    }

    public static int getCamera2Exposure(Context context) {
        String s = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.push_cam_control_exposure), "1000");

        return AstroTools.getInteger(s, 1000, 0, 100000);
    }

    public static int getCamera2ISO(Context context) {
        String s = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.push_cam_control_iso), "800");

        return AstroTools.getInteger(s, 800, 0, 10000);
    }

    public static boolean isCamera2Size1(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_size_1), true);
    }

    public static boolean isCamera2Size2(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_size_2), false);
    }

    public static boolean isCamera2Size3(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_size_3), false);
    }

    public static boolean isCamera2Size4(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_size_4), false);
    }

    public static boolean isCamera2SizeMax(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_size_max), false);
    }

    public static boolean isCamera2AutoFocus(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_control_auto_focus), false);
    }

    public static boolean isCamera2FPS(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_control_ae_fps), false);
    }

    public static boolean isCamera2Preview(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_camera2_preview), false);
    }

    public static void setCamera2Preview(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).
                edit().putBoolean(context.getString(R.string.push_cam_camera2_preview), value).
                commit();
    }

    public static boolean isCamera2PreviewNew(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_camera2_preview_new), false);
    }

    public static void setCamera2PreviewNew(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).
                edit().putBoolean(context.getString(R.string.push_cam_camera2_preview_new), value).
                commit();
    }

    public static boolean isCamera2Photo(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_camera2_photo), true);
    }

    public static void setCamera2Photo(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context).
                edit().putBoolean(context.getString(R.string.push_cam_camera2_photo), value).
                commit();
    }

    public static boolean isCamera2NoiseReductionOff(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_noise_off), false);
    }

    public static String getCamera2SeriesName(Context context) {
        String s = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.push_cam_series_name), "");

        return s;
    }

    public static boolean isCamera2Auto(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.push_cam_auto), false);


    }

    public static int getCamera2AutoNumImages(Context context) {
        String s = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.push_cam_auto_number_of_images), "5");

        return AstroTools.getInteger(s, 5, 0, 100);
    }


}

