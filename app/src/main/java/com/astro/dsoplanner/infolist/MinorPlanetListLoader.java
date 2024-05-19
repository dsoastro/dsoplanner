package com.astro.dsoplanner.infolist;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.base.CMO;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.base.Fields;
import com.astro.dsoplanner.base.MinorPlanet;

public class MinorPlanetListLoader implements InfoListLoader {
    private static final String TAG = MinorPlanetListLoader.class.getSimpleName();
    BufferedReader bin;
    int row = 1;

    public MinorPlanetListLoader(InputStream in) {
        bin = new BufferedReader(new InputStreamReader(in));

    }

    public void open() {
    }

    public String getName() throws IOException {
        return "";
    }

    public Object next(ErrorHandler.ErrorRec erec) throws IOException {
        String s = bin.readLine();
        if (s == null) throw new EOFException();
        byte[] buf = s.getBytes();
        try {
            //21 -  25  a5     Epoch (in packed form, .0 TT)
            StringBuilder sb = new StringBuilder();
            sb = new StringBuilder();
            for (int i = 20; i < 25; i++) {
                sb.append((char) buf[i]);
            }
            data date = getEpoch(sb.toString());

            // 93 - 103  f11.7  Semimajor axis (AU)
            sb = new StringBuilder();
            for (int i = 92; i < 103; i++) {
                sb.append((char) buf[i]);
            }
            double A = Double.parseDouble(sb.toString());


            //	 27 -  35  f9.5   Mean anomaly at the epoch, in degrees
            sb = new StringBuilder();
            for (int i = 26; i < 35; i++) {
                sb.append((char) buf[i]);
            }
            double M0 = Double.parseDouble(sb.toString());

            //	    71 -  79  f9.7   Orbital eccentricity
            sb = new StringBuilder();
            for (int i = 70; i < 79; i++) {
                sb.append((char) buf[i]);
            }
            double e = Double.parseDouble(sb.toString());

            // 38 -  46  f9.5   Argument of perihelion, J2000.0 (degrees)
            sb = new StringBuilder();
            for (int i = 37; i < 46; i++) {
                sb.append((char) buf[i]);
            }
            double w = Double.parseDouble(sb.toString());

            //    49 -  57  f9.5   Longitude of the ascending node, J2000.0
            // (degrees)
            //
            sb = new StringBuilder();
            for (int i = 48; i < 57; i++) {
                sb.append((char) buf[i]);
            }
            double N = Double.parseDouble(sb.toString());

            //  60 -  68  f9.5   Inclination to the ecliptic, J2000.0 (degrees)
            sb = new StringBuilder();
            for (int i = 59; i < 68; i++) {
                sb.append((char) buf[i]);
            }
            double incl = Double.parseDouble(sb.toString());

            //  9 -  13  f5.2   Absolute magnitude, H
            sb = new StringBuilder();
            for (int i = 8; i < 13; i++) {
                sb.append((char) buf[i]);
            }
            double H = Double.parseDouble(sb.toString());

            //   15 -  19  f5.2   Slope parameter, G
            sb = new StringBuilder();
            for (int i = 14; i < 19; i++) {
                sb.append((char) buf[i]);
            }
            double G = Double.parseDouble(sb.toString());


            //   167 - 194  a      Readable designation
            sb = new StringBuilder();
            for (int i = 166; i < buf.length && i < 194; i++) {
                sb.append((char) buf[i]);
            }

            String name = removeLastSpaces(sb.toString());
            name = removeFirstSpaces(name);

            Fields fields = new Fields();
            fields.put(CMO.DAY, date.day);
            fields.put(CMO.YEAR, date.year);
            fields.put(CMO.E, e);
            fields.put(CMO.I, incl);
            fields.put(CMO.MONTH, date.month);
            fields.put(CMO.NODE, N);
            fields.put(CMO.W, w);
            fields.put(MinorPlanet.H, H);
            fields.put(MinorPlanet.A, A);
            fields.put(MinorPlanet.G, G);
            fields.put(MinorPlanet.MA, M0);
            MinorPlanet mp = new MinorPlanet(0, name, name, "", fields);
            row++;
            return mp;

        } catch (Exception e) {
            Log.d(TAG, "exception=" + AstroTools.getStackTrace(e));
            erec.line = s;
            erec.lineNum = row;
            erec.type = ErrorHandler.DATA_CORRUPTED;
            return null;
        }
    }

    private String removeFirstSpaces(String s) {
        int pos = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch != ' ') {
                pos = i;
                break;
            }
        }
        return s.substring(pos, s.length());
    }

    private String removeLastSpaces(String s) {
        //last non white space symbol
        int pos = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch != ' ') {
                pos = i;
            }
        }
        return s.substring(0, pos + 1);
    }

    public void close() throws IOException {
        bin.close();
    }

    final static char[] base = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V'};

    class data {
        int year;
        int month;
        double day;

        public data(int year, int month, double day) {
            super();
            this.year = year;
            this.month = month;
            this.day = day;
        }
    }

    private int find(char c) {
        for (int i = 0; i < base.length; i++) {
            if (c == base[i])
                return i + 1;
        }
        return -1;
    }

    private data getEpoch(String s) {

        String year = "" + find(s.charAt(0)) + s.charAt(1) + s.charAt(2);
        int month = find(s.charAt(3));
        String day = "" + find(s.charAt(4));
        boolean start = true;
        for (int i = 5; i < s.length(); i++) {
            if (start) {
                day += '.';
                start = false;
            }
            day += s.charAt(i);
        }
        return new data(Integer.parseInt(year), month, Double.parseDouble(day));
    }
}
