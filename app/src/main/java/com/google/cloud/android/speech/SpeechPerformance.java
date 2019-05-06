package com.google.cloud.android.speech;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedList;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;


public class SpeechPerformance extends BaseActivity {
    private String speechName;
    private File dir;
    private static String apiResultPath;
    private static String AUDIO_FILE_PATH;
    private static final String TAG = "MyActivity";
    private SpeechService mSpeechService;

    SharedPreferences sharedPreferences;
    Boolean videoPlaybackState;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_performance);
        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
        sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);

        TextView speechTime = findViewById(R.id.speechTime);
        // Set speech time textview to the elapsed time from this speech
        long timeElapsed = sharedPreferences.getLong("timeElapsed", 0);
        int minutes = (int) timeElapsed / 60000;
        int seconds = (int) timeElapsed % 60000 / 1000;
        speechTime.setText(String.format("Speech time: %02d:%02d", minutes, seconds));

        String speechFolderPath = getApplicationContext().getFilesDir() + File.separator + speechName;
        String newRunFolder = "run" + (sharedPreferences.getInt("currRun", -1) - 1);

        apiResultPath = speechFolderPath + File.separator + newRunFolder + File.separator + "apiResult";

        Log.d("apiResultPath", apiResultPath);
        AUDIO_FILE_PATH = intent.getStringExtra("audioFilePath");
        dialog = new ProgressDialog(this);

        videoPlaybackState = sharedPreferences.getBoolean("videoPlayback", false);
        setAccuracy();
    }

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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(videoPlaybackState) {
            // Prepare Cloud Speech API
            bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onStop() {
        if(videoPlaybackState) {
            // Stop Cloud Speech API
            mSpeechService.removeListener(mSpeechServiceListener);
            unbindService(mServiceConnection);
            mSpeechService = null;
        }
        super.onStop();
    }

    public void goToPlayBack(View view) {
        Intent intent = new Intent(this, PlayBack_List.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToSpeechView(View view) {
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToDiffView() throws FileNotFoundException {
        if (dialog.isShowing()) {
            dialog.hide();
        }
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


    private void appendToFile(String speechScriptPath, String apiResultText) throws IOException {
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
        editor.putString("apiResult", apiResultPath);

        editor.commit();
    }


    public void pressedButton(View view) throws FileNotFoundException {
        if (videoPlaybackState) {
            dialog.setMessage("Preparing your speech!");
            dialog.show();
            Path path = get(AUDIO_FILE_PATH);
            InputStream fin = null;
            try {
                fin = newInputStream(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSpeechService.recognizeInputStream(fin);
        } else {
            goToDiffView();
        }
    }


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
                                try {
                                    goToDiffView();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            };

    private void setAccuracy()
    {
        TextView accuracyPercentage = (TextView) findViewById(R.id.accuracyPercentage);
        String scriptText= "EMPTY SCRIPT FILE :(";
        String speechToText = "EMPTY SPEECH FILE :(";
        diff_match_patch dmp = new diff_match_patch();
        LinkedList<diff_match_patch.Diff> myDiff;
        double deletedWords = 0, insertedWords = 0, incorrectWords = 0 , totalWords = 0;

        diff_match_patch.Operation prevOperation = diff_match_patch.Operation.EQUAL;

        try {
            scriptText = FileService.readFromFile(sharedPreferences.getString("filepath",null));
            // Duplicate for now -- eventually replace with reading most recent speech to text result
            speechToText = FileService.readFromFile(apiResultPath);

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FILE NOT FOUND:", e.toString());
            Toast readToast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_SHORT);
            readToast.show();
        }
        myDiff = dmp.diff_lineMode(scriptText.replaceAll("[^a-zA-z' ]", "").toLowerCase().concat(" "), speechToText.toLowerCase().replaceAll("[^a-zA-z' ]", "").concat(" "));

        for(diff_match_patch.Diff temp : myDiff)
        {
            if(temp.text != " " || temp.text != "")
            {
                String[] words = temp.text.split("\\s+");
                switch (temp.operation)
                {
                    case EQUAL:
                        Log.e("DIFF","EQUAL - "+ temp.text);
                        if (prevOperation == diff_match_patch.Operation.DELETE || prevOperation == diff_match_patch.Operation.INSERT)
                        {
                            double prevIncorrect = Math.max(deletedWords, insertedWords);
                            incorrectWords += prevIncorrect;
                            totalWords += prevIncorrect;
                        }

                        totalWords += words.length;
                        deletedWords = insertedWords = 0;
                        break;
                    case DELETE:
                        Log.e("DIFF","DELETE - "+ temp.text);

                        deletedWords = words.length;
                        prevOperation = diff_match_patch.Operation.DELETE;
                        break;
                    case INSERT:
                        Log.e("DIFF","INSERT - "+ temp.text);

                        insertedWords = words.length;
                        prevOperation = diff_match_patch.Operation.INSERT;
                        break;

                }
            }
        }
        Log.e("ACCURACY:","incorrect / total = " + incorrectWords + "/" + totalWords);
        accuracyPercentage.setText(""+ (100-((int)(100* (incorrectWords/totalWords)))) +"%");

    }

}