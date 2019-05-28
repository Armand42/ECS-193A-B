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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainMenu extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ListView listView;
    private ArrayList<String> fileNamesToDisplay, fileNames;
    private Toolbar mTopToolbar;
    private final String subTitleText = "Select a speech";
    private SharedPreferences defaultPreferences;
    private Set<String> speechNameSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        getIntent();

        setToolbar();

        listView = findViewById(R.id.speechNames);
        registerForContextMenu(listView);
        getFileNames();
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        setToolbar();
        getFileNames();
    }

    public void setToolbar() {
        // Instantiate toolbar
        mTopToolbar = findViewById(R.id.my_toolbar);
        mTopToolbar.setTitle("Home");
        mTopToolbar.setSubtitle(Html.fromHtml("<font color='#ffffff'>" + subTitleText + "</font>"));
        setSupportActionBar(mTopToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //when the + button is clicked
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
    protected void onNewIntent(Intent intent) {
        setContentView(R.layout.main_menu);
        getIntent();
        this.setTitle("Home");

        setToolbar();

        getFileNames();
        listView = findViewById(R.id.speechNames);
        registerForContextMenu(listView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /* gets all existing speeches and fills the list views with the appropriate display names*/
    private void getFileNames() {
        SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //getting all folders in the "speechFiles" directory
        File dir = new File(getFilesDir() + File.separator + "speechFiles");
        dir.mkdirs();

        //adding folders in dir and sorting
        fileNames = new ArrayList<>();
        Collections.sort(fileNames);
        Collections.addAll(fileNames, dir.list());

        listView = findViewById(R.id.speechNames);
        listView.setVisibility(View.VISIBLE);

        TextView noSpeechesMessage = findViewById(R.id.emptyView);
        noSpeechesMessage.setVisibility(View.GONE);
        if (fileNames != null && fileNames.size() != 0) {

            //the folder name is "speech" followed by a number
            //so the fileNamesToDisplay contains the corresponding speechName
            //given by the user upon creation
            fileNamesToDisplay = new ArrayList<>();

            //speech name to display is contained in default preferences
            for (int i = 0; i < fileNames.size(); i++) {
                String displayedName = defaultPreferences.getString(fileNames.get(i), "");
                fileNamesToDisplay.add(displayedName);
            }

            ArrayAdapter<String> itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNamesToDisplay);

            // Connect this adapter to a listview
            listView.setAdapter(itemsAdapter);
            listView.setOnItemClickListener(this);
        } else {
            //hide the list view and show a message when there are no speeches to display
            listView.setVisibility(View.GONE);
            noSpeechesMessage.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_popup, menu);
    }

    /* when an option is chosen from the pop up menu*/
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
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Do nothing here because we override this button later to change the close behaviour.
                            //However, we still need this because on older versions of Android unless we
                            //pass a handler the button doesn't get instantiated
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .setCancelable(false)
                    .setView(layout);

            final AlertDialog dialog = builder.create();

            //edit text to enter new speech name
            final EditText textBox = new EditText(this);
            textBox.setInputType(InputType.TYPE_CLASS_TEXT);
            textBox.setSingleLine();
            textBox.setText(oldName);
            //set cursor to the end of the line
            textBox.setSelection(textBox.getText().length());

            layout.addView(textBox, params);

            dialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dialog.show();

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean wantToCloseDialog = false;
                    String userInput = textBox.getText().toString().trim();
                    if (userInput.isEmpty()) {
                        textBox.setError("The speech name cannot be empty.");
                    } else if (speechNameSet.contains(userInput)) {
                        textBox.setError("This speech name already exists.");
                    } else {
                        saveFile(oldName, userInput, index);
                        wantToCloseDialog = true;
                    }

                    if (wantToCloseDialog)
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

    /* Delete speech and  all associated runs */
    public void deleteSpeech(final String speechName) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("Delete this speech?")
                .setMessage("All associated runs will be lost.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            speechNameSet = defaultPreferences.getStringSet("speechNameSet", new HashSet<String>());

                            //removing speechDisplayName value from set
                            String speechDisplayName = defaultPreferences.getString(speechName, null);
                            speechNameSet.remove(speechDisplayName);

                            //updating default preferences
                            SharedPreferences.Editor defaultEditor = defaultPreferences.edit();
                            defaultEditor.putStringSet("speechNameSet", speechNameSet);
                            defaultEditor.remove(speechName);
                            defaultEditor.commit();

                            //resetting default preferences if there are no more speeches
                            if (speechNameSet.isEmpty()) {
                                defaultPreferences.edit().clear().commit();
                            }

                            //deleting the entire speech folder
                            String SPEECH_FOLDER_PATH = getFilesDir() + File.separator + "speechFiles" + File.separator + speechName;
                            File speechFolder = new File(SPEECH_FOLDER_PATH);
                            FileService.recursiveDelete(speechFolder);

                            //if the speech folder was not deleted
                            if (speechFolder.exists()) {
                                throw new Exception("Error deleting script");
                            }

                            SharedPreferences sharedPreferences = getSharedPreferences(speechName, MODE_PRIVATE);
                            sharedPreferences.edit().clear().commit(); //clears all preferences

                            //update list view
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
                            fileNamesToDisplay.remove(speechDisplayName);
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Log.d("main menu", e.toString());
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Unable to delete speech", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_baseline_warning_24px)
                .show();
    }

    /* replaces the old name for the speech with the new name and updates relevant metadata*/
    public void saveFile(String oldName, String newName, Integer index) {
        String speechFolderName = fileNames.get(index);
        speechNameSet = defaultPreferences.getStringSet("speechNameSet", new HashSet<String>());

        //if the speech name does not already exist
        if (!speechNameSet.contains(newName)) {
            try {
                SharedPreferences.Editor defaultEditor = defaultPreferences.edit();

                //replace the speech name entry in the set and commit to default preferences
                speechNameSet.remove(oldName);
                speechNameSet.add(newName);
                defaultEditor.putStringSet("speechNameSet", speechNameSet);
                defaultEditor.putString(speechFolderName, newName);
                defaultEditor.commit();

                //update the list view with the changed speech name
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) listView.getAdapter();
                fileNamesToDisplay.set(index, newName);
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                Log.d("main menu", e.toString());
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Unable to rename speech", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

}