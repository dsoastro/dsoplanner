package com.astro.dsoplanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.BaseAdapter;

import java.lang.Exception;

public class SettingsSystemActivity extends ParentPreferenceActivity
        implements OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener, OnGestureListener {

    private static final String LON2 = " lon=";
    private static final String LAT2 = "lat=";
    private static final String COLOR = "color_";
    private static final String SEND_MAIL = "Send mail...";
    private static final String PLAIN_TEXT = "plain/text";
    private static final String NONCETIME2 = "|noncetime=";
    private static final String ANDROID_APPWIDGET_ACTION_APPWIDGET_CONFIGURE = "android.appwidget.action.APPWIDGET_CONFIGURE";
    private static final String TAG = SettingsSystemActivity.class.getSimpleName();
    public static boolean dsoMainRedrawRequired = false;
    Handler handler = new Handler();
    private String initialGeoSummary;
    private static final int RESTART_REQUIRED = 1;
    private boolean mNightGuard = false;
    private boolean mNightGuard2 = false;
    private boolean mNightGuard3 = false;
    private static final int requestCodeMigration = 1;

    BroadcastReceiver geoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGeolocationSummary();
        }
    };

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(
                Constants.GEO_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(geoReceiver, filter);
    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(geoReceiver);
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SettingsActivity.isHideNavBarSupported())
            addPreferencesFromResource(R.xml.settings_system);
        else
            addPreferencesFromResource(R.xml.settings_system_old_android);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setupSpecialPrefClickListener();

        findPreference(getString(R.string.exp_pack_reinstall)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                expPackReinstall();
                return false;
            }
        });


        findPreference(getString(R.string.ss_downloads)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(SettingsSystemActivity.this, DownloadListActivity.class));
                return false;
            }
        });
        findPreference(getString(R.string.ss_odatabases)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(SettingsSystemActivity.this, DatabaseManagerActivity.class));
                return false;
            }
        });

        findPreference(getString(R.string.star_chart_settings)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(SettingsSystemActivity.this, SettingsGraphActivity.class));
                return false;
            }
        });

        Preference pref = findPreference(getString(R.string.help_localise));

        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                if (SettingsActivity.getNightMode()) {
                    if (!mNightGuard2) { //first time here
                        mNightGuard2 = true;
                        InputDialog.message(SettingsSystemActivity.this, R.string.nightmode_warning_click_again, 0).show();
                        return true; //prevent going through
                    }
                }


                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType(getString(R.string.plain_text));
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.dsoplanner_gmail_com)});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.localization));

                try {//if there is no one to handle the intent
                    startActivity(Intent.createChooser(emailIntent, getString(R.string.localization)));
                } catch (Exception e) {
                }

                return false;
            }
        });

        findPreference(getString(R.string.ss_about)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(SettingsSystemActivity.this, AboutActivity.class));
                return false;
            }
        });
        findPreference(getString(R.string.ss_whatsnew)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                Intent intent = new Intent(SettingsSystemActivity.this, ScrollableTextActivity.class);
                intent.putExtra(ScrollableTextActivity.ARGUMENT, ScrollableTextActivity.WHATS_NEW);
                startActivity(intent);
                return false;
            }
        });
        findPreference(getString(R.string.ss_backup_restore)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsSystemActivity.this, SettingsInclActivity.class);
                intent.putExtra(Constants.XML_NUM, R.xml.settings_system_backup_incl);
                startActivity(intent);
                return false;
            }
        });
        findPreference(getString(R.string.rate_the_app)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (SettingsActivity.getNightMode()) {
                    if (!mNightGuard) { //first time here
                        mNightGuard = true;
                        InputDialog.message(SettingsSystemActivity.this, R.string.nightmode_warning_click_again, 0).show();
                        return true; //prevent going through
                    }
                }


                rateTheApp();
                return false;
            }
        });

        findPreference(getString(R.string.privacy_policy_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (SettingsActivity.getNightMode()) {
                    if (!mNightGuard3) { //first time here
                        mNightGuard3 = true;
                        InputDialog.message(SettingsSystemActivity.this, R.string.nightmode_warning_click_again, 0).show();
                        return true; //prevent going through
                    }
                }


                showPrivacyPolicy();
                return false;
            }
        });

        findPreference(getString(R.string.qs_dialog)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                Intent intent = new Intent(SettingsSystemActivity.this, ScrollableTextActivity.class);
                intent.putExtra(ScrollableTextActivity.ARGUMENT, ScrollableTextActivity.QUICK_START_GUIDE);
                startActivity(intent);
                //registerDialog(AstroTools.getQuickStartDialog(SettingsSystem1243.this,Settings1243.getNightMode())).show();
                return false;
            }
        });

        findPreference(getString(R.string.geolocation2)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsSystemActivity.this, SettingsGeoActivity.class);
                intent.setAction(ANDROID_APPWIDGET_ACTION_APPWIDGET_CONFIGURE);
                SettingsSystemActivity.this.startActivity(intent);

                return true;
            }
        });

        findPreference(getString(R.string.ss_screen)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsSystemActivity.this, SettingsInclActivity.class);
                intent.putExtra(Constants.XML_NUM, R.xml.settings_system_screen_incl);
                startActivityForResult(intent, RESTART_REQUIRED);
                return false;
            }
        });

        findPreference(getString(R.string.astroeq)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsSystemActivity.this, SettingsInclActivity.class);
                intent.putExtra(Constants.XML_NUM, R.xml.settings_astroeq_incl);
                startActivity(intent);
                return false;
            }
        });


        findPreference(getString(R.string.ss_wide_settings)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsSystemActivity.this, SettingsInclActivity.class);
                intent.putExtra(Constants.XML_NUM, R.xml.settings_system_wide_incl);
                startActivityForResult(intent, RESTART_REQUIRED);
                return false;
            }
        });
        initialGeoSummary = updateGeolocationSummary();
    }

    private void rateTheApp() {
        final Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

        if (getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0) {
            startActivity(rateAppIntent);
        }

    }

    private void showPrivacyPolicy() {
        final Uri uri = Uri.parse(getString(R.string.http_www_dsoplanner_com_privacy_policy));
        final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);

        try {
            startActivity(rateAppIntent);
        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        Log.d(TAG, "request code=" + requestCode + " resultCode=" + resultCode);
        if (requestCode == RESTART_REQUIRED) {
            if (resultCode == RESULT_OK) {
                restart();
            }
        }
    }

    private InputDialog pd;

    //traverse the preferences list and set listener by pattern in the name
    private void setupSpecialPrefClickListener() {
        //not working anymore! Don't know why
        String[] keys = {getString(R.string.color_object), getString(R.string.color_userobject), getString(R.string.color_crossmarker), getString(R.string.color_eyepieces), getString(R.string.color_ep_cross),
                getString(R.string.color_telrad), getString(R.string.color_dss_image), getString(R.string.color_constellations), getString(R.string.color_horizon),
                getString(R.string.color_user_horizon), getString(R.string.color_labels), getString(R.string.color_grid),
                SettingsActivity.OPT_NIGHT_MODE, SettingsActivity.OPT_ONYX_SKIN, getString(R.string.autonightmode), getString(R.string.hide_nav_bar)};
        for (String key : keys) {
            Preference p = findPreference(key);
            if (p != null) p.setOnPreferenceClickListener(this);
        }
    }

    /**
     * Clears sh prefs indicating that expansion pack was installed
     *
     * @param context
     */
    public static void clearExpPackShPrefs(Context context) {
        SettingsActivity.putSharedPreferences(Constants.EP_EXPANDED, false, context);
        SettingsActivity.putSharedPreferences(Constants.EP_PATCH_EXPANDED, false, context);
        SettingsActivity.putSharedPreferences(Constants.EP_BEING_EXPANDED, Constants.EP_BEING_EXPANDED_DEF_VALUE, context);
        SettingsActivity.putSharedPreferences(Constants.EP_PATCH_BEING_EXPANDED, Constants.EP_PATCH_BEING_EXPANDED_DEF_VALUE, context);
        SettingsActivity.putSharedPreferences(Constants.MP_UPDATED, false, context);

    }

    private void expPackReinstall() {
        Runnable r = new Runnable() {
            public void run() {
                clearExpPackShPrefs(SettingsSystemActivity.this);
                SettingsActivity.setResetNgcicCometMpFlag(getApplicationContext(), true);
                registerDialog(InputDialog.message(SettingsSystemActivity.this, R.string.exp_pack_app_restart, 0)).show();
            }
        };
        registerDialog(AstroTools.getDialog(this, getString(R.string.do_you_wish_to_reinstall_default_primary_data_files_), r)).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        String currentGeoSummary = getPreferenceScreen().findPreference(getString(R.string.geolocation2)).getSummary().toString();
        if (currentGeoSummary != null) {
            if (!currentGeoSummary.equals(initialGeoSummary))
                SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, this);
        } else
            SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, this);
        unregisterReceiver();
        Log.d(TAG, "onPause");
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.dimlight))
                || key.equals(SettingsActivity.OPT_NIGHT_MODE)
                || key.equals(SettingsActivity.OPT_ONYX_SKIN)
                || key.equals(getString(R.string.skinfront))
                || key.equals(getString(R.string.d_lightsensorlimit))//no ref in xml???
                || key.equals(getString(R.string.autonightmode))
                || key.equals(getString(R.string.hide_nav_bar))
        )
            dsoMainRedrawRequired = true; //to redraw main screen with new skin
        if (key.equals(getString(R.string.nicestars)))//no ref in xml???
            SettingsActivity.setStarMode();
        if (key.equals(getString(R.string.d_batterywarning)))
            SettingsActivity.setBatteryLow();
        if (key.equals(SettingsActivity.FLICK_BRIGHT)) {
            dsoMainRedrawRequired = true;
            if (!SettingsActivity.isFlickBrightnessEnabled()) {
                SettingsActivity.updateBrightness(SettingsActivity.NORM_BRIGHTNESS);

            }
        }

    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.

                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                //	| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        );
    }

    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        final SharedPreferences prefs = preference.getSharedPreferences();
        Log.d(TAG, "key=" + key);
        if (key.startsWith(COLOR)) {
            if (SettingsActivity.getInverseSky()) key = "i" + key;
            int color0 = prefs.getInt(key, 0xffffffff);
            final ColorPickerDialog d = new ColorPickerDialog(this, color0, key.substring(key.indexOf('_') + 1));
            d.setAlphaSliderVisible(true);
            d.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt((SettingsActivity.getInverseSky() ? "i" : "") + COLOR + d.getName(), d.getColor());
                    editor.commit();
                }
            });

            d.setButton2(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                //set the app defaults
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            registerDialog(d).show();

            return true;
        } else if (key.equals(SettingsActivity.OPT_NIGHT_MODE)) {
            if (SettingsActivity.isAutoNightModeOn()) {
                boolean nightmode = SettingsActivity.getNightMode();
                boolean nmrequired = AstroTools.isNightModeRequired();
                if (nightmode != nmrequired) {
                    SettingsActivity.forceNightMode(!nightmode);//else wrong toast color, after restart the correct noght mode will be set anyway
                    InputDialog.toast(getString(R.string.automatic_nighmode_is_on_), this).show();
                }
            }
            restart();
        } else if (key.equals(getString(R.string.hide_nav_bar))) {
            restart();
        } else if (key.equals(getString(R.string.autonightmode))) {
            if (SettingsActivity.isAutoNightModeOn()) {
                boolean nightmode = SettingsActivity.getNightMode();
                boolean nmrequired = AstroTools.isNightModeRequired();
                if (nightmode != nmrequired) {
                    restart();
                }
            }
        } else if (key.equals(SettingsActivity.OPT_ONYX_SKIN))
            restart();
        else if (key.equals(getString(R.string.default_colors)))
            SettingsActivity.setDefaultColors(true, getApplicationContext());

        return false;
    }

    private String updateGeolocationSummary() {
        PreferenceScreen p = getPreferenceScreen();
        double lat = SettingsActivity.getLattitude();
        double lon = SettingsActivity.getLongitude();
        boolean checked = SettingsActivity.getAutoLoc();//p.getSharedPreferences().getBoolean(Settings.OPT_auto_location,false);
        String locString = LAT2 + AstroTools.getLatString(lat) + LON2 + AstroTools.getLonString(lon);
        String summary = (checked ? getString(R.string._auto_) : "") + locString;
        if (checked) {
            String provider = SettingsActivity.getStringFromSharedPreferences(this, Constants.GEO_PROVIDER, "");//Settings1243.getSharedPreferences(this).getString(Constants.GEO_PROVIDER, "");
            boolean lastKnown = SettingsActivity.getSharedPreferences(this).getBoolean(Constants.GEO_LAST_KNOWN, true);
            provider = getString(R.string.provider_) + provider;
            String lk = (lastKnown ? getString(R.string._last_known_location_) : getString(R.string._current_update_));
            summary = summary + "\n" + provider + lk + "\n" + AstroTools.getLastUpdateDateTimeString(this);
        }
        p.findPreference(getString(R.string.geolocation2)).setSummary(summary);

        ((BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
        return summary;
    }

    @Override
    public void onResume() {
        updateGeolocationSummary();
        registerReceiver();
        super.onResume();
    }

    //Restart current activity (universal)
    void restart() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected int getTestActivityNumber() {
        return TestIntentService.SETTINGS_SYSTEM;
    }


    @Override
    protected void startTest(int action, int param) {
        super.startTest(action, param);
        if (action == TestIntentService.SS_OBJECTS_DB) {
            startActivity(new Intent(SettingsSystemActivity.this, DatabaseManagerActivity.class));

        }
    }

    GestureDetector gDetector = new GestureDetector(this);

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        Log.d(TAG, "touch");
        return gDetector.onTouchEvent(me);
    }

    public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
        Log.d(TAG, "swipe");
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
        Log.d(TAG, "scroll");
        return true;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }
}
