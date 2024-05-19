package com.astro.dsoplanner.base;

import static com.astro.dsoplanner.Constants.CATALOG;
import static com.astro.dsoplanner.Constants.CONSTEL;
import static com.astro.dsoplanner.Constants.DEC;
import static com.astro.dsoplanner.Constants.ID;
import static com.astro.dsoplanner.Constants.MAG;
import static com.astro.dsoplanner.Constants.RA;
import static com.astro.dsoplanner.Constants.TYPE;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;

import com.astro.dsoplanner.AstroTools;
import com.astro.dsoplanner.Constants;
import com.astro.dsoplanner.DetailsActivity;
import com.astro.dsoplanner.Global;


import com.astro.dsoplanner.R;
import com.astro.dsoplanner.SettingsActivity;
import com.astro.dsoplanner.database.DbListItem;

public abstract class AstroObject extends Point implements ObjectInfo, Exportable {


	private static final String TAG1 = AstroObject.class.getSimpleName();
	
	
	
	
	private static final String BRIGHT_DS = "Bright DS";
	private static final String QUASAR = "Quasar";
	private static final String NOT_FOUND = "Not found";
	private static final String ASTERISM = "Asterism";
	private static final String DARK_NEBULA = "Dark Nebula";
	private static final String CLUSTER_OF_GALAXIES = "Cluster of galaxies";
	private static final String PLANET ="Planet";
	private static final String COMET4 = "Comet";
	private static final String DOUBLE_STAR = "Double Star";
	private static final String STAR4 = "Star";
	private static final String MINOR_PLANET2 = "Minor Planet";
	private static final String CUSTOM_TYPE = "Custom Type";
	private static final String SUPERNOVA_REMNANT = "Supernova Remnant";
	private static final String PLANETARY_NEBULA = "Planetary Nebula";
	private static final String OPEN_CLUSTER_NEBULA = "Open Cluster+Nebula";
	private static final String OPEN_CLUSTER = "Open Cluster";
	private static final String NEBULA = "Nebula";
	private static final String HII_REGION = "HII Region";
	private static final String GALAXY_CLOUD = "Galaxy Cloud";
	private static final String GALAXY = "Galaxy";
	private static final String GLOBULAR_CLUSTER = "Globular Cluster";
	private static final String QS2 = "QS";
	private static final String DN2 = "DN";
	private static final String CG2 = "CG";
	private static final String PLANETS = "Planets";
	private static final String PLANET5 = PLANET;
	private static final String PLANET4 = "PLANET";
	private static final String UGC = "UGC";
	private static final String NEB_CONTOUR = "Neb Contour";
	private static final String NO_CATALOG = "No catalog";
	private static final String PGC = "PGC";
	private static final String UCAC4 = "UCAC4";
	private static final String UCAC2 = "UCAC2";
	private static final String YALE = "Yale";
	private static final String TYCHO_2 = "Tycho-2";
	private static final String NGC_IC = "NgcIc";
	private static final String DEC2 = ",dec=";
	private static final String RA2 = ",ra=";
	private static final String MAG2 = ", mag=";
	private static final String CON2 = ", con=";
	private static final String ID2 = ", id=";
	private static final String CATALOG2 = ", catalog=";
	private static final String ASTRO_OBJECT_TYPE = "AstroObject [type=";
	private static final String COMET3 = COMET4;
	private static final String STAR3 = STAR4;
	private static final String PLANET3 = "mPlanet";
	private static final String NEB3 = "Neb";
	private static final String HII_RG = "HIIRg";
	private static final String GXY_CLD = "GxyCld";
	private static final String GX2 = "Gx";
	private static final String COMET2 = "COMET";
	private static final String DS = "DS";
	private static final String STAR2 = "STAR";
	private static final String PLANET2 = "MPLANET";
	private static final String CUSTOM2 = "CUSTOM";
	private static final String SNR2 = "SNR";
	private static final String PN2 = "PN";
	private static final String OCN = "OCN";
	private static final String OC2 = "OC";
	private static final String NEB2 = "NEB";
	private static final String HIIRG = "HIIRG";
	private static final String GXYCLD2 = "GXYCLD";
	private static final String GX = "GX";
	private static final String GC2 = "GC";
	private static final String NF2 = "NF";
	private static final String AST2 = "AST";
	
	
	public static final int GC = 1;
	public static final int Gxy = 2;
	public static final int GxyCld = 3;
	public static final int HIIRgn = 4;
	public static final int Neb = 5;
	public static final int OC = 6;
	public static final int OCNeb = 7;
	public static final int PN = 8;
	public static final int SNR = 9;	
	public static final int Custom=10;
	public static final int MINOR_PLANET=11;
	public static final int Star=12;
	public static final int DoubleStar=13;
	public static final int Comet=14;
	public static final int REAL_PLANET=15;
	public static final int CG=16;
	public static final int DN=17;
	public static final int AST=18;
	public static final int NF=19;
	public static final int QS=20;
	
