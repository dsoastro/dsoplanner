package com.astro.dsoplanner;

import static com.astro.dsoplanner.Constants.*;

import java.util.Calendar;
import java.util.HashMap;

import android.os.Bundle;

import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.Exportable;
import com.astro.dsoplanner.database.NoteDatabase;


public class NoteRecord implements Exportable {

    private static final String NAME3 = ", name=";
    private static final String PATH3 = ", path=";
    private static final String NOTE3 = ", note=";
    private static final String DATE3 = ", date=";
    private static final String NOTE_DATABASE2 = ", noteDatabase=";
    private static final String NOTEBASE_ID = ", notebaseId=";
    private static final String CATALOG3 = ", catalog=";
    private static final String NOTE_RECORD_ID = "NoteRecord [id=";
    private static final String NAME2 = "name";
    private static final String PATH2 = "path";
    private static final String NOTE2 = "note";
    private static final String DATE2 = "date";
    private static final String NOTE_DATABASE = "noteDatabase";
    private static final String NOTE_BASE_ID = "noteBaseId";
    private static final String CATALOG2 = "catalog";
    private static final String ID2 = "id";


    public int id;//id in catalog
    public int catalog;
    public int notebaseId;//id in noteDatabase
    public int noteDatabase;
    public long date;
    public String note = "";
    public String path = "";//path to voice note
    public String name = "";

    public NoteRecord() {

    }

    public NoteRecord(int id, int catalog, int notebaseId, int noteDatabase,
                      long date, String note, String path, String name) {

        this.id = id;
        this.catalog = catalog;
        this.notebaseId = notebaseId;
        this.noteDatabase = noteDatabase;
        this.date = date;
        this.note = note;
        this.path = path;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof NoteRecord) {
            NoteRecord rec = (NoteRecord) o;
            if (rec.notebaseId == this.notebaseId)
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return notebaseId;
    }

    public NoteRecord(Bundle b) {
        id = b.getInt(ID2);
        catalog = b.getInt(CATALOG2);
        notebaseId = b.getInt(NOTE_BASE_ID);
        noteDatabase = b.getInt(NOTE_DATABASE);
        date = b.getLong(DATE2);
        note = b.getString(NOTE2);
        path = b.getString(PATH2);
        name = b.getString(NAME2);

    }

    public Bundle getBundle() {
        Bundle b = new Bundle();
        b.putInt(ID2, id);
        b.putInt(CATALOG2, catalog);
        b.putInt(NOTE_BASE_ID, notebaseId);
        b.putInt(NOTE_DATABASE, noteDatabase);
        b.putLong(DATE2, date);
        b.putString(NOTE2, note);
        b.putString(PATH2, path);
        b.putString(NAME2, name);
        return b;
    }

    @Override
    public String toString() {
        return NOTE_RECORD_ID + id + CATALOG3 + catalog
                + NOTEBASE_ID + notebaseId + NOTE_DATABASE2
                + noteDatabase + DATE3 + date + NOTE3 + note
                + PATH3 + path + NAME3 + name + "]";
    }

    public HashMap<String, String> getStringRepresentation() {
        HashMap<String, String> map = new HashMap<String, String>();

        map.put(PATH, path);
        map.put(NOTE, note);
        map.put(NOTEBASEID, "" + notebaseId);
        map.put(NOTEDATABASE, "" + noteDatabase);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);
        String dateStr = DetailsActivity.makeDateString(c, true);
        map.put(DATE, dateStr);

        String timeStr = DetailsActivity.makeTimeString(c, true);
        map.put(TIME, timeStr);


        map.put(NAME, name);

        AstroObject obj = new NoteDatabase().getObject(this, new ErrorHandler());
        if (obj != null) {
            map.putAll(obj.getStringRepresentation());
        }
        return map;
    }

    public int getClassTypeId() {
        return Exportable.NOTE_RECORD;
    }

    public byte[] getByteRepresentation() {
        throw new UnsupportedOperationException();
    }


}

