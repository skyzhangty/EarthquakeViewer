package com.raymond.raymondearthquakesviewer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EarthquakeRefreshableList extends LinearLayout implements OnTouchListener{


	public static final int STATUS_PULL_TO_REFRESH = 0;
	public static final int STATUS_RELEASE_TO_REFRESH = 1;
	public static final int STATUS_REFRESHING = 2;
	public static final int STATUS_REFRESH_DONE = 3;
	
	
	private static final int SCROLL_SPEED = -20;
	//The UI Objects 
	private View mHeader;
	private ListView mListView;
	private TextView mDescription;
	private ProgressBar mProgressBar;
	private ImageView mArrow;
	private MarginLayoutParams mHeaderLayoutParams;
	private int mHideHeaderHeight;
	
	private int mCurrentStatus = STATUS_REFRESH_DONE;
	private int mLastStatus = mCurrentStatus;
	
	private float mYDown;     //The Y value when touching the screen;
	
	private boolean mCanPull;  //Check if we can pull to refresh
	
	private float mTouchSlop;   //The maximum touch movement before it can be viewed as a scrolling action
	
	private boolean mLoadOnce;   //load the header+listview layout only once in onLayout method
	
	private PullToRefreshListener mListener;
	
	public EarthquakeRefreshableList(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHeader = LayoutInflater.from(context).inflate(R.layout.listview_header_layout, null, true);
		mProgressBar = (ProgressBar)mHeader.findViewById(R.id.pulltorefresh_progressbar);
		mDescription = (TextView)mHeader.findViewById(R.id.description_text);
		mArrow = (ImageView)mHeader.findViewById(R.id.arrow_image);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		setOrientation(VERTICAL);
		addView(mHeader,0);
		
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		
		if(changed && !mLoadOnce) {
			//Move the header out of the screen by setting its top margin to the negative value of its height
			mHideHeaderHeight = -mHeader.getHeight();
			
			mHeaderLayoutParams = (MarginLayoutParams) mHeader.getLayoutParams();
			mHeaderLayoutParams.topMargin = mHideHeaderHeight;
			
			mListView = (ListView) getChildAt(1);
			mListView.setOnTouchListener(this);
			mLoadOnce = true;
		}
	}
	public int getHeaderHeight() {
		return mHeader.getHeight();
	}
	private void setCanPull(MotionEvent event) {
		View firstItem = mListView.getChildAt(0);
		if(firstItem!=null) {
			int firstVisiblePos = mListView.getFirstVisiblePosition();
			if(firstVisiblePos == 0 && firstItem.getTop()==0) {
				//if the first item of the list is visible and at the top, we can pull to refresh
				if(mCanPull == false) {
					mYDown = event.getRawY();
				}
				mCanPull = true;
			}
			else {
				mCanPull = false;
			}
		}
		else {    //If the list view is empty, pull to refresh is still allowed
			mCanPull = true;
		}
	}
	
	private void updateHeaderView() {
		if(mLastStatus != mCurrentStatus) {
			if(mCurrentStatus==STATUS_PULL_TO_REFRESH) {
				mDescription.setText(getResources().getString(R.string.pull_to_refresh));
				mArrow.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.INVISIBLE);
				rotateArrow();
			}
			else if(mCurrentStatus == STATUS_RELEASE_TO_REFRESH) {
				mDescription.setText(getResources().getString(R.string.release_to_refresh));
				mArrow.setVisibility(View.VISIBLE);
				mProgressBar.setVisibility(View.INVISIBLE);
				rotateArrow();
			}
			else if(mCurrentStatus == STATUS_REFRESHING) {
				mDescription.setText(getResources().getString(R.string.updating));
				mArrow.setVisibility(View.INVISIBLE);
				mProgressBar.setVisibility(View.VISIBLE);
				mArrow.clearAnimation();
			}
		}
	}
	
	private void rotateArrow() {
		float pivotX = mArrow.getWidth()/2f;
		float pivotY = mArrow.getHeight()/2f;
		float fromDegrees = 0f;
		float toDegrees = 0f;
		if(mCurrentStatus == STATUS_PULL_TO_REFRESH) {
			fromDegrees = 180f;
			toDegrees = 360f;
		}
		else if(mCurrentStatus == STATUS_RELEASE_TO_REFRESH) {
			fromDegrees = 0f;
			toDegrees = 180f;
		}
		RotateAnimation rotateAnimation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
		rotateAnimation.setDuration(100);
		rotateAnimation.setFillAfter(true);
		mArrow.startAnimation(rotateAnimation);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// called when the listview is being touched
		setCanPull(event);
		if(mCanPull) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mYDown = event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:
					float yMove = event.getRawY();
					int move_distance = (int)(yMove-mYDown);
					
					if(move_distance<=0 && mHeaderLayoutParams.topMargin<=mHideHeaderHeight) {
						//scroll up 
						return false;
					}
					if(move_distance<mTouchSlop) {
						//not enough movement to scroll
						return false;
					}
					
					if(mCurrentStatus!=STATUS_REFRESHING) {
						if(mHeaderLayoutParams.topMargin>0) {
							//Now all the header is out, we can release to refresh
							mCurrentStatus = STATUS_RELEASE_TO_REFRESH;
						}
						else {
							//only part of the header is out, keep pulling
							mCurrentStatus = STATUS_PULL_TO_REFRESH;
						}
						mHeaderLayoutParams.topMargin = (move_distance/2)+mHideHeaderHeight;
						mHeader.setLayoutParams(mHeaderLayoutParams);
					}
					break;
				case MotionEvent.ACTION_UP:
				default:
					if(mCurrentStatus == STATUS_RELEASE_TO_REFRESH) {
						new RefreshingTask().execute();
					}
					else if(mCurrentStatus == STATUS_PULL_TO_REFRESH) {
						new HideHeaderTask().execute();
					}
					break;
				
			}   //end of switch
			
			if(mCurrentStatus == STATUS_PULL_TO_REFRESH || mCurrentStatus == STATUS_RELEASE_TO_REFRESH) {
				updateHeaderView();   //Change the arrow direction, the description text and showing(hiding) the processing bar
				
				mListView.setPressed(false);
				mListView.setFocusable(false);
				mListView.setFocusableInTouchMode(false);
				mLastStatus = mCurrentStatus;
				
				return true;
				
			}
		}
		return false;
	}
	
	private void sleep(int mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void startRefreshing() {
		mCurrentStatus = STATUS_REFRESHING;
		new RefreshingTask().execute();
	}
	
	public void finishRefreshing() {
		mCurrentStatus = STATUS_REFRESH_DONE;
		new HideHeaderTask().execute();
		
	}
	
	public void setOnRefreshListener(PullToRefreshListener listener) {
		mListener = listener;
	}
	class RefreshingTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			int topMargin = mHeaderLayoutParams.topMargin;
			while(true) {
				topMargin += SCROLL_SPEED;
				if(topMargin<=0) {
					topMargin=0;
					break;
				}
				publishProgress(topMargin);
				sleep(10);
			}
			mCurrentStatus = STATUS_REFRESHING;
			publishProgress(0);
			//Do the refresh here
			if(mListener!=null) {
				mListener.onRefresh();
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			updateHeaderView();
			mHeaderLayoutParams.topMargin=values[0];
			mHeader.setLayoutParams(mHeaderLayoutParams);
		}
		
	}
	
	class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			int topMargin = mHeaderLayoutParams.topMargin;
			while(true) {
				topMargin += SCROLL_SPEED;
				if(topMargin<=mHideHeaderHeight) {
					topMargin=mHideHeaderHeight;
					break;
				}
				publishProgress(topMargin);
				sleep(10);
			}
			return topMargin;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			mHeaderLayoutParams.topMargin=values[0];
			mHeader.setLayoutParams(mHeaderLayoutParams);
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			mHeaderLayoutParams.topMargin=result;
			mHeader.setLayoutParams(mHeaderLayoutParams);
			mCurrentStatus=STATUS_REFRESH_DONE;
		}
		
	}
	
	public interface PullToRefreshListener {
		void onRefresh();
	}
}
