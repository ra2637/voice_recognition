package uw.ytchang.vaguard;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;


public class MainActivity extends Activity implements View.OnClickListener {

    private final String TAG = "MainActivity";

    private enum State {TRIGGER, COMMAND, CHALLENGE, STOP, FINISH, REJECT};

    private TextView guide_line, result_tv;
    private Button vaguard_listen_btn, add_user_btn;
    private AndroidSpeechRecognizerManager androidSpeechRecognizerManager;
    private String recordVoicePath, speaker;
    private static AudioRecorderManager audioRecorderManager;
    private final int RECORD_TIME =  5*1000;

    private TextToSpeech ttobj;
    private String challenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        setClickListeners();
    }

    private void findViews() {
        guide_line = (TextView) findViewById(R.id.guide_line);
        guide_line.setText(getString(R.string.press_button));
        result_tv = (TextView) findViewById(R.id.result_tv);
        vaguard_listen_btn = (Button) findViewById(R.id.vaguard_listen_btn);
        add_user_btn = (Button) findViewById(R.id.add_user_btn);
    }


    private void setClickListeners() {
        vaguard_listen_btn.setOnClickListener(this);
        add_user_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (PermissionHandler.checkPermission(this, PermissionHandler.RECORD_AUDIO) &&
                PermissionHandler.checkPermission(this, PermissionHandler.INTERNET)) {

            switch (v.getId()) {
                case R.id.vaguard_listen_btn:
                    if (vaguard_listen_btn.getText().equals(getString(R.string.vaguard_start))) {
                        // Ready to runProgress
                        if (androidSpeechRecognizerManager != null &&
                                !androidSpeechRecognizerManager.ismIsListening()) {
                            androidSpeechRecognizerManager.destroy();
                        }
                        runProgress(State.TRIGGER);
                        vaguard_listen_btn.setText(getString(R.string.vaguard_stop));
                    } else {
                        // Ready to stop
                        runProgress(State.STOP);
                        if (androidSpeechRecognizerManager != null) {
                            androidSpeechRecognizerManager.destroy();
                            androidSpeechRecognizerManager = null;
                        }
                        vaguard_listen_btn.setText(getString(R.string.vaguard_start));
                    }
                    break;
                case R.id.add_user_btn:
                    // TODO: do something to train user voice for alize
                    break;
            }
        } else {
            PermissionHandler.askForPermission(PermissionHandler.RECORD_AUDIO, this);
        }
    }

    private void runProgress(State state) {
        switch (state) {
            case TRIGGER:
                Log.d(TAG, "TRIGGER state");
                guide_line.setText(getString(R.string.you_may_speak));
                androidSpeechRecognize();
                break;
            case COMMAND:
                Log.d(TAG, "COMMAND state");
                result_tv.setText("");
                guide_line.setText("Say \"Transfer money to Yuntai\"");
                recordVoice(State.COMMAND);
                break;
            case CHALLENGE:
                Log.d(TAG, "CHALLENGE state");
                int challengeInt = getRandomNumber();
                String challeng = String.valueOf(challengeInt);
                guide_line.setText("Hi "+ speaker);
                checkChallenge(challeng, State.CHALLENGE);
                break;
            case STOP:
                Log.d(TAG, "STOP state");
                guide_line.setText("");
                result_tv.setText("Process stopped");
                break;
            case FINISH:
                Log.d(TAG, "FINISH state");
                vaguard_listen_btn.setText(getString(R.string.vaguard_start));
                guide_line.setText("Succeeded Authentication!");
                result_tv.setText("Transfer money...");
                break;
            case REJECT:
                Log.d(TAG, "REJECT state");
                vaguard_listen_btn.setText(getString(R.string.vaguard_start));
                guide_line.setText("Failed Authenticate!");
                result_tv.setText("Command rejected\n");
                break;
        }
    }

    private void androidSpeechRecognize() {
        androidSpeechRecognizerManager = new AndroidSpeechRecognizerManager(this, new AndroidSpeechRecognizerManager.onResultsReady() {
            @Override
            public void onResults(ArrayList<String> results) {
                if (results != null && results.size() > 0) {
                    if (results.contains("okay Google") || results.contains("OK Google")) {
                        result_tv.setText("Ok Google");
                        runProgress(State.COMMAND);
                    } else {
                        result_tv.setText("Result not match, please try again");
                        androidSpeechRecognizerManager.listenAgain();
                    }
                } else {
                    result_tv.setText(getString(R.string.no_results_found));
                }
            }
        });
    }

    Random random;

    private int getRandomNumber() {
        if (random == null) {
            random = new Random(new Date().getTime());
        }
        return Math.abs(random.nextInt() % 10000);
    }


    private void recordVoice(State state){
        recordVoicePath = getBaseContext().getFilesDir().getPath()+"/test.wav";
        if(audioRecorderManager == null){
            audioRecorderManager = new AudioRecorderManager(getBaseContext());
        }
        audioRecorderManager.startRecording();
        setRestartVoiceRecorder(RECORD_TIME, state);

    }

    private void setRestartVoiceRecorder(final int delay, final State state){
        Log.d(TAG, "setRestartVoiceRecorder");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                    audioRecorderManager.stopRecording();
                    if(audioRecorderManager.isHasVoice()){
                        voiceRecognition(state);
                    }else{
                        audioRecorderManager.startRecording();
                        setRestartVoiceRecorder(RECORD_TIME, state);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void voiceRecognition(State state) {
        // record the audio
        audioRecorderManager.stopRecording();
        while(true){
            Log.d(TAG, "file is closed: "+ audioRecorderManager.isFileClosed() );
            if(audioRecorderManager.isFileClosed()){
                AlizeVoiceRecognizerManager alizeVoiceRecognizerManager = new AlizeVoiceRecognizerManager(getBaseContext());
                if((speaker = alizeVoiceRecognizerManager.identifySpeaker(recordVoicePath)) != null) {
                    if(state.equals(State.COMMAND)){
                        runProgress(State.CHALLENGE);
                    } else if(state.equals(State.CHALLENGE)){
                        checkContent();
                    }
                }else{
                    speaker = null;
                }
                break;
            }
        }
    }

    private Handler handler;
    private void checkChallenge(final String challenge, final State state){
        final String UTTERID_SPEAKING = "speaking";
        final String UTTERID_FINISH = "finishSpeak";
        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    ttobj.setLanguage(Locale.US);
//                    ttobj.setSpeechRate(0.8f);
                    String utterId = "";
                    for (int i=0; i<challenge.length(); i++){
                        String s = String.valueOf(challenge.charAt(i));

                        if(i == 0){
                            utterId = UTTERID_SPEAKING;
                        } else if(i == challenge.length()-1){
                            utterId = UTTERID_FINISH;
                        }
                        // use TextToSpeech.QUEUE_ADD to make speak char by char
                        ttobj.speak(s, TextToSpeech.QUEUE_ADD, null, utterId);
                    }
                }
            }
        });

        ttobj.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Speaking started.
                if(utteranceId.equals(UTTERID_SPEAKING)){
                    result_tv.setText("Wait...");
                }
            }

            @Override
            public void onDone(String utteranceId) {
                if(utteranceId.equals(UTTERID_FINISH)){
                    guide_line.setText("Now please respond: " + challenge);
                    result_tv.setText("");
                    // record the response, recognizing the voice and the response content
                    recordVoice(state);
                }
            }

            @Override
            public void onError(String utteranceId) {
                Log.d("Error in speakOutChallenge", utteranceId);
            }
        });
    }

    private void checkContent(){
        Log.d(TAG, "checkContent");
        GoogleSpeechRecognizerManager googleSpeechRecognizerManager = new GoogleSpeechRecognizerManager(getApplicationContext());
        if(googleSpeechRecognizerManager.recognizeFile(recordVoicePath, challenge)){
            runProgress(State.FINISH);
        }else{
            runProgress(State.REJECT);
        }
    }



//    private void playMusic() {
//        if (mMediaPlayer == null) {
//            mMediaPlayer = MediaPlayer.create(this, Uri.parse(recordVoicePath));
//            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    // we need to transition to the READY/Home state
//                    Log.d(TAG, "Music Finished");
//                    stopMusic();
//                }
//            });
//        }
//        mMediaPlayer.start();
//    }
//
//    /**
//     * Stops the playback of the MP3 file.
//     */
//    private void stopMusic() {
//        if (mMediaPlayer != null) {
//            mMediaPlayer.stop();
//            mMediaPlayer.release();
//            mMediaPlayer = null;
//        }
//    }
}
