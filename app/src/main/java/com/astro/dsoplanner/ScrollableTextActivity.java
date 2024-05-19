package com.astro.dsoplanner;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ScrollableTextActivity extends ParentActivity {
    public static final String ARGUMENT = "text_id";
    public static final String ARGUMENT_2 = "message";
    public final static int QUICK_START_GUIDE = 1;
    public final static int WHATS_NEW = 2;
    public final static int BACKUP_HELP = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int text_id = getIntent().getIntExtra(ARGUMENT, -1);
        boolean nightmode = SettingsActivity.getNightMode();
        if (nightmode)
            setContentView(R.layout.quick_start_guide_dialog_night);
        else
            setContentView(R.layout.quick_start_dialog);

        int id = R.id.quick_start_guide_text;
        if (nightmode)
            id = R.id.quick_start_guide_text_night;

        if (text_id == QUICK_START_GUIDE) {
            setTitle(R.string.quick_start_guide1);
        } else if (text_id == WHATS_NEW) {
            setTitle(getString(R.string.what_s_new));
            TextView tv = (TextView) findViewById(id);
            String text = getWhatsNewText();
            Spanned spanned = Html.fromHtml(text);
            tv.setText(spanned);
        } else if (text_id == BACKUP_HELP) {
            setTitle(R.string.help);
            TextView tv = (TextView) findViewById(id);
            String message = getIntent().getStringExtra(ARGUMENT_2);
            tv.setText(message);
        } else { //should not be here
            setTitle("");
            TextView tv = (TextView) findViewById(id);
            tv.setText("");
        }
    }

    private String getWhatsNewText() {
        OutputStream output = null;
        InputStream in = null;
        String s = "";
        String s2 = "";
        try {
            in = getResources().openRawResource(R.raw.whatsnew);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((s2 = br.readLine()) != null) {
                s = s + s2 + "\n";
            }
        } catch (Exception e) {
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
        return s;
    }
}
