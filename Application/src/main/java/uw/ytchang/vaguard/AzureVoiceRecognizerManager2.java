package uw.ytchang.vaguard;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ra2637 on 1/19/18.
 */

public class AzureVoiceRecognizerManager2 extends AbstractVoiceRecognizerManager implements InterfaceVoiceRecognizerManager {
    private final String TAG = this.getClass().getSimpleName();
    private static final String AZURE_CREDENTIAL_FILE = "azure_credentials";
    private final String AZURE_API_URI = "https://westus.api.cognitive.microsoft.com/spid/v1.0";
    private static String speakerBaseFolder = "azure";

//    private CloseableHttpClient httpClient;
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
    public JSONObject addSpeaker(String speakerName, String audioPath) {
        String profileId = createSpeakerProfileInAzure();
        if(profileId == null) { return null; }

        if(isSpeakerIdExisted(profileId) || isSpeakerNameExisted(speakerName)){
            Log.d(TAG, "SpeakerId or speakerName is existed. speakerId: "+profileId+
                            "speakerName: "+speakerName);
            return null;
        }

        if(enrollSpeakerInAzure(profileId, audioPath) && this.saveIdName(profileId, speakerName)){
            JSONObject result = new JSONObject();
            try {
                result.put("status", "success");
                result.put("speaker", speakerName);
                return result;
            } catch (JSONException e) {
                Log.d(TAG, e.getMessage());
            }
        }
        return null;
    }

    private String createSpeakerProfileInAzure(){
        try {
            URIBuilder builder = new URIBuilder(AZURE_API_URI + "/identificationProfiles");

            HttpPost request = new HttpPost(builder.build());
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", credentialString);


            // Request body
            StringEntity reqEntity = new StringEntity("{\"locale\":\"en-us\"}");
            request.setEntity(reqEntity);

            HttpClient httpClient = HttpClients.createDefault();
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

            HttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                if(response.getStatusLine().getStatusCode() == 202){
                    String operationUri = response.getFirstHeader("Operation-Location").getValue();
                    Log.d(TAG, "operation location value: "+operationUri);
                    int tryTimes = 3;
                    while(tryTimes>=0){
                        Thread.sleep(1000);
                        tryTimes--;
                        JSONObject result = checkAzureOperation(operationUri);
                        if(result.getString("status").equals("succeeded") &&
                                result.getJSONObject("processingResult").getString("enrollmentStatus").equals("Enrolled")){
                            return true;
                        }
                    }
                } else{
                    Log.d(TAG, "enroll failed");
                    return false;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return false;
    }

    private JSONObject checkAzureOperation(String url){
        try{

            HttpGet request = new HttpGet(url);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", credentialString);

            HttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(request);

            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    BufferedReader dataInputStream = new BufferedReader(new InputStreamReader(entity.getContent()));
                    StringBuilder reponseStr = new StringBuilder();
                    String line;
                    while((line = dataInputStream.readLine()) != null){
                        reponseStr.append(line);
                    }
                    JSONObject jsonObj = new JSONObject(reponseStr.toString());
                    Log.d(TAG, "operation back: "+jsonObj.toString());
                    return jsonObj;
                }
            }
        } catch (Exception e){
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

    @Override
    public JSONObject verifySpeaker(String speakerId, String audioPath) {
        JSONObject response = azureIdentify(speakerId, audioPath);
        JSONObject result = null;

        try {
            if(response == null){
                return null;
            }

//            if(!response.get("data").equals(speakerId)){
//                return null;
//            }

            result = new JSONObject();
            result.put("status", "success");
            result.put("data", true);
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject identifySpeaker(String audioPath){
        Set<String> speakerIdSet = this.getSpekaerIds();
        String identificationProfileIds = speakerIdSet.stream().collect(Collectors.joining(","));
        return azureIdentify(identificationProfileIds, audioPath);
    }

    public JSONObject azureIdentify(String identificationProfileIds, String audioPath){
        if(identificationProfileIds == null){
            return null;
        }

        try{
            Log.d(TAG, "speakerIds: "+identificationProfileIds);
            URIBuilder builder = new URIBuilder(AZURE_API_URI + "/identify");

            builder.setParameter("identificationProfileIds", identificationProfileIds);
            builder.setParameter("shortAudio", "true");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", credentialString);

            // Request body
            FileEntity reqEntity = new FileEntity(new File(audioPath), "application/octet-stream");
            request.setEntity(reqEntity);

            HttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                if(response.getStatusLine().getStatusCode() == 202){
                    String operationUri = response.getFirstHeader("Operation-Location").getValue();
                    Log.d(TAG, "operation location value: "+operationUri);
                    int tryTimes = 3;
                    while(tryTimes>=0){
                        Thread.sleep(1000);
                        tryTimes--;
                        JSONObject result = checkAzureOperation(operationUri);
                        final String nonMatchId = "00000000-0000-0000-0000-000000000000";
                        if(result.getString("status").equals("succeeded")){
                            if(result.getJSONObject("processingResult").getString("identifiedProfileId").equals(nonMatchId)){
                                return null;
                            }else{
                                JSONObject newResult = new JSONObject();
                                newResult.put("status", "success");
                                newResult.put("data", result.getJSONObject("processingResult").getString("identifiedProfileId"));
                                return newResult;
                            }
                        }
                    }
                } else{
                    Log.d(TAG, "verification failed");
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }

        return null;

    }

    @Override
    public JSONObject getSpeakerId(String speakerName) {
        return null;
    }

    @Override
    public JSONObject deleteSpeaker(String speakerId) {
        return null;
    }

//    @Override
//    protected void onPostExecute(JSONObject result)
//    {
//        Log.d(TAG, "RESULT = " + result);
//
//    }

}
