package com.astro.dsoplanner;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.astro.dsoplanner.AstroTools.TransitRec;


public class SetDateTimeActivity extends ParentActivity implements OnGestureListener {
    private static final String TAG = SetDateTimeActivity.class.getSimpleName();
    private Button b1;
    private Button b2;
    private Calendar c1;
    private Calendar c2;


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideMenuBtn();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timerange);

        b1 = (Button) findViewById(R.id.timeBeg);
        b2 = (Button) findViewById(R.id.timeEnd);

        c1 = Calendar.getInstance();
        c2 = Calendar.getInstance();

        //Begin Time
        Calendar bt = Calendar.getInstance();
        Long start = SettingsActivity.getSharedPreferences(this).getLong(Constants.START_OBSERVATION_TIME, 0);
        bt.setTimeInMillis(start);
        updateField(1, bt);
        b1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SettingsActivity.putSharedPreferences(Constants.DTP_DISPLAY_MODE, DateTimePickerActivity.BOTH, SetDateTimeActivity.this);
                SettingsActivity.putSharedPreferences(Constants.DTP_TIME, c1.getTimeInMillis(), SetDateTimeActivity.this);

                Intent i = new Intent(SetDateTimeActivity.this, DateTimePickerActivity.class);
                i.putExtra(Constants.DTP_RT, false);
                startActivityForResult(i, 1);
            }

        });
        //Now
        Button b11 = (Button) findViewById(R.id.btbNow);
        b11.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                c1 = Calendar.getInstance(); //Current time
                updateField(1, c1);
            }

        });
        //Sunset
        Button b12 = (Button) findViewById(R.id.btbSet);
        b12.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TransitRec tr = AstroTools.getRiseSetting(Global.sun, c1, AstroTools.hSun);
                if (tr.tSetting != null) {
                    c1.setTime(tr.tSetting.getTime());
                    updateField(1, c1);
                }
            }

        });
        //Astro twilight Ends
        Button b13 = (Button) findViewById(R.id.btbTwi);
        b13.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TransitRec tr = AstroTools.getRiseSetting(Global.sun, c1, AstroTools.hAstro);
                if (tr.tSetting != null) {
                    c1.setTime(tr.tSetting.getTime());
                    updateField(1, c1);
                }
            }

        });

        //End Time
        Calendar et = Calendar.getInstance();
        Long end = SettingsActivity.getSharedPreferences(this).getLong(Constants.END_OBSERVATION_TIME, 0);
        et.setTimeInMillis(end);
        updateField(2, et);
        b2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SettingsActivity.putSharedPreferences(Constants.DTP_DISPLAY_MODE, DateTimePickerActivity.TIME, SetDateTimeActivity.this);
                SettingsActivity.putSharedPreferences(Constants.DTP_TIME, c2.getTimeInMillis(), SetDateTimeActivity.this);
                Intent i = new Intent(SetDateTimeActivity.this, DateTimePickerActivity.class);
                i.putExtra(Constants.DTP_RT, false);
                startActivityForResult(i, 2);
            }

        });

        //!!!NOTE: the day must be the following sunset, 
        // however due to the subtle difference it could be left alone
        // may be corrected later

        //Sunrise
        Button b21 = (Button) findViewById(R.id.bteRise);
        b21.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TransitRec tr = AstroTools.getRiseSetting(Global.sun, c2, AstroTools.hSun);
                if (tr.tRise != null) {
                    c2.setTime(tr.tRise.getTime());
                    updateField(2, c2);
                }
            }
        });
        //Astro twilight
        Button b22 = (Button) findViewById(R.id.bteTwi);
        b22.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TransitRec tr = AstroTools.getRiseSetting(Global.sun, c2, AstroTools.hAstro);
                if (tr.tRise != null) {
                    c2.setTime(tr.tRise.getTime());
                    updateField(2, c2);
                }
            }
        });

        //Done Button, same as back -------------------------------
        Button b3 = (Button) findViewById(R.id.tbdone);
        b3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setTimeRange();
                finish();
            }

        });
    }

    @Override
    public void onPause() {
        super.onPause();
        setTimeRange();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (c1 == null) return;
            long time = data.getLongExtra(Constants.DATE_TIME_PICKER_MILLIS, Calendar.getInstance().getTimeInMillis());
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(time);
            updateField(requestCode, c);
        }
    }

    private void updateField(int requestCode, Calendar c) {
        Log.d(TAG, "update field");
        Calendar gc = AstroTools.getDefaultTime(this);
        if (c != null) gc = c;
        gc.set(Calendar.SECOND, 0);

        if (requestCode == 1) {//from DateTimePicker for startTime
            c1.setTime(gc.getTime());
            b1.setText(c1.getTime().toString());
        } else if (requestCode == 2) {//from DateTimePicker for endTime
            c2.setTime(gc.getTime());
        }
        //sync c2 day with c1
        c2.set(Calendar.YEAR, c1.get(Calendar.YEAR));
        c2.set(Calendar.MONTH, c1.get(Calendar.MONTH));
        c2.set(Calendar.DAY_OF_MONTH, c1.get(Calendar.DAY_OF_MONTH));

        if (c1.after(c2)) { //usual situation, it's next day
            c2.add(Calendar.DAY_OF_MONTH, 1);
        }
        b2.setText(c2.getTime().toString()); //Updating (will not change if it was ok anyway)
        Log.d(TAG, "c1=" + c1);
        Log.d(TAG, "c2=" + c2);
    }

    protected void setTimeRange() {
        SettingsActivity.putSharedPreferences(Constants.START_OBSERVATION_TIME, c1.getTimeInMillis(), this);
        SettingsActivity.putSharedPreferences(Constants.END_OBSERVATION_TIME, c2.getTimeInMillis(), this);
        SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, this);
    }

    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
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
        if (dy > Global.flickLength) { //up
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU));
            return true;
        } else if (dx > Global.flickLength) { //left
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
            return true;
        }
        return false;
    }

    public void onLongPress(MotionEvent e) {
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
    //-----------
}
