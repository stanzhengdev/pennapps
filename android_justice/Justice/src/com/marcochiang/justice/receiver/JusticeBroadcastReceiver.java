package com.marcochiang.justice.receiver;

import com.marcochiang.justice.view.settings.SettingsActivity;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

public class JusticeBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "JusticeBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			// LOCK DAT
			Log.i(TAG, "LOCK DAT");
			String pin = PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.PIN, "");
			DevicePolicyManager manager = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
			manager.resetPassword(pin, 0);
		}
	}
}
