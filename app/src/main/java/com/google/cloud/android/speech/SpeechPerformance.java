package com.google.cloud.android.speech;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import static com.google.cloud.android.speech.FileService.readFromFile;

public class SpeechPerformance extends AppCompatActivity {
    String filePath;
    String speechName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_performance);
        Intent intent = getIntent();
        filePath = intent.getStringExtra("filePath");
        speechName = intent.getStringExtra("speechName");

        if (speechName != null) {
            String scriptText = null;
            try {
                scriptText = readFromFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (scriptText != null) {

                TextView speechText = (TextView) findViewById(R.id.APIResultView);
                speechText.setText(scriptText);
            }
        }
    }
    public void goToPlayBack(View view){
        Intent intent = new Intent(this, PlayBack_List.class);
        intent.putExtra("filePath", filePath);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToSpeechView(View view){
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("filePath", filePath);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToMainMenu(View view){
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }
}
