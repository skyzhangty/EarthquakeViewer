package com.raymond.raymondearthquakesviewer;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import com.raymond.raymondearthquakesviewer.EarthquakesFragment.OnFragmentInteractionListener;
import com.raymond.raymondearthquakesviewer.EarthquakesResultFragment.OnResultFragmentInteractionListener;

public class EarthquakesViewerActivity extends Activity implements OnFragmentInteractionListener, OnResultFragmentInteractionListener{

	public static final String LATITUDE_KEY = "com.raymond.earthquakesviewer.LAT";
	public static final String LONGITUDE_KEY = "com.raymond.earthquakesviewer.LONG";
	public static final String MAGNITUDE_KEY = "com.raymond.earthquakesviewer.MAG";
	public static final String TIME_KEY = "com.raymond.earthquakesviewer.TIME";
	public static final String MINMAG_KEY = "PREF_MIN_MAG";
	public static final String SORT_BY_KEY = "PREF_SORT_BY";
	public static final String VIEW_PAST_KEY = "PREF_VIEW_PAST";
	public static final String AUTO_REFRESH_KEY = "PREF_AUTO_REFRESH";
	public static final String REFRESH_FREQ_KEY = "PREF_REFRESH_FREQ";
	public static int SHOW_PREF = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_earthquake_viewer);
		EarthquakesFragment earthquakesFragment = EarthquakesFragment.newInstance("","");
		getFragmentManager().beginTransaction().add(R.id.earthquakes_container, earthquakesFragment, "MAIN").commit();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		handleIntent(getIntent());  //Handle Search Intent
	}
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		handleIntent(intent);  //Handle Search Intent
	}
	private void handleIntent(Intent intent) {
		// TODO Auto-generated method stub
		
		if(Intent.ACTION_VIEW.equals(intent.getAction())) {
			String id = intent.getData().getLastPathSegment();
			ContentResolver cr = getContentResolver();
			String where = EarthquakesContentProvider.COL_ID+"="+id;
			Cursor query = cr.query(EarthquakesContentProvider.CONTENT_URI, null, where, null, null);
			if(query.getCount()==1 && query.moveToFirst()) {
				double latitude = query.getDouble(query.getColumnIndex(EarthquakesContentProvider.COL_LAT));
				double longitude = query.getDouble(query.getColumnIndex(EarthquakesContentProvider.COL_LONG));
				double magnitude = query.getDouble(query.getColumnIndex(EarthquakesContentProvider.COL_MAG));
				long time = query.getLong(query.getColumnIndex(EarthquakesContentProvider.COL_TIME));
				Intent viewIntent = new Intent(this, MapsActivity.class);
				viewIntent.putExtra(LATITUDE_KEY, latitude);
				viewIntent.putExtra(LONGITUDE_KEY, longitude);
				viewIntent.putExtra(MAGNITUDE_KEY, magnitude);
				viewIntent.putExtra(TIME_KEY, time);
				startActivity(viewIntent);
			}
		}
		else if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			
			//Only one instance will be created
			FragmentManager fragmentManager = this.getFragmentManager();
			if(fragmentManager.findFragmentByTag("MAIN").isVisible()) {
				setContentView(R.layout.activity_earthquake_viewer);
				FragmentTransaction transcation = fragmentManager.beginTransaction();
				EarthquakesResultFragment resultFragment = EarthquakesResultFragment.newInstance(query);  
				transcation.replace(R.id.earthquakes_container, resultFragment,"RESULTS");
				transcation.addToBackStack(null);
				transcation.commit();
			}
			else {
				EarthquakesResultFragment resultFragment = (EarthquakesResultFragment) fragmentManager.findFragmentByTag("RESULTS");
				if(resultFragment.isVisible())
					resultFragment.startSearch(query);
			}
		}
	}
	
	
	@Override
	public void onFragmentInteraction(double latitude, double longitude,double magnitude,long time) {
		// TODO Auto-generated method stub
		Intent intent= new Intent(this, MapsActivity.class);
		intent.putExtra(LATITUDE_KEY, latitude);
		intent.putExtra(LONGITUDE_KEY, longitude);
		intent.putExtra(MAGNITUDE_KEY, magnitude);
		intent.putExtra(TIME_KEY, time);
		startActivity(intent);
	}
	@Override
	public void onResultFragmentInteraction(double latitude, double longitude,double magnitude,long time) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, MapsActivity.class);
		intent.putExtra(LATITUDE_KEY, latitude);
		intent.putExtra(LONGITUDE_KEY, longitude);
		intent.putExtra(MAGNITUDE_KEY, magnitude);
		intent.putExtra(TIME_KEY, time);
		startActivity(intent);
	}
	
	
	public static String getDateSettings(SharedPreferences sPref) {
		String view_past = sPref.getString(EarthquakesViewerActivity.VIEW_PAST_KEY, "7 Days");
		if(view_past.equals("Hour")) {
			return EarthquakesContentProvider.COL_TIME+">="+(System.currentTimeMillis()-(long)60*60*1000);
		}
		else if(view_past.equals("Day")) {
			return EarthquakesContentProvider.COL_TIME+">="+(System.currentTimeMillis()-(long)24*60*60*1000);
		}
		else if(view_past.equals("7 Days")) {
			return EarthquakesContentProvider.COL_TIME+">="+(System.currentTimeMillis()-(long)7*24*60*60*1000);
		}
		else if(view_past.equals("30 Days")) {
			return EarthquakesContentProvider.COL_TIME+">="+(System.currentTimeMillis()-(long)30*24*60*60*1000);
		}
		return null;
	}
	
	
}
