package com.astro.dsoplanner.expansion;


import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.database.Db;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.DbManager;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListCollectionFiller;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.Init;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.NgcPicListItem;
import com.astro.dsoplanner.database.NoteDatabase;


import com.astro.dsoplanner.Prefs;
import com.astro.dsoplanner.SearchRequestItem;
import com.astro.dsoplanner.SettingsActivity;

public class APKExpansion {
    private static final String BRIGHTMP_DB = "brightmp.db";
    private static final String COMMIT = "commit";
    private static final String BEGIN = "begin";
    private static final String INSERT_INTO_MAIN_CUSTOMDBB_ID_G_H_M_AXIS_DAY_E_I_MONTH_NODE_W_YEAR_NAME1_NAME2_TYPE_VALUES = "insert into main.customdbb (_id,g,h,m,axis,day,e,i,month,node,w,year,name1,name2,type) values (";
    private static final String SELECT_Q2_ID_Q2_G_Q2_H_Q2_M_Q2_AXIS_Q2_DAY_Q2_E_Q2_I_Q2_MONTH_Q2_NODE_Q2_W_Q2_YEAR_Q2_NAME1_Q2_NAME2_Q2_TYPE_FROM_MP_CUSTOMDBB_Q2_WHERE_Q2_ID_10000 = "select q2._id,q2.g,q2.h,q2.m,q2.axis,q2.day,q2.e,q2.i,q2.month,q2.node,q2.w,q2.year,q2.name1,q2.name2,q2.type from mp.customdbb q2 where q2._id>10000";
    private static final String AS_MP = "' as mp";
    private static final String ATTACH_DATABASE = "attach database '";
    private static final String MP_DB = "mp.db";
    private static final String ALL_CALDWELL = "All Caldwell";
    private static final String ALL_MESSIER = "All Messier";
    private static final String OLDDBS = "olddbs";
    private static final String ASTRO = "astro";
    private static final String REF = "ref";
    private static final String PRAGMA_TABLE_INFO_CUSTOMDBB = "pragma table_info('customdbb')";
    private static final String NGCIC_DB = "ngcic.db";
    private static final String NSOG_DB = "nsog.db";
    private static final String SH2 = "SH2";
    private static final String STRUCT = "struct";
    private static final String FORM = "form";
    private static final String BRIGHTNESS2 = "brightness";
    private static final String SH2_DB = "sh2.db";
    private static final String MISC = "Misc";
    private static final String MISC_DB = "misc.db";
    private static final String SG_NOTES = "SGNotes";
    private static final String SNOTES_DB = "snotes.db";
    private static final String PGC = "PGC";
    private static final String PGC_DB = "pgc.db";
    private static final String MESSIER = "Messier";
    private static final String M_DB = "m.db";
    private static final String CALDWELL = "Caldwell";
    private static final String C_DB = "c.db";
    private static final String PK = "PK";
    private static final String PK_DB = "pk.db";
    private static final String BRIGHT_DS = "Bright DS";
    private static final String HAAS_DB = "haas.db";
    private static final String HCG = "HCG";
    private static final String COUNT = "count";
    private static final String HCG_DB = "hcg.db";
    private static final String ABELL = "Abell";
    private static final String RICHNESS = "richness";
    private static final String ABELL_DB = "abell.db";
    private static final String SAC = "SAC";
    private static final String NOTES = "NOTES";
    private static final String DESCR = "DESCR";
    private static final String BCHM = "BCHM";
    private static final String BRSTR = "BRSTR";
    private static final String NSTS = "NSTS";
    private static final String CLASS = "CLASS";
    private static final String TI = "TI";
    private static final String U2K = "U2K";
    private static final String SUBR = "SUBR";
    private static final String SACTYPE = "SACTYPE";
    private static final String OTHERNAME = "OTHERNAME";
    private static final String SAC_DB = "sac.db";
    private static final String TN_DB = "tn.db";
    private static final String TQL_DB = "tql.db";
    private static final String TQ_DB = "tq.db";
    private static final String LBN = "LBN";
    private static final String LDN = "LDN";
    private static final String BARNARD = "Barnard";
    private static final String UGCQL_DB = "ugcql.db";
    private static final String UGCQ_DB = "ugcq.db";
    private static final String TYCHOQLS_DB = "tychoqls.db";
    private static final String TYCHOQS_DB = "tychoqs.db";
    private static final String NGCQL_DB = "ngcql.db";
    private static final String NGCQ_DB = "ngcq.db";
    private static final String UGC = "UGC";
    private static final String BN_LYNDS = "BN(Lynds)";
    private static final String BRIGHTNESS = BRIGHTNESS2;
    private static final String LBN_DB = "lbn.db";
    private static final String LDN_DB = "ldn.db";
    private static final String DN_LYNDS = "DN(Lynds)";
    private static final String DN_BARNARD = "DN(Barnard)";
    private static final String OPACITY = "opacity";
    private static final String BARNARD_DB = "barnard.db";
    private static final String WDS = "WDS";
    private static final String SEPARATION = "separation";
    private static final String SPECTRUM = "spectrum";
    private static final String MAG2 = "mag2";
    private static final String COMPONENTS = "components";
    private static final String YEAR = "year";
    private static final String WDS_DB = "wds.db";
    private static final String UGC_DB = "ugc.db";
    private static final String UCACQL_DB = "ucacql.db";
    private static final String PGCQL_DB = "pgcql.db";
    private static final String PGCQ_DB = "pgcq.db";
    private static final String TYCHOQL_DB = "tychoql.db";
    private static final String TYCHOQ_DB = "tychoq.db";
    private static final String UCAC4QL_DB = "ucac4ql.db";
    private static final String UCAC4Q_DB = "ucac4q.db";
    private static final String OBB = ".obb";
    private static final String MAIN = "main.";
    private static final String PATCH = "patch.";
    public static final String EXP_PATH = "/Android/obb/";

