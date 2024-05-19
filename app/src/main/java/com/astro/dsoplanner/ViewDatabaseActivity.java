package com.astro.dsoplanner;

import static com.astro.dsoplanner.Global.ALEX_MENU_FLAG;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.astro.dsoplanner.alexmenu.alexMenu;
import com.astro.dsoplanner.alexmenu.alexMenu.OnMenuItemSelectedListener;
import com.astro.dsoplanner.alexmenu.alexMenuItem;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.CustomObject;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.database.CometsDatabase;
import com.astro.dsoplanner.database.CustomDatabase;
import com.astro.dsoplanner.database.CustomDatabaseLarge;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.NgcicDatabase;
import com.astro.dsoplanner.graph.GraphActivity;
import com.astro.dsoplanner.graph.GraphRec;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListStringSaverImp;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.infolist.ObsListFiller;
import com.astro.dsoplanner.util.Holder3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.io.OutputStream;

public class ViewDatabaseActivity extends ParentListActivity implements IPickFileCallback, OnGestureListener, Handler.Callback/*DialogInterface.OnDismissListener*/ {
    private static final int MINOR_PLANET_LARGEST_INTERNAL_ID = 10000;

    private static final String NGC = "NGC";
    private static final String MENU_ERROR2 = "Menu error! ";
    private static final String EDIT = "edit";
    private static final String CATALOG_POSITION = "catalogPosition";


    private static final String TAG = ViewDatabaseActivity.class.getSimpleName();
    private static final String MENU_ERROR = MENU_ERROR2;
    private final static int REQUEST_CODE = 1;
    private static final int EXPORT_CODE = 2;
    private final static int EDIT_OBJECT_DIALOG = 3;

