package com.astro.dsoplanner;

import android.util.Log;

import com.astro.dsoplanner.base.AstroObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.astro.dsoplanner.Constants.ALT;
import static com.astro.dsoplanner.Constants.CALDWELL;
import static com.astro.dsoplanner.Constants.CONSTEL;
import static com.astro.dsoplanner.Constants.HERSHELL;
import static com.astro.dsoplanner.Constants.MESSIER;
import static com.astro.dsoplanner.Constants.VIS;
import static com.astro.dsoplanner.Constants._ID;
import static com.astro.dsoplanner.Constants.constellations;


public class Analisator {
    private static final String QS = "QS";
    private static final String NF = "NF";
    private static final String AST = "AST";
    private static final String DN = "DN";
    private static final String CG = "CG";
    private static final String ADVANCED_SEARCH_NUMBER_CONVERSION_ERROR_OF = "Advanced search: number conversion error of ";
    private static final String INTEGER = "Integer";
    private static final String NAME2 = "Name";
    private static final String S = " != ";
    private static final String NOT = " NOT ";
    private static final String XOR = " XOR ";
    private static final String OR = " OR ";
    private static final String AND = " AND ";
    private static final String EXPECTED = " expected";
    private static final String BOOLEAN_LITERAL = "Boolean Literal";
    private static final String ADVANCED_SEARCH_UNEXPECTED_END_OF_EXPRESSION = "Advanced search: unexpected end of expression";
    private static final String ID2 = "id";
    private static final String CON2 = "con";
    private static final String PA = "pa";
    private static final String MAG = "mag";
    private static final String DEC = "dec";
    private static final String RA = "ra";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String ADVANCED_SEARCH_END_OF_EXPRESSION_EXPECTED = "Advanced search: end of expression expected";
    private static final String ID = "ID";
    private static final String CON = "CON";
    private static final String CUSTOM = "CUSTOM";
    private static final String COMET = "COMET";
    private static final String DS = "DS";
    private static final String STAR = "STAR";
    private static final String PLANET = "MPLANET";
    private static final String SNR = "SNR";
    private static final String PN = "PN";
    private static final String OCN = "OCN";
    private static final String OC = "OC";
    private static final String NEB = "NEB";
    private static final String HIIRGN = "HIIRGN";
    private static final String GXYCLD = "GXYCLD";
    private static final String GX = "GX";
    private static final String GC = "GC";
    private static final String TAG = Analisator.class.getSimpleName();
    private static Map<String, Integer> constMap = new HashMap<String, Integer>();
    private static Map<String, String> varMap = new HashMap<String, String>();//map where things like m=messier etc are stored

    static {
        constMap.put(GC, 1);
        constMap.put(GX, 2);
        constMap.put(GXYCLD, 3);
        constMap.put(HIIRGN, 4);
        constMap.put(NEB, 5);
        constMap.put(OC, 6);
        constMap.put(OCN, 7);
        constMap.put(PN, 8);
        constMap.put(SNR, 9);
        constMap.put(PLANET, 11);
        constMap.put(STAR, 12);
        constMap.put(DS, 13);
        constMap.put(COMET, 14);
        constMap.put(CUSTOM, 10);
        constMap.put(CG, AstroObject.CG);
        constMap.put(DN, AstroObject.DN);
        constMap.put(AST, AstroObject.AST);
        constMap.put(NF, AstroObject.NF);
        constMap.put(QS, AstroObject.QS);


        for (int i = 1; i < constellations.length; i++) {
            constMap.put(constellations[i].toUpperCase(Locale.US), i);
        }

        varMap.put(CON, CONSTEL);
        varMap.put(ID, _ID);

    }

    private static Integer isConst(String s) {
        return (constMap.get(s.toUpperCase(Locale.US)));
    }

    private static String isPredefinedVar(String s) {
        return (varMap.get(s.toUpperCase(Locale.US)));
    }

