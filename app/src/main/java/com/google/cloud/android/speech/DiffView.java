//package com.google.cloud.android.speech;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.graphics.Color;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.support.v7.widget.Toolbar;
//import android.text.SpannableString;
//import android.text.Spanned;
//import android.text.method.ScrollingMovementMethod;
//import android.text.style.ForegroundColorSpan;
//import android.text.style.UnderlineSpan;
//import android.util.Log;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.LinkedList;
//
//import static com.google.cloud.android.speech.diff_match_patch.Operation.DELETE;
//import static com.google.cloud.android.speech.diff_match_patch.Operation.EQUAL;
//import static com.google.cloud.android.speech.diff_match_patch.Operation.INSERT;
//
//public class DiffView extends AppCompatActivity {
//
//    String scriptText;
//    String speechToText;
//    int scriptStart= -1,  scriptEnd= -1,  speechStart = -1,  speechEnd = -1;
//    //make an arraylist for all the errors
//    ArrayList<Errors> errors = new ArrayList<Errors>();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_diff_view);
//
//        Intent intent = getIntent();
//
//        // Set toolbar
//        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
//        this.setTitle("Diff View");
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24px);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        // Make script viewable
//        //CREATE the shared preference file and get necessary values
//        SharedPreferences sharedPreferences= getSharedPreferences(intent.getStringExtra("speechName"), MODE_PRIVATE);
//        try {
//            scriptText = FileService.readFromFile(sharedPreferences.getString("filepath",null));
//            speechToText = FileService.readFromFile(sharedPreferences.getString("apiResult",null));
//
//            setScriptText();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast readToast = Toast.makeText(getApplicationContext(),
//                    e.toString(), Toast.LENGTH_SHORT);
//            readToast.show();
//        }
//    }
//
//    // Display speech in playback
//    private void setScriptText() {
//
//        SpannableString script = new SpannableString(scriptText);
//        SpannableString speech = new SpannableString(speechToText);
//
//        // New diff object
//        diff_match_patch dmp = new diff_match_patch();
//        dmp.Diff_Timeout = 0;
//
//        int currPos1 = 0, currPos2 = 0, templength = 0;
//        int lastSpace1 =0, lastSpace2 =0, nextSpace1 = 0, nextSpace2=0;
//        diff_match_patch.Operation prevOperation = EQUAL;
//        LinkedList<diff_match_patch.Diff> myDiff;
////        if(scriptText.replaceAll("[^a-zA-z']", "").length()>speechToText.length()*1.25)
////        {
////            //this assumes that the user always starts the speech from the beginning
////            myDiff = dmp.diff_lineMode(scriptText.concat(" ").replaceAll("[^a-zA-z' ]", " ").substring(0,(int)(speechToText.length() * 1.3)).toLowerCase(), speechToText.toLowerCase().replaceAll("[^a-zA-z' ]", " ").concat(" "));
////            script.setSpan(new ForegroundColorSpan(Color.RED), (int)(speechToText.length() * 1.3), scriptText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
////
////        }
////        else {
//            myDiff = dmp.diff_lineMode(scriptText.replaceAll("[^a-zA-z' ]", " ").toLowerCase().concat(" "), speechToText.toLowerCase().replaceAll("[^a-zA-z' ]", " ").concat(" "));
////        }
//        // PRINT OUT ALL CONTENT FROM STRING
//
//
//        for(diff_match_patch.Diff temp: myDiff)
//        {
//            switch(temp.operation)
//            {
//                case EQUAL:
//                    if (prevOperation == DELETE || prevOperation == INSERT)
//                    {
//                        Errors singleError = new Errors( scriptStart,  scriptEnd,  speechStart,  speechEnd);
//                        errors.add(singleError);
//                        scriptStart = scriptEnd = speechStart = speechEnd = -1;
//                    }
//
//                    currPos1 += temp.text.length();
//                    currPos2 += temp.text.length();
//                    lastSpace1 = (scriptText.lastIndexOf(' ',currPos1)==-1) ? lastSpace1 : scriptText.lastIndexOf(' ',currPos1) ;
//                    lastSpace2 = (speechToText.lastIndexOf(' ',currPos2)==-1) ? lastSpace2 : speechToText.lastIndexOf(' ',currPos2);
//                    prevOperation = EQUAL;
//                    break;
//                case INSERT://for 2
//                    templength = temp.text.length();
//                    speechStart = currPos2;
//                    speech.setSpan(new ForegroundColorSpan(Color.RED), currPos2, (speechToText.length()<(currPos2+templength))?(speechToText.length()):(currPos2+=templength), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                    speechEnd = currPos2;
//                    //speech.setSpan(new UnderlineSpan(),speechStart,speechEnd>speechToText.length()?speechToText.length():speechEnd,Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                    prevOperation = INSERT;
//
//                    break;
//                case DELETE: //for 1
//                    templength = temp.text.length();
//                    scriptStart = currPos1;
//                    script.setSpan(new ForegroundColorSpan(Color.RED), currPos1, (scriptText.length()<(currPos1+templength))?(scriptText.length()):(currPos1+=templength), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//                    scriptEnd = currPos1;
//                    //script.setSpan(new UnderlineSpan(),scriptStart,scriptEnd>scriptText.length()?scriptText.length():scriptEnd,Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
//
//                    prevOperation = DELETE;
//                    break;
//            }
//        }
//
//        ignore(scriptText,script,',');
//        ignore(scriptText,script,'.');
//        ignore(scriptText,script,'!');
//        ignore(scriptText,script,'?');
//        ignore(scriptText,script,':');
//        ignore(scriptText,script,';');
//        ignore(scriptText,script,'-');
//
//        // Get text body
//        TextView scriptBody = (TextView) findViewById(R.id.scriptBody);
//
//        // Make script scrollable
//        scriptBody.setMovementMethod(new ScrollingMovementMethod());
//
//        // Set text of scriptBody to be what we read from the file
//        scriptBody.setText(script);
//
//        TextView speechToTextBody = (TextView) findViewById(R.id.speechToTextBody);
//
//        speechToTextBody.setMovementMethod(new ScrollingMovementMethod());
//
//        speechToTextBody.setText(speech);
////        for(Errors test: errors)
////        {
////            Log.e("ERRORS:", test.toString());
////        }
//    }
//
//    void ignore(String text1, SpannableString string1, char c)
//    {
//        int index = 0;
//        while(text1.indexOf(c, index+1)>0)
//        {
//            index = text1.indexOf(c, index+1);
//            string1.setSpan(new ForegroundColorSpan(Color.DKGRAY), index, index+1,Spanned.SPAN_INCLUSIVE_INCLUSIVE);
//
//        }
//    }
//}
