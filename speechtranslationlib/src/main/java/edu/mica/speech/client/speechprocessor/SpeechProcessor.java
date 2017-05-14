package edu.mica.speech.client.speechprocessor;

import android.net.Uri;

import com.example.speechtranslationlib.R;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.LogRecord;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.mica.speech.client.Utilities.ProcessStatus;
import edu.mica.speech.client.controller.ClientConnectionManager;
import edu.mica.speech.client.events.BeginOfSpeech;
import edu.mica.speech.client.events.EndOfSpeech;
import edu.mica.speech.client.events.ProcessEvent;
import edu.mica.speech.client.events.Ready;
import edu.mica.speech.client.events.ResultEvent;
import edu.mica.speech.client.speechlistener.SpeechListener;
import edu.mica.speech.client.tools.audio.SimpleRecorder;
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
    private HashMap<String,String> result = new HashMap<String, String>();
    private String host = "172.16.76.216";
    private int port = 9875;
    protected String configURL = null;
    private Context context;
    private String configName = "frontend.config.xml";
    private SpeechProcessorThread speechProcessorThread;

    public static final String KEY_RECOGNITION = "recognition";
    public static final String KEY_TRANSATION = "translation";


    private SenderThread senderThread;
    private ConfigurationManager cm;
    private int numbertest = 0;
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


    public boolean startListenning() throws UnknownHostException, Exception {
        numbertest++;
        if(this.speechProcessorThread == null){
            this.speechProcessorThread = new SpeechProcessor.SpeechProcessorThread();
            this.speechProcessorThread.start();
            return true;
        }
        return false;
    }
    public boolean isAliveThread(){
        return speechProcessorThread.isAlive();
    }

    public String threadState(){
        return speechProcessorThread.getState().toString();
    }
    public abstract  boolean stopListenning()throws InterruptedException ;

    /**
     * force stop all thread
     * */
    public boolean stop() throws Exception{
        if(this.speechProcessorThread == null) {
            return false;
        }

        try {
            this.stopListenning();
            // sender threader haven't used yet.
            /*if(this.senderThread != null){
                this.senderThread.interrupt();
                this.senderThread.join();
            }*/
            this.speechProcessorThread.interrupt();
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

    /**
    * Save a config file from assets folder to a local directory on external disk
    */
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


    public void extractAllLiveFeature() throws Exception {
        if(this.featureExtractor == null) {
            throw new Exception("Extractor Null: start Extractor before getFeature!");
        }
        this.allFeatures = this.featureExtractor.processLiveSpeech();
    }
    public void extractAllFeature(String inputAudioFile) throws Exception {
        if(this.featureExtractor == null) {
            throw new Exception("Extractor Null: start Extractor before getFeature!");
        }
        this.allFeatures = this.featureExtractor.processSpeech(inputAudioFile);
    }
    public void extractAllFeature() throws Exception {
        if(this.featureExtractor == null) {
            throw new Exception("Extractor Null: start Extractor before getFeature!");
        }
        this.allFeatures = this.featureExtractor.processSpeech();
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
    protected boolean processing(ProcessStatus status){
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
                SpeechProcessor.this.processing(ProcessStatus.Translating);
                SpeechProcessor.this.result = clientConnectionManager.translate(allFeatures);
                //SpeechProcessor.this.result = clientConnectionManager.translate(SpeechProcessor.this.tempAudioFile);
                SpeechProcessor.this.result();
                SpeechProcessor.this.processing(ProcessStatus.Done);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setHost(String host) throws UnknownHostException{
            clientConnectionManager.setSever(host);
        }
        public void setPort(int port){
            clientConnectionManager.setPort(port);
        }

    }
    public String tempAudioFile = "";
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

    public HashMap<String, String> getResult() {
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

    public void setHost(String host) throws UnknownHostException {
        this.host = host;
        if(this.speechProcessorThread != null){
            this.speechProcessorThread.setHost(host);
        }
    }
    public void setPort(int port){
        this.port = port;
        if(this.speechProcessorThread != null){
            this.speechProcessorThread.setPort(port);
        }
    }
    public int getPort() {
        return port;
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
