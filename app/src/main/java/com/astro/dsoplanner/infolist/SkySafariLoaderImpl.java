package com.astro.dsoplanner.infolist;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.ErrorHandler;


public class SkySafariLoaderImpl implements InfoListLoader {

    static final String OPEN = "SkyObject=BeginObject";
    static final String CLOSE = "EndObject=SkyObject";

    BufferedReader reader;
    Context context;
    int linenum;

    public SkySafariLoaderImpl(BufferedReader reader, Context context) {
        this.reader = reader;
        this.context = context;
    }

    public void open() {
    }

    public String getName() throws IOException {
        return "";
    }

    public void close() throws IOException {
        reader.close();
    }

    public Object next(ErrorHandler.ErrorRec rec) throws IOException {
        String s;
        while ((s = reader.readLine()) != null) {
            linenum++;
            process(s);
            if (state == REPORT_OBJECT) {
                ErrorHandler eh = new ErrorHandler();
                AstroObject obj = AstroTools.getObject(map, context, eh, true);

                if (obj == null) {
                    if (eh.hasError()) {
                        ErrorHandler.ErrorRec er = eh.getErrors().get(0);
                        rec.line = line;
                        rec.type = er.type;
                        rec.lineNum = linenum;
                    } else {
                        rec.line = line;
                        rec.type = ErrorHandler.DATA_CORRUPTED;
                        rec.lineNum = linenum;
                    }
                }
                state = NONOBJECT;
                line = "";
                map = new HashMap<String, String>();

                if (obj != null) {
                    return new ObsInfoListImpl.Item(obj, false);
                } else
                    return null;

            }
        }
        throw new EOFException();
    }

    int state = 0;
    static final int NONOBJECT = 0;
    static final int PROCESS_OBJECT = 1;
    static final int REPORT_OBJECT = 2;


    String line = "";
    Map<String, String> map = new HashMap<String, String>();


    private void processObjectString(String s) {
        String[] arr = s.split("=");
        if (arr.length == 2) {
            String[] arr2 = arr[1].split(" ");
            if (arr2.length == 2) {
                map.put(arr2[0], arr2[1]);
            }
        }
    }

    public void process(String s) {
        switch (state) {
            case NONOBJECT:
                if (s.equals(OPEN)) {
                    state = PROCESS_OBJECT;
                }
                return;
            case PROCESS_OBJECT:
                if (s.equals(CLOSE)) {
                    state = REPORT_OBJECT;
                } else {
                    line += s + "\n";
                    processObjectString(s);
                }
                return;
        }


    }

}
