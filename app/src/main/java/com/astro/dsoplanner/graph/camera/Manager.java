package com.astro.dsoplanner.graph.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;

import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.Logg;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.pushcamera.Cluster2;
import com.astro.dsoplanner.pushcamera.DbBuilder;
import com.astro.dsoplanner.pushcamera.P;
import com.astro.dsoplanner.pushcamera.P2i;
import com.astro.dsoplanner.pushcamera.P4;
import com.astro.dsoplanner.pushcamera.Plate;
import com.astro.dsoplanner.util.Holder2;

import android.os.Process;


//ToDo. LV - adjust histogram of jpg files for viewing
//interrupt cluster and plate. feedback to presenter
//Graph. Life cycle with respect to camera. Done?
//what if abortSolving puts interrupt right when we are sending the plate resutlt? Then the state changes to align, but then comes the plate result. Maybe it is ok after all

@RequiresApi(api = Build.VERSION_CODES.M)
public class Manager implements Runnable {
    public static final String DATA = "data";
    public static final String PHOTO_TYPE = "photo_type";
    public static final String ALIGN_TYPE = "align_type";
    public static final String GYRO_ALT = "gyro_alt";
    public static final String HR = "hr";
    public static final String RCX = "rcx";
    public static final String RCY = "rcy";
    public static final String PUSH_CAMERA_ACTION = "push_camera_action";
    public static final String CIX = "cix";
    public static final String CIY = "ciy";
    public static final String CIZ = "ciz";
    public static final String CIT = "cit";
    public static final String PUSH_CAMERA_TEXT = "push_camera_text";
    static int pic_index = 0;

    int N = 25;
    int count = 0;
    int actuallyAdded = 0;
    long time = 0;
    long time_decoding = 0;
    String TAG = Camera2APIService.TAG;
    List<byte[]> list = Collections.synchronizedList(new ArrayList<byte[]>());
    short[] total;
    ExecutorService es = Executors.newFixedThreadPool(Prefs.NUM_THREADS);

    Context context;
    int w, h;
    PhotoParams params;
    ImageType type;

    public static void resetTestPicIndex() {
        pic_index = 0;
    }

    public enum ImageType {
        YUV(ImageFormat.YUV_420_888),
        JPEG(ImageFormat.JPEG);
        int type;

        ImageType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    public Manager(Context context, int w, int h, PhotoParams params, ImageType type) {
        this.context = context;
        this.w = w;
        this.h = h;
        this.N = params.N;
        this.params = params;
        total = new short[w * h];
        this.type = type;
    }

    public void add(byte[] data) {
        if (type == ImageType.YUV && data.length != w * h) {
            Logg.d(TAG, "Manager, wrong data length, " + data.length + " w=" + w + " h=" + h);
            return;
        }
        if (list == null) {
            Logg.d(TAG, "Manager, adding data to null list, skipping data");
            return; // thread was stopped
        }

        list.add(data);
        Logg.d(TAG, "Manager, adding data to list" + " list size=" + list.size());
    }

    public int getQueueSize() {
        if (list == null) //thread was stopped
            return 0;
        return list.size();
    }

    public synchronized void incCount() {
        count++;
    }

    public synchronized int getCount() {
        return count;
    }

