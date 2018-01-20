package uw.ytchang.vaguard;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by ra2637 on 1/19/18.
 */

public abstract class AbstractVoiceRecognizerManager extends AsyncTask<String, Void, Boolean>{
    private final String TAG = this.getClass().getSimpleName();
    private String speakersAudioFolder;
    private final String DB_PATH = "id_name.db";

    protected Context context;
    private HashMap<String, String> id_name_map;

    public enum Actions {
        ADD_SPEAKER,
        VERIFY_SPEAKER,
        GET_SPEAKER_ID,
        DELETE_SPEAKER
    };

    public AbstractVoiceRecognizerManager(Context context, String speakerBaseFolder){
        Log.d(TAG, "init voice recognizer");
        
        this.context = context;
        speakersAudioFolder = context.getFilesDir().getPath()+"/"+speakerBaseFolder;
        File audioFolders = new File(speakersAudioFolder);
        audioFolders.mkdirs();

        // Recover map from db
        id_name_map = new HashMap<String, String>();
        try{
            File dbFile = new File(context.getFilesDir().getPath()+"/"+DB_PATH);
            if(dbFile.exists() && dbFile.length() > 0){
                BufferedReader reader = new BufferedReader(new FileReader(dbFile));
                String line;
                while((line = reader.readLine()) != null){
                    String[] data = line.split(",");
                    if(data.length != 2 ){
                        throw new IOException("DB file format error: "+line);
                    }
                    id_name_map.put(data[0], data[1]);
                }
                reader.close();
            }
        }catch(IOException e){
            Log.d(TAG, e.getStackTrace().toString());
        }

        Log.d(TAG, "finish init voice recognizer");
    }

    public String getSpeakersAudioFolder() {
        return speakersAudioFolder;
    }

    /**
     * Store the mapping of speakerId and the speakerName
     * Now speakerId an speakerName are all unique.
     * @param speakerId
     * @param speakerName
     * @return true for succeed, false for failed
     */
    protected boolean saveIdName(String speakerId, String speakerName){
        // check speakerId and speakerName are unique.
        if(isSpeakerIdExisted(speakerId) || isSpeakerNameExisted(speakerName)){
            return false;
        }

        String line = speakerId+","+speakerName;
        String filePath = context.getFilesDir().getPath() + "/" + DB_PATH;
        try {
            File file = new File(filePath);
            BufferedWriter writer;

            if(file.createNewFile()){ // file is new
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(line);
            }else{ // file is existed
                writer = new BufferedWriter(new FileWriter(file));
                if(file.length() > 0){
                    writer.newLine();
                }
                writer.write(line);
            }
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e){
            Log.d(TAG, e.getStackTrace().toString());
        }
        return false;
    }

    public boolean isSpeakerIdExisted(String speakerId){
        return id_name_map.containsKey(speakerId);
    }

    public boolean isSpeakerNameExisted(String speakerName){
        return id_name_map.containsValue(speakerName);
    }

    protected abstract boolean addSpeaker(String speakerName, String audioPath);
    protected abstract boolean verifySpeaker(String speakerId, String audioPath);
    protected abstract String getSpeakerId(String speakerName);
    protected abstract boolean deleteSpeaker(String speakerId);


    @Override
    protected Boolean doInBackground(String... strings) {
        Actions action = Actions.valueOf(strings[0]);
        switch (action){
            case ADD_SPEAKER:
                if(strings.length != 3) {
                    return false;
                }
                return addSpeaker(strings[1], strings[2]);
            case VERIFY_SPEAKER:
                if(strings.length != 3) {
                    return false;
                }
                return verifySpeaker(strings[1], strings[2]);
            case GET_SPEAKER_ID:
                if(strings.length != 2) {
                    return false;
                }
                 if(getSpeakerId(strings[1]) == null) {
                    return false;
                 } else{
                    return true;
                 }
            case DELETE_SPEAKER:
                if(strings.length != 2) {
                    return false;
                }
                return deleteSpeaker(strings[1]);
            default:
                Log.d(TAG, "Unrecognized action: "+action.toString());
                return false;
        }
    }
}
