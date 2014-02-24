package com.raymond.raymondearthquakesviewer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.raymond.raymondearthquakesviewer.EarthquakesFragment.OnFragmentInteractionListener;

public class EarthquakesViewerActivity extends Activity implements OnFragmentInteractionListener{

	public static final String LATITUDE_KEY = "com.raymond.earthquakesviewer.LAT";
	public static final String LONGITUDE_KEY = "com.raymond.earthquakesviewer.LONG";
	public static final String MAGNITUDE_KEY = "com.raymond.earthquakesviewer.MAG";
	public static final String TIME_KEY = "com.raymond.earthquakesviewer.TIME";
	public static final String SEARCH_ID_KEY="com.raymond.earthquakesviewer.SEARCHID";
	public static final String SEARCH_QUERY_KEY="com.raymond.earthquakesviewer.SEARCHQUERY";
	public static final String MINMAG_KEY = "PREF_MIN_MAG";
	public static final String SORT_BY_KEY = "PREF_SORT_BY";
	public static final String VIEW_PAST_KEY = "PREF_VIEW_PAST";
	public static final String AUTO_REFRESH_KEY = "PREF_AUTO_REFRESH";
	public static final String REFRESH_FREQ_KEY = "PREF_REFRESH_FREQ";
	private static final String NAVINDEX_KEY = "NAVINDEX_KEY";
	public static int SHOW_PREF = 1;
	
	private  ActionBar mActionBar;
	private  Tab mListTab;
	private  Tab mMapTab;
	
	private Fragment mListFragment,mMapsFragment;
	//private TabListener<EarthquakesFragment> mEarthquakesTabListener =;
	//private TabListener<MapsFragment> mMapsTabListener = ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_earthquake_viewer);
		mListFragment = getFragmentManager().findFragmentById(R.id.list_fragment);
		mMapsFragment = getFragmentManager().findFragmentById(R.id.maps_fragment);
		mActionBar = getActionBar();
		mActionBar.removeAllTabs();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mListTab = mActionBar.newTab();
		mListTab.setText("List");
		mListTab.setTabListener( new TabListener<EarthquakesFragment>(this, R.id.earthquakes_container, EarthquakesFragment.class));
		mActionBar.addTab(mListTab);
		
		mMapTab = mActionBar.newTab();
		mMapTab.setText("Maps");
		mMapTab.setTabListener(new TabListener<MapsFragment>(this, R.id.earthquakes_container, MapsFragment.class));
		mActionBar.addTab(mMapTab);
		
		//mMapTab.select();
		//mListTab.select();
		if(savedInstanceState!=null && savedInstanceState.containsKey(NAVINDEX_KEY)) {
			mActionBar.setSelectedNavigationItem(savedInstanceState.getInt(NAVINDEX_KEY));
		}
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
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(NAVINDEX_KEY, mActionBar.getSelectedNavigationIndex());
		super.onSaveInstanceState(outState);
	}
	private void handleIntent(Intent intent) {
		// TODO Auto-generated method stub
		String id="";
		String query = "";
		if(Intent.ACTION_VIEW.equals(intent.getAction())) {
			id = intent.getData().getLastPathSegment();
		}
		else if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
			query = intent.getStringExtra(SearchManager.QUERY);
		}
		if(!id.equals("") || !query.equals("")) {
			showSearchResults(id, query);
		}
	}
	
	private void showSearchResults(String id, String query) {
		Intent intent = new Intent(this, EarthquakesResultActivity.class);
		intent.putExtra(SEARCH_ID_KEY, id);
		intent.putExtra(SEARCH_QUERY_KEY, query);
		startActivity(intent);
	}
	@Override
	public void onFragmentInteraction(double latitude, double longitude,double magnitude,long time) {
		mMapTab.select();
		GoogleMap map = ((MapsFragment)mMapsFragment).getMap();
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
		String formattedDate = sdf.format(date);
		String markerTitle ="Mag: "+magnitude+" Date: "+formattedDate;
		LatLng earthquake_location = new LatLng(latitude, longitude);
		map.addMarker(new MarkerOptions().position(earthquake_location).title(markerTitle)).showInfoWindow();
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(earthquake_location, 4));
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
	
	public  class TabListener <T extends Fragment>implements ActionBar.TabListener {

		private Fragment fragment;
		private Activity activity;
		private Class<T> fragmentClass;
		private int fragmentContainer;
		
		public TabListener(Activity activity, int fragmentContainer, Class<T>fragmentClass) {
			this.activity = activity;
			this.fragmentContainer = fragmentContainer;
			this.fragmentClass = fragmentClass;
			this.fragment = null;
		}
		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			if(fragmentClass.getName().equals("com.raymond.raymondearthquakesviewer.EarthquakesFragment")) {
				activity.getFragmentManager().beginTransaction().show(mListFragment).commit();
				activity.getFragmentManager().beginTransaction().hide(mMapsFragment).commit();
			}
			else if (fragmentClass.getName().equals("com.raymond.raymondearthquakesviewer.MapsFragment")) {
				activity.getFragmentManager().beginTransaction().hide(mListFragment).commit();
				activity.getFragmentManager().beginTransaction().show(mMapsFragment).commit();
			}
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			if(fragmentClass.getName().equals("com.raymond.raymondearthquakesviewer.EarthquakesFragment")) {
				activity.getFragmentManager().beginTransaction().show(mListFragment).commit();
				activity.getFragmentManager().beginTransaction().hide(mMapsFragment).commit();
			}
			else if (fragmentClass.getName().equals("com.raymond.raymondearthquakesviewer.MapsFragment")) {
				activity.getFragmentManager().beginTransaction().hide(mListFragment).commit();
				activity.getFragmentManager().beginTransaction().show(mMapsFragment).commit();
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			
		}
		
	}
	
}
