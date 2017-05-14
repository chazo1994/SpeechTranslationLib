package edu.mica.speech.client.events;

import java.util.ArrayList;
import java.util.HashMap;

import edu.mica.speech.client.speechlistener.SpeechListener;

/**
 * Created by thinh on 26/03/2017.
 */

public class ResultEvent extends SpeechEvent {
    private HashMap<String,String> result;
    public ResultEvent(ArrayList<SpeechListener> listeners, HashMap<String,String> result){
        super(listeners);
        this.result = result;
    }
    @Override
    protected void execute(SpeechListener listener) {
        listener.onResult(result);
    }
}
