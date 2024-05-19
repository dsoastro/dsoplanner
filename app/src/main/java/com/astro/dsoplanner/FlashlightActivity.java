package com.astro.dsoplanner;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class FlashlightActivity extends ParentActivity implements OnGestureListener {

    private static final String COLOR = "color";
    private static final String BRIGHTNESS = "brightness";
    private static final String STATE = "state";

    private static final String TAG = FlashlightActivity.class.getSimpleName();
    private float mCurBr;
    private int brDistance = 40; //vertical step counted as change
    private int mColor = 80;
    private int mColorStep;
    private final int mColorMask = 0xff000000;
    private View mView;
    private View mText;
    private boolean ON = false;


    @Override
    protected void onSaveInstanceState(Bundle b) {
        b.putBoolean(STATE, ON);
        b.putFloat(BRIGHTNESS, mCurBr);
        b.putInt(COLOR, mColor);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashlight);

        mView = findViewById(R.id.fl_screen);
        mText = findViewById(R.id.fl_text);

        boolean initialised = false;
        if (savedInstanceState != null) {
            try {
                ON = savedInstanceState.getBoolean(STATE);
                mCurBr = savedInstanceState.getFloat(BRIGHTNESS);
                mColor = savedInstanceState.getInt(COLOR);


                initialised = true;
            } catch (Exception e) {

            }

        }
        if (!initialised) {
            mCurBr = SettingsActivity.getBrightness();
        }

        if (SettingsActivity.getNightMode()) mColorStep = 0x00010000;
        else mColorStep = 0x00010101;

    }

    private void feedback() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) vibrator.vibrate(50);
    }

    void changeBrightness(float dy) {
        if (!ON) return;
        float br = mCurBr;
        if (br == -1) br = SettingsActivity.MIN_BRIGHTNESS; //minimal
        br += br * dy / brDistance / 10;//brStep;

        if (br < SettingsActivity.MIN_BRIGHTNESS) {
            br = SettingsActivity.MIN_BRIGHTNESS;
            feedback();
        } else if (br > 1) {
            br = 1f;
            feedback();
        }

        setBrightness(br);
    }

    void changeColor(float dx) {
        if (!ON) return;
        mColor -= dx / 30;
        if (mColor > 255) {
            mColor = 255;
            feedback();
        } else if (mColor < 1) {
            mColor = 1;
            feedback();
        }
        setColor(mColor);
    }

    private void setColor(int c) {
        mView.setBackgroundColor(mColorMask + mColorStep * c);
        ((TextView) mText).setTextColor(mColorMask + mColorStep * (c + 30));
    }

    private void setBrightness(float br) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = br;
        getWindow().setAttributes(lp);
        mCurBr = br;
        mView.setBackgroundColor(mColorMask + mColorStep * mColor);
    }

    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideMenuBtn();
        init();
    }

    //Gesture Detector (just implement OnGestureListener in the Activity)
    GestureDetector gDetector = new GestureDetector(this);

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        return gDetector.onTouchEvent(me);
    }

    public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
        if (start == null || finish == null) return false;
        float dy = start.getRawY() - finish.getRawY();
        float dx = start.getRawX() - finish.getRawX();
        //vertical flic
        float yy = Math.abs(dy);
        float xx = Math.abs(dx);
        if (yy > brDistance && yy > xx) {
            changeBrightness(dy);
            return true;
        } else if (xx > brDistance && xx > yy) {
            changeColor(dx);
            return true;
        }
        return false;
    }

    public void onLongPress(MotionEvent e) {
        if (ON) {
            mView.setBackgroundColor(0xff000000);
        } else { //turn on
            setColor(mColor);
        }
        ON = !ON;
        feedback();
    }

    private void init() {
        if (ON) {
            setBrightness(mCurBr);
            setColor(mColor);
        } else {
            mView.setBackgroundColor(0xff000000);
        }
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onDown(MotionEvent e) {
        return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
}
