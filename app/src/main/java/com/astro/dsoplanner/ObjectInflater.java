package com.astro.dsoplanner;

import static com.astro.dsoplanner.Constants.A;
import static com.astro.dsoplanner.Constants.B;
import static com.astro.dsoplanner.Constants.BAYER;
import static com.astro.dsoplanner.Constants.CATALOG;
import static com.astro.dsoplanner.Constants.COMMENT;
import static com.astro.dsoplanner.Constants.CONSTEL;
import static com.astro.dsoplanner.Constants.DATE;
import static com.astro.dsoplanner.Constants.DEC;
import static com.astro.dsoplanner.Constants.FLAMSTEED;
import static com.astro.dsoplanner.Constants.HIP;
import static com.astro.dsoplanner.Constants.HR;
import static com.astro.dsoplanner.Constants.ID;
import static com.astro.dsoplanner.Constants.LOCAL_SEARCH_STRING;
import static com.astro.dsoplanner.Constants.MAG;
import static com.astro.dsoplanner.Constants.NAME;
import static com.astro.dsoplanner.Constants.NAME1;
import static com.astro.dsoplanner.Constants.NAME2;
import static com.astro.dsoplanner.Constants.NOTE;
import static com.astro.dsoplanner.Constants.NOTEBASEID;
import static com.astro.dsoplanner.Constants.PA;
import static com.astro.dsoplanner.Constants.PATH;
import static com.astro.dsoplanner.Constants.RA;
import static com.astro.dsoplanner.Constants.SELECTED;
import static com.astro.dsoplanner.Constants.SQL_SEARCH_STRING;
import static com.astro.dsoplanner.Constants.TIME;
import static com.astro.dsoplanner.Constants.TYC1;
import static com.astro.dsoplanner.Constants.TYC2;
import static com.astro.dsoplanner.Constants.TYC3;
import static com.astro.dsoplanner.Constants.TYPE;
import static com.astro.dsoplanner.Constants.TYPESTR;
import static com.astro.dsoplanner.Constants.constellations;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.Comet;
import com.astro.dsoplanner.base.ContourObject;
import com.astro.dsoplanner.base.CustomObject;
import com.astro.dsoplanner.base.CustomObjectLarge;
import com.astro.dsoplanner.base.DoubleStarObject;
import com.astro.dsoplanner.base.Exportable;
import com.astro.dsoplanner.base.Fields;
import com.astro.dsoplanner.base.HrStar;
import com.astro.dsoplanner.base.MinorPlanet;
import com.astro.dsoplanner.base.NgcicObject;
import com.astro.dsoplanner.base.PgcObject;
import com.astro.dsoplanner.base.Planet;
import com.astro.dsoplanner.base.TychoStar;
import com.astro.dsoplanner.base.Ucac4Star;
import com.astro.dsoplanner.database.CustomDatabase;
import com.astro.dsoplanner.database.CustomDatabaseLarge;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.NgcicDatabase;
import com.astro.dsoplanner.database.NoteDatabase;
import com.astro.dsoplanner.infolist.ObsInfoListImpl;

public class ObjectInflater implements Exportable.Inflater {

    private static final String DOUBLE_EXPECTED_FOR_THE_FIELD = "Double expected for the field ";
    private static final String INTEGER_EXPECTED_FOR_THE_FIELD = "Integer expected for the field ";
    private static final String WRONG_FIELD = "Wrong field ";
    private static final String PGC = "PGC";
    private static final String COULD_NOT_RECOGNISE_THE_FORMAT = "could not recognise the format";
    private static final String IC2 = "ic";
    private static final String IC = "IC";
    private static final String NGC = "NGC";

    private static final String TAG = ObjectInflater.class.getSimpleName();
    private static ObjectInflater objInf = new ObjectInflater();
    private static Map<String, Integer> conMap = new HashMap<String, Integer>();

    static {
        for (int i = 1; i < constellations.length; i++) {
            conMap.put(constellations[i], i);
        }
    }

    private ObjectInflater() {
    }

    private static String trim(String s) {
        return AstroTools.trim(s);
    }

    public synchronized static ObjectInflater getInflater() {
        return objInf;//this is needed for multi threading!!!
    }

