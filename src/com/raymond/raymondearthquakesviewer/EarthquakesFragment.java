package com.raymond.raymondearthquakesviewer;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.raymond.raymondearthquakesviewer.EarthquakeRefreshableList.PullToRefreshListener;


/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class EarthquakesFragment extends ListFragment implements LoaderCallbacks<Cursor>{

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";
	
	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
	
	private OnFragmentInteractionListener mListener;
	ProgressBar mProgressBar;
	//private SimpleCursorAdapter mAdapter;
	private EarthquakesCusorAdapter mAdapter;
	//Two broadcast receivers. One for UI refresh, the other for periodic refresh
	private ResponseReceiver mResponseReceiver;
	private IntentFilter mBroadcastFilter;
	private EarthquakeRefreshableList mRefreshableList;
	public static EarthquakesFragment newInstance(String param1, String param2) {
		EarthquakesFragment fragment = new EarthquakesFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public EarthquakesFragment() {
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
		}
		getLoaderManager().initLoader(0, null, this);
		setHasOptionsMenu(true);
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v= inflater.inflate(R.layout.earthquakes_fragment, container, false);
		mProgressBar = (ProgressBar)(v.findViewById(R.id.progressbar));
		mRefreshableList = (EarthquakeRefreshableList)(v.findViewById(R.id.refreshable_list));
		return v;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getLoaderManager().restartLoader(0, null, this);
		mResponseReceiver = new ResponseReceiver();
		IntentFilter broadcastFilter = new IntentFilter(EarthquakeUpdateService.BROADCAST_ACTION);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mResponseReceiver, broadcastFilter);
		
		String []from = {EarthquakesContentProvider.COL_PLACE,EarthquakesContentProvider.COL_MAG};
		int []to = {android.R.id.text1, android.R.id.text2};
		mAdapter = new EarthquakesCusorAdapter(getActivity(), android.R.layout.simple_list_item_2, null, from, to, 0);
		setListAdapter(mAdapter);
		mRefreshableList.setOnRefreshListener(new PullToRefreshListener() {
			
			@Override
			public void onRefresh() {
				refreshEarthquakeList();
			}
		});
		
		refreshEarthquakeList();
		
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mResponseReceiver);
	}
	
	public void refreshEarthquakeList() {
		getListView().setEmptyView(mProgressBar);
		Intent serviceIntent = new Intent(getActivity(), EarthquakeUpdateService.class);
		getActivity().startService(serviceIntent);
		
	}

	
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		if (null != mListener) {
			// Notify the active callbacks interface (the activity, if the
			// fragment is attached to one) that an item has been selected.
			
			ContentResolver cr = getActivity().getContentResolver();
			String where = EarthquakesContentProvider.COL_ID+"="+id;
			Cursor query = cr.query(EarthquakesContentProvider.CONTENT_URI, null, where, null, null);
			if(query.getCount()==1) {
				query.moveToFirst();
				int latitude_index = query.getColumnIndex(EarthquakesContentProvider.COL_LAT);
				int longitude_index = query.getColumnIndex(EarthquakesContentProvider.COL_LONG);
				int magnitude_index = query.getColumnIndex(EarthquakesContentProvider.COL_MAG);
				int time_index = query.getColumnIndex(EarthquakesContentProvider.COL_TIME);
				if(latitude_index!=-1 && longitude_index!=-1) {
					double latitude = query.getDouble(latitude_index);
					double longitude = query.getDouble(longitude_index);
					double magnitude = query.getDouble(magnitude_index);
					long time = query.getLong(time_index);
					mListener.onFragmentInteraction(latitude,longitude,magnitude,time);
				}
			}
			query.close();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.earthquake_viewer_actionbar, menu);
		SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_settings: {
			Intent intent = new Intent(getActivity().getApplicationContext(), SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		case R.id.action_search: {
			
			return true;
		}
		case R.id.action_refresh: {
				mRefreshableList.startRefreshing();
		}
		
	}
	return false;
	}
	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated to
	 * the activity and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(double latitude, double longitude,double magnitude,long time);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String view_past_where=EarthquakesViewerActivity.getDateSettings(sPref);
		
		String where = EarthquakesContentProvider.COL_MAG + ">=" +
							Integer.parseInt(sPref.getString(EarthquakesViewerActivity.MINMAG_KEY, "3"))
							+" AND "+view_past_where;
		String sort_by = sPref.getString(EarthquakesViewerActivity.SORT_BY_KEY, "Date");
		String sortOrder = null;    //null- default is sorted by Date
		if(sort_by.equals("Magnitude")) {
			sortOrder = EarthquakesContentProvider.COL_MAG+" DESC";
		}
		CursorLoader loader = new CursorLoader(getActivity(), EarthquakesContentProvider.CONTENT_URI, null, where, null, sortOrder);

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// TODO Auto-generated method stub
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		mAdapter.swapCursor(null);
	}
	
	//When the service finishes loading data, it will send a broadcast to notify the UI update
	//This class will receive the broadcast
	private class ResponseReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			String status = intent.getStringExtra(EarthquakeUpdateService.STATUS_KEY);
			if(status.equals("SUCCESS")) {
				getLoaderManager().restartLoader(0, null, EarthquakesFragment.this);
			}
			else if(status.equals("NO NETWORK")) {
				Toast.makeText(getActivity(), "No Network Connection", Toast.LENGTH_LONG).show();
			}
			mProgressBar.setVisibility(View.INVISIBLE);
			mRefreshableList.finishRefreshing();
		}
		
	}
	

}
