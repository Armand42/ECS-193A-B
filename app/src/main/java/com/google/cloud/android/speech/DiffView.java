package com.google.cloud.android.speech;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.LinkedList;

import static com.google.cloud.android.speech.diff_match_patch.Operation.DELETE;
import static com.google.cloud.android.speech.diff_match_patch.Operation.EQUAL;
import static com.google.cloud.android.speech.diff_match_patch.Operation.INSERT;

public class DiffView extends AppCompatActivity {

    String scriptText;
    String speechToText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff_view);

        Intent intent = getIntent();

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        this.setTitle("Diff View");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24px);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Make script viewable
        //CREATE the shared preference file and get necessary values
        SharedPreferences sharedPreferences= getSharedPreferences(intent.getStringExtra("speechName"), MODE_PRIVATE);
        try {
            scriptText = FileService.readFromFile(sharedPreferences.getString("filepath",null));
            // Duplicate for now -- eventually replace with reading most recent speech to text result
            speechToText = FileService.readFromFile(sharedPreferences.getString("apiResult",null));

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

        SpannableString script = new SpannableString(scriptText);
        SpannableString speech = new SpannableString(speechToText);

        // New diff object
        diff_match_patch dmp = new diff_match_patch();
        dmp.Diff_Timeout = 0;

        int currPos1 = 0, currPos2 = 0, templength = 0;
        int lastSpace1 =0, lastSpace2 =0, nextSpace1 = 0, nextSpace2=0;
        diff_match_patch.Operation prevOperation = EQUAL;
        LinkedList<diff_match_patch.Diff> me;
        if(scriptText.length()>speechToText.length()*1.25)
        {
            //this assumes that the user always starts the speech from the beginning
            me = dmp.diff_main(scriptText.substring(0,(int)(speechToText.length() * 1.125)).toLowerCase(), speechToText.toLowerCase(), false);
            script.setSpan(new ForegroundColorSpan(Color.RED), (int)(speechToText.length() * 1.125), scriptText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        }
        else {
            me = dmp.diff_main(scriptText.toLowerCase(), speechToText.toLowerCase(), false);
        }
        // PRINT OUT ALL CONTENT FROM STRING


        for(diff_match_patch.Diff temp: me)
        {
            switch(temp.operation)
            {
                case EQUAL:
//                    if (prevOperation == DELETE)
//                    {
//                        String temporary = scriptText.substring(currPos1);
//                        int space = (temporary.indexOf(" ") == -1) ? currPos1 + temp.text.length()  : scriptText.indexOf(' ', currPos1);
//                        script.setSpan(new ForegroundColorSpan(Color.RED), currPos1, space, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                    }
//
//                    if(prevOperation == INSERT) {
//                        String temporary = speechToText.substring(currPos2);
//                        int space = (temporary.indexOf(" ") == -1) ? currPos2 + temp.text.length()  : speechToText.indexOf(' ', currPos2);
//                        speech.setSpan(new ForegroundColorSpan(Color.RED), currPos2, space, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//
//                    }

                    currPos1 += temp.text.length();
                    currPos2 += temp.text.length();
                    lastSpace1 = (scriptText.lastIndexOf(' ',currPos1)==-1) ? lastSpace1 : scriptText.lastIndexOf(' ',currPos1) ;
                    lastSpace2 = (speechToText.lastIndexOf(' ',currPos2)==-1) ? lastSpace2 : speechToText.lastIndexOf(' ',currPos2);
                    prevOperation = EQUAL;
                    break;
                case INSERT://for 2
                    templength = temp.text.length();
                    speech.setSpan(new ForegroundColorSpan(Color.RED), currPos2, currPos2+templength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    currPos2 += templength;
                    prevOperation = INSERT;

                    break;
                case DELETE: //for 1
                    templength = temp.text.length();
                    script.setSpan(new ForegroundColorSpan(Color.RED), currPos1, currPos1+templength, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    currPos1 += templength;
                    prevOperation = DELETE;
                    break;
            }
        }

        ignore(scriptText,script,',');
        ignore(scriptText,script,'.');
        ignore(scriptText,script,'!');
        ignore(scriptText,script,'?');
        ignore(scriptText,script,':');
        ignore(scriptText,script,';');
        ignore(scriptText,script,'-');

        // Get text body
        TextView scriptBody = (TextView) findViewById(R.id.scriptBody);

        // Make script scrollable
        scriptBody.setMovementMethod(new ScrollingMovementMethod());

        // Set text of scriptBody to be what we read from the file
        scriptBody.setText(script);

        TextView speechToTextBody = (TextView) findViewById(R.id.speechToTextBody);

        speechToTextBody.setMovementMethod(new ScrollingMovementMethod());

        speechToTextBody.setText(speech);
    }

    void ignore(String text1, SpannableString string1, char c)
    {
        int index = 0;
        while(text1.indexOf(c, index+1)>0)
        {
            index = text1.indexOf(c, index+1);
            string1.setSpan(new ForegroundColorSpan(Color.DKGRAY), index, index+1,Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        }
    }
}
