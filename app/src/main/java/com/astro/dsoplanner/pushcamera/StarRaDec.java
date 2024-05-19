package com.astro.dsoplanner.pushcamera;

import com.astro.dsoplanner.util.Holder2;

public class StarRaDec {
    double rows, cols, star_r, star_c, rac, decc, angle, scale, ra_initial, dec_initial;


    public StarRaDec(double rows, double cols, double star_r, double star_c, double rac, double decc, double angle,
                     double scale, double ra_initial, double dec_initial) {
        super();
        this.rows = rows;
        this.cols = cols;
        this.star_r = star_r;
        this.star_c = star_c;
        this.rac = rac;
        this.decc = decc;
        this.angle = angle;
        this.scale = scale;
        this.ra_initial = ra_initial;
        this.dec_initial = dec_initial;
    }

    public static class F1_2_param implements F2 {
        double star_r;
        double star_c;
        double rac;
        double decc;
        double angle;
        double scale;
        double rows, cols;

        public F1_2_param(double star_r, double star_c, double rac, double decc, double angle, double scale,
                          double rows, double cols) {
            super();
            this.star_r = star_r;
            this.star_c = star_c;
            this.rac = rac;
            this.decc = decc;
            this.angle = angle;
            this.scale = scale;
            this.rows = rows;
            this.cols = cols;
        }

        public double f(double x1, double x2) {
            double star_ra = x1;
            double star_dec = x2;
            return CenterLocation.f(rac, decc, angle, scale, star_ra, star_dec, star_r, star_c, rows, cols).x;
        }
    }

    public static class F2_2_param implements F2 {
        double star_r;
        double star_c;
        double rac;
        double decc;
        double angle;
        double scale;
        double rows, cols;

        public F2_2_param(double star_r, double star_c, double rac, double decc, double angle, double scale,
                          double rows, double cols) {
            super();
            this.star_r = star_r;
            this.star_c = star_c;
            this.rac = rac;
            this.decc = decc;
            this.angle = angle;
            this.scale = scale;
            this.rows = rows;
            this.cols = cols;
        }

        public double f(double x1, double x2) {
            double star_ra = x1;
            double star_dec = x2;
            return CenterLocation.f(rac, decc, angle, scale, star_ra, star_dec, star_r, star_c, rows, cols).y;
        }

    }

    private boolean is_close_f_to_zero_2(V2 fs) {
        double h = 1;
        double[] a = fs.getArray();
        double f1 = a[0];
        double f2 = a[1];


        if (Math.abs(f1) < h && Math.abs(f2) < h)
            return true;
        else
            return false;
    }

    public V2 get() {

        F2 f1 = new F1_2_param(star_r, star_c, rac, decc, angle, scale, rows, cols);
        F2 f2 = new F2_2_param(star_r, star_c, rac, decc, angle, scale, rows, cols);
        Eq2 eq = new Eq2(f1, f2);


        Holder2<V2, V2> h = eq.next(new V2(ra_initial, dec_initial));

        for (int i = 0; i < 20; i++) {
            h = eq.next(h.x);
            if (is_close_f_to_zero_2(h.y))
                break;

        }


        if (is_close_f_to_zero_2(h.y))
            return h.x;
        else
            return null;

    }


}
