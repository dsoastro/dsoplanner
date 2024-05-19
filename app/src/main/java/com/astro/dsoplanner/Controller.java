package com.astro.dsoplanner;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;


abstract class Controller {

    @SuppressWarnings("unused")
    private static final String TAG = Controller.class.getSimpleName();
    private final List<Handler> outboxHandlers = new ArrayList<Handler>();

    protected boolean disposed = false;

    public Controller() {

    }

    public void dispose() {
    }

    abstract public boolean handleMessage(int what, Object data);

    public boolean handleMessage(int what) {
        return handleMessage(what, null);
    }

    public final void addOutboxHandler(Handler handler) {
        outboxHandlers.add(handler);
    }

    public final void removeOutboxHandler(Handler handler) {
        outboxHandlers.remove(handler);
    }

    protected final void notifyOutboxHandlers(int what, int arg1, int arg2, Object obj) {
        if (disposed) return;
        if (!outboxHandlers.isEmpty()) {
            for (Handler handler : outboxHandlers) {
                Message msg = Message.obtain(handler, what, arg1, arg2, obj);
                if (msg != null) {
                    msg.sendToTarget();
                }
            }
        }
    }

    /**
     * Use this for passing to UI thread which would set the context
     *
     * @author leonid
     */
    abstract static class Executable implements Runnable {
        protected Context context;

        public void setContext(Context context) {
            this.context = context;
        }
    }
}

