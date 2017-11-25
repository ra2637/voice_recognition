package uw.ytchang.vaguard;

/**
 * Created by ra2637 on 11/24/17.
 */

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import java.io.IOException;
import java.io.InputStream;

import AlizeSpkRec.*;

public class AlizeVoiceRecognizerManager {
    private final String TAG = "AlizeVoiceRecognizerManager";

    private SimpleSpkDetSystem alizeSystem;

    public AlizeVoiceRecognizerManager(Context context) {
        try {
            InputStream configAsset = context.getAssets().open("alize.cfg");

            SimpleSpkDetSystem alizeSystem = new SimpleSpkDetSystem(configAsset, context.getFilesDir().getPath());
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
            AssetFileDescriptor ytAudio = context.getAssets().openFd("yt2.WAV");


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

            ytAudio = context.getAssets().openFd("test.wav");

            /////// Translating from AssetDescriptor to Byte array
            byte[] moreAudio = new byte[(int) ytAudio.getLength()];
            ytAudio.createInputStream().read(moreAudio);
            alizeSystem.addAudio(moreAudio);

            // Perform speaker verification against the model we created earlier
            SimpleSpkDetSystem.SpkRecResult verificationResult = alizeSystem.verifySpeaker("spk01");
            System.out.println("Is spk01: " + verificationResult.match);
            System.out.println("Is spk01: " + verificationResult.score);

            verificationResult = alizeSystem.verifySpeaker("yt");
            System.out.println("Is yt: " + verificationResult.match);
            System.out.println("Is yt: " + verificationResult.score);

            SimpleSpkDetSystem.SpkRecResult identificationResult = alizeSystem.identifySpeaker();
            System.out.println("Who is speaking: " + identificationResult.speakerId);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (AlizeException e) {
            e.printStackTrace();
//                return;
        }
    }

}
