package com.astro.dsoplanner;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Text view that auto adjusts text size to fit within the view. If the text
 * size equals the minimum text size and still does not fit, append with an
 * ellipsis.
 * <p>
 * 2011-10-29 changes by Alan Jay Weiner
 * * change to fit both vertically and horizontally
 * * test mTextSize for 0 in resizeText() to fix exception in Layout Editor
 *
 * @author Chase Colburn
 * @since Apr 4, 2011
 * <p>
 * former AutoResizeTextView
 */
public class Artv extends TextView {

    // Minimum text size for this text view
    public static final float MIN_TEXT_SIZE = 4;

    // Interface for resize notifications
    public interface OnTextResizeListener {
        public void onTextResize(TextView textView, float oldSize, float newSize);
    }

    // Off screen canvas for text size rendering
    private static final Canvas sTextResizeCanvas = new Canvas();

    // Our ellipse string

    private static final String mEllipsis = "...";

    // Registered resize listener
    private OnTextResizeListener mTextResizeListener;

    // Flag for text and/or size changes to force a resize
    private boolean mNeedsResize = false;

    // Text size that is set from code. This acts as a starting point for
    // resizing
    private float mTextSize;

    // Temporary upper bounds on the starting text size
    private float mMaxTextSize = 0;

    // Lower bounds for text size
    private float mMinTextSize = MIN_TEXT_SIZE;

    // Text view line spacing multiplier
    private float mSpacingMult = 1.0f;

    // Text view additional line spacing
    private float mSpacingAdd = 0.0f;

    // Add ellipsis to text that overflows at the smallest text size
    private boolean mAddEllipsis = false;//true;


    // Default constructor override
    public Artv(Context context) {
        this(context, null);
    }


    // Default constructor when inflating from XML file
    public Artv(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    // Default constructor override
    public Artv(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTextSize = getTextSize();
    }


    /**
     * When text changes, set the force resize flag to true and reset the text
     * size.
     */
    @Override
    protected void onTextChanged(final CharSequence text, final int start,
                                 final int before, final int after) {
        mNeedsResize = true;
        // Since this view may be reused, it is good to reset the text size
        resetTextSize();
    }


    /**
     * If the text view size changed, set the force resize flag to true
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            mNeedsResize = true;
        }
    }


    /**
     * Register listener to receive resize notifications
     * former setOnResizeListener
     *
     * @param listener
     */
    public void stOnResizeListener(OnTextResizeListener listener) {
        mTextResizeListener = listener;
    }


    /**
     * Override the set text size to update our internal reference values
     */
    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        mTextSize = getTextSize();
    }


