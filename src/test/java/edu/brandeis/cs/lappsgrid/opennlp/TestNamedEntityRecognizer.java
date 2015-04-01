package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;

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

	NamedEntityRecognizer ner;

	public TestNamedEntityRecognizer() throws OpenNLPWebServiceException {
		ner = new NamedEntityRecognizer();
	}


    @Before
    public void data() throws IOException {

    }
	@Test
	public void testFind() {
		String tokens[] = SimpleTokenizer.INSTANCE
				.tokenize("Mike, Smith is a good person and he is from Boston.");
		System.out.println(Arrays.toString(tokens));
		Span[] spans = ner.find(tokens);
		System.out.println(Arrays.toString(spans));
		Span[] goldSpans = { new Span(11,12,"location") , new Span(0, 1, "person"), new Span(2, 3, "person")};
		Assert.assertArrayEquals("NamedEntityRecognizer Failure.", spans,
				goldSpans);
	}

    @Test
    public void testExecute(){

        System.out.println("/-----------------------------------\\");

        String json = ner.execute("Mike");
        System.out.println(json);
        Container container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = ner.execute(jsons.get("payload1.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = ner.execute(jsons.get("payload2.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = ner.execute(jsons.get("payload3.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = ner.execute(jsons.get("tokens.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());


        System.out.println("\\-----------------------------------/\n");
    }
}
