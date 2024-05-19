package com.astro.dsoplanner.database;

import static com.astro.dsoplanner.Constants.ALT;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.astro.dsoplanner.Analisator;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.base.CMO;
import com.astro.dsoplanner.base.Comet;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.base.CustomObjectLarge;
import com.astro.dsoplanner.base.Fields;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.base.MinorPlanet;


import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.QueryActivity;
import com.astro.dsoplanner.SearchRules;
import com.astro.dsoplanner.SettingsActivity;

public class CometsDatabase extends CustomDatabaseLarge {

    private static final String AND_REF_IS_NOT_NULL_GROUP_BY_REF_UNION_SELECT_FROM_CUSTOMDBB_WHERE2 = ") and ref is not null group by ref union select * from customdbb where (";
    private static final String SELECT_FROM_CUSTOMDBB_WHERE2 = "select * from customdbb where (";
    private static final String H = "h";
    private static final String C2 = "c";
    private static final String M = "m";
    private static final String B = "b";
    private static final String A2 = "a";
    private static final String ID = "id";
    private static final String CON = "con";
    private static final String PA = "pa";
    private static final String MAG = "mag";
    private static final String DEC = "dec";
    private static final String RA = "ra";
    private static final String NAME = "name";
    private static final String TYPE = "type";


    private static final String TAG = CometsDatabase.class.getSimpleName();

    private static String getDbName(int cat) {
        switch (cat) {
            case AstroCatalog.COMET_CATALOG:
                return Comet.DB_NAME;
            case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
                return MinorPlanet.DB_NAME_BRIGHT;
        }
        return null;
    }

    private static DbListItem.FieldTypes getFtypes(int cat) {
        switch (cat) {
            case AstroCatalog.COMET_CATALOG:
                return Comet.FTYPES;
            case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
                return MinorPlanet.FTYPES;
        }
        return null;
    }

    public CometsDatabase(Context context, int catalog) {
        super(context, getDbName(catalog), catalog, getFtypes(catalog));
    }

    @Override
    public CMO getObjectFromCursor(Cursor cursor) {
        CustomObjectLarge obj = (CustomObjectLarge) super.getObjectFromCursor(cursor);
        switch (catalog) {
            case AstroCatalog.COMET_CATALOG:
                Comet comet = new Comet(obj.id, obj.getShortName(), obj.getLongName(), obj.getComment(), obj.getFields());
                return comet;
            case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
                MinorPlanet planet = new MinorPlanet(obj.id, obj.getShortName(), obj.getLongName(), obj.getComment(), obj.getFields());
                return planet;
        }

        return null;
    }

    public List<AstroObject> search() {
        List<AstroObject> list = new ArrayList<AstroObject>();

        String sqlReq = SearchRules.createQuery(context, true, false, false);//createQuery();

        Log.d(TAG, "search, sqlReq=" + sqlReq);
        Cursor cursor;
        boolean rd = SettingsActivity.isRemovingDuplicates() && internalDeepSky;

        try {
            if (!rd)
                cursor = db.query(TABLE_NAME, null, sqlReq, null, null, null, null);
            else {
                String sql = SELECT_FROM_CUSTOMDBB_WHERE2 + sqlReq + AND_REF_IS_NOT_NULL_GROUP_BY_REF_UNION_SELECT_FROM_CUSTOMDBB_WHERE2 + sqlReq + ") and ref is null;";
                Log.d(TAG, "sql=" + sql);
                cursor = db.rawQuery(sql, null);
            }

        } catch (SQLiteException e) {
            Log.d(TAG, "e=" + e);
            return list;
        }
        Log.d(TAG, "size=" + cursor.getCount());
        int filter = SettingsActivity.getFilter();
        Log.d(TAG, "filter=" + filter);
        double maxmag = SettingsActivity.getMaxMag();

        double minalt = SettingsActivity.getMinAlt();
        boolean stopflag = false;
        int i = 0;
        double DL = SettingsActivity.getDetectionLimit();

        int emptyRule = SettingsActivity.getEmptyRule();

        boolean comettype = (catalog == AstroCatalog.COMET_CATALOG);
        Calendar sc = Calendar.getInstance();
        long start = SettingsActivity.getSharedPreferences(context).getLong(Constants.START_OBSERVATION_TIME, 0);
        sc.setTimeInMillis(start);

        AstroTools.CheckAltContext checkContext = new AstroTools.CheckAltContext();

        while (cursor.moveToNext() && !QueryActivity.isStopping()) {
            // if...altitude and visibility
            CMO comet = getObjectFromCursor(cursor);
            comet.recalculateRaDec(sc);

            if (i++ % 100 == 0) {
                Log.d(TAG, "i=" + i + " list size=" + list.size());
            }
            double ra = comet.getRa();
            double dec = comet.getDec();

            double a = Double.NaN;
            double b = Double.NaN;
            double mag = comet.getMag();
            boolean flagNearby = true;
            boolean flag = true;
            if ((flagNearby) && (AstroTools.CheckAlt(ra, dec, checkContext))) {

                //if above minimum altitude put the object into idList
                switch (filter) {
                    case 0:

                        flag = AstroTools.CheckVisibility(a, b, mag, DL, emptyRule, comettype ? AstroObject.Comet : AstroObject.MINOR_PLANET);
                        break;
                    case 1:
                        flag = (mag <= maxmag);
                        break;
                }
                if (flag) {
                    list.add(comet);
                    if (list.size() > Global.SQL_SEARCH_LIMIT) {
                        break;
                    }
                }
            }
        }

        cursor.close();
        return list;
    }


