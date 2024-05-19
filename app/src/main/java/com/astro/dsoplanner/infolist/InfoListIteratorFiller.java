package com.astro.dsoplanner.infolist;

import com.astro.dsoplanner.infolist.InfoListFiller;

import java.util.Iterator;

public class InfoListIteratorFiller implements InfoListFiller {
	private Iterator iterator;
	public InfoListIteratorFiller(Iterator iterator){
		this.iterator=iterator;		
	}	
	
	public void update(){
		
	}
	public Iterator getIterator(){
		return iterator;
	}
}
