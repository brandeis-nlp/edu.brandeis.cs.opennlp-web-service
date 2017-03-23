package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.tokenize.WhitespaceTokenizer;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lappsgrid.discriminator.Discriminators;
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


    String splitterjson = "{\"discriminator\":\"http://vocab.lappsgrid.org/ns/media/jsonld#lif\",\"payload\":{\"@context\":\"http://vocab.lappsgrid.org/context-1.0.0.jsonld\",\"metadata\":{},\"text\":{\"@value\":\"Hello World.\",\"@language\":\"en\"},\"views\":[{\"metadata\":{\"contains\":{\"http://vocab.lappsgrid.org/Token\":{\"producer\":\"edu.brandeis.cs.lappsgrid.stanford.corenlp.Tokenizer:2.0.1-SNAPSHOT\",\"type\":\"tokenizer:stanford\"}}},\"annotations\":[{\"id\":\"tk_0_0\",\"start\":0,\"end\":5,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"Hello\"}},{\"id\":\"tk_0_1\",\"start\":6,\"end\":11,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\"World\"}},{\"id\":\"tk_0_2\",\"start\":11,\"end\":12,\"@type\":\"http://vocab.lappsgrid.org/Token\",\"features\":{\"word\":\".\"}}]}]}}";

//	POSTagger postagger;
	public TestPOSTagger() throws OpenNLPWebServiceException {
		service = new POSTagger();
	}

    @Before
    public void data() throws IOException {

    }

    @Test
	public void testTokenize() throws OpenNLPWebServiceException {
		String whitespaceTokenizerLine[] = WhitespaceTokenizer.INSTANCE
				.tokenize("Hi. How are you? This, is Mike.");
		String[] tags = new POSTagger().tag(whitespaceTokenizerLine);
		System.out.println(Arrays.toString(tags));
		String [] goldTags = {"NNP", "WRB", "VBP", "JJ", "DT", "VBZ", "NNP"};
		Assert.assertArrayEquals("Tokenize Failure.", goldTags, tags);
	}

    @Test
    public void testExecute(){

        String result = service.execute(splitterjson);
        System.out.println("<------------------------------------------------------------------------------");
        System.out.println(String.format("      %s         ", this.getClass().getName()));
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println(result);
        System.out.println("------------------------------------------------------------------------------>");


        System.out.println("/-----------------------------------\\");

        String json = service.execute("Good");
        System.out.println(json);

        json = service.execute(jsons.get("tokens.json"));
        System.out.println(json);


        System.out.println("\\-----------------------------------/\n");
    }

}
