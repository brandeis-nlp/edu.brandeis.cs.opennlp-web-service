package edu.brandeis.cs.lappsgrid.opennlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import edu.brandeis.cs.lappsgrid.api.opennlp.IVersion;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

import org.anc.lapps.serialization.Annotation;
import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.resource.ResourceLoader;
import org.anc.util.IDGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.vocabulary.Annotations;
import org.lappsgrid.vocabulary.Features;
import org.lappsgrid.vocabulary.Metadata;
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
public class Splitter  extends AbstractWebService implements ISplitter {
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



    @Override
    public Data execute(Data data) {
        logger.info("execute(): Execute OpenNLP SentenceDetector ...");
        long discriminator = data.getDiscriminator();
        if (discriminator == Types.ERROR)
        {
            return data;
        } else if (discriminator == Types.JSON) {
            JSONObject jsonobj = new JSONObject(data.getPayload());

            String text = jsonobj.getJSONObject("text").getString("@value");
            JSONArray steps =  jsonobj.getJSONArray("steps");

            JSONObject resultStep = new JSONObject();
            JSONObject resultContain = new JSONObject();
            resultContain.put( "producer", this.getClass().getName() + ":" + VERSION);
            resultContain.put( "type", "splitter:opennlp");
            resultStep.put("metadata", new JSONObject().put("contains", new JSONObject().put("Sentence", resultContain)));


            Span[] spans = sentPosDetect(text);
            IDGenerator id = new IDGenerator();

            JSONArray annotations = new JSONArray();
            for (Span span : spans) {
                JSONObject annotation = new JSONObject();
                annotation.put("id", id.generate("s"));
                annotation.put("start", span.getStart());
                annotation.put("end", span.getEnd());
                annotation.put("@type", Annotations.SENTENCE);
                annotations.put(annotation);
            }
            resultStep.put("annotations", annotations);
            jsonobj.put("steps", steps.put(resultStep));
            return DataFactory.json(jsonobj.toString());

        } else if (discriminator == Types.TEXT)
        {
            String text = data.getPayload();
            Span[] spans = sentPosDetect(text);

            IDGenerator id = new IDGenerator();
            JSONArray annotations = new JSONArray();
            for (Span span : spans) {
                JSONObject annotation = new JSONObject();
                annotation.put("id", id.generate("s"));
                annotation.put("start", span.getStart());
                annotation.put("end", span.getEnd());
                annotation.put("@type", Annotations.SENTENCE);
                annotations.put(annotation);
            }

            JSONObject resultStep = new JSONObject();
            JSONObject resultContain = new JSONObject();
            resultContain.put( "producer", this.getClass().getName() + ":" + VERSION);
            resultContain.put( "type", "splitter:opennlp");
            resultStep.put("metadata", new JSONObject().put("contains", new JSONObject().put("Sentence", resultContain)));
            resultStep.put("annotations", annotations);
            JSONObject jsonobj = new JSONObject();
            JSONArray steps = new JSONArray();
            jsonobj.put("metadata", new JSONObject());
            jsonobj.put("text", new JSONObject().put("@value", text));
            jsonobj.put("steps", steps.put(resultStep));
            return DataFactory.json(jsonobj.toString());

        }
        else {
            String name = DiscriminatorRegistry.get(discriminator);
            String message = "Invalid input type. Expected JSON but found " + name;
            logger.warn(message);
            return DataFactory.error(message);
        }
    }


	@Override
	public long[] requires() {
		return TYPES_REQUIRES;
	}

	@Override
	public long[] produces() {
		return TYPES_PRODUCES;
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
