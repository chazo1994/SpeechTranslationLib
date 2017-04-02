package edu.mica.speech.client.tools.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.DataStartSignal;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.util.DataUtil;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Boolean;
import edu.cmu.sphinx.util.props.S4Integer;
import edu.cmu.sphinx.util.props.S4String;

/**
 * Created by thinh on 19/03/2017.
 */


public class AudioRecoder extends BaseDataProcessor {

    private String tag = "AudioRecoder";
    /**
     * The property for the sample rate of the data.
     */
    @S4Integer(defaultValue = 16000)
    public static final String PROP_SAMPLE_RATE = "sampleRate";

    /**
     * The property that specifies whether or not the microphone will release the audio between utterances.  On
     * certain systems (Linux for one), closing and reopening the audio does not work too well. The default is false for
     * Linux systems, true for others.
     */
    @S4Boolean(defaultValue = true)
    public final static String PROP_CLOSE_BETWEEN_UTTERANCES = "closeBetweenUtterances";

    /**
     * The property that specifies the number of milliseconds of audio data to read each time from the underlying
     * Java Sound audio device.
     */
    @S4Integer(defaultValue = 10)
    public final static String PROP_MSEC_PER_READ = "msecPerRead";

    /**
     * The property for the number of bits per value.
     */
    @S4Integer(defaultValue = 16)
    public static final String PROP_BITS_PER_SAMPLE = "bitsPerSample";

    /**
     * The property specifying the number of channels.
     */
    @S4Integer(defaultValue = 1)
    public static final String PROP_CHANNELS = "channels";

    /**
     * The property specify the endianness of the data.
     */
    @S4Boolean(defaultValue = true)
    public static final String PROP_BIG_ENDIAN = "bigEndian";

    /**
     * The property specify whether the data is signed.
     */
    @S4Boolean(defaultValue = true)
    public static final String PROP_SIGNED = "signed";

    /**
     * The property that specifies whether to keep the audio data of an utterance around until the next utterance
     * is recorded.
     */
    @S4Boolean(defaultValue = false)
    public final static String PROP_KEEP_LAST_AUDIO = "keepLastAudio";

    /**
     * The property that specifies how to convert stereo audio to mono. Currently, the possible values are
     * "average", which averages the samples from at each channel, or "selectChannel", which chooses audio only from
     * that channel. If you choose "selectChannel", you should also specify which channel to use with the
     * "selectChannel" property.
     */
    @S4String(defaultValue = "average", range = {"average", "selectChannel"})
    public final static String PROP_STEREO_TO_MONO = "stereoToMono";

    /**
     * The property that specifies the channel to use if the audio is stereo
     */
    @S4Integer(defaultValue = 0)
    public final static String PROP_SELECT_CHANNEL = "selectChannel";

    /**
     * The property that specifies the mixer to use.  The value can be "default," (which means let the
     * AudioSystem decide), "last," (which means select the last Mixer supported by the AudioSystem), which appears to
     * be what is often used for USB headsets, or an integer value which represents the index of the Mixer.Info that is
     * returned by AudioSystem.getMixerInfo(). To get the list of Mixer.Info objects, run the AudioTool application with
     * a command line argument of "-dumpMixers".
     *
     * @see edu.cmu.sphinx.tools.audio.AudioTool
     */
    @S4String(defaultValue = "default")
    public final static String PROP_SELECT_MIXER = "selectMixer";


    /**
     * The property that specifies the size of the buffer used to store
     * audio samples recorded from the microphone. Default value
     * correspond to 200ms. Smaller value decrease microphone latency with
     * danger of dropping out the frames if decoding thread will
     * be slow enough to process the result.
     */
    @S4Integer(defaultValue = 6400)
    public final static String PROP_BUFFER_SIZE = "bufferSize";


    private BlockingQueue<Data> audioList;
    private Utterance currentUtterance;
    private boolean doConversion;
    private volatile boolean recording;
    private volatile boolean utteranceEndReached = true;

