package com.marcochiang.justice.view;

import java.util.ArrayList;

import com.marcochiang.justice.R;
import com.marcochiang.justice.model.GestureCellModel;
import com.marcochiang.justice.service.JusticeService;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class GestureListActivity extends Activity {

	public static final String TAG = "MainActivity";
	private static final String PREFS = "prefs"; 
	
	private ListView mList;
	private LaunchCellAdapter mAdapter;
	private ArrayList<GestureCellModel> mData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture_list_activity);
		Log.d(TAG, "onCreate()");
		
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
	
	// Returns true if there was saved data, false if nothing was there
	public boolean loadGestureCellData() {
		SharedPreferences prefs = getSharedPreferences(PREFS, 0);
		String json = prefs.getString("gestures", null);
		if (json != null) {
			mData = GestureCellModel.arrayFromString(json);
			return true;
		} else {
			return false;
		}
	}
	
	public static class LaunchCellAdapter extends ArrayAdapter<GestureCellModel> {
		
		private Context mContext;
		private int mResource;
		private ArrayList<GestureCellModel> mData = new ArrayList<GestureCellModel>();

		public LaunchCellAdapter(Context context, int resource) {
			super(context, resource);
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
