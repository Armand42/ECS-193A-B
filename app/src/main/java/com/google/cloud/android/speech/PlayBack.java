package com.google.cloud.android.speech;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

public class PlayBack extends AppCompatActivity {
    String speechName;
    String filePath;
    String videoName;
    String scriptText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_back);

        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
        filePath = intent.getStringExtra("filePath");
        videoName = intent.getStringExtra("videoName");


        VideoView videoView =(VideoView)findViewById(R.id.vdVw);
        //Set MediaController  to enable play, pause, forward, etc options.
        MediaController mediaController= new MediaController(this);
        mediaController.setAnchorView(videoView);
        //Location of Media File
        Uri videoUri = Uri.parse(getVideoFilePath(getApplicationContext()));
        //Starting VideView By Setting MediaController and URI
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.start();
    // Make script viewable
        try {
            scriptText = FileService.readFromFile(intent.getStringExtra("filePath"));
            setScriptText();
        } catch (IOException e) {
            e.printStackTrace();
            Toast readToast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_SHORT);
            readToast.show();
        }
    }


    private String getVideoFilePath(Context context) {
        final File dir = getDir(speechName, MODE_PRIVATE);

        return (dir == null ? "" : (dir.getAbsolutePath() + "/"))
                +  videoName;
    }
    // Display speech in playback
    private void setScriptText() {
        // Get text body
        TextView scriptBody = (TextView) findViewById(R.id.scriptBody);

        // Make script scrollable
        scriptBody.setMovementMethod(new ScrollingMovementMethod());

        // Set text of scriptBody to be what we read from the file
        scriptBody.setText(scriptText);

    }

    // Call scriptview function here somehow?

}