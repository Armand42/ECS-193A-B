package com.google.cloud.android.speech;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
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

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;

public class PlayBack extends AppCompatActivity implements View.OnClickListener {
    private String speechName, selectedRunMediaPath, scriptText, speechRunFolder;
    private Boolean videoPlaybackState, isAudioPlaying;
    private SharedPreferences sharedPreferences;
    private Integer bufferFrames, lengthOfAudioClip;
    private Button playButton, replayButton;
    private Runnable runnable;
    private AudioTrack audioTrack;
    private Toolbar mTopToolbar;
    FileInputStream fileInputStream;
    private String AUDIO_FILE_PATH;
    FFmpeg ffmpeg;

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

//            extractAudioFromVideo(speechFolderPath, speechRunFolder);

            isAudioPlaying = false;
            fileInputStream = null;
            playButton = findViewById(R.id.playButton);
            replayButton = findViewById(R.id.replay);

            playButton.setOnClickListener(this);
            replayButton.setOnClickListener(this);
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

        // Instantiate toolbar
        mTopToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);
        mTopToolbar.setNavigationIcon(R.drawable.ic_baseline_home_24px);
        mTopToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMainMenu(view);

            }
        });
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String speechDisplayName = defaultPreferences.getString(speechName, null);
        this.setTitle(speechDisplayName + ": Run " + speechRunFolder.charAt(speechRunFolder.length() - 1));
    }



    public void goToMainMenu(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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
        Intent intent = new Intent(PlayBack.this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
        finish();
    }

    private void initAudio() throws IOException {
//        File file = new File(selectedRunMediaPath);
//        byte[] buffer = new byte[(int) file.length()];
//
//        FileInputStream fileInputStream = null;
//
//        try {
//            fileInputStream = new FileInputStream(file);
//            fileInputStream.read(buffer);
//            fileInputStream.close();
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
////        audioFileLength = (float)file.length()/1600/2;
//        // Set and push to audio track..
//        int intSize = android.media.AudioTrack.getMinBufferSize(16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
//        Log.d("PLAYBACK", intSize + "");
//
//        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);
////        bufferFrames = audioTrack.getBufferSizeInFrames();
//        if (audioTrack != null) {
//            Log.d("PLAYBACK", "audio track PLAY ");
//            // Write the byte array to the track
//            Integer bytesWritten = audioTrack.write(buffer, 0, buffer.length);
//            audioTrack.play();
//
//            Log.d("PLAYBACK", "bufferFrames is " + bufferFrames);
//            Log.d("PLAYBACK", "bytesWritten is " + bytesWritten);
//
////            audioTrack.stop();
////            audioTrack.release();
//        } else
//            Log.d("PLAYBACK", "audio track is not initialised ");


        File file = new File(selectedRunMediaPath);
        int audioLength = (int)file.length();
        final byte filedata[] = new byte[audioLength];
            InputStream inputStream = new BufferedInputStream(new FileInputStream(selectedRunMediaPath));
            lengthOfAudioClip = inputStream.read(filedata, 0, audioLength);
            audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 16000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,audioLength, AudioTrack.MODE_STATIC);
            bufferFrames = audioTrack.write(filedata, 0, lengthOfAudioClip);
            audioTrack.play();

//        audioTrack.setNotificationMarkerPosition(lengthOfAudioClip);
//        audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
//            @Override
//            public void onPeriodicNotification(AudioTrack track) {
//                // nothing to do
//            }
//            @Override
//            public void onMarkerReached(AudioTrack track) {
////                audioTrack.write(filedata, 0, lengthOfAudioClip);
//                Log.d("playback", "marker reached");
//                audioTrack.stop();
//                    audioTrack.release();
//                audioTrack.reloadStaticData();
//                playButton.setBackground(getResources().getDrawable(R.drawable.play_arrow_24px));
//            }
//        });



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playButton:
                if (isAudioPlaying) {
                    audioTrack.pause();
                    playButton.setBackground(getResources().getDrawable(R.drawable.play_arrow_24px));
                    isAudioPlaying = false;
                } else {
                  if(audioTrack == null) {
                      try {
                          Log.d("PLAYBACK", "initAudio called");
                          initAudio();
                      } catch (IOException e) {
                          e.printStackTrace();
                      }
                  } else {
                      audioTrack.play();
                  }
                  Log.d("playback", "play pressed initially");
                    isAudioPlaying = true;
                    playButton.setBackground(getResources().getDrawable(R.drawable.pause_24px));
                }
                break;
            case R.id.replay:
                try {
                    reset();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
//            case R.id.backButton:
//                break;
        }
    }

    public void reset() throws IOException {
        audioTrack = null;
        initAudio();
        isAudioPlaying = true;
        playButton.setBackground(getResources().getDrawable(R.drawable.pause_24px));
    }

    public void loadFfmpegLibrary() throws FFmpegNotSupportedException {
        if (ffmpeg == null) {
            ffmpeg = FFmpeg.getInstance(this);
            try {
                ffmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFailure() {
                        Log.e("FFMPEG", "library failed to load");
                    }

                    @Override
                    public void onSuccess() {
                        Log.e("FFMPEG", "library loaded successfully");
                    }

                    @Override
                    public void onFinish() {
                    }
                });
            } catch (FFmpegNotSupportedException e) {
                // Handle if FFmpeg is not supported by device
            }
        }
    }

    public void executeFfmpegCommand(final String[] cmd) throws FFmpegCommandAlreadyRunningException {
        ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
            @Override
            public void onSuccess(String s) {
                Log.e("FFMPEG", "executed successfully" + s);
                super.onSuccess(s);
            }

            @Override
            public void onProgress(String s) {
                Log.e("FFMPEG", "execute in progress" + s);
                super.onProgress(s);
            }

            @Override
            public void onFailure(String s) {
                Log.e("FFMPEG", "execute failed" + s);
                super.onFailure(s);
            }

            @Override
            public void onStart() {
                Log.e("FFMPEG", "execute started");
                super.onStart();
            }

            @Override
            public void onFinish() {
                Log.e("FFMPEG", "execute finished");

                super.onFinish();
            }
        });
    }

    private void extractAudioFromVideo(String speechFolderPath, String newRunFolder) {

        AUDIO_FILE_PATH = speechFolderPath + File.separator + newRunFolder + File.separator
                + "audio.mp3";


        if (selectedRunMediaPath == null) {
            Log.d("VIDEO FILE PATH", "VIDEO PATH NULL");
        }

        try {
            loadFfmpegLibrary();
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }

//        command = new String[]{"-i", VIDEO_FILE_PATH, "-vn", "-f", "s16le", "-acodec", "pcm_s16le" , AUDIO_FILE_PATH};

        String[] command = new String[]{"-f", "s16le","-i", selectedRunMediaPath, "-acodec", "mp3", "-filter:a", "atempo=0.5", "asetrate=r=48K", "-ab", "192k", AUDIO_FILE_PATH};
        try {
            executeFfmpegCommand(command);
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }
}
