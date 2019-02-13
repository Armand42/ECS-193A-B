package com.example.speechpreparation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

public class MainMenu extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        File dir = new File(getFilesDir() + File.separator + "speech-scripts");
        // Get all files saved to speech scripts
        File[] files = dir.listFiles();

        ArrayAdapter<File> itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, files);

        // Connect this adapter to a listview to be populated
        listView = (ListView) findViewById(R.id.speechNames);
        listView.setAdapter(itemsAdapter);

        listView.setOnItemClickListener(this);
    }

    public void goToNewSpeech(View view) {
        Intent intent = new Intent(this, NewSpeech.class);
        startActivity(intent);
    }

    public void goToSpeechMenu(View view, String speechName){
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView temp = (TextView) view;
        String speechName = listView.getItemAtPosition(position).toString();

//        Toast.makeText(getApplicationContext(), Value, Toast.LENGTH_SHORT).show();
        goToSpeechMenu(view, speechName);
    }
}
