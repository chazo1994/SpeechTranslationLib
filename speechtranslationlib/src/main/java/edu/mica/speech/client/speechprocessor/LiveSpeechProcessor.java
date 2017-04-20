package edu.mica.speech.client.speechprocessor;

import android.content.Context;
import android.media.AudioFormat;
import android.util.Log;
import edu.mica.speech.client.tools.audio.AudioRecoder;
import edu.mica.speech.client.tools.audio.WaveWriter;

/**
 * Created by thinh on 02/04/2017.
 */

public class LiveSpeechProcessor extends SpeechProcessor {
    private AudioRecoder audioRecoder;

    public LiveSpeechProcessor(Context context) throws Exception {
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
                this.ready();
                if(audioRecoder.startRecording()){
                    if(this.listeners == null || this.listeners.isEmpty()) {
                        this.audioRecoder.stopRecording();
                        throw  new Exception("Add listener before start recording!");
                    }
                    this.processing("begin of speech");
                    this.beginOfSpeech();
                    this.extractAllLiveFeature();
                    this.endOfSpeech();
                    this.processing("end of speech");

                    audioRecoder.stopRecording();
                }
            } catch (Exception e){
                Log.e("SpeechProcessor", e.getMessage());
                e.printStackTrace();
            }
        }

    }
}
