package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.opennlp.api.ISplitter;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.util.Span;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

/**
 * <i>AbstractOpenNLPWebService.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p><a href="http://opennlp.sourceforge.net/models-1.5/">Models for 1.5 series</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class Splitter extends OpenNLPAbstractWebService implements ISplitter {
    private static SentenceDetector sentenceDetector;
    public Splitter() throws OpenNLPWebServiceException {
        if (sentenceDetector == null) {
            init();
            sentenceDetector = loadSentenceDetector(registModelMap.get(this.getClass()));
        }
    }

    @Override
    public String[] sentDetect(String s) {
        if (sentenceDetector == null) {
            try {
                init();
            } catch (OpenNLPWebServiceException e) {
                throw new RuntimeException("sentDetect(): Fail to initialize SentenceDetector", e);
            }
        }

        String sentences[] = sentenceDetector.sentDetect(s);
        return sentences;
    }

    @Override
    public Span[] sentPosDetect(String s) {
        if (sentenceDetector == null) {
            try {
                init();
            } catch (OpenNLPWebServiceException e) {
                throw new RuntimeException("sentPosDetect(): Fail to initialize SentenceDetector", e);
            }
        }
        Span [] offsets = sentenceDetector.sentPosDetect(s);
        return offsets;
    }

    @Override
    public String execute(Container container) throws OpenNLPWebServiceException {

        logger.info("execute(): Execute OpenNLP SentenceDetector ...");
        String txt = container.getText();

        View view = container.newView();
        view.addContains(Uri.SENTENCE,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "splitter:opennlp");

        Span[] spans = sentPosDetect(txt);
        int count = 0;
        for (Span span : spans) {
            int start = span.getStart();
            int end = span.getEnd();
            Annotation ann = view.newAnnotation(SENT_ID + count++,
                    Uri.SENTENCE, start, end);
            ann.getFeatures().put("sentence", txt.substring(start, end));
        }
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }
}
