package com.astro.dsoplanner;

import static com.astro.dsoplanner.Global.ALEX_MENU_FLAG;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.InfoListImpl;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MiniLauncherActivity extends ParentListActivity implements OnGestureListener {

    private static final String MLANSWER = "mlanswer";
    private static final String BUNDLE = "bundle";

    private static final String COM_ASTRO_DSOPLANNER;
    private static final String COM_ASTRO_DSOPLANNER_FLASHLIGHT;
    private static final String COM_ASTRO_DSOPLANNER_TWILIGHT;
    private static final String COM_ASTRO_DSOPLANNER_NOTES;

    static {
        COM_ASTRO_DSOPLANNER = "com.astro.dsoplanner.";
        COM_ASTRO_DSOPLANNER_FLASHLIGHT = "com.astro.dsoplanner.FlashlightActivity";
        COM_ASTRO_DSOPLANNER_TWILIGHT = "com.astro.dsoplanner.TwilightActivity";
        COM_ASTRO_DSOPLANNER_NOTES = "com.astro.dsoplanner.NoteListActivity";
    }
    private static final String APPLICATIONS_LIST = "Applications list";
    private static final String TAG = MiniLauncherActivity.class.getSimpleName();
    public static final int INTERNAL_APP = -10;
    public static final int COLOR_MASK = 0xFF8F0000;
    private PackageManager m_pm;

    InfoList list = new InfoListImpl(APPLICATIONS_LIST, MiniLauncherRecord.class);

    //Add DSO Planner internal activity modules
    static class ToolsFiller implements InfoListFiller {
        List<MiniLauncherRecord> l = new ArrayList<MiniLauncherRecord>();

        public ToolsFiller(Context context) {
            l.add(new MiniLauncherRecord(INTERNAL_APP, context.getString(R.string.dso_twilight), COM_ASTRO_DSOPLANNER_TWILIGHT));
            l.add(new MiniLauncherRecord(INTERNAL_APP, context.getString(R.string.dso_flashlight), COM_ASTRO_DSOPLANNER_FLASHLIGHT));
            l.add(new MiniLauncherRecord(INTERNAL_APP, context.getString(R.string.dso_observation_notes), COM_ASTRO_DSOPLANNER_NOTES));
        }

        public Iterator getIterator() {
            return l.iterator();
        }
    }

    //List adapter filler
    private APSadapter mAdapter;

    private class APSadapter extends BaseAdapter {
        private LayoutInflater mInflater;
        public APSadapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.minilauncher_ap_item, null);
            }

            MiniLauncherRecord rec = (MiniLauncherRecord) list.get(position);

            boolean missing = false;

            //retrieve icon
            ImageView i = ((ImageView) convertView.findViewById(R.id.ml_iicon));
            Drawable icon = null;
            if (rec.id == INTERNAL_APP) {
                //temporary icon == DSO main app icon, later add icons for each tool in manifest
                icon = MiniLauncherActivity.this.getApplication().getApplicationInfo().loadIcon(m_pm);
            } else {
                try {
                    icon = m_pm.getApplicationIcon(rec.pkgname);
                } catch (NameNotFoundException e) { //get DSO icon instead
                    icon = MiniLauncherActivity.this.getApplication().getApplicationInfo().loadIcon(m_pm);
                    missing = true;
                }
            }
            //Color drawable if in night mode
            if (nightMode) {
                icon.setColorFilter(COLOR_MASK, PorterDuff.Mode.MULTIPLY);
            }
            i.setImageDrawable(icon);

            //Label
            ((TextView) convertView.findViewById(R.id.ml_label)).setText((missing ? getString(R.string._missing_) : "") + rec.label);

            //make dark background
            if (SettingsActivity.getDarkSkin() || nightMode) convertView.setBackgroundColor(0xff000000);

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_pm = getPackageManager();
        setContentView(R.layout.activity_mini_launcher_no_laucher);
        mAdapter = new APSadapter();
        setListAdapter(mAdapter);
    }

    //Start selected app
    public void onListItemClick(ListView parent, View v, int position, long id) {
        Intent i = null;
        MiniLauncherRecord rec = (MiniLauncherRecord) list.get(position);//ListHolder.getListHolder().get(InfoList.APP_LIST).get(position);
        Log.d(TAG, "rec=" + rec);
        Class<?> c = null;
        try {
            if (rec.pkgname.contains(COM_ASTRO_DSOPLANNER)) {
                c = Class.forName(rec.pkgname);
                i = new Intent(MiniLauncherActivity.this, c);
            } else //APP
                i = m_pm.getLaunchIntentForPackage(rec.pkgname);

            if (c == null && SettingsActivity.getNightMode()) { //real app in night mode
                final Intent ii = i;
                InputDialog d = new InputDialog(MiniLauncherActivity.this, getString(R.string.nightmode_warning), getResources().getString(R.string.minilaunchr_wantlaunch));
                d.setPositiveButton(getString(R.string.yes), new InputDialog.OnButtonListener() {
                    public void onClick(String value) {
                        try {
                            startActivity(ii);
                        } catch (Exception e) {

                            registerDialog(InputDialog.message(MiniLauncherActivity.this, R.string.sorry_can_t_start_this_application)).show();

                        }
                    }
                });
                d.setNegativeButton(getString(R.string.cancel));
                registerDialog(d).show();
            } else {
                if (rec.pkgname.contentEquals(COM_ASTRO_DSOPLANNER_NOTES)) {
                    i.putExtra(BUNDLE, new NoteRequest(null, NoteRequest.GET_ALL_NOTES).getBundle());
                }
                startActivity(i);
            }

        } catch (Exception e) {
            registerDialog(InputDialog.message(MiniLauncherActivity.this, R.string.sorry_can_t_start_this_application)).show();
        }
    }

    private void initList() {
        list.removeAll();
        list.fill(new ToolsFiller(getApplicationContext())); //tools
        int ln = list.getCount();
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(this);
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        initList();
        super.onResume();
        hideMenuBtn();
    }

    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
    }

    //overriding menu button
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (ALEX_MENU_FLAG && keyCode == KeyEvent.KEYCODE_MENU) {
            //aMenu.show(tv);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
