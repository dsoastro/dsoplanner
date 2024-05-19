package com.astro.dsoplanner.pushcamera;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.google.gson.Gson;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.util.Holder2;

public class DbBuilder {
    public static final String PATH = Global.exportImportPath;
    public static final String STARDB = "stars.bin";
    public static final String QFDB = "qf.bin";
    private static Map<Integer, StarEntry> stars_db = null;
    private static Map<Integer, List<Holder2<P4, P4i>>> qf_db = null;

    private static Map<Integer, StarEntry> createStarsDb() throws Exception {
        Db db = new Db(PATH, "hrdb.db");
        DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(PATH, STARDB)));

        List<String[]> list = db.exec("SELECT _id,ra,dec,name1,mag FROM customdbb where mag<4;");
        Map<Integer, StarEntry> map = new HashMap<>();

        for (String[] as : list) {
            int id = Integer.parseInt(as[0]);
            double ra = Double.parseDouble(as[1]);
            double dec = Double.parseDouble(as[2]);
            String name1 = as[3];
            int hr = Integer.parseInt(name1.replace("HR", ""));
            double mag = Double.parseDouble(as[4]);
            out.writeInt(id);
            out.writeDouble(ra);
            out.writeDouble(dec);
            out.writeInt(hr);
            out.writeDouble(mag);

            map.put(id, new StarEntry(ra, dec, mag, hr));

        }
        out.close();
        return map;
    }

    public static Map<Integer, StarEntry> loadStarsDb() throws Exception {
        if (stars_db == null) {
            DataInputStream in = new DataInputStream(new FileInputStream(new File(PATH, STARDB)));
            Map<Integer, StarEntry> map = new HashMap<>();
            int size = 0;
            while (in.available() > 0) {
                int id = in.readInt();
                double ra = in.readDouble();
                double dec = in.readDouble();
                int hr = in.readInt();
                double mag = in.readDouble();
                map.put(id, new StarEntry(ra, dec, mag, hr));
                size++;
            }
            in.close();
            System.out.println("stars db size " + size);
            stars_db = map;
        }
        return stars_db;
    }

    public static Map<Integer, List<Holder2<P4, P4i>>> loadQf() throws Exception {
        if (qf_db == null) {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(PATH, QFDB))));
            Map<Integer, List<Holder2<P4, P4i>>> map = new HashMap<>();
            while (in.available() > 0) {
                int key = in.readInt();
                int size = in.readInt();
                List<Holder2<P4, P4i>> lh = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    double h1 = in.readDouble();
                    double h2 = in.readDouble();
                    double h3 = in.readDouble();
                    double h4 = in.readDouble();

                    int id1 = in.readInt();
                    int id2 = in.readInt();
                    int id3 = in.readInt();
                    int id4 = in.readInt();

                    lh.add(new Holder2<P4, P4i>(new P4(h1, h2, h3, h4), new P4i(id1, id2, id3, id4)));
                }
                map.put(key, lh);
            }
            qf_db = map;
            in.close();
        }
        return qf_db;
    }


}
