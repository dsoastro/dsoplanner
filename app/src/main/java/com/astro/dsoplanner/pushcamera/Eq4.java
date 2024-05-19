package com.astro.dsoplanner.pushcamera;

import com.astro.dsoplanner.util.Holder2;

public class Eq4 {

    //check for null reverse matrix !!!
    private static final double h = 0.0001;
    F4 f1, f2, f3, f4;

    public Eq4(F4 f1, F4 f2, F4 f3, F4 f4) {
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.f4 = f4;
    }

    private double diff(F4 f, V4 v, double fv, int index) {
        double[] va = v.getArray();
        double x1 = va[0];
        double x2 = va[1];
        double x3 = va[2];
        double x4 = va[3];

        switch (index) {
            case 1:
                return (f.f(x1 + h, x2, x3, x4) - fv) / h; // df/dx1
            case 2:
                return (f.f(x1, x2 + h, x3, x4) - fv) / h; // df/dx2
            case 3:
                return (f.f(x1, x2, x3 + h, x4) - fv) / h; // df/dx3
            case 4:
                return (f.f(x1, x2, x3, x4 + h) - fv) / h; // df/dx4
            default:
                return Double.NaN; //should not be
        }

    }

    private M4 W(double f1v, double f2v, double f3v, double f4v, V4 v) {
        M4 w = new M4();

        w.set(diff(f1, v, f1v, 1), 0, 0);
        w.set(diff(f1, v, f1v, 2), 0, 1);
        w.set(diff(f1, v, f1v, 3), 0, 2);
        w.set(diff(f1, v, f1v, 4), 0, 3);

        w.set(diff(f2, v, f2v, 1), 1, 0);
        w.set(diff(f2, v, f2v, 2), 1, 1);
        w.set(diff(f2, v, f2v, 3), 1, 2);
        w.set(diff(f2, v, f2v, 4), 1, 3);

        w.set(diff(f3, v, f3v, 1), 2, 0);
        w.set(diff(f3, v, f3v, 2), 2, 1);
        w.set(diff(f3, v, f3v, 3), 2, 2);
        w.set(diff(f3, v, f3v, 4), 2, 3);

        w.set(diff(f4, v, f4v, 1), 3, 0);
        w.set(diff(f4, v, f4v, 2), 3, 1);
        w.set(diff(f4, v, f4v, 3), 3, 2);
        w.set(diff(f4, v, f4v, 4), 3, 3);

        return w;

    }

    public Holder2<V4, V4> next(V4 v) {
        double[] vs = v.getArray();
        double x1 = vs[0];
        double x2 = vs[1];
        double x3 = vs[2];
        double x4 = vs[3];

        double f1v = f1.f(x1, x2, x3, x4);
        double f2v = f2.f(x1, x2, x3, x4);
        double f3v = f3.f(x1, x2, x3, x4);
        double f4v = f4.f(x1, x2, x3, x4);
        M4 w = W(f1v, f2v, f3v, f4v, v);
        V4 fx = new V4(f1v, f2v, f3v, f4v);
        V4 tmp = w.inverse().dot(fx);
        return new Holder2<V4, V4>(v.minus(tmp), new V4(f1v, f2v, f3v, f4v));
    }

    public static void main(String[] args) {
        F4 f1 = new F4() {

            @Override
            public double f(double x1, double x2, double x3, double x4) {

                return x1 + x2 + x3 + x4 - 10;
            }
        };
        F4 f2 = new F4() {

            @Override
            public double f(double x1, double x2, double x3, double x4) {

                return x1 * 6 + x2 * 2 + x3 * 5 + x4 - 29;
            }
        };
        F4 f3 = new F4() {

            @Override
            public double f(double x1, double x2, double x3, double x4) {

                return x1 + 3 * x2 + 2 * x3 + 4 * x4 - 29;
            }
        };
        F4 f4 = new F4() {

            @Override
            public double f(double x1, double x2, double x3, double x4) {

                return x1 * 7 + x2 * 8 + x3 * 9 + x4 * 11 - 94;
            }
        };
        Eq4 eq = new Eq4(f1, f2, f3, f4);
        V4 v = new V4(0, 0, 0, 0);
        for (int i = 0; i < 20; i++) {
            Holder2<V4, V4> h = eq.next(v);
            v = h.x;
            System.out.println(v);
        }

    }

}
