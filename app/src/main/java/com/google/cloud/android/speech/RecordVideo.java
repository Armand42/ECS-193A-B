package com.google.cloud.android.speech;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class RecordVideo extends BaseActivity implements IMainActivity, TimerFragment.OnFragmentInteractionListener {
    private String apiResultPath, speechName;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create video record view
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        speechName = intent.getStringExtra("speechName");
        sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);
        setContentView(R.layout.record_video);

        this.setTitle("Record a Speech");

        String speechFolderPath = getApplicationContext().getFilesDir() + File.separator + "speechFiles" + File.separator + speechName;
        String speechRunFolder = "Run " + sharedPreferences.getInt("currRun", -1);

        apiResultPath = speechFolderPath + File.separator + speechRunFolder + File.separator + "apiResult";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        // Start recording
        super.onStart();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, CameraToVideo.newInstance())
                .commit();
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
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (isFinal) {
                                goToSpeechPerformance();
                            }
                        }
                    });
                }
            };

    public void goToSpeechPerformance() {
        Intent intent = new Intent(this, SpeechPerformance.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("prevActivity", "recording");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Start taking action for back press
        final Intent intent = new Intent(RecordVideo.this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        //alerts user to ensure that they want to leave recording
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
    }

    // From timer fragment and associated interface
    @Override
    public void stopButtonPressed(Long speechTimeMs) {
        // Set time elapsed in shared prefs
        SharedPreferences.Editor editor = getSharedPreferences(speechName, MODE_PRIVATE).edit();
        editor.putLong("timeElapsed", speechTimeMs);
        editor.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

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

}