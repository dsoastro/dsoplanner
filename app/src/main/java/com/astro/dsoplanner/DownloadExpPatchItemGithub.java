package com.astro.dsoplanner;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.astro.dsoplanner.expansion.APKExpansion;

import java.io.File;

public class DownloadExpPatchItemGithub extends DownloadItem {

    public DownloadExpPatchItemGithub(Context context, Handler handler) {
        super(context, handler, "", context.getString(R.string.expansion_patch), "", context.getString(R.string.expansion_patch_contains), "", (Global.BASIC_VERSION ? Global.EXP_PATCH_SIZE_BASIC : (Global.PLUS_VERSION ? Global.EXP_PATCH_SIZE_PLUS : Global.EXP_PATCH_SIZE_PRO)), 0, "Downloading data pack");
        path = APKExpansion.getMainExpPatchPath(Global.getAppContext(), Global.patchVersion);
        File f = new File(path);
        f.getParentFile().mkdirs();
        fileUri = Uri.fromFile(f);
    }


    public void start() {
        Runnable r1 = new Runnable() {
            public void run() {
                url = Global.GITHUB_EXPPATCH_URL;
                setUri(url);
                long id = startDownload();
                if (id != -1) {
                    DownloadReceiver.downloadMap.put(id, DownloadReceiver.ACTION_EXP_PATCH_DOWNLOAD);
                }
                handler.sendEmptyMessage(DownloadListActivity.DISMISS_DIALOG);
            }
        };
        InputDialog d = AstroTools.getDialogNoCancel(context, context.getString(R.string.please_restart_the_application_after_downloading_expansion_pack_to_install_it_), r1);
        d.show();
    }
}
