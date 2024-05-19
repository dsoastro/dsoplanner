package com.astro.dsoplanner.database;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.DSOmainActivity;
import com.astro.dsoplanner.DatabaseManagerActivity.DbListFiller;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.Global;


import com.astro.dsoplanner.Prefs;
import com.astro.dsoplanner.QueryActivity;
import com.astro.dsoplanner.SearchRules;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.ListHolder;

/**
 * use this to manage Databases
 *
 * @author leonid
 */
public class DbManager {


    private static final String WDS = "WDS";
    private static final String MINOR_PLANETS = "Minor Planets";
    private static final String COMETS = "Comets";
    private static final String HCG = "HCG";
    private static final String ABELL = "Abell";
    private static final String PK = "PK";
    private static final String SH2 = "SH2";
    private static final String LBN = "LBN";
    private static final String BARNARD = "Barnard";
    private static final String LDN = "LDN";
    private static final String PGC = "PGC";
    private static final String UGC = "UGC";
    private static final String SAC = "SAC";
    private static final String NGC_IC = "NgcIc";
    private static final String HERSCHEL400 = "Herschel400";
    private static final String CALDWELL = "Caldwell";
    private static final String MESSIER = "Messier";
    private static final String ERROR_COPYING_DATABASE = "Error copying database";
    private static final String DB = ".db";
    private static final String ASTRO = "astro";

    private static final String TAG = DbManager.class.getSimpleName();


    /**
     * use this to add new user database
     *
     * @param ftypes
     * @param context
     * @param name     - database name
     * @param calltype - true for ui thread, false for non ui thread. handler could be null
     * @param handler  - use this for non ui thread to show errors. Could be null, the error will not be shown
     * @return the internal name of the db or null if no database created
     */
    public static String addNewDatabase(DbListItem.FieldTypes ftypes, Activity context, String name, boolean calltype, Handler handler) {

        int maxc = getMaxCatalogId();
        String internalName = ASTRO + maxc + DB;
        DbListItem item = new DbListItem(getMaxMenuId(), maxc, name, internalName, ftypes);

        AstroCatalog cat;
        if (ftypes.isEmpty())
            cat = new CustomDatabase(context, item.dbFileName, maxc);
        else
            cat = new CustomDatabaseLarge(context, item.dbFileName, maxc, ftypes);
        ErrorHandler eh = new ErrorHandler();
        cat.open(eh);
        if (eh.hasError()) {
            if (calltype)
                eh.showErrorInToast(context);
            else {
                if (handler != null)
                    eh.showError(context, handler);
            }
            return null;
        }
        cat.close();
        SettingsActivity.putSharedPreferences(Constants.MAX_CATALOG_ID, maxc, context);
        //prefs.edit().putInt(Constants.MAX_CATALOG_ID, maxc).commit();
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        InfoListFiller filler = new DbListFiller(Arrays.asList(new DbListItem[]{item}));
        iL.fill(filler);
        new Prefs(context).saveList(InfoList.DB_LIST);
        Log.d(TAG, "item=" + item);
        return internalName;
    }

    public static String getInternalDbName(int dbid) {
        String internalName = ASTRO + dbid + DB;
        return internalName;
    }

    /**
     * use this to add new internal user database
     *
     * @param ftypes
     * @param context
     * @param name     - database name
     * @param dbid     - number in astro catalog
     * @param calltype - true for ui thread, false for non ui thread. handler could be null
     * @param handler  - use this for non ui thread to show errors. Could be null, the error will not be shown
     * @return the internal name of the db or null if no database created
     */
    public static String addNewInternalDatabase(DbListItem.FieldTypes ftypes, Context context, String name, boolean calltype, Handler handler, int dbid) {
        int maxc = dbid;


        String internalName = getInternalDbName(dbid);
        DbListItem item = new DbListItem(getMaxMenuId(), maxc, name, internalName, ftypes);

        AstroCatalog cat;
        if (ftypes.isEmpty())
            cat = new CustomDatabase(context, item.dbFileName, maxc);
        else
            cat = new CustomDatabaseLarge(context, item.dbFileName, maxc, ftypes);
        ErrorHandler eh = new ErrorHandler();
        cat.open(eh);
        if (eh.hasError()) {
            if (calltype)
                eh.showErrorInToast(context);
            else {
                if (handler != null)
                    eh.showError(context, handler);
            }
            return null;
        }
        cat.close();
        boolean name_changed = false;
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        boolean exists = false;
        for (Object o : iL) {
            DbListItem item2 = (DbListItem) o;
            if (item2.cat == dbid) { //there is such item no need to add
                if (!item2.dbName.equals(name)) {
                    item2.dbName = name;
                    name_changed = true;
                }
                exists = true;
                break;
            }
        }
        if (name_changed) {
            new Prefs(context).saveList(InfoList.DB_LIST);
        }
        if (!exists) {
            InfoListFiller filler = new DbListFiller(Arrays.asList(new DbListItem[]{item}));
            iL.fill(filler);
            new Prefs(context).saveList(InfoList.DB_LIST);
            Log.d(TAG, "item=" + item);
        }
        return internalName;
    }

