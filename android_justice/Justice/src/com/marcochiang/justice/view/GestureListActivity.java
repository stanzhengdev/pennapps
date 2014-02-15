package com.marcochiang.justice.view;

import java.util.ArrayList;

import com.marcochiang.justice.R;
import com.marcochiang.justice.model.GestureCellModel;
import com.marcochiang.justice.service.JusticeService;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class GestureListActivity extends Activity {

	public static final String TAG = "MainActivity";
	
	private ListView mList;
	private GestureCellAdapter mAdapter;
	private ArrayList<GestureCellModel> mData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture_list_activity);
		
		// Get references to UI components
		mList = (ListView)findViewById(android.R.id.list);
		
		// Start a service to listen for Pebble data
		Intent serviceIntent = new Intent(this, JusticeService.class);
		startService(serviceIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// We load the data in onResume rather than in onCreate because onResume is called each time the activity
		// is shown again. Most importantly, when we arrive back at this list after having edited an item, this method
		// will be called to load the changes.

		if (!loadGestureCellData()) {
			// There is no data saved, so create some here and commit it
			mData = new ArrayList<GestureCellModel>();
			
			String[] gestures = { "X-Axis Shake", "Y-Axis Shake", "Z-Axis Shake" };
			for (String gesture : gestures) {
				GestureCellModel model = new GestureCellModel();
				model.gestureName = gesture;
				model.iconResource = 0; // no icon (yet)
				model.name = "Screen Unlock"; // default to a plain screen unlock
				model.packageName = "";
				mData.add(model);
			}
			saveGestureCellData();
		}

		// Initialize the adapter and hook it up to the list
		mAdapter = new GestureCellAdapter(this, R.layout.gesture_cell);
		mAdapter.setData(mData);
		mList.setAdapter(mAdapter);
		
		// Set up click handler
		final Activity activity = this;
		mList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Launch a GestureEditActivity to select a new app for this gesture
				Intent intent = new Intent(activity, GestureEditActivity.class);

				// Tell the new activity what gesture position to edit
				intent.putExtra(GestureEditActivity.POSITION, position);
				startActivity(intent);
			}
		});
	}
	
	// Returns true if there was saved data, false if nothing was there
	public boolean loadGestureCellData() {
		// Get a string from SharedPreferences and convert it to a real Java object array
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String json = prefs.getString("gestures", null);
		Log.d(TAG, "loaded json: " + json);
		if (json != null) {
			mData = GestureCellModel.arrayFromString(json);
			return true;
		} else {
			return false;
		}
	}
	
	public void saveGestureCellData() {
		// Convert our data array to a JSON Array string and save it in SharedPreferences
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		String json = GestureCellModel.toJSONArrayString(mData);
		Log.d(TAG, "saving json: " + json);
		editor.putString("gestures", json);
		editor.commit();
	}
	
	public static class GestureCellAdapter extends ArrayAdapter<GestureCellModel> {
		
		private Context mContext;
		private int mResource;
		private ArrayList<GestureCellModel> mData = new ArrayList<GestureCellModel>();

		public GestureCellAdapter(Context context, int resource) {
			super(context, resource);
			mContext = context;
			mResource = resource;
		}
		
		public void setData(ArrayList<GestureCellModel> data) {
			mData = data;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(mContext);
				convertView = inflater.inflate(mResource, parent, false);
			}
			
			// Get UI components
			TextView gestureName = (TextView)convertView.findViewById(R.id.gestureName);
			ImageView appIcon = (ImageView)convertView.findViewById(R.id.appIcon);
			TextView appName = (TextView)convertView.findViewById(R.id.appName);

			// Get the data for the item and set up the view
			GestureCellModel model = mData.get(position);
			
			gestureName.setText(model.gestureName);
			appIcon.setImageResource(model.iconResource);
			appName.setText(model.name);

			return convertView;
		}
		
		@Override
		public int getCount() {
			return mData.size();
		}
	}
}
