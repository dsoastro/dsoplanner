package com.astro.dsoplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.Log;
import android.widget.BaseAdapter;

import com.astro.dsoplanner.scopedrivers.CommunicationManager;

public class SettingsGraphActivity extends ParentPreferenceActivity implements OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final String D_DSSB = "d_dssb";
    private static final String DSSZOOM = "dsszoom";
    private static final String AUTO_ROTATION = "autoRotation";
    private static final String CROSS = "cross_";
    private static final String DSO = "dso__";
    private static final String D_TR = "d_tr";
    private static final String TAG = SettingsGraphActivity.class.getSimpleName();

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_graph_new);
        findPreference(getString(R.string.settings_graph_start_chart)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsGraphActivity.this, SettingsInclActivity.class);
                intent.putExtra(Constants.XML_NUM, R.xml.settings_graph_start_chart_incl);
                startActivity(intent);
                return false;
            }
        });

        findPreference(getString(R.string.settings_graph_eqt)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsGraphActivity.this, SettingsInclActivity.class);
                intent.putExtra(Constants.XML_NUM, R.xml.settings_graph_eqt_incl);
                startActivity(intent);
                return false;
            }
        });

        findPreference(getString(R.string.settings_graph_layers)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsGraphActivity.this, SettingsInclActivity.class);
                intent.putExtra(Constants.XML_NUM, R.xml.settings_graph_layers_incl);
                startActivity(intent);
                return false;
            }
        });

        findPreference(getString(R.string.settings_graph_other)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsGraphActivity.this, SettingsInclActivity.class);
                intent.putExtra(Constants.XML_NUM, R.xml.settings_graph_other_incl);
                startActivity(intent);
                return false;
            }
        });
        findPreference(getString(R.string.settings_graph_push_cam)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsGraphActivity.this, SettingsInclActivity.class);
                intent.putExtra(Constants.XML_NUM, R.xml.settings_graph_push_cam_incl);
                startActivity(intent);
                return false;
            }
        });

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        Preference p = findPreference(SettingsActivity.OPT_NIGHT_MODE);
        p.setOnPreferenceClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateRealTime() {
        Log.d(TAG, "update real time");
        CheckBoxPreference p = (CheckBoxPreference) findPreference(getString(R.string.autotime));
        p.setChecked(SettingsActivity.isAutoTimeUpdating());

    }

    private void updateBTsummary() {
        getPreferenceScreen().findPreference(getString(R.string.settings_bt)).setSummary(
                CommunicationManager.getComModule().isConnected() ?
                        R.string.goto_mount_connected : R.string.goto_mount_disconnected);

        ((BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsActivity.OPT_NIGHT_MODE)) {
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        final String key = preference.getKey();
        Log.d(TAG, "key=" + key);
        final SharedPreferences prefs = preference.getSharedPreferences();

        if (key.equals(SettingsActivity.OPT_NIGHT_MODE)) {
            if (SettingsActivity.isAutoNightModeOn()) {
                boolean nightmode = SettingsActivity.getNightMode();
                boolean nmrequired = AstroTools.isNightModeRequired();
                if (nightmode != nmrequired) {
                    SettingsActivity.forceNightMode(!nightmode);//else wrong toast color, after restart the correct noght mode will be set anyway
                    InputDialog.toast(getString(R.string.automatic_nighmode_is_on_), this).show();
                } else
                    NightModeChangeTracker.set();
            } else
                NightModeChangeTracker.set();
            restart();
        }
        return false;
    }

    void restart() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

}
