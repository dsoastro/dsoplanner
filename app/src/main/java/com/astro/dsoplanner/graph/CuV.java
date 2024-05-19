package com.astro.dsoplanner.graph;

import static com.astro.dsoplanner.Global.ALEX_MENU_FLAG;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.DetailsActivity;
import com.astro.dsoplanner.EyepiecesListActivity;
import com.astro.dsoplanner.EyepiecesRecord;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.graph.camera.Prefs;
import com.astro.dsoplanner.graph.cuv_helper.DSS;
import com.astro.dsoplanner.graph.cuv_helper.DssRectangles;
import com.astro.dsoplanner.graph.cuv_helper.SharedData;
import com.astro.dsoplanner.graph.cuv_helper.Signal;
import com.astro.dsoplanner.graph.cuv_helper.Upload;
import com.astro.dsoplanner.graph.cuv_helper.UploadRec;
import com.astro.dsoplanner.util.Holder2;
import com.astro.dsoplanner.base.HrStar;
import com.astro.dsoplanner.InputDialog;
import com.astro.dsoplanner.LabelLocations;
import com.astro.dsoplanner.MarkTAG;


import com.astro.dsoplanner.base.ObjCursor;
import com.astro.dsoplanner.base.ObjectInfo;
import com.astro.dsoplanner.ObservationListActivity;
import com.astro.dsoplanner.base.Planet;

import com.astro.dsoplanner.QueryActivity;
import com.astro.dsoplanner.R;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.SettingsSystemActivity;
import com.astro.dsoplanner.base.TychoStar;
import com.astro.dsoplanner.matrix.Vector2;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.BoundaryPoint;
import com.astro.dsoplanner.base.ConNamePoint;
import com.astro.dsoplanner.base.ConPoint;
import com.astro.dsoplanner.base.ContourObject;
import com.astro.dsoplanner.base.CustomPoint;
import com.astro.dsoplanner.base.ExtendedObject;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.infolist.ObsInfoListImpl;
import com.astro.dsoplanner.graph.cuv_helper.Grid;

//this class draws a sky

/**
 * former CustomView
 *
 * @author leonid
 */
public class CuV extends ImageView {

    private static final int FOV_FILL_THRESHOLD = 149;
    public static final int FOV_149 = 149;
    public static final double COS_THRESHOLD_149 = -0.3;
    public static final int COS_THRESHOLD_181 = -1;
    public static final int FOV_181 = 181;


    public static final int N_PLANET_DEF_INTENSITY = 144;
    public static final int N_STAR_DEF_INTENSITY = 144;
    public static final int N_EYEPIECES_DEF_INTENSITY = 112;
    public static final int N_MILKY_WAY_DEF_INTENSITY = 176;
    public static final int N_CON_BOUNDARY_DEF_INTENSITY = 96;
    public static final int N_UHOR_DEF_INTENSITY = 128;
    public static final int N_TELRAD_DEF_INTENSITY = 80;
    public static final int N_CONSTELLATIONS_DEF_INTENSITY = 112;
    public static final int N_CROSS_MARKER_DEF_INTENSITY = 144;
    public static final int N_EP_DEF_INTENSITY = 112;
    public static final int N_EP_CROSS_DEF_INTENSITY = 80;
    public static final int N_LABELS_DEF_INTENSITY = 144;
    public static final int N_HOR_DEF_INTENSITY = 128;
    public static final int N_GRID_DEF_INTENSITY = 80;
    public static final int N_OBJECT_DEF_INTENSITY = 176;

    private static final int DIM_LAYER_ALPHA = 0x80;
    public static final double PGC_UPLOAD_TRESHOLD = 30.1;
    public static final double UCAC_UPLOAD_TRESHOLD = 5.1;
    public static final double TYCHO_SHORT_UPLOAD_TRSHOLD = 10.1;

    private static final String SELECT_NAME_FROM_COMPONENTS = "select name from components";
    private static final String COMP_DB = "comp.db";
    private static final String E = "E";
    private static final String W = "W";
    private static final String N = "N";
    private static final String S = "S";
    private static final String NW = "NW";
    private static final String SW = "SW";
    private static final String SE = "SE";
    private static final String NE = "NE";

    private static final String FILE_SIZE = "FILE_SIZE";
    private static final String FILE_NAME = "FILE_NAME";
    private static final String FILE_URL = "FILE_URL";

    private static final String COM_ANDROID_VENDING = "com.android.vending";


    private static final String TAG = "CustomView";
    @MarkTAG
    private static boolean FOVchanged = true;//flag for changing field of view
    private boolean onStart = true;//first run

    private List<Point> starList = new ArrayList<Point>();//points that we draw
    /**
     * contains selected object and objects from observation list
     */
    public List<AstroObject> objList = new ArrayList<AstroObject>();//dso that we draw
    public List<AstroObject> tychoList = new ArrayList<AstroObject>();
    private List<AstroObject> tychoListShort = new ArrayList<AstroObject>();
    private List<AstroObject> ucac2List = new ArrayList<AstroObject>();
    public List<AstroObject> ucac4List = new ArrayList<AstroObject>();
    public List<AstroObject> pgcList = new ArrayList<AstroObject>();
    public List<AstroObject> ngcList = new ArrayList<AstroObject>();
    public List<Point> conBoundaryList = new ArrayList<Point>();
    public List<Point> milkyWayList = new ArrayList<Point>();
    private List<ConPoint> conList = new ArrayList<ConPoint>();//constellations point that we draw

    private List<Point> labList;//N,S,W,E,etc
    private List<Point> zenithList;//zenith nadir
    //used in onTouchEvent for setting object cursor
    private float mTouchStartX = 0;
    private float mTouchCurrX = 0;
    private float mTouchStartY = 0;
    private float mTouchCurrY = 0;
    float deltaX = 0;
    float deltaY = 0;
    private float moveX = 0;//distance of move, used for quick update on FOV<=5
    private float moveY = 0;
    private boolean firstDraw = false;//flag used for quick update on FOV<=5
    private float D2R = (float) (PI / 180);
    private float R2D = 1 / D2R;
    boolean isFullscreen = false;

    private ObjCursor objc = new ObjCursor(0, 0);
    //paints for each type of objects
    private Paint starPaint;
    private Paint gridPaint;
    private Paint epPaint;//eyepieces
    private Paint epCrosshairPaint;//eyepie with crosshair
    private Paint NSEWPaint;
    private Paint conPaint;//constellations
    private Paint horPaint;//horizon
    private Paint labPaint; //horizon labels
    private Paint zenithPaint;//zenith nadir labels
    private Paint objcPaint;//object cursor
    private Paint telPaint; //telrad
    private Paint uhorPaint; //user Horizon
    private Paint txtPaint; //text messages
    private Paint uobjPaint; //user object
    private Paint dssContourPaint;
    private Paint objPaint; //object
    private Paint conBoundaryPaint;
    private Paint milkyWayPaint;

    private Paint epLabelPaint;

    private Paint planetPaint;


    AstroObject global_object;
    private final static int NUMBER_OF_FILES = 10;
    private static final Random RANDOM = new Random();
    private float oldDist;
    private PointF mid = new PointF();
    private int modeZoom = 0;

    SharedPreferences m_pm;

    //caution: used in Threads
    Thread uploadThread = null;
    Signal signal = new Signal();
    SharedData sh = new SharedData();

    //Brightness flick controls
    private int brBorder = 40; //distance of sensitive strip
    private int brDistance = 40; //vertical step counted as change
    private final float brStep = 0.1f;//1f/250; //deveider of br change
    private static float br = 0.5f;
    DssRectangles dssRectangles;

    public static final int LOADING = 20;
    public static final int STOP_LOADING = 21;
    public static final int INVALIDATE = 22;

    private int loading = 0;//tracks queue of loading messages from StarUploadThread

    /**
     * helper class to manage message at the top
     */
    class TopMessage {
        public static final int STAR_UPLOAD_PRIORITY = 0;
        public static final int DSS_UPLOAD_PRIORITY = 1;
        public static final int BATTERY_WARNING = 2;
        public static final int NEARBY_SEARCH = 3;
        List<String> list = new ArrayList<String>();

        /**
         * use your own priority, do not use priorities of others!
         * as this could make them malfunction!
         *
         * @param s         message
         * @param priority, the lower the priority value, the more urgent it is (0 - max priority). Displayed is the first non-empty message
         *                  of the highest priority
         */
        public void set(String s, int priority) {
            while (priority >= list.size()) {
                list.add("");
            }
            list.set(priority, s);
            invalidate();

        }

        /**
         * get the message to display
         */
        public void draw(Canvas canvas) {
            String message = "";
            for (String s : list) {
                if (!s.isEmpty()) {
                    message = s;
                    break;
                }
            }
            if (message.isEmpty()) return;
            canvas.drawText(message, getWidth() / 2 - txtPaint.measureText(message) / 2, txtPaint.getTextSize() + 2, txtPaint);

        }

    }

