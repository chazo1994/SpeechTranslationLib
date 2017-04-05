package edu.mica.speech.client.speechprocessor;

import android.net.Uri;

import com.example.speechtranslationlib.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import edu.cmu.sphinx.frontend.FrontEnd;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.mica.speech.client.configure.Configure;
import edu.mica.speech.client.controller.ClientConnectionManager;
import edu.mica.speech.client.events.BeginOfSpeech;
import edu.mica.speech.client.events.EndOfSpeech;
import edu.mica.speech.client.events.ProcessEvent;
import edu.mica.speech.client.events.Ready;
import edu.mica.speech.client.events.ResultEvent;
import edu.mica.speech.client.speechlistener.SpeechListener;
import edu.mica.speech.client.tools.feature.FeatureExtractor;

/**
 * Created by thinh on 21/03/2017.
 */

public abstract class SpeechProcessor {
    //private SpeechListener target;
    protected ArrayList<SpeechListener> listeners = new ArrayList<SpeechListener>();
    protected FeatureExtractor featureExtractor = null;
    private String tempDir;
    private List<float[]> allFeatures;
    private String result = "Translating Failed!";
    private String host = "172.16.76.216";
    private int port = 9875;
    protected String configURL = null;
    private Context context;
    private String configName = "frontend.config.xml";
    private SpeechProcessorThread speechProcessorThread;


    private SenderThread senderThread;
    private ConfigurationManager cm;
    // Initial a handler object to comunicate with UI
    private final Handler mHandler = new Handler(Looper.getMainLooper()) ;

    public SpeechProcessor(Context context) throws Exception {
        super();
        this.context = context;
        init();
       // mHandler.post(this);
    }
    private void init() throws Exception {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/speechtemp");
        if(!dir.exists()){
            dir.mkdirs();
        }
        tempDir = dir.getAbsolutePath();
        this.saveConfigFile();



    }
    public void addListener(SpeechListener target){
        synchronized (this.listeners){
            listeners.add(target);
        }

    }

    public void removeListener(SpeechListener listener){
        synchronized (this.listeners){
            listeners.remove(listener);
        }
    }


    public boolean startListenning() throws UnknownHostException {
        if(this.speechProcessorThread == null){
            this.speechProcessorThread = new SpeechProcessor.SpeechProcessorThread();
            this.speechProcessorThread.start();
            return true;
        }
        return false;
    }

    public abstract  boolean stopListenning()throws InterruptedException ;

    /**
     * force stop all thread
     * */
    public boolean stop(){
        if(this.speechProcessorThread == null) {
            return false;
        }
        if(this.senderThread != null){
            this.senderThread.interrupt();
        }
        this.speechProcessorThread.interrupt();
        try {
            this.stopListenning();
            this.speechProcessorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.speechProcessorThread = null;
        }
        return true;
    }

    /**
     * this method to record speech and then dump it to features
     */
    public abstract void excuteJob() ;
    /*public void translating() throws UnknownHostException, InterruptedException,Exception {
        this.senderThread = new SpeechProcessor.SenderThread(host);
        senderThread.start();
        senderThread.join();
        this.mHandler.post(new ResultEvent(listeners,result));
    }*/

    /*
    * Save a config file from assets folder to a local directory on external disk
    * */
    private void saveConfigFile() throws Exception {
        if(context != null) {
            InputStream in = context.getAssets().open(configName);
            OutputStream out = null;
            File dir = new File(tempDir + "/config");
            if(!dir.exists()){
                dir.mkdirs();
            }
            File file = new File(dir, configName);
            /*if(!file.exists()){
                file.createNewFile();
            }*/
            file.createNewFile();
            out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            configURL = file.getAbsolutePath();
            cm = new ConfigurationManager(configURL);
        } else {
            throw new Exception("set context before run!");
        }
    }



    public void startExtractor(String frontEndName ) throws IOException {
        this.featureExtractor = new FeatureExtractor(cm,frontEndName);
    }

    public void extractAllFeature() throws Exception {
        if(this.featureExtractor == null) {
            throw new Exception("Extractor Null: start Extractor before getFeature!");
        }
        this.allFeatures = this.featureExtractor.processSpeech();
    }
    public void extractAllFeature(String inputAudioFile) throws Exception {
        if(this.featureExtractor == null) {
            throw new Exception("Extractor Null: start Extractor before getFeature!");
        }
        this.allFeatures = this.featureExtractor.processSpeech(inputAudioFile);
    }

    protected boolean ready(){
      return this.mHandler.post(new Ready(listeners));
    }
    protected boolean beginOfSpeech() {
        return this.mHandler.post(new BeginOfSpeech(listeners));
    }

    protected boolean endOfSpeech() {
        return this.mHandler.post(new EndOfSpeech(listeners,"end of speech"));
    }
    protected boolean processing(String status){
        return this.mHandler.post(new ProcessEvent(listeners,status));
    }
    protected boolean result() {
        return this.mHandler.post(new ResultEvent(listeners,result));
    }
    private class SpeechProcessorThread extends Thread{
        private ClientConnectionManager clientConnectionManager;
        public SpeechProcessorThread() throws UnknownHostException {
            super("SpeechProcessorThread");
            clientConnectionManager = new ClientConnectionManager(SpeechProcessor.this.host, SpeechProcessor.this.port);
        }

        @Override
        public void run() {
            super.run();
            try {
                SpeechProcessor.this.excuteJob();
                SpeechProcessor.this.result = clientConnectionManager.translate(allFeatures);
                SpeechProcessor.this.result();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ConfigurationManager getConfigurationManager() {
        return cm;
    }

    public void setConfigURL(String configURL) {
        this.configURL = configURL;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
        //mHandler = new Handler(context.getMainLooper());
    }

    public String getResult() {
        return result;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


    private class SenderThread extends Thread {
        ClientConnectionManager clientConnectionManager;

        public SenderThread(){
            super("Sender");
        }
        public SenderThread(String host, int port) throws UnknownHostException {
            super("Sender");
            clientConnectionManager = new ClientConnectionManager(host,port);
        }
        public SenderThread(String host) throws UnknownHostException {
            super("Sender");
            clientConnectionManager = new ClientConnectionManager(host);
        }
        @Override
        public void run() {
            super.run();
            try {
                SpeechProcessor.this.result = clientConnectionManager.translate(allFeatures);
            } catch (IOException e) {
                this.notifyAll();
                e.printStackTrace();
            }
        }

    }
}
