package com.astro.dsoplanner.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;


import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.DMS;
import com.astro.dsoplanner.graph.cuv_helper.DSS;
import com.astro.dsoplanner.Global;


import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.R;
import com.astro.dsoplanner.SettingsActivity;

public class DSSdownloadable extends Downloadable {

    private static final String TEMP = "temp";
    public static final String F_GIF_C_NONE_FOV_NONE_V3 = "&f=gif&c=none&fov=NONE&v3=";
    public static final String HTTP_ARCHIVE_STSCI_EDU_CGI_BIN_DSS_SEARCH_V_POSS2UKSTU_RED_R = "https://archive.stsci.edu/cgi-bin/dss_search?v=poss2ukstu_red&r=";
    public static final String HTTP_ARCHIVE_STSCI_EDU_CGI_BIN_DSS_SEARCH_V_POSS2UKSTU_RED_R_OLD = "http://archive.stsci.edu/cgi-bin/dss_search?v=poss2ukstu_red&r=";

    private static final String COMPRESSION_ERROR = "Compression error";
    private static final String DECODING_ERROR = "Decoding error";

    private static final String TAG = DSSdownloadable.class.getSimpleName();
    private String fileName = "";
    private Point p;

    /**
     * @param p  - point to download
     * @param gh - handler from Graph Activity to update sky view
     */
    public DSSdownloadable(Point p) {//,Handler gh){
        super();
        this.p = p;
        url = makeUrl(AstroTools.normalise24(p.getRa()), p.getDec());
        fileName = makeName(DSS.size, AstroTools.normalise24(p.getRa()), p.getDec());
        f = new File(Global.DSSpath, TEMP);//first downloading to a temporary file
    }

    @Override
    protected void firstNotification() {
        boolean neg = (p.getDec() < 0);
        DMS r = AstroTools.d2dms(AstroTools.normalise24(p.getRa()));
        DMS d = AstroTools.d2dms(neg ? -p.getDec() : p.getDec());
        String loc = String.format("R/D:%02d %02.0f%s%02d %02.0f", r.d, r.m + (r.s / 60f + 0.5), (neg ? "-" : "+"), d.d, d.m + (d.s / 60f + 0.5));
        helper.createNotification(context.getString(R.string.downloading_dss_image), context.getString(R.string.dss_at_) + loc, context.getString(R.string.download_in_progress));
        notifyGraph(ADD_DOWNLOAD_TEXT);
    }


    @Override
    public void run() {
        //1 attempts to download
        for (int i = 0; i < 1; i++) {
            helper = new NotificationHelper2(context, "Att." + (i + 1) + ". ");//move out of cycle?
            boolean result = download();
            if (result) {
                boolean result2 = compressFile();

                if (result2) {
                    sendMessage(context.getString(R.string.download_complete));
                    notifyGraph(UPDATE_SKY);
                    helper.completed();
                    break;
                }
            }
            if (interrupted) break;
            if (i == 2)
                break;//no need to sleep at the last attempt, without this you could press on error notification and go to handling activity though this is superflous at this point
            NotificationHelper2 helper2 = new NotificationHelper2(context);//need to create a new one to send download interrupted message
            //so that it does not overwrite the main notification
            //need to sleep to be able to press on pull down menu
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                interrupted = true;
                sendMessage(context.getString(R.string.download_interrupted));
                helper2.interrupted();
            }
            if (interrupted) break;
            //need to sleep between unsuccessfull attempts hoping that the connection will become better
            try {
                Thread.currentThread().sleep(8000);
            } catch (InterruptedException e) {
                sendMessage(context.getString(R.string.download_interrupted));
                helper2.interrupted();
                break;
            }

        }
        notifyGraph(REMOVE_DOWNLOAD_TEXT);
        notifyService();
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
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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

    private boolean compressFile() {
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
                sendMessage(DECODING_ERROR);
                helper.error(DECODING_ERROR);
                return false;
            }
        } catch (Throwable t) {
            sendMessage(DECODING_ERROR);
            helper.error(DECODING_ERROR);
            return false;
        }

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(new File(Global.DSSpath, fileName));
            image.compress(Bitmap.CompressFormat.JPEG, 70, fos);

            DSS.addToFileList(p.getRa(), p.getDec(), fileName);

        } catch (Exception e) {
            sendMessage(COMPRESSION_ERROR);
            helper.error(COMPRESSION_ERROR);
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
        String s = SettingsActivity.getDSSdownloadUrl(Global.getAppContext());//HTTP_ARCHIVE_STSCI_EDU_CGI_BIN_DSS_SEARCH_V_POSS2UKSTU_RED_R;
        DMS dmsRa = AstroTools.d2dms(ra);
        DMS dmsDec = AstroTools.d2dms(dec);

        String dStr;
        if (dec < 0 && dec > -1) {
            dStr = "-0";
        } else
            dStr = String.valueOf(dmsDec.d);

        s = s + dmsRa.d + "+" + dmsRa.m + "+" + dmsRa.s + "&d=" + dStr + "+" + dmsDec.m + "+" + dmsDec.s + "&e=J2000&h=" + DSS.size + "&w=" + DSS.size + F_GIF_C_NONE_FOV_NONE_V3;
        return s;

    }

    private String makeName(int size, double ra, double dec) {
        String sRa = String.format(Locale.US, "%.5f", ra);
        sRa = sRa.replaceAll(",", ".");
        String sDec = String.format(Locale.US, "%.5f", dec);
        sDec = sDec.replaceAll(",", ".");
        return size + " " + sRa + " " + sDec;
    }

    /**
     * log purposes
     */
    private String getFileList() {
        File f = new File(Global.DSSpath);
        String[] list = f.list();
        String s = "";
        if (list != null) {
            for (String name : list) {
                s += name + "\n";
            }
        }
        return s;
    }
}
