package edu.mica.speech.client.speechlistener;

import java.util.HashMap;

import edu.mica.speech.client.Utilities.ProcessStatus;

/**
 * Created by thinh on 20/03/2017.
 */

public interface SpeechListener {
    public void onReady();
    public void onBeginOfSpeech();
    public void onEndOfSpeech(String status);
    public void onResult(HashMap<String,String> result);
    public void onProcess(ProcessStatus status);
}
