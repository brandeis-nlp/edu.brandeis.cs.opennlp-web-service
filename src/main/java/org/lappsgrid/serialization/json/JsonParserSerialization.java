package org.lappsgrid.serialization.json;

import org.lappsgrid.vocabulary.Annotations;

/**
 * Created by shi on 5/13/14.
 */
public class JsonParserSerialization extends JsonSerialization {

    public JsonParserSerialization(String textjson) {
        super(textjson);
        this.setAnnotationType(Annotations.SENTENCE);
        this.setIdHeader("parser");
        this.setLastAnnotationType(Annotations.SENTENCE);
    }

    public JsonParserSerialization() {
        super();
        this.setAnnotationType(Annotations.SENTENCE);
        this.setIdHeader("parser");
        this.setLastAnnotationType(Annotations.SENTENCE);
    }
}