package com.astro.dsoplanner.base;

import java.io.DataInputStream;
import java.util.Map;

public interface Exportable {
    static final int NGCIC_OBJECT = 1;//object class type
    static final int OBS_LIST_ITEM = 2;
    static final int CUSTOM_OBJECT = 3;
    static final int PLANET_OBJECT = 4;
    static final int HR_STAR_OBJECT = 5;
    static final int NOTE_RECORD = 6;
    static final int DB_LIST_ITEM = 7;
    static final int SEARCH_REQUEST_ITEM = 8;
    static final int TELESCOPE_RECORD = 9;
    static final int EYEPIECES_RECORD = 10;
    static final int CUSTOM_OBJECT_LARGE = 11;
    static final int APP_RECORD = 12;
    static final int LOCATION_ITEM = 13;
    static final int CONTOUR_OBJECT = 14;
    static final int COMET_OBJECT = 15;
    static final int MINOR_PLANET_OBJECT = 16;
    static final int SH_ITEM = 17;
    static final int NGC_PIC_ITEM = 18;
    static final int DS_OBJECT = 19;
    static final int TYCHO_STAR_OBJECT = 20;
    static final int UCAC4_STAR_OBJECT = 21;

    int getClassTypeId();//NGCIC object, observation list item, Planet, etc

    byte[] getByteRepresentation();//if an object has a catalog and id number in that catalog, it adds this to String Represenation

    interface Inflater {
        Object inflate(int classTypeId, DataInputStream stream);
    }

    Map<String, String> getStringRepresentation();
}