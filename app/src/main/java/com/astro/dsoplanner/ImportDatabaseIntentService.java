package com.astro.dsoplanner;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.ClipboardManager;
import android.util.Log;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.CustomObject;
import com.astro.dsoplanner.database.CometsDatabase;
import com.astro.dsoplanner.database.CustomDatabase;
import com.astro.dsoplanner.database.CustomDatabaseLarge;
import com.astro.dsoplanner.database.DbListItem.FieldTypes;
import com.astro.dsoplanner.database.NoteDatabase;
import com.astro.dsoplanner.infolist.CometListLoader;
import com.astro.dsoplanner.infolist.InfoListImpl;
import com.astro.dsoplanner.infolist.InfoListLoader;
import com.astro.dsoplanner.infolist.InfoListStringLoaderImp;
import com.astro.dsoplanner.infolist.MinorPlanetListLoader;
import com.astro.dsoplanner.misc.NSOG;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//do not use toasts as the handle intent is run in non-UI thread
public class ImportDatabaseIntentService extends IntentService {
    private static final String IMPORT_DATABASE_ERROR_LOG_TXT = "import_database_error_log.txt";
    private static final String NSOG_IMPORT = "NSOG import";
    private static final String IMPORT_DATABASE = "0";
    private static final String TEMP = "temp";
    private static final String CLASS_CAST_ERROR = "class cast error";
    private static final String TAG = ImportDatabaseIntentService.class.getSimpleName();
    private static int NOTIFICATION_ID = 1337;

    private NotificationHelper notificationHelper;
    private static Set<String> dbset = new HashSet<String>();//set of databases to be imported to

