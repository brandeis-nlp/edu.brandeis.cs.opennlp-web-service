package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.tokenize.WhitespaceTokenizer;
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

        System.out.println("/-----------------------------------\\");

        String json = postagger.execute("Good");
        System.out.println(json);
        Container container = new Container((Map) Serializer.parse(json, Data.class).getPayload());


        json = postagger.execute("Good Morning");
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = postagger.execute(jsons.get("payload1.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = postagger.execute(jsons.get("payload2.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = postagger.execute(jsons.get("payload3.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());

        json = postagger.execute(jsons.get("tokens.json"));
        System.out.println(json);
        container = new Container((Map) Serializer.parse(json, Data.class).getPayload());


        System.out.println("\\-----------------------------------/\n");
    }

}
