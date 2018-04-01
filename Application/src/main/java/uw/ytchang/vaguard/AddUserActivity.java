package uw.ytchang.vaguard;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AddUserActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddUserActivity";
    private Button record_btn;
    private EditText user_name;
    private TextView error_line, guide_line, recording_line;
    private ProgressBar spinner;
    private AzureVoiceRecognizerManager2 azureVoiceRecognizerManager;
    private AudioRecorderManager audioRecorderManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        findViews();
        setClickListeners();

        azureVoiceRecognizerManager = new AzureVoiceRecognizerManager2(getBaseContext());
    }

    private void findViews() {
        record_btn = (Button) findViewById(R.id.record_btn);
        record_btn.setText(getString(R.string.start_recording));
        user_name = (EditText) findViewById(R.id.user_name);
        user_name.setText("");
        error_line = (TextView) findViewById(R.id.add_user_error_line);
        guide_line = (TextView) findViewById(R.id.add_user_guide_line);
        recording_line = (TextView) findViewById(R.id.recording_line);
        spinner = (ProgressBar) findViewById(R.id.progress_loader);

        spinner.setVisibility(View.GONE);
        recording_line.setVisibility(View.GONE);
    }

    private void setClickListeners() {
        record_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.record_btn:
                String speakerName = user_name.getText().toString();
                String outputFile = azureVoiceRecognizerManager.getSpeakersAudioFolder()+"/"+speakerName+".wav.tmp";

                if(record_btn.getText().equals(getString(R.string.start_recording))){
                    // Check if username is valid
                    user_name.setFreezesText(true);
                    if(!speakerName.matches("\\w+")){
                        Log.d(TAG, "Name can only contains A-Z, a-z and digits.");
                        error_line.setText("Name can only contains A-Z, a-z and digits.");
                        user_name.setFreezesText(false);
                        return;
                    }

                    // TODO: start recording process
                    if(audioRecorderManager == null){
                        audioRecorderManager = new AudioRecorderManager(outputFile);
                    } else{
                        audioRecorderManager.setOutputFileName(outputFile);
                    }

                    guide_line.setText("Please response \n\n\" 5 6 2 1 3 8 7 4 0 9 \"\n\n and press stop after you finished.");
                    recording_line.setVisibility(View.VISIBLE);
                    audioRecorderManager.startRecording();
                    record_btn.setText(getString(R.string.stop_recording));
                    error_line.setText("");
                }else{
                    // TODO: stop recording process and create user
                    audioRecorderManager.stopRecording();

                    String wavOutputFile = azureVoiceRecognizerManager.getSpeakersAudioFolder()+"/"+speakerName+".wav";
                    audioRecorderManager.createWavFile(outputFile, wavOutputFile);
                    azureVoiceRecognizerManager.new AddSpeaker(this).execute(speakerName, wavOutputFile);
                }
                break;
        }
    }
}
