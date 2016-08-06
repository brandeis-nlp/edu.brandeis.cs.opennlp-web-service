package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.api.opennlp.ITokenizer;
import opennlp.tools.util.Span;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;

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
    public String execute(LIFJsonSerialization json) throws OpenNLPWebServiceException {
        logger.info("execute(): Execute OpenNLP tokenizer ...");
        String txt = json.getText();
        JsonObj view = json.newView();
        json.newContains(view, Discriminators.Uri.TOKEN,
                "tokenizer:opennlp", this.getClass().getName() + ":" + Version.getVersion());
        json.setIdHeader("tok");
        Span[] spans = tokenizePos(txt);
        for (Span span : spans) {
            int start = span.getStart();
            int end = span.getEnd();
            JsonObj ann = json.newAnnotation(view, Discriminators.Uri.TOKEN, start, end);
            json.setWord(ann, txt.substring(start, end));
        }
        return json.toString();
    }
}
