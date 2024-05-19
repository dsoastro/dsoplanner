package com.astro.dsoplanner.pushcamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

//import javax.imageio.ImageIO;

import com.astro.dsoplanner.graph.camera.Prefs;
import com.astro.dsoplanner.util.Holder2;
import com.astro.dsoplanner.Logg;

public class Cluster {
    public static final String TAG = "PushCamera";

    static String PATH = "../";
    int threshold = 20;
    int eps = 10;
    int m = 2;
    int max_pixels_in_cluster = 150;
    boolean r_bright_areas = true;
    boolean auto_threshold = true;
    int eps_bright = 10;
    int m_bright = 4;
    int min_pixels_in_area = 100;
    int decimation = 8; //should be not more that 8, otherwise decimation may go out of short
    int inSampleSize = 1;
    P4i crop = null;
    int min_clusters = 15;
    boolean mirror = false;

    String name;

    //{cluster #k:[(row,col)]}
    Map<Integer, List<P2i>> clusters = new HashMap<>();

    public Cluster(String name, boolean mirror) {

        this.name = name;
        this.mirror = mirror;
    }

    private P4 get_data(List<P2i> points, short[][] data2) {

        double brightness = 0;
        double x = 0;
        double y = 0;
        int count = 0;
        double dispersion = 0;
        for (P2i p : points) {
            int i = p.x;
            int j = p.y;
            brightness += data2[i][j];
            x += i;
            y += j;
            count += 1;
        }

        x = x / count;
        y = y / count;
        for (P2i p : points) {
            int i = p.x;
            int j = p.y;
            dispersion += (i - x) * (i - x) + (j - y) * (j - y);
        }
        double sigma = 0;
        if (count > 1)
            sigma = Math.sqrt(dispersion / (count - 1));
        return new P4(brightness, x, y, sigma);
    }

    private void print_cluster_info(short[][] data2) {
        for (Map.Entry<Integer, List<P2i>> e : clusters.entrySet()) {
            int k = e.getKey();
            List<P2i> v = e.getValue();
            Logg.d(TAG, "Cluster #" + k + " Size:" + v.size() + " Its pixels (x, y):");

            P4 tmp = get_data(v, data2);
            double brightness = tmp.x;
            double x = tmp.y;
            double y = tmp.z;
            double sigma = tmp.t;
            print("Cluster #" + k + " total brightness " + brightness + " average pixel brightness " + brightness / v.size() + " x " + x + " y " + y + " radius " + sigma);

        }
    }

