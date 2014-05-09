package edu.brandeis.cs.lappsgrid.opennlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import edu.brandeis.cs.lappsgrid.api.opennlp.IVersion;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

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

import edu.brandeis.cs.lappsgrid.api.opennlp.ITokenizer;

/**
 * <i>Tokenizer.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p><a href="http://opennlp.sourceforge.net/models-1.5/">Models for 1.5 series</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class Tokenizer extends AbstractWebService implements ITokenizer {
    protected static final Logger logger = LoggerFactory.getLogger(Tokenizer.class);
    
    private static opennlp.tools.tokenize.Tokenizer tokenizer;
    
    
	public Tokenizer() throws OpenNLPWebServiceException {
        if (tokenizer == null)
		    init();
	}
    
	protected void init() throws OpenNLPWebServiceException {
	      logger.info("init(): Creating OpenNLP Tokenizer ...");
	      
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
	      String tokenModel = prop.getProperty(PROP_COMPNENT_MODEL, "en-token.bin");
	      
	      logger.info("init(): load opennlp-web-service.properties.");
	      
//	      stream = ResourceLoader.open(tokenModel);
          stream = this.getClass().getResourceAsStream("/" + tokenModel);
	      if (stream == null) {
	    	  logger.error("init(): fail to open TOKEN MODEl \""+tokenModel+"\".");
	    	  throw new OpenNLPWebServiceException("init(): fail to open TOKEN MODEl \""+tokenModel+"\".");
	      }
	      
	      logger.info("init(): load TOKEN MODEl \""+tokenModel+"\"");
	     
		try {
			try {
				TokenizerModel model = new TokenizerModel(stream);
				tokenizer = new TokenizerME(model);
			} finally {
				stream.close();
			}
		} catch (IOException e) {
	    	  logger.error("init(): fail to load TOKEN MODEl \""+tokenModel+"\".");
	    	  throw new OpenNLPWebServiceException("init(): fail to load TOKEN MODEl \""+tokenModel+"\".");
		}
		
      logger.info("init(): Creating OpenNLP Tokenizer!");
	}

	@Override
	public Data configure(Data data) {
		return DataFactory.ok();
	}

	@Override
    public Data execute(Data data) {
        logger.info("execute(): Execute OpenNLP tokenizer ...");
        long discriminator = data.getDiscriminator();
        if (discriminator == Types.ERROR)
        {
            return data;
        } else if (discriminator == Types.JSON) {
            JSONObject jsonobj = new JSONObject(data.getPayload());

            String text = jsonobj.getJSONObject("text").getString("@value");
            JSONArray steps =  jsonobj.getJSONArray("steps");

            ArrayList<JSONObject> laststeparr = new ArrayList<JSONObject>(16);
            JSONObject laststep = (JSONObject)steps.get(steps.length() - 1);
            JSONObject laststepmeta = laststep.getJSONObject("metadata");
            JSONArray laststepannotations = laststep.getJSONArray("annotations");

            // find target JSONObject
            JSONObject contains = laststepmeta.getJSONObject("contains");
            Object sentence_type = contains.opt(Annotations.SENTENCE);
            if (sentence_type != null) {
                // contains sentence
                for(int j = 0; j < laststepannotations.length(); j++) {
                    JSONObject annotation = laststepannotations.getJSONObject(j);
                    if(annotation.has("@type") && annotation.getString("@type") == Annotations.SENTENCE){
                        laststeparr.add(annotation);
                    }
                }
            }

            IDGenerator id = new IDGenerator();
            JSONArray annotations =  new JSONArray();
            for(JSONObject sentenceannotation: laststeparr) {
                int start = sentenceannotation.getInt("start");
                int end = sentenceannotation.getInt("end");
                String sentence = sentenceannotation.getString(text.substring(start, end));
                Span[] spans = tokenizePos(sentence);
                for (Span span : spans) {
                    JSONObject annotation = new JSONObject();
                    annotation.put("id", id.generate("tok"));
                    annotation.put("start", start + span.getStart());
                    annotation.put("end", start + span.getEnd());
                    annotation.put("@type", Annotations.TOKEN);
                    annotation.put("features", new JSONObject().put(Features.WORD, sentence.substring(span.getStart(), span.getEnd())));
                    annotations.put(annotation);
                }
            }

            // put into json.
            JSONObject resultStep = new JSONObject();
            JSONObject resultContain = new JSONObject();
            resultContain.put("producer", this.getClass().getName() + ":" + VERSION);
            resultContain.put("type", "tokenizer:opennlp");
            resultStep.put("metadata", new JSONObject().put("contains", new JSONObject().put("Token", resultContain)));
            resultStep.put("annotations", annotations);
            jsonobj.put("steps", steps.put(resultStep));
            return DataFactory.json(jsonobj.toString());

        } else if (discriminator == Types.TEXT) {
            String text = data.getPayload();
            Span[] spans = tokenizePos(text);
            IDGenerator id = new IDGenerator();
            JSONArray annotations = new JSONArray();
            for (Span span : spans) {
                JSONObject annotation = new JSONObject();
                annotation.put("id", id.generate("tok"));
                annotation.put("start", span.getStart());
                annotation.put("end", span.getEnd());
                annotation.put("@type", Annotations.TOKEN);
                annotation.put("features", new JSONObject().put(Features.WORD, text.substring(span.getStart(), span.getEnd())));
                annotations.put(annotation);
            }

            JSONObject resultStep = new JSONObject();
            JSONObject resultContain = new JSONObject();
            resultContain.put( "producer", this.getClass().getName() + ":" + VERSION);
            resultContain.put( "type", "tokenizer:opennlp");
            resultStep.put("metadata", new JSONObject().put("contains", new JSONObject().put( Annotations.TOKEN, resultContain)));
            resultStep.put("annotations", annotations);
            JSONObject jsonobj = new JSONObject();
            JSONArray steps = new JSONArray();
            jsonobj.put("metadata", new JSONObject());
            jsonobj.put("text", new JSONObject().put("@value", text));
            jsonobj.put("steps", steps.put(resultStep));
            return DataFactory.json(jsonobj.toString());
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
	public String[] tokenize(String s) {
		String tokens[] = tokenizer.tokenize(s);
		return tokens;
	}

	@Override
	public Span[] tokenizePos(String s) {
		Span [] boundaries = tokenizer.tokenizePos(s);
		return boundaries;
	}

}
