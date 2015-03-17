package edu.brandeis.cs.lappsgrid.opennlp;


import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.LIFJsonSerialization;
import org.junit.Assert;
import org.junit.Test;
import org.lappsgrid.serialization.json.JsonArr;
import org.lappsgrid.serialization.json.JsonObj;

public class TestLIFJsonSerialization {
    public static final String json = "{\n" +
            "  \"discriminator\": \"http://vocab.lappsgrid.org/ns/media/jsonld\",\n" +
            "  \"payload\": {\n" +
            "  \"@context\": \"http://vocab.lappsgrid.org/context-1.0.0.jsonld\"," +
            "    \"metadata\": {\n" +
            "      \n" +
            "    },\n" +
            "    \"text\": {\n" +
            "      \"@value\": \"Hello world\"\n" +
            "    },\n" +
            "    \"views\": [\n" +
            "      {\n" +
            "        \"metadata\": {\n" +
            "          \"contains\": {\n" +
            "            \"http://vocab.lappsgrid.org/Token\": {\n" +
            "              \"producer\": \"org.anc.lapps.stanford.Tokenizer:2.0.0\",\n" +
            "              \"type\": \"stanford\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        \"annotations\": [\n" +
            "          {\n" +
            "            \"id\": \"tok0\",\n" +
            "            \"start\": 0,\n" +
            "            \"end\": 5,\n" +
            "            \"label\": \"http://vocab.lappsgrid.org/Token\",\n" +
            "            \"features\": {\n" +
            "              \"word\": \"Hello\"\n" +
            "            }\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": \"tok1\",\n" +
            "            \"start\": 6,\n" +
            "            \"end\": 11,\n" +
            "            \"label\": \"http://vocab.lappsgrid.org/Token\",\n" +
            "            \"features\": {\n" +
            "              \"word\": \"world\"\n" +
            "            }\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"metadata\": {\n" +
            "          \"contains\": {\n" +
            "            \"http://vocab.lappsgrid.org/Token#pos\": {\n" +
            "              \"producer\": \"org.anc.lapps.stanford.Tagger:2.0.0\",\n" +
            "              \"type\": \"tagset:penn\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        \"annotations\": [\n" +
            "          {\n" +
            "            \"id\": \"tok0\",\n" +
            "            \"start\": 0,\n" +
            "            \"end\": 5,\n" +
            "            \"label\": \"http://vocab.lappsgrid.org/Token\",\n" +
            "            \"features\": {\n" +
            "              \"pos\": \"UH\",\n" +
            "              \"word\": \"Hello\"\n" +
            "            }\n" +
            "          },\n" +
            "          {\n" +
            "            \"id\": \"tok1\",\n" +
            "            \"start\": 6,\n" +
            "            \"end\": 11,\n" +
            "            \"label\": \"http://vocab.lappsgrid.org/Token\",\n" +
            "            \"features\": {\n" +
            "              \"pos\": \"NN\",\n" +
            "              \"word\": \"world\"\n" +
            "            }\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    public static String jsontxt = "{\n" +
            "\"discriminator\" : \"http://vocab.lappsgrid.org/ns/media/text\",\n" +
            "\"payload\" : \"Hello world\"\n" +
            "}";

    @Test
    public void test(){
        LIFJsonSerialization rlif = new LIFJsonSerialization(json);
        LIFJsonSerialization wlif = new LIFJsonSerialization();
        wlif.setText("Hello world");
        JsonObj view = wlif.newView();
        wlif.newContains(view, Discriminators.Uri.TOKEN, "stanford", "org.anc.lapps.stanford.Tokenizer:2.0.0");
        JsonObj ann = wlif.newAnnotation(view, Discriminators.Uri.TOKEN,"tok0", 0, 5);
        wlif.setWord(ann, "Hello");
        ann = wlif.newAnnotation(view, Discriminators.Uri.TOKEN,"tok1", 6, 11);
        wlif.setWord(ann, "world");

        view = wlif.newView();
        wlif.newContains(view, Discriminators.Uri.POS, "tagset:penn", "org.anc.lapps.stanford.Tagger:2.0.0");
        ann = wlif.newAnnotation(view, Discriminators.Uri.TOKEN,"tok0", 0, 5);
        wlif.setWord(ann, "Hello");
        wlif.setPOSTag(ann, "UH");

        ann = wlif.newAnnotation(view, Discriminators.Uri.TOKEN,"tok1", 6, 11);
        wlif.setWord(ann, "world");
        wlif.setPOSTag(ann, "NN");
        System.out.println(wlif);
        System.out.println(rlif);
        Assert.assertTrue(rlif.equals(wlif));
        LIFJsonSerialization txtlif = new LIFJsonSerialization();
        txtlif.setText("Hello world");
        txtlif.setDiscriminator(Discriminators.Uri.TEXT);
        Assert.assertTrue(txtlif.equals(new LIFJsonSerialization(jsontxt)));
        System.out.println(new LIFJsonSerialization(jsontxt).toString());
    }
}