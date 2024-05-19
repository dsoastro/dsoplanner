package com.astro.dsoplanner.base;

import java.util.List;

import android.database.Cursor;

import com.astro.dsoplanner.Analisator;
import com.astro.dsoplanner.ErrorHandler;

public interface AstroCatalog{
	final int NGCIC_CATALOG=1;
	final int PLANET_CATALOG=2;
	
	final int YALE_CATALOG=3;
	final int TYCHO_CATALOG=4;
	final int UCAC2_CATALOG=5;
	final int UCAC4_CATALOG=6;
	/**
	 * layer
	 */
	final int PGC_CATALOG=7;//layer
	final int MARK_CATALOG=8;//for center mark
	final int COMET_CATALOG=9;
	final int BRIGHT_MINOR_PLANET_CATALOG=10;
	final int CONTOUR_CATALOG=11;
	final int SAC_LAYER_CATALOG=12;//layer
	final int CUSTOM_CATALOG=20;//no database attached
	
	final int DNBARNARD=30;
	final int WDS=31;
	final int UGC=32;
	final int BNLYNDS=33;
	final int DNLYNDS=34;
	
	final int SAC=60;
	final int ABELL=61;
	final int HCG=62;
	final int PK=63;
	/**
	 * sqlite3 db
	 */
	final int PGC=64;
	final int SH2=65;
	
	final int MESSIER=66;
	
	final int CALDWELL=67;
	
	/**
	 * not a catalog, just a checkbox
	 */
	final int HERSHEL=68;
	/**
	 * Steve Gottlieb's NGC Notes
	 */
	final int SNOTES=69;
	final int MISC=70;
	final int HAAS=71;
	final int NEW_CATALOG_FIRST=200;//there is a database. All user databases have a number higher or equal to this number
	
	
	public List<AstroObject> search(); //without constellation, taking all parameters for the search eg min alt etc
	public List<AstroObject> search(String s); //taking only the string for the search
	
	/**
	 * 
	 * searching for objects based both on request to database and on local request(altitude,visibility etc)
	 * @param an - local request, should be compiled already!!!
	 * @param start - sidereal start time
	 * @param end - sidereal end time
	 */
	public List<AstroObject> search(String s, Analisator an, double start, double end);
	
	/**
	 * 
	 * looking for the name coincidence (the upper case and lower case are considered
	 * the same). The search depends upon catalog to search in
	 * 
	 */
	public List<AstroObject> searchName(String s);
	
	/**
	 * to be used for More in Details
	 * @param s
	 * @return
	 */
	public List<AstroObject> searchNameExact(String s);
	
	/**
	 * 
	 * @param s
	 * @return the list of objects with sought for string included into comments
	 */
	public List<AstroObject> searchComment(String s);
	public AstroObject getObject(int id);
	public long add(AstroObject obj, ErrorHandler eh);//-1 stands for bad addition
	public void open(ErrorHandler eh);
	public void close();
	public void remove(int id) throws UnsupportedOperationException;
	public void removeAll() throws UnsupportedOperationException;
	public Cursor getAll();
	public AstroObject getObjectFromCursor(Cursor cursor);
	
	public void beginTransaction();
	public void setTransactionSuccessful();
	public void endTransaction();
	
	/**
	 * 
	 * @param nearbyObject
	 * @return nearby objects with visibility higher than vis within fov/2 distance
	 */
	public List<AstroObject> searchNearby(Point nearbyObject, double fov, double vis);
}

