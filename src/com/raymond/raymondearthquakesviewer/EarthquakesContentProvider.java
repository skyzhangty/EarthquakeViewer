package com.raymond.raymondearthquakesviewer;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class EarthquakesContentProvider extends ContentProvider {
	//Publish the content provider's URI
	public static final Uri CONTENT_URI = Uri.parse("content://com.raymond.EarthquakesContentProvider/earthquakes"); 
	//Columns in the database
	public static final String COL_ID = "_id";
	public static final String COL_PLACE = "place";
	public static final String COL_TIME = "time";
	public static final String COL_LAT = "latitude";
	public static final String COL_LONG = "longitude";
	public static final String COL_MAG = "magnitude";
	private EarthquakeSQLiteHelper earthquakeSQLiteHelper;
	private static final HashMap<String, String> SEARCH_PROJECTION_MAP=buildColumnMap();
	private static final int SINGLE_ROW = 1;
	private static final int ALLROWS = 2;
	private static final int SEARCH = 3;
	
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.raymond.EarthquakesContentProvider", "earthquakes/#", SINGLE_ROW);
		uriMatcher.addURI("com.raymond.EarthquakesContentProvider", "earthquakes", ALLROWS);
		
		uriMatcher.addURI("com.raymond.EarthquakesContentProvider", SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
		uriMatcher.addURI("com.raymond.EarthquakesContentProvider", SearchManager.SUGGEST_URI_PATH_QUERY+"/*", SEARCH);
		uriMatcher.addURI("com.raymond.EarthquakesContentProvider", SearchManager.SUGGEST_URI_PATH_SHORTCUT, SEARCH);
		uriMatcher.addURI("com.raymond.EarthquakesContentProvider", SearchManager.SUGGEST_URI_PATH_SHORTCUT+"/*", SEARCH);
	}
	
	public EarthquakesContentProvider() {
		
	}

	private static HashMap<String, String> buildColumnMap() {
		// TODO Auto-generated method stub
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(BaseColumns._ID, COL_ID + " AS "+BaseColumns._ID);
		map.put(SearchManager.SUGGEST_COLUMN_TEXT_1, COL_PLACE + " AS "+SearchManager.SUGGEST_COLUMN_TEXT_1);
		map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA, COL_ID + " AS "+SearchManager.SUGGEST_COLUMN_INTENT_DATA);
		return map;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Implement this to handle requests to delete one or more rows.
		SQLiteDatabase db = earthquakeSQLiteHelper.getWritableDatabase();
		if(uriMatcher.match(uri)==SINGLE_ROW) {
			String rowID = uri.getPathSegments().get(1);
			selection = COL_ID + "=" +rowID
					+ ((!TextUtils.isEmpty(selection))?" AND("+selection+")":"");
		}
		int deletedNum = db.delete(EarthquakeSQLiteHelper.DB_TABLE, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return deletedNum;
	}
	
	@Override
	public String getType(Uri uri) {
		// TODO: Implement this to handle requests for the MIME type of the data
		// at the given URI.
		switch(uriMatcher.match(uri)) {
			case SINGLE_ROW: return "vnd.android.cursor.item/vnd.raymond.earthquake";
			case ALLROWS: return "vnd.android.cursor.dir/vnd.raymong.earthquake";
			case SEARCH: return SearchManager.SUGGEST_MIME_TYPE;
			default: throw new IllegalArgumentException("URI does not match");
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = earthquakeSQLiteHelper.getWritableDatabase();
		long rowID = db.insert(EarthquakeSQLiteHelper.DB_TABLE, null, values);
		if(rowID>-1) {
			Uri newUri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(newUri, null);
			return newUri;
		}
		throw new SQLiteException("Failed to insert a new row");
	}

	@Override
	public boolean onCreate() {
		// TODO: Implement this to initialize your content provider on startup.
		earthquakeSQLiteHelper = new EarthquakeSQLiteHelper(getContext(), EarthquakeSQLiteHelper.DB_NAME, null
								, EarthquakeSQLiteHelper.DB_VERSION);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO: Implement this to handle query requests from clients.
		SQLiteDatabase db = earthquakeSQLiteHelper.getReadableDatabase();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(EarthquakeSQLiteHelper.DB_TABLE);
		
		//If only one row
		if(uriMatcher.match(uri)==SINGLE_ROW) {
			String rowID = uri.getPathSegments().get(1);
			queryBuilder.appendWhere(COL_ID + "=" + rowID);
		}
		else if(uriMatcher.match(uri)==SEARCH) {
			queryBuilder.setProjectionMap(SEARCH_PROJECTION_MAP);
			SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(getContext());
			String miniManaitudeWhere = COL_MAG+">="+sPref.getString(EarthquakesViewerActivity.MINMAG_KEY, "3");
			queryBuilder.appendWhere(COL_PLACE+ " LIKE \"%"+uri.getPathSegments().get(1)+"%\"" + " AND "+EarthquakesViewerActivity.getDateSettings(sPref)
					+" AND "+miniManaitudeWhere);
		}
		if(TextUtils.isEmpty(sortOrder)) {
			sortOrder = COL_TIME + " DESC";
		}
		
		Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO: Implement this to handle requests to update one or more rows.
		SQLiteDatabase db = earthquakeSQLiteHelper.getWritableDatabase();
		if(uriMatcher.match(uri)==SINGLE_ROW) {
			String rowID = uri.getPathSegments().get(1);
			selection = COL_ID + "=" +rowID +
					((!TextUtils.isEmpty(selection))? " AND ("+selection+")":"");
		}
		int updateRow = db.update(EarthquakeSQLiteHelper.DB_TABLE, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return updateRow;
	}
	
	private class EarthquakeSQLiteHelper extends SQLiteOpenHelper {

		private static final String DB_NAME = "earthquakes.db";
		private static final int DB_VERSION = 1;
		private static final String DB_TABLE = "earthquakes";
		
		private static final String TABLE_CREATE="CREATE TABLE " + DB_TABLE 
				+ " ("+ COL_ID + " integer primary key autoincrement, "
				+ COL_PLACE +" TEXT, "
				+ COL_TIME + " BIGINT, "
				+ COL_LAT + " FLOAT, "
				+ COL_LONG + " FLOAT, "
				+ COL_MAG + " FLOAT);";
		
		public EarthquakeSQLiteHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			Log.v("FUCK","FUCK");
			db.execSQL(TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS "+DB_TABLE);
			onCreate(db);
		}
		
	}
}
