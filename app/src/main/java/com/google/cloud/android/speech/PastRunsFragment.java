package com.google.cloud.android.speech;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class PastRunsFragment extends Fragment {
    private String speechName, userInputtedRunName;
    private File fileNames[], dir;
    ArrayList<PlaybackListItem> playbackListItems;
    PlaybackListAdapter playbackListAdapter;
    private String SPEECH_FOLDER_PATH;

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

        if (fileNames != null) {

            // Connect this adapter to a listview to be populated
            listView = getView().findViewById(R.id.speechNames);
            registerForContextMenu(listView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectedRun = "run" + (position + 1);
                    Log.d("PLAYBACKLIST", "selectedRun is " + position + 1);
                    goToSpeechPerformance(view, selectedRun);
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

        for (int i = 1; i < dir.listFiles().length; i++) {
            String jsonFilePath = SPEECH_FOLDER_PATH + File.separator + "run" + i + File.separator + "metadata";
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
        int i = item.getItemId();
        if (i == R.id.edit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Rename run");

            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(50, 0, 30, 0);

            final EditText textBox = new EditText(getContext());
            textBox.setInputType(InputType.TYPE_CLASS_TEXT);
            textBox.setSingleLine();
            textBox.setText(playbackListItems.get(index).runNum);
            textBox.setSelection(textBox.getText().length());
            layout.addView(textBox, params);

            builder.setView(layout);

            // Set up the buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    userInputtedRunName = textBox.getText().toString();
                    setNewRunName(userInputtedRunName, index + 1);
                    playbackListAdapter.notifyDataSetChanged();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    private void setNewRunName(String newRunName, int position){
        Integer percentAccuracy = 0;
        String date = "";
        String jsonFilePath = SPEECH_FOLDER_PATH + File.separator + "run" + position + File.separator + "metadata";
        JSONObject jsonObj;
        try {
            jsonObj = new JSONObject(FileService.readFromFile(jsonFilePath));
            jsonObj.put("runDisplayName", newRunName);
            percentAccuracy = jsonObj.getInt("percentAccuracy");
            date = jsonObj.getString("dateRecorded");

            FileService.writeToFile("metadata", jsonObj.toString(),
                    SPEECH_FOLDER_PATH + File.separator + "run" + position);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        playbackListItems.set(position - 1, new PlaybackListItem(newRunName, date, percentAccuracy));
    }


}
