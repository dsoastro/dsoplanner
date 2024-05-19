package com.astro.dsoplanner.graph;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;

import com.astro.dsoplanner.FloatingActionButton;
import com.astro.dsoplanner.Logg;
import com.astro.dsoplanner.R;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.pushcamera.P;
import com.astro.dsoplanner.pushcamera.Utils;

import static android.animation.ValueAnimator.INFINITE;

@RequiresApi(api = Build.VERSION_CODES.M)
class PushCamPresenter implements View.OnClickListener {
    private static final String TAG = "PCamPresenter";
    private static final String KEY1 = "sjdhdffjs1";
    private static final String KEY2 = "sjdhdffjs2";
    private static final int OLD_DURATION = 5;

    private static final int ALIGN_BLINK_DURATION_MS = 500;
    private static final int SOLVE_BLINK_DURATION_MS = 500;
    private static final int ERROR_BLINK_DURATION_MS = 200;

    private final PushCamImplementation pushCam;

    private PushCamUiModel model;
    private boolean isSolvingInProgress = false;
    private ObjectAnimator blinkingAnimator;
    private int oldDuration = OLD_DURATION; //To avoid too frequent updates when far away or on target.

    Context context;

    /**
     * Create lazily when pushcam mode is turned on
     **/
    PushCamPresenter(PushCamImplementation pushCam, Context context) {
        this.pushCam = pushCam;
        this.context = context;
        if (isFabNotNull()) {
            pushCam.getFab().setOnClickListener(this);
        }
        init();
    }

    void init() {
        model = new PushCamUiModel();
        isSolvingInProgress = false;
        restore(context);
        if (model.getMode() == Mode.ALIGN) {
            pushCam.limitStars();
        }
        updateButton();
    }

    /**
     * To Do: reset to initial state
     */
    void reset() {
        model = new PushCamUiModel();
        model.clearSharedPrefs(context);
        isSolvingInProgress = false;
        pushCam.limitStars();
        updateButton();
    }

    void save() {
        //model, isSolvingInProgress, oldDuration
        model.save(context);
    }

    void restore(Context context) {
        model.restore(context);
        isSolvingInProgress = false;
        Mode mode = model.getMode();
        if (mode == Mode.ERROR_ALIGN)
            model.setMode(Mode.ALIGN);
        else if (mode == Mode.ERROR_PAUSE || mode == Mode.MOVING)
            model.setMode(Mode.PAUSED);
    }

    void clearSharedPrefs() {
        model.clearSharedPrefs(context);
    }

    /**
     * Call when object selected on the chart (and pushcam mode is on)
     **/
    void setSelectedObject(Point skyPoint) {
        Logg.d("Graph", "setSelectedObject=" + skyPoint);
        model.setSkyPoint(skyPoint);
    }

    void moveGyroToPauseIfRequired() {
        isSolvingInProgress = false;
        if (model.getMode() == Mode.MOVING) {
            pushCam.stopGyro();
            model.setMode(Mode.PAUSED);
        }
        updateButton();
    }

    /**
     * Call when solved with the found point data
     */
    public void onSolvedSuccess() {
        isSolvingInProgress = false;
        Mode mode = model.getMode();
        if (mode == Mode.ALIGN) {
            model.setMode(Mode.PAUSED);
            pushCam.unlimitStars();
        } else if (mode == Mode.PAUSED) {
            model.setMode(Mode.MOVING);
            pushCam.startGyro();
        }
        updateButton();
    }

    /**
     * Call if solve failed
     **/
    public void onSolvedError() {
        isSolvingInProgress = false;
        Mode mode = model.getMode();
        if (mode == Mode.ALIGN)
            model.setMode(Mode.ERROR_ALIGN);
        else
            model.setMode(Mode.ERROR_PAUSE);
        updateButton();
    }

    /**
     * Call when there is a new Gyro data available (with the new point)
     * (Can be called only every half second or something as it only updates the blinking rate)
     **/
    public void onGyroUpdated(P centerPoint) {
        model.setCenterPoint(centerPoint);
        updateButton();
    }

    // region private implementation