    private short[][] decimate(short[][] data, int decimation) {
        long t1 = Calendar.getInstance().getTimeInMillis();
        int N = decimation;
        int rows = data.length;
        int cols = data[0].length;

        int nr = rows / N;
        int nc = cols / N;
        short[][] data2 = new short[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                short sum = 0;
                int base_r = i * N;
                int base_c = j * N;
                for (int k = 0; k < N; k++) {
                    for (int l = 0; l < N; l++) {
                        sum += data[base_r + k][base_c + l];
                    }
                }
                data2[i][j] = sum;
            }
        }
        long t2 = Calendar.getInstance().getTimeInMillis();
        print("decimate, time " + (t2 - t1));
        return data2;
    }

    private Map<Integer, List<P2i>> get_bright_areas(short[][] data, int threshold, int eps, int m, int decimation, int min_pixels_in_area) {
        long t1 = Calendar.getInstance().getTimeInMillis();
        print("get_bright_areas, start");
        short[][] data2 = decimate(data, decimation);
        print("get_bright_areas, decimation over");
        int rows = data2.length;
        int cols = data2[0].length;
        List<P2i> P = new ArrayList<P2i>();

        Map<Integer, List<P2i>> dbscan_hash_db = new HashMap<>(); //  # {id: [(x,y)]}

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (data2[i][j] > threshold) {
                    P2i p = new P2i(i, j);
                    P.add(p);
                    int id_ = Utils.get_dbscan_id(eps, p);
                    if (dbscan_hash_db.get(id_) == null)
                        dbscan_hash_db.put(id_, new ArrayList<P2i>());
                    dbscan_hash_db.get(id_).add(p);
                }
            }
        }

        print("get bright areas, points=" + P.size());

        Map<Integer, List<P2i>> clusters = new DbScan(P, eps, m, rows, cols, dbscan_hash_db).getClusters();

        clusters.remove(DbScan.NOISE);

        Set<Integer> to_remove = new HashSet<>();
        for (Map.Entry<Integer, List<P2i>> e : clusters.entrySet()) {
            int k = e.getKey();
            List<P2i> v = e.getValue();
            if (v.size() < min_pixels_in_area)
                to_remove.add(k); // remove possible stars from bright areas
        }
        for (int k : to_remove) {
            clusters.remove(k);
        }


        long t2 = Calendar.getInstance().getTimeInMillis();
        print("get bright areas, time " + (t2 - t1));
        return clusters;
    }

    private short[][] remove_bright_areas(short[][] data, Map<Integer, List<P2i>> clusters, int decimation) {
        int rows = data.length;
        int cols = data[0].length;
        for (Map.Entry<Integer, List<P2i>> e : clusters.entrySet()) {

            List<P2i> v = e.getValue();
            for (P2i p : v) {
                int i = p.x;
                int j = p.y;
                int row = i * decimation;
                int col = j * decimation;
                for (int k = 0; k < 2 * decimation; k++) {
                    for (int l = 0; l < 2 * decimation; l++) {
                        int r = row + k;
                        int c = col + l;
                        if (r < rows && c < cols)
                            data[r][c] = 0;
                        r = row - k;
                        c = col - l;
                        if (r >= 0 && c >= 0)
                            data[r][c] = 0;


                    }
                }
            }
        }

        return data;
    }


    /**
     * find threshold so that there are "size" elements in data above it
     *
     * @param data
     * @return
     */
    private int find_threshold(short[][] data, int size) {
        short[] a = new short[Short.MAX_VALUE];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                a[data[i][j]] += 1;
            }
        }
        int sum = 0;
        int pos = Short.MAX_VALUE - 1;
        while (sum < size && pos > 0) {
            sum += a[pos];
            pos--;
        }
        Logg.d(TAG, "sum=" + sum + " pos=" + pos);
        return pos;
    }

    public static long total_bright_areas = 0;

    public Holder2<P2i, Map<Integer, P>> run() throws Exception {

        long t1 = Calendar.getInstance().getTimeInMillis();
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (inSampleSize != 1)
            options.inSampleSize = inSampleSize;
        Bitmap image = BitmapFactory.decodeFile(name, options);
        int w = image.getWidth();
        int h = image.getHeight();
        int rows = h;
        int cols = w;
        int[] pixels = new int[w * h];
        image.getPixels(pixels, 0, w, 0, 0, w, h);

        long t2 = Calendar.getInstance().getTimeInMillis();
        print("loading image complete, time=" + (t2 - t1));
        print("rows=" + rows + " cols=" + cols);
        short[][] data2 = new short[rows][cols];
        double sum = 0;
        if (mirror) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    int color = pixels[i * cols + j];
                    int blue = color & 0xff;
                    int green = (color & 0xff00) >> 8;
                    int red = (color & 0xff0000) >> 16;
                    data2[i][cols - j - 1] = (short) ((blue + green + red) / 3);
                    sum += data2[i][j];
                }
            }
        } else {
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
        }
        pixels = null;
        image = null;

        double av_pixel = sum / (rows * cols);
        print("average pixel value=" + av_pixel);
        long t3 = Calendar.getInstance().getTimeInMillis();
        print("calculating average, time=" + (t3 - t2));

        long t4 = Calendar.getInstance().getTimeInMillis();
        if (r_bright_areas) {
            int thresh = 3328;

            Map<Integer, List<P2i>> bright_clusters = get_bright_areas(data2, (int) (av_pixel * decimation * decimation * 1.3), eps_bright, m_bright, decimation, min_pixels_in_area);
            data2 = remove_bright_areas(data2, bright_clusters, decimation);
        }
        long t4_1 = Calendar.getInstance().getTimeInMillis();
        print("bright areas removing, total=" + (t4_1 - t4));
        total_bright_areas += (t4_1 - t4);

        int row_low = 0, row_high = 0, col_low = 0, col_high = 0;
        if (crop != null) {
            row_low = crop.x;
            row_high = crop.y;
            col_low = crop.z;
            col_high = crop.t;
        }
        print("" + row_low + " " + row_high + " " + col_low + " " + col_high);
        List<P3s> P_sort = new ArrayList<>();

        int thresh = find_threshold(data2, 10000);
        Logg.d(TAG, "new threshold=" + thresh);


        if (crop == null) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (data2[i][j] > thresh)
                        P_sort.add(new P3s(i, j, data2[i][j]));

                }
            }
        } else {
            for (int i = row_low; i < row_high; i++) {
                for (int j = col_low; j < col_high; j++) {
                    if (data2[i][j] > thresh)
                        P_sort.add(new P3s(i, j, data2[i][j]));
                }
            }
        }

        print("Number of pixels above threshold=" + P_sort.size());
        Comparator<P3s> comp = new Comparator<P3s>() {
            @Override
            public int compare(P3s o1, P3s o2) {
                if (o1.z > o2.z)
                    return 1;
                else if (o1.z == o2.z)
                    return 0;
                else
                    return -1;
            }
        };
        Collections.sort(P_sort, comp);

        int P_sort_len = P_sort.size();
        int P_sort_index = Math.max(P_sort_len - 10000, 0);
        double P_sort_threshold = P_sort.get(P_sort_index).z;
        //{int:(x,y)}
        Map<Integer, P> data;

        int step = 3;
        while (true) {
            data = new HashMap<>();
            Map<Integer, List<P2i>> dbscan_hash_db = new HashMap<>();
            List<P2i> P = new ArrayList<>();
            for (int i = P_sort_index; i < P_sort_len; i++) {
                P3s tmp = P_sort.get(i);
                P2i p = new P2i(tmp.x, tmp.y);
                P.add(p);
                int id_ = Utils.get_dbscan_id(eps, p);
                if (dbscan_hash_db.get(id_) == null)
                    dbscan_hash_db.put(id_, new ArrayList<P2i>());
                dbscan_hash_db.get(id_).add(p);
            }
            if (!auto_threshold && P.size() > 15000) {
                print("Error. Too much points");
                return null;
            }
            clusters = new DbScan(P, eps, m, rows, cols, dbscan_hash_db).getClusters();
            clusters.remove(DbScan.NOISE);
            for (Map.Entry<Integer, List<P2i>> e : clusters.entrySet()) {
                List<P2i> points = e.getValue();
                int key = e.getKey();
                P4 p4 = get_data(points, data2);
                double x = p4.y;
                double y = p4.z;
                data.put(key, new P(x, y));

            }

            if (!auto_threshold) {
                print_cluster_info(data2);
                break;
            } else {
                print("num points " + (P_sort_len - P_sort_index) + " number of clusters " + clusters.size() + " index " + P_sort_index + " brightness " + P_sort.get(P_sort_index).z);
                if (clusters.size() < 25)
                    step = 1;
                if (clusters.size() > min_clusters) {
                    P_sort_threshold += step;
                    if (P_sort_threshold > 255)
                        break;
                    while (P_sort_index < P_sort_len && P_sort.get(P_sort_index).z < P_sort_threshold)
                        P_sort_index += 1;
                } else {
                    print_cluster_info(data2);
                    break;
                }
            }
        }
        long t5 = Calendar.getInstance().getTimeInMillis();
        print("Cluster run time from data load " + (t5 - t4));
        return new Holder2<P2i, Map<Integer, P>>(new P2i(rows, cols), data);

    }

    public static void print(String s) {
        Logg.d(TAG, s);
    }

}
