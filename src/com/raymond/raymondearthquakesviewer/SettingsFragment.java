package com.raymond.raymondearthquakesviewer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{

	public SettingsFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		ListPreference sort_by = (ListPreference)getPreferenceManager().findPreference(EarthquakesViewerActivity.SORT_BY_KEY);
		sort_by.setSummary(sort_by.getValue());
		ListPreference view_past = (ListPreference)getPreferenceManager().findPreference(EarthquakesViewerActivity.VIEW_PAST_KEY);
		view_past.setSummary(view_past.getValue());
		CheckBoxPreference auto_refresh = (CheckBoxPreference)findPreference(EarthquakesViewerActivity.AUTO_REFRESH_KEY);
		ListPreference refresh_freq = (ListPreference)findPreference(EarthquakesViewerActivity.REFRESH_FREQ_KEY);
		refresh_freq.setEnabled(auto_refresh.isChecked());
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		if(key.equals(EarthquakesViewerActivity.SORT_BY_KEY) || key.equals(EarthquakesViewerActivity.VIEW_PAST_KEY)) {
			Preference sort_by = findPreference(key);
			sort_by.setSummary(sharedPreferences.getString(key, ""));
		}
		else if(key.equals(EarthquakesViewerActivity.AUTO_REFRESH_KEY)) {
			CheckBoxPreference auto_refresh = (CheckBoxPreference)findPreference(key);
			ListPreference refresh_freq = (ListPreference)findPreference(EarthquakesViewerActivity.REFRESH_FREQ_KEY);
			refresh_freq.setEnabled(auto_refresh.isChecked());
		}
	}
}