    /**
     * Override the set text size to update our internal reference values
     */
    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        mTextSize = getTextSize();
    }


    /**
     * Override the set line spacing to update our internal reference values
     */
    @Override
    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        mSpacingMult = mult;
        mSpacingAdd = add;
    }


    /**
     * Set the upper text size limit and invalidate the view
     * former setMaxTextSize
     *
     * @param maxTextSize
     */
    public void stMaxTextSize(float maxTextSize) {
        mMaxTextSize = maxTextSize;
        requestLayout();
        invalidate();
    }


    /**
     * Return upper text size limit
     * former getMaxTextSize
     *
     * @return
     */
    public float gtMaxTextSize() {
        return mMaxTextSize;
    }


    /**
     * Set the lower text size limit and invalidate the view
     * former setMinTextSize
     *
     * @param minTextSize
     */
    public void stMinTextSize(float minTextSize) {
        mMinTextSize = minTextSize;
        requestLayout();
        invalidate();
    }


    /**
     * Return lower text size limit
     * former getMinTextSize
     *
     * @return
     */
    public float gtMinTextSize() {
        return mMinTextSize;
    }


    /**
     * Set flag to add ellipsis to text that overflows at the smallest text size
     * former setAddEllipsis
     *
     * @param addEllipsis
     */
    public void stAddEllipsis(boolean addEllipsis) {
        mAddEllipsis = addEllipsis;
    }


    /**
     * Return flag to add ellipsis to text that overflows at the smallest text
     * size
     * former getAddEllipsis
     *
     * @return
     */
    public boolean gtAddEllipsis() {
        return mAddEllipsis;
    }


    /**
     * Reset the text to the original size
     */
    public void resetTextSize() {
        mMaxTextSize = mTextSize;
    }


    /**
     * Resize text after measuring
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed || mNeedsResize) {
            int widthLimit = (right - left) - getCompoundPaddingLeft()
                    - getCompoundPaddingRight();
            int heightLimit = (bottom - top) - getCompoundPaddingBottom()
                    - getCompoundPaddingTop();
            resizeText(widthLimit, heightLimit);
        }
        //SAND TEST (OK) super.onLayout(changed, left, top, right, bottom);
    }


    /**
     * Resize the text size with default width and height
     */
    public void resizeText() {
        int heightLimit = getHeight() - getPaddingBottom() - getPaddingTop();
        int widthLimit = getWidth() - getPaddingLeft() - getPaddingRight();
        resizeText(widthLimit, heightLimit);
    }


    /**
     * Resize the text size with specified width and height
     *
     * @param width
     * @param height
     */
    public void resizeText(int width, int height) {
        CharSequence text = getText();
        // Do not resize if the view does not have dimensions or there is no
        // text
        // or if mTextSize has not been initialized
        if (text == null || text.length() == 0 || height <= 0 || width <= 0
                || mTextSize == 0) {
            return;
        }

        // Get the text view's paint object
        TextPaint textPaint = getPaint();

        // Store the current text size
        float oldTextSize = textPaint.getTextSize();

        // If there is a max text size set, use the lesser of that and the
        // default text size
        float targetTextSize = mMaxTextSize > 0 ? Math.min(mTextSize, mMaxTextSize)
                : mTextSize;

        // Get the required text height
        int textHeight = getTextHeight(text, textPaint, width, targetTextSize);
        int textWidth = getTextWidth(text, textPaint, width, targetTextSize);

        // Until we either fit within our text view or we had reached our min
        // text size, incrementally try smaller sizes
        while (((textHeight > height) || (textWidth > width))
                && targetTextSize > mMinTextSize) {
            targetTextSize -= 1;//Math.max(targetTextSize - 1, mMinTextSize);
            textHeight = getTextHeight(text, textPaint, width, targetTextSize);
            textWidth = getTextWidth(text, textPaint, width, targetTextSize);
        }

        // If we had reached our minimum text size and still don't fit, append
        // an ellipsis
        if (mAddEllipsis && targetTextSize == mMinTextSize && textHeight > height) {
            // Draw using a static layout
            StaticLayout layout = new StaticLayout(text, textPaint, width,
                    Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, false);
            layout.draw(sTextResizeCanvas);
            int lastLine = layout.getLineForVertical(height) - 1;
            int start = layout.getLineStart(lastLine);
            int end = layout.getLineEnd(lastLine);
            float lineWidth = layout.getLineWidth(lastLine);
            float ellipseWidth = textPaint.measureText(mEllipsis);

            // Trim characters off until we have enough room to draw the
            // ellipsis
            while (width < lineWidth + ellipseWidth) {
                lineWidth = textPaint.measureText(text.subSequence(start, --end + 1)
                        .toString());
            }
            setText(text.subSequence(0, end) + mEllipsis);

        }

        // Some devices try to auto adjust line spacing, so force default line
        // spacing
        // and invalidate the layout as a side effect
        textPaint.setTextSize(targetTextSize);
        //////setLineSpacing(mSpacingAdd, mSpacingMult);

        // Notify the listener if registered
        if (mTextResizeListener != null) {
            mTextResizeListener.onTextResize(this, oldTextSize, targetTextSize);
        }

        // Reset force resize flag
        mNeedsResize = false;
    }


    // Set the text size of the text paint object and use a static layout to
    // render text off screen before measuring
    private int getTextHeight(CharSequence source, TextPaint paint, int width,
                              float textSize) {
        // Update the text paint object
        paint.setTextSize(textSize);
        // Draw using a static layout
        StaticLayout layout = new StaticLayout(source, paint, width,
                Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, true);
        layout.draw(sTextResizeCanvas);
        return layout.getHeight();
    }


    // Set the text size of the text paint object and use a static layout to
    // render text off screen before measuring
    private int getTextWidth(CharSequence source, TextPaint paint, int width,
                             float textSize) {
        // Update the text paint object
        paint.setTextSize(textSize);
        // Draw using a static layout
        StaticLayout layout = new StaticLayout(source, paint, width,
                Alignment.ALIGN_NORMAL, mSpacingMult, mSpacingAdd, true);
        layout.draw(sTextResizeCanvas);
        if (layout.getLineCount() > 1)
            return width + 10; //SAND trick - we need to fit in only 1 line!
        return width; //layout.getWidth();
    }

}