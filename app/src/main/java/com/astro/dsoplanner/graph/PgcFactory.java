package com.astro.dsoplanner.graph;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Global;


import com.astro.dsoplanner.SearchRules;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.CustomObject;
import com.astro.dsoplanner.base.PgcObject;
import com.astro.dsoplanner.expansion.APKExpansion;
import com.astro.dsoplanner.base.Point;


public class PgcFactory implements StarFactory {

    private static final String PGC = "PGC";

    private static final String TAG = PgcFactory.class.getSimpleName();
    RandomAccessFile db;

    public void open() throws IOException {
        db = new RandomAccessFile(APKExpansion.getExpPatchPath(Global.getAppContext(), Global.patchVersion), "r");

    }

    public AstroObject get(int pos) throws IOException {
        long offset = SettingsActivity.getPgcOffset();
        if (offset == -1)
            return null;
        db.seek(offset + pos * 18);
        double ra = db.readFloat();
        double dec = db.readFloat();
        int m = db.readByte();
        if (m < 0) m += 256;
        double mag = m / 10f;
        if (mag == 0) mag = Double.NaN;
        double a = db.readShort() / 100f;
        if (a == 0) a = Double.NaN;
        double b = db.readShort() / 100f;
        if (b == 0) b = Double.NaN;
        int pa = db.readByte();
        if (pa < 0) pa += 256;
        int id = db.readInt();
        String name = PGC + id;
        CustomObject obj = new PgcObject(AstroCatalog.PGC_CATALOG, id, ra, dec, AstroTools.getConstellation(ra, dec), AstroObject.Gxy, "", a == 0 ? Double.NaN : a, b == 0 ? Double.NaN : b, mag == 0 ? Double.NaN : mag, pa, name, name, "");
        return obj;
    }

    public List<Point> get(int q, double mag_limit) throws IOException {
        ArrayList<Point> as = new ArrayList<Point>();

        long offset = SettingsActivity.getPgcOffset();
        if (offset == -1)
            return as;

        boolean layer_vis = SettingsActivity.showVisibleStatus();
        double threshold = SettingsActivity.getLayerVisibilityThreshold();

        if (Global.BASIC_VERSION) return as;
        int pos = Global.pgcQ.get(q);
        int length = Global.pgcQ.get(q + 1) - pos;
        for (int i = 0; i < length; i++) {
            db.seek(offset + (i + pos) * 18);
            double ra = db.readFloat();
            double dec = db.readFloat();
            int m = db.readByte();
            if (m < 0) m += 256;
            double mag = m / 10f;
            if (mag == 0) mag = Double.NaN;
            double a = db.readShort() / 100f;
            if (a == 0) a = Double.NaN;
            double b = db.readShort() / 100f;
            if (b == 0) b = Double.NaN;
            int pa = db.readByte();
            if (pa < 0) pa += 256;
            int id = db.readInt();

            boolean include = false;
            if (layer_vis) {
                if (Double.isNaN(a) || Double.isNaN(b) || Double.isNaN(mag))
                    include = false;
                else
                    include = AstroTools.CheckVisibility(a, b, mag, threshold, SearchRules.EMPTY_RULE_INCLUDE, AstroObject.Gxy);

            } else {
                include = (mag <= mag_limit || (StarMags.PGC_MAX == mag_limit && Double.isNaN(mag)));

            }


            if (include) {

                String name = PGC + id;
                CustomObject obj = new PgcObject(AstroCatalog.PGC_CATALOG, id, ra, dec, AstroTools.getConstellation(ra, dec), AstroObject.Gxy, "", a == 0 ? Double.NaN : a, b == 0 ? Double.NaN : b, mag == 0 ? Double.NaN : mag, pa, name, name, "");
                obj.setLayerFlag();
                as.add(obj);
            }
        }
        return as;
    }

    /**
     * looks for the specified pgc by looking through the whole db
     * very slow
     *
     * @param id PGC num
     * @return
     * @throws IOException
     */
    public PgcObject find(int id) throws IOException {

        if (Global.BASIC_VERSION) return null;

        long offset = SettingsActivity.getPgcOffset();
        if (offset == -1)
            return null;

        boolean found = false;
        int i = 0;

        try {
            while (!found) {
                db.seek(offset + i * 18 + 14);
                int num = db.readInt();
                if (id == num)
                    found = true;
                else
                    i++;
            }
        } catch (EOFException e) {

        }
        if (found) {
            db.seek(offset + i * 18);
            double ra = db.readFloat();
            double dec = db.readFloat();
            int m = db.readByte();
            if (m < 0) m += 256;
            double mag = m / 10f;
            double a = db.readShort() / 100f;
            double b = db.readShort() / 100f;
            int pa = db.readByte();
            if (pa < 0) pa += 256;
            int id2 = db.readInt();
            if (mag == 0) mag = StarMags.PGC_MAX;

            String name = PGC + id;
            PgcObject obj = new PgcObject(AstroCatalog.PGC_CATALOG, id2, ra, dec, AstroTools.getConstellation(ra, dec), AstroObject.Gxy, "", a, b, mag, pa, name, name, "", i);
            return obj;

        }
        return null;


    }

    public void close() throws IOException {
        db.close();
    }
}

