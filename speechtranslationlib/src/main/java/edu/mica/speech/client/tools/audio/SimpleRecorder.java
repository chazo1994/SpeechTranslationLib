package edu.mica.speech.client.tools.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by thinh on 02/04/2017.
 */

public class SimpleRecorder extends Thread {
    private boolean isRecording = false;
    private AudioRecord audioRecord;
    private String audiofile;
    private final Object lock = new Object();
    private int samplerate = 16000;
    private boolean done = false;
    public static final int CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_IN_MONO;
    //public static final int CHANNEL_CONFIGURATION = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    public static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    public SimpleRecorder(String tempdir) throws IOException {
        super("SimpleRecorder");
        File dir = new File(tempdir + "/wav");
        if(!dir.exists()){
            dir.mkdir();
        }
        //tempFile = File.createTempFile("temp",".raw ",dir);
        audiofile = dir + "/temp.raw";
        File file = new File(audiofile);
        if(file.exists()) file.delete();
    }

    @Override

    public void run() {
        super.run();
        done = false;
        isRecording = true;
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(audiofile)));
        } catch (FileNotFoundException e) {
            try {
                dos.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        int bufferSize = AudioRecord.getMinBufferSize(samplerate,
                CHANNEL_CONFIGURATION, AUDIO_ENCODING);
         audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, samplerate,
                CHANNEL_CONFIGURATION, AUDIO_ENCODING, bufferSize);
        try {

            byte[] buffer = new byte[bufferSize];
            audioRecord.startRecording();
            int r = 0;
            while (isRecording) {
                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                /*for (int i = 0; i < bufferReadResult; i++) {
                    dos.writeShort(buffer[i]);
                }*/
                dos.write(buffer);
                r++;
            }
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            done = true;
            isRecording = false;
            synchronized (lock) {
                lock.notify();
            }
        }
    }

    public String getFilePath(){
        if(done){
            done = false;
            String newFile = WaveWriter.copyWaveFile(audiofile,CHANNEL_CONFIGURATION,AUDIO_ENCODING,samplerate);
            //tempFile = new File(newFile);
            return newFile;
        }

        return null;
    }

        /*public void deleteTempFile(){
            tempFile.delete();
        }*/
    public void stopRecording(){
        if(audioRecord == null) return;
        isRecording = false;
        stopRecord();
    }

    private void stopRecord() {
        if(audioRecord == null) return;
        try {
            if(audioRecord.getState() == AudioRecord.STATE_INITIALIZED){
                audioRecord.stop();
            }
            audioRecord.release();
            synchronized (lock) {
                while (isRecording) {
                    lock.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        audioRecord = null;
    }

    public boolean isRecording() {
        return isRecording;
    }
}
