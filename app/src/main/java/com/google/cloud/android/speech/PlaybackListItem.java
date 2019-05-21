package com.google.cloud.android.speech;

import android.widget.Button;

public class PlaybackListItem {
    String runNum;
    String runDate;
    String percentAccuracy;

    public PlaybackListItem(String runNum, String runDate, int percentAccuracy) {
        this.runNum = runNum;
        this.runDate = runDate;
        this.percentAccuracy = percentAccuracy + "% Accurate";
    }
}
