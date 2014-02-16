package com.marcochiang.justice.service;

import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

@SuppressWarnings("deprecation")
public class JusticeService extends Service {
	
	public static final String TAG = "JusticeService";
	public static final UUID JUSTICE_APP_UUID = UUID.fromString("259047ec-66dc-4f44-b3b5-1d1477fc7a90");

	private WakeLock mWakeLock;
	private SensorManager mSensorManager;

	public final float timeout = 3000; // 3 seconds
	private final float threshold = 6.0f; // how far do we have to move to count as a nice gesture?

	private boolean needsInit = true;
	private float minX, maxX, minY, maxY, minZ, maxZ;
	private long startTime;
	
	// This is the all-important current axis
	public String axis;
	public long axisTime;
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Learn what this method does and implement it properly
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		try {
			// Start the Justice pebble app
			PebbleKit.startAppOnPebble(getApplicationContext(), JUSTICE_APP_UUID);

			// Initialize the accelerometer
		    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 2000);

		} catch (Exception e) {
			Log.e(TAG, "Stuff didn't work...");
			e.printStackTrace();
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
				
				// Save start time
				startTime = System.currentTimeMillis();
				
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
					Log.i(TAG, "android x shake");
					axis = "x";
					success = true;
				} else if (maxY - minY > threshold) {
					Log.i(TAG, "android y shake");
					axis = "y";
					success = true;
				} else if (maxZ - minZ > threshold) {
					Log.i(TAG, "android z shake");
					axis = "z";
					success = true;
				}
				
				if (success) {
					axisTime = System.currentTimeMillis();
				}
				
				if (success || System.currentTimeMillis() - startTime < timeout) {
					// Reset initialization and start time
					startTime = -1;
					needsInit = true;
				}
			}
	    }

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
	  
	  
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");

		// Register a data receiver with the Pebble framework
		final JusticeService service = this;
		PebbleKit.registerReceivedDataHandler(getApplicationContext(), 
		new PebbleKit.PebbleDataReceiver(JUSTICE_APP_UUID) {
			@Override
			public void receiveData(Context context, int transactionId, PebbleDictionary data) {
				try {
					JSONArray array = new JSONArray(data.toJsonString());
					JSONObject obj = (JSONObject)array.get(0);
					
					String axis = obj.getString("value");
					
					Log.i(TAG, "pebble axis gesture: " + axis);
					Log.i(TAG, "android axis gesture: " + axis);
					if (axis.equals(service.axis) && (System.currentTimeMillis() - service.axisTime) < service.timeout) {
						Log.e(TAG, "match!");
					}

				} catch (Exception e) {
					Log.e(TAG, "JSON error (from Pebble)");
					e.printStackTrace();
				}
			}
		});
		
		// This tells the Android framework to continue to run this service until
		// it is explicitly stopped.
		return START_STICKY;
	}
	
	public void wakeDevice() {
	    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    mWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
	    mWakeLock.acquire();

	    KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
	    keyguardLock.disableKeyguard();
	    new Handler().post(new Runnable() {
	        public void run(){
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
	        	
	        	new Handler().post(new Runnable() {
	        		public void run() {
	        			windowManager.removeView(view);
	        		}
	        	});
	        	
	        	mWakeLock.release();
	        }
	    });
	}
}