    public void run() {
        if (Prefs.TESTING_PUSHCAM) {
            processTest();
            return;
        }
        Process.setThreadPriority(Prefs.THREAD_PRIORITY);
        long last_image_time = 0;
        boolean interrupted = false;
        while (getCount() < N) {
            if (list.size() > 0) {
                long add_start = Calendar.getInstance().getTimeInMillis();
                byte[] data2 = list.remove(0);
                List<Future<Void>> lf = new ArrayList<>();

                if (type == ImageType.YUV) {
                    for (int i = 0; i < Prefs.NUM_THREADS; i++) {
                        Sum sum = new Sum(total, data2, null, i, Prefs.NUM_THREADS, ImageType.YUV);
                        lf.add(es.submit(sum));
                    }
                } else { //JPEG
                    Bitmap image = BitmapFactory.decodeByteArray(data2, 0, data2.length);
                    long decoding = Calendar.getInstance().getTimeInMillis();
                    time_decoding += (decoding - add_start);
                    if (image == null) {
                        data2 = null;
                        image = null;
                        Logg.d(TAG, "Manager, null image");
                        continue;
                    }
                    int w_ = image.getWidth();
                    int h_ = image.getHeight();
                    if (w != w_ || h != h_) {
                        data2 = null;
                        image = null;
                        Logg.d(TAG, "Manager, wrong data length, " + "w_=" + w_ + "h_=" + h_ + " w=" + w + " h=" + h);
                        continue;
                    }

                    int[] pixels = new int[w * h];
                    image.getPixels(pixels, 0, w, 0, 0, w, h);
                    for (int i = 0; i < Prefs.NUM_THREADS; i++) {
                        Sum sum = new Sum(total, null, pixels, i, Prefs.NUM_THREADS, ImageType.JPEG);
                        lf.add(es.submit(sum));
                    }

                }
                for (int i = 0; i < Prefs.NUM_THREADS; i++) {
                    lf.get(i);
                }
                last_image_time = Calendar.getInstance().getTimeInMillis();
                long add_stop = Calendar.getInstance().getTimeInMillis();
                time += (add_stop - add_start);

                Logg.d(TAG, "Manager, add over, count=" + count);
                incCount();
                actuallyAdded++;
            }
            if (Thread.interrupted()) {
                Logg.d(TAG, "Manager, interrupted");
                interrupted = true;
                break;

            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Logg.d(TAG, "Manager, interrupted during sleep");
                interrupted = true;
                break;
            }
            long now = Calendar.getInstance().getTimeInMillis();
            if (last_image_time != 0 && (now - last_image_time > Prefs.MAX_DELAY)) {
                Logg.d(TAG, "Manager, timeout");
                break;
            }

        }
        Logg.d(TAG, "time adding, ms =" + time);
        Logg.d(TAG, "time decoding, ms =" + time_decoding);
        es.shutdown();
        list = null;
        if (!interrupted)
            process();
        else
            onInterrupted();

    }

    protected void onInterrupted() {
        Logg.d(TAG, "Manager, interrupted");
        sendMessage("Interrupted");
        sendAction(Prefs.ON_CANCELLED);
    }


