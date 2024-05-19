package com.astro.dsoplanner;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.astro.dsoplanner.expansion.APKExpansion;

import java.io.File;

public class DownloadExpPackItemGithub extends DownloadItem {

    public DownloadExpPackItemGithub(Context context, Handler handler) {
        super(context, handler, "", context.getString(R.string.expansion_pack), "", context.getString(R.string.expansion_pack_contains_all_application_databases), "", Global.BASIC_VERSION ? 97287250L : Global.PLUS_VERSION ? 461969731L : 1838572749L, 0, "Downloading data pack");
        path = APKExpansion.getMainExpPath(Global.getAppContext(), Global.mainVersion);
        File f = new File(path);
        f.getParentFile().mkdirs();
        fileUri = Uri.fromFile(f);
    }


    public void start() {

        Runnable r1 = new Runnable() {
            public void run() {

                url = Global.GITHUB_EXPPACK_URL;
                setUri(url);
                long id = startDownload();
                if (id != -1) {
                    DownloadReceiver.downloadMap.put(id, DownloadReceiver.ACTION_EXP_PACK_DOWNLOAD);
                }
                handler.sendEmptyMessage(DownloadListActivity.DISMISS_DIALOG);
            }
        };
        InputDialog d = AstroTools.getDialogNoCancel(context, context.getString(R.string.please_restart_the_application_after_downloading_expansion_pack_to_install_it_), r1);
        d.show();

    }
}

