package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.opennlp.api.ITokenizer;
import opennlp.tools.util.Span;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

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
    public String execute(Container container) throws OpenNLPWebServiceException {
        logger.info("execute(): Execute OpenNLP tokenizer ...");
        String txt = container.getText();
        View view = container.newView();
        view.addContains(Uri.TOKEN,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "tokenizer:opennlp");
        Span[] spans = tokenizePos(txt);
        int count = 0;
        for (Span span : spans) {
            int start = span.getStart();
            int end = span.getEnd();
            Annotation ann = view.newAnnotation(TOKEN_ID + count++,
                    Uri.TOKEN, start, end);
            ann.getFeatures().put("word", txt.substring(start, end));
        }
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }
}
