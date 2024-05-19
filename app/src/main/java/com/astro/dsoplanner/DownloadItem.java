package com.astro.dsoplanner;

import java.io.File;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;


/**
 * class for simple download from the server
 *
 * @author leonid
 */
abstract public class DownloadItem {
    private static final int ERROR = -1;

    private static final String MOBILE = "MOBILE";
    private static final String WIFI = "WIFI";

    public static final String TAG = "DownloadItem";
    public final static int NO_ACTION = 1;//just download
    public final static int ACTION_UNZIP = 2;//download and unzip
    public final static int ACTION_ADD_DATABASE = 3;//download and add database

    Context context;
    Handler handler;
    String url = "";
    String name = "";
    String des = "";//description
    String path = "";
    String title = "";

    long size = 0;
    long checksum;
    Uri fileUri;
    Uri uri;//internet uri
    String downloadNotificationTitle;

    /**
     * @param url
     * @param title    to be shown in the list
     * @param filename - file name, used in download list menu
     * @param des      - description
     * @param path     - full path to the file
     * @param size     - file size
     * @param checksum - Adler32 checksum
     */
    public DownloadItem(Context context, Handler handler, String url, String title, String filename, String des, String path,
                        long size, long checksum, String downloadNotificationTitle) {
        this.handler = handler;
        this.context = context;

        this.url = url;
        this.uri = Uri.parse(url);
        this.name = filename;
        this.des = des;
        this.path = path;
        this.size = size;
        this.checksum = checksum;
        File f = new File(path);
        fileUri = Uri.fromFile(f);
        this.downloadNotificationTitle = downloadNotificationTitle;
    }

    /**
     * @return true if necessary files for the item exist
     */
    public boolean exists() {
        File f = new File(path);
        return f.exists();
    }

    /**
     * @return the space required to download and process the file
     */
    public long spaceRequired() {
        return size;
    }

    abstract public void start();

    public static final int NETWORK_WIFI = 1;
    public static final int NETWORK_ALL = 3;
    private int network_type = NETWORK_WIFI;

    /**
     * WiFi allowed by default
     *
     * @param type
     */
    public void setNetworkTypeAllowed(int type) {
        network_type = type;
    }

    public void setUri(String url) {
        uri = Uri.parse(url);
    }

    public long startDownload() {
        boolean wifi = haveNetworkConnectionWifi();
        boolean mobile = haveNetworkConnectionMobile();
        switch (network_type) {
            case NETWORK_WIFI:
                if (!wifi) {
                    postMessage(context.getString(R.string.this_download_requires_wifi_network_connection_));
                    return ERROR;
                }
                break;
            case NETWORK_ALL:
                if (!(wifi || mobile)) {
                    postMessage(context.getString(R.string.this_download_requires_network_connection_));
                    return ERROR;
                }
                break;
        }


        if (isDownloading()) {
            Log.d(TAG, "isDownloading already");
            postMessage(context.getString(R.string.the_file_is_already_downloading_));
            return ERROR;
        }

        File f = new File(path);
        if (f.exists()) {
            f.delete();//need to delete as download service does not overwrite the file
        }
        Log.d(TAG, "url=" + url + " uri=" + uri);
        DownloadManager.Request request;
        try {
            request = new DownloadManager.Request(uri);
        } catch (Exception e) {
            return ERROR;
        }

        request.setDestinationUri(fileUri);
        request.setTitle(downloadNotificationTitle);
        if (network_type == NETWORK_WIFI)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        long id = mgr.enqueue(request);
        return id;
    }

    public boolean isDownloading() {
        DownloadManager mgr = (DownloadManager)
                context.getSystemService(Context.DOWNLOAD_SERVICE);
        boolean isdownloading = false;
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(
                DownloadManager.STATUS_PAUSED |
                        DownloadManager.STATUS_PENDING |
                        DownloadManager.STATUS_RUNNING
        );
        Cursor cur = mgr.query(query);
        int col = cur.getColumnIndex(
                DownloadManager.COLUMN_LOCAL_URI);
        int col_uri = cur.getColumnIndex(DownloadManager.COLUMN_URI);
        Log.d(TAG, "cursor size=" + cur.getCount());
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            String local_uri = cur.getString(col);
            if (local_uri == null) {
                String cur_uri = cur.getString(col_uri);
                if (cur_uri != null) {
                    isdownloading = isdownloading || (uri.toString().equals(cur_uri));
                }
            } else
                isdownloading = isdownloading || (local_uri.toString().equals(fileUri));
        }
        cur.close();
        return isdownloading;
    }

    protected void postMessage(final String message) {
        if (handler != null) handler.post(new Runnable() {
            public void run() {
                InputDialog.toast(message, Global.getAppContext()).show();
            }
        });
    }

    private boolean haveNetworkConnectionWifi() {
        boolean haveConnectedWifi = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase(WIFI))
                if (ni.isConnected())
                    haveConnectedWifi = true;

        }
        return haveConnectedWifi;
    }

    private boolean haveNetworkConnectionMobile() {
        boolean haveConnectedMobile = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase(MOBILE))
                if (ni.isConnected())
                    haveConnectedMobile = true;

        }
        return haveConnectedMobile;
    }

}
