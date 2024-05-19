package com.astro.dsoplanner.graph;

import android.content.Context;
import android.graphics.Path;
import android.net.Uri;
import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.CustomPoint;
import com.astro.dsoplanner.base.Point;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UserHorizon {
    private static final String TAG = UserHorizon.class.getSimpleName();
    private static float[] pairs = null;
    private static int sz = 0; //final array size
    private static String dataLocation = "";
    private static float treshold = 5; //5 degrees per segment of line - max
    private static float mindst = 20;//if distance between the points is greater than this value they are consired se

    private static final int fillsection = 30;//for filling

    private static List<CustomPoint> cpath;
    private static List<List<CustomPoint>> fpath;//for filling. each list in the list is a separate path


    static class PointF {
        float x;
        float y;

        public PointF(float x, float y) {
            super();
            this.x = x;
            this.y = y;
        }
    }

    public static void init(Context context) {
        treshold = SettingsActivity.getUHorStep();
        dataLocation = SettingsActivity.getUHorFile();
        if (!"".equals(dataLocation)) {
            readUserDataFile(context);
        }
    }

    private static InputStream getInputStream(Context context) throws FileNotFoundException {
        return context.getContentResolver().openInputStream(Uri.parse(dataLocation));
    }

    private static void readUserDataFile(Context context) {
        List<PointF> list = new ArrayList<PointF>();
        try (BufferedReader in =
                     new BufferedReader(new InputStreamReader(getInputStream(context)))) {
            String s;
            //initial user data collection and leftmost point search
            while ((s = in.readLine()) != null) {
                String[] slist = s.split(" ");
                if (slist.length < 2) continue; //skip empty lines, keep OK lines
                float x = 0;
                float y = 0;
                try {
                    x = Float.parseFloat(slist[0]);
                    y = Float.parseFloat(slist[1]);
                } catch (Exception e) {
                    continue;
                }
                if (y < 0) y = 0;//do not accept below zero mag points
                PointF p = new PointF(x, y);
                list.add(normalized(p));

            }
        } catch (Exception ignored) {
        }

        Comparator<PointF> comp = new Comparator<PointF>() {
            @Override
            public int compare(PointF lhs, PointF rhs) {
                if (lhs.x == rhs.x) {
                    if (lhs.y < rhs.y)
                        return -1;
                    if (lhs.y == rhs.y)
                        return 0;
                    return 1;
                }
                if (lhs.x < rhs.x) return -1;
                return 1;
            }
        };

        if (list.size() == 0) {
            cpath = null;
            fpath = null;
            return;
        }
        Collections.sort(list, comp);

        //removing equal az alt
        PointF prev = null;
        Iterator<PointF> iterator = list.iterator();
        while (iterator.hasNext()) {
            PointF current = iterator.next();
            if (prev != null) {
                if (prev.x == current.x && prev.y == current.y) {
                    iterator.remove();
                }

            }
            prev = current;
        }

        //making linear approximation
        List<PointF> glist = new ArrayList<PointF>();
        prev = null;
        for (PointF p : list) {

            if (prev != null) {
                List<PointF> addpoints = addPoints(prev, p);
                glist.addAll(addpoints);

            }
            glist.add(p);
            prev = p;
        }
        //from end to start
        List<PointF> addpoints = addPoints(list.get(list.size() - 1), list.get(0));
        glist.addAll(addpoints);

        //making a path of custom points
        cpath = new ArrayList<CustomPoint>();
        //path for filling
        fpath = new ArrayList<List<CustomPoint>>();
        //making a fill path of custom points

        PointF lp = glist.get(glist.size() - 1);//last point
        CustomPoint cprev = new CustomPoint(lp.x, lp.y, "");
        for (PointF p : glist) {

            CustomPoint cp = new CustomPoint(p.x, p.y, "");
            cpath.add(cp);

            //making fill portion
            if (!(cprev.alt == 0 && cp.alt == 0)) {
                List<CustomPoint> sublist = new ArrayList<CustomPoint>();
                if (cprev.alt != 0) {//making zero alt point with the same az
                    CustomPoint ps = new CustomPoint(cprev.az, 0, "");
                    sublist.add(ps);
                }
                sublist.add(cprev);
                sublist.add(cp);
                if (cp.alt != 0) {
                    CustomPoint pe = new CustomPoint(cp.az, 0, "");
                    sublist.add(pe);
                }
                fpath.add(sublist);
            }
            cprev = cp;
        }
    }

    private static List<PointF> addPoints(PointF start, PointF end) {
        Log.d(TAG, "start=" + start);
        Log.d(TAG, "end=" + end);
        List<PointF> list = new ArrayList<PointF>();
        float dst = clockwiseDst(start, end);

        if (dst > treshold) {
            int addpoints = (int) (dst / treshold) - 1;
            float dy = (end.y - start.y) / dst * treshold;
            for (int i = 0; i < addpoints; i++) {
                PointF p = new PointF(start.x + treshold * (i + 1), start.y + dy * (i + 1));
                p = normalized(p);
                list.add(p);
                Log.d(TAG, "point added " + p);
            }

        }

        return list;
    }

    /**
     * @param start
     * @param end
     * @return distance measured clockwise from start to end
     */
    private static float clockwiseDst(PointF start, PointF end) {
        if (Math.abs(end.x - start.x) < 0.001)
            return 0;
        if (end.x > start.x)
            return end.x - start.x;
        else
            return 360 - start.x + end.x;
    }

    private static float distanceAz(PointF p1, PointF p2) {
        float x1 = Math.min(p1.x, p2.x);
        float x2 = Math.max(p1.x, p2.x);
        float d1 = x2 - x1;
        float d2 = 360 - x2 + x1;
        return Math.min(d1, d2);
    }

    public static Path getPath() {
        if (cpath == null)
            return null;
        if (cpath.size() == 0)
            return null;
        Path path = new Path();

        CustomPoint prev = cpath.get(cpath.size() - 1);//last point
        prev.setXY();
        prev.setDisplayXY();
        for (int i = 0; i < cpath.size(); i++) {
            CustomPoint next = cpath.get(i);
            next.setXY();
            next.setDisplayXY();
            if (isLineDrawable(prev, next)) {
                path.moveTo(prev.getXd(), prev.getYd());
                path.lineTo(next.getXd(), next.getYd());
            }
            prev = next;
        }


        return path;
    }

    private static boolean isLineDrawable(CustomPoint p1, CustomPoint p2) {
        return (p1.z2 > -0.5 || p2.z2 > -0.5);
    }

    public static List<Path> getFillPaths() {
        if (fpath == null) return null;
        List<Path> listpath = new ArrayList<Path>();
        for (List<CustomPoint> list : fpath) {
            Path path = new Path();
            boolean first = true;


            double xmin = 0;
            double xmax = 0;
            double ymin = 0;
            double ymax = 0;
            Set<Double> setx = new HashSet<Double>();
            Set<Double> sety = new HashSet<Double>();
            boolean ontheotherside = false;//on the other side of the sphere that we project to plane. needed to avoid stranded lines and filling
            for (CustomPoint cp : list) {
                cp.setXY();
                cp.setDisplayXY();
                if (cp.z2 < -0.5) {
                    ontheotherside = true;
                    break;
                }
                setx.add((double) cp.getXd());
                sety.add((double) cp.getYd());
            }
            if (ontheotherside)
                continue;
            xmin = getMin(setx);
            xmax = getMax(setx);
            ymin = getMin(sety);
            ymax = getMax(sety);
            boolean show = showFill(xmin, xmax, ymin, ymax);
            if (!show)
                continue;

            for (CustomPoint cp : list) {
                cp.setXY();
                cp.setDisplayXY();

                if (first) {
                    path.moveTo(cp.getXd(), cp.getYd());
                    first = false;
                } else {
                    path.lineTo(cp.getXd(), cp.getYd());
                }
            }
            path.close();
            listpath.add(path);

        }
        return listpath;
    }

    private static double getMin(Set<Double> set) {
        boolean first = true;
        double xmin = 0;
        for (double d : set) {
            if (first) {
                xmin = d;
                first = false;
            } else {
                if (d < xmin)
                    xmin = d;
            }
        }
        return xmin;
    }

    private static double getMax(Set<Double> set) {
        boolean first = true;
        double xmax = 0;
        for (double d : set) {
            if (first) {
                xmax = d;
                first = false;
            } else {
                if (d > xmax)
                    xmax = d;
            }
        }
        return xmax;
    }
	/*Если прямоугольники заданы так:
		Первый:
		(x1,y1)(x1,y2)
		(x2,y1)(x2,y2)
		Второй:
		(a1,b1)(a1,b2)
		(a2,b1)(a2,b2)
		То простым условием (а не алгоритмом) их пересечения явится выражение:
		(a1<=x1<=a2 или x1<=a1<=x2) и (b1<=y1<=b2 или y1<=b1<=y2).*/

    static class Rectangle {
        double x1;
        double x2;
        double y1;
        double y2;


        public Rectangle(double x1, double x2, double y1, double y2) {
            super();
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }

        public boolean intersect(Rectangle r) {//a for r
            return ((r.x1 <= x1 && x1 <= r.x2) || (x1 <= r.x1 && r.x1 <= x2)) && ((r.y1 <= y1 && y1 <= r.y2) || (y1 <= r.y1 && r.y1 <= y2));
        }
    }

    private static boolean showFill(double x1, double x2, double y1, double y2) {
        Rectangle screen = new Rectangle(0, Point.getWidth(), 0, Point.getHeight());
        Rectangle r = new Rectangle(x1, x2, y1, y2);
        Log.d(TAG, "r=" + r);
        return r.intersect(screen);
    }

    private static PointF normalized(PointF p0) {
        PointF p = new PointF(p0.x, p0.y);
        p.x = (float) AstroTools.normalise(p.x);
        if (p.y > 90) p.y = 90;
        return p;
    }
}
