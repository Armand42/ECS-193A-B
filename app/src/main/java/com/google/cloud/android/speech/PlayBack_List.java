package com.google.cloud.android.speech;

import android.content.Intent;
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

import org.w3c.dom.Text;

import java.io.File;

public class PlayBack_List extends AppCompatActivity {
    ListView listView;
    String[] fileNames;
    File[] filePathNames;

    String speechName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back__list);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setTitle(speechName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24px);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.setTitle("Speeches");

        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");

        File dir = getDir(speechName, MODE_PRIVATE);


        //get file names
        fileNames = dir.list();
        filePathNames= dir.listFiles();


        TextView noVid = (TextView) findViewById(R.id.text_view_id);

        if (fileNames != null) {
            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNames);
//            Log.i("filenames",fileNames[0]);
            // Connect this adapter to a listview to be populated
            listView = (ListView) findViewById(R.id.speechNames);
            listView.setAdapter(itemsAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String videoName = filePathNames[position].toString();
                    goToPlayBack(view, videoName);
                }
            });
        }
        if(fileNames.length == 0)
        {
            noVid.setVisibility(View.VISIBLE);
        }
        else
        {
            noVid.setVisibility(View.GONE);
        }
    }
    public void goToPlayBack(View view, String videoName){
        Intent intent = new Intent(this, PlayBack.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("videoName", videoName);
        Log.i("VIDEONAME in playback:", videoName);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }


}
