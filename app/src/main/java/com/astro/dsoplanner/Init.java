package com.astro.dsoplanner;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.Comet;
import com.astro.dsoplanner.base.ContourObject;
import com.astro.dsoplanner.base.MinorPlanet;
import com.astro.dsoplanner.base.Planet;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.DbManager;
import com.astro.dsoplanner.expansion.APKExpansion;
import com.astro.dsoplanner.graph.ConFigure;
import com.astro.dsoplanner.graph.Quadrant;
import com.astro.dsoplanner.graph.StarMags;
import com.astro.dsoplanner.graph.cuv_helper.DSS;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListCollectionFiller;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.InfoListHolder;
import com.astro.dsoplanner.infolist.InfoListImpl;
import com.astro.dsoplanner.infolist.InfoListLoader;
import com.astro.dsoplanner.infolist.InfoListLoaderImp;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListSaverImp;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.infolist.ObsInfoListImpl;
import com.astro.dsoplanner.util.Holder2;
import com.astro.dsoplanner.util.Holder3;

import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Init extends Thread {


    public static final String MWQL_DB = "mwql.db";
    public static final String MWQ_DB = "mwq.db";
    public static final String BQL_DB = "bql.db";
    public static final String BQ_DB = "bq.db";
    private static final String LOG_TXT = "log.txt";
    private static final String TN_DB = "tn.db";
    private static final String NGC_LIST = "NGC_LIST";
    private static final String UGCQL_DB = "sacql.db";
    private static final String UGCQ_DB = "sacq.db";
    private static final String NGCQL_DB = "tql.db";//"ngcql.db";
    private static final String NGCQ_DB = "tq.db";//ngcq.db";
    private static final String TYCHOQLS_DB = "tychoqls.db";
    private static final String TYCHOQS_DB = "tychoqs.db";
    private static final String TMP = "tmp";
    public static final String DS_OPLANNER;// = "DSOplanner";

    static {

        if (Global.BASIC_VERSION) {
            DS_OPLANNER = "DSOplannerbasic";
        } else if (Global.PLUS_VERSION) {
            DS_OPLANNER = "DSOplannerplus";
        } else//pro
            DS_OPLANNER = "DSOplanner";
    }

    private static final String NGC_IC_CATALOG = "NgcIc Catalog";
    private static final String PREF_LIST = "PrefList";
    private static final String CONTOUR = "Contour";
    private static final String LOCATIONS_LIST = "Locations list";
    private static final String TELESCOPES_LIST = "Telescopes list";
    private static final String SEARCH_REQUEST_LIST = "Search request list";
    private static final String DATABASE_LIST = "Database list";
    private static final String OBSERVATION_LIST_4 = "Observation List 4";
    private static final String OBSERVATION_LIST_3 = "Observation List 3";
    private static final String OBSERVATION_LIST_2 = "Observation List 2";
    private static final String OBSERVATION_LIST_1 = "Observation List 1";
    private static final String NGCIC = "NGCIC";
    private static final String PGCQL_DB = "pgcql.db";
    private static final String TYCHOQL_DB = "tychoql.db";
    private static final String TYCHOQ_DB = "tychoq.db";
    public static final String NOTES2 = "notes";
    public static final String IMAGES2 = "images";

    private static final String DISKCACHE = "cache";
    public static final String DSS2 = "dss";

    private static final String TAG = Init.class.getSimpleName();
    public static final int INIT_OVER = 0;
    Context context;
    Handler handler;

    public Init(Context context, Handler handler) {
        super();
        this.context = context;
        this.handler = handler;
    }

    public void run() {
        Global.setAppContext(context.getApplicationContext());

        initDirs();
        Point.setLat(SettingsActivity.getLattitude());
        initTime();
        initLists();
        initPlanets();
        initStars();
        initDss();
        SettingsActivity.setUHor();
        //disable lock screen so we can turn on and turn off without it
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardLock lock = keyguardManager.newKeyguardLock(context.KEYGUARD_SERVICE);
        lock.disableKeyguard();


        SettingsActivity.initTelescope(context); //get from Pref current id and populate
        SettingsActivity.setStarMode();
        SettingsActivity.setDefaultColors(false, context);
        SettingsActivity.updateTelrad(); //Read Telrad settings
        SettingsActivity.setBatteryLow();
        SettingsActivity.initLabels();
        SettingsActivity.initGraphSelection();
        SettingsActivity.updateLM();
        SettingsActivity.updateVisibleStatus();
        SettingsActivity.updateLayerVisibilityThreshold();
        SettingsActivity.updateRemovingDuplicates();
        SettingsActivity.initLabelsScale(context);
        SettingsActivity.initMinorPlanetUpdateUrlInputPref(context);
        SettingsActivity.updateZoomLevelIfNeeded(context);
        initComMapDb(context);
        SettingsActivity.initAutoSkyFlag(context);
        SettingsActivity.initAutoRotationFlag(context);
        if (handler != null) handler.sendEmptyMessage(INIT_OVER);

    }

    /**
     * making a file in my format of map ref - ngcic number of common name. works for other catalogs with the same ref as well
     *
     * @param context
     */
    public static void initComMapDb(Context context) {
        boolean res = loadComMapDb(context);
        Log.d(TAG, "loadComMapDb=" + res);
        if (!res) {
            AstroTools.fillComMapDb(context);
            saveComMapDb(context);
        }
    }

    /**
     * loading map of ref - ngc for getting common name from the list
     *
     * @return
     */
    private static boolean loadComMapDb(Context context) {
        File dir = context.getFilesDir();//Tools.getExternalFilesDir(context);
        File f = new File(dir, Constants.COMMON_NAME_MAP);
        InputStream in = null;
        if (f.exists()) {
            try {
                in = new FileInputStream(f);
                InfoListLoader loader = new InfoListLoaderImp(in);
                InfoList il = new InfoListImpl("", ShItem.class);
                ErrorHandler ehan = il.load(loader);
                if (!ehan.hasError()) {
                    ShItem item = (ShItem) il.get(0);
                    if (item.type == ShItem.INT_ARR) {
                        int[] arr = item.int_arr_value;
                        for (int i = 0; i < arr.length; i += 2) {
                            AstroTools.commapdb.put(arr[i], arr[i + 1]);
                        }
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "e=" + AstroTools.getStackTrace(e));
            } finally {
                try {
                    in.close();
                } catch (Exception e) {

                }
            }

        }
        return false;
    }

    private static void saveComMapDb(Context context) {
        InfoList il = new InfoListImpl("", ShItem.class);
        int[] arr = new int[AstroTools.commapdb.size() * 2];
        int i = 0;
        for (Map.Entry<Integer, Integer> e : AstroTools.commapdb.entrySet()) {
            arr[i++] = e.getKey();
            arr[i++] = e.getValue();
        }
        ShItem item = new ShItem("", arr);
        InfoListCollectionFiller filler = new InfoListCollectionFiller(Arrays.asList(new Object[]{item}));
        il.fill(filler);

        File f = null;
        OutputStream out = null;
        try {
            File dir = context.getFilesDir();//Tools.getExternalFilesDir(context);
            f = new File(dir, Constants.COMMON_NAME_MAP);
            out = new FileOutputStream(f);

            InfoListSaver saver = new InfoListSaverImp(out);
            il.save(saver);
        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
        } finally {
            try {
                out.close();
            } catch (Exception e) {

            }
        }

    }

    /**
     * to be run BEFORE the init thread is started!
     *
     * @param context
     */
    public static void runOnUi(Activity context) {
        PreferenceManager.setDefaultValues(context, R.xml.settings_graph, false);
        PreferenceManager.setDefaultValues(context, R.xml.settings_system, false);

        if (Global.EXCEPTION_TRACKER) {
            final Thread.UncaughtExceptionHandler prevHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler( //override native handler
                    new Thread.UncaughtExceptionHandler() {
                        public void uncaughtException(Thread t, Throwable e) {
                            try {
                                MyExTracker.saveReport(e);
                            } catch (Throwable e1) { /*close silently*/ }
                            //return native control
                            if (prevHandler != null) prevHandler.uncaughtException(t, e);
                        }
                    });

            MyExTracker.sendReport(context);
        }
    }

    /**
     * @return true if at least some of the global vars were cleared
     */
    public static boolean initRequired() {

        int[] listnum = new int[]{InfoList.NGCIC_SELECTION_LIST, InfoList.PrimaryObsList, InfoList.PrimaryObsList + 1, InfoList.PrimaryObsList + 2, InfoList.PrimaryObsList + 3, InfoList.DB_LIST, InfoList.SREQUEST_LIST, InfoList.TELESCOPE_LIST, InfoList.LOCATION_LIST, InfoList.NEBULA_CONTOUR_LIST, InfoList.SREQUEST_LIST};
        InfoListHolder h = ListHolder.getListHolder();

        for (int i : listnum) {
            if (h.get(i) == null) return true;
        }
        boolean result = (Global.planets == null) || (Global.databaseHr == null) || (Global.sd == null) || (Global.cflist == null) || (Global.dss == null);
        Log.d(TAG, "init req, res=" + result);
        return result;

    }

    /**
     * Autobackup backs observation lists, prefs and user database except for user object databases.
     * It does not backup content of external dir (dss,notes, files there) and standard databases from expansion pack.
     * Autobackup restores the app without standard databases from expansion pack.
     * Thus if they are absent then restore took place and we need to
     * re expand them again from expansion pack.
     * The function checks if some of the needed database is absent
     *
     * @param context
     * @return true if some of the databases are missing
     */
    public static boolean isDataExpansionRequired(Context context) {
        String[] dbBasic = {Constants.NGCIC_DATABASE_NAME, Comet.DB_NAME, MinorPlanet.DB_NAME_BRIGHT, DbManager.getInternalDbName(AstroCatalog.WDS), DbManager.getInternalDbName(AstroCatalog.CALDWELL), DbManager.getInternalDbName(AstroCatalog.MESSIER), DbManager.getInternalDbName(AstroCatalog.SNOTES), Constants.SQL_DATABASE_CROSS_DB, Constants.EP_DB, Constants.LOCATIONS_DB, Constants.SQL_DATABASE_COMP_DB, Constants.SQL_DATABASE_OREL_DB, Constants.SQL_DATABASE_HRCROSS_DB, Constants.SQL_DATABASE_SAO_TYC_DB};
        String[] databases = dbBasic;

        for (String name : databases) {
            File f = context.getDatabasePath(name);
            if (!f.exists()) return true;
        }
        return false;
    }

    /**
     * @param context
     * @param root    root folder in SAF
     * @return true if database list was read, false otherwise
     */
    public static boolean initDbList(Context context, DocumentFile root) {
        InfoList dbList = new InfoListImpl(DATABASE_LIST, DbListItem.class);
        InputStream in = null;
        try {
            String name = Constants.PREF_LIST_NAME_BASE + InfoList.DB_LIST;
            DocumentFile dfile = root.findFile(name);
            Uri uri = dfile.getUri();
            in = context.getContentResolver().openInputStream(uri);
            InfoListLoader loader = new InfoListLoaderImp(in);
            ErrorHandler ehan = dbList.load(loader);
            if (ehan.hasError()) {
                Log.d(TAG, "init, error loading init list,eh=" + ehan);
            }
        } catch (Exception e) {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(ba);
            e.printStackTrace(ps);
            Log.d(TAG, "initDbList, error loading list=" + InfoList.DB_LIST + " " + ba.toString());
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
        if (dbList.getCount() == 0) return false;

        // remove current DB_LIST only if another one is loaded successfully from the file
        ListHolder.getListHolder().remove(InfoList.DB_LIST);
        ListHolder.getListHolder().addNewList(InfoList.DB_LIST, dbList);


        if (dbList.getCount() == 0) {
            //adding NGCIC DATABASE
            DbListItem item = new DbListItem(QueryActivity.MENU_NGCIC, AstroCatalog.NGCIC_CATALOG, NGC_IC_CATALOG, Constants.NGCIC_DATABASE_NAME, new DbListItem.FieldTypes());
            InfoListFiller filler = new DatabaseManagerActivity.DbListFiller(Arrays.asList(new DbListItem[]{item}));
            dbList.fill(filler);
            dbList.setListName("" + AstroCatalog.NGCIC_CATALOG);
        }
        return true;
    }

    /**
     * @param context
     * @param dir
     * @param update  if true, update the list from file, if false, return if there is one
     * @return
     */
    public static void initDbList(Context context, File dir, boolean update) {
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        if (iL != null && !update) return;
        if (iL != null) ListHolder.getListHolder().remove(InfoList.DB_LIST);

        InfoList dbList = new InfoListImpl(DATABASE_LIST, DbListItem.class);
        ListHolder.getListHolder().addNewList(InfoList.DB_LIST, dbList);

        File f = null;
        InputStream in = null;
        try {
            f = new File(dir, Constants.PREF_LIST_NAME_BASE + InfoList.DB_LIST);
            in = new FileInputStream(f);
            InfoListLoader loader = new InfoListLoaderImp(in);
            ErrorHandler ehan = dbList.load(loader);
            if (ehan.hasError()) {
                Log.d(TAG, "init, error loading init list,eh=" + ehan);
            }
        } catch (Exception e) {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(ba);
            e.printStackTrace(ps);
            Log.d(TAG, "initDbList, error loading list=" + InfoList.DB_LIST + " " + ba.toString());
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }

        if (dbList.getCount() == 0) {
            //adding NGCIC DATABASE
            DbListItem item = new DbListItem(QueryActivity.MENU_NGCIC, AstroCatalog.NGCIC_CATALOG, NGC_IC_CATALOG, Constants.NGCIC_DATABASE_NAME, new DbListItem.FieldTypes());
            InfoListFiller filler = new DatabaseManagerActivity.DbListFiller(Arrays.asList(new DbListItem[]{item}));
            dbList.fill(filler);
            dbList.setListName("" + AstroCatalog.NGCIC_CATALOG);
        }
    }

    public static void initRootDirs(Context context) {
        Global.dsoPath = context.getExternalFilesDir(null).getAbsolutePath() + File.separator;
        Log.d(TAG, "Global.dsoPath=" + Global.dsoPath);
        Global.exportImportPath = Global.dsoPath;

    }


    public void initDirs() {
        try {
            initRootDirs(context);
            Global.databasesPath = context.getFilesDir().getAbsolutePath();
            Global.tmpPath = Global.dsoPath + TMP;
            Global.diskCachePushCamPath = Global.dsoPath + DISKCACHE;
            Global.DSSpath = Global.exportImportPath + DSS2; //dss images
            Global.notesPath = Global.exportImportPath + NOTES2;
            Global.customImagesPath = Global.exportImportPath + IMAGES2 + File.separator;
            Global.cameraPath = Global.exportImportPath + "pics" + File.separator;
            Logg.setFile(Global.cameraPath + "log.txt");
            Global.tycNewDb = new File(Global.databasesPath, TYCHOQ_DB);
            Global.tycNewDbRef = new File(Global.databasesPath, TYCHOQL_DB);

            Global.tycNewDbShort = new File(Global.databasesPath, TYCHOQS_DB);
            Global.tycNewDbShortRef = new File(Global.databasesPath, TYCHOQLS_DB);

            Global.pgcDbRef = new File(Global.databasesPath, PGCQL_DB);

            Global.ngcDb = new File(Global.databasesPath, NGCQ_DB);
            Global.ngcDbRef = new File(Global.databasesPath, NGCQL_DB);
            Global.ngcnDb = new File(Global.databasesPath, TN_DB);

            Global.ugcDb = new File(Global.databasesPath, UGCQ_DB);
            Global.ugcDbRef = new File(Global.databasesPath, UGCQL_DB);

            Global.conBoundaryDb = new File(Global.databasesPath, BQ_DB);
            Global.conBoundaryDbRef = new File(Global.databasesPath, BQL_DB);

            Global.milkyWayDb = new File(Global.databasesPath, MWQ_DB);
            Global.milkyWayDbRef = new File(Global.databasesPath, MWQL_DB);

            File dso = new File(Global.dsoPath);
            boolean res = new File(Global.exportImportPath).mkdirs();
            Log.d(TAG, "folder creation result=" + res);

            File dss = new File(Global.DSSpath);
            File images = new File(Global.customImagesPath);
            if (!dso.exists()) dso.mkdirs();
            if (!dss.exists()) dss.mkdirs();
            if (!images.exists()) images.mkdirs();
            File dbs = new File(Global.databasesPath);
            if (!dbs.exists()) dbs.mkdirs();
            File notes = new File(Global.notesPath);
            if (!notes.exists()) notes.mkdirs();

            File camera = new File(Global.cameraPath);
            camera.mkdirs();

            File tmp = new File(Global.tmpPath);
            tmp.mkdirs();

            new File(Global.diskCachePushCamPath).mkdirs();

            File db = context.getDatabasePath(Constants.NGCIC_DATABASE_NAME);

            db = context.getDatabasePath(MinorPlanet.DB_NAME_BRIGHT);
            if (Global.BASIC_VERSION) SettingsActivity.setMPsize(db.length());

            Global.log = new File(Global.exportImportPath, LOG_TXT);
            Logr.setFile(Global.exportImportPath + LOG_TXT);
            Logr.d(TAG, "Init, git hash=" + BuildConfig.GitHash);

        } catch (Exception e) {
            e.printStackTrace();
        }

        File db = context.getDatabasePath(Constants.NGCIC_DATABASE_NAME);
        db.getParentFile().mkdirs();

        context.getFilesDir().mkdirs();
    }

    private void initTime() {
        Calendar c = Calendar.getInstance();
        long start = SettingsActivity.getSharedPreferences(context).getLong(Constants.START_OBSERVATION_TIME, 0);
        if (start == 0) {
            SettingsActivity.putSharedPreferences(Constants.START_OBSERVATION_TIME, c.getTimeInMillis(), context);
        }

        long end = SettingsActivity.getSharedPreferences(context).getLong(Constants.END_OBSERVATION_TIME, 0);
        if (end == 0) {
            long endtime = c.getTimeInMillis() + 1000 * 60;//end time is 60 s ahead of start time
            SettingsActivity.putSharedPreferences(Constants.END_OBSERVATION_TIME, endtime, context);
        }

        Long time = SettingsActivity.getSharedPreferences(context).getLong(Constants.GLOBAL_CALENDAR, 0);
        if (time == 0) AstroTools.setDefaultTime(c, context);
        SettingsActivity.setCurrentTime(Calendar.getInstance().getTimeInMillis());
    }

    public void initLists() {
        int obsList = SettingsActivity.getSharedPreferences(context).getInt(Constants.ACTIVE_OBS_LIST, 0);
        if (obsList == 0)
            SettingsActivity.putSharedPreferences(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList, context);

        ListHolder.getListHolder().removeAll();//this is required as static variables are kept some time after the

        //program ends working
        InfoListHolder ilh = ListHolder.getListHolder();

        InfoList il2 = new InfoListImpl(NGCIC, ObsInfoListImpl.Item.class);
        ListHolder.getListHolder().addNewList(InfoList.NGCIC_SELECTION_LIST, il2);
        Log.d(TAG, "lists=" + ilh);

        InfoList obs1 = new ObsInfoListImpl(OBSERVATION_LIST_1);
        ListHolder.getListHolder().addNewList(InfoList.PrimaryObsList, obs1);
        Log.d(TAG, "lists=" + ilh);

        InfoList obs2 = new ObsInfoListImpl(OBSERVATION_LIST_2);
        ListHolder.getListHolder().addNewList(InfoList.PrimaryObsList + 1, obs2);
        Log.d(TAG, "lists=" + ilh);

        InfoList obs3 = new ObsInfoListImpl(OBSERVATION_LIST_3);
        ListHolder.getListHolder().addNewList(InfoList.PrimaryObsList + 2, obs3);
        Log.d(TAG, "lists=" + ilh);

        InfoList obs4 = new ObsInfoListImpl(OBSERVATION_LIST_4);
        ListHolder.getListHolder().addNewList(InfoList.PrimaryObsList + 3, obs4);
        Log.d(TAG, "lists=" + ilh);

        InfoList dbList = new InfoListImpl(DATABASE_LIST, DbListItem.class);
        ListHolder.getListHolder().addNewList(InfoList.DB_LIST, dbList);
        Log.d(TAG, "lists=" + ilh);

        InfoList list = new InfoListImpl(SEARCH_REQUEST_LIST, SearchRequestItem.class);
        ListHolder.getListHolder().addNewList(InfoList.SREQUEST_LIST, list);
        Log.d(TAG, "lists=" + ilh);

        InfoList telescopes = new InfoListImpl(TELESCOPES_LIST, TelescopeRecord.class);
        ListHolder.getListHolder().addNewList(InfoList.TELESCOPE_LIST, telescopes);
        Log.d(TAG, "lists=" + ilh);

        InfoList locList = new InfoListImpl(LOCATIONS_LIST, LocationItem.class);
        ListHolder.getListHolder().addNewList(InfoList.LOCATION_LIST, locList);
        Log.d(TAG, "lists=" + ilh);

        InfoList conlist = new InfoListImpl(CONTOUR, ContourObject.class);
        ListHolder.getListHolder().addNewList(InfoList.NEBULA_CONTOUR_LIST, conlist);
        loadContourList(conlist);
        Log.d(TAG, "lists=" + ilh);

        InfoList preflist = new InfoListImpl(PREF_LIST, ShItem.class);
        ListHolder.getListHolder().addNewList(InfoList.PREFERENCE_LIST, preflist);

        InfoList ngclist = new InfoListImpl(NGC_LIST, NgcPicListItem.class);
        ListHolder.getListHolder().addNewList(InfoList.NGC_PIC_LIST, ngclist);
		
        new Prefs(context).loadLists();

    }

    private void initPlanets() {
        Calendar c = AstroTools.getDefaultTime(context);
        Global.planets = new ArrayList<Planet>();

        Global.mercury = new Planet(Planet.PlanetType.Mercury, c, context);
        Global.planets.add(Global.mercury);

        Global.uranus = new Planet(Planet.PlanetType.Uranus, c, context);
        Global.planets.add(Global.uranus);

        Global.venus = new Planet(Planet.PlanetType.Venus, c, context);
        Global.planets.add(Global.venus);
        Global.mars = new Planet(Planet.PlanetType.Mars, c, context);
        Global.planets.add(Global.mars);

        Global.sun = new Planet(Planet.PlanetType.Sun, c, context);
        Global.planets.add(Global.sun);

        Global.jupiter = new Planet(Planet.PlanetType.Jupiter, c, context);
        Global.planets.add(Global.jupiter);

        Global.saturn = new Planet(Planet.PlanetType.Saturn, c, context);
        Global.planets.add(Global.saturn);

        Global.uranus = new Planet(Planet.PlanetType.Uranus, c, context);
        Global.planets.add(Global.uranus);

        Global.neptune = new Planet(Planet.PlanetType.Neptune, c, context);
        Global.planets.add(Global.neptune);

        Global.moon = new Planet(Planet.PlanetType.Moon, c, context);
        Global.planets.add(Global.moon);
    }

    private void initStars() {
        StarMags.initMags(context);
        FileTools.copyHrDatabase(context);//hr

        Quadrant.quadrants = Quadrant.quadrantsHip;

        Global.sd = new StarData();
        Global.sd.list = new MyList[Quadrant.quadrants.length];

        loadList(R.raw.hrlist, Global.sd.list);

        ConFigure.fillConstellationList();
        Global.cflist = new MyList[Quadrant.quadrants.length];
        loadList(R.raw.cflist, Global.cflist);
        loadTychoRef();
        loadTychoShortRef();
        loadUcac4Ref(context);
        loadPgcRef(context);
        loadNgcRef();
        loadConBoundaryRef();
        loadMilkyWayRef();
    }

    private void initDss() {
        Global.dss = new DSS();
        createDSSFileList();
    }
	
    private void loadContourList(InfoList list) {
        InputStream in = context.getResources().openRawResource(R.raw.contours);
        InfoListLoader loader = new InfoListLoaderImp(in);
        try {
            list.load(loader);
        } catch (Exception e) {
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
    }


    private boolean loadList(int resid, MyList[] list) {
        boolean flag = true; // represent successfull reading
        try {
            InputStream fin = context.getResources().openRawResource(resid);//new FileInputStream(f);
            DataInputStream in = new DataInputStream(new BufferedInputStream(fin));
            for (int k = 0; k < list.length; k++)
                list[k] = new MyList();
            for (MyList aList : list) {
                int num = in.readInt();
                for (int j = 0; j < num; j++) {
                    aList.add(in.readInt());
                }
            }

        } catch (Exception e) {
            Log.d(TAG, "exception=" + e);
            flag = false;
        }
        return flag;
    }

    public static void loadTychoRef() {
        Global.tycQ = new ArrayList<Integer>();

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(Global.tycNewDbRef);
            DataInputStream in = new DataInputStream(new BufferedInputStream(fin));
            int sum = 0;
            Global.tycQ.add(0);//zero quadrant has zero reference
            while (true) {
                int length = in.readInt();
                sum += length;
                Global.tycQ.add(sum);
            }
        } catch (Exception e) {

        } finally {
            try {
                fin.close();
            } catch (Exception e) {

            }
        }

    }

    public static void loadTychoShortRef() {
        Global.tycQshort = new ArrayList<Integer>();

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(Global.tycNewDbShortRef);
            DataInputStream in = new DataInputStream(new BufferedInputStream(fin));
            int sum = 0;
            Global.tycQshort.add(0);//zero quadrant has zero reference
            while (true) {
                int length = in.readInt();
                sum += length;
                Global.tycQshort.add(sum);
            }
        } catch (Exception e) {

        } finally {
            try {
                fin.close();
            } catch (Exception e) {

            }
        }
    }


    public static void loadUcac4Ref(Context context) {
        //boolean flag=true; // represent successfull reading
        Global.ucac4Q = new ArrayList<Integer>();
        if (Global.BASIC_VERSION) return;
        if (Global.PLUS_VERSION) {
            String path = APKExpansion.getExpPath(context, Global.mainVersion);
            File f = new File(path);
            if (f != null && f.length() > 529456345) return;
        }
        RandomAccessFile fin = null;
        PrintStream ps = null;
        try {
            fin = new RandomAccessFile(APKExpansion.getExpPath(Global.getAppContext(), Global.mainVersion), "r");
            long offset = SettingsActivity.getSharedPreferences(Global.getAppContext()).getLong(Constants.UCAC4QL_START_POS, -1);
            if (offset == -1) return;
            long size = SettingsActivity.getSharedPreferences(Global.getAppContext()).getLong(Constants.UCAC4QL_SIZE, -1);
            if (size == -1) return;
            fin.seek(offset);

            int sum = 0;
            Global.ucac4Q.add(0);//zero quadrant has zero reference
            while (true) {
                int length = fin.readInt();
                //	ps.println(""+length);
                long pos = fin.getFilePointer();
                if (pos - offset >= size) break;
                sum += length;
                Global.ucac4Q.add(sum);
            }
        } catch (Exception e) {
        } finally {
            try {
                fin.close();
            } catch (Exception e) {

            }
        }

    }


    public static void loadPgcRef(Context context) {
        Global.pgcQ = new ArrayList<Integer>();
        if (Global.BASIC_VERSION) return;
        if (Global.PLUS_VERSION) {
            String path = APKExpansion.getExpPatchPath(context, Global.patchVersion);
            File f = new File(path);
            if (f != null && f.length() > 56567541) return;
        }
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(Global.pgcDbRef);
            DataInputStream in = new DataInputStream(new BufferedInputStream(fin));
            int sum = 0;
            Global.pgcQ.add(0);//zero quadrant has zero reference
            while (true) {
                int length = in.readInt();
                sum += length;
                Global.pgcQ.add(sum);
            }
        } catch (Exception e) {
        } finally {
            try {
                fin.close();
            } catch (Exception e) {

            }
        }

    }

    public static void loadNgcRef() {
        Global.ngcQ = new ArrayList<Integer>();
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(Global.ngcDbRef);
            DataInputStream in = new DataInputStream(new BufferedInputStream(fin));
            int sum = 0;
            Global.ngcQ.add(0);//zero quadrant has zero reference
            while (true) {
                int length = in.readInt();
                sum += length;
                Global.ngcQ.add(sum);
            }
        } catch (Exception e) {
        } finally {
            try {
                fin.close();
            } catch (Exception e) {

            }
        }
    }

    public static void loadConBoundaryRef() {
        Global.conBoundaryQ = new ArrayList<Integer>();
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(Global.conBoundaryDbRef);
            DataInputStream in = new DataInputStream(new BufferedInputStream(fin));
            int sum = 0;
            Global.conBoundaryQ.add(0);//zero quadrant has zero reference
            while (true) {
                int length = in.readInt();
                sum += length;
                Global.conBoundaryQ.add(sum);
            }
        } catch (Exception e) {
        } finally {
            try {
                fin.close();
            } catch (Exception e) {

            }
        }
    }

    public static void loadMilkyWayRef() {
        Global.milkyWayQ = new ArrayList<Integer>();
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(Global.milkyWayDbRef);
            DataInputStream in = new DataInputStream(new BufferedInputStream(fin));
            int sum = 0;
            Global.milkyWayQ.add(0);//zero quadrant has zero reference
            while (true) {
                int length = in.readInt();
                sum += length;
                Global.milkyWayQ.add(sum);
            }
        } catch (Exception e) {
        } finally {
            try {
                fin.close();
            } catch (Exception e) {

            }
        }
    }

    public static void createDSSFileList() {
        DSS.mFileList = new ConcurrentLinkedQueue<Holder3<Double, Double, String>>();

        File fdir = new File(Global.DSSpath);

        File[] picFiles = null;
        picFiles = fdir.listFiles();

        if (picFiles == null || picFiles.length == 0) return;


        for (File f : picFiles) {
            String name = f.getName();
            Holder2<Double, Double> h1 = DSS.getRaDec(name);
            if (h1 != null)
                DSS.mFileList.add(new Holder3<Double, Double, String>(h1.x, h1.y, name));
        }
    }
}
