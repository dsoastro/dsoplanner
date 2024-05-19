package com.astro.dsoplanner.graph;

public class XY {
    public double x;
    public double y;

    public XY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "XY{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
