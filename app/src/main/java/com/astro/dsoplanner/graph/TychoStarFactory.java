package com.astro.dsoplanner.graph;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.base.TychoStar;
import com.astro.dsoplanner.startools.BitTools;
import com.astro.dsoplanner.startools.TychoObject;

public class TychoStarFactory implements StarFactory {
    private static final String TAG = TychoStarFactory.class.getSimpleName();

    RandomAccessFile db;//full db

    public void open() throws IOException {

        db = new RandomAccessFile(Global.tycNewDb, "r");

    }

    public AstroObject get(int pos) throws IOException {
        db.seek(pos * 13);
        byte[] buffer = new byte[5];
        double ra = db.readFloat();
        double dec = db.readFloat();
        db.read(buffer);
        TychoObject obj = new TychoObject(ra, dec, buffer);
        TychoStar star = new TychoStar(obj.ra, obj.dec, obj.mag, AstroTools.getConstellation(obj.ra, obj.dec), obj.tyc123index);
        return star;

    }

    public List<Point> get(int q, double mag_limit) throws IOException {
        ArrayList<Point> as = new ArrayList<Point>();

        RandomAccessFile dbu = db;
        List<Integer> reflist = Global.tycQ;


        int pos = reflist.get(q);
        int length = reflist.get(q + 1) - pos;
        for (int i = 0; i < length; i++) {
            dbu.seek((i + pos) * 13);
            byte[] buffer = new byte[5];
            double ra = dbu.readFloat();
            double dec = dbu.readFloat();
            dbu.read(buffer);
            TychoObject obj = new TychoObject(ra, dec, buffer);


            if (obj.mag <= mag_limit) {
                TychoStar star = new TychoStar(obj.ra, obj.dec, obj.mag, AstroTools.getConstellation(obj.ra, obj.dec), obj.tyc123index);
                as.add(star);
            }
        }
        return as;
    }

    /**
     * looks for the specified tycho star by looking through the whole db
     * very slow
     *
     * @param tyc1
     * @param tyc2
     * @param tyc3
     * @return
     * @throws IOException
     */
    public AstroObject find(int tyc1, int tyc2, int tyc3) throws IOException {
        int id = BitTools.convertTycToInt(tyc1, tyc2, tyc3);

        boolean found = false;
        int i = 0;

        try {
            while (!found) {
                db.seek(i * 13 + 8);
                int num = db.readInt();
                if (id == num)
                    found = true;
                else
                    i++;
            }
        } catch (EOFException e) {

        }
        if (found) {
            db.seek(i * 13);
            byte[] buffer = new byte[5];
            double ra = db.readFloat();
            double dec = db.readFloat();
            db.read(buffer);
            TychoObject obj = new TychoObject(ra, dec, buffer);

            return new TychoStar(obj.ra, obj.dec, obj.mag, AstroTools.getConstellation(obj.ra, obj.dec), obj.tyc123index);


        }
        return null;


    }

    public void close() throws IOException {
        db.close();
    }
}
