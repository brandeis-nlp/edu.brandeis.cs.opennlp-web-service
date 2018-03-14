package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;
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
 * <i>TestTokenizer.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p>
 * <p>
 * Test cases are from <a
 * href="http://www.programcreek.com/2012/05/opennlp-tutorial/">OpenNLP
 * Tutorial</a>
 * <p>
 * 
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>
 *         Nov 20, 2013<br>
 * 
 */
public class TestNamedEntityRecognizer extends TestService {

//	NamedEntityRecognizer ner;


	String taggerjson = "{\"discriminator\":\"http://vocab.lappsgrid.org/ns/media/jsonld#lif\",\"payload\":{\"@context\":\"http://vocab.lappsgrid.org/context-1.0.0.jsonld\",\"metadata\":{},\"text\":{\"@value\":\"If possible, we would appreciate comments no later than 3:00 PM EST on Sunday, August 26.  Comments can be faxed to my attention at 202/338-2416 or emailed to cfr@vnf.com or gdb@vnf.com (Gary GaryBachman).\\n\\nThank you.\"},\"views\":[{\"metadata\":{\"contains\":{\"http://vocab.lappsgrid.org/Token\":{\"producer\":\"edu.brandeis.cs.lappsgrid.opennlp.Tokenizer:2.0.1-SNAPSHOT\",\"type\":\"tokenizer:opennlp\"}}},\"annotations\":[{\"id\":\"tok0\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":0,\"end\":2,\"features\":{\"word\":\"If\"}},{\"id\":\"tok1\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":3,\"end\":11,\"features\":{\"word\":\"possible\"}},{\"id\":\"tok2\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":11,\"end\":12,\"features\":{\"word\":\",\"}},{\"id\":\"tok3\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":13,\"end\":15,\"features\":{\"word\":\"we\"}},{\"id\":\"tok4\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":16,\"end\":21,\"features\":{\"word\":\"would\"}},{\"id\":\"tok5\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":22,\"end\":32,\"features\":{\"word\":\"appreciate\"}},{\"id\":\"tok6\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":33,\"end\":41,\"features\":{\"word\":\"comments\"}},{\"id\":\"tok7\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":42,\"end\":44,\"features\":{\"word\":\"no\"}},{\"id\":\"tok8\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":45,\"end\":50,\"features\":{\"word\":\"later\"}},{\"id\":\"tok9\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":51,\"end\":55,\"features\":{\"word\":\"than\"}},{\"id\":\"tok10\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":56,\"end\":60,\"features\":{\"word\":\"3:00\"}},{\"id\":\"tok11\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":61,\"end\":63,\"features\":{\"word\":\"PM\"}},{\"id\":\"tok12\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":64,\"end\":67,\"features\":{\"word\":\"EST\"}},{\"id\":\"tok13\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":68,\"end\":70,\"features\":{\"word\":\"on\"}},{\"id\":\"tok14\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":71,\"end\":77,\"features\":{\"word\":\"Sunday\"}},{\"id\":\"tok15\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":77,\"end\":78,\"features\":{\"word\":\",\"}},{\"id\":\"tok16\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":79,\"end\":85,\"features\":{\"word\":\"August\"}},{\"id\":\"tok17\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":86,\"end\":88,\"features\":{\"word\":\"26\"}},{\"id\":\"tok18\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":88,\"end\":89,\"features\":{\"word\":\".\"}},{\"id\":\"tok19\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":91,\"end\":99,\"features\":{\"word\":\"Comments\"}},{\"id\":\"tok20\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":100,\"end\":103,\"features\":{\"word\":\"can\"}},{\"id\":\"tok21\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":104,\"end\":106,\"features\":{\"word\":\"be\"}},{\"id\":\"tok22\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":107,\"end\":112,\"features\":{\"word\":\"faxed\"}},{\"id\":\"tok23\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":113,\"end\":115,\"features\":{\"word\":\"to\"}},{\"id\":\"tok24\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":116,\"end\":118,\"features\":{\"word\":\"my\"}},{\"id\":\"tok25\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":119,\"end\":128,\"features\":{\"word\":\"attention\"}},{\"id\":\"tok26\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":129,\"end\":131,\"features\":{\"word\":\"at\"}},{\"id\":\"tok27\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":132,\"end\":144,\"features\":{\"word\":\"202/338-2416\"}},{\"id\":\"tok28\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":145,\"end\":147,\"features\":{\"word\":\"or\"}},{\"id\":\"tok29\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":148,\"end\":155,\"features\":{\"word\":\"emailed\"}},{\"id\":\"tok30\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":156,\"end\":158,\"features\":{\"word\":\"to\"}},{\"id\":\"tok31\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":159,\"end\":170,\"features\":{\"word\":\"cfr@vnf.com\"}},{\"id\":\"tok32\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":171,\"end\":173,\"features\":{\"word\":\"or\"}},{\"id\":\"tok33\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":174,\"end\":185,\"features\":{\"word\":\"gdb@vnf.com\"}},{\"id\":\"tok34\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":186,\"end\":187,\"features\":{\"word\":\"(\"}},{\"id\":\"tok35\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":187,\"end\":191,\"features\":{\"word\":\"Gary\"}},{\"id\":\"tok36\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":192,\"end\":203,\"features\":{\"word\":\"GaryBachman\"}},{\"id\":\"tok37\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":203,\"end\":204,\"features\":{\"word\":\")\"}},{\"id\":\"tok38\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":204,\"end\":205,\"features\":{\"word\":\".\"}},{\"id\":\"tok39\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":207,\"end\":212,\"features\":{\"word\":\"Thank\"}},{\"id\":\"tok40\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":213,\"end\":216,\"features\":{\"word\":\"you\"}},{\"id\":\"tok41\",\"@type\":\"http://vocab.lappsgrid.org/Token\",\"start\":216,\"end\":217,\"features\":{\"word\":\".\"}}]}]}}";
	public TestNamedEntityRecognizer() throws OpenNLPWebServiceException {
		service = new NamedEntityRecognizer();
	}


