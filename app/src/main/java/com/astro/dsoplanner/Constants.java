package com.astro.dsoplanner;


import android.provider.BaseColumns;

public interface Constants extends BaseColumns {

    public static final String TABLE_NAME = "ngcic";
    public static final String TABLE_NAME_HR = "hr";
    public static final String NOTE_DATABASE_NAME = "notemain.db";
    public static final String EYEPIECES_DATABASE_NAME = "eyepiecesmain.db";
    public static final String TELESCOPE_DATABASE_NAME = "scopemain.db";
    public static final String LAUNCHER_DATABASE_NAME = "launchermain.db";
    public static final String NGCIC_DATABASE_NAME = "ngcic.db";
    public static final String PREF_LIST_NAME_BASE = "preferencelist";
    public static final String COMMON_NAME_MAP = "common_name_db";
    // Columns in the ngcic database
    public static final String NAME = "name";
    public static final String RA = "ra";
    public static final String DEC = "dec";
    public static final String MAG = "mag";
    public static final String QUADRANT = "quadrant";
    public static final String A = "a";
    public static final String B = "b";
    public static final String CONSTEL = "constellation";

    public static final String TYPE = "type";
    public static final String TYPESTR = "typestr";
    public static final String PA = "pa";
    public static final String MESSIER = "messier";
    public static final String CALDWELL = "caldwell";
    public static final String HERSHELL = "hershel";
    public static final String FLAMSTEED = "flamsteed";
    public static final String BAYER = "bayer";

    public static final String CATALOG = "catalog";
    public static final String ID = "id";
    public static final String NAME1 = "name1";
    public static final String NAME2 = "name2";
    public static final String SELECTED = "selected";

    public static final String HR = "hr";

    public static final String LATITUDE = "Hqb84PPn8f";
    public static final String LONGITUDE = "CWCNpMxTgN";

    public static final String TYC1 = "tyc1";
    public static final String TYC2 = "tyc2";

    public static final String TYC3 = "tyc3";
    public static final String HIP = "hip";
    public static final String COMMENT = "comment";
    public static final String PATH = "path";
    public static final String NOTE = "note";
    public static final String NOTEBASEID = "notebaseid";
    public static final String NOTEDATABASE = "notedatabase";
    public static final String DATE = "date";
    public static final String TIME = "time";
    ;
    public static final String ALT = "alt";
    public static final String VIS = "vis";//visibility
    public static final String PREFS = "DSOPlannerPreferences";
    public static final String SQL_SEARCH_STRING = "sqlstring";
    public static final String LOCAL_SEARCH_STRING = "localstring";


    public static final String SEARCH_DB = "xymdfywvEJ5qS";//database used in search
    public static final String UCAC4_START_POS = "HZ1SbbmCnUm";//start pos in exp pack

    public static final String UCAC4QL_START_POS = "8WLmIsfuFr9";//start pos in exp pack
    public static final String UCAC4QL_SIZE = "Qt7CiancORG8w";
    public static final String PGC_START_POS_PATCH = "RFbftTjksUc647";

    public static final String SEARCH_FIELDS = "DrlgjLSiDIzZh";
    public static final String SEARCH_STRING = "FAZxys37153piU";
    public static final String FIND_STRING = "bLTOK40MCrIRn";

    public static final String OBS_FIND_STRING = "CvxWkvz7CXMwK";
    public static final String VDA_FIND_STRING = "Oh8jaXG69";
    public static final String CURRENT_ZOOM_LEVEL = "NF15hnhFFm";//changed by Graph Activity, used to launch Graph activity at latest zoom level

    public static final String ASTRO_OBJECT_INTENT = "GbGvlb6eiqr";//used for passing astro objects in intents
    public static final String NO_MORE_BUTTON = "WgNoFqyPeL9ay";
    public static final String CALLER_GRAPH = "sK6zQhtukndX";//used in Graph for setting caller activity for details
    public static final String GLOBAL_CALENDAR = "pu7esmh";
    public static final String START_OBSERVATION_TIME = "sVYRoIH2KyUF6o";
    public static final String END_OBSERVATION_TIME = "PhZihFfXPGyEi";
    public static final String ALIGN_ADJ_AZ = "hsqd63cD8FbX1";//one star align
    public static final String ALIGN_ADJ_ALT = "6dzJJlux2NAob";//one star align
    public static final String ALIGN_TIME = "pkihpoji37645dcGFd";//one star align time
    public static final String QUERY_UPDATE = "GFTo4a2tdpLL3";//used by dso selection to switch update/select btn

