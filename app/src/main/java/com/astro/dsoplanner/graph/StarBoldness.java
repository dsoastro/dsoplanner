package com.astro.dsoplanner.graph;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;


import com.astro.dsoplanner.R;
import com.astro.dsoplanner.RangeSeekBar;
import com.astro.dsoplanner.RangeSeekBar.OnRangeSeekBarChangeListener;
import com.astro.dsoplanner.SettingsActivity;

public class StarBoldness extends RelativeLayout {

    private static final String _0_25_13_0_0_0 = "0.25 13.0 0.0;";
    private static final String _0_5_11_0_0_0 = "0.5 11.0 0.0;";
    private static final String _1_0_11_0_0_0 = "1.0 11.0 0.0;";
    private static final String _2_0_11_0_0_0 = "2.0 11.0 0.0;";
    private static final String _5_0_9_0_10_0 = "5.0 9.0 -10.0;";
    private static final String _10_0_9_0_10_0 = "10.0 9.0 -10.0;";
    private static final String _20_0_6_0_10_0 = "20.0 6.0 -10.0;";
    private static final String _30_0_6_0_10_0 = "30.0 6.0 -10.0;";
    private static final String _45_0_6_0_15_0 = "45.0 6.0 -15.0;";
    private static final String _60_0_6_0_20_0 = "60.0 6.0 -20.0;";
    private static final String _90_0_4_5_20_0 = "90.0 4.5 -20.0;";
    private static final String _0_06_13_0_0_0 = "0.06 13.0 0.0;";
    private static final String _0_12_13_0_0_0 = "0.12 13.0 0.0;";
    private static final String _0_25_13_4_0_0 = "0.25 13.4 0.0;";
    private static final String _0_5_12_8_5_8 = "0.5 12.8 5.8;";
    private static final String _1_0_11_0_7_0 = "1.0 11.0 7.0;";
    private static final String _2_0_11_0_5_3 = "2.0 11.0 5.3;";
    private static final String _5_0_9_9_1_8 = "5.0 9.9 1.8;";
    private static final String _10_0_8_2_1_2 = "10.0 8.2 1.2;";
    private static final String _20_0_6_4_0_0 = "20.0 6.4 0.0;";
    private static final String _30_0_6_4_0_6 = "30.0 6.4 -0.6;";
    private static final String _45_0_6_4_1_7 = "45.0 6.4 -1.7;";
    private static final String _60_0_6_4_2_9 = "60.0 6.4 -2.9;";
    private static final String _90_0_5_24_4 = "90.0 5.24 -4;";

    protected static final float MAG_MIN = 18; //Minimal star magnitude
    protected static final float MAG_MAX = -40;//Maximal magnitude (unreal, but necessary)
    private Context _context;
    private boolean _isActive = false;
    private RangeSeekBar<Integer> seekBar = null;
    private static int _minpix = 1; //Min size of the star drawing
    private static int _maxpix = 20;//Max size of the star drawing
    private static float _dm = 1; //Interpolation multiplier
    private static ArrayList<FovMap> fovmap = new ArrayList<FovMap>();
    private static FovMap _curfov = null;

    public static class FovMap {
        public float fov;
        public float minm;
        public float maxm;

        public FovMap(float a, float b, float c) {
            fov = a;
            minm = b;
            maxm = c;
        }
    }

