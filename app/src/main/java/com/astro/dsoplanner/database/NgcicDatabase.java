package com.astro.dsoplanner.database;

import static android.provider.BaseColumns._ID;
import static com.astro.dsoplanner.Constants.A;
import static com.astro.dsoplanner.Constants.ALT;
import static com.astro.dsoplanner.Constants.B;
import static com.astro.dsoplanner.Constants.CONSTEL;
import static com.astro.dsoplanner.Constants.DEC;
import static com.astro.dsoplanner.Constants.HERSHELL;
import static com.astro.dsoplanner.Constants.MAG;
import static com.astro.dsoplanner.Constants.NAME;
import static com.astro.dsoplanner.Constants.PA;
import static com.astro.dsoplanner.Constants.RA;
import static com.astro.dsoplanner.Constants.TABLE_NAME;
import static com.astro.dsoplanner.Constants.TYPE;
import static com.astro.dsoplanner.Constants.VIS;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.astro.dsoplanner.Analisator;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.base.NgcicObject;


import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.QueryActivity;
import com.astro.dsoplanner.SearchRules;
import com.astro.dsoplanner.SettingsActivity;

public class NgcicDatabase implements AstroCatalog {
    private static final String AND_REF_IS_NULL = ") and ref is null;";
    private static final String AND_REF_IS_NOT_NULL_GROUP_BY_REF_UNION_SELECT_FROM_NGCIC_WHERE = ") and ref is not null group by ref union select * from ngcic where (";
    private static final String SELECT_FROM_NGCIC_WHERE = "select * from ngcic where (";

    private static final String _02 = "=0";
    private static final String _0 = ">0";
    private static final String AND2 = " AND ";
    private static final String AND = " AND (";
    private static final String OR = " OR ";
    private static final String LIKE = " LIKE ";
    private static final String ASC = " ASC";
    private static final String IC = "ic";
    private static final String NGC2 = "ngc";
    private static final String NGCIC_DATABASE = "NgcicDatabase";
    private static final String ERROR_OPENING_DATABASE = "Error opening database";

    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";
    private static final String TEXT = " TEXT);";
    private static final String FLOAT = " FLOAT,";
    private static final String INTEGER = " INTEGER,";
    private static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    private static final String CREATE_TABLE = "CREATE TABLE ";

    private static final String DATABASE_NAME = Constants.NGCIC_DATABASE_NAME;
    public static final int ID_POS = 0;
    public static final int NAME_POS = 1;
    public static final int RA_POS = 2;
    public static final int DEC_POS = 3;
    public static final int MAG_POS = 4;
    public static final int A_POS = 5;
    public static final int B_POS = 6;
    public static final int CON_POS = 7;
    public static final int TYPE_POS = 8;
    public static final int PA_POS = 9;
    public static final int MESSIER_POS = 10;
    public static final int CALDWELL_POS = 11;
    public static final int HERSHELL_POS = 12;
    public static final int NAME1_POS = 13;
    public static final int COMMENT_POS = 14;
    public static final int CLASS_POS = 15;
    public static final int REF_POS = 16;
    private static final String TAG = NgcicDatabase.class.getSimpleName();
    static final String NAME1 = "name1";

    NGCData ngc;
    public SQLiteDatabase db;
    public Context context;

    private class NGCData extends SQLiteOpenHelper {


        private static final int DATABASE_VERSION = 2;


