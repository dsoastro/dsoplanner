package com.astro.dsoplanner.database;

import static android.provider.BaseColumns._ID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.ImportDatabaseIntentService;
import com.astro.dsoplanner.NoteRecord;


import com.astro.dsoplanner.base.Planet;
import com.astro.dsoplanner.StarData;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.HrStar;

public class NoteDatabase implements NoteCatalog {


    private static final String SELECT_NAME_FROM_NOTEMAIN_WHERE = "select name from notemain where ";

    private static final String OR_NAME_LIKE = " or name like '";
    private static final String OR = " or ";
    private static final String NAME_LIKE = "name like '";
    private static final String A_Z_A_Z = "[a-zA-Z]+";
    private static final String A_Z_A_Z_0_9_A_Z_A_Z = "[a-zA-Z]+[0-9]+[a-zA-Z]*";
    private static final String ERROR_ADDING_A_NOTE = "Error adding a note";
    private static final String LIKE = " LIKE ";
    private static final String AND = " AND ";
    private static final String ERROR_OPENING_DATABASE = "Error opening database";

    private static final String CATALOG = "catalog";//eg AstroCatalog.NGCIC
    private static final String CATALOGID = "catalogid";//eg row in NGCIC
    private static final String TABLE_NAME = "notemain";
    private static final String TEXT_NOTE = "textnote";
    private static final String DATE = "date";
    private static final String PATH = "path";
    private static final String NAME = "name";
    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";
    private static final String TEXT2 = " TEXT);";
    private static final String INTEGER = " INTEGER, ";
    private static final String LONG = " LONG, ";
    private static final String TEXT = " TEXT, ";
    private static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    private static final String CREATE_TABLE = "CREATE TABLE ";

    private static final String TAG = NoteDatabase.class.getSimpleName();

    private class Note1Data extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;


        public Note1Data(Context ctx) {
            super(ctx, Constants.NOTE_DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String s = "";
            s = CREATE_TABLE + TABLE_NAME + " (" + _ID
                    + INTEGER_PRIMARY_KEY_AUTOINCREMENT + TEXT_NOTE + TEXT + DATE + LONG + PATH + TEXT + CATALOG
                    + INTEGER + CATALOGID + INTEGER + NAME + TEXT2;
            db.execSQL(s);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            db.execSQL(DROP_TABLE_IF_EXISTS + TABLE_NAME);
            onCreate(db);
        }
    }

    private Note1Data note1;
    public SQLiteDatabase db;
    Context context;

    public NoteDatabase(Context acontext) {
        context = acontext;
        note1 = new Note1Data(context);

    }

    public NoteDatabase() {
        this(Global.getAppContext());
    }

    public void open(ErrorHandler eh) {
        if (ImportDatabaseIntentService.isBeingImported(Constants.NOTE_DATABASE_NAME)) {
            eh.addError(new ErrorHandler.ErrorRec(ErrorHandler.WARNING, Global.DB_IMPORT_RUNNING));
            return;
        }

        try {
            db = note1.getReadableDatabase();
        } catch (Exception e) {
            ErrorHandler.ErrorRec rec = new ErrorHandler.ErrorRec(ErrorHandler.SQL_DB, ERROR_OPENING_DATABASE);
            eh.addError(rec);
        }
    }

    public void close() {
        try {
            note1.close();
        } catch (Exception e) {
        }
    }

    private NoteRecord getObjectFromCursor(Cursor cursor) { //should be called after cursor.moveToNext called
        int id = cursor.getInt(0);
        String note = cursor.getString(1);
        String path = cursor.getString(3);
        Long date = cursor.getLong(2);
        int catalog = cursor.getInt(4);
        int catalogid = cursor.getInt(5);
        String name = cursor.getString(6);
        NoteRecord obj = new NoteRecord(catalogid, catalog, id, NoteCatalog.MAIN_CATALOG, date, note, path, name);
        return obj;
    }

    /**
     * @param starttime
     * @param endtime
     * @return object names with in upper case
     */
    public Set<String> searchNames(long starttime, long endtime) {
        Set<String> set = new HashSet<String>();
        if (db == null) return set;
        String query = SELECT_NAME_FROM_NOTEMAIN_WHERE + DATE + ">" + starttime + AND + DATE + "<" + endtime;
        Cursor cursor;
        try {
            cursor = db.rawQuery(query, null);
        } catch (SQLiteException e) {
            return set;
        }
        if (cursor == null) return set;
        Log.d(TAG, "starttime=" + starttime + " endtime=" + endtime + "cursor size=" + cursor.getCount());
        while (cursor.moveToNext()) {
            set.add(cursor.getString(0).toUpperCase(Locale.US));
        }
        return set;
    }

