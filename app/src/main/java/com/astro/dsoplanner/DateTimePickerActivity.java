package com.astro.dsoplanner;

import java.util.Calendar;
import java.util.Date;

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
import android.widget.Gallery;
import android.widget.TextView;

import com.astro.dsoplanner.datetimepicker.utils.CustomDateTimePickerHelper;
import com.astro.dsoplanner.datetimepicker.utils.DayMonthYear;
import com.astro.dsoplanner.datetimepicker.utils.HourMinute;

public class DateTimePickerActivity extends ParentActivity implements OnGestureListener {
	private static final String TAG = DateTimePickerActivity.class.getSimpleName();
	CustomDateTimePickerHelper CustomDateGallery;
	DayMonthYear galleryHolderDayMonthYear;
	HourMinute galleryHolderHourMinute;
	TextView datetimeBox;
	public static Calendar c;

	public static final int BOTH=1;
	public static final int DATE=2;
	public static final int TIME=3;
	
	private static int mDisplayMode = BOTH;
	int galleryItemLayout;
	
	@Override
	protected void onResume(){
		super.onResume();
		hideMenuBtn();
	}
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		galleryItemLayout = R.layout.galleryitem;
		setContentView(R.layout.datetimepicker);
		
		long time= SettingsActivity.getSharedPreferences(this).getLong(Constants.DTP_TIME, 0);
		c=Calendar.getInstance();
		c.setTimeInMillis(time);
		mDisplayMode= SettingsActivity.getSharedPreferences(this).getInt(Constants.DTP_DISPLAY_MODE, BOTH);