        public NGCData(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    public NgcicDatabase(Context acontext) {
        context = acontext;
        ngc = new NGCData(context);
    }

    public NgcicDatabase() {
        this(Global.getAppContext());
    }

    public void open(ErrorHandler eh) {
        try {
            db = ngc.getReadableDatabase();
        } catch (Exception e) {
            ErrorHandler.ErrorRec rec = new ErrorHandler.ErrorRec(ErrorHandler.SQL_DB, ERROR_OPENING_DATABASE);
            eh.addError(rec);
            Log.d(TAG, "e=" + e);
        }
    }

    public boolean isOpen() {
        return db.isOpen();
    }

    public void close() {
        try {
            ngc.close();
        } catch (Exception e) {
        }
    }

    public String toString() {
        return NGCIC_DATABASE;
    }

    public AstroObject getObjectFromCursor(Cursor cursor) { //should be called after cursor.moveToNext called

        int id = cursor.getInt(ID_POS);
        int ngc_num = cursor.getInt(NAME_POS);
        double ra = cursor.getFloat(RA_POS);
        double dec = cursor.getFloat(DEC_POS);

        double a = Double.NaN;
        try {
            String astr = cursor.getString(A_POS);
            if (astr != null)
                a = cursor.getFloat(A_POS);
        } catch (Exception e) {
        }

        double b = Double.NaN;
        try {
            String bstr = cursor.getString(B_POS);
            if (bstr != null)
                b = cursor.getFloat(B_POS);
        } catch (Exception e) {
        }

        double mag = Double.NaN;
        try {

            String magStr = cursor.getString(MAG_POS);
            if (magStr != null) mag = cursor.getFloat(MAG_POS);
        } catch (Exception e) {
        }

        int con = cursor.getInt(CON_POS);
        int type = cursor.getInt(TYPE_POS);

        double pa = Double.NaN;
        try {
            String pastr = cursor.getString(PA_POS);
            if (pastr != null)
                pa = cursor.getFloat(PA_POS);
        } catch (Exception e) {

        }

        int m = 0;
        try {
            m = cursor.getInt(MESSIER_POS);
        } catch (Exception e) {

        }
        int c = 0;
        try {
            c = cursor.getInt(CALDWELL_POS);
        } catch (Exception e) {
        }

        int h = 0;
        try {
            h = cursor.getInt(HERSHELL_POS);
        } catch (Exception e) {
        }

        String comment = "";
        try {
            comment = cursor.getString(COMMENT_POS);
        } catch (Exception e) {
        }


        String clas = "";
        try {
            clas = cursor.getString(CLASS_POS);
        } catch (Exception e) {
        }

        AstroObject obj = new NgcicObject(ra, dec, con, type, id, a, b, mag, ngc_num, m, c, h, pa, comment, clas);
        int ref = 0;
        try {
            ref = cursor.getInt(REF_POS);//ref to _id of NGCIC DB
        } catch (Exception e) {

        }

        if (ref != 0) {
            obj.ref = ref;
        }
        return obj;


    }

    public ContentValues getContentValuesFromCursor(Cursor cursor) {
        int ngc_num = cursor.getInt(NAME_POS);
        double ra = cursor.getFloat(RA_POS);
        double dec = cursor.getFloat(DEC_POS);
        double a = cursor.getFloat(A_POS);
        double b = cursor.getFloat(B_POS);
        double mag = cursor.getFloat(MAG_POS);
        int con = cursor.getInt(CON_POS);
        int type = cursor.getInt(TYPE_POS);
        double pa = cursor.getFloat(PA_POS);
        int m = cursor.getInt(MESSIER_POS);
        int c = cursor.getInt(CALDWELL_POS);
        int h = cursor.getInt(HERSHELL_POS);

        ContentValues values = new ContentValues();
        values.put(NAME, ngc_num);
        values.put(RA, ra);
        values.put(DEC, dec);
        values.put(A, a);
        values.put(B, b);
        values.put(MAG, mag);
        values.put(CONSTEL, con);
        values.put(TYPE, type);
        values.put(PA, pa);
        values.put(Constants.MESSIER, m);
        values.put(Constants.CALDWELL, c);
        values.put(HERSHELL, h);

        String name1 = "";
        if (ngc_num < 10000)
            name1 = NGC2 + ngc_num;
        else
            name1 = IC + (ngc_num - 10000);
        values.put(NgcicDatabaseTemp.NAME1, name1);
        return values;

    }

    public ContentValues getContentValuesWithIdFromCursor(Cursor cursor) {
        int id = cursor.getInt(ID_POS);
        int ngc_num = cursor.getInt(NAME_POS);
        double ra = cursor.getFloat(RA_POS);
        double dec = cursor.getFloat(DEC_POS);
        double a = cursor.getFloat(A_POS);
        double b = cursor.getFloat(B_POS);
        double mag = cursor.getFloat(MAG_POS);
        int con = cursor.getInt(CON_POS);
        int type = cursor.getInt(TYPE_POS);
        double pa = cursor.getFloat(PA_POS);
        int m = cursor.getInt(MESSIER_POS);
        int c = cursor.getInt(CALDWELL_POS);
        int h = cursor.getInt(HERSHELL_POS);

        ContentValues values = new ContentValues();
        values.put(_ID, id);
        values.put(NAME, ngc_num);
        values.put(RA, ra);
        values.put(DEC, dec);
        values.put(A, a);
        values.put(B, b);
        values.put(MAG, mag);
        values.put(CONSTEL, con);
        values.put(TYPE, type);
        values.put(PA, pa);
        values.put(Constants.MESSIER, m);
        values.put(Constants.CALDWELL, c);
        values.put(HERSHELL, h);

        String name1 = "";
        if (ngc_num < 10000)
            name1 = NGC2 + ngc_num;
        else
            name1 = IC + ngc_num;
        values.put(NgcicDatabaseTemp.NAME1, name1);
        return values;

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
                    null, NAME + ASC);
        } catch (SQLiteException e) {
            return null;
        }

