package com.astro.dsoplanner;

import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.astro.dsoplanner.mCP.v.CPV;
import com.astro.dsoplanner.mCP.v.CoPV;
import com.astro.dsoplanner.mCP.v.CoPV.OnColorChangedListener;

public class ColorPickerDialog extends AlertDialog implements CoPV.OnColorChangedListener {

    private static final String COLOR_OF = "Color of ";

    private CoPV mColorPicker;

    private CPV mNewColor;

    private TextView mSelectedColor;

    private OnColorChangedListener mListener;

    private String objectName; //Which object to color

    protected ColorPickerDialog(Context context, int initialColor, String key) {
        super(context);
        objectName = key;

        init(initialColor);
    }

    private void init(int color) {
        // To fight color branding.
        getWindow().setFormat(PixelFormat.RGBA_8888);
        setUp(color);

    }

    private void setUp(int color) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_color_picker, null);

        setView(layout);

        setTitle(COLOR_OF + objectName);

        //Color value
        mSelectedColor = (TextView) layout.findViewById(R.id.selected_color);
        mColorPicker = (CoPV) layout.findViewById(R.id.color_picker_view);
        mNewColor = (CPV) layout.findViewById(R.id.new_color_panel);
        mColorPicker.setOnColorChangedListener(this);
        mColorPicker.setColor(color, true);
    }

    /**
     * Uses the current color value to update screen Text field
     */
    private void updateColorIndicator() {
        mSelectedColor.setText(String.format(Locale.US, "0x%x", mNewColor.getColor()));
    }

    public void onColorChanged(int color) {
        mNewColor.setColor(color);
        updateColorIndicator();
        if (mListener != null) {
            mListener.onColorChanged(color);
        }
    }

    public void setAlphaSliderVisible(boolean visible) {
        mColorPicker.setAlphaSliderVisible(visible);
    }

    public int getColor() {
        return mColorPicker.getColor();
    }

    public String getName() {
        return objectName;
    }

}
