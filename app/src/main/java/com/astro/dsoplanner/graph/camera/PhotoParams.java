package com.astro.dsoplanner.graph.camera;

import android.content.Context;

import com.astro.dsoplanner.BuildConfig;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.pushcamera.P;

public class PhotoParams {
    Prefs.PhotoType photo_type;
    Prefs.AlignType align_type;
    int hr;
    P rc;
    public double gyro_alt;
    public int N;

    public boolean isCameraApi;
    public boolean isSensorHighSpeed;
    public boolean isRemoveBrightAreas;
    public boolean isPushCameraAnyAlignObject;
    public boolean isPushCamGyroAltValidation;
    public String gitHash;


    public PhotoParams(Prefs.PhotoType photo_type, Prefs.AlignType align_type, int hr, P rc, double gyro_alt, int N) {
        this.photo_type = photo_type;
        this.align_type = align_type;
        this.hr = hr;
        this.rc = rc;
        this.gyro_alt = gyro_alt;
        this.N = N;
    }


    @Override
    public String toString() {
        return "PhotoParams{" + "photo_type=" + photo_type + ", align_type=" + align_type + ", hr=" + hr + ", rc=" + rc + ", gyro_alt=" + gyro_alt + ", N=" + N + ", isCameraApi=" + isCameraApi + ", isSensorHighSpeed=" + isSensorHighSpeed + ", isRemoveBrightAreas=" + isRemoveBrightAreas + ", isPushCameraAnyAlignObject=" + isPushCameraAnyAlignObject + ", isPushCamGyroAltValidation=" + isPushCamGyroAltValidation + ", gitHash='" + gitHash + '\'' + '}';
    }

    public void setFromSettings(Context context) {
        isCameraApi = SettingsActivity.isCameraApi(context);

        isSensorHighSpeed = SettingsActivity.isSensorHighSpeed(context);
        isRemoveBrightAreas = SettingsActivity.isRemoveBrightAreas(context);
        isPushCameraAnyAlignObject = SettingsActivity.isPushCameraAnyAlignObject(context);

        isPushCamGyroAltValidation = SettingsActivity.isPushCamGyroAltValidation(context);
        gitHash = BuildConfig.GitHash;

    }

}