    //important to be syncronized as there is access to databases
    public synchronized Object inflate(int classTypeId, DataInputStream stream) {
        try {
            switch (classTypeId) {
                case Exportable.NGCIC_OBJECT:
                    return new NgcicObject(stream);
                case Exportable.OBS_LIST_ITEM:
                    ObsInfoListImpl.Item item = new ObsInfoListImpl.Item(stream);
                    if (item.x == null)
                        return null;
                    else
                        return item;
                case Exportable.PLANET_OBJECT:
                    AstroObject obj = new Planet(stream);
                    for (Planet p : Global.planets) {
                        if (p.getPlanetType().ordinal() == obj.getId())
                            return p;
                    }
                    return null;
                case Exportable.HR_STAR_OBJECT:
                    return new HrStar(stream);
                case Exportable.TYCHO_STAR_OBJECT:
                    return new TychoStar(stream);
                case Exportable.UCAC4_STAR_OBJECT:
                    return new Ucac4Star(stream);
                case Exportable.CUSTOM_OBJECT:
                    return new CustomObject(stream);
                case Exportable.CUSTOM_OBJECT_LARGE:
                    return new CustomObjectLarge(stream);
                case Exportable.DS_OBJECT:
                    return new DoubleStarObject(stream);
                case Exportable.DB_LIST_ITEM:
                    return new DbListItem(stream);
                case Exportable.SEARCH_REQUEST_ITEM:
                    return new SearchRequestItem(stream);
                case Exportable.LOCATION_ITEM:
                    return new LocationItem(stream);
                case Exportable.CONTOUR_OBJECT:
                    return new ContourObject(stream);
                case Exportable.COMET_OBJECT:
                    return new Comet(stream);
                case Exportable.MINOR_PLANET_OBJECT:
                    return new MinorPlanet(stream);
                case Exportable.SH_ITEM:
                    return new ShItem(stream);
                case Exportable.NGC_PIC_ITEM:
                    return new NgcPicListItem(stream);
            }
            return null;
        } catch (IOException IOe) {
            return null;
        }
    }

    public static CustomObject ngcicToCustom(NgcicObject obj) {
        CustomObject cobj = new CustomObject(AstroCatalog.CUSTOM_CATALOG, 0, obj.getRa(), obj.getDec(), obj.getCon(), obj.getType(), "", obj.getA(), obj.getB(), obj.getMag(), obj.getPA(), obj.getNgcIcName(), obj.getNgcIcName(), obj.getComment());
        return cobj;
    }

    /**
     * use when there are no restrictions on the additional fields
     *
     * @param ignoreCustomDbRefs - true, to inflate other users databases (and thus references to customs and note databases are ignored)
     *                           false, to inflate own database (references are not ignored)
     */
    public synchronized Object inflate(Map<String, String> map, ErrorHandler.ErrorRec erec, boolean ignoreCustomDbRefs, boolean ignoreNgcicRefs) {
        return inflate(map, erec, null, ignoreCustomDbRefs, ignoreNgcicRefs);
    }

