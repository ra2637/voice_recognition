package uw.ytchang.vaguard;

/**
 * Created by ra2637 on 11/24/17.
 */

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import AlizeSpkRec.*;

public class AlizeVoiceRecognizerManager {
    private final String TAG = "AlizeVoiceRecognizerManager";

    private SimpleSpkDetSystem alizeSystem;

    public AlizeVoiceRecognizerManager(Context context) {
        try {
            InputStream configAsset = context.getAssets().open("alize.cfg");

            alizeSystem = new SimpleSpkDetSystem(configAsset, context.getFilesDir().getPath());
            configAsset.close();


            InputStream backgroundModelAsset = context.getAssets().open("gmm/world.gmm");
            alizeSystem.loadBackgroundModel(backgroundModelAsset);
            backgroundModelAsset.close();

            System.out.println("System status:");
            System.out.println("  # of features: " + alizeSystem.featureCount());   // at this point, 0
            System.out.println("  # of models: " + alizeSystem.speakerCount());     // at this point, 0
            System.out.println("  UBM is loaded: " + alizeSystem.isUBMLoaded());    // true

            //Train a speaker model
            // Record audio in the format specified in the configuration file and return it as an array of bytes
            AssetFileDescriptor ytAudio = context.getAssets().openFd("yt.WAV");


            /////// Translating from AssetDescriptor to Byte array
            byte[] audioByte = new byte[(int) ytAudio.getLength()];
            ytAudio.createInputStream().read(audioByte);

            // Send audio to the system
            alizeSystem.addAudio(audioByte);     //only the bytes
            System.out.println("ytAudio lenght: " + ytAudio.getLength());

            // Train a model with the audio
            System.out.println("Creating speaker model...");
            alizeSystem.createSpeakerModel("yt");

            System.out.println("  speaker count: " + alizeSystem.speakerCount());    // 1

            // Reset input before sending another signal
            alizeSystem.resetAudio();
            alizeSystem.resetFeatures();

            ytAudio = context.getAssets().openFd("joey.wav");
            byte[] audioByte2 = new byte[(int) ytAudio.getLength()];
            ytAudio.createInputStream().read(audioByte2);

            // Send audio to the system
            alizeSystem.addAudio(audioByte2);     //only the bytes

            // Train a model with the audio
            System.out.println("Creating speaker model...");
            alizeSystem.createSpeakerModel("spk01");
            System.out.println("  speaker count: " + alizeSystem.speakerCount());    // 2

            // Reset input before sending another signal
            alizeSystem.resetAudio();
            alizeSystem.resetFeatures();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (AlizeException e) {
            e.printStackTrace();
//                return;
        }
    }

    public String identifySpeaker(String audioFilePath){

        try {
            alizeSystem.resetAudio();
            alizeSystem.resetFeatures();

            File testFile = new File(audioFilePath);
            FileInputStream testInputstream = new FileInputStream(audioFilePath);
            /////// Translating from AssetDescriptor to Byte array
            byte[] moreAudio = new byte[(int) testFile.length()];
            testInputstream.read(moreAudio);
            alizeSystem.addAudio(moreAudio);
            Log.d(TAG, "speaker score: "+alizeSystem.identifySpeaker().score);
            Log.d(TAG, "speaker score of spk01: "+alizeSystem.verifySpeaker("spk01").score);
            Log.d(TAG, "speaker score of spk01: "+alizeSystem.verifySpeaker("spk01").match);
            Log.d(TAG, "speaker score of yt: "+alizeSystem.verifySpeaker("yt").score);
            Log.d(TAG, "speaker score of yt: "+alizeSystem.verifySpeaker("yt").match);

            return alizeSystem.identifySpeaker().speakerId;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (AlizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
