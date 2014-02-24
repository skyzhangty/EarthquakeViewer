package com.raymond.raymondearthquakesviewer;

import android.os.Bundle;


public class MapsFragment extends com.google.android.gms.maps.MapFragment {

	public MapsFragment() {
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
	}

}