    /**
     * @param map                - map to inflate
     * @param erec               - error rec to trace errors
     * @param ftypes             - to inflate custom objects with additional fields
     * @param ignoreCustomDbRefs - true, to inflate other users databases (and thus references to customs and note databases are ignored)
     *                           false, to inflate own database (references are not ignored)
     * @param ignoreNgcicRefs    - true, ignore refs to internal dbs; false - use refs to internal dbs
     * @return
     */
    public synchronized Object inflate(Map<String, String> map, ErrorHandler.ErrorRec erec, DbListItem.FieldTypes ftypes,
                                       boolean ignoreCustomDbRefs, boolean ignoreNgcicRefs) {//need to add constellation???

        map = removeWhiteSpaces(map, ftypes);

        if (map.get(NOTE) != null || map.get(NOTEBASEID) != null || map.get(DATE) != null || map.get(TIME) != null || map.get(PATH) != null) {
            NoteRecord rec = new NoteInflater(map).inflate(ignoreCustomDbRefs, ignoreNgcicRefs);
            if (rec != null)
                return rec;
        }
        //Location Item
        if (map.get(LocationItem.LATTITUDE) != null || map.get(LocationItem.LONGITUDE) != null) {
            LocationItem item = new LocationInflater(map).inflate();
            if (item != null)
                return item;
        }
        //Telescope
        if (map.get(TelescopeRecord.TAPERTURE) != null) {
            TelescopeRecord rec = new TelescopeInflater(map).inflate();
            if (rec != null)
                return rec;
        }

        //Eyepiece
        if (map.get(EyepiecesRecord.EAFOV) != null) {
            EyepiecesRecord rec = new EyepiecesInflater(map).inflate();
            if (rec != null)
                return rec;
        }


        if (map.get(SQL_SEARCH_STRING) != null) {
            String name = map.get(NAME);
            if (name == null) name = "";
            String sql = map.get(SQL_SEARCH_STRING);
            if (sql == null) sql = "";
            String local = map.get(LOCAL_SEARCH_STRING);
            if (local == null) local = "";
            return new SearchRequestItem(name, sql, local);
        }
        //trying for NGC and IC designations
        String ngcStr = "";
        for (Map.Entry<String, String> e : map.entrySet()) {
            String key = e.getKey();
            if (key != null) {
                if (key.toUpperCase().equals(NGC))
                    ngcStr = e.getValue();
            }
        }

        int ngcNum = 0;
        boolean ngcError = false;
        try {
            ngcNum = Integer.parseInt(ngcStr.replace("\n", "").trim());
        } catch (Exception e) {
            ngcError = true;
        }
        if (!ngcError) {
            AstroCatalog catalog = new NgcicDatabase();
            ErrorHandler eh = new ErrorHandler();
            catalog.open(eh);
            if (!eh.hasError()) {
                List<AstroObject> list = catalog.search(NAME + "=" + ngcNum);
                catalog.close();
                if (list.size() > 0) {
                    if (ignoreNgcicRefs)
                        return ngcicToCustom((NgcicObject) list.get(0));
                    else
                        return list.get(0);
                }
            }
        }

        String icStr = map.get(IC);
        if (icStr == null)
            icStr = map.get(IC2);

        int icNum = 0;
        boolean icError = false;
        try {
            icNum = Integer.parseInt(icStr.replace("\n", "").trim());
        } catch (Exception e) {
            icError = true;
        }
        if (!icError) {
            icNum = icNum + 10000;
            AstroCatalog catalog = new NgcicDatabase();
            ErrorHandler eh = new ErrorHandler();
            catalog.open(eh);
            if (!eh.hasError()) {
                List<AstroObject> list = catalog.search(NAME + "=" + icNum);
                catalog.close();
                if (list.size() > 0) {
                    if (ignoreNgcicRefs)
                        return ngcicToCustom((NgcicObject) list.get(0));
                    else
                        return list.get(0);
                }

            }
        }

        String catStr = map.get(CATALOG);
        String idStr = map.get(ID);
        String selectedStr = map.get(SELECTED);
        Boolean selected = null;
        try {
            selected = Boolean.parseBoolean(selectedStr.replace("\n", "").trim());
        } catch (Exception e) {

        }
        int cat = -1;
        int id = -1;
        boolean catError = false;
        try {
            cat = Integer.parseInt(catStr.replace("\n", "").trim()); //may throw exception when catStr==null
            id = Integer.parseInt(idStr.replace("\n", "").trim());
        } catch (Exception e) {
            catError = true;
        }
        if (!catError) {
            //user dbs & own list
            if (!ignoreCustomDbRefs && !SearchRules.isInternalCatalog(cat)) {
                DbListItem item = AstroTools.findItemByCatId(cat);

                if (item != null) {//there is a database given by obj catalog and gatalog id
                    AstroCatalog catalog;
                    if (item.ftypes.isEmpty())
                        catalog = new CustomDatabase(Global.getAppContext(), item.dbFileName, cat);
                    else
                        catalog = new CustomDatabaseLarge(Global.getAppContext(), item.dbFileName, cat, item.ftypes);
                    ErrorHandler eh = new ErrorHandler();
                    catalog.open(eh);
                    if (!eh.hasError()) {
                        AstroObject obj = catalog.getObject(id);
                        catalog.close();
                        if (obj != null) {
                            if (selectedStr != null)
                                return new ObsInfoListImpl.Item(obj, selected);
                            else
                                return obj;
                        }
                    }
                }
            }
            //internal databases , not CMO

            if (!ignoreNgcicRefs && SearchRules.isInternalCatalog(cat) && (!(cat == AstroCatalog.COMET_CATALOG || cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG))) {
                List<AstroObject> list = SearchRules.search(cat, "_id=" + id, Global.getAppContext());
                if (list.size() > 0) {
                    AstroObject obj = list.get(0);
                    if (selectedStr != null)
                        return new ObsInfoListImpl.Item(obj, selected);
                    else
                        return obj;
                }
            }


            //CMO if user said that this is his database
            if (!ignoreNgcicRefs && !ignoreCustomDbRefs && (cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG || cat == AstroCatalog.COMET_CATALOG)) {
                List<AstroObject> list = SearchRules.search(cat, "_id=" + id, Global.getAppContext());
                if (list.size() > 0) {
                    AstroObject obj = list.get(0);
                    if (selectedStr != null)
                        return new ObsInfoListImpl.Item(obj, selected);
                    else
                        return obj;
                }
            }

            switch (cat) {

                case AstroCatalog.PLANET_CATALOG:
                    Planet planet = null;
                    for (Planet p : Global.planets) {
                        if (p.getPlanetType().ordinal() == id)
                            planet = p;
                    }
                    if (planet != null) {
                        if (selectedStr != null)
                            return new ObsInfoListImpl.Item(planet, selected);
                        else
                            return planet;
                    }
                    break;


            }
        }
        double ra = 0;
        double dec = 0;
        String raStr = map.get(RA);
        String decStr = map.get(DEC);
        try {
            ra = Double.parseDouble(raStr.trim());
            dec = Double.parseDouble(decStr.trim());
        } catch (Exception e) {

        }

        double mag = 0;
        String magStr = map.get(MAG);
        try {
            mag = Double.parseDouble(magStr.replace("\n", "").trim());
        } catch (Exception e) {
        }

        int con = 0;
        String conStr = map.get(CONSTEL);
        if (conStr != null) {
            Integer i = conMap.get(conStr.replace("\n", ""));
            if (i != null)
                con = i;
        }
        if (con == 0)
            con = AstroTools.getConstellation(ra, dec);

        if (cat == AstroCatalog.YALE_CATALOG || cat == AstroCatalog.TYCHO_CATALOG || cat == AstroCatalog.UCAC2_CATALOG || cat == AstroCatalog.UCAC4_CATALOG) {
            int hr = 0;
            int fl = 0;
            int bayer = 0;
            int tyc1 = 0;
            int tyc2 = 0;
            int tyc3 = 0;
            int zone = 0;
            int zoneid = 0;
            int hip = 0;
            try {
                tyc1 = Integer.parseInt(map.get(TYC1).replace("\n", "").trim());
                tyc2 = Integer.parseInt(map.get(TYC2).replace("\n", "").trim());
                tyc3 = Integer.parseInt(map.get(TYC3).replace("\n", "").trim());
            } catch (Exception e) {
            }
            try {
                zone = Integer.parseInt(map.get(Ucac4Star.UZONE).replace("\n", "").trim());
                zoneid = Integer.parseInt(map.get(Ucac4Star.UZONEID).replace("\n", "").trim());
            } catch (Exception e) {
            }
            try {
                hr = Integer.parseInt(map.get(HR).replace("\n", "").trim());
                fl = Integer.parseInt(map.get(FLAMSTEED).replace("\n", "").trim());
                bayer = Integer.parseInt(map.get(BAYER).replace("\n", "").trim());

                hip = Integer.parseInt(map.get(HIP).replace("\n", "").trim());
            } catch (Exception e) {
            }

            //	(int hr,double ra,double dec,double mag,int fl,int bayer,int con,int tyc1,int tyc2,int tyc3,int hip)

            AstroObject obj;

            if (zone != 0 && zoneid != 0) {
                obj = new Ucac4Star(ra, dec, mag, con, zone, zoneid);
            } else if (tyc1 != 0 && tyc2 != 0 && tyc3 != 0) {
                obj = new TychoStar(ra, dec, mag, con, tyc1, tyc2, tyc3);
            } else {
                obj = AstroTools.getHrStar("HR" + hr);
                if (obj == null) return null;
            }


            if (selectedStr != null)
                return new ObsInfoListImpl.Item(obj, selected);
            else
                return obj;
        }


        //custom object
        if (mag == 0) mag = Double.NaN;
        int type = AstroObject.Custom;
        String typeStr = map.get(TYPESTR);
        if (typeStr == null)
            typeStr = "";
        else
            typeStr = typeStr.replace("\n", "");
        String typeS = map.get(TYPE);
        if (typeS != null) {
            typeS = typeS.replace("\n", "");
            String typeSUp = typeS.toUpperCase();
            Integer i = AstroObject.typeMap.get(typeSUp);
            if (i != null) {
                type = i;
            }
        }

        String name1 = map.get(NAME1);
        if (name1 == null)
            name1 = "";
        else
            name1 = name1.replace("\n", "");
        String name2 = map.get(NAME2);
        if (name2 == null)
            name2 = name1;
        else
            name2 = name2.replace("\n", "");

        String comment = map.get(COMMENT);
        if (comment == null)
            comment = "";

        double pa = Double.NaN;
        String paStr = map.get(PA);
        try {
            pa = Double.parseDouble(paStr.replace("\n", "").trim());
        } catch (Exception e) {
        }

        double a = Double.NaN;
        String aStr = map.get(A);
        try {
            aStr = aStr.replace("\n", "").trim();
            a = Double.parseDouble(aStr);
        } catch (Exception e) {
        }

        double b = Double.NaN;
        String bStr = map.get(B);
        try {
            b = Double.parseDouble(bStr.replace("\n", "").trim());
        } catch (Exception e) {
        }

        if (a == 0 && b == 0) {
            if (aStr != null) {
                String[] dimArr = aStr.split("x");
                if (dimArr.length == 2) {
                    try {
                        a = Double.parseDouble(dimArr[0]);
                        b = Double.parseDouble(dimArr[1]);
                    } catch (Exception e) {
                    }
                }
            }
        }


        if (cat == AstroCatalog.PGC_CATALOG) {
            String name = PGC + id;

            CustomObject pgcObject = new PgcObject(AstroCatalog.PGC_CATALOG, id, ra, dec, AstroTools.getConstellation(ra, dec), AstroObject.Gxy, "", a, b, mag, pa, name, name, "");
            if (selectedStr != null)
                return new ObsInfoListImpl.Item(pgcObject, selected);
            else
                return pgcObject;
        }
        //putting custom object in custom database!!!!!!!!!!!!!!!!
        Fields fields = null;

        if (ftypes == null)//there is no restriction on fields
            fields = getFields(map);//use fields as they are
        else
            fields = getFields(map, ftypes, erec);//use imposed fields

        if (fields == null)
            return null;//error

        AstroObject obj;
        if (fields.isEmpty())
            obj = new CustomObject(AstroCatalog.CUSTOM_CATALOG, 0, ra, dec, con, type, typeStr, a, b, mag, pa, name1, name2, comment);
        else
            obj = new CustomObjectLarge(AstroCatalog.CUSTOM_CATALOG, 0, ra, dec, con, type, typeStr, a, b, mag, pa, name1, name2, comment, fields);

        if (selectedStr != null)
            return new ObsInfoListImpl.Item(obj, selected);
        else
            return obj;


    }

