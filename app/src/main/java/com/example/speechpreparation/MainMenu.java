package com.example.speechpreparation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        getIntent();
        this.setTitle("Speeches");

        File dir = new File(getFilesDir() + File.separator + "speech-scripts");
        // Get all files saved to speech scripts
        File[] files = dir.listFiles();

        if(files != null) {
            ArrayAdapter<File> itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, files);

            // Connect this adapter to a listview to be populated
            ListView listView = (ListView) findViewById(R.id.speechNames);
            listView.setAdapter(itemsAdapter);
        }
    }

    public void goToNewSpeech(View view) {
        Intent intent = new Intent(this, NewSpeech.class);
        startActivity(intent);
    }

    public void goToSpeechMenu(View view){
        Intent intent = new Intent(this, SpeechView.class);
        startActivity(intent);
    }

//    public void goToDrive(View view) {
//        Intent intent = new Intent(this, GoogleDriveActivity.class);
//        startActivity(intent);
//    }
}
