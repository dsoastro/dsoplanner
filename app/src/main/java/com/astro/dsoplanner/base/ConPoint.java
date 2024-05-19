package com.astro.dsoplanner.base;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.StarData;

import java.io.Serializable;

//constellation point
public class ConPoint extends Point implements Serializable {
    private static final String TAG = ConPoint.class.getSimpleName();
    private int hrs; //initial hr number (Yale)
    private int hre; //end hr number

    Point end;

    public ConPoint(int hrs, int hre) {
        super(0, 0);
        HrStar s = Global.databaseHr[StarData.ConvHrToRow(hrs)];
        if (s != null) { //SAND
            ra = s.ra;
            dec = s.dec;
        }
        this.hrs = hrs;
        this.hre = hre;

        end = new HrStar(Global.databaseHr[StarData.ConvHrToRow(hre)]);
    }

    public ConPoint(ConPoint c) {
        super(c.ra, c.dec);
        this.hrs = c.getHrs();
        this.hre = c.getHre();
        end = new HrStar(Global.databaseHr[StarData.ConvHrToRow(hre)]);
    }

    public int getHrs() {
        return hrs;
    }

    public int getHre() {
        return hre;
    }

    @Override
    public void raiseNewPointFlag() {
        super.raiseNewPointFlag();
        end.raiseNewPointFlag();
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        setXY();
        setDisplayXY();
        end.setXY();
        end.setDisplayXY();
        float xd = getXd();
        float yd = getYd();
        float exd = end.getXd();
        float eyd = end.getYd();
        //avoid stray constellation lines by checking the distance
        if ((xd - exd) * (xd - exd) + (yd - eyd) * (yd - eyd) < Point.getWidth() * Point.getHeight() * 10)
            canvas.drawLine(getXd(), getYd(), end.getXd(), end.getYd(), paint);//draw line from initial point to the end point
    }
}
