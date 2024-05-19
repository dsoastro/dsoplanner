package com.astro.dsoplanner.graph.camera;

import android.os.Process;

public class Prefs {

    public static boolean TESTING_PUSHCAM = false;
    public static boolean BEEP_ON = false;
    /**
     * delay for opening camera
     */
    public static final int CAMERA_OPEN_DELAY = 1000;
    public static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();
    public static final int MAX_DELAY = 2000; //max delay from the last calculated image to leaving the cycle
    public static final int THREAD_PRIORITY = Process.THREAD_PRIORITY_DEFAULT;
    public static final int MAX_IMAGE_QUEUE_SIZE = 3;


    public static final int HR_ALIGNMENT = 1;
    public static final int OBJECT_ALIGNMENT = 2;
    public static final int PLATE_SOLVE = 3;
    public static final int PLATE_SOLVE_RADECS = 4;
    public static final int PLATE_SOLVE_CENTER_INFO = 5;
    public static final int ON_SOLVED_ERROR = 7;
    public static final int ON_SOLVED_SUCCESS = 8;
    public static final int ON_CANCELLED = 9;
    public static final int NONE = -1;
    /**
     * used for comparison of gyro alt and plate alt
     */
    public static final int ALT_ACCURACY = 5;
    public static final int MAX_DST = 1800; //for plate resolving
    /**
     * calibration limit is measured in star magnitude (i.e. stars below this limit are only seen)
     */
    public static final double PUSH_CAMERA_CALIBRATION_LIMIT = 5;

    public enum PhotoType {
        HR_ALIGNMENT,
        OBJECT_ALIGNMENT,
        PLATE_SOLVE
    }

    ;

    public enum AlignType {
        HR_ALIGNMENT,
        OBJECT_ALIGNMENT,
        NONE
    }

    ;

}
