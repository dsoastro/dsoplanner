package com.astro.dsoplanner.graph;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.graph.cuv_helper.UploadRec;
import com.astro.dsoplanner.util.Holder2;

public class StarUploadThread extends Thread {
    private static final String TAG = StarUploadThread.class.getSimpleName();
    private static final int UCAC4_NUM_LIMIT = 20000;
	
    public static interface Filter {
        public double getMagLimit(double FOV);

        public double getDistanceParam(double FOV);
    }

    public static class TychoFilter implements Filter {
        public double getMagLimit(double FOV) {
            double mag_limit = 14;
            mag_limit = StarMags.getMagLimit(StarMags.TYCHO, FOV);
            return mag_limit;
        }

        public double getDistanceParam(double FOV) {
            if (FOV == 10)
                return 6;
            else
                return 3;
        }
    }

    public static class TychoFilterShort implements Filter {
        public double getMagLimit(double FOV) {
            double mag_limit = 14;
            mag_limit = StarMags.getMagLimit(StarMags.TYCHO, FOV);
            return mag_limit;
        }

        public double getDistanceParam(double FOV) {
            if (FOV > 45.1) return 12;
            return 6;
        }
    }

    public static class UcacFilter implements Filter {
        public double getMagLimit(double FOV) {
            double mag_limit = 18;
            mag_limit = StarMags.getMagLimit(StarMags.UCAC, FOV);
            return mag_limit;
        }

        public double getDistanceParam(double FOV) {
            if (FOV > 2.1)
                return 2;
            else
                return 1;
        }
    }

    public static class PgcFilter implements Filter {
        public double getMagLimit(double FOV) {
            double mag_limit = 25;
            mag_limit = StarMags.getMagLimit(StarMags.PGC, FOV);
            return mag_limit;
        }

        public double getDistanceParam(double FOV) {
            if (FOV > 20.1)
                return 6;
            else if (FOV > 10.1)
                return 4;
            else
                return 3;

        }
    }

    public static class ConBoundaryFilter implements Filter {
        public double getMagLimit(double FOV) {
            double mag_limit = 25;


            return mag_limit;
        }

        public double getDistanceParam(double FOV) {
            return 20;

        }
    }

    public static class NgcFilter implements Filter {
        public double getMagLimit(double FOV) {
            double mag_limit = 25;
            mag_limit = StarMags.getMagLimit(StarMags.NGC, FOV);
            return mag_limit;
        }

        public double getDistanceParam(double FOV) {
            return 10;//for low fov we need high parameter as standard quadrant size is large!

        }
    }

    static float D2R = (float) (PI / 180);
    static float R2D = 1 / D2R;

    public final static CacheManager cacheManager = new CacheManager();

    public static class CacheManager {

        private CacheManager() {

        }

        private static final int OBJECT_LIMIT = 30000;
        //Object is of class Map<Integer,List<AstroObject>>
        Map<Integer, Map<Integer, List<Point>>> mapHolder = new HashMap<Integer, Map<Integer, List<Point>>>();

        private static boolean clearcache = false;
        private static int cctthreadid = -1;

        /**
         * clears current cache and does not allow next update
         *
         * @param threadId
         */
        public synchronized void clearCacheAndSetFlag(int threadId) {
            Map<Integer, List<Point>> map = new HashMap<Integer, List<Point>>();
            mapHolder.put(threadId, map);
            clearcache = true;
            cctthreadid = threadId;
        }

        /**
         * just clears cache of a specified thread
         *
         * @param threadId
         */
        public synchronized void clear(int threadId) {
            Map<Integer, List<Point>> map = new HashMap<Integer, List<Point>>();
            mapHolder.put(threadId, map);
        }

        /**
         * @param threadId
         * @return empty map if there is none
         */
        private synchronized Map<Integer, List<Point>> get(int threadId) {
            if (!running)
                return new HashMap<Integer, List<Point>>();

            Map<Integer, List<Point>> map = mapHolder.get(threadId);
            if (map == null) {
                map = new HashMap<Integer, List<Point>>();
                mapHolder.put(threadId, map);
            }
            return map;
        }

