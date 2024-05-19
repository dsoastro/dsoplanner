package com.astro.dsoplanner.base;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.util.Holder2;
import com.astro.dsoplanner.matrix.Matrix2;
import com.astro.dsoplanner.matrix.Vector2;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;

//this is separate class as custom point does not need ra dec coords only az alt coords
public class CustomPoint extends Point {
	 public double az;
	public double alt;
	private String name;
	private static final String TAG = CustomPoint.class.getSimpleName();
  public CustomPoint(double az,double alt,String name){//initialise by az alt, may also have a label
	  super(0,0);
	  this.az=az;
	  this.alt=alt;
	  this.name=name;
	  
  }
  @Override
	public String toString() {
		return "CustomPoint [az=" + az + ", alt=" + alt +", x= "+xd+", y="+yd+"]";
	}
public CustomPoint(Vector2 v){//v - az alt coords
	  this(v.x,v.y,"");
  }
	public double x2,y2,z2;
  @Override
  public void setXY(){//calculate its screen location
		double x1 = tfun.cos( alt*PI/180) * tfun.sin((az-azCenter)*PI/180);
		double y1 = tfun.sin(alt*PI/180);
		double z1 = tfun.cos(alt*PI/180) * tfun.cos((az-azCenter)*PI/180);
  // y,z coords in the system connected with point of tangency, x stays the same
		y2 = y1 * tfun.cos(altCenter * PI / 180) - z1 * tfun.sin(altCenter * PI / 180);
		z2 = z1 * tfun.cos(altCenter * PI / 180) + y1 * tfun.sin(altCenter * PI / 180);
		x2=x1;
		x = (float)(x2 / (1 + z2));
		y = (float)(y2 / (1 + z2));
		
		double xf=x*tfun.cos(rot_angle)+y*tfun.sin(rot_angle);
		double yf=-x*tfun.sin(rot_angle)+y*tfun.cos(rot_angle);
		
		x=(float)xf;
		y=(float)yf;
	}
  public static Vector2 getAzAlt(Vector2 v){//v - xd,yd
	  return getAzAlt(v.x,v.y);
  }
  public static Vector2 getAzAlt(double xd,double yd){//getting alt and az from screen coordinates
	  double tmp = 180/PI*Point.getWidth()/Point.getFOV()*2;
	  double xf=0;
	  double yf=0;
	  
		switch (orientationAngle){
		case 0:
			xf=(xd-(Point.getWidth()/2))/(tmp*mirror);//(float)(x*tmp*mirror+(width>>1));
			yf=((Point.getHeight()/2)-yd)/tmp;//yd=(float)((height>>1)-y*tmp);
			break;
		
		case 180:
			xf=-(xd-(Point.getWidth()>>1))/(tmp*mirror);//(float)(x*tmp*mirror+(width>>1));
			yf=-((Point.getHeight()>>1)-yd)/tmp;//yd=(float)((height>>1)-y*tmp);
			break;	
			
		}

	  Matrix2 m=new Matrix2(tfun.cos(rot_angle),tfun.sin(rot_angle),-tfun.sin(rot_angle),tfun.cos(rot_angle));
	  Vector2 v=m.backMatrix().timesVector(new Vector2(xf,yf));//x,y
	  
	  double tmp1=v.x*v.x+v.y*v.y;
	  Holder2<Double,Double> h= AstroTools.solveQe(tmp1+1, 2*tmp1, tmp1-1);
	  double z2=h.x;
	  if(abs(h.x+1)<0.00001){		  //z2=-1 is one of the roots
		  z2=h.y;
	  }
	  
	  double x2=v.x*(1+z2);
	  double y2=v.y*(1+z2);

	  
	  /*  y2=  cos(a) -sin(a)  y1
	   *  z2   sin(a)  cos(a)  z1
	   * 
	   */
	  double cosa=tfun.cos(altCenter * PI / 180);
	  double sina=tfun.sin(altCenter * PI / 180);
	  
	  m=new Matrix2(cosa,-sina,sina,cosa);
	  v=m.backMatrix().timesVector(new Vector2(y2,z2));//v.x==y1,v.y==z1
	  double altr=asin(v.x);//in radians
	  double azr=atan2(x2,v.y);
	  return new Vector2(azr*180/PI+azCenter,altr*180/PI);
	  
  }

  @Override
  public void draw(Canvas canvas,Paint paint){
	  if (!Point.withinBounds(getXd(), getYd())) return;
	  Paint p=new Paint(paint);
	  float size=p.getTextSize()*Point.getScalingFactor();
	  
	  p.setTextSize(size);
	  canvas.drawText(name,getXd(),getYd(),p); //draw point label if it has one
  }
}
