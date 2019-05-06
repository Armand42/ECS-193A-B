package com.google.cloud.android.speech;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import static com.google.cloud.android.speech.diff_match_patch.Operation.DELETE;
import static com.google.cloud.android.speech.diff_match_patch.Operation.EQUAL;
import static com.google.cloud.android.speech.diff_match_patch.Operation.INSERT;

public class DiffViewTest extends AppCompatActivity implements IScrollListener {

    String scriptText, speechToText;

    SpannableString scriptFull, speechFull;

    ObservableScrollView scriptScroll, speechToTextScroll;
    int scriptStart= -1,  scriptEnd= -1,  speechStart = -1,  speechEnd = -1, errorsIndex = 0;

    // Make an arraylist for all the errors
    ArrayList<Errors> errors = new ArrayList<Errors>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff_view_test);

        Intent intent = getIntent();

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        this.setTitle("Diff View");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24px);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Make script viewable
        // Create the shared preference file and get necessary values
        SharedPreferences sharedPreferences= getSharedPreferences(intent.getStringExtra("speechName"), MODE_PRIVATE);
        try {
            scriptText = FileService.readFromFile(sharedPreferences.getString("filepath",null));
            // Duplicate for now -- eventually replace with reading most recent speech to text result

            // TODO: UNCOMMENT AFTER DONE TESTING DIFF VIEW
//            speechToText = FileService.readFromFile(sharedPreferences.getString("apiResult",null));

            setScriptText();
        } catch (IOException e) {
            e.printStackTrace();
            Toast readToast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_SHORT);
            readToast.show();
        }

        // Deal with synced ScrollViews
        scriptScroll = (ObservableScrollView) this.findViewById(R.id.scriptScroll);
        speechToTextScroll = (ObservableScrollView) this.findViewById(R.id.speechToTextScroll);

        scriptScroll.setScrollViewListener(this);
        speechToTextScroll.setScrollViewListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.diff_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id)
        {
            case R.id.action_focus_next:
                setNextErrorFocus();
                return true;
            case R.id.action_focus_prev:
                setPrevErrorFocus();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setScriptText() {
        // FOR TESTING PURPOSES ONLY
        scriptText = "When in the Course of human events it becomes necessary for one people to dissolve the political bands which have connected them with another and to assume among the powers of the earth, the separate and equal station to which the Laws of Nature and of Nature's God entitle them, a decent respect to the opinions of mankind requires that they should declare the causes which impel them to the separation.\n" +
                "\n" +
                "We hold these truths to be self-evident, that all men are created equal, that they are endowed by their Creator with certain unalienable Rights, that among these are Life, Liberty and the pursuit of Happiness. — That to secure these rights, Governments are instituted among Men, deriving their just powers from the consent of the governed, — That whenever any Form of Government becomes destructive of these ends, it is the Right of the People to alter or to abolish it, and to institute new Government, laying its foundation on such principles and organizing its powers in such form, as to them shall seem most likely to effect their Safety and Happiness. Prudence, indeed, will dictate that Governments long established should not be changed for light and transient causes; and accordingly all experience hath shewn that mankind are more disposed to suffer, while evils are sufferable than to right themselves by abolishing the forms to which they are accustomed. But when a long train of abuses and usurpations, pursuing invariably the same Object evinces a design to reduce them under absolute Despotism, it is their right, it is their duty, to throw off such Government, and to provide new Guards for their future security. — Such has been the patient sufferance of these Colonies; and such is now the necessity which constrains them to alter their former Systems of Government. The history of the present King of Great Britain is a history of repeated injuries and usurpations, all having in direct object the establishment of an absolute Tyranny over these States. To prove this, let Facts be submitted to a candid world.\n" +
                "\n" +
                "He has refused his Assent to Laws, the most wholesome and necessary for the public good.\n" +
                "\n" +
                "He has forbidden his Governors to pass Laws of immediate and pressing importance, unless suspended in their operation till his Assent should be obtained; and when so suspended, he has utterly neglected to attend to them.\n" +
                "\n" +
                "He has refused to pass other Laws for the accommodation of large districts of people, unless those people would relinquish the right of Representation in the Legislature, a right inestimable to them and formidable to tyrants only.";
        speechToText = "When in the Course of human events it becomes needed for one people to dissolve the political bounds which have connected them with another and to assume among the powers of the earth, the separate and equal station to which the Laws of Nature and of Nature's God entitle us, a decent respect to the opinions of mankind requires that they should declare the causes which impel them to the separation.\n\n   We hold these truths to be self-evident, that all men are created the same, that they are endowed by their Creator with certain unalienable Rights, that among these are Life, Liberty and the pursuit of Happiness. — That to secure these rights, Governments are instituted among Men, deriving their just powers from the consent of the governed, — That whenever any Form of Government becomes destructive of these ends, it is the Right of the People to alter or to abolish it, and to institute new Government, laying its foundation on such principles and organizing its powers in such form, as to them shall seem most likely to effect their Safety and Happiness. Prudence, indeed, will dictate that Governments long established should not be changed for light and transient causes; and accordingly all experience hath shewn that mankind are more disposed to suffer, while evils are sufferable than to right themselves by abolishing the forms to which they are accustomed. But when a long train of abuses and usurpations, pursuing invariably the same Object evinces a design to reduce them under absolute Despotism, it is their right, it is their duty, to throw off such Government, and to provide new Guards for their future security. — Such has been the patient sufferance of these Colonies; and such is now the necessity which constrains them to alter their former Systems of Government. The history of the present King of Great Britain is a history of repeated injuries and usurpations, all having in direct object the establishment of an absolute Tyranny over these States. To prove this, let Facts be submitted to a candid world. He has refused his Assent to Laws, the most wholesome and necessary for the public good. He has forbidden his Governors to pass Laws of immediate and pressing importance, unless suspended in their operation till his Assent should be obtained; and when so suspended, he has utterly neglected to attend to them.";

        SpannableString script = new SpannableString(scriptText);
        SpannableString speech = new SpannableString(speechToText);

        // New diff object
        diff_match_patch dmp = new diff_match_patch();
        dmp.Diff_Timeout = 0;

        int currPos1 = 0, currPos2 = 0, templength = 0;
        int lastSpace1 =0, lastSpace2 =0, nextSpace1 = 0, nextSpace2=0;
        diff_match_patch.Operation prevOperation = EQUAL;
        LinkedList<diff_match_patch.Diff> me;
        if(scriptText.replaceAll("[^a-zA-z']", "").length()>speechToText.length()*1.25)
        {
            //this assumes that the user always starts the speech from the beginning
            me = dmp.diff_lineMode(scriptText.replaceAll("[^a-zA-z']", " ").substring(0,(int)(speechToText.replaceAll("[^a-zA-z']", " ").length() * 1.3)).toLowerCase(), speechToText.toLowerCase());
            script.setSpan(new ForegroundColorSpan(Color.RED), (int)(speechToText.length() * 1.3), scriptText.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        }
        else {
            me = dmp.diff_lineMode(scriptText.replaceAll("[^a-zA-z']", " ").toLowerCase(), speechToText.replaceAll("[^a-zA-z']", " ").toLowerCase());
        }
        // PRINT OUT ALL CONTENT FROM STRING


        for(diff_match_patch.Diff temp: me)
        {
            switch(temp.operation)
            {
                case EQUAL:
                    if (prevOperation == DELETE || prevOperation == INSERT)
                    {
                        Errors singleError = new Errors( scriptStart,  scriptEnd,  speechStart,  speechEnd);
                        errors.add(singleError);
                        scriptStart = scriptEnd = speechStart = speechEnd = -1;
                    }

                    currPos1 += temp.text.length();
                    currPos2 += temp.text.length();
                    lastSpace1 = (scriptText.lastIndexOf(' ',currPos1)==-1) ? lastSpace1 : scriptText.lastIndexOf(' ',currPos1) ;
                    lastSpace2 = (speechToText.lastIndexOf(' ',currPos2)==-1) ? lastSpace2 : speechToText.lastIndexOf(' ',currPos2);
                    prevOperation = EQUAL;
                    break;
                case INSERT://for 2
                    templength = temp.text.length();
                    speechStart = currPos2;
                    speech.setSpan(new ForegroundColorSpan(Color.RED), currPos2, (speechToText.length()<(currPos2+templength))?(speechToText.length()):(currPos2+=templength), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    speechEnd = currPos2;
                    prevOperation = INSERT;

                    break;
                case DELETE: //for 1
                    templength = temp.text.length();
                    scriptStart = currPos1;
                    script.setSpan(new ForegroundColorSpan(Color.RED), currPos1, (scriptText.length()<(currPos1+templength))?(scriptText.length()):(currPos1+=templength), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    scriptEnd = currPos1;
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

        scriptFull = script;
        speechFull = speech;

        // Set first error focus highlights
        setErrorFocus();

        setDiffTexts();
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

    @Override
    public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {

        if (scrollView == scriptScroll) {
            speechToTextScroll.scrollTo(x, y);
        } else if (scrollView == speechToTextScroll) {
            scriptScroll.scrollTo(x, y);
        }
    }

    private void setErrorFocus()
    {
        Errors error = errors.get(errorsIndex);
        int highlight = getResources().getColor(R.color.highlight);

        // Set background colors to yellow
        speechFull.setSpan(new BackgroundColorSpan(highlight), error.speechStart, error.speechEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        scriptFull.setSpan(new BackgroundColorSpan(highlight), error.scriptStart, error.scriptEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        // Set text color to white
        speechFull.setSpan(new ForegroundColorSpan(Color.WHITE), error.speechStart, error.speechEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        scriptFull.setSpan(new ForegroundColorSpan(Color.WHITE), error.scriptStart, error.scriptEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        setDiffTexts();
    }

    private void unsetErrorFocus()
    {
        Errors error = errors.get(errorsIndex);
        int transparent = getResources().getColor(R.color.transparent);

        // Set background colors to transparent
        speechFull.setSpan(new BackgroundColorSpan(transparent), error.speechStart, error.speechEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        scriptFull.setSpan(new BackgroundColorSpan(transparent), error.scriptStart, error.scriptEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        // Set text color to red
        speechFull.setSpan(new ForegroundColorSpan(Color.RED), error.speechStart, error.speechEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        scriptFull.setSpan(new ForegroundColorSpan(Color.RED), error.scriptStart, error.scriptEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    private void setNextErrorFocus()
    {
        if (errorsIndex < errors.size()-1)
        {
            // Get curr error and set background to transparent
            // Set text to red
            unsetErrorFocus();

            // Go to next error
            errorsIndex++;
            setErrorFocus();
        }
    }

    private void setPrevErrorFocus()
    {
        if (errorsIndex > 0)
        {
            // Get curr error and set background to transparent
            // Set text to red
            unsetErrorFocus();

            // Go to prev error
            errorsIndex--;
            setErrorFocus();
        }
    }

    private void setDiffTexts()
    {
        // Script
        TextView scriptBody = (TextView) findViewById(R.id.scriptBody);
        scriptBody.setText(scriptFull);

        // Speech to text
        TextView speechToTextBody = (TextView) findViewById(R.id.speechToTextBody);
        speechToTextBody.setText(speechFull);
    }
}
