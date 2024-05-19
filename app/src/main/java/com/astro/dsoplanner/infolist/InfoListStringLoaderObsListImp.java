package com.astro.dsoplanner.infolist;

import android.util.Log;

import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.ErrorHandler;


import java.io.IOException;
import java.io.InputStream;

public class InfoListStringLoaderObsListImp extends InfoListStringLoaderImp { //String Loader for OBSERVATION list

    private static final String ASTRO_OBJECT_EXPECTED = "astro object expected";

    private static final String TAG = InfoListStringLoaderObsListImp.class.getSimpleName();

    public InfoListStringLoaderObsListImp(InputStream in) {
        super(in);
    }

    public InfoListStringLoaderObsListImp(String s) {

        super(s);
    }

    @Override
    public Object next(ErrorHandler.ErrorRec erec) throws IOException {

        Object obj = super.next(erec);
        Log.d(TAG, "obj=" + obj);
        if (obj == null) {

            return null;
        }
        ObsInfoListImpl.Item item = null;

        if (!(obj instanceof ObsInfoListImpl.Item)) {
            if (!(obj instanceof AstroObject)) {
                erec.type = ErrorHandler.WRONG_TYPE;
                erec.message = ASTRO_OBJECT_EXPECTED;
                return null;
            }
            item = new ObsInfoListImpl.Item((AstroObject) obj, false);
        } else
            item = (ObsInfoListImpl.Item) obj;


        return item;

    }


}
