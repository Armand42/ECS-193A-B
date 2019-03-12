package com.google.cloud.android.speech;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class DiffView extends AppCompatActivity {

    String scriptText;
    String speechToText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff_view);

        Intent intent = getIntent();

        this.setTitle("Speeches");
        // Make script viewable
        try {
            scriptText = FileService.readFromFile(intent.getStringExtra("filePath"));
            // Duplicate for now -- eventually replace with reading most recent speech to text result
            speechToText = FileService.readFromFile(intent.getStringExtra("filePath"));
            setScriptText();
        } catch (IOException e) {
            e.printStackTrace();
            Toast readToast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_SHORT);
            readToast.show();
        }
    }

    // Display speech in playback
    private void setScriptText() {
        // Get text body
        TextView scriptBody = (TextView) findViewById(R.id.scriptBody);

        // Make script scrollable
        scriptBody.setMovementMethod(new ScrollingMovementMethod());

        // Set text of scriptBody to be what we read from the file
        scriptBody.setText(scriptText);

        TextView speechToTextBody = (TextView) findViewById(R.id.speechToTextBody);

        speechToTextBody.setMovementMethod(new ScrollingMovementMethod());

        speechToTextBody.setText(speechToText);
    }
}
