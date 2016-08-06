package edu.brandeis.cs.lappsgrid.opennlp;


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

    protected HashMap<String,String> jsons = new HashMap<String,String>();

    OpenNLPAbstractWebService service;
    public static String getResource(String name) throws IOException{
        java.io.InputStream in =  TestService.class.getClassLoader().getResourceAsStream(name);
        return IOUtils.toString(in);
    }

    @Before
    public void init() throws IOException {
        File jsonDir = FileUtils.toFile(this.getClass().getResource("/jsons"));
//        System.out.println(jsonDir);
        Collection<File> fils = FileUtils.listFiles(jsonDir, new String[]{"json"}, true);
//        System.out.println(fils);
        for(File jsonFil : fils){
//            System.out.println(jsonFil.getName());
            jsons.put(jsonFil.getName(), FileUtils.readFileToString(jsonFil, "UTF-8"));
        }
        payload1 = jsons.get("payload1.json");
        payload2 = jsons.get("payload2.json");
    }

    protected Container wrapContainer(String plainText) {
        Data data = Serializer.parse(service.getMetadata(), Data.class);
        Container container = new Container();
//        container.setLanguage("en");
        container.setText(plainText);
        // return empty metadata for process result (for now)
//        container.setMetadata((Map) data.getPayload());
        return container;
    }

    @Test
    public void test() {
//        System.out.println(payload1);
//        System.out.println("<-------------------------------->");
//
//        System.out.println(payload2);
    }

//    @BeforeClass
//    public static void prepare() {
//        System.out.println("/-----------------------------------\\");
//    }
//
//    @AfterClass
//    public static void tear() {
//        System.out.println("\\-----------------------------------/\n");
//    }

}
