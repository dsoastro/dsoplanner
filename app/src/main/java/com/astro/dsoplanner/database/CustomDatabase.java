package com.astro.dsoplanner.database;

import static android.provider.BaseColumns._ID;
import static com.astro.dsoplanner.Constants.*;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.astro.dsoplanner.Analisator;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.base.CustomObject;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.ImportDatabaseIntentService;


import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.QueryActivity;
import com.astro.dsoplanner.database.SQLInstructions.ProcessCursor;
import com.astro.dsoplanner.SearchRules;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.ListHolder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class CustomDatabase implements AstroCatalog {


    private static final String AND_REF_IS_NULL = ") and ref is null;";
    private static final String AND_REF_IS_NOT_NULL_GROUP_BY_REF_UNION_SELECT_FROM_CUSTOMDBB_WHERE = ") and ref is not null group by ref union select * from customdbb where (";
    private static final String SELECT_FROM_CUSTOMDBB_WHERE = "select * from customdbb where (";
    private static final String AND2 = " AND ";
    private static final String AND = " AND (";
    public static final String OR = " OR ";
    public static final String LIKE = " LIKE ";
    private static final String ASC = " ASC";
    private static final String ERROR_ADDING_AN_OBJECT = "Error adding an object";
    private static final String ERROR_OPENING_DATABASE = "Error opening database";
    private static final String CUSTOM_DATABASE = "CustomDatabase";
    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";
    private static final String TEXT2 = " TEXT);";
    private static final String TEXT = " TEXT, ";
    private static final String INTEGER = " INTEGER, ";
    private static final String FLOAT = " FLOAT, ";
    private static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    private static final String CREATE_TABLE = "CREATE TABLE ";
    private static final String _0 = ">0";


    private static final String TAG = CustomDatabase.class.getSimpleName();
    protected final static int ID_POS = 0;

    protected final static int RA_POS = 2;
    protected final static int DEC_POS = 1;
    protected final static int MAG_POS = 4;
    protected final static int A_POS = 3;
    protected final static int B_POS = 5;
    protected final static int CON_POS = 6;
    protected final static int TYPE_POS = 7;
    protected final static int PA_POS = 8;
    protected final static int TYPESTR_POS = 9;
    protected final static int NAME1_POS = 10;
    protected final static int NAME2_POS = 11;
    protected final static int COMMENT_POS = 12;
    protected static final int REF_POS = 13;


    public final static String TYPESTR = "typestr";
    public final static String NAME1 = "name1";
    public final static String NAME2 = "name2";
    protected final static String TABLE_NAME = "customdbb";


    protected int catalog;  //AstroCatalog
    int a = 3;

    protected class CustomData extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 2;

        public CustomData(Context ctx, String name) {
            super(ctx, name, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String s = "";
            s = CREATE_TABLE + TABLE_NAME + " (" + _ID
                    + INTEGER_PRIMARY_KEY_AUTOINCREMENT + DEC + FLOAT + RA + FLOAT
                    + A + FLOAT
                    + MAG + FLOAT + B + FLOAT + CONSTEL + INTEGER +
                    TYPE + INTEGER + PA + FLOAT + TYPESTR + TEXT + NAME1 + TEXT + NAME2 + TEXT + COMMENT + TEXT2;

            db.execSQL(s);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            db.execSQL(DROP_TABLE_IF_EXISTS + TABLE_NAME);
            onCreate(db);
        }
    }

    protected CustomData custom;
    protected SQLiteDatabase db;
    protected Context context;

    public String toString() {
        return CUSTOM_DATABASE;
    }

    private String dbname;
    public boolean internalDeepSky = false;

    public CustomDatabase(Context context, String dbname, int catalog) {//catalog needed to make CustomObjects correctly as they have a catalog field, besides customs objects may belong to different catalogs
        custom = new CustomData(context, dbname);
        this.context = context;
        this.catalog = catalog;
        this.dbname = dbname;
        if (Constants.NGCIC_DATABASE_NAME.equals(dbname))//insurance that ngcic is not corrupted by mistake
            throw new UnsupportedOperationException();
        internalDeepSky = SearchRules.isInternalDeepSky(catalog) && catalog != AstroCatalog.PGC;
    }

    public void open(ErrorHandler eh) {
        if (ImportDatabaseIntentService.isBeingImported(dbname)) {
            eh.addError(new ErrorHandler.ErrorRec(ErrorHandler.WARNING, Global.DB_IMPORT_RUNNING));
            return;
        }
        try {
            db = custom.getReadableDatabase();
        } catch (Exception e) {
            Log.d(TAG, "exception=" + AstroTools.getStackTrace(e));
            ErrorHandler.ErrorRec rec = new ErrorHandler.ErrorRec(ErrorHandler.SQL_DB, ERROR_OPENING_DATABASE);
            eh.addError(rec);
        }

    }

    public void close() {
        try {
            custom.close();
        } catch (Exception e) {
        }
    }

    public boolean isOpen() {
        return db.isOpen();
    }

    public AstroObject getObjectFromCursor(Cursor cursor) { //should be called after cursor.moveToNext called
        int id = cursor.getInt(ID_POS);

        double ra = cursor.getFloat(RA_POS);
        double dec = cursor.getFloat(DEC_POS);


        double a = Double.NaN;
        try {
            String astr = cursor.getString(A_POS);
            if (astr != null) {
                a = cursor.getFloat(A_POS);
            }
        } catch (Exception e) {
        }

        double b = Double.NaN;
        try {
            String bstr = cursor.getString(B_POS);
            if (bstr != null) {
                b = cursor.getFloat(B_POS);
            }
        } catch (Exception e) {
        }


        double mag = Double.NaN;
        try {
            String magstr = cursor.getString(MAG_POS);
            if (magstr != null) {
                mag = cursor.getFloat(MAG_POS);
            }
        } catch (Exception e) {
        }


        int con = cursor.getInt(CON_POS);
        int type = cursor.getInt(TYPE_POS);
        double pa = Double.NaN;
        try {
            String pastr = cursor.getString(PA_POS);
            if (pastr != null) {
                pa = cursor.getFloat(PA_POS);
            }
        } catch (Exception e) {
        }


        String typestr = cursor.getString(TYPESTR_POS);
        if (typestr == null) typestr = "";
        String name1 = cursor.getString(NAME1_POS);
        if (name1 == null) name1 = "";
        String name2 = cursor.getString(NAME2_POS);
        if (name2 == null) name2 = "";
        String comment = cursor.getString(COMMENT_POS);
        if (comment == null) comment = "";
        AstroObject obj = new CustomObject(catalog, id, ra, dec, con, type, typestr, a,
                b, mag, pa, name1, name2, comment);


        if (internalDeepSky) {
            int ref = 0;
            try {
                ref = cursor.getInt(REF_POS);

            } catch (Exception e) {

            }
            if (ref != 0) {
                obj.ref = ref;
            }
        }

        return obj;


    }

    public long add(AstroObject obj, ErrorHandler eh) {
        CustomObject ob;
        if (!(obj instanceof CustomObject))
            return -1;
        else
            ob = (CustomObject) obj;

        ContentValues values = new ContentValues();
        values.put(RA, obj.getRa());
        values.put(DEC, obj.getDec());

        double a = obj.getA();
        if (Double.isNaN(a)) {
            values.putNull(A);
        } else
            values.put(A, a);

        double b = obj.getB();
        if (Double.isNaN(b))
            values.putNull(B);
        else
            values.put(B, b);

        double mag = obj.getMag();
        if (Double.isNaN(mag))
            values.putNull(MAG);
        else
            values.put(MAG, mag);


        values.put(CONSTEL, obj.getCon());
        values.put(TYPE, obj.getType());

        double pa = ob.pa;
        if (Double.isNaN(pa))
            values.putNull(PA);
        else
            values.put(PA, pa);

        values.put(TYPESTR, ob.typeStr);
        values.put(NAME1, ob.getShortName());
        values.put(NAME2, ob.getLongName());
        values.put(COMMENT, ob.comment);
        long result = -1;
        boolean error = false;
        try {
            result = db.insertOrThrow(TABLE_NAME, null, values);
        } catch (Exception e) {
            error = true;

            Log.d(TAG, "exception=" + e);
        }
        if (result == -1)
            error = true;
        if (error) {
            ErrorHandler.ErrorRec rec = new ErrorHandler.ErrorRec(ErrorHandler.SQL_DB, ERROR_ADDING_AN_OBJECT);
            eh.addError(rec);
        }
        return result;
    }

    public void remove(int id) {

        db.delete(TABLE_NAME, _ID + "=" + id, null);

    }

    public void removeAll() {

        db.delete(TABLE_NAME, null, null);

    }

    public AstroObject getObject(int id) {
        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, _ID + "=" + id, null, null,
                    null, null);
        } catch (SQLiteException e) {
            return null;
        }
        AstroObject obj = null;
        if (cursor.moveToNext()) {
            obj = getObjectFromCursor(cursor);
        }
        cursor.close();
        return obj;
    }

    public Cursor getAll() {
        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null,
                    null, _ID + ASC);
        } catch (SQLiteException e) {
            return null;
        }

        return cursor;
    }

    public List<AstroObject> rawQuery(String s) {
        List<AstroObject> list = new ArrayList<AstroObject>();
        Cursor cursor;
        try {
            cursor = db.rawQuery(s, null);
        } catch (Exception e) {
            return list;
        }
        while (cursor.moveToNext()) {

            AstroObject obj = getObjectFromCursor(cursor);

            list.add(obj);
            if (list.size() > Global.SQL_SEARCH_LIMIT) {
                break;
            }

        }
        cursor.close();
        return list;
    }

    public List<AstroObject> search() {
        List<AstroObject> list = new ArrayList<AstroObject>();


        boolean double_star = (catalog == AstroCatalog.HAAS || catalog == AstroCatalog.WDS);
        String sqlReq = SearchRules.createQuery(context, false, false, double_star);//createQuery();

        Log.d(TAG, "search, sqlReq=" + sqlReq);
        Cursor cursor;
        boolean rd = SettingsActivity.isRemovingDuplicates() && internalDeepSky;

        try {
            if (!rd)
                cursor = db.query(TABLE_NAME, null, sqlReq, null, null, null, null);
            else {
                String sql = SELECT_FROM_CUSTOMDBB_WHERE + sqlReq + AND_REF_IS_NOT_NULL_GROUP_BY_REF_UNION_SELECT_FROM_CUSTOMDBB_WHERE + sqlReq + AND_REF_IS_NULL;
                Log.d(TAG, "sql=" + sql);
                cursor = db.rawQuery(sql, null);
            }

        } catch (SQLiteException e) {
            Log.d(TAG, "e=" + e);
            return list;
        }
        Log.d(TAG, "size=" + cursor.getCount());
        int filter = SettingsActivity.getFilter();
        Log.d(TAG, "filter=" + filter);
        boolean nearby = SettingsActivity.getNearbyMode();
        Point nearbyObject = SettingsActivity.getNearbyObject();
        double raObj = 0;
        double decObj = 0;
        double dist = SettingsActivity.getNearbyDistance();
        if (dist == 0)
            nearby = false;
        if (nearbyObject == null)
            nearby = false;
        else {
            raObj = nearbyObject.getRa();
            decObj = nearbyObject.getDec();
        }
        double minalt = SettingsActivity.getMinAlt();
        boolean stopflag = false;
        int i = 0;
        double DL = SettingsActivity.getDetectionLimit();

        int emptyRule = SettingsActivity.getEmptyRule();

        AstroTools.CheckAltContext checkContext = new AstroTools.CheckAltContext();

        while (cursor.moveToNext() && !QueryActivity.isStopping()) {
            // if...altitude and visibility
            if (i++ % 100 == 0) {
                Log.d(TAG, "i=" + i + " list size=" + list.size());
            }
            double ra = cursor.getFloat(RA_POS);
            double dec = cursor.getFloat(DEC_POS);

            double a = Double.NaN;
            try {
                String astr = cursor.getString(A_POS);
                if (astr != null) {
                    a = cursor.getFloat(A_POS);
                }
            } catch (Exception e) {
            }

            double b = Double.NaN;
            try {
                String bstr = cursor.getString(B_POS);
                if (bstr != null) {
                    b = cursor.getFloat(B_POS);
                }
            } catch (Exception e) {
            }

            double mag = Double.NaN;
            try {
                String magstr = cursor.getString(MAG_POS);
                if (magstr != null) {
                    mag = cursor.getFloat(MAG_POS);
                }
            } catch (Exception e) {
            }

            int type = cursor.getInt(TYPE_POS);
            boolean flagNearby = true;
            if (nearby) {
                if (cos(dist / 60 * PI / 180) >= (sin(dec * PI / 180) * sin(decObj * PI / 180) + cos(dec * PI / 180) * cos(decObj * PI / 180) * cos((ra - raObj) * PI / 12)))
                    flagNearby = false;
            }

            boolean flag = true;
            if ((flagNearby) && (AstroTools.CheckAlt(ra, dec, checkContext))) {
                //if above minimum altitude put the object into idList
                switch (filter) {
                    case 0:

                        flag = AstroTools.CheckVisibility(a, b, mag, DL, emptyRule, type);
                        Log.d(TAG, "a=" + a + " b=" + b + " mag=" + mag + " flag=" + flag);
                        break;
                }
                if (flag) {
                    AstroObject obj = getObjectFromCursor(cursor);


                    list.add(obj);
                    if (list.size() > Global.SQL_SEARCH_LIMIT) {
                        break;
                    }
                }
            }
        }

        cursor.close();
        return list;
    }

    public List<AstroObject> search(String s) {
        List<AstroObject> list = new ArrayList<AstroObject>();


        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, s, null, null,
                    null, _ID + ASC);
        } catch (SQLiteException e) {
            Log.d(TAG, "exception=" + e);
            return list;
        }

        while (cursor.moveToNext()) {
            AstroObject obj = getObjectFromCursor(cursor);
            list.add(obj);
            if (list.size() > Global.SQL_SEARCH_LIMIT) {
                break;
            }

        }
        cursor.close();
        return list;
    }


    public List<AstroObject> searchName(String s) {
        List<AstroObject> list = new ArrayList<AstroObject>();
        s = s.replace("'", "''");

        String query;
        if (SearchRules.isInternalCatalog(catalog)) {
            switch (catalog) {
                case AstroCatalog.MESSIER:
                case AstroCatalog.CALDWELL:
                    query = NAME1 + LIKE + "'" + s + "' or " + NAME2 + LIKE + "'" + s + "'";
                    break;
                case AstroCatalog.HAAS:
                    query = NAME1 + "='" + s.toUpperCase(Locale.US) + "' or " + NAME2 + "='" + s.toUpperCase(Locale.US) + "'";
                    break;
                case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG://data may be in lower case!!!
                case AstroCatalog.COMET_CATALOG:////data may be in lower case!!!
                    if (s.length() >= Global.COMMON_NAME_MIN_LENGTH_FOR_EXT_SEARCH) {//if long word look for part of the word else use default search
                        query = NAME1 + LIKE + "'%" + s + "%' or " + NAME2 + LIKE + "'%" + s + "%'";

                    } else {
                        query = NAME1 + LIKE + "'" + s + "' or " + NAME2 + LIKE + "'" + s + "'";
                        ;

                    }
                    break;

                case AstroCatalog.DNBARNARD:
                case AstroCatalog.ABELL:
                case AstroCatalog.SAC:
                    if (s.length() >= Global.COMMON_NAME_MIN_LENGTH_FOR_EXT_SEARCH)
                        query = NAME1 + LIKE + "'" + s + "' or othername like '%" + s + "%'";
                    else
                        query = NAME1 + LIKE + "'" + s + "'";
                    Log.d(TAG, "query=" + query);
                    break;
                default:
                    query = NAME1 + "='" + s.toUpperCase(Locale.US) + "'";    //data is in upper case in the databases!
            }

        } else {
            if (s.length() >= Global.COMMON_NAME_MIN_LENGTH_FOR_EXT_SEARCH) {//if long word look for part of the word else use default search
                query = NAME1 + LIKE + "'%" + s + "%' or " + NAME2 + LIKE + "'%" + s + "%'";

            } else {
                query = NAME1 + LIKE + "'" + s + "'" + OR + NAME2 + LIKE + "'" + s + "'";

            }

        }
        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, query, null, null,
                    null, NAME1 + ASC);
        } catch (SQLiteException e) {
            Log.d(TAG, "exception=" + e);
            return list;
        }
        Log.d(TAG, "size=" + cursor.getCount());

        while (cursor.moveToNext()) {
            AstroObject obj = getObjectFromCursor(cursor);
            list.add(obj);
            if (list.size() > Global.SQL_SEARCH_LIMIT) {
                break;
            }

        }
        cursor.close();
        return list;
    }

    public List<AstroObject> searchNameExact(String s) {
        List<AstroObject> list = new ArrayList<AstroObject>();
        s = s.replace("'", "''");

        String query;
        if (SearchRules.isInternalCatalog(catalog)) {
            switch (catalog) {
                case AstroCatalog.MESSIER:
                case AstroCatalog.CALDWELL:
                    query = NAME1 + LIKE + "'" + s + "' or " + NAME2 + LIKE + "'" + s + "'";
                    break;
                case AstroCatalog.HAAS:
                    query = NAME1 + "='" + s.toUpperCase(Locale.US) + "' or " + NAME2 + "='" + s.toUpperCase(Locale.US) + "'";
                    break;
                case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG://data may be in lower case!!!
                case AstroCatalog.COMET_CATALOG:
                    query = NAME1 + LIKE + "'" + s + "' or " + NAME2 + LIKE + "'" + s + "'";


                    break;

                case AstroCatalog.DNBARNARD:
                case AstroCatalog.ABELL:
                case AstroCatalog.SAC:
                    query = NAME1 + LIKE + "'" + s + "'";

                    break;
                default:
                    query = NAME1 + "='" + s.toUpperCase(Locale.US) + "'";    //data is in upper case in the databases!
            }

        } else { //user catalog

            query = NAME1 + LIKE + "'" + s + "'" + OR + NAME2 + LIKE + "'" + s + "'";


        }
        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, query, null, null,
                    null, NAME1 + ASC);
        } catch (SQLiteException e) {
            Log.d(TAG, "exception=" + e);
            return list;
        }
        Log.d(TAG, "size=" + cursor.getCount());

        while (cursor.moveToNext()) {
            AstroObject obj = getObjectFromCursor(cursor);
            list.add(obj);
            if (list.size() > Global.SQL_SEARCH_LIMIT) {
                break;
            }

        }
        cursor.close();
        return list;
    }


    public List<AstroObject> searchComment(String s) {
        List<AstroObject> list = new ArrayList<AstroObject>();
        s = s.replace("'", "''");


        String query = COMMENT + LIKE + "'" + s + "'";
        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, query, null, null,
                    null, NAME1 + ASC);
        } catch (SQLiteException e) {
            return list;
        }


        while (cursor.moveToNext()) {
            AstroObject obj = getObjectFromCursor(cursor);
            list.add(obj);
            if (list.size() > Global.SQL_SEARCH_LIMIT) {
                break;
            }

        }
        cursor.close();
        return list;
    }

    public List<AstroObject> search(String sqlReq, Analisator an, double start, double end) {


        Log.d(TAG, "search catalog=" + catalog + " sqlReq=" + sqlReq);
        boolean rd = SettingsActivity.isRemovingDuplicates() && internalDeepSky;

        if (an == null) {
            List<AstroObject> list = new ArrayList<AstroObject>();


            Cursor cursor;
            try {

                if (!rd)
                    cursor = db.query(TABLE_NAME, null, sqlReq, null, null, null, null);
                else {
                    String sql = SELECT_FROM_CUSTOMDBB_WHERE + sqlReq + AND_REF_IS_NOT_NULL_GROUP_BY_REF_UNION_SELECT_FROM_CUSTOMDBB_WHERE + sqlReq + AND_REF_IS_NULL;
                    Log.d(TAG, "sql=" + sql);
                    cursor = db.rawQuery(sql, null);
                }


            } catch (SQLiteException e) {
                Log.d(TAG, "exception=" + e);
                return list;
            }
            Log.d(TAG, "cursor size=" + cursor.getCount());
            while (cursor.moveToNext() && !QueryActivity.isStopping()) {
                AstroObject obj = getObjectFromCursor(cursor);

                list.add(obj);
                if (list.size() > Global.SQL_SEARCH_LIMIT) {
                    break;
                }

            }
            cursor.close();
            return list;
        }


        List<AstroObject> list = new ArrayList<AstroObject>();
        Log.d(TAG, "catalog=" + catalog + " rd=" + rd + " sqlReq=" + sqlReq);

        Cursor cursor;
        try {
            if (!rd)
                cursor = db.query(TABLE_NAME, null, sqlReq, null, null, null, null);
            else {
                String sql = SELECT_FROM_CUSTOMDBB_WHERE + sqlReq + AND_REF_IS_NOT_NULL_GROUP_BY_REF_UNION_SELECT_FROM_CUSTOMDBB_WHERE + sqlReq + AND_REF_IS_NULL;
                Log.d(TAG, "sql=" + sql);
                cursor = db.rawQuery(sql, null);
            }


        } catch (SQLiteException e) {
            Log.d(TAG, "e=" + e);
            return list;
        }

        Set<String> varsUsed;
        varsUsed = an.getVarsUsedInExpression();
        boolean altUsed = varsUsed.contains(ALT.toUpperCase());
        boolean visUsed = varsUsed.contains(VIS.toUpperCase());
        Calendar c = Calendar.getInstance();
        double lat = SettingsActivity.getLattitude();
        int i = 0;
        while (cursor.moveToNext() && !QueryActivity.isStopping()) {
            if (i++ % 10 == 0) {
                Log.d(TAG, "i=" + i + " list size=" + list.size());
            }
            double ra = cursor.getFloat(RA_POS);
            double dec = cursor.getFloat(DEC_POS);
            double a = Double.NaN;
            try {
                String astr = cursor.getString(A_POS);
                if (astr != null) {
                    a = cursor.getFloat(A_POS);
                }
            } catch (Exception e) {
            }

            double b = Double.NaN;
            try {
                String bstr = cursor.getString(B_POS);
                if (bstr != null) {
                    b = cursor.getFloat(B_POS);
                }
            } catch (Exception e) {
            }

            double mag = Double.NaN;
            try {
                String magstr = cursor.getString(MAG_POS);
                if (magstr != null) {
                    mag = cursor.getFloat(MAG_POS);
                }
            } catch (Exception e) {
            }


            int type = cursor.getInt(TYPE_POS);

            double vis = 0;
            if (visUsed) {
                vis = AstroTools.getMaxVisibility(a, b, mag, type);
                an.setVars(VIS, vis);
            }
            try {
                if (!altUsed) {
                    if (an.calculate()) {
                        AstroObject obj = getObjectFromCursor(cursor);

                        list.add(obj);
                        obj = null;
                        if (list.size() > Global.SQL_SEARCH_LIMIT) {
                            break;
                        }
                    }
                } else {//alt used in search
                    double ste;

                    if (end < start)
                        ste = end + 24;
                    else
                        ste = end;
                    double sttime = start;
                    boolean result = false;
                    boolean flag;

                    do {
                        double alt = AstroTools.Altitude(sttime, lat, ra, dec);
                        an.setVars(ALT, alt);
                        result = an.calculate();
                        // Log.d(TAG,"Alt="+Altitude(sttime,lat,ra,dec));
                        sttime = sttime + 0.5;
                        flag = (sttime < ste);
                        if (result)
                            flag = false;
                    } while (flag);
                    if (result) {
                        AstroObject obj = getObjectFromCursor(cursor);

                        list.add(obj);
                        obj = null;
                        if (list.size() > Global.SQL_SEARCH_LIMIT) {
                            break;
                        }
                    }


                }

            } catch (Exception e) {
            }

        }
        cursor.close();
        return list;
    }

    public long edit(CustomObject obj) {
        ContentValues values = new ContentValues();
        values.put(RA, obj.getRa());
        values.put(DEC, obj.getDec());
        if (Double.isNaN(obj.getA()))
            values.putNull(A);
        else
            values.put(A, obj.getA());
        if (Double.isNaN(obj.getB()))
            values.putNull(B);
        else
            values.put(B, obj.getB());
        if (Double.isNaN(obj.getMag()))
            values.putNull(MAG);
        else
            values.put(MAG, obj.getMag());
        values.put(CONSTEL, obj.getCon());
        values.put(TYPE, obj.getType());

        if (Double.isNaN(obj.pa))
            values.putNull(PA);
        else
            values.put(PA, obj.pa);

        values.put(TYPESTR, obj.typeStr);
        values.put(NAME1, obj.getShortName());
        values.put(NAME2, obj.getLongName());
        values.put(COMMENT, obj.comment);
        long result = -1;

        result = db.update(TABLE_NAME, values, _ID + "=" + obj.getId(), null);

        return result;
    }


    public List<AstroObject> searchNearby(Point nearbyObject, double fov, double vis) {
        List<AstroObject> list = new ArrayList<AstroObject>();
        String sqlReq = SearchRules.createQueryNearby(context);
        boolean rd = SettingsActivity.isRemovingDuplicates() && internalDeepSky;
        Cursor cursor;
        try {
            if (!rd)
                cursor = db.query(TABLE_NAME, null, sqlReq, null, null, null, null);
            else {
                String sql = SELECT_FROM_CUSTOMDBB_WHERE + sqlReq + AND_REF_IS_NOT_NULL_GROUP_BY_REF_UNION_SELECT_FROM_CUSTOMDBB_WHERE + sqlReq + AND_REF_IS_NULL;
                Log.d(TAG, "sql=" + sql);
                cursor = db.rawQuery(sql, null);
            }


        } catch (SQLiteException e) {
            Log.d(TAG, "e=" + e);
            return list;
        }
        double raObj = 0;
        double decObj = 0;
        double dist = fov / 2;
        raObj = nearbyObject.getRa();
        decObj = nearbyObject.getDec();
        int emptyRule = SettingsActivity.getEmptyRule();

        while (cursor.moveToNext()) {

            AstroObject obj = getObjectFromCursor(cursor);

            if (cos(dist / 60 * PI / 180) < (sin(obj.getDec() * PI / 180) * sin(decObj * PI / 180) + cos(obj.getDec() * PI / 180) * cos(decObj * PI / 180) * cos((obj.getRa() - raObj) * PI / 12))) {
                if (vis < 0 || AstroTools.CheckVisibility(obj.getA(), obj.getB(), obj.getMag(), vis, emptyRule, obj.getType()))
                    list.add(obj);
            }
        }
        return list;
    }

    public static void exportToTextFile(Context context) {
        int pos = -1;
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        int i = 0;
        for (Object o : iL) {
            DbListItem item = (DbListItem) o;
            if (item.dbName.toUpperCase().equals("UGC")) {
                pos = i;
                break;
            }
            i++;
        }
        if (pos == -1) {
            Log.d(TAG, "not found");
            return;
        }
        DbListItem item = (DbListItem) iL.get(pos);


        CustomDatabase db = new CustomDatabase(context, item.dbFileName, item.cat);

        ErrorHandler eh = new ErrorHandler();
        db.open(eh);
        if (eh.hasError()) {
            Log.d(TAG, "error=" + eh);
        }
        Cursor cursor = db.getAll();
        File f = new File("/sdcard/ugc.txt");
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(f));
            while (cursor.moveToNext()) {
                AstroObject obj = db.getObjectFromCursor(cursor);
                CustomObject o = (CustomObject) obj;
                String s = o.ra + "," + o.dec + "," + o.mag + "," + o.a + "," + o.b + "," + o.id + "," + o.pa + "," +
                        o.type + "," + o.name1;
                pw.println(s);
            }
            pw.close();
            db.close();

        } catch (Exception e) {
            Log.d(TAG, "exception=" + e);
        }
    }


    static class ProcessCursorImpl implements ProcessCursor {
        int catalog;

        public ProcessCursorImpl(int catalog) {
            this.catalog = catalog;
        }

        @Override
        public AstroObject getObjectFromCursor(Cursor cursor) {
            int id = cursor.getInt(ID_POS);

            double ra = cursor.getFloat(RA_POS);
            double dec = cursor.getFloat(DEC_POS);
            double a = cursor.getFloat(A_POS);
            double b = cursor.getFloat(B_POS);
            double mag = cursor.getFloat(MAG_POS);
            int con = cursor.getInt(CON_POS);
            int type = cursor.getInt(TYPE_POS);
            double pa = cursor.getFloat(PA_POS);
            String typestr = cursor.getString(TYPESTR_POS);
            String name1 = cursor.getString(NAME1_POS);
            String name2 = cursor.getString(NAME2_POS);
            String comment = cursor.getString(COMMENT_POS);
            AstroObject obj = new CustomObject(catalog, id, ra, dec, con, type, typestr, a,
                    b, mag, pa, name1, name2, comment);
            return obj;
        }
    }

    /**
     * internal method for building db
     * use .dump table result from sqlite3
     *
     * @param context
     */
    public static void buildDb(Context context, String file, int catalog) {
        ErrorHandler eh = new ErrorHandler();
        String dbname = DbManager.getDbFileName(catalog);
        CustomDatabase db = new CustomDatabase(context, dbname, catalog);
        Log.d(TAG, "opening db");
        db.open(eh);
        if (eh.hasError()) {
            Log.d(TAG, "error opening db " + eh.getErrorString());
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(new File(Global.exportImportPath, file))));
            String s;
            int j = 0;
            while ((s = reader.readLine()) != null) {
                db.db.execSQL(s);
                if (j % 100 == 0)
                    Log.d(TAG, "" + s);
                j++;
            }
            reader.close();
        } catch (Exception e) {
            Log.d(TAG, "exception=" + e);
        }
        db.close();
        File dbf = context.getDatabasePath(dbname);
        AstroTools.FileToCopy fc = new AstroTools.FileToCopy(dbf.getParent(), dbname, Global.exportImportPath);
        boolean result = fc.copy();
        Log.d(TAG, "copy result=" + result);
    }

    public void beginTransaction() {
        db.beginTransaction();
    }

    public void setTransactionSuccessful() {
        db.setTransactionSuccessful();
    }

    public void endTransaction() {
        db.endTransaction();
    }
}
