package com.astro.dsoplanner.graph;

import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.base.BoundaryPoint;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.graph.StarFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


public class ConBoundaryFactory implements StarFactory {

    RandomAccessFile db;//full db

    public void open() throws IOException {

        db = new RandomAccessFile(Global.conBoundaryDb, "r");

    }

    public List<Point> get(int con) throws IOException {
        ArrayList<Point> as = new ArrayList<Point>();
        RandomAccessFile dbu = db;
        int count = (int) dbu.length() / 18;
        int i = 0;
        try {
            while (i++ < count) {
                double ra1 = dbu.readFloat();
                double dec1 = dbu.readFloat();
                double ra2 = dbu.readFloat();
                double dec2 = dbu.readFloat();
                byte con2 = dbu.readByte();
                byte ignore = dbu.readByte();
                if (con2 == con) {
                    BoundaryPoint point = new BoundaryPoint(ra1, ra2, dec1, dec2, con2);
                    if (ignore == 1) point.setIgnoreFlag();
                    as.add(point);
                }
            }
        } catch (EOFException e) {

        }
        return as;
    }

    public List<Point> get(int q, double mag_limit) throws IOException {
        ArrayList<Point> as = new ArrayList<Point>();
        RandomAccessFile dbu = db;
        List<Integer> reflist = Global.conBoundaryQ;

        int pos = reflist.get(q);
        int length = reflist.get(q + 1) - pos;
        for (int i = 0; i < length; i++) {
            dbu.seek((i + pos) * 18);

            double ra1 = dbu.readFloat();
            double dec1 = dbu.readFloat();
            double ra2 = dbu.readFloat();
            double dec2 = dbu.readFloat();
            byte con = dbu.readByte();
            byte ignore = dbu.readByte();
            BoundaryPoint point = new BoundaryPoint(ra1, ra2, dec1, dec2, con);
            if (ignore == 1) point.setIgnoreFlag();
            as.add(point);
        }
        return as;
    }

    public void close() throws IOException {
        db.close();
    }

}
