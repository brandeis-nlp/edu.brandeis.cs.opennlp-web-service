package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.api.opennlp.INamedEntityRecognizer;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.util.Span;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
public class NamedEntityRecognizer extends OpenNLPAbstractWebService implements INamedEntityRecognizer {
	protected static final Logger logger = LoggerFactory
			.getLogger(NamedEntityRecognizer.class);

	private static ArrayList<TokenNameFinder> nameFinders = new ArrayList<TokenNameFinder> ();

	public NamedEntityRecognizer() throws OpenNLPWebServiceException {
        if (nameFinders.size() == 0) {
            init();
            nameFinders.addAll(loadTokenNameFinders(registModelMap.get(this.getClass())).values());
        }
	}

//	protected static final TokenNameFinder load(String nerModel) throws OpenNLPWebServiceException {
//		TokenNameFinder nameFinder;
////		InputStream stream = ResourceLoader.open(nerModel);
//        InputStream stream = NamedEntityRecognizer.class.getResourceAsStream("/" + nerModel);
//		if (stream == null) {
//			logger.error("load(): fail to open NER MODEL \"" + nerModel
//					+ "\".");
//			throw new OpenNLPWebServiceException(
//					"load(): fail to open NER MODEL \"" + nerModel + "\".");
//		}
//
//		logger.info("load(): load NER MODEL \"" + nerModel + "\"");
//
//		try {
//			try {
//				TokenNameFinderModel model = new TokenNameFinderModel(stream);
//				nameFinder = new NameFinderME(model);
//			} finally {
//				stream.close();
//			}
//		} catch (IOException e) {
//			logger.error("load(): fail to load NER MODEL \"" + nerModel
//					+ "\".");
//			throw new OpenNLPWebServiceException(
//					"load(): fail to load NER MODEL \"" + nerModel + "\".");
//		}
//		return nameFinder;
//	}
//
//	protected void init() throws OpenNLPWebServiceException {
//		logger.info("init(): Creating OpenNLP NamedEntityRecognizer ...");
//
//		Properties prop = new Properties();
////		InputStream stream = ResourceLoader
////				.open("opennlp-web-service.properties");
//        InputStream stream = this.getClass().getResourceAsStream("/" + "opennlp-web-service.properties");
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
//		String nerModels = prop.getProperty(PROP_COMPNENT_MODEL,
//				"en-ner-person.bin");
//		logger.info("init(): load opennlp-web-service.properties.");
//
//		for (String nerModel : nerModels.split(":")) {
//			logger.info("init(): load " + nerModel + " ...");
//			if (nerModel.trim().length() > 0) {
//				TokenNameFinder nameFinder = load(nerModel);
//				if (nameFinder != null)
//					nameFinders.add(nameFinder);
//			}
//		}
//
//		logger.info("init(): Creating OpenNLP NamedEntityRecognizer!");
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
//
    public static String capitalize(String s) {
        if (s == null || s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
//
//
//    @Override
//    public Data execute(Data data) {
//        logger.info("execute(): Execute OpenNLP NamedEntityRecognizer ...");
//        String discriminatorstr = data.getDiscriminator();
//        long discriminator = DiscriminatorRegistry.get(discriminatorstr);
//        if (discriminator == Types.ERROR)
//        {
//            return data;
//        } else if (discriminator == Types.JSON) {
//
//            String jsonstr = data.getPayload();
//            JsonNERSerialization json = new JsonNERSerialization(jsonstr);
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("ner:opennlp");
//            List<JSONObject> tokenObjs = json.findLastAnnotations();
//            if (tokenObjs == null) {
//                String message = "Invalid JSON input. Expected annotation type: " + json.getLastAnnotationType();
//                logger.warn(message);
//                return DataFactory.error(message);
//            }
//
//            String[] tokens = new String[tokenObjs.size()];
//            for(int i = 0; i < tokens.length; i++ ) {
//                tokens[i] = json.getAnnotationTextValue(tokenObjs.get(i));
//            }
//
//            for (TokenNameFinder nameFinder : nameFinders) {
//                Span [] partSpans = nameFinder.find(tokens);
//                for (Span span:partSpans){
//                    JSONObject annotation = json.newAnnotationWithType(
//                            capitalize(span.getType()), tokenObjs.get(span.getStart()));
//                    json.newContain(
//                            capitalize(span.getType()));
//                }
//            }
//            return DataFactory.json(json.toString());
//        } else if (discriminator == Types.TEXT)
//        {
//            String text = data.getPayload();
//            JsonNERSerialization json = new JsonNERSerialization();
//            json.setTextValue(text);
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("ner:opennlp");
//
//            Span[] spans = find(new String[]{text});
//            for (Span span : spans) {
//                JSONObject annotation = json.newAnnotationWithType(
//                        capitalize(span.getType()));
//                json.newContain(
//                        capitalize(span.getType()));
//                json.setWord(annotation, text);
//                json.setStart(annotation, 0);
//                json.setEnd(annotation, text.length());
//            }
//            return DataFactory.json(json.toString());
//
//        } else {
//            String message = "Invalid input type. Expected JSON but found " + discriminatorstr;
//            logger.warn(message);
//            return DataFactory.error(message);
//        }
//    }

	public Span[] find(String[] tokens) {
		if (nameFinders.size() == 0) {
			try {
				init();
			} catch (OpenNLPWebServiceException e) {
				throw new RuntimeException(
						"tokenize(): Fail to initialize NamedEntityRecognizer", e);
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

    @Override
    public String execute(LIFJsonSerialization json) throws OpenNLPWebServiceException {
        logger.info("execute(): Execute OpenNLP ner ...");
        String txt = json.getText();
        JsonObj view = json.newView();

        json.newContains(view, Discriminators.Uri.TOKEN,
                "ner:opennlp", this.getClass().getName() + ":" + Version.getVersion());
        json.setIdHeader("tok");

        List<JsonObj> tokenObjs = json.getLastViewAnnotations();
        if (tokenObjs == null) {
            // is word.
            if (txt.matches("[a-zA-Z]+")) {
                for (TokenNameFinder nameFinder : nameFinders) {
                    Span [] partSpans = nameFinder.find(new String[]{txt});
                    for (Span span:partSpans){
                        JsonObj annotation =  json.newAnnotation(view);
                        json.setStart(annotation, 0);
                        json.setEnd(annotation, txt.length());
                        json.setLabel(annotation, Discriminators.Uri.NE);
                        json.setFeature(annotation, "category", span.getType());
                    }
                }
            } else {
                throw new OpenNLPWebServiceException("Wrong Input: CANNOT find " + Discriminators.Uri.TOKEN);
            }
        } else {
            String[] tokens = new String[tokenObjs.size()];
            for(int i = 0; i < tokens.length; i++ ) {
                tokens[i] = json.getAnnotationText(tokenObjs.get(i));
            }

            for (TokenNameFinder nameFinder : nameFinders) {
                Span [] partSpans = nameFinder.find(tokens);
                for (Span span:partSpans){
                    JsonObj org = tokenObjs.get(span.getStart());
                    JsonObj annotation = json.newAnnotation(view, org);
                    json.setLabel(annotation, Discriminators.Uri.NE);
                    json.setFeature(annotation, "category", span.getType());
                }
            }
        }
        return json.toString();
    }
}
