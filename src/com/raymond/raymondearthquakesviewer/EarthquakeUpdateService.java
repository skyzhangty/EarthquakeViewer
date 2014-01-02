package com.raymond.raymondearthquakesviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

public class EarthquakeUpdateService extends IntentService {
	
	private static final String SERVICE_TAG="Earthquake Update Service";
	public static final String BROADCAST_ACTION = "BROADCAST_UPDATE_UI";
	public static final String BROADCAST_ALARM = "BROADCAST_ALARM";
	public static final String STATUS_KEY = "STATUS";
	
	private AlarmManager mAlarmManager;
	private PendingIntent mAlarmIntent;
	
	public EarthquakeUpdateService() {
		super(SERVICE_TAG);
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		Intent intentToFire = new Intent(BROADCAST_ALARM);
		mAlarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
		
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		//Set Alarm for periodic UI refresh
		Context context = getApplicationContext();
		SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isAutoRefresh = sPref.getBoolean(EarthquakesViewerActivity.AUTO_REFRESH_KEY, false);
		if(isAutoRefresh) {
			int refresh_freq = Integer.parseInt(sPref.getString(EarthquakesViewerActivity.REFRESH_FREQ_KEY, "60"));
			int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
			long timeToRefresh = SystemClock.elapsedRealtime()+refresh_freq+60*1000;
			mAlarmManager.setInexactRepeating(alarmType, timeToRefresh, refresh_freq+60*1000, mAlarmIntent);
			
		}
		else {
			mAlarmManager.cancel(mAlarmIntent);
		}
		
		refreshEarthquakeList();
	}
	
	
	private void refreshEarthquakeList() {
		Intent localBoradcastIntent = new Intent(BROADCAST_ACTION);
		ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if(activeNetwork!=null && activeNetwork.isConnected()) {
			JSONObject jsonEarthquakes = getEarthquakeJson();
			addEarthquakeToDatabase(jsonEarthquakes);
			localBoradcastIntent.putExtra(STATUS_KEY, "SUCCESS");
		}
		else {
			localBoradcastIntent.putExtra(STATUS_KEY, "NO NETWORK");
		}
		LocalBroadcastManager.getInstance(this).sendBroadcast(localBoradcastIntent);
	}
	
	private JSONObject getEarthquakeJson() {
		StringBuilder stringBuilder = new StringBuilder();
		JSONObject jsonResponse = null;
		try {
			URL url = new URL("http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_month.geojson");
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
			String line;
			while((line=br.readLine())!=null) {
				stringBuilder.append(line);
			}
			jsonResponse = new JSONObject(stringBuilder.toString());
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonResponse;
		
	}
	
	private void addEarthquakeToDatabase(JSONObject jsonEarthquakes) {
		try {
			JSONArray features = jsonEarthquakes.getJSONArray("features");
			ContentResolver cr = getContentResolver();
			boolean loadAllData = false;
			if(cr.query(EarthquakesContentProvider.CONTENT_URI, null, null, null, null).getCount()==0) {
				loadAllData = true;
			}
			for(int i=0;i<features.length();i++) {
				JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
				JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");
				JSONArray coordinates = geometry.getJSONArray("coordinates");
				String place = properties.getString("place");
				long time = properties.getLong("time");
				double magnitude = properties.getDouble("mag");
				double longtitude = coordinates.getDouble(0);
				double latitude = coordinates.getDouble(1);
				
				if(magnitude>3) {
					if(loadAllData) {
						ContentValues values = new ContentValues();
						values.put(EarthquakesContentProvider.COL_PLACE, place);
						values.put(EarthquakesContentProvider.COL_TIME, time);
						values.put(EarthquakesContentProvider.COL_LAT, latitude);
						values.put(EarthquakesContentProvider.COL_LONG, longtitude);
						values.put(EarthquakesContentProvider.COL_MAG, magnitude);
						cr.insert(EarthquakesContentProvider.CONTENT_URI, values);
					}
					else {
						String where_time = EarthquakesContentProvider.COL_TIME + "=" + time;
						Cursor query = cr.query(EarthquakesContentProvider.CONTENT_URI, null, where_time, null, null);
						if(query.getCount()==0) {
							
							ContentValues values = new ContentValues();
							values.put(EarthquakesContentProvider.COL_PLACE, place);
							values.put(EarthquakesContentProvider.COL_TIME, time);
							values.put(EarthquakesContentProvider.COL_LAT, latitude);
							values.put(EarthquakesContentProvider.COL_LONG, longtitude);
							values.put(EarthquakesContentProvider.COL_MAG, magnitude);
							cr.insert(EarthquakesContentProvider.CONTENT_URI, values);
						}
						else {
							query.close();
							
							break;
						}
						query.close();
						//Delete any earthquakes that are over 30 days.
						long currentMillis = System.currentTimeMillis();
						String where = EarthquakesContentProvider.COL_TIME+"<"+(currentMillis-(long)30*24*60*60*1000);
						cr.delete(EarthquakesContentProvider.CONTENT_URI, where, null);
					}
				}
			}
			
			
		
				
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
