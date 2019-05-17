package com.google.cloud.android.speech;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.content.SharedPreferences;


import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class SpeechView extends AppCompatActivity {
    private String filePath, speechName, scriptText;
    private Boolean videoPlaybackState, viewScriptState, timerdisplayState;
    private SharedPreferences defaultPreferences;

    String SPEECH_FOLDER_PATH;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_view);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
        SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        filePath = sharedPreferences.getString("filepath", "error");
        speechName = intent.getStringExtra("speechName");
        videoPlaybackState = sharedPreferences.getBoolean("videoPlayback", false);
        viewScriptState = sharedPreferences.getBoolean("displaySpeech", false);
        timerdisplayState = sharedPreferences.getBoolean("timerDisplay", false);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        setTitle(defaultPreferences.getString(speechName, null));
    }

    /**
     * Called when the user taps the Send button
     */
    public void goToSpeechToText(View view) {
        Intent intent = new Intent(this, RecordAudio.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    /**
     * Called when the user taps the Send button
     */
    public void goToSpeechSettings(View view) {
        Intent intent = new Intent(this, SpeechSettings.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }


    public void goToSpeechRecord(View view) {
        Intent intent;
        // Only video
        if (videoPlaybackState)
            intent = new Intent(this, RecordVideo.class);
        else
            intent = new Intent(this, RecordAudio.class);

        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToPlayBack(View view) {
        Intent intent = new Intent(this, PlayBack_List.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToScriptView(View view) {
        Intent intent = new Intent(this, ScriptView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToDiffView(View view) {
        Intent intent = new Intent(this, DiffViewTest.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToMainMenu(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    /* Delete all associated files */
    public void deleteSpeech(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Delete this speech?")
                .setMessage("All associated files will be lost.")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        try {
                            //getting speechDisplayName value from set and then removing it from set
                            Set<String> speechNameSet = defaultPreferences.getStringSet("speechNameSet", new HashSet<String>());
                            speechNameSet.remove(defaultPreferences.getString(speechName, null));

                            SPEECH_FOLDER_PATH = getFilesDir() + File.separator + "speechFiles" + File.separator + speechName;
                            File speechFolder = new File(SPEECH_FOLDER_PATH);
                            recursiveDelete(speechFolder);

                            if (speechFolder.exists()) {
                                throw new Exception("Error deleting script");
                            }
                            Toast toast = Toast.makeText(getApplicationContext(), "Speech deleted", Toast.LENGTH_SHORT);
                            toast.show();
                        } catch (Exception e) {
                            Toast toast = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);

                        sharedPreferences.edit().clear().apply(); //clears all preferences

                        Intent intent = new Intent(SpeechView.this, MainMenu.class);
                        startActivity(intent);
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.speech_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete) {
            View view = findViewById(R.id.action_delete);
            deleteSpeech(view);
            return true;
        } else if (id == R.id.action_home) {
            View view = findViewById(R.id.action_delete);
            goToMainMenu(view);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void recursiveDelete(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                recursiveDelete(child);
            }
        }

        fileOrDirectory.delete();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SpeechView.this, MainMenu.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
        finish();
    }

}
