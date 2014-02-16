package com.marcochiang.justice.view.settings;

import com.marcochiang.justice.R;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

public class ShaneActivity extends Activity {
	
	private WakeLock mFullWakeLock;

	@Override
	public void onAttachedToWindow() {
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | 
	            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN | 
	            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | 
	            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | 
	            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardLock keyguardLock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
	    keyguardLock.disableKeyguard();
		
		setContentView(R.layout.launcher_activity);
		
		Intent intent = getIntent();
		String packageName = intent.getStringExtra("launch-this");
		if (packageName != null) {
			Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
			startActivity(launch);
		} else {
			finish();
		}

	    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    mFullWakeLock = powerManager.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
	    mFullWakeLock.acquire();
		mFullWakeLock.release();
	}
}
