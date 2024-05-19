package com.astro.dsoplanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.Global;


import com.astro.dsoplanner.TelescopeRecord;

import java.util.ArrayList;
import java.util.List;

import static android.provider.BaseColumns._ID;

public class TelescopeDatabase {

    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";
    private static final String TEXT2 = " TEXT );";
    private static final String INTEGER = " INTEGER, ";
    private static final String DOUBLE = " DOUBLE, ";
    private static final String TEXT = " TEXT, ";
    private static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    private static final String CREATE_TABLE = "CREATE TABLE ";
    private static final String TABLE_NAME = "scopemain";

    private static final String DATABASE_NAME = Constants.TELESCOPE_DATABASE_NAME;
    private static final String TAG = TelescopeDatabase.class.getSimpleName();
    private Scope1Data scope1;

    SQLiteDatabase db;
    Context context;

    private class Scope1Data extends SQLiteOpenHelper {


        private static final int DATABASE_VERSION = 1;

        public Scope1Data(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String s = "";
            s = CREATE_TABLE + TABLE_NAME + " ("
                    + _ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT
                    + TelescopeRecord.TNAME + TEXT
                    + TelescopeRecord.TAPERTURE + DOUBLE
                    + TelescopeRecord.TFOCUS + DOUBLE
                    + TelescopeRecord.TPASS + DOUBLE
                    + TelescopeRecord.TEP + INTEGER
                    + TelescopeRecord.TDESCR + TEXT2;

            db.execSQL(s);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            db.execSQL(DROP_TABLE_IF_EXISTS + TABLE_NAME);
            onCreate(db);
        }
    }

    public TelescopeDatabase(Context acontext) {
        context = acontext;
        scope1 = new Scope1Data(context);
    }

    public TelescopeDatabase() {
        this(Global.getAppContext());
    }

    public void open() {
        try {
            db = scope1.getReadableDatabase();
        } catch (Exception e) {
        }
    }

    public void close() {
        try {
            scope1.close();
        } catch (Exception e) {
        }
    }

    private TelescopeRecord getObjectFromCursor(Cursor cursor) { //should be called after cursor.moveToNext called
        return new TelescopeRecord(cursor.getInt(0),
                cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3),
                cursor.getDouble(4), cursor.getInt(5), cursor.getString(6));
    }

    public List<TelescopeRecord> search() {
        List<TelescopeRecord> list = new ArrayList<TelescopeRecord>();
        if (db == null) return list;

        String query = null;
        Cursor cursor = db.query(TABLE_NAME, null, query, null, null, null, null);
        while (cursor.moveToNext()) {
            TelescopeRecord nr = getObjectFromCursor(cursor);
            if (cursor != null)
                list.add(nr);
        }

        return list;
    }

    public TelescopeRecord getTelescopeRecord(int id) { //id in notedatabase
        Cursor cursor = db.query(TABLE_NAME, null, _ID + "=" + id, null, null, null, null);
        if (cursor.moveToNext()) return (getObjectFromCursor(cursor));
        return null;
    }

    public long add(String name, double ap, double fo, double pa, int epid, String note) {
        ContentValues v = new ContentValues();
        v.put(TelescopeRecord.TNAME, name);
        v.put(TelescopeRecord.TAPERTURE, ap);
        v.put(TelescopeRecord.TFOCUS, fo);
        v.put(TelescopeRecord.TPASS, pa);
        v.put(TelescopeRecord.TEP, epid);
        v.put(TelescopeRecord.TDESCR, note);

        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_NAME, null, v);
        } catch (Exception e) {
        }
        return result;
    }

    public long add(TelescopeRecord rec) {
        ContentValues v = new ContentValues();
        v.put(TelescopeRecord.TNAME, rec.name);
        v.put(TelescopeRecord.TAPERTURE, rec.aperture);
        v.put(TelescopeRecord.TFOCUS, rec.focus);
        v.put(TelescopeRecord.TPASS, rec.pass);
        v.put(TelescopeRecord.TEP, rec.ep_id);
        v.put(TelescopeRecord.TDESCR, rec.note);

        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_NAME, null, v);
        } catch (Exception e) {
        }
        return result;
    }

    public void edit(TelescopeRecord t) {
        ContentValues v = new ContentValues();
        v.put(TelescopeRecord.TNAME, t.name);
        v.put(TelescopeRecord.TAPERTURE, t.aperture);
        v.put(TelescopeRecord.TFOCUS, t.focus);
        v.put(TelescopeRecord.TPASS, t.pass);
        v.put(TelescopeRecord.TEP, t.ep_id);
        v.put(TelescopeRecord.TDESCR, t.note);
        try {
            db.update(TABLE_NAME, v, _ID + "=" + t.id, null);
        } catch (Exception e) {
        }
    }

    public void remove(TelescopeRecord t) {
        db.delete(TABLE_NAME, _ID + "=" + t.id, null);
    }

    public void removeAll() {
        db.delete(TABLE_NAME, null, null);
    }

}

