package com.astro.dsoplanner.datetimepicker.utils;

import java.util.Calendar;
import android.content.Context;
import com.astro.dsoplanner.R;

public class DateToStringArray {

	public static String[] getMinutes() {
		String[] minutes = new String[60];
		for (int i = 0; i < 60; i++) {
			if (i < 10) {
				minutes[i] = '0'+String.valueOf(i);
			} else {
				minutes[i] = String.valueOf(i);
			}
		}
		return minutes;
	}

	public static String[] getHours() {
		String[] hours = new String[24];
		for (int i = 0; i < 24; i++) {
			if (i < 10) {
				hours[i] = '0'+String.valueOf(i);
			} else {
				hours[i] = String.valueOf(i);
			}
		}
		return hours;
	}

	public static String[] getYears(int from, int to) {
		String[] years = new String[to - from + 1];
		int index = 0;
		for (int i = from; i < to + 1; i++) {
			years[index] = String.valueOf(i);
			index++;
		}
		return years;
	}

	public static String[] getMonths(Context context) {
		return context.getResources().getStringArray(com.astro.dsoplanner.R.array.months);
	}

	public static String[] getDays(int year, int month) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, 1);
		int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		String[] days = new String[max];

		for (int i = 1; i < max + 1; i++) {
			if (i < 10) {
				days[i - 1] = '0'+String.valueOf(i);
			} else {
				days[i - 1] = String.valueOf(i);
			}
		}

		return days;
	}
}