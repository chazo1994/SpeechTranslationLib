package edu.mica.speech.client.speechprocessor;

import android.content.Context;
import android.util.Log;
import java.io.File;
import edu.mica.speech.client.speechprocessor.SpeechProcessor;

/**
 * Created by thinh on 19/04/2017.
 */

public class SimpleFileSpeechProcessor extends SpeechProcessor {
    private String tempAudioFile;
    public SimpleFileSpeechProcessor(Context context, String tempAudioFile) throws Exception {
        super(context);
        this.tempAudioFile = tempAudioFile;
    }

    @Override
    public boolean stopListenning() throws InterruptedException {
        return true;
    }

    @Override
    public void excuteJob() {
        try {
            if(configURL == null) {
                throw new Exception("Error: load configuration failed!");
            }
            this.startExtractor("SimpleFrontEnd");
            if(true) {

                File temp = new File(tempAudioFile);
                if(temp.exists())
                {
                    this.extractAllFeature(temp.getAbsolutePath());
                    this.processing("begin translating");
                }

            }

        } catch (Exception e){
            Log.e("SimpleSpeechProcessor",e.getMessage());
            e.printStackTrace();
        }
    }
}
