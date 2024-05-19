package com.astro.dsoplanner.graph.cuv_helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.graph.CuV;
import com.astro.dsoplanner.util.Holder2;
import com.astro.dsoplanner.util.Holder3;
import com.astro.dsoplanner.download.DSSdownloadable;
import com.astro.dsoplanner.download.DownloadService;
import com.astro.dsoplanner.base.Point;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;


public class DSS {
    public static final int FREE_LIMIT = 160;
    private static final String TAG = DSS.class.getSimpleName();
    public final static int NUMBER_OF_FILES = 15;
    public final static int size = 30;//in min
    private volatile ConcurrentLinkedQueue<Holder2<Point, Integer>> mQueue = new ConcurrentLinkedQueue<Holder2<Point, Integer>>();//download queue. Point - position on the screen, Integer - the number of attempts
    public volatile static ConcurrentLinkedQueue<Holder3<Double, Double, String>> mFileList = new ConcurrentLinkedQueue<Holder3<Double, Double, String>>();
    private Thread dssThread = null;//download
    private Signal signal = new Signal();//for upload
    private Thread uploadThread = null;//upload
    private volatile boolean uploadDSSinterruption = false;//indicates that upload thread should end and clear image list

    private List<Holder3<Point, Bitmap, String>> dssList = new ArrayList<Holder3<Point, Bitmap, String>>();
    private int mPos = 0;//position in dssList

    class Monitor {
    }//used for interrupt sync

    final Monitor m = new Monitor();
    private boolean isInterrupted = false;//download thread interruption

    /**
     * adding downloaded file information to the file list
     */
    public static void addToFileList(double ra, double dec, String name) {
        mFileList.add(new Holder3<Double, Double, String>(ra, dec, name));
    }

    /**
     * @param name - the file to be deleted
     */
    public static void delete(String name) {
        for (Holder3<Double, Double, String> h : mFileList) {
            if (h.z.equals(name)) {
                mFileList.remove(h);
                File f = new File(Global.DSSpath, name);
                f.delete();
                break;
            }
        }

    }

    /**
     * called from UI thread
     */
    public synchronized void removeFromDssList(String name) {
        for (Holder3<Point, Bitmap, String> h : dssList) {
            if (h.z.equals(name)) {
                dssList.remove(h);
                break;
            }

        }
    }

    public static Collection<Holder3<Double, Double, String>> getDssCollection() {
        return mFileList;
    }

    public static List<AstroTools.RaDecRec> getDssRaDec() {
        List<AstroTools.RaDecRec> list = new ArrayList<AstroTools.RaDecRec>();
        for (Holder3<Double, Double, String> h : mFileList) {
            list.add(new AstroTools.RaDecRec(h.x, h.y));
        }
        return list;
    }

    public void interruptDownload() {
        synchronized (m) {
            if (dssThread != null) {
                if (dssThread.isAlive()) {
                    isInterrupted = true;
                }
            }
        }
    }

    private boolean getInterruptDownloadStatus() {
        synchronized (m) {
            return isInterrupted;
        }
    }

    private void clearInterruptStatus() {
        synchronized (m) {
            isInterrupted = false;
        }
    }

    //returns null if come to the end of list
    public synchronized Holder2<Bitmap, Point> next() {
        if (mPos < dssList.size()) {
            Bitmap bmp = dssList.get(mPos).y;
            mPos++;
            return new Holder2<Bitmap, Point>(bmp, dssList.get(mPos - 1).x);
        }
        return null;
    }

    //clears internal list position for the next reading
    public void clearPos() {
        mPos = 0;
    }

    //clears DSS list
    public void clearDssList() {
        if (uploadThread != null) {
            if (uploadThread.isAlive()) {
                uploadDSSinterruption = true;
                return;
            }
        }

        dssList = new ArrayList<Holder3<Point, Bitmap, String>>();
        Log.d(TAG, "DSS list cleared from main thread");


    }

    public DSS() {

    }

    public static Holder2<Double, Double> getRaDec(String name) {

        try {
            String parts[] = name.split(" ");
            double ra = AstroTools.getDouble(parts[1], 0, 0, 24);
            double dec = AstroTools.getDouble(parts[2], 0, -90, 90);
            return new Holder2<Double, Double>(ra, dec);
        } catch (Exception e) {
            return null;
        }
    }

    public void uploadDSS(UploadRec u, View v, Handler h) {
        signal.setRec(u);
        if (uploadThread != null) {
            if (uploadThread.isAlive()) {
                Log.d(TAG, "upload thread alive");
                return;
            }
        }
        uploadThread = new Thread(new DSSupload(v, h));
        uploadThread.start();
        Log.d(TAG, "upload thread started");


    }

    class Signal {
        private UploadRec u = null;

        public synchronized UploadRec getRec() {
            return u;
        }

        public synchronized void setRec(UploadRec u) {
            this.u = u;
        }
    }

    class DSSupload implements Runnable {
        double azCenter;
        double altCenter;
        double raCenter;
        double decCenter;
        double FOV;
        double height;
        double width;
        boolean raiseNewPointFlag;
        boolean newFOV;
        Handler handler;

        public DSSupload(View v, Handler handler) {
            this.handler = handler;
        }

