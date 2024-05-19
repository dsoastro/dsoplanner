package com.astro.dsoplanner;

import static com.astro.dsoplanner.Global.ALEX_MENU_FLAG;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.SensorEventListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.Comet;
import com.astro.dsoplanner.base.MinorPlanet;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.download.DSSdownloadable;
import com.astro.dsoplanner.expansion.APKExpansion;
import com.astro.dsoplanner.graph.GraphActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.Adler32;


public class DSOmainActivity extends ParentActivity implements ActivityCompat.OnRequestPermissionsResultCallback, GestureDetector.OnGestureListener {
    private Handler han;
    private static final String TAG = "DSOmain";
    private static final String PrefFileName = "prefdata";

    //exp pack handler constants (what)
    public static final int SHOW_INFO = 1;
    public static final int WORK_OVER = 2;
    public static final int HANDLE_EXP_PACK = 3;

    public static final int requestCodeDsoSelection = 22;
    public static final int requestCodeDsoObservation = 23;
    public static final int requestCodeDsoSky = 24;
    public static final int requestCodeDsoSettings = 25;
    public static final int requestCodeDsoTools = 26;
    public static final int requestCodeHandleExpPack = 27;
    private SensorEventListener mySensorListener = null;
    private boolean firstBack = true;
    private BroadcastReceiver mBatteryReceiver;
    /**
     * permissions to ask
     */
    String[] permissions = {

            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};


    private void dsoSelectionCommands() {
        startActivity(new Intent(DSOmainActivity.this, QueryActivity.class));
    }

