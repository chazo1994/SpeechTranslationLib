package edu.mica.speech.client.events;

import java.util.ArrayList;

import edu.mica.speech.client.speechlistener.SpeechListener;

/**
 * Created by thinh on 26/03/2017.
 */

public class Ready extends SpeechEvent {

    public Ready(ArrayList<SpeechListener> listeners){
        super(listeners);
    }
    @Override
    protected void execute(SpeechListener listener) {
        listener.onBeginOfSpeech();
    }
}
