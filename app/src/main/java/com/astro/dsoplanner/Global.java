package com.astro.dsoplanner;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.astro.dsoplanner.base.ConPoint;
import com.astro.dsoplanner.base.HrStar;
import com.astro.dsoplanner.base.Planet;
import com.astro.dsoplanner.graph.DscRec;
import com.astro.dsoplanner.graph.cuv_helper.DSS;

import java.io.File;
import java.util.Calendar;
import java.util.List;

//class of static global variables
public class Global {
    public static final String EPDATABASE_CALLER = "epdabasecaller";
    public static final String DB_IMPORT_RUNNING = "Import for this database is running!";
    public static final boolean BASIC_VERSION = GlobalFlavours.BASIC_VERSION;
    public static final boolean PLUS_VERSION = GlobalFlavours.PLUS_VERSION;
    public static final boolean EXCEPTION_TRACKER = true;
    public static final boolean TEST_MODE = false;

    public static final String PLAYSTORE_SIGNATURE = "5oGhR5aqBD0Z0UcVfvCbEKle/gw=";
    public static final boolean GITHUB_DOWNLOAD = true;
    public static final String PACKAGE_NAME_PRO = "com.astro.dsoplanner";

    /**
     * use this for obs list DSS pic downloading in DSS list
     * Not for production purposes!!!
     */
    public static final boolean DOWNLOAD_OBS = false;//

    public static boolean ALEX_MENU_FLAG = true;
    public static boolean ALEX_DIALOG_MESSAGE = true;    //false - use standard Toast.makeText instead of InputDialog.message
    public static String dsoPath = ""; //Default, can be changed in settings permanently
    public static String DSSpath = ""; //dss images
    public static String databasesPath = "";
    public static String diskCachePushCamPath = "";
    public static String customImagesPath = "";
    public static String cameraPath = "";
    public static String notesPath = "";
    public static String exportImportPath = "";
    public static String tmpPath = "";
    public static File tycNewDb;
    public static File tycNewDbRef;
    public static File tycNewDbShort;
    public static File tycNewDbShortRef;
    public static File pgcDbRef;
    public static File ngcDb;
    public static File ngcnDb;
    public static File ngcDbRef;
    public static File ugcDb;
    public static File ugcDbRef;
    public static File conBoundaryDb;
    public static File conBoundaryDbRef;
    public static File milkyWayDb;
    public static File milkyWayDbRef;
    public static Calendar calibrationTime;
    public static StarData sd; //containts a reference to a list of quadrants numbers/belongin HR stars
    public static HrStar[] databaseHr;//HR database
    public static List<ConPoint> cf;//con figures
    public static MyList[] cflist;//cflist[k] containts a list of constellation points belonging to k th quadrant
    public static List<Planet> planets; //
    public static Planet mars;
    public static Planet mercury;
    public static Planet venus;
    public static Planet moon;
    public static Planet sun;
    public static Planet jupiter;
    public static Planet saturn;
    public static Planet uranus;
    public static Planet neptune;
    public static int noteListDSO;
    public static DSS dss;
    public static List<Integer> tycQ;//list of positions of a start o a portion of object belonging to the quadrant
    public static List<Integer> tycQshort;
    public static List<Integer> pgcQ;//list of positions of a start o a portion of object belonging to the quadrant
    public static List<Integer> ngcQ;
    public static List<Integer> ugcQ;
    public static List<Integer> conBoundaryQ;
    public static List<Integer> milkyWayQ;
    public static List<Integer> ucac4Q;//list of positions of a start o a portion of object belonging to the quadrant
    public static final String assign_char = "=";
    public static final String delimiter_char = ";";
    public static boolean lockCursor = false;
    private static Context appContext;

    public static synchronized void setAppContext(Context context) {
        appContext = context;
    }

    public static synchronized Context getAppContext() {
        return appContext;
    }

    //for gesture control
    public static int screenH = 0;
    public static int screenW = 0;
    public static float flickLength = 50;

