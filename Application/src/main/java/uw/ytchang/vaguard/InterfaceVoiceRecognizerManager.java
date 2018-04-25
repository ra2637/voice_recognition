package uw.ytchang.vaguard;

import org.json.JSONObject;

/**
 * Created by ra2637 on 1/19/18.
 */

public interface InterfaceVoiceRecognizerManager {

    public boolean initVoiceRecognizer();

    public JSONObject addSpeaker(String speakerName, String audioPath);

    public JSONObject verifySpeaker(String speakerId, String audioPath);

    public JSONObject getSpeakerId(String speakerName);

    public JSONObject deleteSpeaker(String speakerId);

}