    /**
     * modified search for comet database
     *
     * @param ans   compliled sql request
     * @param anl   compiled local request
     * @param start
     * @param end
     * @param start time in calendar format
     * @return
     */
    public List<AstroObject> searchMod(String sqlStr, String locStr, double start, double end, Calendar cal) {

        Log.d(TAG, "sqlStr=" + sqlStr + " " + sqlStr.length());
        Log.d(TAG, "locStr=" + locStr + " " + locStr.length());
        Log.d(TAG, "start=" + start);
        Log.d(TAG, "end=" + end);
        Log.d(TAG, "cal=" + cal.getTimeInMillis());

        List<AstroObject> list = new ArrayList<AstroObject>();


        Analisator asql = new Analisator();
        asql.setInputString(sqlStr);

        try {
            initSql(asql);
            asql.compile();

        } catch (Exception e) {
            Log.d(TAG, "exception=" + e);
            return list;
        }

        Analisator alocal = new Analisator();
        alocal.dsoInitLocalrequest();
        boolean localused = true;
        try {

            alocal.setInputString(locStr);
            alocal.compile();
        } catch (Exception e) {
            localused = false;
        }
        Log.d(TAG, "localused=" + localused);
        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null,
                    null, null);
        } catch (SQLiteException e) {
            return list;
        }

        Set<String> varsUsed;
        varsUsed = alocal.getVarsUsedInExpression();
        boolean altUsed = varsUsed.contains(ALT.toUpperCase());
        boolean visUsed = varsUsed.contains(Constants.VIS.toUpperCase());
        Calendar c = Calendar.getInstance();
        double lat = SettingsActivity.getLattitude();

        int i = 0;
        while (cursor.moveToNext() && !QueryActivity.isStopping()) {
            CMO comet = getObjectFromCursor(cursor);
            comet.recalculateRaDec(cal);
            fillSql(asql, comet);
            if (asql.calculate()) {
                boolean passed = true;
                if (localused)
                    passed = checkLocal(alocal, comet, altUsed, visUsed, start, end, lat);
                if (passed) {
                    if (i++ < Global.SQL_SEARCH_LIMIT) {

                        list.add(comet);
                    } else
                        break;
                }

            }
        }
        return list;
    }

    /**
     * fill analisator with all vars from comet
     *
     * @param a
     * @param c
     */
    private void fillSql(Analisator a, CMO c) {
        Set<String> nfields = types.getNumericFields();
        Fields f = c.getFields();
        for (String s : nfields) {
            Double value = f.getNum(s);
            if (value != null)
                a.addVar(s, value);
        }
        int type;
        if (catalog == AstroCatalog.COMET_CATALOG)
            type = AstroObject.Comet;
        else
            type = AstroObject.MINOR_PLANET;
        a.addVar(TYPE, type);

        a.addVar(A2, 0);
        a.addVar(B, 0);
        a.addVar(NAME, 0);
        a.addVar(RA, c.getRa());
        a.addVar(DEC, c.getDec());
        a.addVar(MAG, c.getMag());
        a.addVar(PA, c.getPA());
        a.addVar(M, 0);
        a.addVar(C2, 0);
        a.addVar(H, 0);
        a.addVar(CON, c.getCon());
        a.addVar(ID, c.getId());
    }

    /**
     * init analisator for compilation
     *
     * @param a
     */
    private void initSql(Analisator a) {
        Set<String> nfields = types.getNumericFields();

        for (String s : nfields) {
            a.addVar(s, 0);
        }

        a.addVar(TYPE, AstroObject.Comet);
        a.addVar(A2, 0);
        a.addVar(B, 0);
        a.addVar(NAME, 0);
        a.addVar(RA, 0);
        a.addVar(DEC, 0);
        a.addVar(MAG, 0);
        a.addVar(PA, 0);
        a.addVar(M, 0);
        a.addVar(C2, 0);
        a.addVar(H, 0);
        a.addVar(CON, 0);
        a.addVar(ID, 0);
    }

    private boolean checkLocal(Analisator alocal, CMO c, boolean altUsed, boolean visUsed, double start, double end, double lat) {
        try {
            if (visUsed) {
                if (catalog == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG) {//visibility not calculated for comets, by default it is zero
                    double vis = AstroTools.getMaxVisibility(0, 0, c.getMag(), AstroObject.MINOR_PLANET);
                    alocal.setVars(Constants.VIS, vis);
                }
            }
            if (!altUsed) {
                return alocal.calculate();

            } else {//alt used in search
                double ste;

                if (end < start)
                    ste = end + 24;
                else
                    ste = end;
                double sttime = start;
                boolean result = false;
                boolean flag;
                double ra = c.getRa();
                double dec = c.getDec();
                do {
                    double alt = AstroTools.Altitude(sttime, lat, ra, dec);
                    alocal.setVars(ALT, alt);
                    result = alocal.calculate();
                    sttime = sttime + 0.5;
                    flag = (sttime < ste);
                    if (result)
                        flag = false;
                } while (flag);
                if (result) {
                    return true;
                } else
                    return false;
            }
        } catch (Exception e) {
            return false;
        }

    }

    @Override
    public List<AstroObject> searchNearby(Point nearbyObject, double fov, double vis) {
        Log.d(TAG, "vis=" + vis + " catalog=" + catalog);
        List<AstroObject> list = new ArrayList<AstroObject>();


        int emptyRule = SettingsActivity.getEmptyRule();
        boolean comettype = catalog == AstroCatalog.COMET_CATALOG;
        if (catalog == AstroCatalog.COMET_CATALOG && vis > 0)//comet vis default by zero
            return list;
        Cursor cursor;
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null,
                    null, null);
        } catch (SQLiteException e) {
            return list;
        }
        double raObj = 0;
        double decObj = 0;
        double dist = fov / 2;
        raObj = nearbyObject.getRa();
        decObj = nearbyObject.getDec();
        Calendar cal = AstroTools.getDefaultTime(Global.getAppContext());
        int i = 0;
        while (cursor.moveToNext()) {

            //public DSO (int ngc,double ra,double dec,double mag,double a,double b,int messier,int caldwell,int hershell,int type,int con,double pa)
            //                1           2         3         4           5       6         10           11       12           8        7      9
            // if...altitude and visibility

            CMO comet = getObjectFromCursor(cursor);
            comet.recalculateRaDec(cal);
            if (cos(dist / 60 * PI / 180) < (sin(comet.getDec() * PI / 180) * sin(decObj * PI / 180) + cos(comet.getDec() * PI / 180) * cos(decObj * PI / 180) * cos((comet.getRa() - raObj) * PI / 12))) {
                if (catalog == AstroCatalog.COMET_CATALOG)
                    list.add(comet);
                else if (AstroTools.CheckVisibility(comet.getA(), comet.getB(), comet.getMag(), vis, emptyRule, (comettype ? AstroObject.Comet : AstroObject.MINOR_PLANET)))
                    list.add(comet);
            }
        }
        Log.d(TAG, "over, list size=" + list.size());
        for (AstroObject o : list) {
            Log.d(TAG, "obj=" + o);
        }
        return list;

    }
}
