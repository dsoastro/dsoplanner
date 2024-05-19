package com.astro.dsoplanner.database;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * general class for databases
 * @author leonid
 *
 */

public class Db {
	Helper helper;
	String name;
	Context context;
	SQLiteDatabase db;
	private class Helper extends SQLiteOpenHelper {



		private static final int DATABASE_VERSION = 2;

		

		public Helper(Context ctx,String name) {
			super(ctx, name, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}

	public Db(Context context,String name){
		this.context=context;
		helper=new Helper(context,name);
	}
	
	public void open(){		
			db = helper.getReadableDatabase(); 		
	}
	public void openWritable(){		
		db = helper.getWritableDatabase(); 		
}
	public boolean isOpen(){
		return db.isOpen();
	}
	public void close(){
		helper.close();
	}
	public String toString(){
		return name;
	}
	
	public Cursor rawQuery(String s){
		return db.rawQuery(s, null);
	}
	
	public void execSQL(String sql){
		db.execSQL(sql);
	}
	
	

}
