package com.astro.dsoplanner;

import java.util.HashMap;
import java.util.Locale;

import android.os.Bundle;

import com.astro.dsoplanner.base.Exportable;

public class EyepiecesRecord implements Exportable {

    private static final String DESCR = ", descr=";
    private static final String EP_IDS = ", ep_ids=";
    private static final String AFOV2 = ", AFOV=";
    private static final String FOCUS2 = ", focus=";
    private static final String NAME2 = ", name=";
    private static final String TELESCOPE_RECORD_ID = "EpRecord [id=";
    public static final String EID = "id";
    public static final String ENAME = "name";
    public static final String EFOCUS = "focus";
    public static final String EAFOV = "afov";
    public static final String EEP = "scopes";
    public static final String EDESCR = "descr";


    public static final String NO_CROSS_DATA = "NO";
    static final String CROSS_DELIMITER = "\0x09";

    private int id;//id in catalog
    private String name = "";        //Eyepiece name
    private double focus = 0;        //focal range in mm

    public static boolean hasAngleData(String cross) {
        return !cross.contentEquals(NO_CROSS_DATA);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFocus(double focus) {
        this.focus = focus;
    }

    public void setAfov(double afov) {
        this.afov = afov;
    }

    public void setEp_id(String ep_id) {
        this.ep_id = ep_id;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setCrossData(String crossData) {
        this.crossData = crossData;
    }

    private double afov = 0;        //AFOV in deg
    private String ep_id = "";    //id of eyepieces records in the EP database
    private String note = "";        //Eyepiece's short note.
    private String crossData = NO_CROSS_DATA;
    public EyepiecesRecord() {

    }

    public EyepiecesRecord(Bundle b) {
        id = b.getInt(EID);
        name = b.getString(ENAME);
        focus = b.getDouble(EFOCUS);
        afov = b.getDouble(EAFOV);
        ep_id = b.getString(EEP);
        decodeNote(b.getString(EDESCR));
    }

    public EyepiecesRecord(int int1, String s1, double double1,
                           double double2, String s2, String s3) {
        id = int1;
        name = s1;
        focus = double1;
        afov = double2;
        ep_id = s2;
        decodeNote(s3);
    }

    /**
     * Extract data from the note.
     * <br>
     * The former note field might now contain the cross angle.
     * This way we can have additional data not required for database manipulations
     * but no changes in it its schema or our EP legacy fields.
     * <ul>Can be extended further;
     *
     * @param s former note string from the database.
     */
    private void decodeNote(String s) {
        if (s != null && s.length() > 1) {
            String[] arr = s.split(CROSS_DELIMITER, 2);
            if (arr.length == 2) {
                note = arr[0];
                crossData = arr[1];
            } else {
                note = arr[0];
                crossData = NO_CROSS_DATA;
            }
        } else { //old note
            note = s;
            crossData = NO_CROSS_DATA;
        }
    }

    /**
     * Encode data to the note field.
     *
     * @return encoded note string.
     * @see #decodeNote(String)
     */
    static String encodeNote(String note, String crossData) {
        return note + CROSS_DELIMITER + crossData;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getFocus() {
        return focus;
    }

    public double getAfov() {
        return afov;
    }

    public String getEp_id() {
        return ep_id;
    }

    public String getNote() {
        return note;
    }

    public String getAngleData() {
        return crossData;
    }

    public String getDatabaseNote() {
        return encodeNote(note, crossData);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EyepiecesRecord) {
            EyepiecesRecord rec = (EyepiecesRecord) o;
            if (rec.id == this.id)
                return true;
        }
        return false;
    }

    public Bundle getBundle() {
        Bundle b = new Bundle();
        b.putInt(EID, id);
        b.putString(ENAME, name);
        b.putDouble(EFOCUS, focus);
        b.putDouble(EAFOV, afov);
        b.putString(EEP, ep_id);
        b.putString(EDESCR, encodeNote(note, crossData));

        return b;
    }

    @Override
    public String toString() {
        return TELESCOPE_RECORD_ID + id
                + NAME2 + name
                + FOCUS2 + focus
                + AFOV2 + afov
                + EP_IDS + ep_id
                + DESCR + encodeNote(note, crossData)
                + "]";
    }

    public String getSummary() {

        boolean ep = !SettingsActivity.isCCD(focus);
        String focus_string = String.format(Locale.US, "%.1f", ep ? focus : (focus - EyepiecesListActivity.OFFSET));
        String summary = name + " (" + (focus_string) + "/" + (ep ? (int) afov : String.format(Locale.US, "%.1f", (afov - EyepiecesListActivity.OFFSET))) + (ep ? "" : " CCD") + ")";
        return summary;
    }

    public String getSummaryShort() {
        return name;
    }

    public HashMap<String, String> getStringRepresentation() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(ENAME, name);
        map.put(EFOCUS, "" + focus);
        map.put(EAFOV, "" + afov);
        map.put(EEP, "" + ep_id);
        map.put(EDESCR, encodeNote(note, crossData));
        return map;
    }

    public int getClassTypeId() {
        return Exportable.EYEPIECES_RECORD;
    }

    public byte[] getByteRepresentation() {
        throw new UnsupportedOperationException();
    }

}
