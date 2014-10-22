package edu.brandeis.cs.lappsgrid.opennlp;


import edu.brandeis.cs.lappsgrid.util.LIFJsonSerialization;
import org.junit.Assert;
import org.junit.Test;
import org.lappsgrid.serialization.json.JSONArray;
import org.lappsgrid.serialization.json.JSONObject;

public class TestLIFJsonSerialization {
    public static final String json = "{\n" +
            "  \"text\": \"Sue sees herself\",\n" +
            "  \"views\": [ \n" +
            "    { \"metadata\": {\n" +
            "        \"contains\": {\n" +
            "          \"Token\": { },\n" +
            "          \"Markable\": { },\n" +
            "          \"Coreference\": { } }},\n" +
            "      \"annotations\": [\n" +
            "         { \"@type\": \"Token\", \"id\": \"tok0\", \"start\": 0, \"end\": 3 },\n" +
            "         { \"@type\": \"Token\", \"id\": \"tok2\", \"start\": 9, \"end\": 16 },\n" +
            "         { \"@type\": \"Markable\",\n" +
            "           \"id\": \"m0\",\n" +
            "           \"features\": {\n" +
            "             \"targets\": [ \"tok0\" ] }},\n" +
            "         { \"@type\": \"Markable\",\n" +
            "           \"id\": \"m1\",\n" +
            "           \"features\": {\n" +
            "             \"targets\": [ \"tok2\" ],\n" +
            "             \"ENTITY_MENTION_TYPE\": \"PRONOUN\" } },\n" +
            "         { \"@type\": \"Coreference\", \n" +
            "           \"id\": \"coref0\", \n" +
            "           \"features\": {\n" +
            "             \"mentions\": [ \"m0\", \"m1\" ],\n" +
            "             \"representative\": \"m0\" }}]}]\n" +
            "}";


    @Test
    public void test(){
        LIFJsonSerialization rlif = new LIFJsonSerialization(json);
        LIFJsonSerialization wlif = new LIFJsonSerialization();
        wlif.setText("Sue sees herself");
        JSONObject view = wlif.newView();
        JSONObject contains = new JSONObject();
        contains.put("Token", new JSONObject());
        contains.put("Markable", new JSONObject());
        contains.put("Coreference", new JSONObject());
        wlif.newMetadata(view,"contains", contains);
        wlif.newAnnotation(view, "Token","tok0", 0, 3);
        wlif.newAnnotation(view, "Token","tok2", 9, 16 );
        JSONObject ann = wlif.newAnnotation(view, "Markable","m0");
        JSONArray targets = new JSONArray();
        targets.put("tok0");
        wlif.setFeature(ann, "targets", targets);
        ann = wlif.newAnnotation(view, "Markable","m1");
        targets = new JSONArray();
        targets.put("tok2");
        wlif.setFeature(ann, "targets", targets);
        wlif.setFeature(ann, "ENTITY_MENTION_TYPE", "PRONOUN");
        ann = wlif.newAnnotation(view, "Coreference","coref0");
        JSONArray mentions = new JSONArray();
        mentions.put("m0");
        mentions.put("m1");
        wlif.setFeature(ann, "mentions", mentions);
        wlif.setFeature(ann,"representative", "m0");
        System.out.println(wlif);
        Assert.assertEquals(rlif.toString(), wlif.toString());
    }
}