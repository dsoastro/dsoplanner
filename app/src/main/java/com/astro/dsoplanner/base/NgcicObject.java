package com.astro.dsoplanner.base;


import static com.astro.dsoplanner.Constants.NAME1;
import static com.astro.dsoplanner.Constants.NAME2;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.astro.dsoplanner.AstroTools;


import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.ListHolder;

public class NgcicObject extends ExtendedObject {
	public static final int IC_THRESHOLD = 10000;
	
	private static final String PA2 = " pa=";
	private static final String MAG2 = " mag";
	private static final String DEC2 = " dec=";
	private static final String RA2 = "ra= ";
	private static final String DSO_NGC = "DSO, ngc=";
	private static final String H = " H";
	private static final String C = "C";
	private static final String M = "M";
	private static final String IC = "IC";
	private static final String NGC2 = "NGC";
	

	private static final String TAG = NgcicObject.class.getSimpleName();

	public int ngc;
	public int messier;
	public int caldwell;
	public int hershell;
	public String comment;
	public String clas;
	public NgcicObject(double ra,double dec,int con,int type,int id,double a,
			double b,double mag,int ngc,int messier,int caldwell,int hershell,double pa,String comment,String clas){
		super( ra, dec, con, type, AstroCatalog.NGCIC_CATALOG, id, a, b, mag,pa);
		this.ngc=ngc;
		this.messier=messier;
		this.caldwell=caldwell;
		this.hershell=hershell;
		this.comment=comment;
		this.clas=clas;
	}
	public NgcicObject(DataInputStream stream) throws IOException{
		super(stream);
		ngc=stream.readShort();
		messier=stream.readShort();
		caldwell=stream.readShort();
		hershell=stream.readShort();
	}
	public int getClassTypeId(){
		return Exportable.NGCIC_OBJECT;
	}
	
	public int getCatalogNumber(){
		return AstroCatalog.NGCIC_CATALOG;
	}
	public int getId(){
		return id;
	}
	
	@Override
	public byte[] getByteRepresentation(){
		byte[] buf=super.getByteRepresentation();
		ByteArrayOutputStream buff=new ByteArrayOutputStream(2*4);
		DataOutputStream stream=new DataOutputStream(buff);
		try{
			stream.writeShort(ngc);
			stream.writeShort(messier);
			stream.writeShort(caldwell);
			stream.writeShort(hershell);
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
	public boolean isIc(){
		return(ngc>IC_THRESHOLD);
	}
	public String getNgcIcName(){
		if(ngc<IC_THRESHOLD)return NGC2+ngc;
		Integer i = Integer.valueOf(ngc - IC_THRESHOLD);
		return IC+i;
	}
	public static String getNgcIcName(int ngc){
		if(ngc<IC_THRESHOLD)return NGC2+ngc;
		Integer i = Integer.valueOf(ngc - IC_THRESHOLD);
		return IC+i;
	}
	public String getMessierName(){
		if(messier!=0){
			return M+messier;			
		}
		return"";
	}
	public String getCaldwellName(){
		if(caldwell!=0){
			return C+caldwell;			
		}
		return"";
	}
	public String getDsoSelName(){
		return getShortName();
	}
	public String getLongName(){
		String s=null;
		if(ngc<IC_THRESHOLD)
			s=NGC2+ngc;
		else
			s=IC+(ngc-IC_THRESHOLD);
		if(messier!=0)
			s=s+" "+M+messier;
		if(caldwell!=0)
			s=s+" "+C+caldwell;
		if(hershell!=0)
			s=s+H;
		
		return s;
	}
	public String getShortName(){
		String s=null;
		if(messier!=0){
			s=M+messier;
			return s;
		}
		if(caldwell!=0){
			s=C+caldwell;
			return s;
		}
			
		if(ngc<IC_THRESHOLD)
			s=NGC2+ngc;
		else
			s=IC+(ngc-IC_THRESHOLD);
		
		
		if(hershell!=0)
			s=s+H;
		
		return s;
	}
	@Override
	public HashMap<String,String> getStringRepresentation(){
		HashMap<String,String> map=super.getStringRepresentation();
		map.put(NAME1, getShortName());
		map.put(NAME2, getNgcIcName());	
		
		return map;
		
	}
	@Override
	public String toString(){
		return(DSO_NGC+ngc+ RA2+ra+DEC2+dec+MAG2+mag+PA2+pa+"id="+id+" ref="+ref);
	}

	@Override
	public boolean hasVisibility(){
		return true;
	}  
	
	@Override
	protected void drawPath(Canvas canvas,Paint p){
		int cat=getCatalog();
		if(!(cat==AstroCatalog.MESSIER||cat==AstroCatalog.CALDWELL||cat==AstroCatalog.NGCIC_CATALOG)){
			super.drawPath(canvas, p);
			return;
		}
		Integer value= AstroTools.getContourNumber(ref);
		if(value==null){
			super.drawPath(canvas, p);
		}
		else{
			InfoList list= ListHolder.getListHolder().get(InfoList.NEBULA_CONTOUR_LIST);
			ContourObject obj=(ContourObject)list.get(value);
			obj.setXY();
			obj.setDisplayXY();
			obj.draw(canvas, p);
			obj.drawCross(canvas, p, getXd(), getYd());
		}
	}
	
	@Override
	public String getComment(){
		Log.d(TAG,"comment="+comment+" class="+clas);
		if(comment==null && clas==null)return null;
		String res="";
		if(clas!=null){
			res=clas;
		}
		if(comment!=null){
			if(clas==null)
				res=comment;
			else
				res=clas+"\n"+comment;
		}
		return res;
	}
	@Override
	public String[] getNoteNames(){
		return new String[]{getNgcIcName(),getCaldwellName(),getMessierName(),getShortName(),getLongName()};
	}
	@Override
	public String getCanonicName(){
		return getNgcIcName();
	}
}
