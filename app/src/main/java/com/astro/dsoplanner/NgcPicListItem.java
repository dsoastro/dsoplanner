package com.astro.dsoplanner;

import com.astro.dsoplanner.base.Exportable;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class NgcPicListItem implements Exportable {
	short ngc;
	long pos;
	int size;

	public NgcPicListItem(String name,long pos,long size) {
		super();
		ngc=getShortFromName(name);
		this.pos=pos;
		this.size=(int)size;
				
	}
	
	public String getName(){
		String s = String.valueOf(ngc);
		String s1 = String.valueOf(ngc);
		for(int m = 0; m < 4-s1.length(); m++){
			s = "0" + s;
		}
		s += ".jpg";
		return s;
	}
	private short getShortFromName(String name){
		return (short)AstroTools.getInteger(name.replace(".jpg", ""), 0, 0, 10000);
	}
	
	@Override
	public String toString() {
		return "NgcPicListItem [name=" + ngc + ", pos=" + pos + ", size="
				+ size + "]";
	}

	public NgcPicListItem(DataInputStream stream) throws IOException{
		ngc=stream.readShort();
		pos=stream.readLong();
		size=stream.readInt();
	}
	public int getClassTypeId(){
		return Exportable.NGC_PIC_ITEM;
	}
	public Map<String,String> getStringRepresentation(){
		throw new UnsupportedOperationException();
	}
	public byte[] getByteRepresentation(){
		
		ByteArrayOutputStream buff=new ByteArrayOutputStream();
		DataOutputStream stream=new DataOutputStream(buff);
		try{
			stream.writeShort(ngc);
			stream.writeLong(pos);
			stream.writeInt(size);
			
		}
		catch(IOException e){
			return null;
		}		
		
		return buff.toByteArray();			
	}
	
	public int getValue(){

		return ngc;
	}
	
	
}
