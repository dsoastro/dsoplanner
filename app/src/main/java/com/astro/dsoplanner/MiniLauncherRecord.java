package com.astro.dsoplanner;

import android.os.Bundle;

import com.astro.dsoplanner.base.Exportable;

import java.util.HashMap;

public class MiniLauncherRecord implements Exportable, Comparable<MiniLauncherRecord> {

    private static final String PKG = ", pkg=";
    private static final String LABEL2 = ", label=";
    private static final String TELESCOPE_RECORD_ID = "TelescopeRecord [id=";
    public static final String AID = "id";
    public static final String ANAME = "label";
    public static final String APACKAGE = "pkgname";

    int id;//id in catalog
    String label = "";    //app label
    String pkgname = "";    //package name

    public MiniLauncherRecord(int int1, String string, String string2) {
        id = int1;
        label = string;
        pkgname = string2;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TelescopeRecord) {
            TelescopeRecord rec = (TelescopeRecord) o;
            if (rec.id == this.id) return true;
        }
        return false;
    }

    public Bundle getBundle() {
        Bundle b = new Bundle();
        b.putInt(AID, id);
        b.putString(ANAME, label);
        b.putString(APACKAGE, pkgname);

        return b;
    }

    @Override
    public String toString() {
        return TELESCOPE_RECORD_ID + id + LABEL2 + label + PKG + pkgname + "]";
    }

    public HashMap<String, String> getStringRepresentation() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(ANAME, label);
        map.put(APACKAGE, pkgname);
        return map;
    }

    public int getClassTypeId() {
        return Exportable.TELESCOPE_RECORD;
    }

    public byte[] getByteRepresentation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(MiniLauncherRecord another) {
        return this.label.compareTo(another.label);
    }
}
