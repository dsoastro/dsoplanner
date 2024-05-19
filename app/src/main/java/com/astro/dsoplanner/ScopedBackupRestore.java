package com.astro.dsoplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.util.Log;

import com.astro.dsoplanner.base.Comet;
import com.astro.dsoplanner.base.MinorPlanet;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.graph.StarMags;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListCollectionFiller;
import com.astro.dsoplanner.infolist.InfoListImpl;
import com.astro.dsoplanner.infolist.InfoListLoader;
import com.astro.dsoplanner.infolist.InfoListLoaderImp;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListSaverImp;
import com.astro.dsoplanner.infolist.ListHolder;

//https://stackoverflow.com/questions/61118918/create-new-file-in-the-directory-returned-by-intent-action-open-document-tree


public class ScopedBackupRestore {
    public static final String TAG = "ScopedBackup";
    public static final String BACKUP = "backup";
    public static final String DATABASES = "databases";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    SettingsInclActivity activity;
    int requestCode;

    public ScopedBackupRestore(SettingsInclActivity activity, int requestCode) {
        this.activity = activity;
        this.requestCode = requestCode;
    }

    private void copyMyShPrefsFromSdToMemory(Uri uri) {
        String[] names = AstroTools.getMyImportantPrefs();
        copyShPrefsFromSdToMemory(uri, SettingsActivity.getSharedPreferences(activity).edit(), names);
    }

    /**
     * do not remove old prefs but just overrides them. Thus some old prefs may remain
     *
     * @param prefs
     * @param filter null if to restore all. definite list with names if only some
     */
    private void copyShPrefsFromSdToMemory(Uri uri, SharedPreferences.Editor prefs, String[] filter) {
        String[] exclude = new String[]{activity.getString(R.string.dimlight),
                activity.getString(R.string.autonightmode),
                activity.getString(R.string.system_language),
                SettingsActivity.OPT_NIGHT_MODE,
                SettingsActivity.OPT_ONYX_SKIN,
                activity.getString(R.string.skinfront),
                activity.getString(R.string.nagscreenon)};
        Set<String> ex_set = new HashSet<String>();
        for (String s : exclude) {
            ex_set.add(s);
        }

        InputStream in = null;
        InfoList list = new InfoListImpl("", ShItem.class);
        try {
            in = activity.getContentResolver().openInputStream(uri);
            InfoListLoader loader = new InfoListLoaderImp(in);
            list.load(loader);


            Set<String> name_set = new HashSet<String>();
            if (filter != null) {
                name_set.addAll(Arrays.asList(filter));
            }
            for (Object o : list) {
                ShItem sh = (ShItem) o;
                if (filter != null) {
                    if (!name_set.contains(sh.name))
                        continue;
                }
                if (ex_set.contains(sh.name))
                    continue;
                Log.d(TAG, "sh=" + sh);
                switch (sh.type) {
                    case ShItem.BOOLEAN:
                        prefs.putBoolean(sh.name, sh.bvalue);
                        break;
                    case ShItem.FLOAT:
                        prefs.putFloat(sh.name, sh.fvalue);
                        break;
                    case ShItem.INT:
                        prefs.putInt(sh.name, sh.ivalue);
                        break;
                    case ShItem.LONG:
                        prefs.putLong(sh.name, sh.lvalue);
                        break;
                    case ShItem.STRING:
                        prefs.putString(sh.name, sh.svalue);
                        break;
                }
            }
            prefs.commit();
        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }

    }

