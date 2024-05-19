package com.astro.dsoplanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.CustomObject;
import com.astro.dsoplanner.base.CustomObjectLarge;
import com.astro.dsoplanner.base.DoubleStarObject;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.base.Fields;
import com.astro.dsoplanner.Global;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.provider.BaseColumns._ID;
import static com.astro.dsoplanner.Constants.A;
import static com.astro.dsoplanner.Constants.B;
import static com.astro.dsoplanner.Constants.COMMENT;
import static com.astro.dsoplanner.Constants.CONSTEL;
import static com.astro.dsoplanner.Constants.DEC;
import static com.astro.dsoplanner.Constants.MAG;
import static com.astro.dsoplanner.Constants.PA;
import static com.astro.dsoplanner.Constants.RA;
import static com.astro.dsoplanner.Constants.TYPE;

public class CustomDatabaseLarge extends CustomDatabase {

    private static final String ASC2 = " ASC";
    private static final String LIKE2 = " LIKE ";
    private static final String OR2 = " OR ";
    private static final String ERROR_ADDING_AN_OBJECT2 = "Error adding an object";
    private static final String TEXT4 = " TEXT";
    private static final String TEXT3 = " TEXT, ";
    private static final String INTEGER2 = " INTEGER, ";
    private static final String FLOAT2 = " FLOAT, ";
    private static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT2 = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    private static final String CREATE_TABLE2 = "CREATE TABLE ";


    private static final String TAG = CustomDatabaseLarge.class.getSimpleName();

    protected class CustomDataLarge extends CustomData {
        DbListItem.FieldTypes types;

        public CustomDataLarge(Context ctx, String name, DbListItem.FieldTypes types) {
            super(ctx, name);
            this.types = types;
        }

        @Override

        public void onCreate(SQLiteDatabase db) {
            String s = "";
            s = CREATE_TABLE2 + TABLE_NAME + " (" + _ID
                    + INTEGER_PRIMARY_KEY_AUTOINCREMENT2 + DEC + FLOAT2 + RA + FLOAT2
                    + A + FLOAT2
                    + MAG + FLOAT2 + B + FLOAT2 + CONSTEL + INTEGER2 +
                    TYPE + INTEGER2 + PA + FLOAT2 + TYPESTR + TEXT3 + NAME1 + TEXT3 + NAME2 + TEXT3 + COMMENT + TEXT4
                    + getFieldStr();
            Log.d(TAG, "on create string=" + s);

            db.execSQL(s);

        }

        private String getFieldStr() {
            String[] names = new String[]{"", TEXT3, FLOAT2, INTEGER2, TEXT3, TEXT3};//representation of String, Double, Int, Photo,Url
            String s = "";
            for (Map.Entry<String, DbListItem.FieldTypes.TYPE> e : types.getNameTypeMap().entrySet()) {
                s = s + e.getKey() + names[e.getValue().id];
            }
            if (s.length() > 2) {
                s = s.substring(0, s.length() - 2);//removing ,
                s = ", " + s + ");";
                Log.d(TAG, "field string=" + s);
                return s;
            }
            return ");";
        }
    }

    public DbListItem.FieldTypes types;

    public CustomDatabaseLarge(Context context, String dbname, int catalog, DbListItem.FieldTypes types) {
        super(context, dbname, catalog);
        custom = new CustomDataLarge(context, dbname, types);
        this.types = types;
    }

    @Override
    public AstroObject getObjectFromCursor(Cursor cursor) { //should be called after cursor.moveToNext called
        String[] sarr = cursor.getColumnNames();

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

        int ref = 0;
        if (internalDeepSky) {

            try {
                ref = cursor.getInt(REF_POS);

            } catch (Exception e) {

            }
        }


        Fields field = new Fields();
        try {
            for (Map.Entry<String, DbListItem.FieldTypes.TYPE> e : types.getNameTypeMap().entrySet()) {
                switch (e.getValue()) {
                    case STRING:
                        String fNameS = e.getKey();

                        int colind = cursor.getColumnIndex(fNameS);
                        String valueS = cursor.getString(colind);
                        if (valueS == null) valueS = "";
                        field.put(e.getKey(), valueS);
                        break;
                    case DOUBLE:
                        String fNameD = e.getKey();
                        double valueD = cursor.getFloat(cursor.getColumnIndex(fNameD));
                        field.put(e.getKey(), valueD);
                        break;

                    case INT:
                        String fNameI = e.getKey();
                        int valueI = cursor.getInt(cursor.getColumnIndex(fNameI));
                        field.put(e.getKey(), valueI);
                        break;
                    case PHOTO:
                        fNameS = e.getKey();
                        Log.d(TAG, "fnameS=" + fNameS);
                        Log.d(TAG, "fnameSindex=" + cursor.getColumnIndex(fNameS));
                        valueS = cursor.getString(cursor.getColumnIndex(fNameS));
                        Log.d(TAG, "valueS=" + valueS);
                        field.put(e.getKey(), new Fields.Photo(valueS));
                        break;
                    case URL:
                        fNameS = e.getKey();
                        valueS = cursor.getString(cursor.getColumnIndex(fNameS));
                        field.put(e.getKey(), new Fields.Url(valueS));
                        break;
                }
            }
        } catch (Exception e) {
        }
        AstroObject obj;
        if (catalog == AstroCatalog.HAAS || catalog == AstroCatalog.WDS) {
            obj = new DoubleStarObject(catalog, id, ra, dec, con, type, typestr, a,
                    b, mag, pa, name1, name2, comment, field);
        } else {
            obj = new CustomObjectLarge(catalog, id, ra, dec, con, type, typestr, a,
                    b, mag, pa, name1, name2, comment, field);
        }

        if (ref != 0) {
            obj.ref = ref;
        }
        return obj;


    }

