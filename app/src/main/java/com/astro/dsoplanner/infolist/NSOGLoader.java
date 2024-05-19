package com.astro.dsoplanner.infolist;

import java.io.EOFException;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.astro.dsoplanner.ErrorHandler;


import com.astro.dsoplanner.SearchRules;
import com.astro.dsoplanner.graph.TychoStarFactory;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.database.Db;
import com.astro.dsoplanner.database.NgcicDatabase;
import com.astro.dsoplanner.graph.PgcFactory;

/**
 * @author leonid
 */
public class NSOGLoader implements InfoListLoader {

    private static final String SELECT_FROM_OBJS_WHERE_CON = "select * from objs where con=";

    private static final int CON = 2;
    private static final int ID = 1;
    private static final int CID = 0;

    private static final String TAG = NSOGLoader.class.getSimpleName();
    String dbname;
    Db db;
    Context context;
    int con;
    Cursor cursor;

    public NSOGLoader(Context context, String dbname, int con) {
        this.dbname = dbname;
        this.context = context;
        this.con = con;
    }

    public void open() {
        db = new Db(context, dbname);
        try {
            db.open();
            String q = SELECT_FROM_OBJS_WHERE_CON + con;
            Log.d(TAG, "q=" + q);
            cursor = db.rawQuery(q);
            Log.d(TAG, "size=" + cursor.getCount());
        } catch (Exception e) {

        }
    }

    public String getName() {
        return "";
    }

    public void close() {
        try {
            db.close();
        } catch (Exception e) {
        }
    }

    public Object next(ErrorHandler.ErrorRec rec) throws EOFException {
        if (cursor == null)
            throw new EOFException();
        try {
            if (cursor.moveToNext()) {
                int cid = cursor.getInt(CID);
                int id = cursor.getInt(ID);

                AstroCatalog cat = null;
                if (cid == AstroCatalog.NGCIC_CATALOG)
                    cat = new NgcicDatabase(context);
                else if (cid != AstroCatalog.TYCHO_CATALOG && cid != AstroCatalog.PGC_CATALOG)
                    cat = SearchRules.getCatalog(cid, context);
                if (cat != null) {
                    ErrorHandler eh = new ErrorHandler();
                    cat.open(eh);
                    if (!eh.hasError()) {
                        AstroObject obj = cat.getObject(id);
                        cat.close();
                        if (obj == null)
                            Log.d(TAG, "error obj=" + cid + " " + id);
                        if (obj != null)
                            return new ObsInfoListImpl.Item(obj, false);
                        else
                            return null;
                    } else {
                        Log.d(TAG, "error " + cid + " " + id);
                    }
                } else if (cid == AstroCatalog.TYCHO_CATALOG) {
                    TychoStarFactory factory = new TychoStarFactory();
                    factory.open();
                    AstroObject obj = factory.get(id);
                    if (obj != null)
                        return new ObsInfoListImpl.Item(obj, false);
                    else
                        return null;
                } else if (cid == AstroCatalog.PGC_CATALOG) {
                    PgcFactory factory = new PgcFactory();
                    factory.open();
                    AstroObject obj = factory.get(id);
                    if (obj != null)
                        return new ObsInfoListImpl.Item(obj, false);
                    else
                        return null;
                }

            } else
                throw new EOFException();
        } catch (Exception e) {
            throw new EOFException();
        }
        return null;
    }


}
