package com.astro.dsoplanner;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.astro.dsoplanner.graph.GraphActivity;

//Spinner replacer with Button

/**
 * former BSpinner
 *
 * @author leonid
 */
public class BSp extends Button {
    public interface OnUpCallback {
        void onUp();
    }

    private String[] spinArr;
    private String[] spinArr2;
    private GraphActivity gra;

    public BSp(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        color();
    }

    public BSp(Context context, AttributeSet attrs) {
        super(context, attrs);
        color();
    }

    public BSp(Context context) {
        super(context);
        color();
    }

    private void color() {
        if (SettingsActivity.getNightMode()) setTextColor(0xff9f0000);
        else setTextColor(0xffefefef);
    }

    public void init(GraphActivity g) {
        gra = g;
        spinArr = g.getSpinArray();
        spinArr2 = g.getSpinArray2();
    }

    /**
     * setSelection
     *
     * @param i
     */
    public void stSelection(int i) {
        String text = spinArr2[i];
        setText(text);
        setTag(String.valueOf(i));
        gra.performZoomItemSelect(spinArr[i]);
    }

    /**
     * setSelection
     *
     * @param s
     */
    public void stSelection(String s) {
        int i = 0;
        for (String s0 : spinArr) {
            if (s0.contentEquals(s)) {
                stSelection(i);
                break;
            }
            i++;
        }
    }

    /**
     * getSelectedItemPosition
     *
     * @return
     */
    public int gtSelectedItemPosition() {
        return AstroTools.getInteger(getTag().toString(), 0, -1, 100000);
    }
}