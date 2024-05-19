package com.astro.dsoplanner;

import android.content.Context;
import android.widget.Toast;

public class InputDialogReplacement extends InputDialog {

    String s;
    Context context;

    public InputDialogReplacement(Context context, String s) {
        super(context);
        this.context = context;
        this.s = s;
    }

    @Override
    public void show() {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
