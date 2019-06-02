package ecs193.speechPrepPal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ecs193.speechPrepPal.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class SpeechPerformance extends BaseActivity {

    private String speechName, speechFolderPath, jsonFilePath, apiResultPath, selectedRunMediaPath,
            prevActivity, speechRunFolder;
    private SharedPreferences sharedPreferences;
    private Boolean videoPlaybackState;
    private EditText notes;
    private long timeElapsed, overtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_performance);
        Intent intent = getIntent();
        speechName = intent.getStringExtra("speechName");
        prevActivity = intent.getStringExtra("prevActivity");
        timeElapsed = intent.getLongExtra("timeElapsed", 0);
        overtime = intent.getLongExtra("overtime", 0);

        String selectedRun = intent.getStringExtra("selectedRun");
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);

        notes = findViewById(R.id.note_body);
        notes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        TextView speechTime = findViewById(R.id.speechTime);

        speechFolderPath = getApplicationContext().getFilesDir() + File.separator + "speechFiles" + File.separator
                + speechName;

        //setting the speechRunFolder based on the previous screen
        if (prevActivity.equals("recording")) {
            int speechRunNum = (sharedPreferences.getInt("currRun", -1) - 1);
            speechRunFolder = "Run " + speechRunNum;
        } else if (prevActivity.equals("pastRuns")) {
            speechRunFolder = selectedRun;
        } else if (prevActivity.equals("DiffView")) {
            speechRunFolder = intent.getStringExtra("speechRunFolder");
        }

        apiResultPath = speechFolderPath + File.separator + speechRunFolder + File.separator + "apiResult";

        //setting the correct media path to pass to the playback screen based on whether a video exists
        String videoFilePath = speechFolderPath + File.separator + speechRunFolder + File.separator + "video.mp4";
        File videoFile = new File(videoFilePath);

        if (videoFile.exists()) {
            selectedRunMediaPath = videoFilePath;
        } else {
            selectedRunMediaPath = speechFolderPath + File.separator + speechRunFolder + File.separator + "audio.wav";
        }

        videoPlaybackState = sharedPreferences.getBoolean("videoPlayback", false);

        int percentAccuracy = 100;

        jsonFilePath = speechFolderPath + File.separator + speechRunFolder + File.separator + "metadata";
        File jsonFile = new File(jsonFilePath);

        if (!jsonFile.exists()) {
            //creating a new JSON if the run does not already exist
            percentAccuracy = calculateAccuracy();
            setAccuracy(percentAccuracy);

            final JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("percentAccuracy", percentAccuracy);
                jsonObj.put("currScriptNum", sharedPreferences.getInt("currScriptNum", -1));
                jsonObj.put("timeElapsed", timeElapsed);
                jsonObj.put("videoPlayback", videoPlaybackState);
                jsonObj.put("runDisplayName", speechRunFolder);
                jsonObj.put("note", "Enter notes here.");
                jsonObj.put("overtime", overtime);
                Date todayDate = Calendar.getInstance().getTime();
                SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                String currentDateTimeString = formatter.format(todayDate);

                jsonObj.put("dateRecorded", currentDateTimeString);
                FileService.writeToFile("metadata", jsonObj.toString(),
                        speechFolderPath + File.separator + speechRunFolder);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //reading from the existing JSON if it exists
            try {
                JSONObject jsonObj = new JSONObject(FileService.readFromFile(jsonFilePath));
                percentAccuracy = jsonObj.getInt("percentAccuracy");
                setAccuracy(percentAccuracy);
                String note = jsonObj.getString("note");
                notes.setText(note);

                // Deal with overtime
                overtime = jsonObj.getLong("overtime");
                timeElapsed = jsonObj.getLong("timeElapsed");

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int minutes = (int) timeElapsed / 60000;
        int seconds = (int) timeElapsed % 60000 / 1000;

        String baseTimeInfo = String.format("Speech time: %02d:%02d", minutes, seconds);

        // if at least a second over your target time
        if (overtime >= 1000) {
            int overtimeMins = (int) overtime / 60000;
            int overtimeSecs = (int) overtime % 60000 / 1000;

            String extraTimeInfo = String.format("   ( +%02d:%02d )", overtimeMins, overtimeSecs);

            baseTimeInfo += extraTimeInfo;
        }

        speechTime.setText(baseTimeInfo);

        // Performance message changes based on accuracy.
        setPerformanceMessage(percentAccuracy);

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setSubtitle(defaultPreferences.getString(speechName, null));
        toolbar.setTitle("Speech Performance");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_home_24px);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("ONCLICK", "going home");
                goToMainMenu(view);

            }
        });

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

    public void goToPlayback(View view) {
        saveSpeechNotes();
        Intent intent = new Intent(this, PlayBack.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("selectedRunMediaPath", selectedRunMediaPath);
        intent.putExtra("speechRunFolder", speechRunFolder);
        startActivity(intent);
    }

    public void goToDiffView(View view) {
        saveSpeechNotes();
        Intent intent = new Intent(this, DiffView.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("apiResultPath", apiResultPath);
        intent.putExtra("speechRunFolder", speechRunFolder);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

    public void goToMainMenu(View view) {
        saveSpeechNotes();
        Intent intent = new Intent(this, MainMenu.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    private int calculateAccuracy() {
        String scriptText = "EMPTY SCRIPT FILE :(";
        String speechToText = "EMPTY SPEECH FILE :(";
        diff_match_patch dmp = new diff_match_patch();
        LinkedList<diff_match_patch.Diff> myDiff;
        double deletedWords = 0, insertedWords = 0, incorrectWords = 0, totalWords = 0;

        diff_match_patch.Operation prevOperation = diff_match_patch.Operation.EQUAL;

        try {
            scriptText = FileService.readFromFile(sharedPreferences.getString("filepath", null));
            // Duplicate for now -- eventually replace with reading most recent speech to text result
            speechToText = FileService.readFromFile(apiResultPath);

        } catch (IOException e) {
            e.printStackTrace();
            Toast readToast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_SHORT);
            readToast.show();
        }
        myDiff = dmp.diff_lineMode(scriptText.replaceAll("[^a-zA-z' ]", " ").toLowerCase().concat(" "), speechToText.toLowerCase().replaceAll("[^a-zA-z' ]", " ").concat(" "));

        for (diff_match_patch.Diff temp : myDiff) {
            if (temp.text != " " || temp.text != "") {
                String[] words = temp.text.split("\\s+");
                switch (temp.operation) {
                    case EQUAL:
                        if (prevOperation == diff_match_patch.Operation.DELETE || prevOperation == diff_match_patch.Operation.INSERT) {
                            double prevIncorrect = Math.max(deletedWords, insertedWords);
                            incorrectWords += prevIncorrect;
                            totalWords += prevIncorrect;
                        }

                        totalWords += words.length;
                        deletedWords = insertedWords = 0;
                        break;
                    case DELETE:
                        deletedWords = words.length;
                        prevOperation = diff_match_patch.Operation.DELETE;
                        break;
                    case INSERT:
                        insertedWords = words.length;
                        prevOperation = diff_match_patch.Operation.INSERT;
                        break;

                }
            }
        }
        Log.e("ACCURACY:", "incorrect / total = " + incorrectWords + "/" + totalWords);
        int percent = (100 - ((int) (100 * (incorrectWords / totalWords))));
        return percent;
    }

    public void setAccuracy(int percent) {
        TextView accuracyPercentage = findViewById(R.id.accuracyPercentage);
        accuracyPercentage.setText("Accuracy: " + percent + "%");
    }

    @Override
    public void onBackPressed() {
        saveSpeechNotes();
        Intent intent = new Intent(SpeechPerformance.this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("prevActivity", "speechPerformance");
        startActivity(intent);
        finish();
    }

    private void setPerformanceMessage(int percentAccuracy) {
        TextView performanceMessage = (TextView) findViewById(R.id.performanceMessage);
        String msg;
        if (percentAccuracy == 100) {
            msg = "You didn't make a single mistake! Good job.";

            // Hide diffview button if the user got 100% accuracy
            Button diffViewButton = findViewById(R.id.diffView);
            diffViewButton.setVisibility(View.GONE);
        } else if (percentAccuracy > 70) {
            msg = "You're almost there! Keep practicing.";
        } else {
            msg = "Practice makes perfect, keep it up!";
        }
        performanceMessage.setText(msg);
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(NewSpeech.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void saveSpeechNotes() {
        try {
            JSONObject jsonObj = new JSONObject(FileService.readFromFile(jsonFilePath));
            jsonObj.put("note", notes.getText().toString());
            FileService.writeToFile("metadata", jsonObj.toString(),
                    speechFolderPath + File.separator + speechRunFolder);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}