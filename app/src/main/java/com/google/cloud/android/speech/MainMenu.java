package com.google.cloud.android.speech;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainMenu extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ListView listView;
    ArrayList<String> fileNamesToDisplay, fileNames;
    private Toolbar mTopToolbar;
    private final String subTitleText = "Select a speech";
    SharedPreferences defaultPreferences;
    Set<String> speechNameSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        getIntent();

        // Instantiate toolbar
        mTopToolbar = findViewById(R.id.my_toolbar);
        mTopToolbar.setTitle("Home");
        mTopToolbar.setSubtitle(Html.fromHtml("<font color='#ffffff'>" + subTitleText + "</font>"));
        setSupportActionBar(mTopToolbar);

        listView = findViewById(R.id.speechNames);
        registerForContextMenu(listView);
        getFileNames();
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Instantiate toolbar
        mTopToolbar = findViewById(R.id.my_toolbar);
        mTopToolbar.setTitle("Home");
        mTopToolbar.setSubtitle(Html.fromHtml("<font color='#ffffff'>" + subTitleText + "</font>"));
        setSupportActionBar(mTopToolbar);

        getFileNames();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            View view = findViewById(R.id.action_add);
            goToNewSpeech(view);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToNewSpeech(View view) {
        Intent intent = new Intent(this, NewSpeech.class);
        intent.putExtra("prevActivity", "mainMenu");
        startActivity(intent);
    }

    public void goToSpeechMenu(View view, String speechName) {
        Intent intent = new Intent(this, SpeechView.class);
        intent.putExtra("speechName", speechName);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String speechName = fileNames.get(position);
        goToSpeechMenu(view, speechName);
    }

    @Override
    protected void onNewIntent(Intent intent){
        setContentView(R.layout.main_menu);
        getIntent();
        this.setTitle("Home");

        // Instantiate toolbar
        mTopToolbar = findViewById(R.id.my_toolbar);
        mTopToolbar.setSubtitle(Html.fromHtml("<font color='#ffffff'>" + subTitleText + "</font>"));

        getFileNames();
        listView = findViewById(R.id.speechNames);
        registerForContextMenu(listView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void getFileNames(){
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        File dir = new File(getFilesDir() + File.separator + "speechFiles");
        dir.mkdirs();


        fileNames = new ArrayList<>();
        Collections.sort(fileNames);
        Collections.addAll(fileNames, dir.list());

        listView = findViewById(R.id.speechNames);
        listView.setVisibility(View.VISIBLE);

        TextView empty = findViewById(R.id.emptyView);
        empty.setVisibility(View.GONE);
        if (fileNames != null && fileNames.size() != 0) {
            fileNamesToDisplay = new ArrayList<>();

            for (int i = 0; i < fileNames.size(); i++) {
                String displayedName = defaultPreferences.getString(fileNames.get(i), "");
                fileNamesToDisplay.add(displayedName);
            }

            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNamesToDisplay);

            // Connect this adapter to a listview to be populated
            listView.setAdapter(itemsAdapter);
            listView.setOnItemClickListener(this);
        }
        else
        {

            listView.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_popup, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int index = info.position;
        final String oldName = fileNamesToDisplay.get(index);
        speechNameSet = defaultPreferences.getStringSet("speechNameSet", new HashSet<String>());

        int i = item.getItemId();
        if (i == R.id.edit) {
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(50, 0, 30, 0);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Rename: " + oldName)
                    .setPositiveButton("OK",  new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //Do nothing here because we override this button later to change the close behaviour.
                            //However, we still need this because on older versions of Android unless we
                            //pass a handler the button doesn't get instantiated
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .setCancelable(false)
                    .setView(layout);

            final AlertDialog dialog = builder.create();
            final EditText textBox = new EditText(this);
            textBox.setInputType(InputType.TYPE_CLASS_TEXT);
            textBox.setSingleLine();
            textBox.setText(oldName);
            textBox.setSelection(textBox.getText().length());
            layout.addView(textBox, params);


            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Boolean wantToCloseDialog = false;
                    String userInput = textBox.getText().toString().trim();
                    if (userInput.isEmpty()) {
                        textBox.setError("The speech name cannot be empty.");
                    } else if(speechNameSet.contains(userInput)){
                        textBox.setError("This speech name already exists.");
                    } else {
                        saveFile(oldName, userInput, index);
                        wantToCloseDialog = true;
                    }

                    if(wantToCloseDialog)
                        dialog.dismiss();
                }
            });
            return true;
        } else if (i == R.id.deleteRun) {
            deleteSpeech(fileNames.get(index));
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    /* Delete all associated files */
    public void deleteSpeech(final String speechName) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("Delete this speech?")
                .setMessage("All associated runs will be lost.")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        try {
                            speechNameSet = defaultPreferences.getStringSet("speechNameSet", new HashSet<String>());

                            String speechDisplayName = defaultPreferences.getString(speechName, null);
                            //getting speechDisplayName value from set and then removing it from set
                            speechNameSet.remove(speechDisplayName);

                            SharedPreferences.Editor defaultEditor = defaultPreferences.edit();
                            defaultEditor.putStringSet("speechNameSet", speechNameSet);
                            defaultEditor.remove(speechName);
                            defaultEditor.commit();

                            if(speechNameSet.isEmpty()){
                                defaultPreferences.edit().clear().commit();
                            }

                            String SPEECH_FOLDER_PATH = getFilesDir() + File.separator + "speechFiles" + File.separator + speechName;
                            File speechFolder = new File(SPEECH_FOLDER_PATH);
                            FileService.recursiveDelete(speechFolder);

                            if (speechFolder.exists()) {
                                throw new Exception("Error deleting script");
                            }

                            SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);
                            sharedPreferences.edit().clear().commit(); //clears all preferences

                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
                            fileNamesToDisplay.remove(speechDisplayName);

                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Toast toast = Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                })
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_baseline_warning_24px)
                .show();
    }

    public void saveFile(String oldName, String newName, Integer index) {
        String speechFolderName = fileNames.get(index);

//         Check if speech script directory exists
        if (!speechNameSet.contains(newName)) {
            try {
                SharedPreferences.Editor defaultEditor = defaultPreferences.edit();
                speechNameSet.remove(oldName);
                speechNameSet.add(newName);
                defaultEditor.putStringSet("speechNameSet", speechNameSet);
                defaultEditor.putString(speechFolderName, newName);
                defaultEditor.commit();

                ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
                fileNamesToDisplay.set(index, newName);
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        e.toString(), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

}