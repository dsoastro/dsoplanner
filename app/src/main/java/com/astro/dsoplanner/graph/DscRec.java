package com.astro.dsoplanner.graph;

import android.content.Context;
import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.matrix.DMatrix;
import com.astro.dsoplanner.matrix.DVector;
import com.astro.dsoplanner.matrix.Line;

import java.util.Calendar;

public class DscRec {
    public static final String TAG = "DscRec";
    public Point star1;
    public Point star2;
    public long time1;
    public long time2;
    public long timeFix;

    /**
     * az = minus normal azimuth, it is measured counterclockwise
     *
     * @param star_scope
     * @return
     */
    public static DVector calc_lmn(XY star_scope) {
        double az1 = star_scope.x * Point.D2R;
        double alt1 = star_scope.y * Point.D2R;
        DVector scope_lmn = new DVector(Math.cos(alt1) * Math.cos(az1),
                Math.cos(alt1) * Math.sin(az1),
                Math.sin(alt1));
        return scope_lmn;
    }

    public static XY calc_az_alt(DVector lmn) {
        double alt = Math.asin(lmn.z) * Point.R2D;
        double az = Math.atan2(lmn.y, lmn.x) * Point.R2D;
        return new XY(az, alt);
    }


    /**
     * None - none set
     * First - one set
     * Second - both set
     */
    enum Stage {None, First, Second}

    ;

    Stage stage;

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * back T matrix - from scope to ra/dec (eq system)
     */
    public DMatrix bT;

    public boolean isbTset() {
        return bT != null;
    }

    public void reset() {
        star1 = null;
        star2 = null;
        bT = null;
        time1 = 0;
        timeFix = 0;
        stage = Stage.None;
    }

    /**
     * angles
     */
    public double star1_az, star1_alt;
    public double star2_az, star2_alt;

    public boolean isReady() {
        if (star1 != null && star2 != null)
            return true;
        return false;
    }