    public ErrorHandler copySqlDatabasesFromSdToMemory(DocumentFile root) { //overwrites existing in memory databases
        ErrorHandler eh = new ErrorHandler();
        String[] names = new String[]{Constants.NOTE_DATABASE_NAME, Constants.EYEPIECES_DATABASE_NAME, Constants.TELESCOPE_DATABASE_NAME, Constants.LAUNCHER_DATABASE_NAME};
        List<String> listnames = new ArrayList<String>();

        Collections.addAll(listnames, names);

        boolean result = Init.initDbList(activity, root);//update the list with the list from the file
        Log.d(TAG, "initDbList result=" + result);

        if (result) { //db list list loaded successfully
            //load databases only if database list was loaded successfully
            Iterator it = ListHolder.getListHolder().get(InfoList.DB_LIST).iterator();
            for (; it.hasNext(); ) {
                DbListItem item = (DbListItem) it.next();
                Log.d(TAG, "restore, item=" + item);
                if (SearchRules.isToBeBackedUp(item.cat)) {
                    listnames.add(item.dbFileName);
                }
            }
        } else { //add comets and minor planets
            listnames.add(Comet.DB_NAME);
            listnames.add(MinorPlanet.DB_NAME_BRIGHT);
        }
        File dirOut = activity.getDatabasePath(Constants.NGCIC_DATABASE_NAME).getParentFile();

        for (String name : listnames) {
            DocumentFile dfile = root.findFile(name);
            if (dfile == null) {
                continue;
            }
            Uri uri = dfile.getUri();

            boolean res = new AstroTools.FileToCopy(uri, dirOut.getAbsolutePath(), name, activity).copy();
            Log.d(TAG, "copied " + name + " result=" + res);
            if (!res)
                eh.addError(ErrorHandler.IO_ERROR, activity.getString(R.string.error_copying_file) + name);
        }


        int[] prefl = new int[]{InfoList.DB_LIST, InfoList.PrimaryObsList, InfoList.PrimaryObsList + 1, InfoList.PrimaryObsList + 2, InfoList.PrimaryObsList + 3,
                InfoList.LOCATION_LIST, InfoList.SREQUEST_LIST};

        String dirstr = activity.getFilesDir().getAbsolutePath();
        for (int l : prefl) {
            String name = Constants.PREF_LIST_NAME_BASE + l;
            DocumentFile dfile = root.findFile(name);
            if (dfile != null) {
                Uri uri = dfile.getUri();
                boolean res = new AstroTools.FileToCopy(uri, dirstr, name, activity).copy();
                Log.d(TAG, "copied " + name + " result=" + res);
                if (!res)
                    eh.addError(ErrorHandler.IO_ERROR, activity.getString(R.string.error_copying_file) + name);
            }
        }
        new Init(activity, null).initLists();
        SettingsActivity.putSharedPreferences(Constants.QUERY_CONTROLLER_SPIN_POS, 0, activity);
        SettingsActivity.putSharedPreferences(Constants.SRA_ACTIVE_SEARCH_REQUEST, -1, activity);

        DocumentFile dfile = root.findFile(Constants.DEFAULT_PREFS);
        if (dfile != null) {
            Uri uri = dfile.getUri();
            copyShPrefsFromSdToMemory(uri, PreferenceManager.getDefaultSharedPreferences(activity).edit(), null);
        }

        dfile = root.findFile(Constants.MY_PREFS);
        if (dfile != null) {
            Uri uri = dfile.getUri();
            copyMyShPrefsFromSdToMemory(uri);
        }

        StarMags.initMags(activity);
        return eh;


    }