    @Override
    public long add(AstroObject obj, ErrorHandler eh) {
        CustomObjectLarge ob;
        if (!(obj instanceof CustomObjectLarge))
            return -1;
        else
            ob = (CustomObjectLarge) obj;


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

        if (Double.isNaN(ob.pa))
            values.putNull(PA);
        else
            values.put(PA, ob.pa);

        values.put(TYPESTR, ob.typeStr);
        values.put(NAME1, ob.getShortName());
        values.put(NAME2, ob.getLongName());
        values.put(COMMENT, ob.comment);
        Fields field = ob.getFields();
        for (Map.Entry<String, String> e : field.getStringMap().entrySet()) {
            values.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Double> e : field.getDoubleMap().entrySet()) {
            values.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Integer> e : field.getIntMap().entrySet()) {
            values.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, String> e : field.getPhotoMap().entrySet()) {
            values.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, String> e : field.getUrlMap().entrySet()) {
            values.put(e.getKey(), e.getValue());
        }
        long result = -1;
        boolean error = false;
        try {
            result = db.insertOrThrow(TABLE_NAME, null, values);
        } catch (Exception e) {
            error = true;

            Log.d(TAG, "exception=" + e);
        }
        if (result == -1) {
            error = true;
        }
        if (error) {
            ErrorHandler.ErrorRec rec = new ErrorHandler.ErrorRec(ErrorHandler.SQL_DB, ERROR_ADDING_AN_OBJECT2);
            eh.addError(rec);
        }
        return result;
    }

    @Override
    public long edit(CustomObject obj) {
        CustomObjectLarge ob;
        if (!(obj instanceof CustomObjectLarge))
            return -1;
        else
            ob = (CustomObjectLarge) obj;


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

        values.put(TYPESTR, ob.typeStr);
        values.put(NAME1, ob.getShortName());
        values.put(NAME2, ob.getLongName());
        values.put(COMMENT, ob.comment);
        Fields field = ob.getFields();
        for (Map.Entry<String, String> e : field.getStringMap().entrySet()) {
            values.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Double> e : field.getDoubleMap().entrySet()) {
            values.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, Integer> e : field.getIntMap().entrySet()) {
            values.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, String> e : field.getPhotoMap().entrySet()) {
            values.put(e.getKey(), e.getValue());
        }
        for (Map.Entry<String, String> e : field.getUrlMap().entrySet()) {
            values.put(e.getKey(), e.getValue());
        }
        long result = -1;
        try {
            result = db.update(TABLE_NAME, values, _ID + "=" + obj.getId(), null);
        } catch (Exception e) {
            Log.d(TAG, "exception=" + e);
        }
        return result;
    }

    public List<AstroObject> searchTextFields(String s) {
        List<AstroObject> list = new ArrayList<AstroObject>();
        String query = "";

        for (String field : types.getStringFields()) {
            if (!"".equals(query))
                query += OR2 + field + LIKE2 + "'" + s + "'";
            else
                query += field + LIKE2 + "'" + s + "'";
        }

        if ("".equals(query)) return list;
        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, query, null, null,
                    null, NAME1 + ASC2);
        } catch (SQLiteException e) {
            return list;
        }


        while (cursor.moveToNext()) {

            list.add(getObjectFromCursor(cursor));
            if (list.size() > Global.SQL_SEARCH_LIMIT) {
                break;
            }

        }
        cursor.close();
        return list;
    }
}
