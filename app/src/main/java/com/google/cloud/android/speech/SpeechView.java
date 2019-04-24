package com.google.cloud.android.speech;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.view.Display;


import java.io.File;
import java.io.IOException;

public class SpeechView extends AppCompatActivity {
    String filePath;
    String speechName;
    String scriptText;
    Boolean videoPlaybackState;
    Boolean viewScriptState;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_view);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
        SharedPreferences sharedPreferences = getSharedPreferences(speechName,MODE_PRIVATE);
        final File dir = getDir(speechName, MODE_PRIVATE);
        filePath = sharedPreferences.getString("filepath", "error");
//        if(sharedPreferences.contains("filePath"))
        speechName = intent.getStringExtra("speechName");
        videoPlaybackState = sharedPreferences.getBoolean("videoPlayback", false);
        viewScriptState = sharedPreferences.getBoolean("displaySpeech", false);
        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setTitle(speechName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24px);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /** Called when the user taps the Send button */
    public void goToSpeechToText(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    /** Called when the user taps the Send button */
    public void goToSpeechSettings(View view) {
        Intent intent = new Intent(this, SpeechSettings.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }
    // SORT OF WORKS BOI NEED MORE VIEWS
    public void goToSpeechRecord(View view) {
        Intent intent;
        // If Video and Script
        if(videoPlaybackState && viewScriptState) {
            intent = new Intent(this, SpeechRecord.class);
        }
        // If just video
        else if (videoPlaybackState) {
            intent = new Intent(this, SpeechRecord2.class);
        }
        // If just want the Audio
        else if(viewScriptState) {
            intent = new Intent(this, MainActivity2.class);
        }
        // If want Audio and Script
        else{
            intent = new Intent(this, MainActivity.class);
        }
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToPlayBack(View view){
        Intent intent = new Intent(this, PlayBack_List.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToScriptView(View view) {
        Intent intent = new Intent(this, ScriptView.class);
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
                            //            FileService.deleteSpeech(filePath);
                            File script = new File(filePath);
                            if (!script.delete()) {
                                throw new Exception("Error deleting script");
                            }
                            Toast toast = Toast.makeText(getApplicationContext(), "Speech deleted", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        catch (Exception e){
                            Toast toast = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        //Delete  all videos
                        final File dir = getDir(speechName, MODE_PRIVATE);
                        SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);
                        for(int i =0 ; i<sharedPreferences.getInt("currVid", -1) ; i++)
                        {
                            File vid = new File(dir.getAbsolutePath() + "/" + speechName+" "+ i+".mp4" );
                            vid.delete();
                        }
                        dir.delete();
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
        }

        return super.onOptionsItemSelected(item);
    }

}
