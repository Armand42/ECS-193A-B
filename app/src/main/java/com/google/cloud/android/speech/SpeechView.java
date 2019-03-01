package com.google.cloud.android.speech;

import android.content.Intent;
import android.os.Bundle;
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
        // TODO: add "are you sure?" alert dialog on pressing this button

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

        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }

}
