package com.astro.dsoplanner;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.graph.GraphActivity;
import com.astro.dsoplanner.graph.GraphRec;
import com.astro.dsoplanner.infolist.ObsInfoListImpl;
import com.astro.dsoplanner.infolist.ObsInfoListImpl.Item;
import com.astro.dsoplanner.database.CometsDatabase;
import com.astro.dsoplanner.database.CustomDatabase;
import com.astro.dsoplanner.database.CustomDatabaseLarge;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.DbManager;
import com.astro.dsoplanner.database.NgcicDatabase;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.InfoListImpl;
import com.astro.dsoplanner.infolist.InfoListIteratorFiller;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListStringSaverImp;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.infolist.ObsListFiller;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.util.Holder2;


public class QueryController extends Controller {

    private static final String QCS = "qcs=";
    private static final String IMPORT_IS_ALREADY_RUNNING = "Import is already running";

    private static final String CATALOG_POSITION = "catalogPosition";
    private static final String SEARCH_IS_ALREADY_RUNNING = "Search is already running";
    private static final String WORKER_THREAD = "Worker Thread";

    private static final String TAG = QueryController.class.getSimpleName();
    Activity activity;
    private HandlerThread workerThread;
    private Handler workerHandler;

    public static final int MESSAGE_INIT = 1;
    public static final int MESSAGE_ERROR_HANDLER = 2;
    public static final int MESSAGE_TEXT = 3;
    public static final int MESSAGE_UPDATE_VIEW = 4;
    public static final int MESSAGE_INPROGRESS = 5;
    public static final int MESSAGE_SET_ACTIVITY_NAME = 6;
    public static final int MESSAGE_SAVE = 7;
    public static final int MESSAGE_SET_TIME = 8;
    public static final int MESSAGE_SEE_PICTURE = 9;
    public static final int MESSAGE_ADD_OBSLIST = 10;
    public static final int MESSAGE_ADD_ALL_OBSLIST = 11;
    public static final int MESSAGE_ADD_NOTE = 12;
    public static final int MESSAGE_SEE_NOTES = 13;
    public static final int MESSAGE_SEE_ALL_NOTES = 14;
    public static final int MESSAGE_UPDATE_CATALOG = 15;
    public static final int MESSAGE_UPDATE_LIST = 16;
    public static final int MESSAGE_EXPORT = 17;
    public static final int MESSAGE_SHARE = 18;
    public static final int MESSAGE_EXECUTE_ON_UI_THREAD = 19;
    public static final int MESSAGE_SKY_VIEW = 20;
    public static final int MESSAGE_REMOVE_INPROGRESS = 23;
    public static final int MESSAGE_KEEP_CURRENT_LIST = 24;
    public static final int MESSAGE_FIND = 25;
    public static final int MESSAGE_SET_LIST_LOCATION = 26;
    public static final int MESSAGE_FIND_NEXT = 27;
    public static final int MESSAGE_FIND_RESET = 28;
    public static final int MESSAGE_TEXT_FIND = 29;
    public static final int MESSAGE_UPDATE_FILTER_BTN = 30;
    public static final int MESSAGE_CHECKBOX_CHOSEN = 31;
    public static final int MESSAGE_ADD_MARKED_OBSLIST = 32;
    public static final int MESSAGE_SHOW_ON_STAR_CHART = 33;

    private FindRunner findRunner;
    private boolean skyviewFlag = false;

    /**
     * indicate that SkyView is used with the list
     */
    public void setSkyViewFlag() {
        skyviewFlag = true;
    }

    public void clearSkyViewFlag() {
        skyviewFlag = false;
    }

