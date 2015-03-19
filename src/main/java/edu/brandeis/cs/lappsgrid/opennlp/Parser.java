package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.api.opennlp.IParser;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
public class Parser extends OpenNLPAbstractWebService implements IParser {
	protected static final Logger logger = LoggerFactory
			.getLogger(Parser.class);

	private static opennlp.tools.parser.Parser parser;

	public Parser() throws OpenNLPWebServiceException {
        if (parser == null) {
            init();
            parser = loadParser(registModelMap.get(this.getClass()));
        }
	}

//	protected void init() throws OpenNLPWebServiceException {
//		logger.info("init(): Creating OpenNLP Parser ...");
//
//		Properties prop = new Properties();
//
//		InputStream stream = this.getClass().getResourceAsStream("/" + "opennlp-web-service.properties");
//		if (stream == null) {
//			logger.error("init(): fail to open \"opennlp-web-service.properties\".");
//			throw new OpenNLPWebServiceException(
//					"init(): fail to open \"opennlp-web-service.properties\".");
//		}
//		try {
//			prop.load(stream);
//			stream.close();
//		} catch (IOException e) {
//			logger.error("init(): fail to load \"opennlp-web-service.properties\".");
//			throw new OpenNLPWebServiceException(
//					"init(): fail to load \"opennlp-web-service.properties\".");
//		}
//
//		// default English
//		String parserModel = prop.getProperty(PROP_COMPNENT_MODEL,
//				"en-parser-chunking.bin");
//
//		logger.info("init(): load opennlp-web-service.properties.");
//
////		stream = ResourceLoader.open(parserModel);
//        stream = this.getClass().getResourceAsStream("/" + parserModel);
//		if (stream == null) {
//			logger.error("init(): fail to open PARSER MODEl \"" + parserModel
//					+ "\".");
//			throw new OpenNLPWebServiceException(
//					"init(): fail to open PARSER MODEl \"" + parserModel + "\".");
//		}
//
//		logger.info("init(): load PARSER MODEl \"" + parserModel + "\"");
//
//		try {
//			try {
//				ParserModel model = new ParserModel(stream);
//				parser = ParserFactory.create(model);
//			} finally {
//				stream.close();
//			}
//		} catch (IOException e) {
//			logger.error("init(): fail to load PARSER MODEl \"" + parserModel
//					+ "\".");
//			throw new OpenNLPWebServiceException(
//					"init(): fail to load PARSER MODEl \"" + parserModel + "\".");
//		}
//
//		logger.info("init(): Creating OpenNLP Parser!");
//	}

//	@Override
//	public Data configure(Data data) {
//		return DataFactory.ok();
//	}
//
//    static Data metadata = loadMetadata();
//
//    static private Data loadMetadata() {
//        Data metadata = null;
//        try {
//            String json = "";
//            metadata = DataFactory.meta(json);
//        } catch(Exception e){
//            metadata = DataFactory.error("Unable to load metadata", e);
//        }
//        return metadata;
//    }
//
//    public Data getMetadata() {
//        return metadata;
//    }
//
//    @Override
//	public Data execute(Data data) {
//		logger.info("execute(): Execute OpenNLP Parser ...");
//        String discriminatorstr = data.getDiscriminator();
//        long discriminator = DiscriminatorRegistry.get(discriminatorstr);
//        if (discriminator == Types.ERROR)
//        {
//            return data;
//        } else if (discriminator == Types.JSON) {
//            String jsonstr = data.getPayload();
//            JsonTaggerSerialization json = new JsonTaggerSerialization(jsonstr);
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("parser:opennlp");
//            List<JSONObject> annotationObjs = json.findLastAnnotations();
//            if (annotationObjs == null) {
//                String message = "Invalid JSON input. Expected annotation type: " + json.getLastAnnotationType();
//                logger.warn(message);
//                return DataFactory.error(message);
//            }
//
//            for(int i = 0; i < annotationObjs.size(); i++ ) {
//                String s = json.getAnnotationTextValue(annotationObjs.get(i));
//                JSONObject annotation = json.newAnnotation(annotationObjs.get(i));
//                json.setPattern(annotation, parse(s));
//            }
//            return DataFactory.json(json.toString());
//        } else if (discriminator == Types.TEXT) {
//
//            String textvalue = data.getPayload();
//            JsonTaggerSerialization json = new JsonTaggerSerialization();
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("parser:opennlp");
//            json.setTextValue(textvalue);
//
//            String pattern = parse(textvalue);
//
//            JSONObject annotation = json.newAnnotation();
//
//            json.setStart(annotation, 0);
//            json.setEnd(annotation, textvalue.length());
//            json.setPattern(annotation, pattern);
//            return DataFactory.json(json.toString());
//        } else {
//            String name = DiscriminatorRegistry.get(discriminator);
//            String message = "Invalid input type. Expected JSON but found " + name;
//            logger.warn(message);
//            return DataFactory.error(message);
//        }
//	}

	public String parse(String sentence) {
		StringBuffer builder = new StringBuffer();
		Parse parses[] = ParserTool.parseLine(sentence, parser, 1);
		for (int pi = 0, pn = parses.length; pi < pn; pi++) {
			parses[pi].show(builder);
			builder.append("\n");
		}
		return builder.toString();
	}


    @Override
    public String execute(LIFJsonSerialization json) throws OpenNLPWebServiceException {
        String txt = json.getText();
        JsonObj view = json.newView();


        json.newContains(view, "Parse", "parser:opennlp", this.getClass().getName() + ":" + VERSION);
        List<JsonObj> annotationObjs = json.findLastAnnotations();
        for(int i = 0; i < annotationObjs.size(); i++ ) {
            String s = json.getAnnotationText(annotationObjs.get(i));
            JsonObj annotation = json.newAnnotation(view, annotationObjs.get(i));
            json.setFeature(annotation, "pattern", parse(s));
        }
        return json.toString();
    }
}
