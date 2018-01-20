package uw.ytchang.vaguard;

/**
 * Created by ra2637 on 1/19/18.
 */

public interface InterfaceVoiceRecognizerManager {

    public boolean initVoiceRecognizer();

    public boolean addSpeaker(String speakerName, String audioPath);

    public boolean verifySpeaker(String speakerId, String audioPath);

    public String getSpeakerId(String speakerName);

    public boolean deleteSpeaker(String speakerId);

//    public boolean addSpeaker();
//
//    public String verifySpeaker();
//
//    public String getSpeakerId();
//
//    public boolean deleteSpeaker();

}
