package com.astro.dsoplanner.pushcamera;

public class M2 {
    double a11, a12, a21, a22;

    public M2(double a11, double a12, double a21, double a22) {
        super();
        this.a11 = a11;
        this.a12 = a12;
        this.a21 = a21;
        this.a22 = a22;
    }

    public String toString() {
        String s = "" + a11 + " " + a12 + "\n" + a21 + " " + a22;
        return s;
    }

    public M2 T() {
        return new M2(a11, a21, a12, a22);
    }

    public double det() {
        return (a11 * a22 - a12 * a21);
    }

    public V2 dot(V2 v) {
        double[] va = v.getArray();
        double v1 = a11 * va[0] + a12 * va[1];
        double v2 = a21 * va[0] + a22 * va[1];
        return new V2(v1, v2);
    }

    public M2 inverse() {
        double d = det();
        if (d == 0) //inverse matrix does not exist
            return null;
        M2 m = new M2(a22 / d, -a12 / d, -a21 / d, a11 / d);
        return m;
    }

    public static void main(String[] args) {
        System.out.println(new M2(1, 2, 3, 4).dot(new V2(2, 3)));
    }

}
