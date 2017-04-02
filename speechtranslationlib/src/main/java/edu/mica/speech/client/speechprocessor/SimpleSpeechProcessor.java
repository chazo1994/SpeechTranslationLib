package edu.mica.speech.client.speechprocessor;

import android.content.Context;
import android.util.Log;

import edu.mica.speech.client.tools.audio.SimpleRecorder;

/**
 * Created by thinh on 02/04/2017.
 */

public class SimpleSpeechProcessor extends SpeechProcessor {
    private SimpleRecorder simpleRecorder = null;
    public SimpleSpeechProcessor(Context context) throws Exception {
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
                //excuteJobContinuous(cm);
            }
            this.startExtractor("SimpleFrontEnd");
           if(simpleRecorder == null) {
               this.beginOfSpeech();
               simpleRecorder = new SimpleRecorder(this.getTempDir());
               simpleRecorder.start();
               simpleRecorder.join();
               String tempAudioFile = simpleRecorder.getFilePath();
               this.processing("Begin extraction");
               this.extractAllFeature(tempAudioFile);
               this.processing("begin translating");
               this.translating();
               //simpleRecorder.interrupt();
               //simpleRecorder = null;
           }

        } catch (Exception e){
            Log.e("SimpleSpeechProcessor",e.getMessage());
            e.printStackTrace();
        }
    }
}
