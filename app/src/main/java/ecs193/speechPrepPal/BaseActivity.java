package ecs193.speechPrepPal;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import ecs193.speechPrepPal.R;

public abstract class BaseActivity extends AppCompatActivity {

    // For future use to be extended by other activites so we don't have to keep repeating toolbar logic
    private Toolbar mTopToolbar;

    protected final void onCreate(Bundle savedInstanceState, int layoutId, String title) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24px);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return true;
    }
}