		datetimeBox = (TextView)findViewById(R.id.dateTimePick);
		findViewById(R.id.DTPset).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boolean rt=getIntent().getBooleanExtra(Constants.DTP_RT, true);//rt button is active
				boolean auto= SettingsActivity.isAutoTimeUpdating();
				if(rt)
					SettingsActivity.setAutoTimeUpdating(false); //auto time disabled only if tr button shown!
				updateSky(true);
				Intent i=new Intent();
				i.putExtra(Constants.DATE_TIME_PICKER_MILLIS, c.getTimeInMillis());
				DateTimePickerActivity.this.setResult(RESULT_OK,i);
				if(auto&&rt)//show message that real time off
					updateAutoBtn(true,false);				
				finish();
			}
		});
		findViewById(R.id.DTPcancel).setOnClickListener(new OnClickListener() {
			public void onClick(View v) { finish(); }
		});
		findViewById(R.id.DTPnow).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				c = Calendar.getInstance();
				initGalleries();
			}
		});

		boolean rt=getIntent().getBooleanExtra(Constants.DTP_RT, true);
		if(!rt){
			findViewById(R.id.DTPend).setVisibility(View.GONE);
		}
		//rt btn in fact
		findViewById(R.id.DTPend).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				boolean auto= SettingsActivity.isAutoTimeUpdating();
				SettingsActivity.setAutoTimeUpdating(!auto);
				
				//rt on
				if(!auto){
					c = Calendar.getInstance();
					initGalleries();		
					updateSky(false);
					Intent i=new Intent();
					i.putExtra(Constants.DATE_TIME_PICKER_MILLIS, c.getTimeInMillis());
					DateTimePickerActivity.this.setResult(RESULT_OK,i);
				}
				updateAutoBtn(true,false);
				finish();
			}
		});
		
		updateAutoBtn(false,true);
		initGalleries();
	}
	
    private void updateAutoBtn(boolean showToast,boolean setBtn){
    	Button autobtn=(Button)findViewById(R.id.DTPend);
    	boolean autostatus= SettingsActivity.isAutoTimeUpdating();
		if(autostatus){
			if(setBtn)autobtn.setText(getString(R.string.rt_off));
			if(showToast)InputDialog.toast(getString(R.string.realtime_mode_on), this).show();
		}
		else{
			if(setBtn)autobtn.setText(getString(R.string.rt_on));
			if(showToast)InputDialog.toast(getString(R.string.realtime_mode_off), this).show();
		}
    }
	private void initGalleries() {
		CustomDateTimePickerHelper customDateTimePickerHelper = 
				new CustomDateTimePickerHelper(getApplicationContext());
		
		// year month date holder object
		// you could also use separate Gallery objects if you like
		galleryHolderDayMonthYear = new DayMonthYear();
		galleryHolderDayMonthYear.mDay = (Gallery) findViewById(R.id.GalleryDay);
		galleryHolderDayMonthYear.mMonth = (Gallery) findViewById(R.id.GalleryMonth);
		galleryHolderDayMonthYear.mYear = (Gallery) findViewById(R.id.GalleryYear);


		// hour minute
		galleryHolderHourMinute = new HourMinute();
		galleryHolderHourMinute.mHour = (Gallery) findViewById(R.id.GalleryHour);
		galleryHolderHourMinute.mMinute = (Gallery) findViewById(R.id.GalleryMinute);
		
		if (nightMode || SettingsActivity.getDarkSkin()){
			int background = nightMode?0xff3f0000:0xff1f1f1f;
			galleryHolderDayMonthYear.mDay.setBackgroundColor(background);
			galleryHolderDayMonthYear.mMonth.setBackgroundColor(background);
			galleryHolderDayMonthYear.mYear.setBackgroundColor(background);

			galleryHolderHourMinute.mMinute.setBackgroundColor(background);
			galleryHolderHourMinute.mHour.setBackgroundColor(background);
			galleryItemLayout = nightMode?R.layout.galleryitemred:R.layout.galeryitemonyx;
		}

		galleryHolderDayMonthYear = customDateTimePickerHelper.getYearMonthDay(galleryHolderDayMonthYear, galleryItemLayout, R.id.datepickergallery_textView, 50);
		galleryHolderHourMinute = customDateTimePickerHelper.getHourMinute(galleryHolderHourMinute, galleryItemLayout, R.id.datepickergallery_textView);

		//To update time string each time
		customDateTimePickerHelper.setOnChangeListener(new CustomDateTimePickerHelper.OnEvent() {
			public void call() {
				updateDateTimeBox();
			}
		});

		//Processing various modes hiding elements
		switch(mDisplayMode){
		case BOTH:
			galleryHolderDayMonthYear.mDay.setVisibility(View.VISIBLE);
			galleryHolderDayMonthYear.mMonth.setVisibility(View.VISIBLE);
			galleryHolderDayMonthYear.mYear.setVisibility(View.VISIBLE);
			galleryHolderHourMinute.mHour.setVisibility(View.VISIBLE);
			galleryHolderHourMinute.mMinute.setVisibility(View.VISIBLE);
			break;
		case DATE:
			galleryHolderDayMonthYear.mDay.setVisibility(View.VISIBLE);
			galleryHolderDayMonthYear.mMonth.setVisibility(View.VISIBLE);
			galleryHolderDayMonthYear.mYear.setVisibility(View.VISIBLE);
			galleryHolderHourMinute.mHour.setVisibility(View.GONE);
			galleryHolderHourMinute.mMinute.setVisibility(View.GONE);
			break;
		case TIME: //TIME ONLY
			galleryHolderDayMonthYear.mDay.setVisibility(View.GONE);
			galleryHolderDayMonthYear.mMonth.setVisibility(View.GONE);
			galleryHolderDayMonthYear.mYear.setVisibility(View.GONE);
			galleryHolderHourMinute.mHour.setVisibility(View.VISIBLE);
			galleryHolderHourMinute.mMinute.setVisibility(View.VISIBLE);
			break;
		}

	}
	//Update preview text string content
	protected void updateDateTimeBox() {
		updateCalendar(false);
		Date d = c.getTime();
		datetimeBox.setText(d.toString());
	}
	
	protected void updateSky(boolean setZeroSeconds) {
		updateCalendar(setZeroSeconds);
	}
	//grab date and time from picker controls to c
	private void updateCalendar(boolean setZeroSeconds) {
		Log.d(TAG,"update calendar "+setZeroSeconds);
		c.set(Calendar.YEAR, Integer.valueOf(String.valueOf(galleryHolderDayMonthYear.mYear.getSelectedItem())));
		c.set(Calendar.MONTH, galleryHolderDayMonthYear.mMonth.getSelectedItemPosition());
		c.set(Calendar.DAY_OF_MONTH, Integer.valueOf(String.valueOf(galleryHolderDayMonthYear.mDay.getSelectedItem())));
		c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(String.valueOf(galleryHolderHourMinute.mHour.getSelectedItem())));
		c.set(Calendar.MINUTE, Integer.valueOf(String.valueOf(galleryHolderHourMinute.mMinute.getSelectedItem())));
		if(setZeroSeconds){
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
		}
		SettingsActivity.putSharedPreferences(Constants.DTP_TIME, c.getTimeInMillis(), this);
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
  	public synchronized boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
  		if(start==null || finish==null) return false;
  		float dy = start.getRawY() - finish.getRawY();
  		float dx = start.getRawX() - finish.getRawX();
  		if (dy>Global.flickLength){ //up
  			super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU));
  			super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU));
  			return true;
  		}
  		else if(dx > Global.flickLength) { //left
  			super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
  			super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
  			return true;
  		}
  		return false;
  	}
  	public void onLongPress(MotionEvent e) {}
  	public void onShowPress(MotionEvent e) {}
  	public boolean onDown(MotionEvent e) {return true;}
  	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {return false;}
  	public boolean onSingleTapUp(MotionEvent e) {return false;}
}
