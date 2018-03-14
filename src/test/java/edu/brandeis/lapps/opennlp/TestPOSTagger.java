package edu.brandeis.lapps.opennlp;

import opennlp.tools.tokenize.WhitespaceTokenizer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * <i>TestPOSTagger.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p> Test cases are from <a href="http://www.programcreek.com/2012/05/opennlp-tutorial/">OpenNLP Tutorial</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class TestPOSTagger extends TestService {


    String splitterjson = "{\"discriminator\":\"http://vocab.lappsgrid.org/ns/media/jsonld#lif\",\"payload\":{\"@context\":\"http://vocab.lappsgrid.org/context-1.0.0.jsonld\",\"metadata\":{},\"text\":{\"@value\":\"Hello World.\",\"@language\":\"en\"},\"views\":[{\"metadata\":{\"contains\":{\"http://vocab.lappsgrid.org/Token\":{\"producer\":\"edu.brandeis.cs.lappsgrid.stanford.corenlp.Tokenizer:2.0.1-SNAPSHOT\",\"type\":\"tokenizer:stanford\"}}},\"annotations\":[{\"id\":\"tk_0_0\",\"start\":0,\"end\":5,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"Hello\"}},{\"id\":\"tk_0_1\",\"start\":6,\"end\":11,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"World\"}},{\"id\":\"tk_0_2\",\"start\":11,\"end\":12,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\".\"}}]}]}}";

//	POSTagger postagger;
	public TestPOSTagger() throws OpenNLPWebServiceException {
		service = new POSTagger();
	}

    @Before
    public void data() throws IOException {

    }

    @Test
	public void testTokenize() throws OpenNLPWebServiceException {
		String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE
				.tokenize("Hi. How are you? This, is Mike.");
		String[] tags = new POSTagger().tag(whitespaceTokenizerLine);
		System.out.println(Arrays.toString(tags));
		String [] goldTags = {"NNP", "WRB", "VBP", "JJ", "DT", "VBZ", "NNP"};
		Assert.assertArrayEquals("Tokenize Failure.", goldTags, tags);
	}

    @Test
    public void testExecute(){

        String result = service.execute(splitterjson);
        System.out.println("<------------------------------------------------------------------------------");
        System.out.println(String.format("      %s         ", this.getClass().getName()));
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println(result);
        System.out.println("------------------------------------------------------------------------------>");


        System.out.println("/-----------------------------------\\");

        String json = service.execute("Good");
        System.out.println(json);

        json = service.execute(jsons.get("tokens.json"));
        System.out.println(json);


        System.out.println("\\-----------------------------------/\n");
    }
    
    @Test
    public void testMetadata() {
    	String json = service.getMetadata();
    	Assert.assertNotNull("service.getMetadata() returned null", json);

		Data data = Serializer.parse(json, Data.class);
		Assert.assertNotNull("Unable to parse metadata json.", data);
		Assert.assertNotSame(data.getPayload().toString(), Uri.ERROR, data.getDiscriminator());
		
		ServiceMetadata metadata = new ServiceMetadata((Map) data.getPayload());
		Assert.assertEquals("Vendor is not correct", "http://www.cs.brandeis.edu/", metadata.getVendor());
		Assert.assertEquals("Name is not correct", service.getClass().getName(), metadata.getName());
		Assert.assertEquals("Version is not correct", service.getVersion(), metadata.getVersion());
		Assert.assertEquals("License is not correct", Uri.APACHE2, metadata.getLicense());
		
		IOSpecification requires = metadata.getRequires();
		Assert.assertEquals("Requires encoding is not correct", "UTF-8", requires.getEncoding());
		Assert.assertTrue("English not accepted", requires.getLanguage().contains("en"));
		Assert.assertEquals("One format should be required", 1, requires.getFormat().size());
		Assert.assertTrue("LIF format not accepted", requires.getFormat().contains(Uri.LAPPS));
		Assert.assertEquals("One annotation should be required", 1, requires.getAnnotations().size());
		Assert.assertTrue("Tokens annotation required", requires.getAnnotations().contains(Uri.TOKEN));
		
		IOSpecification produces = metadata.getProduces();
		Assert.assertEquals("Produces encoding is not correct", "UTF-8", produces.getEncoding());
		Assert.assertTrue("English not produced", produces.getLanguage().contains("en"));
		Assert.assertEquals("One format should be produced", 1, produces.getFormat().size());
		Assert.assertTrue("LIF format not produced.", produces.getFormat().contains(Uri.LAPPS));
		Assert.assertEquals("One annotation should be produced", 1, produces.getAnnotations().size());
		Assert.assertEquals("POS not produced", Uri.POS, produces.getAnnotations().get(0));
    }

}
