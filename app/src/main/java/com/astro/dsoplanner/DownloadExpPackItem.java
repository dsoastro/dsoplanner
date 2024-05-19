package com.astro.dsoplanner;

import java.io.File;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.astro.dsoplanner.expansion.APKExpansion;
import com.astro.dsoplanner.googleplay.LVL;

public class DownloadExpPackItem extends DownloadItem {

    public DownloadExpPackItem(Context context, Handler handler) {
        super(context, handler, "", context.getString(R.string.expansion_pack), "", context.getString(R.string.expansion_pack_contains_all_application_databases), "", Global.BASIC_VERSION ? 97287250L : Global.PLUS_VERSION ? 461969731L : 1838572749L, 0, "Downloading data pack");
        path = APKExpansion.getMainExpPath(Global.getAppContext(), Global.mainVersion);
        File f = new File(path);
        f.getParentFile().mkdirs();
        fileUri = Uri.fromFile(f);
    }


    public void start() {
        final Runnable r = new Runnable() {
            public void run() {
                new LVL("FILE_URL1").connectToGooglePlayServers(context, new ExpPackCallback());
            }
        };
        Runnable r1 = new Runnable() {
            public void run() {
                new Thread(r).start();
            }
        };
        InputDialog d = AstroTools.getDialogNoCancel(context, context.getString(R.string.please_restart_the_application_after_downloading_expansion_pack_to_install_it_), r1);
        d.show();

    }

    private class ExpPackCallback implements LVL.Passable {

        public void pass(String expansionURL) {

            Log.d(TAG, "url=" + expansionURL);

            if (expansionURL == null || expansionURL.length() < 10) {
                String msg = Global.getAppContext().getString(R.string.can_not_connect_to_the_server_please_try_again_);
                postMessage(msg);
            } else {
                url = expansionURL;
                setUri(url);
                long id = startDownload();
                if (id != -1) {
                    DownloadReceiver.downloadMap.put(id, DownloadReceiver.ACTION_EXP_PACK_DOWNLOAD);
                }
            }
            handler.sendEmptyMessage(DownloadListActivity.DISMISS_DIALOG);
        }
    }
}

