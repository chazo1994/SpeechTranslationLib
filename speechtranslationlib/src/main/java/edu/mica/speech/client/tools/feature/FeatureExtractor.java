package edu.mica.speech.client.tools.feature;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import edu.cmu.sphinx.util.props.ConfigurationManager;

public class FeatureExtractor extends FeatureFileDumper{

	public FeatureExtractor(ConfigurationManager cm, String frontEndName) throws IOException {
		super(cm, frontEndName);
		// TODO Auto-generated constructor stub
	}
	public List<float[]> processSpeech() throws Exception {
		allFeatures = new LinkedList<float[]>();
		getAllFeatures();
		return  allFeatures;
	}
	public List<float[]> processSpeech(String inputAudioFile) throws Exception {
		allFeatures = new LinkedList<float[]>();
		processFile(inputAudioFile);
		return  allFeatures;
	}

	
	public List<float[]> getVoiceFeature(){
		return allFeatures;
	}

}
