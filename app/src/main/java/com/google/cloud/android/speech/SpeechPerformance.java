package com.google.cloud.android.speech;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;


public class SpeechPerformance extends BaseActivity {
    String speechName;
    String apiResult;
    String[] command;
    Boolean videoPlayback;
    FFmpeg ffmpeg;
    private static String SPEECH_SCRIPT_PATH;
    private static String VIDEO_FILE_PATH;
    private static String AUDIO_FILE_PATH;
    private static final String TAG = "MyActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_performance);
        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
        SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);

        final File dir = getApplicationContext().getDir(speechName, MODE_PRIVATE);
        SPEECH_SCRIPT_PATH = dir.getAbsolutePath() + "/" + speechName + "apiResult";
        videoPlayback = sharedPreferences.getBoolean("videoPlayback", false);
        Log.d("SPEECH_SCRIPT_PATH", SPEECH_SCRIPT_PATH);



        if(videoPlayback) {
            VIDEO_FILE_PATH = sharedPreferences.getString("videoFilePath", null);

            AUDIO_FILE_PATH = dir.getAbsolutePath() + "/" + speechName + sharedPreferences.getInt("currVid", -1) + ".wav";

            if(VIDEO_FILE_PATH == null){
                Log.d("VIDEO FILE PATH", "VIDEO PATH NULL");
            }

            try {
                loadFfmpegLibrary();
            } catch (FFmpegNotSupportedException e) {
                e.printStackTrace();
            }

            Log.i("VIDEO_FILE_PATH", VIDEO_FILE_PATH);
            Log.i("AUDIO_FILE_PATH", AUDIO_FILE_PATH);

//        command = new String[]{"-i", VIDEO_FILE_PATH, "-vn", "-f", "s16le", "-acodec", "pcm_s16le" , AUDIO_FILE_PATH};
            command = new String[]{"-i", VIDEO_FILE_PATH, AUDIO_FILE_PATH};
            try {
                executeFfmpegCommand(command);
            } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
            }
        }

    }

    public void loadFfmpegLibrary() throws FFmpegNotSupportedException{
        if(ffmpeg == null) {
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

    public void executeFfmpegCommand(final String[] cmd) throws FFmpegCommandAlreadyRunningException{
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

    public void goToPlayBack(View view){
        Intent intent = new Intent(this, PlayBack_List.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToSpeechView(View view){
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToDiffView(View view) throws FileNotFoundException {
        Intent intent = new Intent(this, DiffView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToMainMenu(View view){
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }


    private void appendToFile(String speechScriptPath, String apiResultText)throws IOException {
        addToSharedPreferences(apiResultText);
        File file = new File(speechScriptPath);
        Log.d(TAG, "APPENDING TO FILE");
        //This point and below is responsible for the write operation
        FileOutputStream outputStream = null;
        try {
            //second argument of FileOutputStream constructor indicates whether
            //to append or create new file if one exists -- for now we're creating a new file
            outputStream = new FileOutputStream(file, true);

            outputStream.write(apiResultText.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void addToSharedPreferences(String apiResultText) {
        //CREATE the shared preference file and add necessary values
        SharedPreferences sharedPref = getSharedPreferences(speechName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("apiResult", SPEECH_SCRIPT_PATH);

        editor.commit();
    }

    private SpeechService mSpeechService;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };


    @Override
    protected void onStart() {
        super.onStart();

        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);


    }

    @Override
    protected void onStop() {
        // Stop Cloud Speech API
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;

        super.onStop();
    }

    public void speechToText() throws FileNotFoundException {
        Path path = get(AUDIO_FILE_PATH);
        InputStream fin = null;
        try {
            fin = newInputStream(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSpeechService.recognizeInputStream(fin);
    }

    public void pressedButton(View view) throws FileNotFoundException {
        File file = new File(AUDIO_FILE_PATH);
        Path path = get(AUDIO_FILE_PATH);
        InputStream fin = null;
        try {
            fin = newInputStream(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mSpeechService.recognizeInputStream(fin);
    }


    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    appendToFile(SPEECH_SCRIPT_PATH, text);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (isFinal) {
                                    apiResult = text;
                                }
                            }
                        });
                }
            };
}