package com.astro.dsoplanner;

import android.content.Context;
import android.os.Handler;

import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.Comet;
import com.astro.dsoplanner.base.MinorPlanet;

public class DownloadCMOItem extends DownloadItem{
	int catalog;
	
	private static String getUrl(int catalog, Context context){
		switch(catalog){
			case AstroCatalog.COMET_CATALOG:
				return SettingsActivity.getCometUpdateUrl(context);//.DOWNLOAD_URL;
			case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
				return SettingsActivity.getMinorPlanetUpdateUrl(context);
		}
		return null;
	}
	private static String getPath(int catalog){
		switch(catalog){
		case AstroCatalog.COMET_CATALOG:
			return Comet.DOWNLOAD_FILE_PATH;
		case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
			return MinorPlanet.DOWNLOAD_FILE_PATH;
		}
		return null;
	}
	
	public DownloadCMOItem(Context context,Handler handler,int catalog){
		super(context,handler,getUrl(catalog, context),"","","",getPath(catalog),0,0, catalog == AstroCatalog.COMET_CATALOG ? "Downloading comets" : "Downloading minor planets");
		this.catalog=catalog;
	}
	public void start(){
		long id=startDownload();
		if(id!=-1){
			switch(catalog){
			case AstroCatalog.COMET_CATALOG:
				DownloadReceiver.downloadMap.put(id, DownloadReceiver.ACTION_UDPATE_COMET_CATALOG);
				break;
			case AstroCatalog.BRIGHT_MINOR_PLANET_CATALOG:
				DownloadReceiver.downloadMap.put(id, DownloadReceiver.ACTION_UDPATE_BRIGHT_MINOR_PLANET_CATALOG);
				break;

			}
		}
	}
}