    TopMessage topMessage = new TopMessage();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == INVALIDATE) {
                invalidate();
                return;
            }

            if (msg.arg1 == LOADING) {
                loading++;
                topMessage.set(getContext().getString(R.string.loading), TopMessage.STAR_UPLOAD_PRIORITY);
                return;
            }
            if (msg.arg1 == STOP_LOADING) {
                loading--;
                if (loading == 0)
                    topMessage.set("", TopMessage.STAR_UPLOAD_PRIORITY);
                return;
            }


            if (graph_destroyed) {
                clearLists();
                return;
            }
            if (msg.arg1 == CuV.TYCHO_THREAD_ID) {


                Holder2<List<AstroObject>, Double> h = (Holder2<List<AstroObject>, Double>) msg.obj;
                double mag_limit_now = new StarUploadThread.TychoFilter().getMagLimit(Point.getFOV());
                tychoList = limitStarList(h.x, mag_limit_now);
                tychoListShort = new ArrayList<AstroObject>();//limitList(tychoListShort,mag_limit_now);
                invalidate();
                return;
            }
            if (msg.arg1 == CuV.CON_BOUNDARY_THREAD_ID) {
                Holder2<List<Point>, Double> h = (Holder2<List<Point>, Double>) msg.obj;

                conBoundaryList = h.x;

                invalidate();
                return;
            }
            if (msg.arg1 == CuV.MILKY_WAY_THREAD_ID) {
                Holder2<List<Point>, Double> h = (Holder2<List<Point>, Double>) msg.obj;

                milkyWayList = h.x;

                invalidate();
                return;
            }
            if (msg.arg1 == CuV.TYCHO_THREAD_SHORT_ID) {
                Holder2<List<AstroObject>, Double> h = (Holder2<List<AstroObject>, Double>) msg.obj;
                double mag_limit_now = new StarUploadThread.TychoFilterShort().getMagLimit(Point.getFOV());
                tychoListShort = limitStarList(h.x, mag_limit_now);
                tychoList = new ArrayList<AstroObject>();
                invalidate();
                return;
            }

            if (msg.arg1 == CuV.UCAC4_THREAD_ID) {


                Holder2<List<AstroObject>, Double> h = (Holder2<List<AstroObject>, Double>) msg.obj;
                double mag_limit_now = new StarUploadThread.UcacFilter().getMagLimit(Point.getFOV());
                ucac4List = limitStarList(h.x, mag_limit_now);
                invalidate();
                return;
            }
            if (msg.arg1 == CuV.PGC_THREAD_ID) {
                Holder2<List<AstroObject>, Double> h = (Holder2<List<AstroObject>, Double>) msg.obj;
                double mag_limit_now = new StarUploadThread.PgcFilter().getMagLimit(Point.getFOV());
                pgcList = limitList(h.x, mag_limit_now);
                invalidate();
                return;
            }

            if (msg.arg1 == CuV.NGCIC_THREAD_ID) {
                Holder2<List<AstroObject>, Double> h = (Holder2<List<AstroObject>, Double>) msg.obj;
                double mag_limit_now = new StarUploadThread.NgcFilter().getMagLimit(Point.getFOV());
                ngcList = limitNgcList(h.x, mag_limit_now);
                invalidate();
                return;
            }
            synchronized (CuV.this) {
                //when quick pinching there could be a situation that there is one result
                //for two handler request,as a result one is null. Theses arise when new calculation is perforemd
                //and overrides an old one whereas the request to the handler is not yet processed
                if (sh.uploadStarList == null || sh.uploadConList == null)
                    return;

                starList = sh.uploadStarList;
                conList = sh.uploadConList;
                sh.uploadStarList = null;
                sh.uploadConList = null;
            }
            invalidate();
        }
    };

    private boolean graph_destroyed = false;

    public void setGraphDestroyedFlag() {
        graph_destroyed = true;
    }

    public void clearLists() {
        starList = new ArrayList<Point>();
        objList = new ArrayList<AstroObject>();
        pgcList = new ArrayList<AstroObject>();
        ngcList = new ArrayList<AstroObject>();
        tychoList = new ArrayList<AstroObject>();
        tychoListShort = new ArrayList<AstroObject>();
        ucac4List = new ArrayList<AstroObject>();
        sh = new SharedData();
        uploadThread = null;
        ngcThread = null;
        pgcThread = null;
        ucac4Thread = null;
        tychoThread = null;
    }


    private boolean sizeChanged = false;
    private boolean mMoved;
    private float mFlick;


    int w;
    int h;

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        Point.setHeight(h);
        Point.setWidth(w);
        this.w = w;
        this.h = h;

        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged, w=" + w + "h=" + h);
        sizeChanged = true;

        brBorder = w / 8; //distance of sensitive strip
        brDistance = h / 8; //vertical step counted as change
    }

    private long l_init_time;

    public void init(Context context) {
        setFocusable(true);
        setFocusableInTouchMode(true);

        m_pm = PreferenceManager.getDefaultSharedPreferences(context);
        setKeepScreenOn(m_pm.getBoolean(context.getString(R.string.sleep_disable), false));
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        Point.densityDpi = dm.densityDpi;

        float gr_scale = getResources().getDimension(R.dimen.graph_scale);//mine
        float density = dm.density;
        Point.grscale = gr_scale / density / 100f;

        Log.d(TAG, "density=" + dm.densityDpi + "gr_scale=" + gr_scale);
    }

    public CuV(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public CuV(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CuV(Context context) {
        super(context);
        init(context);
    }

    private boolean limit_push_camera_stars = false;

    public void limitStarsPushCamera() {
        limit_push_camera_stars = true;
    }

    public void unlimitStarsPushCamera() {
        limit_push_camera_stars = false;
    }

    public void clearTychoList() {
        tychoList = new ArrayList<AstroObject>();
    }

    public void clearTychoListShort() {
        tychoListShort = new ArrayList<AstroObject>();
    }

    public void clearUcac2List() {
        ucac2List = new ArrayList<AstroObject>();
    }

    public void clearUcac4List() {
        ucac4List = new ArrayList<AstroObject>();
    }

    public void clearPgcList() {
        pgcList = new ArrayList<AstroObject>();
    }

    public void clearNgcList() {
        ngcList = new ArrayList<AstroObject>();
    }

    public void clearConBoundariesList() {
        conBoundaryList = new ArrayList<Point>();
    }

    public void clearMilkyWayList() {
        milkyWayList = new ArrayList<Point>();
    }

    //newFOV=false if FOV is not changed and newFOV=true if changed
    int hcount = 0;

    public void sgrChanged(final boolean newFOV) { //sgr==lst==local sidereal time, if changed need to redraw
        if (newFOV) {
            Context context = getContext();
            if (context != null && context instanceof GraphActivity) {
                ((GraphActivity) context).setTimeLabel();
            }
        }
        LabelLocations.getLabelLocations().clear();
        if (Point.getFOV() < Math.max(10.1, SettingsActivity.getDSSZoom()))
            Point.tfun = Point.slowFunctions; //exact sin and cos, needed as DSS pictures should align exactly
        else
            Point.tfun = Point.fastFunctions; //approximate but fast calculations

        if (newFOV) {
            if (Point.getFOV() > 90.1 || Point.getFOV() < CuV.TYCHO_SHORT_UPLOAD_TRSHOLD) {
                tychoListShort = new ArrayList<AstroObject>();
            } else {
                double mag_limit = new StarUploadThread.TychoFilterShort().getMagLimit(Point.getFOV());
                tychoListShort = limitStarList(tychoListShort, mag_limit);
            }

            if (Point.getFOV() > CuV.TYCHO_SHORT_UPLOAD_TRSHOLD) {
                tychoList = new ArrayList<AstroObject>();
            } else
                tychoList = limitStarList(tychoList, new StarUploadThread.TychoFilter().getMagLimit(Point.getFOV()));

            if (Point.getFOV() > UCAC_UPLOAD_TRESHOLD) {
                ucac4List = new ArrayList<AstroObject>();
            } else {
                ucac4List = limitStarList(ucac4List, new StarUploadThread.UcacFilter().getMagLimit(Point.getFOV()));
            }
            if (Point.getFOV() > PGC_UPLOAD_TRESHOLD) {
                pgcList = new ArrayList<AstroObject>();
            } else {
                pgcList = limitList(pgcList, new StarUploadThread.PgcFilter().getMagLimit(Point.getFOV()));
            }

            ngcList = limitNgcList(ngcList, new StarUploadThread.NgcFilter().getMagLimit(Point.getFOV()));

        }
        Runnable r = new Runnable() {
            public void run() {

                if (sizeChanged) {
                    UploadRec u = makeUploadRec();
                    u.raiseNewPointFlag = true;
                    u.newFOV = newFOV;
                    upload(u, false, -1);

                    if (Point.getFOV() <= SettingsActivity.getDSSZoom() && SettingsActivity.isDSSon()) {
                        Global.dss.uploadDSS(u, CuV.this, handler);
                    }
                } else {
                    hcount++;
                    if (hcount > 30) {
                        hcount = 0;
                        return;
                    }
                    handler.postDelayed(this, 20);//restarts itself until size is actually set!!!! important for upload threads as size is used for calculations!!!!
                }

            }
        };
        r.run();
        Calendar c = AstroTools.getDefaultTime(getContext());
        Point.setCurrentTime(c);

        if (dssRectangles != null && SettingsActivity.areDSScontoursOn()) {
            dssRectangles.update();
        }


        if (global_object != null) {
            global_object.raiseNewPointFlag(); //selected DSO object, eg from Query Activity
            global_object.recalculateRaDec(c);
        }

        Context context = getContext();
        if (context instanceof GraphActivity) {
            AstroObject center = ((GraphActivity) context).raDecCenter;
            if (center != null) {
                center.raiseNewPointFlag();
            }
        }
        int obsList = SettingsActivity.getSharedPreferences(getContext()).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
        Iterator it = ListHolder.getListHolder().get(obsList).iterator();
        for (; it.hasNext(); ) {
            Object o = it.next();
            if (o instanceof ObsInfoListImpl.Item) {
                ObsInfoListImpl.Item item = ((ObsInfoListImpl.Item) o);
                item.x.raiseNewPointFlag();
                item.x.recalculateRaDec(c);
            }
        }
        for (AstroObject o : tychoList) {
            o.raiseNewPointFlag();
        }
        for (AstroObject o : tychoListShort) {
            o.raiseNewPointFlag();
        }

        for (AstroObject o : ucac4List) {
            o.raiseNewPointFlag();
        }
        for (AstroObject o : pgcList) {
            o.raiseNewPointFlag();
        }
        for (AstroObject o : ngcList) {
            o.raiseNewPointFlag();
        }
        for (Point o : conBoundaryList) {
            o.raiseNewPointFlag();
        }
        for (Point p : ConFigure.connames) {
            p.raiseNewPointFlag();
        }
        for (Object o : ListHolder.getListHolder().get(InfoList.NEBULA_CONTOUR_LIST)) {
            Point p = (Point) o;
            p.raiseNewPointFlag();
        }

        for (Planet p : Global.planets) {
            p.raiseNewPointFlag(); //planets
            p.recalculateRaDec(c); //recalculate ra dec for new time
        }
        if (context instanceof GraphActivity) {
            for (Point p : ((GraphActivity) context).listNearby) {
                p.raiseNewPointFlag();
                p.recalculateRaDec(c);
            }
        }
        if (objc != null) {
            objc.raiseNewPointFlag(); //object cursor

            Point p = objc.getObjSelected();
            if (p == null) return;
            p.raiseNewPointFlag();
            p.recalculateRaDec(c);
            objc.setRaDec(p.ra, p.dec); //set new ra dec if those of planet on which it was put changed

        }

        conFigureRaiseNewPointFlag();
        for (Point p : starList) {
            p.raiseNewPointFlag();
        }
        //besides calculation takes place only when the con point is really drawn
        updateLabel();
        if (selectedConBoundary != null) {
            selectedConBoundary.raiseNewPointFlag();
        }

    }

    public void conFigureRaiseNewPointFlag() {
        for (ConPoint cp : conList) {
            cp.raiseNewPointFlag();
        }
    }

    public void FOVchanged() { //this procedure is called in parent Graph activity
        sgrChanged(true);
        ObjCursor.cllist = new ArrayList<Point>();
        ObjCursor.cllistpos = 0;
    }

    public ObjCursor getObjCursor() {//this procedure is called in parent Graph activity
        return objc;
    }

    public void setObjCursor(ObjCursor obj) {//this procedure is called in parent Graph activity
        objc = obj;

    }

    /**
     * keeps recs of whether hr in obs list is double
     */
    public void initDsoList() {
        objList = new ArrayList<AstroObject>();

        if ((global_object != null)) {
            objList.add(global_object);//adding selected object
        }
        boolean flag = m_pm.getBoolean(getContext().getString(R.string.selobj), false);//whether not no show selected objects
        int obsList = SettingsActivity.getSharedPreferences(getContext()).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
        Iterator it = ListHolder.getListHolder().get(obsList).iterator();
        for (; it.hasNext(); ) {//adding observation list
            Object o = it.next();
            if (o instanceof ObsInfoListImpl.Item) {
                ObsInfoListImpl.Item item = (ObsInfoListImpl.Item) o;
                if (flag) { //do not show selected objects
                    if (!item.y) {
                        objList.add(item.x);
                    }
                } else {
                    objList.add(item.x);
                }
            }
        }

    }

    private enum SettingsToDraw {CON_BOUNDARY, DSS_CONTOURS, GRID_TYPE, GRID_LABEL, UHOR, DSS, CONSTELLATION, OTHER_LABELS, HORIZON, EYEPIECES, TELRAD, NIGHT_MODE, ANGLE_MEASURE, FULLSCREEN, EYEPIECES_LABELS, QUINSIGHT}

    ;


    private Map<SettingsToDraw, Boolean> settingsMap = new HashMap<SettingsToDraw, Boolean>();

    /**
     * keeping true/false values for options usually kept in xml,
     * used for quicker drawing in onDraw()
     */
    public void initSettingsToDrawMap() {
        settingsMap = new HashMap<SettingsToDraw, Boolean>();
        Context context = getContext();
        settingsMap.put(SettingsToDraw.CON_BOUNDARY, SettingsActivity.isConBoundaryOn(context));
        settingsMap.put(SettingsToDraw.DSS_CONTOURS, SettingsActivity.areDSScontoursOn());

        int grid_type = SettingsActivity.getGridType();
        if (grid_type == SettingsActivity.ALTAZ_GRID)
            settingsMap.put(SettingsToDraw.GRID_TYPE, true);
        else
            settingsMap.put(SettingsToDraw.GRID_TYPE, false);

        settingsMap.put(SettingsToDraw.GRID_LABEL, SettingsActivity.isGridLabelOn());
        settingsMap.put(SettingsToDraw.UHOR, m_pm.getBoolean(context.getString(R.string.userhorizonon), false));
        settingsMap.put(SettingsToDraw.DSS, SettingsActivity.isDSSon());
        settingsMap.put(SettingsToDraw.CONSTELLATION, m_pm.getBoolean(context.getString(R.string.conston), true));
        settingsMap.put(SettingsToDraw.OTHER_LABELS, SettingsActivity.areOtherLabelsOn());
        settingsMap.put(SettingsToDraw.HORIZON, m_pm.getBoolean(context.getString(R.string.horizon), true));
        settingsMap.put(SettingsToDraw.EYEPIECES, SettingsActivity.isEpOn(context));
        settingsMap.put(SettingsToDraw.TELRAD, m_pm.getBoolean(context.getString(R.string.telradon), false));
        settingsMap.put(SettingsToDraw.QUINSIGHT, m_pm.getBoolean(context.getString(R.string.quinsighton), false));
        settingsMap.put(SettingsToDraw.NIGHT_MODE, SettingsActivity.getNightMode());
        settingsMap.put(SettingsToDraw.ANGLE_MEASURE, m_pm.getBoolean(context.getString(R.string.angle_measurement), true));
        settingsMap.put(SettingsToDraw.FULLSCREEN, SettingsActivity.isFullScreen(getContext()));
        settingsMap.put(SettingsToDraw.EYEPIECES_LABELS, SettingsActivity.isEyepiecesLabels(getContext()));
    }

    private boolean getSettingsValue(SettingsToDraw setting) {
        Boolean draw = settingsMap.get(setting);
        if (draw == null)
            return false;
        else
            return draw;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (disableDr)
            return;

        if (SettingsActivity.isObsObjectLabelOn() || SettingsActivity.isStarLabelOn()) {
            LabelLocations.getLabelLocations().init();
            LabelLocations.getLabelLocations().update();
        }
        AstroTools.setLabelPathRotation();
        if (onStart) { //first time initialisation, maybe the better place is in constructor
            initDraw();
            initCompassLabels();
            initZenithNadirLabels();
            onStart = false;
        }

        //new upload if user moved his finger more than by width/4 or /2
        if (Point.getFOV() > 149) {
            if ((abs(deltaX) > getWidth() / 32) || (abs(deltaY) > getWidth() / 32)) {

                deltaX = 0;
                deltaY = 0;
                UploadRec u = makeUploadRec();
                upload(u, false, -1);
                if (getSettingsValue(SettingsToDraw.DSS_CONTOURS))
                    dssRectangles.update();
            }
        } else if (Point.getFOV() > 45.1) {
            if ((abs(deltaX) > getWidth() / 16) || (abs(deltaY) > getWidth() / 16)) {
                deltaX = 0;
                deltaY = 0;
                UploadRec u = makeUploadRec();
                upload(u, false, -1);
                if (getSettingsValue(SettingsToDraw.DSS_CONTOURS))
                    dssRectangles.update();
            }
        } else if (Point.getFOV() > 30.1) {
            if ((abs(deltaX) > getWidth() / 8) || (abs(deltaY) > getWidth() / 8)) {
                deltaX = 0;
                deltaY = 0;
                UploadRec u = makeUploadRec();
                upload(u, false, -1);
                if (getSettingsValue(SettingsToDraw.DSS_CONTOURS))
                    dssRectangles.update();
            }
        } else if (Point.getFOV() > 15) {
            if ((abs(deltaX) > getWidth() / 4) || (abs(deltaY) > getWidth() / 4)) {
                deltaX = 0;
                deltaY = 0;
                UploadRec u = makeUploadRec();
                upload(u, false, -1);

                if (getSettingsValue(SettingsToDraw.DSS_CONTOURS)) {
                    dssRectangles.update();
                }
            }
        } else if ((abs(deltaX) > getWidth() / 2) || (abs(deltaY) > getWidth() / 2)) {

            deltaX = 0;
            deltaY = 0;
            UploadRec u = makeUploadRec();
            upload(u, false, -1);
            if (Point.getFOV() < SettingsActivity.getDSSZoom() && SettingsActivity.isDSSon()) {
                Global.dss.uploadDSS(u, this, handler);
            }
            if (getSettingsValue(SettingsToDraw.DSS_CONTOURS)) {
                dssRectangles.update();
            }
        }

        int gridtype = SettingsActivity.getGridType();
        boolean gridlabel = getSettingsValue(SettingsToDraw.GRID_LABEL);

        switch (gridtype) {
            case SettingsActivity.ALTAZ_GRID:
                Grid grid = new Grid(canvas, gridPaint, gridlabel, getContext(), fov_label_size, fov_label_height, mirror_label_size, mirror_label_height);
                grid.draw();
                break;
            case SettingsActivity.EQ_GRID:
                grid = new Grid(canvas, gridPaint, gridlabel, getContext(), fov_label_size, fov_label_height, mirror_label_size, mirror_label_height);
                grid.setEq();
                grid.draw();
                break;
        }
        boolean uhor = getSettingsValue(SettingsToDraw.UHOR);
        if (uhor) {
            uhorPaint.setStrokeWidth(SettingsActivity.getUHorWidth());
            drawUserHorizon(canvas, uhorPaint);
        }

        //Draw DSS
        if (Point.getFOV() <= SettingsActivity.getDSSZoom() && getSettingsValue(SettingsToDraw.DSS)) {
            Global.dss.clearPos();//clear position in Bitmap Array
            for (Holder2<Bitmap, Point> h = Global.dss.next(); h != null; h = Global.dss.next())
                drawDSSImage(canvas, starPaint, h.y, h.x);
        }

        if (getSettingsValue(SettingsToDraw.CONSTELLATION))
            drawConFigures(canvas, conPaint);

        drawStars(canvas, starPaint);//important that this is before dso to draw star laber first
        drawPlanets(canvas, planetPaint);
        drawDsoObj(canvas, objPaint);

        if (getSettingsValue(SettingsToDraw.DSS_CONTOURS) && Point.getFOV() < Grid.FOV_91) {
            dssRectangles.draw(canvas, dssContourPaint);
        }
        if (getSettingsValue(SettingsToDraw.OTHER_LABELS)) {
            drawLabels(canvas, labPaint);
            drawZenithLabels(canvas, zenithPaint);
        }
        if (getSettingsValue(SettingsToDraw.HORIZON))
            drawHorizonNew(canvas, horPaint); //if alt az view

        if (objc != null) { //drawing object cursor
            objc.setXY();
            objc.setDisplayXY();
            objc.draw(canvas, objcPaint);
        }

        Context contex = getContext();
        if (contex instanceof GraphActivity) {
            AstroObject center = ((GraphActivity) contex).raDecCenter;
            if (center != null) {
                center.setXY();
                center.setDisplayXY();
                center.draw(canvas, gridPaint);
            }
        }

        if (getSettingsValue(SettingsToDraw.EYEPIECES))
            drawEyePiecesFOV(canvas, epPaint);
        if (getSettingsValue(SettingsToDraw.TELRAD))
            drawTelrad(canvas, telPaint, getSettingsValue(SettingsToDraw.QUINSIGHT));
        //Battery warning (near the top edge)
        int level = SettingsActivity.getSharedPreferences(getContext()).getInt(Constants.BATTERY_LEVEL, 0);
        boolean fullscreen = getSettingsValue(SettingsToDraw.FULLSCREEN);
        topMessage.set(getContext().getString(R.string._battery_low_) + level + "% ", TopMessage.BATTERY_WARNING);
        topMessage.draw(canvas);

        drawFOVlabel(canvas, labPaint);
        drawMirrorRotateLabel(canvas, labPaint);
        if (getSettingsValue(SettingsToDraw.ANGLE_MEASURE))
            drawDstlabel(canvas, labPaint);

        drawSelectedObject(canvas);
        drawObjInfoLabel(canvas, labPaint);
        if (selectedConBoundary != null)
            selectedConBoundary.draw(canvas, conBoundaryPaint);
    }

    private void drawUserHorizon(Canvas canvas, Paint paint) {
        boolean chfill = SettingsActivity.getUHorFill();
        if (!chfill) {
            Path p = UserHorizon.getPath();
            if (p != null) canvas.drawPath(p, paint);
        } else {
            List<Path> list = UserHorizon.getFillPaths();
            if (list != null) {
                Paint paintf = new Paint(paint);
                paintf.setStyle(Paint.Style.FILL);
                for (Path path : list) {
                    canvas.drawPath(path, paintf);
                }
            }
        }

    }


    /**
     * drawing selected object second time to make it brighter
     *
     * @param canvas
     */
    private void drawSelectedObject(Canvas canvas) {
        //drawing selected object second time to make it brighter
        Point selobj = getObjCursor().getObjSelected();
        boolean planet_selected = selobj instanceof Planet;


        if (selobj != null && selobj instanceof ExtendedObject) {
            selobj.setXY();
            selobj.setDisplayXY();
            selobj.draw(canvas, planet_selected ? planetPaint : objPaint);
            selobj.draw(canvas, planet_selected ? planetPaint : objPaint);
        }
    }

    private void drawDSSImage(Canvas canvas, Paint paint, Point l, Bitmap bitmap) {
        Point loc = new Point(l);
        loc.setXY();
        loc.setDisplayXY();

        Point test = new CustomPoint(Point.getAzCenter(), Point.getAltCenter(), "");
        Point testup = new CustomPoint(Point.getAzCenter(), Point.getAltCenter() + DSS.size / 60f / 2f, "");
        testup.setXY();
        testup.setDisplayXY();
        test.setXY();
        test.setDisplayXY();
        int dim = (int) test.distanceOnDisplay(testup);

        Point n = new Point(loc.ra, loc.dec + 0.1);
        n.setXY();
        n.setDisplayXY();
        double angleNorth = atan2(n.getYd() - loc.yd, n.getXd() - loc.xd);
        float angle = (float) ((angleNorth * R2D)) + 90;
        Matrix matrix = new Matrix();
        matrix.setRotate(angle, loc.getXd(), loc.getYd());

        RectF r = null;
        r = new RectF(loc.xd - dim, loc.yd - dim, loc.xd + dim, loc.yd + dim);

        canvas.save();

        //SAND Bitmap color tinting
        int color;

        float invert[] =
                {
                        -1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                        0.0f, -1.0f, 0.0f, 1.0f, 1.0f,
                        0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                };

        Paint p;
        if (SettingsActivity.getNightMode()) {
            color = 0xff000000 + 0x00010000 * SettingsActivity.getDSSbrightness();
            p = new Paint(color);
            ColorFilter filter = new LightingColorFilter(color, 1);
            p.setColorFilter(filter);
        } else if (SettingsActivity.getInverseSky()) {
            ColorMatrix cm = new ColorMatrix(invert);
            p = new Paint();
            p.setColorFilter(new ColorMatrixColorFilter(cm));
        } else {
            try {
                color = m_pm.getInt(getContext().getString(R.string.color_dss_image), 0xffffffff);
            } catch (Exception e) {
                color = 0xffffffff;
            } //in case there is a junk in the DSS color field
            p = new Paint(color);
            ColorFilter filter = new LightingColorFilter(color, 1);
            p.setColorFilter(filter);
        }

        //SAND mirror bitmap
        if (Point.mirror < 0) {
            Matrix m = new Matrix();
            m.preScale(-1, 1);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
        }
        canvas.rotate(angle, loc.getXd(), loc.getYd());
        canvas.drawBitmap(bitmap, null, r, p);
        canvas.restore();
    }

    private void drawEyePiecesFOV(Canvas canvas, Paint initialPaint) {
        final double sin45 = 1 / Math.sqrt(2);
        double fov;
        double maxFovAbs = 0;
        double maxFovLimited = 0;
        Path path;
        Paint paint;
        int num = SettingsActivity.getEPsNumber();
        double pixelperdegree = Point.getWidth() / Point.getFOV();
        double ratio = (double) Point.getHeight() / (double) Point.getWidth();
        double limit = Point.getFOV();

        if (ratio < 1) {
            limit = Point.getFOV() * ratio;
        }


        for (int i = 0; i < num; i++) {
            EyepiecesRecord rec = SettingsActivity.getEpRecord(i);
            if (rec != null) {
                path = null;
                paint = initialPaint;
                String cross = rec.getAngleData(); //todo AK: consider making real object
                boolean isAngled = EyepiecesRecord.hasAngleData(cross);
                boolean isCCD = SettingsActivity.isCCD(SettingsActivity.getEpFocus(i));

                if (isCCD) {
                    double dec = Point.getDec(Point.getAzCenter(), Point.getAltCenter());
                    double ra = Point.getRa(Point.getAzCenter(), Point.getAltCenter());
                    double dx = rec.getFocus() - EyepiecesListActivity.OFFSET;
                    double dy = rec.getAfov() - EyepiecesListActivity.OFFSET;
                    double tFocus = SettingsActivity.getTFocus();
                    double fra = Math.atan(dx / tFocus) * R2D;
                    double fdec = Math.atan(dy / tFocus) * R2D;
                    if (fra > 0 && fra < 1000 && fdec > 0 && fdec < 1000 && Math.max(fra, fdec) * pixelperdegree > 20) {
                        path = getCCD(ra, dec, fra, fdec);
                    }
                } else { //Normal Eyepiece type
                    fov = SettingsActivity.getEpFOV(i);
                    if (fov > maxFovLimited && fov <= limit) {
                        maxFovLimited = fov;
                    }
                    if (fov > maxFovAbs) maxFovAbs = fov;
                    if (fov > 0 && fov < 1000 && fov * pixelperdegree > 20) {
                        DrawEyepieceData data = eyePiece(fov, isAngled);

                        path = data.path;

                        //Draw EP label
                        if (getSettingsValue(SettingsToDraw.EYEPIECES_LABELS)) {
                            String name = rec.getSummaryShort();
                            if (name.length() > 50) {
                                name = name.substring(0, 50);
                            }
                            Paint p = new Paint(epLabelPaint);
                            float w = p.measureText(name);
                            if (w < data.radius * 2) {

                                float offset = data.radius;
                                drawLabel(name, canvas, p, data.x, data.y + offset, -1f, 1.5f);
                            }
                        }
                    }
                }
                if (path != null && isAngled) { //Rotate it
                    Matrix mMatrix = new Matrix();
                    RectF bounds = new RectF();
                    float angle = Float.valueOf(rec.getAngleData());
                    path.computeBounds(bounds, true);
                    mMatrix.postRotate(angle, bounds.centerX(), bounds.centerY());
                    path.transform(mMatrix);
                    paint = isCCD ? initialPaint : epCrosshairPaint;
                }
                if (path != null) {
                    canvas.drawPath(path, paint);
                }
            }
        }

        //EP EQ lines
        boolean guideLine = m_pm.getBoolean(getContext().getString(R.string.eps_guide_lines_on), false);
        if (guideLine && maxFovAbs > 0 && maxFovAbs < 1000 && maxFovAbs * pixelperdegree > 20) {
            Holder2<Path, Path> h = neLine(maxFovAbs, canvas, initialPaint);
            canvas.drawPath(h.x, initialPaint);
            canvas.drawPath(h.y, initialPaint);
        }
        //EP EQ directions labels
        boolean labels = m_pm.getBoolean(getContext().getString(R.string.ep_labels), true);
        if (labels && maxFovLimited * pixelperdegree > 100)
            drawNSWE(maxFovLimited, canvas, NSEWPaint);
    }

    class DrawEyepieceData {
        Path path;
        float x;
        float y;
        float radius;

        public DrawEyepieceData(Path path, float x, float y, float radius) {
            super();
            this.path = path;
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

    }

    private DrawEyepieceData eyePiece(double fov, boolean isCrossed) {
        Point test = new CustomPoint(Point.getAzCenter(), Point.getAltCenter(), "");
        Point testup = new CustomPoint(Point.getAzCenter(), Point.getAltCenter() + fov / 2, "");
        testup.setXY();
        testup.setDisplayXY();
        test.setXY();
        test.setDisplayXY();
        float x = test.getXd();
        float y = test.getYd();
        float radius = (float) test.distanceOnDisplay(testup);

        Path circle = new Path();
        circle.addCircle(x, y, radius, Direction.CW);
        if (isCrossed) {
            //vertical
            circle.moveTo(x, y - radius);
            circle.lineTo(x, y + radius);
            //horizontal
            circle.moveTo(x - radius, y);
            circle.lineTo(x + radius, y);
        }
        DrawEyepieceData rec = new DrawEyepieceData(circle, x, y, radius);

        return rec;
    }

    private Path getCCD(double ra, double dec, double a, double b) {
        double scale = Point.getWidth() / Point.getFOV() / 2;
        Path circle = new Path();
        Point n = new Point(ra, dec + 0.1);
        n.setXY();
        n.setDisplayXY();

        Point n0 = new Point(ra, dec);
        n0.setXY();
        n0.setDisplayXY();

        double angleNorth = atan2(n.getYd() - n0.getYd(), n.getXd() - n0.getXd());
        float angle = 0;

        if (Point.mirror == Point.NO_MIRROR)
            angle = (float) ((angleNorth * 180 / PI));
        else
            angle = (float) ((angleNorth * 180 / PI));


        double xl = n0.getXd() - scale * a;
        double xr = n0.getXd() + scale * a;
        double ytop = n0.getYd() - scale * b;
        double ybot = n0.getYd() + scale * b;


        RectF rect = new RectF((float) xl, (float) ytop, (float) xr, (float) ybot);
        circle.addRect(rect, Direction.CW);

        Matrix matrix = new Matrix();
        matrix.setRotate(angle, n0.getXd(), n0.getYd());
        circle.transform(matrix);
        return circle;
    }

    private void drawLabel(String name, Canvas canvas, Paint p, float xd, float yd, float signx, float signy) {
        p.setStyle(Paint.Style.FILL);

        float size = p.getTextSize() * Point.getScalingFactor();
        float offset = 7 * Point.getScalingFactor();
        p.setTextSize(size);

        float xl = xd;
        float yl = yd;

        float w = p.measureText(name);
        FontMetrics fm = p.getFontMetrics();
        float h = (fm.descent - fm.ascent);//font height

        double angle = Point.getRotAngle();
        if (angle == 0)
            canvas.drawText(name, xl + signx * w / 2, yl + signy * h / 2, p);
        else {
            Path path = AstroTools.getLabelPath(xl, yl);
            canvas.drawTextOnPath(name, path, 0, 0, p);

        }
    }

    /**
     * drawing cardinal directions
     *
     * @param fov
     * @param canvas
     * @param paint
     */
    private void drawNSWE(double fov, Canvas canvas, Paint paint) {
        Point test = new CustomPoint(Point.getAzCenter(), Point.getAltCenter(), "");
        Point testup = new CustomPoint(Point.getAzCenter(), Point.getAltCenter() + fov / 2, "");
        testup.setXY();
        testup.setDisplayXY();
        test.setXY();
        test.setDisplayXY();
        float size = paint.getTextSize() * Point.getScalingFactor();
        float radius = (float) test.distanceOnDisplay(testup) + size;

        double dec = Point.getDec(Point.getAzCenter(), Point.getAltCenter());
        double ra = Point.getRa(Point.getAzCenter(), Point.getAltCenter());

        Point n = new Point(ra, dec + 0.1);
        n.setXY();
        n.setDisplayXY();
        Point n0 = new Point(ra, dec);
        n0.setXY();
        n0.setDisplayXY();

        double angleNorth = atan2(n.getYd() - n0.getYd(), n.getXd() - n0.getXd());

        Paint p = new Paint(paint);

        double rcos = radius * cos(angleNorth);
        double rsin = radius * sin(angleNorth);
        int signrcos = 1;
        if (rcos < 0) signrcos = -1;
        int signrsin = 1;
        if (rsin < 0) signrsin = -1;

        drawLabel("N", canvas, p, (float) (test.getXd() + rcos), (float) (test.getYd() + rsin), -1f, 0.5f);
        p = new Paint(paint);
        drawLabel("S", canvas, p, (float) (test.getXd() - rcos), (float) (test.getYd() - rsin), -1, 0.5f);
        p = new Paint(paint);
        drawLabel(Point.mirror == 1 ? "W" : "E", canvas, p, (float) (test.getXd() - rsin), (float) (test.getYd() + rcos), -1, 0.5f);
        p = new Paint(paint);
        drawLabel(Point.mirror == 1 ? "E" : "W", canvas, p, (float) (test.getXd() + rsin), (float) (test.getYd() - rcos), -1, 0.5f);
    }

    private Holder2<Path, Path> neLine(double fov, Canvas canvas, Paint paint) {
        Point test = new CustomPoint(Point.getAzCenter(), Point.getAltCenter(), "");
        Point testup = new CustomPoint(Point.getAzCenter(), Point.getAltCenter() + fov / 2, "");
        testup.setXY();
        testup.setDisplayXY();
        test.setXY();
        test.setDisplayXY();
        float radius = (float) test.distanceOnDisplay(testup);
        Path line1 = new Path();
        line1.moveTo(test.getXd(), test.getYd() - radius);
        line1.lineTo(test.getXd(), test.getYd() + radius);

        Path line2 = new Path(line1);

        double dec = Point.getDec(Point.getAzCenter(), Point.getAltCenter());
        double ra = Point.getRa(Point.getAzCenter(), Point.getAltCenter());

        Point n = new Point(ra, dec + 0.1);
        n.setXY();
        n.setDisplayXY();
        Point n0 = new Point(ra, dec);
        n0.setXY();
        n0.setDisplayXY();

        double angleNorth = atan2(n.getYd() - n0.getYd(), n.getXd() - n0.getXd());
        float angle = (float) ((angleNorth * R2D)) + 90;

        Matrix matrix1 = new Matrix();
        matrix1.setRotate((float) (angleNorth * R2D), n0.getXd(), n0.getYd());

        Matrix matrix2 = new Matrix();
        matrix2.setRotate(angle, n0.getXd(), n0.getYd());

        line1.transform(matrix1);
        line2.transform(matrix2);

        return new Holder2<Path, Path>(line1, line2);

    }

    private void drawTelrad(Canvas canvas, Paint paint, boolean quinsight) {
        final int NPOINTS = quinsight ? 7 : 5;
        float x = (float) Point.getAzCenter();
        float y = (float) Point.getAltCenter();

        Point[] p = new Point[NPOINTS];
        for (int i = 0; i < NPOINTS; i++) {
            p[i] = new CustomPoint(x, y + SettingsActivity.getTr(i) / 2, "");
            p[i].setXY();
            p[i].setDisplayXY();
        }

        Point center = new CustomPoint(x, y, "");
        center.setXY();
        center.setDisplayXY();
        x = center.getXd();
        y = center.getYd();

        final float[] d = {0, 0, 0, 0, 0};
        d[0] = (float) p[0].distanceOnDisplay(center);
        d[1] = (float) p[1].distanceOnDisplay(center);
        d[2] = (float) p[2].distanceOnDisplay(center);
        if (quinsight) {
            d[3] = (float) p[5].distanceOnDisplay(center);
            d[4] = (float) p[6].distanceOnDisplay(center);
        }
        Path telrad = new Path();
        telrad.addCircle(x, y, d[0], Direction.CW);
        telrad.addCircle(x, y, d[1], Direction.CW);
        telrad.addCircle(x, y, d[2], Direction.CW);
        if (quinsight) {
            telrad.addCircle(x, y, d[3], Direction.CW);
            telrad.addCircle(x, y, d[4], Direction.CW);
        }
        float in = d[quinsight ? 4 : 2];
        float out = d[quinsight ? 2 : 1];
        telrad.moveTo(x, y + in);
        telrad.lineTo(x, y + out); //top
        telrad.moveTo(x, y - in);
        telrad.lineTo(x, y - out); //bottom
        telrad.moveTo(x - in, y);
        telrad.lineTo(x - out, y); //left
        telrad.moveTo(x + in, y);
        telrad.lineTo(x + out, y); //right


        float angle = 0;
        if (SettingsActivity.isAutoRotationOn()) {
            Point test = new CustomPoint(Point.getAzCenter(), Point.getAltCenter(), "");
            test.setXY();
            test.setDisplayXY();
            Point testup = new CustomPoint(Point.getAzCenter(), Point.getAltCenter() + 0.1, "");
            testup.setXY();
            testup.setDisplayXY();
            double angleNorth = atan2(testup.getYd() - test.yd, testup.getXd() - test.xd);
            angle = (float) ((angleNorth * 180 / Math.PI)) + 90;

        }

        canvas.save();
        canvas.rotate(angle - SettingsActivity.getTr(3), x, y);
        paint.setStrokeWidth(SettingsActivity.getTr(4));

        canvas.drawPath(telrad, paint);
        canvas.restore();
    }

    /**
     * return color based on alfa chanel (brightness)
     *
     * @param resid R.string of the preference resource
     * @param def   default value
     * @return
     */
    public int getColor(int resid, int def) {
        String bright = m_pm.getString(getContext().getString(resid), String.valueOf(def));
        int brightness = SettingsActivity.getInt(bright, 122, 0, 255);
        return (brightness << 24) | (0xff << 16);
    }

    private float getLabelScale() {
        return SettingsActivity.getLabelsScale();
    }

    public void initDraw() {
        Point.setHeight(getHeight());
        Point.setWidth(getWidth());
        Point.setCuVType(callType);

        SettingsActivity.setDSOsettings();
        SettingsActivity.setDSSbrightness();
        SettingsActivity.setDSSZoom();

        isFullscreen = SettingsActivity.isFullScreen(getContext());

        float labelsScale = getLabelScale();
        Log.d(TAG, "scaleX=" + Point.getAngleDimensionX());
        starPaint = new Paint();
        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setAntiAlias(Global.antialiasing);
        starPaint.setTextSize(20 * Point.getScalingFactor() * labelsScale);

        HrStar.setStarImage(getResources(), R.drawable.star16x16);

        objPaint = null;
        objPaint = new Paint();
        objPaint.setStrokeWidth(2);
        objPaint.setStyle(Paint.Style.STROKE);
        objPaint.setTextSize(20 * labelsScale);
        objPaint.setAntiAlias(Global.antialiasing);

        gridPaint = new Paint();//cyan
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1);
        gridPaint.setAntiAlias(Global.antialiasing);
        gridPaint.setTextSize(20 * Point.getScalingFactor());

        horPaint = new Paint();//cyan
        horPaint.setStyle(Paint.Style.STROKE);
        horPaint.setStrokeWidth(5);
        horPaint.setAntiAlias(Global.antialiasing);

        labPaint = new Paint();//cyan
        labPaint.setStyle(Paint.Style.FILL);
        labPaint.setStrokeWidth(1);
        labPaint.setTextSize(35);//adjustment is made in custom point draw method
        labPaint.setAntiAlias(Global.antialiasing);

        zenithPaint = new Paint();//cyan
        zenithPaint.setStyle(Paint.Style.FILL);
        zenithPaint.setStrokeWidth(1);
        zenithPaint.setTextSize(20);
        zenithPaint.setAntiAlias(Global.antialiasing);

        epPaint = new Paint();
        epPaint.setStyle(Paint.Style.STROKE);
        epPaint.setStrokeWidth(2);
        PathEffect effect = new DashPathEffect(new float[]{5, 5}, 1);
        epPaint.setPathEffect(effect);
        epPaint.setAntiAlias(Global.antialiasing);

        epCrosshairPaint = new Paint();
        epCrosshairPaint.setStyle(Paint.Style.STROKE);
        epCrosshairPaint.setStrokeWidth(8); //todo possibly make a user setting
        epCrosshairPaint.setAntiAlias(Global.antialiasing);

        epLabelPaint = new Paint();
        epLabelPaint.setStyle(Paint.Style.FILL);
        epLabelPaint.setStrokeWidth(1);
        epLabelPaint.setTextSize(22);//adjustment is made in custom point draw method
        epLabelPaint.setAntiAlias(Global.antialiasing);

        NSEWPaint = new Paint();
        NSEWPaint.setStyle(Paint.Style.FILL);
        NSEWPaint.setTextSize(25);
        NSEWPaint.setAntiAlias(Global.antialiasing);

        objcPaint = new Paint();
        objcPaint.setStyle(Paint.Style.STROKE);
        objcPaint.setAntiAlias(Global.antialiasing);

        dssContourPaint = new Paint();
        dssContourPaint.setStyle(Paint.Style.STROKE);
        dssContourPaint.setAntiAlias(Global.antialiasing);

        conPaint = new Paint();
        conPaint.setStyle(Paint.Style.STROKE);
        conPaint.setStrokeWidth(1);
        conPaint.setAntiAlias(Global.antialiasing);

        telPaint = new Paint();
        telPaint.setStyle(Paint.Style.STROKE);
        telPaint.setAntiAlias(Global.antialiasing);

        uhorPaint = new Paint();
        uhorPaint.setStyle(Paint.Style.STROKE);
        uhorPaint.setStrokeWidth(SettingsActivity.getUHorWidth());
        uhorPaint.setAntiAlias(Global.antialiasing);

        uobjPaint = new Paint();
        uobjPaint.setStyle(Paint.Style.STROKE);
        uobjPaint.setStrokeWidth(1);
        uobjPaint.setAntiAlias(Global.antialiasing);

        txtPaint = new Paint();
        txtPaint.setStyle(Paint.Style.FILL);
        txtPaint.setStrokeWidth(1);
        txtPaint.setTextSize(17);
        txtPaint.setAntiAlias(Global.antialiasing);

        conBoundaryPaint = new Paint();
        conBoundaryPaint.setStyle(Paint.Style.STROKE);
        conBoundaryPaint.setStrokeWidth(1);
        conBoundaryPaint.setAntiAlias(Global.antialiasing);
        conBoundaryPaint.setTextSize(35);

        milkyWayPaint = new Paint();
        milkyWayPaint.setStyle(Paint.Style.STROKE);
        milkyWayPaint.setStrokeWidth(1);
        milkyWayPaint.setAntiAlias(Global.antialiasing);
        PathEffect effect2 = new DashPathEffect(new float[]{2, 4}, 1);
        if (SettingsActivity.getInverseSky() || SettingsActivity.getNightMode())
            effect2 = new DashPathEffect(new float[]{2, 2}, 1);
        milkyWayPaint.setPathEffect(effect2);

        planetPaint = new Paint();
        planetPaint.setStyle(Paint.Style.STROKE);
        planetPaint.setStrokeWidth(2);
        planetPaint.setAntiAlias(Global.antialiasing);
        planetPaint.setTextSize(20 * Point.getScalingFactor() * labelsScale);

        //Deafults will never be used as we have setDefaultColors calles in DSOmain
        //if need to change look there!
        if (SettingsActivity.getNightMode()) { //Nightmode, set all colors red
            starPaint.setColor(getColor(R.string.ncolor_star, N_STAR_DEF_INTENSITY));//0x90ff0000
            objPaint.setColor(getColor(R.string.ncolor_object, N_OBJECT_DEF_INTENSITY));//0xB0ff0000);
            dssContourPaint.setColor(0xA0ff0000);
            gridPaint.setColor(getColor(R.string.ncolor_grid, N_GRID_DEF_INTENSITY));//0x50ff0000);
            horPaint.setColor(getColor(R.string.ncolor_horizon, N_HOR_DEF_INTENSITY));//0x80ff0000);
            labPaint.setColor(getColor(R.string.ncolor_labels, N_LABELS_DEF_INTENSITY));//0x90ff0000);
            zenithPaint.setColor(0x50ff0000);
            epPaint.setColor(getColor(R.string.ncolor_eyepieces, N_EP_DEF_INTENSITY));//0x70ff0000);
            epCrosshairPaint.setColor(getColor(R.string.ncolor_ep_cross, N_EP_CROSS_DEF_INTENSITY));//0x20ff0000);
            NSEWPaint.setColor(0x90ff0000);
            objcPaint.setColor(getColor(R.string.ncolor_crossmarker, N_CROSS_MARKER_DEF_INTENSITY));//0x90ff0000);
            conPaint.setColor(getColor(R.string.ncolor_constellations, N_CONSTELLATIONS_DEF_INTENSITY));//0x70ff0000);
            telPaint.setColor(getColor(R.string.ncolor_telrad, N_TELRAD_DEF_INTENSITY));//0x50ff2100);
            uhorPaint.setColor(getColor(R.string.ncolor_user_horizon, N_UHOR_DEF_INTENSITY));//0x805f0000);
            uobjPaint.setColor(0xa05f0000);
            txtPaint.setColor(0x90ff0000);
            conBoundaryPaint.setColor(getColor(R.string.ncolor_con_boundary, N_CON_BOUNDARY_DEF_INTENSITY));//0x60ff0000);
            milkyWayPaint.setColor(getColor(R.string.ncolor_milky_way, N_MILKY_WAY_DEF_INTENSITY));//0xB0ff0000);

            epLabelPaint.setColor(getColor(R.string.ncolor_eyepieces, N_EYEPIECES_DEF_INTENSITY));

            planetPaint.setColor(getColor(R.string.ncolor_planet, N_PLANET_DEF_INTENSITY));//0x90ff0000);

        } else if (SettingsActivity.getInverseSky()) {
            starPaint.setColor(0xff000000);
            objPaint.setColor(m_pm.getInt(getContext().getString(R.string.icolor_object), 0xd0ff00ff));
            dssContourPaint.setColor(m_pm.getInt(getContext().getString(R.string.icolor_object), 0xd0ff00ff));
            gridPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_GRID, 0xc033ccff));
            horPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_HORIZON, 0xc00000ff));
            labPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_LABELS, 0xd0f0c000));
            zenithPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_LABELS, 0xd0f0c000));
            epPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_EYEPIECES, 0xd0df0000));//red
            epCrosshairPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_EP_CROSS, 0xc0ff0000));//red
            epLabelPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_EYEPIECES, 0xd0df0000));
            NSEWPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_LABELS, 0xd0f0c000));//red

            objcPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_CROSS_MARKER, 0xd0ff0000));//red
            conPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_CONSTELLATIONS, 0xd0008f00));//green
            telPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_TELRAD, 0xd0ffb000));//yellish red
            uhorPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_USER_HORIZON, 0xc00090ff));
            uobjPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_USER_OBJECT, 0xd0ff00ff));//green
            txtPaint.setColor(m_pm.getInt(SettingsActivity.ICOLOR_TEXT, 0xd0ff0000));
            conBoundaryPaint.setColor(m_pm.getInt("i" + getContext().getString(R.string.color_con_boundary), SettingsActivity.DEFAULT_ICOLOR_CON_BOUNDARY));
            milkyWayPaint.setColor(m_pm.getInt("i" + getContext().getString(R.string.color_milky_way), SettingsActivity.DEFAULT_ICOLOR_MILKY_WAY));
            planetPaint.setColor(0x90ff0000);
        } else { //Colors for day mode, dark sky
            starPaint.setColor(0xA0ffffff);//white
            objPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_object), 0xB000ffbb));
            dssContourPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_object), 0xB0f0f0ff));
            gridPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_grid), 0x4000ffff));
            horPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_horizon), 0x8000ffff));
            labPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_labels), 0x8000ffff));
            zenithPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_labels), 0x8000ffff));
            epPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_eyepieces), 0x60ff0000));//red
            epCrosshairPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_ep_cross), 0x30ff0000));//red
            epLabelPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_eyepieces), 0x60ff0000));
            NSEWPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_labels), 0x8000ffff));

            objcPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_crossmarker), 0x90ff0000));//red
            conPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_constellations), 0x7000ff00));//green
            telPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_telrad), 0x40ff4100));//yellish red
            uhorPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_user_horizon), 0x8000ffff));
            uobjPaint.setColor(m_pm.getInt(SettingsActivity.COLOR_USER_OBJECT, 0xd000ff00));//green
            txtPaint.setColor(m_pm.getInt(SettingsActivity.COLOR_TEXT, 0x80ffffff));
            conBoundaryPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_con_boundary), SettingsActivity.DEFAULT_COLOR_CON_BOUNDARY));
            milkyWayPaint.setColor(m_pm.getInt(getContext().getString(R.string.color_milky_way), SettingsActivity.DEFAULT_COLOR_MILKY_WAY));
            planetPaint.setColor(0x90ff0000);
        }

        Global.lockCursor = false;
        dssRectangles = new DssRectangles(CuV.this);
        if (SettingsActivity.areDSScontoursOn())
            dssRectangles.update();//initialising
    }

    private void initCompassLabels() {
        labList = new ArrayList<Point>();
        Point south = new CustomPoint(180, 0, S);
        labList.add(south);
        Point north = new CustomPoint(0, 0, N);
        labList.add(north);
        Point west = new CustomPoint(270, 0, W);
        labList.add(west);
        Point east = new CustomPoint(90, 0, E);
        labList.add(east);
        labList.add(new CustomPoint(45, 0, NE));
        labList.add(new CustomPoint(135, 0, SE));
        labList.add(new CustomPoint(225, 0, SW));
        labList.add(new CustomPoint(315, 0, NW));
    }

    Point zenith;
    Point nadir;

    private void initZenithNadirLabels() {
        zenithList = new ArrayList<Point>();
        zenith = new CustomPoint(0, 90, getContext().getString(R.string.zenith));
        nadir = new CustomPoint(0, -90, getContext().getString(R.string.nadir));
        zenithList.add(zenith);
        zenithList.add(nadir);
    }

    //for star chart movement
    Point zenith2 = new CustomPoint(0, 90, "");
    Point nadir2 = new CustomPoint(0, -90, "");

    private void drawLabels(Canvas canvas, Paint paint) {
        for (Point p : labList) {
            p.setXY();
            p.setDisplayXY();
            p.draw(canvas, paint);
        }
    }

    private void drawZenithLabels(Canvas canvas, Paint paint) {
        for (Point p : zenithList) {
            p.setXY();
            p.setDisplayXY();
            p.draw(canvas, paint);
        }
    }

    public void vibrate() {

        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null)
            vibrator.vibrate(SettingsActivity.getVibrationLength(getContext()));
    }

    //SAND:for multitouch
    private float spacing(MotionEvent event) {
        try {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) sqrt(x * x + y * y); //was FloatMath. (more efficient)
        } catch (Exception e) {
            return 0;
        }
    }

    private void midPoint(PointF point, MotionEvent event) { //was PointF
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    private GraphActivity getGraphActivity() {
        Context context = getContext();
        if (context != null && context instanceof GraphActivity) {
            return (GraphActivity) context;
        }
        return null;
    }

    private Point prevCursorObj = null;

    private void objCursorMake(float x, float y) {
        ObjCursor o = new ObjCursor(x, y);
        List<Point> list;
        if (!limit_push_camera_stars) {
            list = new ArrayList<Point>(starList);
        } else {
            list = new ArrayList<Point>();
            for (Point s : starList) {
                if (s instanceof AstroObject) {
                    if (((AstroObject) s).getMag() > Prefs.PUSH_CAMERA_CALIBRATION_LIMIT)
                        continue;
                    else
                        list.add(s);
                }

            }
        }
        if (!limit_push_camera_stars) {
            list.addAll(tychoList);
            list.addAll(tychoListShort);
            list.addAll(ucac2List);
            list.addAll(ucac4List);
            list.addAll(ngcList);
            list.addAll(pgcList);
            list.addAll(objList);
        }
        list.addAll(Global.planets);
        Context contex = getContext();

        if (contex instanceof GraphActivity && !limit_push_camera_stars) {
            list.addAll(((GraphActivity) contex).listNearby);

            AstroObject center = ((GraphActivity) contex).raDecCenter;
            if (center != null) {
                list.add(center);
            }
        }
        Point p = o.closestPoint(list); //closest point
        if (p != null) {
            ObjCursor.setParameters(m_pm);

            o.setRaDec(p.getRa(), p.getDec());
            prevCursorObj = objc.getObjSelected();
            objc = o;

            objc.setObjSelected(p); //associating object cursor with the object found
            updateLabel();
            invalidate();

            vibrate();
            boolean isPushCameraMode = SettingsActivity.isPushCameraOn(getContext());

            if (isPushCameraMode) {
                GraphActivity activity = getGraphActivity();
                if (activity != null)
                    activity.getPushCamPresenter().setSelectedObject(p);
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (ALEX_MENU_FLAG && keyCode == KeyEvent.KEYCODE_MENU) {
            Context contex = getContext();
            if (contex instanceof GraphActivity) {
                ((GraphActivity) contex).doMenu(this);
                return true; //always eat it!
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (process_long_touch_flag) {
            gDetector.onTouchEvent(event);
        }

        float scaleRa = (float) (Point.getFOV() / Point.getWidth() * 24 / 360);
        double co;
        if (!Point.coordSystem)
            co = cos(Point.getDecCenter() * D2R);
        else
            co = cos(Point.getAltCenter() * D2R);
        if (abs(co) > 0.0001)
            scaleRa = (float) (scaleRa / co);
        float scaleDec = (float) (Point.getFOV() / Point.getWidth());
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                mTouchCurrX = x;
                mTouchCurrY = y;

                mTouchStartX = mTouchCurrX;
                mTouchStartY = mTouchCurrY;
                mMoved = false;
                mFlick = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if (modeZoom > 0) {
                    if (modeZoom == 1) { //prevent continuous zoom and fight scroll on modeZoom reset
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            if (Math.abs(newDist - oldDist) > 20f) {//large enough pinch
                                Context contex = getContext();
                                if (contex instanceof GraphActivity) {
                                    if (((GraphActivity) contex).zoomChange(newDist - oldDist > 0)) { //can do the zoom
                                        oldDist = newDist;
                                        modeZoom = 2;
                                    }
                                } else {//CuV by itself
                                    if (newDist - oldDist > 0)
                                        zoomIn();
                                    else
                                        zoomOut();
                                    oldDist = newDist;
                                    modeZoom = 2;

                                }
                            }
                            mMoved = true;
                        }
                    }
                } else { //not zoom mode
                    mTouchCurrX = x;
                    mTouchCurrY = y;
                    float dx = mTouchCurrX - mTouchStartX;
                    float dy = mTouchCurrY - mTouchStartY;
                    if (dx * dx < 100 && dy * dy < 49) return true; //cut noise

                    //Else it's real move
                    mMoved = true;

                    //detect fling on left side of the scr for brightness
                    if (mTouchCurrX < brBorder && mTouchStartX < brBorder) {
                        mFlick = dy;
                        return true;
                    }

                    //Pan
                    double ranew = 0;
                    double decnew = 0;
                    if (!Point.coordSystem) {
                        ranew = (mTouchCurrX - mTouchStartX) * scaleRa + Point.getRaCenter();
                        decnew = (mTouchCurrY - mTouchStartY) * scaleDec + Point.getDecCenter();

                        if (decnew > 89.5) decnew = 89.5;
                        if (decnew < -89.5) decnew = -89.5;
                        Point.setCenter(ranew, decnew);
                    } else {
                        int signx = 1;
                        int signy = 1;
                        if (Point.orientationAngle == 180) { //user wants upside down view
                            signx = -signx;
                            signy = -signy;
                        }
                        if (Point.mirror == -1) //mirror view
                            signx = -signx;

                        //new Alt and Az of the screen center after finger moves accross display
                        //ranew and decnew are actually az and alt here


                        double dxf = dx * Point.tfun.cos(Point.rot_angle) + dy * Point.tfun.sin(Point.rot_angle);
                        double dyf = -dx * Point.tfun.sin(Point.rot_angle) + dy * Point.tfun.cos(Point.rot_angle);
                        zenith2.setXY();
                        zenith2.setDisplayXY();
                        nadir2.setXY();
                        nadir2.setDisplayXY();
                        boolean zenithWithinBounds = Point.withinBounds(zenith2.getXd(), zenith2.getYd());
                        boolean nadirWithinBounds = Point.withinBounds(nadir2.getXd(), nadir2.getYd());

                        if (zenithWithinBounds || nadirWithinBounds) {
                            Point p = null;
                            if (zenithWithinBounds) p = zenith2;
                            if (nadirWithinBounds) p = nadir2;
                            double zx = p.getXd();
                            double zy = p.getYd();

                            double dstx = (mTouchCurrX - p.getXd());
                            double dsty = (mTouchCurrY - p.getYd());
                            double dstToZenith = Math.sqrt(dstx * dstx + dsty * dsty);
                            double angle = dstToZenith * scaleDec;//angle distance between point of touch and zenith / nadir
                            if (angle > Point.getFOV() / 10) {
                                if (angle > 90)
                                    angle = 90;
                                double angleR = angle * D2R;    //angle distance in radians
                                double scaleRaa = (Point.getFOV() / Point.getWidth() * 24 / 360) / Math.sin(angleR);//to account for the length of 1 degree in pixels at the given altitude (90-angle)

                                double dsp = dxf;
                                if (Point.rot_angle == 0) {


                                    double ds = Math.sqrt(dxf * dxf + dyf * dyf);//length of movement

                                    double cosb = dxf / ds;//b - angle of movement to x axis
                                    double sinb = -dyf / ds;
                                    double cosa = (mTouchCurrY - zy) / dstToZenith;//a - angle of line between touch point and zenith / nadir and y axis
                                    double sina = (zx - mTouchCurrX) / dstToZenith;

                                    dsp = ds * (cosa * cosb - sina * sinb);    //projection of movement to the line tangential to the altitude circle=ds*cos(a+b)
                                    if (nadirWithinBounds) {

                                        dsp = -dsp;
                                        if (zenithWithinBounds && (zenith2.getYd() > mTouchCurrY))
                                            dsp = -dsp;
                                    }
                                }

                                ranew = -signx * (dsp) * scaleRaa / 12 * 180 + Point.getAzCenter();
                            } else
                                ranew = Point.getAzCenter();
                        } else
                            ranew = -signx * (dxf) * scaleRa / 12 * 180 + Point.getAzCenter();

                        decnew = signy * (dyf) * scaleDec + Point.getAltCenter();


                        //do not allow to go over poles
                        double delta = 0;

                        if (decnew > 90 - delta) decnew = 90 - delta;
                        if (decnew < -90 + delta) decnew = -90 + delta;

                        double az = Point.getAzCenter();
                        double alt = Point.getAltCenter();
                        double ra = Point.getRa(az, alt);
                        double dec = Point.getDec(az, alt);
                        Point p = new Point(ra, dec);
                        p.setXY();
                        p.setDisplayXY();
                        float xd = p.getXd();
                        float yd = p.getYd();

                        Point.setCenterAz(ranew, decnew); //setting new alt and az of the center
                        p = new Point(ra, dec);
                        p.setXY();
                        p.setDisplayXY();
                        moveX += p.getXd() - xd;
                        moveY += p.getYd() - yd;

                        Context contex = getContext();
                        if (contex instanceof GraphActivity)
                            ((GraphActivity) contex).setCenterLoc();
                    }

                    deltaX += mTouchCurrX - mTouchStartX;
                    deltaY += mTouchCurrY - mTouchStartY;
                    mTouchStartX = mTouchCurrX;
                    mTouchStartY = mTouchCurrY;


                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (!mMoved) { //then it was a simple touch
                    if (!Global.lockCursor) {
                        //looking for closest object if touch point is close to the previous touch
                        objCursorMake(x, y);
                    }
                } else { //some movement detected
                    if (mFlick != 0 && SettingsActivity.isFlickBrightnessEnabled()) { //Update brightness value
                        SettingsSystemActivity.dsoMainRedrawRequired = true;
                        br = SettingsActivity.getBrightness(); //usually -1 = auto
                        Context contex = getContext();
                        if (contex instanceof GraphActivity) {
                            Activity activity = (GraphActivity) contex;
                            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                            if (br == -1) {
                                br = lp.screenBrightness; //middle
                                if (br == -1) {
                                    try {
                                        float curBrightnessValue = android.provider.Settings.System.getInt(
                                                activity.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
                                        br = curBrightnessValue / 255f;

                                    } catch (SettingNotFoundException e) {
                                        // TODO Auto-generated catch block
                                        br = 0.5f;
                                    }
                                }
                            }
                            br -= br * mFlick / brDistance / 10;

                            if (br < SettingsActivity.MIN_BRIGHTNESS) {
                                br = SettingsActivity.MIN_BRIGHTNESS;
                                InputDialog.message(getContext(), R.string.min, 750).show();
                            } else if (br > 1) {
                                br = 1f;
                                InputDialog.message(getContext(), R.string.max, 750).show();
                            }

                            //update Sky brightness
                            SettingsActivity.updateBrightness(br);

                            lp.screenBrightness = br; //Settings.getDimmer();
                            activity.getWindow().setAttributes(lp);

                            mFlick = 0; //reset
                        }
                    }
                }
                mMoved = false;
                modeZoom = 0;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: //second finger
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    midPoint(mid, event);
                    modeZoom = 1;
                }
                break;
        }
        return true;
    }

    //---------------------------------------------------------------
    //Updating alt, az coords of the selected object,
    //  adjusted figures given if calibration is enabled (see Calibrate)
    private void updateLabel() {
        if (objc != null) {
            Point p = objc;
            if (!(objc.getObjSelected() instanceof Planet)) {
                p = AstroTools.precession(p, AstroTools.getDefaultTime(getContext()));
            }
            double lat = SettingsActivity.getLattitude();
            Holder2<Double, Double> pos = AstroTools.getAzAlt(Point.getLST(), lat, p.ra, p.dec);

            boolean adj = false;
            if (SettingsActivity.isCalibrationEnabled()) {
                long last_time = SettingsActivity.getSharedPreferences(getContext()).getLong(Constants.ALIGN_TIME, -1);
                long now = Calendar.getInstance().getTimeInMillis();
                if (last_time != -1 && (now - last_time) < 24 * 3600 * 1000) { //do not draw adjusted alt/az for outdated alignment
                    float adj_alt = SettingsActivity.getSharedPreferences(getContext()).getFloat(Constants.ALIGN_ADJ_ALT, 0);
                    float adj_az = SettingsActivity.getSharedPreferences(getContext()).getFloat(Constants.ALIGN_ADJ_AZ, 0);

                    boolean orient = SettingsActivity.getSharedPreferences(getContext()).getBoolean(GraphActivity.CALIBR_ORIENT, false);
                    pos.y = pos.y - adj_alt;
                    if (orient)
                        pos.x = AstroTools.normalise(pos.x - adj_az);
                    else
                        pos.x = AstroTools.normalise(adj_az - pos.x);
                    adj = true;
                }
            }
            double mag = 0;
            if (objc.getObjSelected() instanceof AstroObject) {
                AstroObject obj = (AstroObject) objc.getObjSelected();
                mag = obj.getMag();


            }
            String s;

            if (!Double.isNaN(mag))
                s = String.format(Locale.US, "%.1f" + (adj ? "m%%" : "m ") + "%04.1f%+05.1f", mag, pos.x, pos.y);
            else
                s = String.format(Locale.US, (adj ? "%%" : "") + "%04.1f%+05.1f", pos.x, pos.y);

            //label is accessed via Graph activity
            Context contex = getContext();
            if (contex instanceof GraphActivity) {
                ((GraphActivity) contex).setLocationLabel(s);  //if adjusted looks like A%h instead of A/h
                ((GraphActivity) contex).setObjName();
            } else {
                objName = getSelectedObjectName();
                objInfo = s;
            }

        }
    }

    public String getSelectedObjectName() {
        String label = getContext().getString(R.string.no_data2);
        if (getObjCursor() != null) {
            Point obj = getObjCursor().getObjSelected();
            boolean obslist_object = false;
            boolean indicate_obs = PreferenceManager.
                    getDefaultSharedPreferences(getContext()).getBoolean(getContext().getString(R.string.obs_colon), false);
            if (obj instanceof ObjectInfo) {
                if (obj instanceof AstroObject) {


                    AstroObject o = (AstroObject) obj;
                    if (indicate_obs) {
                        for (AstroObject p : objList) {
                            if (p.getCatalog() == o.getCatalog() && p.getId() == o.getId()) {
                                obslist_object = true;
                            }

                        }
                    }


                    if (o.getCatalog() > NgcFactory.LAYER_OFFSET) {
                        AstroObject ob = GraphActivity.getDbObj(o.getCatalog() - NgcFactory.LAYER_OFFSET, o.getId(), getContext());
                        if (ob != null) {
                            obj = ob;
                        }
                    }
                }

                label = ((ObjectInfo) obj).getShortName() + (obslist_object ? ":" : "");
            }

        }
        return label;

    }


    public static UploadRec makeUploadRec() {
        UploadRec u = new UploadRec();
        u.azCenter = Point.getAzCenter();
        u.altCenter = Point.getAltCenter();
        u.raCenter = Point.getRa(u.azCenter, u.altCenter);
        u.decCenter = Point.getDec(u.azCenter, u.altCenter);
        u.FOV = Point.getFOV();
        u.height = Point.getHeight();
        u.width = Point.getWidth();
        u.raiseNewPointFlag = false;
        u.newFOV = false;
        u.clearcache = false;
        return u;
    }

    public static List<AstroObject> limitList(List<AstroObject> list, double mag_limit) {
        List<AstroObject> ilist = new ArrayList<AstroObject>();
        for (AstroObject obj : list) {
            double mag = obj.getMag();
            if (SettingsActivity.showVisibleStatus())
                ilist.add(obj);
            else if (mag <= mag_limit || (Double.isNaN(mag) && mag_limit == StarMags.PGC_MAX))
                ilist.add(obj);

        }
        return ilist;

    }

    public static List<AstroObject> limitStarList(List<AstroObject> list, double mag_limit) {
        List<AstroObject> ilist = new ArrayList<AstroObject>();
        for (AstroObject obj : list) {
            double mag = obj.getMag();

            if (mag <= mag_limit || (Double.isNaN(mag) && mag_limit == StarMags.PGC_MAX))
                ilist.add(obj);

        }
        return ilist;

    }

    public static List<AstroObject> limitNgcList(List<AstroObject> list, double mag_limit) {

        List<AstroObject> ilist = new ArrayList<AstroObject>();
        for (AstroObject obj : list) {
            double mag = obj.getMag();
            if (SettingsActivity.showVisibleStatus())
                ilist.add(obj);
            else {
                if (Double.isNaN(mag) && !SettingsActivity.isZeroMagOn())
                    continue;
                if (mag <= mag_limit || Double.isNaN(mag))
                    ilist.add(obj);
            }

        }
        return ilist;

    }

    StarUploadThread tychoThread;
    StarUploadThread tychoThreadShort;
    StarUploadThread ucac2Thread;
    StarUploadThread ucac4Thread;
    StarUploadThread pgcThread;
    StarUploadThread ngcThread;
    StarUploadThread ugcThread;
    StarUploadThread conBoundaryThread;
    StarUploadThread milkyWayThread;
    public static final int TYCHO_THREAD_ID = 1;
    static final int UCAC2_THREAD_ID = 2;
    public static final int UCAC4_THREAD_ID = 3;
    public static final int PGC_THREAD_ID = 4;
    public static final int TYCHO_THREAD_SHORT_ID = 5;
    public static final int YALE_THREAD_ID = 6;
    public static final int NGCIC_THREAD_ID = 7;
    public static final int CON_BOUNDARY_THREAD_ID = 8;
    public static final int MILKY_WAY_THREAD_ID = 9;

    /**
     * @param threadid   optional, if set only this thread will be run,otherwise all threads will be run (put to -1 to run all threads)
     * @param u
     * @param clearcache use true only if mag limits have been changed to clear the cache
     */
    public void upload(UploadRec u, boolean clearcache, int threadid) {
        //zero mag limit means that we do not start the thread, the last part needed if we put 0 mag limit in starMags to clear cache

        boolean validid = (threadid == TYCHO_THREAD_SHORT_ID || threadid == TYCHO_THREAD_ID || threadid == UCAC4_THREAD_ID
                || threadid == PGC_THREAD_ID || threadid == YALE_THREAD_ID || threadid == CON_BOUNDARY_THREAD_ID || threadid == MILKY_WAY_THREAD_ID);

        boolean run = !validid || threadid == TYCHO_THREAD_SHORT_ID;//run for non valid id, or particular thread only for validid
        boolean runshortat10 = u.FOV == 10 && (StarMags.getMagLimit(StarMags.TYCHO, u.FOV) < StarMags.TYCHO_SHORT_MAX);
        if (run && SettingsActivity.getTychoStatus() && u.FOV < 90.1 && (u.FOV > TYCHO_SHORT_UPLOAD_TRSHOLD || runshortat10) && (clearcache || StarMags.getMagLimit(StarMags.TYCHO, u.FOV) != 0)) {//10.1){

            if (tychoThreadShort == null || !tychoThreadShort.isAlive()) {
                tychoThreadShort = new StarUploadThread(handler, TYCHO_THREAD_SHORT_ID, new TychoStarFactoryShort(),
                        new StarUploadThread.TychoFilterShort(), Quadrant.quadrants, clearcache);
                tychoThreadShort.addUnpload(u);
                tychoThreadShort.start();
            } else {
                if (tychoThreadShort != null)//remove this check
                    tychoThreadShort.addUnpload(u);
            }
        }

        run = !validid || threadid == TYCHO_THREAD_ID;//run for non valid id, or particular thread only for validid

        if (!runshortat10 && run && SettingsActivity.getTychoStatus() && (u.FOV < TYCHO_SHORT_UPLOAD_TRSHOLD) && (clearcache || StarMags.getMagLimit(StarMags.TYCHO, u.FOV) != 0)) {//10.1){

            if (tychoThread == null || !tychoThread.isAlive()) {
                tychoThread = new StarUploadThread(handler, TYCHO_THREAD_ID, new TychoStarFactory(),
                        new StarUploadThread.TychoFilter(), Quadrant.quadrantsTyc, clearcache);
                tychoThread.addUnpload(u);
                tychoThread.start();
            } else {
                if (tychoThread != null)//remove this check
                    tychoThread.addUnpload(u);
            }
        }

        run = !validid || threadid == UCAC4_THREAD_ID;//run for non valid id, or particular thread only for validid

        if (run && SettingsActivity.getUcac4Status() && u.FOV < UCAC_UPLOAD_TRESHOLD && (clearcache || StarMags.getMagLimit(StarMags.UCAC, u.FOV) != 0)) {//<2.1

            if (ucac4Thread == null || !ucac4Thread.isAlive()) {
                ucac4Thread = new StarUploadThread(handler, UCAC4_THREAD_ID, new Ucac4StarFactory(),
                        new StarUploadThread.UcacFilter(), Quadrant.quadrantsUcac, clearcache);
                ucac4Thread.addUnpload(u);
                ucac4Thread.start();
            } else {
                if (ucac4Thread != null)//remove this check
                    ucac4Thread.addUnpload(u);
            }
        }

        run = !validid || threadid == PGC_THREAD_ID;//run for non valid id, or particular thread only for validid

        if (run && SettingsActivity.getPgcStatus() && u.FOV < PGC_UPLOAD_TRESHOLD && (clearcache || StarMags.getMagLimit(StarMags.PGC, u.FOV) != 0)) {//<2.1

            if (pgcThread == null || !pgcThread.isAlive()) {
                pgcThread = new StarUploadThread(handler, PGC_THREAD_ID, new PgcFactory(),
                        new StarUploadThread.PgcFilter(), Quadrant.quadrantsTyc, clearcache);
                pgcThread.addUnpload(u);
                pgcThread.start();
            } else {
                if (pgcThread != null)//remove this check
                    pgcThread.addUnpload(u);
            }
        }

        run = !validid || threadid == NGCIC_THREAD_ID;//run for non valid id, or particular thread only for validid

        if (run && SettingsActivity.getNgcIcStatus() && (clearcache || StarMags.getMagLimit(StarMags.NGC, u.FOV) != 0)) {

            if (ngcThread == null || !ngcThread.isAlive()) {
                ngcThread = new StarUploadThread(handler, NGCIC_THREAD_ID, new NgcFactory(),
                        new StarUploadThread.NgcFilter(), Quadrant.quadrants, clearcache);
                ngcThread.addUnpload(u);
                ngcThread.start();
            } else {
                if (ngcThread != null)//remove this check
                    ngcThread.addUnpload(u);
            }
        }

        run = !validid || threadid == CON_BOUNDARY_THREAD_ID;//run for non valid id, or particular thread only for validid

        if (run && SettingsActivity.isConBoundaryOn(getContext())) {

            if (conBoundaryThread == null || !conBoundaryThread.isAlive()) {
                conBoundaryThread = new StarUploadThread(handler, CON_BOUNDARY_THREAD_ID, new ConBoundaryFactory(),
                        new StarUploadThread.ConBoundaryFilter(), Quadrant.quadrants, clearcache);
                conBoundaryThread.addUnpload(u);
                conBoundaryThread.start();
            } else {
                if (conBoundaryThread != null)//remove this check
                    conBoundaryThread.addUnpload(u);
            }
        }

        run = !validid || threadid == MILKY_WAY_THREAD_ID;//run for non valid id, or particular thread only for validid

        if (run && SettingsActivity.isMilkyWayOn(getContext())) {

            if (milkyWayThread == null || !milkyWayThread.isAlive()) {
                milkyWayThread = new StarUploadThread(handler, MILKY_WAY_THREAD_ID, new MilkyWayFactory(),
                        new StarUploadThread.ConBoundaryFilter(), Quadrant.quadrants, clearcache);
                milkyWayThread.addUnpload(u);
                milkyWayThread.start();
            } else {
                if (milkyWayThread != null)//remove this check
                    milkyWayThread.addUnpload(u);
            }
        }

        run = !validid || threadid == YALE_THREAD_ID;
        if (!run) return;
        UploadRec uCurrent = signal.getRec();
        //it is important to correctly update this time flag. it needs to be updated even if we miss other events
        if (!u.raiseNewPointFlag)
            if (uCurrent != null && uCurrent.raiseNewPointFlag)
                return;


        signal.setRec(u);

        if (uploadThread != null) {
            if (uploadThread.isAlive()) {
                return;
            }
        }

        if (clearcache)
            sh.hrMap = new HashMap<Integer, ArrayList<HrStar>>();//
        uploadThread = new Thread(new Upload(signal, sh, CuV.this, handler));
        uploadThread.start();
    }

    private void drawConFigures(Canvas canvas, Paint paint) {
        for (ConPoint cp : conList) {
            cp.draw(canvas, paint);
        }
    }

    private void drawConNames(Canvas canvas, Paint paint) {
        for (ConNamePoint point : ConFigure.connames) {
            point.setXY();
            point.setDisplayXY();
            point.draw(canvas, paint);
        }

    }

    private void drawNebContours(Canvas canvas, Paint paint) {
        InfoList list = ListHolder.getListHolder().get(InfoList.NEBULA_CONTOUR_LIST);
        for (Object o : list) {
            ContourObject p = (ContourObject) o;
            p.setXY();
            p.setDisplayXY();
            //Log.d(TAG,"drawing "+p.getShortName());
            p.draw(canvas, paint);
        }
    }

    private void drawConNameLabel(Canvas canvas, Paint paint, String name, float xmin, float xmax, float ymin, float ymax) {

        Style orig_style = paint.getStyle();
        float orig_size = paint.getTextSize();

        paint.setStyle(Paint.Style.FILL);

        float size = paint.getTextSize() * Point.getScalingFactor();

        paint.setTextSize(size);
        float w = paint.measureText(name);
        float x = (xmin + xmax) / 2;
        float y = (ymin + ymax) / 2;

        double angle = Point.getRotAngle();
        if (angle == 0) {
            x = x - w / 2;
            canvas.drawText(name, x, y, paint);
        } else {
            x = x - (float) (w / 2 * Math.cos(angle * Point.D2R));
            y = y - (float) (w / 2 * Math.sin(angle * Point.D2R));
            Path path = AstroTools.getLabelPath(x, y);
            canvas.drawTextOnPath(name, path, 0, 0, paint);
        }

        paint.setStyle(orig_style);
        paint.setTextSize(orig_size);


    }

    private void drawConBoundaries(Canvas canvas) {
        Map<Integer, Float> mxmin = new HashMap<Integer, Float>();
        Map<Integer, Float> mxmax = new HashMap<Integer, Float>();
        Map<Integer, Float> mymin = new HashMap<Integer, Float>();
        Map<Integer, Float> mymax = new HashMap<Integer, Float>();

        for (Point p : conBoundaryList) {
            p.setXY();
            p.setDisplayXY();
            p.draw(canvas, conBoundaryPaint);
            BoundaryPoint bp = (BoundaryPoint) p;
            if (bp.getIgnoreFlag())
                continue;
            int con = bp.getCon();

            //imporant that setXY and setDisplayXY are called first!!!
            float xd = bp.getXd();
            float yd = bp.getYd();


            Float xmin = mxmin.get(con);
            if (xmin != null) {
                if (xd < xmin)
                    mxmin.put(con, xd);
            } else {
                mxmin.put(con, xd);
            }

            Float xmax = mxmax.get(con);
            if (xmax != null) {
                if (xd > xmax)
                    mxmax.put(con, xd);
            } else {
                mxmax.put(con, xd);
            }

            Float ymin = mymin.get(con);
            if (ymin != null) {
                if (yd < ymin)
                    mymin.put(con, yd);
            } else {
                mymin.put(con, yd);
            }

            Float ymax = mymax.get(con);
            if (ymax != null) {
                if (yd > ymax)
                    mymax.put(con, yd);
            } else {
                mymax.put(con, yd);
            }

        }
        int length = Constants.constellationLong.length;
        int sel_con_boundary_con = -1;
        if (selectedConBoundary != null)
            sel_con_boundary_con = selectedConBoundary.getCon();
        for (int con = 1; con < length; con++) {
            Float xmin = mxmin.get(con);
            Float ymin = mymin.get(con);
            Float xmax = mxmax.get(con);
            Float ymax = mymax.get(con);

            if (xmin != null && xmax != null && ymin != null && ymax != null) {
                drawConNameLabel(canvas, conBoundaryPaint, Constants.constellationLong[con], xmin, xmax, ymin, ymax);
                if (con == sel_con_boundary_con)
                    drawConNameLabel(canvas, conBoundaryPaint, Constants.constellationLong[con], xmin, xmax, ymin, ymax);

            }
        }
    }

    private class SelectedConBoundary {
        int con;
        List<Point> list = new ArrayList<Point>();

        public SelectedConBoundary(int con) {
            this.con = con;
            init();
        }

        public int getCon() {
            return con;
        }

        public void init() {
            ConBoundaryFactory factory = new ConBoundaryFactory();
            try {
                factory.open();
                list = factory.get(con);
            } catch (Exception e) {
            } finally {
                try {
                    factory.close();
                } catch (Exception e) {
                }
            }
        }

        public void draw(Canvas canvas, Paint paint) {
            float orig_width = paint.getStrokeWidth();
            paint.setStrokeWidth(3);
            float xmin = 0;
            float xmax = 0;
            float ymin = 0;
            float ymax = 0;

            boolean first = true;
            for (Point p : list) {
                p.setXY();
                p.setDisplayXY();
                p.draw(canvas, paint);
                BoundaryPoint bp = (BoundaryPoint) p;
                boolean ignore = bp.getIgnoreFlag();

                if (first && !ignore) {
                    xmin = p.getXd();
                    ymin = p.getYd();
                    xmax = xmin;
                    ymax = ymin;
                    first = false;
                }
                if (!ignore) {
                    xmin = Math.min(p.getXd(), xmin);
                    xmax = Math.max(p.getXd(), xmax);
                    ymin = Math.min(p.getYd(), ymin);
                    ymax = Math.max(p.getYd(), ymax);
                }

            }
            paint.setStrokeWidth(orig_width);
            if (conBoundaryList.isEmpty())
                drawConNameLabel(canvas, paint, Constants.constellationLong[con], xmin, xmax, ymin, ymax);
        }

        public void raiseNewPointFlag() {
            for (Point p : list) {
                p.raiseNewPointFlag();
            }
        }

        private double distance(double az1, double az2) {
            double dst1 = AstroTools.normalise(az1 - az2);
            return Math.min(dst1, 360 - dst1);
        }

        public double getAz() {
            if (list.size() < 2)
                return 0;

            double az1 = AstroTools.normalise(list.get(0).getAz());//boundary point 1
            double az2 = AstroTools.normalise(list.get(1).getAz());//boundary point 2

            for (int i = 2; i < list.size(); i++) {
                Point p = list.get(i);
                double az = AstroTools.normalise(p.getAz());

                double dst1 = distance(az, az1);
                double dst2 = distance(az, az2);
                double dst = distance(az1, az2);

                if (dst1 <= dst && dst2 <= dst) {
                    continue;//between boundary points
                } else {//outside of boundary points
                    if (dst1 < dst2) {
                        //1 point is closest
                        az1 = az;//new first point
                    } else {
                        az2 = az;//new second point
                    }
                }

            }

            double first = (AstroTools.normalise(az1) + AstroTools.normalise(az2)) / 2;
            double second = AstroTools.normalise(first + 180);

            double dst1 = distance(az1, first);
            double dst2 = distance(az1, second);


            if (dst1 < dst2)
                return first;
            else
                return second;

        }

        public double getAlt() {
            double alt = 0;

            int i = 0;
            for (Point p : list) {
                alt = alt + p.getAlt();
                i++;
            }
            return alt / i;
        }
    }

    SelectedConBoundary selectedConBoundary;

    /**
     * this is for drawing selected boundary only.
     *
     * @param con
     * @param set_centered if set, the screen is centered on the constellation.
     *                     Uses Point.setCenterAz to center the screen
     */
    public void initSelectedConBoundary(int con, boolean set_centered) {
        if (con != 0)
            selectedConBoundary = new SelectedConBoundary(con);
        if (set_centered) {
            double az = selectedConBoundary.getAz();
            double alt = selectedConBoundary.getAlt();
            Point.setCenterAz(az, alt);
        }
    }

    /**
     * @return selected constellation or zero if there is none
     */
    public int getSelectedConBoundary() {
        if (selectedConBoundary == null)
            return GraphRec.NO_CONSTELLATION_SELECTED;
        return selectedConBoundary.getCon();
    }

    private void drawPlanets(Canvas canvas, Paint paint) {
        for (AstroObject o : Global.planets) {
            o.setXY();
            o.setDisplayXY();
            o.draw(canvas, paint);
        }
    }

    private void drawStars(Canvas canvas, Paint paint) {

        if (starList == null) return;
        for (Point s : starList) {//should be before object so that it get labels first
            if (limit_push_camera_stars) {
                if (s instanceof AstroObject) {
                    if (((AstroObject) s).getMag() > Prefs.PUSH_CAMERA_CALIBRATION_LIMIT)
                        continue;
                }
            }
            s.setXY();
            s.setDisplayXY();
            s.draw(canvas, paint);
        }
        if (!limit_push_camera_stars) {
            for (AstroObject obj : tychoList) {
                obj.setXY();
                obj.setDisplayXY();
                obj.draw(canvas, paint);
            }
            for (AstroObject obj : tychoListShort) {
                obj.setXY();
                obj.setDisplayXY();
                obj.draw(canvas, paint);
            }
            for (AstroObject obj : ucac2List) {
                obj.setXY();
                obj.setDisplayXY();
                obj.draw(canvas, paint);
            }
            for (AstroObject obj : ucac4List) {
                obj.setXY();
                obj.setDisplayXY();
                obj.draw(canvas, paint);
            }
        }

        boolean dim = m_pm.getBoolean(getContext().getString(R.string.dim_layer_objects), false);
        Paint pa = new Paint(objPaint);
        if (dim) {
            pa.setAlpha(DIM_LAYER_ALPHA);
        }


        drawConBoundaries(canvas);
        for (Point p : milkyWayList) {
            p.setXY();
            p.setDisplayXY();
            p.draw(canvas, milkyWayPaint);
        }

        if (!limit_push_camera_stars) {
            for (AstroObject obj : ngcList) {
                obj.setXY();
                obj.setDisplayXY();
                obj.draw(canvas, pa);
            }
            for (AstroObject obj : pgcList) {
                obj.setXY();
                obj.setDisplayXY();
                obj.draw(canvas, pa);
            }
        }


    }

    private void drawDsoObj(Canvas canvas, Paint paint) {

        if (limit_push_camera_stars)
            return;

        for (AstroObject p : objList) {
            p.setXY();
            p.setDisplayXY();
            int cat = p.getCatalog();
            if (cat == AstroCatalog.YALE_CATALOG || cat == AstroCatalog.TYCHO_CATALOG || cat == AstroCatalog.UCAC4_CATALOG) {
                if (p instanceof HrStar) {
                    HrStar star = (HrStar) p;
                    star.drawStar(canvas, paint);
                } else if (p instanceof TychoStar) {//ucac4star as well
                    TychoStar star = (TychoStar) p;
                    boolean double_star = false;
                    star.drawStar(canvas, paint, double_star);
                } else {
                    p.draw(canvas, paint);//should not be
                }
            } else {
                if (p.getCatalog() == AstroCatalog.PLANET_CATALOG) {
                    p.draw(canvas, planetPaint);
                } else
                    p.draw(canvas, paint);
            }
        }
        Context context = getContext();
        if (context != null && context instanceof GraphActivity) {
            for (Point p : ((GraphActivity) context).listNearby) {
                p.setXY();
                p.setDisplayXY();
                p.draw(canvas, paint);
            }
        }
    }


    private static float fov_label_size;
    private static float fov_label_height;

    private void drawFOVlabel(Canvas canvas, Paint paint) {
        Paint p = new Paint(paint);

        p.setTextSize(27 * Point.getScalingFactor());
        float x = 10 * Point.getScalingFactor();
        float y = 25 * Point.getScalingFactor();
        String s = Point.getFOVdim();
        fov_label_size = p.measureText(s) + x;
        FontMetrics fm = p.getFontMetrics();
        fov_label_height = (fm.descent - fm.ascent) + y;//font height
        canvas.drawText(s, x, y, p);
    }

    private static float mirror_label_size;
    private static float mirror_label_height;

    private void drawMirrorRotateLabel(Canvas canvas, Paint paint) {
        if (Point.mirror == Point.NO_MIRROR && Point.orientationAngle == 0) {
            mirror_label_size = 0;
            mirror_label_height = 0;
            return;
        }

        Paint p = new Paint(paint);

        p.setTextSize(27 * Point.getScalingFactor());
        float x = 10 * Point.getScalingFactor();
        float y = 25 * Point.getScalingFactor();
        String s = "";
        if (Point.mirror == Point.MIRROR)
            s = s + getContext().getString(R.string.mirror_);
        if (Point.orientationAngle != 0)
            s = s + getContext().getString(R.string.rotated);
        s = s.trim();
        int w = Point.getWidth();
        mirror_label_size = p.measureText(s) + x;
        FontMetrics fm = p.getFontMetrics();
        mirror_label_height = (fm.descent - fm.ascent) + y;//font height
        canvas.drawText(s, w - mirror_label_size, y, p);
    }

    String objName = null;
    String objInfo = null;

    private void drawObjInfoLabel(Canvas canvas, Paint paint) {

        if (objName == null || objInfo == null)
            return;
        Paint p = new Paint(paint);

        p.setTextSize(27 * Point.getScalingFactor());
        float x = 10 * Point.getScalingFactor();
        //float y=25*Point.getScalingFactor();

        FontMetrics fm = p.getFontMetrics();
        float fontHeight = (fm.descent - fm.ascent);//font height
        canvas.drawText(objName, x, getHeight() - 2 * fontHeight, p);
        canvas.drawText(objInfo, x, getHeight() - fontHeight, p);
    }

    private void drawDstlabel(Canvas canvas, Paint paint) {
        Paint p = new Paint(paint);

        p.setTextSize(25 * Point.getScalingFactor());
        float x = 10 * Point.getScalingFactor();
        float y = 60 * Point.getScalingFactor();

        String s = null;
        Point p1 = objc.getObjSelected();
        Point p2 = prevCursorObj;
        //Log.d(TAG,"p1="+p1+" p2="+p2);
        if (p1 != null && p2 != null) {
            double dst = AstroTools.getDst(p1.getRa(), p1.getDec(), p2.getRa(), p2.getDec());
            if (dst > 1) {
                s = DetailsActivity.doubleToGrad(dst, '\u00B0', (char) 39);
            } else if (dst > 1 / 60f) {
                s = String.format(Locale.US, "%.1f", dst * 60) + (char) 39;
            } else
                s = String.format(Locale.US, "%.1f", dst * 3600) + (char) 39 + (char) 39;
        }
        if (s != null) {
            fov_label_size = Math.max(p.measureText(s) + x, fov_label_size);
            FontMetrics fm = p.getFontMetrics();
            fov_label_height = Math.max(fov_label_height, (fm.descent - fm.ascent) + y);//font height
            canvas.drawText(s, x, y, p);
        }
    }

    private void drawHorizonNew(Canvas canvas, Paint paint) {
        boolean fill = SettingsActivity.getHorizonFillStatus();
        final double az = Point.getAzCenter();

        Grid grid = new Grid(canvas, paint, false, getContext(), fov_label_size, fov_label_height, mirror_label_size, mirror_label_height);

        CustomPoint p1 = new CustomPoint(az, 0, "");
        p1.setXY();
        p1.setDisplayXY();

        CustomPoint p2 = new CustomPoint(az + 90, 0, "");
        p2.setXY();
        p2.setDisplayXY();
        CustomPoint p3 = new CustomPoint(az + 190, 0, "");
        p3.setXY();
        p3.setDisplayXY();

        Grid.Circle c = grid.getCircle(p1, p2, p3);

        if (fill && Point.getFOV() < FOV_FILL_THRESHOLD) {
            fillHorizon(canvas, paint);
        } else
            grid.drawAltCircle(0, c, null, null, null, null);


    }

    /**
     * @return path for the whole screen
     */
    private Path getWholeScreen() {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(Point.getWidth(), 0);
        path.lineTo(Point.getWidth(), Point.getHeight());
        path.lineTo(0, Point.getHeight());
        path.lineTo(0, 0);
        return path;
    }


    private void fillHorizon(Canvas canvas, Paint paint) {
        final double az = Point.getAzCenter();

        Grid grid = new Grid(canvas, paint, false, getContext(), fov_label_size, fov_label_height, mirror_label_size, mirror_label_height);

        CustomPoint p1 = new CustomPoint(az, 0, "");
        p1.setXY();
        p1.setDisplayXY();

        CustomPoint p2 = new CustomPoint(az + 90, 0, "");
        p2.setXY();
        p2.setDisplayXY();
        CustomPoint p3 = new CustomPoint(az + 190, 0, "");
        p3.setXY();
        p3.setDisplayXY();

        Grid.Circle c = grid.getCircle(p1, p2, p3);

        ArrayList<Vector2> list = grid.getIntersection(c);

        Paint p = new Paint(paint);
        p.setStyle(Paint.Style.FILL);

        if (SettingsActivity.getNightMode()) {
            p.setColor(0x505f0000);
        }
        if (list.size() > 2) {
            //need to take the farthest points


            ArrayList<Vector2> list1 = Grid.getFarthestPoints(list);
            list = list1;
        }

        if (list.size() == 2) {//two intersections
            RefPoint screen = RefPoint.getScreen();
            RefPoint r1 = screen.insert(new RefPoint(list.get(0).x, list.get(0).y));
            RefPoint r2 = screen.insert(new RefPoint(list.get(1).x, list.get(1).y));

            Holder2<Path, Boolean> h = grid.getArcPaths(c, list.get(0), list.get(1));
            if (!h.y) {
                RefPoint tmp = r2;
                r2 = r1;
                r1 = tmp;
            }

            RefPoint next = r2.next;
            Vector2 v = CustomPoint.getAzAlt(next.x, next.y);
            boolean direction;//true if next is used for getting screen below the horizon, false otherwise
            if (v.y < 0)
                direction = true;
            else
                direction = false;

            int i = 0;

            if (direction) {
                while (!r2.next.equals(r1) && i++ < 20) {
                    h.x.lineTo((float) r2.next.x, (float) r2.next.y);
                    r2 = r2.next;
                }
            } else {
                while (!r2.prev.equals(r1) && i++ < 20) {
                    h.x.lineTo((float) r2.prev.x, (float) r2.prev.y);
                    r2 = r2.prev;
                }
            }
            h.x.close();
            canvas.drawPath(h.x, p);
        } else {
            if (Point.getAltCenter() < 0) {
                Path path = getWholeScreen();//fill the whole screen
                canvas.drawPath(path, p);
            } else//below the screen
                return;
        }

    }


    /**
     * Sky Chart in lists
     */
    public static final int EXTERNAL = 1;
    public static final int GRAPH = 2;
    private int callType = GRAPH;

    /**
     * where the view is called from - Graph or lists
     *
     * @param type
     */
    public void setCallType(int type) {
        callType = type;
    }

    private int currentZoom;

    private void zoomIn() {
        if (currentZoom < GraphActivity.spinArr.length - 1) {
            double fov = Double.parseDouble(GraphActivity.spinArr[++currentZoom]);
            Point.setFOV(fov);//resetting fov
            StarBoldness.setFovOther(fov);
            FOVchanged();
        }
    }

    private void zoomOut() {
        if (currentZoom > 0) {
            double fov = Double.parseDouble(GraphActivity.spinArr[--currentZoom]);
            Point.setFOV(fov);//resetting fov
            StarBoldness.setFovOther(fov);
            FOVchanged();
        }
    }

    public void updateObsList() {
        initDsoList();
        sgrChanged(false);
    }

    private boolean process_long_touch_flag = false;
    GestureDetector gDetector;

    /**
     * long touch processing for external calls
     */
    public void setProcessLongTouch() {
        process_long_touch_flag = true;
        gDetector = new GestureDetector(new OnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // TODO Auto-generated method stub

                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                    float distanceY) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                // TODO Auto-generated method stub
                Log.d(TAG, "long press");
                Context context = getContext();
                if (context instanceof QueryActivity) {
                    ((QueryActivity) context).goFullSkyView();
                } else if (context instanceof ObservationListActivity) {
                    ((ObservationListActivity) context).goFullSkyView();
                }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                // TODO Auto-generated method stub
                return false;
            }
        });
    }


    private boolean disableDr = false;

    public void disableDraw() {
        disableDr = true;
    }

    public void enableDraw() {
        disableDr = false;
    }

    public void clearListsIfRequired() {
        if (!SettingsActivity.getTychoStatus()) {
            clearTychoList();
            clearTychoListShort();
        }
        if (!SettingsActivity.getUcac2Status())
            clearUcac2List();
        if (!SettingsActivity.getUcac4Status())
            clearUcac4List();
        if (!SettingsActivity.getPgcStatus()) {
            clearPgcList();
        }

        if (!SettingsActivity.getNgcIcStatus()) {
            clearNgcList();
        }
        if (!SettingsActivity.isConBoundaryOn(getContext())) {
            clearConBoundariesList();
        }

        if (!SettingsActivity.isMilkyWayOn(getContext()))
            clearMilkyWayList();
    }

    /**
     * other than Graph calls
     *
     * @param gr
     */
    public void initExternal(GraphRec gr) {
        Log.d(TAG, "init other start");
        initSettingsToDrawMap();
        ObjCursor.setParameters(PreferenceManager.getDefaultSharedPreferences(getContext()));

        if (SettingsActivity.getInverseSky() && !SettingsActivity.getNightMode())
            setBackgroundColor(0xffffffff);
        else
            setBackgroundColor(0xff000000);
        objList = new ArrayList<AstroObject>();

        global_object = SettingsActivity.getObjectFromSharedPreferencesNew(Constants.GRAPH_OBJECT, getContext());

        initDsoList();
        clearListsIfRequired();
        SettingsActivity.setAntialiasing();

        if (gr == null) return;
        currentZoom = gr.FOV;
        Point.setFOV(Double.parseDouble(GraphActivity.spinArr[gr.FOV]));//resetting fov
        StarBoldness.initOther(Point.getFOV());


        AstroTools.setDefaultTime(gr.c, getContext());
        if (global_object != null)
            global_object.raiseNewPointFlag();//update internal calculations of az,alt as time has changed

        for (Planet pl : Global.planets) {
            pl.raiseNewPointFlag();
            pl.recalculateRaDec(gr.c);
        }

        ObjCursor o = new ObjCursor(0, 0);
        Point p;

        p = gr.obj;

        if (p == null)
            p = global_object;


        if (p != null) {
            o.setRaDec(p.getRa(), p.getDec());
            o.setObjSelected(p);
        }

        setObjCursor(o);

        Point.setLST(AstroTools.sdTime(gr.c)); //setting current time for looking through the sky
        Point.setCurrentTime(gr.c);


        //this allows to center on last selected object if no object is passed
        //actually this never happens now as the only place from where the null object is passed
        //in dso main sky view now uses the last saved Global.graphCreate
        double azCenter = gr.azCenter;
        double altCenter = gr.altCenter;


        if (gr.obj == null && global_object != null) {
            azCenter = global_object.getAz();
            altCenter = global_object.getAlt();
        }
        Point.setCenterAz(azCenter, altCenter);
        ConFigure.raiseNewPointFlag();
        Log.d(TAG, "init other before sgr changed");
        sgrChanged(true);
    }


    public void saveScreenIntoPrefs() {
        int zPos = currentZoom;
        new GraphRec(zPos, Point.getAzCenter(), Point.getAltCenter(), AstroTools.getDefaultTime(getContext()),
                getObjCursor().getObjSelected()).save(getContext());

    }

    public int getZoom() {
        return currentZoom;
    }

}
