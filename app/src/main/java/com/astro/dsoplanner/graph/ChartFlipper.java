package com.astro.dsoplanner.graph;

import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.Point;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChartFlipper {
    private static final String TAG = ChartFlipper.class.getSimpleName();
    private static final String[] FEATURE_OFF_STATE_PLACEHOLDER = {"0", "0", "0"};
    private static List<RotMirr> rotMirrs = new ArrayList<>();
    private static RotMirr currentState;

    /**
     * Every time the {@link Point#setFOV(double)} used this method must be called to update
     * {@link Point} with the maintained here flipper data.
     */
    public static void onPointSetFov() {
        //Log.d(TAG,"onPointSetFov called");
        if (rotMirrs.size() == 0) {
            rotMirrs.add(new RotMirr(FEATURE_OFF_STATE_PLACEHOLDER));
        }
        double newFov = isFeatureOn() ? Point.getFOV() : 0;
        currentState = null;
        for (RotMirr rec : rotMirrs) {
            if (rec.isClose(newFov)) {
                currentState = rec;
                break;
            }
        }
        verifyCurrentState();
        updatePointData();
    }

    /**
     * Call this to update the Flipper state from user input.
     */
    public static void setRotated(boolean is) {
        verifyCurrentState();
        currentState.isRotated = is ? 1 : 0;
    }

    /**
     * Call this to update the Flipper state from user input.
     */
    public static void setMirrored(boolean is) {
        verifyCurrentState();
        currentState.isMirrored = is ? 1 : 0;
    }

    static void saveToPrefs() {
        SettingsActivity.saveChartFlipperData(getAsString());
    }

    static void setFromString(String s) {
        //Log.d(TAG,"setFromString  called");
        Set<Integer> set = new HashSet<>();
        if (s != null) {
            rotMirrs = new ArrayList<>();
            String[] recs = s.split(";");
            String[] vals;
            for (String rec : recs) {
                vals = rec.split(" ");
                if (vals.length == 3) {

                    String fov = vals[0];
                    Double fov_d;
                    int fov_int;
                    try {
                        fov_d = 1000 * Double.parseDouble(fov);
                        fov_int = fov_d.intValue();
                    }
                    catch (Exception e){
                        continue;
                    }
                    if (set.contains(fov_int))
                        continue;
                    else
                        set.add(fov_int);

                    RotMirr r = null;
                    try{
                        r = new RotMirr(vals);
                    }
                    catch (Exception e){}
                    if (r != null)
                        rotMirrs.add(r);
                }
            }
            if (!set.contains(0))
                rotMirrs.add(new RotMirr(FEATURE_OFF_STATE_PLACEHOLDER));

        }
        /*
        for (RotMirr r:rotMirrs){
            Log.d(TAG,"setFromString=" + r);
        }
        */
         
        
    }



    private static String getAsString() {
        String format = "%f %d %d;";
        StringBuilder sb = new StringBuilder();
        for (RotMirr rec : rotMirrs) {
            sb.append(String.format(Locale.US, format, rec.fov, rec.isRotated, rec.isMirrored));
        }
        return sb.toString();
    }

    private static boolean isFeatureOn() {
        return SettingsActivity.getIsPerFovFlipperOn();
    }

    private static void updatePointData() {
        Point.orientationAngle = currentState.isRotated == 1 ? 180 : 0;
        Point.mirror = currentState.isMirrored == 1 ? Point.MIRROR : Point.NO_MIRROR;
    }

    /**
     * Create the new state record if not yet defined for this FOV.
     * If the feature is off {@link #currentState} wouldn't be null.
     */
    private static void verifyCurrentState() {
        if (currentState == null) {
            RotMirr newState = new RotMirr();
            rotMirrs.add(newState);
            currentState = newState;
        }
    }

    /**
     * Flipper state data class.
     */
    static class RotMirr {
        static final double DELTA = 0.001;
        double fov;
        int isRotated;
        int isMirrored;

        RotMirr() {
            this.fov = Point.getFOV();
            isRotated = 0;
            isMirrored = 0;
        }

        RotMirr(String[] vals) {
            fov = Double.valueOf(vals[0]);
            isRotated = Integer.valueOf(vals[1]);
            isMirrored = Integer.valueOf(vals[2]);
        }

        boolean isClose(double newFov) {
            return fov < newFov + DELTA && fov > newFov - DELTA;
        }

        @Override
        public String toString() {
            return "RotMirr{" +
                    "fov=" + fov +
                    ", isRotated=" + isRotated +
                    ", isMirrored=" + isMirrored +
                    '}';
        }
    }
}
