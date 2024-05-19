package com.astro.dsoplanner.graph;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Logg;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.matrix.Axis;
import com.astro.dsoplanner.matrix.DMatrix;
import com.astro.dsoplanner.matrix.DVector;
import com.astro.dsoplanner.matrix.Line;
import com.astro.dsoplanner.pushcamera.P;
import com.astro.dsoplanner.pushcamera.P4;
import com.astro.dsoplanner.util.Holder2;

import java.util.ArrayList;
import java.util.Calendar;

import static java.lang.Math.PI;
//ToDo. LV. Replace matrix chaining

public class SensorProcessor {
    public static final String TAG = "Graph";
    private float[] mags = new float[3];
    private float[] accels = new float[3];
    private float[] rm = new float[9];
    private float[] rm_snapshot = new float[9];
    private float[] InclinationMat = new float[9];
    private final float declination = 0;
    DMatrix m;


    private DVector uploadVector = null;
    private int Nattempts = 3;
    private double maxDst = 0.2;//threshold for skipping stray points
    private int attempts = 0;
    private DVector v1prev = null;
    private DVector v2prev = null;
    private double alfa = 0.1;//weight of the new

    private class Holder {
        DVector x;//storing DVector(0,0,-1);
        DVector y;//DVector(0,1,0)

        public Holder(DVector x, DVector y) {
            this.x = x;
            this.y = y;
        }
    }

    private ArrayList<Holder> list = new ArrayList<Holder>();
    private boolean nadirDisabled;
    private double mACorrection = 0;
    int rotation;
    Display display;

    enum sMode {START, ADJUST, WORK}

    ;
    sMode status = sMode.START;
    DMatrix phoneMatrix;
    boolean running = false;

    GraphActivity graphActivity;

    public SensorProcessor(GraphActivity graphActivity) {
        this.graphActivity = graphActivity;
    }

    public void init() {
        Nattempts = SettingsActivity.getASnattempts();
        maxDst = SettingsActivity.getASmaxdst();
        alfa = SettingsActivity.getASalpha();
        nadirDisabled = SettingsActivity.getANadir();
        mACorrection = 0;
        display = graphActivity.getWindowManager().getDefaultDisplay();
        last_event = null;
    }

    public void start() {
        running = true;

    }

    public void stop() {
        running = false;
    }


    SensorEvent last_event = null;
    DMatrix R_alpha = null;
    DMatrix R_minus_alpha = null;
    DVector eyepiece = null;
    DVector eyepiece2 = null;
    P4 radecs = null;
    int N = 0;


    public void clearAdjData() {
        radecs = null;
    }


    boolean flagStart = false;

    public void setAdjData(P4 radecs) {
        Logg.d(TAG, "SensorProcessor, setAdjData=" + radecs);
        this.radecs = radecs;
        flagStart = true;
    }

    public void setPhoneMatrix(DMatrix phoneMatrix) {
        this.phoneMatrix = phoneMatrix;
    }

    /**
     * get altitude from latest gyro event
     *
     * @return
     */
    public double getAlt() {
        if (last_event != null) {
            float[] rm = new float[9];
            SensorManager.getRotationMatrixFromVector(rm, last_event.values);
            m = new DMatrix(new Line(rm[0], rm[1], rm[2]),
                    new Line(rm[3], rm[4], rm[5]), new Line(rm[6], rm[7], rm[8]));
            DVector vec = new DVector(0, 0, -1);
            DVector v1 = m.timesVector(vec); //vector pointing from the center to the sky according to gyro

            v1.normalise();
            double alt = 180 / PI * (Math.asin(v1.z));
            return alt;
        } else
            return Double.NaN;

    }

