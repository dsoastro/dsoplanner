package com.astro.dsoplanner.pushcamera;

import android.os.Build;
import android.support.annotation.RequiresApi;

import com.astro.dsoplanner.Logg;
import com.astro.dsoplanner.graph.camera.Camera2APIService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Calendar;

@RequiresApi(api = Build.VERSION_CODES.M)
class DbScan {
    private final static String TAG = Camera2APIService.TAG;
    List<P2i> P_list;
    int eps;
    int m;

    int rows;
    int cols;
    Map<Integer, List<P2i>> dbscan_hash_db;

    public final static int NOISE = 0;
    int C = 0;

    Set<P2i> visited_points = new HashSet<P2i>();
    Set<P2i> clustered_points = new HashSet<P2i>();
    Map<Integer, List<P2i>> clusters = new HashMap<>();


    //Constructor using fields
    //clusters = {NOISE: []}

    public DbScan(List<P2i> p_list, int eps, int m, int rows, int cols, Map<Integer, List<P2i>> dbscan_hash_db) {
        super();
        P_list = p_list;
        this.eps = eps;
        this.m = m;
        this.rows = rows;
        this.cols = cols;
        this.dbscan_hash_db = dbscan_hash_db;
        clusters.put(NOISE, new ArrayList<P2i>());

    }

    private Set<P2i> region_query(P2i p) {
        Set<P2i> list = new HashSet<>();
        if (rows == 0 || cols == 0 || dbscan_hash_db == null) {
            System.out.println("naive region_query");
            for (P2i q : P_list) {
                double dst = Utils.dst_equ(p, q);
                if (dst < eps)
                    list.add(q);
            }
        } else {
            List<Integer> ids = Utils.get_adj_dbscan_ids(rows, cols, eps, p);
            for (int id : ids) {
                if (dbscan_hash_db.get(id) != null) {
                    for (P2i q : dbscan_hash_db.get(id)) {
                        if (Utils.dst_equ(p, q) < eps)
                            list.add(q);
                    }
                }
            }
        }
        return list;
    }

    /**
     * @param p
     * @param neighbours
     * @return true if interrupted
     */
    private boolean expand_cluster(P2i p, Set<P2i> neighbours) {
        if (clusters.get(C) == null) {
            clusters.put(C, new ArrayList<P2i>());
        }
        clusters.get(C).add(p);
        clustered_points.add(p);
        while (neighbours.size() > 0) {
            Set<P2i> new_neighbors = new HashSet<>();
            for (P2i q : neighbours) {
                if (Thread.currentThread().isInterrupted()) {
                    Logg.d(TAG, "dbscan, interrupted");
                    return true;
                }
                //2i q = neighbours.remove(neighbours.size() - 1);
                //may be we need to override equals and hash in Pi!!!
                if (!visited_points.contains(q)) {
                    visited_points.add(q);
                    Set<P2i> neighbourz = region_query(q);
                    if (neighbourz.size() > m)
                        new_neighbors.addAll(neighbourz);
                }
                if (!clustered_points.contains(q)) {
                    clustered_points.add(q);
                    clusters.get(C).add(q);
                    if (clusters.get(NOISE).contains(q))
                        clusters.get(NOISE).remove(q);
                }
            }
            new_neighbors.removeAll(neighbours);
            neighbours = new_neighbors;

        }
        return false;
    }

    public Map<Integer, List<P2i>> getClusters() {
        long t1 = Calendar.getInstance().getTimeInMillis();
        //may be we need to override equals and hash in Pi!!!
        for (P2i p : P_list) {
            if (visited_points.contains(p))
                continue;
            visited_points.add(p);
            Set<P2i> neighbours = region_query(p);
            if (neighbours.size() < m) {
                clusters.get(NOISE).add(p);
            } else {
                C += 1;
                boolean interrupted = expand_cluster(p, neighbours);
                if (interrupted) {
                    return null;
                }
            }

        }
        long t2 = Calendar.getInstance().getTimeInMillis();
        Logg.d(TAG, "dbscan, time=" + (t2 - t1));
        return clusters;

    }
}
