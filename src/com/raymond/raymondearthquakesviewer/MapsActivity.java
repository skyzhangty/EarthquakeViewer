package com.raymond.raymondearthquakesviewer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class MapsActivity extends Activity {

	private GoogleMap mMap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		mMap = ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		Intent intent = getIntent();
		double latitude = intent.getDoubleExtra(EarthquakesViewerActivity.LATITUDE_KEY, 0);
		double longitude = intent.getDoubleExtra(EarthquakesViewerActivity.LONGITUDE_KEY, 0);
		double magnitude = intent.getDoubleExtra(EarthquakesViewerActivity.MAGNITUDE_KEY, 0);
		long time = intent.getLongExtra(EarthquakesViewerActivity.TIME_KEY, 0);
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
		String formattedDate = sdf.format(date);
		String markerTitle ="Mag: "+magnitude+" Date: "+formattedDate;
		LatLng earthquake_location = new LatLng(latitude, longitude);
		mMap.addMarker(new MarkerOptions().position(earthquake_location).title(markerTitle)).showInfoWindow();
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(earthquake_location, 4));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.maps, menu);
		return true;
	}

}
