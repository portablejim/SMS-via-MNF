package au.id.jamesmckay.smsviamnf;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import au.id.jamesmckay.smsviamnf.mnfsms_about;
import au.id.jamesmckay.smsviamnf.mnfsms_help;
import au.id.jamesmckay.smsviamnf.mnfsms_settings;

import au.id.jamesmckay.smsviamnf.R;
import au.id.jamesmckay.smsviamnf.R.id;

public class mnfsms extends AppCompatActivity {
    public static final String SETTINGS_NAME = "MNFSMS-settings";
    private static final int CONTACT_PICKER_RESULT = 1001;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // "Send" Button
        Button send_button = (Button)findViewById(id.send_button);
        send_button.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // Send message in background
                mnfsms_sendsms sendThread = new mnfsms_sendsms();
                sendThread.execute();
            }
        });

        // "Clear" Button
        ((Button)findViewById(id.cancel_button)).setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // Clear the text in both text boxes
                ((EditText) findViewById(id.to_textbox)).setText("");
                ((EditText) findViewById(id.message_textbox)).setText("");
            }
        });

        // "Exit" Button
        ((Button)findViewById(id.exit_button)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                // Exit the app
                finish();
            }
        });

        // "Add Contact" Button
        ((Button)findViewById(id.add_contact_button)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK);
                contactPickerIntent.setType(Phone.CONTENT_TYPE);
                startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
            }
        });

        // Show length on changing the message text
        // Also called when loaded to set to zero.
        ((EditText)findViewById(id.message_textbox)).addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
            public void afterTextChanged(Editable s) {
                int length = s.length();
                int text_colour = Color.WHITE;

                // Display number of characters left
                ((TextView)findViewById(id.message_textcount)).setText((160 - length) + " left");

                //Sets colour of text
                if(length < 140)
                {
                    text_colour = Color.GREEN;
                }
                else if(length < 150)
                {
                    text_colour = Color.YELLOW;
                }
                else if(length < 160)
                {
                    text_colour = Color.rgb(254, 127, 1);
                }
                else if(length >= 160)
                {
                    text_colour = Color.RED;
                }
                ((TextView)findViewById(id.message_textcount)).setTextColor(text_colour);
            }
        });

        SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, 0);
        SharedPreferences.Editor settings_editor = settings.edit();
        if(settings.getBoolean("firstrun", true))
        {
            new AlertDialog.Builder(mnfsms.this).setTitle(R.string.firstrun_title).setMessage(R.string.firstrun).setPositiveButton("OK", null).show();
            settings_editor.putBoolean("firstrun", false);
            settings_editor.commit();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        MenuItem mi_settings = menu.findItem(R.id.settingsbutton);
        mi_settings.setIntent(new Intent(this, mnfsms_settings.class));

        MenuItem mi_about = menu.findItem(R.id.about_button);
        mi_about.setIntent(new Intent(this, mnfsms_about.class));

        MenuItem mi_help = menu.findItem(R.id.help_button);
        mi_help.setIntent(new Intent(this, mnfsms_help.class));

        return true;
    }

    // After user picks contact
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Cursor cursor = null;
            String phone_num = "";
            switch (requestCode) {
                case CONTACT_PICKER_RESULT:
                    // handle contact results
                    try {
                        Uri result = data.getData();

                        // get the contact id from the Uri
                        String id = result.getLastPathSegment();

                        // query for everything phone
                        cursor = getContentResolver().query(
                                Phone.CONTENT_URI,
                                null,
                                Phone._ID + " = ?",
                                new String[]{id},
                                null);

                        int phonenumIdx = cursor.getColumnIndex(Phone.NUMBER);

                        // let's just get the first phone number
                        if (cursor.moveToFirst()) {
                            phone_num = cursor.getString(phonenumIdx);
                        } else {
                        }
                    } catch (Exception e) {
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                        EditText phonenumEntry = (EditText) findViewById(id.to_textbox);

                        //Check if there is a comma at the end
                        Pattern pattern = Pattern.compile(",.*$");
                        Matcher matcher = pattern.matcher(phonenumEntry.getText().toString());
                        Pattern pattern_empty = Pattern.compile("^( |,)*$");
                        Matcher matcher_empty = pattern_empty.matcher(phonenumEntry.getText().toString());

                        // Add comma if not present (and if not empty
                        if (!(matcher.matches()) || (matcher_empty.matches())) {
                            phonenumEntry.append(", ");
                        }

                        if (matcher_empty.matches()) {
                            SharedPreferences settings = getSharedPreferences(SETTINGS_NAME, 0);
                            if (settings.getBoolean("fix_phone", false)) {
                                // Format phone number correctly
                                phone_num = fix_phone(phone_num);
                            }

                            // Put number into field
                            phonenumEntry.setText(phone_num);
                        } else {
                            phonenumEntry.append(phone_num);
                        }

                        if (phone_num.length() == 0) {
                            Toast.makeText(this, "Error getting phone number for contact.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
            }

        } else {
            // gracefully handle failure
            Toast.makeText(this, "No phone number added", Toast.LENGTH_SHORT).show();
        }
    }

    private class mnfsms_sendsms extends AsyncTask<Void, Void, Integer>
    {
        // Set up vars for sending SMS
        private String URL_username = "";
        private String URL_password = "";
        private String URL_numbers = "";
        private String URL_subscription = "";
        private String URL_message = "";

        // Other Vars
        HttpURLConnection sms_request = null;
        int req;
        URL SMS_url = null;
        String sms_result = "";
        ProgressDialog pd;

        protected void onPreExecute()
        {
            pd = ProgressDialog.show(mnfsms.this , "Sending...", "Sending", false, true);

            // Get preferences - Method not thread safe
            SharedPreferences settings = getSharedPreferences(mnfsms.SETTINGS_NAME, 0);

            try{
                // Extract SMS info from settings or from fields in View.
                URL_username = URLEncoder.encode(settings.getString("user_number", ""), "UTF-8");
                URL_password = URLEncoder.encode(settings.getString("user_password", ""), "UTF-8");
                URL_numbers = URLEncoder.encode(((EditText) findViewById(id.to_textbox)).getText().toString(), "UTF-8");
                URL_subscription = URLEncoder.encode(settings.getString("user_smssubid", ""), "UTF-8");
                String text_message = ((EditText) findViewById(id.message_textbox)).getText().toString();
                text_message.replaceAll("\\n", "\r\n");
                URL_message = URLEncoder.encode(text_message, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Integer doInBackground(Void... v) {
            try {
                SMS_url = new URL("https://www.mynetfone.com.au/send-sms?username=" + URL_username + "&password=" + URL_password + "&to=" + URL_numbers + "&subscriptionId=" + URL_subscription + "&text=" + URL_message);
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }


            try{
                sms_request = (HttpURLConnection) SMS_url.openConnection();
                sms_request.setDoInput(true);
                sms_request.connect();
                req = ((HttpURLConnection)sms_request).getResponseCode();
            } catch (IOException e) {
                // May indicate 401 (Unauthorised) error.
                req = 401;
            }

            return req;
        }

        protected void onPostExecute(Integer req)
        {
            pd.dismiss();

            switch (req) {
                case 200:
                    sms_result = "SMS sent successfully";
                    break;

                case 401:
                    sms_result = "SMS failed to send. Please check settings (MENU => Settings) to ensure they are correct.";
                    break;

                case 500:
                    sms_result = "SMS failed to send. Ensure there is a message to send.";
                    break;

                case 400:
                default:
                    sms_result = "SMS failed to send because of an unknown error. Please check you have internet access, and all details (including MENU => Settings) are correct.";
                    break;
            }

            new AlertDialog.Builder(mnfsms.this).setMessage(sms_result).setPositiveButton("Close", null).show();
        }

    }

    public void show_msg_length(Editable s)
    {
        int length = s.length();
        int text_colour = Color.WHITE;

        // Display number of characters left
        ((TextView)findViewById(id.message_textbox)).setText((160 - length) + " left");

        //Sets colour of text
        if(length < 110)
        {
            text_colour = Color.GREEN;
        }
        else if(length < 135)
        {
            text_colour = Color.YELLOW;
        }
        else if(length < 155)
        {
            text_colour = Color.rgb(255, 127, 0);
        }
        else if(length >= 160)
        {
            text_colour = Color.GREEN;
        }
        ((TextView)findViewById(id.message_textbox)).setTextColor(text_colour);
    }

    public String fix_phone(String old_phone)
    {
        String new_phone = old_phone.replaceAll("^(?:\\d*(\\u002B))?\\u0028?(\\d*)\\u0029?\\s?(\\d*)\\s?\\u002D?\\s?(\\d+)$", "$1$2$3$4");

        // Remove "null" from start of string when not international number
        new_phone = new_phone.replaceAll("^null", "");

        return new_phone;
    }
}