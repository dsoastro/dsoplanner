package com.astro.dsoplanner.graph;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.base.TychoStar;
import com.astro.dsoplanner.startools.TychoObject;

public class TychoStarFactoryShort implements StarFactory {
    private static final String TAG = TychoStarFactoryShort.class.getSimpleName();
    RandomAccessFile dbs;//short db

    public void open() throws IOException {
        dbs = new RandomAccessFile(Global.tycNewDbShort, "r");
    }

    public List<Point> get(int q, double mag_limit) throws IOException {
        ArrayList<Point> as = new ArrayList<Point>();

        RandomAccessFile dbu = dbs;
        List<Integer> reflist = Global.tycQshort;

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

    public void close() throws IOException {

        dbs.close();
    }
}
