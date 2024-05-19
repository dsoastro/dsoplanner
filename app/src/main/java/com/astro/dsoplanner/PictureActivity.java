package com.astro.dsoplanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

public class PictureActivity extends ParentActivity implements OnGestureListener {

    public static final String ARRAY = "array";
    public static final String NAME = "name";

    private static final String TAG = PictureActivity.class.getSimpleName();

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
    }

    String[] picarr;
    int pos = 0;
    boolean listView = false;

    @Override
    protected void onResume() {
        super.onResume();
        hideMenuBtn();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture);

        TextView txt = (TextView) findViewById(R.id.pic_txt);

        ImageView iv = (ImageView) findViewById(R.id.my_image);
        Intent in = getIntent();
        String desname = in.getStringExtra(NAME);
        picarr = in.getStringArrayExtra(ARRAY);

        for (String s : picarr) {
            Log.d(TAG, "s=" + s);
        }

        if (picarr == null) {
            finish();
            return;
        }
        if (picarr.length == 0) {
            finish();
            return;
        }
        if (picarr.length > 1)
            listView = true;
        txt.setText(desname);


        Uri uri = Uri.parse(picarr[0]);
        setImage(uri);

        //SAND: Go back (close the activity)
        OnClickListener oclClose = new OnClickListener() {
            public void onClick(View v) {
                if (!listView)
                    finish();
                else {

                    pos++;
                    if (pos >= picarr.length)
                        pos = 0;
                    Log.d(TAG, "picture=" + picarr[pos]);
                    Uri uri = Uri.parse(picarr[pos]);
                    setImage(uri);


                }

            }
        };
        View bL = findViewById(R.id.my_image);
    }

    private void scroll(boolean up) {
        if (!listView) return;
        if (!up) {

            if (pos < picarr.length - 1)
                pos++;
            Uri uri = Uri.parse(picarr[pos]);
            setImage(uri);
        } else {

            if (pos > 0)
                pos--;
            Uri uri = Uri.parse(picarr[pos]);
            setImage(uri);
        }
    }

    public static Bitmap getBitmap(Uri uri, Context context) {
        InputStream in = null;
        Bitmap image = null;
        try {
            in = context.getContentResolver().openInputStream(uri);
            image = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            return null;
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }

        return image;
    }

    public static Bitmap getBitmap(String uri, Context context) {
        final Bitmap bitmap = getBitmap(Uri.parse(uri), context);
        return bitmap;
    }

    private void setImage(Uri uri) {
        Bitmap image = getBitmap(uri, getApplicationContext());
        if (image != null) {
            ImageView iv = (ImageView) findViewById(R.id.my_image);
            iv.setImageBitmap(image);
            if (SettingsActivity.getNightMode()) {
                ColorFilter filter = new LightingColorFilter(0xff900000, 1);
                iv.setColorFilter(filter);
            } else
                iv.setColorFilter(null);
            iv.invalidate();
        } else
            finish();
    }


    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
    }

    //Gesture Detector (just implement OnGestureListener in the Activity)
    GestureDetector gDetector = new GestureDetector(this);

    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
        gDetector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
        if (start == null || finish == null) return false;
        float dy = start.getRawY() - finish.getRawY();
        float dx = start.getRawX() - finish.getRawX();
        Log.d(TAG, "onFling, dx=" + dx + " dy=" + dy);
        Log.d(TAG, "listView=" + listView);

        boolean resting = false;
        ImageView iv = (ImageView) findViewById(R.id.my_image);
        if (iv instanceof PV) {
            PV ph = (PV) iv;
            resting = ph.isImageResting();
        }

        if (dy > Global.flickLength) { //up
            if (resting) {
                scroll(true);
            }
            return true;
        } else if (dy < -Global.flickLength) {
            if (resting)
                scroll(false);
            return true;
        } else if (dx > Global.flickLength) { //left
            if (resting) {
                super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                return true;
            }
        }
        return false;
    }

    public void onLongPress(MotionEvent e) {
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onDown(MotionEvent e) {
        return false;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
    //-----------
}
