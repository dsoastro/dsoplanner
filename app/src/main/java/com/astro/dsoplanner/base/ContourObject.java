package com.astro.dsoplanner.base;

import static java.lang.Math.max;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PathEffect;
import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.LabelLocations;


import com.astro.dsoplanner.SettingsActivity;


public class ContourObject extends CustomObject {
	
	private static final String LIST_SIZE = " list size=";
	private static final String CONTOUR_OBJECT_TOTAL_LISTS = "ContourObject [total lists=";
	

	private static final String TAG = ContourObject.class.getSimpleName();
	public static class RaDec{
		
		private static final String DEC3 = ", dec=";
		private static final String RA_DEC_RA = "RaDec [ra=";
		
		
		public double ra;
		public double dec;
		public RaDec(double ra, double dec) {
			super();
			this.ra = ra;
			this.dec = dec;
		}
		@Override
		public String toString() {
			return RA_DEC_RA + ra + DEC3 + dec + "]";
		}
		
	}
	private List<List<RaDec>> list=new ArrayList<List<RaDec>>();
	
	
	
	
	public ContourObject(int catalog,int id,double ra,double dec,int con,int type,String typeStr,double a,
			double b,double mag,double pa,String name1,String name2,String comment,List<List<RaDec>> list){
		super(catalog,id,ra,dec,con,type,typeStr,a,
			b,mag,pa,name1,name2,comment);
		this.list=list;
	}
	public ContourObject(DataInputStream stream) throws IOException{
		super(stream);
		byte listnum=stream.readByte();
		for(int i=0;i<listnum;i++){
			List<RaDec> ilist=new ArrayList<RaDec>();
			list.add(ilist);
			int ilistsize=stream.readInt();
			for(int j=0;j<ilistsize;j++){
				double ra=stream.readDouble();
				double dec=stream.readDouble();
				ilist.add(new RaDec(ra,dec));
			}
		}
	}
	@Override
	public int getClassTypeId(){
		return Exportable.CONTOUR_OBJECT;
	}
	@Override
	public String toString() {
		String s=super.toString()+CONTOUR_OBJECT_TOTAL_LISTS+list.size()+",\n";
		int i=0;
		return s;
		
	}
	public String printLists(){
		int i=0;
		String s="";
		for(List<RaDec> ilist:list){			
			for(RaDec rec:ilist){
				s=s+"list "+i+" "+rec;
			}
			s=s+"\n";
			i++;
		}
		return s;
	}
	/**
	 * 
	 * @return number of RaDec records in list
	 */
	public int getDimension(){
		int sum=0;
		for(List<RaDec> rec:list){
			sum+=rec.size();
		}
		return sum;
	}
	@Override
	public HashMap<String,String> getStringRepresentation(){
		throw new UnsupportedOperationException();
	}
	@Override
	public byte[] getByteRepresentation(){
		byte[] buf=super.getByteRepresentation();
		ByteArrayOutputStream buff=new ByteArrayOutputStream();
		try{
			buff.write(buf);
		}
		catch(IOException e){
			return null;
		}
		DataOutputStream stream=new DataOutputStream(buff);
		try{
			stream.writeByte(list.size());//assume there are less than 256 contour lists for one object)))
			for(List<RaDec> ilist:list){
				stream.writeInt(ilist.size());
				for(RaDec rec:ilist){
					stream.writeDouble(rec.ra);
					stream.writeDouble(rec.dec);
				}
			}
		}
		catch(IOException e){
			return null;
		}
		return buff.toByteArray();		
	}
	/**
	 * used for bundle purposes to avoid saving lists when passing object between activities
	 * @return
	 */
	public byte[] getShortByteRepresentation(){
		byte[] buf=super.getByteRepresentation();
		ByteArrayOutputStream buff=new ByteArrayOutputStream();
		try{
			buff.write(buf);
		}
		catch(IOException e){
			return null;
		}
		DataOutputStream stream=new DataOutputStream(buff);
		try{
			stream.writeByte(0);//zero number of lists
			
		}
		catch(IOException e){
			return null;
		}
		return buff.toByteArray();		
	}
	@Override
	public void draw(Canvas canvas,Paint paint){
		Log.d(TAG,"draw");
		if (!Point.withinBounds(getXd(), getYd(),(float)(5*max(a,b)/60*Point.getWidth()/Point.getFOV()/2)))
			return;
		Log.d(TAG,"draw after bounds");
		for(List<RaDec> ilist:list){
				drawContour(canvas,paint,ilist);
		}
	}
	public void drawCross(Canvas canvas,Paint p2,float x,float y){
		Path cross=getCleanPath();//new Path();
		float st = 14* SettingsActivity.dso_Scale()*Point.getScalingFactor();
		cross.moveTo(x-st/2, y);
		cross.lineTo(x+st/2, y);
		cross.moveTo(x, y-st/2);
		cross.lineTo(x, y+st/2);
		canvas.drawPath(cross,p2);
	}
	
