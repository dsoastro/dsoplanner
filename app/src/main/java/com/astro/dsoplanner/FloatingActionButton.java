package com.astro.dsoplanner;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

public class FloatingActionButton extends View {
    public static final int FLOATING_BUTTON_SIZE = 60;
    final static OvershootInterpolator overshootInterpolator = new OvershootInterpolator();
    final static AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();
    private static final String TAG = FloatingActionButton.class.getSimpleName();

    Context context;
    Paint mButtonPaint;
    Paint mDrawablePaint;
    Bitmap mBitmap;
    boolean mHidden = false;
    float scale;
    Rect dst = new Rect(0, 0, 0, 0);

    public FloatingActionButton(Context context) {
        super(context);
        this.context = context;
        init(Color.WHITE);
    }

    public void setFloatingActionButtonColor(int FloatingActionButtonColor) {
        init(FloatingActionButtonColor);
    }

    public void setFloatingActionButtonDrawable(Drawable FloatingActionButtonDrawable) {
        mBitmap = ((BitmapDrawable) FloatingActionButtonDrawable).getBitmap();
        invalidate();
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void init(int FloatingActionButtonColor) {
        setWillNotDraw(false);
        mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mButtonPaint.setColor(FloatingActionButtonColor);
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint.setShadowLayer(10.0f, 0.0f, 3.5f, Color.argb(100, 0, 0, 0));
        mDrawablePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setClickable(true);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (float) (getWidth() / 2.6), mButtonPaint);

        float w = Builder.convertToPixels(mBitmap.getWidth(), scale) / 1.2f;
        float h = Builder.convertToPixels(mBitmap.getHeight(), scale) / 1.2f;

        float left = getWidth() / 4;
        float top = getHeight() / 4;
        float right = getWidth() * 3 / 4;
        float bottom = getHeight() * 3 / 4;
        dst.set((int) left, (int) top, (int) right, (int) bottom);
        canvas.drawBitmap(mBitmap, null, dst, mDrawablePaint);
    }

    public void hideFloatingActionButton() {
        if (!mHidden) {
            mHidden = true;
            this.setVisibility(View.INVISIBLE);
        }
    }

    public void showFloatingActionButton() {
        if (mHidden) {
            mHidden = false;
        }
    }

    public static class Builder {

        private FrameLayout.LayoutParams params;
        private final Activity activity;
        int gravity = Gravity.BOTTOM | Gravity.RIGHT; // default bottom right
        Drawable drawable;
        int color = Color.WHITE;
        int size = 0;
        float scale = 0;

        public Builder(Activity context) {
            scale = context.getResources().getDisplayMetrics().density;
            size = convertToPixels(FLOATING_BUTTON_SIZE, scale); // default size is 72dp by 72dp
            params = new FrameLayout.LayoutParams(size, size);
            params.gravity = gravity;

            this.activity = context;
        }

        /**
         * Sets the gravity for the FAB
         */
        public Builder withGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        /**
         * Sets the margins for the FAB in dp
         */
        public Builder withMargins(int left, int top, int right, int bottom) {
            params.setMargins(left, top, right, bottom);
            return this;
        }

        /**
         * Sets the FAB drawable
         */
        public Builder withDrawable(final Drawable drawable) {
            this.drawable = drawable;
            return this;
        }

        /**
         * Sets the FAB color
         */
        public Builder withButtonColor(final int color) {
            this.color = color;
            return this;
        }

        public FloatingActionButton create() {
            final FloatingActionButton button = new FloatingActionButton(activity);
            button.setFloatingActionButtonColor(this.color);
            button.setFloatingActionButtonDrawable(this.drawable);
            button.setScale(scale);
            params.gravity = this.gravity;
            ViewGroup root = (ViewGroup) activity.findViewById(android.R.id.content);
            root.addView(button, params);

            return button;
        }

        // The calculation (value * scale + 0.5f) is a widely used to convert to dps to pixel units
        // based on density scale
        // see developer.android.com (Supporting Multiple Screen Sizes)
        public static int convertToPixels(int dp, float scale) {
            return (int) (dp * scale + 0.5f);
        }
    }
}

