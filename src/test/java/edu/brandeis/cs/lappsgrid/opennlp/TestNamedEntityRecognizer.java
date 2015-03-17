package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

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

	NamedEntityRecognizer ner;

	public TestNamedEntityRecognizer() throws OpenNLPWebServiceException {
		ner = new NamedEntityRecognizer();
	}


    @Before
    public void data() throws IOException {
        java.io.InputStream in =  this.getClass().getClassLoader().getResourceAsStream("tokens.json");
        payload = IOUtils.toString(in);

    }
	@Test
	public void testFind() {
		String tokens[] = SimpleTokenizer.INSTANCE
				.tokenize("Mike, Smith is a good person and he is from Boston.");
		System.out.println(Arrays.toString(tokens));
		Span[] spans = ner.find(tokens);
		System.out.println(Arrays.toString(spans));
		Span[] goldSpans = { new Span(0, 1, "person"), new Span(2, 3, "person"), new Span(11,12,"location") };
		Assert.assertArrayEquals("NamedEntityRecognizer Failure.", spans,
				goldSpans);
	}

    @Test
    public void testExecute(){

    }
}
