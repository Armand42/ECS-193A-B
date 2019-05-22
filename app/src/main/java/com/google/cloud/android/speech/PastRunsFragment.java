package com.google.cloud.android.speech;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import static android.content.Context.MODE_PRIVATE;
import static android.support.v4.content.ContextCompat.getSystemService;

public class PastRunsFragment extends Fragment {
    private String speechName, userInputtedRunName;
    private File fileNames[], dir;
    ArrayList<PlaybackListItem> playbackListItems;
    PlaybackListAdapter playbackListAdapter;
    private String SPEECH_FOLDER_PATH;
    SharedPreferences sharedPreferences;

    ListView listView;

    public PastRunsFragment(String name, File[] files, String SFP, File directory) {
        speechName = name;
        fileNames = files;
        SPEECH_FOLDER_PATH = SFP;
        dir = directory;
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        TextView noVid = getView().findViewById(R.id.text_view_id);
        sharedPreferences = getActivity().getSharedPreferences(speechName, MODE_PRIVATE);
        if (fileNames != null) {

            // Connect this adapter to a listview to be populated
            listView = getView().findViewById(R.id.speechNames);
            registerForContextMenu(listView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String runName = playbackListItems.get(position).runNum;
                    String runMetadataFilePath = sharedPreferences.getString("runDisplayNameToFilepath", null);
                    JSONObject jsonObj = null;
                    try {
                        jsonObj = new JSONObject(runMetadataFilePath);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    File runFolder = null;
                    try {
                        runFolder = new File(jsonObj.getString(runName));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    goToSpeechPerformance(view, runFolder.getName());
                }
            });

            try {
                getPlaybackListData();
                playbackListAdapter = new PlaybackListAdapter(getActivity(), playbackListItems);
                listView.setAdapter(playbackListAdapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (playbackListItems != null && playbackListItems.size() == 0) {
            noVid.setVisibility(View.VISIBLE);
        } else {
            noVid.setVisibility(View.GONE);
        }


    }

    private void getPlaybackListData() throws JSONException {
        playbackListItems = new ArrayList<>();

        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            String runFilePath = files[i].getName();
            if (runFilePath.startsWith("run")) {
                String jsonFilePath = files[i].toString() + File.separator + "metadata";
                Log.d("playbacklist", "jsonFilePath" + jsonFilePath);
                Integer percentAccuracy = 0;
                String date = "", runDisplayName = "";
                JSONObject jsonObj = null;
                try {
                    jsonObj = new JSONObject(FileService.readFromFile(jsonFilePath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (jsonObj != null) {
                    percentAccuracy = jsonObj.getInt("percentAccuracy");
                    date = jsonObj.getString("dateRecorded");
                    runDisplayName = jsonObj.getString("runDisplayName");
                }

                playbackListItems.add(new PlaybackListItem(runDisplayName, date, percentAccuracy));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_play_back__list, container, false);
    }

    public void goToSpeechPerformance(View view, String selectedRun) {
        Intent intent = new Intent(getActivity(), SpeechPerformance.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("selectedRun", selectedRun);
        intent.putExtra("prevActivity", "pastRuns");
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.past_runs_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int index = info.position;
        final String originalRunName = playbackListItems.get(index).runNum;
        int i = item.getItemId();
        if (i == R.id.edit) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Rename run");

            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(50, 0, 30, 0);

            final EditText textBox = new EditText(getContext());


            textBox.setInputType(InputType.TYPE_CLASS_TEXT);
            textBox.setSingleLine();
            textBox.setText(originalRunName);
            textBox.setSelection(textBox.getText().length());
            layout.addView(textBox, params);

            builder.setView(layout);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    userInputtedRunName = textBox.getText().toString();
                    setNewRunName(originalRunName, userInputtedRunName, index);
                    playbackListAdapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog alertToShow = builder.create();
            alertToShow.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            alertToShow.show();

            return true;
        } else if (i == R.id.deleteRun) {
            deleteRun(originalRunName, index);
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    private void setNewRunName(String oldRunName, String newRunName, int position) {
        Integer percentAccuracy = 0;
        String date = "";
        String runMetadataFilePath = sharedPreferences.getString("runDisplayNameToFilepath", null);
        JSONObject jsonObj, runJsonObj = null;
        try {
            runJsonObj = new JSONObject(runMetadataFilePath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            String runFolder = runJsonObj.getString(oldRunName);
            runJsonObj.remove(oldRunName);
            runJsonObj.put(newRunName, runFolder);
            jsonObj = new JSONObject(FileService.readFromFile(runFolder + File.separator + "metadata"));

            jsonObj.put("runDisplayName", newRunName);
            percentAccuracy = jsonObj.getInt("percentAccuracy");
            date = jsonObj.getString("dateRecorded");

            FileService.writeToFile("metadata", jsonObj.toString(), runFolder);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("runDisplayNameToFilepath", runJsonObj.toString());
            editor.commit();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        playbackListItems.set(position, new PlaybackListItem(newRunName, date, percentAccuracy));
    }

    private void deleteRun(final String runName, final int position) {
        new android.support.v7.app.AlertDialog.Builder(getContext())
                .setTitle("Delete this run?")
                .setMessage("All associated speech runs will be lost.")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        try {

                            String runMetadataFilePath = sharedPreferences.getString("runDisplayNameToFilepath", null);
                            JSONObject jsonObj = null;
                            try {
                                jsonObj = new JSONObject(runMetadataFilePath);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            String runFolder = jsonObj.getString(runName);
                            jsonObj.remove(runName);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("runDisplayNameToFilepath", jsonObj.toString());
                            editor.commit();

                            File speechFolder = new File(runFolder);
                            recursiveDelete(speechFolder);

                            playbackListItems.remove(position);
                            playbackListAdapter.notifyDataSetChanged();
                            if (speechFolder.exists()) {
                                throw new Exception("Error deleting script");
                            }

                        } catch (Exception e) {
                            Toast toast = Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT);
                            toast.show();
                        }

                    }
                })
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void recursiveDelete(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                recursiveDelete(child);
            }
        }

        fileOrDirectory.delete();
    }
}
