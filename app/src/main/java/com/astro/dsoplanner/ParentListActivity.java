package com.astro.dsoplanner;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ParentListActivity extends ListActivity {

    private static final String ANDROID_INTENT_ACTION_DOWNLOAD_COMPLETE = "android.intent.action.DOWNLOAD_COMPLETE";

    private static final String TAG = ParentListActivity.class.getSimpleName();
    protected boolean nightMode;
    protected boolean finishedInOnResume;
    private boolean orientationChanged = false;
    private int initloop = 2;
    private boolean initagain = false;
    private int nightModeId;
    private Handler handler = new Handler();
    private FloatingActionButton fabButtonBack;
    private FloatingActionButton fabButtonMenu;

    //for inheriting activites - first run super.onCreate() then everything else
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Global.setAppContext(getApplicationContext());
        nightMode = SettingsActivity.setDayNightList(this);
        super.onCreate(savedInstanceState);

        if (Init.initRequired()) {
            Init.runOnUi(this);
            new Init(this, null).run();
        }
        nightModeId = NightModeChangeTracker.register();
        if (SettingsActivity.isHideNavBarSupported()) {
            if (SettingsActivity.isHideNavBar(this)) {
                View decorView = getWindow().getDecorView();
                decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {

                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {

                            View decorView = getWindow().getDecorView();
                            decorView.setSystemUiVisibility(
                                    View.SYSTEM_UI_FLAG_IMMERSIVE

                                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
                        }
                    }
                });
            }

        }


    }

    protected void setupScreenSize() {
        View content = getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        Global.screenH = content.getHeight();
        Global.screenW = content.getWidth();
        Log.d(TAG, "WxH " + Global.screenW + "x" + Global.screenH);
    }

    //the method may call finish if night mode needs to be changed
    @Override
    protected void onResume() {
        super.onResume();
        finishedInOnResume = AstroTools.changeNightModeIfNeeded(this, nightMode, nightModeId);
        AstroTools.startAutoLocationIfNeeded(this);
        registerReceiver();

        boolean showButtonsInRedMode = SettingsActivity.isHideNavBar(getApplicationContext()) || (!finishedInOnResume && SettingsActivity.areButtonsInRedMode(getApplicationContext()));
        boolean showButtonsInDayMode = SettingsActivity.isHideNavBar(getApplicationContext()) || (!finishedInOnResume && SettingsActivity.areButtonsInDayMode(getApplicationContext()));

        if (nightMode && showButtonsInRedMode) {
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            int height = display.getHeight();


            if (fabButtonBack == null) {
                fabButtonBack = SettingsActivity.getNightModeBackButton(this, height);
            }
            if (fabButtonMenu == null) {
                fabButtonMenu = SettingsActivity.getNightModeMenuButton(this, height);

            }
        } else if (!nightMode && showButtonsInDayMode) {
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            int height = display.getHeight();

            //DSOmain treated separately
            if (fabButtonBack == null) {
                fabButtonBack = SettingsActivity.getDayModeBackButton(this, height, SettingsActivity.getDarkSkin());
            }
            if (fabButtonMenu == null) {
                fabButtonMenu = SettingsActivity.getDayModeMenuButton(this, height, SettingsActivity.getDarkSkin());

            }
        }

    }

    /**
     * to be called in on Resume as the btn as created in onResume
     */
    protected void hideMenuBtn() {
        if (fabButtonMenu != null)
            fabButtonMenu.hideFloatingActionButton();
    }

    /**
     * to be called in on Resume as the btn as created in onResume
     */
    protected void setMenuBtnOnClickListener(View.OnClickListener listener) {
        if (fabButtonMenu != null)
            fabButtonMenu.setOnClickListener(listener);
    }

    @Override
    protected void onPause() {

        super.onPause();
        unregisterReceiver();
        AutoLocation.stopAll();

    }

    @Override
    protected void onStart() {
        super.onStart();
        registerDownloadReceiver();
        registerBatteryMonitor();
        if (Global.TEST_MODE) {
            registerTestReceiver();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterDownloadReceiver();
        unregisterBatteryMonitor();
        if (Global.TEST_MODE) {
            unregisterTestReceiver();
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setupScreenSize();
    }

    private List<Dialog> listDialog = new ArrayList<Dialog>();

    protected Dialog registerDialog(Dialog dialog) {
        listDialog.add(dialog);
        return dialog;
    }

    @Override
    protected void onDestroy() {
        for (Dialog d : listDialog) {
            try {
                d.dismiss();
                Log.d(TAG, "dialog dismissed");
            } catch (Exception e) {
                Log.d(TAG, "exception=" + e);
            }
        }
        super.onDestroy();
        NightModeChangeTracker.unregister(nightModeId);
    }

    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Constants.IDIS_ERROR_BROADCAST.equals(action)) {
                String s = intent.getStringExtra(Constants.IDIS_ERROR_STRING);
                if (s != null) {
                    InputDialog.toast(s, ParentListActivity.this).show();
                    //toast used otherwise leaked windows when error in vda
                }
            }
            if (Constants.BTCOMM_MESSAGE_BROADCAST.equals(action)) {
                String s = intent.getStringExtra(Constants.BTCOMM_MESSAGE);
                if (s != null) {
                    InputDialog.toast(s, ParentListActivity.this).show();

                }
            }


        }
    };

    DownloadReceiver downloadReceiver = new DownloadReceiver(this);

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.IDIS_ERROR_BROADCAST);
        filter.addAction(Constants.BTCOMM_MESSAGE_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);


    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);

    }

    private void registerDownloadReceiver() {
        IntentFilter dfilter = new IntentFilter();
        dfilter.addAction(ANDROID_INTENT_ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, dfilter);
    }

    private void unregisterDownloadReceiver() {
        unregisterReceiver(downloadReceiver);
    }

    BroadcastReceiver mBatteryReceiver;

    private void registerBatteryMonitor() {
        mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int lev = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scal = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int level = lev * 100 / scal;
                SettingsActivity.putSharedPreferences(Constants.BATTERY_LEVEL, level, context);

            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryReceiver, filter);
    }

    private void unregisterBatteryMonitor() {
        if (mBatteryReceiver != null) unregisterReceiver(mBatteryReceiver);
    }

    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
    }

    protected void startTest(int action, int param) {
        this.getListView().setKeepScreenOn(true);
        if (action == TestIntentService.FINISH) {
            finish();
            return;
        }

    }

    protected int getTestActivityNumber() {
        return -1;
    }

    BroadcastReceiver testReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int activity = intent.getIntExtra(Constants.TEST_ACTIVITY, 0);
            if (activity != getTestActivityNumber())
                return;
            int action = intent.getIntExtra(Constants.TEST_ACTIVITY_ACTION, 0);
            int param = intent.getIntExtra(Constants.TEST_ACTIVITY_PARAM, 0);
            startTest(action, param);


        }
    };

    private void registerTestReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.TEST_BROADCAST);

        LocalBroadcastManager.getInstance(this).registerReceiver(testReceiver, filter);

    }

    private void unregisterTestReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(testReceiver);

    }

}
