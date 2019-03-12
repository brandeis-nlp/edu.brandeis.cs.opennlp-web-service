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

public class TestSplitter extends TestOpennlpService {

    public TestSplitter() throws BrandeisServiceException {
        service = new Splitter();
        testText = "{\"discriminator\":\"http://vocab.lappsgrid.org/ns/media/jsonld#lif\",\"payload\":{\"@context\":\"http://vocab.lappsgrid.org/context-1.0.0.jsonld\",\"$schema\":\"http://vocab.lappsgrid.org/schema/1.1.0/lif-schema-1.1.0.json\",\"metadata\":{},\"text\":{\"@value\":\"Karen flew to New York. She went to see her cousin. \\n\",\"@language\":\"en\"},\"views\":[]}}";
    }

    @Test
    public void testSentDetect() throws BrandeisServiceException {
        String [] sents = new Splitter().sentDetect("Hi. How are you? This is Mike.");
        System.out.println(Arrays.toString(sents));
        String [] goldSents = {"Hi. How are you?","This is Mike."};
        Assert.assertArrayEquals("Splitter Failure.", goldSents, sents);
    }

    @Test
    public void testSentDetectPos() throws BrandeisServiceException {
        Span[] offsets = new Splitter()
                .sentPosDetect("Hi. How are you? This is Mike.");
        System.out.println(Arrays.toString(offsets));
        assertEquals(
                "Splitter Failure.",
                "[[0..16), [17..30)]",
                Arrays.toString(offsets));
    }

    @Test
    public void testExecute(){

        Data executedData = Serializer.parse(service.execute(testText), Data.class);
        Container executionResult = new Container((Map) executedData.getPayload());

        super.testExecuteResult(executionResult, true);

        View view = executionResult.getView(executionResult.getViews().size() - 1);
        List<Annotation> annotations = view.getAnnotations();

        assertEquals("Sentences", 2, annotations.size());
        Annotation annotation = annotations.get(0);
        assertEquals("@type is not correct: " + annotation.getAtType(), Uri.SENTENCE, annotation.getAtType());

    }

    @Test
    public void testMetadata() {
        ServiceMetadata metadata = super.testDefaultMetadata();

        IOSpecification requires = metadata.getRequires();
        assertTrue("No annotations should be required", requires.getAnnotations().isEmpty());

        IOSpecification produces = metadata.getProduces();
        assertEquals("One annotation should be produced", 1, produces.getAnnotations().size());
        assertTrue("Sentence not produced", produces.getAnnotations().contains(Uri.SENTENCE));
    }

}
