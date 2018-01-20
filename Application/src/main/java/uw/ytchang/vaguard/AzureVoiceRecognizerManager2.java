package uw.ytchang.vaguard;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Created by ra2637 on 1/19/18.
 */

public class AzureVoiceRecognizerManager2 extends AbstractVoiceRecognizerManager implements InterfaceVoiceRecognizerManager {
    private final String TAG = this.getClass().getSimpleName();
    private static final String AZURE_CREDENTIAL_FILE = "azure_credentials";
    private final String AZURE_API_URI = "https://westus.api.cognitive.microsoft.com/spid/v1.0";
    private static String speakerBaseFolder = "azure/speakers";

    private HttpClient httpClient;
    private String credentialString;

    public AzureVoiceRecognizerManager2(Context context) {
        super(context, speakerBaseFolder);
        if(!initVoiceRecognizer()){
            Log.e(TAG, "Cannot init voice recognize");
            return;
        }

    }

    @Override
    public boolean initVoiceRecognizer() {
        Log.d(TAG, "init voice recognizer");
        try {
            if(httpClient == null){
                httpClient = HttpClients.createDefault();
            }
            InputStream stream = context.getAssets().open(AZURE_CREDENTIAL_FILE);
            BufferedReader dataInputStream = new BufferedReader(new InputStreamReader(stream));
            if((credentialString=dataInputStream.readLine()) == null){
                Log.e(TAG, "Error: azure_credentials is empty");
            } else{
                Log.d(TAG, "init voice recognizer succeed.");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        }
        Log.d(TAG, "init voice recognizer failed.");
        return false;
    }

    @Override
    public boolean addSpeaker(String speakerName, String audioPath) {
        String profileId = createSpeakerProfileInAzure();
        if(profileId == null) { return false; }

        if(isSpeakerIdExisted(profileId) || isSpeakerNameExisted(speakerName)){
            Log.d(TAG, "SpeakerId or speakerName is existed. speakerId: "+profileId+
                            "speakerName: "+speakerName);
            return false;
        }

        if(enrollSpeakerInAzure(profileId, audioPath)){
           return this.saveIdName(profileId, speakerName);
        }
        return false;
    }

    private String createSpeakerProfileInAzure(){
        try {
            URIBuilder builder = new URIBuilder(AZURE_API_URI + "/identificationProfiles");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", credentialString);


            // Request body
            StringEntity reqEntity = new StringEntity("{\"locale\":\"en-us\"}");
            request.setEntity(reqEntity);

            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                BufferedReader dataInputStream = new BufferedReader(new InputStreamReader(entity.getContent()));
                StringBuilder reponseStr = new StringBuilder();
                String line;
                while((line = dataInputStream.readLine()) != null){
                    reponseStr.append(line);
                }
                JSONObject jsonObj = new JSONObject(reponseStr.toString());
                return jsonObj.getString("identificationProfileId");
            }
        }
        catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

    private boolean enrollSpeakerInAzure(String profileId, String audioPath){
        try{
            URIBuilder builder = new URIBuilder(AZURE_API_URI +
                    "/identificationProfiles/"+ profileId + "/enroll");

            builder.setParameter("shortAudio", "true");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "multipart/form-data");
            request.setHeader("Ocp-Apim-Subscription-Key", credentialString);

            // Request body
            FileEntity reqEntity = new FileEntity(new File(audioPath), "multipart/form");
            request.setEntity(reqEntity);

            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                BufferedReader dataInputStream = new BufferedReader(new InputStreamReader(entity.getContent()));
                StringBuilder reponseStr = new StringBuilder();
                String line;
                while((line = dataInputStream.readLine()) != null){
                    reponseStr.append(line+"\n");
                }
                Log.d(TAG, reponseStr.toString());
                Log.d(TAG+"/Enroll response header:", String.valueOf(response.getStatusLine().getStatusCode()));
                if(response.getStatusLine().getStatusCode()/200 == 1){
                    return true;
                } else{
                    Log.d(TAG, "enroll failed: "+reponseStr);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return false;
    }

    @Override
    public boolean verifySpeaker(String speakerId, String audioPath) {
        return false;
    }

    @Override
    public String getSpeakerId(String speakerName) {
        return null;
    }

    @Override
    public boolean deleteSpeaker(String speakerId) {
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        Log.d(TAG, "RESULT = " + result);

    }
}
