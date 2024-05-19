package com.astro.dsoplanner;

import com.astro.dsoplanner.InputDialog.DType;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

public class InputPreference extends EditTextPreference {
    private InputDialog d;

    Context context;

    public InputPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        this.context = context;
    }

    public InputPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public InputPreference(Context context) {
        super(context);
        this.context = context;
    }

    protected void onClick() {
        DType mode;
        final String key = getKey();
        String message = getDialogTitle().toString();
        if (message.length() == 0) message = getTitle().toString();

        d = new InputDialog(context);
        d.setTitle(getContext().getString(R.string.dso_planner_settings));
        d.setMessage(message);
        if (key.substring(0, 1).contentEquals("d")) mode = InputDialog.DType.INPUT_NUMBER;
        else mode = InputDialog.DType.INPUT_STRING;
        d.setType(mode);
        String value = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).getString(key, "");
        d.setValue(value);

        d.setPositiveButton(getContext().getString(R.string.ok), new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).edit();
                editor.putString(key, value);
                editor.commit();
            }
        });
        d.setNegativeButton(getContext().getString(R.string.cancel));
        d.show();
    }
}
