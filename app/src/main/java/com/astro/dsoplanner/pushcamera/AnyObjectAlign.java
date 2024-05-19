package com.astro.dsoplanner.pushcamera;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Logg;
import com.astro.dsoplanner.matrix.DMatrix;
import com.astro.dsoplanner.matrix.DVector;
import com.astro.dsoplanner.matrix.Line;
import com.astro.dsoplanner.util.Holder2;

public class AnyObjectAlign {
    public static final String TAG = "AnyObjectAlign";

    public static DMatrix M(double x, double y, double z, double a) {

        //https://ru.wikipedia.org/wiki/%D0%9C%D0%B0%D1%82%D1%80%D0%B8%D1%86%D0%B0_%D0%BF%D0%BE%D0%B2%D0%BE%D1%80%D0%BE%D1%82%D0%B0
        //rotation ccw by angle a around axis given by unit vector x,y,z

        a = a * Math.PI / 180;
        Line l1 = new Line(Math.cos(a) + (1 - Math.cos(a)) * x * x, (1 - Math.cos(a)) * x * y - Math.sin(a) * z, (1 - Math.cos(a)) * x * z + Math.sin(a) * y);
        Line l2 = new Line((1 - Math.cos(a)) * y * x + Math.sin(a) * z, Math.cos(a) + (1 - Math.cos(a)) * y * y, (1 - Math.cos(a)) * y * z - Math.sin(a) * x);
        Line l3 = new Line((1 - Math.cos(a)) * z * x - Math.sin(a) * y, (1 - Math.cos(a)) * z * y + Math.sin(a) * x, Math.cos(a) + (1 - Math.cos(a)) * z * z);
        DMatrix m = new DMatrix(l1, l2, l3);
        return m;

    }

    public static DVector normal_vector(double ra, double dec, double angle) {


        //return :np array of vector ortogonal to (ra,dec) and rotated around it by angle. to be used as vector parallel to shorter side
        P3 center = Utils.get_xyz_from_ra_dec(new P(ra, dec)); // rotation axis
        //DVector v1 = new DVector(center.x, center.y, center.z);

        P3 ort = Utils.get_xyz_from_ra_dec(new P(ra + 12, 90 - dec)); // ortogonal vector
        DVector v2 = new DVector(ort.x, ort.y, ort.z);

        return M(center.x, center.y, center.z, angle).timesVector(v2);

    }

    public static DMatrix get_matrix(double ra, double dec, double angle) {

        //return : matrix formed by vector (ra,dec), normal vector and their vector multiplication

        P3 center = Utils.get_xyz_from_ra_dec(new P(ra, dec));
        DVector v1 = new DVector(center.x, center.y, center.z);
        DVector v2 = normal_vector(ra, dec, angle);
        DVector v3 = Utils.vector_mul(v1, v2);
        DMatrix m = new DMatrix(v1, v2, v3).T();
        return m;
    }

    public static DMatrix getPhoneRotationMatrix(double ra_center, double dec_center, double angle, double lat, double lst) {

        Logg.d(TAG, "gPRM, ra_center=" + ra_center + " dec_center=" + dec_center + " angle=" + angle);
        Logg.d(TAG, "gPRM, lat=" + lat + " lst=" + lst);
        //az alt from the center of image
        Holder2<Double, Double> az_alt = AstroTools.getAzAlt(lst, lat, ra_center, dec_center);
        double az_center = az_alt.x;
        double alt_center = az_alt.y;
        //Log.d(TAG,"az_center="+az_center + " alt_center="+alt_center);

        P3 center = Utils.get_xyz_from_az_alt(new P(az_center, alt_center));
        DVector v_z = new DVector(-center.x, -center.y, -center.z); //z axis
        //Log.d(TAG, "v_z=" + v_z);

        DVector xyz_x_axis = normal_vector(ra_center, dec_center, -angle); // ra dec of x axis
        //Log.d(TAG,"normal vector=" + xyz_x_axis);
        P ra_dec_x_axis = Utils.get_ra_dec_from_xyz(new P3(xyz_x_axis));
        //Log.d(TAG,"normal vector ra dec=" +  ra_dec_x_axis);
        az_alt = AstroTools.getAzAlt(lst, lat, ra_dec_x_axis.x, ra_dec_x_axis.y);
        //Log.d(TAG,"normal vector az alt=" + az_alt.x + " " + az_alt.y);
        double az_x = az_alt.x;
        double alt_x = az_alt.y;

        P3 x_axis = Utils.get_xyz_from_az_alt(new P(az_x, alt_x));
        DVector v_x = new DVector(x_axis); //x axis
        //Log.d(TAG,"v_x=" + v_x);
        //Logg.d(TAG, "v_x=" + v_x + " v_z=" + v_z);
        //Logg.d(TAG, "v_x * v_z=" +  v_x.timesVector(v_z) +" v_x.length=" + v_x.length() + " v_z.length=" + v_z.length());


        DVector v_y = Utils.vector_mul(v_z, v_x); //y = z * x
        DMatrix m = new DMatrix(v_x, v_y, v_z).T();
        return m;
    }

    double ra1, dec1, angle1;
    double ra2, dec2, angle2;
    double ra_eyepiece, dec_eyepiece;

    public AnyObjectAlign(double ra1, double dec1, double angle1, double ra2, double dec2, double angle2, double ra_eyepiece, double dec_eyepiece) {
        this.ra1 = ra1;
        this.dec1 = dec1;
        this.angle1 = angle1;
        this.ra2 = ra2;
        this.dec2 = dec2;
        this.angle2 = angle2;
        this.ra_eyepiece = ra_eyepiece;
        this.dec_eyepiece = dec_eyepiece;
    }

    public P getEyepieceRaDec() {
        // first position        
        DMatrix m1 = get_matrix(ra1, dec1, -angle1);
        P3 eyepiece_1 = Utils.get_xyz_from_ra_dec(new P(ra_eyepiece, dec_eyepiece));

        //second position        
        DMatrix m2 = get_matrix(ra2, dec2, -angle2);

        // transform
        DMatrix T = m2.timesMatrix(m1.backMatrix());
        DVector eyepiece_2 = T.timesVector(new DVector(eyepiece_1));

        return Utils.get_ra_dec_from_xyz(new P3(eyepiece_2));

    }

}