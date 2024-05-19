package com.astro.dsoplanner.infolist;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.base.CMO;
import com.astro.dsoplanner.base.Comet;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.base.Fields;


public class CometListLoader implements InfoListLoader {
    private static final String TAG = CometListLoader.class.getSimpleName();
    BufferedReader bin;
    int row = 1;

    public CometListLoader(InputStream in) {
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
            StringBuilder sb = new StringBuilder();
            //15 -  18  i4     Year of perihelion passage
            for (int i = 14; i < 18; i++) {
                sb.append((char) buf[i]);
            }
            int year = Integer.parseInt(sb.toString());

            //20 -  21  i2     Month of perihelion passage
            sb = new StringBuilder();
            sb.append((char) buf[19]).append((char) buf[20]);
            int month = Integer.parseInt(sb.toString());

            //23 -  29  f7.4   Day of perihelion passage (TT)
            sb = new StringBuilder();
            for (int i = 22; i < 29; i++) {
                sb.append((char) buf[i]);
            }
            double day = Double.parseDouble(sb.toString());

            //	 31 -  39  f9.6   Perihelion distance (AU)
            sb = new StringBuilder();
            for (int i = 30; i < 39; i++) {
                sb.append((char) buf[i]);
            }
            double q = Double.parseDouble(sb.toString());

            //	   42 -  49  f8.6   Orbital eccentricity
            sb = new StringBuilder();
            for (int i = 41; i < 49; i++) {
                sb.append((char) buf[i]);
            }
            double e = Double.parseDouble(sb.toString());

            // 52 -  59  f8.4   Argument of perihelion, J2000.0 (degrees)
            sb = new StringBuilder();
            for (int i = 51; i < 59; i++) {
                sb.append((char) buf[i]);
            }
            double w = Double.parseDouble(sb.toString());

            //   62 -  69  f8.4   Longitude of the ascending node, J2000.0
            //                     (degrees)
            sb = new StringBuilder();
            for (int i = 61; i < 69; i++) {
                sb.append((char) buf[i]);
            }
            double N = Double.parseDouble(sb.toString());

            //  72 -  79  f8.4   Inclination in degrees, J2000.0 (degrees)
            sb = new StringBuilder();
            for (int i = 71; i < 79; i++) {
                sb.append((char) buf[i]);
            }
            double incl = Double.parseDouble(sb.toString());

            // 92 -  95  f4.1   Absolute magnitude
            sb = new StringBuilder();
            for (int i = 91; i < 95; i++) {
                sb.append((char) buf[i]);
            }
            double absmag = Double.parseDouble(sb.toString());

            //   97 - 100  f4.0   Slope parameter
            sb = new StringBuilder();
            for (int i = 96; i < 100; i++) {
                sb.append((char) buf[i]);
            }
            double slope = Double.parseDouble(sb.toString());


            //  103 - 158  a56    Designation and Name
            sb = new StringBuilder();
            for (int i = 102; i < buf.length && i < 158; i++) {
                sb.append((char) buf[i]);
            }
            String name = removeLastSpaces(sb.toString());

            Fields fields = new Fields();
            fields.put(CMO.DAY, day);
            fields.put(CMO.YEAR, year);
            fields.put(CMO.E, e);
            fields.put(CMO.I, incl);
            fields.put(CMO.MONTH, month);
            fields.put(CMO.NODE, N);
            fields.put(CMO.W, w);
            fields.put(Comet.ABSMAG, absmag);
            fields.put(Comet.Q, q);
            fields.put(Comet.SLOPE, slope);
            Comet comet = new Comet(0, name, name, "", fields);
            row++;
            return comet;

        } catch (Exception e) {
            Log.d(TAG, "exception=" + AstroTools.getStackTrace(e));
            erec.line = s;
            erec.lineNum = row;
            erec.type = ErrorHandler.DATA_CORRUPTED;
            return null;
        }


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
}
