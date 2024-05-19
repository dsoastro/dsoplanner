package com.astro.dsoplanner;

import java.util.HashMap;
import java.util.Map;

public class NightModeChangeTracker {

	private static Map<Integer,Boolean>map=new HashMap<Integer, Boolean>();
	
	private static int id=1;
	
	public static int register(){
		map.put(id,false);
		return id++;
	}
	public static void unregister(int activity){
		map.remove(activity);
	}
	/**
	 * 
	 * @param activity
	 * @return night mode change flag for activity
	 */
	public static boolean get(int activity){
		Boolean b=map.get(activity);
		if(b!=null)
			return b;
		
		return false;
	}
	/**
	 * activity clears its flag
	 * @param activity
	 */
	public static void clear(int activity){
		map.put(activity, false);
	}
	
	/**
	 * night mode changed, set change flag for all
	 */
	public static void set(){
		for(Integer i:map.keySet()){
			map.put(i, true);
		}
		
	}

}
