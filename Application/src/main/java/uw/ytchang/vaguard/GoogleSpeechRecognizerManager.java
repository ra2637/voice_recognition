package uw.ytchang.vaguard;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.google.cloud.speech.v1beta1.StreamingRecognizeResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.grpc.ManagedChannel;

import static android.provider.Telephony.Carriers.PORT;

/**
 * Created by ra2637 on 11/25/17.
 */

public class GoogleSpeechRecognizerManager {

    private static final String HOSTNAME = "speech.googleapis.com";
    private static final int PORT = 443;
    private StreamingRecognizeClient mStreamingClient;

    public GoogleSpeechRecognizerManager(final Context context){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//
//            }
//        }).start();
        // Required to support Android 4.x.x (patches for OpenSSL from Google-Play-Services)
        try {
            ProviderInstaller.installIfNeeded(context);
        } catch (GooglePlayServicesRepairableException e) {

            // Indicates that Google Play services is out of date, disabled, etc.
            e.printStackTrace();
            // Prompt the user to install/update/enable Google Play services.
            GooglePlayServicesUtil.showErrorNotification(
                    e.getConnectionStatusCode(), context);
            return;

        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates a non-recoverable error; the ProviderInstaller is not able
            // to install an up-to-date Provider.
            e.printStackTrace();
            return;
        }

        try {
            InputStream credentials = context.getAssets().open("credentials.json");
            ManagedChannel channel = StreamingRecognizeClient.createChannel(
                    HOSTNAME, PORT, credentials);
            mStreamingClient = new StreamingRecognizeClient(channel, AudioRecorderManager.RECORDER_SAMPLERATE);
        } catch (Exception e) {
            Log.e(MainActivity.class.getSimpleName(), "Error", e);
        }
    }

    public void recognizeFile(String audioFilePath){
        byte audioByte[] = new  byte[AudioRecorderManager.RECORDER_AUDIO_BUFFER_SIZE];
        try {
            FileInputStream audioFileStream = new FileInputStream(new File(audioFilePath));
            while (audioFileStream.read(audioByte) != -1){
                mStreamingClient.recognizeBytes(audioByte, audioByte.length);
            }

        }catch(IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
