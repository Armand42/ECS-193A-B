package ecs193.speechPrepPal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ScrollView;

import ecs193.speechPrepPal.R;

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

    //Function used in buttons to scroll to top
    public void toTop(View view)
    {
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.scrollTo(0,0);
    }


    //All the functions below scroll to different parts of the guide
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
