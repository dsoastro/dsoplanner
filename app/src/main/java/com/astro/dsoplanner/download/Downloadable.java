package com.astro.dsoplanner.download;

import java.io.File;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.Adler32;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.astro.dsoplanner.AstroTools;

/**
 * set handler and context in service before using
 */

public class Downloadable implements Runnable {


    private static final String DOWNLOAD_INTERRUPTED = "Download interrupted";
    private static final String DOWNLOAD_ERROR = "Download error";
    private static final String ERROR_DOWNLOADING = "Error downloading";
    private static final String DOWNLOAD_IN_PROGRESS = "Download in progress";
    private static final String DOWNLOAD = " download";
    private static final String DSO_FILE = "DSO file ";
    private static final String DOWNLOADING_DSO_FILE = "downloading DSO file";
    private static final String DOWNLOAD_COMPLETE = "Download complete";


    private static final String TAG = Downloadable.class.getSimpleName();
    protected File f;
    protected String url;
    private Handler handler;//service handler
    protected NotificationHelper2 helper;
    protected Context context;
    protected boolean interrupted = false;
    private boolean adler32Flag = false;//if calculate adler 32
    private long adler32checksum;

    /**
     * do not forget to initialise fields when using this constructor
     */
    protected Downloadable() {
    }

    public Downloadable(File f, String url) {

        this.f = f;
        this.url = url;
    }

    /**
     * sets flag for calculation of checksum
     */
    protected void setCheckSumFlag() {
        adler32Flag = true;
    }

    protected long getCheckSum() {
        return adler32checksum;
    }

    public void run() {
        helper = new NotificationHelper2(context);
        boolean result = download();
        if (result) {
            sendMessage(DOWNLOAD_COMPLETE);
            helper.completed();
        }


        notifyService();
    }

    /**
     * @param handler - service handler to handle messages
     */
    protected void setServiceHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * @param context - service context
     */
    protected void setContext(Context context) {
        this.context = context;
    }

    protected void sendMessage(String s) {
        Message m = new Message();
        m.arg1 = 1;
        m.obj = s;
        handler.sendMessage(m);
    }

    /**
     * notification that a Thread stopped working
     */
    protected void notifyService() {
        Message m = new Message();
        m.arg1 = 2;
        handler.sendMessage(m);
    }

    protected void firstNotification() {
        helper.createNotification(DOWNLOADING_DSO_FILE, DSO_FILE + f.getName() + DOWNLOAD, DOWNLOAD_IN_PROGRESS);
    }

    protected void progressNotification(int percent) {
        helper.progressUpdate(percent);
    }

    protected boolean download() { //returns result of download operation


        interrupted = false;
        URLConnection conn = null;
        FileOutputStream fos = null;
        InputStream is = null;
        Adler32 adler32 = new Adler32();
        try {
            firstNotification();

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
                if (Thread.currentThread().isInterrupted()) {
                    interrupted = true;
                    helper.interrupted();
                    break;
                }
                if (adler32Flag) {
                    adler32.update(buffer, 0, length);
                }

                fos.write(buffer, 0, length);
                total += length;
                int percent = (int) ((total * 100) / lenghtOfFile);
                if (percent - previousPercent >= 10 || percent == 100) {//not to spoil bar with updates

                    previousPercent = percent;
                    progressNotification(percent);
                }
            }


            //Close the streams
            fos.flush();
        } catch (Exception e) {
            f.delete();
            Log.d(TAG, "point D, exception=" + e.getMessage());

            sendMessage(ERROR_DOWNLOADING);
            helper.error(DOWNLOAD_ERROR);

            return false;
        } finally {
            try {
                is.close();
                fos.close();
            } catch (Exception e) {
            }
        }
        if (interrupted) {
            sendMessage(DOWNLOAD_INTERRUPTED);
            helper.interrupted();
            f.delete();
            return false;
        }
        adler32checksum = adler32.getValue();
        return true;
    }

}
