package com.marcochiang.justice.service;

import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
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

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Learn what this method does and implement it properly
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");

		try {
			// Start the Justice pebble app
			PebbleKit.startAppOnPebble(getApplicationContext(), JUSTICE_APP_UUID);
		} catch (Exception e) {
			Log.e(TAG, "Stuff didn't work...");
			e.printStackTrace();
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand()");

		// Register a data receiver with the Pebble framework
		Log.d(TAG, "Registering data receiver...");
		PebbleKit.registerReceivedDataHandler(getApplicationContext(), 
		new PebbleKit.PebbleDataReceiver(JUSTICE_APP_UUID) {
			@Override
			public void receiveData(Context context, int transactionId, PebbleDictionary data) {
				Log.i(TAG, "Received data from pebble: " + data.toJsonString());
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
