package com.google.cloud.android.speech;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SpeechRecord extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_record);
        getIntent();
        this.setTitle("Record a Speech");
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, com.google.cloud.android.speech.Camera2VideoFragment.newInstance())
                    .commit();
        }
      
    }


}

