package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.Version;
import edu.brandeis.cs.lappsgrid.api.opennlp.IParser;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.JsonArr;
import org.lappsgrid.serialization.json.JsonObj;
import org.lappsgrid.serialization.json.LIFJsonSerialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <i>Parser.java</i> Language Application Grids
 * (<b>LAPPS</b>)
 * <p>
 * <p>
 * <a href="http://opennlp.sourceforge.net/models-1.5/">Models for 1.5
 * series</a>
 * <p>
 * 
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>
 *         Nov 20, 2013<br>
 * 
 */
public class Parser extends OpenNLPAbstractWebService implements IParser {
	protected static final Logger logger = LoggerFactory
			.getLogger(Parser.class);

	private static opennlp.tools.parser.Parser parser;

	public Parser() throws OpenNLPWebServiceException {
        if (parser == null) {
            init();
            parser = loadParser(registModelMap.get(this.getClass()));
        }
	}

	public String parse(String sentence) {
		StringBuffer builder = new StringBuffer();
		Parse parses[] = ParserTool.parseLine(sentence, parser, 1);
        System.out.println(" parses.length = " + parses.length);
		for (int pi = 0, pn = parses.length; pi < pn; pi++) {
			parses[pi].show(builder);
			builder.append("\n");
		}
		return builder.toString();
	}


    @Override
    public String execute(LIFJsonSerialization json) throws OpenNLPWebServiceException {
        String txt = json.getText();
        List<JsonObj> annotationObjs = json.getLastViewAnnotations(Discriminators.Uri.SENTENCE);


        JsonObj view = json.newView();
        json.newContains(view, "Parse", "parser:opennlp", this.getClass().getName() + ":" + Version.getVersion());
        for(int i = 0; i < annotationObjs.size(); i++ ) {
            // for each sentence
            String sent = json.getAnnotationText(annotationObjs.get(i));
            JsonObj ann = json.newAnnotation(view);
            json.setId(ann, "ps" + i);
            json.setType(ann, "http://vocab.lappsgrid.org/PhraseStructure");
            json.setStart(ann, annotationObjs.get(i).getInt("start"));
            json.setEnd(ann, annotationObjs.get(i).getInt("end"));
            json.setSentence(ann, sent);
            json.setFeature(ann, "penntree", parse(sent));
            JsonArr constituents = new JsonArr();
            json.setFeature(ann, "constituents", constituents);
            Parse parses[] = ParserTool.parseLine(sent, parser, 1);
            for (int pi = 0, pn = parses.length; pi < pn; pi++) {
                fillParseAnnotation(parses[pi], constituents, i);
            }
        }
        return json.toString();
    }


    protected String fillParseAnnotation(Parse parse, JsonArr constituents, int sentId) {
        JsonObj constituent = new JsonObj();
        constituents.put(constituent);
        String id = "cs" +sentId+"_"+constituents.length();
        constituent.put("id", id);
        constituent.put("@type", "http://vocab.lappsgrid.org/Constituent");
//        System.out.println("parse.getLabel() = " + parse.getLabel());
        if (!parse.getType().equals(AbstractBottomUpParser.TOK_NODE)) {
            constituent.put("label", parse.getType());
        } else {
            constituent.put("label", parse.getCoveredText());
        }
        if(parse.getChildren().length > 0 ) {
            JsonArr children = new JsonArr();
            constituent.put("children", children);
            for (Parse child : parse.getChildren()) {
                String childId = fillParseAnnotation(child, constituents, sentId);
                children.put(childId);
            }
        }
        return id;
    }
}
