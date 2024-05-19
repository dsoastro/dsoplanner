package com.astro.dsoplanner.misc;

import static java.lang.Math.PI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.astro.dsoplanner.ErrorHandler;
import com.astro.dsoplanner.Global;


import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.database.CustomDatabase;
import com.astro.dsoplanner.database.DbManager;
import com.astro.dsoplanner.database.NgcicDatabase;

public class CrossRef {
	
	private static final String _ID_0 = "_id>0";
	
	private static final String TAG = CrossRef.class.getSimpleName();
	
	private static double cdist(AstroObject o1, AstroObject o2){
		
			double cosd = Math.sin(o1.dec*PI/180) * Math.sin(o2.dec*PI/180) + Math.cos(o1.dec*PI/180) * Math.cos(o2.dec*PI/180) * Math.cos((o1.ra-o2.ra)*PI/12);
			return cosd;
		
	}
	public static void compare(AstroCatalog c1, AstroCatalog c2){
		ErrorHandler eh=new ErrorHandler();
		try{
			PrintWriter pw=new PrintWriter(new FileOutputStream(new File(Global.exportImportPath,"cross.txt")));
			c1.open(eh);
			c2.open(eh);
			List<AstroObject> list1=c1.search(_ID_0);
			List<AstroObject> list2=c2.search(_ID_0);
			int i=0;
			for(AstroObject o1:list1){
				if(i%100==0) Log.d(TAG,""+i);
				i++;
				for(AstroObject o2:list2){
					if(cdist(o1,o2)>Math.cos(2/60f*Math.PI/180)){
						if(Math.abs(o1.getA()-o2.getA())<0.3*Math.max(o1.getA(), o2.getA())){
							pw.println(o1.getShortName()+";"+o2.getShortName());
						}
					}
				}
			}
			c1.close();
			c2.close();
			pw.close();
			Log.d(TAG,"over");
		}
		catch(Exception e){
			Log.d(TAG,"exception="+e);
		}
	}
	public static void cmpUGCNGC(Context context){
		AstroCatalog c2=new NgcicDatabase(context);
		AstroCatalog c1=new CustomDatabase(context, DbManager.getDbFileName(AstroCatalog.UGC),AstroCatalog.UGC);
		compare(c1,c2);
	}
	public static void printObjs(Context context){
		ErrorHandler eh=new ErrorHandler();
		try{
			PrintWriter pw=new PrintWriter(new FileOutputStream(new File(Global.exportImportPath,"cross.txt")));
			AstroCatalog c1=new CustomDatabase(context,DbManager.getDbFileName(AstroCatalog.UGC),AstroCatalog.UGC);
			c1.open(eh);
			List<AstroObject> list1=c1.search(_ID_0);
			for(AstroObject obj:list1){
				pw.println("query "+obj.getShortName());
			}
			pw.close();
		}
		catch(Exception e){}

	}

}
