package edu.brandeis.lapps.opennlp;

import edu.brandeis.lapps.BrandeisServiceException;
import opennlp.tools.tokenize.SimpleTokenizer;
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
import org.lappsgrid.vocabulary.Features;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestNamedEntityRecognizer extends TestOpennlpService {

    public TestNamedEntityRecognizer() throws BrandeisServiceException {
        service = new NamedEntityRecognizer();
        testText = "{\"discriminator\":\"http://vocab.lappsgrid.org/ns/media/jsonld#lif\",\"payload\":{\"@context\":\"http://vocab.lappsgrid.org/context-1.0.0.jsonld\",\"$schema\":\"http://vocab.lappsgrid.org/schema/1.1.0/lif-schema-1.1.0.json\",\"metadata\":{},\"text\":{\"@value\":\"Karen flew to New York. She went to see her cousin. \\n\",\"@language\":\"en\"},\"views\":[{\"id\":\"v1\",\"metadata\":{\"contains\":{\"http://vocab.lappsgrid.org/Token\":{\"producer\":\"hand-written-sample\",\"type\":\"tokens-pos\",\"posTagSet\":\"penntb\"}}},\"annotations\":[{\"id\":\"tk_0_0\",\"start\":0,\"end\":5,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"Karen\",\"pos\":\"NNP\"}},{\"id\":\"tk_0_1\",\"start\":6,\"end\":10,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"flew\",\"pos\":\"VBD\"}},{\"id\":\"tk_0_2\",\"start\":11,\"end\":13,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"to\",\"pos\":\"TO\"}},{\"id\":\"tk_0_3\",\"start\":14,\"end\":17,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"New\",\"pos\":\"NNP\"}},{\"id\":\"tk_0_4\",\"start\":18,\"end\":22,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"York\",\"pos\":\"NNP\"}},{\"id\":\"tk_0_5\",\"start\":22,\"end\":23,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\".\",\"pos\":\".\"}},{\"id\":\"tk_1_0\",\"start\":24,\"end\":27,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"She\",\"pos\":\"PRP\"}},{\"id\":\"tk_1_1\",\"start\":28,\"end\":32,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"went\",\"pos\":\"VBD\"}},{\"id\":\"tk_1_2\",\"start\":33,\"end\":35,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"to\",\"pos\":\"TO\"}},{\"id\":\"tk_1_3\",\"start\":36,\"end\":39,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"see\",\"pos\":\"VB\"}},{\"id\":\"tk_1_4\",\"start\":40,\"end\":43,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"her\",\"pos\":\"PRP$\"}},{\"id\":\"tk_1_5\",\"start\":44,\"end\":50,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"cousin\",\"pos\":\"NN\"}},{\"id\":\"tk_1_6\",\"start\":50,\"end\":51,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\".\",\"pos\":\".\"}}]}]}}";
    }

    @Test
    public void testFind() throws BrandeisServiceException {
        String tokens[] = SimpleTokenizer.INSTANCE.tokenize("Mike, Smith is a good person and he is from Boston.");
        System.out.println(Arrays.toString(tokens));
        Span[] spans = new NamedEntityRecognizer().find(tokens);
        System.out.println(Arrays.toString(spans));
        Span[] goldSpans = {  new Span(0, 1, "person"), new Span(2, 3, "person"), new Span(11,12,"location")};
        Assert.assertArrayEquals("NamedEntityRecognizer Failure.", goldSpans,
                spans);
    }

    @Test
    public void testExecute(){
        Data executedData = Serializer.parse(service.execute(testText), Data.class);
        Container executionResult = new Container((Map) executedData.getPayload());

        super.testExecuteResult(executionResult, true);

        View view = executionResult.getView(executionResult.getViews().size() - 1);
        List<Annotation> annotations = view.getAnnotations();

        assertEquals("NE", 2, annotations.size());
        // Karen
        Annotation annotation = annotations.get(0);
        assertEquals("@type is not correct", Uri.NE, annotation.getAtType());
        assertEquals("ne-category is not correct", Uri.PERSON, annotation.getFeature(Features.NamedEntity.CATEGORY));
        // New York
        annotation = annotations.get(1);
        assertEquals("@type is not correct", Uri.NE, annotation.getAtType());
        assertEquals("ne-category is not correct", Uri.LOCATION, annotation.getFeature(Features.NamedEntity.CATEGORY));

    }

    @Test
    public void testMetadata() {

        ServiceMetadata metadata = super.testDefaultMetadata();

        IOSpecification requires = metadata.getRequires();
        assertEquals("One annotation should be required", 1, requires.getAnnotations().size());
        assertTrue("Token annotations should be required", requires.getAnnotations().contains(Uri.TOKEN));

        IOSpecification produces = metadata.getProduces();
        assertEquals("One annotation should be produced", 1, produces.getAnnotations().size());
        assertTrue("NE not produced", produces.getAnnotations().contains(Uri.NE));
    }
}
