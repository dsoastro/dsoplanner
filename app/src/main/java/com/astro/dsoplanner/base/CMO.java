package com.astro.dsoplanner.base;


import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.abs;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Calendar;

import com.astro.dsoplanner.AstroTools;


/**
 * Base class for comets and minor planets
 *
 * @author leonid
 */
public class CMO extends CustomObjectLarge {

    private static final String MONTH2 = "month";
    private static final String YEAR2 = "year";
    private static final String DAY2 = "day";
    private static final String NODE2 = "node";


    private static final String TAG = CMO.class.getSimpleName();

    public final static double TO_RAD = Math.PI / 180;
    public final static double TO_GRAD = 1 / TO_RAD;
    public final static double OBL_ECL = 23 + 26 / 60.0 + 21.448 / 3600.0;

    public final static int COMET = 1;
    public final static int MINOR_PLANET = 2;

    public final static String NODE = NODE2;

    public final static String I = "i";
    public final static String W = "w";
    public final static String E = "e";


    public final static String DAY = DAY2;
    public final static String YEAR = YEAR2;
    public final static String MONTH = MONTH2;


    public double N;//Long of asc. node
    public double i;//Inclination
    public double w;//Argument of perihelion
    /**
     * semi major axis
     */
    public double sma;//Semi-major axis
    public double e;//Eccentricity
    public double jdp;//julian day of perih
    public double absmag;
    public double slope;
    public double M0;//mean anomaly at jdp
    public double q;//needed for comet parabolic orbits

    public int obj_type;//comet vs minor planet. Needed for magnitude calculation


    public CMO(int catalog, int id, int type, String name1, String name2, String comment, Fields fields) {
        super(catalog, id, 0, 0, 0, type, "", Double.NaN, Double.NaN, Double.NaN, Double.NaN, name1, name2, comment, fields);
    }

    public CMO(DataInputStream stream) throws IOException {
        super(stream);

    }

    @Override
    public void recalculateRaDec(Calendar c) {
        setRaDec(c);
    }

    @Override
    public boolean hasDimension() {
        return false;
    }

