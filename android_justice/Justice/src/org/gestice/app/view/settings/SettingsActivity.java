package org.gestice.app.view.settings;

import org.gestice.app.receiver.JusticeAdminReceiver;

import org.gestice.app.R;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

public class SettingsActivity extends Activity {
	
	public static final String TAG = "SettingsActivity";
	public static final String PIN = "pin";

	private final int ENABLE_ADMIN_REQUEST_CODE = 1000;
	
	private Button mEnableDeviceAdminButton;
	private RelativeLayout mChangePinButton;
	
	@Override 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		setTitle(getResources().getString(R.string.settings_activity_title));
		
		//////////////////////////////////////////////////////////
		// Buttons
		//////////////////////////////////////////////////////////
		mEnableDeviceAdminButton = (Button)findViewById(R.id.enableDeviceAdminButton);
		mChangePinButton = (RelativeLayout)findViewById(R.id.changePinButton);

		final Activity activity = this;
		mChangePinButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				// Start a change pin activity
				Intent intent = new Intent(activity, ChangePinActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_right);
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// Setup UI here, so it refreshed every time the activity is shown
		setEnableDeviceAdminButtonState();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (requestCode == ENABLE_ADMIN_REQUEST_CODE) {
			setEnableDeviceAdminButtonState();
		}
	}
	
	// UI methods
	private void setEnableDeviceAdminButtonState() {
		boolean enabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(JusticeAdminReceiver.DEVICE_ADMIN_ENABLED, false);

		// We'll enable the button if our admin receiver *isn't* currently active.
		if (!enabled) {
			// Save reference to activity for callback context usage
			final Activity activity = this;
			mEnableDeviceAdminButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Launch an intent to ask the user to let us be a device admin. Permissions are
					// specified in res/xml/justice_device_admin.xml.
		            Intent activateDeviceAdminIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		            activateDeviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
		            		new ComponentName(activity, JusticeAdminReceiver.class));

		            // It is good practice to include the optional explanation text to explain to
		            // user why the application is requesting to be a device administrator.  The system
		            // will display this message on the activation screen.
		            activateDeviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
		                    getResources().getString(R.string.gestice_device_admin_desc));
		            startActivityForResult(activateDeviceAdminIntent, ENABLE_ADMIN_REQUEST_CODE);
				}
			});
			
		} else {
			// Disable the button and change its text.
			mEnableDeviceAdminButton.setOnClickListener(null);
			mEnableDeviceAdminButton.setEnabled(false);
			mEnableDeviceAdminButton.setText(getResources().getString(R.string.enable_admin_button_finished));
		}
	}
}
