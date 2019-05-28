package com.google.cloud.android.speech;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.SharedPreferences;

import org.json.JSONObject;


public class NewSpeech extends AppCompatActivity {
    private String SPEECH_SCRIPT_PATH, speechFileName, prevActivity;
    private SharedPreferences sharedPref, defaultPreferences;
    private Set<String> speechNameSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_speech);

        EditText speechName = (EditText) findViewById(R.id.speechName);
        EditText speechText = (EditText) findViewById(R.id.editText);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        setTitle("New speech");
        toolbar.setNavigationIcon(R.drawable.ic_baseline_home_24px);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMainMenu(view);

            }
        });
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get extras for editing a script (if they exist)
        Intent intent = getIntent();
        speechFileName = intent.getStringExtra("speechName");
        String scriptText = intent.getStringExtra("scriptText");
        prevActivity = intent.getStringExtra("prevActivity");

        if (speechFileName != null) {
            String scriptName = defaultPreferences.getString(speechFileName, null);
            this.setTitle("Edit: " + scriptName);
            // Set the text in our speech name edit text to be our speechName
            speechName.setText(scriptName);
        } else {
            this.setTitle("Enter a Speech");
        }
        if (scriptText != null) {
            speechText.setText(scriptText);
        }

    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(NewSpeech.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            View view = findViewById(R.id.action_save);
            saveFile(view);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void goToMainMenu(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }

    public void goToSpeechView(View view, String speechName) {
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void saveFile(View view) {
        /* Get speech text from editText */
        EditText editText = findViewById(R.id.editText);
        String speechText = editText.getText().toString();

        /* Get speech name from speechText editText */
        EditText speechNameET = findViewById(R.id.speechName);
        String speechDisplayName = speechNameET.getText().toString();
        speechDisplayName = speechDisplayName.trim();

        /* Validate speech name */
        String regex = "^[a-zA-Z0-9,_.\\- ]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(speechDisplayName);

        // Valid name
        if (matcher.matches()) {
            String filePath;
            String selectedSpeechName = defaultPreferences.getString(speechFileName, null);
            // Get the value for the run counter
            speechNameSet = defaultPreferences.getStringSet("speechNameSet", new HashSet<String>());

            // Check if speech script directory exists
            if (speechDisplayName.isEmpty()) {
                emptySpeechNameDialog();
            } else if (speechText.isEmpty()) {
                emptySpeechContentDialog();
            } else if (prevActivity.equals("mainMenu") && speechNameSet.contains(speechDisplayName)) {
                speechAlreadyExistsDialog();
            } else if (prevActivity.equals("scriptView")) {
                if (!selectedSpeechName.equals(speechDisplayName) && speechNameSet.contains(speechDisplayName))
                    speechAlreadyExistsDialog();
            } else if (!speechNameSet.contains(speechDisplayName)) {
                Log.d("NEWSPEECH", "SPEECH NOT IN SET");
                try {
                    int counter = defaultPreferences.getInt("counter", 1);
                    String speechName = "speech" + counter;

                    SPEECH_SCRIPT_PATH = getFilesDir() + File.separator + "speechFiles" + File.separator + speechName;

                    File f = new File(SPEECH_SCRIPT_PATH, "speech-script");
                    f.mkdirs();

                    sharedPref = getSharedPreferences(speechName, MODE_PRIVATE);

                    //CREATE the shared preference file and add necessary values
                    SharedPreferences.Editor editor = sharedPref.edit();
                    SharedPreferences.Editor defaultEditor = defaultPreferences.edit();
                    JSONObject jsonObj = new JSONObject();
                    editor.putInt("currRun", 1);
                    editor.putInt("currScriptNum", 1);
                    editor.putString("runDisplayNameToFilepath", jsonObj.toString());
                    editor.commit();

                    speechNameSet.add(speechDisplayName);
                    defaultEditor.putStringSet("speechNameSet", speechNameSet);
                    defaultEditor.putInt("counter", counter + 1);
                    defaultEditor.putString(speechName, speechDisplayName);
                    defaultEditor.commit();

                    /* Write speech text to file */
                    filePath = FileService.writeToFile(speechName + "1", speechText,
                            SPEECH_SCRIPT_PATH + File.separator + "speech-script");
                    Log.d("NEWSPEECH", filePath);

                    editor.putString("filepath", filePath);
                    editor.commit();

                    goToSpeechView(view, speechName);

                } catch (Exception e) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            e.toString(), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        } else {
            // Display error on layout
            invalidSpeechNameDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_speech_menu, menu);
        return true;
    }

    public void speechAlreadyExistsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("A speech with this name already exists")
                .setMessage("Please enter a different name for your speech.")
                .setIcon(R.drawable.ic_baseline_warning_24px)
                .show();
    }

    public void invalidSpeechNameDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Invalid speech name.")
                .setMessage("Your speech name can only include alphanumeric characters, commas, periods, and dashes.")
                .setIcon(R.drawable.ic_baseline_warning_24px)
                .show();
    }

    public void emptySpeechNameDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Empty Speech Name")
                .setMessage("Please enter a name for your speech.")
                .setIcon(R.drawable.ic_baseline_warning_24px)
                .show();
    }

    public void emptySpeechContentDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Empty Speech")
                .setMessage("Please enter text for your speech.")
                .setIcon(R.drawable.ic_baseline_warning_24px)
                .show();
    }

    public void overwriteExistingSpeech(View view, final String speechFileName, final String speechContent, final String speechDisplayName) {
        SPEECH_SCRIPT_PATH = getFilesDir() + File.separator + "speechFiles" + File.separator + speechFileName;

        File f = new File(SPEECH_SCRIPT_PATH, "speech-script");
        f.mkdirs();

        sharedPref = getSharedPreferences(speechFileName, MODE_PRIVATE);
        int currScriptNum = sharedPref.getInt("currScriptNum", -1) + 1;

        SharedPreferences.Editor editor = sharedPref.edit();
        SharedPreferences.Editor defaultEditor = defaultPreferences.edit();

        editor.putInt("currScriptNum", currScriptNum);
        editor.commit();

        speechNameSet.remove(defaultPreferences.getString(speechFileName, null));
        speechNameSet.add(speechDisplayName);
        defaultEditor.putString(speechFileName, speechDisplayName);
        defaultEditor.putStringSet("speechNameSet", speechNameSet);
        defaultEditor.commit();

        String filePath = null;
        try {
            filePath = FileService.writeToFile(speechFileName + currScriptNum, speechContent,
                    SPEECH_SCRIPT_PATH + File.separator + "speech-script");
        } catch (Exception e) {
            e.printStackTrace();
        }

        editor.putString("filepath", filePath);
        editor.commit();

        goToSpeechView(view, speechFileName);
    }

    @Override
    public void onBackPressed() {
        EditText speechName = (EditText) findViewById(R.id.speechName);
        EditText speechText = (EditText) findViewById(R.id.editText);

        String name = speechName.getText().toString();

        if (!(name.isEmpty() && speechText.getText().toString().isEmpty()) && prevActivity.equals("mainMenu")) {
            new AlertDialog.Builder(this)
                    .setTitle("Exit new speech?")
                    .setMessage("Your new speech will not be saved.")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO: Delete this speech run
                            NewSpeech.super.onBackPressed();
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(R.drawable.ic_baseline_warning_24px)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void setOnFocusChangeListener(EditText editText) {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });
    }

}