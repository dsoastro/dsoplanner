package com.astro.dsoplanner.base;



import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.AstroTools.RaDecRec;
import com.astro.dsoplanner.Constants;

public class BoundaryPoint extends Point {
	private static final String TAG = BoundaryPoint.class.getSimpleName();
	double dec1;
	double dec2;
	Point end;
	byte con;
	boolean ignore=false;
	public BoundaryPoint( double ra1, double ra2,
			double dec1, double dec2, byte con) {
		super(ra1, dec1);
		end=new Point(ra2,dec2);
		this.con=con;
	}
	/**
	 * the point is ignored for calculation of label location
	 */
	public void setIgnoreFlag(){
		ignore=true;
	}
	public boolean getIgnoreFlag(){
		return ignore;
	}
	public Point getPoint(){
		return end;
	}
	public int getCon(){
		return con;
	}
	@Override
	public void raiseNewPointFlag(){
		super.raiseNewPointFlag();
		end.raiseNewPointFlag();
	}
	
	@Override
	public void draw(Canvas canvas, Paint paint){
		
		end.setXY();
		end.setDisplayXY();
		canvas.drawLine(getXd(), getYd(), end.getXd(), end.getYd(), paint);//draw line from initial point to the end point
	}
	
	private void drawLabel(Canvas canvas, Paint p){

		String name= Constants.constellationLong[con];
		Log.d(TAG,"name="+name);
		float size=p.getTextSize()*Point.getScalingFactor();
		
		float offset=7*Point.getScalingFactor();
		p.setTextSize(size);
		p.setStyle(Paint.Style.FILL);
		float xl=xd+offset;
		float yl=yd-offset;

		Log.d(TAG,"xl="+xl+" yl="+yl);
		
		
		if(Point.getAltCenter()==0)
			canvas.drawText(name, xl, yl, p);
		else{
			Path path= AstroTools.getLabelPath(xl, yl);
			canvas.drawTextOnPath(name, path, 0, 0, p);
		}


		
		
	}
	private void drawExact(Canvas canvas, Paint paint){
		
		
		RaDecRec rec=AstroTools.precession(ra,dec,1875,1,1);
		double ra75=AstroTools.getNormalisedRa(rec.ra);//precessed to 1875, the equinox of the boundaries
		double dec75=rec.dec;
		
		rec=AstroTools.precession(end.ra,end.dec,1875,1,1);
		double era75=AstroTools.getNormalisedRa(rec.ra);//precessed to 1875, the equinox of the boundaries
		double edec75=rec.dec;
		
		List<Point>list=new ArrayList<Point>();
		if(Math.abs(dec75-edec75)<0.05){
			double step=(era75-ra75)/10;
			for(int i=0;i<=10;i++){
				list.add(new Point(ra75+step*i,dec75));
			}			
		}
		else if (Math.abs(ra75-era75)<0.05){
			double step=(edec75-dec75)/10;
			for(int i=0;i<=10;i++){
				list.add(new Point(ra75,dec75+step*i));
			}	
		}
		else{

		}
		Point prev=null;
		for(Point p:list){

			double jdinit=AstroTools.JD(1875, 1, 1, 12, 0);
			double jdtarget=AstroTools.JD(2000, 1, 1, 12, 0);
			rec=AstroTools.getRaDecExact(p.ra,p.dec,jdinit,jdtarget);
			p.ra=(float)rec.ra;
			p.dec=(float)rec.dec;
			
			Log.d(TAG,"ra="+p.ra+" dec="+p.dec);
			
			p.setXY();
			p.setDisplayXY();
			if(prev!=null){
				canvas.drawLine(prev.getXd(), prev.getYd(), p.getXd(), p.getYd(), paint);//draw line from initial point to the end point

			}
			prev=p;
		}


	}
}
