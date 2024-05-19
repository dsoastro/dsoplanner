package com.astro.dsoplanner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.Comet;
import com.astro.dsoplanner.base.MinorPlanet;
import com.astro.dsoplanner.expansion.APKExpansion;

import android.net.Uri;

public class DownloadReceiver extends BroadcastReceiver {
    private static final String MP_DB = "mp.db";
    private static final String TAG = DownloadReceiver.class.getSimpleName();
    //keeps track of downloads id and relevant actions
    public static Map<Long, Integer> downloadMap = new HashMap<Long, Integer>();

    //actions
    public static final int NO_ACTION = 0;
    public static final int ACTION_EXP_PACK_DOWNLOAD = 1;
    public static final int ACTION_UPDATE_UCAC2 = 2;
    public static final int ACTION_UDPATE_COMET_CATALOG = 3;
    public static final int ACTION_UDPATE_BRIGHT_MINOR_PLANET_CATALOG = 4;
    public static final int ACTION_UDPATE_CRITICAL_MINOR_PLANET_CATALOG = 5;
    public static final int ACTION_UDPATE_DISTANT_MINOR_PLANET_CATALOG = 6;
    public static final int ACTION_UPDATE_UNUSUAL_MINOR_PLANET_CATALOG = 7;
    public static final int ACTION_EXP_PATCH_DOWNLOAD = 8;


    Activity activity;

    /**
     * Activity is needed to close VDA activity when updating CMO catalogs
     *
     * @param activity
     */
    public DownloadReceiver(Activity activity) {
        this.activity = activity;
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        long receivedID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
        DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(receivedID);
        Cursor cur = mgr.query(query);
        int index = cur.getColumnIndex(DownloadManager.COLUMN_STATUS);
        if (cur.moveToFirst()) {
            Log.d(TAG, "value=" + cur.getInt(index));
            if (cur.getInt(index) == DownloadManager.STATUS_SUCCESSFUL) {
                Integer action = downloadMap.get(receivedID);
                if (action != null) {
                    switch (action) {
                        case ACTION_EXP_PACK_DOWNLOAD:
                            SettingsActivity.putSharedPreferences(Constants.EP_EXPANDED, false, context);
                            InputDialog.toast(context.getString(R.string.download_is_complete_please_restart_the_application), context).show();
                            break;
                        case ACTION_EXP_PATCH_DOWNLOAD:
                            SettingsActivity.putSharedPreferences(Constants.EP_PATCH_EXPANDED, false, context);
                            InputDialog.toast(context.getString(R.string.download_is_complete_please_restart_the_application), context).show();
                            break;
                        case ACTION_UDPATE_COMET_CATALOG:
                            startUpdatingComets(context);
                            break;

                        case ACTION_UDPATE_BRIGHT_MINOR_PLANET_CATALOG:
                        case ACTION_UDPATE_CRITICAL_MINOR_PLANET_CATALOG:
                        case ACTION_UDPATE_DISTANT_MINOR_PLANET_CATALOG:
                        case ACTION_UPDATE_UNUSUAL_MINOR_PLANET_CATALOG:
                            final Context context2 = activity.getApplicationContext();
                            if (activity instanceof ViewDatabaseActivity) {
                                if (!activity.isFinishing()) activity.finish();
                            }

                            Runnable r = new Runnable() {

                                @Override
                                public void run() {
                                    boolean res = updateMinorPlanets(context2);
                                    Intent intent = new Intent(Constants.BTCOMM_MESSAGE_BROADCAST);
                                    String message = null;
                                    if (res) {
                                        message = context2.getString(R.string.minor_planets_database_updated);
                                        SettingsActivity.putSharedPreferences(Constants.MP_LAST_UPDATE_NUMBER, Global.server_version, context2);
                                        Log.d(TAG, "mp updated, server version=" + Global.server_version);
                                    } else
                                        message = context2.getString(R.string.error_updating_minor_planets_database);
                                    intent = new Intent(Constants.BTCOMM_MESSAGE_BROADCAST);

                                    intent.putExtra(Constants.BTCOMM_MESSAGE, message);
                                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context2);
                                    localBroadcastManager.sendBroadcast(intent);


                                }
                            };
                            new Thread(r).start();
                            break;
                    }
                }
            }
        }
        cur.close();
    }

    private void startUpdatingComets(Context context) {
        Intent intent = new Intent(context, ImportDatabaseIntentService.class);
        intent.putExtra(Constants.IDIS_CATALOG, AstroCatalog.COMET_CATALOG);
        intent.putExtra(Constants.IDIS_DBNAME, Comet.DB_NAME);
        intent.putExtra(Constants.IDIS_PASTING, false);
        String uriPath = Uri.fromFile(new File(Comet.DOWNLOAD_FILE_PATH)).toString();
        intent.putExtra(Constants.IDIS_FILENAME, uriPath);
        intent.putExtra(Constants.IDIS_CMO_UPDATE, true);
        ImportDatabaseIntentService.registerImportToService(Comet.DB_NAME);
        try {
            context.startService(intent);
        } catch (Exception e) {
        }
        if (activity instanceof ViewDatabaseActivity) {
            if (!activity.isFinishing()) activity.finish();
        }
        if (Global.LAST_COMET_UPDATE_DATE != null) {
            SettingsActivity.putSharedPreferences(Constants.COMET_UPDATE_DATE_IN_HEADER, Global.LAST_COMET_UPDATE_DATE, context);
        }

    }

    private boolean updateMinorPlanets(Context context) {
        File f = context.getDatabasePath(MinorPlanet.DB_NAME_BRIGHT);
        boolean result = f.renameTo(context.getDatabasePath(MP_DB));
        if (result) {

            boolean res = AstroTools.copyFile(MinorPlanet.DOWNLOAD_FILE_PATH, context.getDatabasePath(MinorPlanet.DB_NAME_BRIGHT).getAbsolutePath());

            if (res) {
                boolean res2 = APKExpansion.updateMinorPlanets(context);
                Log.d(TAG, "update mp end " + res);
                if (res2) {
                    //PUT DB VERSION IN SHARED PREFS
                    context.getDatabasePath(MP_DB).delete();
                    return true;
                } else {
                    f = context.getDatabasePath(MP_DB);
                    f.renameTo(context.getDatabasePath(MinorPlanet.DB_NAME_BRIGHT));
                }
            }
        }
        return false;
    }
}
