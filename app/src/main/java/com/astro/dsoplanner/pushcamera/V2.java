package com.astro.dsoplanner.pushcamera;

import java.util.Arrays;

public class V2 {
    double[] v = new double[2];

    @Override
    public String toString() {
        return "V2 [v=" + Arrays.toString(v) + "]";
    }

    public V2(double[] v) {
        for (int i = 0; i < 2; i++) {
            this.v[i] = v[i];
        }
    }

    public V2(double x1, double x2) {
        v[0] = x1;
        v[1] = x2;

    }

    public double get(int i) {
        return v[i];
    }

    public double[] getArray() {
        return v;
    }

    public V2 minus(V2 v) {
        double[] v1 = v.getArray();
        double[] v2 = new double[2];
        for (int i = 0; i < 2; i++) {
            v2[i] = this.v[i] - v1[i];
        }
        return new V2(v2);
    }


}
