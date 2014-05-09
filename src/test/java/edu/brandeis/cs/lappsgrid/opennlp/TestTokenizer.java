package edu.brandeis.cs.lappsgrid.opennlp;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;
import opennlp.tools.util.Span;

import org.anc.lapps.serialization.Container;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lappsgrid.api.Data;
import org.lappsgrid.core.DataFactory;

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
	
	Tokenizer tokenizer;
	
	public TestTokenizer() throws OpenNLPWebServiceException {
		tokenizer = new Tokenizer();
	}


    @Before
    public void data() throws IOException {
        java.io.InputStream in =  this.getClass().getClassLoader().getResourceAsStream("splitter.json");
        payload = IOUtils.toString(in);
        data = DataFactory.json(payload);
        container = new Container(payload);
    }
	
	@Test
	public void testTokenize() {
		String [] tokens = tokenizer.tokenize("Hi. How are you? This is Mike.");
		System.out.println(Arrays.toString(tokens));
		String [] goldTokens = {"Hi",".","How","are","you","?","This","is","Mike","."};
		Assert.assertArrayEquals("Tokenize Failure.", goldTokens, tokens);
	}
	
	@Test
	public void testTokenizePos() {
		Span[] boundaries = tokenizer
				.tokenizePos("Hi. How are you? This is Mike.");
		Assert.assertEquals(
				"Tokenize Failure.",
				"[[0..2), [2..3), [4..7), [8..11), [12..15), [15..16), [17..21), [22..24), [25..29), [29..30)]",
				Arrays.toString(boundaries));
	}

    @Test
    public void testExecute(){
        Data res = tokenizer.execute(data);
        System.out.println(res.getPayload());
    }
}
