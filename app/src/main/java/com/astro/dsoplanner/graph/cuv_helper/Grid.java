package com.astro.dsoplanner.graph.cuv_helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.DMS;


import com.astro.dsoplanner.base.CustomPoint;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.matrix.Vector2;
import com.astro.dsoplanner.util.Holder2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.astro.dsoplanner.base.Point.FSIN;

import android.graphics.Path.Direction;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import android.graphics.Paint.FontMetrics;

public class Grid {

    private static final String LINE = "line ";
    private static final String CIRCLE = "circle ";

    public static final int FOV_91 = 91;
    private static final String TAG = "Grid";

    public static float D2R = (float) (PI / 180);
    public static float R2D = 1 / D2R;


    final double w = Point.getWidth();
    final double h = Point.getHeight();
    double altcen = Point.getAltCenter();
    double azcen = Point.getAzCenter();
    float offset = 3 * Point.getScalingFactor();
    int Ngrid; //should divide by 2
    int Ngridaz;
    double step;//angle of the grid, first meridian passes at point az=alt=0;
    double stepaz;
    Canvas canvas;
    Paint paint;
    Paint labpaint;

    Point npole;
    Point spole;
    final int MIN_GRID = 20;
    boolean labelon;
    Context context;

    boolean eq = false;
    private static final int HTG = 15;//ra 0-24 to grad 0-360

    int count = 0;

    public void init() {
        count = 0;
    }

    public int get() {
        return count;
    }

    private void incCounter() {
        count++;
    }

    float fov_label_size;
    float fov_label_height;
    float mirror_label_size;
    float mirror_label_height;


    public void setEq() {
        eq = true;

        altcen = Point.getDec(Point.getAzCenter(), Point.getAltCenter());
        azcen = HTG * Point.getRa(Point.getAzCenter(), Point.getAltCenter());

        npole = eq ? new Point(0, 90) : new CustomPoint(0, 90, "");

        npole.setXY();
        npole.setDisplayXY();

        spole = eq ? new Point(0, -90) : new CustomPoint(0, -90, "");
        spole.setXY();
        spole.setDisplayXY();
        zenith = npole;
        nadir = spole;


    }

    Calendar cal;
    Point zenith;
    Point nadir;

    public Grid(Canvas canvas, Paint paint, boolean label, Context context, float fov_label_size, float fov_label_height, float mirror_label_size, float mirror_label_height) {
        this.context = context;
        this.fov_label_size = fov_label_size;
        this.fov_label_height = fov_label_height;
        this.mirror_label_size = mirror_label_size;
        this.mirror_label_height = mirror_label_height;
        this.canvas = canvas;
        this.paint = paint;
        labpaint = new Paint(paint);
        labpaint.setStyle(Paint.Style.FILL);
        labelon = label;
        cal = AstroTools.getDefaultTime(context);
        double fov = Point.getFOV();
        if (h < w) fov = fov * h / w;
        int fovswitch = -100;
        if (fov >= 20) fovswitch = 20;
        else if (fov >= 10) fovswitch = 10;
        else if (fov >= 5) fovswitch = 5;
        else if (fov >= 2) fovswitch = 2;
        else if (fov >= 1) fovswitch = 1;
        else if (fov >= 0.5) fovswitch = 0;
        else if (fov >= 0.25) fovswitch = -1;
        else if (fov >= 0.12) fovswitch = -2;
        else if (fov >= 0.06) fovswitch = -3;
        else if (fov >= 0.03) {
            fovswitch = -4;
        } else if (fov >= 0.015) {
            fovswitch = -5;
        }

        npole = eq ? new Point(0, 90) : new CustomPoint(0, 90, "");

        npole.setXY();
        npole.setDisplayXY();

        spole = eq ? new Point(0, -90) : new CustomPoint(0, -90, "");
        spole.setXY();
        spole.setDisplayXY();

        zenith = npole;
        nadir = spole;

        switch (fovswitch) {
            case -5:
                Ngrid = 7680 * 4;
                break;
            case -4:
                Ngrid = 7680 * 2;
                break;
            case -3:
                Ngrid = 7680;
                break;
            case -2:
                Ngrid = 3840;
                break;
            case -1:
                Ngrid = 1920;
                break;
            case 0:
                Ngrid = 960;
                break;
            case 1:
                Ngrid = 480;
                break;
            case 2:
                Ngrid = 240;
                break;
            case 5:
                Ngrid = 120;
                break;
            case 10:
                Ngrid = 60;
                break;
            default:
                Ngrid = 30;
        }
        step = 360f / Ngrid;
        Ngridaz = Ngrid;
        stepaz = step;
    }

