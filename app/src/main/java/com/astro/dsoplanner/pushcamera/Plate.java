package com.astro.dsoplanner.pushcamera;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.graph.camera.Prefs;
import com.astro.dsoplanner.util.Holder2;
import com.astro.dsoplanner.Logg;

public class Plate {
    private static final String TAG = Cluster.TAG;

    int MAX_DISTANCE = 1000;
    double H = 0.05;
    int rows = 3024;
    int cols = 4032;
    double dst_max = 0.2;
    int TIME_LIMIT = 5000;
    public static double MAX_MAG = 3.5;

    Map<Integer, P> points;
    Map<Integer, StarEntry> stars_db;
    Map<Integer, List<Holder2<P4, P4i>>> qf;

    double lst;
    double lat;
    double gyro_alt;
    boolean checkalt;

    // {(id1,id2,id3,id4):(h1, h2, h3, h4)}
    // id - cluster id
    Map<P4i, P4> hmap = new HashMap<>();

    class CheckCoordsItem {
        int total_count;
        int total_good;
        double ra_center;
        double dec_center;
        double angle;
        double scale;
        List<P2i> point_hrs; //[ (point_id, hr) ]
        P4i local_index;

        public CheckCoordsItem(int total_count, int total_good, double ra_center, double dec_center, double angle,
                               double scale, List<P2i> point_hrs, P4i local_index) {
            super();
            this.total_count = total_count;
            this.total_good = total_good;
            this.ra_center = ra_center;
            this.dec_center = dec_center;
            this.angle = angle;
            this.scale = scale;
            this.point_hrs = point_hrs;
            this.local_index = local_index;
        }

    }

    public Plate(Holder2<P2i, Map<Integer, P>> h, int MAX_DISTANCE, double lst, double lat, double gyro_alt, boolean check_alt) throws Exception {
        P2i rc = h.x;
        rows = rc.x;
        cols = rc.y;
        points = h.y;
        this.MAX_DISTANCE = MAX_DISTANCE;
        long t1 = Calendar.getInstance().getTimeInMillis();
        stars_db = DbBuilder.loadStarsDb();
        qf = DbBuilder.loadQf();
        long t2 = Calendar.getInstance().getTimeInMillis();
        print("plate loading db, time=" + (t2 - t1));
        this.lst = lst;
        this.lat = lat;
        this.checkalt = check_alt;
        this.gyro_alt = gyro_alt;
        Logg.d(TAG, "Plate, lst=" + lst + " lat=" + lat + " gyro_alt=" + gyro_alt + " check_alt=" + check_alt);

    }

    private Holder2<Double, Integer> get_close_star_dst_old(double ra_center, double dec_center, double angle, double scale, double r, double c) {
        V2 radec = new StarRaDec(rows, cols, r, c, ra_center, dec_center, angle, scale, ra_center, dec_center).get();

        if (radec == null)
            return new Holder2<Double, Integer>(1000., -1);

        double star_ra = radec.get(0);
        double star_dec = radec.get(1);
        double min_dst = 1000000;
        int min_star_id = 0;
        for (Map.Entry<Integer, StarEntry> e : stars_db.entrySet()) {
            int k = e.getKey();
            StarEntry se = e.getValue();
            double ra = se.ra;
            double dec = se.dec;
            double dst = Utils.dst_angle(new P(ra, dec), new P(star_ra, star_dec));
            if (dst < min_dst) {
                min_dst = dst;
                min_star_id = k;
            }
        }
        return new Holder2<Double, Integer>(min_dst, min_star_id);
    }

    private void add_to_hmap(int id1, int id2, int id3, int id4) {
        boolean flag = false;
        List<P4i> permutations = Utils.permutations(id1, id2, id3, id4);
        for (P4i a : permutations) {
            if (hmap.get(a) != null) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            for (P4i a : permutations) {
                int i1 = a.x;
                int i2 = a.y;
                int i3 = a.z;
                int i4 = a.t;

                P p1_ = points.get(i1);
                P p1 = new P(p1_.y, -p1_.x);

                P p2_ = points.get(i2);
                P p2 = new P(p2_.y, -p2_.x);

                P p3_ = points.get(i3);
                P p3 = new P(p3_.y, -p3_.x);

                P p4_ = points.get(i4);
                P p4 = new P(p4_.y, -p4_.x);

                if (Utils.is_convex(p1, p2, p3, p4)) {
                    List<Holder2<P4, P4i>> res = Utils.hash_combinations(i1, i2, i3, i4, p1, p2, p3, p4);
                    for (Holder2<P4, P4i> r : res) {
                        if (r != null) {
                            P4 h = r.x;
                            P4i key = r.y;
                            hmap.put(key, h);
                        }
                    }
                    break;
                }
            }
        }
    }

