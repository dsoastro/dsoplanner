package com.astro.dsoplanner;

import android.content.Context;
import android.database.Cursor;

import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.database.Db;
import com.astro.dsoplanner.graph.TychoStarFactory;

/**
 * working with SAO TYC cross ref db
 * @author leonid
 *
 */
public class SaoTyc extends Db {
	
	public SaoTyc(Context context){
		super(context,"saotyc.db");
		
	}
	public static AstroObject getTycObjFromSaoNum(int saonum, Context context){
		SaoTyc db=new SaoTyc(context);
		try{
			db.open();
			Cursor cursor=db.rawQuery("select tycpos from cross where saoid="+saonum);
			if(cursor.moveToNext()){
				int pos=cursor.getInt(0);
				TychoStarFactory factory=new TychoStarFactory();
				try{
					factory.open();
					AstroObject obj=factory.get(pos);
					return obj;
				}
				catch(Exception e){}
				finally{
					factory.close();
				}

			}
		}
		catch(Exception e){}
		finally{
			try{
				db.close();
			}
			catch(Exception e){}
		}
		return null;
	}
	
	
	
	public static AstroObject getTycObjFromTycIndex(int index,Context context){
		SaoTyc db=new SaoTyc(context);
		try{
			db.open();
			Cursor cursor=db.rawQuery("select tycpos from cross where tycid="+index);
			if(cursor.moveToNext()){
				int pos=cursor.getInt(0);
				TychoStarFactory factory=new TychoStarFactory();
				try{
					factory.open();
					AstroObject obj=factory.get(pos);
					return obj;
				}
				catch(Exception e){}
				finally{
					factory.close();
				}

			}
		}
		catch(Exception e){}
		finally{
			try{
				db.close();
			}
			catch(Exception e){}
		}
		return null;
	}
	/**
	 * 
	 * @param tycindex
	 * @return null if no SAO number
	 */
	public static String getSaoNumberFromTycIndex(int tycindex,Context context){
		SaoTyc db=new SaoTyc(context);
		try{
			db.open();
			Cursor cursor=db.rawQuery("select saoid from cross where tycid="+tycindex);
			if(cursor.moveToNext()){
				int saoid=cursor.getInt(0);
				return "SAO"+saoid;

			}
		}
		catch(Exception e){}
		finally{
			try{
				db.close();
			}
			catch(Exception e){}
		}
		return null;
	}
	
	
}
