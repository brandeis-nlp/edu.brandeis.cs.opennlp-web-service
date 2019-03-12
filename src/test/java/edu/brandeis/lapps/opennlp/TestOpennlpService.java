package edu.brandeis.lapps.opennlp;


import edu.brandeis.lapps.TestBrandeisService;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.lappsgrid.discriminator.Discriminators.Uri;

public class TestOpennlpService extends TestBrandeisService {

    protected String testText;

    public ServiceMetadata testDefaultMetadata() {
        String expectedCharset = "UTF-8";

        ServiceMetadata metadata = super.testDefaultMetadata();

        IOSpecification requires = metadata.getRequires();
        assertEquals(String.format("Must require annotations in %s character set", expectedCharset), expectedCharset.toLowerCase(), requires.getEncoding().toLowerCase());
        assertTrue("Must accept only English input", requires.getLanguage().size() == 1 && requires.getLanguage().get(0).toLowerCase().equals("en"));
        assertEquals("Must only accept LIF input", 1, requires.getFormat().size());
        assertTrue("Must accept LIF payload", requires.getFormat().contains(Uri.LIF));

        IOSpecification produces = metadata.getProduces();
        assertTrue("Must produce annotations for English", produces.getLanguage().contains("en"));
        assertEquals(String.format("Must produce annotations in %s character set", expectedCharset), expectedCharset.toLowerCase(), produces.getEncoding().toLowerCase());
        assertEquals("Must produce only one payload format", 1, produces.getFormat().size());
        assertEquals("Must produce LIF payload", Uri.LIF, produces.getFormat().get(0));

        return metadata;
    }


}
