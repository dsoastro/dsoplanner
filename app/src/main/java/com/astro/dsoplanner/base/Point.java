package com.astro.dsoplanner.base;

import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.Calendar;
import java.util.Locale;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.graph.ChartFlipper;
import com.astro.dsoplanner.graph.CuV;


import com.astro.dsoplanner.matrix.Vector2;

//one of the most important classes. Star, DSO etc inherits from it
//defines coordinate transformation

public class Point {

    private static final String DEC2 = " dec=";
    private static final String POINT_RA = "point, ra=";

    private static final String TAG = Point.class.getSimpleName();
    public static final int MIRROR = -1;
    public static final int NO_MIRROR = 1;
    public float ra; //year 2000
    public float dec;

    public float x;
    public float y;
    public float xd; //x coord on display
    public float yd; //y coord on display


    private boolean newPointFlag = true; //for fast calculations. at the fixed sidereal time we calculate altitude and azimuth in alt-az coord system
    private float altr;
    private float az;
    private float cosa;
    private float sina;

    private static double FOV = 5;
    private static double raCenter = 0;
    private static double decCenter = 0;
    protected static double azCenter = 0;
    protected static double altCenter = 0;
    public static int orientationAngle = 0;
    public static int mirror = 1;//1 - no mirror, -1 - mirror
    private static int height; //in px
    private static int width; // in px
    private static double lat;
    private static double lst; //local sidereal time
    private static Calendar cal;//current time, experimental
    public static boolean coordSystem = true; //false for ra/dec, true for az/alt

    public static float densityDpi;
    public static float grscale;

    public static float getScalingFactor() {

        float max = (cuvType == CuV.GRAPH ? 1f : 0.7f) * Math.max(Point.getHeight(), cuvType == CuV.GRAPH ? Point.getWidth() : 2 * Point.getWidth()) * grscale;
        return (max / 1054f);
    }

    private static int cuvType = CuV.GRAPH;

    public static void setCuVType(int type) {
        cuvType = type;
    }

    public static double D2R = PI / 180;
    public static double R2D = 1 / D2R;

    public static double[] sin_arr = new double[370];
    public static double[] cos_arr = new double[370];

    public static double rot_angle = -0 * D2R;

    static {
        for (int i = 0; i < 370; i++) {
            sin_arr[i] = sin(i * D2R);
            cos_arr[i] = cos(i * D2R);
        }
    }

    public static void setRotAngle(double angle) {
        rot_angle = angle * D2R;
    }

    public static double getRotAngle() {
        return rot_angle * R2D;
    }

    public static interface FSIN {
        public double sin(double x);

        public double cos(double x);
    }

    public static FSIN fastFunctions = new FSIN() {
        public double sin(double x) {
            return fsin(x);
        }

        public double cos(double x) {
            return fcos(x);
        }

    };
    public static FSIN slowFunctions = new FSIN() {
        public double sin(double x) {
            return Math.sin(x);
        }

        public double cos(double x) {
            return Math.cos(x);
        }
    };
    public static FSIN tfun = slowFunctions;

    private static double fsin(double d) {

        double dg = d * R2D;

        dg = AstroTools.normalise(dg);
        int d1 = (int) (dg);

        double delta = (dg - d1) * D2R;
        return sin_arr[d1] + cos_arr[d1] * delta;
    }

    private static double fcos(double d) {
        //Log.d(TAG,"fcos"+d+" start");
        double dg = d * R2D;

        dg = AstroTools.normalise(dg);
        int d1 = (int) (dg);
        double delta = (dg - d1) * D2R;
        return cos_arr[d1] - sin_arr[d1] * delta;
    }

    public Point(double ra, double dec) {
        this.ra = (float) ra;
        this.dec = (float) dec;
    }

    public Point(Point point) {
        ra = point.ra;
        dec = point.dec;


    }

    /**
     * used for precession calc
     *
     * @param c
     */
    public static void setCurrentTime(Calendar c) {
        cal = c;
    }

    public static double getDec(double az, double alt) {
        alt *= D2R;
        double lh = cos(alt) * cos(-(az + 180) * D2R);
        double nh = sin(alt);

        double ne = sin((lat - 90) * D2R) * lh + cos((lat - 90) * D2R) * nh;

        return asin(ne) * R2D;

    }

    public static double getRa(double az, double alt) {
        alt *= D2R;
        az = -(az + 180) * D2R;
        double lh = cos(alt) * cos(az);
        double mh = cos(alt) * sin(az);
        double nh = sin(alt);

        double le = cos((lat - 90) * D2R) * lh - sin((lat - 90) * D2R) * nh;
        double me = mh;

        double h = atan2(me, le);
        h = -h * 12 / PI;

        return (lst - h);


    }