    /**
     * removing spaces from key and trimming value
     *
     * @param map
     * @param ftypes
     * @return
     */
    private Map<String, String> removeWhiteSpaces(Map<String, String> map, DbListItem.FieldTypes ftypes) {

        Map<String, String> mapl = new HashMap<String, String>();
        Set<String> setF = new HashSet<String>();
        if (ftypes != null)
            setF = ftypes.getStringFields();
        for (Map.Entry<String, String> e : map.entrySet()) {
            String key = e.getKey().replace(" ", "");
            String value = e.getValue();

            mapl.put(key, trim(value));

        }
        return mapl;
    }


    /**
     * @param map
     * @param ftypes - if this is empty, there is no check of custom object additional
     *               field types (e.g. observation list), if not (new custom database) the check is performed.
     *               if there are wrong types/wrong fields the error is fixed, if some of the additional fields are
     *               missing they are considered to be zero or empty string.
     * @param rec
     * @return
     */
    private Fields getFields(Map<String, String> map, DbListItem.FieldTypes ftypes, ErrorHandler.ErrorRec rec) {
        if (ftypes.isEmpty())
            return getFields(map);

        Set<String> set = new HashSet<String>();
        final String[] arr = new String[]{NOTE, NOTEBASEID, DATE, TIME, PATH, TelescopeRecord.TAPERTURE, EyepiecesRecord.EAFOV, SQL_SEARCH_STRING,
                NAME, LOCAL_SEARCH_STRING, NGC, IC, CATALOG, ID, SELECTED, RA, DEC, MAG, CONSTEL, HR, FLAMSTEED, BAYER, TYC1, TYC2, TYC3, HIP,
                TYPE, TYPESTR, NAME1, NAME2, COMMENT, PA, A, B};
        for (String s : arr) {
            set.add(s.toUpperCase());
        }

        //removing the default fields
        Set<String> keys = new HashSet(map.keySet());
        for (String key : keys) {
            if (set.contains(key.toUpperCase()))
                map.remove(key);//after the cycle map contains additional fields
        }
        Set<String> setM = new HashSet<String>(map.keySet());//set of additional fields
        setM.removeAll(ftypes.getFields());//look if there are fields not specified in ftypes
        if (!setM.isEmpty()) {//there are fields not in ftypes
            rec.type = ErrorHandler.WRONG_TYPE;
            String f = "";
            for (String s : setM) {
                f = f + " " + s + " ";
            }
            rec.message = WRONG_FIELD + f;
            return null;
        }
        Set<String> setF = new HashSet<String>(ftypes.getFields());
        setF.removeAll(map.keySet());//look if there are imposed fields not in the map to assign default values
		

        Fields f = new Fields();
        for (Map.Entry<String, String> e : map.entrySet()) {
            String value = e.getValue();//value for the field
            DbListItem.FieldTypes.TYPE type = ftypes.getType(e.getKey());//defined type for the field
            switch (type) {
                case INT:
                    Integer i = null;
                    try {
                        i = Integer.parseInt(value.replace("\n", "").trim());
                    } catch (Exception ex) {

                    }
                    if (i != null) {//integer
                        f.put(e.getKey(), i);
                        continue;
                    } else {
                        rec.type = ErrorHandler.WRONG_TYPE;
                        rec.message = INTEGER_EXPECTED_FOR_THE_FIELD + e.getKey();
                        return null;
                    }
                case DOUBLE:
                    Double d = null;
                    try {
                        d = Double.parseDouble(value.replace("\n", "").trim());
                    } catch (Exception ex) {

                    }
                    if (d != null) {//integer
                        f.put(e.getKey(), d);
                        continue;
                    } else {
                        rec.type = ErrorHandler.WRONG_TYPE;
                        rec.message = DOUBLE_EXPECTED_FOR_THE_FIELD + e.getKey();
                        return null;
                    }
                case STRING:
                    f.put(e.getKey(), value);//string
                    continue;
                case PHOTO:
                    f.put(e.getKey(), new Fields.Photo(value.replace("\n", "").trim()));
                    continue;
                case URL:
                    f.put(e.getKey(), new Fields.Url(value.replace("\n", "").trim()));
                    continue;
                default:
                    rec.type = ErrorHandler.DATA_CORRUPTED;
                    return null;
            }
        }
        if (!setF.isEmpty()) {//assign default values
            for (String s : setF) {
                DbListItem.FieldTypes.TYPE type = ftypes.getType(s);
                switch (type) {
                    case INT:
                        f.put(s, 0);
                        break;
                    case DOUBLE:
                        f.put(s, 0.0);
                        break;
                    case STRING:
                        f.put(s, "");
                        break;
                    case PHOTO:
                        f.put(s, new Fields.Photo(""));
                        break;
                    case URL:
                        f.put(s, new Fields.Url(""));
                        break;
                }
            }
        }
        return f;

    }

