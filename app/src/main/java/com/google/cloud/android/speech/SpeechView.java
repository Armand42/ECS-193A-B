package com.google.cloud.android.speech;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;


import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class SpeechView extends AppCompatActivity {
    File fileNames[], dir;

    private SectionsPageAdapter mSectionsPageAdapter;
    private ViewPager mViewPager;
    private String filePath, speechName, scriptText, prevActivity;
    private Boolean videoPlaybackState, viewScriptState, timerdisplayState;
    private SharedPreferences defaultPreferences;

    String SPEECH_FOLDER_PATH;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_view);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        // Get speech name to be added to shared preferences
        speechName = intent.getStringExtra("speechName");
        prevActivity = intent.getStringExtra("prevActivity");
        SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final File dir = getDir(speechName, MODE_PRIVATE);
        filePath = sharedPreferences.getString("filepath", "error");
        videoPlaybackState = sharedPreferences.getBoolean("videoPlayback", false);
        viewScriptState = sharedPreferences.getBoolean("displaySpeech", false);
        timerdisplayState = sharedPreferences.getBoolean("timerDisplay", false);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Speech View");
        toolbar.setSubtitle(defaultPreferences.getString(speechName, null));
        toolbar.setNavigationIcon(R.drawable.ic_baseline_home_24px);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMainMenu(view);

            }
        });

        // Set script to be viewable
        try {
            scriptText = FileService.readFromFile(sharedPreferences.getString("filepath", null));
        } catch (IOException e) {
            e.printStackTrace();
            Toast readToast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_SHORT);
            readToast.show();
        }

        getFileNames();

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);
        // Set up layout for splitting the tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onStart() {
        // Start recording a new speech
        super.onStart();

        if(prevActivity != null && prevActivity.equals("speechPerformance")){
            TabLayout tabLayout = findViewById(R.id.tabs);
            TabLayout.Tab tab = tabLayout.getTabAt(0);
            tab.select();
        }
    }
    // Set up the split view between past runs and script
    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new PastRunsFragment(speechName, fileNames, SPEECH_FOLDER_PATH, dir), "Your past runs");
        adapter.addFragment(new ScriptViewFragment(scriptText), "Your script");
        viewPager.setAdapter(adapter);
    }

    // Go to settings if settings icon is tapped
    public void goToSpeechSettings(View view) {
        Intent intent = new Intent(this, SpeechSettings.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }
    // Determine if user wishes to record video or audio
    public void goToSpeechRecord(View view) {
        Intent intent;
        // Only video
        if (videoPlaybackState)
            intent = new Intent(this, RecordVideo.class);
        // Only Audio
        else
            intent = new Intent(this, RecordAudio.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }
    // Go to comparison screen
    public void goToDiffView(View view) {
        Intent intent = new Intent(this, DiffView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }
    // Go back to main menu
    public void goToMainMenu(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    // Delete all associated speech runs
    public void deleteSpeech(View view) {
        // Set warning message
        new AlertDialog.Builder(this)
                .setTitle("Delete this speech?")
                .setMessage("All associated speech runs will be lost.")
                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        try {
                            // Getting speechDisplayName value from set and then removing it from set
                            Set<String> speechNameSet = defaultPreferences.getStringSet("speechNameSet", new HashSet<String>());
                            speechNameSet.remove(defaultPreferences.getString(speechName, null));
                            // Edit shared preferences with removal
                            SharedPreferences.Editor defaultEditor = defaultPreferences.edit();
                            defaultEditor.putStringSet("speechNameSet", speechNameSet);
                            defaultEditor.commit();
                            // Delete all files
                            File speechFolder = new File(SPEECH_FOLDER_PATH);
                            recursiveDelete(speechFolder);
                            // If runs cannot be deleted
                            if (speechFolder.exists()) {
                                throw new Exception("Error deleting script");
                            }

                        } catch (Exception e) {
                            Toast toast = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);
                        // Clear all preferences
                        sharedPreferences.edit().clear().apply();
                        // Go back to main menu
                        Intent intent = new Intent(SpeechView.this, MainMenu.class);
                        startActivity(intent);
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_baseline_warning_24px)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.speech_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Toolbar icons to be selected
        if (id == R.id.action_delete) {
            View view = findViewById(R.id.action_delete);
            deleteSpeech(view);
            return true;
        }
        else if (id == R.id.action_settings) {
            View view = findViewById(R.id.action_settings);
            goToSpeechSettings(view);
            return true;
        }
        else if (id == R.id.action_edit) {
            View view = findViewById(R.id.action_edit);
            goToEditSpeech(view);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // Delete all associated runs with a script
    public void recursiveDelete(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                recursiveDelete(child);
            }
        }

        fileOrDirectory.delete();
    }
    // Get name of speech
    private void getFileNames() {
        SPEECH_FOLDER_PATH = getFilesDir() + File.separator + "speechFiles" + File.separator + speechName;

        dir = new File(SPEECH_FOLDER_PATH);

        //get file names
        fileNames = dir.listFiles();
    }

    @Override
    public void onBackPressed() {
        // Go back to main menu
        super.onBackPressed();
        Intent intent = new Intent(SpeechView.this, MainMenu.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
        finish();
    }
    // Allow user to edit speech
    public void goToEditSpeech(View view) {
        Intent intent = new Intent(this, NewSpeech.class);
        intent.putExtra("prevActivity", "scriptView");
        intent.putExtra("speechName", speechName);
        intent.putExtra("scriptText", scriptText);
        startActivity(intent);
    }

}