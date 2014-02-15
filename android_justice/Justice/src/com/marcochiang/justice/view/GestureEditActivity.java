package com.marcochiang.justice.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

import com.marcochiang.justice.R;
import com.marcochiang.justice.model.GestureCellModel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class GestureEditActivity extends Activity {

	public static final String TAG = "GestureEditActivity";
	public static final String POSITION = "position";


	
	// Holds the position of this specific model in the data list
	private int mPosition;
	private GestureCellModel mModel;
	// TODO: should keep a static reference to this data somewhere.. serializing might be slow
	private ArrayList<GestureCellModel> mGesturesData;
	
	// List components
	private ListView mList;
	private ApplicationCellAdapter mAdapter;
	private List<ResolveInfo> mApplicationData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture_edit_activity);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		///////////////////////////////////////////////////
		// Initialize gesture data
		///////////////////////////////////////////////////
		mPosition = getIntent().getIntExtra(POSITION, 0);

		// Get the data from SharedPreferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String json = prefs.getString("gestures", null);
		mGesturesData = GestureCellModel.arrayFromString(json);
		
		if (mGesturesData != null) {
			mModel = mGesturesData.get(mPosition);
		}
		
		// Set the title with the name of the gesture
		setTitle("Editing " + mModel.gestureName);
		
		///////////////////////////////////////////////////
		// Initialize application data and list
		///////////////////////////////////////////////////
		mList = (ListView)findViewById(android.R.id.list);
		mAdapter = new ApplicationCellAdapter(this, R.layout.appliction_cell);
		
		// Get all launchable applications
		final PackageManager manager = getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		mApplicationData = manager.queryIntentActivities(mainIntent, 0);
		Collections.sort(mApplicationData, new Comparator<ResolveInfo>() {
			@Override
			public int compare(ResolveInfo a, ResolveInfo b) {
				String aName = (String)a.activityInfo.applicationInfo.loadLabel(manager);
				String bName = (String)b.activityInfo.applicationInfo.loadLabel(manager);
				return aName.compareTo(bName);
			}
		});
		mAdapter.setData(mApplicationData);
		mList.setAdapter(mAdapter);
		
		final Activity activity = this;
		mList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Save the information in the model
				ResolveInfo applicationData = mApplicationData.get(position);
				mModel.packageName = applicationData.activityInfo.packageName;
				mModel.name = (String) applicationData.activityInfo.loadLabel(getPackageManager());
				mGesturesData.set(mPosition, mModel);
				
				// Save the data to SharedPreferences
				SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
				editor.putString("gestures", GestureCellModel.toJSONArrayString(mGesturesData));
				editor.commit();
				
				// Finish this activity (i.e. return to main list)
				finish();
				overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_left);
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			finish();
			overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_left);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static class ApplicationCellAdapter extends ArrayAdapter<ResolveInfo> {
		
		private Context mContext;
		private int mResource;
		private List<ResolveInfo> mData = new ArrayList<ResolveInfo>();

		public ApplicationCellAdapter(Context context, int resource) {
			super(context, resource);
			mContext = context;
			mResource = resource;
		}
		
		public void setData(List<ResolveInfo> data) {
			if (data != null) {
				mData = data;
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(mContext);
				convertView = inflater.inflate(mResource, parent, false);
			}
			
			// Get UI Components
			ImageView icon = (ImageView)convertView.findViewById(R.id.appIcon);
			TextView name = (TextView)convertView.findViewById(R.id.appName);
			
			// Set data
			ResolveInfo info = mData.get(position);
			PackageManager packageManager = mContext.getPackageManager();
			icon.setImageDrawable(info.activityInfo.loadIcon(packageManager));
			name.setText(info.activityInfo.applicationInfo.loadLabel(packageManager));
			
			return convertView;
		}
		
		@Override
		public int getCount() {
			return mData.size();
		}
	}
}
