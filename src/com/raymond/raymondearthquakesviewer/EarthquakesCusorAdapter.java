package com.raymond.raymondearthquakesviewer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class EarthquakesCusorAdapter extends SimpleCursorAdapter{

	private LayoutInflater mInflater;
	private Context mContext;
	private int mLayout;
	private Cursor mCursor;
	private String[] mFrom;
	private int[] mTo;
	private int mFlags;
	public EarthquakesCusorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);
		mContext = context;
		mLayout = layout;
		mCursor = c;
		mFrom = from;
		mTo = to;
		mFlags = flags;
		mInflater = LayoutInflater.from(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView placeText = (TextView)view.findViewById(android.R.id.text1);
		TextView summaryText = (TextView)view.findViewById(android.R.id.text2);
		String place = cursor.getString(cursor.getColumnIndex(EarthquakesContentProvider.COL_PLACE));
		long time = cursor.getLong(cursor.getColumnIndex(EarthquakesContentProvider.COL_TIME));
		double magnitude = cursor.getDouble(cursor.getColumnIndex(EarthquakesContentProvider.COL_MAG));
		
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US);
		//sdf.setTimeZone(TimeZone.getDefault());
		String formattedDate = sdf.format(date);
		
		String summary ="Mag: "+magnitude+" Date: "+formattedDate;
		
		placeText.setText(place);
		summaryText.setText(summary);
		placeText.setTypeface(Typeface.SANS_SERIF);
		placeText.setTextSize(20);
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		return mInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
	}
	
	
}