    private static double accuracy = 1e-5;
    String input = "";
    int pos = 0;
    char look;
    Instructions instructions = new Instructions();

    /**
     * setting string to compile
     */
    public void setInputString(String s) {
        input = s.replace(" ", "");
        input = input + "@";
    }

    /**
     * defining and/or setting variables in expression
     */
    public void addVar(String name, double value) {//also ok for setting new value
        instructions.addVar(name, value);
    }

    public void setVars(String name, double value) {
        instructions.setVar(name, value);
    }

    public void compile() throws UnsupportedOperationException {//ok if exception==null
        if ("@".equals(input)) return;//empty string

        init();
        BoolExpression();
        if (look != '@') abort(ADVANCED_SEARCH_END_OF_EXPRESSION_EXPECTED);


    }

    /**
     * calculating compiled expression
     *
     * @return
     */
    public boolean calculate() {
        if ("@".equals(input)) return true;//true for empty string
        double r = instructions.calculate();
        return (Math.abs(r) > accuracy);
    }

    /**
     * getting variables used in expression
     *
     * @return
     */
    public Set<String> getVarsUsedInExpression() {
        return instructions.getVarsUsedInExpression();
    }

    /**
     * Adding variables used for sql database request
     */
    public void dsoInitSQLrequest() {//adding primary request variables
        addVar(TYPE, 0);
        addVar("a", 0);
        addVar("b", 0);
        addVar(NAME, 0);
        addVar(RA, 0);
        addVar(DEC, 0);
        addVar(MAG, 0);
        addVar(PA, 0);
        addVar(CON2, 0);
        addVar(ID2, 0);
    }

    public void dsoInitLocalrequest() {
        addVar(ALT, 0);
        addVar(VIS, 0);
    }

    @Override
    public String toString() {
        return "Analisator [input=" + input + ", instructions=" + instructions + "]";
    }

    private interface StackMember {
        final static int OPERATION = 0;
        final static int OPERAND = 1;
        final static int NOT_OPERATION = 2;//separate treatment required as this operation takes one parameter

        int getType();
    }

    private interface Operation extends StackMember {
        double calculate(double x, double y);
    }


    private interface Operand extends StackMember {
        double get();
    }

    private class Add implements Operation {
        private static final String ADDITION = "addition";

        public double calculate(double x, double y) {
            return x + y;
        }

        public int getType() {
            return StackMember.OPERATION;
        }

        public String toString() {
            return ADDITION;
        }
    }

    private class Substract implements Operation, StackMember {
        private static final String SUBTRACTION = "subtraction";

        public double calculate(double x, double y) {
            return x - y;
        }

        public int getType() {
            return StackMember.OPERATION;
        }

        public String toString() {
            return SUBTRACTION;
        }
    }

    private class Multiply implements Operation, StackMember {
        private static final String MULTIPLICATION = "multiplication";

        public double calculate(double x, double y) {
            return x * y;
        }

        public int getType() {
            return StackMember.OPERATION;
        }

        public String toString() {
            return MULTIPLICATION;
        }
    }

    private class Divide implements Operation, StackMember {
        private static final String DIVISION = "division";

        public double calculate(double x, double y) {
            return x / y;
        }

        public int getType() {
            return StackMember.OPERATION;
        }

        public String toString() {
            return DIVISION;
        }
    }

    private class More implements Operation {//boolean
        private static final String MORE = "more";

        public double calculate(double x, double y) {
            return (x > y ? 1 : 0);
        }

        public int getType() {
            return StackMember.OPERATION;
        }

        public String toString() {
            return MORE;
        }
    }

    private class Less implements Operation {//boolean
        private static final String LESS = "less";

        public double calculate(double x, double y) {
            return (x < y ? 1 : 0);
        }

        public int getType() {
            return StackMember.OPERATION;
        }

        public String toString() {
            return LESS;
        }
    }

    private class Equal implements Operation {//boolean
        private static final String EQUAL = "equal";

