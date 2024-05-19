package com.astro.dsoplanner;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import android.os.Bundle;

import com.astro.dsoplanner.base.AstroObject;

class NoteRequest extends Request {

    private static final String NOTE_RECORD = "noteRecord";
    private static final String ACTION3 = "action";
    private static final String BYTE_ARRAY = "byteArray";
    private static final String OBJ_TYPE = "objType";
    private static final String RECORD2 = ", record=";
    private static final String ACTION2 = ", action=";
    private static final String NOTE_REQUEST_OBJ = "NoteRequest [obj=";

    static final int NEW_NOTE_ACTION = 1;
    static final int EDIT_NOTE_ACTION = 2;
    static final int GET_OBJECT_NOTES = 3;
    static final int GET_ALL_NOTES = 4;
    static final int SEARCH_NOTES = 5;
    static final int GET_OBJECT_NOTES_BY_NAME = 6;

    public int getKind() {
        return NOTE_REQUEST;
    }

    AstroObject obj = null;

    int action = 0;
    NoteRecord record = null;

    public NoteRequest(AstroObject obj, long date, String path) {        //new note
        this.obj = obj;
        record = new NoteRecord();
        record.date = date;
        record.path = path;
        action = NEW_NOTE_ACTION;
    }

    public NoteRequest(AstroObject obj, int action) {        //get object Notes,get all notes
        this.obj = obj;
        this.action = action;
        record = new NoteRecord();
    }

    public NoteRequest(NoteRecord record) {    //edit note
        this.record = record;
        action = EDIT_NOTE_ACTION;
    }

    public NoteRequest(String searchString) {//search notes
        action = SEARCH_NOTES;
        record = new NoteRecord();
        record.name = searchString;//used for passing searchString
    }


    @Override
    public String toString() {
        return NOTE_REQUEST_OBJ + obj + ACTION2 + action + RECORD2
                + record + "]";
    }

    public NoteRequest(Bundle b) {
        int objType = b.getInt(OBJ_TYPE);
        byte[] objrep = b.getByteArray(BYTE_ARRAY);
        if (objrep != null) {
            DataInputStream data = new DataInputStream(new ByteArrayInputStream(objrep));
            Object o = ObjectInflater.getInflater().inflate(objType, data);
            if (o instanceof AstroObject)
                obj = (AstroObject) o;
        }

        action = b.getInt(ACTION3);
        Bundle nr = b.getBundle(NOTE_RECORD);
        if (nr != null)
            record = new NoteRecord(nr);
        else
            record = new NoteRecord();
    }

    public Bundle getBundle() {
        Bundle b = new Bundle();
        byte[] objrep = null;
        int classType = 0;
        if (obj != null) {
            objrep = obj.getByteRepresentation();
            classType = obj.getClassTypeId();
        }

        b.putByteArray(BYTE_ARRAY, objrep);
        b.putInt(OBJ_TYPE, classType);

        b.putInt(ACTION3, action);
        b.putBundle(NOTE_RECORD, record.getBundle());
        return b;

    }

    public NoteRecord getRecord() {
        return record;
    }


}