package com.astro.dsoplanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

/**
 * former SelectPreference
 *
 * @author leonid
 */
public class SeP extends ListPreference {
    private InputDialog d;
    Context context;

    public SeP(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        this.context = context;
    }

    public SeP(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public SeP(Context context) {
        super(context);
        this.context = context;
    }

    protected void onClick() {
        final String key = getKey();
        String message = getDialogTitle().toString();
        if (message.length() == 0) message = getTitle().toString();

        d = new InputDialog(context);
        d.setTitle("".equals(message) ? getContext().getString(R.string.dso_planner_settings) : message);
        String value = PreferenceManager.getDefaultSharedPreferences(context).getString(key, "");
        d.setValue(value);

        //As no OK button provided only Cancel button will be automatically appended
        d.setListItems(getEntries(), new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(context).edit();
                editor.putString(key, value);
                editor.commit();

            }
        });
        d.setNegativeButton(getContext().getString(R.string.cancel));
        d.show();
    }
}