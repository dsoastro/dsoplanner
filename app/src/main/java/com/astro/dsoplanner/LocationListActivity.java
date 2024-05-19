package com.astro.dsoplanner;

import static com.astro.dsoplanner.Global.ALEX_MENU_FLAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.astro.dsoplanner.alexmenu.alexMenu;
import com.astro.dsoplanner.alexmenu.alexMenu.OnMenuItemSelectedListener;
import com.astro.dsoplanner.alexmenu.alexMenuItem;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListLoader;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListStringLoaderImp;
import com.astro.dsoplanner.infolist.InfoListStringSaverImp;
import com.astro.dsoplanner.infolist.ListHolder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.io.OutputStream;
import java.io.IOException;

public class LocationListActivity extends ParentListActivity implements IPickFileCallback, GestureDetector.OnGestureListener {


    public static final String POS = "pos";
    private static final String MENU_ERROR = "Menu error! ";

    final static int MENU_IMPORT = 1;
    final static int MENU_EXPORT = 2;
    final static int CONTEXT_MENU_REMOVE = 3;
    final static int MENU_SHARE = 4;
    final static int MENU_PASTE = 5;
    private static final int EXPORT_CODE = 1;
    private LLadapter mAdapter;
    private alexMenu aMenu;
    private alexMenu contextMenu;
    ExportData exportData;

