package com.astro.dsoplanner.matrix;

public class Matrix2 {
    double a11, a12, a21, a22;

    public Matrix2(double a11, double a12, double a21, double a22) {// a11 a12
        this.a11 = a11;                                            //a21 a22
        this.a12 = a12;
        this.a21 = a21;
        this.a22 = a22;
    }

    private double det() {
        return a11 * a22 - a21 * a12;
    }

    public Matrix2 backMatrix() {
        double d = det();
        double ta11 = a22 / d;
        double ta21 = -a21 / d;
        double ta12 = -a12 / d;
        double ta22 = a11 / d;
        return new Matrix2(ta11, ta12, ta21, ta22);

    }

    public Vector2 timesVector(Vector2 v) {
        return new Vector2(a11 * v.x + a12 * v.y, a21 * v.x + a22 * v.y);
    }

    public String toString() {
        return ("" + a11 + " " + a12 + " " + a21 + " " + a22);
    }

}
