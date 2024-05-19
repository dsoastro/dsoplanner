package com.astro.dsoplanner.graph.cuv_helper;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;

import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.base.CustomPoint;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.graph.CuV;
import com.astro.dsoplanner.graph.GraphActivity;
import com.astro.dsoplanner.util.Holder3;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;

/**
 * managing dss rectangles
 * referenced in Graph activity to get info, delete images
 */
public class DssRectangles {

    Set<Rectangle> set = new HashSet<Rectangle>();
    Canvas canvas;
    Paint paint;
    CuV cuV;

    public DssRectangles(CuV cuV) {
        this.cuV = cuV;
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

    PathEffect effect = new DashPathEffect(new float[]{5, 5}, 1);

    private Matrix one_matrix = new Matrix();

    private Matrix getCleanMatrix() {
        one_matrix.reset();
        return one_matrix;
    }

    /**
     * internal class representing drawn rectangle over each image
     */
    private class Rectangle implements GraphActivity.DssImage {
        public static final int EXISTING_IMAGE = 1;
        public static final int POTENTIAL_IMAGE = 2;


        double ra;
        double dec;
        String name;
        int type;

        boolean selected = false;

        @Override
        public boolean equals(Object o) {
            if (o instanceof Rectangle) {
                Rectangle r = (Rectangle) o;
                return (name.equals(r.name) && r.coordEqual(this));
            }
            return false;
        }

        /**
         * creating Rectangle for existing image
         *
         * @param name - file name without directory
         */
        public Rectangle(double ra, double dec, String name) {

            this.ra = ra;
            this.dec = dec;
            this.name = name;
            type = EXISTING_IMAGE;

        }

        /**
         * creating Rectangle for potential image
         */
        public Rectangle(double ra, double dec) {
            this.ra = ra;
            this.dec = dec;
            type = POTENTIAL_IMAGE;
            name = "";
        }

        public boolean isPotential() {
            return (type == POTENTIAL_IMAGE);
        }

        public boolean isSelected() {
            return selected;
        }

        public void select() {
            selected = true;
            cuV.vibrate();
        }

        public void deselect() {
            selected = false;
        }

        /**
         * actual delete of dss image
         */
        public void delete() {
            DSS.delete(name);
            Global.dss.removeFromDssList(name);
            set.remove(this);
        }

        public GraphActivity.DssImageRec getInfo() {
            String filename = "".equals(name) ? "" : Global.DSSpath + File.separator + name;
            return new GraphActivity.DssImageRec(ra, dec, filename);
        }

        public void draw() {
            Point loc = new Point(ra, dec);
            loc.setXY();
            loc.setDisplayXY();

            if (!Point.withinBounds(loc.getXd(), loc.getYd(), (float) (DSS.size / 60f * Point.getWidth() / Point.getFOV() / 2)))
                return;

            Point test = new CustomPoint(Point.getAzCenter(), Point.getAltCenter(), "");
            Point testup = getCleanCustomPoint(Point.getAzCenter(), Point.getAltCenter() + DSS.size / 60f / 2f);

            testup.setXY();
            testup.setDisplayXY();
            test.setXY();
            test.setDisplayXY();
            float dim = (float) test.distanceOnDisplay(testup);
            if (Point.getFOV() > 179) dim = dim * 8;


            Point n = getCleanPoint(loc.ra, loc.dec + 0.001);//new Point(loc.ra,loc.dec+0.001);
            n.setXY();
            n.setDisplayXY();
            double angleNorth = atan2(n.getYd() - loc.yd, n.getXd() - loc.xd);
            float angle = (float) ((angleNorth * Grid.R2D)) + 90;
            Matrix matrix = getCleanMatrix();
            matrix.setRotate(angle, loc.getXd(), loc.getYd());

            Path path = getCleanPath();//new Path();

            path.moveTo(loc.xd - dim, loc.yd + dim);
            path.lineTo(loc.xd - dim, loc.yd - dim);
            path.lineTo(loc.xd + dim, loc.yd - dim);
            path.lineTo(loc.xd + dim, loc.yd + dim);
            path.lineTo(loc.xd - dim, loc.yd + dim);
            path.transform(matrix);

            float original_width = paint.getStrokeWidth();
            PathEffect original_effect = paint.getPathEffect();

            if (type == POTENTIAL_IMAGE) {
                paint.setPathEffect(effect);
            }
            if (selected) {
                paint.setStrokeWidth(10);
            }


            canvas.drawPath(path, paint);

            paint.setStrokeWidth(original_width);
            paint.setPathEffect(original_effect);


        }

        public double distance(double x, double y) {
            Point loc = getCleanPoint(ra, dec);//new Point(ra,dec);
            loc.setXY();
            loc.setDisplayXY();

            double dist = Math.sqrt((loc.getXd() - x) * (loc.getXd() - x) + (loc.getYd() - y) * (loc.getYd() - y));

            return dist;
        }

        public boolean coordEqual(Rectangle r) {
            final double delta = 1e-4;
            return (Math.abs(r.ra - ra) < delta && Math.abs(r.dec - dec) < delta);
        }
    }

    /**
     * updates the list of available rectangles to draw,
     * to be called after the screen has been moved for some distance
     */
    public void update() {
        Iterator<Rectangle> it = set.iterator();
        while (it.hasNext()) {
            Rectangle r = it.next();
            if (!r.isSelected()) it.remove();
        }
        double raC = Point.getRa(Point.getAzCenter(), Point.getAltCenter());
        double decC = Point.getDec(Point.getAzCenter(), Point.getAltCenter());

        double hw = (double) Point.getHeight() / (double) Point.getWidth();
        if (hw < 1) hw = 1 / hw;

        double stepdec = Point.getFOV() / 2 * hw;
        double costhreshold = cos((stepdec + 2 * DSS.size / 60f) * PI / 180);
        if (Point.getFOV() > CuV.FOV_149) costhreshold = CuV.COS_THRESHOLD_149;

        for (Holder3<Double, Double, String> rec : DSS.getDssCollection()) {
            double ra = rec.x;
            double dec = rec.y;


            double cosDist = Point.tfun.sin(decC * PI / 180) * Point.tfun.sin(dec * PI / 180) + Point.tfun.cos(decC * PI / 180) * Point.tfun.cos(dec * PI / 180) * Point.tfun.cos((raC - ra) * PI / 12);

            if (cosDist > costhreshold) { //upload needed
                //Log.d(TAG,"upload bitmap added");
                set.add(new Rectangle(ra, dec, rec.z));
            }
        }
    }

    /**
     * there is a room for optimisation in calculations, as some calculations are
     * dublicated and may be brought out of for cycle
     */
    public void draw(Canvas canvas, Paint paint) {
        this.canvas = canvas;
        this.paint = paint;
        for (Rectangle r : set) {
            r.draw();
        }
    }

    /**
     * @param x coord of the pressed point
     * @param y coord of the pressed point
     * @return DSS contour pressed
     */
    public GraphActivity.DssImage pressed(double x, double y) {
        double min = 10000;
        Rectangle rmin = null;
        for (Rectangle r : set) {
            double dist = r.distance(x, y);
            if (dist < min) {
                min = dist;
                rmin = r;
            }
        }
        if (rmin == null) return null;
        //pressed outside of image
        if ((min / Point.getWidth() > DSS.size / 2 / 60f / Point.getFOV())) {
            Rectangle potential = findPotential(x, y, rmin);
            if (potential != null) set.add(potential);
            return potential;
        } else return rmin;
    }

    /**
     * @param x - press coords
     * @param y
     * @param r - closest rectangle to the pressed point
     * @return
     */
    private Rectangle findPotential(double x, double y, Rectangle r) {
        Set<Rectangle> localset = new HashSet<Rectangle>();
        double stepRa = DSS.size / 60f * 24f / 360f / cos((r.dec + DSS.size / 60f) * PI / 180);
        double stepDec = DSS.size / 60f;

        Rectangle up = new Rectangle(r.ra, r.dec + stepDec);
        Rectangle down = new Rectangle(r.ra, r.dec - stepDec);
        Rectangle right = new Rectangle(r.ra + stepRa, r.dec);
        Rectangle left = new Rectangle(r.ra - stepRa, r.dec);
        localset.add(up);
        localset.add(down);
        localset.add(right);
        localset.add(left);
        double min = 10000;
        Rectangle rmin = null;

        //looking for potential rectangle closest to the point pressed
        for (Rectangle rect : localset) {
            double dist = rect.distance(x, y);
            if (dist < min) {
                min = dist;
                rmin = rect;
            }
        }
        //check if the distance is too far
        if ((min / Point.getWidth() > DSS.size / 60f / Point.getFOV())) return null;
        //check if there is an rectangle on this place already
        if (rmin != null) {
            boolean thereis = false;
            for (Rectangle rect : set) {
                if (rect.coordEqual(rmin)) {
                    thereis = true;
                    break;
                }
            }
            if (!thereis) return rmin;
        }
        return null;

    }

    /**
     * deselects all rectangles. Selection is performed via interface access to Rectangle class
     */
    public void deselectAll() {
        for (Rectangle r : set) {
            r.deselect();
        }
    }

}
