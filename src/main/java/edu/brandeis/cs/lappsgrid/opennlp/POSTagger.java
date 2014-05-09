package edu.brandeis.cs.lappsgrid.opennlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import edu.brandeis.cs.lappsgrid.api.opennlp.IVersion;
import net.arnx.jsonic.JSON;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.util.Sequence;

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
            JSONObject jsonobj = new JSONObject(data.getPayload());

            String text = jsonobj.getJSONObject("text").getString("@value");
            JSONArray steps =  jsonobj.getJSONArray("steps");

            ArrayList<JSONObject> tokens = new ArrayList<JSONObject>(16);
            JSONObject laststep = (JSONObject)steps.get(steps.length() - 1);
            JSONObject laststepmeta = laststep.getJSONObject("metadata");
            JSONArray laststepannotations = laststep.getJSONArray("annotations");

            // find target JSONObject
            JSONObject contains = laststepmeta.getJSONObject("contains");
            Object sentence_type = contains.opt("Token");
            if (sentence_type != null) {
                // contains sentence
                for(int j = 0; j < laststepannotations.length(); j++) {
                    JSONObject annotation = laststepannotations.getJSONObject(j);
                    if(annotation.has("@type") && annotation.getString("@type") == "Token"){
                        tokens.add(annotation);
                    }
                }
            }

//            ArrayList<String[]> tags = new ArrayList<String[]>(tokens.size());
            JSONArray annotations =  new JSONArray();
            for(JSONObject annotation: tokens) {
                int start = annotation.getInt("start");
                int end = annotation.getInt("end");
                String token = annotation.getString(text.substring(start, end));
                String[] tags = postagger.tag(new String[]{token});
                JSONObject features = null;
                if(annotation.has("features")) {
                    features = annotation.getJSONObject("features");
                } else {
                    features = new JSONObject();
                }
                features.put("category", tags[0]);
                features.put("string", token);
                annotation.put("features", features);
                annotations.put(annotation);
            }

            // put into json.
            JSONObject resultStep = new JSONObject();
            JSONObject resultContain = new JSONObject();
            resultContain.put("producer", this.getClass().getName() + ":" + VERSION);
            resultContain.put("type", "tagger:opennlp");
            resultStep.put("metadata", new JSONObject().put("contains", new JSONObject().put("Token", resultContain)));
            resultStep.put("annotations", annotations);
            jsonobj.put("steps", steps.put(resultStep));
            return DataFactory.json(jsonobj.toString());

        } else  if (discriminator == Types.TEXT) {
            String [] tags = tag(new String[]{data.getPayload()});
            JSONArray annotations = new JSONArray();
            for(int i = 0; i < tags.length; i++) {
                JSONObject annotation = new JSONObject();
                annotation.put("@type", "Token").put("id", i).put("features", new JSONObject().put( "category", tags[i]));
                annotations.put(annotation);
            }
            JSONObject resultStep = new JSONObject();
            JSONObject resultContain = new JSONObject();
            resultContain.put( "producer", this.getClass().getName() + ":" + VERSION);
            resultContain.put( "type", "annotation:tagger");
            resultStep.put("metadata", new JSONObject().put("contains", new JSONObject().put("Tagger", resultContain)));
            resultStep.put("annotations", annotations);

            JSONObject jsonobj = new JSONObject();
            JSONArray steps = new JSONArray();
            jsonobj.put("metadata", new JSONObject());
            jsonobj.put("text", new JSONObject().put("@value", data.getPayload()));
            jsonobj.put("steps", steps.put(resultStep));
            return DataFactory.json(jsonobj.toString());

        } else {
            String name = DiscriminatorRegistry.get(discriminator);
            String message = "Invalid input type. Expected JSON but found " + name;
            logger.warn(message);
            return DataFactory.error(message);
        }
    }

//
//	public Data executes(Data data) {
//		logger.info("execute(): Execute OpenNLP POSTagger ...");
//
//        Container container = null;
//        try {
//            container = getContainer(data);
//        } catch (LappsException e) {
//            return DataFactory.error(e.getMessage());
//        }
//        String[] tags = tag(new String[]{"This email message is intended solely for the individual or individuals named above."});
//        // steps
//        ProcessingStep step = new ProcessingStep();
//        // steps metadata
////        step.getMetadata().put(Metadata.PRODUCED_BY, this.getClass().getName() + ":" + VERSION);
////        step.getMetadata().put(Metadata.CONTAINS, "Splitter");
//
//        step.addContains(Features.PART_OF_SPEECH, this.getClass().getName() + ":" + VERSION, "posTag");
//
//        //
//        IDGenerator id = new IDGenerator();
//        System.out.println("TAGS: "  + container.getText());
//        System.out.println("TAGS: "  + Arrays.toString(tags));
//        for (String tag: tags) {
//            org.anc.lapps.serialization.Annotation ann =
//                    new org.anc.lapps.serialization.Annotation();
//            ann.setId(id.generate("tok"));
//            ann.setLabel(Annotations.SENTENCE);
//            Map<String, String> features = ann.getFeatures();
//            putFeature(features, Features.PART_OF_SPEECH, tag);
//            step.addAnnotation(ann);
//        }
//
////        if (data.getDiscriminator() != Types.TEXT)
////        {
////            String type = DiscriminatorRegistry.get(data.getDiscriminator());
////            logger.error("execute(): Invalid input, expected TEXT, found " + type);
////            return DataFactory.error("execute(): Invalid input, expected TEXT, found " + type);
////        }
//
//        container.getSteps().add(step);
//        return DataFactory.json(container.toJson());
//	}
//
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
