package edu.brandeis.cs.lappsgrid.opennlp;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * <i>TestParser.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p> Test cases are from <a href="http://www.programcreek.com/2012/05/opennlp-tutorial/">OpenNLP Tutorial</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class TestParser extends TestService {
	
//	Parser parser;

	String splitterjson = "{\"discriminator\":\"http://vocab.lappsgrid.org/ns/media/jsonld#lif\",\"payload\":{\"@context\":\"http://vocab.lappsgrid.org/context-1.0.0.jsonld\",\"metadata\":{},\"text\":{\"@value\":\"If possible, we would appreciate comments no later than 3:00 PM EST on Sunday, August 26.  Comments can be faxed to my attention at 202/338-2416 or emailed to cfr@vnf.com or gdb@vnf.com (Gary GaryBachman).\\n\\nThank you.\"},\"views\":[{\"metadata\":{\"contains\":{\"http://vocab.lappsgrid.org/Sentence\":{\"producer\":\"edu.brandeis.cs.lappsgrid.opennlp.Splitter:2.0.1-SNAPSHOT\",\"type\":\"splitter:opennlp\"}}},\"annotations\":[{\"id\":\"sent0\",\"@type\":\"http://vocab.lappsgrid.org/Sentence\",\"start\":0,\"end\":89,\"features\":{\"sentence\":\"If possible, we would appreciate comments no later than 3:00 PM EST on Sunday, August 26.\"}},{\"id\":\"sent1\",\"@type\":\"http://vocab.lappsgrid.org/Sentence\",\"start\":91,\"end\":185,\"features\":{\"sentence\":\"Comments can be faxed to my attention at 202/338-2416 or emailed to cfr@vnf.com or gdb@vnf.com\"}},{\"id\":\"sent2\",\"@type\":\"http://vocab.lappsgrid.org/Sentence\",\"start\":186,\"end\":205,\"features\":{\"sentence\":\"(Gary GaryBachman).\"}},{\"id\":\"sent3\",\"@type\":\"http://vocab.lappsgrid.org/Sentence\",\"start\":207,\"end\":217,\"features\":{\"sentence\":\"Thank you.\"}}]}]}}";

    @Before
    public void data() throws IOException {

    }

    public TestParser() throws OpenNLPWebServiceException {
		service = new Parser();
	}
	
	@Test
	public void testParser() throws OpenNLPWebServiceException {
		String print = new Parser().parse("Programcreek is a very huge and useful website.");
		System.out.println(print);

		String goldPrint = "(TOP (S (NP (NN Programcreek)) (VP (VBZ is) (NP (DT a) (ADJP (RB very) (JJ huge) (CC and) (JJ useful)))) (. website.)))\n";
		Assert.assertEquals("Parse Failure.", print, goldPrint);
	}

    @Test
    public void testExecute(){

		String result = service.execute(splitterjson);
		System.out.println("<------------------------------------------------------------------------------");
		System.out.println(String.format("      %s         ", this.getClass().getName()));
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println(result);
		System.out.println("------------------------------------------------------------------------------>");


	}
}
