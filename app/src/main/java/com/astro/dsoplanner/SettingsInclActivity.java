package com.astro.dsoplanner;

import static com.astro.dsoplanner.Constants.constellations;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;


import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.BaseAdapter;

import com.astro.dsoplanner.base.Comet;
import com.astro.dsoplanner.base.ObjCursor;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.download.DSSdownloadable;
import com.astro.dsoplanner.expansion.APKExpansion;
import com.astro.dsoplanner.graph.GraphActivity;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.scopedrivers.CommunicationManager;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SettingsInclActivity extends ParentPreferenceActivity
        implements OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener, OnGestureListener {
    private static final int LIST_SELECTOR = 1;
    private static final int BACKUP_OPEN_TREE = 2;
    private static final int RESTORE_OPEN_TREE = 3;
    private static final String TAG = "SettingsIncl";
    private final GestureDetector gDetector = new GestureDetector(this);
    static final int MAX_UDB_CH_PREFS = 10;//if necessary add prefs in settings_select_catalogs_incl
    private static final String _3_100 = "3 100";
    private static final String _12_515 = "12 515";
    private static final String PRO = " (Pro)";
    private static final String COLOR = "color_";
    private static final String UHOR = "uhor__";
    private static final String D_DSSB = "d_dssb";
    private static final String DSSZOOM = "dsszoom";
    private static final String AUTO_ROTATION = "autoRotation";
    private static final String CROSS = "cross_";
    private static final String DSO = "dso__";
    private static final String D_TR = "d_tr";
    private ScopedBackupRestore backupProcess;
    private ScopedBackupRestore restoreProcess;
    private boolean mNightGuard = false;
    private boolean mNightGuard2 = false;
    int xml;
    /**
     * SettingsActivity.DSO_SELECTION OR SEARCH_NEARBY
     */
    int search_call = SettingsActivity.DSO_SELECTION;
    private String[] spinStringArr = new String[constellations.length];
    private boolean ucac4flag = false;

    Map<Integer, Integer> udbmap = new HashMap<Integer, Integer>();//num of pref - cat number


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onAct res=" + requestCode + " " + resultCode + " " + data);
        if (requestCode == LIST_SELECTOR && resultCode == RESULT_OK) {
            boolean[] checks = data.getBooleanArrayExtra(Constants.LIST_SELECTOR_CHECKS);
            for (int i = 0; i < 10; i++) {
                Log.d(TAG, "check=" + checks[i]);
            }
            String convert = SettingsActivity.convertBooleanArrToString(checks);
            Log.d(TAG, "convert=" + convert);
            SettingsActivity.putSharedPreferences(Constants.LIST_SELECTOR_CHECKS, convert, getApplicationContext());
            updateConstellationSummary();
        }
        if (requestCode == BACKUP_OPEN_TREE && resultCode == RESULT_OK) {
            backupProcess.processBackupCallback(requestCode, resultCode, data);
        }
        if (requestCode == RESTORE_OPEN_TREE && resultCode == RESULT_OK) {
            restoreProcess.processRestoreCallback(requestCode, resultCode, data);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        xml = i.getIntExtra(Constants.XML_NUM, -1);
        if (xml == -1) {
            finish();
            return;
        }

        backupProcess = new ScopedBackupRestore(this, BACKUP_OPEN_TREE);
        restoreProcess = new ScopedBackupRestore(this, RESTORE_OPEN_TREE);


        addPreferencesFromResource(xml);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        setupSpecialPrefClickListener();
        if (xml == R.xml.settings_basic_search_vis_incl) {
            updateMinAlt();
            updateMinDim();
            updateFilter();
        }
        if (xml == R.xml.settings_basic_search_obj_types_incl) {
            search_call = SettingsActivity.DSO_SELECTION;
        }
        if (xml == R.xml.settings_basic_search_obj_types_search_nearby_incl || xml == R.xml.settings_select_catalogs_nearby_incl) {
            search_call = SettingsActivity.SEARCH_NEARBY;
        }
        if (xml == R.xml.settings_select_catalogs_incl || xml == R.xml.settings_select_catalogs_nearby_incl) {
            listUserDatabases();
        }
        Preference pref = findPreference(getString(R.string.settings_obj_types));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                                                  @Override
                                                  public boolean onPreferenceClick(Preference preference) {
                                                      Intent i = new Intent(SettingsInclActivity.this, SettingsInclActivity.class);
                                                      i.putExtra(Constants.XML_NUM, R.xml.settings_basic_search_obj_types_incl);
                                                      int querycat = getIntent().getIntExtra(SettingsSearchActivity.QUERYCAT2, 0);
                                                      i.putExtra(SettingsSearchActivity.QUERYCAT2, querycat);
                                                      startActivity(i);
                                                      return true;
                                                  }
                                              }
            );
        }
        pref = findPreference(getString(R.string.catprefscreen));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                                                  @Override
                                                  public boolean onPreferenceClick(Preference preference) {
                                                      Intent i = new Intent(SettingsInclActivity.this, SettingsInclActivity.class);
                                                      i.putExtra(Constants.XML_NUM, R.xml.settings_basic_search_cat_incl);
                                                      startActivity(i);
                                                      return true;
                                                  }
                                              }
            );
        }

        pref = findPreference(getString(R.string.backup));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    if (SettingsActivity.getNightMode()) {
                        if (!mNightGuard) { //first time here
                            mNightGuard = true;
                            InputDialog.message(SettingsInclActivity.this, R.string.nightmode_warning_click_again, 0).show();
                            return true; //prevent going through
                        }
                    }

                    backupProcess.startBackup();
                    return false;
                }
            });
        }
        pref = findPreference(getString(R.string.restore));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {

                    if (SettingsActivity.getNightMode()) {
                        if (!mNightGuard2) { //first time here
                            mNightGuard2 = true;
                            InputDialog.message(SettingsInclActivity.this, R.string.nightmode_warning_click_again, 0).show();
                            return true; //prevent going through
                        }
                    }
                    restoreProcess.startRestore();
                    return false;
                }
            });
        }
        pref = findPreference(getString(R.string.backup_help));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    String message = getString(R.string.backup_restore_detailed_help);
                    if (Global.PLUS_VERSION || Global.BASIC_VERSION) {
                        message = message.replace(getString(R.string.dsoplanner), Init.DS_OPLANNER);
                    }
                    Intent intent = new Intent(SettingsInclActivity.this, ScrollableTextActivity.class);
                    intent.putExtra(ScrollableTextActivity.ARGUMENT, ScrollableTextActivity.BACKUP_HELP);
                    intent.putExtra(ScrollableTextActivity.ARGUMENT_2, message);
                    startActivity(intent);
                    return false;
                }
            });
        }


        pref = findPreference(getString(R.string.basic_search_constellations));
        if (pref != null) {
            updateConstellationSummary();
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getApplicationContext(), ListSelectorActivity.class);
                    String[] names = new String[Constants.constellationLong.length - 1];
                    for (int i = 1; i < Constants.constellationLong.length; i++) {
                        names[i - 1] = Constants.constellationLong[i];
                    }

                    String checkstr = SettingsActivity.getStringFromSharedPreferences(getApplicationContext(), Constants.LIST_SELECTOR_CHECKS, "");
                    Log.d(TAG, "checkstr=" + checkstr);
                    boolean[] checks;
                    if (!"".equals(checkstr))
                        checks = SettingsActivity.retrieveBooleanArrFromString(checkstr);
                    else
                        checks = new boolean[names.length];
                    intent.putExtra(Constants.LIST_SELECTOR_NAMES, names);
                    intent.putExtra(Constants.LIST_SELECTOR_CHECKS, checks);
                    intent.putExtra(Constants.LIST_SELECTOR_NAME, getString(R.string.select_constellations));

                    startActivityForResult(intent, LIST_SELECTOR);
                    return true;
                }
            });
        }

        Preference gpref = findPreference(getString(R.string.antialiasing));
        if (gpref != null) {
            gpref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    Log.d(TAG, "setting anti aliasing");
                    SettingsActivity.setAntialiasing();
                    GraphActivity.redrawRequired = true;
                    return true;
                }
            });
        }

        Preference cpref = findPreference(getString(R.string.pscolors));
        if (cpref != null) {
            cpref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsInclActivity.this, SettingsInclActivity.class);
                    intent.putExtra(Constants.XML_NUM, R.xml.settings_daytimecolors_incl);
                    startActivity(intent);
                    return false;

                }
            });
        }

        cpref = findPreference(getString(R.string.psncolors));
        if (cpref != null) {
            cpref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsInclActivity.this, SettingsInclActivity.class);
                    intent.putExtra(Constants.XML_NUM, R.xml.settings_nighttimecolors_incl);
                    startActivity(intent);
                    return false;

                }
            });
        }


        Preference upref = findPreference(getString(R.string.t_uhor_name));
        if (upref != null) {
            upref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final Runnable rnew = new Runnable() {
                        public void run() {

                            IPickFileCallback listener = new IPickFileCallback() {
                                public void callbackCall(Uri uri) {
                                    SettingsActivity.setUHorUri(uri.toString());
                                    SettingsActivity.setUHor();
                                }

                            };

                            SelectFileActivity.setPath(SettingsActivity.getFileDialogPath(getApplicationContext()));
                            SelectFileActivity.setListener(listener);
                            Intent fileDialog = new Intent(SettingsInclActivity.this, SelectFileActivity.class);
                            startActivity(fileDialog);
                        }
                    };

                    String path = SettingsActivity.getUHorFile();
                    if (new File(path).exists()) {
                        InputDialog dimp = new InputDialog(SettingsInclActivity.this);
                        dimp.setMessage(getString(R.string.would_you_like_to_update_horizon_from_existing_file_or_choose_a_new_file_instead_));
                        dimp.setPositiveButton(getString(R.string.update), new InputDialog.OnButtonListener() {
                            public void onClick(String v) {
                                SettingsActivity.setUHor();
                            }
                        });
                        dimp.setNegativeButton(getString(R.string.new_file), new InputDialog.OnButtonListener() {
                            public void onClick(String v) {
                                rnew.run();
                            }
                        });
                        dimp.show();
                    } else {
                        rnew.run();
                    }


                    return false;
                }

            });
        }


        Preference epref = findPreference(getString(R.string.ss_bluetooth));
        if (epref != null) {
            if (!AstroTools.doesBtExist()) {
                getPreferenceScreen().removePreference(epref);
            } else {
                epref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference arg0) {
                        startActivity(new Intent(SettingsInclActivity.this, SettingsBluetoothActivity.class));
                        return false;
                    }
                });
            }
        }

        pref = findPreference(getString(R.string.settings_bt));
        if (pref != null) {
            if (!AstroTools.doesBtExist()) {
                PreferenceGroup pg = (PreferenceGroup) findPreference(getString(R.string.settings_graph_eqt_telecope_category));
                pg.removePreference(pref);
            } else {
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent(SettingsInclActivity.this, SettingsBluetoothActivity.class);
                        startActivity(intent);
                        return false;
                    }
                });
            }
        }


        epref = findPreference(getString(R.string.eps_screen));
        if (epref != null) {
            epref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    Intent intent = new Intent(SettingsInclActivity.this, EyepiecesListActivity.class);
                    intent.setAction(Constants.ACTION_EP_SHOW);//COM_ASTRO_DSOPLANNER_EYEPIECES_LIST_VIEW);
                    startActivity(intent);
                    return false;
                }
            });
        }
        epref = findPreference(getString(R.string.ss_telescope_search));
        if (epref != null) {
            epref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference arg0) {
                    startActivity(new Intent(SettingsInclActivity.this, TelescopeListActivity.class));
                    return false;
                }
            });
        }


        pref = findPreference(getString(R.string.eps_screen_gr));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsInclActivity.this, EyepiecesListActivity.class);
                    startActivity(intent);
                    return false;
                }
            });
        }

        pref = findPreference(getString(R.string.telrad_screen));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsInclActivity.this, SettingsInclActivity.class);
                    intent.putExtra(Constants.XML_NUM, R.xml.settings_telrad_incl);
                    startActivity(intent);
                    return false;
                }
            });
        }

        pref = findPreference(getString(R.string.userhor));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsInclActivity.this, SettingsInclActivity.class);
                    intent.putExtra(Constants.XML_NUM, R.xml.settings_user_horiz_incl);
                    startActivity(intent);
                    return false;
                }
            });
        }

        pref = findPreference(getString(R.string.crossview));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsInclActivity.this, SettingsInclActivity.class);
                    intent.putExtra(Constants.XML_NUM, R.xml.settings_obj_marker_incl);
                    startActivity(intent);
                    return false;
                }
            });
        }

        pref = findPreference(getString(R.string.dsosettings));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsInclActivity.this, SettingsInclActivity.class);
                    intent.putExtra(Constants.XML_NUM, R.xml.settings_dso_layer_incl);
                    startActivity(intent);
                    return false;
                }
            });
        }

        pref = findPreference(getString(R.string.dss_settings));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsInclActivity.this, SettingsInclActivity.class);
                    intent.putExtra(Constants.XML_NUM, R.xml.settings_dss_pref_screen_incl);
                    startActivity(intent);
                    return false;
                }
            });
        }

        pref = findPreference(getString(R.string.ngcic_types));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsInclActivity.this, SettingsInclActivity.class);
                    intent.putExtra(Constants.XML_NUM, R.xml.settings_graph_ngcobjects_incl);
                    startActivity(intent);
                    return false;
                }
            });
        }

        pref = findPreference(getString(R.string.real_time_screen_update));

        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    registerDialog(getRealTimeDialog()).show();
                    return false;
                }
            });
        }

        pref = findPreference(getString(R.string.tycho));
        if (pref != null && Global.BASIC_VERSION) {
            pref.setSummary(R.string._600_000_stars_up_to_10_7m);
        }

        PreferenceCategory pcat = (PreferenceCategory) findPreference(getString(R.string.star_layers_category));
        if (pcat != null && Global.BASIC_VERSION) {
            pref = pcat.findPreference(getString(R.string.ucac4));
            if (pref != null)
                pcat.removePreference(pref);
        }
        pcat = (PreferenceCategory) findPreference(getString(R.string.object_layers_category));
        if (pcat != null && Global.BASIC_VERSION) {
            pref = pcat.findPreference(getString(R.string.pgclayer));
            if (pref != null)
                pcat.removePreference(pref);

            pref = findPreference(getString(R.string.ngciclayer));
            if (pref != null)
                pref.setTitle(R.string.ngcic);

            pref = findPreference(getString(R.string.ngciclayer));
            if (pref != null)
                pref.setTitle(R.string.ngcic);

            pref = findPreference(getString(R.string.ngcic_types));
            if (pref != null)
                pref.setTitle(R.string.ngcic_object_types);

        }


        pref = findPreference(getString(R.string.ucac4));
        if (pref != null && Global.PLUS_VERSION) {
            pref.setSummary(R.string._15_5_mn_stars_up_to_14m);
        }

        pref = findPreference(getString(R.string.pgclayer));
        if (pref != null && Global.PLUS_VERSION) {
            pref.setSummary(R.string._147_000_galaxies_to_15_3m_from_principal_galaxies_catalogue);
        }


        pref = findPreference(getString(R.string.ngciclayer));
        if (pref != null && Global.BASIC_VERSION) {
            pref.setSummary(R.string._3100_deep_sky_objects);
        }

        pref = findPreference(getString(R.string.select_catalog_ngcic));
        if (pref != null && Global.BASIC_VERSION) {
            String summary = getString(R.string._12_515_objects_of_all_types);
            summary = summary.replace(_12_515, _3_100);
            pref.setSummary(summary);
        }


        if (Global.BASIC_VERSION) {

            PreferenceCategory pc0 = (PreferenceCategory) findPreference(getString(R.string.additional_parameters_double_star_catalogs_category));
            if (pc0 != null) {
                getPreferenceScreen().removePreference(pc0);
            }


            PreferenceCategory pc = (PreferenceCategory) findPreference(getString(R.string.select_catalog_brightest_objects_category));


            if (pc != null) {
                pref = pc.findPreference(getString(R.string.select_catalog_sac));
                if (pref != null)
                    pc.removePreference(pref);
            }


            pc = (PreferenceCategory) findPreference(getString(R.string.select_catalog_nearby_brightest_category));


            if (pc != null) {
                pref = pc.findPreference(getString(R.string.select_catalog_sac2));
                if (pref != null)
                    pc.removePreference(pref);
            }


            pref = findPreference(getString(R.string.select_catalog_galaxies_category));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }

            pref = findPreference(getString(R.string.select_catalog_nebula_category));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }

            pref = findPreference(getString(R.string.select_catalog_clusters_category));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }

            pref = findPreference(getString(R.string.select_catalog_user_category));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }

            pref = findPreference(getString(R.string.select_catalog_ds_category));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }


            pref = findPreference(getString(R.string.select_catalog_galaxies_category2));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }

            pref = findPreference(getString(R.string.select_catalog_nearby_nebula_category));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }

            pref = findPreference(getString(R.string.select_catalog_nearby_clusters_category));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }


            pref = findPreference(getString(R.string.select_catalog_nearby_ds_category));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }

            //trimming types
            pc = (PreferenceCategory) findPreference(getString(R.string.object_types_category));

            pref = findPreference(getString(R.string.type_ast));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
            pref = findPreference(getString(R.string.type_ast2));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
            if (pc != null) {
                pref = pc.findPreference(getString(R.string.graph_ast));
                if (pref != null)
                    pc.removePreference(pref);
            }


            pref = findPreference(getString(R.string.type_cg));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
            pref = findPreference(getString(R.string.type_cg2));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
            if (pc != null) {
                pref = pc.findPreference(getString(R.string.graph_cg));
                if (pref != null)
                    pc.removePreference(pref);
            }


            pref = findPreference(getString(R.string.type_dn));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
            pref = findPreference(getString(R.string.type_dn2));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
            if (pc != null) {
                pref = pc.findPreference(getString(R.string.graph_dn));
                if (pref != null)
                    pc.removePreference(pref);
            }


            pref = findPreference(getString(R.string.type_hiirgn));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
            pref = findPreference(getString(R.string.type_hiirgn2));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
            if (pc != null) {
                pref = pc.findPreference(getString(R.string.graph_hii));
                if (pref != null)
                    pc.removePreference(pref);
            }


            pref = findPreference(getString(R.string.type_custom));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
            pref = findPreference(getString(R.string.type_custom2));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }


            pref = findPreference(getString(R.string.type_star));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
            pref = findPreference(getString(R.string.type_star2));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
            if (pc != null) {
                pref = pc.findPreference(getString(R.string.graph_star));
                if (pref != null)
                    pc.removePreference(pref);
            }


            pref = findPreference(getString(R.string.type_ds));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
            pref = findPreference(getString(R.string.type_ds2));
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }


        }

        if (Global.BASIC_VERSION) {
            pref = findPreference(getString(R.string.ngcic_types));
            if (pref != null) {
                pref.setTitle(R.string.ngcic_object_types);
            }
            if (xml == R.xml.settings_graph_ngcobjects_incl) {
                setTitle(R.string.ngcic_layer_preferences);
            }
        }


        pref = findPreference(getString(R.string.select_catalog_pgc));
        if (pref != null && Global.PLUS_VERSION) {
            PreferenceCategory pc = (PreferenceCategory) findPreference(getString(R.string.select_catalog_galaxies_category));
            if (pc != null)
                pc.removePreference(pref);
        }
        pref = findPreference(getString(R.string.select_catalog_pgc2));
        if (pref != null && Global.PLUS_VERSION) {
            PreferenceCategory pc = (PreferenceCategory) findPreference(getString(R.string.select_catalog_galaxies_category2));
            if (pc != null)
                pc.removePreference(pref);
        }


        pref = findPreference(getString(R.string.select_catalog_clear));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.d(TAG, "clear catalogs");
                    clearCatalogPrefs(search_call);

                    return false;
                }
            });
        }

        pref = findPreference(getString(R.string.obj_types_clear));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean state = SettingsActivity.getSharedPreferences(SettingsInclActivity.this).getBoolean(Constants.BASIC_SEARCH_OBJ_TYPES_STATE, true);
                    state = !state;
                    SettingsActivity.putSharedPreferences(Constants.BASIC_SEARCH_OBJ_TYPES_STATE, state, SettingsInclActivity.this);
                    setBasicSearchObjTypes(state, search_call);

                    return false;
                }
            });
        }

        pref = findPreference(getString(R.string.graph_obj_types_clear));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    boolean state = SettingsActivity.getSharedPreferences(SettingsInclActivity.this).getBoolean(Constants.GRAPH_OBJ_TYPES_STATE, true);
                    state = !state;
                    SettingsActivity.putSharedPreferences(Constants.GRAPH_OBJ_TYPES_STATE, state, SettingsInclActivity.this);
                    setGraphObjTypes(state);

                    return false;
                }
            });
        }

        pref = findPreference(getString(R.string.night_mode_def_intensities));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    SettingsActivity.setDefaultRedModeIntensity(getApplicationContext());
                    InputDialog.message(SettingsInclActivity.this, R.string.default_colors_set).show();
                    return false;
                }
            });
        }

        pref = findPreference(getString(R.string.set_urls));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsInclActivity.this, SettingsInclActivity.class);
                    intent.putExtra(Constants.XML_NUM, R.xml.settings_urls_incl);
                    startActivity(intent);
                    return false;

                }
            });
        }


        pref = findPreference(getString(R.string.data_files_help));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    String summary = APKExpansion.getMainExpPath(getApplicationContext(), Global.mainVersion) + "\n\n";
                    summary += APKExpansion.getMainExpPatchPath(getApplicationContext(), Global.patchVersion);

                    registerDialog(InputDialog.message(SettingsInclActivity.this, summary, 0)).show();
                    return false;
                }
            });
        }
        pref = findPreference(getString(R.string.check_data_file_availability));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference arg0) {
                    String path_exp_pack_main = APKExpansion.getMainExpPath(getApplicationContext(), Global.mainVersion);
                    String path_exp_patch_main = APKExpansion.getMainExpPatchPath(getApplicationContext(), Global.patchVersion);
                    String path_exp_pack_alt = APKExpansion.getExpPath(getApplicationContext(), Global.mainVersion);
                    String path_exp_patch_alt = APKExpansion.getExpPatchPath(getApplicationContext(), Global.patchVersion);
                    boolean exp_pack_main = new File(path_exp_pack_main).exists();
                    boolean exp_patch_main = new File(path_exp_patch_main).exists();
                    boolean exp_pack_alt = new File(path_exp_pack_alt).exists();
                    boolean exp_patch_alt = new File(path_exp_patch_alt).exists();
                    String summary = getString(R.string.main_location) + "\n";
                    summary += path_exp_pack_main + " - " + (exp_pack_main ? getString(R.string.exists) : getString(R.string.absent)) + "\n";
                    summary += path_exp_patch_main + " - " + (exp_patch_main ? getString(R.string.exists) : getString(R.string.absent)) + "\n";
                    summary += "\n" + getString(R.string.alternative_location) + "\n";
                    summary += path_exp_pack_alt + " - " + (exp_pack_alt ? getString(R.string.exists) : getString(R.string.absent)) + "\n";
                    summary += path_exp_patch_alt + " - " + (exp_patch_alt ? getString(R.string.exists) : getString(R.string.absent)) + "\n";


                    registerDialog(InputDialog.message(SettingsInclActivity.this, summary, 0)).show();
                    return false;
                }
            });
        }

        pref = findPreference(getString(R.string.update_urls));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    new URLupdateTask(getApplicationContext()).execute();
                    return false;

                }
            });

        }
        pref = findPreference(getString(R.string.restore_defaults));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick(Preference preference) {
                    SettingsActivity.setCometUpdateUrl(getApplicationContext(), Comet.DOWNLOAD_URL);
                    SettingsActivity.setDSSdownloadUrl(getApplicationContext(), DSSdownloadable.HTTP_ARCHIVE_STSCI_EDU_CGI_BIN_DSS_SEARCH_V_POSS2UKSTU_RED_R);
                    SettingsActivity.setMinorPlanetUpdateUrl(getApplicationContext(), SettingsActivity.getDefaultMinorPlanetUpdateUrl(getApplicationContext()));
                    InputDialog.toast(getString(R.string.default_urls_restored), getApplicationContext()).show();
                    return false;

                }
            });

        }

        pref = findPreference(getString(R.string.quinsighton));
        if (pref != null) {
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    GraphActivity.redrawRequired = true;
                    return false;
                }
            });
        }

    }

    InputDialog pd;

    public void showInProgressDialog() {
        if (pd == null) {
            Log.d(TAG, "in progress started");
            try {
                pd = InputDialog.pleaseWait(this, getString(R.string.please_wait)).pop();
            } catch (Exception e) {
            }
        }
    }

    public void dismissInProgressDialog() {
        if (pd != null) {
            try {
                pd.dismiss();
            } catch (Exception e) {
            }
            pd = null;
        }
    }

    private void showHideDoubleStarInfo() {
        Preference p = findPreference(getString(R.string.show_double_separation));
        if (p == null) return;
        boolean star_label = SettingsActivity.isStarLabelOn();
        boolean obj_label = SettingsActivity.isObsObjectLabelOn();
        if (star_label || obj_label)
            p.setEnabled(true);
        else
            p.setEnabled(false);

    }

    private void updateConstellationSummary() {
        Preference p = findPreference(getString(R.string.basic_search_constellations));
        if (p != null) {
            p.setSummary(SettingsActivity.getBasicSearchConSummary(getApplicationContext()));
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "preference change, key=" + key);
        if (key.startsWith(D_TR)) updateTelrad();
        else if (key.equals(getString(R.string.d_uhor_step))) {
            SettingsActivity.setUHor();
            Log.d(TAG, "step updated");
        } else if (key.equals(getString(R.string.d_uhor__width))) SettingsActivity.setUHorWidth();
        else if (key.equals(getString(R.string.invsky))) GraphActivity.redrawRequired = true;
        else if (key.contains(DSO)) SettingsActivity.setDSOsettings();
        else if (key.contains(CROSS) || key.equals(AUTO_ROTATION))
            ObjCursor.setParameters(PreferenceManager.getDefaultSharedPreferences(this));
        else if (key.contains(DSSZOOM)) SettingsActivity.setDSSZoom();
        else if (key.equals(getString(R.string.d_dso__dim))) SettingsActivity.setDSOsettings();
        else if (key.startsWith(D_DSSB)) SettingsActivity.setDSSbrightness();
        else if (key.equals(getString(R.string.dimlight))

                || key.equals(SettingsActivity.OPT_ONYX_SKIN)
                || key.equals(getString(R.string.skinfront))
                || key.equals(getString(R.string.d_lightsensorlimit))//no ref in xml???
                || key.equals(getString(R.string.autonightmode))
                || key.equals(getString(R.string.nagscreenon))
                || key.equals(getString(R.string.full_screen_on))
                || key.equals(getString(R.string.full_screen_on_night))

        )
            SettingsSystemActivity.dsoMainRedrawRequired = true; //to redraw main screen with new skin
        else if (key.equals(getString(R.string.nicestars)))//no ref in xml???
            SettingsActivity.setStarMode();

        else if (key.equals(SettingsActivity.FLICK_BRIGHT)) {
            SettingsSystemActivity.dsoMainRedrawRequired = true;
            if (!SettingsActivity.isFlickBrightnessEnabled()) {
                SettingsActivity.updateBrightness(SettingsActivity.NORM_BRIGHTNESS);

            }
        } else if (key.equals(getString(R.string.d_batterywarning))) {
            Log.d(TAG, "battery set");
            SettingsActivity.setBatteryLow();
        } else if (key.equals(getString(R.string.bright_star_label)) || key.equals(getString(R.string.object_label)) || key.equals(getString(R.string.object_label_layer)) || key.equals(getString(R.string.show_double_separation))) {
            SettingsActivity.initLabels();
            showHideDoubleStarInfo();
        } else if (key.equals(getString(R.string.graph_gc)) || key.equals(getString(R.string.graph_gx)) || key.equals(getString(R.string.graph_gxycld)) ||
                key.equals(getString(R.string.graph_hii)) || key.equals(getString(R.string.graph_neb)) || key.equals(getString(R.string.graph_oc)) ||
                key.equals(getString(R.string.graph_ocneb)) || key.equals(getString(R.string.graph_pn)) || key.equals(getString(R.string.graph_snr)) ||
                key.equals(getString(R.string.graph_zero_mag)) || key.equals(getString(R.string.graph_ast)) || key.equals(getString(R.string.graph_cg)) || key.equals(getString(R.string.graph_dn)) || key.equals(getString(R.string.graph_star))) {
            SettingsActivity.initGraphSelection();
            updateNoMagFovInfo();
        } else if (key.equals(getString(R.string.grid_preference))) {
            updateGridSummary();
        } else if (key.equals(getString(R.string.d_auto_time_fov))) {
            updateRealTimeInfo();
        } else if (key.equals(getString(R.string.basic_search_name_start))) {
            updateNameStart();
            SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, this);
        } else if (key.equals(SettingsActivity.D_DIMENSION))
            updateMinDim();
        else if (key.equals(SettingsActivity.D_MIN_ALT))
            updateMinAlt();

        else if (key.equals(SettingsActivity.FILTER_PREFERENCE))
            updateFilter();
        else if (key.equals(SettingsActivity.D_DETECTION_LIMIT))
            updateFilter();
        else if (key.equals(SettingsActivity.D_MAX_MAG))
            updateFilter();
        else if (catChanged(key, search_call)) {
            SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, this);
        } else if (key.startsWith("type_"))
            SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, this);
        else if (key.equals(getString(R.string.layer_vis)))
            SettingsActivity.updateVisibleStatus();
        else if (key.equals(getString(R.string.layer_vis_threshold))) {
            SettingsActivity.updateLayerVisibilityThreshold();
            updateVisibilityThresholdSummary();
        } else if (key.equals(getString(R.string.basic_search_min_sep))) {
            updateMinSeparation();
        } else if (key.equals(getString(R.string.basic_search_max_sep))) {
            updateMaxSeparation();
        } else if (key.equals(getString(R.string.basic_search_max_mag2))) {
            updateMaxMag2();
        } else if (key.equals(getString(R.string.d_lm))) {
            SettingsActivity.updateLM();
            updateLimitMagSummary();

        } else if (key.equals(getString(R.string.graph_zero_mag_fov))) {
            updateNoMagFovInfo();
        } else if (key.startsWith("d_ncolor_")) {
            GraphActivity.redrawRequired = true;
        } else if (key.equals(getString(R.string.d_scale_labels))) {
            SettingsActivity.initLabelsScale(getApplicationContext());
            GraphActivity.redrawRequired = true;
        } else if (key.equals(getString(R.string.alternative_data_location))) {
            if (xml == R.xml.settings_exp_pack_incl) {
                Log.d(TAG, "alternative_data_location changed");
                ucac4flag = true;
            }


        } else if (key.equals(getString(R.string.push_cam_camera_api)) ||
                key.equals(getString(R.string.push_cam_camera2_photo)) ||
                key.equals(getString(R.string.push_cam_camera2_preview)) ||
                key.equals(getString(R.string.push_cam_camera2_preview_new))
        ) {
            GraphActivity.redrawRequired = true;
        }
    }

    void restart() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private void updateLimitMagSummary() {
        Preference p = findPreference(getString(R.string.d_lm));
        if (p == null)
            return;

        final String sum = this.getString(R.string.sky_lm_summary);
        double lm = SettingsActivity.getLM();
        p.setSummary(sum + "\n" + String.format(Locale.US, "%.1f", lm));
    }

    private void updateVisibilityThresholdSummary() {
        Preference ep = getPreferenceScreen().findPreference(getString(R.string.layer_vis_threshold));
        if (ep != null) {
            double t = SettingsActivity.getLayerVisibilityThreshold();
            String s = String.format(Locale.US, "%.1f", t);
            ep.setSummary(s);
        }
    }

    private void updateNameStart() {
        Preference ep = getPreferenceScreen().findPreference(getString(R.string.basic_search_name_start));
        if (ep != null) {
            ep.setSummary(SettingsActivity.getBasicSearchNameStartWith(this));
        }
    }

    /**
     * whether the check box for search catalog changed
     *
     * @param key
     * @return
     */
    private boolean catChanged(String key, int where) {
        int[] keys = SettingsActivity.getCatalogKeys(where);
        for (int k : keys) {
            if (key.equals(getString(k))) {
                return true;
            }
        }

        for (Map.Entry<Integer, Integer> e : udbmap.entrySet()) {
            String k = getUdbPrefKey(e.getKey());
            if (k.equals(key))
                return true;
        }
        return false;
    }

    private void updateMinDim() {
        Preference ep = getPreferenceScreen().findPreference(SettingsActivity.D_DIMENSION);
        if (ep == null) return;
        double dim = SettingsActivity.getDimension();
        ep.setSummary(String.format(Locale.US, "%.1f", dim) + (char) 39);
    }

    private void updateMinSeparation() {
        String key = getString(R.string.basic_search_min_sep);
        Preference ep = getPreferenceScreen().findPreference(key);
        if (ep == null) return;
        double minsep = SettingsActivity.getMinSeparation(getApplicationContext());
        ep.setSummary(String.format(Locale.US, "%.1f", minsep) + "\"");
    }

    private void updateMaxSeparation() {
        String key = getString(R.string.basic_search_max_sep);
        Preference ep = getPreferenceScreen().findPreference(key);
        if (ep == null) return;
        double maxsep = SettingsActivity.getMaxSeparation(getApplicationContext());
        ep.setSummary(String.format(Locale.US, "%.1f", maxsep) + "\"");
    }

    private void updateMaxMag2() {
        String key = getString(R.string.basic_search_max_mag2);
        Preference ep = getPreferenceScreen().findPreference(key);
        if (ep == null) return;
        double maxmag2 = SettingsActivity.getMaxMag2(getApplicationContext());
        ep.setSummary(String.format(Locale.US, "%.1f", maxmag2) + "m");
    }

    private void updateMinAlt() {
        Preference p = getPreferenceScreen().findPreference(SettingsActivity.D_MIN_ALT);
        if (p == null) return;
        double alt = SettingsActivity.getMinAlt();
        p.setSummary(String.format(Locale.US, "%.1f", alt) + '\u00B0');
    }

    private void updateFilter() {
        ListPreference ep = (ListPreference) getPreferenceScreen().findPreference(SettingsActivity.FILTER_PREFERENCE);
        Preference p1 = getPreferenceScreen().findPreference(SettingsActivity.D_DETECTION_LIMIT);
        Preference p3 = getPreferenceScreen().findPreference(SettingsActivity.D_MAX_MAG);
        if (ep != null && p1 != null && p3 != null) {
            String s = "";
            int filter = SettingsActivity.getFilter();
            switch (filter) {
                case 0:
                    s = getString(R.string.visibility_filter_objects_with_visibility_below_detection_limit_are_rejected);
                    p1.setEnabled(true);
                    p3.setEnabled(false);
                    break;
                case 1:
                    s = getString(R.string.maximum_magnitude_filter_objects_with_magnitude_higher_than_the_maximum_set_are_rejected);
                    p1.setEnabled(false);
                    p3.setEnabled(true);
                    break;
                case 2:
                    s = getString(R.string.no_filter);
                    p1.setEnabled(false);
                    p3.setEnabled(false);
                    break;
            }
            ep.setSummary(s);

            double dt = SettingsActivity.getDetectionLimit();
            p1.setSummary(String.format(Locale.US, "%.1f", dt));

            double mm = SettingsActivity.getMaxMag();
            p3.setSummary(String.format(Locale.US, "%.1f", mm));
        }
    }

    /**
     * setting obj types to be shown for object layer
     */
    private void setBasicSearchObjTypes(boolean state, int where) {
        int[] keys = SettingsActivity.getObjTypesKeys(where);
        for (int key : keys) {
            Preference p = findPreference(getString(key));
            if (p instanceof CheckBoxPreference) {
                CheckBoxPreference ch = (CheckBoxPreference) p;
                ch.setChecked(state);
            }
        }
    }

    /**
     * setting obj types to be shown for object layer
     */
    private void setGraphObjTypes(boolean state) {
        int[] keys = SettingsActivity.getGraphObjTypesKeys();
        for (int key : keys) {
            Preference p = findPreference(getString(key));
            if (p instanceof CheckBoxPreference) {
                CheckBoxPreference ch = (CheckBoxPreference) p;
                ch.setChecked(state);
            }
        }
    }

    private void clearCatalogPrefs(int where) {
        Log.d(TAG, "where=" + where);
        int[] keys = SettingsActivity.getCatalogKeys(where);
        for (int key : keys) {
            Log.d(TAG, "clear key=" + getString(key));
            Preference p = findPreference(getString(key));
            if (p instanceof CheckBoxPreference) {
                CheckBoxPreference ch = (CheckBoxPreference) p;
                ch.setChecked(false);
            }
        }
        for (Map.Entry<Integer, Integer> e : udbmap.entrySet()) {
            Preference p = findPreference(getUdbPrefKey(e.getKey()));
            if (p instanceof CheckBoxPreference) {
                CheckBoxPreference ch = (CheckBoxPreference) p;
                ch.setChecked(false);
            }
        }
    }

    /**
     * User database pref key depending on the pref number
     */
    private String getUdbPrefKey(int i) {
        return ("select_catalog_up" + i);
    }

    private void listUserDatabases() {
        InfoList ilist = ListHolder.getListHolder().get(InfoList.DB_LIST);
        int j = 1;
        Set<Integer> set = SettingsActivity.getCatalogSelectionPrefs(this, search_call);
        for (Object o : ilist) {
            DbListItem item = (DbListItem) o;
            if (!SearchRules.isInternalCatalog(item.cat)) {
                if (j < MAX_UDB_CH_PREFS + 1) {
                    CheckBoxPreference p = (CheckBoxPreference) findPreference(getUdbPrefKey(j));
                    p.setTitle(item.dbName);
                    p.setChecked(set.contains(item.cat));
                    udbmap.put(j, item.cat);
                    j++;
                }

            }
        }
        //removing unused dbs
        PreferenceCategory pc = (PreferenceCategory) findPreference(getString(R.string.select_catalog_user_category));
        for (int i = j; i < MAX_UDB_CH_PREFS + 1; i++) {
            Preference p = findPreference(getUdbPrefKey(i));
            if (p != null)
                pc.removePreference(p);
        }
    }

    private void saveUserDatabasesPrefs() {
        Set<Integer> set = new HashSet<Integer>();
        for (Map.Entry<Integer, Integer> e : udbmap.entrySet()) {
            try {
                CheckBoxPreference ch = (CheckBoxPreference) findPreference(getUdbPrefKey(e.getKey()));
                if (ch.isChecked()) {
                    set.add(e.getValue());
                }
            } catch (Exception ex) {
                continue;
            }
        }
        SettingsActivity.saveCatalogSelectionPrefs(this, set, search_call);
    }

    private void updateGridSummary() {
        Preference pref = findPreference(getString(R.string.grid_preference));
        if (pref != null) {
            int type = SettingsActivity.getGridType();
            String s = "";
            switch (type) {
                case SettingsActivity.ALTAZ_GRID:
                    s = getString(R.string.azimuthal);
                    break;
                case SettingsActivity.EQ_GRID:
                    s = getString(R.string.equatorial);
                    break;
                default:
                    s = getString(R.string.no_grid);
            }
            pref.setSummary(s);
        }
    }

    private void updateUserHorSummary() {
        Preference upref = findPreference(getString(R.string.t_uhor_name));
        if (upref != null) {
            String name = SettingsActivity.getUHorFile();
            if (!"".equals(name)) {
                String summary = getString(R.string.pairs_az_h_of_points_in_text_file);
                summary += "\n" + name;
                upref.setSummary(summary);
            }

        }
    }

    public void updateTelrad() {
        Preference tr = getPreferenceScreen().findPreference(getString(R.string.telrad_screen));
        if (tr == null) return;
        SharedPreferences set = PreferenceManager.getDefaultSharedPreferences(this);
        SettingsActivity.updateTelrad();
        String sum = String.format(Locale.US, "%.1f:%.1f:%.1f:%.1f:%.1f:%.1f:%.1f", //%d
                SettingsActivity.getFloat(set.getString(getString(R.string.d_tr1), "0.5"), 0.5f, 0.01f, 60f),
                SettingsActivity.getFloat(set.getString(getString(R.string.d_tr2), "2"), 2f, 0.01f, 60f),
                SettingsActivity.getFloat(set.getString(getString(R.string.d_tr3), "4"), 4f, 0.01f, 60f),
                SettingsActivity.getFloat(set.getString(getString(R.string.d_tr4), "8"), 8f, 0.01f, 60f),
                SettingsActivity.getFloat(set.getString(getString(R.string.d_tr5), "16"), 16f, 0.01f, 60f),
                SettingsActivity.getFloat(set.getString(getString(R.string.d_tra), "0"), 0, -360f, 360f),
                SettingsActivity.getFloat(set.getString(getString(R.string.d_trw), "4"), 4f, 0f, 15f));
        tr.setSummary(sum);
    }

    private void updateBTsummary() {
        Preference pref = getPreferenceScreen().findPreference(getString(R.string.settings_bt));
        if (pref == null) return;

        pref.setSummary(
                CommunicationManager.getComModule().isConnected() ?
                        R.string.goto_mount_connected : R.string.goto_mount_disconnected);

        ((BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserHorSummary();
        updateTelrad();
        updateBTsummary();
        updateGridSummary();
        updateRealTimeInfo();
        updateNameStart();
        updateVisibilityThresholdSummary();
        updateMinSeparation();
        updateMaxSeparation();
        updateMaxMag2();
        updateLimitMagSummary();
        updateNoMagFovInfo();
        showHideDoubleStarInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (xml == R.xml.settings_select_catalogs_incl)
            saveUserDatabasesPrefs();
        if (ucac4flag) {
            Log.d(TAG, "updating ucac4");
            Init.loadUcac4Ref(getApplicationContext());
            ucac4flag = false;
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        final String key = preference.getKey();
        final SharedPreferences prefs = preference.getSharedPreferences();
        Log.d(TAG, "key=" + key);
        if (key.startsWith(COLOR)) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    String key1 = key;
                    if (SettingsActivity.getInverseSky())
                        key1 = "i" + key;
                    int color0 = prefs.getInt(key1, 0xffffffff);
                    final ColorPickerDialog d = new ColorPickerDialog(SettingsInclActivity.this, color0, key1.substring(key1.indexOf('_') + 1));
                    d.setAlphaSliderVisible(true);
                    d.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt((SettingsActivity.getInverseSky() ? "i" : "") + COLOR + d.getName(), d.getColor());
                            editor.commit();
                            GraphActivity.redrawRequired = true;
                            Log.d(TAG, "pref name=" + COLOR + d.getName() + " color=" + d.getColor());
                        }
                    });

                    d.setButton2(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        //set the app defaults
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    registerDialog(d).show();
                }
            };


            if (SettingsActivity.getNightMode()) {
                Dialog d = AstroTools.getDialog(this, getString(R.string.this_setting_may_ruin_your_eyes_darkness_adaptation_if_you_are_not_using_a_red_film_over_your_device_s_screen_would_you_like_to_continue_anyway_), r);
                registerDialog(d).show();
            } else
                r.run();
            return true;
        }
        else if (key.equals(getString(R.string.autonightmode))) {
            if (SettingsActivity.isAutoNightModeOn()) {
                boolean nightmode = SettingsActivity.getNightMode();
                boolean nmrequired = AstroTools.isNightModeRequired();
                if (nightmode != nmrequired) {
                    myFinish();
                }
            }
        } else if (key.equals(SettingsActivity.OPT_ONYX_SKIN) || key.equals(getString(R.string.dimlight))
                || key.equals(getString(R.string.full_screen_on))
                || key.equals(getString(R.string.full_screen_on_night))) {
            Log.d(TAG, "onyx");
            myFinish();
        } else if (key.equals(getString(R.string.default_colors))) {
            Log.d(TAG, "default colors");
            SettingsActivity.setDefaultColors(true, getApplicationContext());
            InputDialog.message(this, R.string.default_colors_set).show();
        }

        return false;
    }

    private void setupSpecialPrefClickListener() {
        //not working anymore! Don't know why

        String[] keys = {getString(R.string.color_object), getString(R.string.color_userobject), getString(R.string.color_crossmarker), getString(R.string.color_eyepieces), getString(R.string.color_ep_cross),
                getString(R.string.color_telrad), getString(R.string.color_dss_image), getString(R.string.color_constellations), getString(R.string.color_horizon),
                getString(R.string.color_user_horizon), getString(R.string.color_labels), getString(R.string.color_grid),
                SettingsActivity.OPT_NIGHT_MODE, SettingsActivity.OPT_ONYX_SKIN, getString(R.string.autonightmode), getString(R.string.dimlight), getString(R.string.default_colors), getString(R.string.color_con_boundary),
                getString(R.string.color_milky_way), getString(R.string.full_screen_on), getString(R.string.full_screen_on_night)};
        for (String key : keys) {
            Preference p = findPreference(key);
            if (p != null) p.setOnPreferenceClickListener(this);
        }
    }

    private void myFinish() {
        setResult(RESULT_OK);
        finish();
    }

    private String[] getRealTimeDialogNamesArr() {
        double fov = SettingsActivity.getRealTimeFOV();
        String fovstr = String.format(Locale.US, "%.1f", fov);
        String[] names = new String[]{getString(R.string.update_screen), getString(R.string.do_not_update_screen_for_fov_below_) + fovstr + (char) 0x00B0, getString(R.string.update_screen_and_auto_center_selected_object_for_fov_below_) + fovstr + (char) 0x00B0};
        return names;
    }

    private Dialog getRealTimeDialog() {
        InputDialog d = new InputDialog(this);
        int value = SettingsActivity.getSharedPreferences(this).getInt(Constants.REAL_TIME_DIALOG_CHOICE, 0);
        d.setTitle(getString(R.string.select_real_time_mode));
        d.setValue("" + value);
        d.setNegativeButton(getString(R.string.cancel));
        d.setListItems(getRealTimeDialogNamesArr(), new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                int val = AstroTools.getInteger(value, 0, 0, 10);
                SettingsActivity.putSharedPreferences(Constants.REAL_TIME_DIALOG_CHOICE, val, SettingsInclActivity.this);
                updateRealTimeInfo();
            }
        });
        return d;
    }

    private void updateRealTimeInfo() {
        Preference p = findPreference(getString(R.string.real_time_screen_update));
        if (p == null) return;

        int value = SettingsActivity.getSharedPreferences(this).getInt(Constants.REAL_TIME_DIALOG_CHOICE, 0);
        String name = getRealTimeDialogNamesArr()[value];
        p.setSummary(name);
        p = findPreference(getString(R.string.d_auto_time_fov));
        if (p == null) return;
        double fov = SettingsActivity.getRealTimeFOV();

        p.setSummary(String.format(Locale.US, "%.1f", fov) + (char) 0x00B0);
        if (value == 0) {
            p.setEnabled(false);
        } else {
            p.setEnabled(true);
        }

    }

    private void updateNoMagFovInfo() {
        Preference p = findPreference(getString(R.string.graph_zero_mag_fov));
        if (p == null) return;


        double fov = SettingsActivity.getNoMagFOV();

        p.setSummary(String.format(Locale.US, "%.1f", fov) + (char) 0x00B0);
        if (SettingsActivity.isZeroMagOn()) {
            p.setEnabled(true);
        } else {
            p.setEnabled(false);
        }

    }

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
