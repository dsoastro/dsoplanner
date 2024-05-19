package com.astro.dsoplanner.matrix;

import android.content.Context;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

//tested, not used now

public class DMatrix {
    Line a1;
    Line a2;
    Line a3;

    public DMatrix() {
        a1 = new Line();
        a2 = new Line();
        a3 = new Line();
    }

    public DMatrix(double[][] m) {
        a1 = new Line(m[0][0], m[0][1], m[0][2]);
        a2 = new Line(m[1][0], m[1][1], m[1][2]);
        a3 = new Line(m[2][0], m[2][1], m[2][2]);
    }

    public DMatrix(Line a1, Line a2, Line a3) {
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
    }

    public DMatrix(Axis axis, double angle) {//angle - radians
        switch (axis) {
            case Z:
                a1 = new Line(cos(angle), sin(angle), 0);
                a2 = new Line(-sin(angle), cos(angle), 0);
                a3 = new Line(0, 0, 1);
                break;
            case X:
                a1 = new Line(1, 0, 0);
                a2 = new Line(0, cos(angle), sin(angle));
                a3 = new Line(0, -sin(angle), cos(angle));
                break;
            case Y:
                a1 = new Line(cos(angle), 0, -sin(angle));
                a2 = new Line(0, 1, 0);
                a3 = new Line(sin(angle), 0, cos(angle));
                break;
        }

    }

    public DVector timesVector(DVector v) {
        return (new DVector(a1.timesVector(v), a2.timesVector(v), a3.timesVector(v)));
    }

    public DMatrix timesMatrix(DMatrix m) {
        DVector f = m.firstColumn();
        DVector s = m.secondColumn();
        DVector t = m.thirdColumn();
        Line fLine = new Line(a1.timesVector(f), a1.timesVector(s), a1.timesVector(t));
        Line sLine = new Line(a2.timesVector(f), a2.timesVector(s), a2.timesVector(t));
        Line tLine = new Line(a3.timesVector(f), a3.timesVector(s), a3.timesVector(t));
        return new DMatrix(fLine, sLine, tLine);
    }

    private double det(double a11, double a12, double a21, double a22) {
        return (a11 * a22 - a12 * a21);
    }

    double det(DMatrix m) {
        return (a1.x * det(a2.y, a2.z, a3.y, a3.z) - a1.y * det(a2.x, a2.z, a3.x, a3.z) + a1.z * det(a2.x, a2.y, a3.x, a3.y));
    }

    public double det() {
        return (a1.x * det(a2.y, a2.z, a3.y, a3.z) - a1.y * det(a2.x, a2.z, a3.x, a3.z) + a1.z * det(a2.x, a2.y, a3.x, a3.y));
    }

    public DMatrix backMatrix() {
        DMatrix b = new DMatrix();
        double d = det(this);
        b.a1.x = det(a2.y, a2.z, a3.y, a3.z) / d;
        b.a1.y = -det(a2.x, a2.z, a3.x, a3.z) / d;
        b.a1.z = det(a2.x, a2.y, a3.x, a3.y) / d;
        b.a2.x = -det(a1.y, a1.z, a3.y, a3.z) / d;
        b.a2.y = det(a1.x, a1.z, a3.x, a3.z) / d;
        b.a2.z = -det(a1.x, a1.y, a3.x, a3.y) / d;
        b.a3.x = det(a1.y, a1.z, a2.y, a2.z) / d;
        b.a3.y = -det(a1.x, a1.z, a2.x, a2.z) / d;
        b.a3.z = det(a1.x, a1.y, a2.x, a2.y) / d;

        return new DMatrix(new Line(b.a1.x, b.a2.x, b.a3.x), new Line(b.a1.y, b.a2.y, b.a3.y), new Line(b.a1.z, b.a2.z, b.a3.z));

    }

    DVector firstColumn() {
        return new DVector(a1.x, a2.x, a3.x);
    }

    DVector secondColumn() {
        return new DVector(a1.y, a2.y, a3.y);
    }

    DVector thirdColumn() {
        return new DVector(a1.z, a2.z, a3.z);
    }

    public DMatrix T() {
        DMatrix m = new DMatrix(firstColumn(), secondColumn(), thirdColumn());
        return m;
    }

    void print() {
        System.out.println(a1 + " " + a2 + " " + a3);
    }

    public String toString() {
        return ("[ " + a1 + " ],[ " + a2 + " ],[ " + a3 + " ];");

    }

    public void save(Context context, String key) {
        a1.save(context, key + "_1");
        a2.save(context, key + "_2");
        a3.save(context, key + "_3");
    }

    public static DMatrix restore(Context context, String key) {
        Line a1 = Line.restore(context, key + "_1");
        Line a2 = Line.restore(context, key + "_2");
        Line a3 = Line.restore(context, key + "_3");
        if (a1 != null && a2 != null && a3 != null)
            return new DMatrix(a1, a2, a3);
        else
            return null;

    }

    public static void clearSharedPrefs(Context context, String key) {
        Line.clearSharedPrefs(context, key + "_1");
        Line.clearSharedPrefs(context, key + "_2");
        Line.clearSharedPrefs(context, key + "_3");
    }
}

