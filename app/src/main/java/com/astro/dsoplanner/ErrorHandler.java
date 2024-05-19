package com.astro.dsoplanner;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.text.ClipboardManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.EditText;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private static final String UNRECOGNISED_OBJECT2 = "Unrecognised object";
    private static final String WARNING2 = "Warning";
    private static final String ERROR_WORKING_WITH_SQL_DATABASE = "Error working with SQL database";
    private static final String WRONG_OBJECT_TYPE = "Wrong object type";
    private static final String IO_ERROR2 = "IO Error";
    private static final String DATA_CORRUPTED2 = "Data corrupted";

    public static final int DATA_CORRUPTED = 1;
    public static final int IO_ERROR = 2;
    public static final int WRONG_TYPE = 3;
    public static final int SQL_DB = 4;
    public static final int WARNING = 5;
    public static final int UNRECOGNISED_OBJECT = 6;

    private static final String TAG = ErrorHandler.class.getSimpleName();

    public static String getErrorName(int type) {
        switch (type) {
            case DATA_CORRUPTED:
                return DATA_CORRUPTED2;
            case IO_ERROR:
                return IO_ERROR2;
            case WRONG_TYPE:
                return WRONG_OBJECT_TYPE;
            case SQL_DB:
                return ERROR_WORKING_WITH_SQL_DATABASE;
            case WARNING:
                return WARNING2;
            case UNRECOGNISED_OBJECT:
                return UNRECOGNISED_OBJECT2;
        }
        return "";
    }

    public static class ErrorRec {
        private static final String MESSAGE2 = " message=";
        private static final String TYPE2 = "type=";
        public int type = 0;//no error by default
        public String message = "";
        public int lineNum = 0;//wrong line number
        public String line;//wrong line

        public ErrorRec() {
        }

        public ErrorRec(int type, String message) {
            this.type = type;
            this.message = message;
        }

        public String toString() {
            return TYPE2 + type + MESSAGE2 + message + " line=" + line;
        }

        public ErrorRec(int type, String message, int lineNum) {
            this(type, message);
            this.lineNum = lineNum;
        }

        public ErrorRec(int type, String message, int lineNumber, String line) {
            this(type, message, lineNumber);
            this.line = line;
        }


    }

    private List<ErrorRec> list = new ArrayList<ErrorRec>();
    int lineNum;//current line number
    String line = "";//current String analised

    public ErrorHandler() {

    }

    public ErrorHandler(int type, String message) {
        this();
        addError(type, message);
    }

    public void addError(ErrorRec rec) {
        list.add(rec);
    }

    public void addError(int type, String message) {
        list.add(new ErrorRec(type, message));
    }

    public void addError(int type, String message, String line, int lineNum) {
        ErrorRec err = new ErrorRec(type, message, lineNum, line);
        list.add(err);
    }

    public void addError(ErrorHandler eh) {
        List<ErrorRec> list1 = eh.getErrors();
        for (ErrorRec rec : list1) {
            list.add(rec);
        }
    }

    public boolean hasError() {
        return (list.size() != 0);
    }

    public List<ErrorRec> getErrors() {
        return list;
    }

    public String getErrorString() {
        StringBuilder s = new StringBuilder("");
        int i = 0, l = 0;
        final int MAX_LINES = 10;
        final int MAX_CHARS = 30;
        s.append(list.size()).append(" errors found:\n");
        for (ErrorRec e : list) {

            s.append("").append(++i).append(". ").append(getErrorName(e.type));

            if (!"".equals(e.message)) s.append(": ").append(e.message);

            if (e.lineNum != 0) s.append("\nL").append(e.lineNum);

            if (e.line != null) {
                l = e.line.length();
                if (l > 0) {
                    s.append(": \"");
                    s.append(e.line.substring(0, MAX_CHARS < l ? MAX_CHARS : l));
                    s.append("\"");
                }
            }

            s.append("\n\n");

            if (i > MAX_LINES) {
                s.append("...");
                break;
            }
        }
        return s.toString();
    }

    private Dialog getDialog(final Context context, String s) {
        final InputDialog dl = new InputDialog(context);
        dl.setTitle(context.getString(R.string.error));
        dl.setNegativeButton(context.getString(R.string.ok));
        dl.setPositiveButton(context.getString(R.string.copy_to_clipboard), new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                // Copy error text to the clipboard
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(((EditText) dl.findViewById(R.id.err_dialog_tv)).getText().toString());
            }
        });

        dl.insertLayout(R.layout.error_dialog);

        EditText v = (EditText) dl.findViewById(R.id.err_dialog_tv);
        v.setText(s);
        v.setScroller(new Scroller(context));
        v.setMaxLines(10);
        v.setVerticalScrollBarEnabled(true);
        v.setMovementMethod(new ScrollingMovementMethod());
        v.setKeyListener(null);
        v.setFocusable(true);
        return dl;
    }

    /**
     * use this for UI thread
     */
    public void showError(Activity context) {
        getDialog(context, getErrorString()).show();
    }

    /**
     * Use this for showing errors when showing activity is expected to be closed.
     * To be run from UI thread!
     *
     * @param context
     */
    public void showErrorInToast(Context context) {
        //	Global.context=context;
        InputDialog.toast(getErrorString(), context).show();

    }

    /**
     * use this for non UI thread
     *
     * @param handler - should be declared in UI thread
     */
    public void showError(final Context context, Handler handler) {

        class ShowMessage implements Runnable {
            String s;

            public ShowMessage(String s) {
                this.s = s;
            }

            public void run() {
                Log.d(TAG, "error=" + s);
                InputDialog.toast(s, context).show();
            }
        }
        handler.post(new ShowMessage(getErrorString()));
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (ErrorRec e : list) {
            s.append(e).append("\n");

        }
        return s.toString();
    }
}