    public StarBoldness(Activity _context) {
        super(_context);
        this._context = _context;
        LayoutInflater.from(_context).inflate(R.layout.boldness, this, true);
        findViewById(R.id.boldness).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            }
        });
    }

    public void Init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setVisibility(View.GONE);
        _isActive = false;

        String boldnessString = SettingsActivity.getBoldnessData();
        if (boldnessString == null)
            boldnessString = makeDefaultBoldnessString();
        makeFOVmap(boldnessString);


        // create RangeSeekBar as Integer range between 20 and 100
        seekBar = new RangeSeekBar<Integer>(0, 100, _context);
        seekBar.setNightmode(SettingsActivity.getNightMode());
        seekBar.setNotifyWhileDragging(true);
        seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                updateCurMap(MagScale(minValue), MagScale(maxValue));
                Redraw();
            }
        });

        // add RangeSeekBar to pre-defined layout
        ViewGroup layout = (ViewGroup) findViewById(R.id.rangeSlider);
        layout.addView(seekBar);

        updateRangeSlider();

        //reset this button
        Button b2 = (Button) findViewById(R.id.bb_reset);
        b2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String tmp = makeDefaultBoldnessString();
                for (String data : tmp.split(";")) {
                    String[] rec = data.split(" ");
                    try {
                        if (Float.parseFloat(rec[0]) == _curfov.fov) {
                            updateCurMap(Float.parseFloat(rec[1]), Float.parseFloat(rec[2]));
                            break; //for
                        }
                    } catch (Exception e) {
                    }
                }
                Redraw();
                updateRangeSlider();
            }
        });

    }

    protected void updateCurMap(float min, float max) {
        _curfov.minm = min;
        _curfov.maxm = max;
        _dm = (_maxpix - _minpix) / (_curfov.minm - _curfov.maxm);
    }

    private static void makeFOVmap(String boldnessString) {
        String[] params = boldnessString.split(";");
        fovmap.clear();
        for (String s : params) {
            String[] v = s.split(" ");
            try {
                fovmap.add(new FovMap(
                        Float.parseFloat(v[0]),
                        Float.parseFloat(v[1]),
                        Float.parseFloat(v[2])
                ));
            } catch (Exception e) {
            }
        }
    }

    private void updateRangeSlider() {
        if (_curfov == null) return;
        if (seekBar != null) {
            seekBar.setSelectedMinValue(toProgress(_curfov.minm));
            seekBar.setSelectedMaxValue(toProgress(_curfov.maxm));
        }
    }

    //See the commented method below, which can support changing zoom controller
    private static String makeDefaultBoldnessString() {
		/* manually acquired values
		return "90.0 5.24 -20.0;" +
			   "60.0 5.8199997 -20.0;" +
			   "45.0 5.8199997 -8.1;" +
			   "30.0 5.8199997 -6.9400005;" +
			   "20.0 6.3999996 -6.9400005;" +
			   "10.0 8.0 -5.7800007;" +
			   "5.0 9.2 -10.0;" +
			   "2.0 11.0 0.0;" +
			   "1.0 11.0 0.0;" +
			   "0.5 11.0 0.0;" +
			   "0.25 11.0 0.0;" +
			   "0.12 11.0 0.0;" +
			   "0.06 11.0 0.0;";
		 */
        String soft =
                _90_0_5_24_4 +
                        _60_0_6_4_2_9 +
                        _45_0_6_4_1_7 +
                        _30_0_6_4_0_6 +
                        _20_0_6_4_0_0 +
                        _10_0_8_2_1_2 +
                        _5_0_9_9_1_8 +
                        _2_0_11_0_5_3 +
                        _1_0_11_0_7_0 +
                        _0_5_12_8_5_8 +
                        _0_25_13_4_0_0 +
                        _0_12_13_0_0_0 +
                        _0_06_13_0_0_0;
        String hard =
                _90_0_4_5_20_0 +
                        _60_0_6_0_20_0 +
                        _45_0_6_0_15_0 +
                        _30_0_6_0_10_0 +
                        _20_0_6_0_10_0 +
                        _10_0_9_0_10_0 +
                        _5_0_9_0_10_0 +
                        _2_0_11_0_0_0 +
                        _1_0_11_0_0_0 +
                        _0_5_11_0_0_0 +
                        _0_25_13_0_0_0 +
                        _0_12_13_0_0_0 +
                        _0_06_13_0_0_0;
        return SettingsActivity.getStarMode() == 0 ? hard : soft;
    }

    //Redraw the main star view with new boldness
    protected void Redraw() {
        ((GraphActivity) _context).getSkyView().postInvalidate();
    }

    private static int toProgress(double m) {
        return (int) (100. * (MAG_MIN - m) / (MAG_MIN - MAG_MAX));
    }

    protected float MagScale(int progress) {
        return MAG_MIN - (MAG_MIN - MAG_MAX) * progress / 100;
    }

    public boolean isActive() {
        return _isActive;
    }

    public void stop() {
        if (_isActive) {
            setVisibility(View.GONE);
            _isActive = false;
        }

    }

    public void start() {
        if (!_isActive) {
            setVisibility(View.VISIBLE);
            _isActive = true;
        }

    }

    public void setFov(double fov) {
        if (_curfov == null || (_curfov != null && _curfov.fov != fov)) { //to avoid frequent calls SS
            for (FovMap i : fovmap) {
                if (i.fov <= fov) { //lookup our table, fov decrease in it, so custom zoom is OK too
                    _curfov = i; //remember table entry for future update to user values
                    _dm = (_maxpix - _minpix) / (_curfov.minm - _curfov.maxm);
                    break;
                }
            }
            updateRangeSlider();
        }
    }

    public static void putBoldnesstoPrefs() {
        SettingsActivity.saveBoldnessData(fovmap);
    }

    //returns star radius in pixels
    public static float Calculate(double mag) { //SAND, double fov){
        if (mag >= _curfov.minm) return _minpix;
        if (mag <= _curfov.maxm) return _maxpix;
        float rad = _minpix + (_curfov.minm - (float) mag) * _dm;

        return rad;
    }

    public static void initOther(double fov) {
        String boldnessString = SettingsActivity.getBoldnessData();
        if (boldnessString == null)
            boldnessString = makeDefaultBoldnessString();
        makeFOVmap(boldnessString);
        setFovOther(fov);
    }

    public static void setFovOther(double fov) {
        if (_curfov == null || (_curfov != null && _curfov.fov != fov)) { //to avoid frequent calls SS
            for (FovMap i : fovmap) {
                if (i.fov <= fov) { //lookup our table, fov decrease in it, so custom zoom is OK too
                    _curfov = i; //remember table entry for future update to user values
                    _dm = (_maxpix - _minpix) / (_curfov.minm - _curfov.maxm);
                    break;
                }
            }

        }
    }

}
