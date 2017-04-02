package com.example.thinh.sampleclientst;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import edu.mica.speech.client.speechlistener.SpeechListener;
import edu.mica.speech.client.speechprocessor.LiveSpeechProcessor;
import edu.mica.speech.client.speechprocessor.SimpleSpeechProcessor;
import edu.mica.speech.client.speechprocessor.SpeechProcessor;

public class MainActivity extends AppCompatActivity implements SpeechListener{
    private TextView tvTest;
    private TextView tvResult;
    private Button btMic;
    private SpeechProcessor speechProcessor;
    private boolean isStarted = false;

    private String TAG = "Event Speech";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTest = (TextView) findViewById(R.id.tvTest);
        tvResult = (TextView) findViewById(R.id.tvResult);
        btMic = (Button) findViewById(R.id.btMic);
        try {


        } catch (Exception e) {
            e.printStackTrace();
        }

        new AsyncTask<Void,Void,Exception>(){

            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    setupSpeechProcessor();

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
                            speechProcessor.startListenning();
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
    /*
    * Setup properties must be done before run process!
    * */
    private void setupSpeechProcessor() throws Exception{
        speechProcessor = new LiveSpeechProcessor(this.getApplicationContext());
        //speechProcessor = new SimpleSpeechProcessor(this.getApplicationContext());
        speechProcessor.addListener(this);
        speechProcessor.setHost("192.168.1.68");
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

    @Override
    public void onResult(String result) {
        tvResult.setText(result);
        Log.i(TAG,result);
    }

    @Override
    public void onProcess(String status) {
        tvTest.setText(status);
        Log.i(TAG,status);
    }
}
