package org.lappsgrid.serialization.json;

import org.lappsgrid.vocabulary.Annotations;

/**
 * Created by shi on 5/13/14.
 */
public class JsonTaggerSerialization extends JsonSerialization {

    public JsonTaggerSerialization(String textjson) {
        super(textjson);
        this.setAnnotationType(Annotations.TOKEN);
        this.setIdHeader("pos");
        this.setLastAnnotationType(Annotations.TOKEN);
    }

    public JsonTaggerSerialization() {
        super();
        this.setAnnotationType(Annotations.TOKEN);
        this.setIdHeader("pos");
        this.setLastAnnotationType(Annotations.TOKEN);
    }

}