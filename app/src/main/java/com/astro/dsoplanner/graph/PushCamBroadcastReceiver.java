package com.astro.dsoplanner.graph;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.astro.dsoplanner.InputDialog;
import com.astro.dsoplanner.Logg;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.graph.camera.Manager;
import com.astro.dsoplanner.graph.camera.Prefs;
import com.astro.dsoplanner.matrix.DMatrix;
import com.astro.dsoplanner.pushcamera.AnyObjectAlign;
import com.astro.dsoplanner.pushcamera.P;
import com.astro.dsoplanner.pushcamera.P4;

public class PushCamBroadcastReceiver {
    private static final String TAG = "Graph";
    GraphActivity graph;
    SensorProcessor sensorProcessor;

    public void setPushCamPresenter(PushCamPresenter pushCamPresenter) {
        this.pushCamPresenter = pushCamPresenter;
    }

    PushCamPresenter pushCamPresenter;

    public void setAlignmentObject(Point alignmentObject) {
        this.alignmentObject = alignmentObject;
    }

    public Point getAlignmentObject() {
        return alignmentObject;
    }

    Point alignmentObject = null;

    public void setRc(P rc) {
        this.rc = rc;
    }

    public void setCenter_info_align_any_object(P4 center_info_align_any_object) {
        this.center_info_align_any_object = center_info_align_any_object;
    }

    public P getRc() {
        return rc;
    }

    P rc = null; //push camera axes

    public P4 getCenter_info_align_any_object() {
        return center_info_align_any_object;
    }

    P4 center_info_align_any_object;

    public PushCamBroadcastReceiver(GraphActivity graph, SensorProcessor sensorProcessor) {
        this.graph = graph;
        this.sensorProcessor = sensorProcessor;

    }


    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                InputDialog.toast(message, graph.getApplicationContext()).show();
            }
        });

    }

    private P4 getP4(Intent intent) {
        double x = intent.getDoubleExtra(Manager.CIX, -1);
        double y = intent.getDoubleExtra(Manager.CIY, -1);
        double z = intent.getDoubleExtra(Manager.CIZ, -1);
        double t = intent.getDoubleExtra(Manager.CIT, -1);
        return new P4(x, y, z, t);
    }

    BroadcastReceiver photoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra(Manager.PUSH_CAMERA_TEXT);
            Log.d(TAG, "text=" + text);
            if (text != null)
                showToast(text);
            int type = intent.getIntExtra(Manager.PHOTO_TYPE, -1);

            switch (type) {
                case Prefs.HR_ALIGNMENT: //hr alignment solved
                    P4 rc4 = getP4(intent);
                    rc = new P(rc4.x, rc4.y); // row col of alignment star
                    double ra_center = rc4.z; //image coordinates
                    double dec_center = rc4.t;

                    //ra dec of alignment star
                    double ra_star = alignmentObject.getRa();
                    double dec_star = alignmentObject.getDec();
                    P4 radecs = new P4(ra_star, dec_star, ra_center, dec_center);
                    sensorProcessor.setAdjData(radecs);
                    sensorProcessor.adjust();
                    break;
                case Prefs.OBJECT_ALIGNMENT: //center of image
                    center_info_align_any_object = getP4(intent);
                    double ra_eyepiece = alignmentObject.getRa();
                    double dec_eyepiece = alignmentObject.getDec();
                    double ra2 = center_info_align_any_object.x;
                    double dec2 = center_info_align_any_object.y;
                    double angle2 = (center_info_align_any_object.t < 0 ? center_info_align_any_object.z + Math.PI : center_info_align_any_object.z) * Point.R2D;
                    Logg.d(TAG, "CameraService.OBJECT_ALIGNMENT " + ra2 + "  " + dec2 + " " + angle2);
                    radecs = new P4(ra_eyepiece, dec_eyepiece, ra2, dec2);
                    sensorProcessor.setAdjData(radecs);
                    double lat = SettingsActivity.getLattitude();
                    DMatrix phoneMatrix = AnyObjectAlign.getPhoneRotationMatrix(ra2, dec2, angle2, lat, Point.getLST());
                    Logg.d(TAG, "CameraService.OBJECT_ALIGNMENT. phoneMatrix " + phoneMatrix);
                    sensorProcessor.setPhoneMatrix(phoneMatrix);
                    sensorProcessor.adjust();
                    break;
                case Prefs.PLATE_SOLVE_RADECS: //plate solve. based on hr alignment
                    radecs = getP4(intent);
                    sensorProcessor.setAdjData(radecs);
                    sensorProcessor.adjust();
                    break;
                case Prefs.PLATE_SOLVE_CENTER_INFO: //plate solve. any object
                    P4 center_info = getP4(intent);
                    double ra1 = center_info_align_any_object.x;
                    double dec1 = center_info_align_any_object.y;
                    double angle1 = (center_info_align_any_object.t < 0 ? center_info_align_any_object.z + Math.PI : center_info_align_any_object.z) * Point.R2D;

                    ra2 = center_info.x;
                    dec2 = center_info.y;
                    angle2 = (center_info.t < 0 ? center_info.z + Math.PI : center_info.z) * Point.R2D;
                    Logg.d(TAG, "CameraService.PLATE_SOLVE_CENTER_INFO " + ra2 + "  " + dec2 + " " + angle2);
                    ra_eyepiece = alignmentObject.getRa();
                    dec_eyepiece = alignmentObject.getDec();

                    AnyObjectAlign align = new AnyObjectAlign(ra1, dec1, angle1, ra2, dec2, angle2, ra_eyepiece, dec_eyepiece);
                    P ep_new = align.getEyepieceRaDec();
                    radecs = new P4(ep_new.x, ep_new.y, ra2, dec2);
                    sensorProcessor.setAdjData(radecs);
                    lat = SettingsActivity.getLattitude();
                    phoneMatrix = AnyObjectAlign.getPhoneRotationMatrix(ra2, dec2, angle2, lat, Point.getLST());
                    Logg.d(TAG, "CameraService.PLATE_SOLVE_CENTER_INFO. phoneMatrix " + phoneMatrix);
                    sensorProcessor.setPhoneMatrix(phoneMatrix);
                    sensorProcessor.adjust();
                    break;
            }
            int action = intent.getIntExtra(Manager.PUSH_CAMERA_ACTION, -1);


            switch (action) {
                case Prefs.ON_SOLVED_SUCCESS:
                    if (pushCamPresenter != null)
                        pushCamPresenter.onSolvedSuccess();
                    break;
                case Prefs.ON_SOLVED_ERROR:
                    if (pushCamPresenter != null)
                        pushCamPresenter.onSolvedError();
                    break;
            }


        }
    };

    public BroadcastReceiver getPhotoReceiver() {
        return photoReceiver;
    }
}
