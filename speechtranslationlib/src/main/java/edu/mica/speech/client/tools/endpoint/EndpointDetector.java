package edu.mica.speech.client.tools.endpoint;

/**
 * Created by thinh on 21/03/2017.
 */
import edu.cmu.sphinx.frontend.*;
import edu.cmu.sphinx.frontend.endpoint.SpeechEndSignal;
import edu.cmu.sphinx.frontend.endpoint.SpeechStartSignal;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;

public class EndpointDetector extends BaseDataProcessor {

    private boolean inSpeech = false;
    public EndpointDetector() {
        initLogger();
    }

    @Override
    protected void initLogger() {
        super.initLogger();
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
    }

    @Override
    public void initialize() {
        super.initialize();
        this.inSpeech = true;
    }

    @Override
    public DataProcessor getPredecessor() {
        return super.getPredecessor();
    }

    @Override
    public void setPredecessor(DataProcessor predecessor) {
        super.setPredecessor(predecessor);
    }

    @Override
    public Data getData() throws DataProcessingException {
        Data data = getPredecessor().getData();
        if(inSpeech == false) {
            return null;
        }
        if (data instanceof SpeechStartSignal)
            inSpeech = true;

        if (data instanceof SpeechEndSignal)
            inSpeech = false;
        return data;
    }
}
