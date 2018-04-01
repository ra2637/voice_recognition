package uw.ytchang.vaguard;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ra2637 on 11/24/17.
 */

public class AudioRecorderManager {
    private static final String TAG = "AudioRecorderManager";

    public static final int RECORDER_SAMPLERATE = 16000;
//    public static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORDER_BPP = 16;
    public static final int RECORDER_AUDIO_BUFFER_SIZE = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, AudioFormat
            .CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2;


    private AudioRecord mAudioRecord = null;
    private Thread mRecordingThread = null;
    private boolean mIsRecording = false;
//    private StreamingRecognizeClient mStreamingClient;

    private boolean hasVoice;
    private boolean fileClosed;

    private String mOutputFileName;

    public AudioRecorderManager(String outputFilePath){

        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                RECORDER_AUDIO_BUFFER_SIZE);

        mOutputFileName = outputFilePath;
    }

    public String getOutputFileName() {
        return mOutputFileName;
    }

    public void setOutputFileName(String mOutputFileName) {
        this.mOutputFileName = mOutputFileName;
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
        byte sData[] = new  byte[RECORDER_AUDIO_BUFFER_SIZE];

        try {
            File outputFile = new File(mOutputFileName);
            if(outputFile.exists()){
                outputFile.delete();
            }

            fileClosed = false;
            FileOutputStream fileOutputStream = new FileOutputStream (outputFile);
            while (mIsRecording){
                int bytesRead = mAudioRecord.read(sData, 0, RECORDER_AUDIO_BUFFER_SIZE);
                if (bytesRead > 0) {
                    fileOutputStream.write(sData, 0, bytesRead);
                    if(bytesRead != RECORDER_AUDIO_BUFFER_SIZE){
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

    public void createWavFile(String inFilename, String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = ((RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) ? 1
                : 2);
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;
        byte[] data = new byte[RECORDER_AUDIO_BUFFER_SIZE];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
            File deleteFile = new File(inFilename);
            deleteFile.delete();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException {
        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (((RECORDER_CHANNELS == AudioFormat.CHANNEL_IN_MONO) ? 1
                : 2) * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

}
