package com.astro.dsoplanner.base;

import static com.astro.dsoplanner.Constants.A;
import static com.astro.dsoplanner.Constants.B;
import static com.astro.dsoplanner.Constants.PA;
import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.max;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PathEffect;
import android.graphics.RectF;

import com.astro.dsoplanner.AstroTools;

import com.astro.dsoplanner.LabelLocations;


import com.astro.dsoplanner.SettingsActivity;

public abstract class ExtendedObject extends AstroObject {
    
    private static final String B2 = ", b=";
    private static final String EXTENDED_OBJECT_A = "ExtendedObject [a=";
    

    public double a;
    public double b;
    public double pa;
    public boolean layer = false;
    private static final String TAG = ExtendedObject.class.getSimpleName();

    @Override
    public String toString() {
        return super.toString() + EXTENDED_OBJECT_A + a + B2 + b + "]";
    }

    public ExtendedObject(double ra, double dec, int con, int type, int catalog, int id, double a, double b, double mag, double pa) {
        super(ra, dec, mag, con, type, catalog, id);
        this.a = a;
        this.b = b;
        this.pa = pa;

    }

    public ExtendedObject(DataInputStream stream) throws IOException {
        super(stream);
        a = stream.readDouble();
        b = stream.readDouble();
        pa = stream.readDouble();
    }