	public static Map<String,Integer> typeMap=new HashMap<String,Integer>();
	static{//should coincide with types[]//upper case
		typeMap.put(GC2, GC);
		typeMap.put(GX, Gxy);
		typeMap.put(GXYCLD2, GxyCld);
		typeMap.put(HIIRG, HIIRgn);
		typeMap.put(NEB2, Neb);
		typeMap.put(OC2, OC);
		typeMap.put(OCN, OCNeb);
		typeMap.put(PN2, PN);
		typeMap.put(SNR2, SNR);
		typeMap.put(CUSTOM2, Custom);
		typeMap.put(PLANET2, MINOR_PLANET);
		typeMap.put(STAR2, Star);
		typeMap.put(DS, DoubleStar);
		typeMap.put(COMET2,Comet);
		typeMap.put(PLANET4,REAL_PLANET);
		typeMap.put(CG2,CG);
		typeMap.put(DN2,DN);
		typeMap.put(AST2, AST);
		typeMap.put(NF2, NF);
		typeMap.put(QS2,QS);
	}
	
	private static String[] types= new String[] {"",GC2,GX2,GXY_CLD,HII_RG,NEB3,OC2,OCN,PN2,SNR2,CUSTOM2,PLANET3,STAR3,DS,COMET3,PLANET5,CG2,DN2,AST2,NF2,QS2 };
	public static String getTypeString(int num){
		if (num<0||num>=types.length)
			return "";
		else
			return types[num];
	}
	public static String getLongTypeString(int num){
		if (num<0||num>=types.length)
			return "";
		switch(num){
		case GC:return GLOBULAR_CLUSTER;
		case Gxy:return GALAXY;
		case GxyCld:return GALAXY_CLOUD;
		case HIIRgn:return HII_REGION;
		case Neb:return NEBULA;
		case OC:return OPEN_CLUSTER;
		case OCNeb:return OPEN_CLUSTER_NEBULA;
		case PN:return PLANETARY_NEBULA;
		case SNR:return SUPERNOVA_REMNANT;
		case Custom:return CUSTOM_TYPE;
		case MINOR_PLANET:return MINOR_PLANET2;
		case Star:return STAR4;
		case DoubleStar:return DOUBLE_STAR;
		case Comet:return COMET4;
		case REAL_PLANET:return PLANET;
		case CG:return CLUSTER_OF_GALAXIES;
		case DN:return DARK_NEBULA;
		case AST:return ASTERISM;
		case NF:return NOT_FOUND;
		case QS:return QUASAR;
		}
		return "";
	}
	public static String getLongTypeString(int num,Context context){
		if (num<0||num>=types.length)
			return "";
		switch(num){
		case GC:return context.getString(R.string.GC_title);
		case Gxy:return context.getString(R.string.Gxy_title);
		case GxyCld:return context.getString(R.string.GxyCld_title);
		case HIIRgn:return context.getString(R.string.HIIRgn_title);
		case Neb:return context.getString(R.string.Neb_title);
		case OC:return context.getString(R.string.OC_title);
		case OCNeb:return context.getString(R.string.OCNeb_title);
		case PN:return context.getString(R.string.PN_title);
		case SNR:return context.getString(R.string.SNR_title);
		case Custom:return context.getString(R.string.custom_type);
		case MINOR_PLANET:return context.getString(R.string.minor_planet2);
		case Star:return context.getString(R.string.star2);
		case DoubleStar:return context.getString(R.string.double_star2);
		case Comet:return context.getString(R.string.comet2);
		case REAL_PLANET:return  context.getString(R.string.planet);
		case CG:return context.getString(R.string.cluster_of_galaxies);
		case DN:return context.getString(R.string.dark_nebula);
		case AST:return context.getString(R.string.asterism);
		case NF:return context.getString(R.string.not_found);
		case QS:return context.getString(R.string.quasar);
		}
		return "";
	}
	
	public byte type;//object type
	public short catalog;//do not override as used for equals
	public int id;//do not override as used for equals
	public byte con;
	public double mag;//double as may be DOUBLE.NAN
	public int ref;
	
