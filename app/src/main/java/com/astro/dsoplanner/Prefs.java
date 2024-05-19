package com.astro.dsoplanner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.Comet;
import com.astro.dsoplanner.base.MinorPlanet;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListFiller;
import com.astro.dsoplanner.infolist.InfoListLoader;
import com.astro.dsoplanner.infolist.InfoListLoaderImp;
import com.astro.dsoplanner.infolist.InfoListSaver;
import com.astro.dsoplanner.infolist.InfoListSaverImp;
import com.astro.dsoplanner.infolist.ListHolder;

import android.content.Context;
import android.util.Log;


//info to save
public class Prefs  {
	
	public static final String A_7_B_A_10_TYPE_GX = "a>7*b&a>10&type=gx";
	public static final String EDGE_ON_GALAXIES = "Edge-On galaxies";
	public static final String ALT_0 = "alt>0";
	public static final String SEPARATION_5_MAG2_7 = "separation<5&mag2<7";
	public static final String CLOSE_DOUBLE_STARS_ABOVE_HORIZON = "Close double stars above horizon";
	private static final String ID_0 = "id>0";
	private static final String ERROR_SAVING_PREFERENCES = "Error saving preferences";
	private static final String TAG = Prefs.class.getSimpleName();

	private Context context;

	public Prefs(Context context){
		this.context=context;
	}
	public void loadLists(){
		Set<Integer> listSet=new HashSet<Integer>();
		for(int i = InfoList.PrimaryObsList; i<=InfoList.PrimaryObsList+3; i++)
			listSet.add(i);
		listSet.add(InfoList.NGCIC_SELECTION_LIST);
		listSet.add(InfoList.SREQUEST_LIST);
		listSet.add(InfoList.DB_LIST);
		listSet.add(InfoList.LOCATION_LIST);
		listSet.add(InfoList.PREFERENCE_LIST);
		listSet.add(InfoList.NGC_PIC_LIST);
		for(Integer i:listSet){
			InfoList obs= ListHolder.getListHolder().get(i);

			File f=null;
			InputStream in=null;
			try{
				File dir = context.getFilesDir();//Tools.getExternalFilesDir(context);
				f = new File(dir, Constants.PREF_LIST_NAME_BASE + i);
				in = new FileInputStream(f);
				InfoListLoader loader=new InfoListLoaderImp(in);
				ErrorHandler ehan = obs.load(loader);
				
				if(ehan.hasError()){
					Log.d(TAG,"prefs, error loading, eh="+ehan);
					Log.d(TAG,"list="+i+"size="+obs.getCount());
				}
			}
			catch(Exception e){
				ByteArrayOutputStream ba=new ByteArrayOutputStream();
				PrintStream ps=new PrintStream(ba);
				e.printStackTrace(ps);
				Log.d(TAG,"prefs, error loading list="+i+" "+ba.toString());
			}
			finally{
				try{
					in.close();
				}
				catch(Exception e){}
			}
		}

		InfoList list=ListHolder.getListHolder().get(InfoList.DB_LIST);

		if (list.getCount()==0){

			//adding NGCIC DATABASE
			DbListItem item=new DbListItem(QueryActivity.MENU_NGCIC, AstroCatalog.NGCIC_CATALOG,"NgcIc Catalog","ngcic.db",new DbListItem.FieldTypes());
			InfoListFiller filler=new DatabaseManagerActivity.DbListFiller(Arrays.asList(new DbListItem[]{item}) );
			list.fill(filler);
			list.setListName(""+AstroCatalog.NGCIC_CATALOG);
		}
		
		InfoList list2=ListHolder.getListHolder().get(InfoList.SREQUEST_LIST);
		class Filler implements InfoListFiller {					
			private Collection<SearchRequestItem> collection;
			public Filler(SearchRequestItem item){
				collection=Arrays.asList(new SearchRequestItem[]{item});					
			}				

			public Iterator getIterator(){
				return collection.iterator();
			}
		}
		if(list2.getCount()==0){
			
			list2.fill(new Filler(new SearchRequestItem(context.getString(R.string.all_objects_in_database),ID_0,"")));
			if(!(Global.BASIC_VERSION))list2.fill(new Filler(new SearchRequestItem(context.getString(R.string.close_double_stars),SEPARATION_5_MAG2_7,ALT_0)));
			list2.fill(new Filler(new SearchRequestItem(context.getString(R.string.edge_on_galaxies),A_7_B_A_10_TYPE_GX,"")));

		}
		
		
		
		

		addCometMinorPlanet(list,AstroCatalog.COMET_CATALOG);
		addCometMinorPlanet(list,AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG);



		
    	  for(Object o:list)
    		  Log.d(TAG,""+(DbListItem)(o));
    	  
    	  
    	  InfoList ilist=ListHolder.getListHolder().get(InfoList.NGC_PIC_LIST);
	}
	
	
	private void addCometMinorPlanet(InfoList list,int catalog){
		//checking if there is a comet database
		boolean exists=false;
		int i=0;
		for(Object o:list){
			DbListItem item=(DbListItem)o;
			if(item.cat==catalog){
				//list.remove(i);
				exists=true;
			}
			i++;
		}
		
		
		

		
		
		String name="";
		String dbname="";
		DbListItem.FieldTypes ftypes=null;
		int menu=0;
		switch(catalog){
		case AstroCatalog.COMET_CATALOG:
			name= Comet.DB_DESC_NAME;
			dbname=Comet.DB_NAME;
			ftypes=Comet.FTYPES;
			menu= QueryActivity.MENU_COMET;
			break;
		case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
			name= MinorPlanet.DB_DESC_NAME_BRIGHT;
			dbname=MinorPlanet.DB_NAME_BRIGHT;
			ftypes=MinorPlanet.FTYPES;
			menu= QueryActivity.MENU_BRIGHT_MINOR_PLANET;
			break;
		}
		if(!exists){
			DbListItem item=new DbListItem(menu,catalog,name,dbname,ftypes);
			InfoListFiller filler=new DatabaseManagerActivity.DbListFiller(Arrays.asList(new DbListItem[]{item}) );
			list.fill(filler);

		}
	}

