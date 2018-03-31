package uw.ytchang.vaguard;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
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
                writer = new BufferedWriter(new FileWriter(file, true));
                writer.write(line);
            }else{ // file is existed
                writer = new BufferedWriter(new FileWriter(file, true));
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


    public class AddSpeaker extends AsyncTask<String, Void, JSONObject> {
        private Activity activity;
        private Button record_btn;
        private EditText user_name;
        private TextView guide_line;
        private ProgressBar progressBar;

        public AddSpeaker(Activity activity){
            this.activity = activity;
            this.record_btn = activity.findViewById(R.id.record_btn);
            this.user_name = activity.findViewById(R.id.user_name);
            this.guide_line = activity.findViewById(R.id.add_user_guide_line);
            this.progressBar = activity.findViewById(R.id.progress_loader);
        }

        @Override
        protected void onPreExecute(){
            progressBar.setVisibility(View.VISIBLE);
            record_btn.setEnabled(false);
        }
        @Override
        protected JSONObject doInBackground(String... strings) {
            if(strings.length != 2) {
                return null;
            }
            return addSpeaker(strings[0], strings[1]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            Log.d(TAG, "RESULT = " + result);
            user_name.setFreezesText(false);
            user_name.setText("");
            record_btn.setText(activity.getString(R.string.start_recording));
            record_btn.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            try {
                if(result != null){
                    guide_line.setText("Success to add user "+ result.getString("speaker") + " to the system.");
                }else{
                    guide_line.setText("Fail to add user "+ result.getString("speaker") + " to the system.");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class IdentifySpeaker extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... strings) {
            if(strings.length != 1) {
                    return null;
                }
                return identifySpeaker(strings[0]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            Log.d(TAG, "RESULT = " + result+", status = "+this.getStatus());
        }
    }

    public class VerifySpeaker extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... strings) {
            if(strings.length != 2) {
                return null;
            }
            return verifySpeaker(strings[0], strings[1]);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            Log.d(TAG, "RESULT = " + result+", status = "+this.getStatus());
        }
    }

}
