package com.google.cloud.android.speech;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlayBack_List extends AppCompatActivity {
    ListView listView;
    File fileNames[], dir;
    ArrayList<PlaybackListItem> playbackListItems;

    private String speechName, SPEECH_FOLDER_PATH;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back__list);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
        sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24px);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.setTitle("Past Runs: " + speechName);

        SPEECH_FOLDER_PATH = getFilesDir() + File.separator + "speechFiles" + File.separator + speechName;

        dir = new File(SPEECH_FOLDER_PATH);

        //get file names
        fileNames = dir.listFiles();

        final List<String> filesToDisplay = new ArrayList<>();

        TextView noVid = findViewById(R.id.text_view_id);

        if (fileNames != null) {

//            for (int i = 0; i < fileNames.length; i++) {
//                if (fileNames[i].getName().startsWith("run")) {
//                    filesToDisplay.add(fileNames[i].getName());
//                }
//            }
//
//            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filesToDisplay);
//
            // Connect this adapter to a listview to be populated
            listView = findViewById(R.id.speechNames);
//
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectedRun = "run" + (position + 1);
                    Log.d("PLAYBACKLIST", "selectedRun is " + position + 1);
                    goToSpeechPerformance(view, selectedRun);
                }
            });

            try {
                getPlaybackListData();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (playbackListItems != null && playbackListItems.size() == 0) {
            noVid.setVisibility(View.VISIBLE);
        } else {
            noVid.setVisibility(View.GONE);
        }
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

    private void getPlaybackListData() throws JSONException {
        playbackListItems = new ArrayList<>();

        for (int i=1; i< dir.listFiles().length; i++){
            String jsonFilePath =  SPEECH_FOLDER_PATH + File.separator + "run" + i + File.separator + "metadata";
            Log.d("playbacklist", "jsonFilePath" + jsonFilePath);
            JSONObject jsonObj = null;
            try {
                jsonObj = new JSONObject(FileService.readFromFile(jsonFilePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Integer runNum = i;
            Integer percentAccuracy = jsonObj.getInt("percentAccuracy");
            String date = jsonObj.getString("dateRecorded");

            playbackListItems.add(new PlaybackListItem("Run " + runNum, date, percentAccuracy));
        }

        listView.setAdapter(new PlaybackListAdapter(PlayBack_List.this, playbackListItems));
    }

}
