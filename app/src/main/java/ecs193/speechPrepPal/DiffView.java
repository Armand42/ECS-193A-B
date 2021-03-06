package ecs193.speechPrepPal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ecs193.speechPrepPal.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import static ecs193.speechPrepPal.diff_match_patch.Operation.DELETE;
import static ecs193.speechPrepPal.diff_match_patch.Operation.EQUAL;
import static ecs193.speechPrepPal.diff_match_patch.Operation.INSERT;

public class DiffView extends AppCompatActivity implements IScrollListener {

    String scriptText, speechToText, speechName, speechRunFolder;

    boolean scriptNewLinesAdded;

    SpannableString scriptFull, speechFull, scriptNewLines, speechNewLines;

    ObservableScrollView scriptScroll, speechToTextScroll;
    int scriptStart= -1,  scriptEnd= -1,  speechStart = -1,  speechEnd = -1, errorsIndex = 0;
    int highlight;

    // Make an arraylist for all the errors
    ArrayList<Errors> errors = new ArrayList<Errors>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff_view_test);

        highlight = getResources().getColor(R.color.colorWarning);

        Intent intent = getIntent();
        String apiResultPath = intent.getStringExtra("apiResultPath");
        speechName = intent.getStringExtra("speechName");
        speechRunFolder = intent.getStringExtra("speechRunFolder");
        Log.d("diff view", "api result path is " + apiResultPath);
        Log.d("diff view", "speechname  is " + speechName);
        Log.d("diff view", "speechRunFOlder " + speechRunFolder);

        // Set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_home_24px);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMainMenu(view);

            }
        });
        this.setTitle("Your Mistakes");
        // Make script viewable
        // Create the shared preference file and get necessary values
        SharedPreferences sharedPreferences = getSharedPreferences(intent.getStringExtra("speechName"), MODE_PRIVATE);
        try {
            scriptText = FileService.readFromFile(sharedPreferences.getString("filepath",null));

            speechToText = FileService.readFromFile(apiResultPath);
            Log.d("diff view", "speech to text result is " + speechToText);
            setScriptText();
        } catch (IOException e) {
            e.printStackTrace();
            Toast readToast = Toast.makeText(getApplicationContext(),
                    e.toString(), Toast.LENGTH_SHORT);
            readToast.show();
        }

        setErrorIndexText();

        // Deal with synced ScrollViews
        scriptScroll = (ObservableScrollView) this.findViewById(R.id.scriptScroll);
        speechToTextScroll = (ObservableScrollView) this.findViewById(R.id.speechToTextScroll);

        // Finish scrolling listener
        setFinishScrollingListeners(scriptScroll);
        setFinishScrollingListeners(speechToTextScroll);

        // Scroll to first error
        scroll();
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
                if(errors.size() != 0)
                    setNextErrorFocus();
                return true;
            case R.id.action_focus_prev:
                if(errors.size() != 0)
                    setPrevErrorFocus();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setScriptText() {
        // To facilitate better scrolling
        calculateNewLines();

        SpannableString script = new SpannableString(scriptText);
        SpannableString speech = new SpannableString(speechToText);

        // New diff object
        diff_match_patch dmp = new diff_match_patch();
        dmp.Diff_Timeout = 0;

        int currPos1 = 0, currPos2 = 0, templength = 0;
        int lastSpace1 =0, lastSpace2 =0, nextSpace1 = 0, nextSpace2=0;
        diff_match_patch.Operation prevOperation = EQUAL;
        LinkedList<diff_match_patch.Diff> me;

        me = dmp.diff_lineMode(scriptText.concat(" END").replaceAll("[^a-zA-z0-9']", " ").toLowerCase(), speechToText.concat(" END").replaceAll("[^a-zA-z0-9']", " ").toLowerCase());
        for(diff_match_patch.Diff temp: me)
        {
            switch(temp.operation)
            {
                case EQUAL:
                    if ((prevOperation == DELETE || prevOperation == INSERT) && ( scriptStart!=-1 || speechStart!=-1)  )
                    {
                        if (scriptStart == -1 || scriptEnd == -1)
                        {
                            scriptStart = scriptEnd = currPos1;
                        }
                        if (speechStart == -1 || speechEnd == -1)
                        {
                            speechStart = speechEnd = currPos2;
                        }

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
                    speech.setSpan(new ForegroundColorSpan(highlight), currPos2, (speechToText.length() < (currPos2 + templength)) ? (speechToText.length()) : (currPos2 += templength), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    speechEnd = currPos2;
                    prevOperation = INSERT;

                    if(temp.text.matches("\\s+")){
                        speechStart = speechEnd = -1;
                    }
                    break;
                case DELETE: //for 1

                    templength = temp.text.length();
                    scriptStart = currPos1;
                    script.setSpan(new ForegroundColorSpan(highlight), currPos1, (scriptText.length() < (currPos1 + templength)) ? (scriptText.length()) : (currPos1 += templength), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    scriptEnd = currPos1;
                    prevOperation = DELETE;

                    if(temp.text.matches("\\s+")){
                        scriptStart = scriptEnd = -1;
                    }

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

        Iterator<Errors> e = errors.iterator();
        while( e.hasNext()){
            Log.e("ERRORS:", e.next().toString());
        }

        // Set first error focus highlights if errors exist
        if(errors.size() != 0) {

            setErrorFocus();
            setDiffTexts();
        }
    }

    public void goToMainMenu(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
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
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DiffView.this, SpeechPerformance.class);
        intent.putExtra("speechName", speechName);
        intent.putExtra("prevActivity", "DiffView");
        intent.putExtra("speechRunFolder", speechRunFolder);
        startActivity(intent);
        finish();
    }

    private void setErrorFocus()
    {
        Errors error = errors.get(errorsIndex);
        int focusTextColor = Color.WHITE;

        // Set background colors
        speechFull.setSpan(new BackgroundColorSpan(highlight), error.speechStart, error.speechEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        scriptFull.setSpan(new BackgroundColorSpan(highlight), error.scriptStart, error.scriptEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        // Set text color
        speechFull.setSpan(new ForegroundColorSpan(focusTextColor), error.speechStart, error.speechEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        scriptFull.setSpan(new ForegroundColorSpan(focusTextColor), error.scriptStart, error.scriptEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

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
        speechFull.setSpan(new ForegroundColorSpan(highlight), error.speechStart, error.speechEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        scriptFull.setSpan(new ForegroundColorSpan(highlight), error.scriptStart, error.scriptEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    private void setNextErrorFocus()
    {
        // Get curr error and set background to transparent
        // Set text to red
        unsetErrorFocus();
        if (errorsIndex < errors.size()-1)
        {
            // Go to next error
            errorsIndex++;
        } else {
            // We're at the last error
            // Go to first error
            errorsIndex = 0;
        }
        setErrorFocus();
        setErrorIndexText();
        scroll();
    }

    private void setPrevErrorFocus()
    {
        unsetErrorFocus();
        if (errorsIndex > 0)
        {
            // Go to prev error
            errorsIndex--;
        } else {
            // We're at the first error
            // Go to last error
            errorsIndex = errors.size()-1;
        }
        setErrorFocus();
        setErrorIndexText();
        scroll();
    }

    private void setDiffTexts()
    {
        // Get TextViews
        final TextView scriptBody = (TextView) findViewById(R.id.scriptBody);
        final TextView speechToTextBody = (TextView) findViewById(R.id.speechToTextBody);

        if (scriptNewLines != null && speechNewLines != null) {
            scriptFull = new SpannableString(TextUtils.concat(scriptFull, scriptNewLines));
            speechFull = new SpannableString(TextUtils.concat(speechFull, speechNewLines));
        }

        // Set TextViews
        scriptBody.setText(scriptFull);
        speechToTextBody.setText(speechFull);
    }

    private void setErrorIndexText()
    {
        TextView errorIndex = (TextView) findViewById(R.id.errorIndex);
        String errorIndexText = String.format("Errors: %d / %d", errorsIndex+1, errors.size());
        errorIndex.setText(errorIndexText);
    }

    private void scroll()
    {
        LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.linearLayout);

        // TextViews
        final TextView speechText = findViewById(R.id.speechToTextBody);
        final TextView scriptText = findViewById(R.id.scriptBody);

        //Observe for a layout change -- needed for scrolling to first error, maybe fix/simplify later
        ViewTreeObserver viewTreeObserver = linearLayout.getViewTreeObserver();
        if (speechText.getLayout() == null || scriptText.getLayout() == null)
        {
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        scrollToPos(scriptText, speechText);
                        setScrollListener();
                    }
                });
            }
        }
        else {
            scrollToPos(scriptText, speechText);
        }
    }

    /* Scrolls to position obtained from line of our errors indicated by the current error
       at errorIndex
     */
    private void scrollToPos(TextView scriptText, TextView speechText)
    {
        removeScrollListener();

        final ObservableScrollView sv = (ObservableScrollView) findViewById(R.id.speechToTextScroll);
        final ObservableScrollView sv2 = (ObservableScrollView) findViewById(R.id.scriptScroll);

        // Get starting position from errors
        final Errors error = errors.get(errorsIndex);

        // Define layouts corresponding to our textviews
        Layout layout = speechText.getLayout();
        Layout layout2 = scriptText.getLayout();

        // Scroll
        sv.scrollTo(0, layout.getLineTop(layout.getLineForOffset(error.speechStart)));
        sv2.scrollTo(0, layout2.getLineTop(layout2.getLineForOffset(error.scriptStart)));

        setScrollListener();
    }

    private void setScrollListener()
    {
        scriptScroll.setScrollViewListener(this);
        speechToTextScroll.setScrollViewListener(this);
    }

    private void setFinishScrollingListeners(final ObservableScrollView scroll)
    {
        scroll.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    scroll.startScrollerTask();
                }

                return false;
            }
        });
        scroll.setOnScrollStoppedListener(new ObservableScrollView.OnScrollStoppedListener() {

            public void onScrollStopped() {

                setScrollListener();
            }
        });
    }

    private void removeScrollListener()
    {
        scriptScroll.setScrollViewListener(null);
        speechToTextScroll.setScrollViewListener(null);
    }

    /* Appends whitespace to keep consistent synced scrolling */
    private void calculateNewLines() {
        // Get TextViews
        final TextView scriptBody = (TextView) findViewById(R.id.scriptBody);
        final TextView speechToTextBody = (TextView) findViewById(R.id.speechToTextBody);

        // Set TextViews
        scriptBody.setText(scriptFull);
        speechToTextBody.setText(speechFull);

        // Handle line number differences for scrolling purposes
        scriptBody.post(new Runnable() {
            @Override
            public void run() {
                int scriptBodyLineCount = scriptBody.getLineCount();
                int speechBodyLineCount = speechToTextBody.getLineCount();
                String scrptNewLines = "";
                String spchNewLines = "";

                // Append whitespace to script since its 75% of the layout and speech is 25%
//                double dNumNewlines = Math.ceil((double)(2.0/3.0) * scriptBodyLineCount);
//                int iNumNewlines = (int)dNumNewlines;

                int iNumNewlines = 14;

                if (!scriptNewLinesAdded) {
                    for (int i=0; i<iNumNewlines; i++) {
                        scrptNewLines += "\n";
                    }
                    scriptNewLinesAdded = true;
                }

                // Append whitespace to the TextView with fewer lines
                if (scriptBodyLineCount < speechBodyLineCount) {
                    int lines = speechBodyLineCount - scriptBodyLineCount;
                    // Make a new string with repeates newlines to account for line difference
                    for (int i=0; i<lines; i++) {
                        scrptNewLines += "\n";
                    }

                }
                else if (speechBodyLineCount < scriptBodyLineCount) {
                    int lines = scriptBodyLineCount - speechBodyLineCount;
                    for (int i=0; i<lines; i++) {
                        scrptNewLines += "\n";
                    }
                }

                scriptNewLines = new SpannableString(scrptNewLines);
                speechNewLines = new SpannableString(spchNewLines);

                setDiffTexts();
            }
        });
    }
}