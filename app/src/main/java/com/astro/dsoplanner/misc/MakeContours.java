package com.astro.dsoplanner.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.astro.dsoplanner.base.ContourObject;
import com.astro.dsoplanner.database.Db;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListCollectionFiller;
import com.astro.dsoplanner.infolist.InfoListImpl;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListSaverImp;
import com.astro.dsoplanner.infolist.ListHolder;

/**
 * 1. run both makeContourList
 * 2. run printContours on the second run to get an updated data (in a file out.txt)
 * 2. correct AstroTools.getContourNumber
 *
 * @author leonid
 */
public class MakeContours {
    private static final String TAG = MakeContours.class.getSimpleName();


    public static void makeContourList() {
        File f = new File(Global.exportImportPath, "contour.txt");
        InfoList conlist = new InfoListImpl("Contour", ContourObject.class);
        ;
        int totalrec = 0;
        int i = 0;
        try {
            InputStream in = new FileInputStream(f);
            ContourImporter importer = new ContourImporter(in);

            while (i < 100) {
                ContourObject obj = importer.next();
                conlist.fill(new InfoListCollectionFiller(Arrays.asList(new ContourObject[]{obj})));
                i++;
                Log.d(TAG, "i=" + i);
                totalrec += obj.getDimension();
                Log.d(TAG, "obj=" + obj.getShortName() + " recs=" + obj.getDimension() + " total recs in contour=" + totalrec);
            }

        } catch (Exception e) {
            Log.d(TAG, "Exception at contour importing=" + e);
        }


        OutputStream out = null;
        try {

            f = new File(Global.exportImportPath, "contours");
            out = new FileOutputStream(f);
            InfoListSaver saver = new InfoListSaverImp(out);
            boolean result = conlist.save(saver);

        } catch (Exception e) {

        }
    }

    /**
     * print static initialisation for AstroTools.getContourNumber
     */
    public static void printContours(Context context) {
        InfoList list = ListHolder.getListHolder().get(InfoList.NEBULA_CONTOUR_LIST);
        int i = 0;

        try {
            Db db = new Db(context, "ngcic.db");
            db.open();

            PrintWriter pw = new PrintWriter(new FileOutputStream(new File(Global.exportImportPath, "out.txt")));
            for (Object o : list) {
                ContourObject p = (ContourObject) o;
                String name = p.getShortName().trim();
                int num = 0;
                Cursor cursor = db.rawQuery("select ref from ngcic where name1='" + name + "'");
                if (cursor.moveToNext()) {
                    num = cursor.getInt(0);
                }
                cursor.close();
                pw.println("mapcon.put(" + num + "," + i++ + ");");
                Log.d(TAG, "" + num + " " + i);
            }
            pw.close();
            db.close();

        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
        }
    }
}
