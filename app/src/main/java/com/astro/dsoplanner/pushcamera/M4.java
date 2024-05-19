package com.astro.dsoplanner.pushcamera;

import com.astro.dsoplanner.matrix.DMatrix;

public class M4 {
    //http://www.mathprofi.ru/kak_naiti_obratnuyu_matricu.html

    double[][] a = new double[4][4];

    public String toString() {
        String s = "";
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                s += a[i][j] + " ";
            }
            s += "\n";
        }
        return s;
    }

    public M4() {

    }

    public M4(double[][] m) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                a[i][j] = m[i][j];
            }
        }
    }

    public void set(double value, int i, int j) {
        a[i][j] = value;
    }

    public double get(int i, int j) {
        return a[i][j];
    }

    private double[][] getMinor(int i, int j) {
        double[][] m = new double[3][3]; //for minor calculations

        int l = 0;
        int k = 0;
        int lm = 0;
        int lk = 0;
        while (l < 4) {
            if (l == i) {
                l++;
                if (l == 4)
                    break;
            }
            k = 0;
            lk = 0;
            while (k < 4) {
                if (k == j) {
                    k++;
                    if (k == 4)
                        break;
                }
                m[lm][lk] = a[l][k];
                lk++;
                k++;

            }
            lm++;
            l++;

        }
        return m;
    }

    public M4 T() {
        double[][] b = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                b[i][j] = a[j][i];
            }
        }
        return new M4(b);
    }

    public double det() {
        DMatrix m1 = new DMatrix(getMinor(0, 0));
        DMatrix m2 = new DMatrix(getMinor(0, 1));
        DMatrix m3 = new DMatrix(getMinor(0, 2));
        DMatrix m4 = new DMatrix(getMinor(0, 3));
        return a[0][0] * m1.det() - a[0][1] * m2.det() + a[0][2] * m3.det() - a[0][3] * m4.det();
    }

    public V4 dot(V4 v) {
        double[] vec = v.getArray();
        double[] res = new double[4];
        for (int i = 0; i < 4; i++) {
            double sum = 0;
            for (int j = 0; j < 4; j++) {
                sum += a[i][j] * vec[j];
            }
            res[i] = sum;
        }
        return new V4(res);
    }

    public M4 inverse() {
        double d = det();
        if (d == 0) //inverse matrix does not exist
            return null;
        M4 mm = new M4();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double[][] minor = getMinor(i, j);
                double det = new DMatrix(minor).det();
                mm.set(det, i, j);
            }
        }

        M4 mt = mm.T();
        int sign = 1;
        for (int i = 0; i < 4; i++) {
            if (i % 2 == 1)
                sign = -1;
            else
                sign = 1;
            for (int j = 0; j < 4; j++) {
                mt.set(mt.get(i, j) / d * sign, i, j);
                sign *= -1;
            }
        }
        return mt;

    }

    public static void print(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) {
        double[][] m = {{1, 8, 9, -1}, {1, -3, 6, -1}, {1, 1, 3, 1}, {1, 1, 1, -4}};
        print(new M4(m).inverse().toString());
    }

}
