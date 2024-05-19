package com.astro.dsoplanner.graph.camera;

import android.content.Context;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;

import com.astro.dsoplanner.SettingsActivity;

public class BuilderSettings {

    public static int getCaptureTemplate(Context context) {
        int template = CameraDevice.TEMPLATE_PREVIEW;
        if (SettingsActivity.isTemplateManual(context)) {
            template = CameraDevice.TEMPLATE_MANUAL;
        } else if (SettingsActivity.isTemplateVideo(context)) {
            template = CameraDevice.TEMPLATE_RECORD;
        } else if (SettingsActivity.isTemplatePhoto(context)) {
            template = CameraDevice.TEMPLATE_STILL_CAPTURE;
        }
        return template;
    }

    public static void addParams(CaptureRequest.Builder builder, Context context) {
        if (SettingsActivity.isCamera2Control(context)) {
            builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

            if (SettingsActivity.isCamera2NoiseReductionOff(context)) {
                builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
                builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF);
                builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_OFF);
            }

            if (SettingsActivity.isCamera2AutoFocus(context)) {
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            } else {
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF); //no auto focus
                builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, .0f);
            }
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);//no flash

        } else {
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
            builder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
            builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

            int exposure = SettingsActivity.getCamera2Exposure(context);
            int iso = SettingsActivity.getCamera2ISO(context);
            builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, exposure * 1000000L);
            builder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);

            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, .0f);

            if (SettingsActivity.isCamera2NoiseReductionOff(context)) {
                builder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
                builder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF);
                builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_OFF);
            }

        }
    }
}
