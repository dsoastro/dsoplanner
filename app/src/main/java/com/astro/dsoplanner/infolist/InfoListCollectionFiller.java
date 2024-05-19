package com.astro.dsoplanner.infolist;

import java.util.Collection;
import java.util.Iterator;

public class InfoListCollectionFiller implements InfoListFiller {

	
	private Collection collection;
	public InfoListCollectionFiller(Collection collection){
		this.collection=collection;		
	}	
	
	public void update(){
		
	}
	public Iterator getIterator(){
		return collection.iterator();
	}
}
