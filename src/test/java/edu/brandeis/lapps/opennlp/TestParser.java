package edu.brandeis.lapps.opennlp;

import edu.brandeis.lapps.BrandeisServiceException;
import org.junit.Test;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestParser extends TestOpennlpService {

    public TestParser() throws BrandeisServiceException {
        service = new Parser();
        testText = "{\"discriminator\":\"http://vocab.lappsgrid.org/ns/media/jsonld#lif\",\"payload\":{\"@context\":\"http://vocab.lappsgrid.org/context-1.0.0.jsonld\",\"$schema\":\"http://vocab.lappsgrid.org/schema/1.1.0/lif-schema-1.1.0.json\",\"metadata\":{},\"text\":{\"@value\":\"Karen flew to New York. She went to see her cousin. \\n\",\"@language\":\"en\"},\"views\":[{\"id\":\"v1\",\"metadata\":{\"contains\":{\"http://vocab.lappsgrid.org/Sentence\":{\"producer\":\"hand-written-sample\",\"type\":\"sentences\"}}},\"annotations\":[{\"id\":\"s_0\",\"start\":0,\"end\":23,\"@type\":\"http://vocab.lappsgrid.org/Sentence\",\"features\":{\"sentence\":\"Karen flew to New York.\"}},{\"id\":\"s_1\",\"start\":24,\"end\":51,\"@type\":\"http://vocab.lappsgrid.org/Sentence\",\"features\":{\"sentence\":\"She went to see her cousin.\"}}]}]}}";
    }

    @Test
    public void testParser() throws BrandeisServiceException {
        String print = new Parser().parse("Programcreek is a very huge and useful website.");
        System.out.println(print);

        String goldPrint = "(TOP (S (NP (NN Programcreek)) (VP (VBZ is) (NP (DT a) (ADJP (RB very) (JJ huge) (CC and) (JJ useful)))) (. website.)))\n";
        assertEquals("Parse Failure.", print, goldPrint);
    }

    @Test
    public void testExecute(){
        Data executedData = Serializer.parse(service.execute(testText), Data.class);
        Container executionResult = new Container((Map) executedData.getPayload());

        super.testExecuteResult(executionResult, true);

        View view = executionResult.getView(executionResult.getViews().size() - 1);
        List<Annotation> annotations = view.getAnnotations();

        assertEquals("Trees", 2, annotations.stream().filter(
                ann -> ann.getAtType().equals(Uri.PHRASE_STRUCTURE)).count());

    }

    @Test
    public void testMetadata() {

        ServiceMetadata metadata = super.testDefaultMetadata();

        IOSpecification requires = metadata.getRequires();
        assertEquals("One annotation should be required", 1, requires.getAnnotations().size());
        assertTrue("Sentence annotations should be required", requires.getAnnotations().contains(Uri.SENTENCE));

        IOSpecification produces = metadata.getProduces();
        assertEquals("Two annotation should be produced", 2, produces.getAnnotations().size());
        assertTrue("Constituents not produced", produces.getAnnotations().contains(Uri.CONSTITUENT));
        assertTrue("PS not produced", produces.getAnnotations().contains(Uri.PHRASE_STRUCTURE));
    }

}
