package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.api.opennlp.ISplitter;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.util.Span;

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
	public Splitter() throws OpenNLPWebServiceException {
        if (sentenceDetector == null) {
            init();
            sentenceDetector = loadSentenceDetector(registModelMap.get(this.getClass()));
        }
	}

//    @Override
//    public Data execute(Data data) {
//        logger.info("execute(): Execute OpenNLP SentenceDetector ...");
//        String discriminatorstr = data.getDiscriminator();
//        long discriminator = DiscriminatorRegistry.get(discriminatorstr);
//
//        if (discriminator == Types.ERROR)
//        {
//            return data;
//        } else if (discriminator == Types.JSON) {
//            String jsonstr = data.getPayload();
//            JsonSplitterSerialization json = new JsonSplitterSerialization(jsonstr);
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("splitter:opennlp");
//            Span[] spans = sentPosDetect(json.getTextValue());
//            for (Span span : spans) {
//                JSONObject annotation = json.newAnnotation();
//                json.setStart(annotation, span.getStart());
//                json.setEnd(annotation, span.getEnd());
//            }
//            return DataFactory.json(json.toString());
//
//        } else if (discriminator == Types.TEXT)
//        {
//            String text = data.getPayload();
//            JsonSplitterSerialization json = new JsonSplitterSerialization();
//            json.setTextValue(text);
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("splitter:opennlp");
//
//            Span[] spans = sentPosDetect(text);
//            for (Span span : spans) {
//                JSONObject ann = json.newAnnotation();
//                json.setStart(ann, span.getStart());
//                json.setEnd(ann, span.getEnd());
//            }
//            return DataFactory.json(json.toString());
//        }
//        else {
//            String name = DiscriminatorRegistry.get(discriminator);
//            String message = "Invalid input type. Expected JSON but found " + name;
//            logger.warn(message);
//            return DataFactory.error(message);
//        }
//    }

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
    public String execute(String s) {
        return null;
    }

    @Override
    public String getMetadata() {
        return null;
    }
}
