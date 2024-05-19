package com.astro.dsoplanner;

import static com.astro.dsoplanner.Global.ALEX_MENU_FLAG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.astro.dsoplanner.alexmenu.alexMenu;
import com.astro.dsoplanner.alexmenu.alexMenu.OnMenuItemSelectedListener;
import com.astro.dsoplanner.alexmenu.alexMenuItem;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.DbManager;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.ListHolder;

public class DatabaseManagerActivity extends ParentListActivity implements OnGestureListener {

    private static final String CATALOG_POSITION = "catalogPosition";


    private static final String TAG = DatabaseManagerActivity.class.getSimpleName();

    ListAdapter mAdapter;
    private alexMenu aMenu;
    private alexMenu contextMenu;

    public static class DbListFiller implements InfoListFiller {
        private Collection<DbListItem> collection;

        public DbListFiller(Collection<DbListItem> collection) {
            this.collection = collection;
        }

        public Iterator getIterator() {
            return collection.iterator();
        }
    }

    private class ListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ListAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.database_item, null);
            }

            InfoList dblist = ListHolder.getListHolder().get(InfoList.DB_LIST);
            DbListItem item = (DbListItem) dblist.get(position);
            ((TextView) convertView.findViewById(R.id.dm_text)).setText(item.dbName);

            //make dark background
            if (SettingsActivity.getDarkSkin() || SettingsActivity.getNightMode())
                convertView.setBackgroundColor(0xff000000);

            return convertView;
        }

        public int getCount() {
            InfoList dblist = ListHolder.getListHolder().get(InfoList.DB_LIST);
            int count = dblist.getCount();
            Log.d(TAG, "count=" + count);
            return count;
        }

        public Object getItem(int position) {
            InfoList dblist = ListHolder.getListHolder().get(InfoList.DB_LIST);
            return dblist.get(position);
        }

        public long getItemId(int position) {
            return position;
        }
    }

    @Override
    public void onResume() {
        onResumeCode();
        super.onResume();
        hideMenuBtn();
    }

    private void onResumeCode() {
        mAdapter.notifyDataSetChanged();//needed in case database updated with new fields

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
    }

    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
    }

    private Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (Global.BASIC_VERSION) {
                setContentView(R.layout.dbmanagerbasic);
            } else setContentView(R.layout.dbmanager);

            mAdapter = new ListAdapter();
            setListAdapter(mAdapter);

            //Disable fading edge at night
            boolean nightmode = SettingsActivity.getNightMode();
            getListView().setVerticalFadingEdgeEnabled(!nightmode);

            if (!Global.BASIC_VERSION) {
                findViewById(R.id.dbistaddnew).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        final InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);

                        AstroTools.NameDialogRunnable r = new AstroTools.NameDialogRunnable() {
                            private String name;

                            public void setName(String name) {
                                this.name = name;
                            }

                            public void run() {
                                if (name.isEmpty()) return;

                                class CreateNewDatabase implements Runnable {
                                    String name;

                                    class AddFieldsRunnable implements Runnable {
                                        String name;

                                        public AddFieldsRunnable(String name) {
                                            this.name = name;
                                        }

                                        public void run() {
                                            Intent intent = new Intent(DatabaseManagerActivity.this, FieldSelectionActivityNew.class);
                                            SettingsActivity.putSharedPreferences(Constants.FSA_DBNAME, name, DatabaseManagerActivity.this);
                                            startActivity(intent);
                                        }
                                    }

                                    class NoFieldsRunnable implements Runnable {
                                        String name;

                                        public NoFieldsRunnable(String name) {
                                            this.name = name;
                                        }

                                        public void run() {
                                            DbManager.addNewDatabase(new DbListItem.FieldTypes(), DatabaseManagerActivity.this, name, true, null);
                                            mAdapter.notifyDataSetChanged();
                                        }
                                    }

                                    public CreateNewDatabase(String name) {
                                        this.name = name;
                                    }

                                    public void run() {
                                        InputDialog dialog = AstroTools.getDialog(DatabaseManagerActivity.this, DatabaseManagerActivity.this.getString(R.string.do_you_want_to_add_custom_data_fields_), new AddFieldsRunnable(name), new NoFieldsRunnable(name));
                                        dialog.setPositiveButton(DatabaseManagerActivity.this.getString(R.string.yes));
                                        dialog.setNegativeButton(DatabaseManagerActivity.this.getString(R.string.no));
                                        registerDialog(dialog);
                                        dialog.show();
                                    }
                                }

                                new CreateNewDatabase(name).run();
                            }
                        };
                        InputDialog d = AstroTools.getNameDialog(DatabaseManagerActivity.this, getString(R.string.set_database_name), r);
                        registerDialog(d).show();

                    }
                });
            }
            if (ALEX_MENU_FLAG) {
                initAlexContextMenu();
            } else registerForContextMenu(getListView());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHandler.handleMessage(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        new Prefs(this).saveList(InfoList.DB_LIST);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dbmanager_context_menu, menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int pos = (int) info.id;
        return parseContextMenu(item.getItemId(), pos) ? true : super.onContextItemSelected(item);
    }

    public boolean parseContextMenu(int id, int index) {
        switch (id) {
            case R.id.dbmanager_fields:
                InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
                DbListItem item = (DbListItem) iL.get(index);
                String s = "";
                for (Map.Entry<String, DbListItem.FieldTypes.TYPE> e : item.ftypes.getNameTypeMap().entrySet()) {
                    s = s + e.getKey() + " " + e.getValue().name() + "\n";
                }
                if (s.isEmpty())
                    registerDialog(InputDialog.message(this, R.string.no_additional_fields, 0)).show();
                else registerDialog(InputDialog.message(this, s, 0)).show();
                return true;
            case R.id.dbmanager_remove:
                class RemoveDatabase implements Runnable {

                    private int pos;

                    public RemoveDatabase(int pos) {
                        this.pos = pos;
                    }

                    public void run() {
                        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
                        DbListItem item = (DbListItem) iL.get(pos);
                        if (SearchRules.isInternalCatalog(item.cat)) {//NGCIC
                            registerDialog(InputDialog.message(DatabaseManagerActivity.this, R.string.main_database_can_t_be_removed_)).show();
                            return;
                        }

                        if (ImportDatabaseIntentService.isBeingImported(item.dbFileName)) {
                            registerDialog(InputDialog.message(DatabaseManagerActivity.this, Global.DB_IMPORT_RUNNING)).show();
                            return;
                        }

                        DbManager.removeDatabase(pos, DatabaseManagerActivity.this);
                        mAdapter.notifyDataSetChanged();
                    }
                }
                RemoveDatabase r = new RemoveDatabase(index);
                registerDialog(AstroTools.getDialog(DatabaseManagerActivity.this, getString(R.string.do_you_want_to_remove_the_database_), r)).show();
                return true;
            default:
                return false;
        }
    }


    public void onListItemClick(ListView parent, View v, int position, long id) {

        //SAND Use current time not NOW! Global.cal=Calendar.getInstance();
        Intent i = new Intent(this, ViewDatabaseActivity.class);
        i.putExtra(CATALOG_POSITION, position);
        startActivity(i);

    }

    //overriding buttons
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initAlexContextMenu() {
        contextMenu = new alexMenu(this, new OnMenuItemSelectedListener() {
            public void MenuItemSelectedEvent(alexMenuItem selection) {
                parseContextMenu(selection.getId(), contextMenu.getMenuItemId());
            }
        }, getLayoutInflater());
        contextMenu.setHideOnSelect(true);
        contextMenu.setItemsPerLineInPortraitOrientation(1);
        contextMenu.setItemsPerLineInLandscapeOrientation(1);
        contextMenu.setSkin(SettingsActivity.getNightMode(), SettingsActivity.getDarkSkin());

        //mine
        float text_size = getResources().getDimension(R.dimen.table_main_text_size);//mine
        float density = getResources().getDisplayMetrics().density;
        text_size = text_size / density;
        contextMenu.setTextSize((int) text_size);//contextMenu.setTextSize(18)

        contextMenu.makeFloat();

        //load the menu items
        ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();
        menuItems.add(new alexMenuItem(R.id.dbmanager_fields, getString(R.string.fields), 0, true));
        menuItems.add(new alexMenuItem(R.id.dbmanager_remove, getString(R.string.remove_database), 0, true));

        if (contextMenu.isNotShowing()) {
            try {
                contextMenu.setMenuItems(menuItems);
            } catch (Exception e) {
                InputDialog.message(this, getString(R.string.contextmenu_error_) + e.getMessage(), 0).show();
            }
        }
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v, int index, long arg3) {
                contextMenu.setMenuItemId(index);
                contextMenu.setHeader(((TextView) v.findViewById(R.id.dm_text)).getText());
                contextMenu.show(v);
                return true;
            }

        });
    }

    protected void onDestroy() {
        try {
            aMenu.hide();
            contextMenu.hide();
        } catch (Exception e) {
        }
        super.onDestroy();
    }


    @Override
    protected int getTestActivityNumber() {
        return TestIntentService.DB_MANAGER;
    }


    @Override
    protected void startTest(int action, int param) {
        super.startTest(action, param);
        switch (action) {
            case TestIntentService.DB_MANAGER_ADD:
                findViewById(R.id.dbistaddnew).performClick();
                break;
            case TestIntentService.DB_MANAGER_PRESS_TEST2:
                onListItemClick(null, null, mAdapter.getCount() - 1, 0);
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
}