    public static final String DTP_DISPLAY_MODE = "dEg9EwVTlPy";//used for passing info into Date Time Picker
    public static final String DTP_TIME = "iyF0UWeRB8UpY";//used for passing time to set into DateTimePicker
    public static final String DTP_RT = "qweognc75svpokrgkj";
    public static final String GRAPH_OBJECT = "465Of5BIuM13";//for passing object to sky view
    public static final String GRAPH_DOWNLOAD_STATUS = "PalHmm6xP1HRB";

    public static final String GRAPH_NEARBY_STRING_FOV = "Lua6RzIRLLcBXTq";
    public static final String GRAPH_NEARBY_STRING_DL = "SesJtmRDS2X";
    public static final String GRAPH_NEARBY_SEARCH_DB = "kgUQx2CP8BYBq";

    public static final String ACTIVE_OBS_LIST = "U5qpvLINM";
    public static final String FSA_DBNAME = "JGZ7lAF4GJW";//for passing info to field selection activity
    public static final String NCOA_OBJECT = "dP9MsR29VfnBQE";
    public static final String MAX_CATALOG_ID = "UfVsVQY8xdTWQ";//used for DbManager
    public static final String QUERY_CONTROLLER_SPIN_POS = "LPf9dRenShuI";//used in Query Controller for keeping spinner position
    public static final String SRA_ACTIVE_SEARCH_REQUEST = "kTaHHDmMEWcMZD";//used for saving search requests in search request activity
    public static final String BATTERY_LEVEL = "3s7LtB1jpSQoOP";
    public static final String GEO_LAST_KNOWN = "cj21GX3Ril05cn";
    public static final String GEO_PROVIDER = "EjFMZRwB";

    public static final String GEO_LAST_START = "aNZjwd0eD";
    public static final String GEO_LAST_UPDATE = "Dk5RUP6TDXu";//last update (current of last known)
    public static final String GEO_DETAILS_UPDATE = "YWuSeQbt7Bc4";
    public static final String BTCOMM_MESSAGE = "NlWz3aU3acp1b";

    public static final String BTCOMM_RA = "mIPX5dCkfA2JDC";
    public static final String BTCOMM_DEC = "apHUf3egTHPB";
    public static final String BTCOMM_SCOPE = "jhsgr75tydgJhfbvAslj";
    public static final String BTCOMM_SCOPE_TRACKING_OFF = "37865sdfgjsdhfg764trsd";
    //passing info to ImportDbIntentService
    public static final String IDIS_DBNAME = "Ta2h22OUY1DyoP4";

    public static final String IDIS_PASTING = "9nysGeXf1SI9l";
    public static final String IDIS_CATALOG = "2PBUxIKSoR";
    public static final String IDIS_FTYPES = "MsdhUbRCXBevr";
    public static final String IDIS_FILENAME = "ROOyqfNRhWOKOg";
    public static final String IDIS_CMO_UPDATE = "tVo9AoFlZ8hov";//update after download
    public static final String IDIS_NOTES = "LmHEVTucDJL8";//importing into notes catalog
    public static final String IDIS_IGNORE_NGCIC_REF = "jvMHpojsbxZ6BX";
    public static final String IDIS_IGNORE_CUSTOMDB_REF = "sdvJ1dZp0h";
    //passing info from ImportDbIntentService
    public static final String IDIS_ERROR_STRING = "G9RZpyZLYeD";
    public static final String IDIS_NSOG = "sns693gjejkngkdTRT";
    public static final String NSOG_IMPORT_RUNNING = "vrbbrvr29ut5t64rrut86905";
    //passing info from ExpPackIntentService
    public static final String EXP_PACK_MESSAGE = "jhsbjh56820rt";

    public static final String ACTION_EP_SHOW = "278364twshgfsdfuyerte7fgv";//com.astro.dsoplanner.EyepiecesList.View
    public static final String ACTION_EP_SELECT = "nrewf769dbfhyuhgsqkd";
    public static final String SELECTED_EYEPIECE = "530dfg36jdkjsjr7549pdvnu";
    public static final String SELECTED_TELESCOPE = "nxcbvTTgfgRRDF7353";
    public static final String ACTION_TELESCOPE_SELECT = "219874wjhg765ygdjfhgvbdffh7";