        /**
         * signals that new data was put into map
         *
         * @param threadId
         */
        public synchronized void update(int threadId) {
            if (!running)
                return;

            if (clearcache && threadId == cctthreadid) {
                clearcache = false;
                cctthreadid = -1;
                Map<Integer, List<Point>> map = new HashMap<Integer, List<Point>>();
                mapHolder.put(threadId, map);
                Log.d(TAG, "not putting in the map");
                return;
            }

        }

        private void trim(Map<Integer, List<Point>> map, int maxsize, int threadid) {
            synchronized (map) {
                int count = getCount(map);//potential racing condition because of 2 sync blocks?
                int j = 0;
                while (count > maxsize && j++ < 300) {
                    Iterator<Integer> it = map.keySet().iterator();
                    Integer key = null;
                    for (int i = 0; it.hasNext() && i < 1; i++) {
                        key = it.next();
                    }
                    if (key != null) {
                        List<Point> list = map.remove(key);
                        Log.d(TAG, "removing " + list.size() + " objects from " + key + " quadrant" + " threadid=" + threadid);

                    }
                    count = getCount(map);
                }
            }
        }

        private int sumByValue(Map<Integer, Integer> map) {
            int count = 0;
            for (Map.Entry<Integer, Integer> e : map.entrySet()) {
                count += e.getValue();
            }
            return count;
        }

        /**
         * @return map (threadid count)
         */
        private Map<Integer, Integer> getObjectCount() {
            Map<Integer, Integer> mapc = new HashMap<Integer, Integer>();
            for (Map.Entry<Integer, Map<Integer, List<Point>>> e : mapHolder.entrySet()) {
                Map<Integer, List<Point>> map = e.getValue();
                mapc.put(e.getKey(), getCount(map));
            }
            return mapc;
        }

        private int getCount(Map<Integer, List<Point>> map) {
            int count = 0;
            synchronized (map) {
                for (Map.Entry<Integer, List<Point>> me : map.entrySet()) {
                    count += me.getValue().size();
                }
            }
            return count;
        }

        private boolean running;

        /**
         * call it before using this class (onResume)
         */
        public synchronized void init() {
            Log.d(TAG, "init");
            running = true;
            mapHolder = new HashMap<Integer, Map<Integer, List<Point>>>();
        }

        /**
         * call it after work is over to clear data and not accept data from still running threads
         */
        public synchronized void stop() {
            Log.d(TAG, "stop");
            running = false;
            //all links to data are lost
            mapHolder = new HashMap<Integer, Map<Integer, List<Point>>>();

        }
    }

    Handler handler;
    int threadId;
    StarFactory factory;
    Filter filter;
    Quadrant[] quadrants;
    private List<UploadRec> uploadlist = new ArrayList<UploadRec>();//
    ArrayList<Point> uploadStarList = new ArrayList<Point>();

    /**
     * @param handler  - custom view handler
     * @param threadId - id to be used as arg1 in the message sent to the handler
     *                 map - already existing map of quadrant to the list
     */
    public StarUploadThread(Handler handler, int threadId,
                            StarFactory factory, Filter filter,
                            Quadrant[] quadrants) {
        super();
        this.handler = handler;
        this.threadId = threadId;

        this.factory = factory;
        this.filter = filter;
        this.quadrants = quadrants;
    }

    /**
     * @param handler
     * @param threadId
     * @param factory
     * @param filter
     * @param quadrants
     * @param clearmap  flag that the cash needs to be cleared (eg mag limit has changed)
     */
    public StarUploadThread(Handler handler, int threadId,
                            StarFactory factory, Filter filter,
                            Quadrant[] quadrants, boolean clearmap) {//actually clearmap not used as map is initialised in run again
        super();
        this.handler = handler;
        this.threadId = threadId;

        this.factory = factory;
        this.filter = filter;
        this.quadrants = quadrants;
    }

    /**
     * @param rec - to be added before running the thread!!!
     */
    public synchronized void addUnpload(UploadRec rec) {
        uploadlist.add(rec);
    }

