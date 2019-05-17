package com.google.cloud.android.speech;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.protobuf.compiler.PluginProtos;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static android.media.AudioTrack.STATE_UNINITIALIZED;

public class PlayBack extends AppCompatActivity implements View.OnClickListener {
    private String speechName, selectedRunMediaPath, scriptText, speechRunFolder;
    private Boolean videoPlaybackState, isAudioPlaying;
    private SharedPreferences sharedPreferences;

    private Button playButton, fastForwardButton, backButton;
    private SeekBar audioOnlySeekbar;
    private MediaPlayer mediaPlayer;
    private Runnable runnable;
    private Handler handler;
    private AudioTrack audioTrack;
    private Toolbar mTopToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
        speechRunFolder = intent.getStringExtra("speechRunFolder");
        selectedRunMediaPath = intent.getStringExtra("selectedRunMediaPath");

        sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);

        String speechFolderPath = getApplicationContext().getFilesDir() + File.separator + "speechFiles" + File.separator
                + speechName;

        String jsonFilePath = speechFolderPath + File.separator + speechRunFolder + File.separator + "metadata";

        try {
            JSONObject jsonObj = new JSONObject(FileService.readFromFile(jsonFilePath));
            videoPlaybackState = jsonObj.getBoolean("videoPlayback");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (videoPlaybackState) {
            setContentView(R.layout.activity_play_back);
            //=============VIDEO PLAYBACK=======================
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

        } else {
            setContentView(R.layout.audio_playback);
            isAudioPlaying = false;
            playButton = findViewById(R.id.playButton);
            fastForwardButton = findViewById(R.id.fastForwardButton);
            backButton = findViewById(R.id.backButton);
            audioOnlySeekbar = findViewById(R.id.audioSeekbar);
            handler = new Handler();
            mediaPlayer = new MediaPlayer();

            fastForwardButton.setOnClickListener(this);
            backButton.setOnClickListener(this);
            playButton.setOnClickListener(this);

            try {
                mediaPlayer.setDataSource(selectedRunMediaPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    audioOnlySeekbar.setMax(mediaPlayer.getDuration());
                    mediaPlayer.start();
                    changeSeekbar();
                }
            });

            audioOnlySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            // Instantiate toolbar
            mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);
            setSupportActionBar(mTopToolbar);
            this.setTitle(speechName + " : Run " + speechRunFolder.charAt(speechRunFolder.length() - 1));
        }

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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(PlayBack.this, PlayBack_List.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
        finish();
    }

    private void changeSeekbar() {
        audioOnlySeekbar.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    changeSeekbar();
                }
            };

            handler.postDelayed(runnable, 1000);
        }
    }

    private void initAudio() throws IOException {
        File file = new File(selectedRunMediaPath);
        byte[] buffer = new byte[(int) file.length()];
        FileInputStream in = null;

        try {
            in = new FileInputStream(file);
            in.read(buffer);
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        audioFileLength = (float)file.length()/1600/2;
        // Set and push to audio track..
        int intSize = android.media.AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Log.d("PLAYBACK", intSize + "");

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);
        if (audioTrack != null) {
            audioTrack.play();
            // Write the byte array to the track
            audioTrack.write(buffer, 0, buffer.length);
//            audioTrack.stop();
//            audioTrack.release();
        } else
            Log.d("PLAYBACK", "audio track is not initialised ");

    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.playButton:
//                if(mediaPlayer.isPlaying()){
//                    mediaPlayer.pause();
//                    playButton.setText(">");
//                }else{
//                    mediaPlayer.start();
//                    playButton.setText("||");
//                    changeSeekbar();
//                }
//                break;
//            case R.id.fastForwardButton:
//                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
//                break;
//            case R.id.backButton:
//                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
//                break;
//        }
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playButton:
                if (isAudioPlaying) {
//                    audioTrack.pause();
                    playButton.setBackground(getResources().getDrawable(R.drawable.play_arrow_24px));
                    isAudioPlaying = false;
                } else {
//                    if(audioTrack == null || audioTrack.getPlayState() == STATE_UNINITIALIZED){
//                        try {
//                            initAudio();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }else{
//                        audioTrack.play();
//                    }
                    isAudioPlaying = true;
                    playButton.setBackground(getResources().getDrawable(R.drawable.pause_24px));
                }
                break;
            case R.id.fastForwardButton:
                break;
            case R.id.backButton:
                break;
        }
    }
}
