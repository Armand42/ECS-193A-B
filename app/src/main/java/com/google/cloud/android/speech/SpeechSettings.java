package com.google.cloud.android.speech;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class SpeechSettings extends AppCompatActivity {
    String filePath;
    String speechName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_settings);

        Intent intent = getIntent();

        filePath = intent.getStringExtra("filePath");
        speechName = intent.getStringExtra("speechName");

//        speechName = filePath.substring(filePath.lastIndexOf("\\")+1);
        this.setTitle("Speech Settings");
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("filePath", filePath);
        startActivity(intent);
        return;
    }

    public void goToSpeechMenu (View view){
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("filePath", filePath);
        startActivity(intent);
    }
}
