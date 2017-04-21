package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.api.opennlp.ISplitter;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.util.Span;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;

/**
 * <i>AbstractOpenNLPWebService.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p><a href="http://opennlp.sourceforge.net/models-1.5/">Models for 1.5 series</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class Splitter  extends OpenNLPAbstractWebService implements ISplitter {
    private static SentenceDetector sentenceDetector;
    String metadata;
    
	public Splitter() throws OpenNLPWebServiceException {
        if (sentenceDetector == null) {
            init();
            sentenceDetector = loadSentenceDetector(registModelMap.get(this.getClass()));
            this.metadata = loadMetadata();
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
    public String execute(LIFJsonSerialization json) throws OpenNLPWebServiceException {
        logger.info("execute(): Execute OpenNLP SentenceDetector ...");
        String txt = json.getText();
        JsonObj view = json.newView();
        json.newContains(view, Discriminators.Uri.SENTENCE,
                "splitter:opennlp", this.getClass().getName() + ":" + Version.getVersion());
        json.setIdHeader("sent");
        Span[] spans = sentPosDetect(txt);
        for (Span span : spans) {
            int start = span.getStart();
            int end = span.getEnd();
            JsonObj ann = json.newAnnotation(view, Discriminators.Uri.SENTENCE, start, end);
            json.setSentence(ann, txt.substring(start, end));
        }
        return json.toString();
    }
    
    public String loadMetadata() {
    	ServiceMetadata meta = new ServiceMetadata();
    	meta.setName(this.getClass().getName());
    	meta.setDescription("splitter:opennlp");
    	meta.setVersion(Version.getVersion());
    	meta.setVendor("http://www.cs.brandeis.edu/");
    	meta.setLicense(Discriminators.Uri.APACHE2);
    	
    	IOSpecification requires = new IOSpecification();
    	requires.setEncoding("UTF-8");
    	requires.addLanguage("en");
    	requires.addFormat(Discriminators.Uri.LAPPS);
    	
    	IOSpecification produces = new IOSpecification();
    	produces.setEncoding("UTF-8");
    	produces.addLanguage("en");
    	produces.addFormat(Discriminators.Uri.LAPPS);
    	produces.addAnnotation(Discriminators.Uri.SENTENCE);
    	
    	meta.setRequires(requires);
    	meta.setProduces(produces);
    	Data<ServiceMetadata> data = new Data<> (Discriminators.Uri.META, meta);
    	return data.asPrettyJson();
    }
    
    public String getMetadata() {
    	return this.metadata;
    }
    
}
