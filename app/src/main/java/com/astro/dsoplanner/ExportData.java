package com.astro.dsoplanner;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;


public class ExportData {
    public static final String TAG = "ExportData";

    public static interface Predicate<T> {
        public boolean accept(T t);
    }

    Activity activity;
    int requestCode;
    Predicate<OutputStream> run;
    String defaultFileName;
    String namePassed = null;
    boolean mNightGuard = false;

    public ExportData(Activity activity, int requestCode, String defaultFileName) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.defaultFileName = defaultFileName;
    }

    /**
     * not passed in the constructor for convenience of calling activities -
     * to have one instance of ExportData in the activity
     * for the correct work of night guard.
     *
     * @param run
     */
    public void setCodeToRun(Predicate<OutputStream> run) {
        this.run = run;
    }

    /**
     * @param name could be null to indicate that uriTree contains full path     *
     */
    private void go(Uri uriTree, String name) {
        boolean noError = true;
        Uri uriOut = uriTree;
        if (name != null) {
            DocumentFile root = DocumentFile.fromTreeUri(activity, uriTree);
            DocumentFile dfile = root.createFile("application/octet-stream", name);
            uriOut = dfile.getUri();
        }

        try {
            OutputStream out = new BufferedOutputStream(activity.getContentResolver().openOutputStream(uriOut));
            noError = run.accept(out);
        } catch (Throwable ignore) {
            noError = false;
        }
        String message = (!noError ? activity.getString(R.string.export_error) : activity.getString(R.string.export_successfull_));
        showDialog(InputDialog.message(activity, message));
    }

    public void start() {
        InputDialog d = new InputDialog(activity);
        d.setType(InputDialog.DType.INPUT_STRING);
        if (!"".equals(defaultFileName)) d.setValue(defaultFileName);
        d.setTitle(activity.getString(R.string.export_to_the_file));
        d.setMessage(activity.getString(R.string.please_enter_the_file_name_for_exporting_warning_the_file_with_the_same_name_will_be_silently_overwritten_in_the_));
        d.setPositiveButton(activity.getString(R.string.local), new InputDialog.OnButtonListener() {

            public void onClick(String value) {
                String name = InputDialog.getResult();
                if ("".equals(name)) {
                    String message = activity.getString(R.string.export_error_no_file_name_selected);
                    showDialog(InputDialog.message(activity, message));
                    return;
                } else {
                    String path = Global.exportImportPath + name;
                    Uri uri = Uri.fromFile(new File(path));
                    go(uri, null);
                }
            }
        });
        d.setNegativeButton(activity.getString(R.string.cancel));
        d.setMiddleButton(activity.getString(R.string.picker), new InputDialog.OnMidButtonListener() {
            public void onClick(String value) {
                Log.d(TAG, "setMiddleButton, value=" + value);
                namePassed = value;
                if ("".equals(value)) {
                    String message = activity.getString(R.string.export_error_no_file_name_selected);
                    showDialog(InputDialog.message(activity, message));
                    d.dismiss();
                    return;
                }

                if (SettingsActivity.getNightMode()) {
                    if (!mNightGuard) { //first time here
                        mNightGuard = true;
                        showDialog(InputDialog.message(activity, R.string.nightmode_warning_click_again, 0));

                        return;
                    }
                }
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                try {
                    activity.startActivityForResult(intent, requestCode);
                } catch (Exception e) {
                }
                mNightGuard = false; //to be compatible with import behaviour
                d.dismiss();
            }
        });
        showDialog(d);

    }

    private void showDialog(Dialog d) {
        if (activity instanceof ParentActivity) {
            ((ParentActivity) activity).registerDialog(d).show();
        } else if (activity instanceof ParentListActivity) {
            ((ParentListActivity) activity).registerDialog(d).show();
        } else if (activity instanceof ParentPreferenceActivity) {
            ((ParentPreferenceActivity) activity).registerDialog(d).show();
        }
    }

    public void process(Intent data) {
        Uri folder = data.getData();
        Log.d(TAG, "process, namePassed=" + namePassed);
        go(folder, namePassed);

    }
}