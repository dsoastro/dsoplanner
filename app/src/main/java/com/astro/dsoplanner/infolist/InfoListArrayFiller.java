package com.astro.dsoplanner.infolist;

import java.util.Arrays;
import java.util.Iterator;

public class InfoListArrayFiller implements InfoListFiller {

	
	private Object[] a;
	public InfoListArrayFiller(){
			
	}	
	public void setObject(Object[] a){
		this.a=a;
	}
	public void update(){
		
	}
	public Iterator getIterator(){
		return Arrays.asList(a).iterator();
	}
}