    /**
     * @param map any fields will do. Automatic type definition
     * @return
     */
    private Fields getFields(Map<String, String> map) {
        Set<String> set = new HashSet<String>();
        final String[] arr = new String[]{NOTE, NOTEBASEID, DATE, TIME, PATH, TelescopeRecord.TAPERTURE, EyepiecesRecord.EAFOV, SQL_SEARCH_STRING,
                NAME, LOCAL_SEARCH_STRING, NGC, IC, CATALOG, ID, SELECTED, RA, DEC, MAG, CONSTEL, HR, FLAMSTEED, BAYER, TYC1, TYC2, TYC3, HIP,
                TYPE, TYPESTR, NAME1, NAME2, COMMENT, PA, A, B};
        for (String s : arr) {
            set.add(s.toUpperCase());
        }

        //removing the default fields
        Set<String> keys = new HashSet(map.keySet());
        for (String key : keys) {
            if (set.contains(key.toUpperCase()))
                map.remove(key);
        }
		

        Fields f = new Fields();
        for (Map.Entry<String, String> e : map.entrySet()) {
            String value = e.getValue();
            Integer i = null;
            //boolean flagEx=false;
            try {
                i = Integer.parseInt(value.replace("\n", "").trim());
            } catch (Exception ex) {
                //flagEx=true;
            }
            if (i != null) {//integer
                f.put(e.getKey(), i);
                continue;
            }
            Double d = null;
            try {
                d = Double.parseDouble(value.replace("\n", "").trim());
            } catch (Exception ex) {
            }
            if (d != null) {//double
                f.put(e.getKey(), d);
                continue;
            }
            f.put(e.getKey(), value);//string
        }
        return f;

    }