        public double calculate(double x, double y) {
            return (Math.abs(x - y) < accuracy ? 1 : 0);
        }

        public int getType() {
            return StackMember.OPERATION;
        }

        public String toString() {
            return EQUAL;
        }
    }

    private class NotEqual implements Operation {//boolean
        private static final String NOT_EQUAL = "not equal";

        public double calculate(double x, double y) {
            return (Math.abs(x - y) > accuracy ? 1 : 0);
        }

        public int getType() {
            return StackMember.OPERATION;
        }

        public String toString() {
            return NOT_EQUAL;
        }
    }

    private class And implements Operation {//boolean
        private static final String AND = "and";

        public double calculate(double x, double y) {
            return (Math.abs(x) > accuracy && Math.abs(y) > accuracy ? 1 : 0);
        }

        public int getType() {
            return StackMember.OPERATION;
        }

        public String toString() {
            return AND;
        }
    }

    private class Or implements Operation {//boolean
        private static final String OR = "or";

        public double calculate(double x, double y) {
            return (Math.abs(x) > accuracy || Math.abs(y) > accuracy ? 1 : 0);
        }

        public int getType() {
            return StackMember.OPERATION;
        }

        public String toString() {
            return OR;
        }
    }

    private class Xor implements Operation {//boolean
        private static final String XOR = "xor";

        public double calculate(double x, double y) {
            int or = (Math.abs(x) > accuracy || Math.abs(y) > accuracy ? 1 : 0);
            if (Math.abs(x - 1) < accuracy && Math.abs(y - 1) < accuracy) or = 0;
            return or;
        }

        public int getType() {
            return StackMember.OPERATION;
        }

        public String toString() {
            return XOR;
        }
    }

    private class Not implements Operation {//boolean, y does not matter
        private static final String NOT = "not";

        public double calculate(double x, double y) {
            return (Math.abs(x) < accuracy ? 1 : 0);
        }

        public int getType() {
            return StackMember.NOT_OPERATION;
        }

        public String toString() {
            return NOT;
        }
    }

    private class Instructions {
        private Map<String, Double> varsMap = new HashMap<String, Double>();//var name,its value
        private Set<String> varsList = new HashSet<String>();//vars used in expression

        @Override
        public String toString() {
            return "Instructions [varsMap=" + varsMap + ", varsList=" + varsList + "]";
        }

        class Num implements Operand, StackMember {
            private double value;

            public Num(double value) {
                this.value = value;
            }

            public int getType() {
                return StackMember.OPERAND;
            }

            public double get() {
                return value;
            }

            public String toString() {
                return ("" + value);
            }
        }

        class Var implements Operand, StackMember {
            private String name;

            public Var(String name) {
                this.name = name;
            }

            public int getType() {
                return StackMember.OPERAND;
            }

            public double get() {
                return varsMap.get(name.toUpperCase());
            }

            public String toString() {
                return name;
            }
        }

        private List<StackMember> queue = new ArrayList<StackMember>();
        private LinkedList<StackMember> stack = new LinkedList<StackMember>();

        /**
         * @return set of vars in uppercase
         */
        public Set<String> getVarsUsedInExpression() {
            return varsList;
        }

        public void addVar(String name, double value) {
            varsMap.put(name.toUpperCase(), value);
        }

        public void setVar(String name, double value) {
            if (varsMap.containsKey(name.toUpperCase())) varsMap.put(name.toUpperCase(), value);
        }

        public void addOperandToStack(double num) {
            queue.add(new Num(num));
        }

        public void addOperandToStack(String name) throws UnsupportedOperationException {
            Integer i = isConst(name);
            if (i != null) {
                queue.add(new Num(i.doubleValue()));
                return;
            }
            if (varsMap.containsKey(name.toUpperCase())) {
                queue.add(new Var(name));
                varsList.add(name.toUpperCase());
            } else throw new UnsupportedOperationException("Advanced search: variable not defined");
        }

        public void addOperationToStack(StackMember op) {
            queue.add(op);
        }

