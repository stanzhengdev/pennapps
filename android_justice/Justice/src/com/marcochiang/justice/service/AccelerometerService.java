package com.marcochiang.justice.service;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;



public class AccelerometerService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private final IBinder mBinder = new AccBinder();
    private static float dataFromAccelerometer = 0;

	/**
	 * Accelerometer info
	**/
   private float mLastX, mLastY, mLastZ;
   private boolean mInitialized;
   public String[] name = {"x_move", "y_move", "z_move"};
   public ArrayList<String> history = new ArrayList<String>();
   public String[] gestureKey = {"knock", "shakeX", "shakeY"};
   public String current = null;
   public String gesture = "initalized";// two movements = one gesture 
   private Sensor mAccelerometer;
   private final float NOISE = (float) 2.0;
   private final float threshold = (float) 25.0; //marcos thresholds
   
    
    
    public float getDataFromAccelerometr() {
        return dataFromAccelerometer;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Service is created", Toast.LENGTH_SHORT).show();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//start a service intent.
    } 

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean sr = sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL); // SENSOR_DELAY_GAME
        Toast.makeText(this, "Service is run " + sr, Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service is stopped", Toast.LENGTH_SHORT).show();
        sensorManager.unregisterListener(this);
        dataFromAccelerometer = 0;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
    	//sweet on change fires an event. 
    	//we need to compare this shit
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;

        
        //every time you call this, you get the accelerometer. 
        //you need to compare this shit 
        
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

        dataFromAccelerometer = accelationSquareRoot;
    }

    public class AccBinder extends Binder {
        AccelerometerService getService() {
            return AccelerometerService.this;
        }
    }

}
