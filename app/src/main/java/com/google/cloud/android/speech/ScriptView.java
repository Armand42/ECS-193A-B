package com.google.cloud.android.speech;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ScriptView extends AppCompatActivity {
    String speechName;
    String scriptText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_view);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setTitle(speechName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24px);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
        setTitle("Script: " + speechName);

        try {
            scriptText = FileService.readFromFile(intent.getStringExtra("filePath"));
            setScriptText();
        } catch (IOException e) {
            e.printStackTrace();
            Toast readToast = Toast.makeText(getApplicationContext(),
                              e.toString(), Toast.LENGTH_SHORT);
            readToast.show();
        }
    }

    private void setScriptText() {
        // Get text body
        TextView scriptBody = (TextView) findViewById(R.id.scriptBody);
        // Make script scrollable
        scriptBody.setMovementMethod(new ScrollingMovementMethod());

        // Set text of scriptBody to be what we read from the file
        scriptBody.setText(scriptText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }
}
