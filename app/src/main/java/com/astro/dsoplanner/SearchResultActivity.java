
package com.astro.dsoplanner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.astro.dsoplanner.SearchRules.MySet;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.CMO;
import com.astro.dsoplanner.base.NgcicObject;
import com.astro.dsoplanner.database.CometsDatabase;
import com.astro.dsoplanner.database.CrossDb;
import com.astro.dsoplanner.database.CustomDatabase;
import com.astro.dsoplanner.database.CustomDatabaseLarge;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.NgcicDatabase;
import com.astro.dsoplanner.database.NoteDatabase;
import com.astro.dsoplanner.graph.GraphActivity;
import com.astro.dsoplanner.graph.GraphRec;
import com.astro.dsoplanner.graph.PgcFactory;
import com.astro.dsoplanner.graph.TychoStarFactory;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.startools.BitTools;

public class SearchResultActivity extends ParentListActivity {

    private static final int CONSTELLATION = 3;
    private static final int NOTE_RECORDS = 2;
    private static final int SEARCH_OVER = 1;
    private static final int CODE = 1;

    private static final String SELECT_FROM_CUSTOMDBB_WHERE_ID_LIKE = "select * from customdbb where '('||_id||')' like '";
    private static final String _0_9 = "[\\(][0-9]+[\\)]";
    private static final String SELECT_FROM_CUSTOMDBB_WHERE_NAME1_LIKE_ID = "select * from customdbb where name1 like '('||_id||')'||' '||'";
    private static final String PGC = "PGC";
    private static final String WDS = "WDS";
    private static final String TYC_0_9_0_9_0_9 = "TYC[0-9]+[\\-][0-9]+[\\-][0-9]+";
    private static final String PGC_0_9 = "PGC[0-9]+";
    private static final String VACUUM = "vacuum";
    private static final String REPLACE = "replace";
    private static final String INDEX = "index";
    private static final String PRAGMA = "pragma";
    private static final String INSERT = "insert";
    private static final String DELETE = "delete";
    private static final String UPDATE = "update";
    private static final String ALTER = "alter";
    private static final String DROP = "drop";
    private static final String BUNDLE = "bundle";
    private static final String RESULTS = " results";
    private static final String SEARCH = "Search. ";
    private static final String SEARCH_NO_RESULTS = "Search. No Results";
    private static final String AUDIO = " audio";
    private static final String NOTE_CATALOG = "Note catalog. ";
    private static final String DIM2 = " dim=";
    private static final String MAG2 = " mag=";
    private static final String STRING = "--";

    private static final String TAG = SearchResultActivity.class.getSimpleName();
    BaseAdapter mAdapter;
    List<Object> displayList = new ArrayList<Object>();
    ;

    private HandlerThread workerThread;
    private Handler workerHandler;

    private Handler processHandler = new Handler() {
        int i = 0;

        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1 == CONSTELLATION) {
                displayList.add(msg.obj);
            } else if (msg.arg1 == NOTE_RECORDS) {
                displayList.addAll((Collection) msg.obj);
            } else {
                List<AstroObject> list = (List<AstroObject>) msg.obj;


                List<AstroObject> list2 = new ArrayList<AstroObject>();
                for (AstroObject obj : list) {
                    boolean exists = false;
                    for (Object o : displayList) {
                        if (o instanceof AstroObject) {
                            AstroObject ob = (AstroObject) o;
                            if (obj.getCatalog() == ob.getCatalog() && obj.getId() == ob.getId()) {
                                exists = true;
                                break;
                            }
                        }

                    }
                    if (!exists)
                        list2.add(obj);
                }
                displayList.addAll(list2);
            }

            mAdapter.notifyDataSetChanged();

