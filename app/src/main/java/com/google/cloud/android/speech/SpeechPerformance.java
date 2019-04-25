package com.google.cloud.android.speech;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
<<<<<<< HEAD
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
=======
import android.widget.EditText;
import android.widget.TextView;

>>>>>>> timer
import java.io.IOException;
import android.content.SharedPreferences;

import org.w3c.dom.Text;

import static com.google.cloud.android.speech.FileService.readFromFile;

public class SpeechPerformance extends BaseActivity {
    String speechName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_performance);
        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
<<<<<<< HEAD
        SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);

        final File dir = getApplicationContext().getDir(speechName, MODE_PRIVATE);
        SPEECH_SCRIPT_PATH = dir.getAbsolutePath() + "/" + speechName + "apiResult";
        videoPlayback = sharedPreferences.getBoolean("videoPlayback", false);
        Log.d("SPEECH_SCRIPT_PATH", SPEECH_SCRIPT_PATH);


=======
>>>>>>> timer

        TextView speechTime = (TextView) findViewById(R.id.speechTime);

        SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);

        // Set speech time textview to the elapsed time from this speech
        long timeElapsed = sharedPreferences.getLong("timeElapsed", 0);
        int minutes = (int) timeElapsed / 60000;
        int seconds = (int) timeElapsed % 60000 / 1000;
        speechTime.setText(String.format("Speech time: %02d:%02d", minutes, seconds));
        if (speechName != null) {
            String scriptText = null;
            try {
                Log.d("FILEPATH:", sharedPreferences.getString("filepath",null));
                scriptText = readFromFile(sharedPreferences.getString("filepath",null));

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (scriptText != null) {

                TextView speechText = (TextView) findViewById(R.id.APIResultView);
                speechText.setText(scriptText);
            }
        }
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

    public void goToDiffView(View view){
        Intent intent = new Intent(this, DiffView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }
<<<<<<< HEAD


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
=======
}
>>>>>>> timer
