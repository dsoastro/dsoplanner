package com.astro.dsoplanner;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

/**
 * former MyCheckBox
 *
 * @author leonid
 */
public class ChB extends CheckBox {
    int position = 0;

    public ChB(Context context) {
        super(context);
    }

    public ChB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Default constructor override
    public ChB(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

}