    private void sendMessage(String text) {
        Intent i = new Intent(Constants.PUSH_CAMERA_PHOTO_READY);
        i.putExtra(PUSH_CAMERA_TEXT, text);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    private void sendAction(int action) {
        Intent i = new Intent(Constants.PUSH_CAMERA_PHOTO_READY);
        i.putExtra(PUSH_CAMERA_ACTION, action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    private void sendP4(P4 data, int type) {
        Intent i = new Intent(Constants.PUSH_CAMERA_PHOTO_READY);
        i.putExtra(CIX, data.x);
        i.putExtra(CIY, data.y);
        i.putExtra(CIZ, data.z);
        i.putExtra(CIT, data.t);
        i.putExtra(PHOTO_TYPE, type);
        i.putExtra(PUSH_CAMERA_ACTION, Prefs.ON_SOLVED_SUCCESS);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    private Holder2<short[][], Double> loadFakeImage() {
        String path;
        pic_index++;
        if (pic_index % 2 == 1) {
            path = DbBuilder.PATH + "20210210_214849_Alnilam.jpg";
        } else {
            path = DbBuilder.PATH + "20210210_220453_Sirius.jpg";
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap image = BitmapFactory.decodeFile(path, options);
        if (image == null) {
            return null;
        }
        int w = image.getWidth();
        int h = image.getHeight();
        int rows = h;
        int cols = w;
        int[] pixels = new int[w * h];
        image.getPixels(pixels, 0, w, 0, 0, w, h);
        short[][] data2 = new short[rows][cols];
        double sum = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int color = pixels[i * cols + j];
                int blue = color & 0xff;
                int green = (color & 0xff00) >> 8;
                int red = (color & 0xff0000) >> 16;
                data2[i][j] = (short) ((blue + green + red) / 3);
                sum += data2[i][j];
            }
        }
        return new Holder2<>(data2, sum);

    }

    protected void process() {
        if (actuallyAdded == 0) {
            sendAction(Prefs.ON_SOLVED_ERROR);
            sendMessage("zero image");
            return;
        }
        int[] totalbmp = new int[w * h];
        int Nactual = actuallyAdded;

        String name = String.valueOf(Calendar.getInstance().getTimeInMillis()) + "_" + w + "_" + h + "_" + Nactual + "_" + ".jpg";
        String name_raw = String.valueOf(Calendar.getInstance().getTimeInMillis()) + "_" + w + "_" + h + "_" + Nactual + "_" + ".raw";
        File pictureFile = new File(Global.cameraPath + name);
        File rawFile = new File(Global.cameraPath + name_raw);
        saveRaw(total, w, h, rawFile);
        Logg.d(TAG, "raw saved");


        int max = 0;
        for (int j = 0; j < w * h; j++) {
            if (total[j] > max)
                max = total[j];
        }
        if (max == 0)
            max = 1;
        if (Thread.currentThread().isInterrupted()) {
            Logg.d(TAG, "Manager, interrupted 1");
            onInterrupted();
            return;
        }

        for (int j = 0; j < w * h; j++) {
            short v = (short) (total[j] * 255 / max);
            int value = (0xff << 24) | (v << 16) | (v << 8) | v;
            totalbmp[j] = value;
        }
        if (Thread.currentThread().isInterrupted()) {
            Logg.d(TAG, "Manager, interrupted 2");
            onInterrupted();
            return;
        }

        short[][] data = new short[h][w];
        double sum = 0;
        for (int k = 0; k < w * h; k++) {
            int row = k / w;
            int col = k - row * w;
            short v = (short) (total[k] * 255 / max);
            data[row][col] = v;
            sum += v;
        }
        total = null;
        if (Thread.currentThread().isInterrupted()) {
            Logg.d(TAG, "Manager, interrupted 3");
            onInterrupted();
            return;
        }

        Bitmap bmp = Bitmap.createBitmap(totalbmp, w, h, Bitmap.Config.ARGB_8888);


        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            String filename = pictureFile.getAbsolutePath();
            Logg.d(TAG, "Manager, saved to file " + filename);
        } catch (Exception e) {

            e.printStackTrace();
        }
        totalbmp = null;
        bmp = null;

        if (Thread.currentThread().isInterrupted()) {
            Logg.d(TAG, "Manager, interrupted 4");
            onInterrupted();
            return;
        }

        if (Prefs.TESTING_PUSHCAM) {
            data = null;
            Holder2<short[][], Double> h2 = loadFakeImage();
            if (h2 == null) {
                sendMessage("Fake image missing");
                sendAction(Prefs.ON_CANCELLED);
                return;
            }
            data = h2.x;
            sum = h2.y;
        }

        boolean mirror = SettingsActivity.isPushCameraMirrorOn(context);
        Prefs.PhotoType photo_type = params.photo_type;
        Prefs.AlignType align_type = params.align_type;
        int hrAlignmentStar = params.hr;
        double rcx = -1;
        double rcy = -1;
        if (params.rc != null) {
            rcx = params.rc.x;
            rcy = params.rc.y;
        }

        double lst = Point.getLST();
        double lat = SettingsActivity.getLattitude();
        boolean check_alt = SettingsActivity.isPushCamGyroAltValidation(context);
        double gyro_alt = params.gyro_alt;
        boolean remove_bright_areas = SettingsActivity.isRemoveBrightAreas(context);
        if (photo_type == Prefs.PhotoType.HR_ALIGNMENT) {

            Cluster2 cluster = new Cluster2(data, sum, mirror, w, h, remove_bright_areas);
            Holder2<P2i, Map<Integer, P>> h2 = cluster.run();
            if (h2 == null && Thread.currentThread().isInterrupted()) {
                Logg.d(TAG, "Manager, interrupted 5, cluster");
                onInterrupted();
                return;
            } else if (h2 == null) {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("no clusters found");
                Logg.d(TAG, "no clusters found");
                return;
            }

            Plate plate = null;
            try {
                plate = new Plate(h2, Prefs.MAX_DST, lst, lat, gyro_alt, check_alt);
            } catch (Exception e) {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("Plate Exception");
                Logg.d(TAG, "Plate Exception=" + e);
                return;
            }
            P4 rc_center_info = plate.find_eyepiece_star_on_plate(hrAlignmentStar);
            if (rc_center_info != null) {
                sendP4(rc_center_info, Prefs.HR_ALIGNMENT);// r c ra_center dec_center
                sendMessage("Eyepiece star found on plate. Alignment success!");
                beep();
                Logg.d(TAG, "CameraService.HR_ALIGNMENT. Image=" + pictureFile.getAbsolutePath() + " Plate solved, rc=" + rc_center_info + " lst=" + lst + " lat=" + lat + " gyro_alt=" + gyro_alt + " check_alt=" + check_alt);
                return;
            } else if (rc_center_info == null && Thread.currentThread().isInterrupted()) {
                Logg.d(TAG, "Manager, interrupted 6, plate");
                onInterrupted();
                return;
            } else {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("Eyepiece star not found on plate. Alignment error!");
                Logg.d(TAG, "CameraService.HR_ALIGNMENT. Image=" + pictureFile.getAbsolutePath() + " Plate solved NOT");
                return;
            }
        } else if (photo_type == Prefs.PhotoType.OBJECT_ALIGNMENT) {
            Cluster2 cluster = new Cluster2(data, sum, mirror, w, h, remove_bright_areas);
            Holder2<P2i, Map<Integer, P>> h2 = cluster.run();
            if (h2 == null && Thread.currentThread().isInterrupted()) {
                Logg.d(TAG, "Manager, interrupted 7, cluster");
                onInterrupted();
                return;
            } else if (h2 == null) {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("no clusters found");
                Logg.d(TAG, "no clusters found");
                return;
            }
            Plate plate = null;
            try {
                plate = new Plate(h2, Prefs.MAX_DST, lst, lat, gyro_alt, check_alt);
            } catch (Exception e) {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("Plate Exception");
                Logg.d(TAG, "Plate Exception=" + e);
                return;
            }
            P4 center_info_align_any_object = plate.find_center();
            if (center_info_align_any_object != null) {
                sendP4(center_info_align_any_object, Prefs.OBJECT_ALIGNMENT);
                sendMessage("Alignment success!");
                beep();
                Logg.d(TAG, "CameraService.OBJECT_ALIGNMENT. Image=" + pictureFile.getAbsolutePath() + " Plate solved, center_info_align_any_object=" + center_info_align_any_object + " lst=" + lst + " lat=" + lat + " gyro_alt=" + gyro_alt + " check_alt=" + check_alt);

                return;
            } else if (center_info_align_any_object == null && Thread.currentThread().isInterrupted()) {
                Logg.d(TAG, "Manager, interrupted 8, plate");
                onInterrupted();
                return;
            } else {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("Alignment error!");
                Logg.d(TAG, "CameraService.OBJECT_ALIGNMENT. Image=" + pictureFile.getAbsolutePath() + " Plate solved NOT");
                return;

            }
        } else if (photo_type == Prefs.PhotoType.PLATE_SOLVE) {
            Cluster2 cluster = new Cluster2(data, sum, mirror, w, h, remove_bright_areas);
            Holder2<P2i, Map<Integer, P>> h2 = cluster.run();
            if (h2 == null && Thread.currentThread().isInterrupted()) {
                Logg.d(TAG, "Manager, interrupted 9, cluster");
                onInterrupted();
                return;
            } else if (h2 == null) {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("no clusters found");
                Logg.d(TAG, "no clusters found");
                return;
            }

            Plate plate = null;
            try {
                plate = new Plate(h2, Prefs.MAX_DST, lst, lat, gyro_alt, check_alt);
            } catch (Exception e) {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("Plate Exception");
                Logg.d(TAG, "Plate Exception=" + e);
                return;
            }
            if (align_type == Prefs.AlignType.HR_ALIGNMENT) {
                P4 radecs = plate.find_point_ra_dec(rcx, rcy);
                if (radecs != null) {
                    sendP4(radecs, Prefs.PLATE_SOLVE_RADECS);
                    Logg.d(TAG, "CameraService.PLATE_SOLVE. HR_ALIGNMENT. Image=" + pictureFile.getAbsolutePath() + " Plate solved, radecs=" + radecs + " lst=" + lst + " lat=" + lat + " gyro_alt=" + gyro_alt + " check_alt=" + check_alt);
                    beep();
                    //sendMessage("Plate solved");
                } else if (radecs == null && Thread.currentThread().isInterrupted()) {
                    Logg.d(TAG, "Manager, interrupted 10, plate");
                    onInterrupted();
                    return;
                } else {
                    sendAction(Prefs.ON_SOLVED_ERROR);
                    sendMessage("Plate not solved!");
                    Logg.d(TAG, "CameraService.PLATE_SOLVE. Image=" + pictureFile.getAbsolutePath() + " Plate solved NOT");

                }
                return;
            } else if (align_type == Prefs.AlignType.OBJECT_ALIGNMENT) {
                P4 center_info = plate.find_center();
                if (center_info != null) {
                    sendP4(center_info, Prefs.PLATE_SOLVE_CENTER_INFO);
                    beep();
                    //sendMessage("Plate solved");
                    Logg.d(TAG, "CameraService.PLATE_SOLVE. OBJECT_ALIGNMENT. Image=" + pictureFile.getAbsolutePath() + " Plate solved, center_info=" + center_info + " lst=" + lst + " lat=" + lat + " gyro_alt=" + gyro_alt + " check_alt=" + check_alt);

                } else if (center_info == null && Thread.currentThread().isInterrupted()) {
                    Logg.d(TAG, "Manager, interrupted 11, plate");
                    onInterrupted();
                    return;
                } else {
                    sendAction(Prefs.ON_SOLVED_ERROR);
                    sendMessage("Plate not solved!");
                    Logg.d(TAG, "CameraService.PLATE_SOLVE. Image=" + pictureFile.getAbsolutePath() + " Plate solved NOT");

                }
                return;
            }
        } else {
            sendMessage("Wrong logic. photo type");
        }

    }

    protected void processTest() {
        int w = 4032;
        int h = 3024;

        short[][] data = new short[h][w];
        double sum = 0;

        data = null;
        Holder2<short[][], Double> h2_ = loadFakeImage();
        if (h2_ == null) {
            sendMessage("Fake image missing");
            sendAction(Prefs.ON_CANCELLED);
            return;
        }
        data = h2_.x;
        sum = h2_.y;


        boolean mirror = SettingsActivity.isPushCameraMirrorOn(context);
        Prefs.PhotoType photo_type = params.photo_type;
        Prefs.AlignType align_type = params.align_type;
        int hrAlignmentStar = params.hr;
        double rcx = -1;
        double rcy = -1;
        if (params.rc != null) {
            rcx = params.rc.x;
            rcy = params.rc.y;
        }

        double lst = Point.getLST();
        double lat = SettingsActivity.getLattitude();
        boolean check_alt = false;
        double gyro_alt = params.gyro_alt;
        boolean remove_bright_areas = SettingsActivity.isRemoveBrightAreas(context);
        if (photo_type == Prefs.PhotoType.HR_ALIGNMENT) {

            Cluster2 cluster = new Cluster2(data, sum, mirror, w, h, remove_bright_areas);
            Holder2<P2i, Map<Integer, P>> h2 = cluster.run();
            if (h2 == null && Thread.currentThread().isInterrupted()) {
                Logg.d(TAG, "Manager, interrupted 5, cluster");
                onInterrupted();
                return;
            } else if (h2 == null) {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("no clusters found");
                Logg.d(TAG, "no clusters found");
                return;
            }

            Plate plate = null;
            try {
                plate = new Plate(h2, Prefs.MAX_DST, lst, lat, gyro_alt, check_alt);
            } catch (Exception e) {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("Plate Exception");
                Logg.d(TAG, "Plate Exception=" + e);
                return;
            }
            P4 rc_center_info = plate.find_eyepiece_star_on_plate(hrAlignmentStar);
            if (rc_center_info != null) {
                sendP4(rc_center_info, Prefs.HR_ALIGNMENT);// r c ra_center dec_center
                sendMessage("Eyepiece star found on plate. Alignment success!");
                beep();
                return;
            } else if (rc_center_info == null && Thread.currentThread().isInterrupted()) {
                Logg.d(TAG, "Manager, interrupted 6, plate");
                onInterrupted();
                return;
            } else {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("Eyepiece star not found on plate. Alignment error!");

                return;
            }
        } else if (photo_type == Prefs.PhotoType.OBJECT_ALIGNMENT) {
            Cluster2 cluster = new Cluster2(data, sum, mirror, w, h, remove_bright_areas);
            Holder2<P2i, Map<Integer, P>> h2 = cluster.run();
            if (h2 == null && Thread.currentThread().isInterrupted()) {
                Logg.d(TAG, "Manager, interrupted 7, cluster");
                onInterrupted();
                return;
            } else if (h2 == null) {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("no clusters found");
                Logg.d(TAG, "no clusters found");
                return;
            }
            Plate plate = null;
            try {
                plate = new Plate(h2, Prefs.MAX_DST, lst, lat, gyro_alt, check_alt);
            } catch (Exception e) {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("Plate Exception");
                Logg.d(TAG, "Plate Exception=" + e);
                return;
            }
            P4 center_info_align_any_object = plate.find_center();
            if (center_info_align_any_object != null) {
                sendP4(center_info_align_any_object, Prefs.OBJECT_ALIGNMENT);
                sendMessage("Alignment success!");
                beep();

                return;
            } else if (center_info_align_any_object == null && Thread.currentThread().isInterrupted()) {
                Logg.d(TAG, "Manager, interrupted 8, plate");
                onInterrupted();
                return;
            } else {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("Alignment error!");
                return;

            }
        } else if (photo_type == Prefs.PhotoType.PLATE_SOLVE) {
            Cluster2 cluster = new Cluster2(data, sum, mirror, w, h, remove_bright_areas);
            Holder2<P2i, Map<Integer, P>> h2 = cluster.run();
            if (h2 == null && Thread.currentThread().isInterrupted()) {
                Logg.d(TAG, "Manager, interrupted 9, cluster");
                onInterrupted();
                return;
            } else if (h2 == null) {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("no clusters found");
                Logg.d(TAG, "no clusters found");
                return;
            }

            Plate plate = null;
            try {
                plate = new Plate(h2, Prefs.MAX_DST, lst, lat, gyro_alt, check_alt);
            } catch (Exception e) {
                sendAction(Prefs.ON_SOLVED_ERROR);
                sendMessage("Plate Exception");
                Logg.d(TAG, "Plate Exception=" + e);
                return;
            }
            if (align_type == Prefs.AlignType.HR_ALIGNMENT) {
                P4 radecs = plate.find_point_ra_dec(rcx, rcy);
                if (radecs != null) {
                    sendP4(radecs, Prefs.PLATE_SOLVE_RADECS);
                    beep();
                    //sendMessage("Plate solved");
                } else if (radecs == null && Thread.currentThread().isInterrupted()) {
                    Logg.d(TAG, "Manager, interrupted 10, plate");
                    onInterrupted();
                    return;
                } else {
                    sendAction(Prefs.ON_SOLVED_ERROR);
                    sendMessage("Plate not solved!");

                }
                return;
            } else if (align_type == Prefs.AlignType.OBJECT_ALIGNMENT) {
                P4 center_info = plate.find_center();
                if (center_info != null) {
                    sendP4(center_info, Prefs.PLATE_SOLVE_CENTER_INFO);
                    beep();
                    //sendMessage("Plate solved");

                } else if (center_info == null && Thread.currentThread().isInterrupted()) {
                    Logg.d(TAG, "Manager, interrupted 11, plate");
                    onInterrupted();
                    return;
                } else {
                    sendAction(Prefs.ON_SOLVED_ERROR);
                    sendMessage("Plate not solved!");

                }
                return;
            }
        } else {
            sendMessage("Wrong logic. photo type");
        }

    }


    private void saveRaw(short[] data, int w, int h, File f) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(w * h * 2);
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        shortBuffer.put(data);
        byte[] array = byteBuffer.array();

        try {
            FileOutputStream fos = new FileOutputStream(f);
            DataOutputStream out = new DataOutputStream(fos);
            out.writeShort(w);
            out.writeShort(h);
            out.write(array);
            out.close();
        } catch (Exception e) {
            Logg.d(TAG, "saveRaw exception=" + e);
        }

    }

    private void beep() {
        if (Prefs.BEEP_ON) {
            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 500); //3 beeps
        }

    }
}



