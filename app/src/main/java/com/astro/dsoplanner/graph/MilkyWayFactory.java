package com.astro.dsoplanner.graph;

import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.base.BoundaryPoint;
import com.astro.dsoplanner.base.Point;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class MilkyWayFactory implements StarFactory {

    RandomAccessFile db;//full db

    public void open() throws IOException {

        db = new RandomAccessFile(Global.milkyWayDb, "r");

    }

    public List<Point> get(int q, double mag_limit) throws IOException {
        ArrayList<Point> as = new ArrayList<Point>();

        RandomAccessFile dbu = db;
        List<Integer> reflist = Global.milkyWayQ;

        int pos = reflist.get(q);//Global.tycQ.get(q);
        int length = reflist.get(q + 1) - pos;//Global.tycQ.get(q+1)-pos;
        for (int i = 0; i < length; i++) {
            dbu.seek((i + pos) * 16);

            double ra1 = dbu.readFloat();
            double dec1 = dbu.readFloat();
            double ra2 = dbu.readFloat();
            double dec2 = dbu.readFloat();


            BoundaryPoint point = new BoundaryPoint(ra1, ra2, dec1, dec2, (byte) 0);
            as.add(point);
        }
        return as;
    }

    public void close() throws IOException {
        db.close();
    }

}
