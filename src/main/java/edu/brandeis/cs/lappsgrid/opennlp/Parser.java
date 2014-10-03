package edu.brandeis.cs.lappsgrid.opennlp;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.lappsgrid.serialization.json.JsonTaggerSerialization;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

import org.lappsgrid.serialization.json.JSONObject;
import org.lappsgrid.api.Data;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
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
    public Data getMetadata() {
        return null;
    }

    @Override
	public Data execute(Data data) {
		logger.info("execute(): Execute OpenNLP Parser ...");
        String discriminatorstr = data.getDiscriminator();
        long discriminator = DiscriminatorRegistry.get(discriminatorstr);
        if (discriminator == Types.ERROR)
        {
            return data;
        } else if (discriminator == Types.JSON) {
            String jsonstr = data.getPayload();
            JsonTaggerSerialization json = new JsonTaggerSerialization(jsonstr);
            json.setProducer(this.getClass().getName() + ":" + VERSION);
            json.setType("parser:opennlp");
            List<JSONObject> annotationObjs = json.findLastAnnotations();
            if (annotationObjs == null) {
                String message = "Invalid JSON input. Expected annotation type: " + json.getLastAnnotationType();
                logger.warn(message);
                return DataFactory.error(message);
            }

            for(int i = 0; i < annotationObjs.size(); i++ ) {
                String s = json.getAnnotationTextValue(annotationObjs.get(i));
                JSONObject annotation = json.newAnnotation(annotationObjs.get(i));
                json.setPattern(annotation, parse(s));
            }
            return DataFactory.json(json.toString());
        } else if (discriminator == Types.TEXT) {

            String textvalue = data.getPayload();
            JsonTaggerSerialization json = new JsonTaggerSerialization();
            json.setProducer(this.getClass().getName() + ":" + VERSION);
            json.setType("parser:opennlp");
            json.setTextValue(textvalue);

            String pattern = parse(textvalue);

            JSONObject annotation = json.newAnnotation();

            json.setStart(annotation, 0);
            json.setEnd(annotation, textvalue.length());
            json.setPattern(annotation, pattern);
            return DataFactory.json(json.toString());
        } else {
            String name = DiscriminatorRegistry.get(discriminator);
            String message = "Invalid input type. Expected JSON but found " + name;
            logger.warn(message);
            return DataFactory.error(message);
        }
	}

//	@Override
//	public long[] requires() {
//		return TYPES_REQUIRES;
//	}
//
//	@Override
//	public long[] produces() {
//		return TYPES_PRODUCES;
//	}

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
