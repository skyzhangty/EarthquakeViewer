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
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.raymond.raymondearthquakesviewer.EarthquakesResultFragment.OnResultFragmentInteractionListener;

public class EarthquakesResultActivity extends Activity implements OnResultFragmentInteractionListener{

	private ActionBar mActionBar;
	private Tab mListTab;
	private Tab mMapTab;
	private boolean mNewSearchList = false, mNewSearchMaps = false;
	String mID;
	String mQuery;
	private static final String NAVINDEX_KEY = "NAVINDEX_KEY";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_earthquakes_result);
		Intent intent = getIntent();
		mID= intent.getStringExtra(EarthquakesViewerActivity.SEARCH_ID_KEY);
		mQuery = intent.getStringExtra(EarthquakesViewerActivity.SEARCH_QUERY_KEY);
		mActionBar = getActionBar();
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		mListTab = mActionBar.newTab();
		mListTab.setText("List").setTabListener(new ListTabListener(this));
		mActionBar.addTab(mListTab);
		
		mMapTab = mActionBar.newTab();
		mMapTab.setText("Maps").setTabListener(new MapsTabListener(this));
		mActionBar.addTab(mMapTab);
		
		mMapTab.select();
		mListTab.select();
		if(savedInstanceState!=null && savedInstanceState.containsKey(NAVINDEX_KEY)) {
			mActionBar.setSelectedNavigationItem(savedInstanceState.getInt(NAVINDEX_KEY));
		}
		
	}

	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(NAVINDEX_KEY, mActionBar.getSelectedNavigationIndex());
		super.onSaveInstanceState(outState);
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		mID=mQuery="";
		if(Intent.ACTION_VIEW.equals(intent.getAction())) {
			mID=intent.getData().getLastPathSegment();
		}
		else if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
			mQuery = intent.getStringExtra(SearchManager.QUERY);
		}
		
		if(!mQuery.equals("") ||!mID.equals("")) {
			//fragment = EarthquakesResultFragment.newInstance(id, query);
			//getFragmentManager().beginTransaction().replace(R.id.result_container, fragment, EarthquakesResultFragment.class.getName()).addToBackStack(null).commit();
			//mEarthquakesResultFragment=null;
			mNewSearchList = true;
			mNewSearchMaps = true;
			mListTab.select();
		}
	}

	@Override
	public void onResultFragmentInteraction(double latitude, double longitude,
			double magnitude, long time) {
		mMapTab.select();
		GoogleMap map = ((MapFragment)getFragmentManager().findFragmentByTag(MapsFragment.class.getName())).getMap();
		map.clear();
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
		String formattedDate = sdf.format(date);
		String markerTitle ="Mag: "+magnitude+" Date: "+formattedDate;
		LatLng earthquake_location = new LatLng(latitude, longitude);
		map.addMarker(new MarkerOptions().position(earthquake_location).title(markerTitle)).showInfoWindow();
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(earthquake_location, 4));
	}
	
	private class ListTabListener implements ActionBar.TabListener {

		Activity activity;
		Fragment fragment;
		public ListTabListener(Activity activity) {
			this.activity = activity;
			fragment = activity.getFragmentManager().findFragmentByTag(EarthquakesResultFragment.class.getName());
			if(fragment!=null && !fragment.isDetached()) {
				activity.getFragmentManager().beginTransaction().detach(fragment).commit();
			}
		}
		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			
			if(fragment!=null && !mNewSearchList) {
				ft.attach(fragment);
			}
			else {
				fragment = EarthquakesResultFragment.newInstance(mID, mQuery);
				activity.getFragmentManager().beginTransaction().replace(R.id.result_container,fragment, EarthquakesResultFragment.class.getName()).addToBackStack(null).commit();
				mNewSearchList = false;
			}
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			
			if(fragment==null) {
				fragment = EarthquakesResultFragment.newInstance(mID, mQuery);
				activity.getFragmentManager().beginTransaction().add(R.id.result_container,fragment, EarthquakesResultFragment.class.getName()).commit();
				
			}
			else {
				ft.attach(fragment);
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			Log.v("fuck",fragment.getClass().getName());
			ft.detach(fragment);
		}

	}
	
	private  class MapsTabListener implements ActionBar.TabListener {

		Activity activity;
		Fragment fragment;
		public MapsTabListener(Activity activity) {
			
			this.activity = activity;
			fragment = activity.getFragmentManager().findFragmentByTag(MapsFragment.class.getName());
			if(fragment!=null && !fragment.isDetached()) {
				activity.getFragmentManager().beginTransaction().detach(fragment).commit();
			}
		}
		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			
			if(fragment != null) {
				ft.attach(fragment);
				
			}
			else {
				
				fragment = Fragment.instantiate(activity, MapsFragment.class.getName());
				activity.getFragmentManager().beginTransaction().replace(R.id.result_container, fragment, MapsFragment.class.getName()).commit();
				mNewSearchMaps = false;
			}
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			
			if(fragment == null) {
				
				fragment = Fragment.instantiate(activity, MapsFragment.class.getName());
				activity.getFragmentManager().beginTransaction().add(R.id.result_container, fragment, MapsFragment.class.getName()).commit();
				mNewSearchMaps = false;
			}
			else {
				ft.attach(fragment);
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			ft.detach(fragment);
		}
		
	}


}
