package com.astro.dsoplanner;

import static com.astro.dsoplanner.Constants.TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.HrStar;
import com.astro.dsoplanner.database.CometsDatabase;
import com.astro.dsoplanner.database.CustomDatabase;
import com.astro.dsoplanner.database.CustomDatabaseLarge;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.DbManager;
import com.astro.dsoplanner.database.NgcicDatabase;

/**
 * encompasses search rules
 *
 * @author leonid
 */
public class SearchRules {

    private static final String AND_MAG2 = ") and (mag2<=";
    private static final String AND_SEPARATION2 = ") and (separation<=";
    private static final String AND_SEPARATION = " and (separation>=";
    private static final String OR_NAME2_LIKE = " or name2 like '";
    private static final String AND_NAME1_LIKE = "and (name1 like '";
    private static final String COALESCE_MAG_1000000 = "coalesce(mag,1000000)<=";
    private static final String COALESCE_MAG_0 = "coalesce(mag,0)<=";
    private static final String OR_MAG_IS_NULL = " or mag is null)";
    private static final String MAG = "mag<=";
    private static final String AND3 = " and (";
    private static final String OR_CONSTELLATION = " or constellation=";
    private static final String COALESCE_A_1000000 = "(coalesce(a,1000000)>=";
    private static final String COALESCE_A_0 = "(coalesce(a,0)>=";
    private static final String OR_A_IS_NULL = " or a is null)";
    private static final String _0_9_0_9 = "[0-9]+[\\+\\-][0-9]+";
    private static final String HR = "HR";
    private static final String PGC = "PGC";
    private static final String WDS = "WDS";
    private static final String HCG = "HCG";
    private static final String ABELL = "ABELL";
    private static final String PK = "PK";
    private static final String SH2 = "SH2";
    private static final String LBN = "LBN";
    private static final String B_0_9 = "B[0-9]+.*";
    private static final String LDN = "LDN";
    private static final String UGC = "UGC";
    private static final String IC = "IC";
    private static final String NGC = "NGC";
    public static final String OR = " OR ";
    private static final String AND2 = " AND ";
    private static final String AND = " AND (";


    private static final String TAG = SearchRules.class.getSimpleName();
    public static final int DIMENSION = 1;
    public static final int MAGNITUDE = 2;
    public static final int VISIBILITY = 3;

    /**
     * @param type  - object type
     * @param cat   - object catalog
     * @param param - dimension, mag, visibility
     * @return
     */
    public static boolean isApplicable(int type, int cat, int param) {
        switch (cat) {
            case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
                if (param == DIMENSION) return false;
                break;
            case AstroCatalog.COMET_CATALOG:
                if (param == DIMENSION | param == VISIBILITY) return false;
                break;
            case AstroCatalog.DNLYNDS:
            case AstroCatalog.DNBARNARD:
                if (param == MAGNITUDE | param == VISIBILITY) return false;
                break;
            case AstroCatalog.WDS:
                if (param == DIMENSION) return false;
                break;
        }
        switch (type) {
            case AstroObject.DN:
                if (param == MAGNITUDE | param == VISIBILITY) return false;
                break;
            case AstroObject.DoubleStar:
                if (param == DIMENSION) return false;
                break;
        }
        return true;
    }

    static class HolderA {
        AstroObject obj;

        public HolderA(AstroObject obj) {

            this.obj = obj;
        }

        @Override
        public String toString() {
            return "HolderA [obj=" + obj + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof HolderA) {
                HolderA h = (HolderA) o;
                if (h.obj.getCatalog() == obj.getCatalog() && h.obj.getId() == obj.getId()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return obj.getCatalog() * 37 + obj.getId();
        }

    }

    /**
     * set of AstroObjects. Use to replace standard equal to
     * that of HolderA. Use addAll2 only
     * Needed to remove objects from the same db within one list
     *
     * @author leonid
     */
    static class MySet extends LinkedHashSet {
        public void addAll2(Collection<AstroObject> col) {
            for (AstroObject obj : col) {
                HolderA h = new HolderA(obj);
                add(h);
                Log.d(TAG, "h=" + h);
            }

        }

