package com.astro.dsoplanner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.astro.dsoplanner.f.IFolderItemListener;

import java.io.File;

public class SelectFileActivity extends ParentActivity implements IFolderItemListener {
    private static int REQ_PICKER_USED = 876;
    private static String m_path;
    private static IPickFileCallback m_listener;

    private FL2 localFolders;
    private boolean mNightGuard;

    public static void setPath(String string) {
        m_path = string;
    }

    public static void setListener(IPickFileCallback listener) {
        m_listener = listener;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_select_file);
        localFolders = findViewById(R.id.localfolders);
        localFolders.setIFolderItemListener(this);
        String path = m_path;
        File file = new File(path);
        if (file.isFile())
            path = file.getParentFile().getAbsolutePath();
        localFolders.setDir(path);//change directory if u want,default is root
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //Otherwise the button is View.GONE in the layout
            setupSysUiPicker(getApplicationContext());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setupSysUiPicker(Context context) {
        Button button = findViewById(R.id.b_sysuipicker);
        if (button != null) {
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(view -> {
                if (SettingsActivity.getNightMode()) {
                    if (!mNightGuard) { //first time here
                        mNightGuard = true;
                        registerDialog(InputDialog
                                .message(view.getContext(),
                                        R.string.nightmode_warning_click_again, 0))
                                .show();
                        return;
                    }
                }
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
                Uri uri0 = SettingsActivity.getFileDialogUri(
                        view.getContext().getApplicationContext());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && uri0 != null) {
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri0);
                }
                try {
                    startActivityForResult(intent, REQ_PICKER_USED);
                } catch (Exception e) {
                }
            });
        }
    }

    //Your stuff here for Cannot open Folder
    public void onCannotFileRead(File file) {
        registerDialog(InputDialog.message(this, getString(R.string.error_) + file.getName() + getString(R.string._folder_can_t_be_read_), 0)).show();
    }

    /**
     * Exit, returning full path of the selected file
     *
     * @param file
     */
    public void onFileClicked(File file) {
        SettingsActivity.setFileDialogPath(file.getAbsolutePath(), getApplicationContext());
        onUriClicked(Uri.fromFile(file));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQ_PICKER_USED) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
            }
            if (uri != null)
                onUriClicked(uri);
        }
    }

    private void onUriClicked(Uri uri) {
        m_listener.callbackCall(uri);
        SettingsActivity.setFileDialogUri(uri, getApplicationContext());
        finish();
    }

}
