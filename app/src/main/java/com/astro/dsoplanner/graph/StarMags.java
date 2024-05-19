package com.astro.dsoplanner.graph;

import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.LabelLocations;


import com.astro.dsoplanner.R;
import com.astro.dsoplanner.RangeSeekBar;
import com.astro.dsoplanner.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.astro.dsoplanner.RangeSeekBar.OnRangeSeekBarDraggedChangeListener;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.SettingsInclActivity;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.graph.cuv_helper.UploadRec;

public class StarMags extends RelativeLayout {
    private static final String NGC_IC2 = "NgcIc";
    private static final String NGC2 = "NGC:";
    private static final String PGC3 = "PGC:";
    private static final String PRO = " (Pro)";
    private static final String MAG_LIMIT = ": mag limit=";
    private static final String UGC2 = "UGC";
    private static final String NGC_IC = "NgcIc/SAC";
    private static final String PGC2 = "PGC";
    private static final String UCAC4 = "UCAC4";
    private static final String TYCHO_2 = "Tycho-2";
    private static final String YALE2 = "Yale";

    private static final String TAG = StarMags.class.getSimpleName();
    private Context _context;
    private boolean _isActive = false;
    private RangeSeekBar<Integer> seekBar = null;

    public static final int YALE = 0;
    public static final int TYCHO = 1;
    public static final int UCAC = 2;
    public static final int NGC = 3;
    public static final int UGC = 4;
    public static final int PGC = 5;


    private String getCatalogName() {
        switch (catalog) {
            case YALE:
                return YALE2;
            case TYCHO:

                return TYCHO_2;
            case UCAC:

                return UCAC4;
            case PGC:

                return PGC2;
            case NGC:
                if (Global.BASIC_VERSION)
                    return NGC_IC2;
                else
                    return NGC_IC;
        }
        return "";
    }

    private int catalog;

    public int getCatalog() {
        return catalog;
    }

