package com.google.cloud.android.speech;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class DiffView extends AppCompatActivity {

    String scriptText;
    String speechToText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff_view);

        Intent intent = getIntent();

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setTitle("New speech");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24px);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        this.setTitle("Speeches");
        // Make script viewable
        //CREATE the shared preference file and get necessary values
        SharedPreferences sharedPreferences= getSharedPreferences(intent.getStringExtra("speechName"), MODE_PRIVATE);
        try {
            scriptText = FileService.readFromFile(sharedPreferences.getString("filepath",null));
            // Duplicate for now -- eventually replace with reading most recent speech to text result
            speechToText = FileService.readFromFile(sharedPreferences.getString("filepath",null));
            setScriptText();
        } catch (IOException e) {
            e.printStackTrace();
            Toast readToast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_SHORT);
            readToast.show();
        }
    }

    // Display speech in playback
    private void setScriptText() {
        // Get text body
        TextView scriptBody = (TextView) findViewById(R.id.scriptBody);

        // Make script scrollable
        scriptBody.setMovementMethod(new ScrollingMovementMethod());

        // Set text of scriptBody to be what we read from the file
        scriptBody.setText(scriptText);

        TextView speechToTextBody = (TextView) findViewById(R.id.speechToTextBody);

        speechToTextBody.setMovementMethod(new ScrollingMovementMethod());

        speechToTextBody.setText(speechToText);
    }
}
