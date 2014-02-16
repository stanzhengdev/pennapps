package com.marcochiang.justice.view.settings;

import com.marcochiang.justice.R;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePinActivity extends Activity {
	
	private EditText enterText;
	private EditText confirmText;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_pin_activity);
		setTitle(getResources().getString(R.string.change_pin_activity_title));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		enterText = (EditText)findViewById(R.id.enterPin);
		confirmText = (EditText)findViewById(R.id.confirmPin);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.change_pin_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home:
			Toast.makeText(this, getResources().getString(R.string.change_pin_cancel), Toast.LENGTH_SHORT).show();
			finish();
			overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_left);
			return true;
		case R.id.save:
			handleNewPin();
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		Toast.makeText(this, getResources().getString(R.string.change_pin_cancel), Toast.LENGTH_SHORT).show();
		super.onBackPressed();
		overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_left);
	}
	
	private void handleNewPin() {
		String enter = enterText.getText().toString();
		String confirm = confirmText.getText().toString();
		
		boolean matches = enter.equals(confirm);
		boolean numeric = enter.matches("[0-9]+");
		boolean goodLength = 4 <= enter.length()  && enter.length() <= 20;
		
		// Check the pins for 3 criteria: matching, being numeric, and being of appropriate length (in that order)
		if (matches) {
			if (numeric) {
				if (goodLength) {
					// Success! Commit the PIN to shared preferences, print a message, and close.
					commitPin(enter);
					Toast.makeText(this, getResources().getString(R.string.change_pin_success), Toast.LENGTH_SHORT).show();
					finish();
					overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_left);

				} else {
					Toast.makeText(this, getResources().getString(R.string.change_pin_error_length), Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, getResources().getString(R.string.change_pin_error_numeric), Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, getResources().getString(R.string.change_pin_error_matches), Toast.LENGTH_SHORT).show();
		}
	}
	
	private void commitPin(String pin) {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putString(SettingsActivity.PIN, pin);
		editor.commit();
		
		// Actually change the lock pin!
		DevicePolicyManager devicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
		devicePolicyManager.resetPassword(pin, 0); 
	}
}