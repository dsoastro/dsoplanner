package com.astro.dsoplanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.astro.dsoplanner.InputDialog.OnButtonListener;
import com.astro.dsoplanner.alexmenu.alexMenu;
import com.astro.dsoplanner.alexmenu.alexMenu.OnMenuItemSelectedListener;
import com.astro.dsoplanner.alexmenu.alexMenuItem;
import com.astro.dsoplanner.database.TelescopeDatabase;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.InfoListImpl;
import com.astro.dsoplanner.infolist.InfoListLoader;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListStringLoaderImp;
import com.astro.dsoplanner.infolist.InfoListStringSaverImp;
import com.astro.dsoplanner.infolist.ListHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.OutputStream;

public class TelescopeListActivity extends ParentListActivity implements OnGestureListener {

    private static final int TELESCOPE_DIALOG_NEW = 1;
    private static final int TELESCOPE_DIALOG_EDIT = 2;

    private static final String REC2 = "rec";
    private static final String MENU_ERROR2 = "Menu error! ";
    private static final String MENU_ERROR = MENU_ERROR2;
    private static final String IMPORT = "Import";
    private final int MENU_IMPORT = 1;
    private final int MENU_EXPORT = 2;
    private final int MENU_EDIT = 3;
    private final int MENU_REMOVE = 4;
    private final int MENU_REMOVEALL = 5;
    private final int MENU_EPDB = 6;  //Open eyepieces database
    private final int MENU_EPADD = 7; //Add eyepieces to the telescope
    private static final int EXPORT_CODE = 1;

    public static final int MAX_FREE_TP = 1;
    InputDialog pd = null;
    int dbID = -1;
    int listID = -1;
    int ngc = 0;
    TelescopeRequest mRequest;
    String action;

    private Button bN;
    private alexMenu aMenu;
    private alexMenu contextMenu;
    private TLadapter mAdapter;
    TelescopeRecord rec;
    ExportData exportData;

