package com.astro.dsoplanner;

import com.astro.dsoplanner.base.Exportable;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SearchRequestItem implements Exportable {
	
	private static final String LOCAL_STRING = ", localString=";
	private static final String SQL_STRING = ", sqlString=";
	private static final String SEARCH_REQUEST_ITEM_NAME = "SearchRequestItem [name=";
	
	
	@Override
	public String toString() {
		return SEARCH_REQUEST_ITEM_NAME + name + SQL_STRING + sqlString
				+ LOCAL_STRING + localString + "]";
	}
	public String name;
	public String sqlString;
	public String localString;
	public SearchRequestItem(String name, String sqlString, String localString) {

		this.name = name;
		this.sqlString = sqlString;
		this.localString = localString;
	}
	public SearchRequestItem(DataInputStream stream) throws IOException{
		name=stream.readUTF();
		sqlString=stream.readUTF();
		localString=stream.readUTF();	
	}
	public int getClassTypeId(){
		return Exportable.SEARCH_REQUEST_ITEM;
	}
	public Map<String,String> getStringRepresentation(){
		HashMap<String,String> map=new HashMap<String,String>();
		map.put(Constants.SQL_SEARCH_STRING, sqlString);
		map.put(Constants.LOCAL_SEARCH_STRING, localString);
		map.put(Constants.NAME, name);
		return map;
	}
	public byte[] getByteRepresentation(){

		ByteArrayOutputStream buff=new ByteArrayOutputStream(name.length()+sqlString.length()+localString.length());
		DataOutputStream stream=new DataOutputStream(buff);
		try{
			stream.writeUTF(name);

			stream.writeUTF(sqlString);
			stream.writeUTF(localString);

		}
		catch(IOException e){
			return null;
		}		

		return buff.toByteArray();			
	}
	@Override
	public boolean equals(Object o){
		if(o instanceof SearchRequestItem){
			SearchRequestItem item=(SearchRequestItem)o;
            return item.name.equals(this.name)
                && item.sqlString.equals(this.sqlString)
                && item.localString.equals(this.localString);
		}
		return false;
	}
	@Override
	public int hashCode(){
		return 37*37*37*name.hashCode()+37*37*sqlString.hashCode()+37*localString.hashCode();
	}

}
