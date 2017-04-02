package edu.mica.speech.client.speechlistener;

/**
 * Created by thinh on 20/03/2017.
 */

public interface SpeechListener {
    public void onReady();
    public void onBeginOfSpeech();
    public void onEndOfSpeech(String status);
    public void onResult(String result);
    public void onProcess(String status);
}
