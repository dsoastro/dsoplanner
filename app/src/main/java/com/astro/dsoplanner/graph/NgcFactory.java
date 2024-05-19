package com.astro.dsoplanner.graph;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.SearchRules;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.CustomObject;
import com.astro.dsoplanner.base.Point;

public class NgcFactory implements StarFactory {
    private static final String TAG = NgcFactory.class.getSimpleName();
    private static final int rec_size = 33;
    public static final int LAYER_OFFSET = 10000;
    boolean gc;
    boolean gx;
    boolean gcld;
    boolean hii;
    boolean neb;
    boolean oc;
    boolean ocneb;
    boolean pn;
    boolean snr;

    boolean ast;
    boolean cg;
    boolean dn;
    boolean star;
    boolean zeromagOn;
    boolean layer_vis;
    double vis_threshold;
    boolean fov_ok;

    public NgcFactory() {
        gc = SettingsActivity.isGCon();
        gx = SettingsActivity.isGxOn();
        gcld = SettingsActivity.isGxyCldOn();
        hii = SettingsActivity.isHiiOn();
        neb = SettingsActivity.isNebOn();
        oc = SettingsActivity.isOCon();
        ocneb = SettingsActivity.isOCNebOn();
        pn = SettingsActivity.isPNon();
        snr = SettingsActivity.isSNRon();
        ast = SettingsActivity.isAstOn();
        cg = SettingsActivity.isCGon();
        dn = SettingsActivity.isDNon();
        star = SettingsActivity.isStarOn();

        zeromagOn = SettingsActivity.isZeroMagOn();
        fov_ok = Point.getFOV() < SettingsActivity.getNoMagFOV();
        Log.d(TAG, " " + Point.getFOV() + " " + SettingsActivity.getNoMagFOV() + " " + fov_ok);
        layer_vis = SettingsActivity.showVisibleStatus();
        vis_threshold = SettingsActivity.getLayerVisibilityThreshold();
    }


    RandomAccessFile db;
    RandomAccessFile dbn;//names

    public void open() throws IOException {
        db = new RandomAccessFile(Global.ngcDb, "r");
        dbn = new RandomAccessFile(Global.ngcnDb, "r");
    }

    public List<Point> get(int q, double mag_limit) throws IOException {
        ArrayList<Point> as = new ArrayList<Point>();

        int pos = Global.ngcQ.get(q);
        int length = Global.ngcQ.get(q + 1) - pos;
        if (mag_limit == 0 && !layer_vis) return as;

        for (int i = 0; i < length; i++) {
            db.seek((i + pos) * rec_size);
            double ra = db.readFloat();
            double dec = db.readFloat();

            double mag = db.readFloat();
            double a = db.readFloat();

            double b = db.readFloat();

            int type = db.readByte();
            int pa = db.readByte();
            if (pa < 0) pa += 256;
            int catalog = db.readByte();
            catalog += LAYER_OFFSET;
            int id = db.readShort();
            int ref = db.readInt();
            int npos = db.readInt();
            dbn.seek(npos);
            String name = dbn.readUTF();
            if (name == null)
                name = "";

            boolean include = false;
            if (layer_vis) {
                if (mag == 0 && !(zeromagOn && fov_ok))
                    continue;
                if (!isTypeOK(type))
                    continue;
                include = AstroTools.CheckVisibility(a, b, mag == 0 ? Double.NaN : mag, vis_threshold, SearchRules.EMPTY_RULE_INCLUDE, type);

            } else {
                include = (mag <= mag_limit && isTypeOK(type));
                if (mag == 0 && !(zeromagOn && fov_ok))
                    include = false;
            }


            if (include) {
                CustomObject obj = new CustomObject(catalog, id, ra, dec, AstroTools.getConstellation(ra, dec), type, "", a == 0 ? Double.NaN : a, b == 0 ? Double.NaN : b, mag == 0 ? Double.NaN : mag, pa, name, name, "");
                obj.setLayerFlag();
                obj.ref = ref;

                as.add(obj);
            }


        }
        return as;
    }

    private boolean isTypeOK(int type) {
        boolean result = type == AstroObject.GC && gc || type == AstroObject.Gxy && gx || type == AstroObject.GxyCld && gcld
                || type == AstroObject.HIIRgn && hii || type == AstroObject.Neb && neb || type == AstroObject.OCNeb && ocneb
                || type == AstroObject.PN && pn || type == AstroObject.SNR && snr || type == AstroObject.OC && oc || type == AstroObject.AST
                && ast || type == AstroObject.CG && cg || type == AstroObject.DN && dn || type == AstroObject.Star && star;
        return result;
    }

    public void close() throws IOException {
        db.close();
        dbn.close();
    }

}
