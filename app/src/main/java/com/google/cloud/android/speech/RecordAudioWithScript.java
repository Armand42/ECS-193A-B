/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.android.speech;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class RecordAudioWithScript extends AppCompatActivity
        implements  MessageDialogFragment.Listener,
        TimerFragment.OnFragmentInteractionListener,
        IMainActivity{

    TimerFragment timerFragment;

    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

    private static String apiResultPath;

    private String filePath;
    String speechName;
    String scriptText;
    String speechFolderPath;
    String speechRunFolder;
    StringBuilder savedBytesStringBuilder;
    Button startButton;
    boolean recording = false;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;

    private SpeechService mSpeechService;

    private SharedPreferences sharedPreferences;
    private VoiceRecorder mVoiceRecorder;

    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
                convertBytesToFile(data);
            }
        }

        @Override
        public void onVoiceEnd() {
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }
        }

    };


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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_audio_with_script);
        Intent intent = getIntent();

        savedBytesStringBuilder = new StringBuilder();

        // Handle metadata
        speechName = intent.getStringExtra("speechName");
        sharedPreferences = getSharedPreferences(speechName,MODE_PRIVATE);
        filePath = sharedPreferences.getString("filepath", "error");
        recording = false;
        System.out.println("recording: " + recording);

        //Long timeLeftInMilliseconds = sharedPreferences.getLong("timerMilliseconds", 600000);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setTitle("Practice: " + speechName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24px);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set timer on layout
        //if (savedInstanceState == null) {
        //   getFragmentManager().beginTransaction()
        //          .replace(R.id.timer_container, com.google.cloud.android.speech.TimerFragment.newInstance(timeLeftInMilliseconds, speechName))
        //          .commit();
        // }

        speechFolderPath = getApplicationContext().getFilesDir() + File.separator + "speechFiles" + File.separator
                + speechName;
        speechRunFolder = "run" + sharedPreferences.getInt("currRun",-1);
        File f = new File(speechFolderPath, speechRunFolder);
        f.mkdirs();

        apiResultPath = speechFolderPath + File.separator + speechRunFolder + File.separator + "apiResult";


        // Handle start button click
        startButton = (Button) findViewById(R.id.startButton);
        //startButton.getBackground().setAlpha(200);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                //  timerFragment = (TimerFragment) getFragmentManager().findFragmentById(R.id.timer_container);
                startButton.getBackground().setAlpha(200);

                // Stop button behavior
                if (startButton.getText() == "STOP") {
                    // Stop timer
                    //      timerFragment.stopTimer();

                    // Stop listening
                    stopVoiceRecorder();
                    recording = false;
                    goToSpeechPerformance(getCurrentFocus());
                }
                else {
                    // Start timer
                    //   timerFragment.startTimer();
                    startButton.setText("STOP");

                    // Change UI elements
                    startButton.setBackgroundTintList(getResources().getColorStateList(R.color.cardview_dark_background));

                    // Start listening
                    startVoiceRecorder();
                    recording = true;
                    startButton.setEnabled(false);
                    startButton.setText("RECORDING");
                    startButton.setBackgroundColor(Color.RED);
                }
            }
        });


        try {
            scriptText = FileService.readFromFile(filePath);
//            System.out.print("SCRIPT TEXT: " + scriptText);
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

    private void setScriptText() {
        // Get text body from layout
        TextView scriptBody = (TextView) findViewById(R.id.scriptBody);
        // Make script scrollable
        scriptBody.setMovementMethod(new ScrollingMovementMethod());

        // Set text of scriptBody to be what we read from the file
        scriptBody.setText(scriptText);
    }

    public void goToSpeechPerformance(View view) {
        addToSharedPreferences();
        Intent intent = new Intent(this, SpeechPerformance.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("prevActivity", "recording");
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

        // Start listening to voices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    @Override
    protected void onStop() {
        // Stop listening to voice
        stopVoiceRecorder();

        // Stop Cloud Speech API
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;

        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal) {
                        mVoiceRecorder.dismiss();
                    }
                    if (!TextUtils.isEmpty(text)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    startButton.setEnabled(true);
                                    startButton.setText("STOP");
                                    startButton.setBackgroundColor(getResources().getColor(R.color.primary_dark));
                                    try {
                                        appendToFile(apiResultPath, text);
                                        appendToFile(apiResultPath, " ");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                }
            };


    private void appendToFile(String speechScriptPath, String apiResultText)throws IOException {
        Log.d("AUDIO ONLY", "APPENDING TO FILE");
        File file = new File(speechScriptPath);

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
    @Override
    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty
    }

    @Override
    public void stopButtonPressed(Long speechTimeMs) {
        // Set time elapsed in shared prefs
        SharedPreferences sharedPreferences = this.getSharedPreferences(speechName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // editor.putLong("timeElapsed", speechTimeMs);
        editor.commit();
        goToSpeechPerformance(getCurrentFocus());
    }

    public void addToSharedPreferences() {

        //CREATE the shared preference file and add necessary values
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("apiResult", apiResultPath);
        editor.putInt("currRun",1 + sharedPreferences.getInt("currRun",-1));
        Log.d("ADD TO SHARED PREF", "incrementing curr Run");
        editor.commit();
    }

    private void convertBytesToFile(byte[] bytearray) {
        try {

            File audioFile = new File(speechFolderPath + File.separator + speechRunFolder + File.separator + "audio.wav");
            FileOutputStream fileOutputStream = new FileOutputStream(audioFile, true);
            fileOutputStream.write(bytearray);
            fileOutputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RecordAudioWithScript.this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
        finish();
    }

}