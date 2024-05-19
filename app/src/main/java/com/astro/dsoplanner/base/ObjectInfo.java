package com.astro.dsoplanner.base;


import java.util.Calendar;

import com.astro.dsoplanner.base.Point;

public interface ObjectInfo {
		
		
		public double getMag();
		public boolean hasDimension();
		public boolean hasVisibility();
		public double getA();
		public double getB();
		public int getCon();
		public String getConString();
		
		public String getShortName();
		public String getLongName();
		public String getDsoSelName();
		public int getType();
		public String getTypeString();
		public String getLongTypeString();
		public Point getCurrentRaDec(Calendar c);
		
		public double getRa();
		public double getDec();
		public String getComment();
		public String getCatalogName();

}