    public synchronized UploadRec nextUpload() {
        Map<Integer, List<Point>> map = cacheManager.get(threadId);


        for (UploadRec rec : uploadlist) {
            if (rec.raiseNewPointFlag) {//sgr was changed at least once
                synchronized (rec) {


                    for (Map.Entry<Integer, List<Point>> entry : map.entrySet()) {
                        for (Point obj : entry.getValue()) {
                            obj.raiseNewPointFlag();
                        }
                    }
                }
                break;
            }

        }
        for (UploadRec rec : uploadlist) {
            if (rec.newFOV || rec.clearcache) {
                cacheManager.clear(threadId);
                break;
            }


        }


        if (uploadlist.size() > 0) {
            UploadRec rec = uploadlist.get(uploadlist.size() - 1);
            uploadlist = new ArrayList<UploadRec>();
            return rec;
        } else {
            return null;
        }
    }

    private void printMap(Map<Integer, List<Point>> map) {
        for (Map.Entry<Integer, List<Point>> entry : map.entrySet()) {
            //Log.d(TAG,"quadrant="+entry.getKey()+" size="+entry.getValue().size());
        }
    }

    public void run() {
        UploadRec u;
        try {
            factory.open();
            Message msg = Message.obtain();
            msg.arg1 = CuV.LOADING;
            handler.sendMessage(msg);

            while ((u = nextUpload()) != null) {
                Set<Integer> s = new HashSet<Integer>();
                double stepra = u.FOV * 12 / 180 / 2 * u.height / u.width;
                double co = 1;
                if (abs(co) > 0.01)
                    stepra = (float) (stepra / co);
                double hw = u.height / u.width;
                if (hw < 1) hw = 1 / hw;
                double stepdec = u.FOV / 2 * hw;
                double mag_limit = filter.getMagLimit(u.FOV);
                int l = 0;
                double distparam = filter.getDistanceParam(u.FOV);

                Map<Integer, List<Point>> map = cacheManager.get(threadId);

                boolean found = false;
                for (Quadrant q : quadrants) {
                    double dist = q.cosDist(u.raCenter, u.decCenter);

                    if (!found && q.inQuadrant(u.raCenter, u.decCenter)) {
                        s.add(l);
                        found = true;
                    }

                    if (u.FOV > CuV.FOV_181) {
                        if (dist > CuV.COS_THRESHOLD_181)
                            s.add(l);

                    } else if (u.FOV > CuV.FOV_149) {
                        if (dist > CuV.COS_THRESHOLD_149)
                            s.add(l);
                    } else if (dist > cos((stepdec + distparam) * D2R))
                        s.add(l);
                    l++;
                }

                boolean use_dist = false;
                if (!(Global.BASIC_VERSION) & threadId == CuV.UCAC4_THREAD_ID && u.FOV < 0.3 && getUcac4Size(s) > UCAC4_NUM_LIMIT) {
                    use_dist = true;
                }


                if (!use_dist && threadId == CuV.UCAC4_THREAD_ID) {
                    mag_limit = getUcac4MagLimit(s, u.FOV);
                    Log.d(TAG, "q to be shown=" + s);
                }

                Set<Integer> sf = new HashSet<Integer>(s);        //see idea of incremental download in uploadstars 3

                if (use_dist) {
                    for (Integer q : s) {
                        List<Point> list = new ArrayList<Point>();
                        list.addAll(
                                ((Ucac4StarFactory) factory).get(q, mag_limit, u.raCenter, u.decCenter, u.FOV));
                        uploadStarList.addAll(list);
                    }
                    Log.d(TAG, "upload size=" + uploadStarList.size());
                } else {
                    synchronized (map) {
                        Set<Integer> ts = new HashSet<Integer>(map.keySet());

                        sf.removeAll(ts);
                        if (threadId == CuV.CON_BOUNDARY_THREAD_ID)
                            Log.d(TAG, "Thread=" + threadId + " q to be loaded=" + sf);

                        for (Integer i : ts) {
                            if (!s.contains(i))
                                map.remove(i);
                        }
                        if (threadId == CuV.CON_BOUNDARY_THREAD_ID)
                            Log.d(TAG, "q to be taken from map=" + map.keySet());


                        for (Map.Entry<Integer, List<Point>> me : map.entrySet()) {
                            uploadStarList.addAll(me.getValue());
                        }
                    }

                    Iterator<Integer> it = sf.iterator();//sf

                    Map<Integer, List<Point>> newdata = new HashMap<Integer, List<Point>>();
                    while (it.hasNext()) {
                        int quadrant = it.next();
                        List<Point> list = new ArrayList<Point>();

                        list.addAll(factory.get(quadrant, mag_limit));
                        uploadStarList.addAll(list);
                        newdata.put(quadrant, list);
                    }
                    if (threadId == CuV.CON_BOUNDARY_THREAD_ID)
                        Log.d(TAG, "Thread=" + threadId + " added all=" + uploadStarList.size());

                    synchronized (map) {
                        map.putAll(newdata);
                    }
                    cacheManager.update(threadId);//trim chache if needed
                }
                msg = Message.obtain();

                msg.obj = new Holder2<List<Point>, Double>(uploadStarList, mag_limit);
                ;
                msg.arg1 = threadId;
                handler.sendMessage(msg);
                uploadStarList = new ArrayList<Point>();
            }
            msg = Message.obtain();
            msg.arg1 = CuV.STOP_LOADING;
            handler.sendMessage(msg);
        } catch (Exception e) {
            Log.d(TAG, "Thread=" + threadId + " exception=" + e);
        } finally {
            try {
                factory.close();
            } catch (Exception e) {
            }
            handler = null;
        }

    }

