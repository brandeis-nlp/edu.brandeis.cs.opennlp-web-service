package edu.brandeis.lapps.opennlp;

import edu.brandeis.lapps.BrandeisServiceException;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.util.Span;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

public class Splitter extends AbstractOpennlpWrapper {
    private static String MODEL_NAME = DEFAULT_MODEL_RES_FILE_MAP.get(Splitter.class);
    private static String TOOL_DESCRIPTION = String.format("This service is a wrapper around Apache OpenNLP %s " +
                    "providing an English sentence splitting service. Internally it uses public OpenNLP-1.5 models " +
                    "(available at http://opennlp.sourceforge.net/models-1.5/), in particular, \"%s\" is used.",
            getWrappeeVersion(), MODEL_NAME);
    private SentenceDetector sentenceDetector;

    public Splitter() throws BrandeisServiceException {
        loadAnnotators();
    }

    @Override
    synchronized protected void loadAnnotators() throws BrandeisServiceException {
        super.loadSentenceModel();
        sentenceDetector = new SentenceDetectorME(sentenceDetectorModel);
    }

    public String[] sentDetect(String s) {
        if (sentenceDetector == null) {
            try {
                loadAnnotators();
            } catch (BrandeisServiceException e) {
                throw new RuntimeException("Fail to initialize SentenceDetector", e);
            }
        }

        String sentences[] = sentenceDetector.sentDetect(s);
        return sentences;
    }

    public Span[] sentPosDetect(String s) {
        if (sentenceDetector == null) {
            try {
                loadAnnotators();
            } catch (BrandeisServiceException e) {
                throw new RuntimeException("Fail to initialize SentenceDetector", e);
            }
        }
        Span [] offsets = sentenceDetector.sentPosDetect(s);
        return offsets;
    }

    @Override
    public String processPayload(Container container) {
        logger.info("Executing");
        String txt = container.getText();

        View view = container.newView();
        setUpContainsMetadata(view, PRODUCER_ALIAS);

        Span[] spans = sentPosDetect(txt);
        int count = 0;
        for (Span span : spans) {
            int start = span.getStart();
            int end = span.getEnd();
            Annotation ann = view.newAnnotation(SENT_ID + count++,
                    Uri.SENTENCE, start, end);
            ann.setLabel("S");
            ann.addFeature("sentence", txt.substring(start, end));
        }
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    public ServiceMetadata loadMetadata() {
        ServiceMetadata meta = setDefaultMetadata();
        meta.setDescription(TOOL_DESCRIPTION);
        meta.getProduces().addAnnotation(Uri.SENTENCE);
        return meta;
    }
}