    /**
     * getting ra dec coords from screen coords
     *
     * @param v screen coords
     * @return
     */
    public static Vector2 getRaDec(Vector2 v) {
        Vector2 v2 = CustomPoint.getAzAlt(v);
        double alt = v2.y;
        double az = v2.x;
        alt *= D2R;
        az = -(az + 180) * D2R;
        double lh = cos(alt) * cos(az);
        double mh = cos(alt) * sin(az);
        double nh = sin(alt);

        double le = cos((lat - 90) * D2R) * lh - sin((lat - 90) * D2R) * nh;
        double me = mh;

        double h = atan2(me, le);
        h = -h * 12 / PI;

        double ne = sin((lat - 90) * D2R) * lh + cos((lat - 90) * D2R) * nh;

        double ra = (lst - h);
        double dec = asin(ne) * R2D;

        return new Vector2(ra, dec);
    }

    public static Vector2 getRaDec(double az, double alt) {
        alt *= D2R;
        az = -(az + 180) * D2R;
        double lh = cos(alt) * cos(az);
        double mh = cos(alt) * sin(az);
        double nh = sin(alt);

        double le = cos((lat - 90) * D2R) * lh - sin((lat - 90) * D2R) * nh;
        double me = mh;

        double h = atan2(me, le);
        h = -h * 12 / PI;

        double ne = sin((lat - 90) * D2R) * lh + cos((lat - 90) * D2R) * nh;

        double ra = (lst - h);
        double dec = asin(ne) * R2D;

        return new Vector2(ra, dec);
    }

    public double getAlt() {

        double arg = tfun.sin(lat * D2R) * tfun.sin(dec * D2R)
                + tfun.cos(lat * D2R) * tfun.cos(dec * D2R) * tfun.cos((lst - ra) * D2R * 15);
        if (Math.abs(arg) > 0.99)
            arg = Math.sin(lat * D2R) * Math.sin(dec * D2R)
                    + Math.cos(lat * D2R) * Math.cos(dec * D2R) * Math.cos((lst - ra) * D2R * 15);

        return asin(arg) * R2D;
    }

    public double getAz() {

        double d = dec * D2R, t = -(lst - ra) * D2R * 15;
        double le = tfun.cos(d) * tfun.cos(t);
        double me = tfun.cos(d) * tfun.sin(t);
        double ne = tfun.sin(d);

        double lh = tfun.cos(lat * D2R - PI / 2) * le + tfun.sin(lat * D2R - PI / 2) * ne;
        double mh = me;
        double a = -atan2(mh, lh) * R2D + 180;
        return a;

    }


    public void setXY() {
        double x1;
        double y1;
        double z1;
        double y2;
        double z2;
        double x2;

        double tmp;

        if (!coordSystem) { //ra/dec system, not actually used
            double decR = dec * D2R;
            double decCenterR = decCenter * D2R;
            //x,y,z coords in the coordinate system connected with ra,dec system
            tmp = -(ra - raCenter) * D2R * 15;
            x1 = tfun.cos(decR) * tfun.sin(tmp);
            y1 = tfun.sin(decR);
            z1 = tfun.cos(decR) * tfun.cos(tmp);

            // y,z coords in the system connected with point of tangency, x stays the same
            y2 = y1 * tfun.cos(decCenterR) - z1 * tfun.sin(decCenterR);
            z2 = z1 * tfun.cos(decCenterR) + y1 * tfun.sin(decCenterR);
            x2 = x1;

            x = (float) (x2 / (1 + z2));
            y = (float) (y2 / (1 + z2));


        } else //alt az system
        {
            /*
             * we basically have two systems
             * 1. usual alt,az system where y points to zenith, z passes through tangency point of imagenary plane to
             * which we project the celestial spere with celestial sphere at altitude=0 (as we rotate around to see in new direction z rotates with us)
             *
             * 2.system in 1 rotated to have us view at alt=AltCenter (center of screen altitude) direction.  Here imagenary plane is tangent to
             * celestical sphere at point azCenter,AltCenter
             * Then we just take points on celestical sphere and project them onto imagenary plane (x,y coords) and resize to the screen


             */

            //x,y,z coords in the coordinate system connected with alt,az system

            if (newPointFlag) { //first time calculation
                altr = (float) (getAlt() * D2R);
                az = (float) getAz();
                cosa = (float) tfun.cos(altr);
                sina = (float) tfun.sin(altr);
                newPointFlag = false; //no need to calculate again as we change the view of the sky, until time is changed
            }


            //coords in az system
            tmp = (az - azCenter) * D2R;
            x1 = cosa * tfun.sin(tmp); //coords in system 1
            y1 = sina;
            z1 = cosa * tfun.cos(tmp);

            // y,z coords in the system connected with point of tangency, x stays the same (system 2)
            tmp = altCenter * D2R;
            y2 = y1 * tfun.cos(tmp) - z1 * tfun.sin(tmp);
            z2 = z1 * tfun.cos(tmp) + y1 * tfun.sin(tmp);
            x2 = x1;
            z2 += 1; //!!! SAND
            x = (float) (x2 / z2);//projection to imagenary plane
            y = (float) (y2 / z2);

            double xf = x * tfun.cos(rot_angle) + y * tfun.sin(rot_angle);
            double yf = -x * tfun.sin(rot_angle) + y * tfun.cos(rot_angle);

            x = (float) xf;
            y = (float) yf;
        }
    }

