package com.astro.dsoplanner;

import static java.lang.Math.sqrt;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;


/**
 * former PhotoView
 *
 * @author leonid
 */
public class PV extends ImageView {
    private static final String TAG = PV.class.getSimpleName();
    int width;
    int height;
    int x, y;
    Context context;
    Bitmap image;
    Rectangle rec;
    Paint paint;

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
        x = width / 2;
        y = height / 2;
        Log.d(TAG, "w=" + w + "h=" + h);
        rec.setHeight(h);
        rec.setWidth(w);
        rec.init();

    }

    private void init(Context context) {
        this.context = context;
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public boolean isImageResting() {
        return rec.isResting();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        image = bm;
        rec = new Rectangle(image);
        rec.setHeight(height);
        rec.setWidth(width);
        rec.init();
        paint = new Paint();
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
        paint.setColorFilter(filter);
    }

    public PV(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public PV(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PV(Context context) {
        super(context);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (image != null) {
            rec.draw(canvas);
        }
    }

    float mTouchStartX;
    float mTouchStartY;
    int mFlick;
    int modeZoom;
    private float oldDist;
    PointF mid = new PointF();
    float scale = 1;
    float scaleprev = 1;
    int prevPointerId = -1;


    class Rectangle {
        Bitmap image;
        int width;//display
        int height;
        int constrain;
        float initscale;
        float scale;
        float x, y;//center coord

        int w;//image
        int h;

        private static final int WIDTH_CONSTRAINED = 1;//width always along x
        private static final int HEIGHT_CONSTRAINED = 2;

        public Rectangle(Bitmap image) {
            super();
            this.image = image;
        }

        public void setWidth(int w) {
            width = w;
        }

        public void setHeight(int h) {
            height = h;
        }

        public void init() {
            w = image.getWidth();
            h = image.getHeight();

            if (((float) w / h) > ((float) width / height)) {
                constrain = WIDTH_CONSTRAINED;
                initscale = (float) (width) / w;
                scale = initscale;


            } else {
                constrain = HEIGHT_CONSTRAINED;
                initscale = (float) (height) / h;
                scale = initscale;
            }
            x = width / 2;
            y = height / 2;
        }

        public void moveDx(float dx) {
            if (constrain == WIDTH_CONSTRAINED) {
                if (dx > 0) {
                    x = x + dx;
                    float xl = x - scale * w / 2;
                    if (xl > 0) {//move back
                        x = scale * w / 2;
                    }
                }
                if (dx < 0) {
                    x = x + dx;
                    float xr = x + scale * w / 2;
                    if (xr < width) {
                        x = width - scale * w / 2;
                    }
                }
            } else {
                float xtmp = x + dx;
                float xr = xtmp + scale * w / 2;
                float xl = xtmp - scale * w / 2;

                float xrlimit = width / 2f + scale * w / 2;//x right
                float xllimit = width / 2f - scale * w / 2;//x left
                if (dx > 0) {
                    if (xl > xllimit && xl > 0) return;
                }
                if (dx < 0) {
                    if (xr < xrlimit && xr < width) return;
                }


                x = xtmp;
            }
        }

        public boolean isResting() {
            final float e = 5f;

            if (Math.abs(scale / initscale - 1) < 0.01)
                return true;
            else
                return false;


        }

        public void moveDy(float dy) {
            if (constrain == WIDTH_CONSTRAINED) {
                float ytmp = y + dy;
                float yh = ytmp + scale * h / 2;
                float yl = ytmp - scale * h / 2;

                float yhlimit = height / 2f + scale * h / 2;//y high
                float yllimit = height / 2f - scale * h / 2;//y low
                if (dy > 0) {
                    if (yl > yllimit && yl > 0) return;
                }
                if (dy < 0) {
                    if (yh < yhlimit && yh < height) return;
                }


                y = ytmp;
            } else {
                if (dy > 0) {
                    y = y + dy;
                    float yl = y - scale * h / 2;
                    if (yl > 0) {//move back
                        y = scale * h / 2;
                    }
                }
                if (dy < 0) {
                    y = y + dy;
                    float yh = y + scale * h / 2;
                    if (yh < height) {
                        y = height - scale * h / 2;
                    }
                }
            }
        }

        boolean zoom = false;

        public void setZoom(boolean zoom) {
            this.zoom = zoom;
        }

        public void setZoom(float scal, PointF mid) {
            float midx = mid.x;
            float midy = mid.y;

            float sc = this.scale / initscale;

            float xatsc1 = (x - midx) / sc + midx;
            x = midx + scal * (xatsc1 - midx);
            float yatsc1 = (y - midy) / sc + midy;
            y = midy + scal * (yatsc1 - midy);
            if (constrain == WIDTH_CONSTRAINED) {
                float xl = x - scal * initscale * w / 2;
                float xr = x + scal * initscale * w / 2;
                if (xl > 0) {
                    x = x - xl;
                } else if (xr < width) {
                    x = x + (width - xr);
                }
            } else {
                float yl = y - scal * initscale * h / 2;
                float yh = y + scal * initscale * h / 2;
                if (yl > 0) {
                    y = y - yl;
                } else if (yh < height) {
                    y = y + (height - yh);
                }
            }


            this.scale = this.initscale * scal;

        }

        public void draw(Canvas canvas) {
            RectF r = new RectF(x - scale * w / 2, y - scale * h / 2, x + scale * w / 2, y + scale * h / 2);
            canvas.drawBitmap(image, null, r, paint);
        }

    }

    private void zoomOff(MotionEvent event) {
        if (modeZoom > 0) {
            float x = event.getX();
            float y = event.getY();
            mTouchStartX = x;
            mTouchStartY = y;
            modeZoom = 0;
            scaleprev = scale;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (prevPointerId == -1) {
            prevPointerId = event.getPointerId(0);
        }


        float x = event.getX();
        float y = event.getY();

        if (prevPointerId != event.getPointerId(0)) {
            mTouchStartX = x;
            mTouchStartY = y;
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                mTouchStartX = x;//mTouchCurrX;
                mTouchStartY = y;//mTouchCurrY;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                zoomOff(event);

                break;
            case MotionEvent.ACTION_MOVE:
                if (modeZoom > 0) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        scale = newDist / oldDist;
                        if (scale < 1) scale = 1;
                        rec.setZoom(scale, mid);
                        //	mMoved = true;
                    }

                } else { //not zoom mode
                    float dx = x - mTouchStartX;
                    float dy = y - mTouchStartY;
                    if ((dx * dx < 100 && dy * dy < 49)) return true; //cut noise

                    rec.moveDx(dx);
                    rec.moveDy(dy);

                    //Pan

                    mTouchStartX = x;
                    mTouchStartY = y;


                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                zoomOff(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN: //second finger
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    modeZoom = 1;
                    oldDist = oldDist / scaleprev;
                    midPoint(mid, event);
                }
                break;
        }
        return true;
    }

    //SAND:for multitouch
    private float spacing(MotionEvent event) {
        try {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) sqrt(x * x + y * y); //was FloatMath. (more efficient)
        } catch (Exception e) {
            return 0;
        }
    }

    private void midPoint(PointF point, MotionEvent event) { //was PointF
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
}