        public double calculate() {
            for (StackMember m : queue) {
                if (m.getType() == StackMember.OPERAND) stack.add(m);
                else if (m.getType() == StackMember.OPERATION) {
                    Operand op2 = (Operand) stack.getLast();
                    stack.removeLast();
                    Operand op1 = (Operand) stack.getLast();
                    stack.removeLast();
                    double num = ((Operation) m).calculate(op1.get(), op2.get());
                    stack.add(new Num(num));
                } else {

                    Operand op1 = (Operand) stack.getLast();
                    stack.removeLast();
                    double num = ((Operation) m).calculate(op1.get(), op1.get());
                    stack.add(new Num(num));
                }
            }

            double res = ((Operand) stack.removeLast()).get();
            if (stack.size() > 0) {
                Log.d(TAG, "stack size on exit=" + stack.size());
                stack = new LinkedList<StackMember>();
            }
            return res;
        }
    }

    public static final char TAB = (char) 9;
    public static final char CR = (char) 13;


    // Lookahead Character
    // Read New Character From Input Stream
    private void getChar() throws UnsupportedOperationException {
        if (pos < input.length()) {
            look = input.charAt(pos);
            pos++;
        } else
            throw new UnsupportedOperationException(ADVANCED_SEARCH_UNEXPECTED_END_OF_EXPRESSION);
    }

    private String recStr = "";

    /**
     * Remakes the string into the form suitable for SQL request
     */
    private void addToRecStr(String s) {
        Integer i = isConst(s);
        if (i == null) {
            String s1 = isPredefinedVar(s);
            if (s1 != null) s = s1;
            recStr = recStr + s;
        } else recStr = recStr + i;
        Log.d(TAG, "recStr=" + recStr);
    }

    public String getRecStr() {
        return recStr;
    }

    private void error(String s) {
    }

