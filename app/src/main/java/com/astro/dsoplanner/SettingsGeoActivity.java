package com.astro.dsoplanner;

import java.util.Arrays;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.astro.dsoplanner.database.Db;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListCollectionFiller;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.base.Point;

public class SettingsGeoActivity extends ParentPreferenceActivity implements OnSharedPreferenceChangeListener {
    private static final int SELECTOR_ACTIVITY = 5;
    private static final String LIST2 = "list2";
    private static final String CITY = "city";
    private static final String REGION = "region";
    private static final String COUNTRY = "country";
    public static final String SELECT_LAT_LON_FROM_LIST2_WHERE_ROWID = "select lat,lon from list2 where rowid=";
    private static final String LON2 = " lon=";
    private static final String LAT2 = "lat=";

    private static final String TAG = SettingsGeoActivity.class.getSimpleName();
    private static final int CODE = 1;
    private LocationManager locMgr = null;
    LocationListener locl;
    private String best = null;
    boolean dirty = false;//location list
    private HandlerThread workerThread;
    private Handler workerHandler;
    BroadcastReceiver geoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SettingsActivity.getAutoLoc()) {
                Log.d(TAG, "broadcast receiver");
                init();
            }
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
    public void onResume() {
        onResumeCode();
        super.onResume();
    }

    private void startAutoLocation() {
        Log.d(TAG, "startAutoLocation");
        AutoLocation.stopAll();
        AutoLocation.start(this);
    }

    private void stopAutoLocation() {
        AutoLocation.stopAll();
        //update with last known location of the best provider at the next start
        SettingsActivity.putSharedPreferences(Constants.GEO_LAST_KNOWN, true, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        workerThread.getLooper().quit();
    }

    private void onResumeCode() {
        registerReceiver();
        SettingsGeoActivity.this.getPreferenceScreen().getSharedPreferences().
                registerOnSharedPreferenceChangeListener(SettingsGeoActivity.this);
        init();
    }

    @Override
    public void onPause() {
        super.onPause();
        Point.setLat(SettingsActivity.getLattitude());
        if (dirty) {
            new Prefs(this).saveList(InfoList.LOCATION_LIST);
            dirty = false;
        }
        unregisterReceiver();
        SettingsGeoActivity.this.getPreferenceScreen().getSharedPreferences().
                unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == CODE && resultCode == RESULT_OK && data != null) {
            int pos = data.getIntExtra("pos", -1);
            if (pos >= 0) {
                try {//to avoid crashes when returning to killed activity
                    InfoList list = ListHolder.getListHolder().get(InfoList.LOCATION_LIST);

                    LocationItem item = (LocationItem) list.get(pos);
                    Log.d(TAG, "item=" + item);

                    SettingsActivity.putSharedPreferences(Constants.LONGITUDE, item.lon, SettingsGeoActivity.this);
                    SettingsActivity.putSharedPreferences(Constants.LATITUDE, item.lat, SettingsGeoActivity.this);

                    updateLatLon();
                    CheckBoxPreference ch = (CheckBoxPreference) getPreferenceScreen().findPreference(
                            SettingsActivity.OPT_auto_location);
                    if (ch != null)
                        ch.setChecked(false);
                } catch (Exception e) {

                }

            }
        } else if (requestCode == SELECTOR_ACTIVITY && resultCode == RESULT_OK) {
            int rowid = data.getIntExtra(Constants.SELECTOR_RESULT, -1);
            if (rowid != -1) {
                try {
                    Db db = new Db(getApplicationContext(), Constants.LOCATIONS_DB);
                    db.open();
                    Cursor cursor = db.rawQuery(SELECT_LAT_LON_FROM_LIST2_WHERE_ROWID + rowid);
                    if (cursor.moveToNext()) {

                        double lat = cursor.getFloat(0);
                        double lon = cursor.getFloat(1);
                        SettingsActivity.putSharedPreferences(Constants.LONGITUDE, lon, SettingsGeoActivity.this);
                        SettingsActivity.putSharedPreferences(Constants.LATITUDE, lat, SettingsGeoActivity.this);

                        updateLatLon();
                        //		DisableLocManager();
                        CheckBoxPreference ch = (CheckBoxPreference) getPreferenceScreen().findPreference(
                                SettingsActivity.OPT_auto_location);
                        if (ch != null)
                            ch.setChecked(false);
                    }
                    db.close();
                } catch (Exception e) {
                }
            }

        }
    }

    Handler initHandler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            addPreferencesFromResource(R.xml.settings_geo);

            Preference pref = findPreference(SettingsActivity.OPT_auto_location);
            if (!AstroTools.doesLocProviderExist(getApplicationContext())) {
                getPreferenceScreen().removePreference(pref);
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                        edit().putBoolean(SettingsActivity.OPT_auto_location, false).commit();
            }


            findPreference(getString(R.string.add_location)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    dirty = true;
                    InputDialog d = new InputDialog(SettingsGeoActivity.this);
                    d.setType(InputDialog.DType.INPUT_STRING);
                    d.setTitle(getString(R.string.location_name));
                    d.setMessage(getString(R.string.please_enter_a_distinct_name_for_the_location));
                    d.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
                        public void onClick(String value) {
                            InfoList list = ListHolder.getListHolder().get(InfoList.LOCATION_LIST);
                            LocationItem item = new LocationItem(InputDialog.getResult(), SettingsActivity.getLattitude(), SettingsActivity.getLongitude());
                            list.fill(new InfoListCollectionFiller(Arrays.asList(new LocationItem[]{item})));
                            new Prefs(SettingsGeoActivity.this).saveList(InfoList.LOCATION_LIST);
                            SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, getApplicationContext());

                            updateLatLon();
                        }
                    });
                    d.setNegativeButton(getString(R.string.cancel));
                    registerDialog(d).show();
                    return false;
                }
            });
            findPreference(SettingsActivity.OPT_lattitude).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    InputDialog d = new InputDialog(SettingsGeoActivity.this);
                    d.setType(InputDialog.DType.INPUT_STRING);
                    d.setTitle(getString(R.string.dso_planner_settings));
                    d.setMessage(getString(R.string.set_latitude_n_s_deg_min_sec_));
                    d.setHelp(R.string.examples_n45_34_21_n45_34_s23_13_s23);
                    d.setValue(SettingsGeoActivity.getLatStringSec(SettingsActivity.getLattitude()));
                    d.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
                        public void onClick(String value) {
                            Double lat = AstroTools.convertLattitude(value);
                            if (lat == null) return;
                            SettingsActivity.putSharedPreferences(Constants.LATITUDE, lat, SettingsGeoActivity.this);
                            SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, getApplicationContext());
                            updateLatLon();
                        }
                    });
                    d.setNegativeButton(getString(R.string.cancel));
                    registerDialog(d).show();
                    return false;
                }
            });
            findPreference(SettingsActivity.OPT_longitude).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    InputDialog d = new InputDialog(SettingsGeoActivity.this);
                    d.setType(InputDialog.DType.INPUT_STRING);
                    d.setTitle(getString(R.string.dso_planner_settings));
                    d.setMessage(getString(R.string.set_longitude_e_w_deg_min_sec_));
                    d.setValue(SettingsGeoActivity.getLonStringSec(SettingsActivity.getLongitude()));
                    d.setHelp(R.string.examples_e45_34_21_e45_34_w23_13_w23);
                    d.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
                        public void onClick(String value) {
                            Double lon = AstroTools.convertLongitude(value);
                            if (lon == null) return;
                            SettingsActivity.putSharedPreferences(Constants.LONGITUDE, lon, SettingsGeoActivity.this);
                            updateLatLon();
                        }
                    });
                    d.setNegativeButton(getString(R.string.cancel));
                    registerDialog(d).show();
                    return false;
                }
            });


            findPreference(getString(R.string.select_location)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    dirty = true;
                    Intent i = new Intent(SettingsGeoActivity.this, LocationListActivity.class);
                    startActivityForResult(i, CODE);
                    return false;
                }
            });

            findPreference(getString(R.string.select_location_cities)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {

                    Intent intent = new Intent(SettingsGeoActivity.this, SelectorActivity.class);
					setWorldCitiesParam(intent, getApplicationContext());
                    startActivityForResult(intent, SELECTOR_ACTIVITY);
                    return false;
                }
            });
            workerThread = new HandlerThread("");
            workerThread.start();
            workerHandler = new Handler(workerThread.getLooper());
        }


    };

    /**
     * Setting intent params for launching SelectorActivity with world cities
     *
     * @param intent
     */
    public static void setWorldCitiesParam(Intent intent, Context context) {
        intent.putExtra(Constants.SELECTOR_DBNAME, Constants.LOCATIONS_DB);
        intent.putExtra(Constants.SELECTOR_FIELDS, new String[]{COUNTRY, REGION});
        intent.putExtra(Constants.SELECTOR_FNAME, CITY);
        intent.putExtra(Constants.SELECTOR_FNAMES, new String[]{context.getString(R.string.country), context.getString(R.string.region)});
        intent.putExtra(Constants.SELECTOR_TABLE, LIST2);
        intent.putExtra(Constants.SELECTOR_ACTIVITY_NAME, context.getString(R.string.select_location));
        intent.putExtra(Constants.SELECTOR_CALLING_ACTIVITY, SelectorActivity.GEO);
        intent.putExtra(Constants.SELECTOR_SEARCH_BUTTON, true);
        intent.putExtra(Constants.SELECTOR_SEARCH_TITLE, context.getString(R.string.set_the_string_that_city_starts_with));

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHandler.handleMessage(null);
        Log.d(TAG, "onCreate " + this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsActivity.OPT_auto_location)) {
            if (sharedPreferences.getBoolean(SettingsActivity.OPT_auto_location, false)) {//
                Log.d(TAG, "onSharedPreferenceChanged");
                init();
                startAutoLocation();

            } else {
                init();
                stopAutoLocation();

            }
        } else if (key.equals(R.string.d_lattitude)) {
            updateLatLon();
        } else if (key.equals(R.string.d_longitude)) {
            updateLatLon();
        }
    }

    private void init() {
        Log.d(TAG, "init");
        if (!SettingsActivity.getAutoLoc()) {
            PreferenceScreen ps = getPreferenceScreen();
            Log.d(TAG, "ps=" + ps);
            Preference et1 = ps.findPreference(
                    SettingsActivity.OPT_lattitude);
            Preference et2 = getPreferenceScreen().findPreference(
                    SettingsActivity.OPT_longitude);

            et1.setEnabled(true);
            et2.setEnabled(true);

            CheckBoxPreference ch = (CheckBoxPreference) getPreferenceScreen().findPreference(
                    SettingsActivity.OPT_auto_location);
            if (ch != null)
                ch.setSummary("");
            updateLatLon();
        } else {
            Preference et1 = getPreferenceScreen().findPreference(
                    SettingsActivity.OPT_lattitude);
            Preference et2 = getPreferenceScreen().findPreference(
                    SettingsActivity.OPT_longitude);
            double lat = 0;
            double lon = 0;

            lat = SettingsActivity.getLattitude();
            lon = SettingsActivity.getLongitude();

            updateLatLon();

            et1.setEnabled(false);
            et2.setEnabled(false);

            CheckBoxPreference ch = (CheckBoxPreference) getPreferenceScreen().findPreference(
                    SettingsActivity.OPT_auto_location);


            String locString = LAT2 + AstroTools.getLatString(lat) + LON2 + AstroTools.getLonString(lon);

            String prov = SettingsActivity.getStringFromSharedPreferences(this, Constants.GEO_PROVIDER, "");

            String provider = prov;
            boolean lastKnown = SettingsActivity.getSharedPreferences(this).getBoolean(Constants.GEO_LAST_KNOWN, true);
            String p = getString(R.string.provider_) + provider;
            String lk = (lastKnown ? getString(R.string._last_known_location_) : getString(R.string._current_update_));

            if (ch != null)
                ch.setSummary(locString + "\n" + p + lk + "\n" + AstroTools.getLastUpdateDateTimeString(this));

        }
    }


    private void updateLatLon() {
        Preference ep = getPreferenceScreen().findPreference(getString(R.string.d_lattitude));
        double lat = SettingsActivity.getLattitude();
        ep.setTitle(AstroTools.getLatString(lat));

        Preference ep2 = getPreferenceScreen().findPreference(getString(R.string.d_longitude));
        double lon = SettingsActivity.getLongitude();
        ep2.setTitle(AstroTools.getLonString(lon));
        ep.setSummary("");
        ep2.setSummary("");
        Log.d(TAG, "update latlon lat=" + lat + " lon=" + lon);
        workerHandler.post(new LocationTask(lat, lon));


    }


    Handler lhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String locname = (String) msg.obj;
            if (locname == null)
                locname = "";
            Preference ep = getPreferenceScreen().findPreference(getString(R.string.d_lattitude));
            Preference ep2 = getPreferenceScreen().findPreference(getString(R.string.d_longitude));

            ep.setSummary(locname);
            ep2.setSummary(locname);
        }
    };

    class LocationTask implements Runnable {

        double lat;
        double lon;


        public LocationTask(double lat, double lon) {
            super();
            this.lat = lat;
            this.lon = lon;
        }

        public void run() {
            String name = AstroTools.getLocationName(lat, lon, getApplicationContext());
            Message msg = Message.obtain();
            msg.obj = name;
            lhandler.sendMessage(msg);
        }

    }


    /**
     * lat lon with spaces and seconds
     *
     * @param lat
     * @return
     */
    public static String getLatStringSec(double lat) {

        String s = AstroTools.getGradString(Math.abs(lat));
        if (lat < 0) {

            s = "S" + s;
        } else {
            s = "N" + s;
        }
        return s;
    }

    public static String getLonStringSec(double lon) {
        String s = AstroTools.getGradString(Math.abs(lon));
        if (lon < 0) {

            s = "W" + s;
        } else {
            s = "E" + s;
        }
        return s;
    }

}
