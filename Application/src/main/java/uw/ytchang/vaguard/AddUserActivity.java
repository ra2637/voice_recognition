package uw.ytchang.vaguard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.regex.Pattern;


public class AddUserActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddUserActivity";
    private Button record_btn;
    private EditText user_name;
    private TextView guide_line;
    private AlizeVoiceRecognizerManager alizeVoiceRecognizerManager;
    private AudioRecorderManager audioRecorderManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        findViews();
        setClickListeners();

        alizeVoiceRecognizerManager = new AlizeVoiceRecognizerManager(getBaseContext());
    }

    private void findViews() {
        record_btn = (Button) findViewById(R.id.record_btn);
        record_btn.setText(getString(R.string.start_recording));
        user_name = (EditText) findViewById(R.id.user_name);
        user_name.setText("");
        guide_line = (TextView) findViewById(R.id.add_user_guide_line);
    }

    private void setClickListeners() {
        record_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_btn:
                String speakerName = user_name.getText().toString();
                String outputFile = alizeVoiceRecognizerManager.getSpeakersAudioFolder()+"/"+user_name.getText()+".wav";;
                if(record_btn.getText().equals(getString(R.string.start_recording))){
                    // Check if username is valid
                    user_name.setFreezesText(true);
                    if(!speakerName.matches("\\w+")){
                        Log.d(TAG, "Name can only contains A-Z, a-z and digits.");
                        guide_line.setText("Name can only contains A-Z, a-z and digits.");
                        user_name.setFreezesText(false);
                        return;
                    }
                    if(alizeVoiceRecognizerManager.hasSpeaker(speakerName)){
                        guide_line.setText("User: "+speakerName+ " is existed.");
                        user_name.setFreezesText(false);
                        return;
                    }

                    // TODO: start recording process
                    if(audioRecorderManager == null){
                        audioRecorderManager = new AudioRecorderManager(outputFile);
                    } else{
                        audioRecorderManager.setOutputFileName(outputFile);
                    }
                    guide_line.setText("Please response \" 5 6 2 1 3 8 7 4 0 9 \" and press stop after you finished.");
                    audioRecorderManager.startRecording();
                    record_btn.setText(getString(R.string.stop_recording));
                }else{
                    // TODO: stop recording process and create user in alize
                    audioRecorderManager.stopRecording();
                    alizeVoiceRecognizerManager.addSpeaker(speakerName, outputFile);
                    user_name.setFreezesText(false);
                    user_name.setText("");
                    guide_line.setText("Added user "+ speakerName + " to the system.");
                    record_btn.setText(getString(R.string.start_recording));
                }
                break;
        }
    }
}