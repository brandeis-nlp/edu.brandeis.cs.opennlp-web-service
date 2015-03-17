package edu.brandeis.cs.lappsgrid.opennlp;


import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Chunqi SHI (shicq@cs.brandeis.edu) on 3/5/14.
 */
public class TestService {

    public static final String PAYLOAD_RESOURCE = "payload.txt";

    protected java.lang.String payload = null;

    @Before
    public void data() throws IOException {
        java.io.InputStream in =  this.getClass().getClassLoader().getResourceAsStream(PAYLOAD_RESOURCE);
        payload = IOUtils.toString(in);
    }

    @Test
    public void test() {
    }

}
