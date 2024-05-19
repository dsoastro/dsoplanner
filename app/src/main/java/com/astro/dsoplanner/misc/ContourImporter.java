package com.astro.dsoplanner.misc;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.base.ContourObject;
import com.astro.dsoplanner.base.ContourObject.RaDec;


/**
 * temporary class to import nebula contours from txt file and make the relevant list
 * of ContourObjects
 */
public class ContourImporter {
	
    private static final String END = "end";
    private static final String POLY_END = "POLY END";
    private static final String POLY_POINT = "POLY POINT";
    private static final String POLY_BEG = "POLY BEG";
    private static final String VEIL = "Veil";


    private static final String TAG = ContourImporter.class.getSimpleName();
    InputStream in;
    BufferedReader reader;
    int lastStringNum = 0;
    String lastString = "";

    public ContourImporter(InputStream stream) {
        in = stream;
        reader = new BufferedReader(new InputStreamReader(in));
    }

    private RaDec get(String s) {
        String raStr = s.substring(17, 27);
        String decStr = s.substring(29, 40);
        double ra = 0;
        double dec = 0;
        try {
            ra = Double.parseDouble(raStr);
            dec = Double.parseDouble(decStr);
        } catch (Exception e) {
            return null;
        }
        return new RaDec(ra, dec);

    }

    class data {
        double ra;
        double dec;
        double extent;

        public data(double ra, double dec, double extent) {
            super();
            this.ra = ra;
            this.dec = dec;
            this.extent = extent;
        }


    }


    //calculates average ra/dec
    private data calculate(List<List<RaDec>> list) {

        List<RaDec> listAv = new ArrayList<RaDec>();
        for (List<RaDec> ilist : list) {

            for (RaDec rec : ilist) {
                listAv.add(new RaDec(rec.ra, rec.dec));
            }

        }
        double raSum = 0;
        double decSum = 0;
        for (RaDec rec : listAv) {
            raSum += rec.ra;
            decSum += rec.dec;
        }
        double raAv = raSum / listAv.size();
        double decAv = decSum / listAv.size();
        double distSq = 0;
        for (RaDec rec : listAv) {
            double dstra = Math.min(Math.abs(rec.ra - raAv), 24 - Math.abs(rec.ra - raAv)) * 360 / 24 * Math.cos(decAv * Math.PI / 180);
            double dstdec = rec.dec - decAv;
            distSq += dstra * dstra + dstdec * dstdec;
        }
        //in angular minutes
        double extent = 60 * Math.sqrt(distSq / listAv.size());
        data d = new data(raAv, decAv, extent);
        return d;
    }

    public ContourObject next() throws IOException {
        String name1 = "";
        String name2 = "";
        List<List<ContourObject.RaDec>> list = new ArrayList<List<RaDec>>();
        boolean objdetected = false;
        boolean contourdetected = false;
        boolean compression = false;
        List<ContourObject.RaDec> ilist = new ArrayList<ContourObject.RaDec>();
        while (true) {
            String s;
            int i = 0;
            s = reader.readLine();
            if (s == null) throw new EOFException();
            if (s.length() > 0 && s.charAt(0) == ';') {
                if (s.contains(END)) {//the object description over
                    objdetected = false;
                    if (name1.contains(VEIL))
                        compression = false;
                    data rec = calculate(list);
                    ContourObject obj = new ContourObject(AstroCatalog.CUSTOM_CATALOG, 0, rec.ra,
                            rec.dec, AstroTools.getConstellation(rec.ra, rec.dec), AstroObject.Neb, "",
                            rec.extent, rec.extent, 0, 0, name1, name1, "", list);
                    return obj;
                }
                if (!s.contains(END) && !objdetected) {
                    name1 = s.substring(1, s.length());
                    objdetected = true;
                    if (name1.contains(VEIL)) {
                        //compression=true;
                    }
                }
            }
            if (s.contains(POLY_BEG)) {
                ilist = new ArrayList<ContourObject.RaDec>();
                ContourObject.RaDec rec = get(s);
                ilist.add(rec);
            }
            if (s.contains(POLY_POINT)) {
                i++;
                if (!compression) {
                    ContourObject.RaDec rec = get(s);
                    ilist.add(rec);
                } else if (i % 10 == 0) {
                    ContourObject.RaDec rec = get(s);
                    ilist.add(rec);
                }

            }
            if (s.contains(POLY_END)) {
                i = 0;
                ContourObject.RaDec rec = get(s);
                ilist.add(rec);
                if (ilist.size() != 0)
                    list.add(ilist);
            }

        }
    }
}