	@Override
	protected void drawLabel(Canvas canvas, Paint paint){

		float orig_size=paint.getTextSize();
		Style orig_style=paint.getStyle();
		
		String name=getShortName();
		float size=paint.getTextSize()*Point.getScalingFactor();
		float offset=7*Point.getScalingFactor();
		
		
		paint.setTextSize(size);
		paint.setStyle(Paint.Style.FILL);
		float xl=xd+offset;
		float yl=yd-offset;
		LabelLocations ll=LabelLocations.getLabelLocations();
		if(ll.get(this,name.length())){
			if(Point.getAltCenter()==0)
				canvas.drawText(name, xl, yl, paint);
			else{
				Path path= AstroTools.getLabelPath(xl, yl);
				canvas.drawTextOnPath(name, path, 0, 0, paint);
			}
			
		}
		
		paint.setTextSize(orig_size);
		paint.setStyle(orig_style);
		
	}
	private void drawPoints(Canvas canvas,Paint paint,List<RaDec> list){
		if(list.size()==0)
			return;
		Path neb=new Path();
        for (RaDec radec : list) {
            Point next = new Point(radec.ra, radec.dec);
            next.setXY();
            next.setDisplayXY();
            int st = 2;
            neb.addCircle(next.xd, next.yd, st, Direction.CW);
            canvas.drawPath(neb, paint);
        }
	}
	
	/**
	 * for neb contour average calc
	 * @author leonid
	 *
	 */
	class data{
		double x;
		double y;
		public data(double x, double y) {
			super();
			this.x = x;
			this.y = y;
		}
		
	}
	
	private void drawContour(Canvas canvas,Paint paint,List<RaDec> list){
		if(list.size()==0)
			return;
		
		float orig_width=paint.getStrokeWidth();
		PathEffect orig_effect=paint.getPathEffect();
		Path neb=getCleanPath();//new Path();
		paint.setStrokeWidth(2);
		PathEffect effect=getStaticPathEffect121();//new DashPathEffect(new float[]{1,2},1);
		paint.setPathEffect(effect);
		
		RaDec rec=list.get(0);
		Point beg=new Point(rec.ra,rec.dec);
		beg.setXY();
		beg.setDisplayXY();
		neb.moveTo(beg.getXd(), beg.getYd());
		
		int k=1;
		switch((int)(Point.getFOV())){
		case 90:k=10;break;
		case 60:k=8;break;
		case 45:k=6;break;
		case 30:k=4;break;
		case 20:k=4;break;
		case 10:k=2;break;
		case 5: k=1;break;
		default:k=1;
		}
		
		
		
		for(int i=1;i<list.size();i=i+k){
			RaDec radec=list.get(i);
			Point next=getCleanPoint(radec.ra,radec.dec);//new Point(radec.ra,radec.dec);
			next.setXY();
			next.setDisplayXY();
			neb.lineTo(next.getXd(),next.getYd());
		}
		neb.lineTo(beg.getXd(), beg.getYd());
		canvas.drawPath(neb,paint);
		
		paint.setStrokeWidth(orig_width);
		paint.setPathEffect(orig_effect);
	}
	@Override
	public boolean hasVisibility(){
		return false;
	}
}
