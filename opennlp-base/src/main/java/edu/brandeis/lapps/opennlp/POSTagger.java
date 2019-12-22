package edu.brandeis.lapps.opennlp;

import edu.brandeis.lapps.BrandeisServiceException;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.util.Sequence;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

import java.util.List;

public class POSTagger extends AbstractOpennlpWrapper {
    private static String MODEL_NAME = DEFAULT_MODEL_RES_FILE_MAP.get(POSTagger.class);
    private static String TOOL_DESCRIPTION = String.format("This service is a wrapper around Apache OpenNLP %s " +
                    "providing an English part-of-speech tagger service. Internally it uses public OpenNLP-1.5 models " +
                    "(available at http://opennlp.sourceforge.net/models-1.5/), in particular, \"%s\" is used.",
            getWrappeeVersion(), MODEL_NAME);
    private opennlp.tools.postag.POSTagger postagger;

    public POSTagger() throws BrandeisServiceException {
        loadAnnotators();
    }

    @Override
    synchronized protected void loadAnnotators() throws BrandeisServiceException {
        super.loadPOSModel();
        postagger = new POSTaggerME(posModel);
    }

    public String[] tag(String[] sentence) {
        if (postagger == null) {
            try {
                loadAnnotators();
            } catch (BrandeisServiceException e) {
                throw new RuntimeException("Fail to initialize POSTagger", e);
            }
        }
        String tags[] = postagger.tag(sentence);
        return tags;
    }


    public Sequence[] topKSequences(String[] sentence) {
        if (postagger == null) {
            try {
                loadAnnotators();
            } catch (BrandeisServiceException e) {
                throw new RuntimeException("Fail to initialize POSTagger", e);
            }
        }
        Sequence tags[] = postagger.topKSequences(sentence);
        return tags;
    }

    @Override
    public String processPayload(Container container) throws BrandeisServiceException {
        logger.info("Executing");
        String txt = container.getText();
        List<View> tokenViews = container.findViewsThatContain(Uri.TOKEN);


        // throw exception here, the outer execute method will wrap it into a LEDS
        if (tokenViews.size() == 0) {
            throw new BrandeisServiceException(unmetRequirements(Uri.TOKEN));
        }
        View tokenView =  tokenViews.get(tokenViews.size() - 1);
        List<Annotation> tokenAnns = tokenView.getAnnotations();

        View view = container.newView();
        setUpContainsMetadata(view, PRODUCER_ALIAS);

        int count = 0;
        if (tokenAnns == null || tokenAnns.size() == 0) {
            // is word.
            if (txt.matches("[a-zA-Z]+")) {
                String [] tags = tag(new String []{txt});
                for(int i = 0; i < tags.length; i++) {
                    Annotation ann = view.newAnnotation(TOKEN_ID + count++,
                            Uri.POS, 0, txt.length());
                    ann.addFeature(Features.Token.POS, tags[i]);
                }
            } else {
                throw new BrandeisServiceException(String.format(
                        "Invalid input: could not find proper words in \"%s\" view.", tokenView));
            }
        } else {
            String [] tokens = new String [tokenAnns.size()];
            for (int i = 0; i < tokenAnns.size(); i ++) {
                tokens[i] = getTokenText(tokenAnns.get(i), txt);
            }
            String [] tags = tag(tokens);
            for(int i = 0; i < tags.length; i++) {
                Annotation ann =  view.newAnnotation(TOKEN_ID + count++, Uri.POS,
                        tokenAnns.get(i).getStart(), tokenAnns.get(i).getEnd());
                ann.addFeature(Features.Token.POS, tags[i]);
            }
        }
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    public ServiceMetadata loadMetadata() {
        ServiceMetadata meta = setDefaultMetadata();
        meta.setDescription(TOOL_DESCRIPTION);
        meta.getRequires().addAnnotation(Uri.TOKEN);
        meta.getProduces().addAnnotation(Uri.POS);
        return meta;

    }
}
