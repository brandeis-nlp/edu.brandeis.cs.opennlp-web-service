package edu.brandeis.cs.lappsgrid.opennlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import json.JsonTaggerSerialization;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.util.Sequence;

import opennlp.tools.util.Span;
import org.anc.util.IDGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lappsgrid.api.Data;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brandeis.cs.lappsgrid.api.opennlp.IPOSTagger;

/**
 * <i>POSTagger.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p><a href="http://opennlp.sourceforge.net/models-1.5/">Models for 1.5 series</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class POSTagger extends AbstractWebService implements IPOSTagger  {
    protected static final Logger logger = LoggerFactory.getLogger(POSTagger.class);
    
    private static opennlp.tools.postag.POSTagger postagger;
    
    
	public POSTagger() throws OpenNLPWebServiceException {
        if (postagger == null)
		    init();
	}
    
	protected void init() throws OpenNLPWebServiceException {
	      logger.info("init(): Creating OpenNLP POSTagger ...");
	      
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
	      String taggerModel = prop.getProperty(PROP_COMPNENT_MODEL__Maxent, "en-pos-maxent.bin");
	      
	      logger.info("init(): load opennlp-web-service.properties.");
	      
//	      stream = ResourceLoader.open(taggerModel);
          stream = this.getClass().getResourceAsStream("/" + taggerModel);
	      if (stream == null) {
	    	  logger.error("init(): fail to open POSTAGGER MODEl \""+taggerModel+"\".");
	    	  throw new OpenNLPWebServiceException("init(): fail to open POSTAGGER MODEl \""+taggerModel+"\".");
	      }
	      
	      logger.info("init(): load POSTAGGER MODEl \""+taggerModel+"\"");
	     
		try {
			try {
				POSModel model = new POSModel(stream);
				postagger = new POSTaggerME(model);
			} finally {
				stream.close();
			}
		} catch (IOException e) {
	    	  logger.error("init(): fail to load POSTAGGER MODEl \""+taggerModel+"\".");
	    	  throw new OpenNLPWebServiceException("init(): fail to load POSTAGGER MODEl \""+taggerModel+"\".");
		}
		
      logger.info("init(): Creating OpenNLP POSTagger!");
	}

	@Override
	public Data configure(Data data) {
		return DataFactory.ok();
	}

	@Override
    public Data execute(Data  data) {
        long discriminator = data.getDiscriminator();
        if (discriminator == Types.ERROR)
        {
            return data;
        } else if (discriminator == Types.JSON) {
            String jsonstr = data.getPayload();
            JsonTaggerSerialization json = new JsonTaggerSerialization(jsonstr);
            json.setProducer(this.getClass().getName() + ":" + VERSION);
            json.setType("tagger:opennlp");
            List<JSONObject> tokenObjs = json.findLastAnnotations();
            if (tokenObjs == null) {
                String message = "Invalid JSON input. Expected annotation type: " + json.getLastAnnotationType();
                logger.warn(message);
                return DataFactory.error(message);
            }

            String[] tokens = new String[tokenObjs.size()];
            for(int i = 0; i < tokens.length; i++ ) {
                tokens[i] = json.getAnnotationTextValue(tokenObjs.get(i));
            }

            String[] tags = postagger.tag(tokens);

            for(int i = 0; i < tokenObjs.size(); i++) {
                JSONObject annotation = json.newAnnotation(tokenObjs.get(i));
                json.setCategory(annotation, tags[i]);
            }
            return DataFactory.json(json.toString());
        } else  if (discriminator == Types.TEXT) {
            String textvalue = data.getPayload();
            JsonTaggerSerialization json = new JsonTaggerSerialization();
            json.setProducer(this.getClass().getName() + ":" + VERSION);
            json.setType("tagger:opennlp");
            json.setTextValue(textvalue);

            String [] tags = tag(new String[]{textvalue});
            for(int i = 0; i < tags.length; i++) {
                JSONObject annotation =  json.newAnnotation();
                json.setStart(annotation, 0);
                json.setEnd(annotation, textvalue.length());
                json.setCategory(annotation, tags[i]);
            }
            return DataFactory.json(json.toString());

        } else {
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
	public String[] tag(String[] sentence) {
		if (postagger == null) {
			try {
				init();
			} catch (OpenNLPWebServiceException e) {
				throw new RuntimeException("tokenize(): Fail to initialize POSTagger", e);
			}
		}
		String tags[] = postagger.tag(sentence);
		return tags;
	}


	@Override
	public Sequence[] topKSequences(String[] sentence) {
		if (postagger == null) {
			try {
				init();
			} catch (OpenNLPWebServiceException e) {
				throw new RuntimeException("tokenize(): Fail to initialize POSTagger", e);
			}
		}
		Sequence tags[] = postagger.topKSequences(sentence);
		return tags;
	}

}
