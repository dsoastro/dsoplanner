package com.astro.dsoplanner.graph.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Surface;


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.BuildConfig;
import com.astro.dsoplanner.InputDialog;
import com.astro.dsoplanner.Logg;
import com.astro.dsoplanner.SettingsActivity;

//LV. What to do when max size by width is different from max size by height?
//new SurfaceTexture(10) - what is 10 and what is the right usage?

/**
 * Make photos with Camera2 API
 */
public class Camera2APIService implements CameraInterface {
    public static final String TAG = "PushCamera";
    private String mCameraID;
    private CameraDevice mCameraDevice = null;
    private CameraCaptureSession mCaptureSession;
    private ImageReader mImageReader;
    Context context;
    ExecutorService eservice = Executors.newFixedThreadPool(1);

    public int width;
    public int height;

    CameraManager mCameraManager;
    Handler mBackgroundHandler;
    Manager manager;
    Thread managerThread;
    Manager.ImageType imageType;

    public Camera2APIService(CameraManager cameraManager, String cameraID, Context context) {
        mCameraManager = cameraManager;
        mCameraID = cameraID;
        this.context = context;
    }

    public void setBackgroundHandler(Handler backgroundHandler) {
        mBackgroundHandler = backgroundHandler;
    }

    public boolean makePhoto(PhotoParams params) {
        Log.d(TAG, "make photo");
        if (!SettingsActivity.isCameraPermissionValid(context)) return false;
        if (!isOpen()) {
            Logg.d(TAG, "makePhoto, not open");
            return false;
        }


        Logg.d(TAG, "makePhoto, params=" + params);
        Logg.d(TAG, "makePhoto, image validation=" + SettingsActivity.isPushCamGyroAltValidation(context));
        Logg.d(TAG, "makePhoto, git hash=" + BuildConfig.GitHash);
        if (mCaptureSession == null) return false;
        if (height == 0 || width == 0) {
            InputDialog.toast("Zero height or width", context).show();
            return false;

        }


        manager = new Manager(context, width, height, params, imageType);
        try {
            int template = BuilderSettings.getCaptureTemplate(context);

            // This is the CaptureRequest.Builder that we use to take a picture.
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(template);
            captureBuilder.addTarget(mImageReader.getSurface());
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {

                }

                @Override
                public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
                    Logg.d(TAG, "onCaptureSequenceCompleted " + sequenceId + " " + frameNumber);
                }

                public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
                    Logg.d(TAG, "onCaptureSequenceAborted " + sequenceId);
                }

                @Override
                public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                    Logg.d(TAG, "capture failure=" + failure);
                    manager.incCount();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            captureBuilder.set(CaptureRequest.JPEG_QUALITY, (byte) 100);
            BuilderSettings.addParams(captureBuilder, context);


            if (Prefs.TESTING_PUSHCAM) {
                Logg.d(TAG, "TESTING_PUSHCAM = true");
            }
            List<CaptureRequest> alist = new ArrayList<CaptureRequest>();
            int N = params.N;
            for (int i = 0; i < N; i++) {
                alist.add(captureBuilder.build());
            }
            managerThread = new Thread(manager);
            managerThread.start();
            if (!Prefs.TESTING_PUSHCAM)
                mCaptureSession.captureBurst(alist, CaptureCallback, mBackgroundHandler);

        } catch (Exception e) {
            Logg.d(TAG, "makePhoto, exception=" + AstroTools.getStackTrace(e));
            return false;
        }
        return true;
    }


    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            eservice.execute(new ImageSaver(reader, manager, width, height, imageType));
        }

    };


    private CameraDevice.StateCallback mCameraCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            Logg.d(TAG, "Open camera  with id:" + mCameraDevice.getId());

            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            mCameraDevice.close();

            Logg.d(TAG, "disconnect camera  with id:" + mCameraDevice.getId());
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Logg.d(TAG, "error! camera id:" + camera.getId() + " error:" + error);
        }
    };


    private void createCameraPreviewSession() {

        mImageReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 1);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);

        SurfaceTexture texture = new SurfaceTexture(10);//mImageView.getSurfaceTexture();

        texture.setDefaultBufferSize(width, height);
        Surface surface = new Surface(texture);

        try {
            final CaptureRequest.Builder builder;


            builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF); //no auto focus
            builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, .0f);

            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mCaptureSession = session;
                    try {

                        mCaptureSession.setRepeatingRequest(builder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (Exception e) {
            Logg.d(TAG, "createCameraPreviewSession, exception=" + AstroTools.getStackTrace(e));
        }
    }

    public boolean isOpen() {
        if (mCameraDevice == null) {
            return false;
        } else {
            return true;
        }
    }


    public boolean openCamera() {
        if (!SettingsActivity.isCameraPermissionValid(context)) return false;
        try {

            width = 1920;
            height = 1080;
            imageType = Manager.ImageType.YUV;


            mCameraManager.openCamera(mCameraID, mCameraCallback, mBackgroundHandler);
        } catch (Exception e) {
            Logg.d(TAG, "openCamera, exception=" + AstroTools.getStackTrace(e));
            return false;
        }
        return true;
    }


    public void closeCamera() {

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    public void stopProcessingThreads() {
        Logg.d(TAG, "CameraService. stopProcessingThreads");
        if (managerThread != null && managerThread.isAlive()) {
            managerThread.interrupt();
        }
    }

    private static class ImageSaver implements Runnable {


        /**
         * The JPEG image
         */

        /**
         * The file we save the image into.
         */

        ImageReader mRreader;
        Manager manager;
        int w;
        int h;
        Manager.ImageType imageType;


        ImageSaver(ImageReader reader, Manager manager, int w, int h, Manager.ImageType imageType) {
            mRreader = reader;

            this.manager = manager;
            this.w = w;
            this.h = h;
            this.imageType = imageType;


        }

        @Override
        public void run() {
            Image mImage = mRreader.acquireNextImage();

            if (manager.getQueueSize() > Prefs.MAX_IMAGE_QUEUE_SIZE) {
                Logg.d(TAG, "Image saver, skipping images. Queue size=" + manager.getQueueSize());
                mImage.close();
                manager.incCount();
                return;
            }
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            Logg.d(Camera2APIService.TAG, "buffer capacity=" + buffer.capacity());
            if (imageType == Manager.ImageType.YUV && buffer.capacity() != w * h) {
                Logg.d(TAG, "ImageSaver, wrong size! capacity=" + buffer.capacity() + " w=" + w + " h=" + h);
                manager.incCount();
                mImage.close();
                return;
            }
            byte[] bytes;
            try {
                bytes = new byte[buffer.capacity()];
            } catch (OutOfMemoryError e) {
                Logg.d(TAG, "e=" + e);
                manager.incCount();
                mImage.close();
                return;
            }
            buffer.get(bytes);
            manager.add(bytes);

            Logg.d(TAG, "ImageSaver, add to list");

            mImage.close();

        }
    }
}