package com.raymond.raymondearthquakesviewer;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link EarthquakesResultFragment.OnFragmentInteractionListener} interface to
 * handle interaction events. Use the
 * {@link EarthquakesResultFragment#newInstance} factory method to create an
 * instance of this fragment.
 * 
 */
public class EarthquakesResultFragment extends ListFragment implements LoaderCallbacks<Cursor>{
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String QUERY = "com.raymond.raymondearthquakesviewer.query";
	private static final String ID = "com.raymond.raymondearthquakesviewer.id";
	private String mQuery="", mID="";
	private EarthquakesCusorAdapter mAdapter;
	private OnResultFragmentInteractionListener mListener;
	/**
	 * Use this factory method to create a new instance of this fragment using
	 * the provided parameters.
	 * 
	 * @param param1
	 *            Parameter 1.
	 * @param param2
	 *            Parameter 2.
	 * @return A new instance of fragment EarthquakesResultFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static EarthquakesResultFragment newInstance(String id, String query) {
		
		EarthquakesResultFragment fragment = new EarthquakesResultFragment();
		Bundle args = new Bundle();
		args.putString(ID, id);
		args.putString(QUERY, query);
		fragment.setArguments(args);
	
		return fragment;
	}

	public EarthquakesResultFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mID = getArguments().getString(ID);
			mQuery = getArguments().getString(QUERY);
		}
		setHasOptionsMenu(true);
		getActivity().getActionBar().setHomeButtonEnabled(true);
		getActivity().getActionBar().setDisplayUseLogoEnabled(false); 
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_earthquakes_result,container, false);
		return view;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(0, null, this);
		String []from = {EarthquakesContentProvider.COL_PLACE,EarthquakesContentProvider.COL_MAG};
		int []to = {android.R.id.text1,android.R.id.text2};
		mAdapter = new EarthquakesCusorAdapter(getActivity(), android.R.layout.simple_list_item_2, null, from, to,0);
		setListAdapter(mAdapter);
		
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(0, null, this);
		getActivity().getActionBar().getTabAt(0).select();
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		String where = EarthquakesContentProvider.COL_ID + "=" +id;
		ContentResolver cr = getActivity().getContentResolver();
		Cursor query = cr.query(EarthquakesContentProvider.CONTENT_URI, null, where, null, null);
		if(query.getCount()==1) {
			query.moveToFirst();
			double latitude = query.getDouble(query.getColumnIndex(EarthquakesContentProvider.COL_LAT));
			double longitude = query.getDouble(query.getColumnIndex(EarthquakesContentProvider.COL_LONG));
			double magnitude = query.getDouble(query.getColumnIndex(EarthquakesContentProvider.COL_MAG));
			long time = query.getLong(query.getColumnIndex(EarthquakesContentProvider.COL_TIME));
			if(mListener!=null) {
				mListener.onResultFragmentInteraction(latitude, longitude,magnitude,time);
			}
		}
		query.close();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnResultFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		
		mListener = null;
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
	public interface OnResultFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onResultFragmentInteraction(double latitude, double longitude, double magnitude, long time);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		int miniMag = Integer.parseInt(sPref.getString(EarthquakesViewerActivity.MINMAG_KEY, "3"));
		String view_past_where = EarthquakesViewerActivity.getDateSettings(sPref);
		String where=(mID.equals(""))?(EarthquakesContentProvider.COL_PLACE+" LIKE '%"+mQuery.trim()+"%' AND " 
					+ EarthquakesContentProvider.COL_MAG+">="+miniMag
					+ " AND "+view_past_where):(EarthquakesContentProvider.COL_ID+"="+mID);
		String sort_by = sPref.getString(EarthquakesViewerActivity.SORT_BY_KEY, "Date");
		String sortOrder = null;
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
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.earthquake_viewer_actionbar, menu);
		SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
		
		menu.getItem(2).setVisible(false);
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
		case (android.R.id.home):
			Intent intent = new Intent(getActivity(), EarthquakesViewerActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
	}
	return false;
		
	}
}