    /**
     * @param star_scope   x: az should be multiplied by -1!, y: as is
     * @param timeInMillis
     * @return
     */
    public AstroTools.RaDecRec calculateRaDec(XY star_scope, long timeInMillis) {
        if (bT == null)
            return null;

        DVector lmn3 = calc_lmn(star_scope);
        DVector LMN3 = bT.timesVector(lmn3);

        double ra3 = Math.atan2(LMN3.y, LMN3.x) / 15 * Point.R2D;
        double dec3 = Math.asin(LMN3.z) * Point.R2D;
        double k = 1.002737908;
        double t1 = time1 / 1000. / 3600.;
        double t3 = timeInMillis / 1000. / 3600.;

        ra3 += k * (t3 - t1);
        Log.d(TAG, "ra/dec=" + ra3 + " " + dec3);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timeInMillis);
        AstroTools.RaDecRec rec = AstroTools.get2000RaDec(ra3, dec3, c);
        Log.d(TAG, "ra/dec(2000)=" + rec.ra + " " + rec.dec);
        return rec;

    }

    /**
     * @param t0          in hours
     * @param t1          time of the first star measurement
     * @param t2          time of the second star measurement
     * @param star1_eq    ra in hours,dec in degrees
     * @param star1_scope az in degrees, alt in degrees
     * @param star2_eq
     * @param star2_scope
     */
    private DMatrix calculatebT(double t0, double t1, double t2,
                                XY star1_eq, XY star1_scope,
                                XY star2_eq, XY star2_scope) {
        // p.37 of matrix_method
        // check for the sign of az!!!
        //assume no planet for the moment, thus precession

        Log.d(TAG, "star1_eq=" + star1_eq);
        Log.d(TAG, "star1_scope=" + star1_scope);
        Log.d(TAG, "star2_eq=" + star2_eq);
        Log.d(TAG, "star2_scope=" + star2_scope);
        Log.d(TAG, "t0 t1 t2=" + t0 + " " + t1 + " " + t2);
        double az1 = star1_scope.x * Point.D2R;
        double alt1 = star1_scope.y * Point.D2R;
        double az2 = star2_scope.x * Point.D2R;
        double alt2 = star2_scope.y * Point.D2R;
        Log.d(TAG, "az1=" + az1);
        Log.d(TAG, "alt1=" + alt1);

        DVector scope_lmn1 = new DVector(Math.cos(alt1) * Math.cos(az1),
                Math.cos(alt1) * Math.sin(az1),
                Math.sin(alt1));
        Log.d(TAG, "lmn1=" + scope_lmn1);

        DVector scope_lmn2 = new DVector(Math.cos(alt2) * Math.cos(az2),
                Math.cos(alt2) * Math.sin(az2),
                Math.sin(alt2));
        Log.d(TAG, "lmn2=" + scope_lmn2);

        double ra1 = star1_eq.x * Point.D2R * 15;
        double dec1 = star1_eq.y * Point.D2R;
        double ra2 = star2_eq.x * Point.D2R * 15;
        double dec2 = star2_eq.y * Point.D2R;
        double k = 1.002737908;
        double dt1 = (t1 - t0) * Point.D2R * 15;
        DVector LMN1 = new DVector(Math.cos(dec1) * Math.cos(ra1 - k * dt1),
                Math.cos(dec1) * Math.sin(ra1 - k * dt1),
                Math.sin(dec1));


        double dt2 = (t2 - t0) * Point.D2R * 15;
        DVector LMN2 = new DVector(Math.cos(dec2) * Math.cos(ra2 - k * dt2),
                Math.cos(dec2) * Math.sin(ra2 - k * dt2),
                Math.sin(dec2));
        double len = (LMN1.y * LMN2.z - LMN1.z * LMN2.y) * (LMN1.y * LMN2.z - LMN1.z * LMN2.y);
        len += (LMN1.z * LMN2.x - LMN1.x * LMN2.z) * (LMN1.z * LMN2.x - LMN1.x * LMN2.z);
        len += (LMN1.x * LMN2.y - LMN1.y * LMN2.x) * (LMN1.x * LMN2.y - LMN1.y * LMN2.x);
        len = 1 / Math.sqrt(len);

        DVector LMN3 = new DVector((LMN1.y * LMN2.z - LMN1.z * LMN2.y) * len,
                (LMN1.z * LMN2.x - LMN1.x * LMN2.z) * len,
                (LMN1.x * LMN2.y - LMN1.y * LMN2.x) * len);


        len = (scope_lmn1.y * scope_lmn2.z - scope_lmn1.z * scope_lmn2.y) * (scope_lmn1.y * scope_lmn2.z - scope_lmn1.z * scope_lmn2.y);
        len += (scope_lmn1.z * scope_lmn2.x - scope_lmn1.x * scope_lmn2.z) * (scope_lmn1.z * scope_lmn2.x - scope_lmn1.x * scope_lmn2.z);
        len += (scope_lmn1.x * scope_lmn2.y - scope_lmn1.y * scope_lmn2.x) * (scope_lmn1.x * scope_lmn2.y - scope_lmn1.y * scope_lmn2.x);
        len = 1 / Math.sqrt(len);
        DVector scope_lmn3 = new DVector((scope_lmn1.y * scope_lmn2.z - scope_lmn1.z * scope_lmn2.y) * len,
                (scope_lmn1.z * scope_lmn2.x - scope_lmn1.x * scope_lmn2.z) * len,
                (scope_lmn1.x * scope_lmn2.y - scope_lmn1.y * scope_lmn2.x) * len);
        Log.d(TAG, "lmn3=" + scope_lmn3);
        DMatrix scope_matrix = new DMatrix(new Line(scope_lmn1.x, scope_lmn2.x, scope_lmn3.x),
                new Line(scope_lmn1.y, scope_lmn2.y, scope_lmn3.y),
                new Line(scope_lmn1.z, scope_lmn2.z, scope_lmn3.z));

        DMatrix eq_matrix = new DMatrix(new Line(LMN1.x, LMN2.x, LMN3.x),
                new Line(LMN1.y, LMN2.y, LMN3.y),
                new Line(LMN1.z, LMN2.z, LMN3.z));

        DMatrix bT = eq_matrix.timesMatrix(scope_matrix.backMatrix());
        Log.d(TAG, "bT=" + bT);
        return bT;
    }

    /**
     * @param star1 az in degrees, alt
     * @param star2
     * @return angle distance between stars in degrees
     */
    private double angDst(XY star1, XY star2) {
        double r = Math.sin(star1.y * Point.D2R) * Math.sin(star2.y * Point.D2R) +
                Math.cos(star1.y * Point.D2R) * Math.cos(star2.y * Point.D2R) *
                        Math.cos((star1.x - star2.x) * Point.D2R);
        return Math.acos(r) * Point.R2D;
    }

    public void makeFix(Context context) {
        DscRec rec = this;
        double t1 = rec.time1 / 1000. / 3600.;


        Point p1 = AstroTools.precession(rec.star1, AstroTools.getDefaultTime(context));
        XY star1_eq = new XY(p1.getRa(), p1.getDec());
        XY star1_scope = new XY(-rec.star1_az, rec.star1_alt);

        double t2 = rec.time2 / 1000. / 3600.;
        Point p2 = AstroTools.precession(rec.star2, AstroTools.getDefaultTime(context));
        XY star2_eq = new XY(p2.getRa(), p2.getDec());
        XY star2_scope = new XY(-rec.star2_az, rec.star2_alt);

        DMatrix bT = calculatebT(t1, t1, t2,
                star1_eq, star1_scope,
                star2_eq, star2_scope);

        this.bT = bT;
    }
}
