package com.google.cloud.android.speech;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecordVideo extends BaseActivity {
    String apiResultPath;
    String speechName;
    Boolean displaySpeech;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_video_with_script);
        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
        this.setTitle("Record a Speech");
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, com.google.cloud.android.speech.Camera2VideoFragment.newInstance())
                    .commit();
        }

        SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);

        if(sharedPreferences.getBoolean("displaySpeech", false)){
            setContentView(R.layout.record_video_with_script);
        } else
        {
            setContentView(R.layout.record_video_without_script);
        }
        String speechFolderPath = getApplicationContext().getFilesDir() + File.separator + speechName;
        String speechRunFolder  = "run" + sharedPreferences.getInt("currRun", -1);


        apiResultPath = speechFolderPath + File.separator + speechRunFolder + File.separator + "apiResult";

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();


        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    public SpeechService mSpeechService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

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

    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                appendToFile(apiResultPath, text);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (isFinal) {
                                goToSpeechPerformance();
                            }
                        }
                    });
                }
            };


    private void appendToFile(String speechScriptPath, String apiResultText) throws IOException {
        File file = new File(speechScriptPath);
        Log.d("RECORDACTIVITY", "APPENDING TO FILE");
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

    public void goToSpeechPerformance() {
        Intent intent = new Intent(this, SpeechPerformance.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("prevActivity", "recordVideo");
        startActivity(intent);
    }
}