    private int getUcac4Size(Set<Integer> set) {
        int num = 0;
        for (int i : set) {
            int pos = Global.ucac4Q.get(i);
            int length = Global.ucac4Q.get(i + 1) - pos;
            num += length;
        }
        return num;
    }

    private double getUcac4MagLimit(Set<Integer> set, double fov) {
		/*
		 * 12	0	0.00%
12.1	60	0.06%
12.2	139	0.14%
12.3	0	0.00%
12.4	176	0.17%
12.5	0	0.00%
12.6	108	0.11%
12.7	251	0.25%
12.8	0	0.00%
12.9	305	0.30%
13	0	0.00%
13.1	185	0.18%
13.2	439	0.43%
13.3	0	0.00%
13.4	565	0.56%
13.5	0	0.00%
13.6	314	0.31%
13.7	901	0.89%
13.8	0	0.00%
13.9	1097	1.08%
14	0	0.00%
14.1	673	0.67%
14.2	1697	1.68%
14.3	0	0.00%
14.4	2271	2.24%
14.5	0	0.00%
14.6	1393	1.38%
14.7	3244	3.21%
14.8	0	0.00%
14.9	4085	4.04%
15	0	0.00%
15.1	2484	2.45%
15.2	5671	5.60%
15.3	0	0.00%
15.4	7185	7.10%
15.5	0	0.00%
15.6	4207	4.16%
15.7	11388	11.25%
15.8	0	0.00%
15.9	16324	16.13%
16	0	0.00%
16.1	9569	9.46%
16.2	9203	9.09%
16.3	14080	13.91%
16.4	2755	2.72%
	101194	

		 */

        if (Global.BASIC_VERSION)
            return 0;

        double[] p = new double[]{0.0272, 0.1391, 0.0909, 0.0946, 0, 0.1612, 0, 0.1125, 0.0416, 0, 0.071, 0, 0.056, 0.0245, 0, 0.0404, 0, 0.0321, 0.0138, 0, 0.0224, 0, 0.0168, 0.0067, 0, 0.0108, 0, 0.0089, 0.0031, 0, 0.0056, 0, 0.0043, 0.0018, 0, 0.003, 0, 0.0025, 0.0011, 0, 0.0017, 0, 0.0014, 0.0006};
        int num = 0;
        for (int i : set) {
            int pos = Global.ucac4Q.get(i);
            int length = Global.ucac4Q.get(i + 1) - pos;
            num += length;
        }
        Log.d(TAG, "total stars=" + num);
        double ucac_mag_limit = new UcacFilter().getMagLimit(fov);
        Log.d(TAG, "ucac_mag_limit=" + ucac_mag_limit);
        if (num > UCAC4_NUM_LIMIT) {
            double r = 1 - (double) (UCAC4_NUM_LIMIT) / (double) num;
            double sum = 0;
            int i = 0;
            for (i = 0; i < p.length; i++) {
                sum += p[i];
                if (sum > r)
                    break;
            }
            double mag_limit = 16.4 - (i + 1) * 0.1;
            Log.d(TAG, "mag_limit=" + mag_limit);

            return Math.min(mag_limit, ucac_mag_limit);
        } else {
            return ucac_mag_limit;
        }

    }

}
