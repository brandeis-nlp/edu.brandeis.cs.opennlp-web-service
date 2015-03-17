package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.tokenize.WhitespaceTokenizer;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

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
	
	POSTagger postagger;
	public TestPOSTagger() throws OpenNLPWebServiceException {
		postagger = new POSTagger();
	}

    @Before
    public void data() throws IOException {
        java.io.InputStream in =  this.getClass().getClassLoader().getResourceAsStream("tokens.json");
        payload = IOUtils.toString(in);
        in =  this.getClass().getClassLoader().getResourceAsStream("MASC3-0202-Tagger-in.json");

    }

    @Test
	public void testTokenize() {
		String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE
				.tokenize("Hi. How are you? This, is Mike.");
		String[] tags = postagger.tag(whitespaceTokenizerLine);
		System.out.println(Arrays.toString(tags));
		String [] goldTags = {"NNP", "WRB", "VBP", "JJ", "DT", "VBZ", "NNP"};
		Assert.assertArrayEquals("Tokenize Failure.", goldTags, tags);
	}

    @Test
    public void testExecute(){

    }

}
