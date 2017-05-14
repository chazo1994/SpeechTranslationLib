package edu.mica.speech.client.speechprocessor;

import android.content.Context;
import android.util.Log;
import java.io.File;

import edu.mica.speech.client.Utilities.ProcessStatus;
import edu.mica.speech.client.speechprocessor.SpeechProcessor;
import edu.mica.speech.client.tools.audio.SimpleRecorder;

/**
 * Created by thinh on 19/04/2017.
 */

public class SimpleFileSpeechProcessor extends SpeechProcessor {
    private SimpleRecorder simpleRecorder = null;
    public SimpleFileSpeechProcessor(Context context) throws Exception {
        super(context);
    }

    @Override
    public boolean stopListenning() throws InterruptedException {
        if(simpleRecorder == null || !simpleRecorder.isRecording()){
            return false;
        }
        simpleRecorder.stopRecording();
        return true;
    }

    @Override
    public void excuteJob() {
        try {
            if(configURL == null) {
                throw new Exception("Error: load configuration failed!");
            }
            this.startExtractor("SimpleFrontEnd");
            if(simpleRecorder == null) {
                this.beginOfSpeech();
                simpleRecorder = new SimpleRecorder(this.getTempDir());
                simpleRecorder.start();
                this.processing(ProcessStatus.Recording);
                simpleRecorder.join();
                this.tempAudioFile = simpleRecorder.getFilePath();
                //this.extractAllFeature(tempAudioFile);
                this.endOfSpeech();
            }

        } catch (Exception e){
            Log.e("SimpleSpeechProcessor",e.getMessage());
            e.printStackTrace();
        }
    }
}
