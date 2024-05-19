package com.astro.dsoplanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class NoteLandscapeActivity extends ParentActivity {

    private static final String STRING = "string";

    String tmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        this.setContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        final InputDialog d = new InputDialog(this);

        d.disableBackButton(true);
        d.setTitle("");
        d.setTextLinesNumber(6);
        d.setType(InputDialog.DType.INPUT_TEXT);
        tmp = getIntent().getStringExtra(STRING);
        d.setValue(tmp);
        d.setPositiveButton(getString(R.string.save), new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                // Save text to the current note field
                d.hide();
                Intent result = new Intent();
                result.putExtra(STRING, value);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
        d.setNegativeButton(getString(R.string.cancel), new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                Intent result = new Intent(); //restore old value
                result.putExtra(STRING, tmp);
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
        d.toggleKeyboard();
        d.showAtTop();
    }
}
