package edu.brandeis.lapps.opennlp;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Chunqi SHI (shicq@cs.brandeis.edu) on 3/5/14.
 */
public class TestService {

    protected java.lang.String payload1 = null;
    protected java.lang.String payload2 = null;
    protected HashMap<String,String> jsons = new HashMap<>();

    OpenNLPAbstractWebService service;
    public static String getResource(String name) throws IOException{
        java.io.InputStream in =  TestService.class.getClassLoader().getResourceAsStream(name);
        return IOUtils.toString(in);
    }

    @Before
    public void init() throws IOException {
        File jsonDir = FileUtils.toFile(this.getClass().getResource("/jsons"));
        Collection<File> files = FileUtils.listFiles(jsonDir, new String[]{"json"}, true);
        for(File jsonFile : files){
            jsons.put(jsonFile.getName(), FileUtils.readFileToString(jsonFile, "UTF-8"));
        }
        payload1 = jsons.get("payload1.json");
        payload2 = jsons.get("payload2.json");
    }

    protected Container wrapContainer(String plainText) {
        Data data = Serializer.parse(service.getMetadata(), Data.class);
        Container container = new Container();
        container.setLanguage("en");
        container.setText(plainText);
        return container;
    }

}
