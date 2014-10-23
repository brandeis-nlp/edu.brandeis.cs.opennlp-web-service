package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;
import org.anc.lapps.serialization.Container;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lappsgrid.api.Data;
import org.lappsgrid.core.DataFactory;

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
public class TestCoreference extends TestService {

    Coreference cor;

	public TestCoreference() throws OpenNLPWebServiceException {
		cor = new Coreference();
	}


    @Before
    public void data() throws IOException {
        java.io.InputStream in =  this.getClass().getClassLoader().getResourceAsStream("tokens.json");
        payload = IOUtils.toString(in);
        data = DataFactory.json(payload);
        container = new Container(payload);
    }
	@Test
	public void testFind() throws OpenNLPWebServiceException {
//        String text = "Mike, Smith is a good person and he is from Boston.";
//        String text = "Carol told Bob to attend the party. They arrived together.";
        String text = "Mr. Modi said the grant of money for renovation of the hospitals would also cover immediate procurement of new equipments and machines." +
                "The Prime Minister told reporters after meeting Chief Minister Omar Abdullah that he was “seriously considering” a demand made by people whose houses were damaged by the flood waters if the money for their renovation could be directly sent to their bank accounts." +
                "The Prime Minister, who arrived in Srinagar from Siachen, said he has directed that immediate assistance be given to school children whose schools, books and note books have been damaged by the deluge so that there is no interruption in their studies." +
                "Earlier, Mr. Abdullah briefed Mr. Modi about the status of relief and rehabilitation operations in the state and sought liberal financial assistance from the Centre to rebuild the devastated areas." +
                "Mr. Modi said one of the demands of the people on Thursday was that “whatever assistance is given to the families that have suffered should, if possible, be transferred directly to their bank accounts as it would be more convenient for them.";
//		String tokens[] = SimpleTokenizer.INSTANCE
//				.tokenize("Mike, Smith is a good person and he is from Boston.");
//		System.out.println(Arrays.toString(tokens));
		Object crf = cor.coRef(text);
        System.out.println(crf);
//		System.out.println(Arrays.toString(spans));
//		Span[] goldSpans = { new Span(0, 1, "person"), new Span(2, 3, "person"), new Span(11,12,"location") };
//		Assert.assertArrayEquals("NamedEntityRecognizer Failure.", spans,
//				goldSpans);
	}

    @Test
    public void testExecute(){
//        Data res = cor.execute(data);
//        System.out.println(res.getPayload());
    }
}
