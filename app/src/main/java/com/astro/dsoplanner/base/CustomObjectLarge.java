package com.astro.dsoplanner.base;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.astro.dsoplanner.Global;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * this is the class that allows adding user fields of several types
 *
 */
public class CustomObjectLarge extends CustomObject {
	private static final String TAG = CustomObjectLarge.class.getSimpleName();
	public Fields fields;
	public CustomObjectLarge(int catalog,int id,double ra,double dec,int con,int type,String typeStr,double a,
			double b,double mag,double pa,String name1,String name2,String comment,Fields fields){
		super(catalog,id,ra,dec,con,type,typeStr,a,b,mag,pa,name1,name2,comment);
		this.fields=fields;
	}
	public CustomObjectLarge(DataInputStream stream) throws IOException{
		super(stream);
		fields=new Fields(stream);
	}
	@Override
	public byte[] getByteRepresentation(){
		byte[] buf=super.getByteRepresentation();
		byte[] newBuff=fields.getByteRepresentation();
		if(newBuff==null)
			return buf;
		if(buf==null)
			return null;		
		
		byte[] combBuff=new byte[buf.length+newBuff.length];
        System.arraycopy(buf, 0, combBuff, 0, buf.length);
        System.arraycopy(newBuff, buf.length - buf.length, combBuff, buf.length, combBuff.length - buf.length);
		
		return combBuff;			
	}
	@Override
	public HashMap<String,String> getStringRepresentation(){
		HashMap<String,String> map=super.getStringRepresentation();
		map.putAll(fields.getStringRepresentation());
		return map;
	}
	@Override
	public int getClassTypeId(){
		return Exportable.CUSTOM_OBJECT_LARGE;
	}
	@Override
	public String toString() {
		return super.toString()+fields.toString();
	}
	
	public Fields getFields(){
		return fields;
	}
	/**
	 * 
	 * @return checks if there are photos available on the disk
	 */
	public boolean arePhotosAvailable(Context context){
		Log.d(TAG,"arePhotosAvailable");
		if(!fields.isEmpty()){
			Map<String,String> map=fields.getPhotoMap();
			for(Map.Entry<String, String> e:map.entrySet()){

				Uri uri = Uri.parse(e.getValue());
				Log.d(TAG, "uri=" + uri.toString());
				InputStream in = null;
				try{
					in = context.getContentResolver().openInputStream(uri);
				}
				catch (Exception ex){
					Log.d(TAG,"ex=" + ex);
					continue;
				}
				finally {
					try {
						in.close();
					}
					catch (Exception ignore){}
				}
				Log.d(TAG,"photo available");
				return true;
			}
			
		}
		return false;
	}
	/**
	 * 
	 * @return a list of absolute paths to photos
	 */
	public List<String> getAvailablePhotos(Context context){
		List<String> list=new ArrayList<String>();
		if(!fields.isEmpty()){
			Map<String,String> map=fields.getPhotoMap();
			for(Map.Entry<String, String> e:map.entrySet()){
				Uri uri = Uri.parse(e.getValue());
				Log.d(TAG, "uri=" + uri.toString());
				InputStream in = null;
				try{
					in = context.getContentResolver().openInputStream(uri);
				}
				catch (Exception ex){
					Log.d(TAG,"ex=" + ex);
					continue;
				}
				finally {
					try {
						in.close();
					}
					catch (Exception ignore){}
				}
				Log.d(TAG,"photo available");
				list.add(e.getValue());
			}
			
		}
		return list;
	}
}
