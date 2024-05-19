package com.astro.dsoplanner.graph;

import com.astro.dsoplanner.base.Point;

/**
 * this class is used for correct filling of horizon
 *
 * @author leonid
 */
public class RefPoint {
    double x;
    double y;
    RefPoint next;
    RefPoint prev;

    public RefPoint(double x, double y) {
        super();
        this.x = x;
        this.y = y;
    }

    static RefPoint getScreen() {
        RefPoint r1 = new RefPoint(0, 0);
        RefPoint r2 = new RefPoint(Point.getWidth(), 0);
        RefPoint r3 = new RefPoint(Point.getWidth(), Point.getHeight());
        RefPoint r4 = new RefPoint(0, Point.getHeight());
        r1.next = r2;
        r1.prev = r4;
        r2.next = r3;
        r2.prev = r1;
        r3.next = r4;
        r3.prev = r2;
        r4.next = r1;
        r4.prev = r3;
        return r1;
    }

    public String getShortString() {
        return " x=" + x + " y=" + y;
    }

    @Override
    public String toString() {
        RefPoint p = this;
        int i = 0;
        String s = "";
        boolean start = true;
        while ((start || !this.equals(p)) && i++ < 20) {
            start = false;
            s = s + " x=" + p.x + " y=" + p.y;
            p = p.next;
        }
        return s;
    }

    /**
     * @param p point to be inserted at the current graph
     * @return p
     */
    RefPoint insert(RefPoint p) {
        int i = 0;
        RefPoint r = this;
        boolean inserted = false;
        boolean start = true;
        while ((start || !r.equals(this)) && i++ < 20) {//to avoid cycling
            start = false;
            if (contains(r.prev, r, p)) {//the point p is between r.prev and r
                r.prev.next = p;
                p.prev = r.prev;
                p.next = r;
                r.prev = p;
                inserted = true;
                break;
            }
            r = r.next;
        }
        return p;
    }

    /**
     * @param r1
     * @param r2
     * @param r3 is r3 between r1 and r2 assuming that they are on the same line
     * @return
     */
    static boolean contains(RefPoint r1, RefPoint r2, RefPoint r3) {
        double minx = Math.min(r1.x, r2.x);
        double maxx = Math.max(r1.x, r2.x);
        double miny = Math.min(r1.y, r2.y);
        double maxy = Math.max(r1.y, r2.y);
        double x = r3.x;
        double y = r3.y;
        return (x >= minx && x <= maxx && y >= miny && y <= maxy);
    }
}

