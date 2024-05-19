package com.astro.dsoplanner.base;





import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.Exportable;
import com.astro.dsoplanner.base.TychoStar;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;



public class Ucac4Star extends TychoStar {
	
	public static final String UZONEID = "uzoneid";
	public static final String UZONE = "uzone";
	
	/**
	 * second byte from zone
	 */
	byte b;
	/**
	 * 
	 * @param ra
	 * @param dec
	 * @param mag
	 * @param con
	 * @param zone - last two bytes are non zero
	 * @param zonenum - higher byte is not used
	 */
	public Ucac4Star(double ra,double dec,double mag,int con,int zone,int zonenum){
		
		super(ra,dec,mag,con, AstroCatalog.UCAC4_CATALOG,0);
		int b1=zone&255;
		id=b1<<24|zonenum;//putting the lower byte from zone into the higher byte of zonenum
		b=(byte)(zone>>8&255);
	}
	
	
	public Ucac4Star(DataInputStream stream) throws IOException{
		super(stream);		
		b=stream.readByte();		
	}
	@Override
	public String getShortName(){
		int b1=id>>24&255;//the lower byte from zone
		int b2=b<0?b+256:b;
		int zone=b2<<8|b1;
		int mask=255<<16|255<<8|255;//1 in every bit in three bytes
		int zonenum=mask&id;
		return "UCAC4 "+zone+"-"+zonenum;
	}
	@Override
	public byte[] getByteRepresentation(){
		byte[] buf=super.getByteRepresentation();
		ByteArrayOutputStream buff=new ByteArrayOutputStream();
		DataOutputStream stream=new DataOutputStream(buff);
		try{
			stream.writeByte(b);	

		}
		catch(IOException e){
			return null;
		}
		byte[] newBuff=buff.toByteArray();
		byte[] combBuff=new byte[buf.length+newBuff.length];
        System.arraycopy(buf, 0, combBuff, 0, buf.length);
        System.arraycopy(newBuff, buf.length - buf.length, combBuff, buf.length, combBuff.length - buf.length);

		return combBuff;
	}
	@Override
	public HashMap<String,String> getStringRepresentation(){
		HashMap<String,String> map=super.getStringRepresentation();
		
		int b1=id>>24&255;//the lower byte from zone
		int b2=b<0?b+256:b;
		int zone=b2<<8|b1;
		int mask=255<<16|255<<8|255;//1 in every bit in three bytes
		int zonenum=mask&id;
		
		map.put(UZONE,""+zone);
		map.put(UZONEID,""+zonenum);		
		return map;
		
	}
	@Override
	public int getClassTypeId(){
		return Exportable.UCAC4_STAR_OBJECT;
	}

}
