package uw.ytchang.vaguard;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by ra2637 on 1/19/18.
 */

public abstract class AbstractVoiceRecognizerManager {
    private final String TAG = this.getClass().getSimpleName();
    private final String DB_FILE = "id_name.db";

    private String speakersAudioFolder;
    private String dbPath;

    protected Context context;
    private HashMap<String, String> id_name_map;

    public enum Actions {
        ADD_SPEAKER,
        IDENTIFY_SPEAKER,
        VERIFY_SPEAKER,
        GET_SPEAKER_ID,
        DELETE_SPEAKER
    };

    public AbstractVoiceRecognizerManager(Context context, String speakerBaseFolder){
        Log.d(TAG, "init voice recognizer");

        this.context = context;
        speakersAudioFolder = context.getFilesDir().getPath()+"/"+speakerBaseFolder+"/speakers";
        File audioFolders = new File(speakersAudioFolder);
        audioFolders.mkdirs();

        // Recover map from db
        dbPath = context.getFilesDir().getPath()+"/"+speakerBaseFolder+"/"+ DB_FILE;
        id_name_map = new HashMap<String, String>();
        try{
            File dbFile = new File(dbPath);
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
        try {
            File file = new File(dbPath);
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

            id_name_map.put(speakerId, speakerName);
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

    public Set<String> getSpekaerIds(){
        return id_name_map.keySet();
    }

    public String getSpeakerName(String speakerId){
        return id_name_map.get(speakerId);
    }


    protected abstract JSONObject addSpeaker(String speakerName, String audioPath);
    protected abstract JSONObject identifySpeaker(String audioPath);
    protected abstract JSONObject verifySpeaker(String speakerId, String audioPath);
    protected abstract JSONObject deleteSpeaker(String speakerId);
    protected abstract JSONObject getSpeakerId(String speakerName);


    /**
     *
     * @param strings
     * @return JSONObject with the structure: {"status":"success/fail", "data":""}
     */
//    @Override
//    protected JSONObject doInBackground(String... strings) {
//        Actions action = Actions.valueOf(strings[0]);
//        switch (action){
//            case ADD_SPEAKER:
//                if(strings.length != 3) {
//                    return null;
//                }
//                return addSpeaker(strings[1], strings[2]);
//            case IDENTIFY_SPEAKER:
//                if(strings.length != 2) {
//                    return null;
//                }
//                return identifySpeaker(strings[1]);
//            case VERIFY_SPEAKER:
//                if(strings.length != 3) {
//                    return null;
//                }
//                return verifySpeaker(strings[1], strings[2]);
//            case GET_SPEAKER_ID:
//                if(strings.length != 2) {
//                    return null;
//                }
//                return getSpeakerId(strings[1]);
//
//            case DELETE_SPEAKER:
//                if(strings.length != 2) {
//                    return null;
//                }
//                return deleteSpeaker(strings[1]);
//            default:
//                Log.d(TAG, "Unrecognized action: "+action.toString());
//                return null;
//        }
//    }

    public class AddSpeaker extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... strings) {
            if(strings.length != 3) {
                return null;
            }
            return addSpeaker(strings[1], strings[2]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            Log.d(TAG, "RESULT = " + result);
        }
    }

    public class IdentifySpeaker extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... strings) {
            if(strings.length != 2) {
                    return null;
                }
                return identifySpeaker(strings[1]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            Log.d(TAG, "RESULT = " + result+", status = "+this.getStatus());
        }
    }

    public class VerifySpeaker extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... strings) {
            if(strings.length != 3) {
                return null;
            }
            return verifySpeaker(strings[1], strings[2]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            Log.d(TAG, "RESULT = " + result+", status = "+this.getStatus());
        }
    }

}
