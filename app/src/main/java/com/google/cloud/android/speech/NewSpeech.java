package com.google.cloud.android.speech;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

import android.content.SharedPreferences;


public class NewSpeech extends AppCompatActivity {
    String SPEECH_SCRIPT_PATH;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_speech);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setTitle("New speech");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24px);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get extras for editing a script (if they exist)
        Intent intent = getIntent();
        String scriptName = intent.getStringExtra("filename");
        String scriptText = intent.getStringExtra("scriptText");
        if (scriptName != null) {
            this.setTitle("Edit: " + scriptName);
            // Set the text in our speech name edit text to be our speechName
            EditText speechName = findViewById(R.id.speechName);
            speechName.setText(scriptName);
        } else {
            this.setTitle("Enter a Speech");
        }
        if (scriptText != null) {
            EditText speechText = findViewById(R.id.editText);
            speechText.setText(scriptText);
        }

    }


    public void goToMainMenu(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }

    public void goToSpeechView(View view) {
        Intent intent = new Intent(this, SpeechView.class);
        startActivity(intent);
    }

    public void saveFile(View view) {
        /* Get speech text from editText */
        EditText editText = findViewById(R.id.editText);
        String speechText = editText.getText().toString();

        /* Get speech name from speechText editText */
        EditText speechNameET = findViewById(R.id.speechName);
        String speechName = speechNameET.getText().toString();

        String filePath;

        SPEECH_SCRIPT_PATH = getFilesDir() + File.separator + speechName.replace(" ", "");
        sharedPref = getSharedPreferences(speechName, MODE_PRIVATE);

        // Check if speech script directory exists
        File f = new File(SPEECH_SCRIPT_PATH);
        if(speechName.isEmpty()){
            emptySpeechName();
        } else if (speechText.isEmpty()){
            emptySpeechContent();
        }else if (f.exists()) {
            saveSpeech(speechName, speechText);
        } else if (!f.exists()) {
            f = new File(SPEECH_SCRIPT_PATH, "speech-script");
            f.mkdirs();

            try {
                //CREATE the shared preference file and add necessary values
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("currRun", 1);
                editor.putInt("currScriptNum", 1);
                editor.putString("speechNameToDisplay", speechName);
                editor.commit();

                /* Write speech text to file */
                filePath = FileService.writeToFile(speechName.replace(" ", "") + "1", speechText,
                        SPEECH_SCRIPT_PATH + File.separator + "speech-script");
                Log.d("NEWSPEECH", filePath);

                editor.putString("filepath", filePath);
                editor.commit();

                // Show notification on successful save
                Toast toast = Toast.makeText(getApplicationContext(),
                        "File saved!", Toast.LENGTH_SHORT);
                toast.show();

                // Send back to this speech's menu
                Intent intent = new Intent(this, SpeechView.class);
                intent.putExtra("speechName", speechName);


                startActivity(intent);
            } catch (Exception e) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        e.toString(), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private void printAllFilesInDir() {
        File dir = new File(SPEECH_SCRIPT_PATH);
        // Get all files saved to speech scripts
        File[] files = dir.listFiles();
        for (File file : files) {
            System.out.println(file);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

    public void emptySpeechName(){
        new AlertDialog.Builder(this)
                .setTitle("Empty Speech Name")
                .setMessage("Please enter a name for your speech.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void emptySpeechContent(){
        new AlertDialog.Builder(this)
                .setTitle("Empty Speech")
                .setMessage("Please enter text for your speech.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /* Delete all associated files */
    public void saveSpeech(final String speechName, final String speechContent) {
        new AlertDialog.Builder(this)
                .setTitle("Overwrite File")
                .setMessage("A speech with this name already exists. " +
                        "Are you sure you want to overwrite this file?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        try {
                            int currScriptNum = sharedPref.getInt("currScriptNum", -1) + 1;

                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putInt("currScriptNum", currScriptNum);
                            editor.commit();

                            String filePath = FileService.writeToFile(speechName + currScriptNum, speechContent,
                                    SPEECH_SCRIPT_PATH + File.separator + "speech-script");

                            editor.putString("filepath", filePath);
                            editor.commit();
                        } catch (Exception e) {
                            Toast toast = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        Intent intent = new Intent(NewSpeech.this, MainMenu.class);
                        startActivity(intent);
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}