            if (msg.what == SEARCH_OVER) {
                setTitle(SEARCH + displayList.size() + RESULTS);
                setProgressBarIndeterminateVisibility(false);
            }
        }
    };

    private class SRAadapter extends BaseAdapter {


        private LayoutInflater mInflater;


        public SRAadapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.sresult_item_object, null);
            }
            Object o = displayList.get(position);
            if (o instanceof AstroObject) {
                AstroObject obj = (AstroObject) o;
                String s = obj.getLongName() + " " + obj.getTypeString();

                ((TextView) convertView.findViewById(R.id.sresultitem_dso)).setText(s);
                ((TextView) convertView.findViewById(R.id.sresultitem_catalog)).setText(obj.getCatalogName());
                double ma = obj.getMag();
                String mag = Double.isNaN(ma) ? STRING : String.format(Locale.US, "%.1f", ma);

                double di = Math.max(obj.getA(), obj.getB());
                String dim = Double.isNaN(di) ? STRING : String.format(Locale.US, "%.1f", di) + "'";
                if (obj.hasDimension())
                    s = obj.getConString() + " " + MAG2 + mag + DIM2 + dim;
                else
                    s = obj.getConString() + " " + MAG2 + mag;
                ((TextView) convertView.findViewById(R.id.sresultitem_info)).setText(s);

                //make dark background
                if (SettingsActivity.getDarkSkin() || SettingsActivity.getNightMode())
                    convertView.setBackgroundColor(0xff000000);


            } else if (o instanceof NoteRecord) {
                NoteRecord nr = (NoteRecord) o;

                ((TextView) convertView.findViewById(R.id.sresultitem_dso)).setText(nr.name);
                ((TextView) convertView.findViewById(R.id.sresultitem_info)).setText(nr.note);

                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(nr.date);
                String s = DetailsActivity.makeDateString(c, true) + " " + DetailsActivity.makeTimeString(c, false);
                s = NOTE_CATALOG + s;
                if (!nr.path.isEmpty()) s = s + AUDIO;
                ((TextView) convertView.findViewById(R.id.sresultitem_catalog)).setText(s);
            } else if (o instanceof Constellation) {
                Constellation constellation = (Constellation) o;
                ((TextView) convertView.findViewById(R.id.sresultitem_dso)).setText(Constants.constellationLong[constellation.con]);
                ((TextView) convertView.findViewById(R.id.sresultitem_catalog)).setText(R.string.constellation);
                ((TextView) convertView.findViewById(R.id.sresultitem_info)).setText("");
            }
            return convertView;
        }

        public int getCount() {
            return displayList.size();

        }

        public Object getItem(int position) {
            return displayList.get(position);

        }

        public long getItemId(int position) {
            return position;
        }
    }

    Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setContentView(R.layout.sresult_activity);

            workerThread = new HandlerThread("");
            workerThread.start();
            workerHandler = new Handler(workerThread.getLooper());


            mAdapter = new SRAadapter();
            setListAdapter(mAdapter);
            setTitle(getString(R.string.search_in_progress_));
            setProgressBarIndeterminateVisibility(true);
            initList2(searchString);

        }
    };
    String searchString;
    boolean returntograph;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (workerThread != null)
            workerThread.getLooper().quit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideMenuBtn();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        searchString = getIntent().getStringExtra(SearchManager.QUERY);
        returntograph = getIntent().getBooleanExtra(Constants.RETURN_TO_GRAPH, false);
        if (searchString == null) {
            finish();
            return;
        }
        if ("".equals(searchString)) {
            finish();
            return;
        }
        searchString = protectSearchString(searchString);
        initHandler.handleMessage(null);
    }

    /**
     * protection against sql injection
     *
     * @param s
     * @return
     */
    public static String protectSearchString(String s) {
        return s.toLowerCase(Locale.US).replace(DROP, "").replace(ALTER, "").replace(UPDATE, "").
                replace(DELETE, "").replace(INSERT, "").replace(PRAGMA, "").
                replace(INDEX, "").replace(REPLACE, "").replace(VACUUM, "").
                replace("%", "\\%").replace("_", "\\_").toUpperCase(Locale.US);
    }

    private AstroObject detailsSearchObj = null;
    boolean pgcfound = false;

    private void initList2(final String searchString) {
        final int pgcid;
        String pgcname = searchString.toUpperCase().replace(" ", "");
        if (pgcname.matches(PGC_0_9)) {
            pgcid = Integer.parseInt(pgcname.substring(3));
        } else
            pgcid = -1;

        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                List<AstroObject> list = new ArrayList<AstroObject>();

                for (int i = 1; i < Constants.constellations.length; i++) {
                    if (Constants.constellations[i].equalsIgnoreCase(searchString)) {
                        Message msg = Message.obtain();
                        msg.obj = new Constellation(i);
                        msg.arg1 = CONSTELLATION;
                        processHandler.sendMessage(msg);
                        return;
                    }
                }
                if (searchString.length() >= 3) {
                    for (int i = 1; i < Constants.constellationLong.length; i++) {
                        if (Constants.constellationLong[i].toUpperCase(Locale.US).startsWith(searchString.toUpperCase(Locale.US))) {
                            Message msg = Message.obtain();
                            msg.obj = new Constellation(i);
                            msg.arg1 = CONSTELLATION;
                            processHandler.sendMessage(msg);
                            return;
                        }
                    }
                }

                if (searchString.toUpperCase().matches("SAO[ ]*[0-9]+")) {

                    int saonum = Integer.parseInt(searchString.toUpperCase().replace("SAO", "").replace(" ", ""));

                    AstroObject obj = SaoTyc.getTycObjFromSaoNum(saonum, getApplicationContext());
                    if (obj != null)
                        list.add(obj);
                }

                String sstr = searchString.replace(" ", "").toUpperCase(Locale.US);
                if (sstr.matches(TYC_0_9_0_9_0_9)) {

                    AstroObject obj = findTYCinStarChartLayer(sstr, getApplicationContext());
                    if (obj != null)
                        list.add(obj);
                }
                list.addAll(AstroTools.searchHrStar(searchString, getApplicationContext()));


                //zero, look for ngcic abbreviation
                Point p = DetailsActivity.Search(searchString.replace(" ", ""), SearchResultActivity.this);
                if (p instanceof AstroObject) {
                    list.add((AstroObject) p);
                    detailsSearchObj = (AstroObject) p;
                }

                Message msg = Message.obtain();
                msg.obj = list;
                processHandler.sendMessage(msg);
            }
        };
        workerHandler.post(r1);

        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                //first searching in associated database, e.g. ngc etc

                int db = SearchRules.getAssociatedDatabase(searchString.replace(" ", ""));
                Message msg = Message.obtain();
                MySet myset = new MySet();
                if (db == AstroCatalog.WDS && searchString.replace(" ", "").toUpperCase(Locale.US).startsWith(WDS)) {
                    myset.addAll2(searchDb(db, searchString.replace(" ", "").toUpperCase(Locale.US).replace(WDS, "")));
                } else
                    myset.addAll2(searchDb(db, searchString.replace(" ", "")));

                if (db == AstroCatalog.PGC && myset.size() != 0)
                    pgcfound = true;

                db = AstroCatalog.SAC;
                myset.addAll2(searchDb(db, searchString.replace(" ", "")));
                myset.addAll2(searchDb(db, searchString));//to look in other field correctly

                db = AstroCatalog.MISC;
                myset.addAll2(searchDb(db, searchString.replace(" ", "")));

                db = AstroCatalog.HAAS;
                myset.addAll2(searchDb(db, searchString.replace(" ", "")));

                db = AstroCatalog.CALDWELL;
                myset.addAll2(searchDb(db, searchString.replace(" ", "")));

                db = AstroCatalog.MESSIER;
                myset.addAll2(searchDb(db, searchString.replace(" ", "")));


                msg.obj = myset.get();
                processHandler.sendMessage(msg);
            }
        };
        workerHandler.post(r2);


        Runnable r3 = new Runnable() {
            @Override
            public void run() {
                //looking for cross db names
                Set<String> set = new HashSet<String>();
                set.addAll(CrossDb.searchName(searchString.replace(" ", ""), SearchResultActivity.this));
                set.addAll(CrossDb.searchName(searchString, SearchResultActivity.this));

                if (SearchRules.isOwnNameCandidate(searchString) && searchString.length() >= Global.COMMON_NAME_MIN_LENGTH_FOR_EXT_SEARCH) {
                    set.addAll(CrossDb.searchPartName(searchString, SearchResultActivity.this));
                    set.addAll(AstroTools.searchCommonNames(searchString));
                }
                //for search like m40 etc where the correct result is obtained via Details.Search

                if (detailsSearchObj != null) {
                    if (detailsSearchObj instanceof NgcicObject) {
                        NgcicObject ngc = (NgcicObject) detailsSearchObj;
                        set.add(ngc.getNgcIcName());
                        set.addAll(CrossDb.searchName(ngc.getNgcIcName(), SearchResultActivity.this));

                    } else {
                        String shortname = detailsSearchObj.getShortName();
                        set.add(shortname);
                        String longname = detailsSearchObj.getLongName();
                        set.add(longname);
                        set.addAll(CrossDb.searchName(shortname, SearchResultActivity.this));
                        if (!shortname.equals(longname))
                            set.addAll(CrossDb.searchName(longname, SearchResultActivity.this));


                    }
                }

                List<AstroObject> listobj = new ArrayList<AstroObject>();
                MySet myset = new MySet();

                boolean localpgc = false;//there a ref to pgc in associated db, to be looked upon in the layer if there is PGC ref
                int localpgcnum = -1;
                for (String s : set) {
                    int db = SearchRules.getAssociatedDatabase(s);
                    if (db == AstroCatalog.WDS) {
                        if (s.replace(" ", "").toUpperCase(Locale.US).startsWith(WDS)) {
                            List<AstroObject> l0 = searchDb(db, s.replace(" ", "").toUpperCase(Locale.US).replace(WDS, ""));
                            myset.addAll2(l0);
                        }
                    } else {

                        if (db == AstroCatalog.PGC) {
                            localpgc = true;
                            localpgcnum = AstroTools.getInteger(s.replace(PGC, ""), -1, -1, Integer.MAX_VALUE);
                        }

                        List<AstroObject> l1 = searchDb(db, s);
                        myset.addAll2(l1);
                        List<AstroObject> l2 = searchDb(AstroCatalog.SAC, s);
                        myset.addAll2(l2);
                        List<AstroObject> l3 = searchDb(AstroCatalog.SNOTES, s);
                        myset.addAll2(l3);
                        List<AstroObject> l4 = searchDb(AstroCatalog.MISC, s);
                        myset.addAll2(l4);
                    }
                }
                listobj.addAll(myset.get());

                //look in pgc star chart layer
                if ((pgcid != -1 && listobj.size() == 0 && !pgcfound) || (listobj.size() == 0 && localpgc && localpgcnum != -1)) {
                    Log.d(TAG, "star chart layer search");

                    int id = -1;
                    id = pgcid;
                    if (id == -1) {
                        id = localpgcnum;
                    }
                    if (id != -1) {
                        AstroObject obj = findPGCinStarChartLayer(id);
                        if (obj != null)
                            listobj.add(obj);
                    }
                }
                Message msg = Message.obtain();
                msg.obj = listobj;

                processHandler.sendMessage(msg);
            }
        };
        workerHandler.post(r3);


        Runnable r4 = new Runnable() {
            @Override
            public void run() {
                //user databases, comets and mps
                InfoList info = ListHolder.getListHolder().get(InfoList.DB_LIST);
                MySet set = new MySet();
                for (Object o : info) {
                    DbListItem item = (DbListItem) o;
                    if (!SearchRules.isInternalCatalog(item.cat) || item.cat == AstroCatalog.COMET_CATALOG ||
                            item.cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG || item.cat == AstroCatalog.SNOTES) {
                        set.addAll2(searchDb(item, searchString, false));
                    }

                }

                //checking for mp
                CometsDatabase cdb = new CometsDatabase(SearchResultActivity.this, AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG);
                ErrorHandler eh = new ErrorHandler();
                cdb.open(eh);
                if (!eh.hasError()) {
                    //name
                    String q = SELECT_FROM_CUSTOMDBB_WHERE_NAME1_LIKE_ID + searchString + "'";
                    List<AstroObject> list = cdb.rawQuery(q);
                    for (AstroObject o : list) {
                        CMO cmo = (CMO) o;
                        cmo.recalculateRaDec(AstroTools.getDefaultTime(SearchResultActivity.this));
                    }
                    set.addAll2(list);

                    if (searchString.matches(_0_9)) {
                        //number
                        q = SELECT_FROM_CUSTOMDBB_WHERE_ID_LIKE + searchString + "'";
                        List<AstroObject> list2 = cdb.rawQuery(q);
                        for (AstroObject o : list2) {
                            CMO cmo = (CMO) o;
                            cmo.recalculateRaDec(AstroTools.getDefaultTime(SearchResultActivity.this));
                        }
                        set.addAll2(list2);
                    }
                }

                Message msg = Message.obtain();
                msg.obj = set.get();
                processHandler.sendMessage(msg);
            }
        };
        workerHandler.post(r4);


        Runnable r5 = new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                List<NoteRecord> list = searchNotes(searchString);
                msg.obj = list;
                msg.arg1 = NOTE_RECORDS;
                msg.what = SEARCH_OVER;
                processHandler.sendMessage(msg);
            }
        };
        workerHandler.post(r5);
    }


    public static AstroObject findTYCinStarChartLayer(String name, Context context) {
        if (name.toUpperCase().matches(TYC_0_9_0_9_0_9)) {

            String s = name.substring(3);
            String[] a = s.split("\\-");
            int tyc1 = -1;
            int tyc2 = -1;
            int tyc3 = -1;
            try {
                tyc1 = Integer.parseInt(a[0]);
                tyc2 = Integer.parseInt(a[1]);
                tyc3 = Integer.parseInt(a[2]);
            } catch (Exception e) {
            }
            if (tyc1 != -1 && tyc2 != -1 && tyc3 != -1) {
                Log.d(TAG, "tyc1=" + tyc1 + " tyc2=" + tyc2 + " tyc3=" + tyc3);

                AstroObject ob = SaoTyc.getTycObjFromTycIndex(BitTools.convertTycToInt(tyc1, tyc2, tyc3), context);
                if (ob != null)
                    return ob;
                TychoStarFactory tycho = new TychoStarFactory();
                try {
                    tycho.open();
                    AstroObject obj = tycho.find(tyc1, tyc2, tyc3);
                    return obj;
                } catch (Exception e) {

                } finally {
                    try {
                        tycho.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
        return null;
    }

    public static AstroObject findPGCinStarChartLayer(int id) {
        PgcFactory pgc = new PgcFactory();
        try {
            pgc.open();
            AstroObject obj = pgc.find(id);
            return obj;
        } catch (Exception e) {

        } finally {
            try {
                pgc.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    private List<AstroObject> searchDb(int db, String name) {
        return SearchRules.searchForName(db, name, getApplicationContext());
    }

    private void initList(String searchString) {
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(this);
        int db_num = prefs.getInt(Constants.SEARCH_DB, -1);
        boolean search_fields = prefs.getBoolean(Constants.SEARCH_FIELDS, true);


        displayList = new ArrayList<Object>();
        Set<Object> set = new LinkedHashSet<Object>();//this is used to avoid showing equal objects

        if (db_num == -1 || db_num == AstroCatalog.NGCIC_CATALOG) {//ALL or NgcIc
            Point p = DetailsActivity.Search(searchString, this);
            if (p instanceof AstroObject) {
                set.add(p);
            }
        }
        if (db_num == -1) {//ALL
            Iterator it = ListHolder.getListHolder().get(InfoList.DB_LIST).iterator();
            for (; it.hasNext(); ) {
                DbListItem item = (DbListItem) it.next();
                set.addAll(searchDb(item, searchString, search_fields));
            }
            set.addAll(searchNotes(searchString));
        } else if (db_num == -2) {//Notes
            set.addAll(searchNotes(searchString));
        } else {
            DbListItem item = AstroTools.findItemByCatId(db_num);
            if (item != null)
                set.addAll(searchDb(item, searchString, search_fields));
        }
        displayList.addAll(set);

    }

    private List<NoteRecord> searchNotes(String searchString) {
        List<NoteRecord> list = new ArrayList<NoteRecord>();
        Set<NoteRecord> set = new HashSet<NoteRecord>();
        NoteDatabase db = new NoteDatabase(this);
        ErrorHandler eh = new ErrorHandler();
        db.open(eh);
        if (eh.hasError())
            return list;
        set.addAll(db.searchNameInclusive(searchString));
        set.addAll(db.searchContentInclusive(searchString));
        list.addAll(set);
        return list;
    }

    private List<AstroObject> searchDb(DbListItem item, String searchString, boolean search_fields) {

        List<AstroObject> list = new ArrayList<AstroObject>();
        AstroCatalog cat;
        Log.d(TAG, "item=" + item);
        if (item.cat == AstroCatalog.COMET_CATALOG || item.cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG)
            cat = new CometsDatabase(this, item.cat);
        else {
            if (item.cat == AstroCatalog.NGCIC_CATALOG)
                cat = new NgcicDatabase(this);
            else if (item.ftypes.isEmpty())
                cat = new CustomDatabase(this, item.dbFileName, item.cat);
            else
                cat = new CustomDatabaseLarge(this, item.dbFileName, item.cat, item.ftypes);
        }
        ErrorHandler eh = new ErrorHandler();
        cat.open(eh);
        if (!eh.hasError()) {
            list.addAll(cat.searchName(searchString));
            Log.d(TAG, "list=" + list);
            if (search_fields) {
                list.addAll(cat.searchComment(searchString));
                Log.d(TAG, "list=" + list);
                if (!item.ftypes.isEmpty()) {
                    list.addAll(((CustomDatabaseLarge) cat).searchTextFields(searchString));
                    Log.d(TAG, "list=" + list);
                }
            }
            cat.close();
        }
        Calendar c = AstroTools.getDefaultTime(this);
        for (AstroObject obj : list) {
            obj.recalculateRaDec(c);
        }
        return list;

    }

    /**
     * looks for name in all databases for More in Details
     *
     * @param searchString
     * @param context
     * @return
     */
    public static List<AstroObject> makeSearchByName(String searchString, Context context) {
        List<AstroObject> list = new ArrayList<AstroObject>();
        searchString = protectSearchString(searchString);

        Point p = DetailsActivity.Search(searchString, context);
        if (p instanceof AstroObject) {
            list.add((AstroObject) p);
        }
        Iterator it = ListHolder.getListHolder().get(InfoList.DB_LIST).iterator();
        for (; it.hasNext(); ) {
            DbListItem item = (DbListItem) it.next();
            if (item.cat != AstroCatalog.NGCIC_CATALOG) {//was searched for in details2.search
                AstroCatalog cat;
                if (item.cat == AstroCatalog.COMET_CATALOG)
                    cat = new CometsDatabase(context, AstroCatalog.COMET_CATALOG);
                else if (item.cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG)
                    cat = new CometsDatabase(context, AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG);
                else if (item.ftypes.isEmpty())
                    cat = new CustomDatabase(context, item.dbFileName, item.cat);
                else
                    cat = new CustomDatabaseLarge(context, item.dbFileName, item.cat, item.ftypes);
                ErrorHandler eh = new ErrorHandler();
                cat.open(eh);
                if (!eh.hasError()) {
                    List<AstroObject> list1 = cat.searchNameExact(searchString);
                    cat.close();
                    list.addAll(list1);

                }
            }
        }
        Log.d(TAG, "searchString=" + searchString);
        for (AstroObject o : list) {
            Log.d(TAG, "result element=" + o);
        }
        return list;
    }
    AstroObject detailsObj;

    public void onListItemClick(ListView parent, View v, int position, long id) {
        Object o = displayList.get(position);
        if (o instanceof NoteRecord) {
            NoteRecord rec = (NoteRecord) o;
            AstroObject obj = new NoteDatabase().getObject(rec, new ErrorHandler());
            if (obj == null) {//if there is no associated object editing the note
                NoteRequest request = new NoteRequest(rec);
                Intent i = new Intent(this, NoteActivity.class);
                i.putExtra(BUNDLE, request.getBundle());
                startActivity(i);
            } else {//going into details
                new DetailsCommand(obj, this).execute();
            }
        } else if (o instanceof AstroObject) {
            AstroObject obj = (AstroObject) o;
            detailsObj = obj;
            DetailsCommand d = new DetailsCommand(obj, this);
            if (returntograph)
                d.setReturnGraphFlag(CODE);//on activity result code
            d.execute();
        } else if (o instanceof Constellation) {
            Constellation constellation = (Constellation) o;
            GraphRec rec = new GraphRec(getApplicationContext());
            rec.selected_con = constellation.con;
            rec.set_centered = true;
            Calendar defc = AstroTools.getDefaultTime(this);
            Point.setLST(AstroTools.sdTime(defc));//
            rec.save(getApplicationContext());
            if (returntograph) {
                finish();
            } else {
                Intent i = new Intent(this, GraphActivity.class);
                startActivity(i);
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == CODE) {//coming back from details
            AstroObject obj = detailsObj;
            if (obj == null) return;

            Calendar defc = AstroTools.getDefaultTime(this);
            Point.setLST(AstroTools.sdTime(defc));//need to calculate Alt and Az
            SettingsActivity.putSharedPreferences(Constants.GRAPH_OBJECT, obj, this);
            int zoom = SettingsActivity.getSharedPreferences(this).getInt(Constants.CURRENT_ZOOM_LEVEL, Constants.DEFAULT_ZOOM_LEVEL);
            obj.recalculateRaDec(defc);
            new GraphRec(zoom, obj.getAz(), obj.getAlt(), defc, obj).save(this);//add graph settings for Graph Activity to process it
            finish();

        }
    }

    private class Constellation {
        int con;

        public Constellation(int con) {
            super();
            this.con = con;
        }

    }
}