    //BROADCAST
    public static final String IDIS_ERROR_BROADCAST = "YNBXLP9lb4Tj";
    public static final String TEST_BROADCAST = "VBTEDjsjhgfsh872386387";
    public static final String TEST_ACTIVITY = "ErhTRdgh64759";
    public static final String TEST_ACTIVITY_ACTION = "NbvfHJret54309sdvhbhsddf";
    public static final String TEST_ACTIVITY_PARAM = "bcvbYYTFjnsjbfuasUyyygVA34";
    public static final String GEO_BROADCAST = "8WLC6yCUN6";
    public static final String BTCOMM_MESSAGE_BROADCAST = "2IZR4Q6FIuQfx";
    public static final String BTCOMM_UPDATE_BROADCAST = "EkRBs5=EQ2";

    public static final String BTCOMM_RADEC_BROADCAST = "JPJIkxL72565zj";
    public static final String PUSH_CAMERA_PHOTO_READY = "uwr76457346r";
    public static final String DOWNLOAD_BROADCAST = "Of6jMK9PQgWG";
    public static final String EXP_PACK_BROADCAST = "jhsfbhjsvtjo";

    public static final String QSTART_DIALOG_FLAG = "QpOFEiK1YdDO";

    //expansion pack
    public static final String EP_EXPANDED = "JaGLI7rdhZ7rgk5411xx";
    public static final String EP_PATCH_EXPANDED = "abcgtytfff254211xx";//change the value when expansion patch has changed
    public static final String EP_BEING_EXPANDED = "nvekjndf457692kxx";//keep there time of the exp pack start in millis
    public static final String EP_PATCH_BEING_EXPANDED = "NBVhskTYkdjxx";//keep there time of the exp pack start in millis
    public static final long EP_BEING_EXPANDED_DEF_VALUE = -2;
    public static final long EP_PATCH_BEING_EXPANDED_DEF_VALUE = -2;
    public static final String OBS_LIST_MOVE_DIALOG = "ytw5iuhHHJ737JJ";
    //download of pics in obs list
    public static final String OBS_DOWNLOAD = "EiBDjrU73";
    /**
     * date in the header from the site
     */
    public static final String COMET_UPDATE_DATE_IN_HEADER = "IWEUYt111trVHGF56W9";
    public static final String LAST_COMET_UPDATE_TIME = "786jgbsgquq368ytr"; //timeinmillis(), used if there there is no relevant header in server response

    public static final String XML_NUM = "sbwh46943h12";//used for passing info to SettingsIncl

    //used in StarMags
    public static final String YALE_MAG = "bxvnibist9509";
    public static final String TYCHO_MAG = "763hdfjxcmn895";
    public static final String UCAC_MAG = "8939823dfbsi";
    public static final String PGC_MAG = "sjhsdjh8459834jhds";
    public static final String NGC_MAG = "skjfauwstfsjhfnb45456";
    public static final String UGC_MAG = "jhxbvhje975bfbasf";
    //keeping shared pref in graph
    public static final String STAR_MAG_LIMIT_CATALOG = "nxbv73784578gf";
    public static final String RETURN_TO_GRAPH = "76376bdsfbsjbvfqgdnc";//for global search dialog. return to previous graph screen
    public static final String WHATSNEW_FLAG = "NXNhghRTR656qqv222341";
    public static final boolean WHATSNEW_FLAG_DEF_VALUE = false;
    public static final String SETTINGS_SEARCH_CATALOG = "vbTRYFcjkkbc34";
    public static final String SETTINGS_SEARCH_CATALOG_UPDATE = "ncbvjagrege536";
    public static final String REAL_TIME_DIALOG_CHOICE = "jskfbh4784FGHK";
    public static final String ADD_BTN_OFF = "bvmrteg6812svdg";

    public static final String CATALOG_SELECTION_PREF = "hfGGDajbf875873bjsbksjh";
    public static final String CATALOG_SELECTION_PREF_SEARCH_NEARBY = "bc23gfh55869jhgdhg365";
    public static final String RESET_NGCIC_COMET_MINOR_PLANET_DBS_FLAG = "HJSDF3265FHGSVFHSGFFD";
    public static final String QUERY_ACTIVITY_NAME = "bbcs6647qdshuf";
    public static final String GRAPH_OBJ_TYPES_STATE = "kjfishfnbvjx276562w7rfgj";
    public static final String BASIC_SEARCH_OBJ_TYPES_STATE = "kjfishfnbvjx276562w7rfgj";

