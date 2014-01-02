package com.raymond.raymondearthquakesviewer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Earthquake {
	private String mPlace;
	private long  mTime;
	private double mMagnitude;
	private double mLongitude;
	private double mLatitude;
	
	public Earthquake(String place, long time, double magnitude, double longitude, double latitude) {
		mPlace = place;
		mTime = time;
		mMagnitude = magnitude;
		mLongitude = longitude;
		mLatitude = latitude;
	}
	public double getLongitude() {
		return this.mLongitude;
	}
	
	public double getLatitude() {
		return this.mLatitude;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z",Locale.US);
		String date = sdf.format(new Date(mTime));
		return mPlace+" "+mMagnitude+" "+date;
	}
}
