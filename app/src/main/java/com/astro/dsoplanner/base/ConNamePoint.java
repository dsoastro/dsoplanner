package com.astro.dsoplanner.base;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Constants;

/**
 * to draw constellation name
 * @author leonid
 *
 */
public class ConNamePoint extends Point {
	int con;
	public ConNamePoint(double ra,double dec,int con){
		super(ra,dec);
		this.con=con;
	}
	@Override
	  public void draw(Canvas canvas,Paint paint){
		  if (!Point.withinBounds(getXd(), getYd())) return;
		  drawLabel(canvas, paint);
	  }
	
	protected void drawLabel(Canvas canvas, Paint paint){
		Style orig_style=paint.getStyle();
		float orig_size=paint.getTextSize();

		paint.setStyle(Paint.Style.FILL);
		String name= Constants.constellationLong[con];
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