    // Configuration data

    private boolean closeBetweenUtterances;
    private boolean keepDataReference;
    private boolean signed;
    private boolean bigEndian;
    private int frameSizeInBytes;
    private int channels = 1;
    private int msecPerRead;
    private int sampleSizeInBits = 16; // this is bit per second
    private int selectedChannel;
    private String selectedMixerIndex;
    private String stereoToMono;
    private int sampleRate = 16000;
    private int audioBufferSize;
    private AudioRecord audioRecord;
    private AudioFormat audioFormat;
    private AudioRecordingThread recorder;

    private boolean pause = false;
    private boolean resume = false;

    public AudioRecoder(BlockingQueue<Data> audioList, Utterance currentUtterance, boolean doConversion, boolean recording, boolean utteranceEndReached, boolean closeBetweenUtterances, boolean keepDataReference, boolean signed, boolean bigEndian, int frameSizeInBytes, int msecPerRead, int selectedChannel, String selectedMixerIndex, String stereoToMono, int sampleRate, int audioBufferSize) throws IllegalArgumentException{
        this.audioList = audioList;
        this.currentUtterance = currentUtterance;
        this.doConversion = doConversion;
        this.recording = recording;
        this.utteranceEndReached = utteranceEndReached;
        this.closeBetweenUtterances = closeBetweenUtterances;
        this.keepDataReference = keepDataReference;
        this.signed = signed;
        this.bigEndian = bigEndian;
        this.frameSizeInBytes = frameSizeInBytes;
        this.msecPerRead = msecPerRead;
        this.selectedChannel = selectedChannel;
        this.selectedMixerIndex = selectedMixerIndex;
        this.stereoToMono = stereoToMono;
        this.sampleRate = sampleRate;
        this.audioBufferSize = audioBufferSize;
        iniRecoder();

    }

    public AudioRecoder() {
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException, IllegalArgumentException {
        super.newProperties(ps);
        logger = ps.getLogger();

        sampleRate = ps.getInt(PROP_SAMPLE_RATE);

        sampleSizeInBits = ps.getInt(PROP_BITS_PER_SAMPLE);

        channels = ps.getInt(PROP_CHANNELS);
        bigEndian = ps.getBoolean(PROP_BIG_ENDIAN);
        signed = ps.getBoolean(PROP_SIGNED);


        closeBetweenUtterances = ps.getBoolean(PROP_CLOSE_BETWEEN_UTTERANCES);
        msecPerRead = ps.getInt(PROP_MSEC_PER_READ);
        keepDataReference = ps.getBoolean(PROP_KEEP_LAST_AUDIO);
        stereoToMono = ps.getString(PROP_STEREO_TO_MONO);
        selectedChannel = ps.getInt(PROP_SELECT_CHANNEL);
        selectedMixerIndex = ps.getString(PROP_SELECT_MIXER);
        audioBufferSize = ps.getInt(PROP_BUFFER_SIZE);
        iniRecoder();
    }

    @Override
    public void initialize() throws IllegalArgumentException {
        super.initialize();
        audioList = new LinkedBlockingQueue<Data>();
        iniRecoder();
    }
    private void iniRecoder(){
        int buffersize = AudioRecord.getMinBufferSize(sampleRate,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
        if(audioBufferSize < buffersize) audioBufferSize = buffersize;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, audioBufferSize);
        float sec = this.msecPerRead / 1000.f;
        frameSizeInBytes = (sampleSizeInBits/8)*(int) (sec*sampleRate)*channels;
    }
    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public boolean isResume() {
        return resume;
    }

    public void setResume(boolean resume) {
        this.resume = resume;
    }

    public Utterance getCurrentUtterance() {
        return currentUtterance;
    }

    public boolean isRecording() {
        return recording;
    }

    public boolean isUtteranceEndReached() {
        return utteranceEndReached;
    }
    /**
     * Starts recording audio. This method will return only when a START event is received, meaning that this Microphone
     * has started capturing audio.
     *
     * @return true if the recording started successfully; false otherwise
     */
    public synchronized boolean startRecording() throws Exception{
        if (recording) {
            return false;
        }

        utteranceEndReached = false;
        if(audioRecord == null) {
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, audioBufferSize);
        }

        assert (recorder == null);
        recorder = new AudioRecordingThread("Microphone");
        recorder.start();
        recording = true;
        return true;
    }
    /**
     * Stops recording audio. This method does not return until recording has been stopped and all data has been read
     *
     */
    public synchronized void stopRecording() {
        if (audioRecord != null) {
            if (recorder != null) {
                recorder.stopRecording();
                recorder = null;
            }
            recording = false;
        }


    }

