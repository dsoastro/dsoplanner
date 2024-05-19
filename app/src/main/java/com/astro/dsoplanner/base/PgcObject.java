package com.astro.dsoplanner.base;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.max;

import java.io.DataInputStream;
import java.io.IOException;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.LabelLocations;
import com.astro.dsoplanner.SettingsActivity;

/**
 * used in PgcFactory
 * @author leonid
 *
 */
public class PgcObject extends CustomObject {
	public int cpos=-1;//position in the file
	public PgcObject(int catalog,int id,double ra,double dec,int con,int type,String typeStr,double a,
			double b,double mag,double pa,String name1,String name2,String comment){
		super(catalog,id,ra,dec,con,type,typeStr,a,b,mag,pa,name1,name2,comment);
	}
	public PgcObject(int catalog,int id,double ra,double dec,int con,int type,String typeStr,double a,
			double b,double mag,double pa,String name1,String name2,String comment,int cpos){
		this(catalog,id,ra,dec,con,type,typeStr,a,b,mag,pa,name1,name2,comment);
		this.cpos=cpos;
	}

	public PgcObject(DataInputStream stream) throws IOException{
		super(stream);		
	}
	@Override
	public void draw(Canvas canvas, Paint paint){
		
		if (!Point.withinBounds(getXd(), getYd(),(float)(max(a,b)/60*Point.getWidth()/Point.getFOV()/2))) return;
		double scale=Point.getWidth()/Point.getFOV()/2;
		double size=Math.max(a,b)*scale/60;
		
		
		//drawing in real dimension and position
		if(Point.getFOV()<= SettingsActivity.dso_GetMinZoom()&& SettingsActivity.dso_ShowShape()&&size> SettingsActivity.dso_GetDimScaleFactor()* SettingsActivity.dso_Scale()*12*Point.getScalingFactor()){
			
			Path gxy=getCleanPath();//new Path();
			Point n=getCleanPoint(ra,dec+0.1);//new Point(ra,dec+0.1);
			n.setXY();
			n.setDisplayXY();
			double angleNorth=atan2(n.getYd()-getYd(),n.getXd()-getXd());
			float angle=(float)((angleNorth*180/PI-pa));

			double xl=getXd()-scale*a/60;
			double xr=getXd()+scale*a/60;
			double ytop=getYd()-scale*b/60;
			double ybot=getYd()+scale*b/60;
			RectF rect=getStaticRectF((float)xl,(float)ytop,(float)xr,(float)ybot);
			gxy.addOval(rect, Direction.CW);		
			Matrix matrix=getCleanMatrix();
			matrix.setRotate(angle, getXd(), getYd());
			gxy.transform(matrix);
			canvas.drawPath(gxy, paint);
		}
		else {
			drawGxy(canvas,paint);
		}
		
		if(SettingsActivity.isLayerObjectLabelOn()){
			drawLabel(canvas,paint);
		}
	}
	@Override
	protected void drawLabel(Canvas canvas, Paint paint){
		LabelLocations ll=LabelLocations.getLabelLocations();
		String name=getShortName();
		if(ll.get(this,name.length())){
			Style orig_style=paint.getStyle();
			float orig_size=paint.getTextSize();
			
			paint.setStyle(Paint.Style.FILL);
			
			float size=paint.getTextSize()*Point.getScalingFactor();
			float offset=7*Point.getScalingFactor();
			paint.setTextSize(size);
			
			float xl=xd+offset;
			float yl=yd-offset;
			double angle=Point.getRotAngle();			
			if(angle==0)
				canvas.drawText(name, xl, yl, paint);
			else{
				Path path= AstroTools.getLabelPath(xl, yl);
				canvas.drawTextOnPath(name, path, 0, 0, paint);
			}
			
			paint.setStyle(orig_style);
			paint.setTextSize(orig_size);
			
		}
		
		
	}
	private void drawGxy(Canvas canvas, Paint paint){
		Path gxy=getCleanPath();//new Path();
		float orig_width=paint.getStrokeWidth();
		
		paint.setStrokeWidth(2);
		float st=12* SettingsActivity.dso_Scale()*Point.getScalingFactor();
		RectF rect=getStaticRectF(xd-st,yd-st/2,xd+st,yd+st/2);//new RectF(xd-st,yd-st/2,xd+st,yd+st/2);
		gxy.addOval(rect, Direction.CW);
		Matrix matrix=getCleanMatrix();
		matrix.setRotate(45, getXd(), getYd());
		gxy.transform(matrix);		
		canvas.drawPath(gxy,paint);
		
		paint.setStrokeWidth(orig_width);
	}
}
