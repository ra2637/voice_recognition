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
import java.io.Serializable;
import java.util.ArrayList;

import AlizeSpkRec.*;

public class AlizeVoiceRecognizerManager {

    private final String TAG = "AlizeVoiceRecognizerManager";

    private static SimpleSpkDetSystem alizeSystem;

    private String speakerBaseFolder = "speakers";
    private static String speakersAudioFolder;
    private static ArrayList<String> speakers;


    public AlizeVoiceRecognizerManager(Context context) {
        try {
            if (alizeSystem == null) {
                Log.d(TAG, "initial alizeSystem");
                InputStream configAsset = context.getAssets().open("alize.cfg");

                alizeSystem = new SimpleSpkDetSystem(configAsset, context.getFilesDir().getPath());
                configAsset.close();


                InputStream backgroundModelAsset = context.getAssets().open("gmm/world.gmm");
                alizeSystem.loadBackgroundModel(backgroundModelAsset);
                backgroundModelAsset.close();

                // TODO: Add existing speaker into alize system
                speakersAudioFolder = context.getFilesDir().getPath()+"/"+speakerBaseFolder;
                File audioFolder = new File(speakersAudioFolder);
                if(!audioFolder.exists()){
                    audioFolder.mkdir();
                }

                speakers = new ArrayList<String>();
                File[] listOfFiles = audioFolder.listFiles();
                Log.d(TAG, "files in audioFolder: "+listOfFiles.length);
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        File audioFile = listOfFiles[i];
                        String speakerName = audioFile.getName().split("\\.")[0];
                        addSpeaker(speakerName, audioFile.getPath());
                    }
                }
                Log.d(TAG, "after initialized");
            }
        }catch (AlizeException | IOException e){
            e.printStackTrace();
        }

