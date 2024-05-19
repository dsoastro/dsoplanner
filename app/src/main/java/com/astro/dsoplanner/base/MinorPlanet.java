package com.astro.dsoplanner.base;



import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Global;


import com.astro.dsoplanner.database.DbListItem;

public class MinorPlanet extends CMO {
	private static final String TAG = MinorPlanet.class.getSimpleName();
	public static final DbListItem.FieldTypes FTYPES=new DbListItem.FieldTypes();
	
	public static final String H="H";
	public static final String G="G";
	public static final String MA="M";//mean anomaly at the epoch
	public static final String A="axis";
	public static final String DOWNLOAD_FILE_NAME = "mp.db";
	public static final String DOWNLOAD_FILE_PATH= Global.tmpPath+"mp.db";//where to download updates

	static{
		FTYPES.put(CMO.NODE, DbListItem.FieldTypes.TYPE.DOUBLE);
		FTYPES.put(CMO.I, DbListItem.FieldTypes.TYPE.DOUBLE);
		FTYPES.put(CMO.W, DbListItem.FieldTypes.TYPE.DOUBLE);
		FTYPES.put(CMO.E, DbListItem.FieldTypes.TYPE.DOUBLE);
		FTYPES.put(CMO.DAY, DbListItem.FieldTypes.TYPE.DOUBLE);
		FTYPES.put(A, DbListItem.FieldTypes.TYPE.DOUBLE);
		FTYPES.put(H, DbListItem.FieldTypes.TYPE.DOUBLE);
		FTYPES.put(G, DbListItem.FieldTypes.TYPE.DOUBLE);
		FTYPES.put(CMO.YEAR, DbListItem.FieldTypes.TYPE.INT);
		FTYPES.put(CMO.MONTH, DbListItem.FieldTypes.TYPE.INT);
		FTYPES.put(MA, DbListItem.FieldTypes.TYPE.DOUBLE);
		
	}

	public static final String DB_DESC_NAME_BRIGHT="Minor Planets";
	public static final String DB_NAME_BRIGHT="brightmp.db";

	public MinorPlanet(int id, String name1, String name2, String comment, Fields fields){
		super(AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG,id, AstroObject.MINOR_PLANET,name1,name2,comment,fields);
		init();
	}
	private void init(){
		obj_type=CMO.MINOR_PLANET;
		Map<String,Double> mapd=fields.getDoubleMap();
		Map<String,Integer> mapi=fields.getIntMap();
		try{
			N=mapd.get(CMO.NODE);
			i=mapd.get(CMO.I);
			w=mapd.get(CMO.W);
			e=mapd.get(CMO.E);
			int year=mapi.get(CMO.YEAR);
			int month=mapi.get(CMO.MONTH);
			double day=mapd.get(CMO.DAY);
			jdp= AstroTools.JD(year, month, day, 0, 0);
			M0=mapd.get(MA);
			absmag=mapd.get(H);
			slope=mapd.get(G);
			sma=mapd.get(A);
			
		}
		catch(Exception e){
			Log.d(TAG,"exception="+AstroTools.getStackTrace(e));
		}
	}
	public MinorPlanet(DataInputStream stream) throws IOException{
		super(stream);
		init();
	}
	@Override
	public int getClassTypeId(){
		return Exportable.MINOR_PLANET_OBJECT;
	}

}