    private class LLadapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public LLadapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.location_item, null);
            }
            InfoList loclist = ListHolder.getListHolder().get(InfoList.LOCATION_LIST);
            LocationItem item = (LocationItem) loclist.get(position);
            ((TextView) convertView.findViewById(R.id.location_name)).setText(item.name);
            String lat = AstroTools.getLatString(item.lat);
            String lon = AstroTools.getLonString(item.lon);
            ((TextView) convertView.findViewById(R.id.location_info)).setText(getString(R.string.latitude_) + lat + getString(R.string._longitude_) + lon);


            //make dark background
            if (SettingsActivity.getDarkSkin() || SettingsActivity.getNightMode())
                convertView.setBackgroundColor(0xff000000);

            return convertView;
        }

        public int getCount() {
            return ListHolder.getListHolder().get(InfoList.LOCATION_LIST).getCount();

        }

        public Object getItem(int position) {
            return ListHolder.getListHolder().get(InfoList.LOCATION_LIST).get(position);

        }

        public long getItemId(int position) {
            return position;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EXPORT_CODE && resultCode == RESULT_OK && data != null) {
            exportData.process(data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
    }

    Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setContentView(R.layout.location_list);

            mAdapter = new LLadapter();
            setListAdapter(mAdapter);
            //	nightmode = Settings.getNightMode();
            getListView().setVerticalFadingEdgeEnabled(!nightMode);
            if (ALEX_MENU_FLAG) {
                initAlexMenu();
                initAlexContextMenu();
            } else registerForContextMenu(getListView());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exportData = new ExportData(this, EXPORT_CODE, "");
        initHandler.handleMessage(null);
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        Intent i = new Intent();
        i.putExtra(POS, position);
        this.setResult(RESULT_OK, i);
        finish();
    }

    private boolean parseContextMenu(int itemId, final int objIndex) {
        switch (itemId) {
            case CONTEXT_MENU_REMOVE:
                class DialogImpl implements InputDialog.OnButtonListener {
                    public void onClick(String v) {
                        InfoList loclist = ListHolder.getListHolder().get(InfoList.LOCATION_LIST);
                        loclist.remove(objIndex);
                        mAdapter.notifyDataSetChanged();
                    }
                }

                InputDialog dl2 = new InputDialog(LocationListActivity.this);
                dl2.setTitle(getString(R.string.warning));
                dl2.setMessage(getString(R.string.do_you_want_to_remove_this_item_));
                dl2.setPositiveButton(getString(R.string.ok), new DialogImpl());
                dl2.setNegativeButton(getString(R.string.cancel));
                registerDialog(dl2).show();
                return true;
        }
        return false;
    }

    public boolean parseMenu(int itemid) {
        switch (itemid) {
            case MENU_IMPORT:
                SelectFileActivity.setPath(SettingsActivity.getFileDialogPath(getApplicationContext()));
                SelectFileActivity.setListener(LocationListActivity.this);
                Intent fileDialog = new Intent(LocationListActivity.this, SelectFileActivity.class);
                startActivity(fileDialog);
                return true;
            case MENU_SHARE:
                if (SettingsActivity.nightGuard(this)) return true;

                ByteArrayOutputStream out = null;
                try {
                    out = new ByteArrayOutputStream();
                    InfoListSaver saver = new InfoListStringSaverImp(out, Global.SHARE_LINES_LIMIT, new Handler());
                    InfoList loclist = ListHolder.getListHolder().get(InfoList.LOCATION_LIST);
                    loclist.save(saver);
                } catch (Exception e) {
                }
                if (out != null) {
                    String s = out.toString();
                    new ShareCommand(this, s).execute();
                }
                return true;
            case MENU_PASTE:
                Runnable r1 = new Runnable() {
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                };
                Command command = new PasteCommand(ListHolder.getListHolder().get(InfoList.LOCATION_LIST), 0, r1, this, getString(R.string.do_you_want_to_paste_into_current_locations_list_));
                command.execute();

                return true;
            case MENU_EXPORT:

                final ExportData.Predicate<OutputStream> r = outstream -> {

                    InfoList infoList = ListHolder.getListHolder().get(InfoList.LOCATION_LIST);
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
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (ALEX_MENU_FLAG && keyCode == KeyEvent.KEYCODE_MENU) {
            aMenu.show(findViewById(R.id.location_num_obj));
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
        }, getLayoutInflater());
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

        menuItems.add(new alexMenuItem(MENU_IMPORT, getString(R.string.import2), dayMode ? R.drawable.am_save_v : R.drawable.ram_save_v, true));
        menuItems.add(new alexMenuItem(MENU_EXPORT, getString(R.string.export), dayMode ? R.drawable.am_load_v : R.drawable.ram_load_v, true));
        menuItems.add(new alexMenuItem(MENU_SHARE, getString(R.string.share), dayMode ? R.drawable.am_share_v : R.drawable.ram_share_v, true));
        menuItems.add(new alexMenuItem(MENU_PASTE, getString(R.string.paste), dayMode ? R.drawable.am_paste_v : R.drawable.ram_paste_v, true));

        if (aMenu.isNotShowing()) {
            try {
                aMenu.setMenuItems(menuItems);
            } catch (Exception e) {
                InputDialog.message(LocationListActivity.this, MENU_ERROR + e.getMessage(), 0).show();
            }
        }
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
        contextMenu.setSkin(nightMode, SettingsActivity.getDarkSkin());

        //mine
        float text_size = getResources().getDimension(R.dimen.table_main_text_size);//mine
        float density = getResources().getDisplayMetrics().density;
        text_size = text_size / density;
        contextMenu.setTextSize((int) text_size);//contextMenu.setTextSize(18)

        contextMenu.makeFloat();

        //load the menu items
        ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();

        menuItems.add(new alexMenuItem(CONTEXT_MENU_REMOVE, getString(R.string.remove), 0, true));

        if (contextMenu.isNotShowing()) {
            try {
                contextMenu.setMenuItems(menuItems);
            } catch (Exception e) {
                InputDialog.message(LocationListActivity.this, MENU_ERROR + e.getMessage(), 0).show();
            }
        }
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v, int index, long arg3) {


                contextMenu.setMenuItemId(index);

                LocationItem item = (LocationItem) mAdapter.getItem(index);
                contextMenu.setHeader(item.name);
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

    void handleErrorIfAny(ErrorHandler eh) {
        if (eh.hasError()) {
            eh.showError(LocationListActivity.this);
        }
    }

    public void callbackCall(Uri uri) {
        try {
            readStream(getContentResolver().openInputStream(uri), FileTools.getDisplayName(getApplicationContext(), uri));
        } catch (IOException e) {

        }
    }

    public void readStream(InputStream inputStream, String locationName) {
        Runnable r = () -> {
            InfoList loclist = ListHolder.getListHolder().get(InfoList.LOCATION_LIST);
            InfoListLoader loader = new InfoListStringLoaderImp(inputStream);
            handleErrorIfAny(loclist.load(loader));
            mAdapter.notifyDataSetChanged();
        };
        InputDialog dialog1 = AstroTools.getDialog(LocationListActivity.this, getString(R.string.do_you_really_want_to_import_current_locations_list_from_) + locationName + "?", r);
        registerDialog(dialog1).show();

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
