package edu.mica.speech.client.events;

import android.nfc.Tag;
import android.util.Log;

import java.util.ArrayList;

import edu.mica.speech.client.speechlistener.SpeechListener;

/**
 * Created by thinh on 26/03/2017.
 */

public abstract class SpeechEvent implements Runnable {
    protected String tag = "SpeehcEvent";
    private ArrayList<SpeechListener> listeners;
    public  SpeechEvent(ArrayList<SpeechListener> listeners){
        super();
        this.listeners = listeners;
    }

    @Override
    public void run() {
        for(SpeechListener speechListener: listeners){
            Log.d(tag,speechListener.toString());
            this.execute(speechListener);

        }
    }

    protected abstract void execute(SpeechListener listener);
}
