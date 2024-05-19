package com.astro.dsoplanner;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;

import com.astro.dsoplanner.base.HrStar;
import com.astro.dsoplanner.database.Db;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;


public class FileTools {

    private static final String TAG = FileTools.class.getSimpleName();

    public static boolean copyHrDatabase(Context context) {
        //copying HR stardatabase into memory for quick access and drawing
        Global.databaseHr = new HrStar[9096];
        int i = 0;
        int hr = 0;
        double ra = 0;
        double dec = 0;
        double mag = 0;
        int fl = 0;
        int bay = 0;
        int con = 0;


        try {
            DataInputStream r = new DataInputStream(new BufferedInputStream(context.getResources().openRawResource(R.raw.hr)));
            while (true) {
                hr = r.readShort();
                ra = r.readDouble();
                dec = r.readDouble();
                mag = r.readDouble();
                fl = r.readShort();
                bay = r.readShort();
                con = r.readShort();

                HrStar st = new HrStar(hr, ra, dec, mag, fl, bay, con);
                Global.databaseHr[i] = st;
                i++;
                if (i > 9095) {
                    r.close();
                    break;
                }
            }

        } catch (Exception e) {
            Log.d(TAG, "Exception=" + e);
            return false;
        }

        copyComps(context);

        return true;
    }

    public static void copyComps(Context context) {
        Db db = new Db(context, Constants.SQL_DATABASE_COMP_DB);
        try {
            db.open();
            Cursor cursor = db.rawQuery("select * from components");


            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String comp = cursor.getString(1);
                float sep = cursor.getFloat(2);
                HrStar star = AstroTools.getHrStar(name);
                if (star != null) {
                    star.setData(comp, sep);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
        } finally {
            try {
                db.close();
            } catch (Exception e) {
            }
        }
    }

    public static String getDisplayName(Context context, Uri uri) {
        String name = "";
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
        } catch (Exception e) {
            return name;
        }
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            name = cursor.getString(nameIndex);
            cursor.close();
        } else { //file path uri
            name = uri.getLastPathSegment();
        }
        //Cut file extension if any
        int i = name.lastIndexOf(".");
        if (i > 0) {
            return name.substring(0, i);
        }
        return name;
    }
}
