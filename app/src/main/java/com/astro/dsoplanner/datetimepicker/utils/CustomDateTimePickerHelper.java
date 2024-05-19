package com.astro.dsoplanner.datetimepicker.utils;

import java.util.Calendar;

import com.astro.dsoplanner.DateTimePickerActivity;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Gallery;

public class CustomDateTimePickerHelper  {

	public interface OnEvent {
		public void call();
	}

	private static final String TAG = CustomDateTimePickerHelper.class.getSimpleName();
	
	private Context mContext;
	private Calendar c = DateTimePickerActivity.c;
	private boolean firstTime;
	private OnEvent mOnChangeCallback;

	/**
	 * Context is needed for getting month names from strings.xml, and Calendar
	 * related stuff
	 * 
	 * @param context
	 */
	public CustomDateTimePickerHelper(Context context) {
		mContext = context;
	}

	/**
	 * 
	 * @param galleryHolder
	 * @param textViewLayoutId
	 * @param textViewId
	 * @return
	 */
	public HourMinute getHourMinute(HourMinute galleryHolder, int textViewLayoutId, int textViewId) {

		int currentHour = c.get(Calendar.HOUR_OF_DAY);
		int currentMinute = c.get(Calendar.MINUTE);

		setGalleryAdapters(DateToStringArray.getHours(), galleryHolder.mHour, textViewLayoutId, textViewId);
		setGalleryAdapters(DateToStringArray.getMinutes(), galleryHolder.mMinute, textViewLayoutId, textViewId);

		galleryHolder.mHour.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				highlight(arg1);
				if(mOnChangeCallback!=null) mOnChangeCallback.call();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		galleryHolder.mMinute.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				highlight(arg1);
				if(mOnChangeCallback!=null) mOnChangeCallback.call();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		galleryHolder.mHour.setSelection(currentHour);
		galleryHolder.mMinute.setSelection(currentMinute);

		return galleryHolder;
	}

	private void highlight(View view) {
		if(view!=null) {
			view.setAlpha(1f);
		}
	}

	/**
	 * Already has some setOnItemSelectedListeners, selecting a different month
	 * or year will reset the day selection to the first day of the month.
	 * <br>
	 * Default values: current year month day
	 * 
	 * @param galleryHolder Gallery holder object
	 * @param textViewLayoutId the layout of the textView that is used by the adpaters
	 * @param textViewId the id of the textview that is used to display a single item
	 * @param offset number of years to display next to the current year. eg: if it is 2011, offset=1 will show 2010, 2011, 2012
	 * @return
	 */
	public DayMonthYear getYearMonthDay(final DayMonthYear galleryHolder, final int textViewLayoutId, final int textViewId, int offset) {
		int currentYear = c.get(Calendar.YEAR);
		int currentMonth = c.get(Calendar.MONTH);
		final int currentDay = c.get(Calendar.DAY_OF_MONTH);

		setGalleryAdapters(DateToStringArray.getDays(currentYear, currentMonth), galleryHolder.mDay, textViewLayoutId, textViewId);
		setGalleryAdapters(DateToStringArray.getMonths(mContext), galleryHolder.mMonth, textViewLayoutId, textViewId);
		setGalleryAdapters(DateToStringArray.getYears(currentYear - offset, currentYear + offset), galleryHolder.mYear, textViewLayoutId, textViewId);

		//Overriden in the caller!!!
		galleryHolder.mMonth.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                highlight(arg1);
				int currentSelectedYear = Integer.valueOf(String.valueOf(galleryHolder.mYear.getSelectedItem()));
				setGalleryAdapters(DateToStringArray.getDays(currentSelectedYear, arg2), galleryHolder.mDay, textViewLayoutId, textViewId);
				if (!firstTime) {
					//stupid hack :)
					//onitemselected will run after setselection
					galleryHolder.mDay.setSelection(currentDay-1);
					firstTime = true;
				}
				try {
					Object o = galleryHolder.mDay.getSelectedItem();
				}
				catch (Exception e){ //error in case the current date is out of bounds 
					Log.d(TAG,"Calendar day reset");
					galleryHolder.mDay.setSelection(0); //reset day to the first one
				}
				if(mOnChangeCallback!=null) mOnChangeCallback.call();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		galleryHolder.mYear.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                highlight(arg1);
				int currentSelectedYear = Integer.valueOf(String.valueOf(galleryHolder.mYear.getSelectedItem()));
				setGalleryAdapters(DateToStringArray.getDays(currentSelectedYear, galleryHolder.mMonth.getSelectedItemPosition()), galleryHolder.mDay, textViewLayoutId, textViewId);
				try {
					Object o = galleryHolder.mDay.getSelectedItem();
				}
				catch (Exception e){ //error in case the current date is out of bounds 
					Log.d(TAG,"Calendar day reset");
					galleryHolder.mDay.setSelection(0); //reset day to the first one
				}
				if(mOnChangeCallback!=null) mOnChangeCallback.call();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		galleryHolder.mDay.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                highlight(arg1);
				if(mOnChangeCallback!=null) mOnChangeCallback.call();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		//setting current values
		galleryHolder.mDay.setSelection(currentDay-1);
		galleryHolder.mMonth.setSelection(currentMonth);
		galleryHolder.mYear.setSelection(offset);

		return galleryHolder;
	}

	/**
	 * Helper for setting the adapters
	 * 
	 * @param data
	 * @param gallery
	 * @param textViewLayoutId
	 * @param textViewId
	 */
	private void setGalleryAdapters(String[] data, Gallery gallery, int textViewLayoutId, int textViewId) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, textViewLayoutId, textViewId, data);
		gallery.setAdapter(adapter);
	}

	/**
	 * 
	 * @param dayMonthYearHolder
	 * @return yyyy-mm-dd
	 */
	public String getYearMonthDay(DayMonthYear dayMonthYearHolder) {
		int month = dayMonthYearHolder.mMonth.getSelectedItemPosition();
		int day = Integer.valueOf(String.valueOf(dayMonthYearHolder.mDay.getSelectedItem()));
		StringBuilder sb = new StringBuilder().append(Integer.valueOf(String.valueOf(dayMonthYearHolder.mYear.getSelectedItem()))).append('.');
		if (month < 10) {
			sb.append('0').append(month).append('.');
		} else {
			sb.append(month).append('.');
		}
		if (day < 10) {
			sb.append('0').append(day).append('.');
		} else {
			sb.append(day).append('.');
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param hourMinuteHolder
	 * @return hh-mm
	 */
	public String getHourMinute(HourMinute hourMinuteHolder) {
		int hour = Integer.valueOf(String.valueOf(hourMinuteHolder.mHour.getSelectedItem()));
		int minute = Integer.valueOf(String.valueOf(hourMinuteHolder.mMinute.getSelectedItem()));
		StringBuilder sbFrom = new StringBuilder();
		if (hour < 10) {
			sbFrom.append('0').append(hour).append(':');
		} else {
			sbFrom.append(hour).append(':');
		}
		if (minute < 10) {
			sbFrom.append('0').append(minute);
		} else {
			sbFrom.append(minute);
		}
		return sbFrom.toString();
	}

	public void setOnChangeListener(OnEvent onEvent) {
		mOnChangeCallback = onEvent;
	}
		
}