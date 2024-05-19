package com.astro.dsoplanner.graph;

import static com.astro.dsoplanner.Global.ALEX_MENU_FLAG;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.astro.dsoplanner.Artv;
import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.BSp;

import com.astro.dsoplanner.CenterMarkObject;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.DMS;

import com.astro.dsoplanner.EyepiecesListActivity;
import com.astro.dsoplanner.FloatingActionButton;
//import com.astro.dsoplanner.camera.PhotoIntentService;
import com.astro.dsoplanner.graph.camera.CameraInterface;
import com.astro.dsoplanner.graph.camera.CameraAPIService;
import com.astro.dsoplanner.graph.camera.Camera2APIService;
import com.astro.dsoplanner.graph.camera.Manager;
import com.astro.dsoplanner.graph.camera.Prefs;

import com.astro.dsoplanner.DateTimePickerActivity;
import com.astro.dsoplanner.DetailsActivity;
import com.astro.dsoplanner.DetailsCommand;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.InputDialog;
import com.astro.dsoplanner.InputDialog.OnButtonListener;

import com.astro.dsoplanner.MarkTAG;
import com.astro.dsoplanner.MyDateDialog;


import com.astro.dsoplanner.ParentActivity;
import com.astro.dsoplanner.R;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.SettingsBluetoothActivity;
import com.astro.dsoplanner.SettingsGraphActivity;
import com.astro.dsoplanner.SettingsInclActivity;
import com.astro.dsoplanner.TestIntentService;
import com.astro.dsoplanner.alexmenu.alexMenu;
import com.astro.dsoplanner.alexmenu.alexMenu.OnMenuItemSelectedListener;
import com.astro.dsoplanner.alexmenu.alexMenuItem;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.ObjCursor;
import com.astro.dsoplanner.base.Planet;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.database.CustomDatabase;
import com.astro.dsoplanner.database.CustomDatabaseLarge;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.DbManager;
import com.astro.dsoplanner.database.NgcicDatabase;
import com.astro.dsoplanner.download.DSSdownloadable;
import com.astro.dsoplanner.download.DownloadService;
import com.astro.dsoplanner.download.DownloadService.LocalBinder;
import com.astro.dsoplanner.graph.CuV.TopMessage;
import com.astro.dsoplanner.graph.cuv_helper.UploadRec;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.infolist.ObsInfoListImpl;
import com.astro.dsoplanner.scopedrivers.CommunicationManager;
import com.astro.dsoplanner.scopedrivers.TelescopeDriver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.M)
public class GraphActivity extends ParentActivity implements OnMenuItemSelectedListener, OnGestureListener, TextureView.SurfaceTextureListener {
    private static final String UGC = "UGC";
    private static final String OFF = "Off";
    private static final String LEVEL2 = "Level";
    private static final String COMPASS_LEVEL = "Compass+Level";
    private static final int NBY_DIALOG = 15;
    private static final String _0_06 = "0.06";
    private static final String _0_12 = "0.12";
    private static final String _0_25 = "0.25";
    private static final String _0_5 = "0.5";
    private static final String _1 = "1";
    private static final String _2 = "2";
    private static final String _5 = "5";
    private static final String _10 = "10";
    private static final String _20 = "20";
    private static final String _30 = "30";
    private static final String _45 = "45";
    private static final String _60 = "60";
    private static final String _90 = "90";
    public static final String CALIBR_ORIENT = "CalibrOrient";
    private static final String _1202 = "120";
    private static final String _120 = _1202;
    private static final String _110 = "110";
    private static final String _135 = "135";
    private static final String _160 = "160";
    private static final String _185 = "185";
    private static final String _210 = "210";
    private static final String _235 = "235";

    private static final String _155 = "155";
    private static final String _190 = "190";
    private static final String _240 = "240";
    private static final String _300 = "300";
    private static final String _380 = "380";

    private static final String NOW = "NOW ";
    private static final String NO_DATA = "No Data";
    private static final String R_D_02D_02_0F_S_02D_02_0F = "R/D:%02d %02.0f%s%02d %02.0f";
    private static final String RA = "ra ";
    private static final String FILENAME2 = "filename ";

    private static final int requestCodePushCamera = 22;

    public static boolean redrawRequired = false;
    DscDialog dscDialog;

    CameraInterface myCamera = null;

    private CameraManager mCameraManager = null;


    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler = null;
    private PushCamPresenter pushCamPresenter;

    public static class DssImageRec {
        double ra;
        double dec;
        String name;

        public DssImageRec(double ra, double dec, String name) {
            super();
            this.ra = ra;
            this.dec = dec;
            this.name = name;
        }
    }

    public static interface DssImage {
        DssImageRec getInfo();

        void select();

        void delete();

        boolean isSelected();

        boolean isPotential();//rectangle depicting potential dss image
    }

    public CuV cView;//here we draw the sky
    private RelativeLayout layout;
    private BSp mSpinner;//here we set FOV
    //change arrays below when changing number of zooms
    public static String[] spinArr = new String[]{_380, _300, _240, _190, _155, _1202, _90, _60, _45, _30, _20, _10, _5, _2, _1, _0_5, _0_25, _0_12, _0_06};
    private static int max_fov = Integer.parseInt(spinArr[0]) + 5;
    private String[] spinArr2 = new String[]{_235, _210, _185, _160, _135, _110, _90, _60, _45, _30, _20, _10, _5, _2, _1, "30'", "15'", "7.2'", "3.6'", ""};

    public static final String TAG = "Graph";
    @MarkTAG
    private MyDateDialog dd;//used for running set date and set time requests
    private static final int raDecDialog = 3;
    private GraphRec prefs;
    private Artv mTextRT;//Time
    private Artv mTextRB;//here we show RA and DEC
    private Artv mTextLT;//here we show obj name
    private Artv mTextLB;//here we show az and alt
    private final int GET_CODE = 2;//for running dateTimePicker activity
    private final int SR_CODE = 3;//for returning from search
    private SensorEventListener mySensorListener = null;
    private SensorProcessor sensorProcessor = null;//new SensorProcessor();
    private int mPrevPos = 0;//previous position of spinner
    private volatile Bitmap dssBitmap;
    private Thread dssThread = null;
    private StarBoldness boldnessDialog = null;
    private StarMags starmagsDialog = null;
    private alexMenu aMenu;
    private View bottomBar;

    private FloatingActionButton floatingButton;
    private FloatingActionButton floatingButton2;

