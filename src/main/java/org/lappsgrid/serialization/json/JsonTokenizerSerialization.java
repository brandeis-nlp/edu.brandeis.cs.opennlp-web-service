package org.lappsgrid.serialization.json;

import org.lappsgrid.vocabulary.Annotations;

/**
 * Created by shi on 5/13/14.
 */
public class JsonTokenizerSerialization extends JsonSerialization {

    public JsonTokenizerSerialization(String textjson) {
        super(textjson);
        this.setAnnotationType(Annotations.TOKEN);
        this.setIdHeader("tok");
        this.setLastAnnotationType(Annotations.SENTENCE);
    }

    public JsonTokenizerSerialization() {
        super();
        this.setAnnotationType(Annotations.TOKEN);
        this.setIdHeader("tok");
        this.setLastAnnotationType(Annotations.SENTENCE);
    }

}