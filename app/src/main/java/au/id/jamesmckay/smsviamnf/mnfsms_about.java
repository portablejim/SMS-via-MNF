package au.id.jamesmckay.smsviamnf;

import au.id.jamesmckay.smsviamnf.R;

import android.app.Activity;
import android.os.Bundle;

public class mnfsms_about extends Activity {
	public static final String SETTINGS_NAME = "MNFSMS-settings";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        }
}