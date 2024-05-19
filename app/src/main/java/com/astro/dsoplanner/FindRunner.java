package com.astro.dsoplanner;

import java.util.List;

import android.database.Cursor;
import android.util.Log;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.infolist.InfoList;

/**
 * Implements find/next routine for lists
 * @author leonid
 *
 */
public class FindRunner {
	
	private static final String FRR = "frr";
	
	private static final String TAG = FindRunner.class.getSimpleName();
	/**
	 * Analyses object to see if it contains the search string
	 * @author leonid
	 *
	 */
	static interface Matcher{
		/**
		 * 
		 * @param o - object to be analised
		 * @param searchString - string to be searched inside an object
		 * @return true if a string matches, false otherwise
		 */
		public boolean match(Object o,String searchString);
	}
	/**
	 * Use for registering listeners
	 * @author leonid
	 *
	 */
	
	static interface ListAdapter{
		public Object get(int pos);
		public int size();
	}
	/**
	 * Adapter for a usual List
	 * @author leonid
	 *
	 */
	static class BasicListAdapter implements ListAdapter{
		private List list;
		public BasicListAdapter(List list){
			this.list=list;
		}
		public Object get (int pos){
			return list.get(pos);
		}
		public int size(){
			return list.size();
		}
	}
	static class CursorListAdapter implements ListAdapter{
		private Cursor cursor;
		private AstroCatalog catalog;
		public CursorListAdapter(Cursor cursor,AstroCatalog catalog) {			
			this.cursor = cursor;
			this.catalog=catalog;
		}
		public Object get (int pos){
			cursor.moveToPosition(pos);
			return catalog.getObjectFromCursor(cursor);
		}
		public int size(){
			return cursor.getCount();
		}
	}

	private int last_position_found=-1;
	/**
	 * resets the search. use on the list was changed, but no new search is set yet.
	 * i.e. the find() will not work after resetting untill setSearch specifies the new search
	 */
	public void reset(){
		last_position_found=-1;
		list=null;
		
	}
	private ListAdapter list;
	private String searchString;
	private Matcher matcher;
	/**
	 * Sets a new search 
	 * @param list - list to be searched
	 * @param searchString - the string to be searched for
	 */
	public void setSearch(ListAdapter list,String searchString){
		this.list=list;
		this.searchString=searchString;		
		last_position_found=-1;
	}
	
	/**
	 * 
	 * @param matcher - do not forget to set the matcher, 
	 * you will get NullPointerException otherwise
	 */
	public void setMatcher(Matcher matcher){
		this.matcher=matcher;
	}
	
	/**
	 * 
	 * @return pos in a list,-1 if nothing found
	 */
	public int find(){
		if(list==null||searchString==null)return -1;
		
		if(list.size()==0)return -1;
		int i=0;
		if(last_position_found+1>=list.size())
			i=0;
		else
			i=last_position_found+1;
		
		if(i==0)
			last_position_found=list.size();
		boolean incycle=true;
		boolean morelpf=(i>last_position_found);
		int k=0;
		while(incycle){
			Object obj=list.get(i);
			if(matcher.match(obj, searchString)){
				last_position_found=i;
				return i;
			}
			
			i++;
			k++;
			if(i>=list.size()){
				i=0;
				morelpf=false;
			}
			if(k>list.size())
				break;
			if(!morelpf){		
				
				incycle=(i<=last_position_found);//to show the same search again i<=last_position_found
			}
		}
		last_position_found=-1;
		list=null;
		
		return -1;
	}
	
}
