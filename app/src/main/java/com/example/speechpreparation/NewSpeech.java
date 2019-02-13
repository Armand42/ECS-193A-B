package com.example.speechpreparation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.OutputStreamWriter;

public class NewSpeech extends AppCompatActivity {

    private final static String STORETEXT = "storetext.txt";
    private EditText txtEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_speech);
        Intent intent = getIntent();
        this.setTitle("Enter a Speech");
        txtEditor=(EditText)findViewById(R.id.editText);

    }
    public void goToMainMenu(View view){
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }

    public void saveClicked(View v) {
//
//        try {
//            Toast
//                    .makeText(this, "Speech Saved", Toast.LENGTH_SHORT)
//                    .show();
//        }
//
//        catch (Throwable t) {
//            Toast
//                    .makeText(this, "Exception: "+t.toString(), Toast.LENGTH_SHORT)
//                    .show();
//        }

        Intent intent = new Intent(this, SpeechView.class);
        startActivity(intent);
    }
}