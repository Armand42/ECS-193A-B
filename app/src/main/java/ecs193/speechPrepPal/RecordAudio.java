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
 *//*
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

package ecs193.speechPrepPal;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ecs193.speechPrepPal.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;


public class RecordAudio extends AppCompatActivity
        implements MessageDialogFragment.Listener,
        TimerFragment.OnFragmentInteractionListener,
        IMainActivity {

    private TimerFragment timerFragment;

    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

    private static String apiResultPath;


    private String filePath, speechName, scriptText, speechFolderPath, speechRunFolder;
    private String speechToText;
    private Button startButton;
    private TextView listening;
    private Boolean displayTimer, displayScript, recording;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private Long timeLeftInMilliseconds;
    private SpeechService mSpeechService;
    private SharedPreferences sharedPreferences;
    private VoiceRecorder mVoiceRecorder;

    private long timeElapsed;
    private long overtime;


    private long startTime, endTime;

    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            // Start recording voice
            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            // Voice detection
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
                convertBytesToFile(data);
            }
        }

        @Override
        public void onVoiceEnd() {
            // Stop recording voice
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }
        }

    };

    // Connect to speech service
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
        Intent intent = getIntent();

        speechToText = "";
        // Handle metadata
        speechName = intent.getStringExtra("speechName");

        overtime = 0;

        // Get settings from shared preferences for speech
        sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        filePath = sharedPreferences.getString("filepath", "error");
        displayScript = sharedPreferences.getBoolean("displaySpeech", false);
        displayTimer = sharedPreferences.getBoolean("timerDisplay", false);
        recording = false;


        speechToText = "";
        // Set appropriate content view based on settings
        if (displayScript && displayTimer) {
            setContentView(R.layout.activity_record_audio_with_script_timer);
        } else if (displayScript) {
            setContentView(R.layout.record_audio_with_script);
        } else if (displayTimer) {
            setContentView(R.layout.activity_record_audio_without_script_timer);
        } else {
            setContentView(R.layout.record_audio_without_script);
        }
        // Script view enabled
        if (displayScript) {

            try {
                scriptText = FileService.readFromFile(filePath);
                setScriptText();
            } catch (IOException e) {
                e.printStackTrace();
                Toast readToast = Toast.makeText(getApplicationContext(),
                        e.toString(), Toast.LENGTH_SHORT);
                readToast.show();
            }
        }
        // Timer enabled
        if (displayTimer) {
            timeLeftInMilliseconds = sharedPreferences.getLong("timerMilliseconds", 600000);

            // Set timer on layout
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.timer_container, TimerFragment.newInstance(timeLeftInMilliseconds, speechName))
                        .commit();
            }
        }

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_home_24px);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMainMenu(view);

            }
        });
        setTitle("Practice: " + defaultPreferences.getString(speechName, null));

        // Getting speech path info to create a new run
        speechFolderPath = getApplicationContext().getFilesDir() + File.separator + "speechFiles" + File.separator
                + speechName;
        speechRunFolder = "Run " + sharedPreferences.getInt("currRun", -1);

        apiResultPath = speechFolderPath + File.separator + speechRunFolder + File.separator + "apiResult";

        // Handle start button click
        startButton = findViewById(R.id.startButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                timerFragment = (TimerFragment) getFragmentManager().findFragmentById(R.id.timer_container);
                // Stop button behavior
                if (recording) {
                    recording = false;
                    startButton.setEnabled(false);

                    // Timer on
                    if (displayTimer && timerFragment != null) {
                        timerFragment.stopTimer();
                    } else {
                        // Timer off, capture the time we stopped
                        endTime = System.currentTimeMillis();

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putLong("timeElapsed", endTime - startTime);
                        editor.commit();
                    }

                    // Stop listening
                    addToSharedPreferences();
                    try {
                        Log.d("apiResultPath", apiResultPath);

                        appendToFile(apiResultPath, speechToText);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    goToSpeechPerformance();
                } else {

                    recording = true;

                    // Timer on
                    if (displayTimer && timerFragment != null) {
                        timerFragment.startTimer();
                    } else {
                        // Timer off, capture time we started
                        startTime = System.currentTimeMillis();
                    }
                    startButton.setBackground(getResources().getDrawable(R.drawable.ic_stop_red));
                    // Start listening
                    startVoiceRecorder();
                }
            }
        });



    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

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

    public void goToSpeechPerformance() {
        Intent intent = new Intent(this, SpeechPerformance.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("prevActivity", "recording");

        // Intents for speech timing
        intent.putExtra("timeElapsed", timeElapsed);
        intent.putExtra("overtime", overtime);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        // Begin recording
        super.onStart();
        listening = findViewById(R.id.listening);
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

    private void stopSpeechAPI() {
        // Stop Cloud Speech API
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;
    }

    @Override
    protected void onStop() {
        // Stop listening to voice
        stopVoiceRecorder();
        stopSpeechAPI();
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

    // Start voice recording
    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();

        File f = new File(speechFolderPath, speechRunFolder);
        f.mkdirs();

        Set<String> runNames = sharedPreferences.getStringSet("runNames", null);
        runNames.add(speechRunFolder);
        sharedPreferences.edit().putStringSet("runNames", runNames).commit();
    }

    // Stop voice recording
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
                    if (isFinal && !text.isEmpty()) {
                        if (mVoiceRecorder != null)
                            mVoiceRecorder.dismiss();
                        speechToText = speechToText.concat(text).concat(" ");

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (recording && isFinal) {
                                startButton.setEnabled(true);
                                startButton.setBackground(getDrawable(R.drawable.ic_stop_red));
                                if (listening != null)
                                    listening.setVisibility(View.INVISIBLE);
                            } else {
                                startButton.setEnabled(false);
                                startButton.setBackground(getDrawable(R.drawable.microphone));
                                if (listening != null)
                                    listening.setVisibility(View.VISIBLE);

                            }
                        }
                    });

                }
            };

    // Append result of speech to file
    private void appendToFile(String speechScriptPath, String apiResultText) throws IOException {

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
    }

    @Override
    public void stopButtonPressed(Long speechTimeMs) {
        timeElapsed = speechTimeMs;

        // Exceeded max speech length
        if (speechTimeMs >= timeLeftInMilliseconds)
            overtime = speechTimeMs - timeLeftInMilliseconds;
    }

    //META DATA FOR RUNS
    public void addToSharedPreferences() {
        //CREATE the shared preference file and add necessary values
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String runDisplayNameToFilepath = sharedPreferences.getString("runDisplayNameToFilepath", null);
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(runDisplayNameToFilepath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonObj != null) {
            try {
                jsonObj.put(speechRunFolder, speechFolderPath + File.separator + speechRunFolder);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        editor.putString("runDisplayNameToFilepath", jsonObj.toString());
        editor.putString("apiResult", apiResultPath);
        editor.putInt("currRun", 1 + sharedPreferences.getInt("currRun", -1));
        editor.commit();
    }

    //Saves audio into file to allow playback
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
        // Start taking action for back press
        final Intent intent = new Intent(RecordAudio.this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        //checks if we are currently recording, if so alert the user
        if (recording) {
            new AlertDialog.Builder(this)
                    .setTitle("Exit recording?")
                    .setMessage("Your current speech run will be lost.")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Finish action for pressing back
                            startActivity(intent);
                            finish();
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(R.drawable.ic_baseline_warning_24px)
                    .show();
        } else {
            startActivity(intent);
            finish();
        }
    }

    public void goToMainMenu(View view) {
        // Start taking action for home button press
        final Intent intent = new Intent(this, MainMenu.class);
        intent.putExtra("speechName", speechName);
        if (recording) {
            new AlertDialog.Builder(this)
                    .setTitle("Exit recording?")
                    .setMessage("Your current speech run will be lost.")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Finish action for pressing back
                            startActivity(intent);
                            finish();
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(R.drawable.ic_baseline_warning_24px)
                    .show();
        } else {
            startActivity(intent);
            finish();
        }
    }


}