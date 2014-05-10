package edu.brandeis.cs.lappsgrid.opennlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import edu.brandeis.cs.lappsgrid.api.opennlp.IVersion;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

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

import edu.brandeis.cs.lappsgrid.api.opennlp.IParser;

/**
 * <i>Parser.java</i> Language Application Grids
 * (<b>LAPPS</b>)
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
public class Parser extends AbstractWebService implements IParser {
	protected static final Logger logger = LoggerFactory
			.getLogger(Parser.class);

	private static opennlp.tools.parser.Parser parser;

	public Parser() throws OpenNLPWebServiceException {
        if (parser == null)
		    init();
	}

	protected void init() throws OpenNLPWebServiceException {
		logger.info("init(): Creating OpenNLP Parser ...");

		Properties prop = new Properties();

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
		String parserModel = prop.getProperty(PROP_COMPNENT_MODEL,
				"en-parser-chunking.bin");

		logger.info("init(): load opennlp-web-service.properties.");

//		stream = ResourceLoader.open(parserModel);
        stream = this.getClass().getResourceAsStream("/" + parserModel);
		if (stream == null) {
			logger.error("init(): fail to open PARSER MODEl \"" + parserModel
					+ "\".");
			throw new OpenNLPWebServiceException(
					"init(): fail to open PARSER MODEl \"" + parserModel + "\".");
		}

		logger.info("init(): load PARSER MODEl \"" + parserModel + "\"");

		try {
			try {
				ParserModel model = new ParserModel(stream);
				parser = ParserFactory.create(model);
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			logger.error("init(): fail to load PARSER MODEl \"" + parserModel
					+ "\".");
			throw new OpenNLPWebServiceException(
					"init(): fail to load PARSER MODEl \"" + parserModel + "\".");
		}

		logger.info("init(): Creating OpenNLP Parser!");
	}

	@Override
	public Data configure(Data data) {
		return DataFactory.ok();
	}

	@Override
	public Data execute(Data data) {
		logger.info("execute(): Execute OpenNLP Parser ...");

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
                    System.out.println("annotation:" + annotation);
                    if(annotation.has("@type") && annotation.getString("@type").equals(Annotations.SENTENCE)){
                        laststeparr.add(annotation);
                    }
                }
            }

            IDGenerator id = new IDGenerator();
            JSONArray annotations =  new JSONArray();
            for(JSONObject sentenceannotation: laststeparr) {
                int start = sentenceannotation.getInt("start");
                int end = sentenceannotation.getInt("end");
                String sentence = text.substring(start, end);
                String pattern = parse(sentence);
                JSONObject annotation = new JSONObject(sentenceannotation.toString());
                annotation.put("id", id.generate("parser"));
                annotation.put("@type", Annotations.SENTENCE);
                annotation.put("features", new JSONObject().put("Pattern", pattern));
                annotations.put(annotation);
            }

            // put into json.
            JSONObject resultStep = new JSONObject();
            JSONObject resultContain = new JSONObject();
            resultContain.put("producer", this.getClass().getName() + ":" + VERSION);
            resultContain.put("type", "parser:opennlp");
            resultStep.put("metadata", new JSONObject().put("contains", new JSONObject().put(Annotations.SENTENCE, resultContain)));
            resultStep.put("annotations", annotations);
            jsonobj.put("steps", steps.put(resultStep));
            return DataFactory.json(jsonobj.toString());

        } else if (discriminator == Types.TEXT) {
            String text = data.getPayload();
            String pattern = parse(text);
            IDGenerator id = new IDGenerator();
            JSONArray annotations = new JSONArray();
            JSONObject annotation = new JSONObject();
            annotation.put("id", id.generate("parser"));
            annotation.put("start", 0);
            annotation.put("end", text.length());
            annotation.put("@type", Annotations.SENTENCE);
            annotation.put("features", new JSONObject().put("Pattern", pattern));
            annotations.put(annotation);

            JSONObject resultStep = new JSONObject();
            JSONObject resultContain = new JSONObject();
            resultContain.put( "producer", this.getClass().getName() + ":" + VERSION);
            resultContain.put( "type", "parser:opennlp");
            resultStep.put("metadata", new JSONObject().put("contains", new JSONObject().put( Annotations.SENTENCE, resultContain)));
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
	public String parse(String sentence) {
		if (parser == null) {
			try {
				init();
			} catch (OpenNLPWebServiceException e) {
				throw new RuntimeException(
						"parse(): Fail to initialize Parser", e);
			}
		}

		StringBuffer builder = new StringBuffer();
		Parse parses[] = ParserTool.parseLine(sentence, parser, 1);

		for (int pi = 0, pn = parses.length; pi < pn; pi++) {
			parses[pi].show(builder);
			builder.append("\n");
		}

		return builder.toString();
	}

}