    /**
     * Update button view and animation
     */
    private void updateButton() {
        if (isFabNotNull()) {
            if (isSolvingInProgress) {
                setFabDrawable(R.drawable.ic_pushcam_solving);
                setBlinking(SOLVE_BLINK_DURATION_MS);
            } else {
                switch (model.getMode()) {
                    case ALIGN:
                        setFabDrawable(R.drawable.ic_pushcam_align);
                        setBlinking(ALIGN_BLINK_DURATION_MS);
                        break;
                    case PAUSED:
                        setFabDrawable(R.drawable.ic_pushcam_paused);
                        setBlinking(0);
                        break;
                    case MOVING:
                        setFabDrawable(model.isOnTarget()
                                ? R.drawable.ic_pushcam_ontarget
                                : R.drawable.ic_pushcam_move);
                        setBlinking(model.getNewGeigerDelayMs());
                        break;
                    case ERROR_PAUSE:
                    case ERROR_ALIGN:
                        setFabDrawable(R.drawable.ic_pushcam_error);
                        setBlinking(ERROR_BLINK_DURATION_MS);
                        break;
                    default:
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        Logg.d("Graph", "onClick start, current mode=" + model.getMode() + " isSolvingInProgress=" + isSolvingInProgress);
        if (isSolvingInProgress) {
            isSolvingInProgress = false;
            pushCam.abortSolving();
            if (model.getMode() != Mode.PAUSED)
                model.setMode(Mode.ALIGN);
            pushCam.limitStars();
        } else {
            Mode mode = model.getMode();
            switch (mode) {
                case ALIGN:
                    //the order is important!!! As pushCam may call on SolvedError within startStarAlignment!
                    isSolvingInProgress = true;
                    pushCam.startStarAlignment(model.getSkyPoint());

                    break;
                case PAUSED:
                    //the order is important!!!
                    isSolvingInProgress = true;
                    pushCam.startPlateSolve();

                    break;
                case MOVING:
                    pushCam.stopGyro();
                    model.setMode(Mode.PAUSED);
                    break;
                case ERROR_ALIGN:
                    reset();
                    break;
                case ERROR_PAUSE:
                    model.setMode(Mode.PAUSED);
                    break;
            }
        }
        updateButton();
        Logg.d("Graph", "onClick over, current mode=" + model.getMode() + " isSolvingInProgress=" + isSolvingInProgress);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setFabDrawable(int id) {
        if (isFabNotNull()) {
            //TODO(AK): When updated to AndroidX use Drawable.toBitmap() instead
            Resources resources = pushCam.getFab().getContext().getApplicationContext().getResources();
            try {
                // Vector Drawable top Bitmap
                Drawable drawable = resources.getDrawable(id);
                Bitmap bitmap = Bitmap.createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                Matrix matrix = new Matrix();
                matrix.preScale(-1.0f, 1.0f);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                pushCam.getFab().setFloatingActionButtonDrawable(new BitmapDrawable(resources, bitmap));
            } catch (OutOfMemoryError e) {
                // Fallback
                pushCam.getFab().setFloatingActionButtonDrawable(resources.getDrawable(R.drawable.ram_btn_menu));
            }
        }
    }

    private void setBlinking(int delayMs) {
        if (oldDuration == delayMs) return;
        oldDuration = delayMs;
        if (blinkingAnimator == null) {
            blinkingAnimator = ObjectAnimator.ofFloat(pushCam.getFab(), "alpha", 0f, 1f);
            blinkingAnimator.setRepeatCount(INFINITE);
            blinkingAnimator.setRepeatMode(ValueAnimator.REVERSE); //So it's ON at the end();
        }
        if (delayMs == 0) {
            blinkingAnimator.end();
            oldDuration = -1; //impossible so on any new delay passed later update blinking.
        } else {
            blinkingAnimator.setDuration(delayMs);
            if (!blinkingAnimator.isRunning()) {
                blinkingAnimator.start();
            }
        }
    }

    private boolean isFabNotNull() {
        return pushCam != null && pushCam.getFab() != null;
    }

    // endregion

    enum Mode {
        ALIGN,
        PAUSED,
        MOVING,
        ERROR_ALIGN,
        ERROR_PAUSE
    }

    static class PushCamUiModel {
        private static final String KEY1 = "FFDre451";
        private static final String KEY2 = "FFDre452";
        private static final String KEY3 = "FFDre453";
        private static final String KEY4 = "FFDre454";
        private static final String KEY5 = "FFDre455";
        private static final String KEY6 = "FFDre456";


        private static final double minDistance = 0.25; //degrees
        private static final double maxDistance = 10; //degrees
        private static final int minBlinkTime = 50; //ms
        private static final int maxBlinkTime = 2000;  // 2 sec max blink delay
        private static final int NOT_BLINKING = 0;
        private static final double INITIAL_DISTANCE = maxDistance + 1;

        private P centerPoint;
        private Point skyPoint;
        private Mode mode = Mode.ALIGN;
        private double targetDistance = INITIAL_DISTANCE;
        private boolean isOnTarget = false;

        public void save(Context context) {
            SettingsActivity.putSharedPreferences(KEY3, mode.name(), context);
        }

        public void restore(Context context) {
            mode = Mode.ALIGN;
            try {
                String value = SettingsActivity.getStringFromSharedPreferences(context, KEY3, "");
                mode = Mode.valueOf(value);
            } catch (Exception e) {

            }

        }

        public void clearSharedPrefs(Context context) {
            SettingsActivity.removeSharedPreferencesKey(KEY3, context);
        }

        public void setSkyPoint(Point skyPoint) {
            this.skyPoint = skyPoint;
        }

        public Point getSkyPoint() {
            return skyPoint;
        }

        public boolean isOnTarget() {
            return isOnTarget;
        }

        /**
         * On gyro update
         **/
        public void setCenterPoint(P centerPoint) {
            this.centerPoint = centerPoint;
            calculateDistanceToTheTarget();
        }

        private void calculateDistanceToTheTarget() {
            final double m12_180 = 12. / 180.;
            if (skyPoint != null && centerPoint != null) {
                skyPoint.setXY();
                targetDistance = Utils.dst_angle(
                        new P(skyPoint.getAz() * m12_180, skyPoint.getAlt()),
                        new P(centerPoint.x * m12_180, centerPoint.y));
                isOnTarget = targetDistance <= minDistance;
            } else {
                targetDistance = INITIAL_DISTANCE;
                isOnTarget = false;
            }
        }

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }

        public int getNewGeigerDelayMs() {
            final double[] thresholds = {
                    0.25, 0.5, 1, 2, 4, 8};
            //  0     100  200 400 800 1600
            int t = NOT_BLINKING; //below min distance, 0 means no blinking
            if (targetDistance >= maxDistance || skyPoint == null || centerPoint == null) {
                t = maxBlinkTime;
            } else if (targetDistance > minDistance) {
                t = minBlinkTime;
                for (double threshold : thresholds) {
                    if (targetDistance < threshold) {
                        break;
                    }
                    t *= 2;
                }
            }
            return t;
        }
    }

    interface PushCamImplementation {
        FloatingActionButton getFab();

        void startStarAlignment(Point starPoint);

        void startPlateSolve();

        void abortSolving();

        void startGyro();

        void stopGyro();

        void limitStars();

        void unlimitStars();
    }
}
