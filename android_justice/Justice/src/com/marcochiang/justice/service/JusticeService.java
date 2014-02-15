package com.marcochiang.justice.service;

import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class JusticeService extends Service {
	
	public static final String TAG = "JusticeService";
	public static final UUID JUSTICE_APP_UUID = UUID.fromString("259047ec-66dc-4f44-b3b5-1d1477fc7a90");

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
}