    class NoteInflater {
        private Map<String, String> map;

        public NoteInflater(Map<String, String> map) {
            this.map = new HashMap<String, String>(map);
        }

        private List<String> mySplit(String s) {
            List<String> list = new ArrayList<String>();
            int i = 0;
            StringBuilder sb = new StringBuilder();
            while (i < s.length()) {
                if (s.charAt(i) != '.') {
                    sb.append(s.charAt(i));
                } else {
                    list.add(sb.toString());
                    sb = new StringBuilder();
                }
                if (i == s.length() - 1)
                    list.add(sb.toString());
                i++;
            }
            return list;
        }

        /**
         * @param ignoreCustomDb - if true, references to note databases are ignored
         * @return
         */
        public NoteRecord inflate(boolean ignoreCustomDb, boolean ignoreNgcic) {

            String nidStr = map.get(NOTEBASEID);//if there is a reference to note database trying to get the note record from there
            int nid = -1;
            NoteRecord nr = null;
            try {
                nid = Integer.parseInt(nidStr.replace("\n", "").trim());
            } catch (Exception e) {
            }
            if (nid != -1 && !ignoreCustomDb) {
                NoteDatabase db = new NoteDatabase();
                ErrorHandler eh = new ErrorHandler();
                db.open(eh);
                if (!eh.hasError()) {
                    NoteRecord rec = db.getNoteRecord(nid);
                    db.close();
                    if (rec != null)
                        return rec;
                }
            }
            String dateStr = map.get(DATE);
            dateStr = dateStr.trim();
            long date = Calendar.getInstance().getTimeInMillis();
            //2009 October 21 02:30
            if (dateStr.matches("[0-9]+[ ]+[a-zA-Z]+[ ]+[0-9]+[ ]+[0-9]+[\\:]+[0-9]+")) {
                String[] a = dateStr.split(" ");
                List<String> list = new ArrayList<String>();
                for (String s : a) {
                    if (!"".equals(s))
                        list.add(s);
                }
                String[] months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                int year = 0;
                int month = -1;
                int day = 0;
                int hour = 0;
                int min = 0;

                boolean exception = false;
                try {
                    year = Integer.parseInt(list.get(0));
                    String mon = list.get(1);


                    for (int i = 0; i < months.length; i++) {

                        if (months[i].equals(mon)) {

                            month = i;
                            break;
                        }

                    }
                    day = Integer.parseInt(list.get(2));
                    String timestr = list.get(3);
                    String[] ta = timestr.split(":");
                    hour = Integer.parseInt(ta[0]);
                    min = Integer.parseInt(ta[1]);
                    if (month == -1)
                        exception = true;

                } catch (Exception e) {
                    exception = true;
                }
                if (!exception) {
                    Calendar c = Calendar.getInstance();
                    c.set(Calendar.YEAR, year);
                    c.set(Calendar.MONTH, month);
                    c.set(Calendar.DAY_OF_MONTH, day);
                    c.set(Calendar.HOUR_OF_DAY, hour);
                    c.set(Calendar.MINUTE, min);
                    date = c.getTimeInMillis();
                }

            } else {
                Calendar c = Calendar.getInstance();
                if (dateStr != null) {
                    dateStr = dateStr.replaceAll(" ", "").replace("\n", "");
                    List<String> d = mySplit(dateStr);
                    if (d.size() == 3) {

                        int year = AstroTools.getInteger(d.get(0), 0, 0, 10000);
                        if (year < 100) year = year + 2000;

                        int month = AstroTools.getInteger(d.get(1), 1, 1, 12);
                        int day = AstroTools.getInteger(d.get(2), 1, 1, 31);

                        c.set(Calendar.YEAR, year);
                        c.set(Calendar.MONTH, month - 1);
                        c.set(Calendar.DAY_OF_MONTH, day);
                    }
                }

                String timeStr = map.get(TIME);
                if (timeStr != null) {
                    String[] hms = timeStr.replace("\n", "").trim().split(":");
                    int hour = AstroTools.getInteger(hms[0], 0, 0, 24);
                    int min = AstroTools.getInteger(hms[1], 0, 0, 60);
                    int sec = AstroTools.getInteger(hms[2], 0, 0, 60);


                    c.set(Calendar.HOUR_OF_DAY, hour);
                    c.set(Calendar.MINUTE, min);
                    c.set(Calendar.SECOND, sec);
                }
                date = c.getTimeInMillis();
            }

            String noteStr = map.get(NOTE);
            if (noteStr == null)
                return null;//no need for this note

            String pathStr = map.get(PATH);
            if (pathStr == null)
                pathStr = "";
            else
                pathStr = pathStr.replace("\n", "").trim();

            String nameStr = map.get(NAME);
            if (nameStr == null)
                nameStr = "";
            else
                nameStr = nameStr.replace("\n", "").trim();
            map.remove(PATH);
            map.remove(NOTE);
            map.remove(TIME);
            map.remove(DATE);
            map.remove(NOTEBASEID);
            map.remove(NAME);
            Object obj = ObjectInflater.this.inflate(map, new ErrorHandler.ErrorRec(), ignoreCustomDb, ignoreNgcic);
            NoteRecord rec = null;
            if (obj instanceof AstroObject) {


                AstroObject o = (AstroObject) obj;
                rec = new NoteRecord(o.getId(), o.getCatalog(), 0, 0, date, noteStr, pathStr, o.getLongName());
				
				

            } else {
                rec = new NoteRecord(0, AstroCatalog.CUSTOM_CATALOG, 0, 0, date, noteStr, pathStr, nameStr);

            }
            return rec;


        }
    }

