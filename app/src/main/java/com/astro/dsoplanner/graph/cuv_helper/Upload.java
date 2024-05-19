package com.astro.dsoplanner.graph.cuv_helper;

import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.MyList;
import com.astro.dsoplanner.base.ConPoint;
import com.astro.dsoplanner.base.HrStar;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.graph.CuV;
import com.astro.dsoplanner.graph.Quadrant;
import com.astro.dsoplanner.graph.StarMags;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
//import java.util.logging.Handler;

import static java.lang.Math.abs;
import static java.lang.Math.cos;

import android.os.Handler;

public class Upload implements Runnable {
    double azCenter;
    double altCenter;
    double raCenter;
    double decCenter;
    double FOV;
    double height;
    double width;
    boolean raiseNewPointFlag;
    boolean newFOV;
    Signal signal;
    SharedData sh;
    CuV cuv;
    Handler handler;

    Map<Integer, ArrayList<HrStar>> tycoMap;
    Map<Integer, ArrayList<HrStar>> ucacMap;
    Map<Integer, ArrayList<HrStar>> hrMap;
    Map<Integer, ArrayList<ConPoint>> conMap;

    ArrayList<Point> uploadStarList = new ArrayList<Point>();
    ArrayList<ConPoint> uploadConList = new ArrayList<ConPoint>();

    public Upload(Signal signal, SharedData sh, CuV cuv, Handler handler) {
        this.signal = signal;
        this.sh = sh;
        this.cuv = cuv;
        this.handler = handler;
    }

    public void run() {
        RandomAccessFile ref = null;
        RandomAccessFile list = null;
        RandomAccessFile db = null;
        RandomAccessFile dbq = null;
        RandomAccessFile dbucac = null;

        while (signal.getRec() != null) {
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
            hrMap = sh.getHrMap();
            conMap = sh.getConMap();
            if (newFOV) {
                hrMap = new HashMap<Integer, ArrayList<HrStar>>();
                conMap = new HashMap<Integer, ArrayList<ConPoint>>();
            }
            signal.setRec(null);


            upLoadStars3(ref, list, dbq, dbucac);

            sh.setHrMap(hrMap);
            sh.setConMap(conMap);

            synchronized (cuv) {
                sh.uploadStarList = uploadStarList;
                sh.uploadConList = uploadConList;
                uploadStarList = new ArrayList<Point>();
                uploadConList = null;
            }
            if (raiseNewPointFlag) sh.raiseNewPointFlag();
            if (handler != null) handler.sendEmptyMessage(0);


        }
    }

    //uploads stars for drawing
    public void upLoadStars3(RandomAccessFile ref, RandomAccessFile list, RandomAccessFile db, RandomAccessFile dbucac) {
        try {
            uploadStarList = new ArrayList<Point>();
            uploadConList = new ArrayList<ConPoint>();
            Set<Integer> s = new HashSet<Integer>();// for hr stars
            Set<Integer> sCon = new HashSet<Integer>();//for constellations
            double mag_limit = 6;
            mag_limit = StarMags.getMagLimit(StarMags.YALE, Point.getFOV());
            double stepra = FOV * 12 / 180 / 2 * height / width;
            double co = 1;
            if (abs(co) > 0.01) stepra = (float) (stepra / co);
            double hw = height / width;
            if (hw < 1) hw = 1 / hw;

            double stepdec;
            stepdec = FOV / 2 * hw;


            int l = 0;
            int k = 0;

            //looking for quadrants that are covered by current screen given current FOV
            double dist;
            if (FOV > CuV.FOV_181) dist = CuV.COS_THRESHOLD_181;
            else if (FOV > CuV.FOV_149) dist = CuV.COS_THRESHOLD_149;
            else dist = cos(1.2 * (stepdec + 6) * Grid.D2R);

            for (Quadrant q : Quadrant.quadrants) {
                if (q.cosDist(raCenter, decCenter) > dist) s.add(l); //adding close quadrant
                l++;
            }
            l = 0;
            double distCon;
            if (stepdec < 20 / 2 * hw)//for large magnification we still want to draw the full constellation
                distCon = cos(2 * (20 / 2 * hw + 6) * Grid.D2R);
            else if (FOV > CuV.FOV_181) distCon = CuV.COS_THRESHOLD_181;
            else if (FOV > CuV.FOV_149) distCon = CuV.COS_THRESHOLD_149;
            else distCon = cos(2 * (stepdec + 6) * Grid.D2R);
            for (Quadrant q : Quadrant.quadrants) {
                if (q.cosDist(raCenter, decCenter) > distCon) sCon.add(l); //adding close quadrant
                l++;
            }

            Set<Integer> sf = new HashSet<Integer>(s);    //set of quadrants covered by screen, hr stars
            Set<Integer> ts = new HashSet<Integer>(hrMap.keySet()); //already downloaded quadrants, hr stars

            Set<Integer> sfCon = new HashSet<Integer>(sCon);    //set of quadrants covered by screen, con points
            Set<Integer> tsCon = new HashSet<Integer>(conMap.keySet()); //already downloaded quadrants,con points

            sf.removeAll(ts);//need to download, hr stars
            sfCon.removeAll(tsCon);//con points

            for (Integer i : ts) {
                if (!s.contains(i)) {//removing quadrants that are not covered by the screen, hr stars
                    hrMap.remove(i);
                }
            }
            for (Integer i : tsCon) {
                if (!sCon.contains(i)) {//removing quadrants that are not covered by the screen, con points
                    conMap.remove(i);
                }
            }
            Iterator<Integer> it = sf.iterator();
            for (Map.Entry<Integer, ArrayList<HrStar>> me : hrMap.entrySet()) {
                uploadStarList.addAll(me.getValue()); //adding hr stars to download
            }
            for (Map.Entry<Integer, ArrayList<ConPoint>> me : conMap.entrySet()) {
                uploadConList.addAll(me.getValue()); //adding constellation points to download
            }
            while (it.hasNext()) {
                l = it.next(); //quadrant number
                MyList mList = Global.sd.list[l]; //star HR numbers belonging to the given quadrant
                ArrayList<HrStar> as = new ArrayList<HrStar>();

                for (Integer i : mList) {
                    HrStar st = new HrStar(Global.databaseHr[i]);        //making a new star from the hr database
                    if (st.getMag() <= mag_limit) {
                        as.add(st);
                        uploadStarList.add(st); //adding to starlist if magnitude is right
                    }
                }
                hrMap.put(l, as); //putting pair: quadrant number / list of stars, to implement incrementable download


            }
            Iterator<Integer> itCon = sfCon.iterator();
            for (; itCon.hasNext(); ) {
                l = itCon.next();//quadrant number
                MyList cL = Global.cflist[l];//list of constellation points in a given quadrant
                ArrayList<ConPoint> ac = new ArrayList<ConPoint>();
                for (Integer i : cL) {

                    ConPoint cp = new ConPoint(Global.cf.get(i));
                    ac.add(cp);
                    uploadConList.add(cp);//adding constellation point to draw list
                }
                conMap.put(l, ac);//same idea
            }

        } catch (Exception e) {

        }

    }


}
