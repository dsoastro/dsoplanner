package com.astro.dsoplanner.database;

import static android.provider.BaseColumns._ID;
import static com.astro.dsoplanner.Constants.A;
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

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.Global;

/**
 * class for adding name field
 *
 * @author leonid
 */
public class NgcicDatabaseTemp extends NgcicDatabase {
    private static final String TAG = NgcicDatabaseTemp.class.getSimpleName();
    public static final String NAME1 = "name1";

    private class NGCData1 extends SQLiteOpenHelper {
        static final String DATABASE_NAME = "ngcictmp.db";
        private static final int DATABASE_VERSION = 1;


        public NGCData1(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String s = "";
            s = "CREATE TABLE " + TABLE_NAME + " ("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + NAME + " INTEGER,"
                    + RA + " FLOAT,"
                    + DEC + " FLOAT,"
                    + MAG + " FLOAT,"
                    + A + " FLOAT,"
                    + B + " FLOAT,"
                    + CONSTEL + " INTEGER,"
                    + TYPE + " INTEGER,"
                    + PA + " FLOAT,"
                    + MESSIER + " INTEGER,"
                    + CALDWELL + " INTEGER,"
                    + HERSHELL + " INTEGER," + NAME1 + " TEXT);";

            db.execSQL(s);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    NGCData1 ngc1;

    public NgcicDatabaseTemp(Context acontext) {
        context = acontext;
        ngc1 = new NGCData1(context);
    }

    public void open(ErrorHandler eh) {
        try {
            db = ngc1.getReadableDatabase();
        } catch (Exception e) {
            ErrorHandler.ErrorRec rec = new ErrorHandler.ErrorRec(ErrorHandler.SQL_DB, "Error opening database");
            eh.addError(rec);
        }
    }

    public void add(ContentValues values, ErrorHandler eh) {
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
            ErrorHandler.ErrorRec rec = new ErrorHandler.ErrorRec(ErrorHandler.SQL_DB, "Error adding an object");
            eh.addError(rec);
        }

    }

    /**
     * helper method for adding additional field into ngcic db
     *
     * @param context
     */
    public static void fillDb(Context context) {
        NgcicDatabase ngc = new NgcicDatabase(context);
        ErrorHandler eh = new ErrorHandler();
        ngc.open(eh);
        if (eh.hasError()) {
            Log.d(TAG, "error=" + eh);
            return;
        }
        NgcicDatabaseTemp ngctemp = new NgcicDatabaseTemp(context);
        ngctemp.open(eh);
        if (eh.hasError()) {
            Log.d(TAG, "error=" + eh);
            return;
        }
        Cursor cursor = ngc.getAll();
        int i = 0;
        ngctemp.db.execSQL("begin transaction;");
        while (cursor.moveToNext()) {
            ContentValues values = ngc.getContentValuesFromCursor(cursor);
            ngctemp.add(values, eh);
            if (eh.hasError()) {
                Log.d(TAG, "error=" + eh);

            }
            Log.d(TAG, "i=" + i++);
        }
        ngctemp.db.execSQL("commit;");
        ngc.close();
        ngctemp.close();
        File db = context.getDatabasePath("ngcic.db");
        AstroTools.FileToCopy fc = new AstroTools.FileToCopy(db.getParent(), NGCData1.DATABASE_NAME, Global.dsoPath);
        boolean result = fc.copy();
        Log.d(TAG, "copy result=" + result);
    }

}