    private List<Holder2<P4i, P4>> get_close_hashes(P4 h) {

        List<Integer> poss = Utils.get_adj_hash_ids(h);
        List<Holder2<P4i, P4>> alist = new ArrayList<>();

        for (int pos : poss) {
            List<Holder2<P4, P4i>> vals = qf.get(pos);
            if (vals == null)
                continue;
            for (Holder2<P4, P4i> v : vals) {
                P4 hh = v.x;
                P4i key = v.y;
                alist.add(new Holder2<P4i, P4>(key, hh));
            }
        }
        return alist;
    }

    private boolean cmp(P4 h1, P4 h2) {
        double x1 = h1.x;
        double x2 = h1.y;
        double x3 = h1.z;
        double x4 = h1.t;

        double y1 = h2.x;
        double y2 = h2.y;
        double y3 = h2.z;
        double y4 = h2.t;


        if (Math.abs(x1 - y1) < H && Math.abs(x2 - y2) < H && Math.abs(x3 - y3) < H && Math.abs(x4 - y4) < H)
            return true;
        else
            return false;
    }

    private double cmp_coords(double ra_center, double dec_center, double angle, double scale, int local_star_index, int global_star_index) {
        P star_rc = points.get(local_star_index);
        StarEntry se = stars_db.get(global_star_index);
        double star_ra_in_stars_db = se.ra;
        double star_dec_in_stars_db = se.dec;
        StarRaDec star_ra_dec = new StarRaDec(rows, cols, star_rc.x, star_rc.y, ra_center, dec_center,
                angle, scale, star_ra_in_stars_db, star_dec_in_stars_db);
        V2 radec = star_ra_dec.get();
        if (radec == null) {
            return 1000;
        }
        double va[] = radec.getArray();
        double dst = Utils.dst_angle(new P(star_ra_in_stars_db, star_dec_in_stars_db), new P(va[0], va[1]));
        return dst;

    }

    private String key_to_string(P4i key) {
        String s = "HRs: " + stars_db.get(key.x).hr + " " + stars_db.get(key.y).hr
                + " " + stars_db.get(key.z).hr + " " + stars_db.get(key.t).hr;

        return s;
    }

    private boolean isAboveHorizon(double ra, double dec) {
        double alt = AstroTools.Altitude(lst, lat, ra, dec);
        if (alt > 0)
            return true;
        else
            return false;
    }

    private boolean checkAltAgainstGyro(double ra_center, double dec_center) {
        //az alt from the center of image
        double plate_alt = AstroTools.Altitude(lst, lat, ra_center, dec_center);
        P n_ra_dec = Utils.norm_ra_dec(ra_center, dec_center);
        Logg.d(TAG, "checkAltAgainstGyro, lat=" + lat + " lst=" + lst + " gyro_alt=" + gyro_alt + " plate_alt=" + plate_alt + " ra_center=" + n_ra_dec.x + " dec_center=" + n_ra_dec.y);
        if (Math.abs(gyro_alt - plate_alt) < Prefs.ALT_ACCURACY)
            return true;
        else
            return false;
    }