    /**
     * searching for notes between starttime and endtime
     *
     * @param starttime
     * @param endtime
     * @return
     */
    public List<NoteRecord> search(long starttime, long endtime) {
        List<NoteRecord> list = new ArrayList<NoteRecord>();
        if (db == null) return list;
        String query = DATE + ">" + starttime + AND + DATE + "<" + endtime;
        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, query, null, null,
                    null, null);
        } catch (SQLiteException e) {
            return list;
        }
        if (cursor == null) return list;
        Log.d(TAG, "starttime=" + starttime + " endtime=" + endtime + "cursor size=" + cursor.getCount());
        while (cursor.moveToNext()) {
            NoteRecord nr = getObjectFromCursor(cursor);
            list.add(nr);
        }
        return list;
    }


    /**
     * use obj=null for full search
     * search for objects except for CUSTOM_CATALOG where there are
     * no strict correspondence between notes and objects. Use searchLike instead
     * for graph object search by catalog and long name - there should be strict correspondence
     */
    public List<NoteRecord> search(AstroObject obj) {  //obj==null full search

        List<NoteRecord> list = new ArrayList<NoteRecord>();
        if (db == null) return list;

        int catalogid = 0;
        int catalog = 0;
        if (obj != null) {
            catalogid = obj.getId();
            catalog = obj.getCatalog();
        }

        String query = null;
        if (obj != null)
            query = "(" + CATALOGID + "=" + catalogid + AND + CATALOG + "=" + catalog + ") or " + "(name like'" + obj.getCanonicName().replace("'", "''") + "')";

        Log.d(TAG, "query=" + query);
        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, query, null, null,
                    null, null);
        } catch (SQLiteException e) {
            return list;
        }
        while (cursor.moveToNext()) {
            NoteRecord nr = getObjectFromCursor(cursor);
            list.add(nr);
        }

        return list;

    }


    /**
     * Adds " " between letters and numbers as well
     *
     * @return the list of notes with the same name (case insensitive)
     */
    public List<NoteRecord> searchNameExact(String[] names) {
        String sql = "";
        for (String name : names) {

            sql = sql + OR + NAME_LIKE + name + "' ";
            if (name.matches(A_Z_A_Z_0_9_A_Z_A_Z)) {
                Pattern p = Pattern.compile(A_Z_A_Z);
                Matcher m = p.matcher(name);
                if (m.find()) {
                    String name2 = name.substring(0, m.end()) + " " + name.substring(m.end());
                    sql = sql + OR_NAME_LIKE + name2 + "' ";
                }
            }


        }
        sql = sql.substring(4);


        return search(sql);
    }

    /**
     * @param namepart - name of the object
     * @return the list of notes with the name containing namepart
     */
    public List<NoteRecord> searchNameInclusive(String namepart) {

        return search(NAME + LIKE + "'%" + namepart + "%'");
    }

    /**
     * @param content
     * @return the list of notes containing string content in their text (not name)
     */
    public List<NoteRecord> searchContentInclusive(String content) {

        return search(TEXT_NOTE + LIKE + "'%" + content + "%'");
    }

    /**
     * @param query - sql request
     * @return
     */
    private List<NoteRecord> search(String query) {
        List<NoteRecord> list = new ArrayList<NoteRecord>();
        if (db == null) return list;

        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, query, null, null,
                    null, null);
        } catch (SQLiteException e) {
            return list;
        }
        if (cursor == null) return list;
        while (cursor.moveToNext()) {
            NoteRecord nr = getObjectFromCursor(cursor);
            list.add(nr);
        }
        return list;
    }

    public NoteRecord getNoteRecord(int id) { //id in notedatabase
        Cursor cursor = db.query(TABLE_NAME, null, _ID + "=" + id, null, null,
                null, null);
        if (cursor.moveToNext())
            return (getObjectFromCursor(cursor));
        return null;
    }

    public AstroObject getObject(NoteRecord n, ErrorHandler eh) {
        if (n.catalog == AstroCatalog.CUSTOM_CATALOG || n.catalog == AstroCatalog.TYCHO_CATALOG ||
                n.catalog == AstroCatalog.UCAC4_CATALOG || n.catalog == AstroCatalog.PGC_CATALOG
                || n.catalog == AstroCatalog.CONTOUR_CATALOG)
            return null;
        if (n.catalog == AstroCatalog.NGCIC_CATALOG) {
            AstroCatalog cat = new NgcicDatabase(context);
            AstroObject obj = null;
            cat.open(eh);
            if (eh.hasError()) {

            } else {
                obj = cat.getObject(n.id);
                cat.close();
            }
            return obj;
        }
        if (n.catalog == AstroCatalog.PLANET_CATALOG) {
            for (Planet p : Global.planets)
                if (p.getPlanetType().ordinal() == n.id)
                    return p;
        }
        if (n.catalog == AstroCatalog.YALE_CATALOG) {
            return new HrStar(Global.databaseHr[StarData.ConvHrToRow(n.id)]);
        }
        //Custom catalogs

        DbListItem item = AstroTools.findItemByCatId(n.catalog);
        if (item == null)
            return null;
        AstroCatalog cat;
        if (item.ftypes.isEmpty())
            cat = new CustomDatabase(context, item.dbFileName, n.catalog);
        else
            cat = new CustomDatabaseLarge(context, item.dbFileName, n.catalog, item.ftypes);
        AstroObject obj = null;
        cat.open(eh);
        if (eh.hasError()) {

        } else {
            obj = cat.getObject(n.id);
            cat.close();
        }
        return obj;


    }

    public List<AstroObject> getObjects(List<NoteRecord> list, ErrorHandler eh) {
        List<AstroObject> result = new ArrayList<AstroObject>();
        for (NoteRecord n : list) {
            AstroObject obj = getObject(n, eh);
            if (obj != null) result.add(obj);
        }
        return result;
    }

    public long add(NoteRecord n, ErrorHandler eh) {
        ContentValues values = new ContentValues();
        values.put(CATALOG, n.catalog);
        values.put(CATALOGID, n.id);
        values.put(TEXT_NOTE, n.note);
        values.put(DATE, n.date);
        values.put(PATH, n.path);
        values.put(NAME, n.name);
        long result = -1;
        boolean error = false;
        try {
            result = db.insertOrThrow(TABLE_NAME, null, values);
        } catch (Exception e) {
            error = true;
        }
        if (result == -1)
            error = true;
        if (error) {
            ErrorHandler.ErrorRec rec = new ErrorHandler.ErrorRec(ErrorHandler.SQL_DB, ERROR_ADDING_A_NOTE);
            eh.addError(rec);
        }
        return result;
    }

    public long add(AstroObject obj, String note, long date, String path, String name) {//name used if astroObject=null
        if (obj == null) {
            ContentValues values = new ContentValues();
            values.put(CATALOG, AstroCatalog.CUSTOM_CATALOG);
            values.put(CATALOGID, 0);
            values.put(TEXT_NOTE, note);
            values.put(DATE, date);
            values.put(PATH, path);
            values.put(NAME, name);
            long result = -1;
            try {
                result = db.insertOrThrow(TABLE_NAME, null, values);
            } catch (Exception e) {
            }
            return result;
        }
        ContentValues values = new ContentValues();
        values.put(CATALOG, obj.getCatalog());
        values.put(CATALOGID, obj.getId());
        values.put(TEXT_NOTE, note);
        values.put(DATE, date);
        values.put(PATH, path);
        values.put(NAME, obj.getCanonicName());
        long result = -1;
        try {
            result = db.insertOrThrow(TABLE_NAME, null, values);
        } catch (Exception e) {
        }
        return result;
    }

    public void edit(NoteRecord n) {
        ContentValues values = new ContentValues();
        values.put(CATALOG, n.catalog);
        values.put(CATALOGID, n.id);
        values.put(TEXT_NOTE, n.note);
        values.put(DATE, n.date);
        values.put(PATH, n.path);
        values.put(NAME, n.name);
        try {
            db.update(TABLE_NAME, values, _ID + "=" + n.notebaseId, null);
        } catch (Exception e) {
        }
    }

    public void remove(NoteRecord n) {
        db.delete(TABLE_NAME, _ID + "=" + n.notebaseId, null);
    }

    public void removeAll() {
        db.delete(TABLE_NAME, null, null);
    }
}
