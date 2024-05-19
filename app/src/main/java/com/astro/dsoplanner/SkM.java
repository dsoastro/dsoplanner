package com.astro.dsoplanner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * former SkinMap
 * Displays an image of HTML imageMap like drawing on the screen
 * and uses another image - color template to trigger different events by color number.
 * <p>
 * The XML parameters are
 * = none, as everything is set in the code
 */
public class SkM extends View {

    private static final String TAG = SkM.class.getSimpleName();
    private static final int MAX_ATTEMPTS = 3;
    private static int loadCount = MAX_ATTEMPTS; //number of attempts to load large image
    private int imgFront = 0; //user facing image resource
    private int imgMap = 0; //resource of the mapping image with colored ontouch zones
    private Bitmap mFront;
    private Bitmap bMap;
    private List<Touchable> touchables = new ArrayList<Touchable>();

    private int cMask = 0xffffff; //color mask for 24bit PNG

    public interface OnTouchListener {
        public void onTouch(Touchable t);
    }

    public class Touchable {
        public int color;
        public OnTouchListener ontouch;
        public int x;
        public int y;

        public Touchable(int acolor, OnTouchListener on) {
            color = acolor;
            ontouch = on;
            x = -1;
            y = -1;
        }
    }

    //Image to show on screen
    public void setImgFront(int resId) throws Exception {
        imgFront = resId;
        try {
            mFront = BitmapFactory.decodeResource(getResources(), imgFront);
        } catch (Throwable t) {
            //Most likely out of memory problem.
            Log.d(TAG, "Image out of memory retrying.");
            //Try again in the attempt to recover
            if (--loadCount > 0) {
                Thread.sleep(200);
                setImgFront(imgFront);
            } else throw (new Exception("nomemory"));

        }
        loadCount = MAX_ATTEMPTS;
    }

    //Bitmap to define hot zones on the Map
    public void setImgMap(int i) {
        imgMap = i;
        bMap = ((BitmapDrawable) getResources().getDrawable(imgMap)).getBitmap();
        //bMap = BitmapFactory.decodeResource(getResources(), imgMap);
    }

    //Add hot zone color and processor
    public void addTouchListener(int color, OnTouchListener on) {
        touchables.add(new Touchable(color, on));
    }

    public boolean onTouchEvent(MotionEvent event) {
        float X = event.getX(); //screen coord
        float Y = event.getY(); //screen coord

        int h = bMap.getHeight(); //bitmap coord
        int w = bMap.getWidth();  //bitmap coord
        int H = this.getHeight(); //screen coord
        int W = this.getWidth();  //screen coord
        int x = (int) (X / W * w);
        int y = (int) (Y / H * h);

        int pixel = bMap.getPixel(x, y) & cMask;

        for (Touchable t : touchables) {
            if (t.color == pixel) {
                t.x = x;
                t.y = y;
                t.ontouch.onTouch(t);
                //perhaps a visual effect on the map here
                break;
            }
        }

        return false;
    }


    public SkM(Context context) {
        super(context);
        init(null, 0);
    }

    public SkM(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SkM(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Scale bitmap to fit screen
        final Rect rsrc = new Rect(0, 0, mFront.getWidth(), mFront.getHeight());
        final Rect rdst = new Rect(0, 0, getWidth(), getHeight());
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawBitmap(mFront, rsrc, rdst, paint);
    }
}

