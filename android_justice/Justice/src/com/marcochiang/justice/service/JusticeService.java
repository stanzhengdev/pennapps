package com.marcochiang.justice.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.marcochiang.justice.model.GestureCellModel;
import com.marcochiang.justice.view.settings.JusticeAdminReceiver;
import com.marcochiang.justice.view.settings.JusticeBroadcastReceiver;
import com.marcochiang.justice.view.settings.SettingsActivity;
import com.marcochiang.justice.view.settings.ShaneActivity;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.LauncherActivity;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Handler;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

@SuppressWarnings("deprecation")
public class JusticeService extends Service {
	
	public static final String TAG = "JusticeService";
	public static final UUID JUSTICE_APP_UUID = UUID.fromString("259047ec-66dc-4f44-b3b5-1d1477fc7a90");

	private WakeLock mFullWakeLock;
	private WakeLock mPartialWakeLock;
	private SensorManager mSensorManager;

	public final float timeout = 3000; // 3 seconds
	private final float threshold = 7.0f; // how far do we have to move to count as a full gesture?

	private boolean needsInit = true;
	private float minX, maxX, minY, maxY, minZ, maxZ;
	
	private String pebbleAxis;
	private Government axisGovernment = new Government(10); // the android axis is represented by a government-like organization,
	                                                        // whereby the majority "party" of the last 10 "elected" gestures count as
	                                                        // the true gesture

	private long androidAxisTime; // holds the time that our axis was recorded
	private long pebbleAxisTime; // holds the time we received our pebble update

