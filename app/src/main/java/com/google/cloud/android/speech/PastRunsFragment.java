package com.google.cloud.android.speech;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
    private String speechName;
    private File fileNames[], dir;
    ArrayList<PlaybackListItem> playbackListItems;

    private String SPEECH_FOLDER_PATH;

    ListView listView;

    public PastRunsFragment(String name, File[] files, String SFP, File directory) {
        speechName = name;
        fileNames = files;
        SPEECH_FOLDER_PATH = SFP;
        dir = directory;
    }

    @Override
    public void onViewCreated (View view,
                               Bundle savedInstanceState) {
        final List<String> filesToDisplay = new ArrayList<>();

        TextView noVid = getView().findViewById(R.id.text_view_id);

        if (fileNames != null) {

            // Connect this adapter to a listview to be populated
            listView = getView().findViewById(R.id.speechNames);

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

        for (int i=1; i< dir.listFiles().length; i++){
            String jsonFilePath =  SPEECH_FOLDER_PATH + File.separator + "run" + i + File.separator + "metadata";
            Log.d("playbacklist", "jsonFilePath" + jsonFilePath);
            JSONObject jsonObj = null;
            try {
                jsonObj = new JSONObject(FileService.readFromFile(jsonFilePath));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Integer runNum = i;
            Integer percentAccuracy = jsonObj.getInt("percentAccuracy");
            String date = jsonObj.getString("dateRecorded");

            playbackListItems.add(new PlaybackListItem("Run " + runNum, date, percentAccuracy));
        }

        listView.setAdapter(new PlaybackListAdapter(getActivity(), playbackListItems));
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
        intent.putExtra("prevActivity", "speechView");
        startActivity(intent);
    }
}
