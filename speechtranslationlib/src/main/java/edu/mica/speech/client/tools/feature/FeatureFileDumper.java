/*
 * 
 Copyright 1999-2015 Carnegie Mellon University.  
Portions Copyright 2002-2008 Sun Microsystems, Inc.  
Portions Copyright 2002-2008 Mitsubishi Electric Research Laboratories.
Portions Copyright 2013-2015 Alpha Cephei, Inc.

All Rights Reserved.  Use is subject to license terms.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer. 

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.

3. Original authors' names are not deleted.

4. The authors' names are not used to endorse or promote products
   derived from this software without specific prior written
   permission.

This work was supported in part by funding from the Defense Advanced 
Research Projects Agency and the National Science Foundation of the 
United States of America, the CMU Sphinx Speech Consortium, and
Sun Microsystems, Inc.

CARNEGIE MELLON UNIVERSITY, SUN MICROSYSTEMS, INC., MITSUBISHI
ELECTRONIC RESEARCH LABORATORIES AND THE CONTRIBUTORS TO THIS WORK
DISCLAIM ALL WARRANTIES WITH REGARD TO THIS SOFTWARE, INCLUDING ALL
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL
CARNEGIE MELLON UNIVERSITY, SUN MICROSYSTEMS, INC., MITSUBISHI
ELECTRONIC RESEARCH LABORATORIES NOR THE CONTRIBUTORS BE LIABLE FOR
ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT
OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */
package edu.mica.speech.client.tools.feature;

import android.util.Log;

import edu.cmu.sphinx.frontend.*;
import edu.cmu.sphinx.frontend.endpoint.SpeechEndSignal;
import edu.cmu.sphinx.frontend.endpoint.SpeechStartSignal;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.mica.speech.client.Utilities.SpeechStatus;

import java.io.*;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * This program takes in an audio file, does frontend signal processing to it,
 * and then dumps the resulting Feature into a separate file. Also it can
 * process a list of files at once.
 * <p>
 * Available options:
 * <ul>
 * <li>-config configFile - the XML configuration file</li>
 * <li>-name frontendName - the name of the feature extractor inside the
 * configuration file</li>
 * <li>-i audioFile - the name of the audio file</li>
 * <li>-ctl controlFile - the name of the input file for batch processing</li>
 * <li>-o outputFile - the name of the output file or output folder</li>
 * <li>-format binary/ascii - output file format</li>
 * </ul>
 */

public class FeatureFileDumper {

    private FrontEnd frontEnd;
    protected StreamDataSource audioSource;
    protected List<float[]> allFeatures;
    private int featureLength = -1;
    private SpeechStatus speechStatus = null;
    private ConfigurationManager cm;
    /** The logger for this class */
    private static final Logger logger = Logger
            .getLogger("edu.cmu.sphinx.tools.feature.FeatureFileDumper");