        return cursor;
    }


    public List<AstroObject> search() {
        List<AstroObject> list = new ArrayList<AstroObject>();

        String sqlReq = SearchRules.createQuery(context, false, true, false);
        if (!SettingsActivity.isNgcicSelected()) {
            String mch = getHershelString(SettingsActivity.DSO_SELECTION);
            if (!"".equals(mch))
                sqlReq = "(" + sqlReq + ") and " + mch;
            else return list;
        }


        boolean rd = SettingsActivity.isRemovingDuplicates();
        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, sqlReq, null, null, null, null);
        } catch (SQLiteException e) {
            Log.d(TAG, "e=" + e);
            return list;
        }
        Log.d(TAG, "cursor size=" + cursor.getCount());
        int filter = SettingsActivity.getFilter();

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

        int emptyRule = SettingsActivity.getEmptyRule();

        AstroTools.CheckAltContext checkContext = new AstroTools.CheckAltContext();
        double DL = SettingsActivity.getDetectionLimit();

        while (cursor.moveToNext() && !QueryActivity.isStopping()) {
            // if...altitude and visibility
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
            int name = cursor.getInt(NAME_POS);

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
                        break;
                }
                if (flag) {
                    AstroObject obj = getObjectFromCursor(cursor);
                    list.add(obj);
                }
            }
        }
        cursor.close();
        if (rd)
            return excludeDups(list);
        else
            return list;
    }


    /**
     * list is modified
     *
     * @param list
     */
    private List<AstroObject> excludeDups(List<AstroObject> list) {
        List<AstroObject> list2 = new ArrayList<AstroObject>();

        Comparator<AstroObject> comp = new Comparator<AstroObject>() {
            public int compare(AstroObject lhs, AstroObject rhs) {
                if (lhs.ref < rhs.ref)
                    return -1;
                else if (lhs.ref > rhs.ref)
                    return 1;
                else return 0;
            }
        };

        class Stack {
            List<AstroObject> stack = new ArrayList<AstroObject>();//a la stack

            /**
             *
             * @param obj
             * @return the single object when necessary
             */
            public AstroObject add(AstroObject obj) {
                if (stack.size() == 0) {
                    stack.add(obj);
                    return null;
                } else if (obj.ref == stack.get(0).ref) {
                    stack.add(obj);
                    return null;
                } else {//different refs
                    AstroObject o = getSingle();
                    stack = new ArrayList<AstroObject>();
                    stack.add(obj);
                    return o;
                }
            }

            /**
             * get object with minimum ngc num from stack
             * @return
             */
            public AstroObject getSingle() {
                if (stack.size() == 0)
                    return null;
                int minngc = 100000;
                int pos = -1;
                for (int i = 0; i < stack.size(); i++) {
                    NgcicObject o = (NgcicObject) stack.get(i);
                    if (o.ngc < minngc) {
                        minngc = o.ngc;
                        pos = i;
                    }

                }
                if (pos != -1) {
                    return stack.get(pos);
                } else {//should not be
                    return stack.get(0);
                }

            }
        }

        Stack stack = new Stack();
        Collections.sort(list, comp);

        //assume that first comes 0 ref
        for (AstroObject obj : list) {
            if (obj.ref == 0)
                list2.add(obj);
            else {
                AstroObject o = stack.add(obj);
                if (o != null) {
                    list2.add(o);
                }
            }

        }
        AstroObject o = stack.getSingle();
        if (o != null) {
            list2.add(o);
        }
        return list2;
    }

    public List<AstroObject> search(String s) {
        List<AstroObject> list = new ArrayList<AstroObject>();
        Log.d(TAG, "s=" + s);

        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, s, null, null,
                    null, NAME + ASC);
        } catch (SQLiteException e) {
            return list;
        }


        while (cursor.moveToNext()) {
            AstroObject obj = getObjectFromCursor(cursor);
            list.add(obj);
        }
        cursor.close();
        return list;
    }

    public List<AstroObject> searchName(String s) {
        List<AstroObject> list = new ArrayList<AstroObject>();

        s = s.replace("'", "''");
        String query = NAME1 + LIKE + "'" + s + "'";
        Log.d(TAG, "ngcic db search name=" + s);
        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, query, null, null,
                    null, NAME + ASC);
        } catch (SQLiteException e) {
            return list;
        }


        while (cursor.moveToNext()) {
            AstroObject obj = getObjectFromCursor(cursor);
            list.add(obj);


        }
        cursor.close();
        Log.d(TAG, "ngcic db search name=" + s + " over");
        return list;
    }

    public List<AstroObject> searchNameExact(String s) {
        return searchName(s);
    }

    public List<AstroObject> searchComment(String s) {
        return new ArrayList<AstroObject>();//this database does not have comments
    }


    /**
     * @return additional hersh string ,
     * "" otherwise
     * @type Settings1243.DSO_SELECTION or SEARCH_NEARBY
     */
    public static String getHershelString(int type) {

        List<String> list = new ArrayList<String>();

        if (type == SettingsActivity.DSO_SELECTION) {
            if (SettingsActivity.getHershell()) {
                list.add(Constants.HERSHELL);
            }
        } else if (type == SettingsActivity.SEARCH_NEARBY) {
            if (SettingsActivity.getHershellNearby()) {
                list.add(Constants.HERSHELL);
            }
        }
        if (list.size() == 0)
            return "";
        String s = " (";
        for (int i = 0; i < list.size(); i++) {
            s += list.get(i) + ">0";
            if (i != list.size() - 1)
                s += " or ";
            else
                s += ")";
        }
        return s;

    }

    /**
     * searching for objects based both on request to database and on local request(altitude,visibility etc)
     *
     * @param an - should be compiled already!!!
     */

    public List<AstroObject> search(String sqlReq, Analisator an, double start, double end) {

        List<AstroObject> list = new ArrayList<AstroObject>();
        if (!SettingsActivity.isNgcicSelected()) {
            String mch = getHershelString(SettingsActivity.DSO_SELECTION);
            if (!"".equals(mch))
                sqlReq = "(" + sqlReq + ") and " + mch;
            else return list;
        }


        boolean rd = SettingsActivity.isRemovingDuplicates();
        Cursor cursor;

        try {
            cursor = db.query(TABLE_NAME, null, sqlReq, null, null, null, NAME + ASC);
        } catch (SQLiteException e) {
            Log.d(TAG, "e=" + e);
            return list;
        }


        Set<String> varsUsed = new HashSet<String>();
        if (an != null)
            varsUsed = an.getVarsUsedInExpression();
        boolean altUsed = varsUsed.contains(ALT.toUpperCase());
        boolean visUsed = varsUsed.contains(VIS.toUpperCase());
        Calendar c = Calendar.getInstance();
        double lat = SettingsActivity.getLattitude();

        while (cursor.moveToNext() && !QueryActivity.isStopping()) {

            AstroObject obj = getObjectFromCursor(cursor);

            double vis = 0;
            if (visUsed) {
                vis = AstroTools.getMaxVisibility(obj.getA(), obj.getB(), obj.getMag(), obj.getType());
                an.setVars(VIS, vis);
            }
            try {
                if (!altUsed) {
                    if (an == null) {
                        list.add(obj);
                    } else if (an.calculate()) {
                        list.add(obj);
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
                    double ra = obj.getRa();
                    double dec = obj.getDec();
                    do {
                        double alt = AstroTools.Altitude(sttime, lat, ra, dec);
                        an.setVars(ALT, alt);
                        result = an.calculate();
                        sttime = sttime + 0.5;
                        flag = (sttime < ste);
                        if (result)
                            flag = false;
                    } while (flag);
                    if (result) {
                        list.add(obj);
                    }


                }

            } catch (Exception e) {
            }

        }
        cursor.close();
        if (rd)
            return excludeDups(list);
        else
            return list;
    }


    public long add(AstroObject obj, ErrorHandler eh) {
        return -1;
    }

    public void remove(int id) {
        throw new UnsupportedOperationException();
    }

    public void removeAll() {
        throw new UnsupportedOperationException();
    }

    public List<AstroObject> searchNearby(Point nearbyObject, double fov, double vis) {
        List<AstroObject> list = new ArrayList<AstroObject>();

        Log.d(TAG, "nearbyObj=" + nearbyObject + " fov=" + fov + " vis=" + vis);
        String sqlReq = SearchRules.createQueryNearby(context);
        if (!SettingsActivity.isNgcicNearbySelected()) {
            String mch = getHershelString(SettingsActivity.SEARCH_NEARBY);
            if (!"".equals(mch))
                sqlReq = "(" + sqlReq + ") and " + mch;
            else return list;
        }


        boolean rd = SettingsActivity.isRemovingDuplicates();
        Cursor cursor;
        try {
            if (!rd)
                cursor = db.query(TABLE_NAME, null, sqlReq, null, null, null, null);
            else {
                String sql = SELECT_FROM_NGCIC_WHERE + sqlReq + AND_REF_IS_NOT_NULL_GROUP_BY_REF_UNION_SELECT_FROM_NGCIC_WHERE + sqlReq + AND_REF_IS_NULL;
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

            //public DSO (int ngc,double ra,double dec,double mag,double a,double b,int messier,int caldwell,int hershell,int type,int con,double pa)
            //                1           2         3         4           5       6         10           11       12           8        7      9
            // if...altitude and visibility

            AstroObject obj = getObjectFromCursor(cursor);

            if (cos(dist / 60 * PI / 180) < (sin(obj.getDec() * PI / 180) * sin(decObj * PI / 180) + cos(obj.getDec() * PI / 180) * cos(decObj * PI / 180) * cos((obj.getRa() - raObj) * PI / 12))) {
                if (vis < 0 || AstroTools.CheckVisibility(obj.getA(), obj.getB(), obj.getMag(), vis, emptyRule, obj.getType()))
                    list.add(obj);
            }
        }
        return list;

    }

    public void beginTransaction() {
        throw new UnsupportedOperationException();
    }

    public void setTransactionSuccessful() {
        throw new UnsupportedOperationException();
    }

    public void endTransaction() {
        throw new UnsupportedOperationException();
    }

    public static void exportToTextFile(Context context) {
        NgcicDatabase db = new NgcicDatabase(context);
        ErrorHandler eh = new ErrorHandler();
        db.open(eh);
        if (eh.hasError()) {
            Log.d(TAG, "error=" + eh);
        }
        Cursor cursor = db.getAll();
        File f = new File("/sdcard/ngcic.txt");
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(f));
            while (cursor.moveToNext()) {
                AstroObject obj = db.getObjectFromCursor(cursor);
                NgcicObject o = (NgcicObject) obj;
                String s = o.ra + "," + o.dec + "," + o.mag + "," + o.a + "," + o.b + "," + o.id + "," + o.pa + "," +
                        o.type + "," + o.ngc + "," + o.messier + "," + o.caldwell + "," + o.hershell;
                pw.println(s);
                Log.d(TAG, "exported=" + o);
            }
            pw.close();
            db.close();

        } catch (Exception e) {
            Log.d(TAG, "exception=" + e);
        }
    }
}