        public void addAll2(AstroObject obj) {
            HolderA h = new HolderA(obj);
            add(h);
        }

        public List<AstroObject> get() {
            List<AstroObject> list = new ArrayList<AstroObject>();
            for (Object o : this) {
                HolderA h = (HolderA) o;
                list.add(h.obj);
            }
            return list;
        }
    }


    /**
     * @param cat
     * @return whether this catalog is internal deep sky catalog
     */
    public static boolean isInternalDeepSky(int cat) {
        return (cat == AstroCatalog.NGCIC_CATALOG || cat == AstroCatalog.BNLYNDS ||
                cat == AstroCatalog.DNBARNARD || cat == AstroCatalog.UGC || cat == AstroCatalog.DNLYNDS
                || cat == AstroCatalog.SAC || cat == AstroCatalog.ABELL || cat == AstroCatalog.HCG || cat == AstroCatalog.PGC || cat == AstroCatalog.PK || cat == AstroCatalog.SH2
                || cat == AstroCatalog.MESSIER || cat == AstroCatalog.CALDWELL || cat == AstroCatalog.MISC);
    }

    public static boolean isInternalCatalog(int cat) {
        return (cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG || cat == AstroCatalog.COMET_CATALOG ||
                cat == AstroCatalog.WDS || cat == AstroCatalog.SNOTES || cat == AstroCatalog.HAAS || isInternalDeepSky(cat));
    }

    /**
     * @param cat
     * @return true if catalog could be edited, false otherwise
     */
    public static boolean isEdited(int cat) {
        return (!isInternalCatalog(cat) || cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG || cat == AstroCatalog.COMET_CATALOG);
    }

    /**
     * @param cat
     * @return true if the database needs to be backed up / restored in Backup / Restore
     */
    public static boolean isToBeBackedUp(int cat) {
        if (cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG || cat == AstroCatalog.COMET_CATALOG) {
            return true;
        }
        if (isInternalCatalog(cat)) {//comets and mps were processed before
            return false;
        }
        return true;
    }

    public static boolean isStarChartLayer(int cat) {
        return cat == AstroCatalog.PGC_CATALOG;
    }

    /**
     * @param objname - obj name
     * @return database best associated with the object name
     * or -1 if not
     */
    public static int getAssociatedDatabase(String objname) {
        int db;
        objname = objname.toUpperCase();
        if (objname.startsWith(NGC)) {
            db = AstroCatalog.NGCIC_CATALOG;
        } else if (objname.startsWith(IC)) {
            db = AstroCatalog.NGCIC_CATALOG;
        } else if (objname.startsWith(UGC)) {
            db = AstroCatalog.UGC;
        } else if (objname.startsWith(LDN)) {
            db = AstroCatalog.DNLYNDS;
        } else if (objname.matches(B_0_9)) {
            db = AstroCatalog.DNBARNARD;
        } else if (objname.startsWith(LBN)) {
            db = AstroCatalog.BNLYNDS;
        } else if (objname.startsWith(SH2)) {
            db = AstroCatalog.SH2;
        } else if (objname.startsWith(PK)) {
            db = AstroCatalog.PK;
        } else if (objname.startsWith(ABELL)) {
            db = AstroCatalog.ABELL;
        } else if (objname.startsWith(HCG)) {
            db = AstroCatalog.HCG;
        } else if (objname.startsWith(WDS)) {
            db = AstroCatalog.WDS;
        } else if (objname.startsWith(PGC)) {
            db = AstroCatalog.PGC;
        } else if (objname.startsWith("M")) {
            db = AstroCatalog.MESSIER;
        } else if (objname.startsWith("C")) {
            db = AstroCatalog.CALDWELL;
        } else if (objname.startsWith(HR)) {
            db = AstroCatalog.YALE_CATALOG;
        } else if (objname.matches(_0_9_0_9))
            db = AstroCatalog.WDS;
        else
            db = -1;
        return db;
    }

