package com.astro.dsoplanner.base;

import static com.astro.dsoplanner.Constants.TYC1;
import static com.astro.dsoplanner.Constants.TYC2;
import static com.astro.dsoplanner.Constants.TYC3;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.startools.BitTools;

public class TychoStar extends AstroObject {

	public TychoStar(double ra,double dec,double mag,int con,int index){
		super(ra,dec,mag,con,AstroObject.Star, AstroCatalog.TYCHO_CATALOG,index);
	}
	
	public TychoStar(double ra,double dec,double mag,int con,int tyc1,int tyc2,int tyc3){
		super(ra,dec,mag,con,AstroObject.Star,AstroCatalog.TYCHO_CATALOG,BitTools.convertTycToInt(tyc1, tyc2, tyc3));
		
	}
	
	public TychoStar(double ra,double dec,double mag,int con,int catalog,int id){
		super(ra,dec,mag,con,AstroObject.Star,catalog,id);
	}
	
	@Override
	public String getShortName(){
		BitTools.TycData tycdata=BitTools.convertIntToTyc(id);
		int tyc1=tycdata.tyc1;
		int tyc2=tycdata.tyc2;
		int tyc3=tycdata.tyc3;


		String name="TYC "+tyc1+"-"+tyc2+"-"+tyc3;
		return name;
		
	}
	@Override
	public String getLongName(){
		return getShortName();
	}
	
	public TychoStar(DataInputStream stream) throws IOException{
		super(stream);
		
		
	}
	@Override
	public HashMap<String,String> getStringRepresentation(){
		HashMap<String,String> map=super.getStringRepresentation();
		BitTools.TycData tycdata=BitTools.convertIntToTyc(id);
		int tyc1=tycdata.tyc1;
		int tyc2=tycdata.tyc2;
		int tyc3=tycdata.tyc3;
		map.put(TYC1,""+tyc1);
		map.put(TYC2,""+tyc2);
		map.put(TYC3,""+tyc3);
		
		return map;
		
	}
	@Override
	public byte[] getByteRepresentation(){
		return super.getByteRepresentation();
	}
	
	
	@Override
	public String getCanonicName(){
		return getShortName();
	}
	
	public String getCrossdbSearchName(){
		return getShortName().replace(" ", "");
	}
	
	@Override
	public void draw(Canvas canvas, Paint paint){
		HrStar.draw(canvas,paint,xd,yd,mag);
	}
	
	public void drawStar(Canvas canvas, Paint p,boolean double_star){
		if (HrStar.drawStar(canvas,p,double_star,getXd(),getYd())){
			if(SettingsActivity.isObsObjectLabelOn()){
				drawLabelAsObject(canvas, p);			
			}
		}
	}
	
	private void drawLabelAsObject(Canvas canvas, Paint paint){		
		HrStar.drawLabelAsObject(canvas, paint, getXd(), getYd(), getShortName(), this);
	}
	
	public boolean hasDimension(){
		return false;
	}
	public double getA(){
		return 0;
	}
	public double getB(){
		return 0;
	}
	public String getDsoSelName(){
		return "";
	}
	
	public int getClassTypeId(){
		return Exportable.TYCHO_STAR_OBJECT;
	}
	@Override
	public boolean hasVisibility(){
		return true;
	}

}