	public AstroObject(double ra,double dec,double mag,int con,int type,int catalog,int id){
		super(ra,dec);
		this.type=(byte)type;
		this.catalog=(short)catalog;
		this.id=id;
		this.con=(byte)con;
		this.mag=mag;
	}
	public AstroObject(DataInputStream stream) throws IOException{
		super(0,0);	
		ra=(float)stream.readDouble();
		dec=(float)stream.readDouble();
		mag=stream.readDouble();
		int con2=stream.readShort();
		int type2=stream.readShort();
		catalog=stream.readShort();
		id=stream.readInt();	
		
		int byte1;
		int byte2;
		//taking ref out
		
		byte1=(con2>>8)&255;//higher byte, taking - out
		
		byte2=(type2>>8)&255;//lower byte
		if(byte1!=0||byte2!=0)
			ref=(byte1<<8)|byte2;
		con2=con2&255;
		type2=type2&255;
		con=(byte)con2;
		type=(byte)type2;

	}
	@Override
	public String toString() {
		return ASTRO_OBJECT_TYPE + type + CATALOG2 + catalog + ID2
				+ id + CON2 + con + MAG2 + mag +RA2+ra+" "+ DetailsActivity.doubleToGrad(ra, ' ', ' ')+DEC2+dec+" "+ DetailsActivity.doubleToGrad(dec, ' ', ' ')+" ref="+ref+ "]";
	}
	@Override
	public boolean equals(Object o){

		if(o instanceof AstroObject){
			AstroObject obj=(AstroObject)o;
			boolean removedups= SettingsActivity.isRemovingDuplicates();
			if(obj.ref==0&&ref==0||!removedups){
				if(obj.catalog==catalog&&obj.id==id)
					return true;
			}
			else 
				return(obj.ref==ref);
			
			
		}
		return false;
	}
	
	@Override
	public int hashCode(){

		if(ref!=0)
			return ref;
		else
			return 37*catalog+id;
	}
	public int getType(){
		return type;
	}
	public int getCatalog(){
		return catalog;
	}
	public String getCatalogName(){
		switch(catalog){
		case AstroCatalog.NGCIC_CATALOG:
			return NGC_IC;
		case AstroCatalog.PLANET_CATALOG:
			return PLANETS;
		case AstroCatalog.TYCHO_CATALOG:
			return TYCHO_2;
		case AstroCatalog.YALE_CATALOG:
			return YALE;
		case AstroCatalog.UCAC2_CATALOG:
			return UCAC2;
		case AstroCatalog.UCAC4_CATALOG:
			return UCAC4;
		case AstroCatalog.PGC_CATALOG:
			return PGC;
		case AstroCatalog.CUSTOM_CATALOG:
			return NO_CATALOG;
		case AstroCatalog.MARK_CATALOG:	
			return NO_CATALOG;
		case AstroCatalog.CONTOUR_CATALOG:
			return NEB_CONTOUR;
		case AstroCatalog.SAC_LAYER_CATALOG:
			return UGC;
		case AstroCatalog.HAAS:
			return BRIGHT_DS;
		}
		DbListItem item= AstroTools.findItemByCatId(catalog);
		if(item==null)
			return NO_CATALOG;
		return item.dbName;
	}
	public int getId(){
		return id;
	}
	public int getCon(){
		return con;
	}
	public String getConString(){
		if(con!=0)
			return Constants.constellations[con];
		else
			return Constants.constellations[AstroTools.getConstellation(AstroTools.getNormalisedRa(ra),dec)];
	}
	public double getMag(){
		return mag;
	}
	public String getTypeString(){
		return types[type];
	}
	public String getLongTypeString(){
		try{
			return getLongTypeString(type, Global.getAppContext());
		}
		catch(Exception e){}
		return getLongTypeString(type);
	}
	/**
	 * precession to time c is taken into account
	 */
	public Point getCurrentRaDec(Calendar c){
		return AstroTools.precession(this, c);
	}
	public byte[] getByteRepresentation(){
		ByteArrayOutputStream buff=new ByteArrayOutputStream(4+4+4+2+2+2+4);
		DataOutputStream stream=new DataOutputStream(buff);
		try{
			stream.writeDouble(ra);
			stream.writeDouble(dec);
			stream.writeDouble(mag);
			
			//ref
			int byte1=ref>>8;//higher byte
			int byte2=ref&255;//lower byte

			
			stream.writeShort(con|(byte1<<8));
			stream.writeShort(type|(byte2<<8));
			stream.writeShort(catalog);
			stream.writeInt(id);
		}
		catch(IOException e){
			return null;
		}
		return buff.toByteArray();			
	}
	public HashMap<String,String> getStringRepresentation(){
		HashMap<String,String> map=new HashMap<String,String>();
		map.put(RA,String.format(Locale.US,"%.6f",ra));
		map.put(DEC,String.format(Locale.US,"%.6f",dec));
		map.put(MAG,String.format(Locale.US,"%.6f",mag));
		map.put(CONSTEL, getConString());
		map.put(TYPE, getTypeString());
		map.put(CATALOG, String.valueOf(catalog));
		map.put(ID, String.valueOf(id));
		return map;
	}
	public String getComment(){
		return "";
	}
	public String[] getNoteNames(){		
		return new String[]{getShortName(),getLongName()};
	}
	/**
	 * the name is used for keeping notes. Its only meaning is to replace ngcic long name and use other long name for stars
	 * @return
	 */
	public String getCanonicName(){
		return getLongName();
	}
}
