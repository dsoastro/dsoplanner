package com.astro.dsoplanner.pushcamera;

import com.astro.dsoplanner.util.Holder2;

class CenterLocation {

    double rows, cols, ra_center, dec_center, angle, scale;
    P star1_radec, star2_radec, star1_rc, star2_rc;


    public CenterLocation(double rows, double cols, double ra_center, double dec_center, double angle, double scale,
                          P star1_radec, P star2_radec, P star1_rc, P star2_rc) {
        super();
        this.rows = rows;
        this.cols = cols;
        this.ra_center = ra_center;
        this.dec_center = dec_center;
        this.angle = angle;
        this.scale = scale;
        this.star1_radec = star1_radec;
        this.star2_radec = star2_radec;
        this.star1_rc = star1_rc;
        this.star2_rc = star2_rc;
    }

    public static P f0(double ra_center, double dec_center, double angle, double scale, double star_ra, double star_dec, double rows, double cols) {

        P pr = Utils.project(new P(ra_center, dec_center), new P(star_ra, star_dec));
        double y = pr.x;
        double z = pr.y;
        P xy = Utils.new_xy(angle, new P(y, z));
        double x1 = xy.x;
        double x2 = xy.y;
        x1 *= scale;
        x2 *= scale;

        double c = x1 + cols / 2;
        double r = rows / 2 - x2;

        return new P(r, c);
    }

    public static P f(double ra_center, double dec_center, double angle, double scale, double star_ra, double star_dec, double r, double c, double rows, double cols) {

        P pr = Utils.project(new P(ra_center, dec_center), new P(star_ra, star_dec));
        double y = pr.x;
        double z = pr.y;
        P xy = Utils.new_xy(angle, new P(y, z));
        double x1 = xy.x;
        double x2 = xy.y;
        x1 *= scale;
        x2 *= scale;

        double x = c - cols / 2;
        y = rows / 2 - r;
        return new P(x1 - x, x2 - y);
    }

    public static class F1_param implements F4 {
        double star_ra;
        double star_dec;
        double r;
        double c;
        double rows, cols;

        public F1_param(P star_ra_dec, P star_rc, double rows, double cols) {
            this.star_ra = star_ra_dec.x;
            this.star_dec = star_ra_dec.y;
            this.r = star_rc.x;
            this.c = star_rc.y;
            this.rows = rows;
            this.cols = cols;
        }

        public double f(double x1, double x2, double x3, double x4) {

            double ra_center = x1;
            double dec_center = x2;
            double angle = x3;
            double scale = x4;
            return CenterLocation.f(ra_center, dec_center, angle, scale, star_ra, star_dec, r, c, rows, cols).x;
        }
    }

    public static class F2_param implements F4 {
        double star_ra;
        double star_dec;
        double r;
        double c;
        double rows, cols;

        public F2_param(P star_ra_dec, P star_rc, double rows, double cols) {
            this.star_ra = star_ra_dec.x;
            this.star_dec = star_ra_dec.y;
            this.r = star_rc.x;
            this.c = star_rc.y;
            this.rows = rows;
            this.cols = cols;
        }

        public double f(double x1, double x2, double x3, double x4) {

            double ra_center = x1;
            double dec_center = x2;
            double angle = x3;
            double scale = x4;
            return CenterLocation.f(ra_center, dec_center, angle, scale, star_ra, star_dec, r, c, rows, cols).y;
        }
    }

    private boolean is_close_f_to_zero_4(V4 fs) {
        double h = 1;
        double[] a = fs.getArray();
        double f1 = a[0];
        double f2 = a[1];
        double f3 = a[2];
        double f4 = a[3];

        if (Math.abs(f1) < h && Math.abs(f2) < h && Math.abs(f3) < h && Math.abs(f4) < h)
            return true;
        else
            return false;
    }

    public V4 get() {

        Eq4 eq = new Eq4(new F1_param(star1_radec, star1_rc, rows, cols),
                new F2_param(star1_radec, star1_rc, rows, cols),
                new F1_param(star2_radec, star2_rc, rows, cols),
                new F2_param(star2_radec, star2_rc, rows, cols));

        Holder2<V4, V4> h = eq.next(new V4(ra_center, dec_center, angle, scale));

        for (int i = 0; i < 20; i++) {
            h = eq.next(h.x);
            if (is_close_f_to_zero_4(h.y))
                break;

        }


        if (is_close_f_to_zero_4(h.y))
            return h.x;
        else
            return null;

    }

}
