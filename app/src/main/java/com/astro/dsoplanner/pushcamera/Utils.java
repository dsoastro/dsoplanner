package com.astro.dsoplanner.pushcamera;

import java.util.ArrayList;
import java.util.List;

import com.astro.dsoplanner.matrix.DVector;
import com.astro.dsoplanner.util.Holder2;

public class Utils {

    public static double dst_equ(P p1, P p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    public static double dst_equ(P2i p1, P2i p2) {
        return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));
    }

    public static double dst_angle(P p1, P p2) {
        double ra1 = p1.x;
        double dec1 = p1.y;
        double ra2 = p2.x;
        double dec2 = p2.y;
        double PI = Math.PI;
        double cosd = Math.sin(dec1 * PI / 180) * Math.sin(dec2 * PI / 180) + Math.cos(dec1 * PI / 180) * Math.cos(dec2 * PI / 180) * Math.cos((ra1 - ra2) * PI / 12);
        if (cosd <= 1)
            return Math.acos(cosd) * 180 / PI;
        else
            return 0;

    }

    public static P new_xy(double angle, P p) {
        double x = p.x;
        double y = p.y;
        double cos_angle = Math.cos(angle);
        double sin_angle = Math.sin(angle);
        double x1 = x * cos_angle + y * sin_angle;
        double y1 = y * cos_angle - x * sin_angle;
        return new P(x1, y1);
    }

    public static P3 get_xyz_from_ra_dec(P p) {
        double ra = p.x;
        double dec = p.y;
        ra = ra * Math.PI / 12;
        dec = dec * Math.PI / 180;
        double cos_dec = Math.cos(dec);
        double x = cos_dec * Math.cos(ra);
        double y = cos_dec * Math.sin(ra);
        double z = Math.sin(dec);
        return new P3(x, y, z);
    }

    public static P get_ra_dec_from_xyz(P3 p) {
        double dec = Math.asin(p.z) * 180 / Math.PI;
        double ra = Math.atan2(p.y, p.x) * 12 / Math.PI;
        return new P(ra, dec);
    }

    /**
     * @param p az alt in grad
     * @return
     */
    public static P3 get_xyz_from_az_alt(P p) {

        double az = p.x * Math.PI / 180;
        double alt = p.y * Math.PI / 180;
        double cos_alt = Math.cos(alt);
        double x = cos_alt * Math.sin(az);
        double y = cos_alt * Math.cos(az);
        double z = Math.sin(alt);
        return new P3(x, y, z);
    }

    public static P projection_to_plane(P p) {
        P3 p3 = get_xyz_from_ra_dec(p);

        double x = p3.x;
        double y = p3.y;
        double z = p3.z;
        double yp = y / x;
        double zp = z / x;
        return new P(yp, zp);
    }

    public static P project(P p0, P p1) {
        double ra = p0.x;
        double dec = p0.y;
        double angle_ra = ra * Math.PI / 12;
        P3 p3 = get_xyz_from_ra_dec(p1);
        double x = p3.x;
        double y = p3.y;
        double z = p3.z;
        //rotate by ra
        double cos_angle_ra = Math.cos(angle_ra);
        double sin_angle_ra = Math.sin(angle_ra);

        double x1 = x * cos_angle_ra + y * sin_angle_ra;
        double y1 = y * cos_angle_ra - x * sin_angle_ra;
        double z1 = z;
        //rotate by dec
        double angle_dec = dec * Math.PI / 180;
        double cos_angle_dec = Math.cos(angle_dec);
        double sin_angle_dec = Math.sin(angle_dec);

        double x2 = x1 * cos_angle_dec + z1 * sin_angle_dec;
        double y2 = y1;
        double z2 = z1 * cos_angle_dec - x1 * sin_angle_dec;

        double yp = -y2 / x2;
        double zp = z2 / x2;
        return new P(yp, zp);
    }

    public static P4 hash(P p1, P p2, P p3, P p4) {
        double x1 = p1.x;
        double y1 = p1.y;
        double x2 = p2.x;
        double y2 = p2.y;
        double x3 = p3.x;
        double y3 = p3.y;
        double x4 = p4.x;
        double y4 = p4.y;
        x2 = x2 - x1;
        y2 = y2 - y1;
        x3 = x3 - x1;
        y3 = y3 - y1;
        x4 = x4 - x1;
        y4 = y4 - y1;
        x1 = 0;
        y1 = 0;

        double an = Math.atan2(y4, x4);

        double rotate_an = -(Math.PI / 4 - an);

        P t = new_xy(rotate_an, new P(x2, y2));
        x2 = t.x;
        y2 = t.y;
        t = new_xy(rotate_an, new P(x3, y3));
        x3 = t.x;
        y3 = t.y;
        t = new_xy(rotate_an, new P(x4, y4));
        x4 = t.x;
        y4 = t.y;

        x2 /= x4;
        y2 /= x4;
        x3 /= x4;
        y3 /= x4;
        y4 /= x4;
        x4 /= x4;

        if (dst_equ(new P(x2, y2), new P(0.5, 0.5)) > 1 || dst_equ(new P(x3, y3), new P(0.5, 0.5)) > 1)
            return null;
        else
            return new P4(x2, y2, x3, y3);
    }

    public static Holder2<P4, P4i> normalise_h_and_index(P4 h, P4i ids) {
        if (h == null)
            return null;
        double x1 = h.x;
        double x2 = h.y;
        double x3 = h.z;
        double x4 = h.t;

        int id1 = ids.x;
        int id2 = ids.y;
        int id3 = ids.z;
        int id4 = ids.t;

        if (x1 > x3) {
            double a = x3;
            double b = x4;
            double c = x1;
            double d = x2;

            x1 = a;
            x2 = b;
            x3 = c;
            x4 = d;

            int tmp = id3;
            id3 = id2;
            id2 = tmp;
        }
        return new Holder2<P4, P4i>(new P4(x1, x2, x3, x4), new P4i(id1, id2, id3, id4));

    }

    public static List<Holder2<P4, P4i>> hash_combinations(int id1, int id2, int id3, int id4, P p1, P p2, P p3, P p4) {

        List<Holder2<P4, P4i>> result = new ArrayList<>();
        result.add(normalise_h_and_index(hash(p1, p2, p3, p4), new P4i(id1, id2, id3, id4)));
        result.add(normalise_h_and_index(hash(p2, p4, p1, p3), new P4i(id2, id4, id1, id3)));
        result.add(normalise_h_and_index(hash(p4, p3, p2, p1), new P4i(id4, id3, id2, id1)));
        result.add(normalise_h_and_index(hash(p3, p1, p4, p2), new P4i(id3, id1, id4, id2)));
        return result;
    }

    /**
     * Intersection of p1 - p2 with p3 - p4
     */
    public static boolean is_intersect(P p1, P p2, P p3, P p4) {
        //p1 ->p2 * p1->p3, p1 -> p2 * p1 -> p4
        P p1p2 = p2.minus(p1);
        P p1p3 = p3.minus(p1);
        P p1p4 = p4.minus(p1);

        P3 v1 = vector_mul(new P3(p1p2.x, p1p2.y, 0), new P3(p1p3.x, p1p3.y, 0));
        P3 v2 = vector_mul(new P3(p1p2.x, p1p2.y, 0), new P3(p1p4.x, p1p4.y, 0));

        boolean same1 = (v1.z * v2.z > 0); //p3 and p4 lie on the same side of segment p1-p2

        //p3->p4 * p3->p2, p3->p4 * p3->p1
        P p3p4 = p4.minus(p3);
        P p3p2 = p2.minus(p3);
        P p3p1 = p1.minus(p3);

        P3 v3 = vector_mul(new P3(p3p4.x, p3p4.y, 0), new P3(p3p2.x, p3p2.y, 0));
        P3 v4 = vector_mul(new P3(p3p4.x, p3p4.y, 0), new P3(p3p1.x, p3p1.y, 0));

        boolean same2 = (v3.z * v4.z > 0);
        return (!same1) && (!same2);


    }

    public static P3 vector_mul(P3 a, P3 b) {
        double ax = a.x;
        double ay = a.y;
        double az = a.z;
        double bx = b.x;
        double by = b.y;
        double bz = b.z;
        return new P3(ay * bz - az * by, az * bx - ax * bz, ax * by - ay * bx);
    }

    public static DVector vector_mul(DVector a, DVector b) {
        double ax = a.x;
        double ay = a.y;
        double az = a.z;
        double bx = b.x;
        double by = b.y;
        double bz = b.z;
        return new DVector(ay * bz - az * by, az * bx - ax * bz, ax * by - ay * bx);
    }

    public static P norm_ra_dec(double ra, double dec) {

        dec = dec * Math.PI / 180;
        ra = ra * Math.PI / 12;
        double z = Math.sin(dec);
        double x = Math.cos(dec) * Math.cos(ra);
        double y = Math.cos(dec) * Math.sin(ra);
        double ra2 = Math.atan2(y, x);
        if (ra2 < 0)
            ra2 += 2 * Math.PI;
        double dec2 = Math.asin(z);
        return new P(ra2 * 12 / Math.PI, dec2 * 180 / Math.PI);
    }

    public static boolean is_convex(P p1, P p2, P p3, P p4) {

        return is_intersect(p1, p4, p2, p3);

    }

    public static List<P4i> permutations(int id1, int id2, int id3, int id4) {
        List<P4i> list = new ArrayList<>();
        list.add(new P4i(id1, id2, id3, id4));
        list.add(new P4i(id1, id2, id4, id3));
        list.add(new P4i(id1, id3, id2, id4));
        list.add(new P4i(id1, id3, id4, id2));
        list.add(new P4i(id1, id4, id2, id3));
        list.add(new P4i(id1, id4, id3, id2));
        list.add(new P4i(id2, id1, id3, id4));
        list.add(new P4i(id2, id1, id4, id3));
        list.add(new P4i(id2, id3, id1, id4));
        list.add(new P4i(id2, id3, id4, id1));
        list.add(new P4i(id2, id4, id1, id3));
        list.add(new P4i(id2, id4, id3, id1));
        list.add(new P4i(id3, id1, id2, id4));
        list.add(new P4i(id3, id1, id4, id2));
        list.add(new P4i(id3, id2, id1, id4));
        list.add(new P4i(id3, id2, id4, id1));
        list.add(new P4i(id3, id4, id1, id2));
        list.add(new P4i(id3, id4, id2, id1));
        list.add(new P4i(id4, id1, id2, id3));
        list.add(new P4i(id4, id1, id3, id2));
        list.add(new P4i(id4, id2, id1, id3));
        list.add(new P4i(id4, id2, id3, id1));
        list.add(new P4i(id4, id3, id1, id2));
        list.add(new P4i(id4, id3, id2, id1));
        return list;

    }

    /**
     * @return list of combinations by 3
     */
    public static List<List<Integer>> combinations(List<Integer> list, int level) {
        if (level == 1) {
            List<List<Integer>> result = new ArrayList<>();
            for (Integer e : list) {
                List<Integer> le = new ArrayList<>();
                le.add(e);
                result.add(le);
            }
            return result;
        }
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            List<Integer> copy = new ArrayList<>();
            for (int j = i + 1; j < list.size(); j++) {
                copy.add(list.get(j));
            }
            List<List<Integer>> res = combinations(copy, level - 1);
            for (List<Integer> l : res) {
                l.add(0, list.get(i));
                result.add(l);
            }

        }
        return result;

    }

    /**
     * add 3 point combinations from list to fixed point to get combinations of 4 points
     *
     * @param fixed
     * @param list
     * @return
     */
    public static List<P4i> get_combinations(int fixed, List<Integer> list) {

        List<List<Integer>> res = combinations(list, 3);
        List<P4i> result = new ArrayList<>();
        for (List<Integer> l : res) {
            P4i p = new P4i(fixed, l.get(0), l.get(1), l.get(2));
            result.add(p);
        }
        return result;
    }

    //assume h component is from -1.5 to 1.5
    static double step = 0.05;
    static int N = 60; // 3/0.05
    static int PRIME = 99971;

    public static int get_hash_quick_id(P4 h) {
        double h1 = h.x;
        double h2 = h.y;
        double h3 = h.z;
        double h4 = h.t;

        int i1 = (int) ((h1 + 1.5) / step);
        int i2 = (int) ((h2 + 1.5) / step);
        int i3 = (int) ((h3 + 1.5) / step);
        int i4 = (int) ((h4 + 1.5) / step);
        int id_ = i1 * N * N * N + i2 * N * N + i3 * N + i4;
        int pos = id_ % PRIME;
        return pos;
    }

    public static List<Integer> get_adj_hash_ids(P4 h) {
        double h1 = h.x;
        double h2 = h.y;
        double h3 = h.z;
        double h4 = h.t;
        int i1 = (int) ((h1 + 1.5) / step);
        int i2 = (int) ((h2 + 1.5) / step);
        int i3 = (int) ((h3 + 1.5) / step);
        int i4 = (int) ((h4 + 1.5) / step);

        int i1_min = i1 == 0 ? 0 : i1 - 1;//   0 if i1 == 0 else i1 - 1
        int i2_min = i2 == 0 ? 0 : i2 - 1; //0 if i2 == 0 else i2 - 1
        int i3_min = i3 == 0 ? 0 : i3 - 1; //0 if i3 == 0 else i3 - 1
        int i4_min = i4 == 0 ? 0 : i4 - 1; //0 if i4 == 0 else i4 - 1

        int i1_max = i1 == N ? N : i1 + 1;// N if i1 == N else i1 + 1
        int i2_max = i2 == N ? N : i2 + 1; //N if i2 == N else i2 + 1
        int i3_max = i3 == N ? N : i3 + 1; //N if i3 == N else i3 + 1
        int i4_max = i4 == N ? N : i4 + 1; //N if i4 == N else i4 + 1

        List<Integer> alist = new ArrayList<>();
        for (int a = i1_min; a < i1_max + 1; a++) {
            for (int b = i2_min; b < i2_max + 1; b++) {
                for (int c = i3_min; c < i3_max + 1; c++) {
                    for (int d = i4_min; d < i4_max + 1; d++) {
                        int id_ = a * N * N * N + b * N * N + c * N + d;
                        int pos = id_ % PRIME;
                        alist.add(pos);
                    }
                }
            }
        }

        return alist;
    }

    public static int get_dbscan_id(int eps, P2i p) {


        int x1 = p.x;
        int x2 = p.y;
        int i1 = (int) (x1 / eps);
        int i2 = (int) (x2 / eps);
        int id_ = (31 + i1) * 9973 + i2;
        int pos = id_ % PRIME;
        return pos;
    }

    public static List<Integer> get_adj_dbscan_ids(int rows, int cols, int eps, P2i p) {

        int x1 = p.x;
        int x2 = p.y;
        int i1 = (int) (x1 / eps);
        int i2 = (int) (x2 / eps);

        int N1 = (int) (rows / eps);
        int N2 = (int) (cols / eps);

        int i1_min = i1 == 0 ? 0 : i1 - 1;
        int i2_min = i2 == 0 ? 0 : i2 - 1;

        int i1_max = i1 == N1 ? N1 : i1 + 1;
        int i2_max = i2 == N2 ? N2 : i2 + 1;


        List<Integer> alist = new ArrayList<Integer>();
        for (int a = i1_min; a < i1_max + 1; a++) {
            for (int b = i2_min; b < i2_max + 1; b++) {
                int id_ = (31 + a) * 9973 + b;
                int pos = id_ % PRIME;
                alist.add(pos);
            }
        }
        return alist;
    }

    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);

        List<P4i> result = get_combinations(5, list);
        for (P4i p : result) {
            System.out.println(p);
        }
    }


}
