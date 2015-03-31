package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.util.Span;
import org.junit.Assert;
import org.junit.Test;
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
	
	Splitter splitter;
	
	public TestSplitter() throws OpenNLPWebServiceException {
		splitter = new Splitter();
	}
	
	@Test
	public void testSentDetect() {
		String [] sents = splitter.sentDetect("Hi. How are you? This is Mike.");
		System.out.println(Arrays.toString(sents));
		String [] goldSents = {"Hi. How are you?","This is Mike."};
		Assert.assertArrayEquals("Splitter Failure.", goldSents, sents);
	}
	
	@Test
	public void testSentDetectPos() {
		Span[] offsets = splitter
				.sentPosDetect("Hi. How are you? This is Mike.");
		System.out.println(Arrays.toString(offsets));
		Assert.assertEquals(
				"Splitter Failure.",
				"[[0..16), [17..30)]",
				Arrays.toString(offsets));
	}


    @Test
    public void testExecute(){
        System.out.println("/-----------------------------------\\");
        String json = splitter.execute(jsons.get("payload1.json"));
        System.out.println(json);
        Container container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = splitter.execute(jsons.get("payload2.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = splitter.execute(jsons.get("payload3.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());
        System.out.println("\\-----------------------------------/\n");
    }
}
