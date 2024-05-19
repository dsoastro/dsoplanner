package com.astro.dsoplanner;

import static com.astro.dsoplanner.Global.ALEX_MENU_FLAG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.astro.dsoplanner.alexmenu.alexMenu;
import com.astro.dsoplanner.alexmenu.alexMenu.OnMenuItemSelectedListener;
import com.astro.dsoplanner.alexmenu.alexMenuItem;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListCollectionFiller;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.InfoListImpl;

import java.io.File;


public class DownloadListActivity extends ParentListActivity {
    private static final String BYTES = " bytes";
    private static final String G_B = " gB";
    private static final String M_B = " mB";
    private static final String K_B = " kB";
    private static final String MENU_ERROR = "Menu error! ";

    private DLadapter mAdapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DISMISS_DIALOG) {
                if (pd != null) pd.dismiss();
            }
        }
    };
    private alexMenu contextMenu;
    private InputDialog pd;
    final static int CONTEXT_MENU_DOWNLOAD = 1;

    final static int DISMISS_DIALOG = 2;
    private static final String TAG = DownloadListActivity.class.getSimpleName();
    private InfoList infolist;


    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(getString(R.string.android_intent_action_download_complete));
        registerReceiver(downloadReceiver, filter);
    }

    private void unregisterReceiver() {
        unregisterReceiver(downloadReceiver);
    }


    @Override
    protected void onResume() {
        registerReceiver();
        super.onResume();
        hideMenuBtn();
    }

    @Override
    protected void onPause() {
        unregisterReceiver();
        super.onPause();
    }

    BroadcastReceiver downloadReceiver = new ListDownloadReceiver();

    public class ListDownloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAdapter != null) mAdapter.notifyDataSetChanged();
        }
    }


    private String convertSize(long size) {
        if (size < 1024) return size + BYTES;
        if (size < 1024 * 1024) return String.format(Locale.US, "%.1f", (float) size / 1024) + K_B;
        if (size < 1024L * 1024L * 1024L)
            return String.format(Locale.US, "%.1f", (float) size / (1024 * 1024)) + M_B;
        if (size < 1024L * 1024L * 1024L * 1024L)
            return String.format(Locale.US, "%.1f", (float) size / (1024 * 1024 * 1024)) + G_B;
        return "";
    }


    private class DLadapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public DLadapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.download_item, null);
            }
            DownloadItem item = (DownloadItem) infolist.get(position);
            ((TextView) convertView.findViewById(R.id.obslistitem_dso)).setText(item.title);

            ((TextView) convertView.findViewById(R.id.obslistitem_info)).setText(item.des + "\n" + getString(R.string.size2) + "=" + convertSize(item.size));
            ChB ch = (ChB) convertView.findViewById(R.id.obslistitem_ch);
            ch.setChecked(item.exists());
            ch.setEnabled(false);

            //make dark background
            if (SettingsActivity.getDarkSkin() || SettingsActivity.getNightMode())
                convertView.setBackgroundColor(0xff000000);

            return convertView;
        }

        public int getCount() {
            return infolist.getCount();

        }

        public Object getItem(int position) {
            return infolist.get(position);

        }

        public long getItemId(int position) {
            return position;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
    }

    private Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setContentView(R.layout.location_list);

            mAdapter = new DLadapter();
            setListAdapter(mAdapter);
            getListView().setVerticalFadingEdgeEnabled(!nightMode);
            if (ALEX_MENU_FLAG) {
                initAlexContextMenu();
            } else
                registerForContextMenu(getListView());
        }
    };
    boolean initRequired = false;//global init

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        infolist = new InfoListImpl(getString(R.string.download), DownloadItem.class);
        if (Global.GITHUB_DOWNLOAD)
            fillDownloadListGithub(infolist);
        else
            fillDownloadList(infolist);

        initHandler.handleMessage(null);
    }

    private void fillDownloadList(InfoList list) {
        Resources res = this.getResources();
        DownloadItem exppack = new DownloadExpPackItem(this, handler);
        DownloadItem exppatch = new DownloadExpPatchItem(this, handler);
        InfoListFiller filler = new InfoListCollectionFiller(Arrays.asList(new DownloadItem[]{exppack, exppatch}));
        list.fill(filler);
    }

    private void fillDownloadListGithub(InfoList list) {
        Resources res = this.getResources();
        DownloadItem exppack = new DownloadExpPackItemGithub(this, handler);
        DownloadItem exppatch = new DownloadExpPatchItemGithub(this, handler);
        InfoListFiller filler = new InfoListCollectionFiller(Arrays.asList(new DownloadItem[]{exppack, exppatch}));
        list.fill(filler);
    }


    private boolean parseContextMenu(int itemId, final int objIndex) {
        switch (itemId) {
            case CONTEXT_MENU_DOWNLOAD:
                downloadFile(objIndex);
                return true;
        }
        return false;
    }

    private void downloadFile(int pos) {
        final DownloadItem item = (DownloadItem) infolist.get(pos);
        File path = new File(item.path).getParentFile();
        boolean isFreeSpaceAvailable = SettingsActivity.isFreeSpaceAvailable(this, path, item.spaceRequired());
        if (!isFreeSpaceAvailable) {
            registerDialog(InputDialog.message(this, R.string.no_space_available_, 0)).show();
            return;
        }

        Runnable r = new Runnable() {
            public void run() {
                pd = InputDialog.pleaseWaitBackEnabled(DownloadListActivity.this, getString(R.string.download_in_progress_please_wait)).pop();
                registerDialog(pd);
                item.start();
            }
        };

        if (item.exists()) {
            Dialog d = AstroTools.getDialog(DownloadListActivity.this, getString(R.string.file_exists_overwrite_), r);
            registerDialog(d).show();
        } else {
            Dialog d = AstroTools.getDialog(DownloadListActivity.this, getString(R.string.download_is_about_to_start), r);
            registerDialog(d).show();
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

        float text_size = getResources().getDimension(R.dimen.table_main_text_size);
        float density = getResources().getDisplayMetrics().density;
        text_size = text_size / density;
        contextMenu.setTextSize((int) text_size);

        contextMenu.makeFloat();

        //load the menu items
        ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();

        menuItems.add(new alexMenuItem(CONTEXT_MENU_DOWNLOAD, getString(R.string.download), 0, true));

        if (contextMenu.isNotShowing()) {
            try {
                contextMenu.setMenuItems(menuItems);
            } catch (Exception e) {
                InputDialog.message(DownloadListActivity.this, MENU_ERROR + e.getMessage(), 0).show();
            }
        }
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View v, int index, long arg3) {
                contextMenu.setMenuItemId(index);
                DownloadItem item = (DownloadItem) mAdapter.getItem(index);
                contextMenu.setHeader(item.name);
                contextMenu.show(v);
                return true;
            }

        });
    }

    protected void onDestroy() {
        try {

            contextMenu.hide();
        } catch (Exception e) {
        }
        super.onDestroy();
    }
}
