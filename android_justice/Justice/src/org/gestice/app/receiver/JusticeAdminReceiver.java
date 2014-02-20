package org.gestice.app.receiver;

import com.marcochiang.justice.R;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class JusticeAdminReceiver extends DeviceAdminReceiver {
	
	public static final String DEVICE_ADMIN_ENABLED = "deviceAdminEnabled";
    
    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
    	return context.getResources().getString(R.string.gestice_admin_receiver_disable_warning);
    }
    
    @Override
    public void onEnabled(Context context, Intent intent) {
    	// Commit to shared prefs
    	SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    	editor.putBoolean(DEVICE_ADMIN_ENABLED, true);
    	editor.commit();
    }
    
    @Override
    public void onDisabled(Context context, Intent intent) {
    	// Commit to shared prefs
    	SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
    	editor.putBoolean(DEVICE_ADMIN_ENABLED, false);
    	editor.commit();
    }
}
