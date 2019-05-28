package ecs193.speechPrepPal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import ecs193.speechPrepPal.R;

public class SpeechSettings extends AppCompatActivity {
    String filePath;
    String speechName;
    Switch videoPlayback;
    Switch displaySpeech;
    Switch timerDisplay;
    EditText speechTime;
    long speechLengthMs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_settings);

        // Initiate Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_home_24px);
        // If home icon is tapped, save settings, and go home
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               homeSave(view);

            }
        });
        setTitle("Speech Settings");
        toolbar.setSubtitle(speechName);

        Intent intent = getIntent();

        speechName = intent.getStringExtra("speechName");
        // Get views based on switch value
        videoPlayback = (Switch) findViewById(R.id.videoPlaybackSwitch);
        displaySpeech = (Switch) findViewById(R.id.displaySpeechSwitch);
        timerDisplay = (Switch) findViewById(R.id.displayTimerSwitch);

        speechTime = (EditText) findViewById(R.id.speechTime);

        /* Get shared preferences */
        SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);
        // Get speech length from shared prefs (default value of 10 minutes)
        speechLengthMs = sharedPreferences.getLong("timerMilliseconds", 600000);

        videoPlayback.setChecked(sharedPreferences.getBoolean("videoPlayback", false));
        displaySpeech.setChecked(sharedPreferences.getBoolean("displaySpeech", false));
        timerDisplay.setChecked(sharedPreferences.getBoolean("timerDisplay", false));
        // Display default timer value to be edited if switch is on
        toggleTimeInput(timerDisplay.isChecked());

        timerDisplay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleTimeInput(timerDisplay.isChecked());
            }
        });
    }
    // Toggle input line if timer is switched on
    public void toggleTimeInput(boolean isChecked) {
        TextView speechTimeLabel = findViewById(R.id.textView);
        speechTime.setEnabled(isChecked);
        if (isChecked) {
            speechTimeLabel.setAlpha(1);
            // Set speechLength editText to be this value (in minutes = /60000)
            speechTime.setText(Long.toString(speechLengthMs / 60000));
            speechTime.setSelection(speechTime.getText().length());
            //Show numeric keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

        } else {
            speechTimeLabel.setAlpha(128 / 255);
            speechTime.setText("");
            speechTime.setBackground(null);
        }
    }
    // Hide keyboard and go to Main Menu
    public void goToMainMenu(View view) {
        hideKeyboard(view);
        Intent intent = new Intent(this, MainMenu.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }


    // Save state of settings by tapping the home icon
    public boolean homeSave(View view) {
        long maxMinutes = 0;
        int id = view.getId();

        if (!(speechTime.getText().toString().trim().isEmpty())) {
            maxMinutes = Long.parseLong(speechTime.getText().toString());
        }
            // if timer display is enabled check for a valid time
            if(timerDisplay.isChecked()) {
                if (speechTime.getText().toString().trim().isEmpty() || speechTime.getText().toString().equals("0") || maxMinutes > 60){
                    invalidTimerValueDialog();
                    return false;
                }

                hideSoftKeyboard(SpeechSettings.this);
            }
            addToSharedPreferences();
            goToMainMenu(view);

        return true;

    }
    // Save state of settings by tapping the back button
    public boolean backSave() {
        long maxMinutes = 0;

        if (!(speechTime.getText().toString().trim().isEmpty())) {
            maxMinutes = Long.parseLong(speechTime.getText().toString());
        }
        // if timer display is enabled check for a valid time
        if(timerDisplay.isChecked()) {
            if (speechTime.getText().toString().trim().isEmpty() || speechTime.getText().toString().equals("0") || maxMinutes > 60){
                invalidTimerValueDialog();
                return false;
            }

            hideSoftKeyboard(SpeechSettings.this);
        }
        addToSharedPreferences();

        return true;

    }
    // CREATE the shared preference file and add necessary values
    public void addToSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(speechName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("videoPlayback", videoPlayback.isChecked());
        editor.putBoolean("displaySpeech", displaySpeech.isChecked());
        editor.putBoolean("timerDisplay", timerDisplay.isChecked());
        // Add timer value to preferences if switched on
        if(timerDisplay.isChecked()){
            EditText maxTimerText = (EditText) findViewById(R.id.speechTime);
            long seconds = Long.parseLong(maxTimerText.getText().toString());
            editor.putLong("timerMilliseconds", seconds * 60000);

        }
        editor.commit();

    }
    // Hide keyboard
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(NewSpeech.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    // Go back to main speech menu
    public void goToSpeechMenu() {
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        // save settings if back button is tapped
        if (backSave() == true) {
            super.onBackPressed();
            Intent intent = new Intent(SpeechSettings.this, SpeechView.class);
            intent.putExtra("speechName", speechName);
            startActivity(intent);
            finish();
        }

    }
    // Hide keyboard
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
    // Prompt user to enter a valid time
    public void invalidTimerValueDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Invalid Timer Value")
                .setMessage("Please enter a time from 1 - 60.")
                .setIcon(R.drawable.ic_baseline_warning_24px)
                .show();
    }
}
