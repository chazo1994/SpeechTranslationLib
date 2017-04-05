package com.example.thinh.sampleclientst;

import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.UUID;

import edu.mica.speech.client.speechlistener.SpeechListener;
import edu.mica.speech.client.speechprocessor.LiveSpeechProcessor;
import edu.mica.speech.client.speechprocessor.SpeechProcessor;
import edu.mica.speech.client.tools.audio.SimpleTextToSpeech;

public class MainActivity extends AppCompatActivity implements SpeechListener{
    private TextView tvTest;
    private TextView tvResult;
    private Button btMic;
    private Button btSpeaker;
    private SpeechProcessor speechProcessor = null;
    private boolean isStarted = false;
    private SimpleTextToSpeech simpleTextToSpeech;


    private String TAG = "Event Speech";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTest = (TextView) findViewById(R.id.tvTest);
        tvResult = (TextView) findViewById(R.id.tvResult);
        btMic = (Button) findViewById(R.id.btMic);
        btSpeaker = (Button) findViewById(R.id.btSpeaker);
        simpleTextToSpeech = new SimpleTextToSpeech(getApplicationContext());
        btSpeaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simpleTextToSpeech.stop();
            }
        });
        new AsyncTask<Void,Void,Exception>(){

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                   if(speechProcessor == null) setupSpeechProcessor();

                } catch (IOException e){
                    e.printStackTrace();

                    return e;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                publishProgress();
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onPostExecute(Exception result) {
                super.onPostExecute(result);
                btMic.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        if(!isStarted){
                            tvTest.setText("Start !");
                            try {
                                speechProcessor.startListenning();
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                            isStarted = true;
                        } else {
                            try {
                                speechProcessor.stopListenning();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //tvTest.setText("stop !");
                            isStarted = false;
                        }


                    }
                });
            }
        }.execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /*
        * Setup properties must be done before run process!
        * */
    private void setupSpeechProcessor() throws Exception{

        speechProcessor = new LiveSpeechProcessor(this.getApplicationContext());
        //speechProcessor = new SimpleSpeechProcessor(this.getApplicationContext());
        speechProcessor.addListener(this);
        //speechProcessor.setHost("172.16.76.216");
        speechProcessor.setHost("172.16.75.74");
        //speechProcessor.setHost("192.168.1.68");
    }


    @Override
    public void onReady() {
        Log.i(TAG," Resource is ready");
        tvTest.setText("Ready");
    }

    @Override
    public void onBeginOfSpeech() {
        Log.i(TAG," Begin of speech");
        tvTest.setText("Begin of speech");
    }

    @Override
    public void onEndOfSpeech(String status) {
        tvTest.setText(status);
        Log.i(TAG,status);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResult(String result) {
      //  simpleTextToSpeech = new SimpleTextToSpeech(this.getApplicationContext());
        tvResult.setText(result);
        if(result != null ) {
            simpleTextToSpeech.speakOut(result);
        }
        Log.i(TAG,result);
    }

    @Override
    public void onProcess(String status) {
        tvTest.setText(status);
        Log.i(TAG,status);
    }

    @Override
    protected void onDestroy() {
        simpleTextToSpeech.stop();
        super.onDestroy();
    }
}
