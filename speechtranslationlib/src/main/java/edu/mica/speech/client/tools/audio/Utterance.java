package edu.mica.speech.client.tools.audio;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Build;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by thinh on 19/03/2017.
 */

public class Utterance {
    private final String name;
    private final ByteArrayOutputStream audioBuffer;
    //private final AudioFormat audioFormat;
    private int bitsPerSample = 16;
    private int sampleRate = 16000;

    public Utterance(String name, int bitsPerSample, int sampleRate) {
        this.name = name;
        audioBuffer = new ByteArrayOutputStream();
        this.bitsPerSample = bitsPerSample;
        this.sampleRate = sampleRate;
    }

    public void setBitsPerSample(int bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
    }
    public int getBitsPerSample() {
        return bitsPerSample;
    }

    public String getName() {
        return name;
    }

    public ByteArrayOutputStream getAudioBuffer() {
        return audioBuffer;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void add(byte[] audio){
        synchronized (audioBuffer){
            audioBuffer.write(audio,0,audio.length);
        }
    }

    /*
    return complete audio stream of this utterance
     */
    public byte[] getAudio() {
        return audioBuffer.toByteArray();
    }

    public float getAudioTime() throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return audioBuffer.size() /
                    (sampleRate*bitsPerSample/8);

        }
        else {
            throw new Exception("require Build.VERSION >  " + Build.VERSION_CODES.LOLLIPOP);
        }
    }

    public void save(String fileName)
            throws IOException {
        File file = new File(fileName);
        byte[] audio = getAudio();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(audio);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
