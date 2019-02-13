package com.example.speechpreparation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SpeechRecord extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_record);
        getIntent();
        this.setTitle("Record a Speech");
    }

    public void goToMainMenu(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }

    public void goToSpeechPerformance(View view){
        Intent intent = new Intent(this, SpeechPerformance.class);
        startActivity(intent);
    }
}
