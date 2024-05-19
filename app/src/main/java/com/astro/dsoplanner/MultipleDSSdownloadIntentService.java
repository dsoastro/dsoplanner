package com.astro.dsoplanner;


import static java.lang.Math.PI;
import static java.lang.Math.cos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.Adler32;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.astro.dsoplanner.AstroTools.RaDecRec;
import com.astro.dsoplanner.download.DSSdownloadable;
import com.astro.dsoplanner.download.GifDecoder;
import com.astro.dsoplanner.graph.cuv_helper.DSS;

public class MultipleDSSdownloadIntentService extends IntentService {
    private static final String TAG = MultipleDSSdownloadIntentService.class.getSimpleName();
    private static int NOTIFICATION_ID = 1238;
    private NotificationHelper nh;

    public MultipleDSSdownloadIntentService() {
        super("0");
    }

    private static boolean stopflag = false;

    public static synchronized void stop() {
        stopflag = true;
    }

    private static synchronized void clearFlag() {
        stopflag = false;
    }

    private static synchronized boolean getFlag() {
        return stopflag;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStart, intent=" + intent);
        nh = new NotificationHelper(this);
        startForeground(NOTIFICATION_ID, nh.createNotification(getString(R.string.dss_images_download), getString(R.string.dss_images_download)));

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        nh.clearContent();//for more than 1 download

        File path = new File(Global.DSSpath);

        double[] radec = intent.getDoubleArrayExtra(Constants.MDSS_DATA);
        if (radec == null) {
            nh.completed();
            return;
        }


        List<data> list = getDownloadList(radec);
        Log.d(TAG, "list size=" + list.size());


        int count = list.size();
        int i = 0;
        for (data rec : list) {
            double ra = AstroTools.normalise24(rec.ra);
            double dec = rec.dec;

            String url = makeUrl(ra, dec);
            String fn = makeName(DSS.size, ra, dec);
            File f = new File(path, fn);


            f = new File(path, "temp");//first downloading to a temporary file
            boolean result = download(url, f);
            boolean result2 = false;
            if (result) {
                result2 = compressFile(f, fn);
                if (result2) {
                    DSS.addToFileList(ra, dec, fn);
                    notifyGraph(DSSdownloadable.UPDATE_SKY);
                }
            }
            String message = "";


            nh.progressUpdate((int) (100f * ++i / count));
            if (getFlag()) {
                clearFlag();
                break;
            }
        }
        nh.completed();


    }

    class data {
        double ra;
        double dec;
        double a;

        public data(double ra, double dec, double a) {
            super();
            this.ra = ra;
            this.dec = dec;
            this.a = a;
        }

    }

    private List<data> getDownloadList(double[] data) {

        List<RaDecRec> pics = DSS.getDssRaDec();
        List<data> list = new ArrayList<data>();
        for (int i = 0; i < data.length; i += 3) {
            double ra = AstroTools.normalise24(data[i]);
            double dec = data[i + 1];
            double a = data[i + 2];
            if (Double.isNaN(a))
                a = 0;
            boolean matchfound = false;
            for (RaDecRec h : pics) {
                double ra2 = h.ra;
                double dec2 = h.dec;
                double cosdst = Math.sin(dec * PI / 180) * Math.sin(dec2 * PI / 180) +
                        cos(dec * PI / 180) * cos(dec2 * PI / 180) * cos((ra - ra2) * PI / 12);

                double limit = (DSS.size / 2 - Math.min(a, DSS.size / 2)) / 60;
                if (cosdst >= cos(limit * PI / 180) - 0.0000000001) {
                    matchfound = true;

                    break;
                }
            }
            if (!matchfound) {
                Log.d(TAG, "non matchfound " + ra + " " + dec);
                list.add(new data(ra, dec, a));
            }
        }

        List<data> list2 = new ArrayList<data>();
        //removing objects close to each other among remainig points
        for (data d1 : list) {
            boolean match = false;
            for (data d2 : list2) {
                double cosdst = Math.sin(d1.dec * PI / 180) * Math.sin(d2.dec * PI / 180) +
                        cos(d1.dec * PI / 180) * cos(d2.dec * PI / 180) * cos((d1.ra - d2.ra) * PI / 12);
                double limit = (DSS.size / 2 - Math.min(d1.a, DSS.size / 2)) / 60;
                if (cosdst > cos(limit * PI / 180) - 0.0001) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                list2.add(d1);
            }
        }
        return list2;
    }


    public static final int UPDATE_SKY = 2;
    public static final int REMOVE_DOWNLOAD_TEXT = 3;
    public static final int ADD_DOWNLOAD_TEXT = 4;

