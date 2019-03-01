package com.google.cloud.android.speech;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;

public class PlayBack_List extends AppCompatActivity {
    ListView listView;
    File[] filePathNames;
    String[] fileNames;
    String filePath;
    String speechName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back__list);

        this.setTitle("Speeches");

        Intent intent = getIntent();
        filePath = intent.getStringExtra("filePath");
        speechName = intent.getStringExtra("speechName");

        File dir = getDir(speechName, MODE_PRIVATE);
        // Get all files saved to speech scripts
        filePathNames= dir.listFiles();

        //get file names
        fileNames = dir.list();

        TextView noVid = (TextView) findViewById(R.id.text_view_id);

        if (filePathNames != null) {
            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNames);
//            Log.i("filenames",fileNames[0]);
            // Connect this adapter to a listview to be populated
            listView = (ListView) findViewById(R.id.speechNames);
            listView.setAdapter(itemsAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String videoName = fileNames[position];
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
        intent.putExtra("filePath", filePath);
        intent.putExtra("speechName", speechName);
        intent.putExtra("videoName", videoName);
        Log.i("VIDEONAME:", videoName);
        startActivity(intent);
    }


}
