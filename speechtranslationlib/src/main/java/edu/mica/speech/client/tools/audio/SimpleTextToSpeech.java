package edu.mica.speech.client.tools.audio;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Created by thinh on 04/04/2017.
 */

public class SimpleTextToSpeech {
    /**
     * textToSpeech variable: to synthesis speech from text.
     * default language is Vietnamese
     */
    private TextToSpeech textToSpeech;
    private Context context;
    private boolean ready = false;
    private Locale language ;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SimpleTextToSpeech(Context context)  {
        this(context,Locale.forLanguageTag("vi"));
    }

    public SimpleTextToSpeech(Context context, Locale language) {
        this.context = context;
        this.language = language;
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                Log.e("SimpleTextToSpeech","init text to speech");
                setLanguage(SimpleTextToSpeech.this.language);
            }
        });
    }

    public void stop() {
        if(textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
    }
    private void setLanguage(Locale language){
        if(language == null) {
            this.ready = false;
            Toast.makeText(this.context,"language not set",Toast.LENGTH_SHORT).show();
        }

        int result = textToSpeech.setLanguage(language);
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            this.ready = false;
            Toast.makeText(this.context, "Missing language data", Toast.LENGTH_SHORT).show();
            return;
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            this.ready = false;
            Toast.makeText(this.context, "Language not supported", Toast.LENGTH_SHORT).show();
            return;
        } else {
            this.ready = true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean speakOut(String text){
        if(!ready) {
            Toast.makeText(this.context, "Text to Speech not ready", Toast.LENGTH_LONG).show();
            return false;
        }

        String utterenceID = UUID.randomUUID().toString();
        textToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null,utterenceID);
        return true;
    }
}