    public ImportDatabaseIntentService() {
        super(IMPORT_DATABASE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStart, intent=" + intent);
        notificationHelper = new NotificationHelper(this);
        startForeground(NOTIFICATION_ID, notificationHelper.createNotification(getString(R.string.importing_database), getString(R.string.importing_database)));

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    /**
     * @param dbname - database to be imported into
     */
    public static synchronized void registerImportToService(String dbname) {
        dbset.add(dbname);
    }

    public static synchronized boolean isBeingImported(String dbname) {
        return dbset.contains(dbname);
    }

    private static synchronized void unregisterImportFromService(String dbname) {
        dbset.remove(dbname);
    }

    /**
     * @param result true for completion without errors, false otherwise
     */
    private void workOver(boolean result, ErrorHandler eh, String dbname) {
        if (dbname != null) {
            unregisterImportFromService(dbname);
        }

        if (!result) {
            notificationHelper.error(getString(R.string.error_importing_database));
        } else {
            notificationHelper.completed();
        }
        Log.d(TAG, "import over");
        if (eh != null && eh.hasError()) {
            Intent intent = new Intent(Constants.IDIS_ERROR_BROADCAST);
            String errorString = eh.getErrorString();
            intent.putExtra(Constants.IDIS_ERROR_STRING, errorString);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            File f = new File(Global.dsoPath, IMPORT_DATABASE_ERROR_LOG_TXT);
            MyExTracker.saveInfo2(f, errorString, null);
        }
    }

    private InputStream getInputStream(String dataLocation) throws FileNotFoundException {
        return getContentResolver().openInputStream(Uri.parse(dataLocation));
    }

    private String getShortName(String dataLocation) {
        return FileTools.getDisplayName(getApplicationContext(), Uri.parse(dataLocation));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        notificationHelper.clearContent();//for more than 1 download
        Log.d(TAG, "onHandleIntent, intent=" + intent);
        if (intent == null) {
            workOver(true, null, null);
            return;
        }

        Bundle b = intent.getExtras();
        //just in case
        if (b == null) {
            workOver(true, null, null);
            return;
        }
        for (String key : b.keySet()) {
            Log.d(TAG, "key=" + key + " value=" + b.get(key));
        }


        //NSOG
        boolean nsog = intent.getBooleanExtra(Constants.IDIS_NSOG, false);
        if (nsog) {
            notificationHelper.setFileName(NSOG_IMPORT);
            try {
                new NSOG(this).make();
            } catch (Exception e) {
                Log.d(TAG, "ex=" + e);
            }
            workOver(true, null, null);
            return;
        }

        boolean pasting = intent.getBooleanExtra(Constants.IDIS_PASTING, false);
        String content = "";
        if (pasting) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            if (clipboard.hasText()) {
                content = clipboard.getText().toString();
            }
        }

        boolean cmo_update = intent.getBooleanExtra(Constants.IDIS_CMO_UPDATE, false);
        Log.d(TAG, "import started");

        //initialisation

        String dataLocation = intent.getStringExtra(Constants.IDIS_FILENAME);

        String dbname = intent.getStringExtra(Constants.IDIS_DBNAME);
        if (dbname == null) {
            Log.d(TAG, "dbname=null");
            workOver(false, null, null);
            return;
        }

        if (!pasting && dataLocation == null) {
            Log.d(TAG, "filename=null");
            workOver(false, null, dbname);
            return;
        }

        notificationHelper.setFileName(pasting ? getString(R.string.clipboard) : getShortName(dataLocation));
        boolean notedatabase = intent.getBooleanExtra(Constants.IDIS_NOTES, false);

        int catalog = intent.getIntExtra(Constants.IDIS_CATALOG, -1);
        if (catalog == -1 && !notedatabase) {
            Log.d(TAG, "catalog=-1");
            workOver(false, null, dbname);
            return;
        }

        boolean ignoreNgcicRefs = intent.getBooleanExtra(Constants.IDIS_IGNORE_NGCIC_REF, false);
        boolean ignoreCustomDbRefs = intent.getBooleanExtra(Constants.IDIS_IGNORE_CUSTOMDB_REF, false);


        byte[] ftypesArr = intent.getByteArrayExtra(Constants.IDIS_FTYPES);
        FieldTypes ftypes = null;
        if (ftypesArr != null) {
            ByteArrayInputStream bin = new ByteArrayInputStream(ftypesArr);
            try {
                DataInputStream din = new DataInputStream(bin);
                ftypes = new FieldTypes(din);
            } catch (Exception e) {
            }

        }
        if (ftypes == null) ftypes = new FieldTypes();

        AstroCatalog cat = null;
        NoteDatabase ndb = null;
        if (!notedatabase) {
            switch (catalog) {
                case AstroCatalog.COMET_CATALOG:
                    cat = new CometsDatabase(this, catalog);
                    break;
                case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
                    cat = new CometsDatabase(this, catalog);
                    break;
                default:
                    if (ftypes.isEmpty())//there are no restrictions imposed
                        cat = new CustomDatabase(this, dbname, catalog);
                    else cat = new CustomDatabaseLarge(this, dbname, catalog, ftypes);
            }
        } else {
            ndb = new NoteDatabase(this);
        }

        InputStream in = null;
        ErrorHandler eh = new ErrorHandler();
        unregisterImportFromService(dbname);//so that db could be opened
        if (!notedatabase) {
            cat.open(eh);
        } else {
            ndb.open(eh);
        }

        registerImportToService(dbname);//block for all others
        if (eh.hasError()) {
            //	eh.showErrorInToast(this);
            Log.d(TAG, "error opening catalog");
            workOver(false, eh, dbname);
            return;
        }

        try {
            InfoListImpl iL;
            if (!notedatabase) {
                if ((catalog == AstroCatalog.COMET_CATALOG || catalog == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG) && cmo_update)
                    iL = new SqlDbFillerInfoListImpl(TEMP, CustomObject.class, cat, notificationHelper, true);
                else
                    iL = new SqlDbFillerInfoListImpl(TEMP, CustomObject.class, cat, notificationHelper);//when loading the list all non-custom objects are discarded
                iL.allowObjExtraction();
            } else {
                iL = new NoteDbFillerInfoListImpl(TEMP, ndb, notificationHelper);
            }

            InfoListLoader loader;
            if (!pasting) {
                in = getInputStream(dataLocation);
                if ((catalog == AstroCatalog.COMET_CATALOG) && cmo_update)
                    loader = new CometListLoader(in);
                else if (catalog == AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG && cmo_update)
                    loader = new MinorPlanetListLoader(in);
                else loader = new InfoListStringLoaderImp(in, ftypes);
            } else {
                loader = new InfoListStringLoaderImp(content, ftypes);
            }

            if (ignoreCustomDbRefs && loader instanceof InfoListStringLoaderImp) {
                InfoListStringLoaderImp loaderimp = (InfoListStringLoaderImp) loader;
                loaderimp.setIgnoreCustomDbRefsFlag();
            }
            if (ignoreNgcicRefs && loader instanceof InfoListStringLoaderImp) {
                InfoListStringLoaderImp loaderimp = (InfoListStringLoaderImp) loader;
                loaderimp.setIgnoreNgcicRefsFlag();
            }

            ErrorHandler eh1 = iL.load(loader);
            eh.addError(eh1);
            Log.d(TAG, "error in try catch=" + eh.getErrorString());
        } catch (Exception e) {
            Log.d(TAG, "exception=" + AstroTools.getStackTrace(e));
            if (!eh.hasError()) {
                eh.addError(new ErrorHandler.ErrorRec(ErrorHandler.IO_ERROR, ""));
            }
            workOver(false, eh, dbname);
            return;
        } finally {
            Log.d(TAG, "finally");
            try {
                if (!pasting) in.close();
            } catch (Exception e) {
            }
            if (eh.hasError()) {
                Log.d(TAG, "error=" + eh.getErrorString());
            }

            if (!notedatabase) cat.close();
            else ndb.close();
        }
        workOver(true, eh, dbname);
    }

    private static class NotificationHelper {
        static protected int COMPLETE_NOTIFICATION_ID = 1;
        protected NotificationManager mNotificationManager;
        Context context;

        public NotificationHelper(Context context) {
            this.context = context;
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new NotificationCompat.Builder(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String NOTIFICATION_CHANNEL_ID = "com.astro.dsoplanner";
                String channelName = "DSOPlanner background service";
                NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
                try {
                    mNotificationManager.createNotificationChannel(chan);
                } catch (Exception e) {
                }
                mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            }
        }

        NotificationCompat.Builder mBuilder;
        String title = "";

        String pullDownText;
        String pullDownTitle;

        public Notification createNotification(String statusBarTitle, String pullDownTitle) {//,String filename) {
            Intent notificationIntent = new Intent();//,NotificationDisplay.class);
            PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
            this.pullDownTitle = pullDownTitle;
            mBuilder.setTicker(statusBarTitle).setContentTitle(pullDownTitle).setContentText(pullDownText).setSmallIcon(android.R.drawable.stat_notify_sync).setOngoing(true).setContentIntent(intent);

            try {
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            } catch (Exception e) {
            }
            return mBuilder.build();

        }

        public void setFileName(String name) {
            pullDownText = name;
        }

        public void progressUpdate(int items) {
            mBuilder.setContentText(pullDownText + ":" + "\n" + items + context.getString(R.string._processed));
            try {
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            } catch (Exception e) {
            }
        }

        public void clearContent() {
            mBuilder.setContentText("");
            try {
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            } catch (Exception e) {
            }
        }

        public void completed() {
            Intent notificationIntent = new Intent();//,NotificationDisplay.class);
            PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder mBuilderCompleted = new NotificationCompat.Builder(context);
            mBuilderCompleted.setContentText(context.getString(R.string.import_complete)).setSmallIcon(android.R.drawable.stat_notify_sync_noanim).setOngoing(false).setContentTitle(pullDownTitle).setContentIntent(intent);
            try {
                mNotificationManager.notify(COMPLETE_NOTIFICATION_ID++, mBuilderCompleted.build());
            } catch (Exception e) {
            }
        }


        public void error(String pullDownText) {
            Intent notificationIntent = new Intent();//,NotificationDisplay.class);
            PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder mBuilderError = new NotificationCompat.Builder(context);
            mBuilderError.setContentText(pullDownText).setSmallIcon(android.R.drawable.stat_notify_error).//stat_sys_download_done).
                    setOngoing(false).setContentTitle(pullDownTitle).setContentIntent(intent);
            try {
                mNotificationManager.notify(COMPLETE_NOTIFICATION_ID++, mBuilderError.build());
            } catch (Exception e) {
            }
        }
    }

    private class NoteDbFillerInfoListImpl extends InfoListImpl {
        private final String TAG = NoteDbFillerInfoListImpl.class.getSimpleName();
        NoteDatabase catalog;
        NotificationHelper nh;

        public NoteDbFillerInfoListImpl(String name, NoteDatabase catalog, NotificationHelper nh) {
            super(name, NoteRecord.class);
            this.catalog = catalog;
            this.nh = nh;

        }

        @Override
        public synchronized ErrorHandler load(InfoListLoader listLoader) {

            int line = 1;
            try {
                name = listLoader.getName();
            } catch (IOException e) {

                return new ErrorHandler(ErrorHandler.IO_ERROR, "");
            }
            Object obj = null;
            ErrorHandler eh = new ErrorHandler();
            while (true) {
                try {
                    ErrorHandler.ErrorRec erec = new ErrorHandler.ErrorRec();
                    erec.lineNum = line;
                    obj = listLoader.next(erec);
                    if (obj == null) {
                        eh.addError(erec);//error from listLoader processing
                        continue;
                    }
                    if (objClass != null) {
                        try {
                            Object o = objClass.cast(obj);
                            if (o != null) catalog.add((NoteRecord) o, eh);

                        } catch (ClassCastException e) {
                            eh.addError(ErrorHandler.WRONG_TYPE, CLASS_CAST_ERROR, "", line);
                        }
                    }

                } catch (IOException e) {
                    Log.d(TAG, "IOException=" + AstroTools.getStackTrace(e));
                    if (!(e instanceof EOFException)) {
                        eh.addError(ErrorHandler.IO_ERROR, "", "", line);
                    }
                    try {
                        listLoader.close();
                    } catch (IOException e1) {
                    }

                    return eh;
                }
                if (line % 100 == 0) {
                    nh.progressUpdate(line);
                }
                line++;
            }


        }
    }

    private class SqlDbFillerInfoListImpl extends InfoListImpl {
        private final String TAG = SqlDbFillerInfoListImpl.class.getSimpleName();
        AstroCatalog catalog;
        NotificationHelper nh;
        private boolean update = false;

        /**
         * filling the db, not checking if the object exists in db already
         *
         * @param name
         * @param objClass
         * @param catalog
         * @param nh
         */
        public SqlDbFillerInfoListImpl(String name, Class objClass, AstroCatalog catalog, NotificationHelper nh) {
            super(name, objClass);
            this.catalog = catalog;
            this.nh = nh;

        }

        /**
         * @param name
         * @param objClass
         * @param catalog
         * @param nh
         * @param update   - update object if the object with the same name exists
         */
        public SqlDbFillerInfoListImpl(String name, Class objClass, AstroCatalog catalog, NotificationHelper nh, boolean update) {
            super(name, objClass);
            this.catalog = catalog;
            this.nh = nh;
            this.update = update;

        }

        @Override
        public synchronized ErrorHandler load(InfoListLoader listLoader) {

            int line = 1;
            try {
                name = listLoader.getName();
            } catch (IOException e) {

                return new ErrorHandler(ErrorHandler.IO_ERROR, "");
            }
            Object obj = null;
            ErrorHandler eh = new ErrorHandler();
            catalog.beginTransaction();
            while (true) {
                try {
                    ErrorHandler.ErrorRec erec = new ErrorHandler.ErrorRec();
                    erec.lineNum = line;
                    obj = listLoader.next(erec);
                    if (obj == null) {
                        eh.addError(erec);//error from listLoader processing
                        continue;
                    }
                    if (objClass != null) {
                        try {
                            Object o = castObject(obj);
                            if (o != null) {
                                if (!update) catalog.add((CustomObject) o, eh);
                                else {
                                    CustomObject ao = (CustomObject) o;
                                    List<AstroObject> list = catalog.searchName(ao.getShortName());
                                    if (list.size() == 0) {
                                        catalog.add(ao, eh);
                                    } else {
                                        if (catalog instanceof CustomDatabaseLarge) {
                                            CustomDatabaseLarge cat = (CustomDatabaseLarge) catalog;
                                            for (AstroObject ob : list) {
                                                ao.id = ob.id;
                                                cat.edit(ao);
                                            }

                                        }
                                    }
                                }

                            }

                        } catch (ClassCastException e) {
                            Log.d(TAG, "Incompatible import format. " + e);
                            eh.addError(ErrorHandler.WRONG_TYPE, CLASS_CAST_ERROR, "", line);
                        }
                    }

                } catch (IOException e) {
                    Log.d(TAG, "IOException=" + AstroTools.getStackTrace(e));
                    if (!(e instanceof EOFException)) {
                        eh.addError(ErrorHandler.IO_ERROR, "", "", line);
                    }
                    try {
                        listLoader.close();
                    } catch (IOException e1) {
                    }

                    try {
                        catalog.setTransactionSuccessful();
                    } catch (Exception e2) {

                    } finally {
                        catalog.endTransaction();
                    }


                    return eh;
                }

                if (line % 100 == 0) {
                    nh.progressUpdate(line);
                }
                line++;
            }
        }
    }
}
