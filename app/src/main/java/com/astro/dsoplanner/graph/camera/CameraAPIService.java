package com.astro.dsoplanner.graph.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.astro.dsoplanner.Logg;
import com.astro.dsoplanner.SettingsActivity;

/**
 * Make photos with Camera API
 */
public class CameraAPIService implements Camera.PreviewCallback, CameraInterface {
    public static final String TAG = "PushCamera";
    private Camera camera;
    private SurfaceHolder surfaceHolder;
    boolean photo = false;
    int count = 0;

    Manager manager;
    Thread managerThread;
    Context context;

    int width = 1920;
    int height = 1080;


    public CameraAPIService(SurfaceHolder surfaceHolder, Context context) {
        this.surfaceHolder = surfaceHolder;
        this.context = context;
    }

    public boolean openCamera() {
        camera = Camera.open();
        Camera.Parameters params = camera.getParameters();
        Camera.Size psize = params.getPreviewSize();
        width = psize.width;
        height = psize.height;
        Log.d(TAG, "pwidth=" + width + "pheight=" + height);
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
        }
        camera.setPreviewCallback(this);
        camera.startPreview();
        return true;


    }

    public boolean isOpen() {
        return (camera != null);
    }

    public boolean makePhoto(PhotoParams params) {
        Logg.d(TAG, "params=" + params);
        manager = new Manager(context, width, height, params, Manager.ImageType.YUV);
        photo = true;
        managerThread = new Thread(manager);
        managerThread.start();
        return true;
    }

    public void closeCamera() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void stopProcessingThreads() {
        Log.d(TAG, "CameraService2. stopProcessingThreads");
        if (managerThread != null && managerThread.isAlive()) {
            managerThread.interrupt();
        }
    }

    public void setBackgroundHandler(Handler backgroundHandler) {

    }


    @Override
    public void onPreviewFrame(byte[] bytes, Camera paramCamera) {


        if (photo) {
            Log.d(TAG, "onPreview, size=" + bytes.length);
            byte[] a = new byte[width * height];
            System.arraycopy(bytes, 0, a, 0, width * height);
            manager.add(a);
            count++;
            if (count == SettingsActivity.getNumberOfStackedImages(context)) {
                count = 0;
                photo = false;
            }

        }

    }
}
