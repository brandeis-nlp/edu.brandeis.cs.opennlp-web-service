package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.util.Span;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.discriminator.Discriminators.Uri;
import edu.brandeis.cs.lappsgrid.Version;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * <i>TestTokenizer.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p> Test cases are from <a href="http://www.programcreek.com/2012/05/opennlp-tutorial/">OpenNLP Tutorial</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class TestTokenizer extends TestService {

    String testSent = "If possible, we would appreciate comments no later than 3:00 PM EST on Sunday, August 26.  Comments can be faxed to my attention at 202/338-2416 or emailed to cfr@vnf.com or gdb@vnf.com (Gary GaryBachman).\n\nThank you.";

//    Tokenizer tokenizer;
	
	public TestTokenizer() throws OpenNLPWebServiceException {
		service = new Tokenizer();
	}


    @Before
    public void data() throws IOException {

    }
	
	@Test
	public void testTokenize() throws OpenNLPWebServiceException{
		String [] tokens = new Tokenizer().tokenize("Hi. How are you? This is Mike.");
		System.out.println(Arrays.toString(tokens));
		String [] goldTokens = {"Hi",".","How","are","you","?","This","is","Mike","."};
		Assert.assertArrayEquals("Tokenize Failure.", goldTokens, tokens);
	}
	
	@Test
	public void testTokenizePos() throws OpenNLPWebServiceException {
		Span[] boundaries = new Tokenizer()
				.tokenizePos("Hi. How are you? This is Mike.");
		Assert.assertEquals(
				"Tokenize Failure.",
				"[[0..2), [2..3), [4..7), [8..11), [12..15), [15..16), [17..21), [22..24), [25..29), [29..30)]",
				Arrays.toString(boundaries));
	}

    @Test
    public void testExecute(){



        String input = new Data<>(Discriminators.Uri.LIF, wrapContainer(testSent)).asJson();
        String result = service.execute(input);


        System.out.println("<------------------------------------------------------------------------------");
        System.out.println(String.format("      %s         ", this.getClass().getName()));
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println(result);
        System.out.println("------------------------------------------------------------------------------>");



        System.out.println("/-----------------------------------\\");
        String json = service.execute(jsons.get("payload1.json"));
        System.out.println(json);


//        json = service.execute(jsons.get("payload2.json"));
        System.out.println(json);

//        json = service.execute(jsons.get("payload3.json"));
        System.out.println(json);

//        json = service.execute(jsons.get("splitter.json"));
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
		Assert.assertEquals("Version is not correct", Version.getVersion(), metadata.getVersion());
		Assert.assertEquals("License is not correct", Uri.APACHE2, metadata.getLicense());
		
		IOSpecification requires = metadata.getRequires();
		Assert.assertEquals("Requires encoding is not correct", "UTF-8", requires.getEncoding());
		Assert.assertTrue("English not accepted", requires.getLanguage().contains("en"));
		Assert.assertEquals("One format should be required", 1, requires.getFormat().size());
		Assert.assertTrue("LIF format not accepted", requires.getFormat().contains(Uri.LAPPS));
		
		IOSpecification produces = metadata.getProduces();
		Assert.assertEquals("Produces encoding is not correct", "UTF-8", produces.getEncoding());
		Assert.assertTrue("English not produced", produces.getLanguage().contains("en"));
		Assert.assertEquals("One format should be produced", 1, produces.getFormat().size());
		Assert.assertTrue("LIF format not produced.", produces.getFormat().contains(Uri.LAPPS));
		Assert.assertEquals("Two annotations should be produced", 1, produces.getAnnotations().size());
		Assert.assertTrue("Constituent not produced", produces.getAnnotations().contains(Uri.TOKEN));
    }
    
}
