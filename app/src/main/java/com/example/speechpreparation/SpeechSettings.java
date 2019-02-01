package com.example.speechpreparation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class SpeechSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_settings);
        getIntent();

    }

    public void goToSpeechMenu (View view){
        Intent intent = new Intent(this, SpeechView.class);
        startActivity(intent);
    }
}
