package com.astro.dsoplanner.base;


import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Global;


import com.astro.dsoplanner.database.DbListItem;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

public class Comet extends CMO {

    private static final String COMETS_TXT = "comets.txt";
    public static final String DB_DESC_NAME = "Comets";
    public static final String DB_NAME = "comets.db";
    public static final String DOWNLOAD_URL = "https://minorplanetcenter.net/iau/Ephemerides/Comets/Soft00Cmt.txt";//from where download updates

    public static final String Q = "q";
    public static final String ABSMAG = "absmag";
    public static final String SLOPE = "slope";


    public static final String DOWNLOAD_FILE_PATH = Global.tmpPath + COMETS_TXT;//where to download updates

    public static final DbListItem.FieldTypes FTYPES = new DbListItem.FieldTypes();

    static {
        FTYPES.put(CMO.NODE, DbListItem.FieldTypes.TYPE.DOUBLE);
        FTYPES.put(CMO.I, DbListItem.FieldTypes.TYPE.DOUBLE);
        FTYPES.put(CMO.W, DbListItem.FieldTypes.TYPE.DOUBLE);
        FTYPES.put(CMO.E, DbListItem.FieldTypes.TYPE.DOUBLE);
        FTYPES.put(CMO.DAY, DbListItem.FieldTypes.TYPE.DOUBLE);
        FTYPES.put(Q, DbListItem.FieldTypes.TYPE.DOUBLE);
        FTYPES.put(ABSMAG, DbListItem.FieldTypes.TYPE.DOUBLE);
        FTYPES.put(SLOPE, DbListItem.FieldTypes.TYPE.DOUBLE);
        FTYPES.put(CMO.YEAR, DbListItem.FieldTypes.TYPE.INT);
        FTYPES.put(CMO.MONTH, DbListItem.FieldTypes.TYPE.INT);

    }


    public Comet(int id, String name1, String name2, String comment, Fields fields) {
        super(AstroCatalog.COMET_CATALOG, id, AstroObject.Comet, name1, name2, comment, fields);
        init();
    }

    private void init() {
        obj_type = COMET;
        Map<String, Double> mapd = fields.getDoubleMap();
        Map<String, Integer> mapi = fields.getIntMap();
        try {
            N = mapd.get(CMO.NODE);
            i = mapd.get(CMO.I);
            w = mapd.get(CMO.W);
            e = mapd.get(CMO.E);
            int year = mapi.get(CMO.YEAR);
            int month = mapi.get(CMO.MONTH);
            double day = mapd.get(CMO.DAY);
            jdp = AstroTools.JD(year, month, day, 0, 0);
            M0 = 0;
            absmag = mapd.get(ABSMAG);
            slope = mapd.get(SLOPE);
            q = mapd.get(Q);
            sma = q / (1 - e);
        } catch (Exception e) {
        }
    }

    public Comet(DataInputStream stream) throws IOException {
        super(stream);
        init();
    }

    @Override
    public int getClassTypeId() {
        return Exportable.COMET_OBJECT;
    }

    @Override
    public boolean hasVisibility() {
        return false;
    }
}
