package com.astro.dsoplanner;

import static java.lang.Math.max;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Path.Direction;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.CustomObject;
import com.astro.dsoplanner.base.Point;

/**
 * this is a center mark for ra dec dialog in Graph
 *
 * @author leonid
 */
public class CenterMarkObject extends CustomObject {
    private static final String CENTER_MARK = "center mark";
    public CenterMarkObject(double ra, double dec) {
        super(AstroCatalog.MARK_CATALOG, 0, ra, dec, AstroTools.getConstellation(ra, dec), AstroObject.Custom, "", 0, 0, 0, 0, CENTER_MARK, CENTER_MARK, "");
    }

    @Override
    public void draw(Canvas canvas, final Paint paint) {
        if (!Point.withinBounds(getXd(), getYd(), (float) (max(a, b) / 60 * Point.getWidth() / Point.getFOV() / 2)))
            return;
        Paint p = new Paint(paint);
        Path obj = new Path();
        p.setStrokeWidth(2);
        float st = 24 * SettingsActivity.dso_Scale();
        RectF rect = new RectF(xd - st, yd - st, xd + st, yd + st);
        obj.addOval(rect, Direction.CW);
        obj.moveTo(xd - 2 * st, yd);
        obj.lineTo(xd + 2 * st, yd);
        obj.moveTo(xd, yd - 2 * st);
        obj.lineTo(xd, yd + 2 * st);
        canvas.drawPath(obj, p);
    }

    @Override
    public boolean hasVisibility() {
        return false;
    }
}
