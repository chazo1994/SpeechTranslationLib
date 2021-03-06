package edu.mica.speech.client.speechprocessor;

import android.content.Context;
import android.util.Log;

import edu.mica.speech.client.Utilities.ProcessStatus;
import edu.mica.speech.client.tools.audio.AudioRecoder;
import edu.mica.speech.client.tools.audio.SimpleRecorder;

/**
 * Created by thinh on 02/04/2017.
 */

public class SimpleSpeechProcessor extends SpeechProcessor {
    private AudioRecoder audioRecoder;

    public SimpleSpeechProcessor(Context context) throws Exception {
        super(context);
    }
    @Override
    public boolean stopListenning()throws InterruptedException {
        if(audioRecoder == null || !audioRecoder.isRecording()) {
            return false;
        }
        audioRecoder.stopRecording();
        return true;
    }

    @Override
    public void excuteJob() {
        {
            try {
                if(configURL == null) {
                    throw new Exception("Error: load configuration failed!");
                    //excuteJobContinuous(cm);
                }
                this.startExtractor("LiveFrontEnd");
                this.audioRecoder = (AudioRecoder) this.getConfigurationManager().lookup("microphone");
                audioRecoder.clear();
                this.ready();
                if(audioRecoder.startRecording()){
                    if(this.listeners == null || this.listeners.isEmpty()) {
                        this.audioRecoder.stopRecording();
                        throw  new Exception("Add listener before start recording!");
                    }
                    this.beginOfSpeech();
                    this.processing(ProcessStatus.Recording);
                    this.extractAllFeature();
                    this.endOfSpeech();
                    audioRecoder.stopRecording();
                }
            } catch (Exception e){
                Log.e("SpeechProcessor", e.getMessage());
                e.printStackTrace();
            }
        }

    }
}