    public StarMags(Activity _context) {
        super(_context);
        this._context = _context;
        LayoutInflater.from(_context).inflate(R.layout.starrange, this, true);
        findViewById(R.id.starrange).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            }
        });
    }


    /**
     * dealing with turning catalogs on when they are off
     *
     * @author leonid
     */
    class Catalog {
        int cat;

        public Catalog(int cat) {

            this.cat = cat;
        }

        public boolean isOn() {
            switch (cat) {
                case YALE:
                    return true;
                case TYCHO:
                    return SettingsActivity.getTychoStatus();
                case UCAC:
                    return SettingsActivity.getUcac4Status();
                case PGC:
                    return SettingsActivity.getPgcStatus();
                case NGC:
                    return SettingsActivity.getNgcIcStatus();
            }
            return false;
        }

        public void setOn() {
            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(_context);
            switch (cat) {
                case TYCHO:
                    sh.edit().putBoolean(SettingsActivity.TYCHO, true).commit();
                    break;
                case UCAC:
                    sh.edit().putBoolean(SettingsActivity.UCAC4, true).commit();
                    break;
                case PGC:
                    sh.edit().putBoolean(_context.getString(R.string.pgclayer), true).commit();
                    break;
                case NGC:
                    sh.edit().putBoolean(SettingsActivity.NGCICLAYER, true).commit();
                    break;

            }
        }

        public void showDialog() {
            Runnable r = new Runnable() {
                public void run() {
                    setOn();
                    redraw();
                }
            };

            Dialog d = AstroTools.getDialog(_context, getCatalogName() + " " + _context.getString(R.string.this_catalog_is_off_would_you_like_to_turn_it_on_), r);
            GraphActivity a = (GraphActivity) _context;
            a.registerDialog(d).show();
        }
    }

    public void Init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setVisibility(View.GONE);
        _isActive = false;

        // create RangeSeekBar as Integer range between 20 and 100
        seekBar = new RangeSeekBar<Integer>(0, 100, _context);
        seekBar.setNightmode(SettingsActivity.getNightMode());
        seekBar.setNotifyWhileDragging(false);
        seekBar.hideFirstPoint();
        seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                Log.d(TAG, "minValue=" + minValue + " maxValue=" + maxValue);
                double limit = getMaxLimit(catalog, Point.getFOV());//maximum mag for this catalog and fov
                if (limit == 0) {
                    seekBar.setSelectedMaxValue(0);
                    return;
                }
                setMagLimit(catalog, maxValue, Point.getFOV());
                updateText();
                redraw();
            }
        });

        seekBar.setOnRangeSeekBarDraggedChangeListener(new OnRangeSeekBarDraggedChangeListener<Integer>() {
            public void onRangeSeekBarDraggedValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                double limit = getMaxLimit(catalog, Point.getFOV());//maximum mag for this catalog and fov
                if (limit == 0) return;
                setMagLimit(catalog, maxValue, Point.getFOV());
                updateText();
            }

        });
        seekBar.setVisibility(View.GONE);
        // add RangeSeekBar to pre-defined layout
        ViewGroup layout = (ViewGroup) findViewById(R.id.rangeSlider);
        layout.addView(seekBar);

        final Button b3 = (Button) findViewById(R.id.bb_type);
        b3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(_context, SettingsInclActivity.class);
                intent.putExtra(Constants.XML_NUM, R.xml.settings_graph_ngcobjects_incl);
                _context.startActivity(intent);
            }
        });


        //next button
        Button b1 = (Button) findViewById(R.id.bb_ok);
        b1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                catalog++;
                if (Global.BASIC_VERSION) {
                    if (catalog > NGC)
                        catalog = YALE;
                    if (catalog == UCAC)
                        catalog++;
                } else if (catalog > PGC)//free and pro
                    catalog = YALE;

                if (catalog == NGC) {
                    b3.setVisibility(VISIBLE);
                } else {
                    b3.setVisibility(GONE);
                }
                if (catalog == UGC)
                    catalog = PGC;
                Catalog cat = new Catalog(catalog);
                if (!cat.isOn())
                    cat.showDialog();
                updateRangeSlider();

            }
        });

        //reset this button
        Button b2 = (Button) findViewById(R.id.bb_reset);
        b2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int fov = getFOVnumber(Point.getFOV());
                setMag(catalog, fov, getDefMagLimit(catalog, fov));
                updateRangeSlider();
                redraw();
            }
        });
    }

    public void updateText() {
        TextView tv = (TextView) findViewById(R.id.progresstop);
        double mag = getMagLimit(catalog, Point.getFOV());
        String s = MAG_LIMIT + String.format(Locale.US, "%.1f", mag);

        tv.setText(getCatalogName() + s);
    }

    public void start(int catalog) {
        this.catalog = catalog;
        if (!_isActive) {
            setVisibility(View.VISIBLE);
            _isActive = true;
        }
        Catalog cat = new Catalog(catalog);
        if (!cat.isOn())
            cat.showDialog();
        updateRangeSlider();

        Button b3 = (Button) findViewById(R.id.bb_type);
        if (catalog == NGC) {
            b3.setVisibility(VISIBLE);
        } else {
            b3.setVisibility(GONE);
        }
    }

    public void updateRangeSlider() {

        if (seekBar != null) {
            seekBar.setSelectedMinValue(0);
            int progress = magToProgress(catalog, Point.getFOV());
            Log.d(TAG, "update range slider, progress=" + progress);
            seekBar.setSelectedMaxValue(progress);
            updateText();

            if (SettingsActivity.showVisibleStatus()) {
                if (catalog == NGC || catalog == PGC) {
                    seekBar.hide();
                    TextView tv = (TextView) findViewById(R.id.progresstop);
                    String name = PGC3;
                    if (catalog == NGC)
                        name = NGC2;
                    tv.setText(name + _context.getString(R.string._visibility_filter_on));
                    Button b = (Button) findViewById(R.id.bb_reset);
                    b.setEnabled(false);

                } else {
                    seekBar.show();
                    Button b = (Button) findViewById(R.id.bb_reset);
                    b.setEnabled(true);
                }
            } else {
                seekBar.show();
                Button b = (Button) findViewById(R.id.bb_reset);
                b.setEnabled(true);
            }
        }
    }

    public boolean isActive() {
        return _isActive;
    }

    public void stop() {
        if (_isActive) {
            setVisibility(View.GONE);
            _isActive = false;
        }

    }

    protected void redraw() {
        CuV cView = ((GraphActivity) _context).getSkyView();//.sgrChanged(true);
        UploadRec rec = CuV.makeUploadRec();
        rec.clearcache = true;
        int threadid = -1;
        double mag_limit = getMagLimit(catalog, Point.getFOV());
        switch (catalog) {
            case TYCHO:
                if (Point.getFOV() > CuV.TYCHO_SHORT_UPLOAD_TRSHOLD)
                    threadid = CuV.TYCHO_THREAD_SHORT_ID;
                else if (Point.getFOV() == 10 && mag_limit < TYCHO_SHORT_MAX)
                    threadid = CuV.TYCHO_THREAD_SHORT_ID;
                else
                    threadid = CuV.TYCHO_THREAD_ID;
                break;
            case UCAC:
                threadid = CuV.UCAC4_THREAD_ID;
                break;
            case PGC:
                threadid = CuV.PGC_THREAD_ID;
                break;
            case YALE:
                threadid = CuV.YALE_THREAD_ID;
                break;
            case NGC:
                threadid = CuV.NGCIC_THREAD_ID;
                break;

        }
        StarUploadThread.cacheManager.clearCacheAndSetFlag(threadid);
        cView.upload(rec, true, threadid);
        LabelLocations.getLabelLocations().clear();
    }

    public static int getFOVnumber(double FOV) {
        Log.d(TAG, "getFOVnumber, FOV=" + FOV);
        if (FOV == 90) return 0;
        if (FOV == 60) return 1;
        if (FOV == 45) return 2;
        if (FOV == 30) return 3;
        if (FOV == 20) return 4;
        if (FOV == 10) return 5;
        if (FOV == 5) return 6;
        if (FOV == 2) return 7;
        if (FOV == 1) return 8;
        if (FOV == 0.5) return 9;
        if (FOV == 0.25) return 10;
        if (FOV == 0.12) return 11;
        if (FOV == 0.06) return 12;
        if (FOV == 190) return 13;
        if (FOV == 155) return 14;
        if (FOV == 120) return 15;
        if (FOV == 240) return 16;
        if (FOV == 300) return 17;
        if (FOV == 380) return fov_count - 1; //change FOV count when changing number of zooms
        return -1;
    }

    final static int fov_count_old = 13;
    final static int fov_count = 19; ////change FOV count when changing number of zooms
    final static int catalog_count = 6;
    static double[][] mags = new double[catalog_count][fov_count];
    final static int YALE_MAX = 8;
    final static double TYCHO_MAX;
    public final static double TYCHO_SHORT_MAX = 9.5;//200 000 stars to 9.5
    final static int UCAC_MAX;
    public final static double PGC_MAX;
    final static double NGC_MAX;
    final static int UGC_MAX = 19;

    static {
        if (Global.BASIC_VERSION) {
            TYCHO_MAX = 10.7;
            NGC_MAX = 12.7;
            UCAC_MAX = 0;
            PGC_MAX = 0;
        } else if (Global.PLUS_VERSION) {
            TYCHO_MAX = 14;
            NGC_MAX = 18;
            UCAC_MAX = 14;
            PGC_MAX = 16.7;
        } else {//pro
            TYCHO_MAX = 14;
            NGC_MAX = 18;
            UCAC_MAX = 16;
            PGC_MAX = 19.5;
        }
    }

    //change arrays below when changing number of zooms
    final static double[] DEF_YALE_MAG_LIMITS = new double[]{5, 5.5, 6, 6.5, 7, YALE_MAX, YALE_MAX, YALE_MAX, YALE_MAX, YALE_MAX, YALE_MAX, YALE_MAX, YALE_MAX, 5, 5, 5, 5, 5, 5};
    final static double[] DEF_TYCHO_MAG_LIMITS = new double[]{0, 0, 0, 7, 7.5, 8.8, 9.5, Math.min(11.5, TYCHO_MAX), TYCHO_MAX, TYCHO_MAX, TYCHO_MAX, TYCHO_MAX, TYCHO_MAX, 0, 0, 0, 0, 0, 0};
    final static double[] DEF_UCAC_MAG_LIMITS = new double[]{0, 0, 0, 0, 0, 0, 0, 11.5, 13, Math.min(14, UCAC_MAX), Math.min(15, UCAC_MAX), UCAC_MAX, UCAC_MAX, 0, 0, 0, 0, 0, 0};
    final static double[] DEF_PGC_MAG_LIMITS = new double[]{0, 0, 0, 0, 0, 0, 0, PGC_MAX, PGC_MAX, PGC_MAX, PGC_MAX, PGC_MAX, PGC_MAX, 0, 0, 0, 0, 0, 0};
    final static double[] DEF_NGC_MAG_LIMITS = new double[]{3, 5, 7, 9, Math.min(11, NGC_MAX), Math.min(13, NGC_MAX), Math.min(15, NGC_MAX), NGC_MAX, NGC_MAX, NGC_MAX, NGC_MAX, NGC_MAX, NGC_MAX, 3, 3, 3, 3, 3, 3};
    final static double[] DEF_UGC_MAG_LIMITS = new double[]{0, 0, 0, 0, 0, 0, 0, UGC_MAX, UGC_MAX, UGC_MAX, UGC_MAX, UGC_MAX, UGC_MAX, 0, 0, 0, 0, 0, 0};


    private static double getDefMagLimit(int cat, int fov) {
        switch (cat) {
            case YALE:
                return DEF_YALE_MAG_LIMITS[fov];

            case TYCHO:
                return DEF_TYCHO_MAG_LIMITS[fov];

            case UCAC:
                return DEF_UCAC_MAG_LIMITS[fov];

            case PGC:
                return DEF_PGC_MAG_LIMITS[fov];
            case NGC:
                return DEF_NGC_MAG_LIMITS[fov];
		/*case UGC:
			return DEF_UGC_MAG_LIMITS[fov];*/
        }
        return 0;
    }

    /**
     * init static vars from shared prefs or make them by default
     *
     * @param context
     */
    public static void initMags(Context context) {
        String yale = SettingsActivity.getStringFromSharedPreferences(context, Constants.YALE_MAG, "");
        String tycho = SettingsActivity.getStringFromSharedPreferences(context, Constants.TYCHO_MAG, "");
        String ucac = SettingsActivity.getStringFromSharedPreferences(context, Constants.UCAC_MAG, "");
        String pgc = SettingsActivity.getStringFromSharedPreferences(context, Constants.PGC_MAG, "");
        String ngc = SettingsActivity.getStringFromSharedPreferences(context, Constants.NGC_MAG, "");
        String ugc = SettingsActivity.getStringFromSharedPreferences(context, Constants.UGC_MAG, "");

        Log.d(TAG, "yale=" + yale);
        Log.d(TAG, "tycho=" + tycho);
        Log.d(TAG, "ucac=" + ucac);
        Log.d(TAG, "pgc=" + pgc);
        Log.d(TAG, "ngc=" + ngc);
        Log.d(TAG, "ugc=" + ugc);

        double[] mags = DEF_YALE_MAG_LIMITS;
        if ("".equals(yale)) {
            SettingsActivity.putSharedPreferences(Constants.YALE_MAG, arrToString(DEF_YALE_MAG_LIMITS), context);

        } else {
            mags = stringToArr(yale);


            Log.d(TAG, "yale");
            print(mags);
        }
        for (int i = 0; i < fov_count; i++) {
            if (mags[i] == -2)
                setMag(YALE, i, DEF_YALE_MAG_LIMITS[i]);
            else
                setMag(YALE, i, mags[i]);
        }

        mags = DEF_TYCHO_MAG_LIMITS;
        if ("".equals(tycho)) {
            SettingsActivity.putSharedPreferences(Constants.TYCHO_MAG, arrToString(DEF_TYCHO_MAG_LIMITS), context);

        } else {
            mags = stringToArr(tycho);

            Log.d(TAG, "tycho");
            print(mags);
        }
        for (int i = 0; i < fov_count; i++) {
            setMag(TYCHO, i, mags[i]);
        }

        mags = DEF_UCAC_MAG_LIMITS;
        if ("".equals(ucac)) {
            SettingsActivity.putSharedPreferences(Constants.UCAC_MAG, arrToString(DEF_UCAC_MAG_LIMITS), context);

        } else {
            mags = stringToArr(ucac);

            Log.d(TAG, "ucac");
            print(mags);
        }
        for (int i = 0; i < fov_count; i++) {
            setMag(UCAC, i, mags[i]);
        }

        mags = DEF_PGC_MAG_LIMITS;
        if ("".equals(pgc)) {
            SettingsActivity.putSharedPreferences(Constants.PGC_MAG, arrToString(DEF_PGC_MAG_LIMITS), context);

        } else {
            mags = stringToArr(pgc);
            Log.d(TAG, "pgc");
            print(mags);
        }
        for (int i = 0; i < fov_count; i++) {
            setMag(PGC, i, mags[i]);
        }

        mags = DEF_UGC_MAG_LIMITS;
        if ("".equals(pgc)) {
            SettingsActivity.putSharedPreferences(Constants.UGC_MAG, arrToString(DEF_UGC_MAG_LIMITS), context);

        } else {
            mags = stringToArr(pgc);
            Log.d(TAG, "ugc");
            print(mags);
        }
		/*for(int i=0;i<fov_count;i++){
			setMag(UGC,i,mags[i]);
		}*/


        mags = DEF_NGC_MAG_LIMITS;
        if ("".equals(pgc)) {
            SettingsActivity.putSharedPreferences(Constants.NGC_MAG, arrToString(DEF_NGC_MAG_LIMITS), context);

        } else {
            mags = stringToArr(ngc);

            Log.d(TAG, "ngc");
            print(mags);
        }
        for (int i = 0; i < fov_count; i++) {
            if (mags[i] == -2)
                setMag(NGC, i, DEF_NGC_MAG_LIMITS[i]);
            else
                setMag(NGC, i, mags[i]);
        }
    }

    private static void print(double[] d) {
        for (double dx : d) {
            Log.d(TAG, "" + dx);
        }
    }

    /**
     * synchronized method for getting mag limits
     *
     * @param catalog
     * @param fov     fov position
     * @return
     */
    private static synchronized double getMag(int catalog, int fov) {
        if (fov == -1)
            return mags[catalog][0];
        else
            return mags[catalog][fov];
    }

    /**
     * @param catalog
     * @return array of mags for the given catalog
     */
    private static synchronized double[] getMagArr(int catalog) {
        return mags[catalog];
    }

    /**
     * synchronized method for setting mag limits
     *
     * @param catalog
     * @param fov      fov postion
     * @param maglimit
     */
    private static synchronized void setMag(int catalog, int fov, double maglimit) {
        if (fov == -1) return;
        mags[catalog][fov] = maglimit;
    }

    public static double getMagLimit(int catalog, double FOV) {
        int num = getFOVnumber(FOV);
        if (num == -1)
            return getMag(catalog, 0);
        else
            return getMag(catalog, num);
    }

    /**
     * @param s string with mags such as 10 11 12 etc for fovs 90 60 45 etc
     *          -2 if absent (e.g. for  180 150 120)
     * @return array of values
     */
    private static double[] stringToArr(String s) {
        double[] darr = new double[fov_count];
        for (int i = 0; i < fov_count; i++)
            darr[i] = -2;
        String[] arr = s.split(" ");
        int j = 0;//valid values
        for (int i = 0; i < arr.length; i++) {
            double d = AstroTools.getDouble(arr[i], -1, -1, 100);
            if (d == -1) continue;
            darr[i] = d;
            j++;
            if (j >= fov_count) break;

        }
        return darr;
    }

    /**
     * convert array with limits into string
     *
     * @param darr
     * @return
     */
    private static String arrToString(double[] darr) {
        String s = "";
        for (double d : darr) {
            s += String.format(Locale.US, "%.1f", d) + " ";
        }
        return s;
    }

    private static double getMaxLimit(int catalog, double FOV) {
        double limit = 0;
        switch (catalog) {
            case YALE:
                limit = YALE_MAX;
                break;
            case TYCHO:
                if (FOV > 90.1)
                    limit = 0;
                else if (FOV > 60.1) {
                    limit = 8;
                } else if (FOV > CuV.TYCHO_SHORT_UPLOAD_TRSHOLD)
                    limit = TYCHO_SHORT_MAX;
                else if (FOV > 5.1)
                    limit = Math.min(11, TYCHO_MAX);
                else
                    limit = TYCHO_MAX;
                break;
            case UCAC:
                if (FOV > CuV.UCAC_UPLOAD_TRESHOLD) {
                    limit = 0;
                    break;
                } else if (FOV > 2.1) {
                    limit = 13;
                } else if (FOV > 1.1) {
                    limit = Math.min(15, UCAC_MAX);
                } else
                    limit = UCAC_MAX;
                break;
            case PGC:
                if (FOV > CuV.PGC_UPLOAD_TRESHOLD) {
                    limit = 0;
                    break;
                } else if (FOV > 20.1) {
                    limit = 13;
                } else if (FOV > 10.1)
                    limit = 14;
                else
                    limit = PGC_MAX;
                break;

            case NGC:
                if (FOV > 60.1) {//90
                    limit = 10;
                } else if (FOV > 45.1) {
                    limit = 12;
                } else if (FOV > 30.1) {
                    limit = Math.min(14, NGC_MAX);
                } else
                    limit = NGC_MAX;
                break;

        }
        return limit;
    }

    /**
     * needed for gradual slider, do not set minimum limit actually
     *
     * @param catalog
     * @param FOV
     * @return
     */
    private static double getMinLimit(int catalog, double FOV) {
        double limit = 0;
        switch (catalog) {
            case TYCHO:
                limit = 5;
                break;
            case UCAC:
                limit = 8;
                break;
            case PGC:
                limit = 8;
                break;
		/*case UGC:
			limit=5;
			break;*/
        }
        return limit;
    }

    /**
     * @param catalog
     * @param FOV
     * @return progress indicator position, mag is taken from static array
     */
    private static int magToProgress(int catalog, double FOV) {
        double maxlimit = getMaxLimit(catalog, FOV);
        double minlimit = getMinLimit(catalog, FOV);
        double mag = getMag(catalog, getFOVnumber(FOV));
        if (maxlimit == 0) return 0;
        if (mag <= minlimit) return 0;
        return (int) ((mag - minlimit) / (maxlimit - minlimit) * 100);
    }


    /**
     * setting magnitude limit according to progress indicator
     * saving result to static vars
     * not saving it to shared prefs
     * 0 progress equal to zero mag
     *
     * @param catalog
     * @param progress
     */
    private static void setMagLimit(int catalog, int progress, double FOV) {
        double maxlimit = getMaxLimit(catalog, FOV);
        double minlimit = getMinLimit(catalog, FOV);
        double m = 0;
        if (progress != 0)
            m = progress / 100. * (maxlimit - minlimit) + minlimit;
        Log.d(TAG, "mag limit=" + m);
        setMag(catalog, getFOVnumber(FOV), m);
    }

    /**
     * putting mag limits info from static arrays into shared prefs
     *
     * @param context
     */

    public static void putMagLimitsToSharedPrefs(Context context) {
        String yale = arrToString(getMagArr(YALE));
        String tycho = arrToString(getMagArr(TYCHO));
        String ucac = arrToString(getMagArr(UCAC));
        String pgc = arrToString(getMagArr(PGC));
        String ngc = arrToString(getMagArr(NGC));
        Log.d(TAG, "yale=" + yale);
        Log.d(TAG, "tycho=" + tycho);
        Log.d(TAG, "ucac=" + ucac);
        Log.d(TAG, "pgc=" + pgc);
        SettingsActivity.putSharedPreferences(Constants.YALE_MAG, yale, context);
        SettingsActivity.putSharedPreferences(Constants.TYCHO_MAG, tycho, context);
        SettingsActivity.putSharedPreferences(Constants.UCAC_MAG, ucac, context);
        SettingsActivity.putSharedPreferences(Constants.PGC_MAG, pgc, context);
        SettingsActivity.putSharedPreferences(Constants.NGC_MAG, ngc, context);
        SettingsActivity.putSharedPreferences(Constants.UGC_MAG, ngc, context);
    }


}