	// axes are invalidated when a match is confirmed
	private boolean validPebbleAxis;
	private boolean validAndroidAxis;
	
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Learn what this method does and implement it properly
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		try {

			// Initialize the accelerometer
		    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 2000);

			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mPartialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "never gonna give you up, never gonna let you go");
			mPartialWakeLock.acquire();
		
			// Start a broadcast receiver for locking the screen when it goes off
			registerReceiver(new JusticeBroadcastReceiver(), new IntentFilter(Intent.ACTION_SCREEN_OFF));

		} catch (Exception e) {
			Log.e(TAG, "Stuff didn't work...");
			e.printStackTrace();
		}
	}
	
	private static class Government {
		int mSize;
		Queue<String> mCareers;
		Map<String, Integer> mRepresentation;
		
		public Government(int size) {
			mCareers = new LinkedList<String>();
			mRepresentation = new HashMap<String, Integer>();
			mRepresentation.put("x", 0);
			mRepresentation.put("y", 0);
			mRepresentation.put("z", 0);
			this.mSize = size;
		}
		
		public void elect(String axis) {
			mCareers.add(axis);
			mRepresentation.put(axis, mRepresentation.get(axis) + 1);

			if (mCareers.size() > mSize) {
				String retired = mCareers.remove();
				mRepresentation.put(retired, mRepresentation.get(retired) - 1);
			}
		}
		
		public String getMajorityParty() {
			int x = mRepresentation.get("x");
			int y = mRepresentation.get("y");
			int z = mRepresentation.get("z");

			// break ties arbitrarily favoring z, then y, then x
			if (z >= x && z >= y) {
				return "z";
			} else if (y >= x && y >= z) {
				return "y";
			} else {
				return "x";
			}
		}
		
		public void revolution() {
			mRepresentation.put("x", 0);
			mRepresentation.put("y", 0);
			mRepresentation.put("z", 0);
			mCareers.clear();
		}
	}

	private final SensorEventListener mSensorListener = new SensorEventListener() {

		public void onSensorChanged(SensorEvent se) {
			float x = se.values[0];
			float y = se.values[1];
			float z = se.values[2];
			
			if (needsInit) {
				// Initialize the min/max values
				minX = maxX = x;
				minY = maxY = y;
				minZ = maxZ = z;
				
				needsInit = false;

			} else {

				// Adjust the min/max values
				if 		(x > maxX) maxX = x;
				else if (x < minX) minX = x;
				
				if 		(y > maxY) maxY = y;
				else if (y < minY) minY = y;
				
				if 		(z > maxZ) maxZ = z;
				else if (z < minZ) minZ = z;
				
				// Check if any of them exceed the threshold
				boolean success = false;
				if (maxX - minX > threshold) {
					Log.v(TAG, "android x shake");
					setAndroidAxis("x");
					success = true;

				} else if (maxY - minY > threshold) {
					Log.v(TAG, "android y shake");
					setAndroidAxis("y");
					success = true;

				} else if (maxZ - minZ > threshold) {
					Log.v(TAG, "android z shake");
					setAndroidAxis("z");
					success = true;
				}
				
				if (success) {
					// Save the current system time and reset initialization
					performActionIfValidAxes();
					needsInit = true;
				}
			}
	    }

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
	  
	  
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");

		// Register a data receiver with the Pebble framework
		PebbleKit.registerReceivedDataHandler(getApplicationContext(), 
		new PebbleKit.PebbleDataReceiver(JUSTICE_APP_UUID) {
			@Override
			public void receiveData(Context context, int transactionId, PebbleDictionary data) {
				try {
					// Get the pebble axis from the message and set it in the service
					JSONArray array = new JSONArray(data.toJsonString());
					JSONObject obj = (JSONObject)array.get(0);
					String axis = obj.getString("value");
					setPebbleAxis(axis);
					
					performActionIfValidAxes();

				} catch (Exception e) {
					Log.e(TAG, "JSON error (in Pebble message)");
					e.printStackTrace();
				}
			}
		});
		
		// This tells the Android framework to continue to run this service until
		// it is explicitly stopped.
		return START_STICKY;
	}
	
	public void setPebbleAxis(String axis) {
		pebbleAxis = axis;
		pebbleAxisTime = System.currentTimeMillis();
		validPebbleAxis = true;
	}
	
	public void setAndroidAxis(String axis) {
		axisGovernment.elect(axis);
		androidAxisTime = System.currentTimeMillis();
		validAndroidAxis = true;
	}
	
	public void performActionIfValidAxes() {

		Log.d(TAG, "android axis: " + axisGovernment.getMajorityParty() + " (" + (System.currentTimeMillis() - androidAxisTime) + "ms ago)");
		Log.d(TAG, "pebble axis: " + pebbleAxis + " (" + (System.currentTimeMillis() - pebbleAxisTime) + "ms ago)");

		// If the axes match and didn't come in too far apart, it's a valid match!
		if (axisGovernment.getMajorityParty().equals(pebbleAxis) && 
				validAxes() && 
				Math.abs(androidAxisTime - pebbleAxisTime) < timeout) {

			Log.e(TAG, "match!");
			wakeDeviceToActivity(axisGovernment.getMajorityParty());
			invalidateAxes();
			
			// Tell the pebble that it was a successful launch
			PebbleDictionary data = new PebbleDictionary();
			data.addString(100, "matched");
			data.addString(0, "success");
			PebbleKit.sendDataToPebble(getApplicationContext(), JUSTICE_APP_UUID, data);
		}
	}
	
	public boolean validAxes() {
		return validAndroidAxis && validPebbleAxis;
	}
	
	public void invalidateAxes() {
		validAndroidAxis = false;
		validPebbleAxis = false;
		
		axisGovernment.revolution();
	}
	
	public void wakeDeviceToActivity(final String gesture) {

		// Remove password
		DevicePolicyManager devicePolicyManager = (DevicePolicyManager)getApplicationContext().getSystemService(DEVICE_POLICY_SERVICE);
		devicePolicyManager.resetPassword("", 0);
		
		// Load the gesture cell data
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String json = prefs.getString("gestures", null);
		Log.d(TAG, "loaded json: " + json);
		ArrayList<GestureCellModel> data = null;
		if (json != null) {
			data = GestureCellModel.arrayFromString(json);
		}
		
		String packageName;
		if (gesture.equals("x")) {
			packageName = data.get(0).packageName;
		} else if (gesture.equals("y")) {
			packageName = data.get(1).packageName;
		} else {
			packageName = data.get(2).packageName;
		}
		Log.i(TAG, "launching " + packageName);

		Intent intent = new Intent(this, ShaneActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
		if (packageName != null) {
			intent.putExtra("launch-this", packageName);
		}
		startActivity(intent);
	}

	/*
	public void wakeDevice() {
		// Remove password
		DevicePolicyManager devicePolicyManager = (DevicePolicyManager)getApplicationContext().getSystemService(DEVICE_POLICY_SERVICE);
		devicePolicyManager.resetPassword("", 0);

	    KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardLock keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
	    keyguardLock.disableKeyguard();

	    final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {

			    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			    mFullWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
			    mFullWakeLock.acquire();
				
				handler.postDelayed(new Runnable() {
					public void run() {
			        	// Builds LayoutParams for new view
			        	int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
			                      | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
			                      | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
			                      | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
			        	WindowManager.LayoutParams params = new WindowManager.LayoutParams(0, 0);
			        	params.flags = flags;
			        	params.x = 0;
			        	params.y = 0;
			        	params.width = 0;
			        	params.height = 0;
			        	params.format = PixelFormat.TRANSLUCENT;
			        	params.windowAnimations = 0;
			        	
			        	// Get window manager
			        	final WindowManager windowManager = (WindowManager)
			        			getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
			        	
			        	// Build a simple transparent view
			        	final View view = new View(getApplicationContext());
			        	view.setBackgroundColor(0x00FFFFFF); // transparent
			        	
			        	// Put the view in the window
			        	windowManager.addView(view, params);
			        	
			        	handler.postDelayed(new Runnable() {
			        		public void run() {
			        			windowManager.removeView(view);
		
			        			String pin = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(SettingsActivity.PIN, null);
								DevicePolicyManager devicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
								devicePolicyManager.resetPassword(pin, 0);
		
					        	mFullWakeLock.release();
			        		}
			        	}, 0);
					}
				}, 0);
			}
		}, 0);
	}
	*/
}

