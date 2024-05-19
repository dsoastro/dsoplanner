package com.astro.dsoplanner.base;


import static java.lang.Math.atan2;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.util.Log;



import com.astro.dsoplanner.SettingsActivity;

//object cursor
public class ObjCursor extends Point {
	private static final int MIN_DIST_FOR_SELECTION = 1500;
	
	private static final String AUTO_ROTATION = "autoRotation";
	private static final String D_CROSS_SIZE = "d_cross_size";
	private static final String D_CROSS_LINE = "d_cross_line";
	public static final String CROSS_GUIDE = "cross_guide";
	private static final String CROSS_ORIENT = "cross_orient";
	private static final String CROSS_SHOW = "cross_show";
	

	private static final String TAG = ObjCursor.class.getSimpleName();
	public static boolean bCross=true;
	public static boolean bOrient=false;
	public static boolean bGuide=false;

	private static int lWidth=1;
	private static int lSize=30;
	private static SharedPreferences m_pm;
	private Point objSelected=null;
	private static boolean rotationOn=false;
	 
	public ObjCursor(float x,float y ){
		super(0,0);
		this.xd=x;
		this.yd=y;
	}
	public void setRaDec(double ra,double dec){
		this.ra=(float)ra;
		this.dec=(float)dec;
	}
	public void draw(Canvas canvas,Paint p){
		if (bCross) {
			if(rotationOn){
				double alt=getAlt();
				double az=getAz();
				
				Point testup=new CustomPoint(az,alt+0.1,"");
				testup.setXY();
				testup.setDisplayXY();
				double angleNorth=atan2(testup.getYd()-yd,testup.getXd()-xd);
				float angle=(float)((angleNorth*180/Math.PI))+90;
				Matrix matrix=new Matrix();
				matrix.setRotate(angle, getXd(), getYd());
				canvas.save();
				canvas.rotate(angle,xd, yd);
				
			}		
			
			Paint paint = new Paint(p);
			paint.setStrokeWidth(lWidth);
			float csize=lSize*Point.getScalingFactor();
			canvas.drawLine(xd, yd-csize, xd, yd+csize, paint);
			canvas.drawLine(xd-csize, yd, xd+csize, yd, paint);

			if(bOrient){
				paint.setStrokeWidth(lWidth*3);
				float dd = 10* SettingsActivity.dso_Scale()*Point.getScalingFactor(); //center offset
				if(Point.orientationAngle==180)	canvas.drawLine(xd, yd+dd, xd, yd+csize, paint);
				else							canvas.drawLine(xd, yd-dd, xd, yd-csize, paint);
				if(Point.mirror==-1)			canvas.drawLine(xd-dd, yd, xd-csize, yd, paint);
				else							canvas.drawLine(xd+dd, yd, xd+csize, yd, paint);
			}
			
			paint.setPathEffect(null);
			if(rotationOn){
				canvas.restore();
			}
			
			if(bGuide){
				PathEffect effect=new DashPathEffect(new float[]{10,5},1);
				paint.setPathEffect(effect);
				paint.setStrokeWidth(lWidth);
				Point center=new CustomPoint((float)Point.getAzCenter(),(float)Point.getAltCenter(),"");
				center.setXY();	center.setDisplayXY();
				canvas.drawLine(xd, yd, center.getXd(), center.getYd(), paint);
			}

		}
	}
	public void setObjSelected(Point p){//reference to selected objects is stored inside
		objSelected=p;
	}
	public Point getObjSelected(){
		return objSelected;
	}
	public static List<Point> cllist=new ArrayList<Point>();//set of close points from the last touching
	public static int cllistpos=0;
	public Point closestPoint(List<Point> l){
		Log.d(TAG,"entering closestPoint");
		float d = MIN_DIST_FOR_SELECTION * Point.getScalingFactor() * Point.getScalingFactor();
		Point pclose = null;
		List<Point> currentlist=new ArrayList<Point>();//list of close points to the closest one
		for (Point p:l){
			float distSq = (p.getXd()-xd)*(p.getXd()-xd)+(p.getYd()-yd)*(p.getYd()-yd);
			if(p instanceof HrStar ||p instanceof TychoStar)
				distSq=distSq*2;
			if(distSq < d){
				pclose = p;
				d = distSq;
			}
		} 
		if(pclose==null)
			return null;
		//looking for points closest to pclose

		for(Point p:l){
			float distSq = (p.getXd()-pclose.getXd())*(p.getXd()-pclose.getXd())+(p.getYd()-pclose.getYd())*(p.getYd()-pclose.getYd());
			if(distSq<20){
				currentlist.add(p);
			}
		}
		Log.d(TAG,"pclose="+pclose);
		for(Point p:currentlist){
			Log.d(TAG,"current list point="+p);
		}
		for(Point p:cllist){
			Log.d(TAG,"cllist point="+p);
		}
		//checking if pclose was in a list of closest points last time
		//and double tap to center is turned off
		if(cllist.contains(pclose)&&!SettingsActivity.getCenterObjectStatus()){
			Log.d(TAG,"contains");
			//select next obj in vicinity
			if(cllistpos!=-1){
				cllistpos++;
				if(cllistpos>cllist.size()-1)
					cllistpos=0;
			}
			pclose=cllist.get(cllistpos);
			
		}
		else{
			cllist=currentlist;
			cllistpos=cllist.indexOf(pclose);
		}
		
		Log.d(TAG,"leaving closestPoint");
		return pclose;
	}
	
	public float distance(Point p){
		return (float)sqrt(pow((p.getXd()-xd),2)+pow((p.getYd()-yd),2));
	}
	
	public static void setParameters(SharedPreferences pm) {
		m_pm = pm;
		bCross = m_pm.getBoolean(CROSS_SHOW, true);
		bOrient= m_pm.getBoolean(CROSS_ORIENT, true);
		bGuide = m_pm.getBoolean(CROSS_GUIDE, false);
		lWidth = SettingsActivity.getInt(m_pm.getString(D_CROSS_LINE, "3"),3,0,15);
		lSize =  SettingsActivity.getInt(m_pm.getString(D_CROSS_SIZE, "40"),40,0,200);
		rotationOn=m_pm.getBoolean(AUTO_ROTATION,false);
	}
}
