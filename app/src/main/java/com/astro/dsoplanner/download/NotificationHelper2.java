package com.astro.dsoplanner.download;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;


import com.astro.dsoplanner.R;

public class NotificationHelper2 {

    static protected int NOTIFICATION_ID = 1;
    protected NotificationManager mNotificationManager;
    Context context;

    public NotificationHelper2(Context context) {

        NOTIFICATION_ID++;
        this.context = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
    }

    String prefix = null;

    /**
     * @param context
     * @param prefix  - prefix before name in pulldownTitle
     *                used in DSS downloadable
     */
    public NotificationHelper2(Context context, String prefix) {
        this(context);
        this.prefix = prefix;
    }

    NotificationCompat.Builder mBuilder;
    String title = "";

    public void createNotification(String statusBarTitle, String pullDownTitle, String pullDownText) {
        Intent notificationIntent = new Intent();
        title = (prefix != null ? prefix + pullDownTitle : pullDownTitle);
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        mBuilder.setTicker(statusBarTitle).setContentTitle(title).setContentText(pullDownText).
                setSmallIcon(android.R.drawable.stat_sys_download).setContentIntent(intent);
        try {
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
        }

    }

    public void progressUpdate(int percent) {
        mBuilder.setProgress(100, percent, false);
        try {
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
        }
    }


    public void completed() {
        mBuilder.setContentText(context.getString(R.string.download_complete)).setSmallIcon(android.R.drawable.stat_sys_download_done).setProgress(0, 0, false);
        try {
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
        }
    }


    public void interrupted() {
        NotificationCompat.Builder b = new NotificationCompat.Builder(context);//this is to eliminate pending intent
        Intent notificationIntent = new Intent();
        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        //this is to fix bug in android 2.2
        b.setContentTitle(context.getString(R.string.downloads_interrupted)).setSmallIcon(android.R.drawable.stat_notify_error).setContentIntent(intent);
        try {
            mNotificationManager.notify(NOTIFICATION_ID, b.build());
        } catch (Exception e) {
        }
    }

    public void error(String pullDownText) {
        mBuilder.setProgress(0, 0, false).setContentText(pullDownText).setSmallIcon(android.R.drawable.stat_notify_error);
        try {
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } catch (Exception e) {
        }
    }
}
