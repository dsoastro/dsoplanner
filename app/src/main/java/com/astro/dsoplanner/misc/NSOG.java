package com.astro.dsoplanner.misc;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import android.content.Context;
import android.util.Log;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.ErrorHandler.ErrorRec;
import com.astro.dsoplanner.Global;
import com.astro.dsoplanner.infolist.InfoListLoader;
import com.astro.dsoplanner.infolist.ObsInfoListImpl;
import com.astro.dsoplanner.base.PgcObject;
import com.astro.dsoplanner.infolist.SkyToolsLoader;

/**
 * making text file for building nsog db
 * manually add index with
 * create index index_con on objs(con);
 *
 * @author leonid
 */
public class NSOG {
    private static final String TAG = NSOG.class.getSimpleName();
    Context context;


    public NSOG(Context context) {
        super();
        this.context = context;

    }

    String path = Global.exportImportPath + "NSOG/";

    /**
     * before using modify TychoStar for cpos field!!!
     *
     * @throws Exception
     */
    public void make() throws Exception {
        PrintWriter pw = new PrintWriter(new FileOutputStream(new File(Global.exportImportPath, "err.txt")));
        PrintWriter pw2 = new PrintWriter(new FileOutputStream(new File(Global.exportImportPath, "out.txt")));
        pw2.println("drop table if exists objs;");
        pw2.println("create table objs (cid integer,id integer,con integer);");
        pw2.println("begin;");
        File[] files = new File(path).listFiles();
        for (File f : files) {
            InfoListLoader loader = new SkyToolsLoader(
                    new BufferedReader(new InputStreamReader(new FileInputStream(f))), context);
            ErrorHandler.ErrorRec e = new ErrorRec();
            loader.open();
            int line = 0;
            pw.println(f.getName());
            while (true) {
                try {
                    Object o = loader.next(e);
                    line++;
                    Log.d(TAG, "o=" + o);
                    if (o == null)
                        pw.println(line + " " + e.line);
                    else {
                        ObsInfoListImpl.Item item = (ObsInfoListImpl.Item) o;
                        AstroObject obj = item.x;
                        int id = obj.getId();
                        if (obj.getCatalog() == AstroCatalog.TYCHO_CATALOG)
                            if (obj.getCatalog() == AstroCatalog.PGC_CATALOG)
                                id = ((PgcObject) obj).cpos;
                        pw2.println("insert into objs (cid,id,con) values(" + obj.getCatalog() + "," + id + "," + obj.getCon() + ");");
                    }


                } catch (EOFException ex) {
                    break;
                }
            }
            loader.close();

        }
        pw2.println("commit;");
        pw.close();
        pw2.close();
    }

}
