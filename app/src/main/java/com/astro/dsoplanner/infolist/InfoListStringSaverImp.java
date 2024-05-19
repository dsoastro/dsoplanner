package com.astro.dsoplanner.infolist;

import android.os.Handler;
import android.util.Log;

import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.base.Exportable;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.InputDialog;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

//String Loaders and Savers do not load/save list names
public class InfoListStringSaverImp implements InfoListSaver { //string saver for any type of lists
    private static final int SIZE_LIMIT = 65000;

    private static final String TOO_MANY_ROWS_TRUNCATED = "Too many rows. Truncated";

    private static final String TAG = "ILS";
    private PrintStream out;
    private int maxRows = -1;//if no limit set
    private Handler handler;
    private ErrorHandler eh;

    public InfoListStringSaverImp(OutputStream out) {
        this.out = new PrintStream(out);
    }

    /**
     * Use this to limit the number of rows to be saved
     *
     * @param out
     * @param maxRows - limit on rows to be saved, if reached no further saving is performed
     * @param handler - use this to post the message if the maximum number of rows reached in the UI thread. If null no message posted
     */
    public InfoListStringSaverImp(OutputStream out, int maxRows, Handler handler) {
        this(out);
        this.maxRows = maxRows;
        this.handler = handler;
    }

    /**
     * Use this to limit the number of rows to be saved
     *
     * @param out
     * @param maxRows - limit on rows to be saved, if reached no further saving is performed
     * @param eh      - use this to post the message if the maximum number of rows reached in the UI thread. If null no message posted
     */
    public InfoListStringSaverImp(OutputStream out, int maxRows, ErrorHandler eh) {
        this(out);
        this.maxRows = maxRows;
        this.eh = eh;
    }

    public void open() {
    }

    public void addName(String name) throws IOException {

    }

    private int row = 0;
    private boolean messageShown = false;
    int size = 0;
    int i = 1;

    public void addObject(Exportable obj) throws IOException {
        Map<String, String> map0 = obj.getStringRepresentation();
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, String> e : map0.entrySet()) {
            String value = e.getValue();
            value = value.replace(";", "\\\\;");
            map.put(e.getKey(), value);
            size = size + e.getKey().length() + value.length();
        }
        if (maxRows > -1 && size > SIZE_LIMIT) {
            if (!messageShown) {
                messageShown = true;
                if (handler != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            InputDialog.toast(TOO_MANY_ROWS_TRUNCATED, Global.getAppContext()).show();
                        }
                    });
                }
                if (eh != null) {
                    ErrorHandler.ErrorRec rec = new ErrorHandler.ErrorRec(ErrorHandler.WARNING, TOO_MANY_ROWS_TRUNCATED);
                    eh.addError(rec);
                }

            }
            return;
        }


        out.print("&");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            out.print(entry.getKey() + Global.assign_char + entry.getValue() + Global.delimiter_char);
        }
        out.println();
        Log.d(TAG, "i=" + i);
        i++;

    }

    public void close() throws IOException {
        out.close();
        Log.d(TAG, "close");
    }
}