    //need to update some calculations (time changed)
    public void raiseNewPointFlag() {
        this.newPointFlag = true;
    }

    /**
     * used in comets/minor planet recalculation
     * gives 2000 Ra Dec
     *
     * @param c
     */
    public void recalculateRaDec(Calendar c) {

    }

    //calculation of dispay coords
    public void setDisplayXY() {
        double tmp = R2D * width / FOV * 2;
        switch (orientationAngle) {
            case 0:
                xd = (float) (x * tmp * mirror + (width >> 1));
                yd = (float) ((height >> 1) - y * tmp);
                break;

            case 180:
                xd = (float) (-x * tmp * mirror + (width >> 1));
                yd = (float) ((height >> 1) + y * tmp);
                break;


        }
    }

    public void setDisplayXY(float dx, float dy) { //for quick redrawing on FOV<=5
        xd = xd + dx;
        yd = yd + dy;
    }

    public double distanceOnDisplay(Point p) {
        return Math.sqrt((xd - p.getXd()) * (xd - p.getXd()) + (yd - p.getYd()) * (yd - p.getYd()));
    }

    public void precess(Calendar c) {//changed ra/dec for precession
        AstroTools.RaDecRec r = AstroTools.precession(ra, dec, c);
        ra = (float) r.ra;
        dec = (float) r.dec;
    }

    public static void setFOV(double FOV) {
        Point.FOV = FOV;
        ChartFlipper.onPointSetFov();
    }

    public static double getFOV() {
        return FOV;
    }

    /**
     * return coordinate of projection of angle on sphere to the plane
     */
    private static double getXprojection(double a) {
        a = a * Math.PI / 180;
        return Math.sin(a) / (1 + Math.cos(a));
    }

    /**
     * solving equation sin a/(1+cos a) = xproject to find real FOV of the screen
     */
    public static double getAngleDimensionX() {
        double k = D2R * FOV / (4);
        double r = Math.asin(2 * k / (1 + k * k)) * R2D;
        if (Math.abs(getXprojection(r) - k) < 1e-5)
            return 2 * r;
        else
            return 2 * (180 - r);
    }

    /**
     * @return real fov of y dimension of the screen
     */
    public static double getAngleDimensionY() {
        double k = D2R * FOV / (4) * Point.getHeight() / Point.getWidth();
        double r = Math.asin(2 * k / (1 + k * k)) * R2D;
        if (Math.abs(getXprojection(r) - k) < 1e-5)
            return 2 * r;
        else
            return 2 * (180 - r);
    }


    public static String getFOVdim_old() {
        double w = Point.getFOV();
        double h = Point.getFOV() * Point.getHeight() / Point.getWidth();
        return getFOVstr(w) + " x " + getFOVstr(h);

    }

    public static String getFOVdim() {
        double w = Point.getAngleDimensionX();

        double h = Point.getAngleDimensionY();
        return getFOVstr(w) + " x " + getFOVstr(h);

    }

    private static String getFOVstr(double fov) {
        if (FOV >= 1) {
            return String.format(Locale.US, "%.1f", fov) + '\u00B0';
        } else {
            return String.format(Locale.US, "%.1f", fov * 60) + (char) 39;
        }

    }

    public static void setHeight(int h) {
        height = h;
    }

    public static void setWidth(int w) {
        width = w;
    }

    public static int getHeight() {
        return height;
    }

    public static int getWidth() {
        return width;
    }

    public static void setCenter(double ra, double dec) {
        raCenter = ra;
        decCenter = dec;
    }

    public static void setCenterAz(double az, double alt) {
        azCenter = az;
        altCenter = alt;
    }

    public static double getRaCenter() {
        return raCenter;
    }

    public static double getDecCenter() {
        return decCenter;
    }

    public static double getAltCenter() {
        return altCenter;
    }

    public static double getAzCenter() {
        return azCenter;
    }

    public float getXd() {
        return xd;
    }

    public float getYd() {
        return yd;
    }

    /**
     * @return 2000 ra by def
     */
    public double getRa() {
        return ra;
    }

    /**
     * @return 2000 dec by def
     */
    public double getDec() {
        return dec;
    }

    public void draw(Canvas canvas, Paint paint) {
    }

    public static void setLST(double lst) {
        Point.lst = lst;

    }

    public static double getLST() {
        return Point.lst;

    }

    public static void setLat(double lat) {
        Point.lat = lat;
    }

    public static double getLat() {
        return lat;
    }

    public String toString() {
        return (POINT_RA + ra + DEC2 + dec);
    }

    public static boolean withinBounds(float x, float y) {
        return ((x >= 0) && (x <= width) && (y >= 0) && (y <= height));
    }

    public static boolean withinBounds(float x, float y, float dim) {
        if (Double.isNaN(dim))
            return withinBounds(x, y);
        return ((x > -dim) && (x <= width + dim) && (y > -dim) && (y <= height + dim));
    }

}

