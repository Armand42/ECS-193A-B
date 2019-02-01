package com.example.speechpreparation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SpeechView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_view);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
    }

    /** Called when the user taps the Send button */
    public void goToSpeechSettings(View view) {
        Intent intent = new Intent(this, SpeechSettings.class);
        startActivity(intent);
    }

    public void goToSpeechRecord(View view) {
        Intent intent = new Intent(this, SpeechRecord.class);
        startActivity(intent);
    }
}