    public static Calendar twtime = null;//date time in Twilight activity

    //Expansion pack retrieval
    public static final String GITHUB_EXPPACK_URL;
    public static final String GITHUB_EXPPATCH_URL;
    public static final String GITHUB_URL_PREFIX = "https://github.com/dsoastro/dsoplanner/releases/download/data/";
    public static final int mainVersion;

    static {

        if (Global.BASIC_VERSION) {
            mainVersion = 16;
            GITHUB_EXPPACK_URL = GITHUB_URL_PREFIX + "main.16.com.astro.dsoplannerbasic.obb";
            GITHUB_EXPPATCH_URL = GITHUB_URL_PREFIX + "patch.16.com.astro.dsoplannerbasic.obb";

        } else if (Global.PLUS_VERSION) {
            mainVersion = 17;
            GITHUB_EXPPACK_URL = GITHUB_URL_PREFIX + "main.17.com.astro.dsoplannerplus.obb";
            GITHUB_EXPPATCH_URL = GITHUB_URL_PREFIX + "patch.17.com.astro.dsoplannerplus.obb";

        } else {
            mainVersion = 22;
            GITHUB_EXPPACK_URL = GITHUB_URL_PREFIX + "main.22.com.astro.dsoplanner.obb";
            GITHUB_EXPPATCH_URL = GITHUB_URL_PREFIX + "patch.22.com.astro.dsoplanner.obb";
        }
    }

    public static final int patchVersion;

    static {

        if (Global.BASIC_VERSION) {
            patchVersion = 16;
        } else if (Global.PLUS_VERSION) {
            patchVersion = 17;
        } else {
            patchVersion = 22;
        }
    }

    public static String LAST_COMET_UPDATE_DATE = null;
    public static boolean antialiasing = false; //antialiasing Sky drawing

    public static final int SHARE_LINES_LIMIT = 300;
    public static final int OBS_LIST_NUM_OBJECTS_LIMIT = 5000;
    public static final int SQL_SEARCH_LIMIT = 15000;

    //free space required for exp pack. Change value in string error_not_enough_free_space as well!!!
    public static final long EXP_PACK_FREE_SPACE_REQUIRED;//=50*1024*1024L;

    static {
        if (Global.BASIC_VERSION)
            EXP_PACK_FREE_SPACE_REQUIRED = 20 * 1024 * 1204L;//dss pics, tycho layer
        else if (Global.PLUS_VERSION)
            EXP_PACK_FREE_SPACE_REQUIRED = 42 * 1024 * 1024L;//31 tychoq +11 wds
        else//pro
            EXP_PACK_FREE_SPACE_REQUIRED = 42 * 1024 * 1024L; //31 tychoq +11 wds
    }

    public static final long EXP_PACK_PATCH_FREE_SPACE_REQUIRED;
    public static final long EXP_PATCH_SIZE_PRO = 87279709L;
    public static final long EXP_PATCH_SIZE_PLUS = 55682775L;
    public static final long EXP_PATCH_SIZE_BASIC = 20047752L;

    static {
        if (Global.BASIC_VERSION)
            EXP_PACK_PATCH_FREE_SPACE_REQUIRED = EXP_PATCH_SIZE_BASIC;//ngc,tychos layers
        else if (Global.PLUS_VERSION) EXP_PACK_PATCH_FREE_SPACE_REQUIRED = 18 * 1024 * 1024L;
        else//pro
            EXP_PACK_PATCH_FREE_SPACE_REQUIRED = 24 * 1024 * 1024L;//minus pgc layer files and wds
    }

    public static final int COMMON_NAME_MIN_LENGTH_FOR_EXT_SEARCH = 4;//for extensive search

    public static File log;
    public static int server_version = -1;

    /**
     * Max attempts to ask for permission if it was not granted
     */
    public static int max_permission_attempts = 4;
    public static DscRec dscR = new DscRec();
}
