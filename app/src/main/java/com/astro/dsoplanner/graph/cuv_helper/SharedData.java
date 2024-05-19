package com.astro.dsoplanner.graph.cuv_helper;

import com.astro.dsoplanner.base.ConPoint;
import com.astro.dsoplanner.base.HrStar;
import com.astro.dsoplanner.base.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * used in CuV for hr stars upload
 */
public class SharedData {
    public Map<Integer, ArrayList<HrStar>> hrMap = new HashMap<Integer, ArrayList<HrStar>>();//the same for Yale catalog
    public Map<Integer, ArrayList<ConPoint>> conMap = new HashMap<Integer, ArrayList<ConPoint>>();//and for constellation points
    public volatile List<Point> uploadStarList;//points that we draw
    public volatile List<ConPoint> uploadConList;//constellations point that we draw

    public synchronized void raiseNewPointFlag() {
        for (Map.Entry<Integer, ArrayList<HrStar>> me : hrMap.entrySet())
            for (HrStar s : me.getValue())
                s.raiseNewPointFlag(); //and hr stars
        for (Map.Entry<Integer, ArrayList<ConPoint>> me : conMap.entrySet())
            for (ConPoint s : me.getValue())
                s.raiseNewPointFlag();//and constellation points

    }

    public synchronized Map<Integer, ArrayList<HrStar>> getHrMap() {
        return hrMap;
    }

    public synchronized Map<Integer, ArrayList<ConPoint>> getConMap() {
        return conMap;
    }

    public synchronized void setHrMap(Map<Integer, ArrayList<HrStar>> m) {
        hrMap = m;
    }

    public synchronized void setConMap(Map<Integer, ArrayList<ConPoint>> m) {
        conMap = m;
    }
}
