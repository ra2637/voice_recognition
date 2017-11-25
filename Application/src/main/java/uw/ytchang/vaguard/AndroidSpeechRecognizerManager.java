package uw.ytchang.vaguard;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by ra2637 on 11/23/17.
 */

public class AndroidSpeechRecognizerManager {

    protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;

    protected boolean mIsListening;
    private boolean mIsStreamSolo;

    private final static String TAG = "AndroidSpeechRecognizerManager";

    private onResultsReady mListener;

    private Context mContext;

    public AndroidSpeechRecognizerManager(Context context, onResultsReady listener) {
        try {
            mListener = listener;
        } catch (ClassCastException e) {
            Log.e(TAG, e.toString());
        }
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                context.getPackageName());
        startListening();
    }

    public void listenAgain() {
        if (mIsListening) {
            mIsListening = false;
            mSpeechRecognizer.cancel();
            startListening();
        }
    }


    public void startListening() {
        if (!mIsListening) {
            mIsListening = true;
            mIsStreamSolo = true;
            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
        }
    }

    public void stopListening() {

    }

    /**
     * Before application finish, call destroy method
     */
    public void destroy() {
        mIsListening = false;
        if (!mIsStreamSolo) {
            mIsStreamSolo = true;
        }
        Log.d(TAG, "onDestroy");
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.stopListening();
            mSpeechRecognizer.cancel();
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;
        }

    }

    protected class SpeechRecognitionListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
//            startRecording();
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "buffer:" + buffer.toString());
        }

        @Override
        public void onEndOfSpeech() {
//            stopRecording();
        }

        @Override
        public synchronized void onError(int error) {

            if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                if (mListener != null) {
                    ArrayList<String> errorList = new ArrayList<String>(1);
                    errorList.add("ERROR RECOGNIZER BUSY");
                    if (mListener != null)
                        mListener.onResults(errorList);
                }
                return;
            }

            if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                if (mListener != null)
                    mListener.onResults(null);
            }

            if (error == SpeechRecognizer.ERROR_NETWORK) {
                ArrayList<String> errorList = new ArrayList<String>(1);
                errorList.add("STOPPED LISTENING");
                if (mListener != null)
                    mListener.onResults(errorList);
            }
            Log.d(TAG, "error = " + error);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    listenAgain();
                }
            }, 100);


        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onResults(Bundle results) {
            if (results != null && mListener != null)
                mListener.onResults(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));
//            listenAgain();

        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

    }

    public boolean ismIsListening() {
        return mIsListening;
    }


    public interface onResultsReady {
        public void onResults(ArrayList<String> results);
    }
}
