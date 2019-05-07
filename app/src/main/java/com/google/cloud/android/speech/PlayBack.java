package com.google.cloud.android.speech;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

public class PlayBack extends AppCompatActivity {
    String speechName;
    String selectedRunMediaPath;
    String scriptText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back);

        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
        selectedRunMediaPath = intent.getStringExtra("selectedRunMediaPath");


        VideoView videoView = (VideoView) findViewById(R.id.vdVw);
        //Set MediaController  to enable play, pause, forward, etc options.
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        //Location of Media File
        Uri videoUri = Uri.parse(selectedRunMediaPath);
        //Starting VideView By Setting MediaController and URI
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.start();
        SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);

        // Make script viewable
        try {
            scriptText = FileService.readFromFile(sharedPreferences.getString("filepath", null));
            setScriptText();
        } catch (IOException e) {
            e.printStackTrace();
            Toast readToast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_SHORT);
            readToast.show();
        }
    }


//    private String getVideoFilePath(Context context) {
//        final File dir = getDir(speechName, MODE_PRIVATE);
//
//        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
//                + selectedRun;
//    }

    // Display speech in playback
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

    // Call scriptview function here somehow?

}