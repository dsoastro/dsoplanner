package com.astro.dsoplanner;

import java.io.FileOutputStream;

import android.app.Activity;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListStringSaverImp;
import com.astro.dsoplanner.util.Holder2;
import com.astro.dsoplanner.util.Holder3;

public class ViewDatabaseController extends Controller {

    private static final String NO_COINCIDENCE_FOUND = "No coincidence found!";
    private static final String WORKER_THREAD = "Worker Thread";


    private static final String TAG = ViewDatabaseController.class.getSimpleName();
    private HandlerThread workerThread;
    private Handler workerHandler;

    public static final int MESSAGE_UPDATE = 1;
    public static final int MESSAGE_ERROR_HANDLER = 2;
    public static final int MESSAGE_UPDATE_VIEW = 3;
    public static final int MESSAGE_FIND = 4;
    public static final int MESSAGE_FIND_NEXT = 5;
    public static final int MESSAGE_SET_LIST_LOCATION = 6;
    public static final int MESSAGE_TEXT = 7;
    public static final int MESSAGE_INPROGRESS = 8;
    public static final int MESSAGE_REMOVE_INPROGRESS = 9;
    public static final int MESSAGE_RESET_FIND = 10;
    public static final int MESSAGE_EXPORT = 11;
    public static final int MESSAGE_REMOVE_ALL = 12;
    public static final int MESSAGE_TEXT_FIND = 13;

    private FindRunner findRunner;
    Activity activity;

    public ViewDatabaseController(Activity activity) {
        this.activity = activity;
        workerThread = new HandlerThread(WORKER_THREAD);
        workerThread.start();
        workerHandler = new Handler(workerThread.getLooper());
        findRunner = new FindRunner();
        findRunner.setMatcher(new FindRunner.Matcher() {
            @Override
            public boolean match(Object o, String searchString) {
                if (o instanceof AstroObject) {
                    AstroObject obj = (AstroObject) o;

                    if (!"".equals(searchString) && (obj.getDsoSelName().toUpperCase().contains(searchString.toUpperCase())
                    )) {
                        return true;
                    }
                }
                return false;
            }
        });
    }


    @Override
    public void dispose() {
        super.dispose();
        workerThread.getLooper().quit();
        disposed = true;
        Log.d(TAG, "controller disposed");
    }

    @Override
    public synchronized boolean handleMessage(int what, final Object data) {
        switch (what) {
            case MESSAGE_UPDATE:

                workerHandler.post(new Runnable() {
                    public void run() {
                        update((AstroCatalog) data);
                    }
                });
                return true;
            case MESSAGE_FIND:
                Holder3<String, Cursor, AstroCatalog> h = (Holder3<String, Cursor, AstroCatalog>) data;
                if (h.y == null || h.z == null) {
                    notifyOutboxHandlers(MESSAGE_TEXT, 0, 0, NO_COINCIDENCE_FOUND);
                    return false;
                }
                findRunner.setSearch(new FindRunner.CursorListAdapter(h.y, h.z), h.x);
                makeFind();
                return true;

            case MESSAGE_FIND_NEXT:
                makeFind();
                return true;
            case MESSAGE_RESET_FIND:
                findRunner.reset();
                return true;
            case MESSAGE_EXPORT:
                workerHandler.post(new Runnable() {
                    public void run() {
                        Holder2<AstroCatalog, String> h = (Holder2<AstroCatalog, String>) data;
                        export(h.y, h.x);
                    }
                });
                return true;
            case MESSAGE_REMOVE_ALL:

                workerHandler.post(new Runnable() {
                    public void run() {
                        removeAll((AstroCatalog) data);
                    }
                });
                return true;
        }
        return false;
    }

    private void makeFind() {
        int pos = findRunner.find();
        if (pos > -1)
            notifyOutboxHandlers(MESSAGE_SET_LIST_LOCATION, pos, 0, null);
        else
            notifyOutboxHandlers(MESSAGE_TEXT_FIND, 0, 0, activity.getString(R.string.no_match_found_));

    }

    private void update(AstroCatalog catalog) {
        Log.d(TAG, "update");
        notifyOutboxHandlers(MESSAGE_INPROGRESS, 0, 0, null);
        ErrorHandler eh = new ErrorHandler();
        catalog.open(eh);
        Log.d(TAG, "open eh=" + eh);
        if (eh.hasError()) {
            notifyOutboxHandlers(MESSAGE_REMOVE_INPROGRESS, 0, 0, null);
            notifyOutboxHandlers(MESSAGE_ERROR_HANDLER, 0, 0, eh);

        } else {
            Log.d(TAG, "update, making cursor");
            Cursor cursor = catalog.getAll();
            Log.d(TAG, "update,cursor made");
            notifyOutboxHandlers(MESSAGE_UPDATE_VIEW, 0, 0, cursor);
        }
    }


    private void export(String path, AstroCatalog catalog) {
        notifyOutboxHandlers(MESSAGE_INPROGRESS, 0, 0, null);
        boolean noError = true;
        ErrorHandler eh = new ErrorHandler();
        InfoListSaver saver = null;
        try {

            Cursor cursor = catalog.getAll();//use separate cursor as the latter is affected by list view
            //even if it is blocked by progress dialog
            if (cursor != null) {

                saver = new InfoListStringSaverImp(new FileOutputStream(path));
                saver.addName("");
                int i = 0;
                while (cursor.moveToNext()) {
                    AstroObject obj = catalog.getObjectFromCursor(cursor);
                    saver.addObject(obj);
                    Log.d(TAG, "i=" + i++);
                }

            } else
                noError = false;
        } catch (Throwable e) {
            Log.d(TAG, "Exception=" + e);
            noError = false;
        } finally {
            try {
                saver.close();
            } catch (Exception e) {
            }
        }
        notifyOutboxHandlers(MESSAGE_REMOVE_INPROGRESS, 0, 0, null);
        notifyOutboxHandlers(MESSAGE_ERROR_HANDLER, 0, 0, eh);
        String message = (!noError ? activity.getString(R.string.export_error) : activity.getString(R.string.export_successfull_));
        notifyOutboxHandlers(MESSAGE_TEXT, 0, 0, message);


    }

    private void removeAll(AstroCatalog catalog) {
        notifyOutboxHandlers(MESSAGE_INPROGRESS, 0, 0, null);
        try {
            catalog.removeAll();
            update(catalog);
        } catch (UnsupportedOperationException e) {
            notifyOutboxHandlers(MESSAGE_TEXT, 0, 0, activity.getString(R.string.operation_not_supported));
        }
        notifyOutboxHandlers(MESSAGE_REMOVE_INPROGRESS, 0, 0, null);
    }
}
