package com.astro.dsoplanner;

import java.util.Calendar;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.BaseAdapter;

public class SettingsSearchActivity extends ParentPreferenceActivity implements OnSharedPreferenceChangeListener {


    public static final String DBNAME = "dbname";
    public static final String CATPOS2 = "catpos";
    private static final String LON2 = " lon=";
    private static final String LAT2 = "lat=";
    private static final String ANDROID_APPWIDGET_ACTION_APPWIDGET_CONFIGURE = "android.appwidget.action.APPWIDGET_CONFIGURE";
    public static final String QUERYCAT2 = "querycat";
    private static final String TAG = SettingsSearchActivity.class.getSimpleName();

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
    public void onResume() {
        onResumeCode();
        super.onResume();
    }

    private void onResumeCode() {
        updateAdvSearchSummary();
        updateBasicSearchSummary();
        updateTelescopeSummary();
        updateGeolocationSummary();

        registerReceiver();
        updateCatalogSummary();
        updateObjTypesSummary();
        updateEmptyFieldTreatment();
        SettingsSearchActivity.this.getPreferenceScreen().getSharedPreferences().
                registerOnSharedPreferenceChangeListener(SettingsSearchActivity.this);
        updateObsTimeSummary();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver();
        SettingsSearchActivity.this.getPreferenceScreen().getSharedPreferences().
                unregisterOnSharedPreferenceChangeListener(SettingsSearchActivity.this);
    }

    Handler initHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            addPreferencesFromResource(R.xml.settings_search);

            //Observation Time Range
            PreferenceScreen ps = (PreferenceScreen) getPreferenceScreen().findPreference(getString(R.string.launch_screen));
            Intent i = new Intent(SettingsSearchActivity.this, SetDateTimeActivity.class);
            ps.setIntent(i);