    /**
     * notifying graph activity to update sky view
     */
    private void notifyGraph(int status) {

        Intent intent = new Intent(Constants.DOWNLOAD_BROADCAST);
        intent.putExtra(Constants.GRAPH_DOWNLOAD_STATUS, status);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private Bitmap customResize(File f) {
        GifDecoder dec = new GifDecoder();
        Bitmap b = null;
        try {
            InputStream is = new FileInputStream(f);
            int status = dec.read(is);
            if (status != 0) return null;
            b = dec.getBitmap();


        } catch (Exception e) {
            return null;
        }
        return b;
    }

    /**
     * @param f        file to compress
     * @param fileName final file
     * @return
     */
    private boolean compressFile(File f, String fileName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap image = null, image1 = null;
        try {

            image1 = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
            if (image1 == null) {

                image1 = customResize(f);
            }

            if (image1 != null) {

                image = Bitmap.createScaledBitmap(image1, 600, 600, false);
                image1.recycle();
                image1 = null;
            }
            if (image == null) {

                return false;
            }
        } catch (Throwable t) {

            return false;
        }


        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(new File(Global.DSSpath, fileName));
            image.compress(Bitmap.CompressFormat.JPEG, 70, fos);

        } catch (Exception e) {

            return false;
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
        return true;
    }

    private String makeUrl(double ra, double dec) {
        String s = SettingsActivity.getDSSdownloadUrl(this);
        DMS dmsRa = AstroTools.d2dms(ra);
        DMS dmsDec = AstroTools.d2dms(dec);

        String dStr;
        if (dec < 0 && dec > -1) {
            dStr = "-0";
        } else
            dStr = String.valueOf(dmsDec.d);

        s = s + dmsRa.d + "+" + dmsRa.m + "+" + dmsRa.s + "&d=" + dStr + "+" + dmsDec.m + "+" + dmsDec.s + "&e=J2000&h=" + DSS.size + "&w=" + DSS.size + DSSdownloadable.F_GIF_C_NONE_FOV_NONE_V3;
        return s;

    }

    private String makeName(int size, double ra, double dec) {
        String sRa = String.format(Locale.US, "%.5f", ra);
        sRa = sRa.replaceAll(",", ".");
        String sDec = String.format(Locale.US, "%.5f", dec);
        sDec = sDec.replaceAll(",", ".");
        return size + " " + sRa + " " + sDec;
    }

    protected boolean download(String url, File f) { //returns result of download operation


        URLConnection conn = null;
        FileOutputStream fos = null;
        InputStream is = null;
        Adler32 adler32 = new Adler32();
        try {

            conn = new URL(url).openConnection();
            conn.setReadTimeout(60000);//60s
            conn.setConnectTimeout(60000);
            conn.connect();


            is = conn.getInputStream();
            long lenghtOfFile = conn.getContentLength();

            fos = new FileOutputStream(f);

            byte[] buffer = new byte[1024];
            int length;
            long total = 0;
            int previousPercent = 0;

            while ((length = is.read(buffer)) > 0) {

                fos.write(buffer, 0, length);

            }


            fos.flush();


        } catch (Exception e) {
            f.delete();


            return false;
        } finally {
            try {
                is.close();
                fos.close();
            } catch (Exception e) {
            }
        }

        return true;
    }

    private static class NotificationHelper {
        static protected int COMPLETE_NOTIFICATION_ID = 1;
        protected NotificationManager mNotificationManager;
        Context context;

        public NotificationHelper(Context context) {
            this.context = context;
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String NOTIFICATION_CHANNEL_ID = "com.astro.dsoplanner";
                String channelName = "DSOPlanner background service";
                NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
                try {
                    mNotificationManager.createNotificationChannel(chan);
                } catch (Exception e) {
                }
                mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            }
        }


        NotificationCompat.Builder mBuilder;
        String title = "";

        String pullDownText;
        String pullDownTitle;

        public Notification createNotification(String statusBarTitle, String pullDownTitle) {//,String filename) {
            Intent notifyIntent = new Intent();

            PendingIntent intent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_IMMUTABLE);
            this.pullDownTitle = pullDownTitle;
            mBuilder.setTicker(statusBarTitle).setContentTitle(pullDownTitle).setContentText(pullDownText).
                    setSmallIcon(android.R.drawable.stat_sys_download).setOngoing(true).setContentIntent(intent);
            try {
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            } catch (Exception e) {
            }
            return mBuilder.build();

        }

        public void setFileName(String name) {
            pullDownText = name;

        }

        public void progressUpdate(int percent) {
            mBuilder.setProgress(100, percent, false);
            try {
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            } catch (Exception e) {
            }
        }

        public void clearContent() {
            mBuilder.setContentText("");
            try {
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            } catch (Exception e) {
            }
        }

        public void completed() {
            Intent notificationIntent = new Intent();
            PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder mBuilderCompleted = new NotificationCompat.Builder(context);
            mBuilderCompleted.setContentText(context.getString(R.string.dss_download_complete)).setSmallIcon(android.R.drawable.stat_sys_download_done).
                    setOngoing(false).setContentTitle(pullDownTitle).setContentIntent(intent);
            try {
                mNotificationManager.notify(COMPLETE_NOTIFICATION_ID++, mBuilderCompleted.build());
            } catch (Exception e) {
            }
        }


        public void error(String pullDownText) {
            Intent notificationIntent = new Intent();
            PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder mBuilderError = new NotificationCompat.Builder(context);
            mBuilderError.setContentText(pullDownText).setSmallIcon(android.R.drawable.stat_notify_error).//stat_sys_download_done).
                    setOngoing(false).setContentTitle(pullDownTitle).setContentIntent(intent);
            try {
                mNotificationManager.notify(COMPLETE_NOTIFICATION_ID++, mBuilderError.build());
            } catch (Exception e) {
            }
        }
    }

}
