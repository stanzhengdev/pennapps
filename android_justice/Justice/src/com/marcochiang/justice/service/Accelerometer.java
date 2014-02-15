package com.marcochiang.justice.service;

import java.util.ArrayList;
import java.util.UUID;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.marcochiang.justice.JusticeApplication;

public class Accelerometer  extends Service implements SensorEventListener {
	
	public static final String TAG = "@@@@@@";
	public static final UUID JUSTICE_APP_UUID = UUID.fromString("259047ec-66dc-4f44-b3b5-1d1477fc7a90");
	private static final Context JusticeApplication = null;

	

   //private final IBinder mBinder = new AccBinder();
    
	/**
	 * Accelerometer info
	 */
    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;

    private SensorManager mSensorManager;
    public String[] name = {"x_move", "y_move", "z_move"};
    public ArrayList<String> history = new ArrayList<String>();
    public String[] gestureKey = {"knock", "shakeX", "shakeY"};
    public String current = null;
    public String gesture = "initalized";// two movements = one gesture 
    private Sensor mAccelerometer;
    private final float NOISE = (float) 2.0;
    private final float threshold = (float) 25.0; //marcos thresholds
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Learn what this method does and implement it properly
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");

		try { startService(new Intent(Accelerometer.JusticeApplication, Accelerometer.class));
	        mInitialized = false;
	        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	        onStartCommand();
		} catch (Exception e) {
			Log.e(TAG, "Stuff didn't work...");
			e.printStackTrace();
		}
	}	
	
	protected void onResume() {
        //super.onResume();
		Log.d(TAG, "onResume()");
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
		Log.d(TAG, "onSensorChanged");
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        } else {
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
            /** set the arrows**/
            if (deltaX > deltaY) {
                current = gestureKey[1];//   
                
            } else if (deltaY > deltaX) {
                current = gestureKey[2];//     
            } else {
                current =gestureKey[0];//   
            }
            
            
            //logs all your past gestures in a stack? 
            history.add(current) ;
            //debugging
            for (String motion : history){
                Log.i("Last Gesture: ", motion);
            }
        }
    }	
	public int onStartCommand() {
		
		Log.d(TAG, "onStartCommand()");

		// Register a data receiver with the Pebble framework
		//holy shit thats a callback TODO handle this correctly 
		Log.d(TAG, "Registering data receiver...");
		
		try {
	        mInitialized = false;
	        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		} catch (Exception e) {
			Log.e(TAG, "Stuff didn't work...");
			e.printStackTrace();
		}
		
		// This tells the Android framework to continue to run this service until
		// it is explicitly stopped.
		return START_STICKY;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}
