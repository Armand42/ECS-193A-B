package com.google.cloud.android.speech;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
    private static String apiResultPath;
    private static String selectedRunMediaPath;
    private static String AUDIO_FILE_PATH;
    private static final String TAG = "MyActivity";
    private SpeechService mSpeechService;
    private String prevActivity;

    SharedPreferences sharedPreferences;
    Boolean videoPlaybackState;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_performance);
        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
        Log.e("speechName", speechName);
        prevActivity = intent.getStringExtra("prevActivity");
        String selectedRun = intent.getStringExtra("selectedRun");
        sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);

        TextView speechTime = findViewById(R.id.speechTime);
        // Set speech time textview to the elapsed time from this speech
        long timeElapsed = sharedPreferences.getLong("timeElapsed", 0);
        int minutes = (int) timeElapsed / 60000;
        int seconds = (int) timeElapsed % 60000 / 1000;
        speechTime.setText(String.format("Speech time: %02d:%02d", minutes, seconds));

        String speechFolderPath = getApplicationContext().getFilesDir() + File.separator
                + speechName.replace(" ", "");
        String speechRunFolder;

        if(!prevActivity.equals("playbackList")){
            int speechRunNum = (sharedPreferences.getInt("currRun", -1) - 1);
            speechRunFolder  = "run" + speechRunNum;
        } else {
            speechRunFolder = selectedRun;
        }

        apiResultPath = speechFolderPath + File.separator + speechRunFolder + File.separator + "apiResult";
        selectedRunMediaPath = speechFolderPath + File.separator + speechRunFolder + File.separator + "audio.wav";
        Log.d("apiResultPath", apiResultPath);
        AUDIO_FILE_PATH = intent.getStringExtra("audioFilePath");
        dialog = new ProgressDialog(this);

        videoPlaybackState = sharedPreferences.getBoolean("videoPlayback", false);
        int percentAccuracy;

        String jsonFilePath = speechFolderPath + File.separator + speechRunFolder + File.separator + "metadata";
        File jsonFile = new File(jsonFilePath);

        if(prevActivity.equals("recording")) {
            percentAccuracy = calculateAccuracy();
            setAccuracy(percentAccuracy);

            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("percentAccuracy", percentAccuracy);
                jsonObj.put("currScriptNum", sharedPreferences.getInt("currScriptNum", -1));
                jsonObj.put("timeElapsed", timeElapsed);
                FileService.writeToFile("metadata", jsonObj.toString(),
                        speechFolderPath + File.separator + speechRunFolder);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                JSONObject jsonObj = new JSONObject(FileService.readFromFile(jsonFilePath));
                percentAccuracy = jsonObj.getInt("percentAccuracy");
                setAccuracy(percentAccuracy);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void goToPastRuns(View view) {
        Intent intent = new Intent(this, PlayBack_List.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToPlayback(View view) {
        Intent intent = new Intent(this, PlayBack.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("selectedRunMediaPath", selectedRunMediaPath);
        startActivity(intent);
    }

    public void goToSpeechView(View view) {
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    public void goToDiffView(View view) throws FileNotFoundException {
        Intent intent = new Intent(this, DiffView.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("apiResultPath", apiResultPath);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }



    public void addToSharedPreferences(String apiResultText) {
        //CREATE the shared preference file and add necessary values
        SharedPreferences sharedPref = getSharedPreferences(speechName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("apiResult", apiResultPath);
        editor.commit();
    }


    private int calculateAccuracy()
    {

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
        myDiff = dmp.diff_lineMode(scriptText.replaceAll("[^a-zA-z' ]", " ").toLowerCase().concat(" "), speechToText.toLowerCase().replaceAll("[^a-zA-z' ]", " ").concat(" "));

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
        int percent = (100-((int)(100* (incorrectWords/totalWords))));
        return percent;
    }

    public void setAccuracy(int percent){
        TextView accuracyPercentage = (TextView) findViewById(R.id.accuracyPercentage);
        accuracyPercentage.setText(""+ percent +"%");
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SpeechPerformance.this, PlayBack_List.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
        finish();
    }
}