    private CheckCoordsItem check_coords(P4i local_index, P4i global_index) {
        int l1 = local_index.x;
        int l2 = local_index.y;
        int l3 = local_index.z;
        int l4 = local_index.t;

        int id1 = global_index.x;
        int id2 = global_index.y;
        int id3 = global_index.z;
        int id4 = global_index.t;

        P star1_radec = new P(stars_db.get(id1).ra, stars_db.get(id1).dec);
        P star2_radec = new P(stars_db.get(id4).ra, stars_db.get(id4).dec);
        int[] ids = {id1, id2, id3, id4};
        if (checkalt) {
            for (int id : ids) {
                StarEntry e = stars_db.get(id);
                if (!isAboveHorizon(e.ra, e.dec)) {
                    return null;
                } else {

                }
            }
        }

        P star1_rc = points.get(l1);
        P star2_rc = points.get(l4);
        CenterLocation cl = new CenterLocation(rows, cols, star1_radec.x, star1_radec.y, 0, 1000, star1_radec, star2_radec, star1_rc, star2_rc);
        V4 center_loc = cl.get();
        if (center_loc == null)
            return null;

        double ra_center = center_loc.get(0);
        double dec_center = center_loc.get(1);
        double angle = center_loc.get(2);
        double scale = center_loc.get(3);

        double dst1 = cmp_coords(ra_center, dec_center, angle, scale, l2, id2);
        double dst2 = cmp_coords(ra_center, dec_center, angle, scale, l3, id3);
        if (dst1 < dst_max && dst2 < dst_max) {
            print(local_index + " " + key_to_string(global_index));
            P n_ra_dec = Utils.norm_ra_dec(ra_center, dec_center);
            String message1 = String.format("center ra %.4f, dec %.4f, angle %.4f, scale %.4f", n_ra_dec.x, n_ra_dec.y, angle, scale);
            String message2 = String.format("error in position of second star %.2f, third star %.2f", dst1, dst2);
            print(message1);
            print(message2);

            int total_count = points.size() - 4;
            int total_good = 0;

            List<P2i> point_hrs = new ArrayList<>(); //[ (point_id, hr) ]
            for (Map.Entry<Integer, P> e : points.entrySet()) {
                int k = e.getKey();
                P rc = e.getValue();
                //(double ra_center, double dec_center, double angle, double scale, double r, double c)
                Holder2<Double, Integer> h2 = get_close_star_dst_old(ra_center, dec_center, angle, scale, rc.x, rc.y);
                double dst = h2.x;
                int id = h2.y;

                //check that hash stars do not change with get_close_star_dst_old
                int g_index = -1;
                if (k == l1)
                    g_index = id1;
                else if (k == l2)
                    g_index = id2;
                else if (k == l3)
                    g_index = id3;
                else if (k == l4)
                    g_index = id4;

                if (g_index != -1) { //4-hash point
                    if (g_index != id) { //should be equal for good match
                        print("4-hash point. different star ids, total_good=" + total_good);
                        return null;
                    }
                    if (dst >= 0.5) {
                        print("large distance for a 4-hash point");
                        return null;
                    }
                }


                if (k != l1 && k != l2 && k != l3 && k != l4 && dst < 0.5) {
                    total_good += 1;
                }

                if (id == -1)
                    continue;
                String message3 = String.format("%d star on image, db name %s, db id %d, error in position %.1f, "
                                + "star row %.4f, star column %.4f", k, "HR" + stars_db.get(id).hr,
                        id, dst, rc.x, rc.y);
                point_hrs.add(new P2i(k, stars_db.get(id).hr));
                print(message3);
            }
            print("total count " + total_count + ", total good " + total_good);
            return new CheckCoordsItem(total_count, total_good, ra_center, dec_center, angle, scale, point_hrs, local_index);

        } else
            return null;

    }

    private boolean is_center_good(int total_count, int total_good) {
        if (total_count == 1)
            return false;
        else if (total_count == 2)
            return (total_good == 2);
        else if (total_count > 2 && total_count <= 4)
            return (total_good >= total_count - 1);
        else
            return (total_good >= Math.min(total_count - 2, total_count * 0.4));
    }