	public ErrorHandler saveLists(Set<Integer> set){
		ErrorHandler eh = new ErrorHandler();
		for(Integer i : set){
			InfoList obs = ListHolder.getListHolder().get(i);
			if(obs==null)continue;
			
			File f = null;
			OutputStream out = null;
			try{
				File dir = context.getFilesDir();//Tools.getExternalFilesDir(context);
				f = new File(dir, Constants.PREF_LIST_NAME_BASE + i);
				out = new FileOutputStream(f);
				Log.d(TAG,"out="+out);
				InfoListSaver saver = new InfoListSaverImp(out);
				boolean result = obs.save(saver);
				Log.d(TAG,i+" list saved");
				if(!result){
					eh.addError(new ErrorHandler.ErrorRec(ErrorHandler.IO_ERROR, ERROR_SAVING_PREFERENCES));
					Log.d(TAG,"eh="+eh);
				}
			}
			catch(Exception e){
				eh.addError(new ErrorHandler.ErrorRec(ErrorHandler.IO_ERROR, ERROR_SAVING_PREFERENCES));
				ByteArrayOutputStream ba=new ByteArrayOutputStream();
				PrintStream ps=new PrintStream(ba);
				e.printStackTrace(ps);
				Log.d(TAG,"error saving list="+i+" "+ba.toString());
			}
			finally{
				try{
					out.close();
					Log.d(TAG,"out closed");
				}
				catch(Exception e){}
			}
		}
		return eh;
	}
	public void saveList(int id){
		Set<Integer> s=new HashSet<Integer>();
		s.add(id);
		saveLists(s);
	}

}
