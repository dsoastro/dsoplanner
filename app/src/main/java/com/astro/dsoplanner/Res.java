package com.astro.dsoplanner;

/**
 * Operation's result value interface and implementations.
 *
 * <li>Could be any defined type, currently only String, Bool, and Double.
 * <li>Controls the conversion between types.
 */
interface Res {
    String S_TRUE = "TRUE";
    String S_FALSE = "FALSE";
    double accuracy = 1e-5; //todo: must be single source (now in Analizator class too.

    enum Type { DBL, STR, BUL }

    Type getType();

    Res add(Res v);

    Res sub(Res v);

    Res mul(Res v);

    Res div(Res v);

    Res neg();

    Res not();

    Res and(Res v);

    Res or(Res v);

    Res xor(Res v);

    Res like(Res v);

    Res more(Res v);

    Res less(Res v);

    Res eq(Res v);

    Res neq(Res v);


    /*********************************************************************************************
     * Double
     */
    class Dbl implements Res {
        static final Double BAD_DATA = 0.;
        Double val;

        Dbl(Double v) {
            val = v;
        }

        Dbl(Res v) {
            switch (v.getType()) {
                case DBL:
                    val = ((Dbl) v).getVal();
                    break;
                case STR:
                    try {
                        val = Double.valueOf(((Str) v).getVal());
                    } catch (Exception e) {
                        val = BAD_DATA;
                    }
                    break;
                case BUL:
                    val = ((Bul) v).getVal() ? 1. : 0.;
                    break;
            }
        }

        Double getVal() {
            return val;
        }

        @Override
        public Type getType() {
            return Type.DBL;
        }

        @Override
        public Res add(Res v) {
            return new Dbl(val + new Dbl(v).getVal());
        }

        @Override
        public Res sub(Res v) {
            return new Dbl(val - new Dbl(v).getVal());
        }

        @Override
        public Res mul(Res v) {
            if (Math.abs(val + 1) < accuracy ) { //delayed negative op
                return v.neg();
            }
            return new Dbl(val * new Dbl(v).getVal());
        }

        @Override
        public Res div(Res v) {
            return new Dbl(val / new Dbl(v).getVal());
        }

        @Override
        public Res neg() {
            return new Dbl(-val);
        }

        @Override
        public Res not() {
            return new Bul(this).not();
        }

        @Override
        public Res and(Res v) {
            return new Bul(new Bul(this).and(new Bul(v)));
        }

        @Override
        public Res or(Res v) {
            return new Bul(new Bul(this).or(new Bul(v)));
        }

        @Override
        public Res xor(Res v) {
            return new Bul(this).xor(new Bul(v));
        }

        @Override
        public Res like(Res v) {
            return new Str(this).like(new Str(v));
        }

        @Override
        public Res more(Res v) {
            return new Bul(val > new Res.Dbl(v).getVal());
        }

        @Override
        public Res less(Res v) {
            return new Bul(val < new Res.Dbl(v).getVal());
        }

        @Override
        public Res eq(Res v) {
            return new Bul(Math.abs(val - new Res.Dbl(v).getVal()) <= accuracy);
        }

        @Override
        public Res neq(Res v) {
            return new Bul(Math.abs(val - new Res.Dbl(v).getVal()) > accuracy);
        }
    }

    /*********************************************************************************************
     * String first
     */
    class Str implements Res {
        static final String BAD_DATA = "";
        String val;

        Str(String v) {
            val = v;
        }

        Str(Res v) {
            switch (v.getClass().getSimpleName()) {
                case "Dbl":
                    val = String.valueOf(((Dbl) v).getVal());
                    break;
                case "Str":
                    val = ((Str) v).getVal();
                    break;
                case "Bul": //if empty str or "FALSE" todo: prob. too much ambiguity
                    val = ((Bul) v).getVal() ? S_TRUE : S_FALSE;
                    break;
            }
        }

        String getVal() {
            return val;
        }

        @Override
        public Type getType() {
            return Type.DBL;
        }

        @Override
        public Res add(Res v) {
            return new Str(val + new Str(v).getVal());
        }

        @Override
        public Res sub(Res v) { //Delete all substing matches
            return new Str(val.replaceAll(new Str(v).getVal(), ""));
        }

