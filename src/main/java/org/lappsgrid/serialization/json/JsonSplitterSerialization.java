package org.lappsgrid.serialization.json;

import org.lappsgrid.vocabulary.Annotations;

/**
 * Created by shi on 5/13/14.
 */
public class JsonSplitterSerialization extends JsonSerialization {

    public JsonSplitterSerialization(String textjson) {
        super(textjson);
        this.setAnnotationType(Annotations.SENTENCE);
        this.setIdHeader("s");
    }

    public JsonSplitterSerialization() {
        super();
        this.setAnnotationType(Annotations.SENTENCE);
        this.setIdHeader("s");
    }

}