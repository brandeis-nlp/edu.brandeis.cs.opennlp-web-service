package edu.brandeis.cs.lappsgrid.opennlp;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;

import java.io.IOException;
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
public class TestCoreference extends TestService {


    String sentText = "Mike, Smith is a good person and he is from Boston. John and Mary went to the store. They bought some milk.";

    public TestCoreference() throws OpenNLPWebServiceException {
        service = new Coreference();
    }



    //	@Test
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
//        Coreference cor = new Coreference();
//		Object crf = cor.coRef(text);
//        System.out.println(crf);
//		System.out.println(Arrays.toString(spans));
//		Span[] goldSpans = { new Span(0, 1, "person"), new Span(2, 3, "person"), new Span(11,12,"location") };
//		Assert.assertArrayEquals("NamedEntityRecognizer Failure.", spans,
//				goldSpans);
	}

    @Test
    public void testExecute()throws OpenNLPWebServiceException {



        String result = service.execute(sentText);
        System.out.println("<------------------------------------------------------------------------------");
        System.out.println(String.format("      %s         ", this.getClass().getName()));
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println(result);
        System.out.println("------------------------------------------------------------------------------>");





//        String text = "Mike, Smith is a good person and he is from Boston. John and Mary went to the store. They bought some milk.";
        String text = "Pierre Vinken, 61 years old, will join the board as a nonexecutive director Nov. 29. Mr. Vinken is chairman of Elsevier N.V., the Dutch publishing group. Rudolph Agnew, 55 years old and former chairman of Consolidated Gold Fields PLC, was named a director of this British industrial conglomerate.";
        Coreference cor = new Coreference();
        String ret = cor.execute(text);
        System.out.println(ret);
        new Container((Map) Serializer.parse(ret, Data.class).getPayload());
    }
}
