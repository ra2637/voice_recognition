package uw.ytchang.vaguard;

/**
 * Created by ra2637 on 11/24/17.
 */

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.fasterxml.jackson.core.util.ByteArrayBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
                        addSpeakerFromFile(speakerName, audioFile.getPath());
                    }
                }
                Log.d(TAG, "after initialized");
            }
        }catch (AlizeException | IOException e){
            e.printStackTrace();
        }

    }

    private void addSpeakerFromFile(String speakerName, String trimmedAudioPath) {
        try {
            if(hasSpeaker(speakerName)){
                Log.d(TAG, "Speaker "+ speakerName + "is in alize system");
                return;
            }
            Log.d(TAG, "Add user: "+speakerName);
            // Reset input before sending another signal
            alizeSystem.resetAudio();
            alizeSystem.resetFeatures();

            alizeSystem.addAudio(trimAudioSilence(trimmedAudioPath).toByteArray());
            if(speakerName.equals("yt1")){
                alizeSystem.createSpeakerModel("yt");
                speakers.add("yt");
//                for (int i=2; i<=8; i++){
//                    alizeSystem.resetAudio();
//                    alizeSystem.resetFeatures();
//                    alizeSystem.addAudio(trimAudioSilence(speakersAudioFolder+"/yt"+i+".wav").toByteArray());
//                    alizeSystem.adaptSpeakerModel("yt");
//                }
            } else if(speakerName.contains("yt")){
                return;
            } else if(speakerName.equals("may1")){
                alizeSystem.createSpeakerModel("may");
                speakers.add("may");
            } else if(speakerName.contains("may")){
                return;
//                alizeSystem.adaptSpeakerModel("may");
            }else{
                alizeSystem.createSpeakerModel(speakerName);
                speakers.add(speakerName);
            }

        } catch (AlizeException e) {
            e.printStackTrace();
        }

    }

    public void addSpeaker(String speakerName, String trainAudioPath){
        try {
            if(hasSpeaker(speakerName)){
                Log.d(TAG, "Speaker "+ speakerName + "is in alize system");
                return;
            }
            Log.d(TAG, "Add user: "+speakerName);
            // Reset input before sending another signal
            alizeSystem.resetAudio();
            alizeSystem.resetFeatures();

            alizeSystem.addAudio(trimAudioSilence(trainAudioPath).toByteArray());
            alizeSystem.createSpeakerModel(speakerName);
            speakers.add(speakerName);

        } catch (AlizeException e) {
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

            alizeSystem.addAudio(audioFilePath);
//            alizeSystem.addAudio(trimAudioSilence(audioFilePath).toByteArray());
            Log.d(TAG, "identify speaker "+ alizeSystem.identifySpeaker().speakerId +" score: "+alizeSystem.identifySpeaker().score);
            for (String speaker: speakers) {
                Log.d(TAG, "speaker score of "+speaker+": "+alizeSystem.verifySpeaker(speaker).score);
            }
            if(alizeSystem.identifySpeaker().match){
                return alizeSystem.identifySpeaker().speakerId;
            }
            return null;
        } catch (AlizeException e) {
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

            alizeSystem.addAudio(audioFilePath);
//            alizeSystem.addAudio(trimAudioSilence(audioFilePath).toByteArray());
            Log.d(TAG, "verify speaker "+ speakerId +" score: "+alizeSystem.verifySpeaker(speakerId).score);
            for (String speaker: speakers) {
                Log.d(TAG, "speaker score of "+speaker+": "+alizeSystem.verifySpeaker(speaker).score);
            }

            return alizeSystem.verifySpeaker(speakerId).match;

        } catch (AlizeException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ByteArrayOutputStream trimAudioSilence(String audioFilePath){
        try{
            File file = new File(audioFilePath);
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            while(true){
                int b = inputStream.read();
                if(b>0){
                    outputStream.write(b);
                }else if(b == -1){
                    break;
                }
            }
            inputStream.close();
            outputStream.close();
            return outputStream;
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    public String getSpeakersAudioFolder(){
        return speakersAudioFolder;
    }

    public boolean hasSpeaker(String speakerName){
        return speakers.contains(speakerName);
    }
}