    /**
     * use this to copy user database
     *
     * @param ftypes
     * @param context
     * @param name    - database name
     * @param f       - file to be copied
     */
    public static void copyNewDatabase(DbListItem.FieldTypes ftypes, Context context, String name, File f) {
        int maxc = getMaxCatalogId();
        DbListItem item = new DbListItem(getMaxMenuId(), maxc, name, ASTRO + maxc + DB, ftypes);

        try {

            DSOmainActivity.copyFile(f, context.getDatabasePath(item.dbFileName));
        } catch (Exception e) {
            ErrorHandler eh = new ErrorHandler();
            ErrorHandler.ErrorRec erec = new ErrorHandler.ErrorRec(ErrorHandler.IO_ERROR, ERROR_COPYING_DATABASE);
            eh.addError(erec);
            eh.showErrorInToast(context);
            return;
        }
        SettingsActivity.putSharedPreferences(Constants.MAX_CATALOG_ID, maxc, context);
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        InfoListFiller filler = new DbListFiller(Arrays.asList(new DbListItem[]{item}));
        iL.fill(filler);
        new Prefs(context).saveList(InfoList.DB_LIST);
    }

    /**
     * @param pos - position in DB_LIST
     */
    public static void removeDatabase(int pos, Context context) {
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        DbListItem item = (DbListItem) iL.get(pos);
        File f = context.getDatabasePath(item.dbFileName);
        boolean result = f.delete();

        iL.remove(pos);
    }

    /**
     * @return this uses a kind of static variable to keep track of all databases ever created.
     * This is necessary to avoid "finding" notes for objects in custom databases
     * which corresponded one time to other now deleted databases
     */
    private static int getMaxCatalogId() {
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        Iterator it = iL.iterator();
        int num = AstroCatalog.NEW_CATALOG_FIRST - 1;
        for (; it.hasNext(); ) {
            DbListItem item = (DbListItem) it.next();
            if (item.cat > num)
                num = item.cat;
        }
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(Global.getAppContext());
        int pnum = prefs.getInt(Constants.MAX_CATALOG_ID, AstroCatalog.NEW_CATALOG_FIRST - 1);
        int fnum = Math.max(num, pnum) + 1;
        Log.d("DM", "getMaxCatalogId=" + fnum);
        return fnum;
    }

    private static int getMaxMenuId() {
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        Iterator it = iL.iterator();
        int num = QueryActivity.MENU_MIN;
        for (; it.hasNext(); ) {
            DbListItem item = (DbListItem) it.next();
            if (item.menuId > num)
                num = item.menuId;
        }
        return num + 1;
    }

    /**
     * @param catalog
     * @return dbname of the catalog, or null if such catalog is not in the database
     */
    public static String getDbName(int catalog) {
        if (SearchRules.isInternalCatalog(catalog) || catalog == AstroCatalog.HERSHEL) {
            switch (catalog) {
                case AstroCatalog.MESSIER:
                    return MESSIER;
                case AstroCatalog.CALDWELL:
                    return CALDWELL;
                case AstroCatalog.HERSHEL:
                    return HERSCHEL400;
                case AstroCatalog.NGCIC_CATALOG:
                    return NGC_IC;
                case AstroCatalog.SAC:
                    return SAC;
                case AstroCatalog.UGC:
                    return UGC;
                case AstroCatalog.PGC:
                    return PGC;
                case AstroCatalog.DNLYNDS:
                    return LDN;
                case AstroCatalog.DNBARNARD:
                    return BARNARD;
                case AstroCatalog.BNLYNDS:
                    return LBN;
                case AstroCatalog.SH2:
                    return SH2;
                case AstroCatalog.PK:
                    return PK;
                case AstroCatalog.ABELL:
                    return ABELL;
                case AstroCatalog.HCG:
                    return HCG;
                case AstroCatalog.WDS:
                    return WDS;
                case AstroCatalog.COMET_CATALOG:
                    return COMETS;
                case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
                    return MINOR_PLANETS;
            }
        }


        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        Iterator it = iL.iterator();

        for (; it.hasNext(); ) {
            DbListItem item = (DbListItem) it.next();
            if (item.cat == catalog)
                return item.dbName;
        }
        return null;
    }

    /**
     * @param catalog
     * @return filename of the catalog
     */
    public static String getDbFileName(int catalog) {
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        Iterator it = iL.iterator();

        for (; it.hasNext(); ) {
            DbListItem item = (DbListItem) it.next();
            if (item.cat == catalog)
                return item.dbFileName;
        }
        return null;
    }

    public static DbListItem getDbListItem(int catalog) {
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        Iterator it = iL.iterator();

        for (; it.hasNext(); ) {
            DbListItem item = (DbListItem) it.next();
            if (item.cat == catalog)
                return item;
        }
        return null;
    }
}
