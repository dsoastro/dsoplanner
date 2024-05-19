package com.astro.dsoplanner;

import static android.provider.BaseColumns._ID;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EyepiecesDatabase implements EyepiecesCatalog {
    private static final String TAG = EyepiecesDatabase.class.getSimpleName();
//	private static final String PATH="path";

    private static final String TABLE_NAME = "epmain";
    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";
    private static final String TEXT2 = " TEXT );";
    private static final String DOUBLE = " DOUBLE, ";
    private static final String TEXT = " TEXT, ";
    private static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    private static final String CREATE_TABLE = "CREATE TABLE ";

    private static final String DATABASE_NAME = Constants.EYEPIECES_DATABASE_NAME;//"eyepiecesmain.db" ;


    private Eyepieces1Data ep1;

    SQLiteDatabase db;
    Context context;

    private class Eyepieces1Data extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;

        public Eyepieces1Data(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String s = "";
            s = CREATE_TABLE + TABLE_NAME + " (" + _ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT + EyepiecesRecord.ENAME + TEXT + EyepiecesRecord.EFOCUS + DOUBLE + EyepiecesRecord.EAFOV + DOUBLE + EyepiecesRecord.EEP + TEXT + EyepiecesRecord.EDESCR + TEXT2;

            db.execSQL(s);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TABLE_IF_EXISTS + TABLE_NAME);
            onCreate(db);
        }
    }

    public EyepiecesDatabase(Context acontext) {
        context = acontext;
        ep1 = new Eyepieces1Data(context);
    }

    public EyepiecesDatabase() {
        this(Global.getAppContext());
    }

    public void open() {
        try {
            db = ep1.getReadableDatabase();
        } catch (Exception ignored) {
        }
    }

    public void close() {
        try {
            ep1.close();
        } catch (Exception ignored) {
        }
    }

    private EyepiecesRecord getObjectFromCursor(Cursor cursor) { //should be called after cursor.moveToNext called
        return new EyepiecesRecord(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3), cursor.getString(4), cursor.getString(5));
    }

    public List<EyepiecesRecord> search() {
        List<EyepiecesRecord> list = new ArrayList<EyepiecesRecord>();
        if (db == null) return list;

        String query = null;
        Cursor cursor = db.query(TABLE_NAME, null, query, null, null, null, null);
        while (cursor.moveToNext()) {
            EyepiecesRecord nr = getObjectFromCursor(cursor);
            if (cursor != null) list.add(nr);
        }

        return list;
    }

    public EyepiecesRecord getEyepiecesRecord(int id) { //id in notedatabase
        Cursor cursor = db.query(TABLE_NAME, null, _ID + "=" + id, null, null, null, null);
        if (cursor.moveToNext()) return (getObjectFromCursor(cursor));
        return null;
    }

    public long add(String name, double fo, double afov, String epid, String note) {
        Log.d(TAG, "adding " + name);
        ContentValues v = new ContentValues();
        v.put(EyepiecesRecord.ENAME, name);
        v.put(EyepiecesRecord.EFOCUS, fo);
        v.put(EyepiecesRecord.EAFOV, afov);
        v.put(EyepiecesRecord.EEP, epid);
        v.put(EyepiecesRecord.EDESCR, note);

        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_NAME, null, v);
        } catch (Exception ignored) {
        }
        return result;
    }

    public long add(EyepiecesRecord t) {
        Log.d(TAG, "adding " + t);
        ContentValues v = new ContentValues();
        v.put(EyepiecesRecord.ENAME, t.getName());
        v.put(EyepiecesRecord.EFOCUS, t.getFocus());
        v.put(EyepiecesRecord.EAFOV, t.getAfov());
        v.put(EyepiecesRecord.EEP, "");
        v.put(EyepiecesRecord.EDESCR, t.getDatabaseNote());

        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_NAME, null, v);
        } catch (Exception ignored) {
        }
        return result;
    }

    public void edit(EyepiecesRecord t) {
        ContentValues v = new ContentValues();
        v.put(EyepiecesRecord.ENAME, t.getName());
        v.put(EyepiecesRecord.EFOCUS, t.getFocus());
        v.put(EyepiecesRecord.EAFOV, t.getAfov());
        v.put(EyepiecesRecord.EEP, t.getEp_id());
        v.put(EyepiecesRecord.EDESCR, t.getDatabaseNote());
        try {
            db.update(TABLE_NAME, v, _ID + "=" + t.getId(), null);
        } catch (Exception ignored) {
        }
    }

    public void remove(EyepiecesRecord t) {
        db.delete(TABLE_NAME, _ID + "=" + t.getId(), null);
    }

    public void removeAll() {
        db.delete(TABLE_NAME, null, null);
    }
}

interface EyepiecesCatalog {
    public List<EyepiecesRecord> search();

    public void open();

    public void close();

    public long add(String name, double fo, double afov, String epid, String note);

    public void edit(EyepiecesRecord rec);

    public void remove(EyepiecesRecord rec);
}