    public QueryController(Activity activity) {
        this.activity = activity;
        workerThread = new HandlerThread(WORKER_THREAD);
        workerThread.start();
        workerHandler = new Handler(workerThread.getLooper());
        findRunner = new FindRunner();
        findRunner.setMatcher(new FindRunner.Matcher() {
            @Override
            public boolean match(Object o, String searchString) {
                if (o instanceof Item) {
                    AstroObject obj = ((Item) o).x;
                    if (!"".equals(searchString) && obj.getDsoSelName().toUpperCase(Locale.US).contains(searchString.toUpperCase()))
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        workerThread.getLooper().quit();
        disposed = true;
        Log.d(TAG, "controller disposed");
    }

    @Override
    public boolean handleMessage(int what, final Object data) {
        switch (what) {
            case MESSAGE_INIT:
                init((Intent) data);
                return true;

            case MESSAGE_SAVE:
                workerHandler.post(new Runnable() {
                    public void run() {
                        save((Integer) data);//spinPos passed from activity for saving in addition to the other data
                    }
                });
                return true;
            case MESSAGE_SET_TIME:
                Point.setLST(AstroTools.sdTime(AstroTools.getDefaultTime(activity)));
                return true;
            case MESSAGE_SEE_PICTURE:
                Command command = new PictureCommand(activity, ((Item) data).x);
                command.execute();
                return true;
            case MESSAGE_ADD_OBSLIST:
                InfoListFiller filler = new ObsListFiller(Arrays.asList(new Item[]{(Item) data}), false);
                int obsList = SettingsActivity.getSharedPreferences(activity).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

                ListHolder.getListHolder().get(obsList).fill(filler);
                dirtyObsPref = true;
                return true;
            case MESSAGE_ADD_MARKED_OBSLIST:
                List<AstroObject> list2 = new ArrayList<AstroObject>();
                List<Item> li = (List<Item>) data;
                for (Item item : li) {
                    if (item.y)
                        list2.add(item.x);
                }

                InfoListFiller filler2 = new ObsListFiller(list2);
                int obsList2 = SettingsActivity.getSharedPreferences(activity).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

                InfoList iL2 = ListHolder.getListHolder().get(obsList2);
                iL2.fill(filler2);
                dirtyObsPref = true;
                return true;

            case MESSAGE_ADD_ALL_OBSLIST:
                InfoListFiller filler1 = new ObsListFiller((List) data, false);
                int obsList1 = SettingsActivity.getSharedPreferences(activity).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

                InfoList iL = ListHolder.getListHolder().get(obsList1);
                iL.fill(filler1);
                dirtyObsPref = true;
                return true;
            case MESSAGE_ADD_NOTE:
                command = new NewNoteCommand(activity, ((Item) data).x, Calendar.getInstance(), "");
                command.execute();
                return true;
            case MESSAGE_SEE_NOTES:
                command = new GetObjectNotesCommand(activity, ((Item) data).x);
                command.execute();
                return true;
            case MESSAGE_SEE_ALL_NOTES:
                command = new GetAllNotesCommand(activity);
                command.execute();
                return true;
            case MESSAGE_UPDATE_CATALOG:
                if (searchRequestRunning) {
                    notifyOutboxHandlers(MESSAGE_TEXT, 0, 0, SEARCH_IS_ALREADY_RUNNING);
                    return true;
                }
                updateCatalog((DbListItem) data);
                return true;
            case MESSAGE_UPDATE_LIST:
                if (searchRequestRunning) {
                    notifyOutboxHandlers(MESSAGE_TEXT, 0, 0, SEARCH_IS_ALREADY_RUNNING);
                    return true;
                }
                notifyOutboxHandlers(MESSAGE_INPROGRESS, 0, 0, null);
                workerHandler.post(new SearchPerformer());
                dirtyPref = true;
                findRunner.reset();
                return true;
            case MESSAGE_EXPORT:

                workerHandler.post(new Runnable() {
                    public void run() {
                        export(data);
                    }
                });

                return true;
            case MESSAGE_SHARE:
                workerHandler.post(new Runnable() {
                    public void run() {
                        share((List<Item>) data);
                    }
                });
                return true;
            case MESSAGE_SKY_VIEW:
                skyView(((Item) data).x);
                return true;
            case MESSAGE_KEEP_CURRENT_LIST:


                if (dirtyPref) {
                    InfoList list = (InfoList) data;
                    Log.d(TAG, "list size=" + list.getCount());
                    InfoList mainlist = ListHolder.getListHolder().get(InfoList.NGCIC_SELECTION_LIST);
                    if (!mainlist.equals(list)) {//if there was no update then list is just a reference of original list
                        mainlist.setListName("");
                        mainlist.removeAll();
                        mainlist.fill(new InfoListIteratorFiller(list.iterator()));
                        Log.d(TAG, "main list size=" + mainlist.getCount());
                    }
                }
                return true;
            case MESSAGE_FIND:
                Holder2<String, List<Item>> h = (Holder2<String, List<Item>>) data;
                findRunner.setSearch(new FindRunner.BasicListAdapter(h.y), h.x);
                makeFind();
                return true;

            case MESSAGE_FIND_NEXT:
                makeFind();
                return true;

            case MESSAGE_FIND_RESET:
                findRunner.reset();
                return true;
            case MESSAGE_CHECKBOX_CHOSEN:
                dirtyPref = true;
                return true;
        }
        return false;
    }

    private void makeFind() {
        int pos = findRunner.find();
        if (pos > -1)
            notifyOutboxHandlers(MESSAGE_SET_LIST_LOCATION, pos, 0, null);
        else
            notifyOutboxHandlers(MESSAGE_TEXT_FIND, 0, 0, activity.getString(R.string.no_match_found_));

    }

    private volatile boolean searchRequestRunning = false;//maybe more correct to make sync query to this variable
    private volatile boolean importRunning = false;
    private String getActivityName() {
        return SettingsActivity.getSelectedCatalogsSummary(activity, SettingsActivity.DSO_SELECTION);
    }

    private void init(Intent i) {
        String aname = SettingsActivity.getStringFromSharedPreferences(activity, Constants.QUERY_ACTIVITY_NAME, "");
        aname = activity.getString(R.string.dso_selection_) + aname;
        notifyOutboxHandlers(MESSAGE_SET_ACTIVITY_NAME, 0, 0, aname);
        InfoList infoList = ListHolder.getListHolder().get(InfoList.NGCIC_SELECTION_LIST);//info list to work with

        if (infoList.getCount() == 0) {//empty list
            Log.d(TAG, "init, search");
            notifyOutboxHandlers(MESSAGE_INPROGRESS, 0, 0, null);
            SettingsActivity.putSharedPreferences(Constants.SETTINGS_SEARCH_CATALOG_UPDATE, true, activity);
            notifyOutboxHandlers(MESSAGE_UPDATE_FILTER_BTN, 0, 0, null);
            workerHandler.post(new SearchPerformer());
            dirtyPref = true;
        } else {
            SharedPreferences prefs = SettingsActivity.getSharedPreferences(activity);
            int spinPos = prefs.getInt(Constants.QUERY_CONTROLLER_SPIN_POS, 0);
            Log.d(TAG, "init, spin pos=" + spinPos);
            notifyOutboxHandlers(MESSAGE_UPDATE_VIEW, spinPos, 0, infoList);
        }

    }

    private boolean dirtyPref = false;//for tracking pref saving necessity
    private boolean dirtyObsPref = false;//for tracking obs list pref saving

    /**
     * saving current InfoList.NGCIC_SELECTION_LIST. Update it with
     * MESSAGE_KEEP_CURRENT_LIST before saving
     *
     * @param spinPos
     */
    private void save(int spinPos) {

        Set<Integer> set = new HashSet<Integer>();
        if (dirtyPref) {
            set.add(InfoList.NGCIC_SELECTION_LIST);
            dirtyPref = false;
        }
        if (dirtyObsPref) {
            int obsList = SettingsActivity.getSharedPreferences(activity).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

            set.add(obsList);
            dirtyObsPref = false;
        }
        Log.d(TAG, "save, set=" + set);
        if (!set.isEmpty())
            new Prefs(activity).saveLists(set);
        //saving chosen constellation
        SettingsActivity.putSharedPreferences(Constants.QUERY_CONTROLLER_SPIN_POS, spinPos, activity);
    }

    private void updateCatalog(DbListItem dbitem) {
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        int pos = findPos(dbitem.menuId);

        DbListItem item = (DbListItem) iL.get(pos);
        if (ImportDatabaseIntentService.isBeingImported(item.dbFileName)) {
            SettingsActivity.putSharedPreferences(Constants.SETTINGS_SEARCH_CATALOG_UPDATE, false, activity);
            notifyOutboxHandlers(MESSAGE_UPDATE_FILTER_BTN, 0, 0, null);
            notifyOutboxHandlers(MESSAGE_TEXT, 0, 0, Global.DB_IMPORT_RUNNING);
            return;
        }

        notifyOutboxHandlers(MESSAGE_INPROGRESS, 0, 0, null);
        workerHandler.post(new SearchPerformer());
        dirtyPref = true;
        findRunner.reset();
    }

    private int findPos(int menuid) {
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        Iterator it = iL.iterator();
        int pos = 0;
        for (; it.hasNext(); ) {
            DbListItem dbitem = (DbListItem) it.next();
            if (dbitem.menuId == menuid)
                return pos;
            pos++;
        }
        return 0;
    }

    private void export(Object data) {
        boolean noError = true;
        ErrorHandler eh = new ErrorHandler();
        InfoListSaver saver = null;
        try {

            Holder2<String, List<Item>> h = (Holder2<String, List<Item>>) data;
            saver = new InfoListStringSaverImp(
                    new FileOutputStream(Global.exportImportPath + h.x));
            saver.addName("");
            for (Item item : h.y) {
                saver.addObject(item.x);
            }


        } catch (Throwable e) {
            Log.d(TAG, "Exception=" + e);
            noError = false;
        } finally {
            try {
                saver.close();
            } catch (Exception e) {
            }
        }
        notifyOutboxHandlers(MESSAGE_ERROR_HANDLER, 0, 0, eh);
        String message = (!noError ? activity.getString(R.string.export_error_) : activity.getString(R.string.export_successfull_));
        notifyOutboxHandlers(MESSAGE_TEXT, 0, 0, message);


    }

    private void share(List<Item> list) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ErrorHandler eh = new ErrorHandler();
        InfoListSaver saver = new InfoListStringSaverImp(out, Global.SHARE_LINES_LIMIT, eh);
        for (Item o : list) {
            try {
                saver.addObject(o.x);
            } catch (Exception e) {
            }

        }
        notifyOutboxHandlers(MESSAGE_ERROR_HANDLER, 0, 0, eh);
        final String s = out.toString();
        notifyOutboxHandlers(MESSAGE_EXECUTE_ON_UI_THREAD, 0, 0, new Executable() {
            public void run() {
                new ShareCommand(context, s).execute();
            }
        });

    }

    AstroObject prevObject = null;

    private boolean skyViewIntent = false;

    public boolean isIntentToGraph() {
        return skyViewIntent;
    }

    public void clearGraphIntentFlag() {
        skyViewIntent = false;
    }

    private void skyView(AstroObject obj) {
        Calendar defc = AstroTools.getDefaultTime(activity);
        Point.setLST(AstroTools.sdTime(defc));//need to calculate Alt and Az
        SettingsActivity.putSharedPreferences(Constants.GRAPH_OBJECT, obj, activity);
        int zoom = SettingsActivity.getSharedPreferences(activity).getInt(Constants.CURRENT_ZOOM_LEVEL, Constants.DEFAULT_ZOOM_LEVEL);
        obj.recalculateRaDec(defc);
        GraphRec rec = new GraphRec(zoom, obj.getAz(), obj.getAlt(), defc, obj);
        rec.save(activity);//add graph settings for Graph Activity to process it

        if (skyviewFlag && !obj.equals(prevObject)) {
            notifyOutboxHandlers(MESSAGE_SHOW_ON_STAR_CHART, 0, 0, rec);
        } else {


            final Intent i = new Intent(activity, GraphActivity.class);
            notifyOutboxHandlers(MESSAGE_EXECUTE_ON_UI_THREAD, 0, 0, new Executable() {
                public void run() {
                    skyViewIntent = true;
                    context.startActivity(i);
                }
            });
        }
        prevObject = obj;

    }

    class DsoSelFillerMod implements InfoListFiller {
        List<AstroObject> objlist = new ArrayList<AstroObject>();

        public Iterator getIterator() {
            List<ObsInfoListImpl.Item> itemlist = new ArrayList<ObsInfoListImpl.Item>();
            for (AstroObject obj : objlist) {
                itemlist.add(new Item(obj, false));
            }
            objlist = null;
            return itemlist.iterator();
        }

        public DsoSelFillerMod() {
            List<Integer> catlist = SettingsActivity.getSelectedInternalCatalogs(activity, SettingsActivity.DSO_SELECTION);
            if (catlist.contains(AstroCatalog.HERSHEL)) {
                if (!catlist.contains(AstroCatalog.NGCIC_CATALOG))
                    catlist.add(AstroCatalog.NGCIC_CATALOG);
            }

            catlist.addAll(SettingsActivity.getCatalogSelectionPrefs(activity, SettingsActivity.DSO_SELECTION));
            for (int cat : catlist) {
                if (QueryActivity.isStopping())
                    break;
                List<AstroObject> la = search(cat);
                Log.d(TAG, "searching " + cat + " size=" + la.size());
                int totalsize = la.size() + objlist.size();
                if (totalsize > Global.SQL_SEARCH_LIMIT) {
                    int neededsize = Global.SQL_SEARCH_LIMIT - objlist.size();
                    if (neededsize <= 0)
                        break;
                    neededsize = Math.min(neededsize, la.size());

                    List<AstroObject> la2 = new ArrayList<AstroObject>();
                    for (int i = 0; i < neededsize; i++) {
                        la2.add(la.get(i));
                    }
                    addObjects(la2);

                    break;

                } else {
                    addObjects(la);
                }
            }

        }

        private void addObjects(List<AstroObject> list) {

            if (SettingsActivity.isRemovingDuplicates()) {
                Log.d(TAG, "removing dups, before " + list.size());
                list.removeAll(objlist);
                Log.d(TAG, "removing dups, after " + list.size());
                objlist.addAll(list);
            } else {
                objlist.addAll(list);
            }
        }

        private List<AstroObject> search(int cat) {
            Log.d(TAG, "searching " + cat);
            List<AstroObject> list = new ArrayList<AstroObject>();
            DbListItem dbitem = DbManager.getDbListItem(cat);
            if (dbitem == null)
                return list;
            ErrorHandler eh = new ErrorHandler();
            AstroCatalog catalog = getCurrentCatalog(dbitem);


            catalog.open(eh);
            if (eh.hasError()) {
                notifyOutboxHandlers(MESSAGE_ERROR_HANDLER, 0, 0, eh);
                return list;
            }

            int search_type = SettingsActivity.getSearchType();
            boolean flag = search_type == SettingsActivity.ADVANVCED_SEARCH;
            SearchRequestItem item = SettingsActivity.getSearchRequestItem();
            if (flag && item == null) {
                eh.addError(ErrorHandler.SQL_DB, activity.getString(R.string.no_sql_expression_selected_) + dbitem.dbName);
                notifyOutboxHandlers(MESSAGE_ERROR_HANDLER, 0, 0, eh);
                return list;
            }

            if (flag)//adv search
            {
                Analisator an = new Analisator();
                Analisator anl = new Analisator();
                an.setInputString(item.sqlString);
                an.dsoInitSQLrequest();

                if (!dbitem.ftypes.isEmpty()) {//catalog with additional fields
                    Set<String> set = dbitem.ftypes.getNumericFields();
                    for (String s : set) {
                        an.addVar(s, 0);
                    }
                }
                anl.setInputString(item.localString);
                anl.dsoInitLocalrequest();

                Log.d(TAG, "adv search,an=" + an + "\nanl=" + anl + "\ncat=" + cat);
                if (item.localString.equals(""))
                    anl = null;
                try {
                    an.compile();
                    Log.d(TAG, "sql request=" + an.getRecStr());
                    if (anl != null) anl.compile();

                    Calendar sc = Calendar.getInstance();
                    long start = SettingsActivity.getSharedPreferences(Global.getAppContext()).getLong(Constants.START_OBSERVATION_TIME, 0);
                    sc.setTimeInMillis(start);

                    Calendar ec = Calendar.getInstance();
                    long end = SettingsActivity.getSharedPreferences(Global.getAppContext()).getLong(Constants.END_OBSERVATION_TIME, 0);
                    ec.setTimeInMillis(end);

                    double LSTfin = AstroTools.sdTime(ec);
                    double LSTstart = AstroTools.sdTime(sc);

                    if (catalog instanceof CometsDatabase) {
                        CometsDatabase db = (CometsDatabase) catalog;
                        list = db.searchMod(item.sqlString, item.localString, LSTstart, LSTfin, sc);
                    } else {
                        String recStr = an.getRecStr();
                        if ("".equals(recStr)) recStr = null;
                        list = catalog.search(recStr, anl, LSTstart, LSTfin);

                    }

                } catch (UnsupportedOperationException e) {
                    Log.d(TAG, "Exception=" + e.getMessage());
                    catalog.close();
                }

            } else {//basic search

                list = catalog.search();
            }


            catalog.close();
            return list;
        }

        private AstroCatalog getCurrentCatalog(DbListItem item) {


            switch (item.cat) {
                case AstroCatalog.NGCIC_CATALOG:
                    return new NgcicDatabase(activity);
                case AstroCatalog.COMET_CATALOG:
                    return new CometsDatabase(activity, AstroCatalog.COMET_CATALOG);
                case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
                    return new CometsDatabase(activity, AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG);
            }

            if (item.ftypes.isEmpty())
                return new CustomDatabase(activity, item.dbFileName, item.cat);
            else
                return new CustomDatabaseLarge(activity, item.dbFileName, item.cat, item.ftypes);
        }

    }


    class DsoSelFiller implements InfoListFiller {
        private AstroCatalog catalog;
        List<AstroObject> list = new ArrayList<AstroObject>();
        DbListItem dbitem;

        public DsoSelFiller(AstroCatalog cat, boolean full, DbListItem dbitem) {
            this.dbitem = dbitem;
            catalog = cat;
            ErrorHandler eh = new ErrorHandler();
            catalog.open(eh);
            if (eh.hasError()) {
                notifyOutboxHandlers(MESSAGE_ERROR_HANDLER, 0, 0, eh);
                return;
            }

            if (!full) {
                int search_type = SettingsActivity.getSearchType();
                boolean flag = search_type == SettingsActivity.ADVANVCED_SEARCH;
                SearchRequestItem item = SettingsActivity.getSearchRequestItem();

                if (flag && item != null) {
                    Analisator an = new Analisator();
                    Analisator anl = new Analisator();
                    an.setInputString(item.sqlString);
                    an.dsoInitSQLrequest();

                    if (!dbitem.ftypes.isEmpty()) {//catalog with additional fields
                        Set<String> set = dbitem.ftypes.getNumericFields();
                        for (String s : set) {
                            an.addVar(s, 0);
                        }
                    }
                    anl.setInputString(item.localString);
                    anl.dsoInitLocalrequest();

                    if (item.localString.equals(""))
                        anl = null;
                    try {
                        an.compile();
                        Log.d(TAG, "sql request=" + an.getRecStr());
                        if (anl != null) anl.compile();

                        Calendar sc = Calendar.getInstance();
                        long start = SettingsActivity.getSharedPreferences(Global.getAppContext()).getLong(Constants.START_OBSERVATION_TIME, 0);
                        sc.setTimeInMillis(start);

                        Calendar ec = Calendar.getInstance();
                        long end = SettingsActivity.getSharedPreferences(Global.getAppContext()).getLong(Constants.END_OBSERVATION_TIME, 0);
                        ec.setTimeInMillis(end);

                        double LSTfin = AstroTools.sdTime(ec);
                        double LSTstart = AstroTools.sdTime(sc);

                        if (catalog instanceof CometsDatabase) {
                            CometsDatabase db = (CometsDatabase) catalog;
                            list = db.searchMod(item.sqlString, item.localString, LSTstart, LSTfin, sc);
                        } else {
                            String recStr = an.getRecStr();
                            if ("".equals(recStr)) recStr = null;
                            list = catalog.search(recStr, anl, LSTstart, LSTfin);
                        }

                    } catch (UnsupportedOperationException e) {
                        Log.d(TAG, "Exception=" + e.getMessage());
                        notifyOutboxHandlers(MESSAGE_TEXT, 0, 0, e.getMessage());
                        catalog.close();
                    }

                } else {

                    list = catalog.search();
                }
            } else
                list = catalog.search(null);//all catalog
            if (SettingsActivity.isRemovingDuplicates())
                list = replace(list);
            catalog.close();
        }

        public void search() {

        }

        public void update() {
            list = new ArrayList<AstroObject>();
            ErrorHandler eh = new ErrorHandler();
            catalog.open(eh);
            if (eh.hasError()) {
                notifyOutboxHandlers(MESSAGE_ERROR_HANDLER, 0, 0, eh);
                return;
            }
            list = catalog.search();
            catalog.close();
        }

        public Iterator getIterator() {
            return list.iterator();
        }

        private List<AstroObject> replace(List<AstroObject> list) {
            List<AstroObject> li = new ArrayList<AstroObject>();
            List<Integer> lint = new ArrayList<Integer>();
            for (AstroObject obj : list) {
                AstroObject o = obj;
                if (obj.getCatalog() == AstroCatalog.UGC) {
                    if (obj.ref != 0) {
                        o = null;
                        lint.add(obj.ref);
                    }
                }
                if (o != null) {
                    li.add(o);
                }
            }
            if (lint.size() > 0) {
                li.addAll(replaceForCrossRefs(lint));
            }
            return li;
        }

        /**
         * @param list - list of _ids to NGCIC db
         * @return
         */
        private List<AstroObject> replaceForCrossRefs(List<Integer> list) {
            List<AstroObject> lis = new ArrayList<AstroObject>();
            NgcicDatabase cat = new NgcicDatabase();
            ErrorHandler eh = new ErrorHandler();
            cat.open(eh);
            if (eh.hasError()) {
                return lis;
            }

            final int size = 60;
            int j = 0;
            String req = "";
            for (int i = 0; i < list.size(); i++) {
                req += Constants._ID + "=" + list.get(i) + " OR ";
                if (i % 60 == 0 && i != 0) {
                    req = req.substring(0, req.length() - 4);
                    List<AstroObject> li = cat.search(req);
                    lis.addAll(li);
                    req = "";
                }
            }
            if (!"".equals(req)) {
                req = req.substring(0, req.length() - 4);
                List<AstroObject> li = cat.search(req);
                lis.addAll(li);
            }
            cat.close();
            return lis;
        }
    }

    class CurrentCatalog {

        private int pos = 0; //in DbList

        void setCatalogPos(int pos) {
            this.pos = pos;
            //Global.queryActCat=cat;

        }

        @Override
        public String toString() {
            return "pos=" + pos + "item=" + getCurrentDbListItem();
        }

        public CurrentCatalog() {

        }

        /**
         * catalog number in AstroCatalog, not position in db list
         *
         * @return
         */
        int getCurrentCatalogInt() {
            InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
            DbListItem item = (DbListItem) iL.get(pos);
            return item.cat;
        }

        /**
         * position in db list
         *
         * @return
         */
        int getCurrentCatalogPos() {
            return pos;
        }

        DbListItem getCurrentDbListItem() {
            InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
            DbListItem item = (DbListItem) iL.get(pos);
            return item;
        }

        AstroCatalog getCurrentCatalog() {
            InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
            DbListItem item = (DbListItem) iL.get(pos);
            if (pos == 0)
                return new NgcicDatabase();
            else {
                switch (item.cat) {
                    case AstroCatalog.COMET_CATALOG:
                        return new CometsDatabase(activity, AstroCatalog.COMET_CATALOG);
                    case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
                        return new CometsDatabase(activity, AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG);
                }

                if (item.ftypes.isEmpty())
                    return new CustomDatabase(activity, item.dbFileName, item.cat);
                else
                    return new CustomDatabaseLarge(activity, item.dbFileName, item.cat, item.ftypes);
            }
        }
    }

    class SearchPerformer implements Runnable {
        public void run() {
            if (searchRequestRunning) {
                notifyOutboxHandlers(MESSAGE_TEXT, 0, 0, SEARCH_IS_ALREADY_RUNNING);
                return;
            }
            if (importRunning) {
                notifyOutboxHandlers(MESSAGE_TEXT, 0, 0, IMPORT_IS_ALREADY_RUNNING);
                return;
            }
            QueryActivity.clearStopFlag();
            searchRequestRunning = true;
            InfoList infoList = new InfoListImpl("", ObsInfoListImpl.Item.class);
            try {
                InfoListFiller filler = new DsoSelFillerMod();//new DsoSelFiller(catalog,false,currentCatalog.getCurrentDbListItem());
                infoList.fill(filler);
            } catch (Exception e) {
                Log.d(TAG, "Exception = " + e);
            }

            SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, false, activity);
            SettingsActivity.putSharedPreferences(Constants.SETTINGS_SEARCH_CATALOG_UPDATE, false, activity);
            notifyOutboxHandlers(MESSAGE_UPDATE_VIEW, 0, 0, infoList);

            String aname = getActivityName();
            SettingsActivity.putSharedPreferences(Constants.QUERY_ACTIVITY_NAME, aname, activity);
            notifyOutboxHandlers(MESSAGE_SET_ACTIVITY_NAME, 0, 0, activity.getString(R.string.dso_selection_) + aname);
            searchRequestRunning = false;
            Log.d(TAG, "search finished");
        }
    }
}