    //{ Report Error and Halt }
    private void abort(String s) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(s);
    }

    //{ Recognize a Boolean Literal }
    private boolean IsBoolean(char c) {
        return false;
    }

    //{ Get a Boolean Literal }
    private boolean GetBoolean() {

        if (!IsBoolean(look)) expected(BOOLEAN_LITERAL);
        boolean flag = (Character.toUpperCase(look) == 'T');
        getChar();
        return flag;
    }

    //{ Parse and Translate a Boolean Expression }
    private void BoolFactor() {
        if (look == '(') {
            match('(');

            BoolExpression();
            match(')');

        } else {
            if (IsBoolean(look)) if (GetBoolean()) {
                emitLn("MOVE # 1,D0");
                instructions.addOperandToStack(1);
            } else {
                emitLn("CLR D0");
                instructions.addOperandToStack(0);
            }
            else Relation();
        }
    }

    //{ Recognize a Relop }
    private boolean IsRelop(char c) {

        return (c == '=' || c == '#' || c == '<' || c == '>');
    }

    //{ Recognize and Translate a Relational Equals }
    private void Equals() {

        match('=');

        expression();
        emitLn("CMP (SP)+,D0");
        emitLn("SEQ D0");
        emitLn("==");
        instructions.addOperationToStack(new Equal());
    }

    //{ Recognize and Translate a Relational Not Equals }
    private void NotEquals() {
        match('#');

        expression();
        emitLn("CMP (SP)+,D0");
        emitLn("SNE D0");
        emitLn("!=");
        instructions.addOperationToStack(new NotEqual());
    }

    //{ Recognize and Translate a Relational Less Than }
    private void Less() {

        match('<');

        expression();
        emitLn("CMP (SP)+,D0");
        emitLn("SGE D0");
        emitLn("<");
        instructions.addOperationToStack(new Less());
    }

    //{ Recognize and Translate a Relational Greater Than }
    private void Greater() {

        match('>');
        expression();
        emitLn("CMP (SP)+,D0");
        emitLn("SLE D0");
        emitLn(">");
        instructions.addOperationToStack(new More());
    }

    //{ Parse and Translate a Relation }
    private void Relation() {

        expression();
        if (IsRelop(look)) {
            emitLn("MOVE D0, (SP)");
            switch (look) {
                case '=':
                    Equals();
                    break;
                case '#':
                    NotEquals();
                    break;
                case '<':
                    Less();
                    break;
                case '>':
                    Greater();
                    break;
            }
            emitLn("TST D0");
        }
    }


    //{ Parse and Translate a Boolean Factor with NOT }
    private void NotFactor() {

        if (look == '!') {
            match('!');
            BoolFactor();
            emitLn("EOR # 1,D0");
            emitLn("not");
            instructions.addOperationToStack(new Not());
        } else BoolFactor();

    }

    //{ Parse and Translate a Boolean Term }
    private void BoolTerm() {

        NotFactor();
        while (look == '&') {
            emitLn("MOVE D0, (SP)");
            match('&');
            NotFactor();
            emitLn("AND (SP)+,D0");
            emitLn("&");
            instructions.addOperationToStack(new And());
        }
    }

    //{ Recognize and Translate a Boolean OR }
    private void BoolOr() {

        match('|');
        BoolTerm();
        emitLn("OR (SP)+,D0");
        emitLn("or");
        instructions.addOperationToStack(new Or());
    }

    //{ Recognize and Translate an Exclusive Or }
    private void BoolXor() {

        match('~');
        BoolTerm();
        emitLn("EOR (SP)+,D0");
        emitLn("Xor");
        instructions.addOperationToStack(new Xor());
    }

    //{ Parse and Translate a Boolean Expression }
    private void BoolExpression() {
        BoolTerm();
        while (IsOrOp(look)) {
            emitLn("MOVE D0, (SP)");
            switch (look) {
                case '|':
                    BoolOr();
                    break;
                case '~':
                    BoolXor();
                    break;
            }
        }
    }

    //{ Recognize a Boolean Orop }
    private boolean IsOrOp(char c) {

        return (c == '|' || c == '~');
    }


    //{ Report What Was Expected }
    private void expected(String s) {
        abort(s + EXPECTED);
    }

    //{ Recognize an Alpha Character }
    private boolean isAlpha(char c) {
        int num = (int) Character.toUpperCase(c);
        return ((num - 65 >= 0) && (num <= 90));
    }

    //{ Recognize a Decimal Digit }
    private boolean isDigit(char c) {
        int num = (int) Character.toUpperCase(c);
        return ((num >= 48) && (num <= 57) || (c == '.'));
    }

    //{ Recognize an Alphanumeric }
    private boolean isAlNum(char c) {

        return (isAlpha(c) || isDigit(c));
    }

    //{ Recognize an Addop }
    private boolean isAddop(char c) {
        return (c == '+' || c == '-');
    }


    //{ Recognize White Space }
    private boolean isWhite(char c) {
        return (c == ' ' || c == TAB);
    }

    //{ Skip Over Leading White Space }
    private void skipWhite() {

        while (isWhite(look)) {
            getChar();
        }
    }

    //{ Match a Specific Input Character }
    private void match(char x) {

        if (look != x) expected(" " + x + " ");
        else {
            getChar();
            skipWhite();
            switch (x) {
                case '&':
                    addToRecStr(AND);
                    break;
                case '|':
                    addToRecStr(OR);
                    break;
                case '~':
                    addToRecStr(XOR);
                    break;
                case '!':
                    addToRecStr(NOT);
                    break;
                case '#':
                    addToRecStr(S);
                    break;
                default:
                    addToRecStr(Character.toString(x));
            }

        }
    }

    //{ Get an Identifier }
    private String getName() {
        String Token = "";


        if (!isAlpha(look)) expected(NAME2);
        while (isAlNum(look)) {
            Token = Token + look;
            getChar();
        }

        skipWhite();
        return Token;
    }

    //{ Get a Number }
    private String getNum() {
        String Value = "";

        if (!isDigit(look)) expected(INTEGER);
        while (isDigit(look)) {
            Value = Value + look;
            getChar();

        }

        skipWhite();
        return Value;
    }

    //{ Output a String with Tab }
    private void emit(String s) {

    }

    //	{ Output a String with Tab and CRLF }
    private void emitLn(String s) {
    }

    //{ Parse and Translate a Identifier }
    private void ident() {
        String Name;

        Name = getName();
        addToRecStr(Name);
        if (look == '(') {
            match('(');
            match(')');
            emitLn("BSR " + Name);
        } else {
            emitLn("MOVE " + Name + "(PC),D0");
            instructions.addOperandToStack(Name);
        }
    }


    //{ Parse and Translate the First Math Factor }
    private void SignedFactor() {

        if (look == '+') {
            getChar();
            addToRecStr(Character.toString('+'));
        }
        if (look == '-') {
            getChar();
            addToRecStr(Character.toString('-'));
            if (isDigit(look)) {
                String numStr = getNum();
                addToRecStr(numStr);
                double num = 0;
                try {
                    num = Double.parseDouble(numStr);
                } catch (Exception e) {
                    throw new UnsupportedOperationException(ADVANCED_SEARCH_NUMBER_CONVERSION_ERROR_OF + numStr);
                }
                emitLn("MOVE # " + numStr + ",D0");

                instructions.addOperandToStack(-num);
            } else {
                factor();
                emitLn("NEG D0");
                instructions.addOperandToStack(-1);
                instructions.addOperationToStack(new Multiply());
            }
        } else factor();
    }

    //{ Parse and Translate a Math Factor }
    private void factor() {

        if (look == '(') {
            match('(');
            expression();
            match(')');
        } else if (isAlpha(look)) ident();
        else {
            String numStr = getNum();
            addToRecStr(numStr);
            double num;
            try {
                num = Double.parseDouble(numStr);
            } catch (Exception e) {
                throw new UnsupportedOperationException(ADVANCED_SEARCH_NUMBER_CONVERSION_ERROR_OF + numStr);
            }
            emitLn("MOVE #" + numStr + ",D0");
            instructions.addOperandToStack(num);
        }
    }

    //{ Recognize and Translate a Multiply }
    private void multiply() {

        match('*');
        factor();
        emitLn("MULS (SP)+,D0");
        instructions.addOperationToStack(new Multiply());
    }

    //{ Recognize and Translate a Divide }
    private void divide() {

        match('/');
        factor();
        emitLn("MOVE (SP)+,D1");
        emitLn("EXS.L D0");
        emitLn("DIVS D1,D0");
        instructions.addOperationToStack(new Divide());
    }

    //{ Parse and Translate a Math Term }
    private void term() {

        SignedFactor();
        while (look == '*' || look == '/') {
            emitLn("MOVE D0, (SP)");

            switch (look) {
                case '*':
                    multiply();
                    break;
                case '/':
                    divide();
                    break;
            }
        }
    }

    //{ Recognize and Translate an Add }
    private void add() {

        match('+');
        term();
        emitLn("ADD (SP)+,D0");
        instructions.addOperationToStack(new Add());
    }

    //{ Recognize and Translate a Subtract }
    private void subtract() {

        match('-');
        term();
        emitLn("SUB (SP)+,D0");
        emitLn("NEG D0");
        instructions.addOperationToStack(new Substract());
    }

    //{ Parse and Translate an Expression }
    private void expression() {
        term();
        while (isAddop(look)) {
            emitLn("MOVE D0, -(SP)");
            switch (look) {
                case '+':
                    add();
                    break;
                case '-':
                    subtract();
            }
        }
    }

    //	{ Parse and Translate an Assignment Statement }
    private void assignment() {
        String Name;

        Name = getName();
        match('=');
        BoolExpression();
    }

    //{ Initialize }
    private void init() {

        getChar();
        skipWhite();
    }
}