        @Override
        public Res mul(Res v) { //2*"z" = "zz", "abc"*2 = "abcabc"
            double d = (new Dbl(this).getVal());
            String res = "", arg = (new Str(v).getVal());
            if (d < 1) { //x is not convertible string, try y
                d = (new Dbl(v).getVal());
                arg = val;
            }
            if (d < 1) { //no numbers to multiply found or zero
                return new Str("");
            }
            //else, multiply string to number
            for (int i = (int) d; d > 0; d--) {
                res += arg;
            }
            return new Str(res);
        }

        @Override
        public Res div(Res v) { //cut string at delimiter (regex supported)
            String[] res = val.split(new Str(v).getVal(), 1);
            return new Str(res[0]);
        }

        @Override
        public Res neg() { //todo: Weird
            return new Str("-" + val);
        }

        @Override
        public Res not() {
            return new Bul(this).not();
        }

        @Override
        public Res and(Res v) {
            return new Bul(new Bul(this).and(new Bul(v)));
        }

        @Override
        public Res or(Res v) {
            return new Bul(new Bul(this).or(new Bul(v)));
        }

        @Override
        public Res xor(Res v) {
            return new Bul(this).xor(new Bul(v));
        }

        @Override
        public Bul like(Res v) {
            String a = val, b = new Str(v).getVal();
            boolean res;
            if (b.endsWith("*")) {
                res = a.startsWith(b.substring(0, b.length() - 2));
            } else if (b.startsWith("*")) {
                res = a.endsWith(b.substring(1));
            } else {
                res = a.contains(b);
            }
            return new Bul(res);
        }

        @Override
        public Bul more(Res v) {
            return new Bul(val.compareTo(new Str(v).getVal()) > 0);
        }

        @Override
        public Res less(Res v) {
            return new Bul(val.compareTo(new Str(v).getVal()) < 0);
        }

        @Override
        public Res eq(Res v) {
            return new Bul(val.compareTo(new Str(v).getVal()) == 0);
        }

        @Override
        public Res neq(Res v) {
            return new Bul(val.compareTo(new Str(v).getVal()) != 0);
        }
    }

    /*********************************************************************************************
     * Boolean
     */
    class Bul implements Res {
        static final Boolean BAD_DATA = false;
        Boolean val;

        Bul(Boolean v) {
            val = v;
        }

        Bul(Res v) {
            switch (v.getClass().getSimpleName()) {
                case "Dbl":
                    val = !(((Dbl) v).getVal() <= accuracy);
                    break;
                case "Str":
                    val = !(((Str) v).getVal().contentEquals(S_FALSE)
                        || ((Str) v).getVal().isEmpty());
                    break;
                case "Bul":
                    val = ((Bul) v).getVal();
                    break;
            }
        }

        Boolean getVal() {
            return val;
        }

        @Override
        public Type getType() {
            return Type.BUL;
        }

        @Override
        public Res add(Res v) {
            return new Bul(val && new Bul(v).getVal());
        }

        @Override
        public Res sub(Res v) {
            return this.xor(new Bul(v));
        }

        @Override
        public Res mul(Res v) {
            return new Bul(val || new Bul(v).getVal());
        }

        @Override
        public Res div(Res v) {
            return new Bul(val && new Bul(v).getVal());
        }

        @Override
        public Res neg() {
            return new Bul(!val);
        }

        @Override
        public Res not() {
            return new Bul(!val);
        }

        @Override
        public Res and(Res v) {
            return new Bul(val && new Bul(v).getVal());
        }

        @Override
        public Res or(Res v) {
            return new Bul(val || new Bul(v).getVal());
        }

        @Override
        public Res xor(Res v) {
            boolean x = val, y = new Bul(v).getVal();
            return new Bul((x || y) && !(x && y));
        }

        @Override
        public Res like(Res v) {
            return new Bul(val == new Bul(v).getVal());
        }

        @Override
        public Res more(Res v) {
            boolean y = new Bul(v).getVal();
            return new Bul(val && !y);
        }

        @Override
        public Res less(Res v) {
            boolean y = new Bul(v).getVal();
            return new Bul(!val && y);
        }

        @Override
        public Res eq(Res v) {
            boolean y = new Bul(v).getVal();
            return new Bul(val == y);
        }

        @Override
        public Res neq(Res v) {
            boolean y = new Bul(v).getVal();
            return new Bul(val != y);
        }
    }
}
