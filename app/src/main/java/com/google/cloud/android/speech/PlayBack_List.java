package com.google.cloud.android.speech;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayBack_List extends AppCompatActivity {
    ListView listView;
    File fileNames[];

    String speechName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back__list);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");

        setSupportActionBar(toolbar);

        this.setTitle("Past Runs: " + speechName);

        String SPEECH_FOLDER_PATH = getFilesDir() + File.separator + speechName;

        File dir = new File(SPEECH_FOLDER_PATH);

        //get file names
        fileNames = dir.listFiles();

        final List<String> filesToDisplay = new ArrayList<>();

        TextView noVid = findViewById(R.id.text_view_id);

        if (fileNames != null) {

            for (int i = 0; i < fileNames.length; i++) {
                if (fileNames[i].getName().startsWith("run")) {
                    filesToDisplay.add(fileNames[i].getName());
                }
            }

            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filesToDisplay);

            // Connect this adapter to a listview to be populated
            listView = findViewById(R.id.speechNames);
            listView.setAdapter(itemsAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectedRun = filesToDisplay.get(position);
                    Log.d("PLAYBACKLIST", "selectedRun is " + selectedRun);
                    goToSpeechPerformance(view, selectedRun);
                }
            });
        }

        if (filesToDisplay.size() == 0) {
            noVid.setVisibility(View.VISIBLE);
        } else {
            noVid.setVisibility(View.GONE);
        }
    }

    public void goToMainMenu(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_home) {
            View view = findViewById(R.id.action_delete);
            goToMainMenu(view);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    public void goToPlayBack(View view, String selectedRunMediaPath) {
//        Intent intent = new Intent(this, PlayBack.class);
//        intent.putExtra("speechName", speechName);
//        intent.putExtra("selectedRunMediaPath", selectedRunMediaPath);
//        startActivity(intent);
//    }

    public void goToSpeechPerformance(View view, String selectedRun) {
        Intent intent = new Intent(this, SpeechPerformance.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("selectedRun", selectedRun);
        intent.putExtra("prevActivity", "playbackList");
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PlayBack_List.this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
        finish();
    }

}