    public P4 run() {
        // {id:[list of close points]}. close points
        Map<Integer, List<Integer>> cp = new HashMap<Integer, List<Integer>>();

        for (Map.Entry<Integer, P> e : points.entrySet()) {
            int k1 = e.getKey();
            P p1 = e.getValue();

            for (Map.Entry<Integer, P> e2 : points.entrySet()) {
                int k2 = e2.getKey();
                P p2 = e2.getValue();
                if (k1 == k2)
                    continue;
                if (Utils.dst_equ(p1, p2) < MAX_DISTANCE) {
                    if (cp.get(k1) == null)
                        cp.put(k1, new ArrayList<Integer>());
                    cp.get(k1).add(k2);
                }
            }
        }
        print("cp=" + cp);
        for (Map.Entry<Integer, List<Integer>> e : cp.entrySet()) {
            int id_ = e.getKey();
            List<Integer> l = e.getValue();
            List<List<Integer>> combinations = Utils.combinations(l, 3);
            for (List<Integer> c : combinations) {
                int id1 = c.get(0);
                int id2 = c.get(1);
                int id3 = c.get(2);
                add_to_hmap(id_, id1, id2, id3);
            }
        }
        int total = 0;
        int all_pics = 0;
        int right_pics = 0;
        int max_total_count = 0;
        double ra_center = 0;
        double dec_center = 0;
        double angle = 0;
        double scale = 0;

        for (Map.Entry<P4i, P4> e : hmap.entrySet()) {
            P4i local_index = e.getKey();
            P4 h1 = e.getValue();
            List<Holder2<P4i, P4>> close_hashes = get_close_hashes(h1);
            for (Holder2<P4i, P4> item : close_hashes) {
                P4i global_index = item.x;
                P4 h2 = item.y;
                if (cmp(h1, h2)) {
                    total += 1;
                    CheckCoordsItem info = check_coords(local_index, global_index);
                    if (info != null) {
                        all_pics += 1;
                        if (is_center_good(info.total_count, info.total_good)) {
                            right_pics += 1;
                            if (info.total_count > max_total_count) {
                                max_total_count = info.total_count;
                                ra_center = info.ra_center;
                                dec_center = info.dec_center;
                                angle = info.angle;
                                scale = info.scale;
                            }
                        }
                    }
                }
            }
        }
        print("all pics " + all_pics + " right pics " + right_pics);
        print("total first positive hashes considered " + total);
        if (max_total_count > 0) {
            String s = String.format("Plate solved. ra center %.4f, dec center %.4f, angle %.4f, scale %.4f", ra_center, dec_center, angle, scale);
            print(s);
            return new P4(ra_center, dec_center, angle, scale);
        } else {
            print("Plate NOT solved");
            return null;
        }


    }

    /**
     * @return ra_center, dec_center, angle, scale, null if interrupted
     */
    public P4 find_center() {
        long start = Calendar.getInstance().getTimeInMillis();
        // {id:[list of close points]}. close points
        Map<Integer, List<Integer>> cp = new HashMap<Integer, List<Integer>>();

        for (Map.Entry<Integer, P> e : points.entrySet()) {
            int k1 = e.getKey();
            P p1 = e.getValue();

            for (Map.Entry<Integer, P> e2 : points.entrySet()) {
                int k2 = e2.getKey();
                P p2 = e2.getValue();
                if (k1 == k2)
                    continue;
                if (Utils.dst_equ(p1, p2) < MAX_DISTANCE) {
                    if (cp.get(k1) == null)
                        cp.put(k1, new ArrayList<Integer>());
                    cp.get(k1).add(k2);
                }
            }
        }
        print("cp=" + cp);
        for (Map.Entry<Integer, List<Integer>> e : cp.entrySet()) {
            int id_ = e.getKey();
            List<Integer> l = e.getValue();
            List<List<Integer>> combinations = Utils.combinations(l, 3);
            for (List<Integer> c : combinations) {
                int id1 = c.get(0);
                int id2 = c.get(1);
                int id3 = c.get(2);
                add_to_hmap(id_, id1, id2, id3);
            }
        }


        List<CheckCoordsItem> centers = new ArrayList<>();
        for (Map.Entry<P4i, P4> e : hmap.entrySet()) {
            P4i local_index = e.getKey();
            P4 h1 = e.getValue();
            List<Holder2<P4i, P4>> close_hashes = get_close_hashes(h1);
            int i = 0;
            for (Holder2<P4i, P4> item : close_hashes) {
                if (Thread.currentThread().isInterrupted())
                    return null;
                long now = Calendar.getInstance().getTimeInMillis();

                if (now - start > TIME_LIMIT) {
                    print("find_center, time limit, i=" + i + " close_hashes size=" + close_hashes.size());
                    if (checkalt && !Double.isNaN(gyro_alt))
                        return null;
                    else
                        return find_center_helper(centers);
                }

                P4i global_index = item.x;
                P4 h2 = item.y;
                if (cmp(h1, h2)) {

                    CheckCoordsItem info = check_coords(local_index, global_index);
                    if (info != null) {
                        if (checkalt && !Double.isNaN(gyro_alt)) {
                            if (info.total_good >= 1) {
                                if (checkAltAgainstGyro(info.ra_center, info.dec_center)) {
                                    return new P4(info.ra_center, info.dec_center, info.angle, info.scale);
                                }
                            }
                        } else {

                            if (is_center_good(info.total_count, info.total_good)) {
                                return new P4(info.ra_center, info.dec_center, info.angle, info.scale);
                            } else if (info.total_good >= 1) {
                                //used for looking for 4-hashes with close ra dec
                                centers.add(info);
                            }
                        }
                    }
                }
                i++;
            }
        }
        if (checkalt && !Double.isNaN(gyro_alt))
            return null;
        else
            return find_center_helper(centers);
    }


