package com.portablejim.mnfsms;

import android.app.Activity;
import android.os.Bundle;

public class mnfsms_help extends Activity {
	public static final String SETTINGS_NAME = "MNFSMS-settings";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        }
}