    private static final String TAG = APKExpansion.class.getSimpleName();

    public static String getMainExpPath(Context context, int mainVersion) {
        String packageName = context.getPackageName();
        File root = Environment.getExternalStorageDirectory();
        File expPath = context.getObbDir();
        String name = MAIN + mainVersion + "." + packageName + OBB;
        String fullPath = expPath.getAbsolutePath() + File.separator + name;
        return fullPath;
    }

    public static String getExpPath(Context context, int mainVersion) {
        String alt_path = SettingsActivity.getAlternativeExpansionFileFolder(context);
        Log.d(TAG, "alt_path=" + alt_path);
        if ("".equals(alt_path) || alt_path == null)
            return getMainExpPath(context, mainVersion);
        else {
            String packageName = context.getPackageName();
            File expPath = new File(alt_path);
            String name = MAIN + mainVersion + "." + packageName + OBB;
            String fullPath = expPath.getAbsolutePath() + File.separator + name;
            return fullPath;
        }

    }

    public static String getMainExpPatchPath(Context context, int mainVersion) {
        String packageName = context.getPackageName();
        File root = Environment.getExternalStorageDirectory();
        File expPath = context.getObbDir();
        String name = PATCH + mainVersion + "." + packageName + OBB;
        String fullPath = expPath.getAbsolutePath() + File.separator + name;
        return fullPath;
    }

    public static String getExpPatchPath(Context context, int mainVersion) {
        String alt_path = SettingsActivity.getAlternativeExpansionFileFolder(context);
        if ("".equals(alt_path) || alt_path == null)
            return getMainExpPatchPath(context, mainVersion);
        else {

            String packageName = context.getPackageName();
            File expPath = new File(alt_path);
            String name = PATCH + mainVersion + "." + packageName + OBB;
            String fullPath = expPath.getAbsolutePath() + File.separator + name;
            return fullPath;
        }
    }

