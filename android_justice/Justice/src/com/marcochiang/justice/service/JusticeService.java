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
	private float mLastX,mLastY,mLastZ = 0;
	private float threshold= (float) .5; //margin of error
	private float NOISE = (float) 3+threshold; 
	private float NOISEZ = 9+threshold;
	private float mAccel; // acceleration apart from gravity
	private float mAccelCurrent; // current acceleration including gravity
	private float mAccelLast; // last acceleration including gravity
	private boolean initalized = false;
    private String current = null;
    private String[] gestureKey = {"knock", "shakeX", "shakeY"};
    
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
			//Accelerometer
		    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		    mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 4000);
		    mAccel = 0.00f;
		    mAccelCurrent = SensorManager.GRAVITY_EARTH;
		    mAccelLast = SensorManager.GRAVITY_EARTH;
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
	      /*
	       * Want Acceleration? 
	      mAccelLast = mAccelCurrent;
	      mAccelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
	      float delta = mAccelCurrent - mAccelLast;
	      mAccel = mAccel * 0.9f + delta; // perform low-cut filter*/
	       //prints a load of shit out. Log.e(TAG, ""+mAccel);
	      // the maximum 
	      if ((abs(x)<NOISE)|| (abs(y)<NOISE)|| (abs(z)>NOISEZ) ){
	        if (!initalized) {
	            mLastX = x;
	            mLastY = y;
	            mLastZ = z;
	            initalized = true;}
	        else{
	            float deltaX = Math.abs(mLastX - x);
	            float deltaY = Math.abs(mLastY - y);
	            float deltaZ = Math.abs(mLastZ - z);
	            if (deltaX < NOISE) deltaX = (float)0.0;
	            if (deltaY < NOISE) deltaY = (float)0.0;
	            if (deltaZ < NOISE) deltaZ = (float)0.0;
	            mLastX = x;
	            mLastY = y;
	            mLastZ = z;	        	
	            /**
	             * Do movement calculation of current gesture
	             * 
	             */
	            /***
	             * The X,Y,Z
	             * compare lastGesture to Current gesture if correct set 
	             * public String[] gesture = {"knock", "shakeX", "shakeY"};
	             */
	            /** set the arrows only if above threshold**/
	            Log.e(TAG, x+","+y+","+z);	            
	            if ((deltaX > deltaY) && (mLastX>threshold)) {
	                //Do Action X-Axis
	                current = gestureKey[1];//   
	                
	            } else if ((deltaY > deltaX) && (mLastY>threshold)) {
	                //Do Action Y-Axis
	                current = gestureKey[2];//     
	            } else if (abs(mLastZ) >NOISEZ) {
	               //Do Action Z-AXIS
	                current =gestureKey[0];//   
	            }
	            Log.e(TAG, current);
	        }//end of else
	      }//end of threshold check
	    }//end of method 

	    private float abs(float x) {
			// TODO Auto-generated method stub
			return 0;
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
	    }
	  };
	  
	  
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