    /**
     * check if the name could be considered an own name, not thing like NGC... UGC... etc
     *
     * @param name
     * @return
     */
    public static boolean isOwnNameCandidate(String objname) {
        objname = objname.toUpperCase().replace(" ", "");
        if (objname.matches("NGC[0-9]+.*")) {
            return false;
        } else if (objname.matches("IC[0-9]+.*")) {
            return false;
        } else if (objname.matches("UGC[0-9]+.*")) {
            return false;
        } else if (objname.matches("LDN[0-9]+.*")) {
            return false;
        } else if (objname.matches(B_0_9)) {
            return false;
        } else if (objname.matches("LBN[0-9]+.*")) {
            return false;
        } else if (objname.matches("SH2-[0-9]+.*")) {
            return false;
        } else if (objname.matches("ABELL[0-9]+.*")) {
            return false;
        } else if (objname.matches("HCG[0-9]+.*")) {
            return false;
        } else if (objname.matches("WDS[0-9]+.*")) {
            return false;
        } else if (objname.matches("PGC[0-9]+.*")) {
            return false;
        } else if (objname.matches("M[0-9]+")) {
            return false;
        } else if (objname.matches("C[0-9]+")) {
            return false;
        } else if (objname.matches("HR[0-9]+")) {
            return false;
        } else if (objname.matches(_0_9_0_9))
            return false;
        return true;
    }

    public static final int EMPTY_RULE_INCLUDE = 0;
    public static final int EMPTY_RULE_EXCLUDE = 1;
    public static final int EMPTY_RULE_ZERO = 2;
    public static final int EMPTY_RULE_MAX = 3;

