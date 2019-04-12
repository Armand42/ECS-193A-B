//package com.google.cloud.android.speech;
//
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.net.Uri;
//import android.os.CountDownTimer;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//
//public class Timer extends AppCompatActivity implements TimerFragment.OnFragmentInteractionListener {
//
//    private TextView countdownText;
//    private Button countdownButton;
//
//    private CountDownTimer countDownTimer;
//    private long timeLeftInMilliseconds; // 10 mins
//    private boolean timerRunning;
//    private String speechName;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_timer);
//        Intent intent = getIntent();
//        speechName = intent.getStringExtra("speechName");
//
//        SharedPreferences sharedPreferences = getSharedPreferences(speechName,MODE_PRIVATE);
//
//        countdownText = findViewById(R.id.countdown_text);
////        countdownButton = findViewById(R.id.countdown_button);
//
//        timeLeftInMilliseconds = sharedPreferences.getLong("timerMilliseconds", 600000);
//
////        countdownButton.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                startStop();
////            }
////        });
//
//        if (savedInstanceState == null) {
//            getFragmentManager().beginTransaction()
//                    .replace(R.id.timer_container, com.google.cloud.android.speech.TimerFragment.newInstance(time))
//                    .commit();
//        }
//    }
//
//    @Override
//    public void onFragmentInteraction(Uri uri){
//        //you can leave it empty
//    }
//
//    public void startStop() {
//        if (timerRunning) {
//            stopTimer();
//        } else {
//            startTimer();
//        }
//    }
//
//    public void startTimer() {
//        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                timeLeftInMilliseconds = millisUntilFinished;
//                updateTimer();
//            }
//
//            @Override
//            public void onFinish() {
//
//            }
//        }.start();
//
//        countdownButton.setText("PAUSE");
//        timerRunning = true;
//    }
//
//    public void stopTimer() {
//        countDownTimer.cancel();
//        countdownButton.setText("START");
//        timerRunning = false;
//    }
//
//    public void updateTimer() {
//        int minutes = (int) timeLeftInMilliseconds / 60000;
//        int seconds = (int) timeLeftInMilliseconds % 60000 / 1000;
//
//        String timeLeftText;
//
//        timeLeftText = "" + minutes;
//        timeLeftText += ":";
//        if (seconds < 10) timeLeftText += "0";
//        timeLeftText += seconds;
//
//        countdownText.setText(timeLeftText);
//    }
//}