    public static void expandDatabasesPack(Context context, int mainVersion) throws IOException {
        String file = getExpPath(context, mainVersion);
        Log.d(TAG, "file=" + file);
        Log.d(TAG, "start pack");
        RandomAccessFile din = new RandomAccessFile(file, "r");
        InfoList ilist = ListHolder.getListHolder().get(InfoList.NGC_PIC_LIST);
        ilist.removeAll();
        int i = 0;


        Log.d(TAG, "i=" + i++);
        try {
            while (true) {
                String name = din.readUTF();
                long size = din.readLong();
                Log.d(TAG, "name=" + name);
                String dir;
                if (!Global.BASIC_VERSION) {//pro and plus
                    if (UCAC4Q_DB.equals(name)) {
                        dir = null;//no need to copy file
                        long start = din.getFilePointer();//start pos of ucac4q in exp pack
                        SettingsActivity.putSharedPreferences(Constants.UCAC4_START_POS, start, context);
                    } else if (UCAC4QL_DB.equals(name)) {
                        dir = null;//no need to copy file
                        long start = din.getFilePointer();//start pos of ucac4ql in exp pack
                        SettingsActivity.putSharedPreferences(Constants.UCAC4QL_START_POS, start, context);
                        SettingsActivity.putSharedPreferences(Constants.UCAC4QL_SIZE, size, context);
                    } else if (TYCHOQ_DB.equals(name) || TYCHOQL_DB.equals(name) //||PGCQ_DB.equals(name)||PGCQL_DB.equals(name)
                            || UCACQL_DB.equals(name))
                        dir = Global.databasesPath + File.separator;

                    else if (WDS_DB.equals(name)) {
                        DbListItem.FieldTypes ftypes = new DbListItem.FieldTypes();
                        ftypes.put(YEAR, DbListItem.FieldTypes.TYPE.INT);
                        ftypes.put(COMPONENTS, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(MAG2, DbListItem.FieldTypes.TYPE.DOUBLE);
                        ftypes.put(SPECTRUM, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(SEPARATION, DbListItem.FieldTypes.TYPE.DOUBLE);

                        String internalName = DbManager.addNewInternalDatabase(ftypes, context, WDS, false, null, AstroCatalog.WDS);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (name.length() > 9 && !BARNARD_DB.equals(name)) {
                        dir = Global.DSSpath + File.separator;
                        Log.d(TAG, "dss name=" + name);
                    } else if (name.contains("jpg")) { //to skip db files in expansion pack that are not needed
                        dir = null;
                        Log.d(TAG, "other name=" + name);
                        NgcPicListItem item = new NgcPicListItem(name, din.getFilePointer(), size);
                        InfoListFiller filler = new InfoListCollectionFiller(Arrays.asList(new NgcPicListItem[]{item}));
                        ilist.fill(filler);

                    } else {
                        dir = null; //just skip and move to next
                    }
                } else if (Global.BASIC_VERSION) {
                    if (TYCHOQ_DB.equals(name) || TYCHOQL_DB.equals(name))
                        dir = Global.databasesPath + File.separator;
                    else if (name.length() > 9)
                        dir = Global.DSSpath + File.separator;
                    else {
                        dir = null;
                        NgcPicListItem item = new NgcPicListItem(name, din.getFilePointer(), size);
                        InfoListFiller filler = new InfoListCollectionFiller(Arrays.asList(new NgcPicListItem[]{item}));
                        ilist.fill(filler);
                    }
                }


                FileIn fin = new FileIn(din, name, size, dir);
                fin.read();
            }
        } catch (EOFException e) {
            Log.d(TAG, "exception=" + e);

        } finally {
            try {
                din.close();
            } catch (Exception e) {
            }
        }

        Log.d(TAG, "ilist size=" + ilist.getCount());
        sortList(ilist);
        new Prefs(context).saveList(InfoList.NGC_PIC_LIST);
        Log.d(TAG, "end pack");

    }

    public static void expandDatabasesPatch(Context context, int mainVersion) throws IOException {
        Log.d(TAG, "start patch");
        try {
            copyDbsIfRequired(context);
        } catch (Exception e1) {
            Log.d(TAG, "e1=" + e1);
        }


        updateNoteDatabase(context);

        File f = new File(Global.databasesPath, PGCQ_DB);
        if (f.exists())//removing pgcq file from the previous version if available as now data is read right from the exp patch
            f.delete();


        String file = getExpPatchPath(context, mainVersion);
        Log.d(TAG, "file=" + file);
        RandomAccessFile din = new RandomAccessFile(file, "r");

        int i = 0;


        Log.d(TAG, "i=" + i++);
        try {
            while (true) {
                String name = din.readUTF();
                long size = din.readLong();

                String dir = null;
                if (Global.BASIC_VERSION) {
                    if (TQ_DB.equals(name) || TQL_DB.equals(name) || TN_DB.equals(name) ||
                            TYCHOQS_DB.equals(name) || TYCHOQLS_DB.equals(name) || Init.BQ_DB.equals(name) || Init.BQL_DB.equals(name) || Init.MWQ_DB.equals(name) || Init.MWQL_DB.equals(name)) {
                        dir = Global.databasesPath + File.separator;
                    } else if (WDS_DB.equals(name)) {

                        DbListItem.FieldTypes ftypes = new DbListItem.FieldTypes();
                        ftypes.put(YEAR, DbListItem.FieldTypes.TYPE.INT);
                        ftypes.put(COMPONENTS, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(MAG2, DbListItem.FieldTypes.TYPE.DOUBLE);
                        ftypes.put(SPECTRUM, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(SEPARATION, DbListItem.FieldTypes.TYPE.DOUBLE);

                        String internalName = DbManager.addNewInternalDatabase(ftypes, context, WDS, false, null, AstroCatalog.WDS);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (C_DB.equals(name)) {
                        String internalName = DbManager.addNewInternalDatabase(new DbListItem.FieldTypes(), context, CALDWELL, false, null, AstroCatalog.CALDWELL);
                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (M_DB.equals(name)) {
                        String internalName = DbManager.addNewInternalDatabase(new DbListItem.FieldTypes(), context, MESSIER, false, null, AstroCatalog.MESSIER);
                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (SNOTES_DB.equals(name)) {
                        String internalName = DbManager.addNewInternalDatabase(new DbListItem.FieldTypes(), context, SG_NOTES, false, null, AstroCatalog.SNOTES);
                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (Constants.SQL_DATABASE_CROSS_DB.equals(name) || Constants.EP_DB.equals(name) || Constants.LOCATIONS_DB.equals(name) || Constants.SQL_DATABASE_COMP_DB.equals(name) || Constants.SQL_DATABASE_OREL_DB.equals(name) || Constants.SQL_DATABASE_HRCROSS_DB.equals(name) || Constants.SQL_DATABASE_SAO_TYC_DB.equals(name)) {
                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                    } else
                        dir = null;
                } else {//pro and plus
                    if (TQ_DB.equals(name) || TQL_DB.equals(name) || TN_DB.equals(name) || TYCHOQS_DB.equals(name) || TYCHOQLS_DB.equals(name)
                            || UGCQ_DB.equals(name) || UGCQL_DB.equals(name) || Init.BQ_DB.equals(name) || Init.BQL_DB.equals(name) || Init.MWQ_DB.equals(name) || Init.MWQL_DB.equals(name))
                        dir = Global.databasesPath + File.separator;
                    else if (SAC_DB.equals(name)) {
                        DbListItem.FieldTypes ftypes = new DbListItem.FieldTypes();
                        //other fields  othername sactype subr u2k ti class nsts brstr bchm ngc_descr notes

                        ftypes.put(OTHERNAME, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(SACTYPE, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(SUBR, DbListItem.FieldTypes.TYPE.DOUBLE);
                        ftypes.put(U2K, DbListItem.FieldTypes.TYPE.INT);
                        ftypes.put(TI, DbListItem.FieldTypes.TYPE.INT);
                        ftypes.put(CLASS, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(NSTS, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(BRSTR, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(BCHM, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(DESCR, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(NOTES, DbListItem.FieldTypes.TYPE.STRING);

                        String internalName = DbManager.addNewInternalDatabase(ftypes, context, SAC, false, null, AstroCatalog.SAC);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);

                    } else if (UGC_DB.equals(name)) {
                        String internalName = DbManager.addNewInternalDatabase(new DbListItem.FieldTypes(), context, UGC, false, null, AstroCatalog.UGC);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (BARNARD_DB.equals(name)) {
                        DbListItem.FieldTypes ftypes = new DbListItem.FieldTypes();
                        ftypes.put(OPACITY, DbListItem.FieldTypes.TYPE.INT);

                        String internalName = DbManager.addNewInternalDatabase(ftypes, context, BARNARD, false, null, AstroCatalog.DNBARNARD);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (LDN_DB.equals(name)) {
                        DbListItem.FieldTypes ftypes = new DbListItem.FieldTypes();
                        ftypes.put(OPACITY, DbListItem.FieldTypes.TYPE.INT);

                        String internalName = DbManager.addNewInternalDatabase(ftypes, context, LDN, false, null, AstroCatalog.DNLYNDS);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (LBN_DB.equals(name)) {
                        DbListItem.FieldTypes ftypes = new DbListItem.FieldTypes();
                        ftypes.put(BRIGHTNESS, DbListItem.FieldTypes.TYPE.INT);

                        String internalName = DbManager.addNewInternalDatabase(ftypes, context, LBN, false, null, AstroCatalog.BNLYNDS);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (ABELL_DB.equals(name)) {
                        DbListItem.FieldTypes ftypes = new DbListItem.FieldTypes();
                        ftypes.put(RICHNESS, DbListItem.FieldTypes.TYPE.INT);

                        String internalName = DbManager.addNewInternalDatabase(ftypes, context, ABELL, false, null, AstroCatalog.ABELL);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (HCG_DB.equals(name)) {
                        DbListItem.FieldTypes ftypes = new DbListItem.FieldTypes();
                        ftypes.put(COUNT, DbListItem.FieldTypes.TYPE.INT);

                        String internalName = DbManager.addNewInternalDatabase(ftypes, context, HCG, false, null, AstroCatalog.HCG);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (HAAS_DB.equals(name)) {
                        DbListItem.FieldTypes ftypes = new DbListItem.FieldTypes();
                        ftypes.put(COMPONENTS, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(MAG2, DbListItem.FieldTypes.TYPE.DOUBLE);
                        ftypes.put(SEPARATION, DbListItem.FieldTypes.TYPE.DOUBLE);

                        String internalName = DbManager.addNewInternalDatabase(ftypes, context, BRIGHT_DS, false, null, AstroCatalog.HAAS);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (WDS_DB.equals(name)) {
                        DbListItem.FieldTypes ftypes = new DbListItem.FieldTypes();
                        ftypes.put(YEAR, DbListItem.FieldTypes.TYPE.INT);
                        ftypes.put(COMPONENTS, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(MAG2, DbListItem.FieldTypes.TYPE.DOUBLE);
                        ftypes.put(SPECTRUM, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(SEPARATION, DbListItem.FieldTypes.TYPE.DOUBLE);

                        String internalName = DbManager.addNewInternalDatabase(ftypes, context, WDS, false, null, AstroCatalog.WDS);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (PK_DB.equals(name)) {
                        String internalName = DbManager.addNewInternalDatabase(new DbListItem.FieldTypes(), context, PK, false, null, AstroCatalog.PK);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (C_DB.equals(name)) {
                        String internalName = DbManager.addNewInternalDatabase(new DbListItem.FieldTypes(), context, CALDWELL, false, null, AstroCatalog.CALDWELL);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (M_DB.equals(name)) {
                        String internalName = DbManager.addNewInternalDatabase(new DbListItem.FieldTypes(), context, MESSIER, false, null, AstroCatalog.MESSIER);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (PGC_DB.equals(name)) {
                        String internalName = DbManager.addNewInternalDatabase(new DbListItem.FieldTypes(), context, PGC, false, null, AstroCatalog.PGC);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (SNOTES_DB.equals(name)) {
                        String internalName = DbManager.addNewInternalDatabase(new DbListItem.FieldTypes(), context, SG_NOTES, false, null, AstroCatalog.SNOTES);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (MISC_DB.equals(name)) {
                        String internalName = DbManager.addNewInternalDatabase(new DbListItem.FieldTypes(), context, MISC, false, null, AstroCatalog.MISC);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (SH2_DB.equals(name)) {
                        DbListItem.FieldTypes ftypes = new DbListItem.FieldTypes();
                        //brightness integer, form text, struct text
                        ftypes.put(BRIGHTNESS2, DbListItem.FieldTypes.TYPE.INT);
                        ftypes.put(FORM, DbListItem.FieldTypes.TYPE.STRING);
                        ftypes.put(STRUCT, DbListItem.FieldTypes.TYPE.STRING);
                        String internalName = DbManager.addNewInternalDatabase(ftypes, context, SH2, false, null, AstroCatalog.SH2);

                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();
                        if (internalName != null) name = internalName;
                        Log.d(TAG, "internal name=" + internalName + "dir=" + dir);
                    } else if (Constants.SQL_DATABASE_CROSS_DB.equals(name) || Constants.EP_DB.equals(name) || Constants.LOCATIONS_DB.equals(name) || Constants.SQL_DATABASE_COMP_DB.equals(name) || Constants.SQL_DATABASE_OREL_DB.equals(name) || Constants.SQL_DATABASE_HRCROSS_DB.equals(name) || Constants.SQL_DATABASE_SAO_TYC_DB.equals(name)) {
                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();

                    } else if (NSOG_DB.equals(name)) {
                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();

                    } else if (NGCIC_DB.equals(name)) {
                        File db = context.getDatabasePath(name);
                        db.getParentFile().mkdirs();
                        dir = db.getParent();

                    } else if (PGCQ_DB.equals(name)) {
                        dir = null;//no need to copy file
                        long start = din.getFilePointer();//start pos of ucac4q in exp pack
                        SettingsActivity.putSharedPreferences(Constants.PGC_START_POS_PATCH, start, context);

                    } else if (PGCQL_DB.equals(name))
                        dir = Global.databasesPath + File.separator;
                    else
                        dir = null;
                }

                FileIn fin = new FileIn(din, name, size, dir);
                fin.read();
            }
        } catch (EOFException e) {
            Log.d(TAG, "exception=" + e);
        } finally {
            try {
                din.close();
            } catch (Exception e) {
            }
        }
        Log.d(TAG, "end patch");


    }

    public static void sortList(InfoList list) {
        Comparator cmp = new Comparator() {
            @Override
            public int compare(Object lhs, Object rhs) {
                NgcPicListItem litem = (NgcPicListItem) lhs;
                NgcPicListItem ritem = (NgcPicListItem) rhs;
                int lval = litem.getValue();
                int rval = ritem.getValue();
                if (lval < rval)
                    return -1;
                else if (lval > rval)
                    return 1;
                else
                    return 0;

            }
        };
        list.sort(cmp);
    }

    /**
     * copying old dbs if changed
     *
     * @param context
     */
    public static void copyDbsIfRequired(Context context) {
        boolean copied_already = SettingsActivity.getSharedPreferences(context).getBoolean(Constants.DBS_COPIED, false);
        if (copied_already)
            return;

        moveInternal(context, AstroCatalog.DNBARNARD, 73728);
        moveInternal(context, AstroCatalog.DNLYNDS, 143360);
        moveInternal(context, AstroCatalog.BNLYNDS, 81920);
        moveInternal(context, AstroCatalog.UGC, 1003520);

        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);

        Set<DbListItem> set = new HashSet<DbListItem>();
        for (Object o : iL) {
            DbListItem item = (DbListItem) o;
            if (item.cat >= AstroCatalog.SAC && item.cat < AstroCatalog.NEW_CATALOG_FIRST) { //for old db copy it
                set.add(item);
            }
        }
        for (DbListItem item : set) {
            move(context, item);
        }
        trimSqlQueries(context);

        SettingsActivity.putSharedPreferences(Constants.DBS_COPIED, true, context);
    }

    private static void trimSqlQueries(Context context) {
        InfoList list = ListHolder.getListHolder().get(InfoList.SREQUEST_LIST);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            SearchRequestItem item = (SearchRequestItem) it.next();
            if (item.name.equals(ALL_MESSIER) || item.name.equals(ALL_CALDWELL)) {
                it.remove();
            }
        }

        class Filler implements InfoListFiller {
            private Collection<SearchRequestItem> collection;

            public Filler(SearchRequestItem item) {
                collection = Arrays.asList(new SearchRequestItem[]{item});
            }

            public Iterator getIterator() {
                return collection.iterator();
            }
        }

        new Prefs(context).saveList(InfoList.SREQUEST_LIST);
    }

    private static boolean isNew(Context context, String name) {
        Db db = new Db(context, name);
        try {
            db.open();
            Cursor cursor = db.rawQuery(PRAGMA_TABLE_INFO_CUSTOMDBB);
            int count = cursor.getColumnCount();
            while (cursor.moveToNext()) {
                for (int i = 0; i < count; i++) {
                    String field = cursor.getString(i);
                    if (field != null && field.equals(REF)) {
                        Log.d(TAG, "contains ref");
                        return true;
                    }
                }
            }

        } catch (Exception e) {
        } finally {
            try {
                db.close();
            } catch (Exception e) {
            }
        }
        Log.d(TAG, "does not contain ref");
        return false;
    }

    /**
     * @param context
     * @param catalog
     * @param size    not used in fact
     */
    private static void moveInternal(Context context, int catalog, int size) {
        String name = ASTRO + catalog + ".db";

        File f = context.getDatabasePath(name);
        Log.d(TAG, "f=" + f + " size=" + f.length());
        if (f.length() != 0) {
            DbListItem item = DbManager.getDbListItem(catalog);
            if (item != null)
                move(context, item);
        }
    }

    /**
     * checks whether the db is new and whether the db with the same name exists.
     * if true no action performed
     *
     * @param context
     * @param item
     */
    private static void move(Context context, DbListItem item) {
        Log.d(TAG, "move, item=" + item);

        if (isNew(context, item.dbFileName) || (item.cat == 64 && item.dbName.toUpperCase(Locale.US).equals(PGC)) || (item.cat == 69 && item.dbName.toUpperCase(Locale.US).equals("SGNOTES")))
            return;//PGC and SGNOTES does not contain ref

        String dirto = Global.databasesPath + OLDDBS + File.separator;
        new File(dirto).mkdirs();

        String dir = context.getDatabasePath(item.dbFileName).getParentFile().getAbsolutePath();
        Log.d(TAG, "dir=" + dir);
        new AstroTools.FileToCopy(dir, item.dbFileName, dirto, item.dbFileName).copy();

    }

    public static void updateNoteDatabase(Context context) {
        boolean updated = SettingsActivity.getSharedPreferences(context).getBoolean(Constants.NOTEMAIN_UPDATED, false);
        if (updated)
            return;
        File f = context.getDatabasePath(Constants.NOTE_DATABASE_NAME);
        if (!f.exists())
            return;
        NoteDatabase db = new NoteDatabase(context);
        ErrorHandler eh = new ErrorHandler();
        List<String> list = new ArrayList<String>();
        try {
            db.open(eh);
            Cursor cursor = db.db.rawQuery("select _id,name from notemain where name like 'ngc%' or name like 'ic%'", null);
            while (cursor.moveToNext()) {
                String name = cursor.getString(1).trim();
                Pattern p = Pattern.compile("NGC[0-9]+");
                Matcher m = p.matcher(name);
                if (m.find()) {
                    String name2 = name.substring(m.start(), m.end());
                    if (!name.equals(name2)) {
                        list.add("update notemain set name='" + name2 + "' where _id=" + cursor.getString(0));
                    }
                } else {
                    p = Pattern.compile("IC[0-9]+");
                    m = p.matcher(name);
                    if (m.find()) {
                        String name2 = name.substring(m.start(), m.end());
                        if (!name.equals(name2)) {
                            list.add("update notemain set name='" + name2 + "' where _id=" + cursor.getString(0));
                        }
                    }
                }

            }
            cursor.close();
            for (int i = 0; i < Math.min(10, list.size()); i++) {
                Log.d(TAG, "sql=" + list.get(i));
            }
            db.db.execSQL(BEGIN);
            for (String s : list) {
                db.db.execSQL(s);
            }
            db.db.execSQL(COMMIT);
            Log.d(TAG, "end");
            SettingsActivity.putSharedPreferences(Constants.NOTEMAIN_UPDATED, true, context);
        } finally {
            try {
                db.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * when updating we take internal data from the file mp.db and put the user data with id>10000 over it
     *
     * @param context
     * @return
     */
    public static boolean updateMinorPlanets(Context context) {
        Db db = new Db(context, BRIGHTMP_DB);
        try {
            db.open();
            Log.d(TAG, "start");
            String path = context.getDatabasePath(MP_DB).getAbsolutePath();
            db.execSQL(ATTACH_DATABASE + path + AS_MP);

            Cursor cursor = db.rawQuery(SELECT_Q2_ID_Q2_G_Q2_H_Q2_M_Q2_AXIS_Q2_DAY_Q2_E_Q2_I_Q2_MONTH_Q2_NODE_Q2_W_Q2_YEAR_Q2_NAME1_Q2_NAME2_Q2_TYPE_FROM_MP_CUSTOMDBB_Q2_WHERE_Q2_ID_10000);

            List<String> list = new ArrayList<String>();
            while (cursor.moveToNext()) {
                String s = INSERT_INTO_MAIN_CUSTOMDBB_ID_G_H_M_AXIS_DAY_E_I_MONTH_NODE_W_YEAR_NAME1_NAME2_TYPE_VALUES + cursor.getString(0) + "," + cursor.getString(1) + "," + cursor.getString(2) + "," + cursor.getString(3) + "," + cursor.getString(4) + "," +
                        cursor.getString(5) + "," + cursor.getString(6) + "," + cursor.getString(7) + "," + cursor.getString(8) + "," + cursor.getString(9) + "," + cursor.getString(10) + "," + cursor.getString(11) + ",'" + cursor.getString(12).replace("'", "''") + "','" + cursor.getString(13).replace("'", "''") + "'," + cursor.getString(14) + ")";

                list.add(s);
            }
            for (int i = 0; i < Math.min(10, list.size()); i++) {
                Log.d(TAG, "sql=" + list.get(i));
            }
            db.execSQL(BEGIN);
            for (String s : list) {
                db.execSQL(s);
            }
            db.execSQL(COMMIT);
            Log.d(TAG, "end");
        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
            return false;
        } finally {
            try {
                db.close();
            } catch (Exception e) {
            }
        }
        return true;
    }
}
