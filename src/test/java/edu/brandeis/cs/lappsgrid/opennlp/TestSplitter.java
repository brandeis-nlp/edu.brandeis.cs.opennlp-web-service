package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.util.Span;
import org.junit.Assert;
import org.junit.Test;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;

import java.util.Arrays;
import java.util.Map;

/**
 * <i>TestSplitter.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p> Test cases are from <a href="http://www.programcreek.com/2012/05/opennlp-tutorial/">OpenNLP Tutorial</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class TestSplitter extends TestService {
	String testSent = "If possible, we would appreciate comments no later than 3:00 PM EST on Sunday, August 26.  Comments can be faxed to my attention at 202/338-2416 or emailed to cfr@vnf.com or gdb@vnf.com (Gary GaryBachman).\n\nThank you.";

	public TestSplitter() throws OpenNLPWebServiceException {
		service = new Splitter();
	}
	
	@Test
	public void testSentDetect() throws OpenNLPWebServiceException {
		String [] sents = new Splitter().sentDetect("Hi. How are you? This is Mike.");
		System.out.println(Arrays.toString(sents));
		String [] goldSents = {"Hi. How are you?","This is Mike."};
		Assert.assertArrayEquals("Splitter Failure.", goldSents, sents);
	}
	
	@Test
	public void testSentDetectPos()  throws OpenNLPWebServiceException {
		Span[] offsets = new Splitter()
				.sentPosDetect("Hi. How are you? This is Mike.");
		System.out.println(Arrays.toString(offsets));
		Assert.assertEquals(
				"Splitter Failure.",
				"[[0..16), [17..30)]",
				Arrays.toString(offsets));
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
        System.out.println("\\-----------------------------------/\n");
    }
}
