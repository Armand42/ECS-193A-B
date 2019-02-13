package com.example.speechpreparation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class NewSpeech extends AppCompatActivity {
    String SPEECH_SCRIPT_PATH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_speech);

        SPEECH_SCRIPT_PATH = getFilesDir() + File.separator + "speech-scripts";

        // Check if speech script directory exists
        File f = new File(SPEECH_SCRIPT_PATH);
        if (f.exists() && f.isDirectory()) {
            System.out.println("we already in there fam");
        }
        else { // If not, create it
            File folder = getFilesDir();
            f = new File(folder, "speech-scripts");
            f.mkdir();
        }
    }

    public void goToMainMenu(View view){
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }
    public void saveFile(View view) {
        /* Get speech text from editText */
        EditText editText = (EditText)findViewById(R.id.editText);
        String speechText = editText.getText().toString();

        /* Get speech name from speechText editText */
        EditText speechNameET = (EditText)findViewById(R.id.speechName);
        String speechName = speechNameET.getText().toString();

        /* Write speech text to file */
        writeToFile(speechName, speechText);

        //Read text from file
        //readFromFile(speechName);

        printAllFilesInDir();
    }
//
//    private void readFromFile(String filename) {
//        //Create a new file that points to the root directory, with the given name:
//        File file = new File(getExternalFilesDir(null), filename);
//
//        StringBuilder text = new StringBuilder();
//
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(file));
//            String line;
//
//            while ((line = br.readLine()) != null) {
//                text.append(line);
//                text.append('\n');
//            }
//            br.close();
//            Toast readToast = Toast.makeText(getApplicationContext(),
//                    text, Toast.LENGTH_SHORT);
//            readToast.show();
//        }
//        catch (IOException e) {
//            //You'll need to add proper error handling here
//            Toast readToast = Toast.makeText(getApplicationContext(),
//                    e.toString(), Toast.LENGTH_SHORT);
//            readToast.show();
//        }
//    }
//
    private void writeToFile(String filename, String speechText) {
        //Create a new file in our speech scripts dir with given filename
        File file = new File(SPEECH_SCRIPT_PATH, filename);

        //This point and below is responsible for the write operation
        FileOutputStream outputStream = null;
        try {
            file.createNewFile();
            //second argument of FileOutputStream constructor indicates whether
            //to append or create new file if one exists -- for now we're creating a new file
            outputStream = new FileOutputStream(file, false);

            outputStream.write(speechText.getBytes());
            outputStream.flush();
            outputStream.close();

            // Show notification on successful save
            Toast toast = Toast.makeText(getApplicationContext(),
                    "%s saved".format(filename), Toast.LENGTH_SHORT);
            toast.show();
        } catch (Exception e) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_SHORT);
            toast.show();
            e.printStackTrace();
        }
    }

    private void printAllFilesInDir() {
        File dir = new File(SPEECH_SCRIPT_PATH);
        // Get all files saved to speech scripts
        File[] files = dir.listFiles();
        for (File file : files) {
            System.out.println(file);
        }
    }
}