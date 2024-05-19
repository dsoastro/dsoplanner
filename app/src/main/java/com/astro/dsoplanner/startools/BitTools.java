package com.astro.dsoplanner.startools;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class BitTools {

    private static final String _11111111111 = "11111111111";
    private static final String _111111111111111111111 = "111111111111111111111";
    private static final String _110000000000000000000000000000 = "110000000000000000000000000000";
    private static final String _1111111111111100000000000000 = "1111111111111100000000000000";
    private static final String _11111111111111 = "11111111111111";

    public static class TycData {

        private static final String TYC32 = ", tyc3=";
        private static final String TYC22 = ", tyc2=";
        private static final String TYC_DATA_TYC1 = "TycData [tyc1=";

        public int tyc1;
        public int tyc2;
        public int tyc3;

        public TycData(int tyc1, int tyc2, int tyc3) {
            super();
            this.tyc1 = tyc1;
            this.tyc2 = tyc2;
            this.tyc3 = tyc3;
        }

        @Override
        public String toString() {
            return TYC_DATA_TYC1 + tyc1 + TYC22 + tyc2 + TYC32
                    + tyc3 + "]";
        }

    }

    static class RaDec {

        private static final String DEC2 = ", dec=";
        private static final String RA_DEC_RA = "RaDec [ra=";


        double ra;
        double dec;

        public RaDec(double ra, double dec) {
            super();
            this.ra = ra;
            this.dec = dec;
        }

        @Override
        public String toString() {
            return RA_DEC_RA + ra + DEC2 + dec + "]";
        }

    }

    static class IndexRange {
        long i1;
        long i2;

        public IndexRange(long i1, long i2) {
            super();
            this.i1 = i1;
            this.i2 = i2;
        }
    }

    public static int convertTycToInt(int tyc1, int tyc2, int tyc3) {
        //tyc1 1 - 9537   14bits
        //tyc 2  1 - 12121   14 bits
        //tyc3 1-3	2 bits
        return (tyc3 << 28) | (tyc2 << 14) | tyc1;
    }

    public static TycData convertIntToTyc(int tyc) {
        final int mask1 = Integer.parseInt(_11111111111111, 2);
        final int mask2 = Integer.parseInt(_1111111111111100000000000000, 2);
        final int mask3 = Integer.parseInt(_110000000000000000000000000000, 2);
        int tyc1 = (tyc & mask1);
        int tyc2 = (tyc & mask2) >> 14;
        int tyc3 = (tyc & mask3) >> 28;
        return new TycData(tyc1, tyc2, tyc3);

    }

    public static void main(String args[]) {
        int res = convertTycToInt(9873, 11456, 3);
        System.out.println(convertIntToTyc(res));

        long index = getTychoIndex(234.5678, 45.3478);
        System.out.println(getRaDecFromTychoIndex(index));
    }

    public static long getTychoIndex(long raint, long decint) {
        final long mask21 = Integer.parseInt(_111111111111111111111, 2);

        long index = ((decint & mask21) << 21) | (raint & mask21);//21 bit of dec, then 21 bit of ra
        index = ((raint >> 21) << 42) | index;//adding remaining bits of ra
        index = ((decint >> 21) << 53) | index;//adding remaining bits of dec, 53=21+21+(32-21);
        return index;
    }

    public static long getTychoIndex(double ra, double dec) {
        long raint = (long) (ra * 360 / 24 * 3600 * 100);//ra in degress
        long decint = (long) ((dec + 90) * 3600 * 100);
        final long mask21 = Integer.parseInt(_111111111111111111111, 2);

        long index = ((decint & mask21) << 21) | (raint & mask21);//21 bit of dec, then 21 bit of ra
        index = ((raint >> 21) << 42) | index;//adding remaining bits of ra
        index = ((decint >> 21) << 53) | index;//adding remaining bits of dec, 53=21+21+(32-21);
        return index;
    }

    public static RaDec getRaDecFromTychoIndex(long index) {
        final long mask21 = Integer.parseInt(_111111111111111111111, 2);
        final long mask11 = Integer.parseInt(_11111111111, 2);
        long raint = (long) (index & mask21);//first 21 bit of ra
        long decint = (long) ((index & (mask21 << 21)) >> 21);//first 21 bit of dec
        raint = (((index & (mask11 << 42)) >> 21) | raint);//last 11 bits of ra
        decint = (((index & (mask11 << 53)) >> 32) | decint);//last 11 bits of dec. 32=53-21
        double ra = raint / 360000f * 24 / 360f;
        double dec = decint / 360000f - 90;
        return new RaDec(ra, dec);

    }

    public static void out(String s) {
        System.out.println(s);
    }

    public static byte[] toByte(int val) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        out.writeInt(val);
        out.close();
        return stream.toByteArray();
    }
}
