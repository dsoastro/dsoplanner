package com.astro.dsoplanner.base;

import static com.astro.dsoplanner.Constants.BAYER;
import static com.astro.dsoplanner.Constants.FLAMSTEED;
import static com.astro.dsoplanner.Constants.HR;
import static com.astro.dsoplanner.Constants.greek;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.LabelLocations;


import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.graph.StarBoldness;

public class HrStar extends AstroObject {
	private static final String TAG = HrStar.class.getSimpleName();
	
	private static final String UCAC42 = "UCAC4 ";
	private static final String UCAC22 = "UCAC2 ";
	private static final String TYC = "TYC ";
	private static final String HR2 = "HR ";
	private static final String DEC3 = " dec=";
	private static final String RA3 = "ra= ";
	private static final String STAR_HR = "star, hr=";
	public static final double step=22.5;
	short fl;
	/**
	 * containts bayer number as well in the form bayer+100*bayernum
	 */
	private short bayer;
	private static Matrix _matrix = new Matrix();
    private static Xfermode overlayMode;
	private static Bitmap starBitmap = null;
	
	
	public static void setStarImage(Resources r, int id) {
		starBitmap = BitmapFactory.decodeResource(r,id);
		overlayMode = new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN);
	}
	public HrStar(int hr,double ra,double dec,double mag,int fl,int bayer,int con){
		super(ra,dec,mag,con,AstroObject.Star, AstroCatalog.YALE_CATALOG,hr);
		this.fl=(short)fl;
		this.bayer=(short)bayer;
	}

	public HrStar(DataInputStream stream) throws IOException{
		super(stream);
		fl=stream.readShort();
		bayer=stream.readShort();
		data=stream.readInt();
	}
	@Override
	public HashMap<String,String> getStringRepresentation(){
		HashMap<String,String> map=super.getStringRepresentation();
		map.put(HR,""+id);
		map.put(FLAMSTEED,""+fl);
		map.put(BAYER,""+bayer);
		return map;
		
	}
	@Override
	public byte[] getByteRepresentation(){
		byte[] buf=super.getByteRepresentation();
		ByteArrayOutputStream buff=new ByteArrayOutputStream();
		DataOutputStream stream=new DataOutputStream(buff);
		try{
			stream.writeShort(fl);
			stream.writeShort(bayer);
			stream.writeInt(data);
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
	public HrStar(HrStar s){
		super(s.ra,s.dec,s.mag,s.con,AstroObject.Star,s.catalog,s.id);
		this.fl=s.fl;
		this.bayer=s.bayer;
		this.data=s.data;
	}
	
	@Override
	public String getCanonicName(){
			return "HR"+id;
	}
	public String getCrossdbSearchName(){
			return "HR"+id;
	}
	
	
	
	private static Path static_path=new Path();
	private static Path getCleanPath(){
		static_path.reset();
		return static_path;
	}
	/**
	 * 
	 * @param canvas
	 * @param paint
	 * @param xd
	 * @param yd
	 * @param mag
	 * @return true if drawn, false otherwise
	 */
	public static boolean draw(Canvas canvas, Paint paint, float xd,float yd,double mag){
		if (!Point.withinBounds(xd, yd)) return false;
		Path circle=getCleanPath();//new Path();

		float rad = StarBoldness.Calculate(mag);//SAND:, Point.getFOV());
		//How to draw it (circle or bitmap)

		if (rad<1) rad=1;
		//in CustomView.init now paint.setAntiAlias(true);
		circle.addCircle(xd, yd, rad, Direction.CW);
		canvas.drawPath(circle, paint);

		return true;			
	}
	@Override
	public void draw(Canvas canvas, Paint paint){
		if(draw(canvas,paint,xd,yd,mag)){
			if(SettingsActivity.isStarLabelOn()&&(bayer>0||fl>0||data!=0)){
				drawLabel(canvas, paint);
			}
		}
	}
	/**
	 * 
	 * @param canvas
	 * @param p
	 * @param double_star
	 * @param xd
	 * @param yd
	 * @return true if drawn,false otherwise
	 */
	public static boolean drawStar(Canvas canvas, Paint p,boolean double_star,float xd,float yd){
		if (!Point.withinBounds(xd, yd)) return false;
		if(!double_star){
			Path star=getCleanPath();//new Path();
			float rad=9* SettingsActivity.dso_Scale()*Point.getScalingFactor();
			star.moveTo(xd, yd+rad);
			for(int i=1;i<11;i++){//i=0 - the highest point
				float dst;
				if(i%2==0)
					dst=rad;
				else
					dst=rad/2;
				float xf=(float)(xd+ ExtendedObject.msin[i]*dst);
				float yf=(float)(yd+dst*ExtendedObject.mcos[i]);
				star.lineTo(xf,yf);
				star.moveTo(xf, yf);
			}
			canvas.drawPath(star, p); 
		}
		else{
			Style orig_style=p.getStyle();
			
			p.setStyle(Paint.Style.FILL);
			
			Path ds=getCleanPath();//new Path();
			float rad = 7* SettingsActivity.dso_Scale()*Point.getScalingFactor();
			ds.addCircle(xd, yd, rad, Direction.CW);
			canvas.drawPath(ds, p);
			
			p.setStyle(Paint.Style.STROKE);
			ds=getCleanPath();//new Path();
			ds.moveTo(xd-2*rad, yd);
			ds.lineTo(xd+2*rad, yd);
			canvas.drawPath(ds, p);
			
			p.setStyle(orig_style);
		}
		return true;
		
	
	}
	
	public void drawStar(Canvas canvas, Paint p){
		if (drawStar(canvas,p,data!=0,getXd(),getYd())){
			if(SettingsActivity.isObsObjectLabelOn()){
				drawLabelAsObject(canvas, p);			
			}
		}
	}
	/**
	 * 
	 * @param canvas
	 * @param paint
	 * @param xd
	 * @param yd
	 * @param name
	 * @param obj instance of star that draws the label
	 */
	public static void drawLabelAsObject(Canvas canvas, Paint paint,float xd,float yd,String name,AstroObject obj){
		float offset=7*Point.getScalingFactor();
		float xl=xd+offset;
		float yl=yd-offset;
		
		
		LabelLocations ll=LabelLocations.getLabelLocations();
		if(!ll.get(obj,name.length())){
			return;
			
		}
		
		
		Style orig_style=paint.getStyle();
		
		paint.setStyle(Paint.Style.FILL);

		double angle=Point.getRotAngle();
		if(angle==0)
			canvas.drawText(name, xl, yl, paint);
		else{
			Path path= AstroTools.getLabelPath(xl, yl);
			
			canvas.drawTextOnPath(name, path, 0, 0, paint);
		}
		
		paint.setStyle(orig_style);
	
	}
	
	private String getLabelName(){
		String str1="";
		String str2="";
		
		float sep=0;
		if(data!=0){
			str1=" "+getComponent()+" "+String.format(Locale.US,"%.1f",getSeparation())+"\"";
			sep=getSeparation();
			str2=" "+String.format(Locale.US,"%.1f",sep)+"\"";
		}
		
		boolean show_double=sep!=0&& SettingsActivity.isDoubleSepOn();
		
		int bayer=getBayer();
		String name=AstroTools.getBrightStarName(bayer, con,id);
		
		
		if(name!=null){//bright
			if(show_double)
				name=name+str1;
			return name;
		}
		else//not bright
		{

			if(bayer>0){
				name=getBayerName()+(show_double?str1:"");
			}
			else if(fl>0){
				name=getFlName();
				if(show_double){						
					name=str2.trim();
				}
			}
			else {
				if(show_double){
					name=str2.trim();
				}
				else				
					name=getCanonicName();


			}
			return name;

		}


	}
	
	
	/**
	 * this is used for stars in obs list where label is drawn irrespective of magnification
	 * @param canvas
	 * @param paint
	 */
	private void drawLabelAsObject(Canvas canvas, Paint paint){
		drawLabelAsObject(canvas, paint, getXd(), getYd(), getLabelName(), this);
	}
	
	
	
	
	private void drawLabel(Canvas canvas, Paint paint){
		
		

		String name=AstroTools.getBrightStarName(bayer, con,id);
		if(name!=null)
			name=getLabelName();
		
		boolean bright=name!=null;
		
		if(!bright){
			if(Point.getFOV()>45.1){
				if(bayer>0&&bayer<4){
					name=getLabelName();
				}
			}
			else {
				if(bayer>0){
					name=getLabelName();
				}
				else if(Point.getFOV()<20.1&&fl>0){
					name=getLabelName();
					
				}
				else if(Point.getFOV()<20){
					name=getLabelName();
					if(name.equals(getCanonicName()))
						name=null;//zero sep or not double or not show double
				}
			}
		}
		
		
		if(name==null)return;
		
		float offset=7*Point.getScalingFactor();
		float xl=xd+offset;
		float yl=yd-offset;
		
		
		LabelLocations ll=LabelLocations.getLabelLocations();
		if(!ll.get(this,name.length())){
			return;
			
		}

		double angle=Point.getRotAngle();
		if(angle==0)
			canvas.drawText(name, xl, yl, paint);
		else{
			Path path=AstroTools.getLabelPath(xl, yl);
			canvas.drawTextOnPath(name, path, 0, 0, paint);
		}
		
		
		
	}
	public int getFlamsteed(){
		return fl;
	}
	/**
	 * 
	 * @return normalised bayer
	 */
	public int getBayer(){
		if(bayer%100>=greek.length){
			return greek.length-1;
		}
		return (bayer%100);
	}
	/**
	 * 
	 * @return e.g. pi 1 pi 2 etc
	 */
	public int getBayerNum(){
		return bayer/100;
	}

	public int getHr(){
		return id;
	}
	@Override
	public String toString(){
		return(STAR_HR+id+ RA3+ra+DEC3+dec+" "+getComponent()+" "+getSeparation());
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
	
	
	public String getBayerName(){
		if(bayer==0)return "";
		String s= ""+greek[getBayer()];
		int num=getBayerNum();
		if(num!=0)
			s=s+num;
		return s;
	}
	/**
	 * avoiding numbers after the greek letter,
	 * used for doubles
	 * @return
	 */
	public String getBayerNameWithoutNum(){
		if(bayer==0)return "";
		String s= ""+greek[getBayer()];		
		return s;
	}
	
	public String getFlName(){
		if(fl==0)
			return "";
		else
			return ""+fl;
	}
	public String getShortName(){
		String sstar="";
		int bayer=getBayer();
		if(bayer!=0){
			sstar+=greek[bayer];
			int num=getBayerNum();
			if(num>0)
				sstar+=num;
			sstar+="   "+getConString();
			return sstar;
		}
		if (fl!=0){
			sstar+=String.valueOf(fl);
			sstar+="   "+getConString();
			return sstar;
		}
		
		
		if(id!=0){
			sstar+=HR2+id;
			return sstar;
		}
		return "";
	}
	public String getLongName(){
		int bayer=getBayer();
		String sstar=AstroTools.getBrightStarName(bayer, con,id);
		if(sstar==null)
			sstar="";
		else
			sstar+="  ";

		if(bayer!=0){
			sstar+=""+greek[bayer];
			int num=getBayerNum();
			if(num>0)
				sstar+=num;
			sstar+=" ";
		}
		if (fl!=0)
			sstar+=""+fl+" ";
		if((fl!=0)||(bayer!=0))
			sstar+=" "+getConString()+"  ";
		if(id!=0)
			sstar+=HR2+id;
		
		return sstar;
	}
	public String getDsoSelName(){
		return "";
	}
	
	public int getClassTypeId(){
		return Exportable.HR_STAR_OBJECT;
	}
	@Override
	public boolean hasVisibility(){
		return true;
	}
	
	public String getFlamsteedName(){
		if (fl!=0)
			return fl+" "+getConString();
		return null;
	}
	@Override
	public String[] getNoteNames(){
		
		String flname=getFlamsteedName();
		String bname=AstroTools.getBrightStarName(bayer, con,id);
		
		List<String>list=new ArrayList<String>();
		if(flname!=null)list.add(flname);
		if(bname!=null)list.add(bname);
		list.add(getShortName());
		list.add(getLongName());
		return list.toArray(new String[list.size()]);
		
		
	}
	/**
	 * field for keeping component and separation of double stars
	 * two lower bytes - sep*10
	 * third higher byte - component
	 */
	int data=0;
	/**
	 * 
	 * @param comp - strictly A,B,C,D,E
	 * @param sep - not more than 999 (sep*10 should be short)
	 */
	public void setData(String comp,double sep){
		if(!("A".equals(comp)||"B".equals(comp)||"C".equals(comp)||"D".equals(comp)||"E".equals(comp)))
			return;
		int val=(int)comp.charAt(0)-(int)'A'+1;//A - 1, B-2 etc
		int se=(int)(sep*100);
		data=val<<24|se;
	}
	/**
	 * 
	 * @return "" if no component
	 */
	public String getComponent(){
		int val=data>>24;
		if(val==0)
			return"";
		char c=(char)((int)'A'+val-1);
		return ""+c;
	}
	public float getSeparation(){
		int mask=255<<16|255<<8|255;
		int val=data&mask;
		return val/100f;
	}
}
