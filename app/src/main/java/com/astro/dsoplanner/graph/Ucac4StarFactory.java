package com.astro.dsoplanner.graph;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.Ucac4Star;
import com.astro.dsoplanner.expansion.APKExpansion;
import com.astro.dsoplanner.base.Point;

public class Ucac4StarFactory implements StarFactory {
    RandomAccessFile db;

    public void open() throws IOException {
        db = new RandomAccessFile(APKExpansion.getExpPath(Global.getAppContext(), Global.mainVersion), "r");
    }

    private int getZoneId(byte b1, byte b2, byte b3) {
        int bx = (b1 < 0 ? b1 + 256 : b1);//to avoid loosing digits when byte calculating
        int by = (b2 < 0 ? b2 + 256 : b2);
        int bz = (b3 < 0 ? b3 + 256 : b3);
        return (bx << 16 | by << 8 | bz);
    }

    public List<Point> get(int q, double mag_limit) throws IOException {
        ArrayList<Point> as = new ArrayList<Point>();
        if (Global.BASIC_VERSION) return as;
        long offset = SettingsActivity.getUcac4Offset();
        if (offset == -1)
            return as;
        int pos = Global.ucac4Q.get(q);
        int length = Global.ucac4Q.get(q + 1) - pos;
        for (int i = 0; i < length; i++) {
            db.seek(offset + (i + pos) * 14);

            double ra = db.readFloat();
            double dec = db.readFloat();
            int zone = db.readShort();
            int zonenum = getZoneId(db.readByte(), db.readByte(), db.readByte());
            byte m = db.readByte();//mag
            double mag;
            if (m > 0)
                mag = m / 10f;
            else
                mag = (m + 256) / 10f;

            if (mag <= mag_limit) {
                Ucac4Star star = new Ucac4Star(ra, dec, mag, 0, zone, zonenum);
                as.add(star);
            }
        }
        return as;
    }

    public List<AstroObject> get(int q, double mag_limit, double rac, double decc, double dist) throws IOException {
        ArrayList<AstroObject> as = new ArrayList<AstroObject>();
        if (Global.BASIC_VERSION) return as;
        long offset = SettingsActivity.getUcac4Offset();
        if (offset == -1)
            return as;
        int pos = Global.ucac4Q.get(q);
        int length = Global.ucac4Q.get(q + 1) - pos;

        double cosdecc = cos(decc * PI / 180);
        double sindecc = sin(decc * PI / 180);
        double cdist = cos(dist * PI / 180);

        for (int i = 0; i < length; i++) {
            db.seek(offset + (i + pos) * 14);

            double ra = db.readFloat();
            double dec = db.readFloat();
            int zone = db.readShort();
            int zonenum = getZoneId(db.readByte(), db.readByte(), db.readByte());
            byte m = db.readByte();//mag
            double mag;
            if (m > 0)
                mag = m / 10f;
            else
                mag = (m + 256) / 10f;

            if (mag <= mag_limit) {

                double cosd = sindecc * sin(dec * PI / 180) + cosdecc * cos(dec * PI / 180) * cos((rac - ra) * PI / 12);
                if (cosd < cdist)
                    continue;

                Ucac4Star star = new Ucac4Star(ra, dec, mag, 0, zone, zonenum);

                as.add(star);
            }
        }
        return as;
    }

    public void close() throws IOException {
        db.close();
    }
}
