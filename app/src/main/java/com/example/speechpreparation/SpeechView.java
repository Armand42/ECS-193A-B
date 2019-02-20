package com.example.speechpreparation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.io.File;

public class SpeechView extends AppCompatActivity {
    String filePath;
    String speechName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_view);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        filePath = intent.getStringExtra("filePath");
        speechName = intent.getStringExtra("speechName");
        Log.e("FILE:", filePath);
        speechName = filePath.substring(filePath.lastIndexOf(File.separator)+1);
        setTitle(speechName);
    }

    /** Called when the user taps the Send button */
    public void goToSpeechSettings(View view) {
        Intent intent = new Intent(this, SpeechSettings.class);
        intent.putExtra("filePath", filePath);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToSpeechRecord(View view) {
        Intent intent = new Intent(this, SpeechRecord.class);
        intent.putExtra("filePath", filePath);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToPlayBack(View view){
        Intent intent = new Intent(this, PlayBack.class);
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
}
