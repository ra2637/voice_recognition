package uw.ytchang.vaguard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;


public class AddUserActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String trainAudioFolder = "speakers";

    private Button record_btn;
    private EditText user_name;
    private TextView guide_line;
    private AlizeVoiceRecognizerManager alizeVoiceRecognizerManager;
    private AudioRecorderManager audioRecorderManager;

    private String trainAudioFolerPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        findViews();
        setClickListeners();

        File audioFolder = new File(getBaseContext().getFilesDir().getPath()+"/"+trainAudioFolder);
        if(!audioFolder.exists()){
            audioFolder.mkdir();
        }
        trainAudioFolerPath = getBaseContext().getFilesDir().getPath()+"/"+trainAudioFolder;
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
                if(record_btn.getText().equals(getString(R.string.start_recording))){
                    // TODO: start recording process

                    String outputFile = trainAudioFolerPath+"/"+user_name.getText()+".wav";
                    if(audioRecorderManager == null){
                        audioRecorderManager = new AudioRecorderManager(outputFile);
                    } else{
                        audioRecorderManager.setOutputFileName(outputFile);
                    }
                    audioRecorderManager.startRecording();
                    record_btn.setText(getString(R.string.stop_recording));
                }else{
                    // TODO: stop recording process and create user in alize
                    audioRecorderManager.stopRecording();
                    record_btn.setText(getString(R.string.start_recording));
                }
                break;
        }
    }
}
