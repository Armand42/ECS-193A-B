package com.google.cloud.android.speech;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SpeechView extends AppCompatActivity {
    String filePath;
    String speechName;
    String scriptText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_view);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        filePath = intent.getStringExtra("filePath");
        speechName = intent.getStringExtra("speechName");
        speechName = filePath.substring(filePath.lastIndexOf(File.separator)+1);
        setTitle(speechName);
    }

    /** Called when the user taps the Send button */
    public void goToSpeechToText(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("filePath", filePath);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }


    /** Called when the user taps the Send button */
    public void goToSpeechSettings(View view) {
        Intent intent = new Intent(this, SpeechSettings.class);
        intent.putExtra("filePath", filePath);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToEditSpeech(View view) {
        Intent intent = new Intent(this, NewSpeech.class);
        try {
            scriptText = FileService.readFromFile(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        intent.putExtra("filename", speechName);
        intent.putExtra("scriptText", scriptText);
        startActivity(intent);
    }

    public void goToSpeechRecord(View view) {
        Intent intent = new Intent(this, SpeechRecord.class);
        intent.putExtra("filePath", filePath);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToPlayBack(View view){
        Intent intent = new Intent(this, PlayBack_List.class);
        intent.putExtra("filePath", filePath);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToScriptView(View view) {
        Intent intent = new Intent(this, ScriptView.class);
        intent.putExtra("filePath", filePath);
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
                            String videoFilePath = getExternalFilesDir(null) + speechName+".mp4";
                            FileService.deleteSpeech(filePath, videoFilePath);
                            Toast toast = Toast.makeText(getApplicationContext(), "Speech deleted", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        catch (Exception e){
                            Toast toast = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        Intent intent = new Intent(SpeechView.this, MainMenu.class);
                        startActivity(intent);
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
