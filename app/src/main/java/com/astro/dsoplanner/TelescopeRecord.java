package com.astro.dsoplanner;

import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;

import com.astro.dsoplanner.base.Exportable;

public class TelescopeRecord implements Exportable {

    private static final String DESCR = ", descr=";
    private static final String EYEPIECES = ", eyepieces=";
    private static final String PASS2 = ", pass=";
    private static final String FOCUS2 = ", focus=";
    private static final String APERTURE2 = ", aperture=";
    private static final String NAME2 = ", name=";
    private static final String TELESCOPE_RECORD_ID = "TelescopeRecord [id=";
    public static final String TID = "id";
    public static final String TNAME = "name";
    public static final String TAPERTURE = "aperture";
    public static final String TFOCUS = "focus";
    public static final String TPASS = "pass";
    public static final String TEP = "eyepieces";
    public static final String TDESCR = "descr";


    public int id;//id in catalog
    public String name = "";        //Instrument name
    public double aperture = 0;    //in mm
    public double focus = 0;        //focal range in mm
    public double pass = 0;        //pass through
    public int ep_id = 0;    //id of eyepieces records in the EP database
    public String note = "";        //instrument's short note.

    public TelescopeRecord() {

    }

    public TelescopeRecord(Bundle b) {
        id = b.getInt(TID);
        name = b.getString(TNAME);
        aperture = b.getDouble(TAPERTURE);
        focus = b.getDouble(TFOCUS);
        pass = b.getDouble(TPASS);
        ep_id = b.getInt(TEP);
        note = b.getString(TDESCR);
    }

    public TelescopeRecord(int int1, String string, double double1,
                           double double2, double double3, int int2, String string2) {
        id = int1;
        name = string;
        aperture = double1;
        focus = double2;
        pass = double3;
        ep_id = int2;
        note = string2;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TelescopeRecord) {
            TelescopeRecord rec = (TelescopeRecord) o;
            if (rec.id == this.id)
                return true;
        }
        return false;
    }

    public Bundle getBundle() {
        Bundle b = new Bundle();
        b.putInt(TID, id);
        b.putString(TNAME, name);
        b.putDouble(TAPERTURE, aperture);
        b.putDouble(TFOCUS, focus);
        b.putDouble(TPASS, pass);
        b.putInt(TEP, ep_id);
        b.putString(TDESCR, note);

        return b;
    }

    @Override
    public String toString() {
        return TELESCOPE_RECORD_ID + id
                + NAME2 + name
                + APERTURE2 + aperture
                + FOCUS2 + focus
                + PASS2 + pass
                + EYEPIECES + ep_id
                + DESCR + note
                + "]";
    }

    public HashMap<String, String> getStringRepresentation() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TNAME, name);
        map.put(TAPERTURE, "" + aperture);
        map.put(TFOCUS, "" + focus);
        map.put(TPASS, "" + pass);
        map.put(TEP, "" + ep_id);
        map.put(TDESCR, note);
        return map;
    }

    public int getClassTypeId() {
        return Exportable.TELESCOPE_RECORD;
    }

    public byte[] getByteRepresentation() {
        throw new UnsupportedOperationException();
    }

    public String getSummary(Context context) {
        String summary = name + " (" + (int) aperture + "x" + (int) focus
                + ((ep_id > 0) ? context.getString(R.string._ep) : ")"); //eyepieces mark
        return summary;
    }

}
