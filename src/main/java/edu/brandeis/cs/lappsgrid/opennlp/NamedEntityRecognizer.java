package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.api.opennlp.INamedEntityRecognizer;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.util.Span;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
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
            super.init();
            nameFinders.addAll(loadTokenNameFinders(registModelMap.get(this.getClass())).values());
        }
    }

    public static String capitalize(String s) {
        if (s == null || s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

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
    public String execute(Container container) throws OpenNLPWebServiceException {
        logger.info("execute(): Execute OpenNLP ner ...");
        String txt = container.getText();
        List<View> tokenViews = container.findViewsThatContain(Uri.TOKEN);
        List<Annotation> tokenAnns = tokenViews.get(tokenViews.size()).getAnnotations();


        View view = container.newView();
        view.addContains(Uri.NE,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "ner:opennlp");
        int count = 0;
        if (tokenAnns == null || tokenAnns.size() == 0)  {
            // is word.
            if (txt.matches("[a-zA-Z]+")) {
                for (TokenNameFinder nameFinder : nameFinders) {
                    Span [] neSpans = nameFinder.find(new String[]{txt});
                    for (Span span:neSpans){
                        String atType = getNEType(span);
                        Annotation annotation =  view.newAnnotation(NE_ID + count++,
                                atType, 0, txt.length());
                        annotation.addFeature("word", txt);
                    }
                }
            } else {
                throw new OpenNLPWebServiceException(String.format(
                        "Wrong Input: CANNOT find %s within previous annotations",
                        Uri.TOKEN));
            }
        } else {
            String[] tokens = new String[tokenAnns.size()];
            for(int i = 0; i < tokens.length; i++ ) {
                tokens[i] = getTokenText(tokenAnns.get(i), txt);
            }
            for (TokenNameFinder nameFinder : nameFinders) {
                Span [] namedSpans = nameFinder.find(tokens);
                for (Span span:namedSpans){
                    // namedSpans will keep all named-entities as (start_tok_id, end_tok_id) pairs
                    Long start = tokenAnns.get(span.getStart()).getStart();
                    Long end = tokenAnns.get(span.getEnd()).getEnd();
                    String atType = getNEType(span);
                    Annotation ann = view.newAnnotation(NE_ID + count++, atType, start, end);
                    ann.addFeature("word", txt.substring(start.intValue(), end.intValue()));
                }
            }
        }
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    private String getNEType(Span span) {
        String atType = null;
        switch (span.getType().toLowerCase()) {
            case "location":
                atType = Uri.LOCATION;
                break;
            case "organization":
                atType = Uri.ORGANIZATION;
                break;
            case "date":
                atType = Uri.DATE;
                break;
            case "person":
                atType = Uri.PERSON;
                break;
        }
        return atType;
    }
}
