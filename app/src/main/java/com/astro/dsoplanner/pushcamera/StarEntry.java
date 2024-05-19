package com.astro.dsoplanner.pushcamera;

public class StarEntry {
	
	double ra,dec,mag;
	int hr;
	public StarEntry(double ra, double dec, double mag, int hr) {
		super();
		this.ra = ra;
		this.dec = dec;
		this.mag = mag;
		this.hr = hr;
	}
	@Override
	public String toString() {
		return "StarEntry [ra=" + ra + ", dec=" + dec + ", mag=" + mag + ", hr=" + hr + "]";
	}
	
	
}
