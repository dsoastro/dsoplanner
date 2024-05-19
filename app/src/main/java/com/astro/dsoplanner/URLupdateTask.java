package com.astro.dsoplanner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class URLupdateTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "URLupdateTask";
    @MarkTAG
    public static String DOWNLOAD_URL_BASE = "http://www.dsoplanner.com/update/";
    private String aURL;
    private static final String fname = "urls.txt";

    Context context;

    public static String getUpdateUrl(Context appContext) {
        String packageName = appContext.getPackageName();
        packageName = packageName.replace(".", "/");
        String aURL = DOWNLOAD_URL_BASE + packageName + "/" + fname;
        return aURL;

    }

    public URLupdateTask(Context appContext) {
        this.context = appContext;

        aURL = getUpdateUrl(appContext);
        Log.d(TAG, "aURL=" + aURL);
    }

    @Override
    protected Boolean doInBackground(Void... ds) {

        URL url;
        BufferedReader br = null;
        try {
            url = new URL(aURL);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            InputStream in = con.getInputStream();
            br = new BufferedReader(new InputStreamReader(in));
            String s;
            int i = 0;
            while ((s = br.readLine()) != null && i < 3) {
                //format
                //1 line - comet url
                //2 line - mp url
                //3 line - dss url
                Log.d(TAG, "s=" + s);
                switch (i) {
                    case 0://comet url
                        SettingsActivity.setCometUpdateUrl(context, s);
                        break;
                    case 1: //minor planet url
                        SettingsActivity.setMinorPlanetUpdateUrl(context, s);
                        break;
                    case 2: //dss url
                        SettingsActivity.setDSSdownloadUrl(context, s);
                        break;
                }
                i += 1;


            }

        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
            return false;
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
        }


        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result)
            InputDialog.toast(context.getString(R.string.urls_updated), context).show();
        else
            InputDialog.toast(context.getString(R.string.error_updating_urls), context).show();
    }

}