    private static final String TAG = TelescopeListActivity.class.getSimpleName();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (pd != null) pd.dismiss();
            SettingsActivity.initTelescope(getApplicationContext());
            updateListArray();
        }
    };

    private String _ep;

    class TelescopeListFiller implements InfoListFiller {
        List<TelescopeRecord> list = new ArrayList<TelescopeRecord>();

        public TelescopeListFiller() {
            TelescopeDatabase db = new TelescopeDatabase();
            db.open();
            list = db.search();
            db.close();
        }

        public Iterator getIterator() {
            return list.iterator();
        }
    }

    private class TLadapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private InfoList list = ListHolder.getListHolder().get(InfoList.TELESCOPE_LIST);

        public TLadapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.telescopelist_item, null);
            }

            TelescopeRecord rec = (TelescopeRecord) list.get(position);

            ((TextView) convertView.findViewById(R.id.scope_name)).setText(""
                    + rec.name
                    + " (" + (int) rec.aperture + "x" + (int) rec.focus
                    + ((rec.ep_id > 0) ? _ep : ")")); //eyepieces mark
            ((TextView) convertView.findViewById(R.id.scope_params)).setText("" + rec.note);

            //make dark background
            if (SettingsActivity.getDarkSkin() || SettingsActivity.getNightMode())
                convertView.setBackgroundColor(0xff000000);

            ((CheckBox) convertView.findViewById(R.id.scope_selected))
                    .setChecked(rec.id == SettingsActivity.getTId());
            if (actionSelected) {
                ((CheckBox) convertView.findViewById(R.id.scope_selected)).setEnabled(false);
            }
            return convertView;
        }

        public int getCount() {
            return list.getCount();
        }

        public Object getItem(int position) {
            return list.get(position);
        }

        public long getItemId(int position) {
            return position;
        }
    }

    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        onResumeCode();
        super.onResume();
    }

    private void onResumeCode() {
        InfoList list = ListHolder.getListHolder().get(InfoList.TELESCOPE_LIST);
        list.removeAll();
        list.fill(new TelescopeListFiller());
        mAdapter.notifyDataSetChanged();
    }

    boolean actionSelected = false;
    Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mRequest = new TelescopeRequest();

            setContentView(R.layout.activity_telescope_list);
            action = getIntent().getAction();
            if (Constants.ACTION_TELESCOPE_SELECT.equals(action))
                actionSelected = true;
            //Set List view
            mAdapter = new TLadapter();
            setListAdapter(mAdapter);

            initAlexMenu();
            initAlexContextMenu();

            //add new telescope button
            bN = (Button) findViewById(R.id.scope_new);
            bN.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {

                    final InputDialog d = new InputDialog(TelescopeListActivity.this);
                    d.setTitle(getString(R.string.telescope_parameters));
                    d.insertLayout(R.layout.telescope_dialog);
                    d.setPositiveButton(getString(R.string.save), new InputDialog.OnButtonListener() {
                        public void onClick(String value) {
                            String name = ((EditText) d.findViewById(R.id.scope_name)).getText().toString();
                            String note = ((EditText) d.findViewById(R.id.scope_note)).getText().toString();
                            Double ap = AstroTools.getDouble(((EditText) d.findViewById(R.id.scope_a)).getText().toString(), 0, 0, 100000);
                            Double fo = AstroTools.getDouble(((EditText) d.findViewById(R.id.scope_f)).getText().toString(), 0, 0, 100000);
                            Double pa = AstroTools.getDouble(((EditText) d.findViewById(R.id.scope_p)).getText().toString(), 70, 0, 100);
                            int epid = 0; //temporary

                            TelescopeDatabase db = new TelescopeDatabase();
                            db.open();
                            db.add(name, ap, fo, pa, epid, note);
                            db.close();

                            InfoList list = ListHolder.getListHolder().get(InfoList.TELESCOPE_LIST);
                            list.removeAll();
                            list.fill(new TelescopeListFiller());
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                    d.setNegativeButton(getString(R.string.cancel));
                    registerDialog(d).show();
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exportData = new ExportData(this, EXPORT_CODE, "");
        _ep = getString(R.string._ep);
        initHandler.handleMessage(null);
    }

    public boolean parseMenu(int id) {
        switch (id) {
            case MENU_EXPORT:
                final ExportData.Predicate<OutputStream> r = outstream -> {

                    InfoList infoList = ListHolder.getListHolder().get(InfoList.TELESCOPE_LIST);
                    InfoListSaver saver = new InfoListStringSaverImp(outstream);

                    try {
                        boolean noError = infoList.save(saver);
                        return noError;
                    } catch (Exception ignore) {
                        return false;
                    } finally {
                        try {
                            saver.close();
                        } catch (Exception e) {
                        }
                    }

                };
                exportData.setCodeToRun(r);
                exportData.start();
                return true;
            case MENU_IMPORT: //import more records to the current list
                IPickFileCallback listener = new IPickFileCallback() {

                    void handleErrorIfAny(ErrorHandler eh) {
                        if (eh.hasError()) {
                            eh.showError(TelescopeListActivity.this);
                        }
                    }

                    public void callbackCall(Uri uri) {
                        try {
                            readStream(getContentResolver().openInputStream(uri));
                        } catch (IOException e) {
                            //e.printStackTrace();
                            handleErrorIfAny(new ErrorHandler(ErrorHandler.IO_ERROR, e.getMessage()));
                        }
                    }

                    public void readStream(InputStream inputStream) {
                        InfoList list = new InfoListImpl("", TelescopeRecord.class);
                        InfoListLoader loader = new InfoListStringLoaderImp(inputStream);
                        handleErrorIfAny(list.load(loader));
                        TelescopeDatabase db = new TelescopeDatabase();
                        db.open();
                        for (Object o : list) {
                            db.add((TelescopeRecord) o);
                        }
                        db.close();
                        handler.sendEmptyMessage(0);
                    }
                };

                SelectFileActivity.setPath(SettingsActivity.getFileDialogPath(getApplicationContext()));
                SelectFileActivity.setListener(listener);
                Intent fileDialog = new Intent(this, SelectFileActivity.class);
                startActivity(fileDialog);
                return true;

            case MENU_EPDB: //show eyepieces activity (just launcehr, not for linking)
                Intent i = new Intent(this, EyepiecesListActivity.class);
                i.putExtra(Global.EPDATABASE_CALLER, 1);
                startActivity(i);
                return true;

        }
        return false;
    }

    private boolean parseContextMenu(int itemId, int id) {
        final TelescopeRecord rec = (TelescopeRecord) ListHolder.getListHolder()
                .get(InfoList.TELESCOPE_LIST).get(id);
        Log.d(TAG, "trec=" + rec);
        switch (itemId) {
            case MENU_EDIT:
                mRequest = new TelescopeRequest(rec);
                final InputDialog de = new InputDialog(TelescopeListActivity.this);
                de.setTitle(getString(R.string.telescope_parameters));
                de.insertLayout(R.layout.telescope_dialog);
                //init fields
                ((EditText) de.findViewById(R.id.scope_name)).setText(
                        String.valueOf(mRequest.getRecord().name));
                ((EditText) de.findViewById(R.id.scope_a)).setText(
                        String.valueOf(mRequest.getRecord().aperture));
                ((EditText) de.findViewById(R.id.scope_f)).setText(
                        String.valueOf(mRequest.getRecord().focus));
                ((EditText) de.findViewById(R.id.scope_p)).setText(
                        String.valueOf(mRequest.getRecord().pass));
                ((EditText) de.findViewById(R.id.scope_note)).setText(
                        String.valueOf(mRequest.getRecord().note));
                final int epid = mRequest.getRecord().ep_id;
                de.setPositiveButton(getString(R.string.save), new InputDialog.OnButtonListener() {
                    public void onClick(String value) {
                        String name = ((EditText) de.findViewById(R.id.scope_name)).getText().toString();
                        String note = ((EditText) de.findViewById(R.id.scope_note)).getText().toString();
                        Double ap = AstroTools.getDouble(((EditText) de.findViewById(R.id.scope_a)).getText().toString(), 0, 0, 100000);
                        Double fo = AstroTools.getDouble(((EditText) de.findViewById(R.id.scope_f)).getText().toString(), 0, 0, 100000);
                        Double pa = AstroTools.getDouble(((EditText) de.findViewById(R.id.scope_p)).getText().toString(), 0, 0, 100);

                        mRequest.record.name = name;
                        mRequest.record.aperture = ap;
                        mRequest.record.focus = fo;
                        mRequest.record.pass = pa;
                        mRequest.record.ep_id = epid;
                        mRequest.record.note = note;

                        TelescopeDatabase db = new TelescopeDatabase();
                        db.open();
                        db.edit(mRequest.record);
                        db.close();

                        handler.sendEmptyMessage(0);
                    }
                });
                de.setNegativeButton(getString(R.string.cancel));
                registerDialog(de).show();

                return true;
            case MENU_REMOVE:
                final int listID = id;

                InputDialog dl = new InputDialog(TelescopeListActivity.this);
                dl.setTitle(getString(R.string.warning));
                dl.setMessage(getString(R.string.do_you_want_to_remove_this_instrument_));
                dl.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
                    public void onClick(String value) {
                        TelescopeDatabase db = new TelescopeDatabase();
                        db.open();
                        db.remove(rec);
                        db.close();

                        ListHolder.getListHolder().get(InfoList.TELESCOPE_LIST).remove(listID);

                        handler.sendEmptyMessage(0);
                    }
                });
                dl.setNegativeButton(getString(R.string.cancel));
                registerDialog(dl).show();

                return true;
            case MENU_REMOVEALL:
                InputDialog dl2 = new InputDialog(TelescopeListActivity.this);
                dl2.setTitle(getString(R.string.warning));
                dl2.setMessage(getString(R.string.do_you_want_to_remove_all_of_your_instruments_));
                dl2.setPositiveButton(getString(R.string.ok), new OnButtonListener() {
                    public void onClick(String v) {
                        ListHolder.getListHolder().get(InfoList.TELESCOPE_LIST).removeAll();
                        TelescopeDatabase db = new TelescopeDatabase();
                        db.open();
                        db.removeAll();
                        db.close();
                        handler.sendEmptyMessage(0);
                    }
                });
                dl2.setNegativeButton(getString(R.string.cancel));
                registerDialog(dl2).show();

                return true;
            case MENU_EPADD: //Add set of eyepieces
                Intent i = new Intent(this, EyepiecesListActivity.class);
                i.putExtra(Global.EPDATABASE_CALLER, 2);
                i.putExtra(TelescopeRecord.TEP, rec.ep_id);
                i.putExtra(TelescopeRecord.TID, rec.id);
                startActivityForResult(i, MENU_EPADD);
                return true;

            default:
                return false;
        }

    }

    private void updateListArray() {
        mAdapter.notifyDataSetChanged();
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        Log.d(TAG, "item click");
        TelescopeRecord rec = (TelescopeRecord) ListHolder.getListHolder().get(InfoList.TELESCOPE_LIST).get(position);
        if (actionSelected) {
            Intent intent = new Intent();
            intent.putExtra(Constants.SELECTED_TELESCOPE, rec.getSummary(getApplicationContext()));
            setResult(RESULT_OK, intent);
            finish();
            return;
        }


        SettingsActivity.setCurrentTelescope(rec);

        //move checkbox
        removeAllChecks((ViewGroup) findViewById(R.id.teleMain));
        ((CheckBox) v.findViewById(R.id.scope_selected)).setChecked(true);
        SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, this);
    }

    //recursive blind checks removal for everything inside
    private void removeAllChecks(ViewGroup vg) {
        View v = null;
        for (int i = 0; i < vg.getChildCount(); i++) {
            try {
                v = vg.getChildAt(i);
                ((CheckBox) v).setChecked(false);
            } catch (Exception e1) { //if null tag, null View
                try {
                    removeAllChecks((ViewGroup) v);
                } catch (Exception e2) {
                } //v is not a view group
            }
        }

    }

    //overriding buttons
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            aMenu.show(bN);
            return true;
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
        aMenu.setItemsPerLineInPortraitOrientation(4);
        aMenu.setItemsPerLineInLandscapeOrientation(4);
        aMenu.setSkin(nightMode, SettingsActivity.getDarkSkin());

        //mine
        float text_size = getResources().getDimension(R.dimen.text_size_small);//mine
        float density = getResources().getDisplayMetrics().density;
        text_size = text_size / density;
        aMenu.setTextSize((int) text_size);

        //load the menu items
        ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();

        menuItems.add(new alexMenuItem(MENU_EXPORT,
                getString(R.string.export), dayMode ? R.drawable.am_load_v : R.drawable.ram_load_v, true));
        menuItems.add(new alexMenuItem(MENU_IMPORT,
                getString(R.string.import2), dayMode ? R.drawable.am_save_v : R.drawable.ram_save_v, true));
        menuItems.add(new alexMenuItem(MENU_EPDB,
                getString(R.string.eyepieces_db), dayMode ? R.drawable.am_db_v : R.drawable.ram_db_v, true));

        if (aMenu.isNotShowing()) {
            try {
                aMenu.setMenuItems(menuItems);
            } catch (Exception e) {
                InputDialog.message(TelescopeListActivity.this, MENU_ERROR + e.getMessage(), 0).show();
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

        menuItems.add(new alexMenuItem(MENU_EDIT, getString(R.string.edit), 0, true));
        menuItems.add(new alexMenuItem(MENU_EPADD, getString(R.string.assign_eyepieces), 0, true));
        menuItems.add(new alexMenuItem(MENU_REMOVE, getString(R.string.remove), 0, true));
        menuItems.add(new alexMenuItem(MENU_REMOVEALL, getString(R.string.remove_all), 0, true));

        if (contextMenu.isNotShowing()) {
            try {
                contextMenu.setMenuItems(menuItems);
            } catch (Exception e) {
                InputDialog.message(TelescopeListActivity.this, MENU_ERROR2 + e.getMessage(), 0).show();
            }
        }
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v, int index, long arg3) {
                contextMenu.setMenuItemId(index);
                contextMenu.setHeader(((TextView) v.findViewById(R.id.scope_name)).getText());
                contextMenu.show(v);
                return true;
            }

        });
    }

    @Override
    protected void onDestroy() {
        try {
            aMenu.hide();
            contextMenu.hide();
        } catch (Exception e) {
        }
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case MENU_EPADD:
                if (resultCode == Activity.RESULT_OK) {
                    if (handler != null) handler.sendEmptyMessage(0);
                }
                break;
            case EXPORT_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    exportData.process(data);
                }
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

