package edu.brandeis.lapps.opennlp;

import edu.brandeis.lapps.BrandeisServiceException;
import opennlp.tools.util.Span;
import org.junit.Assert;
import org.junit.Test;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestTokenizer extends TestOpennlpService {

    public TestTokenizer() throws BrandeisServiceException {
        service = new Tokenizer();
        testText = "{\"discriminator\":\"http://vocab.lappsgrid.org/ns/media/jsonld#lif\",\"payload\":{\"@context\":\"http://vocab.lappsgrid.org/context-1.0.0.jsonld\",\"$schema\":\"http://vocab.lappsgrid.org/schema/1.1.0/lif-schema-1.1.0.json\",\"metadata\":{},\"text\":{\"@value\":\"Karen flew to New York. She went to see her cousin. \\n\",\"@language\":\"en\"},\"views\":[]}}";

    }

    @Test
    public void testTokenize() throws BrandeisServiceException {
        String [] tokens = new Tokenizer().tokenize("Hi. How are you? This is Mike.");
        System.out.println(Arrays.toString(tokens));
        String [] goldTokens = {"Hi",".","How","are","you","?","This","is","Mike","."};
        Assert.assertArrayEquals("Tokenize Failure.", goldTokens, tokens);
    }

    @Test
    public void testTokenizePos() throws BrandeisServiceException {
        Span[] boundaries = new Tokenizer()
                .tokenizePos("Hi. How are you? This is Mike.");
        assertEquals(
                "Tokenize Failure.",
                "[[0..2), [2..3), [4..7), [8..11), [12..15), [15..16), [17..21), [22..24), [25..29), [29..30)]",
                Arrays.toString(boundaries));
    }

    @Test
    public void testExecute(){

        Data executedData = Serializer.parse(service.execute(testText), Data.class);
        Container executionResult = new Container((Map) executedData.getPayload());

        super.testExecuteResult(executionResult, true);

        View view = executionResult.getView(executionResult.getViews().size() - 1);
        List<Annotation> annotations = view.getAnnotations();

        assertEquals("Tokens", 13, annotations.size());
        Annotation annotation = annotations.get(0);
        assertEquals("@type is not correct: " + annotation.getAtType(), Uri.TOKEN, annotation.getAtType());

    }

    @Test
    public void testMetadata() {

        ServiceMetadata metadata = super.testDefaultMetadata();

        IOSpecification requires = metadata.getRequires();
        assertTrue("No annotations should be required", requires.getAnnotations().isEmpty());

        IOSpecification produces = metadata.getProduces();
        assertEquals("One annotation should be produced", 1, produces.getAnnotations().size());
        assertTrue("Tokens not produced", produces.getAnnotations().contains(Uri.TOKEN));

    }

}
