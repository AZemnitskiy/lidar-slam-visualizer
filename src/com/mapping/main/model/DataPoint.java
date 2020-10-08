package com.mapping.main.model;

/*
 * This is a data class that holds the point and angle of the 
 * Data Point from the lidar readings
 */

public class DataPoint {

	public int x;
	public int y;
	public int angle;
	public int quality;
	public boolean in_ransac = false;
	
	
	public DataPoint(int x, int y, int angle, int quality)
	{
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.in_ransac = false;
		this.quality = quality;
	}
	
	public String toString()
	{
		return "(" + x + ", " + y + ")-"+ angle;
	}
	
	public boolean equals(DataPoint p)
	{
		if(this.x == p.x && this.y == p.y)
		{
			return true;
		}
		return false;
	}
}