    //Main Menu events
    public void dsoSelection() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                dsoSelectionCommands();
            }
        };

        String explanation = getString(R.string.location_permission);
        AstroTools.askForPermissionsOrRun(this, r, permissions, explanation, requestCodeDsoSelection, true);
    }

    private void dsoObservationCommands() {
        startActivity(new Intent(DSOmainActivity.this, ObservationListActivity.class));
    }

    public void dsoObservation() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                dsoObservationCommands();
            }
        };
        String explanation = getString(R.string.location_permission);
        AstroTools.askForPermissionsOrRun(this, r, permissions, explanation, requestCodeDsoObservation, true);
    }

    private void dsoSkyCommands() {
        Calendar c = AstroTools.getDefaultTime(this);
        Point.setLST(AstroTools.sdTime(c));//need to calculate Alt and Az
        startActivity(new Intent(DSOmainActivity.this, GraphActivity.class));
    }

    public void dsoSky() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                dsoSkyCommands();
            }
        };
        String explanation = getString(R.string.location_permission);
        AstroTools.askForPermissionsOrRun(this, r, permissions, explanation, requestCodeDsoSky, true);
    }

    private void dsoSettingsCommand() {
        startActivity(new Intent(DSOmainActivity.this, SettingsSystemActivity.class));
    }

    public void dsoSettings() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                dsoSettingsCommand();
            }
        };
        String explanation = getString(R.string.location_permission);
        AstroTools.askForPermissionsOrRun(this, r, permissions, explanation, requestCodeDsoSettings, true);
    }

    private void dsoTwilightCommands() {
        startActivity(new Intent(DSOmainActivity.this, MiniLauncherActivity.class));
    }

    public void dsoTwilight() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                dsoTwilightCommands();
            }
        };
        String explanation = getString(R.string.location_permission);
        AstroTools.askForPermissionsOrRun(this, r, permissions, explanation, requestCodeDsoTools, true);
    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        Log.d(TAG, "" + Global.BASIC_VERSION);
        Log.d(TAG, "package" + getPackageName());
        super.onResume();
        if (finishedInOnResume) return;
        onResumeCode();
        paused = false;

    }

    private void onResumeCode() {
        if (SettingsSystemActivity.dsoMainRedrawRequired) { //SAND: changed){
            SettingsSystemActivity.dsoMainRedrawRequired = false;
            Intent i = new Intent(this, DSOmainActivity.class);
            finish();
            startActivity(i);
            //unregisterLightSensor();
            return;
        }
        if (buttoned_front_set) setButtonedFront();
    }


    private long getAdler32FileKey(File f) {
        InputStream is = null;
        Adler32 adler32 = new Adler32();
        byte[] buffer = new byte[1024];
        try {
            is = new BufferedInputStream(new FileInputStream(f));
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                adler32.update(buffer, 0, count);
            }
            return adler32.getValue();
        } catch (Exception e) {
            return -1;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    boolean paused = false;

    @Override
    public void onPause() {
        super.onPause();
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.reenableKeyguard();
        paused = true;
    }

    private static final int DIALOG_ERROR = 1;
    private static final int DIALOG_QUICK_START_GUIDE = 2;
    private static final int DIALOG_WHATSNEW = 3;
    private static final int DIALOG_COMET = 4;
    private static final int DIALOG_MP = 5;
    private static final int DIALOG_MIGRATE = 6;

    private Set<Integer> dialog_set = new HashSet<Integer>();

    /**
     * Manages dialogs to avoid dialog clutter on the screen
     *
     * @param dialog
     * @return
     */
    private boolean isToShowDialog(int dialog) {
        switch (dialog) {

            case DIALOG_QUICK_START_GUIDE:
                if (dialog_set.contains(DIALOG_ERROR) || dialog_set.contains(DIALOG_MIGRATE))
                    return false;
                else {
                    dialog_set.add(dialog);
                    return true;
                }
            case DIALOG_WHATSNEW:
                if (dialog_set.isEmpty()) {
                    dialog_set.add(dialog);
                    return true;
                } else return false;
            case DIALOG_COMET:
                if (dialog_set.isEmpty() || (dialog_set.size() == 1 && dialog_set.contains(DIALOG_MP))) {
                    dialog_set.add(DIALOG_COMET);
                    return true;
                } else return false;
            case DIALOG_MP:
                if (dialog_set.isEmpty() || (dialog_set.size() == 1 && dialog_set.contains(DIALOG_COMET))) {
                    dialog_set.add(DIALOG_MP);
                    return true;
                } else return false;
            case DIALOG_MIGRATE:
                if (dialog_set.contains(DIALOG_ERROR)) return false;
                else {
                    dialog_set.add(dialog);
                    return true;
                }
            case DIALOG_ERROR:
                dialog_set.add(dialog);
                return true;
        }
        return false;
    }

    boolean initRequired = false;
    boolean firstrun = false;
    ProgressDialog pd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        continueOnCreate();
    }


    /**
     * Show a series of popups asking user for the required app permissions.
     * <p>Calls the {@link #onRequestPermissionsResult} when done.</p>
     */

    private void continueOnCreate() {
        if (SettingsActivity.getSkinMap()) requestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.d(TAG, "0");

        han = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(TAG, "han, message=" + msg.what + " " + msg.obj);
                switch (msg.what) {
                    case SHOW_INFO:
                        if (msg.obj != null) waitMessage((String) msg.obj);
                        break;
                    case WORK_OVER:
                        if (msg.obj != null) {
                            if (isToShowDialog(DIALOG_ERROR)) {
                                if (!DSOmainActivity.this.isFinishing())
                                    registerDialog(InputDialog.message(DSOmainActivity.this, (String) msg.obj, 0)).show();
                            }
                        }

                        long exppack = SettingsActivity.getSharedPreferences(DSOmainActivity.this).getLong(Constants.EP_BEING_EXPANDED, Constants.EP_BEING_EXPANDED_DEF_VALUE);
                        long exppatch = SettingsActivity.getSharedPreferences(DSOmainActivity.this).getLong(Constants.EP_PATCH_BEING_EXPANDED, Constants.EP_PATCH_BEING_EXPANDED_DEF_VALUE);
                        Log.d(TAG, "exppack=" + exppack + " exppatch=" + exppatch);
                        if (exppack == -1 && exppatch == -1) {
                            setButtonedFront();
                            if (Global.TEST_MODE) {
                                boolean expanded = SettingsActivity.getSharedPreferences(DSOmainActivity.this).getBoolean(Constants.EP_EXPANDED, false);
                                boolean patchexpanded = SettingsActivity.getSharedPreferences(DSOmainActivity.this).getBoolean(Constants.EP_PATCH_EXPANDED, false);
                                if (expanded && patchexpanded) {
                                    Intent intent = new Intent(getApplicationContext(), TestIntentService.class);
                                    try {
                                        startService(intent);
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        }
                        break;
                    case HANDLE_EXP_PACK:
                        boolean pack_expanded = SettingsActivity.getSharedPreferences(Global.getAppContext()).getBoolean(Constants.EP_EXPANDED, false);
                        boolean patch_expanded = SettingsActivity.getSharedPreferences(Global.getAppContext()).getBoolean(Constants.EP_PATCH_EXPANDED, false);
                        if (!pack_expanded) handleExpPack(han);
                        if (!patch_expanded) handleExpPackPatch(han);
                        break;
                }


            }
        };
        copyNgcicDatabase();
        if (AstroTools.isComMapDbEmpty()) //on the first start there is no ngcic.db yet when Init.initComMapDb is called. So need to call it the second time
            Init.initComMapDb(getApplicationContext());

        createCometDatabase();
        SettingsActivity.setResetNgcicCometMpFlag(this, false);
        if (!nightMode) setContentView(R.layout.progress);
        else setContentView(R.layout.progressnight);
        updateDssUrl();


        boolean dataExpansionRequired = Init.isDataExpansionRequired(this);
        Log.d(TAG, "dataExpansionRequired=" + dataExpansionRequired);
        if (dataExpansionRequired) {
            SettingsSystemActivity.clearExpPackShPrefs(this);
        }
        boolean expanded = SettingsActivity.getSharedPreferences(DSOmainActivity.this).getBoolean(Constants.EP_EXPANDED, false);
        boolean patchexpanded = SettingsActivity.getSharedPreferences(DSOmainActivity.this).getBoolean(Constants.EP_PATCH_EXPANDED, false);
        Log.d(TAG, "expanded=" + expanded);
        Log.d(TAG, "patchexpanded=" + patchexpanded);


        if (!expanded || !patchexpanded) {
            han.sendMessage(getMessage(null, HANDLE_EXP_PACK));
        } else han.sendMessage(getMessage(null, WORK_OVER));

    }

    /**
     * Callback from the request permission flow end.
     *
     * @param requestCode  the above 69
     * @param permissions  original permissions list
     * @param grantResults list of user answers
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AstroTools.initOnWriteExternalStoragePermissionSet(getApplicationContext(), permissions, grantResults);

        if (requestCode == requestCodeDsoSelection) {
            dsoSelectionCommands();
        } else if (requestCode == requestCodeDsoObservation) {
            dsoObservationCommands();
        } else if (requestCode == requestCodeDsoSky) {

            dsoSkyCommands();
        } else if (requestCode == requestCodeDsoSettings) {

            dsoSettingsCommand();
        } else if (requestCode == requestCodeDsoTools) {

            dsoTwilightCommands();
        } else if (requestCode == requestCodeHandleExpPack) {
            for (int i = 0; i < permissions.length && i < grantResults.length; i++) {
                if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[i] != PackageManager.PERMISSION_GRANTED) {

                    SettingsActivity.putSharedPreferences(Constants.EP_BEING_EXPANDED, -1L, DSOmainActivity.this);
                    SettingsActivity.putSharedPreferences(Constants.EP_PATCH_BEING_EXPANDED, -1L, DSOmainActivity.this);
                    han.sendMessage(getMessage(getString(R.string.sd_card_permission_warning), WORK_OVER));

                    return;
                }
            }


            boolean pack_expanded = SettingsActivity.getSharedPreferences(Global.getAppContext()).getBoolean(Constants.EP_EXPANDED, false);
            boolean patch_expanded = SettingsActivity.getSharedPreferences(Global.getAppContext()).getBoolean(Constants.EP_PATCH_EXPANDED, false);
            if (!pack_expanded) handleExpPack(han);
            if (!patch_expanded) handleExpPackPatch(han);
        }
    }


    boolean buttoned_front_set = false;

    //Draws front screen using ordinary buttons
    private void setButtonedFront() {
        Log.d(TAG, "setButtonedFront start");
        Resources resources = getResources();
        Configuration conf = resources.getConfiguration();
        Log.d(TAG, "lang=" + conf.locale.getDisplayLanguage());

        buttoned_front_set = true;//was run at least one time
        boolean nm = true;
        setContentView(R.layout.dsomain);
        int display_mode = getResources().getConfiguration().orientation;


        if (!SettingsActivity.getNightMode()) { //no bitmap in night mode
            nm = false;
            try {
                Bitmap bmp = (SettingsActivity.getNagScreenOn()) ? BitmapFactory.decodeResource(getResources(), display_mode == Configuration.ORIENTATION_PORTRAIT ? R.drawable.som : R.drawable.som_rot) : BitmapFactory.decodeResource(getResources(), R.drawable.blank);
                ImageView iv = (ImageView) findViewById(R.id.start_image);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                iv.setImageBitmap(bmp);
            } catch (Throwable e) {
                Log.d(TAG, "Bitmap decoding Error:  " + e.getMessage());
            }
        }

        Button b1 = (Button) findViewById(R.id.dsoSelection);

        Display display = getWindowManager().getDefaultDisplay();
        int h = display.getHeight() / 12;
        Rect r = new Rect(0, 0, h, h);

        //Get buttons icons
        Drawable icon1 = getResources().getDrawable(nm ? R.drawable.fmr_1 : R.drawable.fmb_1);
        icon1.setBounds(r);
        Drawable icon2 = getResources().getDrawable(nm ? R.drawable.fmr_2 : R.drawable.fmb_2);
        icon2.setBounds(r);
        Drawable icon3 = getResources().getDrawable(nm ? R.drawable.fmr_3 : R.drawable.fmb_3);
        icon3.setBounds(r);
        Drawable icon4 = getResources().getDrawable(nm ? R.drawable.fmr_4 : R.drawable.fmb_4);
        icon4.setBounds(r);
        Drawable icon5 = getResources().getDrawable(nm ? R.drawable.fmr_5 : R.drawable.fmb_5);
        icon5.setBounds(r);


        b1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dsoSelection();
            }
        });
        b1.setCompoundDrawables(icon1, null, null, null);

        Button b2 = (Button) findViewById(R.id.dsoObservation);
        b2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dsoObservation();
            }
        });
        b2.setCompoundDrawables(icon2, null, null, null);

        Button b3 = (Button) findViewById(R.id.dsoSky);
        b3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dsoSky();
            }
        });
        b3.setCompoundDrawables(icon3, null, null, null);

        Button b4 = (Button) findViewById(R.id.dsoTwilight);
        b4.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dsoTwilight();
            }
        });
        b4.setCompoundDrawables(icon4, null, null, null);

        Button b5 = (Button) findViewById(R.id.dsoSettings);
        b5.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dsoSettings();
            }
        });
        b5.setCompoundDrawables(icon5, null, null, null);


        //for showing quick start dialog
        boolean firsttime = SettingsActivity.getSharedPreferences(this).getBoolean(Constants.QSTART_DIALOG_FLAG, true);
        Runnable rd = new Runnable() {
            public void run() {
                Intent intent = new Intent(DSOmainActivity.this, ScrollableTextActivity.class);
                intent.putExtra(ScrollableTextActivity.ARGUMENT, ScrollableTextActivity.QUICK_START_GUIDE);
                startActivity(intent);
            }
        };

        if (firsttime) {
            if (isToShowDialog(DIALOG_QUICK_START_GUIDE)) {
                registerDialog(AstroTools.getDialog(this, getString(R.string.would_you_like_to_see_quick_start_guide_), rd)).show();
                SettingsActivity.putSharedPreferences(Constants.QSTART_DIALOG_FLAG, false, this);
            }
        }


        boolean whatsnew = SettingsActivity.getSharedPreferences(this).getBoolean(Constants.WHATSNEW_FLAG, Constants.WHATSNEW_FLAG_DEF_VALUE);
        if (!whatsnew) {
            if (isToShowDialog(DIALOG_WHATSNEW)) {
                SettingsActivity.putSharedPreferences(Constants.WHATSNEW_FLAG, true, this);
                Runnable r2 = new Runnable() {
                    public void run() {
                        Intent intent = new Intent(DSOmainActivity.this, ScrollableTextActivity.class);
                        intent.putExtra(ScrollableTextActivity.ARGUMENT, ScrollableTextActivity.WHATS_NEW);
                        startActivity(intent);
                    }
                };
                registerDialog(AstroTools.getDialog(this, getString(R.string.would_you_like_to_see_whatsnew), r2)).show();
            }

        }
        if (isToShowDialog(DIALOG_COMET)) new CometUpdateTask().execute();
        if (isToShowDialog(DIALOG_MP)) new MinorPlanetUpdateTask().execute();

        boolean showButtonsInRedMode = SettingsActivity.isHideNavBar(getApplicationContext()) || (SettingsActivity.areButtonsInRedMode(getApplicationContext()));
        boolean showButtonsInDayMode = SettingsActivity.isHideNavBar(getApplicationContext()) || (SettingsActivity.areButtonsInDayMode(getApplicationContext()));

        if (nightMode && showButtonsInRedMode) {
            int height = display.getHeight();
            fabButtonBack = SettingsActivity.getNightModeBackButton(this, height);

        } else if (!nightMode && showButtonsInDayMode) {
            int height = display.getHeight();
            fabButtonBack = SettingsActivity.getDayModeBackButton(this, height, true);

        }
    }

    /**
     * clear front to save memory
     */
    private void clearButtonedFront() {
        ImageView iv = (ImageView) findViewById(R.id.start_image);
        iv.setImageBitmap(null);
        Button b1 = (Button) findViewById(R.id.dsoSelection);
        b1.setCompoundDrawables(null, null, null, null);

        Button b2 = (Button) findViewById(R.id.dsoObservation);

        b2.setCompoundDrawables(null, null, null, null);

        Button b3 = (Button) findViewById(R.id.dsoSky);

        b3.setCompoundDrawables(null, null, null, null);

        Button b4 = (Button) findViewById(R.id.dsoTwilight);

        b4.setCompoundDrawables(null, null, null, null);

        Button b5 = (Button) findViewById(R.id.dsoSettings);

        b5.setCompoundDrawables(null, null, null, null);


    }

    private void copyNgcicDatabase() {
        File db = getDatabasePath(Constants.NGCIC_DATABASE_NAME);
        db.getParentFile().mkdirs();
        Log.d(TAG, "db=" + db);
        Log.d(TAG, "db len" + db.length());
        Log.d(TAG, "db exists=" + db.exists());

        boolean toCopy = !db.exists() || Init.isDataExpansionRequired(this) || SettingsActivity.isResetNgcicCometMp(this);
        Log.d(TAG, "to copy ngc db = " + toCopy);

        if (toCopy) {
            try {
                copyFile(R.raw.ngcic, getDatabasePath(Constants.NGCIC_DATABASE_NAME));
            } catch (Exception e) {
                Log.d(TAG, "copyNgcicDatabase, exception=" + e);
            }
        }

    }


    class MinorPlanetUpdateTask extends AsyncTask<Void, Void, Boolean> {
        private static final int DURATION = 1000 * 3600 * 24 * 7; //a week

        @Override
        protected Boolean doInBackground(Void... ds) {
            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean auto_update = sh.getBoolean(getApplicationContext().getString(R.string.minor_planet_auto_update), true);
            Log.d(TAG, "auto update mp=" + auto_update);
            if (!auto_update) return false;

            long last_time = SettingsActivity.getSharedPreferences(getApplicationContext()).getLong(Constants.MP_LAST_UPDATE_TIME, 0);
            long now = Calendar.getInstance().getTimeInMillis();
            if (now - last_time < DURATION) return false;
            //try to update once a week, may not update but we still record this to diminish the load on server
            SettingsActivity.putSharedPreferences(Constants.MP_LAST_UPDATE_TIME, now, getApplicationContext());


            int current_version = SettingsActivity.getSharedPreferences(getApplicationContext()).getInt(Constants.MP_LAST_UPDATE_NUMBER, -1);
            int server_version = SettingsActivity.getMinorPlanetUpdateNumberOnServer(getApplicationContext());
            Log.d(TAG, "current version=" + current_version);
            Log.d(TAG, "server version=" + server_version);
            if (server_version != -1 && server_version > current_version) {
                Global.server_version = server_version;
                return true;
            } else return false;
        }

        @Override
        protected void onPostExecute(Boolean pass) {
            if (!pass) return;

            final InputDialog dimp = new InputDialog(DSOmainActivity.this);
            dimp.setMessage(getString(R.string.there_are_new_mp_orbital_elements_available));

            dimp.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
                public void onClick(String value) {
                    DownloadCMOItem it = new DownloadCMOItem(getApplicationContext(), null, AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG);
                    it.setNetworkTypeAllowed(DownloadItem.NETWORK_ALL);
                    it.start();
                }
            });
            dimp.setMiddleButton(getString(R.string.cancel), new InputDialog.OnButtonListener() {
                public void onClick(String value) {
                    dimp.dismiss();//not dismissed automatically
                }
            });
            dimp.setNegativeButton(getString(R.string.hide2), new InputDialog.OnButtonListener() {
                public void onClick(String value) {
                    SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    //DO NOT FORGET to put the current db version in shared prefs
                    sh.edit().putBoolean(getApplicationContext().getString(R.string.minor_planet_auto_update), false).commit();
                }
            });
            registerDialog(dimp).show();


        }
    }

    class CometUpdateTask extends AsyncTask<Void, Void, String> {
        private static final int DURATION = 1000 * 3600 * 24; //24 hours

        @Override
        protected String doInBackground(Void... ds) {
            long time_now = Calendar.getInstance().getTimeInMillis();
            long last_time_updated = SettingsActivity.getSharedPreferences(getApplicationContext()).getLong(Constants.LAST_COMET_UPDATE_TIME, 0);
            if (time_now - last_time_updated < DURATION) return null;
            SettingsActivity.putSharedPreferences(Constants.LAST_COMET_UPDATE_TIME, time_now, getApplicationContext());

            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean auto_update = sh.getBoolean(getString(R.string.comet_auto_update), true);
            Log.d(TAG, "auto update=" + auto_update);
            if (!auto_update) return null;
            try {
                URL url = new URL(SettingsActivity.getCometUpdateUrl(getApplicationContext()));// Comet.DOWNLOAD_URL);
                URLConnection con = url.openConnection();
                con.setConnectTimeout(2000);
                String header = con.getHeaderField("Last-Modified");
                if (header == null) header = "";
                return header;
            } catch (Exception e) {
                Log.d(TAG, "e=" + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final String date) {
            if (date == null) return;
            String date_prev = SettingsActivity.getStringFromSharedPreferences(getApplicationContext(), Constants.COMET_UPDATE_DATE_IN_HEADER, "");
            Log.d(TAG, "prev_date=" + date_prev + " date=" + date);

            if (date_prev.equals(date)) return;

            if (paused) return;

            //consider update = time when the dialog was shown to user, not the real update time
            //as otherwise we'll be asking each time in dso main even if he refused to update


            final InputDialog dimp = new InputDialog(DSOmainActivity.this);
            dimp.setMessage(getString(R.string.there_are_new_comet_orbital_elements_available_would_you_like_to_download_them_from_www_minorplanetcenter_net_to_update_comet_database_));

            dimp.setPositiveButton(getString(R.string.ok), new InputDialog.OnButtonListener() {
                public void onClick(String value) {

                    DownloadCMOItem it = new DownloadCMOItem(getApplicationContext(), null, AstroCatalog.COMET_CATALOG);
                    it.setNetworkTypeAllowed(DownloadItem.NETWORK_ALL);
                    it.start();
                    Global.LAST_COMET_UPDATE_DATE = date;
                }
            });
            dimp.setMiddleButton(getString(R.string.cancel), new InputDialog.OnButtonListener() {
                public void onClick(String value) {
                    SettingsActivity.putSharedPreferences(Constants.COMET_UPDATE_DATE_IN_HEADER, date, getApplicationContext());
                    dimp.dismiss();//not dismissed automatically
                }
            });
            dimp.setNegativeButton(getString(R.string.hide2), new InputDialog.OnButtonListener() {
                public void onClick(String value) {
                    SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    sh.edit().putBoolean(getString(R.string.comet_auto_update), false).commit();
                }
            });
            registerDialog(dimp).show();
        }
    }


    private void createCometDatabase() {

        File db = getDatabasePath(Comet.DB_NAME);
        db.getParentFile().mkdirs();
        boolean toCopy = !db.exists() || Init.isDataExpansionRequired(this) || SettingsActivity.isResetNgcicCometMp(this);
        Log.d(TAG, "to copy comet db = " + toCopy);

        if (toCopy) {
            try {
                copyFile(R.raw.comets, getDatabasePath(Comet.DB_NAME));

            } catch (Exception e) {
                Log.d(TAG, "copyCometDatabase, exception=" + e);
            }

        }

        db = getDatabasePath(MinorPlanet.DB_NAME_BRIGHT);
        db.getParentFile().mkdirs();
        toCopy = !db.exists() || Init.isDataExpansionRequired(this) || SettingsActivity.isResetNgcicCometMp(this);
        Log.d(TAG, "to copy mp db = " + toCopy);
        if (toCopy) {
            try {
                copyFile(R.raw.brightmp, getDatabasePath(MinorPlanet.DB_NAME_BRIGHT));
                SettingsActivity.putSharedPreferences(Constants.MP_UPDATED, true, getApplicationContext());

            } catch (Exception e) {
                Log.d(TAG, "copyMpDatabase, exception=" + e);
            }
        } else {
            boolean mp_updated = SettingsActivity.getSharedPreferences(getApplicationContext()).getBoolean(Constants.MP_UPDATED, false);
            if (mp_updated) return;
            File f = getDatabasePath(MinorPlanet.DB_NAME_BRIGHT);
            boolean result = f.renameTo(getDatabasePath("mp.db"));
            if (result) {
                try {
                    copyFile(R.raw.brightmp, getDatabasePath(MinorPlanet.DB_NAME_BRIGHT));
                } catch (Exception e) {
                    Log.d(TAG, "copyNgcicDatabase2, exception=" + e);
                    return;
                }
                Log.d(TAG, "update mp start");
                boolean res = APKExpansion.updateMinorPlanets(getApplicationContext());
                Log.d(TAG, "update mp end " + res);
                if (res)
                    SettingsActivity.putSharedPreferences(Constants.MP_UPDATED, true, getApplicationContext());
                getDatabasePath("mp.db").delete();
            }
        }
    }


    private void updateDssUrl() {
        Context context = getApplicationContext();
        String url = DSSdownloadable.HTTP_ARCHIVE_STSCI_EDU_CGI_BIN_DSS_SEARCH_V_POSS2UKSTU_RED_R;
        String url_old = DSSdownloadable.HTTP_ARCHIVE_STSCI_EDU_CGI_BIN_DSS_SEARCH_V_POSS2UKSTU_RED_R_OLD;
        if (!SettingsActivity.getBooleanFromSharedPreferences(context, Constants.IS_DSS_DOWNLOAD_URL_UPDATED, false)) {
            SettingsActivity.putSharedPreferences(Constants.IS_DSS_DOWNLOAD_URL_UPDATED, true, context);
            if (SettingsActivity.getDSSdownloadUrl(context).equals(url_old)) {
                SettingsActivity.setDSSdownloadUrl(context, url);
            }
        }
    }


    public void copyFile(int resid, File outfile) throws Exception {
        OutputStream output = null;
        InputStream in = null;
        try {
            in = getResources().openRawResource(resid);
            output = new BufferedOutputStream(new FileOutputStream(outfile));
            //transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        } finally {
            //Close the streams
            output.flush();
            output.close();
            in.close();
        }
    }

    public static void copyFile(File infile, File outfile) throws IOException {
        //Open your local db as the input stream

        File dbFile = infile;
        Log.d(TAG, "input file " + dbFile.exists());
        if (dbFile.exists()) {

            File dboFile = outfile;
            Log.d(TAG, "output file " + dboFile.exists());

            InputStream fis = new BufferedInputStream(new FileInputStream(dbFile));


            //Open the empty db as the output stream
            OutputStream output = new BufferedOutputStream(new FileOutputStream(outfile));
            //transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            //Close the streams
            output.flush();
            output.close();
            fis.close();

        }
    }


    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (buttoned_front_set) clearButtonedFront();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        Log.d(TAG, "onDestroy");
    }

    //show a messge on the spinner wheel screen
    private void waitMessage(final String string) {
        han.post(new Runnable() {
            public void run() {
                TextView t = (TextView) findViewById(R.id.pw_waitmessge);
                if (t != null) {
                    t.setText(string);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (ALEX_MENU_FLAG && keyCode == KeyEvent.KEYCODE_MENU) {
            return true; //always eat it!
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (firstBack) {
                firstBack = false;
                InputDialog.toast(getResources().getString(R.string.dsomain_backagain), this).show();
                try {
                    han.postDelayed(new Runnable() {
                        public void run() {
                            firstBack = true;
                        }
                    }, 2000);
                } catch (Exception e) {
                    finish();
                    return true;
                }
                return true;
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private Message getMessage(String s, int what) {
        Message msg = new Message();
        msg.obj = s;
        msg.what = what;
        return msg;
    }

    /**
     * to be run from non-UI thread. Sends a message to be shown or indicate that the work is over
     *
     * @return
     */
    private void handleExpPack(final Handler handler) {
        Log.d(TAG, "handle exp pack");
        Resources res = Global.getAppContext().getResources();

        boolean expanded = SettingsActivity.getSharedPreferences(Global.getAppContext()).getBoolean(Constants.EP_EXPANDED, false);

        if (expanded) {
            Log.d(TAG, "already expanded");
            SettingsActivity.putSharedPreferences(Constants.EP_BEING_EXPANDED, -1L, this);

            handler.sendMessage(getMessage(null, WORK_OVER));
            return;
        }
        handler.sendMessage(getMessage(res.getString(R.string.ep_extracting), SHOW_INFO));
        String expPath = APKExpansion.getExpPath(Global.getAppContext(), Global.mainVersion);
        Log.d(TAG, "expFolder exists=" + (new File(expPath)).getParentFile().exists());
        Log.d(TAG, "expPathFull=" + expPath);

        if (!new File(expPath).exists()) {
            SettingsActivity.putSharedPreferences(Constants.EP_BEING_EXPANDED, -1L, this);

            handler.sendMessage(getMessage(res.getString(R.string.expansion_pack_missing), WORK_OVER));
            return;
        }

        boolean isFreeSpaceAvailable = SettingsActivity.isFreeSpaceAvailable(this, getObbDir(), Global.EXP_PACK_FREE_SPACE_REQUIRED);
        if (!isFreeSpaceAvailable) {
            SettingsActivity.putSharedPreferences(Constants.EP_BEING_EXPANDED, -1L, this);
            handler.sendMessage(getMessage(res.getString(R.string.error_not_enough_free_space_patch), WORK_OVER));
            return;
        }

        long time = SettingsActivity.getSharedPreferences(this).getLong(Constants.EP_BEING_EXPANDED, Constants.EP_BEING_EXPANDED_DEF_VALUE);
        long now = Calendar.getInstance().getTimeInMillis();
        //exp pack being expanded already
        if (now - time < 10 * 60 * 1000) {//just in case there is some error and shared pref is not overwritten

            return;
        }
        SettingsActivity.putSharedPreferences(Constants.EP_BEING_EXPANDED, now, this);


        Log.d(TAG, "exp pack service called");
        Intent intent = new Intent(this, ExpPackIntentService.class);
        try {
            startService(intent);
        } catch (Exception e) {
        }
    }

    private void handleExpPackPatch(final Handler handler) {
        Log.d(TAG, "handle exp patch");
        Resources res = Global.getAppContext().getResources();

        boolean expanded = SettingsActivity.getSharedPreferences(Global.getAppContext()).getBoolean(Constants.EP_PATCH_EXPANDED, false);

        if (expanded) {
            Log.d(TAG, "already expanded");
            SettingsActivity.putSharedPreferences(Constants.EP_PATCH_BEING_EXPANDED, -1L, this);

            handler.sendMessage(getMessage(null, WORK_OVER));
            return;
        }
        handler.sendMessage(getMessage(res.getString(R.string.ep_extracting), SHOW_INFO));

        String expPath = APKExpansion.getExpPatchPath(Global.getAppContext(), Global.patchVersion);

        if (!new File(expPath).exists()) {
            SettingsActivity.putSharedPreferences(Constants.EP_PATCH_BEING_EXPANDED, -1L, this);

            handler.sendMessage(getMessage(res.getString(R.string.expansion_patch_missing), WORK_OVER));
            return;
        }

        boolean isFreeSpaceAvailable = SettingsActivity.isFreeSpaceAvailable(this, getObbDir(), Global.EXP_PACK_PATCH_FREE_SPACE_REQUIRED);
        if (!isFreeSpaceAvailable) {
            SettingsActivity.putSharedPreferences(Constants.EP_PATCH_BEING_EXPANDED, -1L, this);
            handler.sendMessage(getMessage(res.getString(R.string.error_not_enough_free_space_patch), WORK_OVER));
            return;
        }


        long time = SettingsActivity.getSharedPreferences(this).getLong(Constants.EP_PATCH_BEING_EXPANDED, Constants.EP_BEING_EXPANDED_DEF_VALUE);
        long now = Calendar.getInstance().getTimeInMillis();
        //exp pack being expanded already
        if (now - time < 10 * 60 * 1000) {//just in case there is some error and shared pref is not overwritten

            return;
        }
        SettingsActivity.putSharedPreferences(Constants.EP_PATCH_BEING_EXPANDED, now, this);

        Log.d(TAG, "exp pack service called");
        Intent intent = new Intent(this, ExpPackIntentService.class);
        intent.putExtra(Constants.EP_PATCH_BEING_EXPANDED, true);
        try {
            startService(intent);
        } catch (Exception e) {
        }

    }

    BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int message = intent.getIntExtra(Constants.EXP_PACK_MESSAGE, -1);
            Log.d(TAG, "message=" + message);
            switch (message) {
                case Constants.EXP_PACK_ERROR:
                    han.sendMessage(getMessage(getString(R.string.error_expanding_expansion_pack), WORK_OVER));
                    break;
                case Constants.EXP_PACK_COMPLETE:
                    han.sendMessage(getMessage(null, WORK_OVER));
                    break;

            }
        }
    };

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.EXP_PACK_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, filter);

    }

    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);

    }

    GestureDetector gDetector = new GestureDetector(this);

    @Override
    public boolean onTouchEvent(MotionEvent me) {

        return gDetector.onTouchEvent(me);
    }

    public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
        if (start == null || finish == null) return false;
        float dy = start.getRawY() - finish.getRawY();
        float dx = start.getRawX() - finish.getRawX();

        if (dx > Global.flickLength) { //left
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


    @Override
    protected void startTest(int action, int param) {
        super.startTest(action, param);
        switch (action) {
            case TestIntentService.DSO_MAIN_DSO_SELECTION:
                findViewById(R.id.dsoSelection).performClick();
                break;
            case TestIntentService.DSO_MAIN_SETTINGS:
                findViewById(R.id.dsoSettings).performClick();
                break;
        }

    }

    @Override
    protected int getTestActivityNumber() {
        return TestIntentService.DSO_MAIN;
    }


}