    /**
     * @param hr
     * @return row col of hr star on image, ra_center dec_center, null if interrupted
     */
    public P4 find_eyepiece_star_on_plate(int hr) {
        long start = Calendar.getInstance().getTimeInMillis();
        // {id:[list of close points]}. close points
        Map<Integer, List<Integer>> cp = new HashMap<Integer, List<Integer>>();

        for (Map.Entry<Integer, P> e : points.entrySet()) {
            int k1 = e.getKey();
            P p1 = e.getValue();

            for (Map.Entry<Integer, P> e2 : points.entrySet()) {
                int k2 = e2.getKey();
                P p2 = e2.getValue();
                if (k1 == k2)
                    continue;
                if (Utils.dst_equ(p1, p2) < MAX_DISTANCE) {
                    if (cp.get(k1) == null)
                        cp.put(k1, new ArrayList<Integer>());
                    cp.get(k1).add(k2);
                }
            }
        }
        print("cp=" + cp);
        for (Map.Entry<Integer, List<Integer>> e : cp.entrySet()) {
            int id_ = e.getKey();
            List<Integer> l = e.getValue();
            List<List<Integer>> combinations = Utils.combinations(l, 3);
            for (List<Integer> c : combinations) {
                int id1 = c.get(0);
                int id2 = c.get(1);
                int id3 = c.get(2);
                add_to_hmap(id_, id1, id2, id3);
            }
        }
        List<CheckCoordsItem> centers = new ArrayList<>();
        for (Map.Entry<P4i, P4> e : hmap.entrySet()) {
            P4i local_index = e.getKey();
            P4 h1 = e.getValue();
            List<Holder2<P4i, P4>> close_hashes = get_close_hashes(h1);
            int i = 0;
            for (Holder2<P4i, P4> item : close_hashes) {
                if (Thread.currentThread().isInterrupted())
                    return null;
                long now = Calendar.getInstance().getTimeInMillis();
                if (now - start > TIME_LIMIT) {
                    print("find_eyepiece_star_on_plate, time limit, i=" + i + " close_hashes size=" + close_hashes.size());
                    if (checkalt && !Double.isNaN(gyro_alt))
                        return null;
                    else
                        return find_eyepiece_star_on_plate_helper(centers, hr);
                }
                P4i global_index = item.x;
                P4 h2 = item.y;
                if (cmp(h1, h2)) {
                    CheckCoordsItem info = check_coords(local_index, global_index);
                    if (info != null) {
                        if (checkalt && !Double.isNaN(gyro_alt)) {
                            if (info.total_good >= 1) {
                                if (checkAltAgainstGyro(info.ra_center, info.dec_center)) {
                                    for (P2i p : info.point_hrs) {
                                        int k = p.x;
                                        int hr_ = p.y;
                                        if (hr == hr_) {
                                            double r = points.get(k).x;
                                            double c = points.get(k).y;

                                            return new P4(r, c, info.ra_center, info.dec_center);
                                        }
                                    }
                                    print("hr=" + hr + " not found for good 4-hash " + local_index + " " + global_index);
                                    return null;
                                }
                            }
                        } else {
                            if (is_center_good(info.total_count, info.total_good)) {
                                for (P2i p : info.point_hrs) {
                                    int k = p.x;
                                    int hr_ = p.y;
                                    if (hr == hr_) {
                                        double r = points.get(k).x;
                                        double c = points.get(k).y;

                                        return new P4(r, c, info.ra_center, info.dec_center);
                                    }
                                }
                                print("hr=" + hr + " not found for good 4-hash " + local_index + " " + global_index);
                                return null;
                            } else if (info.total_good >= 1) {
                                //used for looking for 4-hashes with close ra dec
                                centers.add(info);
                            }
                        }
                    }
                }
                i++;
            }
        }
        if (checkalt && !Double.isNaN(gyro_alt))
            return null;
        else
            return find_eyepiece_star_on_plate_helper(centers, hr);
    }

