package uw.ytchang.vaguard;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Created by ra2637 on 4/1/18.
 */

public class LogManager {

    private String logFileName;
    private String id;
    private int mode;
    private final String COLUMN_TITLES = "Date,Participant,Identification,Verification,Content_Result";
    private final String baseFolder = "log";
    private final String LOG_INFO_FILE = "participant.db";

    public String date, participant, identification, verification, result;

    private File logFolder;

    public LogManager(Context context) {
        String logDirPath = context.getFilesDir().getPath()+"/"+baseFolder;
        logFolder = new File(logDirPath);
        logFolder.mkdirs();
    }

    /**
     * Used in Add user phase, to create a file to memorize the participant for later use in mainActivity.
     * @param speakerName
     * @param id
     * @return
     */
    public boolean createUserFile(String speakerName, String id){
        try {
            File file = new File(logFolder+"/"+LOG_INFO_FILE);
            file.delete();
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(speakerName);
            writer.newLine();
            writer.write(id);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e){
            e.getStackTrace().toString();
        }
        return false;
    }

    /**
     * Used in mainActivity, for set participant and id in logManager.
     * @return
     */
    public boolean checkUserFile(){
        try {
            File file = new File(logFolder+"/"+LOG_INFO_FILE);

            BufferedReader reader = new BufferedReader(new FileReader(file));
            this.participant = reader.readLine();
            this.id = reader.readLine();
            reader.close();
            return true;
        } catch (IOException e){
            e.getStackTrace().toString();
        }
        return false;
    }

    public boolean write(){
        // TODO: 1. check logFile exists -> if no create the column titles
        try {
            String fileName = mode+"_"+participant+"_"+id;
            File file = new File(logFolder+"/"+fileName);

            BufferedWriter writer;
            if(file.createNewFile()){
                writer = new BufferedWriter(new FileWriter(file, true));
                writer.write(COLUMN_TITLES);
            }else{
                writer = new BufferedWriter(new FileWriter(file, true));
            }

            // TODO: 2. Check each column content by mode
            if(date == null || id == null || identification == null){
                return false;
            }

            String row;
            if(mode == 1){
                row = date+","+id+","+identification;
            }else{
                row = date+","+id+","+identification+","+verification+","+result;
            }

            // TODO: 3. write row to logFile
            writer.newLine();
            writer.write(row);

            writer.flush();
            writer.close();

            identification = null;
            verification = null;
            result = null;

            return true;
        } catch (IOException e){
            e.getStackTrace().toString();
        }
        return false;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setDate(){
        date = new Date().toString();
    }

    public void setParticipant(String participant){
        this.participant = participant;
    }

    public void setIdentification(String identification){
        this.identification = identification;
    }

    public void setVerification(String verification){
        this.verification = verification;
    }

    public void setResult(String result){
        this.result = result;
    }
}
