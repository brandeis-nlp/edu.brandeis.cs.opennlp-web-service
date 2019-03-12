package edu.brandeis.lapps.opennlp;

import edu.brandeis.lapps.BrandeisServiceException;
import opennlp.tools.tokenize.WhitespaceTokenizer;
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

public class TestPOSTagger extends TestOpennlpService {


    public TestPOSTagger() throws BrandeisServiceException {
        service = new POSTagger();
        testText = "{\"discriminator\":\"http://vocab.lappsgrid.org/ns/media/jsonld#lif\",\"payload\":{\"@context\":\"http://vocab.lappsgrid.org/context-1.0.0.jsonld\",\"$schema\":\"http://vocab.lappsgrid.org/schema/1.1.0/lif-schema-1.1.0.json\",\"metadata\":{},\"text\":{\"@value\":\"Karen flew to New York. She went to see her cousin. \\n\",\"@language\":\"en\"},\"views\":[{\"id\":\"v1\",\"metadata\":{\"contains\":{\"http://vocab.lappsgrid.org/Token\":{\"producer\":\"hand-written-sample\",\"type\":\"tokens-pos\",\"posTagSet\":\"penntb\"}}},\"annotations\":[{\"id\":\"tk_0_0\",\"start\":0,\"end\":5,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"Karen\",\"pos\":\"NNP\"}},{\"id\":\"tk_0_1\",\"start\":6,\"end\":10,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"flew\",\"pos\":\"VBD\"}},{\"id\":\"tk_0_2\",\"start\":11,\"end\":13,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"to\",\"pos\":\"TO\"}},{\"id\":\"tk_0_3\",\"start\":14,\"end\":17,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"New\",\"pos\":\"NNP\"}},{\"id\":\"tk_0_4\",\"start\":18,\"end\":22,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"York\",\"pos\":\"NNP\"}},{\"id\":\"tk_0_5\",\"start\":22,\"end\":23,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\".\",\"pos\":\".\"}},{\"id\":\"tk_1_0\",\"start\":24,\"end\":27,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"She\",\"pos\":\"PRP\"}},{\"id\":\"tk_1_1\",\"start\":28,\"end\":32,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"went\",\"pos\":\"VBD\"}},{\"id\":\"tk_1_2\",\"start\":33,\"end\":35,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"to\",\"pos\":\"TO\"}},{\"id\":\"tk_1_3\",\"start\":36,\"end\":39,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"see\",\"pos\":\"VB\"}},{\"id\":\"tk_1_4\",\"start\":40,\"end\":43,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"her\",\"pos\":\"PRP$\"}},{\"id\":\"tk_1_5\",\"start\":44,\"end\":50,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"cousin\",\"pos\":\"NN\"}},{\"id\":\"tk_1_6\",\"start\":50,\"end\":51,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\".\",\"pos\":\".\"}}]}]}}";
    }

    @Test
    public void testTokenize() throws BrandeisServiceException {
        String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE
                .tokenize("Hi. How are you? This, is Mike.");
        String[] tags = new POSTagger().tag(whitespaceTokenizerLine);
        System.out.println(Arrays.toString(tags));
        String [] goldTags = {"NNP", "WRB", "VBP", "JJ", "DT", "VBZ", "NNP"};
        Assert.assertArrayEquals("Tokenize Failure.", goldTags, tags);
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
        assertEquals("@type is not correct", Uri.POS, annotation.getAtType());
        String goodPos = annotation.getFeature("pos");
        assertEquals("Correct tag for 'Karen' is NNP", goodPos, "NNP");

    }

    @Test
    public void testMetadata() {
        ServiceMetadata metadata = super.testDefaultMetadata();

        IOSpecification requires = metadata.getRequires();
        assertEquals("One annotation should be required", 1, requires.getAnnotations().size());
        assertTrue("Token annotations should be required", requires.getAnnotations().contains(Uri.TOKEN));

        IOSpecification produces = metadata.getProduces();
        assertEquals("One annotation should be produced", 1, produces.getAnnotations().size());
        assertTrue("POS not produced", produces.getAnnotations().contains(Uri.POS));
    }

}