    public void adjust() {
        if (last_event == null) {
            Logg.d(TAG, "null last_event");
            return;
        }
        if (radecs == null) {
            Logg.d(TAG, "ERROR SensorProcessor.adjust radec is null!");
            return;
        }
        Logg.d(TAG, "adjust");
        double ra_star = radecs.x;
        double dec_star = radecs.y;

        double ra_center = radecs.z;
        double dec_center = radecs.t;

        double lat = Point.getLat();
        Logg.d(TAG, "adjust, lat1=" + lat + " lat2=" + SettingsActivity.getLattitude());
        Calendar now = Calendar.getInstance();
        double lst = AstroTools.sdTime(now);
        //az alt from the center of image
        Holder2<Double, Double> az_alt = AstroTools.getAzAlt(lst, lat, ra_center, dec_center);
        double az_center = az_alt.x;

        float[] rm_snapshot = new float[9];
        SensorManager.getRotationMatrixFromVector(rm_snapshot, last_event.values);

        DMatrix m = new DMatrix(new Line(rm_snapshot[0], rm_snapshot[1], rm_snapshot[2]),
                new Line(rm_snapshot[3], rm_snapshot[4], rm_snapshot[5]), new Line(rm_snapshot[6], rm_snapshot[7], rm_snapshot[8]));
        rm_last = m; //matrix at the eyepiece calculation
        rm_last2 = rm_last;
        Logg.d(TAG, "rm_last=" + m);
        Logg.d(TAG, "phoneMatrix=" + phoneMatrix);
        DVector vec = new DVector(0, 0, -1);
        DVector v1 = m.timesVector(vec); //vector pointing from the center to the sky according to gyro


        //center screen azimuth in gyro coordinate system
        double az_gyro = 180 / PI * Math.atan2(v1.x, v1.y);
        double alt_gyro = 180 / PI * Math.asin(v1.z);
        double alpha = -(az_center - az_gyro);
        Logg.d(TAG, "az_gyro = " + az_gyro + "alt_gyro=" + alt_gyro + " az_center=" + az_center + " alpha=" + alpha);
        R_alpha = new DMatrix(Axis.Z, alpha * Point.D2R);
        Logg.d(TAG, "R_alpha=" + R_alpha);
        R_minus_alpha = new DMatrix(Axis.Z, -alpha * Point.D2R);

        Holder2<Double, Double> az_alt_star = AstroTools.getAzAlt(lst, lat, ra_star, dec_star);
        double az = az_alt_star.x * Point.D2R;
        double alt = az_alt_star.y * Point.D2R;
        Logg.d(TAG, "adjust, star  az=" + az * Point.R2D + " alt=" + alt * Point.R2D);

        double cos_dec = Math.cos(alt);
        double x = cos_dec * Math.sin(az);
        double y = cos_dec * Math.cos(az);
        double z = Math.sin(alt);
        eyepiece = new DVector(x, y, z);
        eyepiece2 = eyepiece;
        graphActivity.cView.sgrChanged(true);
    }


    DMatrix rm_last = null;
    DMatrix rm_last2 = null;
    int delay = 0;
    int delay_log = 0;

    public void processEvent(SensorEvent event) {
        int type = event.sensor.getType();
        if (type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            last_event = event;
            if (!running)
                return;

            float[] rm = new float[9];
            SensorManager.getRotationMatrixFromVector(rm, event.values);
            m = new DMatrix(new Line(rm[0], rm[1], rm[2]),
                    new Line(rm[3], rm[4], rm[5]), new Line(rm[6], rm[7], rm[8]));

            DVector v1 = null;
            if (rm_last != null && R_alpha != null) { // last two just in case
                DMatrix delta = m.timesMatrix(rm_last.backMatrix());
                if (flagStart) {
                    Logg.d(TAG, "processEvent, m=" + m + " rm_last=" + rm_last + " delta=" + delta);
                }
                DMatrix R = R_minus_alpha.timesMatrix(delta).timesMatrix(R_alpha);
                v1 = R.timesVector(eyepiece);
                eyepiece = v1;
                eyepiece.normalise();
            } else {
                DVector vec = new DVector(0, 0, -1); //vector in the system connected with the phone. y-along the larger side, z - from the screen
                v1 = m.timesVector(vec);//the same vector in the system connected with the earth system. y - pointing to north and tangenial to the ground,z - to zenith
            }
            rm_last = m;

            DVector v2 = m.backMatrix().timesVector(new DVector(0, 0, 1));//vector z==-g pointing to zenith transferred to phone system of coords
            if (v1.isValid() && v2.isValid())
                setCurrVector(v1, v2, m);
            else
                Log.d(TAG, "v1 or v2 not valid " + v1 + " " + v2);
        }
    }

