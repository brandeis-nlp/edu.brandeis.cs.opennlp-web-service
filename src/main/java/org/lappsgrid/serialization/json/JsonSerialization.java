package org.lappsgrid.serialization.json;



import java.util.ArrayList;
import java.util.List;

/**
 * Created by shi on 5/14/14.
 */
public class JsonSerialization {
    String type = null;
    String textValue = null;
    String producer = null;
    String annotationType = null;
    String idHeader = null;
    String lastAnnotationType = null;

    JSONObject json = null;
    JSONObject currentStep = null;
    JSONObject currentStepMeta = null;
    JSONObject contains = null;
    JSONObject contain = null;
    JSONArray annotations = null;
    JSONArray steps = null;
    JSONObject text = null;
    JSONObject metadata = null;
    static int id = 0;

    JSONObject lastStep = null;
    JSONObject lastStepMeta = null;
    JSONArray lastStepAnnotations = null;
    JSONObject lastStepContains = null;

    public JsonSerialization(String textjson) {
        id = 0;
        json = new JSONObject(textjson);
        text = json.getJSONObject("text");
        textValue = text.getString("@value");
        steps =  json.getJSONArray("steps");
        metadata = json.getJSONObject("metadata");

        currentStep = new JSONObject();
        currentStepMeta = new JSONObject();
        contains = new JSONObject();
        contain = new JSONObject();
        annotations = new JSONArray();
    }

    public JsonSerialization() {
        id = 0;
        json = new JSONObject();
        json.put("@context" , "http://vocab.lappsgrid.org/context-1.0.0.jsonld");
        currentStep = new JSONObject();
        currentStepMeta = new JSONObject();
        contains = new JSONObject();
        contain = new JSONObject();
        annotations = new JSONArray();
        steps = new JSONArray();
        text = new JSONObject();
        metadata = new JSONObject();
    }


    public List<JSONObject> findLastAnnotations() {
        ArrayList<JSONObject> lastAnnotations = null;
        if(steps.length() > 0) {
            for(int i = steps.length() - 1; i >= 0; i--) {
                lastStep =  steps.getJSONObject(i);
                lastStepMeta = lastStep.getJSONObject("metadata");
                lastStepAnnotations = lastStep.getJSONArray("annotations");
                lastStepContains = lastStepMeta.getJSONObject("contains");
                if (lastStepContains.has(lastAnnotationType)) {
                    // contains sentence
                    lastAnnotations = new ArrayList<JSONObject>(lastStepAnnotations.length());
                    for(int j = 0; j < lastStepAnnotations.length(); j++) {
                        JSONObject lastStepAnnotation = lastStepAnnotations.getJSONObject(j);
                        lastAnnotations.add(lastStepAnnotation);
                    }
                    break;
                }
            }
        }
        return lastAnnotations;
    }

    public void setAnnotationType(String ann) {
        annotationType = ann;
    }

    public void setLastAnnotationType(String lat) {
        lastAnnotationType = lat;
    }

    public void setIdHeader(String idh) {
        idHeader = idh;
    }

    public void setType(String typ) {
        type = typ;
    }

    public void setProducer(String prod) {
        producer = prod;
    }

    public void setTextValue(String txt) {
        textValue = txt;
    }

    public String getTextValue() {
        return textValue;
    }

    public String getLastAnnotationType(){
        return lastAnnotationType;
    }

    public String getAnnotationTextValue(JSONObject annotation) {
        int start = getStart(annotation);
        int end = getEnd(annotation);
        return getTextValue().substring(start, end);
    }


    public int getStart(JSONObject annotation) {
        return annotation.getInt("start");
    }

    public int getEnd(JSONObject annotation) {
        return annotation.getInt("end");
    }

    public JSONObject newAnnotationWithType(String type, JSONObject json){
        JSONObject annotation = new JSONObject(json.toString());
        annotation.put("@type", type);
        annotation.put("id", idHeader + id ++);
        annotations.put(annotation);
        return annotation;
    }

    public JSONObject newAnnotationWithType(String type, String json){
        JSONObject annotation = new JSONObject(json);
        annotation.put("@type", type);
        annotation.put("id", idHeader + id ++);
        annotations.put(annotation);
        return annotation;
    }

    public JSONObject newAnnotationWithType(String type){
        JSONObject annotation = new JSONObject();
        annotation.put("@type", type);
        annotation.put("id", idHeader + id ++);
        annotations.put(annotation);
        return annotation;
    }

    public JSONObject newAnnotation(JSONObject json){
        JSONObject annotation = new JSONObject(json.toString());
        annotation.put("@type", annotationType);
        annotation.put("id", idHeader + id ++);
        annotations.put(annotation);
        return annotation;
    }

    public JSONObject newAnnotation(String json){
        JSONObject annotation = new JSONObject(json);
        annotation.put("@type", annotationType);
        annotation.put("id", idHeader + id ++);
        annotations.put(annotation);
        return annotation;
    }

    public JSONObject newAnnotation(){
        JSONObject annotation = new JSONObject();
        annotation.put("@type", annotationType);
        annotation.put("id", idHeader + id ++);
        annotations.put(annotation);
        return annotation;
    }

    public void setStart(JSONObject annotation, int start) {
        annotation.put("start", start);
    }

    public void setEnd(JSONObject annotation, int end) {
        annotation.put("end", end);
    }

    public void setLemma(JSONObject annotation, String lemma) {
        setFeature(annotation, "lemma", lemma);
    }

    public void setWord(JSONObject annotation, String word) {
        setFeature(annotation, "word", word);
    }

    public void setSentence(JSONObject annotation, String sentence) {
        setFeature(annotation, "sentence", sentence);
    }

    public void setPattern(JSONObject annotation, String pattern) {
        setFeature(annotation, "pattern", pattern);
    }

    public void setCategory(JSONObject annotation, String category) {
        setFeature(annotation, "category", category);
    }

    public void setFeature(JSONObject annotation, String name,  String value) {
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
        contain.put("producer", producer);
        contain.put("type", type);
        text.put("@value", textValue);
        contains.put(annotationType, contain);
        currentStepMeta.put("contains", contains);
        currentStep.put("metadata", currentStepMeta);
        currentStep.put("annotations", annotations);
        json.put("metadata", metadata);
        json.put("text", text);
        json.put("steps", steps.put(currentStep));
        return json.toString();
    }
}
