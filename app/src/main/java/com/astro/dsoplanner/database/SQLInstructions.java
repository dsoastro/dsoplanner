package com.astro.dsoplanner.database;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.util.Log;

import com.astro.dsoplanner.base.AstroObject;

/**
 * list of sql instructions from sqlite. there are two different types - select/rawQuery, 
 * and execSql for non select operations 
 * @author leonid
 *
 */
public class SQLInstructions {
	private static final String TAG = SQLInstructions.class.getSimpleName();
	public static final int SELECT=0;
	public static final int NONSELECT=1;
	
	int pointer=0;
	
	public static class Instruction{
		String sql;
		int type;
		public Instruction(String sql, int type) {
			super();
			this.sql = sql;
			this.type = type;
		}
		
	}
	public interface ProcessCursor{
		public AstroObject getObjectFromCursor(Cursor cursor);
	}
	
	private ProcessCursor pc;
	public SQLInstructions(ProcessCursor pc){
		this.pc=pc;
	}
	public ProcessCursor getPC(){
		return pc;
	}
	List<Instruction>list=new ArrayList<Instruction>();
	public void add(String sql, int type){
		list.add(new Instruction(sql,type));
	}
	
	public boolean hasNext(){
		boolean res=pointer<list.size();
		Log.d(TAG,"res="+res+" pointer="+pointer);
		return (pointer<list.size());
	}
	public void reset(){
		pointer=0;
	}
	public Instruction next(){
		if(pointer>=list.size())
			return null;
		else{
			Instruction i=list.get(pointer);
			pointer++;
			return i;
			
		}
	}

}
