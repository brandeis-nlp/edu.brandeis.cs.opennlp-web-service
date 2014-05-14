package org.lappsgrid.serialization.json;

import org.lappsgrid.vocabulary.Annotations;

/**
 * Created by shi on 5/13/14.
 */
public class JsonNERSerialization extends JsonSerialization {

    public JsonNERSerialization(String textjson) {
        super(textjson);
        this.setAnnotationType(Annotations.NE);
        this.setIdHeader("ner");
        this.setLastAnnotationType(Annotations.TOKEN);
    }

    public JsonNERSerialization() {
        super();
        this.setAnnotationType(Annotations.NE);
        this.setIdHeader("ner");
        this.setLastAnnotationType(Annotations.TOKEN);
    }
}