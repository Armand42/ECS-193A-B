package com.google.cloud.android.speech;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainMenu extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ListView listView;
    String[] fileNames, fileNamesToDisplay;
    private Toolbar mTopToolbar;
    private final String subTitleText = "Select a speech";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        getIntent();

        // Instantiate toolbar
        mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mTopToolbar.setTitle("Home");
        mTopToolbar.setSubtitle(Html.fromHtml("<font color='#ffffff'>" + subTitleText + "</font>"));
        setSupportActionBar(mTopToolbar);

        getFileNames();
//        Uncomment below for startup redirection
//        else
//        {
//            Intent intent = new Intent(MainMenu.this, NewSpeech.class);
//            startActivity(intent);
//        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Instantiate toolbar
        mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mTopToolbar.setTitle("Home");
        mTopToolbar.setSubtitle(Html.fromHtml("<font color='#ffffff'>" + subTitleText + "</font>"));
        setSupportActionBar(mTopToolbar);

        getFileNames();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            View view = findViewById(R.id.action_add);
            goToNewSpeech(view);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToNewSpeech(View view) {
        Intent intent = new Intent(this, NewSpeech.class);
        intent.putExtra("prevActivity", "mainMenu");
        startActivity(intent);
    }

    public void goToSpeechMenu(View view, String speechName) {
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String speechName = fileNames[position];
        goToSpeechMenu(view, speechName);
    }

    @Override
    protected void onNewIntent(Intent intent){
        setContentView(R.layout.main_menu);
        getIntent();
        this.setTitle("Home");

        // Instantiate toolbar
        mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mTopToolbar.setSubtitle(Html.fromHtml("<font color='#ffffff'>" + subTitleText + "</font>"));

        getFileNames();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void getFileNames(){
        Log.d("MAINMENU", "beginning of getfilenames");
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        File dir = new File(getFilesDir() + File.separator + "speechFiles");
        dir.mkdirs();
        //get file names
        fileNames = dir.list();
        Log.d("MAINMENU", "before if statement");
        listView = findViewById(R.id.speechNames);
        listView.setVisibility(View.VISIBLE);
        TextView empty = (TextView)findViewById(R.id.emptyView);
        empty.setVisibility(View.GONE);
        if (fileNames != null && fileNames.length != 0) {
            fileNamesToDisplay = new String[fileNames.length];

            for (int i = 0; i < fileNames.length; i++) {
                Log.d("MAINMENU", "inside for loop");

                fileNamesToDisplay[i] = defaultPreferences.getString(fileNames[i], "");
            }

            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNamesToDisplay);

            // Connect this adapter to a listview to be populated
            listView.setAdapter(itemsAdapter);

            listView.setOnItemClickListener(this);
        }
        else
        {

            listView.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);

        }
    }

}
