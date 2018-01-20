package uw.ytchang.vaguard;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ra2637 on 1/16/18.
 */

public class AzureVoiceRecognizerManager{

    HttpClient httpClient;
    String credentialString;

    public AzureVoiceRecognizerManager(final Context context){
        try {
            if(httpClient == null){
                httpClient = HttpClients.createDefault();
            }
            InputStream stream = context.getAssets().open("azure_credentials");
            BufferedReader dataInputStream = new BufferedReader(new InputStreamReader(stream));
            if((credentialString=dataInputStream.readLine()) == null){
                Log.e(AzureVoiceRecognizerManager.class.getSimpleName(), "Error: azure_credentials is empty");
            }
        } catch (Exception e) {
            Log.e(AzureVoiceRecognizerManager.class.getSimpleName(), "Error", e);
        }
    }

    private void addSpeakerFromFile(String speakerName, String trimmedAudioPath) {


    }

//    public void addSpeaker(String speakerName, String trainAudioPath){
//        String profileId = createSpeakerProfileInAzure();
//        enrollSpeakerInAzure(profileId, trainAudioPath);
//
//        //TODO: create json file to store the mapo of speakerName and profileId
//
//    }


    public String identifySpeaker(String audioFilePath){

        return null;
    }

    public boolean verifySpeaker(String speakerId, String audioFilePath){

        return false;
    }


    public String getSpeakersAudioFolder(){
        return null;
    }

    public boolean hasSpeaker(String speakerName){

        return false;
    }


//    private String createSpeakerProfileInAzure(){
//        try {
//            URL url = new URL("https://westus.api.cognitive.microsoft.com/spid/v1.0/identificationProfiles");
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setRequestMethod("POST");
//            httpURLConnection.setRequestProperty("Content-Type", "application/json");
//            httpURLConnection.setRequestProperty("Ocp-Apim-Subscription-Key", credentialString);
//
//            JSONObject body = new JSONObject();
//            body.put("locale", "en-us");
//
//            OutputStreamWriter wr= new OutputStreamWriter(httpURLConnection.getOutputStream());
//            wr.write(body.toString());
//            wr.close();
//
//            //read response
//            BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
//            StringBuilder reponseStr = new StringBuilder();
//            String line;
//            while((line = reader.readLine()) != null){
//                reponseStr.append(line);
//            }
//            reader.close();
//            JSONObject jsonObj = new JSONObject(reponseStr.toString());
//            httpURLConnection.disconnect();
//            return jsonObj.getString("identificationProfileId");
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private void enrollSpeakerInAzure(String profileId, String trainAudioPath){
//        try {
//            URL url = new URL("https://westus.api.cognitive.microsoft.com/spid/v1.0/" +
//                    "identificationProfiles/"+ profileId + "/enroll?shortAudio=true");
//
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setRequestMethod("POST");
////            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
////            httpURLConnection.setRequestProperty("Cache-Control", "no-cache");
////            httpURLConnection.setRequestProperty("Accept","*/*");
////            httpURLConnection.setInstanceFollowRedirects(false);
//            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data");
////            httpURLConnection.setRequestProperty("Content-Type", "audio/wav");
//            httpURLConnection.setRequestProperty("Ocp-Apim-Subscription-Key", credentialString);
//
//
//            OutputStreamWriter wr= new OutputStreamWriter(httpURLConnection.getOutputStream());
//            BufferedReader reader = new BufferedReader(new FileReader(trainAudioPath));
//            String line;
//            while((line = reader.readLine()) != null){
//                wr.write(line);
//            }
//            wr.close();
//            reader.close();
//            Log.d("enrollSpeakerInAzure:", "response code: "+httpURLConnection.getResponseCode());
//            //read response
//            reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
////            StringBuilder reponseStr = new StringBuilder();
//            while((line = reader.readLine()) != null){
//                System.out.println(line);
////                reponseStr.append(line);
//            }
//            reader.close();
//            httpURLConnection.disconnect();
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }



        private String createSpeakerProfileInAzure(){
        try {
            URIBuilder builder = new URIBuilder("https://westus.api.cognitive.microsoft.com/spid/v1.0/identificationProfiles");

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
            System.out.println(e.getMessage());
        }
        return null;
    }

    private void enrollSpeakerInAzure(String profileId, String trainAudioPath){
        try{
            URIBuilder builder = new URIBuilder("https://westus.api.cognitive.microsoft.com/spid/v1.0/" +
                    "identificationProfiles/"+ profileId + "/enroll");

            builder.setParameter("shortAudio", "true");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "multipart/form-data");
            request.setHeader("Ocp-Apim-Subscription-Key", credentialString);


            // Request body
            FileEntity reqEntity = new FileEntity(new File(trainAudioPath), "multipart/form");

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
                System.out.println(reponseStr.toString());
//                JSONObject jsonObj = new JSONObject(reponseStr.toString());

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public class AddSpeaker extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... strings) {
            String profileId = createSpeakerProfileInAzure();
            Log.d("doInBackground", strings[1]);
            enrollSpeakerInAzure(profileId, strings[1]);

            //TODO: create json file to store the mapo of speakerName and profileId

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
