package com.google.cloud.android.speech;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

public class HowItWorks extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_it_works);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.setTitle("Welcome to Speaker Prep Pal!");
        setSupportActionBar(toolbar);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(HowItWorks.this, MainMenu.class);
        startActivity(intent);
        finish();
    }

    public void toTop(View view)
    {
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.scrollTo(0,0);
    }

    public void toHome(View view)
    {
        View v = (View) findViewById(R.id.layout_home);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0,v.getTop());
    }

    public void toNewSpeech(View view)
    {
        View v = (View) findViewById(R.id.layout_newSpeech);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0,v.getTop());
    }

    public void toSpeechView(View view)
    {
        View v = (View) findViewById(R.id.layout_speechView);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0,v.getTop());
    }

    public void toSpeechSetting(View view)
    {
        View v = (View) findViewById(R.id.layout_settings);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0,v.getTop());
    }

    public void toSpeechRecord(View view)
    {
        View v = (View) findViewById(R.id.layout_record);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0,v.getTop());
    }

    public void toSpeechPerformance(View view)
    {
        View v = (View) findViewById(R.id.layout_performance);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0,v.getTop());
    }

    public void toMistakes(View view)
    {
        View v = (View) findViewById(R.id.layout_mistakes);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0,v.getTop());
    }

    public void toPlayBack(View view)
    {
        View v = (View) findViewById(R.id.layout_playback);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.smoothScrollTo(0,v.getTop());
    }

}
