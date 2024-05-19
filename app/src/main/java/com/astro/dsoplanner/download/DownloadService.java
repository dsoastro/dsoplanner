package com.astro.dsoplanner.download;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.astro.dsoplanner.InputDialog;

import java.util.ArrayList;
import java.util.List;

public class DownloadService extends Service {
    private static final String TAG = DownloadService.class.getSimpleName();
    private List<Downloadable> mList;
    private Thread thread;
    private Handler handler;
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public DownloadService getService() {
            // Return this instance of LocalService so clients can call public methods
            return DownloadService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "service created");
        mList = new ArrayList<Downloadable>();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //message
                if (msg.arg1 == 1) {
                    String message = (String) msg.obj;
                    InputDialog.toast(message, DownloadService.this).show();
                    return;
                }
                //thread has ended working
                if (msg.arg1 == 2) {
                    startNewDownload();
                }
            }
        };
    }

    public void addDownloadable(Downloadable d) {
        mList.add(d);
        if (thread == null || !thread.isAlive()) {
            startNewDownload();
        }
    }

    public boolean isDownloadInProgress() {
        if (mList.size() > 0)
            return true;
        return thread != null && thread.isAlive();
    }

    public void cancelDownloads() {
        mList = new ArrayList<Downloadable>();
        if (thread != null) {
            if (thread.isAlive())
                thread.interrupt();
            else {
                NotificationHelper2 helper = new NotificationHelper2(this);
                helper.interrupted();
            }
        }
    }

    private void startNewDownload() {
        if (mList.size() > 0) {
            thread = new Thread(mList.get(0));
            init(mList.get(0));
            mList.remove(0);
            thread.start();
        }
    }

    private void init(Downloadable d) {
        d.setContext(DownloadService.this);
        d.setServiceHandler(handler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "service started");
        return START_STICKY;
    }
}
