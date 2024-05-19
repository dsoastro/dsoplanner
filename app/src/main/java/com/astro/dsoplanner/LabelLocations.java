package com.astro.dsoplanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.astro.dsoplanner.base.Point;


public class LabelLocations {

    private static final String TAG = LabelLocations.class.getSimpleName();
    private static LabelLocations ll = new LabelLocations();

    public static LabelLocations getLabelLocations() {
        return ll;

    }

    class Rec {
        Point obj;
        int length;//label length

        public Rec(Point obj, int length) {
            super();
            this.obj = obj;
            this.length = length;
        }

    }

    final static int N = 10;
    Rec[][] arr = new Rec[N + 5][N + 5];
    Set<Rec> listadd = new HashSet<Rec>();
    double stepx;
    double stepy;
    int w, h;

    private LabelLocations() {
    }

    /**
     * dimensions changed
     */
    public void init() {
        w = Point.getWidth();
        h = Point.getHeight();
        stepx = w / N;
        stepy = h / N;
    }

    public void clear() {
        arr = new Rec[N + 5][N + 5];
        listadd = new HashSet<Rec>();
    }
	
    /**
     * @return false if you could not draw
     * @p object for which label is drawn
     * @length length of the label
     */
    public boolean get(Point p, int length) {
        if (p == null) return false;
        float x = p.getXd();
        float y = p.getYd();
        if (x < 0 || x > w || y < 0 || y > h) return false;
        int xc = (int) (x / stepx);
        int yc = (int) (y / stepy);
        Rec rec = arr[xc][yc];
        int len = 0;
        if (rec != null) len = arr[xc][yc].length;
        if (rec == null) {
            arr[xc][yc] = new Rec(p, length);
            return true;
        } else {
            boolean same = p.equals(rec.obj);//you could draw for the same object
            if (same) return true;
            else {
                for (Rec r : listadd) {
                    if (r.obj.equals(p)) return true;
                }

                if (length > 3 * len) {//replacing short strings with long. needed for double stars
                    arr[xc][yc] = new Rec(p, length);
                }
            }
            return false;
        }
    }

    /**
     * to be called before each drawing round
     * updates the array for the current object (with labels) positions
     */
    public void update() {
        List<Rec> list = new ArrayList<Rec>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                Rec rec = arr[i][j];
                if (rec != null) {
                    list.add(rec);

                }
            }
        }
        list.addAll(listadd);
        listadd = new HashSet<Rec>();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                arr[i][j] = null;
            }
        }

        for (Rec rec : list) {
            if (rec == null) continue;//just in case
            rec.obj.setXY();
            rec.obj.setDisplayXY();

            float x = rec.obj.getXd();
            float y = rec.obj.getYd();
            if (x < 0 || x > w || y < 0 || y > h) {

                continue;
            }
            int xc = (int) (x / stepx);
            int yc = (int) (y / stepy);
            if (arr[xc][yc] == null) {
                arr[xc][yc] = rec;
            } else {
                listadd.add(rec);
            }
        }
    }
}


