package com.astro.dsoplanner.database;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.Global;


public class CrossDb {


    private static final String CROSS_DB2 = "CrossDb";
    private static final String SELECT_NAME_FROM_CROSS_WHERE_ID = "select name from cross where id=";
    private static final String SELECT_ID_FROM_CROSS_WHERE_NAME_EQUAL = "select id from cross where name = ";

    private static final String ERROR_OPENING_DATABASE = "Error opening database";
    private static final String DROP_TABLE_IF_EXISTS_CROSS = "drop table if exists CROSS";
    private static final String CREATE_TABLE_CROSS_NAME_TEXT_ID_INTEGER = "CREATE TABLE CROSS(NAME TEXT,ID INTEGER)";


    private static final String TAG = CrossDb.class.getSimpleName();
    NGCData ngc;
    SQLiteDatabase db;
    Context context;

    private class NGCData extends SQLiteOpenHelper {
        private static final int DATABASE_VERSION = 1;

        public NGCData(Context ctx) {
            super(ctx, Constants.SQL_DATABASE_CROSS_DB, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String s = "";
            s = CREATE_TABLE_CROSS_NAME_TEXT_ID_INTEGER;

            db.execSQL(s);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TABLE_IF_EXISTS_CROSS);
            onCreate(db);
        }
    }

    public CrossDb(Context acontext) {
        context = acontext;
        ngc = new NGCData(context);
    }

    public CrossDb() {
        this(Global.getAppContext());
    }

    public void open(ErrorHandler eh) {
        try {
            db = ngc.getReadableDatabase();
        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
            ErrorHandler.ErrorRec rec = new ErrorHandler.ErrorRec(ErrorHandler.SQL_DB, ERROR_OPENING_DATABASE);
            eh.addError(rec);
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
        return CROSS_DB2;
    }

    /*
     * looks for other object designations
     */
    public List<String> search(String s) {
        Cursor cursor;
        List<String> list = new ArrayList<String>();

        try {
            String s2 = s.replace("'", "''").toUpperCase(Locale.US);
            s2 = "'" + s2 + "'";
            Log.d(TAG, "s2=" + s2);
            cursor = db.rawQuery(SELECT_ID_FROM_CROSS_WHERE_NAME_EQUAL + s2, null);
            Log.d(TAG, "cross db cursor size=" + cursor.getCount());
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                Log.d(TAG, "id=" + id);
                Cursor cursor2 = db.rawQuery(SELECT_NAME_FROM_CROSS_WHERE_ID + id, null);
                while (cursor2.moveToNext()) {
                    String name = cursor2.getString(0);
                    Log.d(TAG, "name=" + name);
                    if (!s.equals(name))
                        list.add(name);
                }
                cursor2.close();
            }
            cursor.close();
        } catch (Exception e) {
            Log.d(TAG, "cross db e=" + e);
            return list;
        }

        return list;

    }


    /**
     * use it for own name search. First make sure that it is own name in fact! Not ngc ... string,
     * otherwise will find a lot of irrelevant info
     *
     * @param s - part of the string to be searched in db
     * @return
     */
    public List<String> searchPart(String s) {
        Cursor cursor;
        List<String> list = new ArrayList<String>();
        if (s.length() < Global.COMMON_NAME_MIN_LENGTH_FOR_EXT_SEARCH)
            return list;//just in case

        try {

            String s2 = "'" + s + "%'";
            Log.d(TAG, "s2=" + s2);
            cursor = db.rawQuery("select id from cross where name like " + s2, null);
            Log.d(TAG, "cross db cursor size=" + cursor.getCount());
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                Log.d(TAG, "id=" + id);
                Cursor cursor2 = db.rawQuery(SELECT_NAME_FROM_CROSS_WHERE_ID + id, null);
                while (cursor2.moveToNext()) {
                    String name = cursor2.getString(0);
                    Log.d(TAG, "name=" + name);
                    if (!s.equals(name))
                        list.add(name);
                }
                cursor2.close();
            }
            cursor.close();
        } catch (Exception e) {
            Log.d(TAG, "cross db e=" + e);
            return list;
        }

        return list;

    }

    /**
     * @param s       - object name to be found in cross database
     * @param context
     * @return - a list of other designations
     */
    public static List<String> searchName(String s, Context context) {
        List<String> list = new ArrayList<String>();
        ErrorHandler eh = new ErrorHandler();
        CrossDb db = new CrossDb(context);
        db.open(eh);
        if (eh.hasError()) {
            Log.d(TAG, "error opening db " + eh.getErrorString());
            return list;
        }
        list = db.search(s);
        db.close();
        return list;

    }

    /**
     * @param s       - part of the object name to be found in cross database
     * @param context
     * @return - a list of other designations
     */
    public static List<String> searchPartName(String s, Context context) {
        List<String> list = new ArrayList<String>();
        ErrorHandler eh = new ErrorHandler();
        CrossDb db = new CrossDb(context);
        db.open(eh);
        if (eh.hasError()) {
            Log.d(TAG, "error opening db " + eh.getErrorString());
            return list;
        }
        list = db.searchPart(s);
        db.close();
        return list;

    }

    public static List<List<String>> searchNameForDouble(String name, Context context) {
        List<List<String>> list = new ArrayList<List<String>>();
        ErrorHandler eh = new ErrorHandler();
        CrossDb db = new CrossDb(context);
        db.open(eh);
        if (eh.hasError()) {
            Log.d(TAG, "error opening db " + eh.getErrorString());
            return list;
        }

        try {
            String s2 = name.replace("'", "''");
            s2 = "'" + s2 + "'";
            s2 = s2.toUpperCase();
            Log.d(TAG, "s2=" + s2);
            Cursor cursor = db.db.rawQuery(SELECT_ID_FROM_CROSS_WHERE_NAME_EQUAL + s2, null);

            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                Log.d(TAG, "id=" + id);
                List<String> list2 = new ArrayList<String>();
                Cursor cursor2 = db.db.rawQuery(SELECT_NAME_FROM_CROSS_WHERE_ID + id, null);
                while (cursor2.moveToNext()) {
                    String n = cursor2.getString(0);
                    list2.add(n);
                }
                cursor2.close();
                list.add(list2);
            }
            cursor.close();
        } catch (Exception e) {
            return list;
        } finally {
            db.close();
        }
        return list;
    }

    /**
     * internal method for building db
     * use .dump table result from sqlite3
     *
     * @param context
     */
    public static void buildDb(Context context) {
        ErrorHandler eh = new ErrorHandler();
        CrossDb db = new CrossDb(context);
        Log.d(TAG, "opening db");
        db.open(eh);
        if (eh.hasError()) {
            Log.d(TAG, "error opening db " + eh.getErrorString());
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(new File(Global.exportImportPath, "total_cross_out.txt"))));
            String s;
            int j = 0;
            while ((s = reader.readLine()) != null) {
                db.db.execSQL(s);
                Log.d(TAG, "" + s);
            }
            reader.close();
        } catch (Exception e) {
            Log.d(TAG, "exception=" + e);
        }
        db.close();
        File dbf = context.getDatabasePath("cross.db");
        AstroTools.FileToCopy fc = new AstroTools.FileToCopy(dbf.getParent(), "cross.db", Global.exportImportPath);
        boolean result = fc.copy();
        Log.d(TAG, "copy result=" + result);
    }
}