            findPreference(getString(R.string.obj_catalog_pref)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(SettingsSearchActivity.this, SettingsInclActivity.class);
                    i.putExtra(Constants.XML_NUM, R.xml.settings_select_catalogs_incl);
                    startActivity(i);

                    return true;
                }
            });
            findPreference(getString(R.string.obj_types_pref)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(SettingsSearchActivity.this, SettingsInclActivity.class);
                    i.putExtra(Constants.XML_NUM, R.xml.settings_basic_search_obj_types_incl);
                    startActivity(i);

                    return true;
                }
            });
			
            findPreference(getString(R.string.basic_search)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference arg0) {
                    Intent i = new Intent(SettingsSearchActivity.this, SettingsInclActivity.class);
                    i.putExtra(Constants.XML_NUM, R.xml.settings_basic_search_vis_incl);
                    startActivity(i);
                    return false;
                }
            });

            findPreference(getString(R.string.advanced_search)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference arg0) {
                    startActivity(new Intent(SettingsSearchActivity.this, SearchRequestActivity.class));
                    return false;
                }
            });

            findPreference(getString(R.string.geolocation)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsSearchActivity.this, SettingsGeoActivity.class);
                    intent.setAction(ANDROID_APPWIDGET_ACTION_APPWIDGET_CONFIGURE);
                    SettingsSearchActivity.this.startActivity(intent);

                    return true;
                }
            });

            findPreference(getString(R.string.telescope_search)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsSearchActivity.this, TelescopeListActivity.class);
                    startActivity(intent);

                    return false;
                }
            });
            updateTelescopeSummary();
            updateLimitMagSummary();
            updateSearch();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHandler.handleMessage(null);
    }

    private void updateObsTimeSummary() {
        Preference p = findPreference(getString(R.string.launch_screen));
        long start = SettingsActivity.getSharedPreferences(this).getLong(Constants.START_OBSERVATION_TIME, 0);
        long end = SettingsActivity.getSharedPreferences(this).getLong(Constants.END_OBSERVATION_TIME, 0);
        if (start != 0 && end != 0) {
            String init = getString(R.string.time_range_within_24_hours);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(start);
            String start_string = c.getTime().toString();
            c.setTimeInMillis(end);
            String end_string = c.getTime().toString();
            p.setSummary(init + "\n" + getString(R.string.start_time_) + start_string + "\n" + getString(R.string.end_time_) + end_string);

        }
    }

    private void updateCatalogSummary() {
        Preference p = findPreference(getString(R.string.obj_catalog_pref));
        p.setSummary(SettingsActivity.getSelectedCatalogsSummary(this, SettingsActivity.DSO_SELECTION));
    }

    private void updateEmptyFieldTreatment() {
        Preference p = findPreference(getString(R.string.absent_data));
        String s = getString(R.string.treatment_of_objects_that_have_empty_fields_e_g_magnitude_);
        int value = SettingsActivity.getEmptyRule();//Settings1243.getInt(valuestr, 0, 0, 3);
        String[] a = getResources().getStringArray(R.array.entries_absent_data);
        s = s + "\n" + a[value];
        p.setSummary(s);
    }

    private void updateObjTypesSummary() {
        Preference p = findPreference(getString(R.string.obj_types_pref));
        p.setSummary(SettingsActivity.getObjTypesSummary(this, SettingsActivity.DSO_SELECTION));
    }

    private void updateAdvSearchSummary() {
        SearchRequestItem item = SettingsActivity.getSearchRequestItem();
        Preference advPref = findPreference(getString(R.string.advanced_search));
        if (item == null) {
            advPref.setSummary(getString(R.string.no_expression_selected));
            return;
        }

        String summary = item.name + "\n" + item.sqlString + "\n" + item.localString;
        CharSequence cs = advPref.getSummary();
        if (cs != null) {//returned from other activity
            String oldSummary = cs.toString();
            if (!summary.equals(oldSummary))//Settings1243.getSearchRequestPreference()&&
                SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, SettingsSearchActivity.this);//set dirty if advanced search is active and summary changed
        }
        advPref.setSummary(summary);
    }

    private void updateBasicSearchSummary() {

        Preference bPref = findPreference(getString(R.string.basic_search));
        String summary = SettingsActivity.createSearchQuery(this, SettingsActivity.getSelectedInternalCatalogs(getApplicationContext(), SettingsActivity.DSO_SELECTION));//pass info whether dso sel catalog is ngcic
        CharSequence cs = bPref.getSummary();
        if (cs != null) {//returned from other activity
            String oldSummary = cs.toString();
            if (!summary.equals(oldSummary))
                SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, this);//set dirty if basic search is active and summary changed
        }
        bPref.setSummary(summary);
    }

    private void updateTelescopeSummary() {
        findPreference(getString(R.string.telescope_search)).setSummary(SettingsActivity.getTName() + " ("
                + SettingsActivity.getTAperture() + "x"
                + SettingsActivity.getTFocus() + ")");
    }

    private void updateLimitMagSummary() {
        final String sum = this.getString(R.string.sky_lm_summary);
        double lm = SettingsActivity.getLM();
        findPreference(getString(R.string.d_lm)).setSummary(sum + "\n" + String.format(Locale.US, "%.1f", lm));
    }


    private void updateSearch() {
        int search_type = SettingsActivity.getSearchType();
        Preference obj_types = findPreference(getString(R.string.obj_types_pref));
        Preference empty_field = findPreference(getString(R.string.absent_data));
        Preference primary = findPreference(getString(R.string.basic_search));
        Preference advanced = findPreference(getString(R.string.advanced_search));


        switch (search_type) {
            case SettingsActivity.PRIMARY_SEARCH:
                obj_types.setEnabled(true);
                empty_field.setEnabled(true);
                primary.setEnabled(true);
                advanced.setEnabled(false);
                findPreference(getString(R.string.select_search_type2)).setSummary(R.string.primary);
                break;
            case SettingsActivity.ADVANVCED_SEARCH:
                obj_types.setEnabled(false);
                empty_field.setEnabled(false);
                primary.setEnabled(false);
                advanced.setEnabled(true);
                findPreference(getString(R.string.select_search_type2)).setSummary(R.string.sql_like_expression);
                break;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "preferenceChanged " + this);
        if (key.equals(getString(R.string.d_lm))) {
            SettingsActivity.updateLM();
            updateLimitMagSummary();

        } else if (key.equals(getString(R.string.absent_data))) {
            updateEmptyFieldTreatment();
            SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, this);
        } else if (key.equals(getString(R.string.duplicates_search))) {
            SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, this);
            SettingsActivity.updateRemovingDuplicates();
        } else if (key.equals(getString(R.string.select_search_type2))) {
            SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, this);
            updateSearch();
        }

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
        p.findPreference(getString(R.string.geolocation)).setSummary(summary);

        ((BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
        new LocationTask().execute(lat, lon);
        return summary;
    }

    class LocationTask extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground(Double... ds) {
            double lat = ds[0];
            double lon = ds[1];
            String name = AstroTools.getLocationName(lat, lon, getApplicationContext());
            return name;
        }

        @Override
        protected void onPostExecute(String locname) {

            Preference p = findPreference(getString(R.string.geolocation));
            if (p != null && locname != null) {
                String summary = p.getSummary().toString();
                Log.d(TAG, "summary=" + summary + " " + summary.indexOf("\n"));

                int indexn = summary.indexOf("\n");
                int pos = summary.indexOf(getString(R.string.provider_));
                Log.d(TAG, "pos=" + pos);
                if (pos != -1) {
                    if (Math.abs(pos - summary.indexOf("\n")) < 3)//no location info yet
                        summary = summary.substring(0, pos) + locname + "\n" + summary.substring(pos);
                } else {
                    if (indexn == -1)//no location info yet
                        summary = summary + "\n" + locname;
                }
                p.setSummary(summary);
            }

        }
    }
}
