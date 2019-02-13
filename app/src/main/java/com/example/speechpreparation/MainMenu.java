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
    File[] filesfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        getIntent();
        this.setTitle("Speeches");

        File dir = new File(getFilesDir() + File.separator + "speech-scripts");
        // Get all files saved to speech scripts
        filesfile = dir.listFiles();

        //get file names
        String[] filesname = dir.list();

        if (filesfile != null) {
            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filesname);

            // Connect this adapter to a listview to be populated
            listView = (ListView) findViewById(R.id.speechNames);
            listView.setAdapter(itemsAdapter);

            listView.setOnItemClickListener(this);
        }
    }

    public void goToNewSpeech(View view) {
        Intent intent = new Intent(this, NewSpeech.class);
        startActivity(intent);
    }

    public void goToSpeechMenu(View view, String filePath){
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("filePath", filePath);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView temp = (TextView) view;
//        String speechName = listView.getItemAtPosition(position).toString();
        String speechName = filesfile[position].toString();
//        Toast.makeText(getApplicationContext(), Value, Toast.LENGTH_SHORT).show();
        goToSpeechMenu(view, speechName);
    }
}
