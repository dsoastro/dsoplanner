package com.astro.dsoplanner.graph;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.astro.dsoplanner.FloatingActionButton;
import com.astro.dsoplanner.InputDialog;
import com.astro.dsoplanner.Logg;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.graph.camera.CameraInterface;
import com.astro.dsoplanner.graph.camera.PhotoParams;
import com.astro.dsoplanner.graph.camera.Prefs;
import com.astro.dsoplanner.pushcamera.P;
import com.astro.dsoplanner.pushcamera.Plate;

public class PushCamImplementation implements PushCamPresenter.PushCamImplementation {
    private static final String TAG = "Graph";
    Runnable makePhotoRunnable;

    private CameraInterface myCamera;
    private Context context;
    private SensorProcessor sensorProcessor;
    private Handler initHandler;
    private CuV cView;
    PushCamBroadcastReceiver pushCamBroadcastReceiver;

    public PushCamImplementation(CameraInterface myCamera, Context context, SensorProcessor sensorProcessor, Handler initHandler, CuV cView, PushCamBroadcastReceiver pushCamBroadcastReceiver) {
        this.myCamera = myCamera;
        this.context = context;
        this.sensorProcessor = sensorProcessor;
        this.initHandler = initHandler;
        this.cView = cView;
        this.pushCamBroadcastReceiver = pushCamBroadcastReceiver;
    }

    public void setFabButtonPushcam(FloatingActionButton fabButtonPushcam) {
        this.fabButtonPushcam = fabButtonPushcam;
    }

    public void setPushCamPresenter(PushCamPresenter pushCamPresenter) {
        this.pushCamPresenter = pushCamPresenter;
    }

    FloatingActionButton fabButtonPushcam;
    PushCamPresenter pushCamPresenter;

    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                InputDialog.toast(message, context).show();
            }
        });

    }

    private void makePhoto(Prefs.PhotoType photo_type, Prefs.AlignType align_type, final int hr, final P rc) {
        if (myCamera == null)
            return;


        int N = SettingsActivity.getNumberOfStackedImages(context);
        final PhotoParams params = new PhotoParams(photo_type, align_type, hr, rc, 0, N);
        params.setFromSettings(context);
        makePhotoRunnable = new Runnable() {
            @Override
            public void run() {
                params.gyro_alt = sensorProcessor.getAlt();
                boolean result = myCamera.makePhoto(params);
                if (!result) {
                    showToast("Could not make photo!");
                }
                makePhotoRunnable = null;
            }
        };
        int delay = SettingsActivity.getImageCaptureDelay(context);
        if (!myCamera.isOpen()) {
            boolean result = myCamera.openCamera();
            if (!result) {
                showToast("Could not open camera!");
                return;
            }
            initHandler.postDelayed(makePhotoRunnable, delay + Prefs.CAMERA_OPEN_DELAY);
        } else {
            initHandler.postDelayed(makePhotoRunnable, delay);
        }
    }

    private Point getSelectedPoint() {
        if (cView.getObjCursor() != null) {
            Point p = cView.getObjCursor().getObjSelected();
            return p;
        } else
            return null;
    }

    @Override
    public FloatingActionButton getFab() {
        return fabButtonPushcam;
    }

    private boolean isValidAlignmentStar(Point p) {
        if (p == null)
            return false;
        if (p instanceof AstroObject) {
            AstroObject pStar = (AstroObject) p;
            if (pStar.getCatalog() == AstroCatalog.YALE_CATALOG)
                if (pStar.getMag() <= Plate.MAX_MAG)
                    return true;
        }
        return false;
    }

    boolean align_started = false;

    @Override
    public void startStarAlignment(Point starPoint) {

        Point selectedPoint = getSelectedPoint();
        boolean anyObject = SettingsActivity.isPushCameraAnyAlignObject(context);
        if (anyObject && selectedPoint != null) {
            pushCamBroadcastReceiver.setAlignmentObject(selectedPoint);
            align_started = true;

            makePhoto(Prefs.PhotoType.OBJECT_ALIGNMENT, Prefs.AlignType.NONE, Prefs.NONE, null);
            return;
        } else if (!anyObject && isValidAlignmentStar(selectedPoint)) {
            AstroObject pStar = (AstroObject) selectedPoint;
            if (pStar.getCatalog() == AstroCatalog.YALE_CATALOG) {
                pushCamBroadcastReceiver.setAlignmentObject(selectedPoint);
                Logg.d(TAG, "startStarAlignment, start alignment");
                int hrAlignmentStar = pStar.getId();
                align_started = true;
                makePhoto(Prefs.PhotoType.HR_ALIGNMENT, Prefs.AlignType.NONE, hrAlignmentStar, null);
                return;
            }
        } else {
            pushCamPresenter.onSolvedError();
            Logg.d(TAG, "startStarAlignment, No alignment star selected");
            showToast("No valid alignment star selected!");
        }
    }

    @Override
    public void startPlateSolve() {
        Logg.d(TAG, "startPlateSolve");
        boolean any_object = SettingsActivity.isPushCameraAnyAlignObject(context);
        boolean alignment_made = (any_object && pushCamBroadcastReceiver.getCenter_info_align_any_object() != null) ||
                (!any_object && pushCamBroadcastReceiver.getRc() != null);
        if (!alignment_made) { //should not be
            pushCamPresenter.onSolvedError();
            return;
        }
        if (align_started) {// no need to make photo right after alignment
            align_started = false;
            initHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pushCamPresenter.onSolvedSuccess();
                }
            }, 10);
        } else {
            if (any_object)
                makePhoto(Prefs.PhotoType.PLATE_SOLVE, Prefs.AlignType.OBJECT_ALIGNMENT, Prefs.NONE, null);
            else
                makePhoto(Prefs.PhotoType.PLATE_SOLVE, Prefs.AlignType.HR_ALIGNMENT, Prefs.NONE, pushCamBroadcastReceiver.getRc());
        }
    }

    @Override
    public void abortSolving() {
        if (myCamera == null)
            return;
        Logg.d(TAG, "abortSolving");


        if (makePhotoRunnable != null) {
            Logg.d(TAG, "remove makePhotoRunnable callback");
            initHandler.removeCallbacks(makePhotoRunnable);
        }
        if (myCamera.isOpen()) {
            myCamera.closeCamera();
        }
        myCamera.stopProcessingThreads();
    }

    @Override
    public void startGyro() {
        Logg.d(TAG, "startGyro");
        sensorProcessor.start();
    }

    @Override
    public void stopGyro() {
        Logg.d(TAG, "stopGyro");
        sensorProcessor.stop();
    }

    public void limitStars() {
        if (SettingsActivity.isPushCameraOn(context)) {
            Log.d(TAG, "limit stars");
            cView.limitStarsPushCamera();
            cView.invalidate();
        }
    }

    public void unlimitStars() {
        cView.unlimitStarsPushCamera();
        cView.invalidate();
    }
}
