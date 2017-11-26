package uw.ytchang.vaguard;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ra2637 on 11/24/17.
 */

public class AudioRecorderManager {
    private static final String TAG = "AudioRecorderManager";

    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord mAudioRecord = null;
    private Thread mRecordingThread = null;
    private boolean mIsRecording = false;
//    private StreamingRecognizeClient mStreamingClient;
    private int mBufferSize;

    private boolean hasVoice;
    private boolean fileClosed;

    private static  String mOutputFileName;

    public AudioRecorderManager(Context context){

        mBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat
                .CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2;

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                mBufferSize);

        mOutputFileName = context.getFilesDir().getPath()+"/test.wav";
    }

    public static String getOutputFileName() {
        return mOutputFileName;
    }

    public static void setOutputFileName(String mOutputFileName) {
        AudioRecorderManager.mOutputFileName = mOutputFileName;
    }


    public void startRecording() {
        Log.d(TAG, "startRecording");

        if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.d(TAG, "Not Initialized yet.");
            return;
        }

        if (mIsRecording) {
            mIsRecording = false;
            mAudioRecord.stop();
        }

        mAudioRecord.startRecording();
        hasVoice = false;
        mIsRecording = true;
        mRecordingThread = new Thread(new Runnable() {
            public void run() {
                readData();
            }
        }, "AudioRecorder Thread");
        mRecordingThread.start();
    }

    public void stopRecording(){
        if (mIsRecording) {
            mIsRecording = false;
            mAudioRecord.stop();
        }
    }

    private void readData() {
        byte sData[] = new  byte[mBufferSize];

        try {
            File outputFile = new File(mOutputFileName);
            if(outputFile.exists()){
                outputFile.delete();
            }

            fileClosed = false;
            FileOutputStream fileOutputStream = new FileOutputStream (outputFile);
            while (mIsRecording){
                int bytesRead = mAudioRecord.read(sData, 0, mBufferSize);
                if (bytesRead > 0) {
                    fileOutputStream.write(sData, 0, bytesRead);
                    System.out.println("bytesRead:" +bytesRead);
                    System.out.println("mBufferSize: "+mBufferSize);
                    if(bytesRead != mBufferSize){
                        hasVoice = true;
                    }
                } else{
                    Log.e(getClass().getSimpleName(), "Error while reading bytes: " + bytesRead);
                }
            }

            fileOutputStream.close();
            fileClosed = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isHasVoice() {
        return hasVoice;
    }

    public boolean isFileClosed() {
        return fileClosed;
    }

}
