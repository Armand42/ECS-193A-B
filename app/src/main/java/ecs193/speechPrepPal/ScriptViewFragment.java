package ecs193.speechPrepPal;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ecs193.speechPrepPal.R;

public class ScriptViewFragment extends android.support.v4.app.Fragment {

    private static String scriptText;

    public ScriptViewFragment(String body) {
        scriptText = body;
    }

    @Override
    public void onViewCreated (View view,
                               Bundle savedInstanceState) {
        setScriptText();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_script_view, container, false);
    }

    public static TimerFragment newInstance(String body) {
        TimerFragment fragment = new TimerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void setScriptText() {
        // Get text body
        TextView scriptBody = (TextView) getView().findViewById(R.id.scriptBody);
        // Make script scrollable
        scriptBody.setMovementMethod(new ScrollingMovementMethod());

        // Set text of scriptBody to be what we read from the file
        scriptBody.setText(scriptText);
    }
}