    public static final String DATE_TIME_PICKER_MILLIS = "mzcnuywr6shfjsyewrtdf4534";//passing values in intent from DateTimePicker
    public static final String MDSS_DATA = "jbvcjsbv45873bvbauw7";
    public static final String SELECTOR_FNAMES = "zhbcsu754763bcml";
    public static final String SELECTOR_FIELDS = "563267ghsdhvdfg";
    public static final String SELECTOR_FNAME = "bcbvytuewo7575";
    public static final String SELECTOR_TABLE = "jscvnhs532gn";
    public static final String SELECTOR_DBNAME = "bvnv65748nqje";
    public static final String SELECTOR_RESULT = "xknvxnbgid45048hgdg";
    public static final String SELECTOR_ACTIVITY_NAME = "bvn6583ghafdafbkd";
    public static final String SELECTOR_CALLING_ACTIVITY = "reywu4657hf657";
    public static final String SELECTOR_SEARCH_BUTTON = "orbcvhsf57tkjdgk";
    public static final String SELECTOR_SEARCH_TITLE = "wqrhcvbsg643ugfmne76230";
    public static final String LIST_SELECTOR_NAMES = "eqeryrensad64739";
    public static final String LIST_SELECTOR_CHECKS = "prhvcbsnfj3476dsjk436";
    public static final String LIST_SELECTOR_NAME = "kdffhuydgfs74576sbgq543";
    public static final String QUERY_CHECK_BOXES = "klfjhruyNVxyaqf37865";
    public static final boolean QUERY_CHECK_BOXES_DEF_VALUE = false;
    public static final String QUERY_HEAD_CHECK_BOX = "shjgudsgfhs86546dgsdf3";
    public static final boolean QUERY_HEAD_CHECK_BOX_DEF_VALUE = false;
    public static final String DBS_COPIED = "ASQWuhgsfshRdfdg753674";
    public static final String MP_UPDATED = "Znvgw2133365657";
    public static final String MP_LAST_UPDATE_NUMBER = "snbdvfshg6253RREgas";
    public static final String MP_LAST_UPDATE_TIME = "hugsfywt7654wvbxcxzbvsjh";
    public static final String MP_CHANGE_DEF_UPDATE_URL = "mnsbuksytr7456yrwjshgfjsdhf";
    public static final String NOTEMAIN_UPDATED = "bcsbjFEFCDVHF376yshgf";
    public static final String SQL_DATABASE_COMP_DB = "comp.db";
    public static final String SQL_DATABASE_OREL_DB = "orel.db";
    public static final String SQL_DATABASE_CROSS_DB = "cross.db";
    public static final String SQL_DATABASE_SAO_TYC_DB = "saotyc.db";
    public static final String EP_DB = "ep.db";
    public static final String LOCATIONS_DB = "locations.db";
    public static final String SHOW_QUERY_CUV = "65q34afsjh963bfjdhg";
    public static final String SHOW_OBS_CUV = "mnxbve6537trgdjfdfdf4455gbe";
    public static final String SHOW_OBS_IMAGES = "tqeyt26534rsbgrtlkiu607";
    public static final String SHOW_NOTELIST_IMAGES = "tqedmnsbu456tyribibas";

    public static final String FILE_DIALOG_PATH = "a764gfufvf3teugbgeudvbe";
    public static final String FILE_DIALOG_URI = "b869gbaf4f8tpugb0eudmba";

    public static final String GRAPH_CALLING = "jbxv346TtFSHGVFY";
    public static final String FIRST_TIME_ENTRY_OBJ_SEL = "iuwhehr87we6rygjhdhgf87";
    public static final String FIRST_TIME_ENTRY_OBS_LIST = "iuwhehr56we6rygjhdhgf87";
    public static final String FIRST_TIME_ENTRY_GRAPH = "iuweyr63sdvsd17654e27u";
    public static final String GRAPH_UPDATE_ZOOM_LEVEL = "oojssht254trfsdhfvsdhf";
    public static final String PERMISSIONS_BASE = "a635wjdmvrrbfkdndfjg";
    public static final String IS_DSS_DOWNLOAD_URL_UPDATED = "NCBVUWYE6R754RWEYUFG";

