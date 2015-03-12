package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.api.opennlp.ITokenizer;
import opennlp.tools.util.Span;

/**
 * <i>Tokenizer.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p><a href="http://opennlp.sourceforge.net/models-1.5/">Models for 1.5 series</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class Tokenizer extends OpenNLPAbstractWebService implements ITokenizer {
    private static opennlp.tools.tokenize.Tokenizer tokenizer;

	public Tokenizer() throws OpenNLPWebServiceException {
        if (tokenizer == null) {
            init();
            tokenizer = loadTokenizer(registModelMap.get(this.getClass()));
        }
	}

//
//
//    @Override
//    public Data execute(Data data) {
//        logger.info("execute(): Execute OpenNLP tokenizer ...");
//
//        String discriminatorstr = data.getDiscriminator();
//        long discriminator = DiscriminatorRegistry.get(discriminatorstr);
//
//        if (discriminator == Types.ERROR)
//        {
//            return data;
//        } else if (discriminator == Types.JSON) {
//            String jsonstr = data.getPayload();
//            JsonTokenizerSerialization json = new JsonTokenizerSerialization(jsonstr);
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("tokenizer:opennlp");
//            Span[] spans = tokenizePos(json.getTextValue());
//            for (Span span : spans) {
//                JSONObject ann = json.newAnnotation();
//                json.setStart(ann, span.getStart());
//                json.setEnd(ann, span.getEnd());
//                json.setWord(ann, json.getTextValue().substring(span.getStart(), span.getEnd()));
//            }
//            return DataFactory.json(json.toString());
//
//        } else if (discriminator == Types.TEXT) {
//            String text = data.getPayload();
//            JsonTokenizerSerialization json = new JsonTokenizerSerialization();
//            json.setProducer(this.getClass().getName() + ":" + VERSION);
//            json.setType("tokenizer:opennlp");
//            json.setTextValue(text);
//            Span[] spans = tokenizePos(text);
//            for (Span span : spans) {
//                JSONObject ann = json.newAnnotation();
//                json.setStart(ann, span.getStart());
//                json.setEnd(ann, span.getEnd());
//                json.setWord(ann, json.getTextValue().substring(span.getStart(), span.getEnd()));
//            }
//            return DataFactory.json(json.toString());
//        } else {
//            String name = DiscriminatorRegistry.get(discriminator);
//            String message = "Invalid input type. Expected JSON but found " + name;
//            logger.warn(message);
//            return DataFactory.error(message);
//        }
//    }


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

    @Override
    public String execute(String s) {
        return null;
    }

    @Override
    public String getMetadata() {
        return null;
    }
}
