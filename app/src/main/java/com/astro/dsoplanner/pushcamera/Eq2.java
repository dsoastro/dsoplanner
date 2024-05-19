package com.astro.dsoplanner.pushcamera;

import com.astro.dsoplanner.util.Holder2;

public class Eq2 {
    private static final double h = 0.0001;
    F2 f1, f2;

    public Eq2(F2 f1, F2 f2) {
        super();
        this.f1 = f1;
        this.f2 = f2;
    }

    private double diff(F2 f, V2 v, double fv, int index) {
        double[] va = v.getArray();
        double x1 = va[0];
        double x2 = va[1];


        switch (index) {
            case 1:
                return (f.f(x1 + h, x2) - fv) / h; // df/dx1
            case 2:
                return (f.f(x1, x2 + h) - fv) / h; // df/dx2

            default:
                return Double.NaN; //should not be
        }

    }

    private M2 W(double f1v, double f2v, V2 v) {
        double w11 = diff(f1, v, f1v, 1);
        double w12 = diff(f1, v, f1v, 2);
        double w21 = diff(f2, v, f2v, 1);
        double w22 = diff(f2, v, f2v, 2);
        M2 w = new M2(w11, w12, w21, w22);
        return w;
    }

    public Holder2<V2, V2> next(V2 v) {
        double[] vs = v.getArray();
        double x1 = vs[0];
        double x2 = vs[1];


        double f1v = f1.f(x1, x2);
        double f2v = f2.f(x1, x2);

        M2 w = W(f1v, f2v, v);
        V2 fx = new V2(f1v, f2v);
        V2 tmp = w.inverse().dot(fx);
        return new Holder2<V2, V2>(v.minus(tmp), new V2(f1v, f2v));
    }

    public static void main(String[] args) {
        F2 f1 = new F2() {

            @Override
            public double f(double x1, double x2) {

                return Math.sin(2 * x1 - x2) - 1.2 * x1 - 0.4;
            }
        };
        F2 f2 = new F2() {

            @Override
            public double f(double x1, double x2) {

                return 0.8 * x1 * x1 + 1.5 * x2 * x2 - 1;
            }
        };
        Eq2 eq = new Eq2(f1, f2);
        V2 v = new V2(0.4, -0.75);
        for (int i = 0; i < 20; i++) {
            Holder2<V2, V2> h = eq.next(v);
            v = h.x;
            System.out.println(v);
        }
    }

}
