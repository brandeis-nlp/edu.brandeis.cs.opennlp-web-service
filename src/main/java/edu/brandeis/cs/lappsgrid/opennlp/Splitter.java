package edu.brandeis.cs.lappsgrid.opennlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.lappsgrid.serialization.json.JsonSplitterSerialization;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

import org.lappsgrid.serialization.json.JSONObject;
import org.lappsgrid.api.Data;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brandeis.cs.lappsgrid.api.opennlp.ISplitter;

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
    protected static final Logger logger = LoggerFactory.getLogger(Splitter.class);
    
    private static SentenceDetector sentenceDetector;
    
    
	public Splitter() throws OpenNLPWebServiceException {
        if (sentenceDetector == null)
		    init();
	}
    
	protected void init() throws OpenNLPWebServiceException {
	      logger.info("init(): Creating OpenNLP SentenceDetector ...");
	      
	      Properties prop = new Properties();          
//	      InputStream stream = ResourceLoader.open("opennlp-web-service.properties");

          InputStream stream = this.getClass().getResourceAsStream("/" + "opennlp-web-service.properties");
	      if (stream == null) {
	    	  logger.error("init(): fail to open \"opennlp-web-service.properties\".");
	    	  throw new OpenNLPWebServiceException("init(): fail to open \"opennlp-web-service.properties\".");
	      }
	      try {
	    	  prop.load(stream);
	    	  stream.close();
	      } catch (IOException e) {
	    	  logger.error("init(): fail to load \"opennlp-web-service.properties\".");
	    	  throw new OpenNLPWebServiceException("init(): fail to load \"opennlp-web-service.properties\".");
	      }
	      
	      // default English
	      String sentenceModel = prop.getProperty(PROP_COMPNENT_MODEL, "en-sent.bin");
	      
	      logger.info("init(): load opennlp-web-service.properties.");
	      
//	      stream = ResourceLoader.open(sentenceModel);
          stream = this.getClass().getResourceAsStream("/" + sentenceModel);
	      if (stream == null) {
	    	  logger.error("init(): fail to open SENTENCE MODEl \""+sentenceModel+"\".");
	    	  throw new OpenNLPWebServiceException("init(): fail to open SENTENCE MODEl \""+sentenceModel+"\".");
	      }
	      
	      logger.info("init(): load SENTENCE MODEl \""+sentenceModel+"\"");
	     
		try {
			try {
				SentenceModel model = new SentenceModel(stream);
				sentenceDetector = new SentenceDetectorME(model);
			} finally {
				stream.close();
			}
		} catch (IOException e) {
	    	  logger.error("init(): fail to load SENTENCE MODEl \""+sentenceModel+"\".");
	    	  throw new OpenNLPWebServiceException("init(): fail to load SENTENCE MODEl \""+sentenceModel+"\".");
		}
		
      logger.info("init(): Creating OpenNLP SentenceDetector!");
	}

	@Override
	public Data configure(Data data) {
		return DataFactory.ok();
	}

    static Data metadata = loadMetadata();

    static private Data loadMetadata() {
        Data metadata = null;
        try {
            String json = "";
            metadata = DataFactory.meta(json);
        } catch(Exception e){
            metadata = DataFactory.error("Unable to load metadata", e);
        }
        return metadata;
    }

    public Data getMetadata() {
        return metadata;
    }


    @Override
    public Data execute(Data data) {
        logger.info("execute(): Execute OpenNLP SentenceDetector ...");
        String discriminatorstr = data.getDiscriminator();
        long discriminator = DiscriminatorRegistry.get(discriminatorstr);

        if (discriminator == Types.ERROR)
        {
            return data;
        } else if (discriminator == Types.JSON) {
            String jsonstr = data.getPayload();
            JsonSplitterSerialization json = new JsonSplitterSerialization(jsonstr);
            json.setProducer(this.getClass().getName() + ":" + VERSION);
            json.setType("splitter:opennlp");
            Span[] spans = sentPosDetect(json.getTextValue());
            for (Span span : spans) {
                JSONObject annotation = json.newAnnotation();
                json.setStart(annotation, span.getStart());
                json.setEnd(annotation, span.getEnd());
            }
            return DataFactory.json(json.toString());

        } else if (discriminator == Types.TEXT)
        {
            String text = data.getPayload();
            JsonSplitterSerialization json = new JsonSplitterSerialization();
            json.setTextValue(text);
            json.setProducer(this.getClass().getName() + ":" + VERSION);
            json.setType("splitter:opennlp");

            Span[] spans = sentPosDetect(text);
            for (Span span : spans) {
                JSONObject ann = json.newAnnotation();
                json.setStart(ann, span.getStart());
                json.setEnd(ann, span.getEnd());
            }
            return DataFactory.json(json.toString());
        }
        else {
            String name = DiscriminatorRegistry.get(discriminator);
            String message = "Invalid input type. Expected JSON but found " + name;
            logger.warn(message);
            return DataFactory.error(message);
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

}
