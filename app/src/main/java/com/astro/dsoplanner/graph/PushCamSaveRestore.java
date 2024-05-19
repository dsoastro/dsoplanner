package com.astro.dsoplanner.graph;

//ToDo. Save data from adjust in SensorProcessor? Save rc?
//ToDo. clear prefs for P4 and P. Use their methods

import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.Exportable;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.pushcamera.P;
import com.astro.dsoplanner.pushcamera.P4;

/**
 * manage Graph Push Cam shared Prefs
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class PushCamSaveRestore {
    private static final String TAG = "Graph";
    private static final String KEY1 = "Ssjhdgf7";
    private static final String KEY2 = "Ssjhdgf8";
    private static final String KEY3 = "Ssjhdgf9";
    GraphActivity graph;
    PushCamBroadcastReceiver pushCamBroadcastReceiver;

    public PushCamSaveRestore(GraphActivity graph, PushCamBroadcastReceiver pushCamBroadcastReceiver) {
        this.graph = graph;
        this.pushCamBroadcastReceiver = pushCamBroadcastReceiver;
    }


    public void save() {
        Point alignmentObject = pushCamBroadcastReceiver.getAlignmentObject();
        if (alignmentObject != null && alignmentObject instanceof Exportable) {
            SettingsActivity.putSharedPreferences(KEY1, alignmentObject, graph);
        } else {
            SettingsActivity.removeSharedPreferencesKey(KEY1, graph);
        }
        P4 center_info_align_any_object = pushCamBroadcastReceiver.getCenter_info_align_any_object();
        if (center_info_align_any_object != null) {
            center_info_align_any_object.saveToSharedPrefs(graph, KEY2);
        }
        P rc = pushCamBroadcastReceiver.getRc();
        if (rc != null) {
            rc.saveToSharedPrefs(graph, KEY3);
        }
        Log.d(TAG, "PushCamSaveRestore, save. alignmentObject=" + alignmentObject);
        Log.d(TAG, "PushCamSaveRestore, save. center_info_align_any_object=" + center_info_align_any_object);
        Log.d(TAG, "PushCamSaveRestore, save. rc=" + rc);


    }

    /**
     * restore vars right into Graph instance
     */
    public void restore() {
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(graph);
        Exportable e = SettingsActivity.getExportableFromSharedPreference(KEY1, graph);

        pushCamBroadcastReceiver.setAlignmentObject(null);
        if (e != null && e instanceof Point)
            pushCamBroadcastReceiver.setAlignmentObject((Point) e);

        pushCamBroadcastReceiver.setCenter_info_align_any_object(P4.restoreFromSharedPrefs(graph, KEY2));
        pushCamBroadcastReceiver.setRc(P.restoreFromSharedPrefs(graph, KEY3));
        Log.d(TAG, "PushCamSaveRestore, restore. alignmentObject=" + pushCamBroadcastReceiver.getAlignmentObject());
        Log.d(TAG, "PushCamSaveRestore, restore. center_info_align_any_object=" + pushCamBroadcastReceiver.getCenter_info_align_any_object());
        Log.d(TAG, "PushCamSaveRestore, restore. rc=" + pushCamBroadcastReceiver.getRc());

    }

    public void clearSharedPrefs() {
        SettingsActivity.removeSharedPreferencesKey(KEY1, graph);
        P4 center_info_align_any_object = pushCamBroadcastReceiver.getCenter_info_align_any_object();
        if (center_info_align_any_object != null) {
            center_info_align_any_object.clearSharedPrefs(graph, KEY2);
        }
        P rc = pushCamBroadcastReceiver.getRc();
        if (rc != null) {
            rc.clearSharedPrefs(graph, KEY3);
        }
    }

}