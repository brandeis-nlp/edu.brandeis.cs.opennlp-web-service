package edu.brandeis.lapps.opennlp;

import edu.brandeis.lapps.BrandeisServiceException;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NamedEntityRecognizer extends OpenNLPAbstractWebService {

    private static String TOOL_DESCRIPTION = "This service is a wrapper around Apache OpenNLP 1.5.3 providing an English name finder (NER) service." +
            "\nInternally it uses public OpenNLP-1.5 models (available at http://opennlp.sourceforge.net/models-1.5/), in particular, \n" +
            "\"en-ner-person.bin\", \"en-ner-location.bin\", \"en-ner-organization.bin\", \"en-ner-date.bin\" are used. ";
    protected static final Logger logger = LoggerFactory.getLogger(NamedEntityRecognizer.class);

    private List<TokenNameFinder> nameFinders = new LinkedList<> ();

    protected NamedEntityRecognizer() throws BrandeisServiceException {
        loadAnnotators();
    }

    @Override
    synchronized protected void loadAnnotators() throws BrandeisServiceException {
        super.loadNameFinderModels();
        for (TokenNameFinderModel model : nameFinderModels) {
            nameFinders.add(new NameFinderME(model));
        }
    }

    public Span[] find(String[] tokens) {
        if (nameFinders.size() == 0) {
            try {
                loadAnnotators();
            } catch (BrandeisServiceException e) {
                throw new RuntimeException(
                        "Fail to initialize NamedEntityRecognizer", e);
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
    public String execute(Container container) throws BrandeisServiceException {
        logger.info("Executing");
        String txt = container.getText();
        List<View> tokenViews = container.findViewsThatContain(Uri.TOKEN);

        // throw exception here, the outer execute method will wrap it into a LEDS
        if (tokenViews.size() == 0) {
            throw new BrandeisServiceException(unmetRequirements(Uri.TOKEN));
        }
        View tokenView = tokenViews.get(tokenViews.size() - 1);
        List<Annotation> tokenAnns = tokenView.getAnnotations();

        View view = container.newView();
        setUpContainsMetadata(view, PRODUCER_ALIAS);

        int count = 0;
        if (tokenAnns == null || tokenAnns.size() == 0)  {
            // is word.
            if (txt.matches("[a-zA-Z]+")) {
                for (TokenNameFinder nameFinder : nameFinders) {
                    Span [] neSpans = nameFinder.find(new String[]{txt});
                    for (Span span:neSpans){
                        String category = getNEType(span);
                        Annotation annotation =  view.newAnnotation(NE_ID + count++, Uri.NE, 0, txt.length());
                        annotation.addFeature("word", txt);
                        annotation.addFeature("category", category);
                    }
                }
            } else {
                throw new BrandeisServiceException(String.format(
                        "Invalid input: could not find proper words in \"%s\" view.", tokenView));
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
                    String category = getNEType(span);
                    Annotation ann = view.newAnnotation(NE_ID + count++, Uri.NE, start, end);
                    ann.addFeature("word", txt.substring(start.intValue(), end.intValue()));
                    ann.addFeature("category", category);
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

    public ServiceMetadata loadMetadata() {
        ServiceMetadata meta = setDefaultMetadata();
        meta.setDescription(TOOL_DESCRIPTION);
        meta.getRequires().addAnnotation(Uri.TOKEN);
        meta.getProduces().addAnnotation(Uri.NE);
        return meta;
    }
}
