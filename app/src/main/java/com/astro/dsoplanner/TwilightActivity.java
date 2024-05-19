package com.astro.dsoplanner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.astro.dsoplanner.AstroTools.TransitRec;
import com.astro.dsoplanner.base.Planet;
import com.astro.dsoplanner.base.Point;

public class TwilightActivity extends ParentActivity implements OnGestureListener {
    private static final int TABLE_COUNT = 30;

    static final int DTP_CODE = 1;//code for starting date time picker


    private static final String STRING = "---";

    private MyDateDialog dd;
    private static final String TAG = TwilightActivity.class.getSimpleName();

    /**
     * keeping table at the end of the screen
     */
    List<View> vlist = new ArrayList<View>();


    private HandlerThread workerThread;
    private Handler workerHandler;
    boolean firstrun = true;


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
    }

    @Override
    public void onPause() {
        super.onPause();
        Global.twtime = dd.getDateTime();
    }

    @Override
    public void onResume() {
        update();
        super.onResume();
        hideMenuBtn();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "" + requestCode + " " + resultCode + " " + data);
        if (requestCode == DTP_CODE && resultCode == RESULT_OK) {
            long time = data.getLongExtra(Constants.DATE_TIME_PICKER_MILLIS, -1);
            Log.d(TAG, "time=" + time);
            if (time != -1) {

                dd.setMillis(time);
                update();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        workerThread.getLooper().quit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twilight);
        long twtime;
        if (Global.twtime == null)
            twtime = Calendar.getInstance().getTimeInMillis();
        else
            twtime = Global.twtime.getTimeInMillis();

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(twtime);
        dd = new MyDateDialog(this, c, new MyDateDialog.Updater() {
            public void update() {
                TwilightActivity.this.update();
            }
        });

        View btnback = findViewById(R.id.twilback);
        View btnfor = findViewById(R.id.twilfor);
        View btnpick = findViewById(R.id.twilpick);
        btnback.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dd.minusDay();
                update();
            }
        });
        btnfor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dd.plusDay();
                update();
            }
        });
        btnpick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SettingsActivity.putSharedPreferences(Constants.DTP_DISPLAY_MODE, DateTimePickerActivity.DATE, TwilightActivity.this);
                SettingsActivity.putSharedPreferences(Constants.DTP_TIME, dd.getCalendar().getTimeInMillis(), TwilightActivity.this);
                Intent i = new Intent(getApplicationContext(), DateTimePickerActivity.class);
                i.putExtra(Constants.DTP_RT, false);//no real time button
                startActivityForResult(i, DTP_CODE);
            }
        });

        workerThread = new HandlerThread("");
        workerThread.start();
        workerHandler = new Handler(workerThread.getLooper());
    }

    public void update() {
        Planet sun = new Planet(Planet.PlanetType.Sun, dd.getDateTime());
        Planet moon = new Planet(Planet.PlanetType.Moon, dd.getDateTime());

        TextView dt = (TextView) findViewById(R.id.twdate_text);
        dt.setText(DetailsActivity.makeDateString(dd.getDateTime(), true));

        TextView lt = (TextView) findViewById(R.id.twloc_text);
        double lat = SettingsActivity.getLattitude();
        double lon = SettingsActivity.getLongitude();


        if (firstrun) {
            firstrun = false;
            String latstr = AstroTools.getLatString(lat);
            String lonstr = AstroTools.getLonString(lon);
            lt.setText(latstr + " " + lonstr);
        }
        new LocationTask().execute(lat, lon);


        TextView sr = (TextView) findViewById(R.id.sunrise_text);
        TextView st = (TextView) findViewById(R.id.suntransit_text);
        TextView ss = (TextView) findViewById(R.id.sunset_text);

        TextView mr = (TextView) findViewById(R.id.moonrise_text);
        TextView mt = (TextView) findViewById(R.id.moontransit_text);
        TextView ms = (TextView) findViewById(R.id.moonset_text);

        TransitRec tr = AstroTools.getRiseSetting(sun, dd.getDateTime(), AstroTools.hSun);
        sr.setText(DetailsActivity.makeTimeString(tr.tRise, false));
        st.setText(DetailsActivity.makeTimeString(tr.tTransit, false));
        ss.setText(DetailsActivity.makeTimeString(tr.tSetting, false));

        tr = AstroTools.getRiseSetting(moon, dd.getDateTime(), AstroTools.hSun);
        mr.setText(DetailsActivity.makeTimeString(tr.tRise, dd.getDateTime(), false));
        mt.setText(DetailsActivity.makeTimeString(tr.tTransit, dd.getDateTime(), false));
        ms.setText(DetailsActivity.makeTimeString(tr.tSetting, dd.getDateTime(), false));

        Calendar cf = (Calendar) dd.getDateTime().clone();
        cf.set(Calendar.HOUR_OF_DAY, 0);
        cf.set(Calendar.MINUTE, 0);
        cf.set(Calendar.SECOND, 0);
        cf.set(Calendar.MILLISECOND, 0);

        String mstr = String.format(Locale.US, "%.1f", 100 * AstroTools.MoonFraction(cf)) + "%";
        TextView mfr = (TextView) findViewById(R.id.moonfr_text);
        mfr.setText(mstr);


        TextView cm = (TextView) findViewById(R.id.civilmorning_text);
        TextView ce = (TextView) findViewById(R.id.civilevening_text);
        tr = AstroTools.getRiseSetting(sun, dd.getDateTime(), AstroTools.hCiv);

        cm.setText(DetailsActivity.makeTimeString(tr.tRise, dd.getDateTime(), false));
        String ceText = DetailsActivity.makeTimeString(tr.tSetting, dd.getDateTime(), false);
        ce.setText(ceText);

        TextView nm = (TextView) findViewById(R.id.nauticalmorning_text);
        TextView ne = (TextView) findViewById(R.id.nauticalevening_text);
        tr = AstroTools.getRiseSetting(sun, dd.getDateTime(), AstroTools.hNav);

        nm.setText(DetailsActivity.makeTimeString(tr.tRise, dd.getDateTime(), false));

        String neText = DetailsActivity.makeTimeString(tr.tSetting, dd.getDateTime(), false);
        ne.setText(neText);

        TextView am = (TextView) findViewById(R.id.astromorning_text);
        TextView ae = (TextView) findViewById(R.id.astroevening_text);
        tr = AstroTools.getRiseSetting(sun, dd.getDateTime(), AstroTools.hAstro);

        am.setText(DetailsActivity.makeTimeString(tr.tRise, dd.getDateTime(), false));
        String aeText = DetailsActivity.makeTimeString(tr.tSetting, dd.getDateTime(), false);
        ae.setText(aeText);

        workerHandler.post(new UpdateTableTask(dd.getDateTime()));


    }

    class LocationTask extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground(Double... ds) {
            double lat = ds[0];
            double lon = ds[1];
            String name = AstroTools.getLocationName(lat, lon, getApplicationContext());
            return name;
        }

        @Override
        protected void onPostExecute(String locname) {

            TextView lt = (TextView) findViewById(R.id.twloc_text);
            if (locname != null) {
                lt.setText(locname);
            }

        }
    }


    private void initTable() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.linearLayout2);
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (vlist.size() != 0) {
            for (View v : vlist) {
                ll.removeView(v);
            }
            vlist = new ArrayList<View>();
        }

        for (int i = 0; i < TABLE_COUNT; i++) {
            View v = mInflater.inflate(R.layout.twilight_item, null);
            ll.addView(v);
            vlist.add(v);
        }
    }


    private Handler processHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            List<result> list;
            try {
                list = (List) msg.obj;
                initTable();
                for (int i = 0; i < TABLE_COUNT; i++) {
                    result r = (result) list.get(i);
                    updateTwilightItem(r.c, vlist.get(i), r.tw, r.set, false);
                    if (i == 0) {
                        View v = findViewById(R.id.tw_first_line);
                        updateTwilightItem(r.c, v, r.tw, r.set, true);
                    }
                }
            } catch (Exception e) {
                return;
            }
        }
    };


    class result {
        Calendar c;
        Line tw;
        Set<Line> set;

        public result(Calendar c, Line tw, Set<Line> set) {
            super();
            this.c = c;
            this.tw = tw;
            this.set = set;
        }

    }

    class UpdateTableTask implements Runnable {
        List<result> list = new ArrayList<result>();

        Calendar cc;

        public UpdateTableTask(Calendar c) {
            this.cc = (Calendar) c.clone();
        }

        @Override
        public void run() {
            for (int i = 0; i < TABLE_COUNT; i++) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(cc.getTimeInMillis() + i * 3600 * 24 * 1000L);
                result r = new result(c, getAstroNight(c), getMoonSet(c));
                list.add(r);
            }
            Message msg = Message.obtain();
            msg.obj = list;
            processHandler.sendMessage(msg);
        }


    }


    /**
     * @param c
     * @param v
     * @param tw
     * @param set
     * @param firstline - weekend is not filled with color
     * @return
     */
    private View updateTwilightItem(Calendar c, View v, Line tw, Set<Line> set, boolean firstline) {
		
        boolean weekend = false;
        int day = c.get(Calendar.DAY_OF_WEEK);
        if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
            weekend = true;
        }
        if (firstline) weekend = false;

        TextView t = (TextView) v.findViewById(R.id.twitem_darkness_date);
        t.setText(DetailsActivity.makeDateString(c, true));


        int bc = DetailsActivity.getBcFillingColor(nightMode);
        int fc = DetailsActivity.getFcFillingColor(nightMode);
        if (weekend) {
            t.setBackgroundColor(bc);
            t.setTextColor(fc);
        }


        for (Line l : set) {
            tw = tw.minus(l);
        }
        if (tw.isEmpty()) {
            TextView tv = (TextView) v.findViewById(R.id.twitem_darkness_end);
            tv.setText(STRING);
            if (weekend) {
                tv.setBackgroundColor(bc);
                tv.setTextColor(fc);
            }
            tv = (TextView) v.findViewById(R.id.twitem_darkness_begin);
            tv.setText(STRING);
            if (weekend) {
                tv.setBackgroundColor(bc);
                tv.setTextColor(fc);
            }


            View v2 = v.findViewById(R.id.twitem_darkness_duration);
            if (v2 != null) {
                tv = (TextView) v2;
                tv.setText(STRING);
                if (weekend) {

                    tv.setBackgroundColor(bc);
                    tv.setTextColor(fc);
                }
            }

        } else {
            TextView tv = (TextView) v.findViewById(R.id.twitem_darkness_begin);
            Calendar cb = Calendar.getInstance();
            cb.setTimeInMillis(tw.start);
            tv.setText(DetailsActivity.makeTimeString(cb, c, false));
            if (weekend) {
                tv.setBackgroundColor(bc);
                tv.setTextColor(fc);
            }

            tv = (TextView) v.findViewById(R.id.twitem_darkness_end);
            Calendar cend = Calendar.getInstance();
            cend.setTimeInMillis(tw.end);
            tv.setText(DetailsActivity.makeTimeString(cend, c, false));
            if (weekend) {
                tv.setBackgroundColor(bc);
                tv.setTextColor(fc);
            }

            View v2 = v.findViewById(R.id.twitem_darkness_duration);
            if (v2 != null) {
                tv = (TextView) v2;
                long duration = tw.end - tw.start;
                Calendar ctmp = Calendar.getInstance();
                ctmp.set(Calendar.HOUR_OF_DAY, 0);
                ctmp.set(Calendar.MINUTE, 0);
                ctmp.set(Calendar.SECOND, 0);
                ctmp.set(Calendar.MILLISECOND, 0);
                ctmp.setTimeInMillis(ctmp.getTimeInMillis() + duration);
                tv.setText(DetailsActivity.makeTimeString(ctmp, false));
                if (weekend) {
                    tv.setBackgroundColor(bc);
                    tv.setTextColor(fc);
                }
            }
        }
        return v;

    }

    /**
     * call it inside a handler to avoid using Global.sun at the same time by different threads!
     *
     * @param c
     * @return
     */
    private Line getAstroNight(Calendar c) {
        TransitRec tr = AstroTools.getRiseSetting(Global.sun, c, AstroTools.hAstro);


        Line tw;
        if (tr.tRise == null || tr.tSetting == null) {
            double h = getAlt(Global.sun, tr.tTransit);
            if (h < AstroTools.hAstro) {//always below -18
                tw = Line.wholeDay(tr.tSetting);
            } else
                tw = Line.EMPTY;//always above -18
        } else {
            Calendar nextday = Calendar.getInstance();
            nextday.setTimeInMillis(c.getTimeInMillis() + 24 * 3600 * 1000);
            TransitRec tr2 = AstroTools.getRiseSetting(Global.sun, nextday, AstroTools.hAstro);
            Log.d(TAG, "" + tr2.tRise + "\n" + tr2.tTransit + "\n" + tr2.tSetting);
            if (tr2.tRise == null) {//should not be
                tr2.tRise = Calendar.getInstance();
                tr2.tRise.setTimeInMillis(tr.tRise.getTimeInMillis() + 24 * 3600 * 1000);
            }

            tw = new Line(tr.tSetting.getTimeInMillis(), tr2.tRise.getTimeInMillis());
        }
        return tw;
    }

    /**
     * call it inside a handler to avoid using Global.sun at the same time by different threads!
     *
     * @param c
     * @return
     */
    private Set<Line> getMoonSet(Calendar c) {

        TransitRec trMoon0 = AstroTools.getRiseSetting(Global.moon, c, AstroTools.hSun);

        Calendar trm1 = Calendar.getInstance();
        trm1.setTimeInMillis(c.getTimeInMillis() - 24 * 3600 * 1000);
        Calendar trp1 = Calendar.getInstance();
        trp1.setTimeInMillis(c.getTimeInMillis() + 24 * 3600 * 1000);

        TransitRec trMoonMinus1 = AstroTools.getRiseSetting(Global.moon, trm1, AstroTools.hSun);
        TransitRec trMoonPlus1 = AstroTools.getRiseSetting(Global.moon, trp1, AstroTools.hSun);

        Set<Line> set = new HashSet<Line>();

        if (trMoon0.tRise == null || trMoon0.tSetting == null) {

            double h = getAlt(Global.moon, trMoon0.tTransit);
            Log.d(TAG, "h0=" + h);
            if (h > 0)
                set.add(Line.wholeDay(trMoon0.tTransit));
        } else
            set.add(new Line(trMoon0.tRise.getTimeInMillis(), trMoon0.tSetting.getTimeInMillis()));

        if (trMoonMinus1.tRise == null || trMoonMinus1.tSetting == null) {
            double h = getAlt(Global.moon, trMoonMinus1.tTransit);
            Log.d(TAG, "h-1=" + h);
            if (h > 0)
                set.add(Line.wholeDay(trMoonMinus1.tTransit));

        } else
            set.add(new Line(trMoonMinus1.tRise.getTimeInMillis(), trMoonMinus1.tSetting.getTimeInMillis()));


        if (trMoonPlus1.tRise == null || trMoonPlus1.tSetting == null) {
            double h = getAlt(Global.moon, trMoonPlus1.tTransit);
            Log.d(TAG, "h1=" + h);
            if (h > 0)
                set.add(Line.wholeDay(trMoonPlus1.tTransit));

        } else
            set.add(new Line(trMoonPlus1.tRise.getTimeInMillis(), trMoonPlus1.tSetting.getTimeInMillis()));

        return set;
    }

    private double getAlt(Planet p, Calendar c) {
        p.recalculateRaDec(c);
        Point co = p.getCurrentRaDec(c);
        double lst = AstroTools.sdTime(c);
        double lat = SettingsActivity.getLattitude();
        double alt = AstroTools.Altitude(lst, lat, co.ra, co.dec);
        return alt;
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


    static class Line {
        public static Line EMPTY = new Line(0, 0);
        long start;
        long end;

        /**
         * assume that end>start
         *
         * @param start
         * @param end
         */
        public Line(long start, long end) {
            super();
            this.start = start;
            this.end = end;
        }

        ;

        public boolean isEmpty() {
            return (start == 0 && end == 0);
        }

        /**
         * 12 hours - + from c
         *
         * @param c
         * @return
         */
        public static Line wholeDay(Calendar c) {
            Calendar cs = (Calendar) c.clone();
            cs.setTimeInMillis(c.getTimeInMillis() - 12 * 3600 * 1000);

            Calendar ce = (Calendar) c.clone();
            ce.setTimeInMillis(c.getTimeInMillis() + 12 * 3600 * 1000);

            Line line = new Line(cs.getTimeInMillis(), ce.getTimeInMillis());
            return line;

        }

        /**
         * @param l
         * @return part of the object line not covered by l
         */
        public Line minus(Line l) {
            if (this.isEmpty()) return this;
            if (l.isEmpty()) return this;
            if (l.end <= start) return this;
            if (l.start >= end) return this;
            if (l.start <= start && l.end >= end) return EMPTY;

            if (l.start <= start && l.end > start) return new Line(l.end, end);
            if (l.start > start)
                return new Line(start, l.start);//do not bother with the case when line is broken down in two parts
            return this;//should not happen
        }
    }
}
