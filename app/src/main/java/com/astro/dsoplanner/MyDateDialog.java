package com.astro.dsoplanner;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

//helper class for running date/time dialogs
public class MyDateDialog {
    public static final int DATE_DIALOG_ID = 0;
    public static final int TIME_DIALOG_ID = 1;
    private static final String TAG = MyDateDialog.class.getSimpleName();
    public int mYear, mMonth, mDay, mHour, mMinute;
    public DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year,
                              int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            c.set(Calendar.YEAR, mYear);
            c.set(Calendar.MONTH, mMonth);
            c.set(Calendar.DAY_OF_MONTH, mDay);
            u.update();


        }
    };//callback when the set button is pressed in dialog

    public TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

        public void onTimeSet(TimePicker view, int hourOfDay,
                              int minute) {
            mHour = hourOfDay;
            mMinute = minute;
            c.set(Calendar.HOUR_OF_DAY, mHour);
            c.set(Calendar.MINUTE, mMinute);
            u.update();


        }
    };//callback when the set button is pressed in dialog


    private Activity a;
    private Calendar c;

    public static interface Updater {
        void update();//procedure to be called when date/time changed
    }

    Updater u;

    public MyDateDialog(Activity a, Calendar c, Updater u) {
        this.a = a;
        this.c = c;
        this.u = u;
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

    }

    public void setDateTime(Calendar c) {
        this.c = c;
        updateFields();
    }

    public void startDateDialog() {
        a.showDialog(DATE_DIALOG_ID);
    }

    public void startTimeDialog() {
        a.showDialog(TIME_DIALOG_ID);
    }

    public Calendar getDateTime() {

        return c;
    }

    void setMillis(long millis) {
        c.setTimeInMillis(millis);
        updateFields();
    }

    void plusDay() {
        c.setTimeInMillis(c.getTimeInMillis() + 1000 * 3600 * 24);
        updateFields();
    }

    void plusTime(long sec) {
        c.setTimeInMillis(c.getTimeInMillis() + sec * 1000);
        updateFields();
    }

    void minusDay() {
        c.setTimeInMillis(c.getTimeInMillis() - 1000 * 3600 * 24);
        updateFields();
        Log.d(TAG, "c=" + c);
    }

    public void plusHour() {
        c.setTimeInMillis(c.getTimeInMillis() + 1000 * 3600);
        updateFields();
    }

    public void minusHour() {
        c.setTimeInMillis(c.getTimeInMillis() - 1000 * 3600);
        updateFields();
    }

    private void updateFields() {
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
    }


    public Calendar getCalendar() {
        return c;
    }

}
