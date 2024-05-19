package com.astro.dsoplanner.graph;

import com.astro.dsoplanner.base.Point;

import java.io.IOException;
import java.util.List;



public interface StarFactory {
	
	public void open() throws IOException;
	
	/**
	 * 
	 * @param q - quadrant 
	 * @return - list of stars belonging to the quadrant
	 */
	public List<Point> get(int q, double mag_limit)throws IOException;
	public void close()throws IOException;
	
}
