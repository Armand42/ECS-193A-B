package com.google.cloud.android.speech;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.content.SharedPreferences;

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

        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
        setTitle("Script: " + speechName);
        SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);

        try {
//            Log.d("FILEPATH:", sharedPreferences.getString("filepath", null));
            scriptText = FileService.readFromFile(sharedPreferences.getString("filepath", null));
            setScriptText();
        } catch (IOException e) {
            e.printStackTrace();
            Toast readToast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_SHORT);
            readToast.show();
        }
    }

    public void goToEditSpeech(View view) {
        Intent intent = new Intent(this, NewSpeech.class);

        intent.putExtra("filename", speechName);
        intent.putExtra("scriptText", scriptText);
        startActivity(intent);
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
        getMenuInflater().inflate(R.menu.script_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_edit) {
            View view = findViewById(R.id.action_edit);
            goToEditSpeech(view);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ScriptView.this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
        finish();
    }
}
