package ecs193.speechPrepPal;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ecs193.speechPrepPal.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimerFragment extends Fragment {

    private TextView countdownText;

    private CountDownTimer countDownTimer;
    private long timeLeftInMilliseconds; // 10 mins
    private long totalSpeechLengthMs, countUp;
    private boolean timerRunning, overtime;

    private IMainActivity mIMainActivity;

    OnTimerStopListener callback;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public TimerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TimerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TimerFragment newInstance(Long timeLeftMs, String speechName) {
        TimerFragment fragment = new TimerFragment();
        Bundle args = new Bundle();
        args.putLong("timeLeftMs", timeLeftMs);
        args.putString("speechName", speechName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Save reference to this fragment to use later
        View rootView = inflater.inflate(R.layout.fragment_timer, container, false);

        countdownText = rootView.findViewById(R.id.countdown_text);

        Bundle bundle = getArguments();
        timeLeftInMilliseconds = totalSpeechLengthMs = bundle.getLong("timeLeftMs");
        // Set timer view
        int minutes = (int) timeLeftInMilliseconds / 60000;
        int seconds = (int) timeLeftInMilliseconds % 60000 / 1000;

        String timeText;

        timeText = "" + minutes;
        timeText += ":";
        if (seconds < 10) timeText += "0";
        timeText += seconds;


        countdownText.setText(timeText);
        ;
        // Return the inflated layout for this fragment
        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void setOnTimerStopListener(OnTimerStopListener callback) {
        this.callback = callback;
    }

    public interface OnTimerStopListener {
        public void onTimerStop(Long millisecondsLeft);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Get the context and instantiate interface
        mIMainActivity = (IMainActivity) getActivity();

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void startStop() {
        if (timerRunning) {
            stopTimer();
        } else {
            startTimer();
        }
    }

    public void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMilliseconds = millisUntilFinished;
                updateTimer(timeLeftInMilliseconds);
            }

            @Override
            public void onFinish() {
                startOvertime();
            }
        }.start();

        timerRunning = true;
    }

    public void stopTimer() {
        countDownTimer.cancel();
        timerRunning = false;
        long totalTime;

        if (overtime) {
            // Defined length of our speech plus how much we went over
            totalTime = totalSpeechLengthMs + countUp;
        } else {
            totalTime = totalSpeechLengthMs - timeLeftInMilliseconds;
        }
        mIMainActivity.stopButtonPressed(totalTime);
    }

    /* @milliseconds can either represent the time left (before overtime) or the time elapsed since
    the timer hit zero (after overtime)
     */
    public void updateTimer(long milliseconds) {
        int minutes = (int) milliseconds / 60000;
        int seconds = (int) milliseconds % 60000 / 1000;

        String timeText;

        timeText = "" + minutes;
        timeText += ":";
        if (seconds < 10) timeText += "0";
        timeText += seconds;

        countdownText.setText(timeText);
    }

    /* Timer has reached 0, so now we begin counting up */
    public void startOvertime() {
        countdownText.setTextColor(getResources().getColor(R.color.armandRed));
        // Set timer text to red
        timeLeftInMilliseconds = Long.MAX_VALUE;
        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMilliseconds = millisUntilFinished;
                countUp = Long.MAX_VALUE - timeLeftInMilliseconds + 1000;
                updateTimer(countUp);
            }

            @Override
            public void onFinish() { }
        }.start();

        timerRunning = true;
        overtime = true;
    }
}
