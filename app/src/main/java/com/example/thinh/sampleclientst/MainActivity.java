package com.example.thinh.sampleclientst;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import edu.mica.speech.client.Utilities.ProcessStatus;
import edu.mica.speech.client.speechlistener.SpeechListener;
import edu.mica.speech.client.speechprocessor.LiveSpeechProcessor;
import edu.mica.speech.client.speechprocessor.SimpleFileSpeechProcessor;
import edu.mica.speech.client.speechprocessor.SimpleSpeechProcessor;
import edu.mica.speech.client.speechprocessor.SpeechProcessor;
import edu.mica.speech.client.tools.audio.SimpleTextToSpeech;

public class MainActivity extends AppCompatActivity implements SpeechListener{
    private TextView tvResultRecognition;
    private TextView tvResultTranslation;
    private TextView tvStatus;
    private ImageButton imbtStatus;
    private Button btENVI;
    private Button btVIEN;
    private SpeechProcessor speechProcessor = null;
    private boolean isStarted = false;
    private SimpleTextToSpeech simpleTextToSpeech;
    private long  startsec = 0, endsec = 0;
    private double duration = 0;

    private String host;
    private int enPort;
    private int viPort;
    private boolean typeProcessor = false;

    private String TAG = "Event Speech";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResultRecognition = (TextView) findViewById(R.id.tvResultRecogntion);
        tvResultTranslation = (TextView) findViewById(R.id.tvResultTranslation);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        btENVI = (Button) findViewById(R.id.btENVI);
        btVIEN = (Button) findViewById(R.id.btVIEN);
        imbtStatus = (ImageButton) findViewById(R.id.btStatus);


        loadSettings();

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
                btENVI.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        if(!isStarted){
                            simpleTextToSpeech = new SimpleTextToSpeech(getApplicationContext());
                            try {
                                speechProcessor.stop();
                                // connect to port 9875 on sever for translate english to vietnamese
                                speechProcessor.setPort(enPort);
                                speechProcessor.startListenning();
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            isStarted = true;
                        } else {
                            try {
                                // connect to port 9875 on sever for translate vietnamese to english
                                speechProcessor.stopListenning();
                                startsec = System.currentTimeMillis();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //tvTest.setText("stop !");
                            isStarted = false;
                        }

                    }
                });

                btVIEN.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        if(!isStarted){
                            try {
                                simpleTextToSpeech = new SimpleTextToSpeech(getApplicationContext(),Locale.ENGLISH);
                                speechProcessor.stop();
                                speechProcessor.setPort(viPort);
                                if(speechProcessor.startListenning()){
                                } else {
                                }
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            isStarted = true;
                        } else {
                            try {
                                speechProcessor.stopListenning();
                                //startsec = System.currentTimeMillis();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
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

        /*  initial speechProcessor as LiveSpeechProcessor to record speech and auto detect end of speech
        *   initial speechProcessor as SimpleSpeechProcessor to record speech and reach end of speech signal when call method stoplistening
        *   initial speechProcessor as SimpleFileSpeechProcessor to read speech from a file
        *   */
        if(!typeProcessor){
            speechProcessor = new LiveSpeechProcessor(this.getApplicationContext());

        } else {
            speechProcessor = new SimpleSpeechProcessor(this.getApplicationContext());
        }
        speechProcessor.addListener(this);
        //speechProcessor.setHost("172.16.76.216");
        //speechProcessor.setHost("113.160.41.218");
        //speechProcessor.setHost("172.16.75.74");
        speechProcessor.setHost(host);

    }


    @Override
    public void onReady() {
        Log.i(TAG," Resource is ready");
    }

    @Override
    public void onBeginOfSpeech() {
        Log.i(TAG," Begin of speech");

    }

    @Override
    public void onEndOfSpeech(String status) {
        startsec = System.currentTimeMillis();
        Log.i(TAG,status);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResult(HashMap<String,String> result) {
        String resultRecognition = result.get(SpeechProcessor.KEY_RECOGNITION);
        String resultTranslation = result.get(SpeechProcessor.KEY_TRANSATION);

        if(result != null ) {
            simpleTextToSpeech.speakOut(resultTranslation);
        }
        Log.i(TAG,resultTranslation);
        endsec = System.currentTimeMillis();
        duration = (double)(endsec - startsec);
        //duration = duration/1000;
        tvResultTranslation.setText(resultTranslation + "\n duration: " + duration);
        tvResultRecognition.setText(resultRecognition);
    }

    /**
     * Load all user's settings
     */
    private void loadSettings(){
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        host = sharedPref.getString(SettingsActivity.KEY_IP,"");
        String stringEnPort = sharedPref.getString(SettingsActivity.KEY_ENVI_PORT,"9875");
        String stringViPort = sharedPref.getString(SettingsActivity.KEY_VIEN_PORT,"9876");
        try {
            enPort = Integer.parseInt(stringEnPort);
            viPort = Integer.parseInt(stringViPort);
        } catch (Exception e){
            Toast.makeText(this.getApplicationContext(),"invalid setting's obtions!",Toast.LENGTH_SHORT);
        }

        typeProcessor = sharedPref.getBoolean(SettingsActivity.KEY_TYPE_PROCESSOR,false);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.mnSettings) {
            Intent settings = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settings);
        }
        if(id == R.id.mnAbout){
            Toast.makeText(getApplicationContext(),"Thinh dap troai", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProcess(ProcessStatus status) {
        switch (status){
            case Recording:
                imbtStatus.setBackgroundResource(R.drawable.btrecording);
                tvStatus.setText("Recording");
                break;
            case Translating:
                imbtStatus.setBackgroundResource(R.drawable.bttranslating);
                tvStatus.setText("Translating");
                break;
            case Done:
                imbtStatus.setBackgroundResource(R.drawable.btstatus);
                tvStatus.setText("Done");
                break;
        }
        Log.i(TAG,status.toString());
    }

    @Override
    protected void onDestroy() {
        simpleTextToSpeech.stop();
        super.onDestroy();
    }
}