    private void setCurrVector(DVector v1, DVector v2, DMatrix m) {//v1 for direction, v2 for tilt


        v1.normalise();
        double alt = 180 / PI * (Math.asin(v1.z));
        double az = 180 / PI * Math.atan2(v1.x, v1.y); //+declination
        if (flagStart) {
            Logg.d(TAG, "setCurrVector, az=" + az + " alt=" + alt);
            flagStart = false;
        }

        delay++;
        if (delay == 10) {
            graphActivity.getPushCamPresenter().onGyroUpdated(new P(az, alt));
            delay = 0;
        }
        delay_log++;
        if (delay_log == 30) {
            Logg.d(TAG, "sensorProcessor, setCurrVector, az=" + az + " alt=" + alt);
            Logg.d(TAG, "sensorProcessor, setCurrVector, ra,dec=" + Point.getRaDec(az, alt));
            delay_log = 0;
        }

        double correction = 0;
        rotation = display.getRotation();
        if (rotation == Surface.ROTATION_90)
            correction = -90;
        if (rotation == Surface.ROTATION_270)
            correction = 90;
        if (rotation == Surface.ROTATION_180)
            correction = 180;
        if (SettingsActivity.isPushCameraOn(graphActivity)) {
            DVector vp = new DVector(v2.x, v2.y, 0);//projection of z==-g to phone surface
            if (vp.length() < 0.001) {
                Point.setRotAngle(correction); //0+mACorrection+
                return;
            }

            vp.normalise();
            double angle = 180 / PI * Math.acos(vp.timesVector(new DVector(1, 0, 0)));
            if (vp.timesVector(new DVector(0, 1, 0)) < 0)
                angle = 180 + (180 - angle);

            double rotan = -angle + 90; //angle between g and shorter side of the phone

            Point.setRotAngle(rotan + correction); //+mACorrection+
        }
        CuV cView = graphActivity.getSkyView();
        if (SettingsActivity.isPushCameraOn(graphActivity)) {
            Point.setCenterAz(az, alt);
            double dst = 1000;
            if (uploadVector != null && uploadVector.length() < 2)//use length to cut out NaNs
                dst = v1.distance(uploadVector);
            if (dst * 180 / PI > Point.getFOV() / 4) {
                Log.d(TAG, "sensor upload");

                cView.deltaX = Point.getWidth();//to start upload in CustomView
                cView.deltaY = cView.deltaX;
                uploadVector = v1;
            }
        }
        graphActivity.setCenterLoc();
        cView.invalidate();


    }

    public void processEventCompass(SensorEvent event) {
        int type = event.sensor.getType();
        if (type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rm, event.values);

            m = new DMatrix(new Line(rm[0], rm[1], rm[2]),
                    new Line(rm[3], rm[4], rm[5]), new Line(rm[6], rm[7], rm[8]));
            DVector vec = new DVector(0, 0, -1); //vector in the system connected with the phone. y-along the larger side, z - from the screen
            DVector v1 = m.timesVector(vec);//the same vector in the system connected with the earth system. y - pointing to north and tangenial to the ground,z - to zenith
            if (nadirDisabled && v1.z < 0)
                return;
            //used for getting direction to the sky part to be shown

				/*	vec=new DVector(1,0,0);			//used for getting tilt of the phone
					DVector v2=m.timesVector(vec);*/

            DVector v2 = m.backMatrix().timesVector(new DVector(0, 0, 1));//vector z==-g pointing to zenith transferred to phone system of coords
            if (v1.isValid() && v2.isValid())
                setCurrVector_2(v1, v2);
            else
                Log.d(TAG, "v1 or v2 not valid " + v1 + " " + v2);
        }
    }

    private void setCurrVector_2(DVector v1, DVector v2) {//v1 for direction, v2 for tilt

        double alt = 180 / PI * (Math.asin(v1.z));
        double az = 180 / PI * Math.atan2(v1.x, v1.y);

        double correction = 0;

        rotation = display.getRotation();
        if (rotation == Surface.ROTATION_90)
            correction = -90;
        if (rotation == Surface.ROTATION_270)
            correction = 90;
        if (rotation == Surface.ROTATION_180)
            correction = 180;
        if (SettingsActivity.isAutoRotationOn()) {

            //DVector v=m.backMatrix().timesVector(new DVector(0,0,1));//vector z==-g pointing to zenith transferred to phone system of coords
            DVector vp = new DVector(v2.x, v2.y, 0);//projection of z==-g to phone surface
            if (vp.length() < 0.001) {
                Point.setRotAngle(0 + mACorrection + correction);
                return;
            }

            vp.normalise();
            double angle = 180 / PI * Math.acos(vp.timesVector(new DVector(1, 0, 0)));
            if (vp.timesVector(new DVector(0, 1, 0)) < 0)
                angle = 180 + (180 - angle);

            double rotan = -angle + 90; //angle between g and shorter side of the phone
            Point.setRotAngle(rotan + mACorrection + correction);

        }
        CuV cView = graphActivity.getSkyView();
        if (SettingsActivity.isAutoSkyOn()) {
            Point.setCenterAz(az, alt);
            double dst = 1000;
            if (uploadVector != null && uploadVector.length() < 2)//use length to cut out NaNs
                dst = v1.distance(uploadVector);
            if (dst * 180 / PI > Point.getFOV() / 4) {
                cView.deltaX = Point.getWidth();//to start upload in CustomView
                cView.deltaY = cView.deltaX;
                uploadVector = v1;
            }
        }
        graphActivity.setCenterLoc();
        cView.invalidate();
    }

}