    class LocationInflater {
        private Map<String, String> map;

        public LocationInflater(Map<String, String> map) {
            this.map = new HashMap<String, String>(map);
        }

        public LocationItem inflate() {

            String name = "";

            String latStr = map.get(LocationItem.LATTITUDE);
            String lonStr = map.get(LocationItem.LONGITUDE);
            name = map.get(LocationItem.NAME);
            if (name == null) return null;
            double lat = AstroTools.getDouble(latStr.replace("\n", "").trim(), 0, -90, 90);
            double lon = AstroTools.getDouble(lonStr.replace("\n", "").trim(), 0, -360, 360);
            return new LocationItem(name, lat, lon);

        }
    }

    class TelescopeInflater {
        private Map<String, String> map;

        public TelescopeInflater(Map<String, String> map) {
            this.map = new HashMap<String, String>(map);
        }

        public TelescopeRecord inflate() {

            String name = map.get(TelescopeRecord.TNAME);
            if (name == null) name = "";
            String ap = map.get(TelescopeRecord.TAPERTURE);
            String fo = map.get(TelescopeRecord.TFOCUS);
            String pa = map.get(TelescopeRecord.TPASS);
            String ep = map.get(TelescopeRecord.TEP);
            String no = map.get(TelescopeRecord.TDESCR);
            if (no == null) no = "";


            TelescopeRecord rec = new TelescopeRecord(0, name.replace("\n", "").trim(), AstroTools.getDouble(ap.replace("\n", "").trim(), 0, 0, 10000),
                    AstroTools.getDouble(fo.replace("\n", "").trim(), 0, 0, 100000), AstroTools.getDouble(pa.replace("\n", "").trim(), 0, 0, 100), AstroTools.getInteger(ep.replace("\n", "").trim(), 0, 0, 100), no.replace("\n", "").trim());

            return rec;
        }
    }

    class EyepiecesInflater {
        private Map<String, String> map;

        public EyepiecesInflater(Map<String, String> map) {
            this.map = new HashMap<String, String>(map);
        }

        public EyepiecesRecord inflate() {

            String name = map.get(EyepiecesRecord.ENAME);
            if (name == null) name = "";
            String fo = map.get(EyepiecesRecord.EFOCUS);
            String af = map.get(EyepiecesRecord.EAFOV);
            String ep = map.get(EyepiecesRecord.EEP);
            if (ep == null) ep = "";
            String no = map.get(EyepiecesRecord.EDESCR);
            if (no == null) no = "";

            EyepiecesRecord rec = new EyepiecesRecord(0,
                    name.replace("\n", "").trim(),
                    AstroTools.getDouble(fo.replace("\n", "").trim(), 0, 0, 1000),
                    AstroTools.getDouble(af.replace("\n", "").trim(), 0, 0, 1000),
                    ep.replace("\n", "").trim(),
                    trim(no.replace("\n", "")));
			
            return rec;
        }
    }


}
