package com.astro.dsoplanner.infolist;

import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.ObjectInflater;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//String Loaders and Savers do not load/save list names
public class InfoListStringLoaderImp implements InfoListLoader {
    private static final int STRING = 2;
    private static final int FILE = 1;
    private static final String TAG = InfoListStringLoaderImp.class.getSimpleName();

    interface Flow {
        public boolean hasNext() throws IOException;

        public char next();
    }

    class BufferedReaderFlow implements Flow {
        BufferedReader reader;
        String line;
        int count;
        boolean eof = false;
        boolean newline = true;

        public BufferedReaderFlow(BufferedReader reader) {
            super();
            this.reader = reader;
        }

        public boolean hasNext() throws IOException {
            if (eof)
                return false;


            if (newline) {
                line = reader.readLine();
                count = 0;

                if (line == null) {
                    eof = true;
                    return false;
                } else {
                    line = line + "\n";
                    newline = false;
                }
            }


            return true;
        }

        public char next() {
            if (count < line.length() - 1) {
                return line.charAt(count++);
            } else {
                newline = true;
                return line.charAt(count);
            }
        }
    }

    class StringFlow implements Flow {
        String line;
        int count = 0;

        public StringFlow(String line) {
            super();
            this.line = line;
        }

        public boolean hasNext() {
            return count < line.length();
        }

        public char next() {
            return line.charAt(count++);
        }

    }

    class Answer {
        Map<String, String> map;
        String line;
        boolean eof;

        public Answer(Map<String, String> map, String line, boolean eof) {
            super();
            this.map = map;
            this.line = line;
            this.eof = eof;
        }

        @Override
        public String toString() {
            return "Answer [map=" + map + ", line=" + line + ", eof=" + eof
                    + "]";
        }

    }

    class Automat {
        private static final int END = 4;
        private static final int READING_VALUE = 3;
        private static final int READING_KEY = 2;
        private static final int START = 1;
        Flow f;
        int state;
        List<Character> list = new ArrayList<Character>();
        Map<String, String> map = new HashMap<String, String>();

        public void push(char c) {
            list.add(c);
        }

        public Character get() {
            if (list.size() == 0)
                return null;
            return list.remove(list.size() - 1);
        }


        public Automat(Flow f) {
            super();
            this.f = f;
        }

        String line = "";

        public Answer next() throws IOException {
            state = START;
            map = new HashMap<String, String>();
            key = "";
            value = "";
            line = "";
            while (f.hasNext()) {
                if (state != END) {
                    char c = f.next();
                    line = line + c;
                    push(c);
                }
                switch (state) {
                    case START:
                        stateStart();
                        break;
                    case READING_KEY:
                        stateReadingKey();
                        break;
                    case READING_VALUE:
                        stateReadingValue();
                        break;
                    case END:
                        return new Answer(map, line, false);
                }
            }
            return new Answer(map, line, true);
        }

        private void stateStart() {
            char c = get();
            if (c != '&') {
                push(c);
                state = READING_KEY;
                stateReadingKey();
            } else {
                state = READING_KEY;
            }
        }

        String key = "";

        private void stateReadingKey() {
            char c = get();

            if (c == '=') {
                state = READING_VALUE;
            } else if (c == '&') {
                state = END;
            } else if (c == '\n') {
                return;
            } else {
                key = key + c;
            }
        }

        String value = "";

        private void stateReadingValue() {
            char c = get();
            int sub = value.length() - 2;
            if (sub < 0) sub = 0;
            String lasttwo = value.substring(sub);
            String cmp = "\\\\";
            if (c == ';' && !(cmp.equals(lasttwo))) {

                value = value.replace("\\\\;", ";");
                map.put(key.trim(), AstroTools.trim(value)); //fix for EyepiecesList keeping eyepiece record data in notes (to avoid wrong import)
                state = READING_KEY;
                key = "";
                value = "";
            } else
                value = value + c;
        }
    }


    class StringParser {
        private String stringLine;

        public StringParser(String s) {
            this.stringLine = s;
        }

        public HashMap<String, String> getHashMapRepresentation() {
            HashMap<String, String> map = new HashMap<String, String>();
            String[] list = stringLine.split(Global.delimiter_char);
            for (String s : list) {
                s = s.replace("&", "");//need to remove for the file, not for the paste
                int i = s.indexOf(Global.assign_char);
                if (i == -1)
                    continue;
                String s1 = s.substring(0, i);
                String s2 = s.substring(i + 1);
                map.put(s1, s2);
            }
            return map;
        }
    }