    public P find_eyepiece_object_on_plate(double ra, double dec) {
        long start = Calendar.getInstance().getTimeInMillis();
        // {id:[list of close points]}. close points
        Map<Integer, List<Integer>> cp = new HashMap<Integer, List<Integer>>();

        for (Map.Entry<Integer, P> e : points.entrySet()) {
            int k1 = e.getKey();
            P p1 = e.getValue();

            for (Map.Entry<Integer, P> e2 : points.entrySet()) {
                int k2 = e2.getKey();
                P p2 = e2.getValue();
                if (k1 == k2)
                    continue;
                if (Utils.dst_equ(p1, p2) < MAX_DISTANCE) {
                    if (cp.get(k1) == null)
                        cp.put(k1, new ArrayList<Integer>());
                    cp.get(k1).add(k2);
                }
            }
        }
        print("cp=" + cp);
        for (Map.Entry<Integer, List<Integer>> e : cp.entrySet()) {
            int id_ = e.getKey();
            List<Integer> l = e.getValue();
            List<List<Integer>> combinations = Utils.combinations(l, 3);
            for (List<Integer> c : combinations) {
                int id1 = c.get(0);
                int id2 = c.get(1);
                int id3 = c.get(2);
                add_to_hmap(id_, id1, id2, id3);
            }
        }
        List<CheckCoordsItem> centers = new ArrayList<>();
        for (Map.Entry<P4i, P4> e : hmap.entrySet()) {
            P4i local_index = e.getKey();
            P4 h1 = e.getValue();
            List<Holder2<P4i, P4>> close_hashes = get_close_hashes(h1);
            int i = 0;
            for (Holder2<P4i, P4> item : close_hashes) {
                long now = Calendar.getInstance().getTimeInMillis();
                if (now - start > TIME_LIMIT) {
                    print("find_eyepiece_star_on_plate, time limit, i=" + i + " close_hashes size=" + close_hashes.size());
                    return find_eyepiece_object_on_plate_helper(centers, ra, dec);
                }
                P4i global_index = item.x;
                P4 h2 = item.y;
                if (cmp(h1, h2)) {
                    CheckCoordsItem info = check_coords(local_index, global_index);
                    if (info != null) {
                        if (is_center_good(info.total_count, info.total_good)) {

                            P rc = CenterLocation.f0(info.ra_center, info.dec_center, info.angle, info.scale, ra, dec, rows, cols);
                            return rc;

                        } else if (info.total_count >= 1) {
                            //used for looking for 4-hashes with close ra dec
                            centers.add(info);
                        }
                    }
                }
                i++;
            }
        }
        return find_eyepiece_object_on_plate_helper(centers, ra, dec);
    }