    public boolean hasDimension() {
        return true;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getPA() {
        return pa;
    }

    public void setLayerFlag() {
        layer = true;
    }


    @Override
    public byte[] getByteRepresentation() {
        byte[] buf = super.getByteRepresentation();
        ByteArrayOutputStream buff = new ByteArrayOutputStream(4 + 4 + 4);
        DataOutputStream stream = new DataOutputStream(buff);
        try {
            stream.writeDouble(a);
            stream.writeDouble(b);
            stream.writeDouble(pa);

        } catch (IOException e) {
            return null;
        }
        byte[] newBuff = buff.toByteArray();
        byte[] combBuff = new byte[buf.length + newBuff.length];
        System.arraycopy(buf, 0, combBuff, 0, buf.length);
        System.arraycopy(newBuff, buf.length - buf.length, combBuff, buf.length, combBuff.length - buf.length);

        return combBuff;
    }

    public HashMap<String, String> getStringRepresentation() {
        HashMap<String, String> map = super.getStringRepresentation();
        map.put(A, String.format(Locale.US, "%.6f", a));
        map.put(B, String.format(Locale.US, "%.6f", b));
        map.put(PA, String.format(Locale.US, "%.6f", pa));
        return map;

    }

    private static Path static_path = new Path();

    protected static Path getCleanPath() {
        static_path.rewind();
        return static_path;
    }

    private static RectF static_rect = new RectF(0, 0, 0, 0);

    protected static RectF getStaticRectF(float left, float top, float right, float bottom) {
        static_rect.set(left, top, right, bottom);
        return static_rect;
    }

    private static Matrix static_matrix = new Matrix();

    protected static Matrix getCleanMatrix() {
        static_matrix.reset();
        return static_matrix;
    }

    private static PathEffect static_path_effect_241 = new DashPathEffect(new float[]{2, 4}, 1);

    private static PathEffect getStaticPathEffect241() {
        return static_path_effect_241;
    }

    private static PathEffect static_path_effect_121 = new DashPathEffect(new float[]{1, 2}, 1);

    protected static PathEffect getStaticPathEffect121() {
        return static_path_effect_121;
    }


    private Point one_point = new Point(0, 0);

    protected Point getCleanPoint(double ra, double dec) {
        one_point.ra = (float) ra;
        one_point.dec = (float) dec;
        one_point.raiseNewPointFlag();
        return one_point;

    }

    @Override
    public void draw(Canvas canvas, final Paint paint) {
        if (!Point.withinBounds(getXd(), getYd(), (float) (max(a, b) / 60 * Point.getWidth() / Point.getFOV() / 2)))
            return;

        double scale = Point.getWidth() / Point.getFOV() / 2;
        double size = Math.max(a, b) * scale / 60;


        boolean startype = (type == AstroObject.DoubleStar || type == AstroObject.Star);
        boolean nullDim = (Double.isNaN(a) || Double.isNaN(b));
        if (!startype && !nullDim && Point.getFOV() <= SettingsActivity.dso_GetMinZoom() && SettingsActivity.dso_ShowShape() && size > SettingsActivity.dso_GetDimScaleFactor() * SettingsActivity.dso_Scale() * 12 * Point.getScalingFactor()) {//drawing in real dimension and position
            float orig_width = paint.getStrokeWidth();
            paint.setStrokeWidth(orig_width / 2);

            drawPath(canvas, paint);

            paint.setStrokeWidth(orig_width);
        } else {//drawing either as a unicode symbol / simple graph
            switch (type) {
                case 1: //gc
                    drawGC(canvas, paint);
                    break;
                case 2://gxy
                    drawGxy(canvas, paint);
                    break;
                case 3://gxy cld
                    drawNeb(canvas, paint);
                    break;
                case 4://HII
                    drawNeb(canvas, paint);
                    break;
                case AstroObject.DN:
                case 5://neb
                    drawNeb(canvas, paint);
                    break;
                case 6://OC
                    drawOC(canvas, paint);
                    break;
                case 7://OC+neb
                    drawOC(canvas, paint);
                    break;
                case 8://PN
                    drawPN(canvas, paint);
                    break;

                case 9://SNR
                    drawNeb(canvas, paint);
                    break;
                case 10://Custom Object
                    drawCross(canvas, paint);
                    break;
                case AstroObject.DoubleStar:
                    drawDoubleStar(canvas, paint);
                    break;
                case AstroObject.Comet:
                    drawComet(canvas, paint);
                    break;
                case AstroObject.MINOR_PLANET:
                    drawPlanet(canvas, paint);
                    break;
                case AstroObject.AST:
                case AstroObject.Star:
                    drawStar(canvas, paint);
                    break;
                case AstroObject.CG:
                    drawCircle(canvas, paint);
                    break;

                default:
                    drawCross(canvas, paint);

            }
        }

        if ((SettingsActivity.isObsObjectLabelOn() && !layer) || (SettingsActivity.isLayerObjectLabelOn() && layer)) {
            drawLabel(canvas, paint);
        }
    }

    protected void drawPath(Canvas canvas, Paint p) {
        double scale = Point.getWidth() / Point.getFOV() / 2;
        Path circle = getCleanPath();//new Path();
        Point n = new Point(ra, dec + 0.1);
        n.setXY();
        n.setDisplayXY();
        double angleNorth = atan2(n.getYd() - getYd(), n.getXd() - getXd());
        float angle = 0;
        double pavalue = pa;
        if (Double.isNaN(pavalue))
            pavalue = 0;
        if (Point.mirror == Point.NO_MIRROR)
            angle = (float) ((angleNorth * 180 / PI - pavalue));
        else
            angle = (float) ((angleNorth * 180 / PI + pavalue));


        double xl = getXd() - scale * a / 60;
        double xr = getXd() + scale * a / 60;
        double ytop = getYd() - scale * b / 60;
        double ybot = getYd() + scale * b / 60;
        RectF rect = getStaticRectF((float) xl, (float) ytop, (float) xr, (float) ybot);
        if (type == AstroObject.Neb || type == AstroObject.DN || type == AstroObject.HIIRgn || type == AstroObject.GxyCld || type == AstroObject.SNR) {
            circle.addRect(rect, Direction.CW);
        } else
            circle.addOval(rect, Direction.CW);
        if (type == GC) {
            circle.moveTo((float) xl, getYd());
            circle.lineTo((float) xr, getYd());

            circle.moveTo(getXd(), (float) ytop);
            circle.lineTo(getXd(), (float) ybot);
        }

        Matrix matrix = getCleanMatrix();//new Matrix();

        matrix.setRotate(angle, getXd(), getYd());
        circle.transform(matrix);


        if (type == OC || type == OCNeb) {
            PathEffect effect = getStaticPathEffect241();
            PathEffect orig_path_effect = p.getPathEffect();

            p.setPathEffect(effect);
            canvas.drawPath(circle, p);
            canvas.drawPath(circle, p);

            p.setPathEffect(orig_path_effect);
        } else
            canvas.drawPath(circle, p);
    }

    protected String getLabelName() {
        return getShortName();
    }

    protected void drawLabel(Canvas canvas, Paint p) {
        Style orig_style = p.getStyle();
        float orig_text_size = p.getTextSize();

        p.setStyle(Paint.Style.FILL);
        String name = getLabelName();
        float size = p.getTextSize() * Point.getScalingFactor();
        float offset = 7 * Point.getScalingFactor();
        p.setTextSize(size);

        float xl = xd + offset;
        float yl = yd - offset;
        LabelLocations ll = LabelLocations.getLabelLocations();
        if (ll.get(this, name.length())) {
            double angle = Point.getRotAngle();
            if (angle == 0)
                canvas.drawText(name, xl, yl, p);
            else {
                Path path = AstroTools.getLabelPath(xl, yl);
                canvas.drawTextOnPath(name, path, 0, 0, p);
            }

        }

        p.setStyle(orig_style);
        p.setTextSize(orig_text_size);


    }


    private void drawDoubleStar(Canvas canvas, Paint p) {

        Style orig_stile = p.getStyle();

        p.setStyle(Paint.Style.FILL);

        Path ds = getCleanPath();//new Path();
        float rad = 7 * SettingsActivity.dso_Scale() * Point.getScalingFactor();
        ds.addCircle(xd, yd, rad, Direction.CW);
        canvas.drawPath(ds, p);

        p.setStyle(Paint.Style.STROKE);
        ds = getCleanPath();//new Path();
        ds.moveTo(xd - 2 * rad, yd);
        ds.lineTo(xd + 2 * rad, yd);
        canvas.drawPath(ds, p);

        p.setStyle(orig_stile);

    }

    private void drawComet(Canvas canvas, Paint p) {
        Path comet = getCleanPath();//new Path();
        float st = 12 * SettingsActivity.dso_Scale() * Point.getScalingFactor();
        comet.moveTo(xd - st * 1.5f, yd);
        comet.lineTo(xd, yd);
        comet.moveTo(xd - st * 1.5f, yd + st / 2);
        comet.lineTo(xd, yd);
        comet.moveTo(xd - st * 1.5f, yd - st / 2);
        comet.lineTo(xd, yd);
        canvas.drawPath(comet, p);

        comet = getCleanPath();//new Path();
        comet.addCircle(xd, yd, st / 3, Direction.CW);

        Style orig_style = p.getStyle();
        p.setStyle(Paint.Style.FILL);

        canvas.drawPath(comet, p);

        p.setStyle(orig_style);
    }

    private void drawPlanet(Canvas canvas, Paint p) {

        Path pl = getCleanPath();//new Path();
        float rad = 9 * SettingsActivity.dso_Scale() * Point.getScalingFactor();
        pl.addCircle(xd, yd, rad, Direction.CW);
        canvas.drawPath(pl, p);
        pl = getCleanPath();//new Path();
        pl.moveTo(xd - rad, yd);
        pl.lineTo(xd + rad, yd);
        canvas.drawPath(pl, p);
    }

    //used for drawStar
    public static final float[] msin = new float[11];
    public static final float[] mcos = new float[11];

    static {
        for (int i = 0; i < 11; i++) {
            msin[i] = (float) (Math.sin(36. * i * Math.PI / 180.));
            mcos[i] = (float) (Math.cos(36. * i * Math.PI / 180.));
        }
    }

    private void drawStar(Canvas canvas, Paint p) {


        Path star = getCleanPath();//new Path();
        float rad = 9 * SettingsActivity.dso_Scale() * Point.getScalingFactor();
        star.moveTo(xd, yd + rad);
        for (int i = 1; i < 11; i++) {//i=0 - the highest point
            float dst;
            if (i % 2 == 0)
                dst = rad;
            else
                dst = rad / 2;
            float xf = (float) (xd + msin[i] * dst);
            float yf = (float) (yd + dst * mcos[i]);
            star.lineTo(xf, yf);
            star.moveTo(xf, yf);
        }
        canvas.drawPath(star, p);
    }

    private void drawCircle(Canvas canvas, Paint paint) {
        Path gxy = getCleanPath();//new Path();
        float st = 9 * SettingsActivity.dso_Scale() * Point.getScalingFactor();
        RectF rect = new RectF(xd - st, yd - st, xd + st, yd + st);
        gxy.addOval(rect, Direction.CW);
        canvas.drawPath(gxy, paint);
    }

    private void drawPN(Canvas canvas, Paint paint) {//triangle
        Path neb = getCleanPath();//new Path();
        float st = 14 * SettingsActivity.dso_Scale() * Point.getScalingFactor();
        neb.moveTo(xd, yd - st / 2);
        neb.lineTo(xd - st / 2, yd + st / 2);
        neb.lineTo(xd + st / 2, yd + st / 2);
        neb.lineTo(xd, yd - st / 2);
        canvas.drawPath(neb, paint);
    }

    private void drawCross(Canvas canvas, Paint paint) {
        Path cross = getCleanPath();//new Path();
        float st = 14 * SettingsActivity.dso_Scale() * Point.getScalingFactor();
        cross.moveTo(xd - st / 2, yd);
        cross.lineTo(xd + st / 2, yd);
        cross.moveTo(xd, yd - st / 2);
        cross.lineTo(xd, yd + st / 2);
        canvas.drawPath(cross, paint);
    }

    private void drawNeb(Canvas canvas, Paint paint) {
        Path neb = getCleanPath();//new Path();
        float st = 14 * SettingsActivity.dso_Scale() * Point.getScalingFactor();
        neb.moveTo(xd - st / 2, yd + st / 2);
        neb.lineTo(xd - st / 2, yd - st / 2);
        neb.lineTo(xd + st / 2, yd - st / 2);
        neb.lineTo(xd + st / 2, yd + st / 2);
        neb.lineTo(xd - st / 2, yd + st / 2);
        canvas.drawPath(neb, paint);

    }
	
	
    private void drawGxy(Canvas canvas, Paint paint) {
        Path gxy;

        float st = 12 * SettingsActivity.dso_Scale() * Point.getScalingFactor();

        gxy = getCleanPath();
        RectF rect = getStaticRectF(xd - st, yd - st / 2, xd + st, yd + st / 2);
        gxy.addOval(rect, Direction.CW);
        Matrix matrix = getCleanMatrix();//new Matrix();
        matrix.setRotate(45, getXd(), getYd());
        gxy.transform(matrix);
        canvas.drawPath(gxy, paint);
    }

    private void drawOC(Canvas canvas, Paint paint) {
        Path circle = getCleanPath();//new Path();
        circle.addCircle(xd, yd, 9 * SettingsActivity.dso_Scale() * Point.getScalingFactor(), Direction.CW);

        PathEffect effect = getStaticPathEffect121();//new DashPathEffect(new float[]{1,2},1);
        PathEffect orig_effect = paint.getPathEffect();
        paint.setPathEffect(effect);

        canvas.drawPath(circle, paint);
        canvas.drawPath(circle, paint);

        paint.setPathEffect(orig_effect);
    }

    private void drawGC(Canvas canvas, Paint paint) {
        Path circle = getCleanPath();//new Path();
        float st = 9 * SettingsActivity.dso_Scale() * Point.getScalingFactor();
        circle.addCircle(xd, yd, st, Direction.CW);
        circle.moveTo(xd - st, yd);
        circle.lineTo(xd + st, yd);
        circle.moveTo(xd, yd - st);
        circle.lineTo(xd, yd + st);
        canvas.drawPath(circle, paint);
    }
}
