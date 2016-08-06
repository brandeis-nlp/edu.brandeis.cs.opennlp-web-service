package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.api.opennlp.IPOSTagger;
import opennlp.tools.util.Sequence;
import opennlp.tools.util.Span;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;

import java.util.List;

/**
 * <i>POSTagger.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p><a href="http://opennlp.sourceforge.net/models-1.5/">Models for 1.5 series</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class POSTagger extends OpenNLPAbstractWebService implements IPOSTagger  {
    
    private static opennlp.tools.postag.POSTagger postagger;
    
    
	public POSTagger() throws OpenNLPWebServiceException {
        if (postagger == null) {
            init();
            postagger = loadPOSTagger(registModelMap.get(this.getClass()));
        }
	}

	@Override
	public String[] tag(String[] sentence) {
		if (postagger == null) {
			try {
				init();
			} catch (OpenNLPWebServiceException e) {
				throw new RuntimeException("tokenize(): Fail to initialize POSTagger", e);
			}
		}
		String tags[] = postagger.tag(sentence);
		return tags;
	}


	@Override
	public Sequence[] topKSequences(String[] sentence) {
		if (postagger == null) {
			try {
				init();
			} catch (OpenNLPWebServiceException e) {
				throw new RuntimeException("tokenize(): Fail to initialize POSTagger", e);
			}
		}
		Sequence tags[] = postagger.topKSequences(sentence);
		return tags;
	}

    @Override
    public String execute(LIFJsonSerialization json) throws OpenNLPWebServiceException {
        logger.info("execute(): Execute OpenNLP tokenizer ...");
        String txt = json.getText();
        List<JsonObj> tokenAnns = json.getLastViewAnnotations(Discriminators.Uri.TOKEN);

        JsonObj view = json.newView();
        json.newContains(view, Discriminators.Uri.POS,
                "tagger:opennlp", this.getClass().getName() + ":" + Version.getVersion());
        int cnt = 0;
        if (tokenAnns == null || tokenAnns.size() == 0) {
            // is word.
            if (txt.matches("[a-zA-Z]+")) {
                String [] tags = tag(new String []{txt});
                for(int i = 0; i < tags.length; i++) {
                    JsonObj annotation =  json.newAnnotation(view);
                    json.setId(annotation, "pos"+cnt++);
                    json.setType(annotation, Discriminators.Uri.POS);
                    json.setStart(annotation, 0);
                    json.setEnd(annotation, txt.length());
                    json.setPOSTag(annotation, tags[i]);
                }
            } else {
                throw new OpenNLPWebServiceException(String.format(
                        "Wrong Input: CANNOT find %s within previous annotations",
                        Discriminators.Uri.TOKEN));
            }
        } else {
            String [] tokens = new String [tokenAnns.size()];
            for (int i = 0; i < tokenAnns.size(); i ++) {
                tokens[i] = json.getAnnotationText(tokenAnns.get(i));
            }
            String [] tags = tag(tokens);
            for(int i = 0; i < tags.length; i++) {
                JsonObj annotation =  json.newAnnotation(view, tokenAnns.get(i));
                json.setType(annotation, Discriminators.Uri.POS);
                json.setPOSTag(annotation, tags[i]);
            }
        }
        return json.toString();
    }
}
