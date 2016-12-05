package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.api.opennlp.IPOSTagger;
import opennlp.tools.util.Sequence;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

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
    public String execute(Container container) throws OpenNLPWebServiceException {
        logger.info("execute(): Execute OpenNLP tokenizer ...");
        String txt = container.getText();

        List<View> tokenViews = container.findViewsThatContain(Uri.TOKEN);
        if (tokenViews.size() == 0) {
            throw new OpenNLPWebServiceException(String.format(
                    "Wrong Input: CANNOT find %s within previous annotations",
                    Uri.TOKEN));
        }
        List<Annotation> tokenAnns = tokenViews.get(tokenViews.size() - 1).getAnnotations();

        View view = container.newView();
        view.addContains(Uri.POS,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "tagger:opennlp");

        int count = 0;
        if (tokenAnns == null || tokenAnns.size() == 0) {
            // is word.
            if (txt.matches("[a-zA-Z]+")) {
                String [] tags = tag(new String []{txt});
                for(int i = 0; i < tags.length; i++) {
                    Annotation ann = view.newAnnotation(POS_ID + count++,
                            Uri.POS, 0, txt.length());
                    ann.addFeature(Features.Token.POS, tags[i]);
                }
            } else {
                throw new OpenNLPWebServiceException(String.format(
                        "Wrong Input: CANNOT find %s within previous annotations",
                        Uri.TOKEN));
            }
        } else {
            String [] tokens = new String [tokenAnns.size()];
            for (int i = 0; i < tokenAnns.size(); i ++) {
                tokens[i] = getTokenText(tokenAnns.get(i), txt);
            }
            String [] tags = tag(tokens);
            for(int i = 0; i < tags.length; i++) {
                Annotation ann =  view.newAnnotation(POS_ID + count++, Uri.POS,
                        tokenAnns.get(i).getStart(), tokenAnns.get(i).getEnd());
                ann.addFeature(Features.Token.POS, tags[i]);
            }
        }
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }
}