    /**
     * 2000 epoch
     *
     * @param cal
     */
    public void setRaDec(Calendar cal) {
        double jdo = AstroTools.JD(cal) - (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (1000. * 3600. * 24.0);

        double sinN = Math.sin(N * TO_RAD);
        double cosN = Math.cos(N * TO_RAD);
        double sinOBL = Math.sin(OBL_ECL * TO_RAD);
        double cosOBL = Math.cos(OBL_ECL * TO_RAD);
        double sinI = Math.sin(i * TO_RAD);
        double cosI = Math.cos(i * TO_RAD);


        double F = cosN;
        double G = sinN * cosOBL;

        double H = sinN * sinOBL;
        double P = -sinN * cosI;
        double Q = cosN * cosI * cosOBL - sinI * sinOBL;
        double R = cosN * cosI * sinOBL + sinI * cosOBL;

        double A = Math.atan2(F, P) * TO_GRAD;
        double B = Math.atan2(G, Q) * TO_GRAD;
        double C = Math.atan2(H, R) * TO_GRAD;

        double ta = Math.sqrt(F * F + P * P);
        double b = Math.sqrt(G * G + Q * Q);
        double c = Math.sqrt(H * H + R * R);

        if (sma == 0) return;
        double n = 0.9856076686 / (Math.sqrt(sma * sma * sma));//mean motion, degrees per day
        double timesinceperih = jdo - jdp;
        double M = timesinceperih * n + M0;//mean anomaly

        data d = new data(0, 0);
        if (e < 1)//ecliptic motion
            d = trueAnomaly(M);
        else if (e == 1 && q <= 0)
            return;
        else if (e == 1 && q > 0) {//parabolic motion

            double W = 0.03649116245 * timesinceperih / (Math.sqrt(q * q * q));
            double Y = Math.pow((W / 2 + Math.sqrt((W / 2) * (W / 2) + 1)), 1 / 3.0);
            double s = Y - 1 / Y;
            d.v = Math.atan(s) * 2 * TO_GRAD;
            d.r = q * (1 + s * s);
        } else if (e > 1)
            d = trueAnomalyHiperbolic(timesinceperih, q, e);


        //heliocentric rect equatorial coordinates
        double xe = d.r * ta * Math.sin((A + w + d.v) * TO_RAD);
        double ye = d.r * b * Math.sin((B + w + d.v) * TO_RAD);
        double ze = d.r * c * Math.sin((C + w + d.v) * TO_RAD);

        double t = jdo - 2451545.0;
        t = (t) / 365250.0;


        sunC rec = getSunRectCoords(t);

        double x = xe + rec.x;
        double y = ye + rec.y;
        double z = ze + rec.z;

        double dist = sqrt(x * x + y * y + z * z);
        double distance = dist * 1.495978e8;
        double dt = 0.0057755183 * dist; //time for light to go from planet to earth (we actually see planet at moment t-dt)
        if (obj_type == COMET)
            mag = absmag + 5 * Math.log10(dist) + 2.5 * slope * Math.log10(d.r);
        else {

            double Rsq = rec.x * rec.x + rec.y * rec.y + rec.z * rec.z;
            double cosb = (d.r * d.r + dist * dist - Rsq) / (2 * d.r * dist);
            double anb = Math.acos(cosb);
            double f1 = Math.exp(-3.33 * Math.pow(Math.tan(anb / 2), 0.63));
            double f2 = Math.exp(-1.87 * Math.pow(Math.tan(anb / 2), 1.22));
            double pr = (1 - slope) * f1 + slope * f2;
            mag = absmag + 5 * Math.log10(d.r * dist) - (2.5 * Math.log10(Math.abs(pr)));
        }

        //second time
        t = t - dt / 365250.0; //smotrim gde zemplya i planeta byli v moment t-dt, stolko
        //trebuetsya svetu na preodoleniye rasstotaniya do zemli+ eto uchityvaet
        //aberratsiyu ot dvizheniya zemli

        timesinceperih = jdo - jdp - dt;
        M = timesinceperih * n + M0;//mean anomaly


        if (e < 1)//ecliptic motion
            d = trueAnomaly(M);
        else if (e == 1 && q <= 0)
            return;
        else if (e == 1 && q > 0) {//parabolic motion

            double W = 0.03649116245 * timesinceperih / (Math.sqrt(q * q * q));
            double Y = Math.pow((W / 2 + Math.sqrt((W / 2) * (W / 2) + 1)), 1 / 3.0);
            double s = Y - 1 / Y;
            d.v = Math.atan(s) * 2 * TO_GRAD;
            d.r = q * (1 + s * s);
        } else if (e > 1)
            d = trueAnomalyHiperbolic(timesinceperih, q, e);

        //heliocentric rect equatorial coordinates
        xe = d.r * ta * Math.sin((A + w + d.v) * TO_RAD);
        ye = d.r * b * Math.sin((B + w + d.v) * TO_RAD);
        ze = d.r * c * Math.sin((C + w + d.v) * TO_RAD);


        x = xe + rec.x;
        y = ye + rec.y;
        z = ze + rec.z;

        //end of second time

        //epoch 2000
        ra = (float) AstroTools.getNormalisedRa(atan2(y, x) * TO_GRAD * 24 / 360);

        dec = (float) (atan(z / sqrt(x * x + y * y)) * TO_GRAD);//ecliptic lattitude
        con = AstroTools.getConstellation(AstroTools.getNormalisedRa(ra), dec);

    }


    class data {
        double r;
        double v;

        public data(double r, double v) {
            super();
            this.r = r;
            this.v = v;
        }

    }

    /**
     * @param M - mean anomaly
     * @return
     */
    private data trueAnomaly(double M) {
        M = rev(M);
        final double Mrad = M * TO_RAD;
        double E = Mrad + e * Math.sin(Mrad) * (1 + e * Math.cos(Mrad));//first approx,radians
        int attempts = 0;
        double prev = 0;
        boolean first = true;
        while (attempts++ < 100 && (Math.abs(E - prev) > 1e-5 || first)) {
            double E1 = E - (E - e * Math.sin(E) - Mrad) / (1 - e * Math.cos(E));
            prev = E;
            E = E1;
            first = false;
        }
        double x = sma * (Math.cos(E) - e);
        double y = sma * Math.sin(E) * Math.sqrt(1 - e * e);

        //Convert to distance and true anomaly:

        double r = Math.sqrt(x * x + y * y);
        double v = TO_GRAD * Math.atan2(y, x);
        return new data(r, v);
    }

    /**
     * @param t  - days since perih
     * @param q
     * @param e0 should not be one
     * @return
     */
    private data trueAnomalyHiperbolic(double t, double q, double e0) {
        if (t == 0) {
            return new data(q, 0);
        }
        double k = 0.01720209895;
        double d1 = 10000;
        double d = 1e-9;
        double q1 = k * Math.sqrt((1 + e0) / q) / (2 * q);
        double q2 = q1 * t;
        double G = (1 - e0) / (1 + e0);

        double s = 2 / (3 * Math.abs(q2));
        s = 2 / (tan(2 * atan(Math.pow(tan(atan(s) / 2), 1 / 3.0))));
        if (t < 0) s = -s;

        int l = 0;
        double s0;
        boolean flag;
        do {
            s0 = s;
            double z = 1;
            double y = s * s;
            double g1 = -y * s;
            double q3 = q2 + 2 * G * s * y / 3;
            double f = 0;

            do {
                z++;
                g1 = -g1 * G * y;
                double z1 = (z - (z + 1) * G) / (2 * z + 1);
                f = z1 * g1;
                q3 = q3 + f;
                flag = (z > 50) || (abs(f) > d1);//no convergence

            } while (abs(f) > d && !flag);
            l++;
            if (flag) break;
            double s1;
            int attempts = 0;
            do {
                s1 = s;
                s = (2 * s * s * s / 3 + q3) / (s * s + 1);
                attempts++;
            }
            while (abs(s - s1) > d && attempts < 100);

        } while (abs(s - s0) > d && l < 50);
        double v = 2 * atan(s);
        return new data(q * (1 + e0) / (1 + e0 * cos(v)), v * TO_GRAD);
    }

    /**
     * @param a andgle to be put into 0-360 range
     * @return
     */
    private static double rev(double a) {
        return AstroTools.normalise(a);
    }

    //SUN COORDINATES CALCULATIONS
    //modified for calculation of sun coordinates
    private static double[] EarthL1AMod = {628307584999.0, 206059, 4303, 425, 119, 109, 93, 72, 68, 67};
    private static double[] EarthL2AMod = {8722, 991, 295, 27};
    private static double[] EarthL2BMod = {1.0725, 3.1416, 0.437, 0.05};
    private static double[] EarthL2CMod = {6283.0758, 0, 12566.152, 3.52};

    private static double[] EarthL3AMod = {289};
    private static double[] EarthL3BMod = {5.842};
    private static double[] EarthL3CMod = {6283.076};

    private static double[] EarthB1AMod = {227778, 3806, 3620, 72};
    private static double[] EarthB1BMod = {3.413766, 3.3706, 0, 3.33};
    private static double[] EarthB1CMod = {6283.075, 12566.151, 0, 18849.23};

    static interface EclCoord {
        Rec EclCoordFunction(double t);
    }

    static class Rec {
        public double l;
        public double b;
        public double r;

        public Rec(double l, double b, double r) {
            this.l = l;
            this.b = b;
            this.r = r;
        }
    }

    static class sunC {
        double x;
        double y;
        double z;

        public sunC(double x, double y, double z) {
            super();
            this.x = x;
            this.y = y;
            this.z = z;
        }

    }

    private static double EclMem(double t, double[] a, double[] b, double[] c) {    //ecliptical member

        //t-julian millenium
        double L = 0;

        for (int i = 0; i < a.length; i++)
            L = L + a[i] * cos(b[i] + c[i] * t);
        return L;
    }

    /**
     * @return sun rect coord in 2000 epoch
     */
    public static sunC getSunRectCoords(double t) {
        //p.160

        EclCoord earth = new EclCoord() {
            public Rec EclCoordFunction(double t) {
                double r0 = EclMem(t, Planet.EarthL0A, Planet.EarthL0B, Planet.EarthL0C);
                double r1 = EclMem(t, EarthL1AMod, Planet.EarthL1B, Planet.EarthL1C);
                double r2 = EclMem(t, EarthL2AMod, EarthL2BMod, EarthL2CMod);
                double r3 = EclMem(t, EarthL3AMod, EarthL3BMod, EarthL3CMod);
                double r4 = 0;
                double r5 = 0;
                double L = (r0 + r1 * t + r2 * t * t + r3 * t * t * t + r4 * t * t * t * t + r5 * t * t * t * t * t) * 1e-08;   //ecliptical longitude
                if (L > 0)
                    L = L - (2 * PI) * (int) (L / (2 * PI));
                else
                    L = L - (2 * PI) * ((int) (L / (2 * PI)) - 1);


                r0 = EclMem(t, Planet.EarthB0A, Planet.EarthB0B, Planet.EarthB0C);
                r1 = EclMem(t, EarthB1AMod, EarthB1BMod, EarthB1CMod);
                double B = (r0 + r1 * t) * 1e-08;

                if (B < -PI)
                    B = B - ((int) ((B + PI) / (2 * PI)) - 1) * (2 * PI);
                else if (B > PI)
                    B = B - ((int) ((B + PI) / (2 * PI))) * (2 * PI);

                r0 = EclMem(t, Planet.EarthR0A, Planet.EarthR0B, Planet.EarthR0C);
                r1 = EclMem(t, Planet.EarthR1A, Planet.EarthR1B, Planet.EarthR1C);
                r2 = EclMem(t, Planet.EarthR2A, Planet.EarthR2B, Planet.EarthR2C);
                r3 = 0;
                r4 = 0;
                double R = (r0 + r1 * t + r2 * t * t + r3 * t * t * t + r4 * t * t * t * t) * 1e-08;
                return (new Rec(L, B, R));

            }
        };

        Rec er = earth.EclCoordFunction(t);
        //earth coordinates modified for sun coordinates calculations (p.160)
        double sl = Math.PI + er.l;
        double sb = -er.b;
        double sr = er.r;

        double x = sr * cos(sb) * cos(sl);
        double y = sr * cos(sb) * sin(sl);
        double z = sr * sin(sb);

        double x0 = x + 0.000000440360 * y - 0.000000190919 * z;
        double y0 = -0.000000479966 * x + 0.917482137087 * y - 0.397776982902 * z;
        double z0 = 0.397776982902 * y + 0.917482137087 * z;


        return new sunC(x0, y0, z0);
    }
}
