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
	
	Parser parser;


    @Before
    public void data() throws IOException {
        java.io.InputStream in =  this.getClass().getClassLoader().getResourceAsStream("splitter.json");
        payload = IOUtils.toString(in);

    }

    public TestParser() throws OpenNLPWebServiceException {
		parser = new Parser();
	}
	
	@Test
	public void testParser() {
		String print = parser.parse("Programcreek is a very huge and useful website.");
		System.out.println(print);
		String goldPrint = "(TOP (S (NP (NN Programcreek)) (VP (VBZ is) (NP (DT a) (ADJP (RB very) (JJ huge) (CC and) (JJ useful)))) (. website.)))\n";
		Assert.assertEquals("Parse Failure.", print, goldPrint);
	}

    @Test
    public void testExecute(){

    }
}
