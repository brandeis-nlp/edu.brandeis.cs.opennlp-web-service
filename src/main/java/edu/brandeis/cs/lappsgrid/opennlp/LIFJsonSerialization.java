package edu.brandeis.cs.lappsgrid.opennlp;

import net.arnx.jsonic.JSON;
import org.lappsgrid.serialization.json.JSONArray;
import org.lappsgrid.serialization.json.JSONObject;
import org.lappsgrid.vocabulary.Features;

/**
 * Created by lapps on 10/22/2014.
 * REF:  http://lapps.github.io/interchange/index.html
 *
 */
public class LIFJsonSerialization {
    String text = null;
    final String context =  "http://vocab.lappsgrid.org/context-1.0.0.jsonld";
    JSONObject json = null;
    JSONArray views = null;

    public String getText() {
        return text;
    }

    public void setText (String text) {
        this.text = text;
    }

    public LIFJsonSerialization() {
        text = "";
        views =  new JSONArray();
        json = new JSONObject();
    }

    public LIFJsonSerialization(String textjson) {
        json = new JSONObject(textjson);
        text = json.getString("text");
        views =  json.getJSONArray("views");
    }

    public JSONObject getJSONObject() {
        return json;
    }

    public JSONObject newMetadata(JSONObject view){
        JSONObject metadata = view.getJSONObject("metadata");
        if (metadata == null) {
            metadata = new JSONObject();
            view.put("metadata", metadata);
        }
        return metadata;
    }


    public JSONObject newMetadata(JSONObject view, String key, Object val){
        JSONObject meta = this.newMetadata(view);
        meta.put(key, val);
        return meta;
    }


    public JSONObject newAnnotation(JSONObject view){
        JSONObject annotation = new JSONObject();
        JSONArray annotations = view.getJSONArray("annotations");
        if (annotations == null) {
            annotations = new JSONArray();
            view.put("annotations", annotations);
        }
        annotations.put(annotation);
        return annotation;
    }
    public JSONObject newAnnotation(JSONObject view, String type, String id) {
        JSONObject ann = this.newAnnotation(view);
        ann.put("@type", type);
        ann.put("id", id);
        return ann;
    }
    public JSONObject newAnnotation(JSONObject view, String type, String id, int start, int end) {
        JSONObject ann = this.newAnnotation(view);
        ann.put("@type", type);
        ann.put("id", id);
        ann.put("start", start);
        ann.put("end", end);
        return ann;
    }

    public JSONObject newView() {
        JSONObject view = new JSONObject();
        JSONArray annotations = new JSONArray();
        view.put("metadata", new JSONObject());
        view.put("annotations", annotations);
        views.put(view);
        return view;
    }

//    public JSONObject newAnnotationWithType(String type, JSONObject json){
//        JSONObject annotation = new JSONObject(json.toString());
//        annotation.put("@type", type);
//        annotations.put(annotation);
//        return annotation;
//    }

    public void setLemma(JSONObject annotation, String lemma) {
        setFeature(annotation, Features.LEMMA, lemma);
    }

    public void setWord(JSONObject annotation, String word) {
        setFeature(annotation, Features.WORD, word);
    }

    public void setFeature(JSONObject annotation, String name,  Object value) {
        JSONObject features = annotation.getJSONObject("features");
        if (features == null) {
            features = newFeatures(annotation);
        }
        features.put(name, value);
    }

    public JSONObject newFeatures(JSONObject annotation) {
        JSONObject features = new JSONObject();
        annotation.put("features", features);
        return features;
    }

    public String toString(){
//        json.put("@context" ,context);
        json.put("text", text);
        json.put("views", views);
        return json.toString();
    }

}
