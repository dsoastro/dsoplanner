package com.astro.dsoplanner.pushcamera;

import java.util.Arrays;

public class V4 {
    double[] v = new double[4];
    
    @Override
    public String toString() {
        return "V4 [v=" + Arrays.toString(v) + "]";
    }

    public V4(double[] v) {
        for (int i = 0; i < 4; i++) {
            this.v[i] = v[i];
        }
    }

    public V4(double x1, double x2, double x3, double x4) {
        v[0] = x1;
        v[1] = x2;
        v[2] = x3;
        v[3] = x4;
    }

    public double get(int i) {
        return v[i];
    }

    public double[] getArray() {
        return v;
    }

    public V4 minus(V4 v) {
        double[] v1 = v.getArray();
        double[] v2 = new double[4];
        for (int i = 0; i < 4; i++) {
            v2[i] = this.v[i] - v1[i];
        }
        return new V4(v2);
    }

    public static void main(String[] args) {
        System.out.println(1);
    }
}
