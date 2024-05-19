package com.astro.dsoplanner.graph.camera;

import android.os.Handler;

public interface CameraInterface {
    public boolean openCamera();
    public boolean isOpen();
    public boolean makePhoto (PhotoParams params);
    public void closeCamera();
    public void stopProcessingThreads();
    public void setBackgroundHandler(Handler backgroundHandler);
}
