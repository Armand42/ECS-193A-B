package com.google.cloud.android.speech;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import android.content.SharedPreferences;

import org.w3c.dom.Text;

import static com.google.cloud.android.speech.FileService.readFromFile;

public class SpeechPerformance extends BaseActivity {
    String speechName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_performance);
        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");

        TextView speechTime = (TextView) findViewById(R.id.speechTime);

        SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);

        // Set speech time textview to the elapsed time from this speech
        long timeElapsed = sharedPreferences.getLong("timeElapsed", 0);
        int minutes = (int) timeElapsed / 60000;
        int seconds = (int) timeElapsed % 60000 / 1000;
        speechTime.setText(String.format("Speech time: %02d:%02d", minutes, seconds));
        if (speechName != null) {
            String scriptText = null;
            try {
                Log.d("FILEPATH:", sharedPreferences.getString("filepath",null));
                scriptText = readFromFile(sharedPreferences.getString("filepath",null));

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (scriptText != null) {

                TextView speechText = (TextView) findViewById(R.id.APIResultView);
                speechText.setText(scriptText);
            }
        }
    }
    public void goToPlayBack(View view){
        Intent intent = new Intent(this, PlayBack_List.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToSpeechView(View view){
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToDiffView(View view){
        Intent intent = new Intent(this, DiffView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }
}
