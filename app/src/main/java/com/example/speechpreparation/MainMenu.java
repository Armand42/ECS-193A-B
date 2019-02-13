package com.example.speechpreparation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        getIntent();
        this.setTitle("Speeches");
//        String[] FileList = getApplicationContext().fileList();
    }

    public void goToNewSpeech(View view) {
        Intent intent = new Intent(this, NewSpeech.class);
        startActivity(intent);
    }

    public void goToSpeechMenu(View view){
        Intent intent = new Intent(this, SpeechView.class);
        startActivity(intent);
    }
}
