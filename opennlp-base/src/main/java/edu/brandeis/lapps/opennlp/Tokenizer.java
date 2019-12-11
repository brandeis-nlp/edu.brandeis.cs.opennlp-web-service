package edu.brandeis.lapps.opennlp;

import edu.brandeis.lapps.BrandeisServiceException;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.util.Span;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

public class Tokenizer extends AbstractOpennlpWrapper {
    private static String TOOL_DESCRIPTION = "This service is a wrapper around Apache OpenNLP 1.5.3 providing an English tokenizer service." +
            "\nInternally it uses public OpenNLP-1.5 models (available at http://opennlp.sourceforge.net/models-1.5/), in particular, \n" +
            "\"/en-token.bin\" is used. ";
    private opennlp.tools.tokenize.Tokenizer tokenizer;

    public Tokenizer() throws BrandeisServiceException {
        loadAnnotators();
    }

    @Override
    protected void loadAnnotators() throws BrandeisServiceException {
        super.loadTokenizerModel();
        tokenizer = new TokenizerME(tokenizerModel);
    }

    public String[] tokenize(String s) {
        String tokens[] = tokenizer.tokenize(s);
        return tokens;
    }

    public Span[] tokenizePos(String s) {
        Span[] boundaries = tokenizer.tokenizePos(s);
        return boundaries;
    }

    @Override
    public String processPayload(Container container) {
        logger.info("Executing");
        String txt = container.getText();

        View view = container.newView();
        setUpContainsMetadata(view, PRODUCER_ALIAS);
        Span[] spans = tokenizePos(txt);
        int count = 0;
        for (Span span : spans) {
            int start = span.getStart();
            int end = span.getEnd();
            Annotation ann = view.newAnnotation(TOKEN_ID + count++,
                    Uri.TOKEN, start, end);
            ann.addFeature(Features.Token.WORD, txt.substring(start, end));
        }
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    public ServiceMetadata loadMetadata() {
        ServiceMetadata meta = setDefaultMetadata();
        meta.setDescription(TOOL_DESCRIPTION);
        meta.getProduces().addAnnotation(Uri.TOKEN);
        return meta;
    }
}
