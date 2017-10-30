package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.LifException;
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
public class Splitter extends OpenNLPAbstractWebService {

    private SentenceDetector sentenceDetector;

    public Splitter() throws OpenNLPWebServiceException {
        loadAnnotators();
        this.metadata = loadMetadata();
    }

    @Override
    synchronized protected void loadAnnotators() throws OpenNLPWebServiceException {
        super.loadSentenceModel();
        sentenceDetector = new SentenceDetectorME(sentenceDetectorModel);
    }

    public String[] sentDetect(String s) {
        if (sentenceDetector == null) {
            try {
                loadAnnotators();
            } catch (OpenNLPWebServiceException e) {
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
            } catch (OpenNLPWebServiceException e) {
                throw new RuntimeException("Fail to initialize SentenceDetector", e);
            }
        }
        Span [] offsets = sentenceDetector.sentPosDetect(s);
        return offsets;
    }

    @Override
    public String execute(Container container) throws OpenNLPWebServiceException {

        logger.info("Executing");
        String txt = container.getText();

        View view = null;
        try {
            view = container.newView();
        } catch (LifException ignored) {
        }
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
            ann.addFeature("sentence", txt.substring(start, end));
        }
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    private String loadMetadata() {
        ServiceMetadata meta = new ServiceMetadata();
        meta.setName(this.getClass().getName());
        meta.setDescription("splitter:opennlp");
        meta.setVersion(getVersion());
        meta.setVendor("http://www.cs.brandeis.edu/");
        meta.setLicense(Uri.APACHE2);

        IOSpecification requires = new IOSpecification();
        requires.setEncoding("UTF-8");
        requires.addLanguage("en");
        requires.addFormat(Uri.LAPPS);

        IOSpecification produces = new IOSpecification();
        produces.setEncoding("UTF-8");
        produces.addLanguage("en");
        produces.addFormat(Uri.LAPPS);
        produces.addAnnotation(Uri.SENTENCE);

        meta.setRequires(requires);
        meta.setProduces(produces);
        Data<ServiceMetadata> data = new Data<> (Uri.META, meta);
        return data.asPrettyJson();
    }
}
