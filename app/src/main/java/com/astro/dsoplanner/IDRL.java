package com.astro.dsoplanner;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * RelativeLayout with the width limiter attribute feature.
 * Used to simplify maintaining the Dialog size on wide Tablet screens.
 * <p>Should be later replaced with the ConstraintLayout#app:layout_constraintWidth_max.
 *
 * @author leonid
 */
public class IDRL extends RelativeLayout {

    private final int mMaxWidth;

    public IDRL(Context context) {
        super(context);
        mMaxWidth = 0;
    }

    public IDRL(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MaxWidthLinearLayout);
        mMaxWidth = a.getDimensionPixelSize(R.styleable.MaxWidthLinearLayout_maxWidth, Integer.MAX_VALUE);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (mMaxWidth > 0 && mMaxWidth < measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