    boolean realtimeMode = false;
    boolean autoScopeMode = false;
    boolean mBound = false;
    DownloadService mService;
    InputDialog mZoomDialog;
    List<AstroObject> listNearby = new ArrayList<AstroObject>();
    AstroObject raDecCenter = null;//used by raDecDialog and cView.initDsoObjects
    volatile List<AstroObject> threadListNearby = new ArrayList<AstroObject>();
    PushCamBroadcastReceiver pushCamBroadcastReceiver;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "surface texture available");
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == requestCodePushCamera) {
            turnPushCameraOn();
        }
    }

    PushCamPresenter getPushCamPresenter() {
        return pushCamPresenter;
    }


    private void startMyCameraBackgroundThread() {
        if (myCamera != null) {
            mBackgroundThread = new HandlerThread("CameraBackground");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
            myCamera.setBackgroundHandler(mBackgroundHandler);
        }
    }


    private void stopMyCameraBackgroundThread() {
        if (mBackgroundThread != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBackgroundThread.quitSafely();
            }
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (myCamera != null)
            myCamera.setBackgroundHandler(null);
    }

    /**
     * connection to download service
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG, "onServiceConnected");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    BroadcastReceiver geoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (cView != null)//to try avoiding crash
                cView.sgrChanged(false);
        }
    };


    private void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.GEO_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(geoReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(Constants.BTCOMM_RADEC_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(btcommReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(Constants.DOWNLOAD_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(downloadReceiver, filter);

        filter = new IntentFilter();
        filter.addAction(Constants.PUSH_CAMERA_PHOTO_READY);
        LocalBroadcastManager.getInstance(this).registerReceiver(pushCamBroadcastReceiver.getPhotoReceiver(), filter);

    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(geoReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(btcommReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(downloadReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushCamBroadcastReceiver.getPhotoReceiver());

    }

    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                InputDialog.toast(message, getApplicationContext()).show();
            }
        });

    }

    BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int value = intent.getIntExtra(Constants.GRAPH_DOWNLOAD_STATUS, -1);
            switch (value) {
                case DSSdownloadable.REMOVE_DOWNLOAD_TEXT://remove download message
                    cView.topMessage.set("", TopMessage.DSS_UPLOAD_PRIORITY);
                    break;
                case DSSdownloadable.ADD_DOWNLOAD_TEXT:
                    cView.topMessage.set(context.getString(R.string.dss_image_downloading), TopMessage.DSS_UPLOAD_PRIORITY);
                    break;
                case DSSdownloadable.UPDATE_SKY:
                    UploadRec u = CuV.makeUploadRec();
                    if (Point.getFOV() <= SettingsActivity.getDSSZoom() && SettingsActivity.isDSSon()) {
                        Global.dss.uploadDSS(u, cView, handler);
                    }
                    break;

            }
        }
    };

    BroadcastReceiver btcommReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            double ra = intent.getDoubleExtra(Constants.BTCOMM_RA, -1);
            double dec = intent.getDoubleExtra(Constants.BTCOMM_DEC, -1);
            int scope = intent.getIntExtra(Constants.BTCOMM_SCOPE, -1);
            boolean trackingOff = intent.getBooleanExtra(Constants.BTCOMM_SCOPE_TRACKING_OFF, false);
            if (trackingOff) {
                InputDialog.toast(getString(R.string.auto_scope_mode_off), GraphActivity.this).show();
                autoScopeMode = false;
                return;
            }

            Log.d(TAG, "onReceive, ra=" + ra + " dec=" + dec + " scope=" + scope);

            if (ra == -1 && dec == -1) return;
            Log.d(TAG, "onReceive, isDialogShowing=" + dscDialog.isDialogShowing());
            boolean azSystem = false;
            if (scope == TelescopeDriver.DSC && dscDialog.isDialogShowing()) {
                dscDialog.onDataReceived(ra, dec);
                return;
            } else if (scope == TelescopeDriver.DSC) {
                Calendar c = Calendar.getInstance();
                XY star_scope = new XY(-ra, dec);
                AstroTools.RaDecRec rec = dscDialog.calculateRaDec(star_scope, c.getTimeInMillis());
                if (rec == null) {
                    azSystem = true;
                } else {
                    ra = rec.ra;
                    dec = rec.dec;
                }
            }

            if (azSystem) { //ra dec == az alt in fact
                Point.setCenterAz(ra, dec);
            } else {
                Point p = new Point(ra, dec);
                p.setXY();
                p.setDisplayXY();
                Point.setCenterAz(p.getAz(), p.getAlt());
            }
            setCenterLoc();
            UploadRec u = cView.makeUploadRec();
            cView.upload(u, false, -1);
            if (Point.getFOV() <= 5.1 && SettingsActivity.isDSSon()) {
                Global.dss.uploadDSS(u, cView, handler);
            }
        }
    };


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.arg1) {
                case 1:
                    listNearby = threadListNearby;
                    if (cView != null)
                        cView.invalidate();
                    break;
                case 2:

                    UploadRec u = CuV.makeUploadRec();
                    if (Point.getFOV() <= SettingsActivity.getDSSZoom() && SettingsActivity.isDSSon()) {
                        Global.dss.uploadDSS(u, cView, handler);
                    }

                    break;
                case 4://message from nearby
                    String message = getString(R.string.nearby_search);
                    if (cView != null)
                        cView.topMessage.set(message, cView.topMessage.NEARBY_SEARCH);
                    break;
                case 5:
                    if (cView != null)
                        cView.topMessage.set("", cView.topMessage.NEARBY_SEARCH);
                    break;
                case CuV.INVALIDATE:
                    if (cView != null)
                        cView.invalidate();
                    break;


            }
        }
    };

    private Handler initHandler = new Handler();

    private static final int NO_UPDATE_HIGH_ZOOM = 1;
    private static final int CENTER_HIGH_ZOOM = 2;

    //timer updating screen
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            boolean push_camera = SettingsActivity.isPushCameraOn(getApplicationContext());

            if (!SettingsActivity.isAutoTimeUpdating() && !push_camera)
                return; //skip this if deselected
            long update_period = SettingsActivity.getAutoTimeUpdatePeriod() * 1000;
            if (push_camera) {
                if (update_period > 2000)
                    update_period = 2000;
            }
            handler.postDelayed(this, update_period); //in ms

            int rt = SettingsActivity.getSharedPreferences(GraphActivity.this).getInt(Constants.REAL_TIME_DIALOG_CHOICE, 0);
            if (rt == NO_UPDATE_HIGH_ZOOM
                    && Point.getFOV() <= SettingsActivity.getRealTimeFOV() && !push_camera) return;

            dd.setDateTime(Calendar.getInstance());
            AstroTools.setDefaultTime(dd.getDateTime(), GraphActivity.this);
            Point.setLST(AstroTools.sdTime(dd.getDateTime()));
            setTimeLabel();
            cView.sgrChanged(false);
            if (rt == CENTER_HIGH_ZOOM && Point.getFOV() <= SettingsActivity.getRealTimeFOV())
                parseMenuEvent(R.id.center);
        }
    };

    private Runnable mUpdateScopeGoTask = new Runnable() {
        public void run() {
            if (!autoScopeMode) return;
            if (!SettingsActivity.isScopeGoStarChartAutoUpdate(getApplicationContext())) return;
            handler.postDelayed(this, (int) (1000 * SettingsActivity.getScopeGoStarChartAutoTimeUpdatePeriod(
                    getApplicationContext())));
            if (CommunicationManager.getComModule().isConnected())
                CommunicationManager.getComModule().read();
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, DownloadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        registerReceivers();
        registerSensorListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        unregisterReceivers();
        unregisterSensorListener();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_CODE && resultCode == RESULT_OK) {//from DateTimePicker
            long time = data.getLongExtra(Constants.DATE_TIME_PICKER_MILLIS, Calendar.getInstance().getTimeInMillis());
            Calendar c = Calendar.getInstance();

            c.setTimeInMillis(time);
            AstroTools.setDefaultTime(c, this);

            Calendar defc = c;
            Point.setLST(AstroTools.sdTime(defc));
            if (prefs != null) prefs.c = defc;
            if (dd != null) dd.setDateTime(defc);
            if (cView != null) cView.sgrChanged(false);
        }
        if (requestCode == AstroTools.SR_CODE) {//from search result activity
            restart();
        }
    }

    @Override
    protected void onPause() {
        if (myCamera != null) {
            if (myCamera.isOpen()) {
                myCamera.closeCamera();
            }
            myCamera.stopProcessingThreads();
            stopMyCameraBackgroundThread();
        }
        if (pushCamPresenter != null)
            pushCamPresenter.moveGyroToPauseIfRequired();


        super.onPause();
        Log.d(TAG, "onPause start");
        handler.removeCallbacks(mUpdateTimeTask);
        handler.removeCallbacks(mUpdateScopeGoTask);
        int zPos = mSpinner.gtSelectedItemPosition();

        //set centered for the constellation is false so that after new start star chart is not centered on the constellation
        prefs = new GraphRec(zPos, Point.getAzCenter(), Point.getAltCenter(), AstroTools.getDefaultTime(this),
                cView.getObjCursor().getObjSelected(), cView.getSelectedConBoundary(), false);
        prefs.save(this);

        SettingsActivity.putSharedPreferences(Constants.CURRENT_ZOOM_LEVEL, zPos, this);

        Global.dss.clearDssList();//freeing memory

        StarMags.putMagLimitsToSharedPrefs(this);
        StarBoldness.putBoldnesstoPrefs();
        ChartFlipper.saveToPrefs();
        StarUploadThread.cacheManager.stop();
        Log.d(TAG, "onPause stop");

        if (SettingsActivity.isPushCameraOn(this)) {
            new PushCamSaveRestore(this, pushCamBroadcastReceiver).save();
            if (pushCamPresenter != null)
                pushCamPresenter.save();
        }

        unregisterReceivers();

    }

    private double gx = 0;

    boolean listenerRegistered = false;

    private void registerSensorListener() {
        if (listenerRegistered) {
            unregisterSensorListener();
        }
        boolean pushCamera = SettingsActivity.isPushCameraOn(getApplicationContext());

        boolean autoSky = SettingsActivity.isAutoSkyOn();
        boolean autoRotation = SettingsActivity.isAutoRotationOn();
        boolean any = pushCamera || autoSky || autoRotation;
        if (mySensorListener == null && any) {
            sensorProcessor.init();
            if (pushCamera) {
                mySensorListener = new SensorEventListener() {

                    public void onSensorChanged(SensorEvent event) {
                        int type = event.sensor.getType();
                        if (type == Sensor.TYPE_GAME_ROTATION_VECTOR)
                            sensorProcessor.processEvent(event);

                    }

                    public void onAccuracyChanged(Sensor sensor, int accuracy) {

                    }
                };
            } else {
                mySensorListener = new SensorEventListener() {

                    public void onSensorChanged(SensorEvent event) {
                        sensorProcessor.processEventCompass(event);
                    }

                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    }
                };
            }
            SensorManager myManager = (SensorManager) getSystemService(SENSOR_SERVICE);


            if (myManager != null) {
                listenerRegistered = true;
                boolean highSpeed = SettingsActivity.isSensorHighSpeed(this);
                int speed = highSpeed ? SensorManager.SENSOR_DELAY_FASTEST : SensorManager.SENSOR_DELAY_UI;
                myManager.registerListener(mySensorListener,
                        myManager.getDefaultSensor(pushCamera ? Sensor.TYPE_GAME_ROTATION_VECTOR : Sensor.TYPE_ROTATION_VECTOR),
                        speed);

                Log.d(TAG, "pushcamera=" + pushCamera + " autosky=" + autoSky + " autorotation=" + autoRotation);
                Log.d(TAG, "register sensor listener " + mySensorListener + " at " + GraphActivity.this);
            }
        }

    }

    private void unregisterSensorListener() {
        SensorManager myManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (myManager != null) {

            if (mySensorListener != null) {
                listenerRegistered = false;
                myManager.unregisterListener(mySensorListener);
                Log.d(TAG, "unregister sensor listener " + mySensorListener + " at " + GraphActivity.this);
                mySensorListener = null;
                Point.setRotAngle(0);
            }

        }
    }

    private FloatingActionButton getPushCamButton() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int height = display.getHeight();
        final FloatingActionButton fabButtonPushcam = new FloatingActionButton.Builder(this)
                .withDrawable(getResources().getDrawable(R.drawable.ram_btn_menu))
                .withButtonColor(0x30ff0000)
                .withGravity(Gravity.BOTTOM | Gravity.RIGHT)
                .withMargins(0, 0, 5, Math.max(height / 2, FloatingActionButton.FLOATING_BUTTON_SIZE))
                .create();


        PushCamImplementation pushCamImplementation = new PushCamImplementation(myCamera, getApplicationContext(), sensorProcessor, initHandler, cView, pushCamBroadcastReceiver);
        pushCamImplementation.setFabButtonPushcam(fabButtonPushcam);
        pushCamPresenter = new PushCamPresenter(pushCamImplementation, this);
        pushCamImplementation.setPushCamPresenter(pushCamPresenter);
        pushCamBroadcastReceiver.setPushCamPresenter(pushCamPresenter);

        return fabButtonPushcam;
    }

    @Override
    protected void onResume() {
        super.onResume();
        onResumeCode();
    }

    /**
     * used to correctly manage global init
     */
    private void onResumeCode() {
        if (redrawRequired) {
            redrawRequired = false;
            InputDialog.toast("Restarting Star Chart", getApplicationContext()).show();
            restart();
        }

        StarUploadThread.cacheManager.init();
        init(prefs);
        cView.initDsoList();
        cView.initSettingsToDrawMap();
        if (realTimeMode(SettingsActivity.isAutoTimeUpdating() || SettingsActivity.isPushCameraOn(getApplicationContext()))) {
            handler.postDelayed(mUpdateTimeTask, 2000);
        }
        if (SettingsActivity.isScopeGoStarChartAutoUpdate(getApplicationContext())) {
            handler.postDelayed(mUpdateScopeGoTask, 1000);
        }


        cView.clearListsIfRequired();
        setCenterLoc();
        UploadRec u = CuV.makeUploadRec();
        if (Point.getFOV() <= SettingsActivity.getDSSZoom() && SettingsActivity.isDSSon()) {
            Global.dss.uploadDSS(u, cView, handler);
        }
        Point.setRotAngle(0);
        sensorProcessor.init();
        SettingsActivity.setAntialiasing();
        if (nearbyDialog) {
            nearbyDialog = false;
            parseMenuEvent(R.id.nbyGraph);
        }

        setMenuBtnOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                doMenu(bottomBar);

            }
        });

        if (SettingsActivity.getBooleanFromSharedPreferences(getApplicationContext(),
                Constants.FIRST_TIME_ENTRY_GRAPH, true)) {
            SettingsActivity.putSharedPreferences(Constants.FIRST_TIME_ENTRY_GRAPH,
                    false, getApplicationContext());
            registerDialog(InputDialog.message(GraphActivity.this, getString(R.string.menu_warning), 0)).show();

        }
        if (SettingsActivity.isCameraPermissionValid(this) && SettingsActivity.isPushCameraOn(this))
            startMyCameraBackgroundThread();
        if (Prefs.TESTING_PUSHCAM)
            InputDialog.toast("TESTING MODE!!!", getApplicationContext()).show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case MyDateDialog.DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        dd.mDateSetListener,
                        dd.mYear, dd.mMonth, dd.mDay);
            case MyDateDialog.TIME_DIALOG_ID:
                Log.d(TAG, "hour=" + dd.mHour);
                return new TimePickerDialog(this,
                        dd.mTimeSetListener,
                        dd.mHour, dd.mMinute, false);
            case raDecDialog:
                final InputDialog d2 = new InputDialog(this);
                d2.setTitle(getString(R.string.set_center_s_ra_dec_2000_));
                d2.insertLayout(R.layout.getradec);
                d2.setPositiveButton(getString(R.string.ok), new OnButtonListener() {
                    public void onClick(String s) {
                        Double ra = AstroTools.getRaDecValue(
                                ((EditText) (d2.findViewById(R.id.radec_ra))).getText().toString());
                        Double dec = AstroTools.getRaDecValue(
                                ((EditText) (d2.findViewById(R.id.radec_dec))).getText().toString());
                        if ((ra != null) && (dec != null)) {
                            Point p = new Point(ra, dec);
                            p.setXY();
                            Point.setCenterAz(p.getAz(), p.getAlt());
                            setCenterLoc();
                            raDecCenter = new CenterMarkObject(ra, dec);

                            cView.upload(cView.makeUploadRec(), false, -1);
                        }
                    }
                });
                d2.setNegativeButton(getString(R.string.cancel));
                return d2;

        }

        return super.onCreateDialog(id);
    }

    private Dialog makeDSSselectionDialog(final GraphActivity.DssImage image) {
        final InputDialog d0 = new InputDialog(this);
        String[] sel;
        if (image.isPotential())
            sel = new String[]{getString(R.string.info), getString(R.string.download)};
        else
            sel = new String[]{getString(R.string.info), getString(R.string.remove)};
        d0.setTitle(getString(R.string.select_dss_action));
        d0.setPositiveButton(""); //disable
        d0.setValue("-1"); //remove checks
        d0.setNegativeButton(getString(R.string.cancel));
        d0.setListItems(sel, new InputDialog.OnButtonListener() {
            public void onClick(final String value) {
                final int i = AstroTools.getInteger(value, -1, -2, 1000);
                if (i == -1) return; //nothing selected
                switch (i) {
                    case 0:
                        DssImageRec rec = image.getInfo();
                        String raStr = DetailsActivity.doubleToGrad(rec.ra, 'h', 'm');
                        String decStr = DetailsActivity.doubleToGrad(rec.dec, '\u00B0', (char) 39);
                        String fileName = "".equals(rec.name) ? "" : FILENAME2 + rec.name;
                        String message = RA + raStr + "\n" + "dec " + decStr + "\nsize 30'\n" + fileName;
                        registerDialog(InputDialog.message(GraphActivity.this, message, 0)).show();
                        break;
                    case 1:
                        if (image.isPotential()) {
                            //download


                            if (mBound)
                                Global.dss.downloadDSS(new Point(image.getInfo().ra, image.getInfo().dec), cView, mService);
                        } else {
                            image.delete();
                            cView.invalidate();
                        }
                        break;
                }
            }
        });
        return d0;
    }

    private Dialog makeDSSdialog() {
        final InputDialog d0 = new InputDialog(GraphActivity.this);
        String[] sel = {getString(R.string.single_image_small_object_), getString(R.string.nine_images_larger_object_), getString(R.string.show_dss_images), getString(R.string.show_dss_contours)};
        boolean on = true;
        if (SettingsActivity.isDSSon()) {
            sel[2] = getString(R.string.hide_dss_images);
            on = false;
        }

        if (SettingsActivity.areDSScontoursOn()) {
            sel[3] = getString(R.string.hide_dss_contours);
        }
        final boolean setDSSon = on;

        d0.setTitle(getString(R.string.how_much_to_download_));
        d0.setPositiveButton(""); //disable
        d0.setValue("-1"); //remove checks
        d0.setNegativeButton(getString(R.string.cancel));
        d0.setListItems(sel, new InputDialog.OnButtonListener() {
            public void onClick(final String value) {
                Log.d(TAG, "makeDSSdialog, value=" + value);
                final int i = AstroTools.getInteger(value, -1, -2, 1000);
                if (i == -1) return; //nothing selected
                if (i == 2) {
                    SettingsActivity.setDSSon(setDSSon);//hide/show dss images
                    cView.initSettingsToDrawMap();
                    if (setDSSon) {
                        if (Point.getFOV() <= 5.1) {
                            UploadRec u = CuV.makeUploadRec();
                            Global.dss.uploadDSS(u, cView, handler);
                        }
                    } else
                        cView.invalidate();
                    return;
                } else if (i == 3) {
                    boolean contours = SettingsActivity.areDSScontoursOn();
                    SettingsActivity.setDSScontours(!contours, getApplicationContext());
                    cView.initSettingsToDrawMap();
                    cView.invalidate();
                    return;
                }
                InputDialog d = new InputDialog(GraphActivity.this);
                d.setTitle(getString(R.string.please_confirm));
                d.setMessage(getString(R.string.the_download_is_about_to_start_));
                d.disableBackButton(true);
                d.setPositiveButton(getString(R.string.ok), new OnButtonListener() {
                    public void onClick(String s) {
                        ObjCursor o = cView.getObjCursor();
                        if (o != null) {
                            //Global.context = Graph.this;
                            switch (i) {
                                case 0:
                                    Global.dss.downloadDSS(o, cView, mService);
                                    break;
                                case 1:
                                    Global.dss.downloadDSSimages(o, cView, mService);
                                    break;

                            }

                            Log.d(TAG, "Start DSS " + value + " " + i);
                        }
                    }
                });
                d.setNegativeButton(getString(R.string.cancel), new InputDialog.OnButtonListener() {
                    @Override
                    public void onClick(String value) {
                        d0.dismiss();
                    }
                });
                registerDialog(d).show();
            }
        });
        return d0;
    }


    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case MyDateDialog.TIME_DIALOG_ID:
                ((TimePickerDialog) dialog).updateTime(dd.mHour, dd.mMinute);
                break;
            case MyDateDialog.DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(dd.mYear, dd.mMonth, dd.mDay);
                break;
        }
    }

    private void update() { //needed for MyDateDialog, sky updating

        Calendar defc = dd.getDateTime();
        AstroTools.setDefaultTime(defc, this);
        Point.setLST(AstroTools.sdTime(defc));//setting static lst in Point class as sky is drawn on its basis
        // 	cView.invalidate();
        //SAND new algorithm mTimeChanged=true;
        cView.sgrChanged(false);
        setTimeLabel();
    }

    private Calendar def_time = null;//for calling from NoteList
    private boolean init_first_time_over = false;

    private void init(GraphRec gr) {
        if (gr == null) return;
        Log.d(TAG, "Graph, onResume. setting fov=" + spinArr[gr.FOV]);
        Point.setFOV(Double.parseDouble(spinArr[gr.FOV]));//resetting fov
        boldnessDialog.setFov(Point.getFOV());
        if (callingActivity == Constants.NOTE_LIST_GRAPH_CALLING && !init_first_time_over) {
            def_time = AstroTools.getDefaultTime(getApplicationContext());
            init_first_time_over = true;
        }

        AstroTools.setDefaultTime(gr.c, this);
        if (cView.global_object != null)
            cView.global_object.raiseNewPointFlag();//update internal calculations of az,alt as time has changed
        int obsList = SettingsActivity.getSharedPreferences(GraphActivity.this).getInt(Constants.ACTIVE_OBS_LIST, InfoList.PrimaryObsList);
        Iterator it = ListHolder.getListHolder().get(obsList).iterator();
        for (; it.hasNext(); ) {
            Object o = it.next();
            if (o instanceof ObsInfoListImpl.Item) {
                ((ObsInfoListImpl.Item) o).x.raiseNewPointFlag();
            }
        }
        for (Planet pl : Global.planets) {
            pl.raiseNewPointFlag();
            pl.recalculateRaDec(gr.c);
        }

        ObjCursor o = new ObjCursor(0, 0);
        Point p;
        //setting object cursor on object

        p = gr.obj;
        if (p == null)
            p = cView.global_object;


        if (p != null) {
            o.setRaDec(p.getRa(), p.getDec());
            o.setObjSelected(p);
        }

        cView.setObjCursor(o);

        Point.setLST(AstroTools.sdTime(gr.c)); //setting current time for looking through the sky
        Point.setCurrentTime(gr.c);

        //this allows to center on last selected object if no object is passed
        //actually this never happens now as the only place from where the null object is passed
        //in dso main sky view now uses the last saved Global.graphCreate
        double azCenter = gr.azCenter;
        double altCenter = gr.altCenter;


        if (gr.obj == null && cView.global_object != null) {
            azCenter = cView.global_object.getAz();
            altCenter = cView.global_object.getAlt();
        }
        Point.setCenterAz(azCenter, altCenter);
        cView.conFigureRaiseNewPointFlag();

        if (gr.selected_con != GraphRec.NO_CONSTELLATION_SELECTED) {
            cView.initSelectedConBoundary(gr.selected_con, gr.set_centered);

        }


        mSpinner.stSelection(gr.FOV);//updating the screen
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("destroyed", true);
        Log.d(TAG, "onSaveInstanceState");
    }

    private boolean control() {
        class location {
            double lat;
            double lon;

            public location(double lat, double lon) {
                this.lat = lat;
                this.lon = lon;
            }

        }
        location[] loc = new location[]{new location(53, 41), new location(44, 42)};
        double lattitude = SettingsActivity.getLattitude();
        double longitude = SettingsActivity.getLongitude();
        boolean flag = false;
        for (location l : loc) {
            if ((Math.abs(lattitude - l.lat) < 2) && (Math.abs(longitude - l.lon) < 2)) {
                flag = true;
            }
        }
        return flag;
    }


    int callingActivity = 0;
    SurfaceHolder surfaceHolder;
    TextureView textureView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        nightMode = SettingsActivity.setDayNightSky(this);
        callingActivity = getIntent().getIntExtra(Constants.GRAPH_CALLING, 0);
        Log.d(TAG, "calling act on create=" + callingActivity);

        spinArr2[spinArr2.length - 1] = getString(R.string.cancel);

        boolean night_mode = SettingsActivity.getNightMode();
        boolean onyx = SettingsActivity.getDarkSkin();
        if (!night_mode && !onyx) {
            SettingsActivity.changeNavBarBackground(getWindow(), getApplicationContext());
        }

        sensorProcessor = new SensorProcessor(this);
        pushCamBroadcastReceiver = new PushCamBroadcastReceiver(this, sensorProcessor);

        prefs = new GraphRec(GraphActivity.this);//loading prefs from shared preferences

        handler.removeCallbacks(mUpdateTimeTask);
        handler.removeCallbacks(mUpdateScopeGoTask);


        dd = new MyDateDialog(GraphActivity.this, AstroTools.getDefaultTime(GraphActivity.this), new MyDateDialog.Updater() {//after setting date or time need to update the sky
            public void update() {
                GraphActivity.this.update();
            }
        });


        //MAIN SCREEN LAYOUT DEFINITION
        layout = new RelativeLayout(GraphActivity.this);


        //BOTTOM BAR GADGET
        //=========================
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (SettingsActivity.isCameraApi(this))
            bottomBar = vi.inflate(R.layout.topbar, null, false);
        else
            bottomBar = vi.inflate(R.layout.topbar2, null, false);

        mTextLT = (Artv) bottomBar.findViewById(R.id.tbL1);
        mTextRT = (Artv) bottomBar.findViewById(R.id.tbR1);
        mTextLB = (Artv) bottomBar.findViewById(R.id.tbL2);
        mTextRB = (Artv) bottomBar.findViewById(R.id.tbR2);

        //spinner replacement
        mSpinner = (BSp) bottomBar.findViewById(R.id.zSpinner);
        mSpinner.init(GraphActivity.this);

        mZoomDialog = new InputDialog(GraphActivity.this);
        mZoomDialog.setType(InputDialog.DType.INPUT_DROPDOWN);
        mZoomDialog.setTitle("");
        mZoomDialog.setListItems(spinArr2, new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                int pos = -1;
                for (int i = 0; i < spinArr2.length; i++) {
                    if (spinArr2[i].equals(value)) {
                        pos = i;
                        break;
                    }
                }
                if ((pos != -1) && (pos < spinArr.length))
                    mSpinner.stSelection(spinArr[pos]);
            }
        });

        mSpinner.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (SettingsActivity.isFOVcolumn(getApplicationContext()))
                    registerDialog(mZoomDialog).show();
            }
        });


        //Zoom buttons, etc
        OnClickListener ocl1 = new OnClickListener() {
            public void onClick(View v) {
                zoomChange(true);
            }
        };
        OnClickListener ocl2 = new OnClickListener() {
            public void onClick(View v) {
                zoomChange(false);
            }
        };
        OnLongClickListener ocl3 = new OnLongClickListener() {
            public boolean onLongClick(View v) {
                //openOptionsMenu(); SAND old menu style
                if (ALEX_MENU_FLAG) doMenu(v);
                return true;
            }
        };
        TextView b1 = (TextView) bottomBar.findViewById(R.id.zIn);
        b1.setOnClickListener(ocl1);
        b1.setOnLongClickListener(ocl3);

        TextView b2 = (TextView) bottomBar.findViewById(R.id.zOut);
        b2.setOnClickListener(ocl2);
        b2.setOnLongClickListener(ocl3);

        if (nightMode) {
            b1.setBackgroundResource(R.drawable.zdbtnl);
            mSpinner.setBackgroundResource(R.drawable.zdbtnc);
            b2.setBackgroundResource(R.drawable.zdbtnr);
        }
        mSpinner.setOnLongClickListener(ocl3);

        //Top bar's L and R zones
        OnClickListener oclTop = new OnClickListener() {
            public void onClick(View v) {
                topBarClick(v);
            }
        };
        View bL = bottomBar.findViewById(R.id.leftPane);
        bL.setOnClickListener(oclTop);
        View bR = bottomBar.findViewById(R.id.rightPane);
        bR.setOnClickListener(oclTop);
        bottomBar.bringToFront();


        //BOLDNESS GADGET (HIDDEN)
        //=========================

        boldnessDialog = new StarBoldness(GraphActivity.this);//StarBoldness.getInstance(Graph1243.this);
        boldnessDialog.Init();
        boldnessDialog.bringToFront();

        ChartFlipper.setFromString(SettingsActivity.getChartFlipperData());

        starmagsDialog = new StarMags(GraphActivity.this);//StarMags.getInstance(Graph1243.this);
        starmagsDialog.Init();
        starmagsDialog.bringToFront();

        //CUSTOM VIEW (SKY)
        //=========================
        cView = (CuV) bottomBar.findViewById(R.id.sky_view); //new CustomView(this);

        cView.global_object = SettingsActivity.getObjectFromSharedPreferencesNew(Constants.GRAPH_OBJECT, GraphActivity.this);
        Log.d(TAG, "global_object=" + cView.global_object);

        layout.addView(bottomBar);
        layout.addView(boldnessDialog); //hidden boldness control
        layout.addView(starmagsDialog);

        if (SettingsActivity.getInverseSky() && !nightMode)
            layout.setBackgroundColor(0xffffffff);
        else
            layout.setBackgroundColor(0xff000000);

        setContentView(layout);
        layout.requestFocus();

        if (SettingsActivity.isCameraApi(this)) {
            SurfaceView preview = (SurfaceView) findViewById(R.id.SurfaceView01);
            surfaceHolder = preview.getHolder();
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        } else {
            textureView = (TextureView) findViewById(R.id.TextureView01);
            textureView.setSurfaceTextureListener(this);
        }


        //Object cursor
        ObjCursor.setParameters(PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()));

        //CUSTOM ALEX MENU
        if (ALEX_MENU_FLAG) initAlexMenu(GraphActivity.this);

        Global.lockCursor = false;

        SettingsActivity.setAntialiasing();
        realTimeMode(SettingsActivity.isAutoTimeUpdating());


        if (CommunicationManager.getComModule().isConnected() && SettingsActivity.isScopeGoStarChartAutoUpdate(getApplicationContext())) {
            autoScopeMode = true;
        }
        dscDialog = new DscDialog(GraphActivity.this);


        if (SettingsActivity.isPushCameraOn(getApplicationContext())) {
            activatePushCam();
        }
    }

    private void activatePushCam() {
        if (SettingsActivity.isCameraPermissionValid(this)) {
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            if (SettingsActivity.isCameraApi(this))
                myCamera = new CameraAPIService(surfaceHolder, this);
            else
                myCamera = new Camera2APIService(mCameraManager, "0", getApplicationContext());
            floatingButton = getPushCamButton();
            new PushCamSaveRestore(this, pushCamBroadcastReceiver).restore();
            if (cView.getObjCursor() != null) {
                Point p = cView.getObjCursor().getObjSelected();
                if (pushCamPresenter != null)
                    pushCamPresenter.setSelectedObject(p);
            }
        } else {
            InputDialog.message(this, getString(R.string.push_camera_could_not_work), 0).show();
        }

    }

    private void deactivatePushCam() {
        if (floatingButton != null) {
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            root.removeView(floatingButton);
        }
        if (myCamera != null) {
            if (myCamera.isOpen()) {
                myCamera.closeCamera();
            }
            myCamera.stopProcessingThreads();
            stopMyCameraBackgroundThread();
            myCamera = null;
        }
        if (pushCamPresenter != null)
            pushCamPresenter.clearSharedPrefs();
        pushCamPresenter = null;
        new PushCamSaveRestore(this, pushCamBroadcastReceiver).clearSharedPrefs();
        sensorProcessor.stop();
    }


    //What to do on zoom item click
    public void performZoomItemSelect(String value) {
        double fov = AstroTools.getDouble(value, -1, -1, max_fov);
        Log.d(TAG, "value=" + value + " fov=" + fov);
        if (fov > 0) {

            Point.setFOV(fov);//resetting fov
            cView.FOVchanged();
            boldnessDialog.setFov(fov);
            starmagsDialog.updateRangeSlider();
        }
    }

    //Top bar buttons common events handler
    private void topBarClick(View v) {
        switch (v.getId()) {
            case R.id.leftPane: //left side of the bar
                if (SettingsActivity.getCenterObjectStatus()) {
                    //first center it
                    parseMenuEvent(R.id.center);
                }
                //then show the info graph
                parseMenuEvent(R.id.infoGraph);
                break;
            case R.id.rightPane: //right side of the bar
                parseMenuEvent(R.id.pickDateTime);
                break;
        }
    }

    //Left bottom field (object A/h)
    public void setLocationLabel(String s) { //updating az/alt text, called from custom view
        mTextLB.setText(s);
    }

    //Right bottom field (dynamic RA/DEC)
    public void setCenterLoc() { //updating date/time text, called from custom view

        double dec = Point.getDec(Point.getAzCenter(), Point.getAltCenter());
        double ra = Point.getRa(Point.getAzCenter(), Point.getAltCenter());
        //SAND:Log.d(TAG,"center ra="+ra+" dec="+dec);
        Point p = new Point(ra, dec);
        p = AstroTools.precession(p, AstroTools.getDefaultTime(this));
        p.ra = (float) AstroTools.normalise24(p.ra);
        p.dec = (float) AstroTools.normalise90(p.dec);

        boolean neg = (p.dec < 0);
        DMS r = AstroTools.d2dms(p.ra);
        DMS d = AstroTools.d2dms(neg ? -p.dec : p.dec);

        mTextRB.setText(String.format(Locale.US, R_D_02D_02_0F_S_02D_02_0F, r.d, r.m + (r.s / 60f + 0.5), (neg ? "-" : "+"), d.d, d.m + (d.s / 60f + 0.5)));
    }

    //Left top field (obj name)
    public void setObjName() { //updating date/time text, called from custom view

        mTextLT.setText(cView.getSelectedObjectName());
    }

    //Right top field (date/time)
    public void setTimeLabel() { //updating date/time text, called from custom view
        //	Log.d(TAG,"calendar="+Global.cal);
        Calendar defc = AstroTools.getDefaultTime(this);
        int rt = SettingsActivity.getSharedPreferences(GraphActivity.this).getInt(Constants.REAL_TIME_DIALOG_CHOICE, 0);
        boolean rtb = true;
        if (rt == NO_UPDATE_HIGH_ZOOM
                && Point.getFOV() <= SettingsActivity.getRealTimeFOV()) {
            rtb = false;
        }

        if (realtimeMode && rtb) //show time only with seconds
            mTextRT.setText(NOW + DetailsActivity.makeTimeString(defc, true));
        else
            mTextRT.setText(DetailsActivity.makeShortDateTimeString(defc));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.graph_menu, menu);
        //setMenuBackground(); Disabled
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return parseMenuEvent(item.getItemId());
    }

    public static AstroObject getDbObj(int catalog, int id, Context context) {
        DbListItem item = DbManager.getDbListItem(catalog);
        Log.d(TAG, "catalog=" + catalog + " item=" + item);
        AstroCatalog db;
        if (catalog == AstroCatalog.NGCIC_CATALOG)
            db = new NgcicDatabase(context);
        else if (item.ftypes.isEmpty())
            db = new CustomDatabase(context, item.dbFileName, catalog);
        else
            db = new CustomDatabaseLarge(context, item.dbFileName, catalog, item.ftypes);

        ErrorHandler eh = new ErrorHandler();
        db.open(eh);
        if (eh.hasError()) {
            return null;
        }
        String s = Constants._ID + " = " + id;
        List<AstroObject> list = db.search(s);
        db.close();
        if (list.size() > 0)
            return list.get(0);
        else
            return null;
    }

    private boolean compareUGCObjects(AstroObject o1, AstroObject o2) {
        boolean resra = Math.abs(o1.getRa() - o2.getRa()) < 0.001;//&&
        boolean resdec = Math.abs(o1.getDec() - o2.getDec()) < 0.001;//&&
        boolean resa = Math.abs(o1.getA() - o2.getA()) < 0.001;//&&
        Log.d(TAG, o1.getA() + " " + o2.getA());
        boolean resb = Math.abs(o1.getB() - o2.getB()) < 0.001;//&&
        Log.d(TAG, o1.getB() + " " + o2.getB());
        boolean resname = o1.getShortName().equals(o2.getShortName());
        Log.d(TAG, "res=" + resra + " " + resdec + " " + resa + " " + resb + " " + resname);
        return resra && resdec && resa && resb && resname;
    }

    /**
     * The method checks if camera permission was granted before turning camera on
     */
    private void turnPushCameraOn() {
        if (SettingsActivity.isCameraPermissionValid(this)) {
            SettingsActivity.setPushCamera(getApplicationContext(), true);
            registerSensorListener();
            activatePushCam();
            if (!SettingsActivity.isAutoTimeUpdating()) {
                if (realTimeMode(true)) {
                    handler.postDelayed(mUpdateTimeTask, 2000);
                }
            }
            InputDialog.message(this, "PushCamera turned on", 0).show();
        } else {
            InputDialog.message(this, getString(R.string.push_camera_could_not_work), 0).show();
        }
    }

    private void resetPushCamToCalibration() {
        sensorProcessor.stop();
        if (pushCamPresenter != null) {
            pushCamPresenter.clearSharedPrefs();
            pushCamPresenter.reset();
        }
        new PushCamSaveRestore(GraphActivity.this, pushCamBroadcastReceiver).clearSharedPrefs();
    }

    public boolean parseMenuEvent(int id) {
        switch (id) {
            case R.id.center://centering selected object
                ObjCursor oc = cView.getObjCursor();
                if (oc != null) {
                    if (Point.coordSystem)
                        Point.setCenterAz(oc.getAz(), oc.getAlt());

                    setCenterLoc();
                    UploadRec u = CuV.makeUploadRec();
                    cView.upload(u, false, -1);
                    if (Point.getFOV() <= 5.1 && PreferenceManager.getDefaultSharedPreferences(Global.getAppContext()).getBoolean(getString(R.string.dsson), true)) {

                        Global.dss.uploadDSS(u, cView, handler);
                    }
                }
                return true;

            case R.id.radecGraph:
                showDialog(raDecDialog);
                return true;

            case R.id.updateGraph://updating sky to the current time, not called now

                Calendar defc = Calendar.getInstance();
                AstroTools.setDefaultTime(defc, this);
                Point.setLST(AstroTools.sdTime(defc));
                dd.setDateTime(defc);
                cView.invalidate();
                cView.sgrChanged(false);
                setTimeLabel();
                return true;

            case R.id.pickDateTime://changing date

                SettingsActivity.putSharedPreferences(Constants.DTP_DISPLAY_MODE, DateTimePickerActivity.BOTH, this);
                SettingsActivity.putSharedPreferences(Constants.DTP_TIME, dd.getDateTime().getTimeInMillis(), this);
                startActivityForResult(new Intent(this, DateTimePickerActivity.class), GET_CODE);

                return true;
            case R.id.setDateGraph://changing date
                realTimeMode(false);
                dd.startDateDialog();
                return true;

            case R.id.setTimeGraph://changing time
                realTimeMode(false);
                dd.startTimeDialog();
                return true;

            case R.id.settingsGraph:
                startActivity(new Intent(this, SettingsGraphActivity.class));
                return true;

            case R.id.itemPlusHour:
                realTimeMode(false);
                Log.d(TAG, "before plus hour " + dd.getDateTime());
                dd.plusHour();
                AstroTools.setDefaultTime(dd.getDateTime(), this);
                Log.d(TAG, "after plus hour " + dd.getDateTime());
                Point.setLST(AstroTools.sdTime(dd.getDateTime()));
                cView.invalidate();
                cView.sgrChanged(false);
                setTimeLabel();
                return true;

            case R.id.itemMinusHour:
                realTimeMode(false);
                dd.minusHour();
                AstroTools.setDefaultTime(dd.getDateTime(), this);
                Point.setLST(AstroTools.sdTime(dd.getDateTime()));
                cView.invalidate();
                cView.sgrChanged(false);
                setTimeLabel();
                return true;

            case R.id.orientPlus://rotating sky view upside down
                Point.orientationAngle += 180;
                if (Point.orientationAngle == 360) {
                    Point.orientationAngle = 0;
                }
                ChartFlipper.setRotated(Point.orientationAngle == 180);
                cView.invalidate();
                return true;
            case R.id.mirror://mirroring sky view
                Point.mirror = -Point.mirror;
                ChartFlipper.setMirrored(Point.mirror < 0);
                cView.invalidate();
                return true;
            case R.id.nbyGraph://

                Log.d(TAG, "nby menu");

                if (isNearbySearchRunning()) {
                    registerDialog(InputDialog.message(this, R.string.search_nearby_running)).show();
                    return true;
                }
                registerDialog(getNearbySearchDialog()).show();
                return true;

            case R.id.infoGraph://getting info on selected object/star
                if (cView.getObjCursor() != null) {
                    Point p = cView.getObjCursor().getObjSelected();
                    if (p instanceof AstroObject) {
                        AstroObject obj = (AstroObject) p;
                        if (obj.getCatalog() > NgcFactory.LAYER_OFFSET) {
                            int id1 = obj.getId();
                            AstroObject o = getDbObj(obj.getCatalog() - NgcFactory.LAYER_OFFSET, id1, getApplicationContext());
                            if (o != null) {
                                p = o;
                            }
                        } else if (obj.getCatalog() == AstroCatalog.UCAC4_CATALOG) {
                            obj.con = AstroTools.getConstellation(obj.ra, obj.dec);
                        }
                        DetailsCommand command = new DetailsCommand((AstroObject) p, this);
                        command.setCallerGraph();
                        command.execute();
                    }


                }
                return true;

            case R.id.calibrGraph://running calibration screen

                if (cView.getObjCursor() != null) {
                    Point p = cView.getObjCursor().getObjSelected();
                    if (p != null)
                        calibrationDialog(p);
                } else {
                    registerDialog(InputDialog.message(GraphActivity.this, R.string.select_object_first)).show();
                }
                return true;
            case R.id.dscGraph:
                if (cView.getObjCursor() != null) {
                    Point p = cView.getObjCursor().getObjSelected();
                    if (p != null) {
                        Dialog d = dscDialog.create();
                        registerDialog(d).show();
                    }
                } else {
                    registerDialog(InputDialog.message(GraphActivity.this, R.string.select_object_first)).show();
                }
                return true;

            case R.id.pushCamera:
                boolean autoSky = SettingsActivity.isAutoSkyOn();

                boolean level = SettingsActivity.isAutoRotationOn();
                if (autoSky || level) {
                    registerDialog(InputDialog.message(GraphActivity.this, "Please turn Compass mode off first!", 0)).show();
                    return true;
                }

                boolean status = SettingsActivity.isPushCameraOn(getApplicationContext());
                if (status) {
                    final InputDialog d0 = new InputDialog(GraphActivity.this);
                    String[] sel = {"Turn push camera off", "Calibrate"};//getString(R.string.show_hide)
                    d0.setTitle(getString(R.string.select_action));
                    d0.setValue("-1"); //remove checks
                    d0.setNegativeButton(getString(R.string.cancel));
                    d0.setListItems(sel, new InputDialog.OnButtonListener() {
                        public void onClick(final String value) {
                            if ("0".equals(value)) { // turn push camera off
                                SettingsActivity.setPushCamera(getApplicationContext(), false);
                                deactivatePushCam();
                                //floatingButton.hideFloatingActionButton();
                                Point.setRotAngle(0);
                                cView.unlimitStarsPushCamera();
                                cView.invalidate();
                                if (Prefs.TESTING_PUSHCAM) {
                                    Manager.resetTestPicIndex();
                                }
                            } else if ("1".equals(value)) { //calibrate
                                resetPushCamToCalibration();
                                if (Prefs.TESTING_PUSHCAM) {
                                    Manager.resetTestPicIndex();
                                }
                            }
                        }
                    });
                    registerDialog(d0).show();
                } else { //turn PushCamera On
                    if (Prefs.TESTING_PUSHCAM) {
                        Manager.resetTestPicIndex();
                    }

                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            turnPushCameraOn();
                        }
                    };

                    String explanation = getString(R.string.camera_permission);
                    AstroTools.askForPermissionsOrRun(this, r, new String[]{Manifest.permission.CAMERA},
                            explanation, requestCodePushCamera, true);


                }
                return true;

            case R.id.dssGraph:

                registerDialog(makeDSSdialog()).show();
                return true;


            case R.id.starBold: //stars boldness control

                if (boldnessDialog != null) {
                    if (boldnessDialog.isActive()) { //close it
                        boldnessDialog.stop();
                        cView.layout(0, cView.getTop(), cView.getRight(), boldnessDialog.getBottom());
                        cView.invalidate();
                    } else { //start it
                        boldnessDialog.start();
                        cView.layout(0, cView.getTop(), cView.getRight(), boldnessDialog.getTop());
                        cView.invalidate();
                    }
                }
                return true;


            case R.id.starMags:
                changeStarMagLimit();

                return true;


            case R.id.g_az_grid:
                boolean pushCamera = SettingsActivity.isPushCameraOn(getApplicationContext());
                if (pushCamera) {
                    registerDialog(InputDialog.message(GraphActivity.this, getString(R.string.please_turn_pushcamera_mode_off_first), 0)).show();
                } else {
                    compass();
                }
                return true;
            case R.id.eyepieces://eyepieces
                final InputDialog d0 = new InputDialog(GraphActivity.this);
                boolean epson = SettingsActivity.isEpOn(getApplicationContext());
                String[] sel = {getString(R.string.eyepieces_database), epson ? getString(R.string.hide2) : getString(R.string.show2)};//getString(R.string.show_hide)


                d0.setTitle(getString(R.string.select_action));
                d0.setValue("-1"); //remove checks
                d0.setNegativeButton(getString(R.string.cancel));
                d0.setListItems(sel, new InputDialog.OnButtonListener() {
                    public void onClick(final String value) {
                        if ("0".equals(value)) {//database
                            Intent intent = new Intent(getApplicationContext(), EyepiecesListActivity.class);
                            startActivity(intent);

                        } else if ("1".equals(value)) {//show/hide
                            SettingsActivity.toggle(getString(R.string.epson));
                            cView.initSettingsToDrawMap();
                            cView.invalidate();
                        }
                    }
                });
                registerDialog(d0).show();
                return true;
            case R.id.g_telrad:
                SettingsActivity.toggle(getString(R.string.telradon));
                cView.initSettingsToDrawMap();
                cView.invalidate();
                return true;
            case R.id.qfind:
                AstroTools.invokeSearchActivity(this);
                return true;
            case R.id.scopeGraph:

                if (autoScopeMode) {
                    autoScopeMode = false;
                    InputDialog.toast(getString(R.string.auto_scope_mode_off), GraphActivity.this).show();
                    CommunicationManager.getComModule().cancelContinous();
                    return true;
                }
                if (!CommunicationManager.getComModule().isConnected()) {
                    InputDialog d = new InputDialog(GraphActivity.this, getString(R.string.dso_planner), getString(R.string.bluetooth_connection_required_would_you_like_to_setup_it_now_));
                    d.setPositiveButton(getString(R.string.yes), new InputDialog.OnButtonListener() {
                        public void onClick(String value) {
                            startActivity(new Intent(GraphActivity.this, SettingsBluetoothActivity.class));
                        }
                    });
                    d.setNegativeButton(getString(R.string.later));
                    registerDialog(d).show();
                    return true;
                }
                if (SettingsActivity.isScopeGoStarChartAutoUpdate(getApplicationContext())) {
                    autoScopeMode = true;
                    registerDialog(InputDialog.message(GraphActivity.this, getString(R.string.auto_scope_mode_on), 0)).show();
                    int period = (int) (SettingsActivity.getScopeGoStarChartAutoTimeUpdatePeriod(this) * 1000);
                    CommunicationManager.getComModule().readContinous(period);
                } else {
                    CommunicationManager.getComModule().read();
                }
                return true;
        }

        return false;
    }

    private void changeStarMagLimit() {
        if (starmagsDialog.isActive()) { //close it
            starmagsDialog.stop();
            cView.layout(0, cView.getTop(), cView.getRight(), starmagsDialog.getBottom());
            cView.invalidate();
            return;
        } else { //start it
            int value = SettingsActivity.getSharedPreferences(this).getInt(Constants.STAR_MAG_LIMIT_CATALOG, 0);
            starmagsDialog.start(value);
            cView.layout(0, cView.getTop(), cView.getRight(), starmagsDialog.getTop());
            cView.invalidate();
        }
    }

    private void compass() {
        final SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(this);
        boolean autoSky = SettingsActivity.isAutoSkyOn();
        boolean level = SettingsActivity.isAutoRotationOn();
        int value = 0;
        if (autoSky && !level) value = 0;
        if (autoSky && level) value = 1;
        if (!autoSky && level) value = 2;
        if (!autoSky && !level) value = 3;

        Runnable r = new Runnable() {
            public void run() {
                sh.edit().putBoolean(ObjCursor.CROSS_GUIDE, true).commit();
                ObjCursor.setParameters(sh);
            }
        };
        final Dialog dm = AstroTools.getDialog(this, getString(R.string.would_you_like_to_turn_object_marker_guide_line_on_), r);

        InputDialog d = new InputDialog(this);
        String names[] = new String[]{getString(R.string.compass), getString(R.string.compass_level), getString(R.string.level2), getString(R.string.off)};
        d.setValue("" + value);
        d.setNegativeButton(getString(R.string.cancel));
        d.setListItems(names, new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                int which = AstroTools.getInteger(value, 0, -1, 1000);
                switch (which) {
                    case 0:
                        SettingsActivity.setAutoSkyFlag(getApplicationContext(), true);
                        SettingsActivity.setAutoRotationFlag(getApplicationContext(), false);
                        if (!sh.getBoolean(ObjCursor.CROSS_GUIDE, false)) {
                            registerDialog(dm).show();
                        }
                        Point.setRotAngle(0);
                        registerSensorListener();
                        break;
                    case 1:
                        SettingsActivity.setAutoSkyFlag(getApplicationContext(), true);
                        SettingsActivity.setAutoRotationFlag(getApplicationContext(), true);
                        registerSensorListener();
                        if (!sh.getBoolean(ObjCursor.CROSS_GUIDE, false)) {
                            registerDialog(dm).show();
                        }

                        break;
                    case 2:
                        SettingsActivity.setAutoSkyFlag(getApplicationContext(), false); //turning compass off
                        SettingsActivity.setAutoRotationFlag(getApplicationContext(), true);
                        registerSensorListener();
                        break;
                    case 3:
                        SettingsActivity.setAutoSkyFlag(getApplicationContext(), false); //turning compass off
                        SettingsActivity.setAutoRotationFlag(getApplicationContext(), false);
                        unregisterSensorListener();
                        break;
                }
            }
        });
        d.setTitle(getString(R.string.select_compass_level_mode));
        registerDialog(d).show();

    }


    private boolean realTimeMode(boolean b) {
        realtimeMode = b;
        SettingsActivity.setAutoTimeUpdating(b);
        setTimeLabel();
        return b;
    }

    //SAND for pinch zoom
    //  returns false if no FOV change happen
    public boolean zoomChange(boolean wantToZoomIn) {
        int newPos = 0;
        int oldPos = mSpinner.gtSelectedItemPosition(); //spinner impl
        if (wantToZoomIn) newPos = oldPos + 1;
        else newPos = oldPos - 1;
        if (newPos < 0 || newPos >= spinArr.length)
            return false;
        mSpinner.stSelection(newPos); //triggers the onChange method changing the FOV
        return true;
    }


    public CuV getSkyView() {
        return cView;
    }

    private void initAlexMenu(GraphActivity v) {
        boolean dayMode = !nightMode;

        aMenu = new alexMenu(v, v, v.getLayoutInflater());
        aMenu.setHideOnSelect(true);
        aMenu.setItemsPerLineInPortraitOrientation(5);
        aMenu.setItemsPerLineInLandscapeOrientation(9);
        aMenu.setSkin(!dayMode, SettingsActivity.getDarkSkin());

        //mine
        float text_size = getResources().getDimension(R.dimen.text_size_small);//mine
        float density = getResources().getDisplayMetrics().density;
        text_size = text_size / density;
        aMenu.setTextSize((int) text_size);
        //load the menu items
        //This is kind of a tedious way to load up the menu items.
        //Am sure there is room for improvement.
        ArrayList<alexMenuItem> menuItems = new ArrayList<alexMenuItem>();

        menuItems.add(new alexMenuItem(R.id.center,
                getString(R.string.center), dayMode ? R.drawable.am_center_v : R.drawable.ram_center_v, false));
        menuItems.add(new alexMenuItem(R.id.itemMinusHour,
                getString(R.string._1_hour), dayMode ? R.drawable.am_tminus_v : R.drawable.ram_tminus_v, false));
        menuItems.add(new alexMenuItem(R.id.itemPlusHour,
                getString(R.string._1_hour2), dayMode ? R.drawable.am_tplus_v : R.drawable.ram_tplus_v, false));
        menuItems.add(new alexMenuItem(R.id.mirror,
                getString(R.string.mirror), dayMode ? R.drawable.am_mirror_v : R.drawable.ram_mirror_v, false));
        menuItems.add(new alexMenuItem(R.id.orientPlus,
                getString(R.string.rotate_180_), dayMode ? R.drawable.am_rotate_v : R.drawable.ram_rotate_v, false));


        String telradLabel = "QuInsight";
        if (!SettingsActivity.isQuinsightOn(getApplicationContext())) {
            telradLabel = getString(R.string.telrad);
        }
        menuItems.add(new alexMenuItem(R.id.g_telrad,

                telradLabel, dayMode ? R.drawable.am_telrad_v : R.drawable.ram_telrad_v, false));
        menuItems.add(new alexMenuItem(R.id.eyepieces,
                getString(R.string.eyepieces), dayMode ? R.drawable.am_ep_v : R.drawable.ram_ep_v, false));
        menuItems.add(new alexMenuItem(R.id.nbyGraph,
                getString(R.string.nearby), dayMode ? R.drawable.am_nearby_v : R.drawable.ram_nearby_v, false));
        menuItems.add(new alexMenuItem(R.id.dssGraph,
                getString(R.string.dss), dayMode ? R.drawable.am_dss_v : R.drawable.ram_dss_v, false));
        if (AstroTools.doesBtExist()) {
            menuItems.add(new alexMenuItem(R.id.scopeGraph,
                    getString(R.string.scope_go), dayMode ? R.drawable.am_scope_v : R.drawable.ram_scope_v, true));

        }

        menuItems.add(new alexMenuItem(R.id.g_az_grid,
                getString(R.string.compass), dayMode ? R.drawable.am_gyro_v : R.drawable.ram_gyro_v, false));
        menuItems.add(new alexMenuItem(R.id.calibrGraph,
                getString(R.string.align_star), dayMode ? R.drawable.am_calibrate_v : R.drawable.ram_calibrate_v, true));
        //menuItems.add(new alexMenuItem(R.id.pushCamera,
        //        "Push Camera", dayMode ? R.drawable.am_pushcam_v : R.drawable.ram_pushcam_v, true));
        menuItems.add(new alexMenuItem(R.id.dscGraph, "DSC", dayMode ? R.drawable.am_dsc_v : R.drawable.ram_dsc_v, true));
        menuItems.add(new alexMenuItem(R.id.radecGraph,
                getString(R.string.ra_dec), dayMode ? R.drawable.am_radec_v : R.drawable.ram_radec_v, true));
        menuItems.add(new alexMenuItem(R.id.starBold,
                getString(R.string.boldness), dayMode ? R.drawable.am_boldness_v : R.drawable.ram_boldness_v, true));
        menuItems.add(new alexMenuItem(R.id.starMags,
                getString(R.string.layers), dayMode ? R.drawable.am_layers_v : R.drawable.ram_layers_v, true));
        menuItems.add(new alexMenuItem(R.id.qfind,
                getString(R.string.search), dayMode ? R.drawable.am_search_v : R.drawable.ram_search_v, true));
        menuItems.add(new alexMenuItem(R.id.settingsGraph,

                getString(R.string.settings), dayMode ? R.drawable.am_settings_v : R.drawable.ram_settings_v, true));


        if (aMenu.isNotShowing()) {
            try {
                aMenu.setMenuItems(menuItems);
            } catch (Exception e) {
                InputDialog.message(GraphActivity.this, getString(R.string.menu_error_) + e.getMessage(), 0).show();
            }
        }
    }

    public void doMenu(View v) {
        aMenu.show(v);//Note it doesn't matter what widget you send the menu as long as it gets view.
    }

    protected void onDestroy() {
        try {
            aMenu.hide();
        } catch (Exception e) {
        }
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        cView.setGraphDestroyedFlag();
        cView.clearLists();
        cView = null;
        SettingsActivity.removeSharedPreferencesKey(Constants.GRAPH_OBJECT, getApplicationContext());
        unregisterSensorListener();
    }

    public void MenuItemSelectedEvent(alexMenuItem selection) {
        parseMenuEvent(selection.getId());
    }

    //override the system search request in night mode only
    @Override
    public boolean onSearchRequested() {
        return AstroTools.invokeSearchActivity(this);
    }

    //overriding BACK button
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Bluetooth D-Pad controls the PushCam button
        if (keyCode == KeyEvent.KEYCODE_BUTTON_B // Mini remote B (top edge key)
                || keyCode == KeyEvent.KEYCODE_D // TAP Strap letter D (1-0100)
                && getPushCamPresenter() != null && floatingButton != null) {
            getPushCamPresenter().onClick(floatingButton);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT // Mini remote HAT loop accident prevention)
                || keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || keyCode == KeyEvent.KEYCODE_DPAD_UP
                || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            return true; //ignore
        }
        return onKeyDownImpl(keyCode, event);
    }

    /**
     * @param keyCode
     * @param event   null means emulation from the button. Parent is not called
     *                Actually null not used
     * @return
     */
    private boolean onKeyDownImpl(int keyCode, KeyEvent event) {
        Log.d(TAG, "calling activity=" + callingActivity + " def time=" + def_time);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (boldnessDialog != null && boldnessDialog.isActive()) {
                parseMenuEvent(R.id.starBold); //triggers its closing
                return true;
            }
            if (starmagsDialog != null && starmagsDialog.isActive()) {
                int cat = starmagsDialog.getCatalog();
                SettingsActivity.putSharedPreferences(Constants.STAR_MAG_LIMIT_CATALOG, cat, GraphActivity.this);

                parseMenuEvent(R.id.starMags);
                return true;
            }
            if (callingActivity == Constants.NOTE_LIST_GRAPH_CALLING && def_time != null) {
                AstroTools.setDefaultTime(def_time, getApplicationContext());
                Log.d(TAG, "def time=" + def_time);
            }
            finish();
        }
        if (event == null)
            return true;
        else
            return super.onKeyDown(keyCode, event);
    }

    private boolean nearbyDialog = false;

    //nearby objects dialog
    private Dialog getNearbySearchDialog() {
        final InputDialog d = new InputDialog(this);

        d.setTitle(getString(R.string.nearby_search));
        d.insertLayout(R.layout.graph_nearby_dialog);

        final Button db_btn = (Button) d.findViewById(R.id.gnd_db_btn);
        final Button types_btn = (Button) d.findViewById(R.id.gnd_types_btn);
        final EditText et1 = (EditText) d.findViewById(R.id.gnd_et1);//fov
        final EditText et2 = (EditText) d.findViewById(R.id.gnd_et2);//dl
        aMenu.hide();

        TextView tvt = (TextView) d.findViewById(R.id.gnd_types_tv);
        tvt.setText(SettingsActivity.getObjTypesSummary(this, SettingsActivity.SEARCH_NEARBY));

        TextView tvdb = (TextView) d.findViewById(R.id.gnd_types_db);
        tvdb.setText(SettingsActivity.getSelectedCatalogsSummary(this, SettingsActivity.SEARCH_NEARBY));


        types_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(GraphActivity.this, SettingsInclActivity.class);
                i.putExtra(Constants.XML_NUM, R.xml.settings_basic_search_obj_types_search_nearby_incl);
                startActivity(i);
                SettingsActivity.putSharedPreferences(Constants.GRAPH_NEARBY_STRING_FOV, et1.getText().toString(), GraphActivity.this);
                SettingsActivity.putSharedPreferences(Constants.GRAPH_NEARBY_STRING_DL, et2.getText().toString(), GraphActivity.this);
                nearbyDialog = true;
                d.dismiss();

            }
        });

        String fov = SettingsActivity.getStringFromSharedPreferences(this, Constants.GRAPH_NEARBY_STRING_FOV, _120);
        String dl = SettingsActivity.getStringFromSharedPreferences(this, Constants.GRAPH_NEARBY_STRING_DL, _1);

        et1.setText(fov);//prefs.getString(Constants.GRAPH_NEARBY_STRING_FOV,_120));
        et2.setText(dl);//prefs.getString(Constants.GRAPH_NEARBY_STRING_DL,_1));

        db_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(GraphActivity.this, SettingsInclActivity.class);
                i.putExtra(Constants.XML_NUM, R.xml.settings_select_catalogs_nearby_incl);
                startActivity(i);
                SettingsActivity.putSharedPreferences(Constants.GRAPH_NEARBY_STRING_FOV, et1.getText().toString(), GraphActivity.this);
                SettingsActivity.putSharedPreferences(Constants.GRAPH_NEARBY_STRING_DL, et2.getText().toString(), GraphActivity.this);
                nearbyDialog = true;
                d.dismiss();
            }
        });

        d.setPositiveButton(getString(R.string.search), new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                //form a compatible call
                SettingsActivity.putSharedPreferences(Constants.GRAPH_NEARBY_STRING_FOV, et1.getText().toString(), GraphActivity.this);
                SettingsActivity.putSharedPreferences(Constants.GRAPH_NEARBY_STRING_DL, et2.getText().toString(), GraphActivity.this);

                startNearbySearch(0, et1.getText().toString(), et2.getText().toString());

            }
        });
        d.setNegativeButton("Cancel");
        return d;


    }

    private Thread nearbyThread;

    private boolean isNearbySearchRunning() {
        if (nearbyThread != null)
            if (nearbyThread.isAlive())
                return true;
        return false;
    }

    private void startNearbySearch(int cat, String fovStr, String visStr) {
        double fov = AstroTools.getDouble(fovStr, 0, 0, 10000);
        if (fov <= 0) return;
        if (fov > 600)
            fov = 600;
        double vis = AstroTools.getDouble(visStr, 0, -1, 5);

        if (isNearbySearchRunning()) {
            registerDialog(InputDialog.message(this, R.string.search_nearby_running)).show();
            return;
        }
        Point selobj = cView.getObjCursor().getObjSelected();
        if (selobj == null)
            return;
        nearbyThread = new Thread(new NearbyDSO(selobj, cat, fov, vis, this, handler));
        nearbyThread.start();

    }

    //Replacing the calibration activity
    private void calibrationDialog(Point objSelected) {
        final Point p;

        final InputDialog dlg = new InputDialog(GraphActivity.this);
        dlg.setTitle(getString(R.string.setting_circles_one_star_alignment));
        dlg.setHelp(R.string.helpCalibrateDlg);
        dlg.insertLayout(R.layout.calibrate_dialog);
        //dlg.disableBackButton(true);
        dlg.setNegativeButton(getString(R.string.cancel));

        //fields
        final TextView timetext = (TextView) dlg.findViewById(R.id.currenttime_text);
        final TextView azt = (TextView) dlg.findViewById(R.id.absaz_txt);
        final TextView altt = (TextView) dlg.findViewById(R.id.absalt_txt);
        final EditText azte = (EditText) dlg.findViewById(R.id.aztext);
        final EditText altte = (EditText) dlg.findViewById(R.id.alttext);
        final Button orientBtn = (Button) dlg.findViewById(R.id.orientation_btn);

        //	SharedPreferences prefs=Graph1243.this.getSharedPreferences(Constants.PREFS,Context.MODE_PRIVATE);
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(GraphActivity.this);
        boolean orient = prefs.getBoolean(CALIBR_ORIENT, true);//false for Clockwise, true - for CounterClockwise

        int object_type = -1;
        Planet pl = null;
        String name = null;
        if (objSelected instanceof AstroObject)
            name = ((AstroObject) objSelected).getLongName();

        p = AstroTools.precession(objSelected, AstroTools.getDefaultTime(GraphActivity.this));

        TextView dsc = (TextView) dlg.findViewById(R.id.des_txt);
        dsc.setText(name);
        Global.calibrationTime = null;

        orientBtn.setText(orient ? R.string.counterclockwise : R.string.clockwise);
        orientBtn.setTag(orient);

        //Orientation button
        orientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean orient = (Boolean) orientBtn.getTag();
                orient = !orient;
                orientBtn.setTag(orient);
                orientBtn.setText(orient ? R.string.counterclockwise : R.string.clockwise);
            }
        });

        //NOW button
        //setting current time (when measurement is taken
        View nowButton = dlg.findViewById(R.id.currenttime_btn);
        nowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                String s = DetailsActivity.makeDateString(c, true) + " " + DetailsActivity.makeTimeString(c, true);
                timetext.setText(s);

                //updateAzAlt(c); inplace now
                double lst = AstroTools.sdTime(c);
                double lat = SettingsActivity.getLattitude();

                Double absaz = AstroTools.Azimuth(lst, lat, p.ra, p.dec);
                String s1 = String.format(Locale.US, "%.1f", absaz);
                azt.setText(s1 + '\u00B0');
                azt.setTag(absaz); //passing the var

                Double absalt = AstroTools.Altitude(lst, lat, p.ra, p.dec);
                String s2 = String.format(Locale.US, "%.1f", absalt);
                altt.setText(s2 + '\u00B0');
                altt.setTag(absalt); //passing the var

                Global.calibrationTime = c; //used for calculating the exact star position when calibration is performed

            }
        });

        dlg.setPositiveButton(getString(R.string.one_star_align), new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                double er = 999.9;
                double az = AstroTools.getDouble(azte.getText().toString(), er, -10000, 10000);
                double alt = AstroTools.getDouble(altte.getText().toString(), er, -10000, 10000);

                if (az != er && alt != er && Global.calibrationTime != null) { //calculating adjustments.  These are WRONG calculations!!! They are wrong to take into account the wrong...

                    //azimuthal circle was glued to dobson base counterclockwire when it should have been clockwise or vice versa

                    boolean orient = (Boolean) orientBtn.getTag();
                    double adj_az = 0;
                    double adj_alt = 0;

                    if (orient) {
                        adj_az = (Double) azt.getTag() - az;
                        SettingsActivity.putSharedPreferences(Constants.ALIGN_ADJ_AZ, adj_az, GraphActivity.this);
                        //for correct azimuathal circle equation should be like the one for Alt
                        adj_alt = (Double) altt.getTag() - alt;
                        SettingsActivity.putSharedPreferences(Constants.ALIGN_ADJ_ALT, adj_alt, GraphActivity.this);
                        //Global.adj_Alt = ;
                    } else {
                        adj_az = (Double) azt.getTag() + az;
                        adj_alt = (Double) altt.getTag() - alt;

                        SettingsActivity.putSharedPreferences(Constants.ALIGN_ADJ_AZ, adj_az, GraphActivity.this);
                        SettingsActivity.putSharedPreferences(Constants.ALIGN_ADJ_ALT, adj_alt, GraphActivity.this);
                    }
                    SettingsActivity.putSharedPreferences(Constants.ALIGN_TIME, Calendar.getInstance().getTimeInMillis(), GraphActivity.this);

                    SettingsActivity.putSharedPreferences(CALIBR_ORIENT, orient, GraphActivity.this);

                    String s2 = String.format(Locale.US, "%.1f", adj_az);
                    String s3 = String.format(Locale.US, "%.1f", adj_alt);

                    InputDialog d = new InputDialog(GraphActivity.this);
                    d.setType(InputDialog.DType.MESSAGE_ONLY);
                    d.setTitle(getString(R.string.setting_circles_calibration_successfull_));
                    d.setMessage(getString(R.string.adjustments_daz_) + s2 + getString(R.string._dalt_) + s3);
                    registerDialog(d).show();
                }
            }
        });
        registerDialog(dlg).show();
    }

    public String[] getSpinArray() {
        return spinArr;
    }

    public String[] getSpinArray2() {
        return spinArr2;
    }


    void restart() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }


    @Override
    protected int getTestActivityNumber() {
        return TestIntentService.GRAPH;
    }


    @Override
    protected void startTest(int action, int param) {
        if (!Global.TEST_MODE)
            return;
        super.startTest(action, param);
        switch (action) {
            case TestIntentService.GRAPH_START:
                performZoomItemSelect("2");
                break;
            case TestIntentService.GRAPH_LOG:
                TestIntentService.print("test=" + TestIntentService.getTestNumber() + " result=" + (cView.tychoList.size() > 1000) + " tycho count=" + cView.tychoList.size());
                TestIntentService.print("test=" + TestIntentService.getTestNumber() + " result=" + (cView.ucac4List.size() > 500) + " ucac4 count=" + cView.ucac4List.size());
                TestIntentService.print("test=" + TestIntentService.getTestNumber() + " result=" + (cView.pgcList.size() > 1200) + " pgc count=" + cView.pgcList.size());
                TestIntentService.print("test=" + TestIntentService.getTestNumber() + " result=" + (cView.ngcList.size() > 25) + " ngc count=" + cView.ngcList.size());


                break;
        }
    }


    //Gesture Detector (just implement OnGestureListener in the Activity)
    GestureDetector gDetector = new GestureDetector(this);

    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
        gDetector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
        if (start == null || finish == null) return false;
        if (starmagsDialog.isActive() || boldnessDialog.isActive()) return true;
        int bh = cView.getBottom();
        if (bh == Global.screenH) bh -= 50; //for full screen mode
        if (start.getRawY() > bh) { //in the bottom bar
            if (start.getRawY() - finish.getRawY() > Global.flickLength)
                doMenu(bottomBar);
            else if (start.getRawX() - finish.getRawX() > Global.flickLength) {
                super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                Log.d(TAG, "dispatch back");
            }
        }
        return true;
    }

    public void onLongPress(MotionEvent e) {
        if (starmagsDialog.isActive() || boldnessDialog.isActive()) return;
        try {
            GraphActivity.DssImage image = cView.dssRectangles.pressed(e.getX(), e.getY());
            if (image != null && !image.isSelected()) {//this automatically selectes and draw new potential rectangle objects
                cView.dssRectangles.deselectAll();//deselects all currently selected countours
                image.select();
                cView.invalidate();
                return;
            }

            //calling menu on second tap
            if (image != null && image.isSelected()) {//pressed on dss image
                image.select();//to make it vibrate

                registerDialog(makeDSSselectionDialog(image)).show();
                return;
            }

            View bar = findViewById(R.id.topBar);
            if (bar.getVisibility() == View.GONE)
                bar.setVisibility(View.VISIBLE);
            else
                bar.setVisibility(View.GONE);
        } catch (Exception ex) {
        }
    }

    public void onShowPress(MotionEvent e) {
    }

    public boolean onDown(MotionEvent arg0) {
        return false;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

}