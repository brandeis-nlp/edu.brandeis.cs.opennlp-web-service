package org.lappsgrid.serialization.json;

import org.lappsgrid.discriminator.Constants;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.vocabulary.Features;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lapps on 10/22/2014.
 * REF:  http://lapps.github.io/interchange/index.html
 *
 */
public class LIFJsonSerialization {

    String discriminator = null;
    JsonObj payload = null;
    JsonObj text = null;
    JsonObj error = null;
    String context =  "http://vocab.lappsgrid.org/context-1.0.0.jsonld";

    JsonObj metadata = null;
    JsonArr views = null;
    JsonObj json = null;

    String idHeader = "";
    static int id = 0;

    public void setIdHeader(String idh) {
        idHeader = idh;
        id = 0; // reset.
    }

    public String getText() {
        return text.getString("@value");
    }

    public void setText (String text) {
        this.text.put("@value", text);
    }

    public LIFJsonSerialization() {
        discriminator = Discriminators.Uri.JSON_LD;
        payload= new JsonObj();
        text = new JsonObj();
        views =  new JsonArr();
        metadata = new JsonObj();
        json = new JsonObj();
        error = new JsonObj();
    }

    public void setDiscriminator(String s) {
        this.discriminator = s;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public LIFJsonSerialization(String textjson) {
        json = new JsonObj(textjson);
        discriminator = json.getString("discriminator").trim();
        if (discriminator.equals(Discriminators.Uri.TEXT)) {
            text = new JsonObj();
            text.put("@value", json.getString("payload"));
            // reinitialize other parts.
            discriminator = Discriminators.Uri.JSON_LD;
            payload = new JsonObj();
            metadata =  new JsonObj();
            views = new JsonArr();
        } else if(discriminator.equals(Discriminators.Uri.JSON_LD)) {
            payload = json.getJsonObj("payload");
            text = payload.getJsonObj("text");
            metadata = payload.getJsonObj("metadata");
            if (metadata == null)
                metadata = new JsonObj();
            views =  payload.getJsonArr("views");
            if (views == null)
                views = new JsonArr();
        }
    }

    public JsonObj getJSONObject() {
        return json;
    }

    public JsonObj newViewsMetadata(JsonObj view){
        JsonObj metadata = view.getJsonObj("metadata");
        if (metadata == null) {
            metadata = new JsonObj();
            view.put("metadata", metadata);
        }
        return metadata;
    }


    public JsonObj newViewswMetadata(JsonObj view, String key, Object val){
        JsonObj meta = this.newViewsMetadata(view);
        meta.put(key, val);
        return meta;
    }

    public JsonObj newContains(JsonObj view,String containName, String type, String producer){
        JsonObj meta = this.newViewsMetadata(view);
        JsonObj contains = meta.getJsonObj("contains");
        if (contains == null) {
            contains = new JsonObj();
            meta.put("contains", contains);
        }
        JsonObj contain = new JsonObj();
        contain.put("producer", producer);
        contain.put("type",type);
        contains.put(containName,contain);
        return contains;
    }

    public JsonObj newAnnotation(JsonObj view){
        JsonObj annotation = new JsonObj();
        JsonArr annotations = view.getJsonArr("annotations");
        if (annotations == null) {
            annotations = new JsonArr();
            view.put("annotations", annotations);
        }
        annotations.put(annotation);
        return annotation;
    }

    public JsonObj newAnnotation(JsonObj view, JsonObj copyfrom) {
        JsonObj annotation = new JsonObj(copyfrom.toString());
        JsonArr annotations = view.getJsonArr("annotations");
        if (annotations == null) {
            annotations = new JsonArr();
            view.put("annotations", annotations);
        }
        annotations.put(annotation);
        return annotation;
    }

    public JsonObj newAnnotation(JsonObj view, String label, String id) {
        JsonObj ann = this.newAnnotation(view);
        ann.put("label", label);
        ann.put("id", id);
        return ann;
    }

    public JsonObj newAnnotation(JsonObj view, String label) {
        JsonObj ann = this.newAnnotation(view);
        ann.put("label", label);
        ann.put("id", idHeader+id++);
        return ann;
    }

    public JsonObj newAnnotation(JsonObj view, String label, String id, int start, int end) {
        JsonObj ann = this.newAnnotation(view);
        ann.put("label", label);
        ann.put("id", id);
        ann.put("start", start);
        ann.put("end", end);
        return ann;
    }



    public JsonObj newAnnotation(JsonObj view, String label,  int start, int end) {
        JsonObj ann = this.newAnnotation(view);
        ann.put("label", label);
        ann.put("id", idHeader+id++);
        ann.put("start", start);
        ann.put("end", end);
        return ann;
    }


    public JsonObj newView() {
        JsonObj view = new JsonObj();
        JsonArr annotations = new JsonArr();
        view.put("metadata", new JsonObj());
        view.put("annotations", annotations);
        views.put(view);
        return view;
    }

    public void setStart(JsonObj annotation, int start) {
        annotation.put("start", start);
    }

    public void setEnd(JsonObj annotation, int end) {
        annotation.put("end", end);
    }

    public void setLemma(JsonObj annotation, String lemma) {
        setFeature(annotation, Features.Token.LEMMA, lemma);
    }

    public void setWord(JsonObj annotation, String word) {
        setFeature(annotation, "word", word);
    }
    public List<JsonObj> findLastAnnotations() {
        ArrayList<JsonObj> lastAnnotations = null;
        if(views.length() > 0) {
            for(int i = views.length() - 1; i >= 0; i--) {
                JsonObj lastView =  views.getJsonObj(i);
                JsonObj lastViewMeta = lastView.getJsonObj("metadata");
                JsonArr lastViewAnnotations = lastView.getJsonArr("annotations");
                JsonObj lastViewContains = lastViewMeta.getJsonObj("contains");
                if (lastViewContains.has(Discriminators.Uri.TOKEN)) {
                    // contains sentence
                    lastAnnotations = new ArrayList<JsonObj>(lastViewAnnotations.length());
                    for(int j = 0; j < lastViewAnnotations.length(); j++) {
                        JsonObj lastStepAnnotation = lastViewAnnotations.getJsonObj(j);
                        lastAnnotations.add(lastStepAnnotation);
                    }
                    break;
                }
            }
        }
        return lastAnnotations;
    }

    public int getStart(JsonObj annotation) {
        return annotation.getInt("start");
    }

    public int getEnd(JsonObj annotation) {
        return annotation.getInt("end");
    }

    public String getAnnotationText(JsonObj annotation) {
        int start = getStart(annotation);
        int end = getEnd(annotation);
        return getText().substring(start, end);
    }

    public void setSentence(JsonObj annotation, String sent) {
        setFeature(annotation, "sentence", sent);
    }

    public void setPOSTag(JsonObj annotation, String posTag) {
        setFeature(annotation, "pos", posTag);
    }
    public void setNamedEntity(JsonObj annotation, String ne) {
        setFeature(annotation, "ne", ne);
    }

    public void setError(String msg, String stacktrace) {
        this.setDiscriminator(Discriminators.Uri.ERROR);
        JsonObj val = new JsonObj();
        val.put("@value", msg);
        val.put("stacktrace", stacktrace);
        error.put("text",  val);
    }

    public void setFeature(JsonObj annotation, String name,  Object value) {
        JsonObj features = annotation.getJsonObj("features");
        if (features == null) {
            features = newFeatures(annotation);
        }
        features.put(name, value);
    }

    public JsonObj newFeatures(JsonObj annotation) {
        JsonObj features = new JsonObj();
        annotation.put("features", features);
        return features;
    }

    public String toString(){
        json.put("discriminator" ,discriminator);
        if (discriminator.equals(Discriminators.Uri.TEXT)) {
            json.put("payload" ,text.getString("@value"));
        } else if (discriminator.equals(Discriminators.Uri.JSON_LD)) {
            json.put("payload" ,payload);
            payload.put("@context",context);
            payload.put("metadata", metadata);
            payload.put("text", text);
            payload.put("views", views);
        } else if(discriminator.equals(Discriminators.Uri.ERROR)) {
            json.put("payload" ,error);
        }
        return json.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        LIFJsonSerialization obj = (LIFJsonSerialization)o;
        this.toString();
        obj.toString();
        return this.json.equals(obj.json);
    }
}