    private AstroCatalog catalog;
    private ListAdapter mAdapter;
    private boolean dirtyObsPref = false;//for tracking obs list pref saving
    private alexMenu aMenu;
    private alexMenu contextMenu;
    ExportData exportData;


    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
        }
    };
    InputDialog pd;
    private int dbpos;//position in db list
    private ViewDatabaseController controller;
    DbListItem item;
    int item_selected = -1;

    //cashing astro objects
    long cashing_time;
    Set<AstroObject> cashing_set = new HashSet<AstroObject>();

    private AstroObject getCashedObject(AstroObject obj) {
        for (AstroObject o : cashing_set) {
            if (o.equals(obj))
                return o;
        }
        return null;
    }

    private void putObjectIntoCashe(AstroObject obj) {
        cashing_set.add(obj);
    }

    private void clearCashe() {
        cashing_set = new HashSet<AstroObject>();
    }

    private class ListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Cursor cursor;

        public ListAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        public View getView(int position, View convertView, ViewGroup parent) {
            boolean bold = (position == item_selected);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.dsoselection_item, null);
            }

            if (cursor == null)
                return convertView;

            AstroObject obj;
            try {
                cursor.moveToPosition(position);
                obj = catalog.getObjectFromCursor(cursor);
            } catch (Exception e) {
                finish();
                return convertView;
            }


            AstroObject o = null;
            if (item.cat == AstroCatalog.COMET_CATALOG || item.cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG) {
                o = getCashedObject(obj);
                if (o == null) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(cashing_time);
                    obj.recalculateRaDec(c);
                    putObjectIntoCashe(obj);
                } else {
                    obj = o;
                }
            }


            TextView tv = (TextView) convertView.findViewById(R.id.dsosel_ngcnum);
            tv.setText(obj.getDsoSelName());
            tv.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);

            tv = (TextView) convertView.findViewById(R.id.dsosel_con);
            tv.setText(obj.getConString());
            tv.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);

            tv = (TextView) convertView.findViewById(R.id.dsosel_type);
            tv.setText(obj.getTypeString());
            tv.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);

            tv = (TextView) convertView.findViewById(R.id.dsosel_mag);


            tv.setText(Double.isNaN(obj.getMag()) ? "--" : String.format(Locale.US, "%.1f", obj.getMag()));
            tv.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);

            tv = (TextView) convertView.findViewById(R.id.dsosel_dim);
            double dim = Math.max(obj.getA(), obj.getB());
            tv.setText(obj.hasDimension() ?
                    (Double.isNaN(dim) ? "--" : String.format(Locale.US, "%.1f", dim))
                    : "--");

            tv.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);

            //make dark background
            if (SettingsActivity.getDarkSkin() || SettingsActivity.getNightMode())
                convertView.setBackgroundColor(0xff000000);

            return convertView;
        }

        public int getCount() {
            if (cursor == null)
                return 0;
            else {
                int size = 0;
                try {
                    size = cursor.getCount();
                } catch (Exception e) {
                    finish();
                }
                return size;
            }
        }

        public AstroObject getItem(int position) {
            AstroObject obj = null;
            try {
                cursor.moveToPosition(position);
                obj = catalog.getObjectFromCursor(cursor);
            } catch (Exception e) {
                finish();
            }
            return obj;
        }

        public long getItemId(int position) {
            return position;
        }

        public void setCursor(Cursor cursor) {
            this.cursor = cursor;
        }

        public Cursor getCursor() {
            return cursor;
        }
    }

    @Override
    protected void onDestroy() {

        try {
            aMenu.hide();
            contextMenu.hide();
        } catch (Exception e) {
        }

        super.onDestroy();
        if (controller != null) controller.dispose();
        if (mAdapter != null) {
            Cursor cursor = mAdapter.getCursor();
            try {
                if (cursor != null) cursor.close();
            } catch (Exception e) {
            }
        }
        if (catalog != null) catalog.close();
        if (pd != null) {
            pd.dismiss();
            pd = null;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            if (controller != null) {
                clearCashe();
                controller.handleMessage(ViewDatabaseController.MESSAGE_UPDATE, catalog);
            }
        }
        if (requestCode == EXPORT_CODE && resultCode == RESULT_OK && data != null) {
            exportData.process(data);
        }

    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
    }

    //	boolean nightmode=false;
    int curDb = 0;
    DbListItem.FieldTypes ftypes = new DbListItem.FieldTypes();

    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.view_database_layout);

        exportData = new ExportData(this, EXPORT_CODE, "");

        Intent i = getIntent();
        dbpos = i.getIntExtra(CATALOG_POSITION, 0);

        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        item = (DbListItem) iL.get(dbpos);

        if (ImportDatabaseIntentService.isBeingImported(item.dbFileName)) {
            InputDialog.toast(Global.DB_IMPORT_RUNNING, this).show();
            finish();
            return;
        }


        String name = getString(R.string.view_database_) + item.dbName;
        setTitle(name);
        curDb = item.cat;
        Log.d(TAG, "dbitem=" + item);
        Log.d(TAG, "filename=" + getDatabasePath(item.dbFileName));


        ftypes = item.ftypes;

        if (dbpos == 0)
            catalog = new NgcicDatabase(this);
        else {
            if (ftypes.isEmpty())//there are no restrictions imposed
                catalog = new CustomDatabase(this, item.dbFileName, item.cat);
            else {
                switch (item.cat) {
                    case AstroCatalog.COMET_CATALOG:
                        catalog = new CometsDatabase(this, AstroCatalog.COMET_CATALOG);
                        break;
                    case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
                        catalog = new CometsDatabase(this, AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG);
                        break;
                    default:
                        catalog = new CustomDatabaseLarge(this, item.dbFileName, item.cat, item.ftypes);

                }
            }

        }

        getListView().setVerticalFadingEdgeEnabled(!nightMode);
        if (ALEX_MENU_FLAG) {
            initAlexMenu();
            initAlexContextMenu();
        } else
            registerForContextMenu(getListView());


        mAdapter = new ListAdapter();
        setListAdapter(mAdapter);


        controller = new ViewDatabaseController(this);
        controller.addOutboxHandler(new Handler(this));
        controller.handleMessage(ViewDatabaseController.MESSAGE_UPDATE, catalog);


    }

    private void updateObjNum() {
        if (mAdapter.getCursor() != null) {
            int count = 0;
            try {
                count = mAdapter.getCursor().getCount();
            } catch (Exception e) {
                finish();
            }
            ((TextView) findViewById(R.id.view_database_text)).setText(count + getString(R.string._obj));
        }
    }

    @Override
    public void onResume() {

        SettingsActivity.nightGuardReset();
        updateObjNum();
        Calendar c = AstroTools.getDefaultTime(this);
        if (cashing_time != c.getTimeInMillis()) {
            clearCashe();
            cashing_time = c.getTimeInMillis();
        }

        controller.handleMessage(ViewDatabaseController.MESSAGE_UPDATE, catalog);

        super.onResume();

        //for CMO as we may come back from details graph
        if (ImportDatabaseIntentService.isBeingImported(item.dbFileName))
            finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dirtyObsPref) {
            int obsList = SettingsActivity.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

            new Prefs(this).saveList(obsList);
            dirtyObsPref = false;
        }
    }

    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {

        AstroObject obj = mAdapter.getItem(position);
        if (obj == null) return;
        //SAND use current calendar, not now! Global.cal=Calendar.getInstance();
        Calendar c = AstroTools.getDefaultTime(this);
        Point.setLST(AstroTools.sdTime(c));//need to calculate Alt and Az
        SettingsActivity.putSharedPreferences(Constants.GRAPH_OBJECT, obj, this);
        int zoom = SettingsActivity.getSharedPreferences(this).getInt(Constants.CURRENT_ZOOM_LEVEL, Constants.DEFAULT_ZOOM_LEVEL);
        obj.recalculateRaDec(c);
        new GraphRec(zoom,
                obj.getAz(), obj.getAlt(), AstroTools.getDefaultTime(this), obj).save(this);//add graph settings for Graph Activity to process it
        Intent i = new Intent(this, GraphActivity.class);
        startActivity(i);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return parseMenu(item.getItemId());
    }

    public boolean parseMenu(int itemid) {


        switch (itemid) {
            case R.id.paste_view_db_menu://clipboard content is considered as external
                startPasting();
                return true;
            case R.id.add_view_db_menu:
                Intent i = new Intent(this, NewCustomObjectActivityAsList.class);
                i.putExtra(EDIT, false);
                i.putExtra(CATALOG_POSITION, dbpos);
                startActivityForResult(i, REQUEST_CODE);
                //NewCustomObjectDialogBuilder builder=new NewCustomObjectDialogBuilder(false,dbpos,null,this,handler);
                //builder.getDialog().show();
                return true;
            case R.id.update_view_db_menu:
                Runnable r3 = new Runnable() {
                    public void run() {
                        if (item.cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG) {
                            //not fully correct. Should download current version from server instead
                            Global.server_version = SettingsActivity.
                                    getSharedPreferences(getApplicationContext()).
                                    getInt(Constants.MP_LAST_UPDATE_NUMBER, -1);
                        }
                        DownloadCMOItem it = new DownloadCMOItem(ViewDatabaseActivity.this, handler, item.cat);
                        it.setNetworkTypeAllowed(DownloadItem.NETWORK_ALL);
                        it.start();
                    }
                };

                String warning;
                if (item.cat == AstroCatalog.COMET_CATALOG)
                    warning = getResources().getString(R.string.cmo_updating);
                else
                    warning = getString(R.string.do_you_want_to_update_current_database_);
                registerDialog(AstroTools.getDialog(this, warning, r3)).show();


                return true;
            case R.id.import_query://content is considered as external

                SelectFileActivity.setPath(SettingsActivity.getFileDialogPath(getApplicationContext()));
                SelectFileActivity.setListener(ViewDatabaseActivity.this);
                Intent fileDialog = new Intent(ViewDatabaseActivity.this, SelectFileActivity.class);
                startActivity(fileDialog);
                return true;

            case R.id.vshare:
                if (SettingsActivity.nightGuard(this)) return true;
                Cursor cursor = mAdapter.getCursor();
                cursor.moveToPosition(-1);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                InfoListSaver saver = new InfoListStringSaverImp(out, Global.SHARE_LINES_LIMIT, new Handler());
                while (cursor.moveToNext()) {
                    AstroObject ob = catalog.getObjectFromCursor(cursor);
                    try {
                        saver.addObject(ob);
                    } catch (Exception e) {
                    }

                }
                String s = out.toString();
                Log.d(TAG, "s length=" + s.length());
                new ShareCommand(this, s).execute();
                return true;
            case R.id.find_view_db_menu:
                item_selected = -1;//clear previous "find"
                mAdapter.notifyDataSetChanged();
                String init_string = SettingsActivity.getStringFromSharedPreferences(this, Constants.VDA_FIND_STRING, "");//Settings1243.getSharedPreferences(this).


                InputDialog d = new InputDialog(ViewDatabaseActivity.this);
                d.setValue(init_string);
                d.setType(InputDialog.DType.INPUT_STRING);
                d.setTitle(getString(R.string.find));
                d.setMessage(getString(R.string.please_enter_the_string_to_find_in_the_list));
                d.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
                    public void onClick(String value) {
                        String find_string = InputDialog.getResult();
                        SettingsActivity.putSharedPreferences(Constants.VDA_FIND_STRING, find_string, ViewDatabaseActivity.this);

                        Holder3<String, Cursor, AstroCatalog> data = new Holder3<String, Cursor, AstroCatalog>(InputDialog.getResult(), mAdapter.getCursor(), catalog);
                        controller.handleMessage(ViewDatabaseController.MESSAGE_FIND, data);
                    }
                });
                d.setNegativeButton(getString(R.string.cancel));
                registerDialog(d).show();
                return true;
            case R.id.next_view_db_menu:
                controller.handleMessage(ViewDatabaseController.MESSAGE_FIND_NEXT, null);
                return true;
            case R.id.export_view_db_menu:
                final ExportData.Predicate<OutputStream> r = outstream -> {
                    boolean noError = true;
                    InfoListSaver saver_ = null;
                    Cursor cursor_ = null;
                    try {
                        cursor_ = catalog.getAll();//use separate cursor as the latter is affected by list view
                        //even if it is blocked by progress dialog
                        if (cursor_ != null) {

                            saver_ = new InfoListStringSaverImp(outstream);
                            saver_.addName("");
                            while (cursor_.moveToNext()) {
                                AstroObject obj = catalog.getObjectFromCursor(cursor_);
                                saver_.addObject(obj);
                            }
                        } else
                            noError = false;
                    } catch (Throwable e) {
                        Log.d(TAG, "Exception=" + e);
                        noError = false;
                    } finally {
                        try {
                            saver_.close();
                        } catch (Exception e) {

                        }
                    }
                    return noError;
                };
                exportData.setCodeToRun(r);
                exportData.start();
        }
        return true;

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_database_context_menu, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

        return parseContextMenu(item.getItemId(), (int) info.id) ? true : super.onContextItemSelected(item);
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.d(TAG, "message=" + msg.what);
        switch (msg.what) {
            case ViewDatabaseController.MESSAGE_ERROR_HANDLER:
                ErrorHandler eh = (ErrorHandler) msg.obj;
                if (eh.hasError())
                    eh.showError(this);
                return true;
            case ViewDatabaseController.MESSAGE_UPDATE_VIEW:
                Cursor cursor = mAdapter.getCursor();
                if (cursor != null) cursor.close();
                mAdapter.setCursor((Cursor) msg.obj);
                mAdapter.notifyDataSetChanged();
                updateObjNum();
                if (pd != null) {
                    pd.dismiss();
                    pd = null;
                }
                controller.handleMessage(ViewDatabaseController.MESSAGE_RESET_FIND);
                return true;
            case ViewDatabaseController.MESSAGE_SET_LIST_LOCATION:
                item_selected = msg.arg1;
                mAdapter.notifyDataSetChanged();
                getListView().setSelection(msg.arg1);
                return true;
            case ViewDatabaseController.MESSAGE_TEXT:
                registerDialog(InputDialog.message(this, (String) msg.obj, 0)).show();
                return true;
            case ViewDatabaseController.MESSAGE_TEXT_FIND:
                InputDialog d = InputDialog.message(this, (String) msg.obj, 0);
                d.setTitle(this.getString(R.string.find_results));
                registerDialog(d).show();
                return true;
            case ViewDatabaseController.MESSAGE_INPROGRESS:
                if (pd == null) {
                    Log.d(TAG, "in progress started");

                    pd = InputDialog.pleaseWait(ViewDatabaseActivity.this, getString(R.string.please_wait)).pop();
                }
                return true;
            case ViewDatabaseController.MESSAGE_REMOVE_INPROGRESS:
                if (pd != null) {
                    pd.dismiss();
                    pd = null;
                }
                return true;
        }
        return false;
    }

    private boolean parseContextMenu(int itemId, int objIndex) {
        AstroObject obj = mAdapter.getItem(objIndex);//catalog.getObjectFromCursor(cursor);
        if (obj == null) return true;
        Log.d(TAG, "obj=" + obj);

        NoteRequest request;
        Command command;
        switch (itemId) {
            case R.id.vseepic:
                command = new PictureCommand(this, obj);
                command.execute();
                return true;
            case R.id.vadd:
                InfoListFiller filler = new ObsListFiller(Arrays.asList(new AstroObject[]{obj}));
                int obsList = SettingsActivity.getSharedPreferences(this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);

                ListHolder.getListHolder().get(obsList).fill(filler);
                dirtyObsPref = true;
                return true;


            case R.id.vdetails:
                new DetailsCommand(obj, this).execute();
                return true;
            case R.id.vaddnote:
                command = new NewNoteCommand(this, obj, Calendar.getInstance(), "");
                command.execute();
                return true;
            case R.id.vseenotes:
                command = new GetObjectNotesCommand(this, obj);
                command.execute();
                return true;
            case R.id.vseeallnotes:
                command = new GetAllNotesCommand(this);
                command.execute();
                return true;
            case R.id.vremoveall:
                if (obj.getCatalog() == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG
                ) {
                    registerDialog(InputDialog.message(ViewDatabaseActivity.this, getString(R.string.internal_objects_could_not_be_removed_), 0)).show();
                    return true;
                }

                InputDialog dl = new InputDialog(this);
                dl.setTitle(getString(R.string.warning));
                dl.setMessage(getString(R.string.do_you_want_to_remove_all_of_the_database_items_));
                dl.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
                    public void onClick(String v) {
                        controller.handleMessage(ViewDatabaseController.MESSAGE_REMOVE_ALL, catalog);
                    }
                });
                dl.setNegativeButton(getString(R.string.cancel));
                registerDialog(dl).show();
                return true;

            case R.id.vremove:
                class DialogImpl implements InputDialog.OnButtonListener {
                    AstroObject obj;

                    public DialogImpl(AstroObject obj) {
                        this.obj = obj;
                    }

                    public void onClick(String v) {

                        try {

                            catalog.remove(obj.getId());//better to place catalog into controller as well!
                            controller.handleMessage(ViewDatabaseController.MESSAGE_UPDATE, catalog);

                        } catch (UnsupportedOperationException e) {
                            InputDialog.message(ViewDatabaseActivity.this, R.string.operation_not_supported).show();
                        }
                    }
                }
                if (obj.getCatalog() == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG) {
                    if (obj.getId() <= MINOR_PLANET_LARGEST_INTERNAL_ID) { //id starts from 1
                        registerDialog(InputDialog.message(ViewDatabaseActivity.this, getString(R.string.internal_objects_could_not_be_removed_), 0)).show();
                        return true;
                    }
                }

                InputDialog dl2 = new InputDialog(ViewDatabaseActivity.this);
                dl2.setTitle(getString(R.string.warning));
                dl2.setMessage(getString(R.string.do_you_want_to_remove_this_item_));
                dl2.setPositiveButton(getString(R.string.ok), new DialogImpl(obj));
                dl2.setNegativeButton(getString(R.string.cancel));
                registerDialog(dl2).show();
                return true;
            case R.id.vedit:
                if (obj instanceof CustomObject) {
                    SettingsActivity.putSharedPreferences(Constants.NCOA_OBJECT, obj, this);
                    Intent i = new Intent(this, NewCustomObjectActivityAsList.class);
                    i.putExtra(EDIT, true);
                    i.putExtra(CATALOG_POSITION, dbpos);
                    startActivityForResult(i, REQUEST_CODE);
                } else {
                    registerDialog(InputDialog.message(ViewDatabaseActivity.this, R.string.operation_not_applicable, 0)).show();
                }
                return true;

            default:
                return false; //super.onContextItemSelected(item)	;
        }
    }

    private void startImporting(String dataLocation) {
        Intent intent = new Intent(this, ImportDatabaseIntentService.class);
        intent.putExtra(Constants.IDIS_CATALOG, item.cat);
        intent.putExtra(Constants.IDIS_DBNAME, item.dbFileName);
        intent.putExtra(Constants.IDIS_PASTING, false);
        intent.putExtra(Constants.IDIS_FILENAME, dataLocation);
        intent.putExtra(Constants.IDIS_IGNORE_NGCIC_REF, true);
        intent.putExtra(Constants.IDIS_IGNORE_CUSTOMDB_REF, true);
        if (!item.ftypes.isEmpty())
            intent.putExtra(Constants.IDIS_FTYPES, item.ftypes.getByteRepresentation());
        catalog.close();
        ImportDatabaseIntentService.registerImportToService(item.dbFileName);
        try {
            startService(intent);
        } catch (Exception e) {
        }

        finish();
    }

    private void startPasting() {
        Intent intent = new Intent(this, ImportDatabaseIntentService.class);
        intent.putExtra(Constants.IDIS_CATALOG, item.cat);
        intent.putExtra(Constants.IDIS_DBNAME, item.dbFileName);
        intent.putExtra(Constants.IDIS_PASTING, true);
        intent.putExtra(Constants.IDIS_IGNORE_NGCIC_REF, true);
        intent.putExtra(Constants.IDIS_IGNORE_CUSTOMDB_REF, true);
        if (!item.ftypes.isEmpty())
            intent.putExtra(Constants.IDIS_FTYPES, item.ftypes.getByteRepresentation());
        ImportDatabaseIntentService.registerImportToService(item.dbFileName);
        try {
            startService(intent);
        } catch (Exception e) {
        }
        catalog.close();
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (ALEX_MENU_FLAG && keyCode == KeyEvent.KEYCODE_MENU) {
            aMenu.show(findViewById(R.id.view_database_text));
            return true; //always eat it!
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initAlexMenu() {

        boolean dayMode = !nightMode;

        aMenu = new alexMenu(this, new OnMenuItemSelectedListener() {
            public void MenuItemSelectedEvent(alexMenuItem selection) {
                parseMenu(selection.getId());
            }
        },
                getLayoutInflater());
        aMenu.setHideOnSelect(true);
        aMenu.setItemsPerLineInPortraitOrientation(5);
        aMenu.setItemsPerLineInLandscapeOrientation(53);
        aMenu.setSkin(nightMode, SettingsActivity.getDarkSkin());

        //mine
        float text_size = getResources().getDimension(R.dimen.text_size_small);//mine
        float density = getResources().getDisplayMetrics().density;
        text_size = text_size / density;
        aMenu.setTextSize((int) text_size);

        //load the menu items
        ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();

        boolean not_edit = SearchRules.isInternalCatalog(item.cat);
        if (item.cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG || item.cat == AstroCatalog.COMET_CATALOG)
            not_edit = false;
        if (!Global.BASIC_VERSION && !not_edit) menuItems.add(new alexMenuItem(R.id.import_query,
                getString(R.string.import2), dayMode ? R.drawable.am_save_v : R.drawable.ram_save_v, true));
        if (!Global.BASIC_VERSION && !not_edit)
            menuItems.add(new alexMenuItem(R.id.paste_view_db_menu,
                    getString(R.string.paste), dayMode ? R.drawable.am_paste_v : R.drawable.ram_paste_v, true));
        if (!(Global.BASIC_VERSION || (item.cat == AstroCatalog.SNOTES)))
            menuItems.add(new alexMenuItem(R.id.export_view_db_menu,
                    getString(R.string.export), dayMode ? R.drawable.am_load_v : R.drawable.ram_load_v, true));
        if (!Global.BASIC_VERSION && !not_edit)
            menuItems.add(new alexMenuItem(R.id.add_view_db_menu,
                    getString(R.string.add_object), dayMode ? R.drawable.am_addobj_v : R.drawable.ram_addobj_v, true));
        menuItems.add(new alexMenuItem(R.id.find_view_db_menu,
                getString(R.string.find), dayMode ? R.drawable.am_find_in_page_v : R.drawable.ram_find_in_page_v, true));
        menuItems.add(new alexMenuItem(R.id.next_view_db_menu,
                getString(R.string.next), dayMode ? R.drawable.am_tplus_v : R.drawable.ram_tplus_v, true));

        if (item.cat == AstroCatalog.COMET_CATALOG || item.cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG) {
            menuItems.add(new alexMenuItem(R.id.update_view_db_menu,
                    getString(R.string.update), dayMode ? R.drawable.am_sync_v : R.drawable.ram_sync_v, true));
        }

        if (aMenu.isNotShowing()) {
            try {
                aMenu.setMenuItems(menuItems);
            } catch (Exception e) {
                InputDialog.message(ViewDatabaseActivity.this, MENU_ERROR + e.getMessage(), 0).show();
            }
        }
    }

    private void initAlexContextMenu() {
        contextMenu = new alexMenu(this, new OnMenuItemSelectedListener() {
            public void MenuItemSelectedEvent(alexMenuItem selection) {
                parseContextMenu(selection.getId(), contextMenu.getMenuItemId());
            }
        },
                getLayoutInflater());
        contextMenu.setHideOnSelect(true);
        contextMenu.setItemsPerLineInPortraitOrientation(1);
        contextMenu.setItemsPerLineInLandscapeOrientation(1);
        contextMenu.setSkin(nightMode, SettingsActivity.getDarkSkin());
        //mine
        float text_size = getResources().getDimension(R.dimen.table_main_text_size);//mine
        float density = getResources().getDisplayMetrics().density;
        text_size = text_size / density;
        contextMenu.setTextSize((int) text_size);//contextMenu.setTextSize(18)

        contextMenu.makeFloat();

        //load the menu items
        ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();

        boolean edit = SearchRules.isEdited(item.cat);//SearchRules.isInternalCatalog(item.cat);
        boolean snotes = item.cat == AstroCatalog.SNOTES;
        menuItems.add(new alexMenuItem(R.id.vseepic, getString(R.string.show_image), 0, true));
        if (!snotes)
            menuItems.add(new alexMenuItem(R.id.vadd, getString(R.string.add_to_observation_list), 0, true));

        menuItems.add(new alexMenuItem(R.id.vdetails, getString(R.string.details), 0, true));
        menuItems.add(new alexMenuItem(R.id.vaddnote, getString(R.string.add_note), 0, true));
        menuItems.add(new alexMenuItem(R.id.vseenotes, getString(R.string.see_object_notes), 0, true));
        menuItems.add(new alexMenuItem(R.id.vseeallnotes, getString(R.string.see_all_notes), 0, true));
        if (edit && !Global.BASIC_VERSION)
            menuItems.add(new alexMenuItem(R.id.vremove, getString(R.string.remove_from_list), 0, true));
        if (edit && !Global.BASIC_VERSION)
            menuItems.add(new alexMenuItem(R.id.vremoveall, getString(R.string.remove_all), 0, true));
        if (edit) menuItems.add(new alexMenuItem(R.id.vedit, getString(R.string.edit), 0, true));
        if (contextMenu.isNotShowing()) {
            try {
                contextMenu.setMenuItems(menuItems);
            } catch (Exception e) {
                InputDialog.message(ViewDatabaseActivity.this, MENU_ERROR2 + e.getMessage(), 0).show();
            }
        }
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v, int index, long arg3) {
                String prefix = (curDb == AstroCatalog.NGCIC_CATALOG ? NGC : "");
                contextMenu.setMenuItemId(index);
                contextMenu.setHeader(prefix + ((TextView) v.findViewById(R.id.dsosel_ngcnum)).getText());
                contextMenu.show(v);
                return true;
            }

        });
    }


    public void callbackCall(Uri uri) {
        SettingsActivity.persistUriIfNeeded(uri, getApplicationContext());
        startImport(uri.toString());
    }

    public void startImport(String dataLocation) {
        Runnable r = new Runnable() {
            public void run() {
                startImporting(dataLocation);
            }

        };
        //Confirmation
        InputDialog dialog1 = AstroTools.getDialog(ViewDatabaseActivity.this,
                getString(R.string.do_you_really_want_to_import_current_database_data_from_)
                        + FileTools.getDisplayName(getApplicationContext(), Uri.parse(dataLocation))
                        + "?", r);
        registerDialog(dialog1).show();
    }

    @Override
    protected int getTestActivityNumber() {
        return TestIntentService.VDA;
    }

    @Override
    protected void startTest(int action, int param) {
        super.startTest(action, param);
        switch (action) {
            case TestIntentService.VDA_IMPORT:
                File f = new File(Global.exportImportPath, "test.txt");
                callbackCall(Uri.fromFile(f));
                break;
        }
    }

    //Gesture Detector (just implement OnGestureListener in the Activity)
    GestureDetector gDetector = new GestureDetector(this);

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        return gDetector.onTouchEvent(me);
    }

    public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
        if (start == null || finish == null) return false;
        float dy = start.getRawY() - finish.getRawY();
        float dx = start.getRawX() - finish.getRawX();
        if (dy > Global.flickLength) { //up
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU));
            return true;
        } else if (dx > Global.flickLength) { //left
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
            return true;
        }
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onDown(MotionEvent e) {
        return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
    //-----------
}
