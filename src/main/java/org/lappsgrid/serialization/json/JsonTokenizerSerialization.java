package org.lappsgrid.serialization.json;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lappsgrid.vocabulary.Annotations;

/**
 * Created by shi on 5/13/14.
 */
public class JsonTokenizerSerialization extends JsonSerialization {
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