    @Before
    public void data() throws IOException {

    }
	@Test
	public void testFind() throws OpenNLPWebServiceException {
		String tokens[] = SimpleTokenizer.INSTANCE
				.tokenize("Mike, Smith is a good person and he is from Boston.");
		System.out.println(Arrays.toString(tokens));
		Span[] spans = new NamedEntityRecognizer().find(tokens);
		System.out.println(Arrays.toString(spans));
		Span[] goldSpans = {  new Span(0, 1, "person"), new Span(2, 3, "person"), new Span(11,12,"location")};
		Assert.assertArrayEquals("NamedEntityRecognizer Failure.", goldSpans,
				spans);
	}

    @Test
    public void testExecute(){


		String result = service.execute(taggerjson);
		System.out.println("<------------------------------------------------------------------------------");
		System.out.println(String.format("      %s         ", this.getClass().getName()));
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println(result);
		System.out.println("------------------------------------------------------------------------------>");




        System.out.println("/-----------------------------------\\");

        String json = service.execute("Mike");
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
		Assert.assertTrue("LIF format not accepted.", requires.getFormat().contains(Uri.LAPPS));
		Assert.assertEquals("One annotation should be required", 1, requires.getAnnotations().size());
		Assert.assertEquals("Tokens should be required", Uri.TOKEN, requires.getAnnotations().get(0));
		
		IOSpecification produces = metadata.getProduces();
		Assert.assertEquals("Produces encoding is not correct", "UTF-8", produces.getEncoding());
		Assert.assertTrue("English not produced", produces.getLanguage().contains("en"));
		Assert.assertEquals("Only one format should be produced", 1, produces.getFormat().size());
		Assert.assertTrue("LIF format not produced.", produces.getFormat().contains(Uri.LAPPS));
		Assert.assertEquals("Only one annotation should be produced", 1, produces.getAnnotations().size());
		Assert.assertEquals("Tokens not produced", Uri.NE, produces.getAnnotations().get(0));
    }
}