//        if(alizeSystem == null) {
//            Log.d(TAG, "initial alizeSystem");
//            try {
//                InputStream configAsset = context.getAssets().open("alize.cfg");
//
//                alizeSystem = new SimpleSpkDetSystem(configAsset, context.getFilesDir().getPath());
//                configAsset.close();
//
//
//                InputStream backgroundModelAsset = context.getAssets().open("gmm/world.gmm");
//                alizeSystem.loadBackgroundModel(backgroundModelAsset);
//                backgroundModelAsset.close();
//
//                System.out.println("System status:");
//                System.out.println("  # of features: " + alizeSystem.featureCount());   // at this point, 0
//                System.out.println("  # of models: " + alizeSystem.speakerCount());     // at this point, 0
//                System.out.println("  UBM is loaded: " + alizeSystem.isUBMLoaded());    // true
//
//                //Train a speaker model
//                // Record audio in the format specified in the configuration file and return it as an array of bytes
//                AssetFileDescriptor ytAudio = context.getAssets().openFd("yt.WAV");
//
//
//                /////// Translating from AssetDescriptor to Byte array
//                byte[] audioByte = new byte[(int) ytAudio.getLength()];
//                ytAudio.createInputStream().read(audioByte);
//                ytAudio.createInputStream().read(audioByte);
//
//                // Send audio to the system
////            alizeSystem.addAudio(audioByte);     //only the bytes
//                alizeSystem.addAudio(context.getFilesDir().getPath() + "/speakers/yt.wav");     //only the bytes
//                System.out.println("ytAudio lenght: " + ytAudio.getLength());
//
//                // Train a model with the audio
//                System.out.println("Creating speaker model...");
//                alizeSystem.createSpeakerModel("yt");
//
//                System.out.println("  speaker count: " + alizeSystem.speakerCount());    // 1
//
//                // Reset input before sending another signal
//                alizeSystem.resetAudio();
//                alizeSystem.resetFeatures();
//
//                ytAudio = context.getAssets().openFd("joey.wav");
//                byte[] audioByte2 = new byte[(int) ytAudio.getLength()];
//                ytAudio.createInputStream().read(audioByte2);
//
//                // Send audio to the system
//                alizeSystem.addAudio(audioByte2);     //only the bytes
//
//                // Train a model with the audio
//                System.out.println("Creating speaker model...");
//                alizeSystem.createSpeakerModel("spk01");
//                System.out.println("  speaker count: " + alizeSystem.speakerCount());    // 2
//
//                // Reset input before sending another signal
//                alizeSystem.resetAudio();
//                alizeSystem.resetFeatures();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (AlizeException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void addSpeaker(String speakerName, String trainAudioPath){
        try {
            if(hasSpeaker(speakerName)){
                Log.d(TAG, "Speaker "+ speakerName + "is in alize system");
                return;
            }
            Log.d(TAG, "Add user: "+speakerName);
            alizeSystem.resetAudio();
            alizeSystem.resetFeatures();

            File file = new File(trainAudioPath);
            FileInputStream inputStream = new FileInputStream(file);

            /////// Translating from AssetDescriptor to Byte array
            byte[] audioByte = new byte[AudioRecorderManager.RECORDER_AUDIO_BUFFER_SIZE];
            alizeSystem.addAudio(trainAudioPath);
            alizeSystem.createSpeakerModel(speakerName);

            // Reset input before sending another signal
            alizeSystem.resetAudio();
            alizeSystem.resetFeatures();

            speakers.add(speakerName);
        } catch (AlizeException | IOException e) {
            e.printStackTrace();
        }
    }

    public String identifySpeaker(String audioFilePath){
        try {
            Log.d(TAG, "speakerCount: "+alizeSystem.speakerCount());
            if(alizeSystem.speakerCount() == 0 ){
                Log.d(TAG, "No speakers in alize system.");
                return null;
            }

            alizeSystem.resetAudio();
            alizeSystem.resetFeatures();

            File testFile = new File(audioFilePath);
            FileInputStream testInputstream = new FileInputStream(audioFilePath);
            /////// Translating from AssetDescriptor to Byte array
            byte[] moreAudio = new byte[(int) testFile.length()];
            testInputstream.read(moreAudio);
            alizeSystem.addAudio(moreAudio);
            Log.d(TAG, "identify speaker "+ alizeSystem.identifySpeaker().speakerId +" score: "+alizeSystem.identifySpeaker().score);
//            Log.d(TAG, "speaker score of yt: "+alizeSystem.verifySpeaker("yt").score);
//            Log.d(TAG, "speaker score of yt: "+alizeSystem.verifySpeaker("yt").match);

            if(alizeSystem.identifySpeaker().match){
                return alizeSystem.identifySpeaker().speakerId;
            }
            return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (AlizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean verifySpeaker(String speakerId, String audioFilePath){
        try {
            Log.d(TAG, "speakerCount: "+alizeSystem.speakerCount());
            if(alizeSystem.speakerCount() == 0 ){
                Log.d(TAG, "No speakers in alize system.");
                return false;
            }

            alizeSystem.resetAudio();
            alizeSystem.resetFeatures();

            File testFile = new File(audioFilePath);
            FileInputStream testInputstream = new FileInputStream(audioFilePath);
            /////// Translating from AssetDescriptor to Byte array
            byte[] moreAudio = new byte[(int) testFile.length()];
            testInputstream.read(moreAudio);
            alizeSystem.addAudio(moreAudio);
            Log.d(TAG, "verify speaker "+ speakerId +" score: "+alizeSystem.verifySpeaker(speakerId).score);
//            Log.d(TAG, "speaker verification score: "+alizeSystem.verifySpeaker(speakerId).score);
//            Log.d(TAG, "speaker score of yt: "+alizeSystem.verifySpeaker("yt").match);

            return alizeSystem.verifySpeaker(speakerId).match;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (AlizeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getSpeakersAudioFolder(){
        return speakersAudioFolder;
    }

    public boolean hasSpeaker(String speakerName){
        return speakers.contains(speakerName);
    }
}
