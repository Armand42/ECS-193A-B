package com.google.cloud.android.speech;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Switch;

public class SpeechSettings extends AppCompatActivity {
    String filePath;
    String speechName;
    Switch videoPlayback;

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

        speechName = intent.getStringExtra("speechName");

        this.setTitle("Speech Settings");

        videoPlayback = (Switch) findViewById(R.id.videoPlaybackSwitch);
        SharedPreferences sharedPreferences = getSharedPreferences(speechName,MODE_PRIVATE);
        videoPlayback.setChecked(sharedPreferences.getBoolean("videoPlayback", false));
    }

    public void addToSharedPreferences() {
        //CREATE the shared preference file and add necessary values
        SharedPreferences sharedPref = getSharedPreferences(speechName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("videoPlayback", videoPlayback.isChecked());
        editor.commit();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        addToSharedPreferences();
        startActivity(intent);
    }

    public void goToSpeechMenu (View view){
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        addToSharedPreferences();
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }
}