    class AudioRecordingThread extends Thread {
        private boolean done;
        private volatile boolean started;
        private long totalSamplesRead;
        private final Object lock = new Object();

        /**
         * Creates the thread with the given name
         *
         * @param name the name of the thread
         */
        public AudioRecordingThread(String name) {
            super(name);
        }

        /**
         * Starts the thread, and waits for recorder to be ready
         */
        @Override
        public void start() {
            started = false;
            super.start();
            waitForStart();
        }

        public void stopRecording() {

            try {
                if(audioRecord.getState() == AudioRecord.STATE_INITIALIZED){
                    audioRecord.stop();
                }
                audioRecord.release();
                synchronized (lock) {
                    while (!done) {
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

        /**
         * Implements the run() method of the Thread class. Records audio, and cache them in the audio buffer.
         */
        @Override
        public void run() {
            totalSamplesRead = 0;
            logger.info("started recording");

            if (keepDataReference) {
                currentUtterance = new Utterance
                        ("Microphone",sampleSizeInBits,sampleRate);
            }

            audioList.add(new DataStartSignal(sampleRate));
            logger.info("DataStartSignal added");
            try {
                audioRecord.startRecording();
                while (!done) {
                    /*if(!pause) {
                        if(resume){
                            audioList.add(new DataStartSignal(sampleRate));
                            resume = false;
                        }*/
                        Data data = readData(currentUtterance);
                        Log.i(tag,"AudioRecorder is running!");
                        if (data == null) {
                            Log.i(tag,"AudioRecorder is stopped!");
                            done = true;
                            break;
                        }
                        audioList.add(data);
                    //}
                }
                if (closeBetweenUtterances) {
                    /* Closing the audio stream *should* (we think)
                     * also close the audio line, but it doesn't
                     * appear to do this on the Mac.  In addition,
                     * once the audio line is closed, re-opening it
                     * on the Mac causes some issues.  The Java sound
                     * spec is also kind of ambiguous about whether a
                     * closed line can be re-opened.  So...we'll go
                     * for the conservative route and never attempt
                     * to re-open a closed line.
                     */
//                    audioStream.close();
//                    audioLine.close();
//                    System.err.println("set to null");
//                    audioLine = null;
                    audioRecord.release();
                }
            } catch (IOException ioe) {
                logger.warning("IO Exception " + ioe.getMessage());
                ioe.printStackTrace();
            }
            long duration = (long)
                    (((double) totalSamplesRead /
                            (double) sampleRate) * 1000.0);

            audioList.add(new DataEndSignal(duration));
            logger.info("DataEndSignal ended");
            logger.info("stopped recording");

            synchronized (lock) {
                lock.notify();
            }
        }

        private synchronized void waitForStart() {
            // note that in theory we could use a LineEvent START
            // to tell us when the microphone is ready, but we have
            // found that some javasound implementations do not always
            // issue this event when a line  is opened, so this is a
            // WORKAROUND.

            try {
                while (!started) {
                    wait();
                }
            } catch (InterruptedException ie) {
                logger.warning("wait was interrupted");
            }
        }

        /**
         * Reads one frame of audio data, and adds it to the given Utterance.
         *
         * @param utterance
         * @return an Data object containing the audio data
         * @throws java.io.IOException
         */
        private Data readData(Utterance utterance) throws IOException {
            // Read the next chunk of data from the TargetDataLine.
            byte[] data = new byte[frameSizeInBytes];

            //chanels = audioStream.getFormat().getChannels();
            long firstSampleNumber = totalSamplesRead / channels;

            int numBytesRead = audioRecord.read(data, 0, data.length);

            //  notify the waiters upon start
            if (!started) {
                synchronized (this) {
                    started = true;
                    notifyAll();
                }
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.info("Read " + numBytesRead
                        + " bytes from audio stream.");
            }
            if (numBytesRead <= 0) {
                return null;
            }
            int sampleSizeInBytes =
                    sampleSizeInBits / 8;
            totalSamplesRead += (numBytesRead / sampleSizeInBytes);

            if (numBytesRead != frameSizeInBytes) {

                if (numBytesRead % sampleSizeInBytes != 0) {
                    throw new Error("Incomplete sample read.");
                }

                data = Arrays.copyOf(data, numBytesRead);
            }

            if (keepDataReference) {
                utterance.add(data);
            }

            double[] samples;

            if (bigEndian) {
                samples = DataUtil.bytesToValues
                        (data, 0, data.length, sampleSizeInBytes, signed);
            } else {
                samples = DataUtil.littleEndianBytesToValues
                        (data, 0, data.length, sampleSizeInBytes, signed);
            }

            if (channels > 1) {
                samples = convertStereoToMono(samples, channels);
            }

            return (new DoubleData
                    (samples, (int) sampleRate,
                            firstSampleNumber));
        }

    }

    /**
     * Converts stereo audio to mono.
     *
     * @param samples  the audio samples, each double in the array is one sample
     * @param channels the number of channels in the stereo audio
     */
    private double[] convertStereoToMono(double[] samples, int channels) {
        assert (samples.length % channels == 0);
        double[] finalSamples = new double[samples.length / channels];
        if (stereoToMono.equals("average")) {
            for (int i = 0, j = 0; i < samples.length; j++) {
                double sum = samples[i++];
                for (int c = 1; c < channels; c++) {
                    sum += samples[i++];
                }
                finalSamples[j] = sum / channels;
            }
        } else if (stereoToMono.equals("selectChannel")) {
            for (int i = selectedChannel, j = 0; i < samples.length;
                 i += channels, j++) {
                finalSamples[j] = samples[i];
            }
        } else {
            throw new Error("Unsupported stereo to mono conversion: " +
                    stereoToMono);
        }
        return finalSamples;
    }


    /**
     * Clears all cached audio data.
     */
    public void clear() {
        audioList.clear();
    }

    /**
     * Reads and returns the next Data object from this Microphone, return null if there is no more audio data. All
     * audio data captured in-between <code>startRecording()</code> and <code>stopRecording()</code> is cached in an
     * Utterance object. Calling this method basically returns the next chunk of audio data cached in this Utterance.
     *
     * @return the next Data or <code>null</code> if none is available
     * @throws DataProcessingException if there is a data processing error
     */
    @Override
    public Data getData() throws DataProcessingException {

        Data output = null;

        if (!utteranceEndReached) {
            try {
                output = audioList.take();
            } catch (InterruptedException ie) {
                throw new DataProcessingException("cannot take Data from audioList", ie);
            }
            if (output instanceof DataEndSignal) {
                utteranceEndReached = true;
            }
        }
        return output;
    }


    /**
     * Returns true if there is more data in the Microphone.
     * This happens either if the a DataEndSignal data was not taken from the buffer,
     * or if the buffer in the Microphone is not yet empty.
     *
     * @return true if there is more data in the Microphone
     */
    public boolean hasMoreData() {
        return !(utteranceEndReached && audioList.isEmpty());
    }
}