    /**
     * Constructs a FeatureFileDumper.
     * 
     * @param cm
     *            the configuration manager
     * @param frontEndName
     *            the name for the frontend
     * @throws IOException if error occurred
     */
    public FeatureFileDumper(ConfigurationManager cm, String frontEndName)
            throws IOException {
        try {
            this.cm = cm;
            frontEnd = (FrontEnd) cm.lookup(frontEndName);
            //audioSource = (StreamDataSource) cm.lookup("streamDataSource");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process the file and store the features
     * 
     * @param inputAudioFile
     *            the input audio file
     * @throws FileNotFoundException if exception occurred
     */
    public void processFile(String inputAudioFile) throws FileNotFoundException {
        audioSource = (StreamDataSource) cm.lookup("dataSource");
        audioSource.setInputStream(new BufferedInputStream(new FileInputStream(inputAudioFile)));
        allFeatures = new LinkedList<float[]>();
        getAllFeatures();
        logger.info("Frames: " + allFeatures.size());
    }
    public void getAllFeatures() {
        /*
         * Run through all the data and produce feature.
         */
        try {
            assert (allFeatures != null);
            Data feature = frontEnd.getData();
            while (!(feature instanceof DataEndSignal)) {
                if (feature instanceof DoubleData) {
                    double[] featureData = ((DoubleData) feature).getValues();
                    if (featureLength < 0) {
                        featureLength = featureData.length;
                        logger.info("Feature length: " + featureLength);
                    }
                    float[] convertedData = new float[featureData.length];
                    for (int i = 0; i < featureData.length; i++) {
                        convertedData[i] = (float) featureData[i];
                    }
                    allFeatures.add(convertedData);
                } else if (feature instanceof FloatData) {
                    float[] featureData = ((FloatData) feature).getValues();
                    if (featureLength < 0) {
                        featureLength = featureData.length;
                        logger.info("Feature length: " + featureLength);
                    }
                    allFeatures.add(featureData);
                }
                feature = frontEnd.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve all Features from speech signal, auto detect end of speech and cache all those with actual feature data.
     * This method skip all data after reached end of speech
     */
    public void getAllLiveFeatures() {
        /*
         * Run through all the data and produce feature.
         */
        try {
            assert (allFeatures != null);
            Data feature = frontEnd.getData();
            while (true) {
                //skip all data after reached SpeechEndSignal
                if(feature instanceof  SpeechStartSignal){
                    while (!(feature instanceof SpeechEndSignal) && !(feature instanceof DataEndSignal) && (feature != null)){
                        if (feature instanceof DoubleData) {
                            double[] featureData = ((DoubleData) feature).getValues();
                            if (featureLength < 0) {
                                featureLength = featureData.length;
                                Log.i("Feature length","Feature length:"  + featureLength);
                            }
                            float[] convertedData = new float[featureData.length];
                            for (int i = 0; i < featureData.length; i++) {
                                convertedData[i] = (float) featureData[i];
                            }
                            allFeatures.add(convertedData);
                            Log.d("allFeatures", Arrays.toString(featureData));
                        } else if (feature instanceof FloatData) {
                            float[] featureData = ((FloatData) feature).getValues();
                            if (featureLength < 0) {
                                featureLength = featureData.length;
                                Log.i("Feature length","Feature length:"  + featureLength);
                            }
                            allFeatures.add(featureData);
                            Log.d("allFeatures", Arrays.toString(featureData));
                        }
                        feature = frontEnd.getData();
                    }
                    break;
                }
                feature = frontEnd.getData();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the total number of data points that should be written to the
     * output file.
     * 
     * @return the total number of data points that should be written
     */
    private int getNumberDataPoints() {
        return (allFeatures.size() * featureLength);
    }

    public SpeechStatus getSpeechStatus() {
        return speechStatus;
    }

    /**
     * Dumps the feature to the given binary output.
     * 
     * @param outputFile
     *            the binary output file
     * @throws IOException if error occurred
     */
    public void dumpBinary(String outputFile) throws IOException {
        DataOutputStream outStream = new DataOutputStream(new FileOutputStream(
                outputFile));
        outStream.writeInt(getNumberDataPoints());

        for (float[] feature : allFeatures) {
            for (float val : feature) {
                outStream.writeFloat(val);
            }
        }

        outStream.close();
    }

    /**
     * Dumps the feature to the given ASCII output file.
     * 
     * @param outputFile
     *            the ASCII output file
     * @throws IOException if error occurred
     */
    public void dumpAscii(String outputFile) throws IOException {
        PrintStream ps = new PrintStream(new FileOutputStream(outputFile), true);
        ps.print(getNumberDataPoints());
        ps.print(' ');

        for (float[] feature : allFeatures) {
            for (float val : feature) {
                ps.print(val);
                ps.print(' ');
            }
        }

        ps.close();
    }

    private void processFile(String inputFile, String outputFile, String format)
            throws MalformedURLException, IOException {
        processFile(inputFile);
        if (format.equals("binary")) {
            dumpBinary(outputFile);
        } else if (format.equals("ascii")) {
            dumpAscii(outputFile);
        } else {
            System.out.println("ERROR: unknown output format: " + format);
        }
    }

    private void processCtl(String inputCtl, String inputFolder,
            String outputFolder, String format) throws MalformedURLException,
            IOException {

        Scanner scanner = new Scanner(new File(inputCtl));
        while (scanner.hasNext()) {
            String fileName = scanner.next();
            String inputFile = inputFolder + "/" + fileName + ".wav";
            String outputFile = outputFolder + "/" + fileName + ".mfc";

            processFile(inputFile);
            if (format.equals("binary")) {
                dumpBinary(outputFile);
            } else if (format.equals("ascii")) {
                dumpAscii(outputFile);
            } else {
                System.out.println("ERROR: unknown output format: " + format);
            }
        }
        scanner.close();
    }
}