    /**
     * Copy directory "inFolder" to SAF rootFolder, this includes
     * creating "inFolder" directory in SAF.
     * inFolder is located in Global.exportImportPath, like dss etc
     *
     * @param inFolder   "" means current inFolder
     * @param rootFolder document file for root directory
     */
    public ErrorHandler copyFolder(String inFolder, DocumentFile rootFolder) {
        ErrorHandler eh = new ErrorHandler();
        File dir = new File(Global.exportImportPath, inFolder);
        if (dir.exists() && dir.isDirectory()) {
            DocumentFile folderDocument;
            if ("".equals(inFolder))
                folderDocument = rootFolder;
            else
                folderDocument = rootFolder.createDirectory(inFolder);
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) continue;
                    String name = file.getName();
                    DocumentFile fileDocument = folderDocument.createFile(APPLICATION_OCTET_STREAM, name);
                    Uri uri = fileDocument.getUri();
                    boolean result = new AstroTools.FileToCopy(dir.getAbsolutePath(), name, uri, activity).copy();
                    Log.d(TAG, "copied " + name + " result=" + result);
                    if (!result) {
                        eh.addError(ErrorHandler.IO_ERROR, activity.getString(R.string.error_copying_file) + file.getAbsolutePath());
                    }

                }
            }
        }
        return eh;
    }

    /**
     * Copy directory "outFolder" in SAF rootFolder, to app local storage (Global.exportImportPath).
     * outFolder is dss, notes, .
     *
     * @param rootFolder
     * @param outFolder
     * @return
     */
    public ErrorHandler copyFolder(DocumentFile rootFolder, String outFolder) {
        ErrorHandler eh = new ErrorHandler();
        File dirOut = new File(Global.exportImportPath, outFolder);

        DocumentFile folderDocument = null;
        if ("".equals(outFolder))
            folderDocument = rootFolder;
        else {
            folderDocument = rootFolder.findFile(outFolder);
            if (folderDocument == null || !folderDocument.isDirectory()) {
                return eh;
            }
        }
        DocumentFile[] dfiles = folderDocument.listFiles();
        if (dfiles != null) {
            for (DocumentFile dfile : dfiles) {
                if (dfile.isDirectory()) continue;
                String name = dfile.getName();
                Uri uri = dfile.getUri();
                boolean result = new AstroTools.FileToCopy(uri, dirOut.getAbsolutePath(), name, activity).copy();
                Log.d(TAG, "copied " + name + " result=" + result);
                if (!result) {
                    eh.addError(ErrorHandler.IO_ERROR, activity.getString(R.string.error_copying_file) + name);
                }
            }
        }

        return eh;
    }

    public ErrorHandler copySqlDatabasesFromMemoryToSD(DocumentFile rootFolder) {
        ErrorHandler eh = new ErrorHandler();
        String[] names = new String[]{Constants.NOTE_DATABASE_NAME, Constants.EYEPIECES_DATABASE_NAME, Constants.TELESCOPE_DATABASE_NAME, Constants.LAUNCHER_DATABASE_NAME};
        List<String> listnames = new ArrayList<String>();

        Collections.addAll(listnames, names);

        InfoList list = ListHolder.getListHolder().get(InfoList.DB_LIST);
        Iterator it = list.iterator();
        for (; it.hasNext(); ) {
            DbListItem item = (DbListItem) it.next();
            if (SearchRules.isToBeBackedUp(item.cat)) {//not adding ngcic db
                listnames.add(item.dbFileName);
            }
        }

        File dir = activity.getDatabasePath(Constants.NGCIC_DATABASE_NAME).getParentFile();
        String filesCopied = "";

        for (String name : listnames) {
            File in = new File(dir, name);
            if (in.exists()) {
                DocumentFile file = rootFolder.createFile(APPLICATION_OCTET_STREAM, name);
                Uri uri = file.getUri();
                boolean result = new AstroTools.FileToCopy(dir.getAbsolutePath(), name, uri, activity).copy();
                Log.d(TAG, "copied " + name + " result=" + result);

                if (!result)
                    eh.addError(ErrorHandler.IO_ERROR, activity.getString(R.string.error_copying_file) + in.getAbsolutePath());

            }

        }

        int[] prefl = new int[]{InfoList.DB_LIST, InfoList.PrimaryObsList, InfoList.PrimaryObsList + 1, InfoList.PrimaryObsList + 2, InfoList.PrimaryObsList + 3,
                InfoList.LOCATION_LIST, InfoList.SREQUEST_LIST};
        dir = activity.getFilesDir();
        for (int l : prefl) {
            String name = Constants.PREF_LIST_NAME_BASE + l;
            File in = new File(dir, name);
            if (in.exists()) {
                DocumentFile file = rootFolder.createFile(APPLICATION_OCTET_STREAM, name);
                Uri uri = file.getUri();
                boolean result = new AstroTools.FileToCopy(dir.getAbsolutePath(), name, uri, activity).copy();
                Log.d(TAG, "copied " + name + " result=" + result);
                if (!result)
                    eh.addError(ErrorHandler.IO_ERROR, activity.getString(R.string.error_copying_file) + in.getAbsolutePath());

            }
        }

        DocumentFile file = rootFolder.createFile(APPLICATION_OCTET_STREAM, Constants.DEFAULT_PREFS);
        Uri uri = file.getUri();
        boolean result = copyShPrefsFromMemoryToSd(uri, PreferenceManager.getDefaultSharedPreferences(activity).getAll());
        if (!result)
            eh.addError(ErrorHandler.IO_ERROR, activity.getString(R.string.error_copying_preferences));

        file = rootFolder.createFile(APPLICATION_OCTET_STREAM, Constants.MY_PREFS);
        uri = file.getUri();
        result = copyShPrefsFromMemoryToSd(uri, SettingsActivity.getSharedPreferences(activity).getAll());
        if (!result)
            eh.addError(ErrorHandler.IO_ERROR, activity.getString(R.string.error_copying_preferences));

        return eh;
    }

    private boolean copyShPrefsFromMemoryToSd(Uri uri, Map<String, ?> map) {
        InfoList list = new InfoListImpl("", ShItem.class);
        List<ShItem> tmp_list = new ArrayList<ShItem>();
        for (Map.Entry<String, ?> e : map.entrySet()) {
            String name = e.getKey();
            Object value = e.getValue();
            try {
                tmp_list.add(new ShItem(name, value));
            } catch (Exception ex) {
                continue;
            }
        }
        list.fill(new InfoListCollectionFiller(tmp_list));
        OutputStream out = null;
        try {
            out = activity.getContentResolver().openOutputStream(uri);
            InfoListSaver saver = new InfoListSaverImp(out);
            list.save(saver);
            Log.d(TAG, "sh prefs copied successfully");
        } catch (Exception e) {
            Log.d(TAG, "sh prefs copy, e=" + e);
            return false;
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }
        return true;

    }

    public void startBackup() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
        }
        Log.d(TAG, "start");
    }

    public void startRestore() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
        }
        Log.d(TAG, "start");
    }

    public void processBackupCallback(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "processCallback");
        if (resultCode == activity.RESULT_OK && requestCode == this.requestCode) {
            Uri uriTree = data.getData();
            DocumentFile root = DocumentFile.fromTreeUri(activity, uriTree);

            DocumentFile backup = root.createDirectory(BACKUP);
            DocumentFile dbs = backup.createDirectory(DATABASES);


            class Backup extends AsyncTask<Void, Void, ErrorHandler> {
                @Override
                protected ErrorHandler doInBackground(Void... ds) {
                    ErrorHandler eh = copySqlDatabasesFromMemoryToSD(dbs);
                    String[] folders = new String[]{"", Init.DSS2, Init.NOTES2, Init.IMAGES2};
                    for (String name : folders) {
                        ErrorHandler eh2 = copyFolder(name, backup);
                        eh.addError(eh2);
                    }
                    return eh;
                }

                @Override
                protected void onPostExecute(ErrorHandler eh) {
                    activity.dismissInProgressDialog();
                    if (eh.hasError()) {
                        eh.showError(activity);
                    } else {
                        Log.d(TAG, "Files copied");
                        InputDialog.toast(activity.getString(R.string.files_copied), activity).show();
                    }

                }
            }
            Runnable r = () -> {
                activity.showInProgressDialog();
                new Backup().execute();
            };
            String msg = activity.getString(R.string.backup_data);
            String folderName = root.getName();
            if (folderName == null)
                folderName = "";
            InputDialog d = AstroTools.getDialog(activity, msg + folderName + "?", r);
            ((ParentPreferenceActivity) activity).registerDialog(d).show();


        }
    }

    public void processRestoreCallback(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "processRestoreCallback");
        if (resultCode == activity.RESULT_OK && requestCode == this.requestCode) {
            ErrorHandler eh = new ErrorHandler();
            Uri uriTree = data.getData();
            DocumentFile root = DocumentFile.fromTreeUri(activity, uriTree);

            DocumentFile dbs = root.findFile(DATABASES);

            class Restore extends AsyncTask<Void, Void, ErrorHandler> {
                @Override
                protected ErrorHandler doInBackground(Void... ds) {
                    if (dbs != null && dbs.isDirectory()) {
                        ErrorHandler eh2 = copySqlDatabasesFromSdToMemory(dbs);
                        eh.addError(eh2);
                    } else {
                        eh.addError(ErrorHandler.IO_ERROR, activity.getString(R.string.database_directory_absent));
                        return eh;
                    }

                    String[] folders = new String[]{"", Init.DSS2, Init.NOTES2, Init.IMAGES2};
                    for (String folder : folders) {
                        ErrorHandler eh2 = copyFolder(root, folder);
                        eh.addError(eh2);
                    }
                    return eh;
                }

                @Override
                protected void onPostExecute(ErrorHandler eh) {
                    activity.dismissInProgressDialog();
                    if (eh.hasError()) {
                        eh.showError(activity);
                    } else {
                        Log.d(TAG, "Files copied");
                        InputDialog.toast(activity.getString(R.string.files_copied), activity).show();
                    }

                }
            }
            Runnable r = () -> {
                activity.showInProgressDialog();
                new Restore().execute();
            };
            String msg = activity.getString(R.string.restore_data);
            String folderName = root.getName();
            if (folderName == null)
                folderName = "";
            InputDialog d = AstroTools.getDialog(activity, msg +
                    folderName + "?\n\n" + activity.getString(R.string.user_databases_will_be_overwriten_from_files_in_), r);
            ((ParentPreferenceActivity) activity).registerDialog(d).show();

        }
    }

}
