package com.astro.dsoplanner.graph;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.database.CometsDatabase;
import com.astro.dsoplanner.database.CustomDatabase;
import com.astro.dsoplanner.database.CustomDatabaseLarge;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.NgcicDatabase;

import java.util.ArrayList;
import java.util.List;

class NearbyDSO implements Runnable {
    private static final String TAG = "Graph";
    Point nearbyObject;
    int cat;
    double fov;
    double vis;
    GraphActivity graphActivity;
    Handler handler;

    public NearbyDSO(Point nearbyObject, int cat, double fov, double vis, GraphActivity graphActivity, Handler handler) {
        this.nearbyObject = nearbyObject;
        this.cat = cat;
        this.fov = fov;
        this.vis = vis;
        this.graphActivity = graphActivity;
        this.handler = handler;
    }


    public void run() {
        Message message = new Message();
        message.arg1 = 4;//starting message;
        handler.sendMessage(message);

        graphActivity.threadListNearby = new ArrayList<AstroObject>();

        newsearch();

        message = new Message();
        message.arg1 = 5;//over
        handler.sendMessage(message);

        message = new Message();
        message.arg1 = 1;//stand for nearby
        handler.sendMessage(message);
    }

    private void newsearch() {
        List<Integer> catlist = SettingsActivity.getSelectedInternalCatalogs(graphActivity, SettingsActivity.SEARCH_NEARBY);

        if (catlist.contains(AstroCatalog.HERSHEL)) {
            if (!catlist.contains(AstroCatalog.NGCIC_CATALOG))
                catlist.add(AstroCatalog.NGCIC_CATALOG);
        }

        catlist.addAll(SettingsActivity.getCatalogSelectionPrefs(graphActivity, SettingsActivity.SEARCH_NEARBY));
        for (int cat : catlist) {
            Log.d(TAG, "cat nearby=" + cat);
            DbListItem item = AstroTools.findItemByCatId(cat);
            Log.d(TAG, "item=" + item);
            if (item != null) {
                List<AstroObject> la = searchDb(item, nearbyObject, fov, vis);
                addObjects(la);
            }
        }


    }

    private void addObjects(List<AstroObject> list) {

        if (SettingsActivity.isRemovingDuplicates()) {
            list.removeAll(graphActivity.threadListNearby);
            graphActivity.threadListNearby.addAll(list);
        } else {
            graphActivity.threadListNearby.addAll(list);
        }
    }

    private List<AstroObject> searchDb(DbListItem item, Point nearbyObject, double fov, double vis) {
        List<AstroObject> list = new ArrayList<AstroObject>();

        AstroCatalog cat;
        Log.d(TAG, "item=" + item);
        if (item.cat == AstroCatalog.NGCIC_CATALOG)
            cat = new NgcicDatabase(graphActivity);
        else if (item.cat == AstroCatalog.COMET_CATALOG)
            cat = new CometsDatabase(graphActivity, AstroCatalog.COMET_CATALOG);
        else if (item.cat == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG)
            cat = new CometsDatabase(graphActivity, AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG);
        else if (item.ftypes.isEmpty())
            cat = new CustomDatabase(graphActivity, item.dbFileName, item.cat);
        else
            cat = new CustomDatabaseLarge(graphActivity, item.dbFileName, item.cat, item.ftypes);
        final ErrorHandler eh = new ErrorHandler();
        cat.open(eh);
        if (!eh.hasError()) {
            list = cat.searchNearby(nearbyObject, fov, vis);
            cat.close();
        } else {
            graphActivity.runOnUiThread(new Runnable() {
                public void run() {
                    eh.showErrorInToast(graphActivity);
                }
            });

        }
        return list;
    }

}
