package com.raymond.raymondearthquakesviewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EarthquakeUpdateReceiver extends BroadcastReceiver {
	
	public EarthquakeUpdateReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, EarthquakeUpdateService.class);
		context.startService(serviceIntent);
	}
}