    public static ArrayList<Vector2> getFarthestPoints(ArrayList<Vector2> list) {
        ArrayList<Vector2> listf = new ArrayList<Vector2>();
        if (list.size() == 0 || list.size() == 1 || list.size() == 2) return list;
        double max = 0;
        int k = 0;
        int l = 0;
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.size(); j++) {
                Vector2 v1 = list.get(i);
                Vector2 v2 = list.get(j);
                double dst = (v1.x - v2.x) * (v1.x - v2.x) + (v1.y - v2.y) * (v1.y - v2.y);
                if (dst > max) {
                    max = dst;
                    k = i;
                    l = j;
                }
            }
        }
        listf.add(list.get(l));
        listf.add(list.get(k));
        return listf;
    }

    private Path one_path = new Path();

    private Path getCleanPath() {
        one_path.reset();
        return one_path;
    }

    private Point one_point = new Point(0, 0);

    private Point getCleanPoint(double ra, double dec) {
        one_point.ra = (float) ra;
        one_point.dec = (float) dec;
        one_point.raiseNewPointFlag();
        return one_point;

    }

    private CustomPoint one_custom_point = new CustomPoint(0, 0, "");

    private Point getCleanCustomPoint(double az, double alt) {
        one_custom_point.ra = 0;
        one_custom_point.dec = 0;
        one_custom_point.az = az;
        one_custom_point.alt = alt;
        one_custom_point.raiseNewPointFlag();
        return one_custom_point;

    }

    public class Circle {

        boolean type;//false for line
        double x, y, r;
        double a, b;//for line y=a+b*x;
        boolean paramset = false;
        double param;//either az or alt param

        public void setParam(double param) {
            this.param = param;
            paramset = true;
        }

        public Circle() {
        }


        public Circle(Grid.Circle c) {
            super();
            this.type = c.type;
            this.x = c.x;
            this.y = c.y;
            this.r = c.r;
            this.a = c.a;
            this.b = c.b;
            this.paramset = c.paramset;
            this.param = c.param;
        }

        /**
         * get y based on x for 1/4 of circle
         * this 1/4 is determined by type
         * Given that y goes down and x to the right
         * 1 is for upper left, 2 upper right
         * 3 down left and 4 down right
         */
        private double getY(double xd, int type) {
            double det = r * r - (xd - x) * (xd - x);
            if (type == 1 || type == 2) return y - Math.sqrt(det);
            else return y + Math.sqrt(det);
        }

        private double getX(double yd, int type) {
            double det = r * r - (yd - y) * (yd - y);
            if (type == 2 || type == 4) return x + Math.sqrt(det);
            else return x - Math.sqrt(det);


        }

        /**
         * draw 1/4th of the circle
         */
        private void drawArc(double x1, double y1, double x2, double y2, int type) {

            double xr1 = 0;
            double xr2 = 0;
            if (x1 > w) return;
            if (x2 < 0) return;
            if (x1 < 0) xr1 = 0;
            else xr1 = x1;
            if (x2 < w) xr2 = x2;
            else xr2 = w;

            double yr1 = 0;
            double yr2 = 0;
            if (y1 > h) return;
            if (y2 < 0) return;
            if (y1 < 0) yr1 = 0;
            else yr1 = y1;
            if (y2 < h) yr2 = y2;
            else yr2 = h;

            double stepx = w / 25;
            double stepy = h / 25;

            double xcur = xr1;//getX(yr1,type);//xr1;
            double ycur = getY(xcur, type);//yr1
            int sign = 1;
            //direction of movement if we change y
            // in x direction is always positive
            if (type == 1 || type == 4) sign = -1;
            //1500 operations
            int N = 0;
            while (true) {
                //we draw the 1/4th circle either by changing x or changing y
                //we determine which var to use by looking at the steepness of tanget curve
                //by differentiating (x-xc)^2 + (y-yc)^2=r^2 we get
                //dy/dx = (x-xc)/(y-yc)
                //thus if (x-xc) > (y-yc) we change y else we change x
                double deltay = Math.abs(ycur - y);
                double deltax = Math.abs(xcur - x);
                if (deltax > deltay) {  //dy/dx = (x-xc) / (y-yc)
                    //take y as variable
                    double nexty = ycur + sign * stepy;
                    double nextx = getX(nexty, type);
                    if (within(xcur, ycur) || within(nextx, nexty))
                        drawLine(xcur, ycur, nextx, nexty);
                    xcur = nextx;

                    ycur = nexty;

                } else {
                    //take x as var
                    double nextx = xcur + stepx;
                    double nexty = getY(nextx, type);

                    if (within(xcur, ycur) || within(nextx, nexty))
                        drawLine(xcur, ycur, nextx, nexty);
                    xcur = nextx;
                    ycur = nexty;
                }
                N += 1;
                if (N > 500)
                    break;//this is to avoid hanging for steep zero az curve and draw them fully. if we put 100 we will not see the curve
                if (xcur < x1 || xcur > x2) break;
                if (ycur < y1 || ycur > y2) break;
            }


        }

        private boolean within(double x, double y) {
            return (x > 0) && (x < w) && (y > 0) && (y < h);
        }

        private void drawCircleAsLine() {
            if (b == 0) {
                drawLine(0, a, w, a);
            } else {
                drawLine(-a / b, 0, (h - a) / b, h);
            }
        }

        /**
         * simple circle drawing for testing. not used
         */
        public void draw2() {
            if (!type) return;
            double stepx = w / 10;
            double stepy = h / 90;
            double yd = 0;
            double yprev1 = 0, yperv2 = 0, xprev1 = 0, xprev2 = 0;
            boolean prevset = false;

            while (yd < h + stepy * 2) {
                double det = r * r - (yd - y) * (yd - y);
                if (det >= 0) {
                    double xd1 = x + Math.sqrt(det);
                    double xd2 = x - Math.sqrt(det);
                    if (prevset) {
                        drawLine(xprev1, yprev1, xd1, yd);
                        drawLine(xprev2, yprev1, xd2, yd);

                    } else {

                        prevset = true;
                    }
                    yprev1 = yd;
                    xprev1 = xd1;
                    xprev2 = xd2;
                }
                yd += stepy;
                Grid.this.incCounter();
            }

        }

        public void draw() {
            if (!type) {//line
                drawCircleAsLine();
                return;
            }
            //boundaries of 1/4th of circle
            double x1 = x - r;
            double x2 = x;
            double y1 = y - r;
            double y2 = y;
            drawArc(x1, y1, x2, y2, 1);

            x1 = x;
            x2 = x + r;
            y1 = y - r;
            y2 = y;
            drawArc(x1, y1, x2, y2, 2);

            x1 = x - r;
            x2 = x;
            y1 = y;
            y2 = y + r;
            drawArc(x1, y1, x2, y2, 3);

            x1 = x;
            x2 = x + r;
            y1 = y;
            y2 = y + r;
            drawArc(x1, y1, x2, y2, 4);
        }


        public String toString() {
            String s = "";
            if (type) s = CIRCLE + "x=" + x + " y=" + y + " r=" + r;
            else s = LINE + "a=" + a + " b=" + b;
            return s;
        }
    }

    private class AzLine { //number of the azimuth line
        public int az;

        public AzLine(int az) {
            this.az = az;

        }

        @Override
        public boolean equals(Object o) {//probably wrong as returns true each time
            if (o instanceof Grid.AzLine) {
                Grid.AzLine obj = (Grid.AzLine) o;
                if ((az - obj.az) % (Ngridaz / 2) == 0) return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (az % (Ngridaz / 2));
        }

        public String toString() {
            return "" + az;
        }
    }

    class LineSet extends Holder2<Set<AzLine>, Set<Grid.Circle>> {
        public LineSet(Set<Grid.AzLine> s1, Set<Grid.Circle> s2) {
            super(s1, s2);
        }
    }

    private Grid.LineSet getLineSet() {
        boolean poles_within_bounds = withinBounds(npole) || withinBounds(spole);
        double altlow = altcen - Point.getFOV() * 2;
        if (altlow < -90) altlow = -90;
        double althigh = altcen + Point.getFOV() * 2;
        if (althigh > 90) althigh = 90;

        Set<Grid.AzLine> setAzLines = new HashSet<AzLine>();
        Set<Grid.Circle> setAltLines = new HashSet<Grid.Circle>();

        List<Double> list = getGridAltLines(altlow, althigh);
        if (list.size() == 0) {
            setAzLines.addAll(getAllGridAzLines());
            return new Grid.LineSet(setAzLines, setAltLines);
        }

        for (double a : list) {
            //getting circle for given altitude

            Point p1 = eq ? new Point(azcen / HTG, a) : new CustomPoint(azcen, a, "");
            p1.setXY();
            p1.setDisplayXY();
            Point p2 = eq ? new Point((azcen + 90) / HTG, a) : new CustomPoint(azcen + 90, a, "");
            p2.setXY();
            p2.setDisplayXY();
            Point p3 = eq ? new Point((azcen + 190) / HTG, a) : new CustomPoint(azcen + 190, a, "");
            p3.setXY();
            p3.setDisplayXY();

            Grid.Circle c = getCircle(p1, p2, p3);
            c.setParam(a);
            ArrayList<Vector2> li = getIntersection(c);

            if (poles_within_bounds) {
                setAzLines.addAll(getAllGridAzLines());
                setAltLines.add(c);
                continue;
            }
            if (li.size() == 0) {//no intersections

                if (withinBounds(p1)) {
                    setAzLines.addAll(getAllGridAzLines());
                    setAltLines.add(c);
                }
            } else {
                if (li.size() > 2) {
                    setAltLines.add(c);
                }
                if (li.size() == 2) {
                    Vector2 v1;
                    if (eq) {
                        v1 = Point.getRaDec(li.get(0));
                        v1.x = HTG * v1.x;
                    } else
                        v1 = CustomPoint.getAzAlt(li.get(0).x, li.get(0).y);//az,alt of the first intersection point

                    Vector2 v2;
                    if (eq) {
                        v2 = Point.getRaDec(li.get(1));
                        v2.x = HTG * v2.x;
                    } else
                        v2 = CustomPoint.getAzAlt(li.get(1).x, li.get(1).y);//az,alt of the second intersection point

                    setAltLines.add(c);
                    setAzLines.addAll(getGridAzLines(v1.x, v2.x, a));
                }

            }


        }

        return new Grid.LineSet(setAzLines, setAltLines);
    }

    //for labelts take into account precession adj
    double adj_alt;
    double adj_az;

    public void draw() {
        FSIN tmF = Point.tfun;
        Point.tfun = Point.slowFunctions;
        boolean exit = false;
        int i = 0;
        Grid.LineSet ls;
        do {
            ls = getLineSet();

            if (ls.x.size() < MIN_GRID) {
                exit = true;
            } else {
                Ngridaz = (int) (Ngridaz / 1.2);
                if (Ngridaz % 2 == 1)
                    Ngridaz++;//so that lines with 180 degrees diff are not drawn together
                stepaz = 360f / Ngridaz;
            }
            i++;

        } while (!exit && i < 40);

        //calculating precesion adj for labels
        if (!eq && Point.getFOV() < 10.1) {
            Vector2 v = Point.getRaDec(Point.getAzCenter(), Point.getAltCenter());

            AstroTools.RaDecRec rec = AstroTools.approximatePrecession(v.x, v.y, cal);
            Point p = new Point(rec.ra, rec.dec);
            double alt = p.getAlt();
            double az = normalise(p.getAz());
            adj_az = az - normalise(Point.getAzCenter());
            if (Math.abs(adj_az) > 2) {
                if (adj_az < 0) {
                    adj_az += 360;
                    if (Math.abs(adj_az) > 2) adj_az = 0;
                } else if (adj_az > 0) {
                    adj_az -= 360;
                    if (Math.abs(adj_az) > 2) {
                        adj_az = 0;
                    }
                }
            }

            adj_alt = alt - Point.getAltCenter();
        }

        //alt line
        for (Grid.Circle c : ls.y) {
            drawCircle(c, true);
        }
        boolean extreme = Point.getFOV() < 5.1;//approximating with a line
        boolean allazlines = false;

        if (Point.getFOV() == 30 && Math.abs(altcen) > 60) {
            allazlines = true;
        }
        if (Point.getFOV() == 20 && Math.abs(altcen) > 60) {
            allazlines = true;
        } else if (Point.getFOV() == 10 && Math.abs(altcen) > 70) allazlines = true;
        else if (Point.getFOV() == 5 && Math.abs(altcen) > 80) allazlines = true;
        else if (Point.getFOV() < 5 && Math.abs(altcen) > 85) allazlines = true;

        if (allazlines) {
            ls.x = new HashSet<Grid.AzLine>();
            ls.x.addAll(getAllGridAzLines());
        }

        for (Grid.AzLine al : ls.x) {//az lines
            double azp = al.az * stepaz;

            boolean zenith = false;
            boolean nadir = false;

            if (altcen > 89) {
                zenith = true;
            }
            if (altcen < -89) nadir = true;

            Point p1 = eq ? new Point(azp / HTG, -90) : new CustomPoint(azp, -90, "");
            if (zenith) p1 = eq ? new Point(azp / HTG, -45) : new CustomPoint(azp, -45, "");
            p1.setXY();
            p1.setDisplayXY();

            Point p2 = eq ? new Point(azp / HTG, 90) : new CustomPoint(azp, 90, "");
            if (nadir) p2 = eq ? new Point(azp / HTG, 45) : new CustomPoint(azp, 45, "");
            p2.setXY();
            p2.setDisplayXY();


            Point p3 = eq ? new Point(azp / HTG, 0) : new CustomPoint(azp, 0, "");
            p3.setXY();
            p3.setDisplayXY();

            Grid.Circle c;
            if (extreme) {
                Point cp = eq ? new Point(azp / HTG, altcen) : new CustomPoint(azp, altcen, "");
                cp.setXY();
                cp.setDisplayXY();

                c = getExtremeAzCircle(cp);
            } else if (Point.getAzCenter() > 0) c = getCircle(p2, p3, p1);
            else c = getCircle(p1, p3, p2);

            ArrayList<Vector2> li = getIntersection(c);

            ///suboptimal because nadir and zenith coords are calcualted for each az line
            if (li.size() > 2) {
                ArrayList<Vector2> list = getFarthestPoints(li);
                boolean grid_old = Point.getFOV() < FOV_91; //use old algorithm
                if (grid_old && list.size() != 2) {
                    continue;
                }
                drawAzCircle(al.az * stepaz, c, list.get(0), list.get(1));

            }
            if (li.size() == 2) {

                drawAzCircle(al.az * stepaz, c, li.get(0), li.get(1));//between altitudes v1.y and v2.y, need to check sides?
            }
            if (li.size() < 2) {
                boolean new_algo = Point.getFOV() > FOV_91;
                if (new_algo) c.draw();
            }

        }
        Point.tfun = tmF;
    }

    private double getAzDst(double az1, double az2) {
        double d1 = az2 - az1;
        double d2 = 360 - d1;
        return Math.min(normalise(d1), normalise(d2));
    }

    private void drawAzLabel(Vector2 v, double az) {
        if (!labelon) return;
        String format = "%03.0f";
        if (Point.getFOV() < 2.1) format = "%03.1f";
        if (eq) {
            double ra = normalise(az) / HTG;
            DMS r = AstroTools.d2dms(ra);
            String s = String.format(Locale.US, "%02d %02.0f", r.d, r.m + (r.s / 60f));
            drawLabel(v, s);
        } else drawLabel(v, String.format(format, normalise(az + adj_az)));
    }

    private void drawAzCircle(double az, Grid.Circle c, Vector2 v1, Vector2 v2) {//v1 and v2 - two points between which to draw, xy coords
        boolean grid = Point.getFOV() < FOV_91; //use old algorithm
        if (!grid) {
            c.draw();
        }
        if ((v1 == null) || (v2 == null)) return;

        Vector2 ip1;
        if (eq) {
            ip1 = Point.getRaDec(v1);
            ip1.x = HTG * ip1.x;
        } else {
            ip1 = CustomPoint.getAzAlt(v1);
        }

        Vector2 ip2;
        if (eq) {
            ip2 = Point.getRaDec(v2);
            ip2.x = HTG * ip2.x;
        } else ip2 = CustomPoint.getAzAlt(v2);//az,alt of the second intersection point

        int N = 4;
        if (Math.round(Point.getFOV()) >= 60) N = 16;

        if (withinBounds(zenith)) {
            drawAzLabel(v1, ip1.x);
            drawAzLabel(v2, ip2.x);
            if (grid) {
                Vector2 vz = new Vector2(zenith.getXd(), zenith.getYd());
                if (Point.getFOV() > 31) {//draw as a curve
                    drawPart(az, v1, vz, ip1, new Vector2(0, 90), N);
                    drawPart(az, v2, vz, ip2, new Vector2(0, 90), N);
                } else {
                    drawLine(v1, vz);
                    drawLine(v2, vz);
                }
            }
            return;
        } else if (withinBounds(nadir)) {

            drawAzLabel(v1, ip1.x);
            drawAzLabel(v2, ip2.x);
            if (grid) {
                Vector2 vn = new Vector2(nadir.getXd(), nadir.getYd());
                if (Point.getFOV() > 31) {//draw as a curve
                    drawPart(az, v1, vn, ip1, new Vector2(0, -90), N);
                    drawPart(az, v2, vn, ip2, new Vector2(0, -90), N);
                } else {
                    drawLine(v1, vn);
                    drawLine(v2, vn);
                }
            }


            return;
        }


        //calculating dist from az center

        double dist1 = Math.min(normalise(az - azcen), normalise(360 - az + azcen));
        double dist2 = Math.min(normalise(az + 180 - azcen), normalise(360 - (az + 180 - azcen)));
        boolean second = false;
        if (dist2 < dist1) second = true;
        double azl = second ? normalise(az + 180) : normalise(az);
        drawAzLabel(v1, azl);
        drawAzLabel(v2, azl);
        if (grid) {

            if (Point.getFOV() < 11) {
                drawLine(v1, v2);
                return;
            }

            if (Math.abs(ip1.x - ip2.x) > 175) {//the same circle consisting of 2 parts eg 72 and 72+180
                //draw through zenith/nadir

                double dstz = (v1.x - zenith.x) * (v1.x - zenith.x) + (v1.y - zenith.y) * (v1.y - zenith.y);
                double dstn = (v1.x - nadir.x) * (v1.x - nadir.x) + (v1.y - nadir.y) * (v1.y - nadir.y);

                Vector2 vz = new Vector2(zenith.getXd(), zenith.getYd());
                if (dstn > dstz) {//draw throw zenith
                    if (Point.getFOV() > 31) {//draw as a curve
                        drawPart(az, v1, vz, ip1, new Vector2(0, 90), N);
                        drawPart(az, v2, vz, ip2, new Vector2(0, 90), N);
                    } else {
                        drawLine(v1, vz);
                        drawLine(v2, vz);
                    }
                } else {//draw through nadir
                    Vector2 vn = new Vector2(nadir.getXd(), nadir.getYd());
                    if (Point.getFOV() > 31) {//draw as a curve
                        drawPart(az, v1, vn, ip1, new Vector2(0, -90), N);
                        drawPart(az, v2, vn, ip2, new Vector2(0, -90), N);
                    } else {
                        drawLine(v1, vn);
                        drawLine(v2, vn);
                    }
                }
            } else drawPart(az, v1, v2, ip1, ip2, N);
        }

    }

    /**
     * @param az
     * @param v1
     * @param v2 zenith or nadir
     * @param N
     */
    private void drawPart(double az, Vector2 v1, Vector2 v2, Vector2 v1altaz, Vector2 v2altaz, int N) {
        Vector2 ip1 = v1altaz;//az,alt of the first intersection point
        ip1.x = normalise(ip1.x);
        double diff = Math.abs(ip1.x - az);
        if (Math.abs(diff - 180) < 3) {//in fact the intersection point v1 may correspond to az+-180!
            az = ip1.x;
        }
        Vector2 ip2 = v2altaz;//az,alt of the second intersection point
        ip2.x = normalise(ip2.x);
        double step = (ip2.y - ip1.y) / N;

        int i = 0;
        Vector2 prev = v1;
        do {
            i++;

            Vector2 v;
            if (i == N) v = v2;
            else {
                Point p;
                p = eq ? getCleanPoint(az / HTG, ip1.y + step * i) : getCleanCustomPoint(az, ip1.y + step * i);

                p.setXY();
                p.setDisplayXY();
                v = new Vector2(p.getXd(), p.getYd());
            }


            drawLine(prev, v);
            prev = v;

        } while (i < N);
    }

    private void drawTestPoint(Vector2 v) {
        Path path = new Path();
        path.addCircle((float) v.x, (float) v.y, 5, Direction.CW);
        canvas.drawPath(path, paint);

    }

    public void drawAltCircle(double alt, Grid.Circle c, Vector2 va1, Vector2 va2, Vector2 v1, Vector2 v2) { //va1,2 - alt az coords of intersections, v1,2 - xy
        if (alt == 0) drawCircle(c, false);
        else drawCircle(c, true);


    }

    private void drawLine(double x1, double y1, double x2, double y2) {
        Path path = new Path();
        path.moveTo((float) x1, (float) y1);
        path.lineTo((float) x2, (float) y2);
        canvas.drawPath(path, paint);
    }

    private void drawLine(Point p1, Point p2) {
        Path path = new Path();
        path.moveTo(p1.getXd(), p1.getYd());
        path.lineTo(p2.getXd(), p2.getYd());
        canvas.drawPath(path, paint);
    }

    private void drawLine(Vector2 v1, Vector2 v2) {//xy coords
        Path path = getCleanPath();//new Path();
        path.moveTo((float) v1.x, (float) v1.y);
        path.lineTo((float) v2.x, (float) v2.y);
        canvas.drawPath(path, paint);
    }

    private void drawAltLabels(Grid.Circle c, ArrayList<Vector2> list) {
        if (!labelon) return;
        if (list.size() > 2) list = getFarthestPoints(list);
        String format = "%+1.0f";
        if (Point.getFOV() < 2.1) format = "%+1.1f";
        for (Vector2 v : list) {
            double value = c.param;
            if (!eq) value += adj_alt;
            if (c.paramset) drawLabel(v, String.format(format, value));
        }
    }

    /**
     * alt line
     *
     * @param c
     */
    public void drawCircle(Grid.Circle c, boolean label) {

        Path circle = getCleanPath();
        if (c.type) {
            circle.addCircle((float) c.x, (float) c.y, (float) c.r, Direction.CW);
            ArrayList<Vector2> list = getIntersection(c);
            if (list.size() == 2 && c.r > 2000) {
                drawArc(c, list.get(0), list.get(1));
            } else if (list.size() != 0) {
                canvas.drawPath(circle, paint);
            }
            if (label) {
                drawAltLabels(c, list);

            }
        } else {
            ArrayList<Vector2> list = getIntersection(c);

            if (list.size() == 2) {
                Log.d(TAG, "draw cirle as line, alt=" + c.param);
                circle.moveTo((float) list.get(0).x, (float) list.get(0).y);
                circle.lineTo((float) list.get(1).x, (float) list.get(1).y);
            }
            canvas.drawPath(circle, paint);
            if (label) {
                drawAltLabels(c, list);

            }
        }

    }


    private List<Vector2> labels = new ArrayList<Vector2>();

    private boolean labelThere(Vector2 v) {
        if (v.y == 0 && v.x < fov_label_size)//do not draw under the FOV label
            return true;
        if (v.x == 0 && v.y < fov_label_height) return true;
        if (v.x > Point.getWidth() - mirror_label_size - offset && v.y < mirror_label_height)
            return true;


        double dstmin = 200 * Point.getScalingFactor();
        double dstminhor;
        if (Point.getFOV() > 2.1) dstminhor = 2000 * Point.getScalingFactor();
        else dstminhor = 3000 * Point.getScalingFactor();

        for (Vector2 vl : labels) {
            double dstsqr = (vl.x - v.x) * (vl.x - v.x) + (vl.y - v.y) * (vl.y - v.y);

            if (vl.y == 0 || vl.y == Point.getHeight()) dstmin = dstminhor;
            if (dstsqr < dstmin) return true;
        }
        return false;
    }

    private void addLabel(Vector2 label) {
        labels.add(label);
    }

    public void drawLabel(Vector2 v, String s) {
        if (labelThere(v)) {
            return;
        }
        float x = (float) v.x;
        float y = (float) v.y;
        float w = paint.measureText(s);
        float a = (float) normalise(Point.getRotAngle());
        FontMetrics fm = paint.getFontMetrics();
        float h = (fm.descent - fm.ascent);//font height
        if (v.x == 0) {
            x = x + offset;
            y = y - offset;
            if (a > 90 && a < 270) x += (Math.sin((a - 90) * D2R)) * w;
            if (a > 180 && a < 360) x += Math.sin((a - 180) * D2R) * h;
        } else if (v.x == Point.getWidth()) {
            y = y - offset;
            if (a > 270 || a < 90) x -= (Math.sin((a + 90) * D2R)) * w;
            if (a > 0 && a < 180) x -= Math.sin((a) * D2R) * h;

        } else if (v.y == 0) {
            x = x + offset;
            if (a > 180) y += (Math.sin((a - 180) * D2R)) * w;
            if (a > 270 || a < 90) y += Math.sin((a + 90) * D2R) * h;
        } else if (v.y == Point.getHeight()) {
            x = x + offset;
            if (a > 0 && a < 180) {
                y -= (Math.sin((a) * D2R)) * w;
            }
            if (a > 90 && a < 270) {
                y -= (Math.sin((a - 90) * D2R)) * h;
            }
        }
        double angle = Point.getRotAngle();
        if (angle == 0) canvas.drawText(s, x, y, labpaint);
        else {
            Path path = AstroTools.getLabelPath(x, y, getCleanPath());
            canvas.drawTextOnPath(s, path, 0, 0, labpaint);
        }


        addLabel(v);
    }

    /**
     * draw alt circle as arc between v1 and v2
     *
     * @param c
     * @param v1
     * @param v2
     */
    public void drawArc(Grid.Circle c, Vector2 v1, Vector2 v2) {
        if (Point.getFOV() > 180) {
            c.draw();
            return;
        }
        Vector2 ip1;
        if (eq) {
            ip1 = Point.getRaDec(v1);
            ip1.x = ip1.x * HTG;
        } else ip1 = CustomPoint.getAzAlt(v1);//az,alt of the first intersection point

        Vector2 ip2;
        if (eq) {
            ip2 = Point.getRaDec(v2);
            ip2.x = ip2.x * HTG;
        } else ip2 = CustomPoint.getAzAlt(v2);//az,alt of the second intersection point

        int N = 16;
        double az1 = normalise(ip1.x);
        double az2 = normalise(ip2.x);

        //arc should pass through azc
        double min = Math.min(az1, az2);
        double max = Math.max(az1, az2);
        boolean direction = true;
        boolean startpoint = true;//v1
        if (Math.abs(az2 - min) < 0.001) startpoint = false;//v2

        if (max - min < 360 - max + min) {//the shortest distance
            direction = true;
        } else direction = false;

        double step = 0;
        if (direction) {
            step = (max - min) / N;
        } else {
            step = -(360 - max + min) / N;
        }

        int i = 0;

        Vector2 prev = v1;
        if (!startpoint) prev = v2;
        do {
            i++;

            Vector2 v;
            if (i == N) {
                v = v2;
                if (!startpoint) v = v1;
            } else {
                Point p;
                p = eq ? getCleanPoint((min + step * i) / HTG, ip1.y) : getCleanCustomPoint(min + step * i, ip1.y);

                p.setXY();
                p.setDisplayXY();
                v = new Vector2(p.getXd(), p.getYd());
            }


            drawLine(prev, v);
            prev = v;

        } while (i < N);
    }

    /**
     * draw alt circle as arc between v1 and v2
     *
     * @param c
     * @param v1
     * @param v2
     * @return path and starting point (true for first)
     */
    public Holder2<Path, Boolean> getArcPaths(Grid.Circle c, Vector2 v1, Vector2 v2) {
        Path path = new Path();
        Vector2 ip1;
        Vector2 ip2;

        if (eq) {
            ip1 = Point.getRaDec(v1);
            ip1.x = ip1.x * HTG;
        } else ip1 = CustomPoint.getAzAlt(v1);//az,alt of the first intersection point

        if (eq) {
            ip2 = Point.getRaDec(v2);
            ip2.x = ip2.x * HTG;
        } else ip2 = CustomPoint.getAzAlt(v2);//az,alt of the second intersection point

        int N = 16;
        double az1 = normalise(ip1.x);
        double az2 = normalise(ip2.x);

        //arc should pass through azc
        double min = Math.min(az1, az2);
        double max = Math.max(az1, az2);
        boolean direction = true;
        boolean startpoint = true;//v1
        if (Math.abs(az2 - min) < 0.001) startpoint = false;//v2
        if (max - min < 360 - max + min) {//the shortest distance
            direction = true;
        } else direction = false;

        double step = 0;
        if (direction) {
            step = (max - min) / N;
        } else {
            step = -(360 - max + min) / N;
        }

        int i = 0;

        Vector2 prev = v1;

        if (!startpoint) prev = v2;
        path.moveTo((float) prev.x, (float) prev.y);
        do {
            i++;

            Vector2 v;
            if (i == N) {
                v = v2;
                if (!startpoint) v = v1;
            } else {
                Point p;
                p = eq ? getCleanPoint((min + step * i) / HTG, ip1.y) : getCleanCustomPoint(min + step * i, ip1.y);

                p.setXY();
                p.setDisplayXY();
                v = new Vector2(p.getXd(), p.getYd());
            }
            path.lineTo((float) v.x, (float) v.y);
            prev = v;
        } while (i < N);
        return new Holder2<Path, Boolean>(path, startpoint);
    }

    /**
     * @param c
     * @param type true for alt, false for az
     * @return
     */
    public ArrayList<Vector2> getIntersection(Grid.Circle c) {//intersection with screen boundaries
        ArrayList<Vector2> list = new ArrayList<Vector2>();
        if (c.type) {//circle
            //(x-xc)^2+(y-yc)^2=r^2

            //boundary x=0, xc^2+(y-yc)^2=r^2, do not take tangency into account
            double det = c.r * c.r - c.x * c.x;

            if (det > 0) {
                list.add(new Vector2(0, c.y - sqrt(det)));
                list.add(new Vector2(0, c.y + sqrt(det)));
            }

            //boundary x=w, (w-xc)^2+(y-yc)^2=r^2
            det = c.r * c.r - (w - c.x) * (w - c.x);

            if (det > 0) {
                list.add(new Vector2(w, c.y - sqrt(det)));
                list.add(new Vector2(w, c.y + sqrt(det)));
            }

            //boundary y=0, (x-xc)^2+yc^2=r^2
            det = c.r * c.r - c.y * c.y;
            if (det > 0) {
                list.add(new Vector2(c.x - sqrt(det), 0));
                list.add(new Vector2(c.x + sqrt(det), 0));
            }
            //boundary y=h, (x-xc)^2+(h-yc)^2=r^2
            det = c.r * c.r - (h - c.y) * (h - c.y);
            if (det > 0) {
                list.add(new Vector2(c.x - sqrt(det), h));
                list.add(new Vector2(c.x + sqrt(det), h));
            }


        } else {//line

            //y=a+bx
            //boundary x=0
            list.add(new Vector2(0, c.a));
            //boundary x=w
            list.add(new Vector2(w, c.a + c.b * w));
            //boundary y=0
            list.add(new Vector2(-c.a / c.b, 0));
            //boundary y=h
            list.add(new Vector2((h - c.a) / c.b, h));
        }
        Iterator<Vector2> it = list.iterator();
        while (it.hasNext()) {
            Vector2 v = it.next();
            if (!withinBounds(v)) it.remove();
        }

        return list;

    }

    private boolean withinBounds(Vector2 v) {//vector - x,y coordinates
        return (v.x >= 0 && v.x <= w && v.y >= 0 && v.y <= h);
    }

    public boolean withinBounds(Point p) {
        Vector2 v = new Vector2(p.getXd(), p.getYd());
        return withinBounds(v);
    }

    private List<Double> getGridAltLines(double altmin, double altmax) {//list of altitude grid lines within given range
        List<Double> list = new ArrayList<Double>();
        int low = (int) (altmin / step);
        if (altmin > 0) low++;
        int high = (int) (altmax / step) - 1;
        if (altmax > 0) high++;
        for (int i = low; i <= high; i++)
            list.add(step * i);
        return list;

    }

    private List<Grid.AzLine> getAllGridAzLines() {
        List<Grid.AzLine> list = new ArrayList<Grid.AzLine>();
        for (int i = 0; i < Ngridaz; i++) {
            list.add(new Grid.AzLine(i));
        }
        return list;
    }


    private List<Grid.AzLine> getGridAzLines(double min, double max, double alt) {
        List<Grid.AzLine> list = new ArrayList<Grid.AzLine>();
        min = normalise(min);
        max = normalise(max);
        if (min > max) {
            double tmp = min;
            min = max;
            max = tmp;
        }
        Point p = eq ? new Point((min + max) / 2 / HTG, alt) : new CustomPoint((min + max) / 2, alt, "");
        p.setXY();
        p.setDisplayXY();
        int low = (int) (min / stepaz);
        low++;
        int high = (int) (max / stepaz);
        high++;//?
        if (withinBounds(p)) {
            for (int i = low; i <= high; i++) {
                list.add(new Grid.AzLine(i));
            }
        } else {
            int i = high;
            double an = stepaz * i;
            double anf = 360 + stepaz * low;
            while (an < anf) {
                list.add(new Grid.AzLine(i));
                i++;
                an = an + stepaz;
            }
        }
        return list;

    }

    private double normalise(double an) {
        return AstroTools.normalise(an);
    }

    /**
     * @param azcen
     * @param y0    y coord of zenith or nadir
     * @return
     */
    public Grid.Circle getExtremeAzCircle(Point cp) {
        CustomPoint cpp = null;
        if (!eq) {
            cpp = (CustomPoint) cp;
        }
        double alt2 = eq ? cp.dec + 0.1 : cpp.alt + 0.1;
        if (alt2 > 90) alt2 = eq ? cp.dec - 0.1 : cpp.alt - 0.1;

        Point cp2 = eq ? new Point(cp.ra, alt2) : new CustomPoint(cpp.az, alt2, "");
        cp2.setXY();
        cp2.setDisplayXY();

        if (cp2.xd != cp.xd) {
            double b = (cp.yd - cp2.yd) / (cp.xd - cp2.xd);
            double a = cp.yd - b * cp.xd;
            Grid.Circle c = new Grid.Circle();
            c.a = a;
            c.b = b;
            c.type = false;
            return c;
        } else {
            Grid.Circle c = new Grid.Circle();
            c.b = 1e8;
            c.a = cp.yd - c.b * cp.xd;
            c.type = false;
            return c;
        }
    }

    /**
     * @param c1    should be the key point through which line passes (if line is determined)
     * @param c2    the second point
     * @param c3
     * @param type. true for alt line, false for az line
     * @return
     */
    public Grid.Circle getCircle(Point c1, Point c2, Point c3) {
        float x1 = c1.getXd();
        float y1 = c1.getYd();
        float x2 = c2.getXd();
        float y2 = c2.getYd();
        float x3 = c3.getXd();
        float y3 = c3.getYd();

        if (abs(x1 - x2) < 0.0001) {
            float tmpx3 = x3;
            float tmpy3 = y3;
            x3 = x2;
            y3 = y2;
            x2 = tmpx3;
            y2 = tmpy3;
        }

        float ma = (y2 - y1) / (x2 - x1);
        float mb = (y3 - y2) / (x3 - x2);
        if (abs(ma - mb) < 0.01) {//line
            Grid.Circle c = new Grid.Circle();
            c.type = false;
            c.a = y1 - ma * x1;
            c.b = ma;
            return c;

        }
        Grid.Circle c = new Grid.Circle();
        c.type = true;
        c.x = (ma * mb * (y1 - y3) + mb * (x1 + x2) - ma * (x2 + x3)) / (2 * (mb - ma));
        c.y = -1 / ma * (c.x - (x1 + x2) / 2) + (y1 + y2) / 2;
        c.r = (float) sqrt((c.x - x1) * (c.x - x1) + (c.y - y1) * (c.y - y1));
        return c;
    }
}
