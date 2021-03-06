package edu.mica.speech.client.events;

import java.util.ArrayList;

import edu.mica.speech.client.speechlistener.SpeechListener;

/**
 * Created by thinh on 26/03/2017.
 */

public class EndOfSpeech extends SpeechEvent {
    private String status;
    public EndOfSpeech(ArrayList<SpeechListener> listeners, String status){
        super(listeners);
        this.status = status;
    }
    @Override
    protected void execute(SpeechListener listener) {
        listener.onEndOfSpeech(status);
    }
}