    public static final String PUSH_CAMERA_MODE = "jshf84561w7465shjdfgj";

    public static final String SQL_DATABASE_HRCROSS_DB = "hrcross.db";    //as used in astro tools is out of obf scope
    public static final int EXP_PACK_ERROR = 1;
    public static final int EXP_PACK_COMPLETE = 2;

    public static final int DEFAULT_ZOOM_LEVEL = 8;

    public static final int NOTE_LIST_GRAPH_CALLING = 1;

    public static String[] constellations = new String[]{"", "And", "Ant", "Aps", "Aqr", "Aql", "Ara", "Ari", "Aur", "Boo", "Cae", "Cam", "Cnc", "CVn", "CMa", "CMi", "Cap", "Car", "Cas", "Cen", "Cep", "Cet", "Cha", "Cir", "Col", "Com", "CrA", "CrB", "Crv", "Crt", "Cru", "Cyg", "Del", "Dor", "Dra", "Equ", "Eri", "For", "Gem", "Gru", "Her", "Hor", "Hya", "Hyi", "Ind", "Lac", "Leo", "LMi", "Lep", "Lib", "Lup", "Lyn", "Lyr", "Men", "Mic", "Mon", "Mus", "Nor", "Oct", "Oph", "Ori", "Pav", "Peg", "Per", "Phe", "Pic", "Psc", "PsA", "Pup", "Pyx", "Ret", "Sge", "Sgr", "Sco", "Scl", "Sct", "Ser", "Sex", "Tau", "Tel", "Tri", "TrA", "Tuc", "UMa", "UMi", "Vel", "Vir", "Vol", "Vul"};
    public static final char[] greek = new char[]{'\u03B1', '\u03B1', '\u03B2', '\u03B3', '\u03B4', '\u03B5', '\u03B6', '\u03B7', '\u03B8', '\u03B9', '\u03BA', '\u03BB', '\u03BC', '\u03BD', '\u03BE', '\u03BF', '\u03C0', '\u03C1', '\u03C3', '\u03C4', '\u03C5', '\u03C6', '\u03C7', '\u03C8', '\u03C9'};//'\u03C2'
    public static String[] constellationLong = new String[]{"", "Andromeda", "Antlia", "Apus", "Aquarius", "Aquila", "Ara", "Aries", "Auriga", "Bootes", "Caelum", "Camelopardalis", "Cancer", "Canes Venatici", "Canis Major", "Canis Minor", "Capricornus", "Carina", "Cassiopeia", "Centaurus", "Cepheus", "Cetus", "Chamaeleon", "Circinus", "Columba", "Coma Berenices", "Corona Australis", "Corona Borealis", "Corvus", "Crater", "Crux", "Cygnus", "Delphinus", "Dorado", "Draco", "Equuleus", "Eridanus", "Fornax", "Gemini", "Grus", "Hercules", "Horologium", "Hydra", "Hydrus", "Indus", "Lacerta", "Leo", "Leo Minor", "Lepus", "Libra", "Lupus", "Lynx", "Lyra", "Mensa", "Microscopium", "Monoceros", "Musca", "Norma", "Octans", "Ophiuchus", "Orion", "Pavo", "Pegasus", "Perseus", "Phoenix", "Pictor", "Pisces", "Piscis Austrinus", "Puppis", "Pyxis", "Reticulum", "Sagitta", "Sagittarius", "Scorpius", "Sculptor", "Scutum", "Serpens", "Sextans", "Taurus", "Telescopium", "Triangulum", "Triangulum Australe", "Tucana", "Ursa Major", "Ursa Minor", "Vela", "Virgo", "Volans", "Vulpecula"};

    public static final String[] greekNames = new String[]{"alpha", "alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta", "iota", "kappa", "lambda", "mu", "nu", "xi", "omicron", "pi", "rho", "sigma", "tau", "upsilon", "phi", "chi", "psi", "omega"};
    public static final String DEFAULT_PREFS = Constants.PREF_LIST_NAME_BASE + "shp1";
    public static final String MY_PREFS = Constants.PREF_LIST_NAME_BASE + "shp2";


}