    /**
     * solve the plate and return (ra,dec) of point (row,col)
     *
     * @param row
     * @param col
     * @return ra dec ra_center dec_center, null if interrupted
     */
    public P4 find_point_ra_dec(double row, double col) {
        long start = Calendar.getInstance().getTimeInMillis();
        // {id:[list of close points]}. close points
        Map<Integer, List<Integer>> cp = new HashMap<Integer, List<Integer>>();

        for (Map.Entry<Integer, P> e : points.entrySet()) {
            int k1 = e.getKey();
            P p1 = e.getValue();

            for (Map.Entry<Integer, P> e2 : points.entrySet()) {
                int k2 = e2.getKey();
                P p2 = e2.getValue();
                if (k1 == k2)
                    continue;
                if (Utils.dst_equ(p1, p2) < MAX_DISTANCE) {
                    if (cp.get(k1) == null)
                        cp.put(k1, new ArrayList<Integer>());
                    cp.get(k1).add(k2);
                }
            }
        }
        print("cp=" + cp);
        for (Map.Entry<Integer, List<Integer>> e : cp.entrySet()) {
            int id_ = e.getKey();
            List<Integer> l = e.getValue();
            List<List<Integer>> combinations = Utils.combinations(l, 3);
            for (List<Integer> c : combinations) {
                int id1 = c.get(0);
                int id2 = c.get(1);
                int id3 = c.get(2);
                add_to_hmap(id_, id1, id2, id3);
            }
        }
        int total = 0;
        int all_pics = 0;
        int right_pics = 0;
        int max_total_count = 0;
        double ra_center = 0;
        double dec_center = 0;
        double angle = 0;
        double scale = 0;
        List<CheckCoordsItem> centers = new ArrayList<>();
        for (Map.Entry<P4i, P4> e : hmap.entrySet()) {
            P4i local_index = e.getKey();
            P4 h1 = e.getValue();
            List<Holder2<P4i, P4>> close_hashes = get_close_hashes(h1);
            int i = 0;
            for (Holder2<P4i, P4> item : close_hashes) {
                if (Thread.currentThread().isInterrupted())
                    return null;
                long now = Calendar.getInstance().getTimeInMillis();
                if (now - start > TIME_LIMIT) {
                    print("find_point_ra_dec, time limit, i=" + i + " close_hashes size=" + close_hashes.size());
                    return null;
                }
                P4i global_index = item.x;
                P4 h2 = item.y;
                if (cmp(h1, h2)) {
                    total += 1;
                    CheckCoordsItem info = check_coords(local_index, global_index);
                    if (info != null) {
                        all_pics += 1;
                        if (checkalt && !Double.isNaN(gyro_alt)) {
                            if (info.total_good >= 1) {
                                if (checkAltAgainstGyro(info.ra_center, info.dec_center)) {
                                    StarRaDec sradec = new StarRaDec(rows, cols, row, col, info.ra_center,
                                            info.dec_center, info.angle, info.scale, info.ra_center, info.dec_center);

                                    V2 v2 = sradec.get();
                                    if (v2 != null) {
                                        double ra = v2.get(0);
                                        double dec = v2.get(1);
                                        return new P4(ra, dec, info.ra_center, info.dec_center);
                                    } else {
                                        print("point, row=" + row + " col=" + col + " not found for good 4-hash " + local_index + " " + global_index);
                                        return null;
                                    }
                                }
                            }
                        } else {

                            if (is_center_good(info.total_count, info.total_good)) {

                                StarRaDec sradec = new StarRaDec(rows, cols, row, col, info.ra_center,
                                        info.dec_center, info.angle, info.scale, info.ra_center, info.dec_center);

                                V2 v2 = sradec.get();
                                if (v2 != null) {
                                    double ra = v2.get(0);
                                    double dec = v2.get(1);
                                    return new P4(ra, dec, info.ra_center, info.dec_center);
                                } else {
                                    print("point, row=" + row + " col=" + col + " not found for good 4-hash " + local_index + " " + global_index);
                                    return null;
                                }
                            } else if (info.total_good >= 1) {
                                //used for looking for 4-hashes with close ra dec
                                centers.add(info);
                            }
                        }
                    }
                }
                i++;
            }
        }
        if (checkalt && !Double.isNaN(gyro_alt))
            return null;
        else
            return find_point_ra_dec_helper(centers, row, col);
    }

    /**
     * look for 4-hashes with close ra dec and calculate on their basis if they exist
     *
     * @return
     */
    private P4 find_point_ra_dec_helper(List<CheckCoordsItem> centers, double row, double col) {
        for (int i = 0; i < centers.size(); i++) {
            for (int j = i + 1; j < centers.size(); j++) {
                CheckCoordsItem item_first = centers.get(i);
                CheckCoordsItem item_second = centers.get(j);
                P4i lif = item_first.local_index;
                P4i lis = item_second.local_index;
                P first = new P(item_first.ra_center, item_first.dec_center);
                P second = new P(item_second.ra_center, item_second.dec_center);
                int l1 = lif.x;
                int l2 = lif.y;
                int l3 = lif.z;
                int l4 = lif.t;
                int k1 = lis.x;
                int k2 = lis.y;
                int k3 = lis.z;
                int k4 = lis.t;

                boolean same = (l1 == k4 && l4 == k1 && ((l2 == k3 && l3 == k2) || (l2 == k2 && l3 == k3)));

                if (!same && (Utils.dst_angle(first, second) < 0.5)) {
                    StarRaDec sradec = new StarRaDec(rows, cols, row, col, item_first.ra_center,
                            item_first.dec_center, item_first.angle, item_first.scale, item_first.ra_center, item_first.dec_center);

                    V2 v2 = sradec.get();
                    if (v2 != null) {
                        double ra = v2.get(0);
                        double dec = v2.get(1);
                        print("find_point_ra_dec_helper, 4-hashes" + lif + " " + lis + " ra=" + ra + " dec=" + dec);
                        return new P4(ra, dec, item_first.ra_center, item_first.dec_center);
                    } else {
                        print("point, row=" + row + " col=" + col + " not found for soft 4-hashes " + lif + " " + lis);

                    }

                }
            }
        }
        print("find_point_ra_dec_helper, not found");
        return null;
    }