    class FileReader {
        BufferedReader in;
        List<String> list = new ArrayList<String>();//ready strings

        public FileReader(BufferedReader in) {
            this.in = in;
        }

        boolean eof = false;
        String last = "";

        public String next() throws IOException {

            while (list.size() == 0 && !eof) {
                String s = in.readLine();
                if (s == null)
                    eof = true;
                else
                    fillList(s);
            }
            if (eof) {
                if (!"".equals(last)) {
                    list.add(last);
                    last = "";
                }
            }
            if (list.size() > 0) {
                String s = list.remove(0);
                return s;
            } else
                return null;

        }

        public void close() throws IOException {
            in.close();
        }

        private void fillList(String s) {
            if (s.length() == 0) {
                last = last + "\n";
                return;
            }
            boolean ended = (s.charAt(s.length() - 1) == '&');//ended with &
            String[] arr = s.split("&");
            if (arr.length == 0)
                return;
            if (!"".equals(arr[0]))
                last = last + "\n" + arr[0];
            if (arr.length == 1) {
                if (ended)
                    if (!"".equals(last)) {
                        list.add(last);
                        last = "";
                    }
                return;
            }
            if (arr.length > 1) {
                if (!"".equals(last)) {
                    list.add(last);
                    last = "";
                }

            }
            //in the middle
            for (int i = 1; i < arr.length - 1; i++) {
                if (!"".equals(arr[i]))
                    list.add(arr[i]);
            }
            //last piece
            if (ended) {
                String tmp = arr[arr.length - 1];
                if (!"".equals(tmp)) {
                    list.add(tmp);
                    last = "";
                }
            } else {
                last = arr[arr.length - 1];
            }
        }
    }

    private DbListItem.FieldTypes ftypes = null;
    private boolean ignoreCustomDbRefs = false;
    private boolean ignoreNgcicRefs = false;
    BufferedReader bin;
    String stringline;
    int datatype;
    Automat automat;

    public InfoListStringLoaderImp(InputStream in) {
        bin = new BufferedReader(new InputStreamReader(in));
        datatype = FILE;
        automat = new Automat(new BufferedReaderFlow(bin));
    }

    /**
     * this constructor is used when there should be restrictions imposed
     * on object inflater
     */
    public InfoListStringLoaderImp(InputStream in, DbListItem.FieldTypes ftypes) {
        this(in);
        this.ftypes = ftypes;
    }

    private String[] array;
    int i = 0;
    private boolean paste = false;

    public InfoListStringLoaderImp(String s) {

        stringline = s;
        automat = new Automat(new StringFlow(s));
        datatype = STRING;

    }

    /**
     * this constructor is used when there should be restrictions imposed
     * on object inflater
     *
     * @param s
     * @param ftypes
     */
    public InfoListStringLoaderImp(String s, DbListItem.FieldTypes ftypes) {
        this(s);
        this.ftypes = ftypes;
    }

    public void open() {
    }

    public String getName() throws IOException {
        return "";
    }

    private int row = 1;
    int count = 0;

    public Object next(ErrorHandler.ErrorRec erec) throws IOException {

        Answer answer = automat.next();
        if (answer.map.isEmpty() && answer.eof)
            throw new EOFException();

        Object obj = null;

        try {
            if (ftypes == null)//there are no restrictions imposed
                obj = ObjectInflater.getInflater().inflate(answer.map, erec, ignoreCustomDbRefs, ignoreNgcicRefs);
            else//there are restrictions imposed
                obj = ObjectInflater.getInflater().inflate(answer.map, erec, ftypes, ignoreCustomDbRefs, ignoreNgcicRefs);


        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            Log.d(TAG, "exception=" + e);
            e.printStackTrace(ps);
            Log.d(TAG, "stack trace=" + baos.toString());
            erec.line = answer.line;
            erec.lineNum = row;
            erec.type = ErrorHandler.DATA_CORRUPTED;
            return null;
        }
        if (obj == null) {
            erec.line = answer.line;
            erec.lineNum = row;
        }
        row++;
        return obj;

    }

    public void close() throws IOException {
        if (bin != null)
            bin.close();
    }

    /**
     * use this to indicate that data comes from external sources and therefore
     * references to user databases and user note database should not be used.
     * flag is not set by default
     */
    public void setIgnoreCustomDbRefsFlag() {
        ignoreCustomDbRefs = true;
    }

    /**
     * use this to ignore refs to ngcic db when inflating objects (e.g. when filling a database)
     * flag is not set by default
     */

    public void setIgnoreNgcicRefsFlag() {
        ignoreNgcicRefs = true;
    }
}