        public void run() {
            boolean interruption = false;
            while ((signal.getRec() != null) && (!uploadDSSinterruption)) {
                UploadRec u = signal.getRec();
                azCenter = u.azCenter;
                altCenter = u.altCenter;
                raCenter = u.raCenter;
                decCenter = u.decCenter;
                FOV = u.FOV;
                height = u.height;
                width = u.width;
                raiseNewPointFlag = u.raiseNewPointFlag;
                newFOV = u.newFOV;
                signal.setRec(null);
                Log.d(TAG, "upload thread, uploadrec=" + u);


                double raC = Point.getRa(Point.getAzCenter(), Point.getAltCenter());
                double decC = Point.getDec(Point.getAzCenter(), Point.getAltCenter());
                double hw = (double) Point.getHeight() / (double) Point.getWidth();
                if (hw < 1) hw = 1 / hw;

                double stepdec = Point.getFOV() / 2 * hw;//Point.getHeight()/Point.getWidth();

                Set<Holder2<Point, String>> setUp = new HashSet<Holder2<Point, String>>();//upload needed
                Set<Holder2<Point, String>> setBmp = new HashSet<Holder2<Point, String>>();//what needs to be read from file
                double costhreshold = cos((stepdec + 2 * DSS.size / 60f) * PI / 180);

                for (Holder3<Double, Double, String> h3 : mFileList) {


                    double ra = h3.x;
                    double dec = h3.y;

                    double cosDist = Point.tfun.sin(decC * PI / 180) * Point.tfun.sin(dec * PI / 180) + Point.tfun.cos(decC * PI / 180) * Point.tfun.cos(dec * PI / 180) * Point.tfun.cos((raC - ra) * PI / 12);

                    if (cosDist > costhreshold) { //upload needed
                        Log.d(TAG, "upload bitmap added");
                        setUp.add(new Holder2<Point, String>(new Point(ra, dec), h3.z));
                    }

                }
                synchronized (DSS.this) {
                    Iterator<Holder3<Point, Bitmap, String>> it = dssList.iterator();
                    while (it.hasNext()) {
                        Holder3<Point, Bitmap, String> h = it.next();
                        boolean inset = false;
                        for (Holder2<Point, String> hs : setUp) {
                            if (hs.y.compareTo(h.z) == 0) {
                                inset = true; //the list element is in the upload set
                                break;
                            }
                        }
                        if (!inset)
                            it.remove(); //remove the list element if it is not in the upload set
                    }
                    //what needs to be read from file
                    for (Holder2<Point, String> hs : setUp) {
                        boolean inlist = false;
                        for (Holder3<Point, Bitmap, String> h : dssList) {
                            if (hs.y.compareTo(h.z) == 0) {
                                inlist = true;//there is a bitmap in the List
                                break;
                            }
                        }
                        if (!inlist) setBmp.add(hs);
                    }
                }
                String absname = Global.DSSpath;
                for (Holder2<Point, String> hs : setBmp) {
                    Bitmap image = null;
                    try {
                        image = BitmapFactory.decodeFile(absname + File.separator + hs.y);
                    } catch (Throwable t) {
                        Log.d(TAG, "bitmap error=" + t);
                    }

                    if (image != null && dssList.size() <= NUMBER_OF_FILES) {
                        synchronized (DSS.this) {
                            dssList.add(new Holder3<Point, Bitmap, String>(hs.x, image, hs.y));
                        }
                    }
                }
                Log.d(TAG, "Bitmap list size=" + dssList.size());
                Message msg = Message.obtain();
                msg.arg1 = CuV.INVALIDATE;
                handler.sendMessage(msg);


            }
            if (uploadDSSinterruption) synchronized (DSS.this) {
                dssList = new ArrayList<Holder3<Point, Bitmap, String>>();
                mPos = 0;
                Log.d(TAG, "DSS list cleared from upload thread");
                uploadDSSinterruption = false;
            }
            handler = null;
        }
    }


    public void downloadDSSimages(Point p, View v, DownloadService service) {
        if (abs(p.dec) > 87) return;
        double stepRa = size / 60f * 24f / 360f / cos(p.dec * PI / 180);
        double stepRa0 = size / 60f * 24f / 360f / cos((p.dec - size / 60f) * PI / 180);
        double stepRa1 = size / 60f * 24f / 360f / cos((p.dec + size / 60f) * PI / 180);
        double stepdec = size / 60f;

        downloadDSS(new Point(p.ra, p.dec), v, service);
        downloadDSS(new Point(p.ra - stepRa, p.dec), v, service);
        downloadDSS(new Point(p.ra + stepRa, p.dec), v, service);
        downloadDSS(new Point(p.ra, p.dec - stepdec), v, service);
        downloadDSS(new Point(p.ra - stepRa0, p.dec - stepdec), v, service);
        downloadDSS(new Point(p.ra + stepRa0, p.dec - stepdec), v, service);
        downloadDSS(new Point(p.ra, p.dec + stepdec), v, service);
        downloadDSS(new Point(p.ra - stepRa1, p.dec + stepdec), v, service);
        downloadDSS(new Point(p.ra + stepRa1, p.dec + stepdec), v, service);
    }

    public void downloadDSS(Point p, View v, DownloadService service) {

        if (service == null) return;

        DSSdownloadable d = new DSSdownloadable(p);
        service.addDownloadable(d);

    }


}