    /**
     * look for 4-hashes with close ra dec and calculate on their basis if they exist
     *
     * @return
     */
    private P4 find_eyepiece_star_on_plate_helper(List<CheckCoordsItem> centers, int hr) {
        for (int i = 0; i < centers.size(); i++) {
            for (int j = i + 1; j < centers.size(); j++) {
                CheckCoordsItem item_first = centers.get(i);
                CheckCoordsItem item_second = centers.get(j);
                P4i lif = item_first.local_index;
                P4i lis = item_second.local_index;
                P first = new P(item_first.ra_center, item_first.dec_center);
                P second = new P(item_second.ra_center, item_second.dec_center);
                int l1 = lif.x;
                int l2 = lif.y;
                int l3 = lif.z;
                int l4 = lif.t;
                int k1 = lis.x;
                int k2 = lis.y;
                int k3 = lis.z;
                int k4 = lis.t;

                boolean same = (l1 == k4 && l4 == k1 && ((l2 == k3 && l3 == k2) || (l2 == k2 && l3 == k3)));

                if (!same && (Utils.dst_angle(first, second) < 0.5)) {
                    for (P2i p : item_first.point_hrs) {
                        int k = p.x;
                        int hr_ = p.y;
                        if (hr == hr_) {
                            print("find_eyepiece_star_on_plate_helper, 4-hashes" + lif + " " + lis + " hr=" + hr);
                            double r = points.get(k).x;
                            double c = points.get(k).y;
                            return new P4(r, c, item_first.ra_center, item_first.dec_center);
                        }

                    }
                    print("hr=" + hr + " not found for soft 4-hashes " + lif + " " + lis);


                }
            }
        }
        print("find_eyepiece_star_on_plate_helper, not found");
        return null;
    }

    /**
     * look for 4-hashes with close ra dec and calculate on their basis if they exist
     *
     * @return
     */
    private P find_eyepiece_object_on_plate_helper(List<CheckCoordsItem> centers, double ra, double dec) {
        for (int i = 0; i < centers.size(); i++) {
            for (int j = i + 1; j < centers.size(); j++) {
                CheckCoordsItem item_first = centers.get(i);
                CheckCoordsItem item_second = centers.get(j);
                P4i lif = item_first.local_index;
                P4i lis = item_second.local_index;
                P first = new P(item_first.ra_center, item_first.dec_center);
                P second = new P(item_second.ra_center, item_second.dec_center);
                int l1 = lif.x;
                int l2 = lif.y;
                int l3 = lif.z;
                int l4 = lif.t;
                int k1 = lis.x;
                int k2 = lis.y;
                int k3 = lis.z;
                int k4 = lis.t;

                boolean same = (l1 == k4 && l4 == k1 && ((l2 == k3 && l3 == k2) || (l2 == k2 && l3 == k3)));

                if (!same && (Utils.dst_angle(first, second) < 0.5)) {
                    P rc = CenterLocation.f0(item_first.ra_center, item_first.dec_center, item_first.angle, item_first.scale, ra, dec, rows, cols);
                    print("find_eyepiece_object_on_plate_helper, 4-hashes" + lif + " " + lis);

                    return rc;
                }
            }
        }
        print("find_eyepiece_object_on_plate_helper, not found");
        return null;
    }

    /**
     * look for 4-hashes with close ra dec and calculate on their basis if they exist
     *
     * @return
     */
    private P4 find_center_helper(List<CheckCoordsItem> centers) {
        for (int i = 0; i < centers.size(); i++) {
            for (int j = i + 1; j < centers.size(); j++) {
                CheckCoordsItem item_first = centers.get(i);
                CheckCoordsItem item_second = centers.get(j);
                P4i lif = item_first.local_index;
                P4i lis = item_second.local_index;
                P first = new P(item_first.ra_center, item_first.dec_center);
                P second = new P(item_second.ra_center, item_second.dec_center);
                int l1 = lif.x;
                int l2 = lif.y;
                int l3 = lif.z;
                int l4 = lif.t;
                int k1 = lis.x;
                int k2 = lis.y;
                int k3 = lis.z;
                int k4 = lis.t;

                boolean same = (l1 == k4 && l4 == k1 && ((l2 == k3 && l3 == k2) || (l2 == k2 && l3 == k3)));

                if (!same && (Utils.dst_angle(first, second) < 0.5)) {
                    P4 cent = new P4(item_first.ra_center, item_first.dec_center, item_first.angle, item_first.scale);
                    print("find_center_helper, 4-hashes" + lif + " " + lis);
                    return cent;
                }
            }
        }
        print("find_center_helper, not found");
        return null;
    }

    public static void print(String s) {
        Logg.d(Cluster.TAG, s);
    }

}
