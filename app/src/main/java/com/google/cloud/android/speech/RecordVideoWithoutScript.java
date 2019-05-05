package com.google.cloud.android.speech;

import android.os.Bundle;
import android.view.Menu;

public class RecordVideoWithoutScript extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_video_without_script);
        getIntent();
        this.setTitle("Record a Speech");
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, com.google.cloud.android.speech.Camera2VideoFragment.newInstance())
                    .commit();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }

}
