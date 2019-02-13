package com.example.speechpreparation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ScriptView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_view);

        Intent intent = getIntent();
        setTitle(intent.getStringExtra("speechName") + "script");

        try {
            readFromFile(intent.getStringExtra("filePath"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFromFile(String filepath) throws FileNotFoundException, IOException {
        // Create new file object from given filepath
        File file = new File(filepath);

        // Get text body
        TextView scriptBody = (TextView) findViewById(R.id.scriptBody);

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
//            Toast readToast = Toast.makeText(getApplicationContext(),
//                    text, Toast.LENGTH_SHORT);
//            readToast.show();

            // Set text of scriptBody to be what we read from the file
            scriptBody.setText(text);
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
            Toast readToast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_SHORT);
            readToast.show();
        }
    }

}
