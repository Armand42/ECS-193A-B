package com.google.cloud.android.speech;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

public class SpeechSettings extends AppCompatActivity {
    String filePath;
    String speechName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_settings);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setTitle(speechName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24px);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }
}
