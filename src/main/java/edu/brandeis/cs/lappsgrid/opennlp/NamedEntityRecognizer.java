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
    public String execute(LIFJsonSerialization json) throws OpenNLPWebServiceException {
        logger.info("execute(): Execute OpenNLP ner ...");
        String txt = json.getText();
        List<JsonObj> tokenObjs = json.getLastViewAnnotations();

        JsonObj view = json.newView();
        json.newContains(view, Discriminators.Uri.NE,
                "ner:opennlp", this.getClass().getName() + ":" + Version.getVersion());
        json.setIdHeader("tok");
        int cnt = 0;
        if (tokenObjs == null || tokenObjs.size() == 0)  {
            // is word.
            if (txt.matches("[a-zA-Z]+")) {
                for (TokenNameFinder nameFinder : nameFinders) {
                    Span [] partSpans = nameFinder.find(new String[]{txt});
                    for (Span span:partSpans){
                        JsonObj annotation =  json.newAnnotation(view);
                        json.setId(annotation, "ne"+cnt++);
                        json.setType(annotation, Discriminators.Uri.NE);
                        json.setStart(annotation, 0);
                        json.setEnd(annotation, txt.length());
                        json.setWord(annotation,txt);
                        json.setCategory(annotation, span.getType());
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
                    json.setWord(annotation, json.getAnnotationText(annotation));
                    json.setCategory(annotation, span.getType());
                }
            }
        }
        return json.toString();
    }
}
