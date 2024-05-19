package com.astro.dsoplanner.pushcamera;

import com.astro.dsoplanner.matrix.DVector;

public class P3 {
    public double x, y, z;

    public P3(double x, double y, double z) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public P3(DVector v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }
}