    /**
     * @param context
     * @param nomag   use true for comets/minor planets
     * @return sql query for DSO Selection
     */
    public static String createQueryNearby(Context context) {
        int empty = SettingsActivity.getEmptyRule();
        String s = "";

        //TYPES
        if (SettingsActivity.getAST(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.AST;

        }
        if (SettingsActivity.getCG(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.CG;

        }

        if (SettingsActivity.getComet(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.Comet;
        }
        if (SettingsActivity.getDN(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.DN;
        }
        if (SettingsActivity.getGC(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.GC;

        }
        if (SettingsActivity.getGxy(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.Gxy;
        }
        if (SettingsActivity.getGxyCld(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.GxyCld;
        }
        if (SettingsActivity.getHIIRgn(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.HIIRgn;
        }
        if (SettingsActivity.getMinorPlanet(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.MINOR_PLANET;
        }
        if (SettingsActivity.getNeb(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.Neb;
        }
        if (SettingsActivity.getOC(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.OC;
        }
        if (SettingsActivity.getOCNeb(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.OCNeb;
        }
        if (SettingsActivity.getPN(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.PN;
        }
        if (SettingsActivity.getSNR(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.SNR;
        }

        if (SettingsActivity.getStar(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.Star;
        }
        if (SettingsActivity.getDS(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.DoubleStar;
        }

        if (SettingsActivity.getCustom(context, SettingsActivity.SEARCH_NEARBY)) {
            s = s + OR + TYPE + "=" + AstroObject.Custom;
        }
        if (s.length() != 0) {

            s = s.substring(4, s.length());
            s = "(" + s + ")";
        } else
            s = "(" + TYPE + "=0)"; //show empty result
        return s;
    }


    /**
     * @param context
     * @param nomag   use true for comets/minor planets
     * @return sql query for DSO Selection
     */
    public static String createQuery(Context context, boolean nomag, boolean ngcic, boolean double_star) {
        int empty = SettingsActivity.getEmptyRule();

        String s = "";

        //TYPES
        if (SettingsActivity.getAST(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.AST;

        }
        if (SettingsActivity.getCG(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.CG;

        }

        if (SettingsActivity.getComet(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.Comet;
        }
        if (SettingsActivity.getDN(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.DN;
        }
        if (SettingsActivity.getGC(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.GC;

        }
        if (SettingsActivity.getGxy(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.Gxy;
        }
        if (SettingsActivity.getGxyCld(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.GxyCld;
        }
        if (SettingsActivity.getHIIRgn(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.HIIRgn;
        }
        if (SettingsActivity.getMinorPlanet(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.MINOR_PLANET;
        }
        if (SettingsActivity.getNeb(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.Neb;
        }
        if (SettingsActivity.getOC(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.OC;
        }
        if (SettingsActivity.getOCNeb(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.OCNeb;
        }
        if (SettingsActivity.getPN(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.PN;
        }
        if (SettingsActivity.getSNR(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.SNR;
        }

        if (SettingsActivity.getStar(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.Star;
        }
        if (SettingsActivity.getDS(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.DoubleStar;
        }

        if (SettingsActivity.getCustom(context, SettingsActivity.DSO_SELECTION)) {
            s = s + OR + TYPE + "=" + AstroObject.Custom;
        }
        if (s.length() != 0) {

            s = s.substring(4, s.length());
            s = "(" + s + ")";
        } else
            s = "(" + TYPE + "=0)"; //show empty result


        //DIMESION
        double f = SettingsActivity.getDimension();

        switch (empty) {
            //include objects into search
            case EMPTY_RULE_INCLUDE:
                s = s + AND2 + "(" + Constants.A + ">=" + f + OR_A_IS_NULL;
                break;
            //exclude objects from search
            case EMPTY_RULE_EXCLUDE:
                s = s + AND2 + "(" + Constants.A + ">=" + f + ")";
                break;
            //treat as zero
            case EMPTY_RULE_ZERO:
                s = s + AND2 + COALESCE_A_0 + f + ")";
                break;
            case EMPTY_RULE_MAX:
                s = s + AND2 + COALESCE_A_1000000 + f + ")";
                break;
        }


        String checkstr = SettingsActivity.getStringFromSharedPreferences(context, Constants.LIST_SELECTOR_CHECKS, "");

        if (!"".equals(checkstr)) {
            boolean[] checks = SettingsActivity.retrieveBooleanArrFromString(checkstr);
            String str = "";
            for (int i = 0; i < checks.length; i++) {
                if (checks[i]) {
                    str = str + OR_CONSTELLATION + (i + 1);
                }
            }
            if (!"".equals(str)) {
                str = str.substring(4);
                s = s + AND3 + str + ") ";
            }
        }


        if (!nomag) {
            //MAG
            int filter = SettingsActivity.getFilter();
            //mag filter
            if (filter == 1) {
                double maxmag = SettingsActivity.getMaxMag();

                switch (empty) {
                    //include objects into search
                    case 0:
                        s = s + AND + MAG + maxmag + OR_MAG_IS_NULL;
                        break;
                    case 1:
                        s = s + AND + MAG + maxmag + ")";
                        break;
                    case 2:
                        s = s + AND + COALESCE_MAG_0 + maxmag + ")";
                        break;
                    case 3:
                        s = s + AND + COALESCE_MAG_1000000 + maxmag + ")";
                        break;
                }


            }
        }

        String start_with = SettingsActivity.getBasicSearchNameStartWith(context);
        if (!"".equals(start_with))
            s = s + AND_NAME1_LIKE + start_with + "%'" + (!ngcic ? OR_NAME2_LIKE + start_with + "%'" : "") + ")";


        if (double_star) {
            double sepmin = SettingsActivity.getMinSeparation(context);
            double sepmax = SettingsActivity.getMaxSeparation(context);
            double mag2 = SettingsActivity.getMaxMag2(context);
            s = s + AND_SEPARATION + sepmin + AND_SEPARATION2 + sepmax + AND_MAG2 + mag2 + ")";
        }
        return s;
    }


    public static List<AstroObject> searchForName(int db, String name, Context context) {
        List<AstroObject> list = new ArrayList<AstroObject>();
        if (db == AstroCatalog.YALE_CATALOG) {
            HrStar star = AstroTools.getHrStar(name);
            if (star != null) {
                list.add(star);

            }
            return list;
        }

        AstroCatalog cdb;
        if (db == -1)
            return list;

        DbListItem item = DbManager.getDbListItem(db);
        if (item == null)
            return new ArrayList<AstroObject>();
        cdb = getCatalog(item, context);
        if (cdb == null)
            return new ArrayList<AstroObject>();
        ErrorHandler eh = new ErrorHandler();
        cdb.open(eh);
        if (!eh.hasError()) {
            list = cdb.searchName(name);
            cdb.close();
            return list;

        } else
            return list;
    }

    /**
     * @param db
     * @param s       - where clause
     * @param context
     * @return
     */
    public static List<AstroObject> search(int db, String s, Context context) {
        AstroCatalog cdb;
        if (db == -1)
            return new ArrayList<AstroObject>();

        DbListItem item = DbManager.getDbListItem(db);
        if (item == null)
            return new ArrayList<AstroObject>();

        cdb = getCatalog(item, context);
        if (cdb == null)
            return new ArrayList<AstroObject>();

        ErrorHandler eh = new ErrorHandler();
        cdb.open(eh);
        if (!eh.hasError()) {
            List<AstroObject> list = cdb.search(s);
            cdb.close();
            return list;

        } else
            return new ArrayList<AstroObject>();
    }

    /**
     * @param cat catalog number
     * @return null if catalog not found
     */
    public static AstroCatalog getCatalog(int cat, Context context) {
        DbListItem item = DbManager.getDbListItem(cat);
        if (item == null) return null;
        return getCatalog(item, context);
    }

    public static AstroCatalog getCatalog(DbListItem item, Context context) {
        switch (item.cat) {
            case AstroCatalog.NGCIC_CATALOG:
                return new NgcicDatabase(context);
            case AstroCatalog.COMET_CATALOG:
                return new CometsDatabase(context, AstroCatalog.COMET_CATALOG);
            case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
                return new CometsDatabase(context, AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG);
        }

        if (item.ftypes.isEmpty())
            return new CustomDatabase(context, item.dbFileName, item.cat);
        else
            return new CustomDatabaseLarge(context, item.dbFileName, item.cat, item.ftypes);
    }


    static final int MESSIER = 100;
    static final int CALDWELL = 101;
    static final int HERSHELL = 102;
    /**
     * catalog number - set of types
     */
    private static Map<Integer, int[]> catmap = new HashMap<Integer, int[]>();

    static {
        final int[] messier = new int[]{1, 2, 5, 6, 7, 8};
        final int[] caldwell = new int[]{1, 2, 5, 6, 7, 8, 9};
        final int[] hershell = new int[]{1, 2, 5, 6, 7, 8};
        final int[] ngcic = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        final int[] sac = new int[]{1, 2, 3, 5, 6, 7, 8, 9, 10, 12};
        final int[] ugc = new int[]{AstroObject.Gxy};
        final int[] bnlynds = new int[]{AstroObject.Neb};
        final int[] dnlynds = new int[]{AstroObject.DN};
        final int[] barnard = new int[]{AstroObject.DN};
        final int[] comet = new int[]{AstroObject.Comet};
        final int[] mp = new int[]{AstroObject.MINOR_PLANET};
        final int[] wds = new int[]{AstroObject.DoubleStar};
        catmap.put(MESSIER, messier);
        catmap.put(CALDWELL, caldwell);
        catmap.put(HERSHELL, hershell);
        catmap.put(AstroCatalog.NGCIC_CATALOG, ngcic);
        catmap.put(AstroCatalog.SAC, sac);
        catmap.put(AstroCatalog.UGC, ugc);
        catmap.put(AstroCatalog.BNLYNDS, bnlynds);
        catmap.put(AstroCatalog.DNLYNDS, dnlynds);
        catmap.put(AstroCatalog.DNBARNARD, barnard);
        catmap.put(AstroCatalog.COMET_CATALOG, comet);
        catmap.put(AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG, mp);
        catmap.put(AstroCatalog.WDS, wds);
    }
}
