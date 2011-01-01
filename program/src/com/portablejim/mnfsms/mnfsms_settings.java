package com.portablejim.mnfsms; 

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.portablejim.mnfsms.R.id;

public class mnfsms_settings extends Activity {
	public static final String SETTINGS_NAME = "MNFSMS-settings";

    private OnClickListener save_button_listener = new OnClickListener() {
		public void onClick(View arg0) {
			// Open the preferences.
			SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, 0);
			// Open an editor to edit the preferences.
			SharedPreferences.Editor settings_editor = settings.edit();
			// Store the EditText values in the preferences.
			settings_editor.putString("user_number", ((EditText) findViewById(id.user_number_textbox)).getText().toString());
			settings_editor.putString("user_password", ((EditText) findViewById(id.user_password_textbox)).getText().toString());
			settings_editor.putString("user_smssubid", ((EditText) findViewById(id.user_smssub_textbox)).getText().toString());
			settings_editor.commit();
			
			finish();
		}
	};
	
	private void load_prefs(String value, EditText target) {
		// Fill in the values from the preferences if they are not empty
		if ((value != null) && (value != "")) {
			target.setText(value);
		}
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        // Load the button into the variable and make it save the preferences, then exit the view when clicked.
        Button save_button = (Button)findViewById(id.settings_save_button);
        save_button.setOnClickListener(save_button_listener);
        
     // "Clear" Button 
        Button cancel_button = (Button)findViewById(id.settings_cancel_button);
        cancel_button.setOnClickListener(new OnClickListener() {
    		
    		public void onClick(View arg0) {
    			finish();
    		}
        });
        
        // Load up the preferences, to load them into the fields.
        SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, 0);
        
        load_prefs(settings.getString("user_number", null), (EditText) findViewById(id.user_number_textbox));
        load_prefs(settings.getString("user_password", null), (EditText) findViewById(id.user_password_textbox));
        load_prefs(settings.getString("user_smssubid", null), (EditText) findViewById(id.user_smssub_textbox));
    }
}