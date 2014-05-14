package edu.brandeis.cs.lappsgrid.opennlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.brandeis.cs.lappsgrid.api.opennlp.IVersion;
import json.JsonNERSerialization;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.Parse;
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

import edu.brandeis.cs.lappsgrid.api.opennlp.INamedEntityRecognizer;

/**
 * <i>NamedEntityRecognizer.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p>
 * <p>
 * <a href="http://opennlp.sourceforge.net/models-1.5/">Models for 1.5
 * series</a>
 * <p>
 * 
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>
 *         Nov 20, 2013<br>
 * 
 */
public class NamedEntityRecognizer extends AbstractWebService implements INamedEntityRecognizer  {
	protected static final Logger logger = LoggerFactory
			.getLogger(NamedEntityRecognizer.class);

	private static ArrayList<TokenNameFinder> nameFinders = new ArrayList<TokenNameFinder> ();

	public NamedEntityRecognizer() throws OpenNLPWebServiceException {
        if (nameFinders.size() == 0)
		    init();
	}

	protected static final TokenNameFinder load(String nerModel) throws OpenNLPWebServiceException {
		TokenNameFinder nameFinder;
//		InputStream stream = ResourceLoader.open(nerModel);
        InputStream stream = NamedEntityRecognizer.class.getResourceAsStream("/" + nerModel);
		if (stream == null) {
			logger.error("load(): fail to open NER MODEL \"" + nerModel
					+ "\".");
			throw new OpenNLPWebServiceException(
					"load(): fail to open NER MODEL \"" + nerModel + "\".");
		}

		logger.info("load(): load NER MODEL \"" + nerModel + "\"");

		try {
			try {
				TokenNameFinderModel model = new TokenNameFinderModel(stream);
				nameFinder = new NameFinderME(model);
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			logger.error("load(): fail to load NER MODEL \"" + nerModel
					+ "\".");
			throw new OpenNLPWebServiceException(
					"load(): fail to load NER MODEL \"" + nerModel + "\".");
		}
		return nameFinder;
	}
	
	protected void init() throws OpenNLPWebServiceException {
		logger.info("init(): Creating OpenNLP NamedEntityRecognizer ...");

		Properties prop = new Properties();
//		InputStream stream = ResourceLoader
//				.open("opennlp-web-service.properties");
        InputStream stream = this.getClass().getResourceAsStream("/" + "opennlp-web-service.properties");
		if (stream == null) {
			logger.error("init(): fail to open \"opennlp-web-service.properties\".");
			throw new OpenNLPWebServiceException(
					"init(): fail to open \"opennlp-web-service.properties\".");
		}
		try {
			prop.load(stream);
			stream.close();
		} catch (IOException e) {
			logger.error("init(): fail to load \"opennlp-web-service.properties\".");
			throw new OpenNLPWebServiceException(
					"init(): fail to load \"opennlp-web-service.properties\".");
		}

		// default English
		String nerModels = prop.getProperty(PROP_COMPNENT_MODEL,
				"en-ner-person.bin");
		logger.info("init(): load opennlp-web-service.properties.");

		for (String nerModel : nerModels.split(":")) {
			logger.info("init(): load " + nerModel + " ...");
			if (nerModel.trim().length() > 0) {
				TokenNameFinder nameFinder = load(nerModel);
				if (nameFinder != null)
					nameFinders.add(nameFinder);
			}
		}

		logger.info("init(): Creating OpenNLP NamedEntityRecognizer!");
	}

	@Override
	public Data configure(Data data) {
		return DataFactory.ok();
	}

	@Override
    public Data execute(Data data) {
        logger.info("execute(): Execute OpenNLP NamedEntityRecognizer ...");

        long discriminator = data.getDiscriminator();
        if (discriminator == Types.ERROR)
        {
            return data;
        } else if (discriminator == Types.JSON) {

            String jsonstr = data.getPayload();
            JsonNERSerialization json = new JsonNERSerialization(jsonstr);
            json.setProducer(this.getClass().getName() + ":" + VERSION);
            json.setType("ner:opennlp");
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

            for (TokenNameFinder nameFinder : nameFinders) {
                Span [] partSpans = nameFinder.find(tokens);
                for (Span span:partSpans){
                    JSONObject annotation = json.newAnnotationWithType(span.getType(), tokenObjs.get(span.getStart()));
                }
            }
            return DataFactory.json(json.toString());
        } else if (discriminator == Types.TEXT)
        {
            String text = data.getPayload();
            JsonNERSerialization json = new JsonNERSerialization();
            json.setTextValue(text);
            json.setProducer(this.getClass().getName() + ":" + VERSION);
            json.setType("ner:opennlp");

            Span[] spans = find(new String[]{text});
            for (Span span : spans) {
                JSONObject annotation = json.newAnnotationWithType(span.getType());
                json.setWord(annotation, text);
                json.setStart(annotation, 0);
                json.setEnd(annotation, text.length());
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
	public Span[] find(String[] tokens) {
		if (nameFinders.size() == 0) {
			try {
				init();
			} catch (OpenNLPWebServiceException e) {
				throw new RuntimeException(
						"tokenize(): Fail to initialize NamedEntityRecognizer",
						e);
			}
		}
		ArrayList<Span> spanArr = new ArrayList<Span>(16);
		for (TokenNameFinder nameFinder : nameFinders) {
			Span[] partSpans = nameFinder.find(tokens);
			for (Span span:partSpans)
				spanArr.add(span);
		}
		
		return spanArr.toArray(new Span[spanArr.size()]);
	}

}
