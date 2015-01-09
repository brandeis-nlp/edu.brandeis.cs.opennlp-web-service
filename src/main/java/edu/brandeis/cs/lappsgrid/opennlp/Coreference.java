package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.util.LIFJsonSerialization;
import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention;
import opennlp.tools.coref.mention.MentionContext;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.parser.*;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.util.Span;
import org.lappsgrid.api.Data;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.lappsgrid.serialization.json.JSONArray;
import org.lappsgrid.serialization.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <i>AbstractOpenNLPWebService.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p><a href="http://opennlp.sourceforge.net/models-1.5/">Models for 1.5 series</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Oct 20, 2014<br>
 * 
 */
public class Coreference extends OpenNLPAbstractWebService {
    protected static final Logger logger = LoggerFactory.getLogger(Coreference.class);
    private static long id = 0;
    private static SentenceDetector sentenceDetector;
    private static Linker linker;
    private static TokenizerME tokenDetector;

    public Coreference() throws OpenNLPWebServiceException {
        if (sentenceDetector == null)
		    init();
	}
    
	protected void init() throws OpenNLPWebServiceException {
        super.init();
        linker = super.loadCoRefLinker("Coreference");
        tokenDetector = super.loadTokenizer("Tokenizer");
	}

	@Override
	public Data configure(Data data) {
		return DataFactory.ok();
	}

    static Data metadata = loadMetadata();

    static private Data loadMetadata() {
        Data metadata = null;
        try {
            String json = "";
            metadata = DataFactory.meta(json);
        } catch(Exception e){
            metadata = DataFactory.error("Unable to load metadata", e);
        }
        return metadata;
    }

    public Data getMetadata() {
        return metadata;
    }


    @Override
    public Data execute(Data data) {
        logger.info("execute(): Execute OpenNLP SentenceDetector ...");
        String discriminatorstr = data.getDiscriminator();
        long discriminator = DiscriminatorRegistry.get(discriminatorstr);

        if (discriminator == Types.ERROR)
        {
            return data;
        } else if (discriminator == Types.JSON) {
            String jsonstr = data.getPayload();
//            LIFJsonSerialization rlif = new LIFJsonSerialization(jsonstr);
//            String text = rlif.getText();
            String text = "How are you today, Mike?";
            Object wlif = null;
            try {
                wlif = coRef(text);
            } catch (OpenNLPWebServiceException e) {
                e.printStackTrace();
                String name = DiscriminatorRegistry.get(discriminator);
                String message = e.getMessage();
                logger.warn(message);
                return DataFactory.error(message);
            }
            System.out.println(wlif);
            return DataFactory.json(wlif.toString());
        } else {
            String name = DiscriminatorRegistry.get(discriminator);
            String message = "Invalid input type. Expected JSON but found " + name;
            logger.warn(message);
            return DataFactory.error(message);
        }
    }



    public Object coRef(String text)throws OpenNLPWebServiceException{
        LIFJsonSerialization wlif = new LIFJsonSerialization();
        wlif.setText(text);
        JSONObject view = wlif.newView();
        JSONObject contains = new JSONObject();
        wlif.newMetadataContainType(view, "Token", "opennlp:token",
                "edu.brandeis.cs.lappsgrid.opennlp.Coreference");
        wlif.newMetadataContainType(view, "Markable", "opennlp:markable",
                "edu.brandeis.cs.lappsgrid.opennlp.Coreference");
        wlif.newMetadataContainType(view, "Coreference", "opennlp:coreference",
                "edu.brandeis.cs.lappsgrid.opennlp.Coreference");
        // get modelling resources
        final SentenceDetectorME sentDetector = this.loadSentenceDetector("Sentence-Detector");
        final TokenizerME tokenizer = this.loadTokenizer("Tokenizer");
        final  Map<String,TokenNameFinder>  nameFinders = this.loadTokenNameFinders("Name-Finder");
        final opennlp.tools.parser.Parser parser = this.loadParser("Parser");
        final Linker linker = this.loadCoRefLinker("Coreference");
       // get sentences
        Span[] sentSpans = sentDetector.sentPosDetect(text);

        List<Mention> documentmentions = new ArrayList<Mention>();
        List<Parse> parses = new ArrayList<Parse>();

        for(int sentNum = 0 ; sentNum < sentSpans.length ; sentNum++) {
            String sentenceText = text.substring(sentSpans[sentNum].getStart() , sentSpans[sentNum].getEnd());
            int sentStart = sentSpans[sentNum].getStart();
            Span[] sentTokens = tokenizer.tokenizePos(sentenceText);
            // create a parse tree for the sentence, based on the tokens
            Parse sentParse = parser.parse(this.createSentenceParse(sentenceText, sentTokens));

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // needs to be after the POS tagger :)
            Parse[] tagNodes = sentParse.getTagNodes();
            // create empty array for tag node string values - word tokens
            String[] tokens = new String[tagNodes.length];
            // get the tag node word token values, ready to pass to name finder
            for (int i = 0; i < tagNodes.length; i ++) {
                Span span = tagNodes[i].getSpan();
                int start = span.getStart();
                int end = span.getEnd();
                tokens[i] = tagNodes[i].getText().substring(start, end);
                wlif.newAnnotation(view, "Token","tok"+ start , start, end);
            }
            // Now pass the String[] of the input parse word tokens to the name finder
            for (String nerType:nameFinders.keySet())  {
                Span[] nameSpans  = nameFinders.get(nerType).find(tokens);
                // with an input Parse object we can generate token parses as above
                Parse[] tokenParses = sentParse.getTagNodes();
                // process each name token
                for(int nameTokenIdx = 0; nameTokenIdx < nameSpans.length; nameTokenIdx++) {
                    Span nameTokenSpan = nameSpans[nameTokenIdx];
                    Parse startToken = tokenParses[nameTokenSpan.getStart()];
                    Parse endToken = tokenParses[nameTokenSpan.getEnd()];
                    Parse commonParent = startToken.getCommonParent(endToken);
                    if (commonParent != null) {
                        Span nameSpan = new Span(startToken.getSpan().getStart(),endToken.getSpan().getEnd());
                        if (nameSpan.equals(commonParent.getSpan())) {
                            commonParent.insert(new Parse(commonParent.getText(),nameSpan,nerType,1.0,endToken.getHeadIndex()));
                        } else {
                            Parse[] kids = commonParent.getChildren();
                            boolean crossingKids = false;
                            for (int ki = 0, kn = kids.length; ki < kn; ki++) {
                                if (nameSpan.crosses(kids[ki].getSpan()))
                                    crossingKids = true;
                            }
                            if (crossingKids) {
                                if (commonParent.getType().equals("NP")) {
                                    Parse[] grandKids = kids[0].getChildren();
                                    if (grandKids.length > 1 && nameSpan.contains(grandKids[grandKids.length-1].getSpan())) {
                                        commonParent.insert(new Parse(commonParent.getText(),commonParent.getSpan(),nerType,1.0,commonParent.getHeadIndex()));
                                    }
                                }
                            }else {
                                commonParent.insert(new Parse(commonParent.getText(),nameSpan,nerType,1.0,endToken.getHeadIndex()));
                            }
                        }
                    } // if
                } // for
            }
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            System.out.println("\nSentence#" + (sentNum+1) + " parse after POS & NER tag:");
            sentParse.show();

            // add to list of parses
            parses.add(sentParse);
            // now wrap the parsed sentence result in a DefaultParse object, so it can be used in coref
            DefaultParse sentParseInd = new DefaultParse(sentParse, sentNum);

            // get all mentions in the parsed sentence
            Mention[] sentencementions = linker.getMentionFinder().getMentions(sentParseInd);

            // Copy & paste from TreebankLinker source code.. edited for var name changes
            //construct new parses for mentions which don't have constituents.
            for (int ei=0,en=sentencementions.length;ei<en;ei++) {
                if (sentencementions[ei].getParse() == null) {
//                    //not sure how to get head index, but its not used at this point.
                    Parse snp = new Parse(sentParse.getText(),sentencementions[ei].getSpan(),"NML",1.0,0);
                    sentParse.insert(snp);
                    sentencementions[ei].setParse(new DefaultParse(snp, sentNum));
                }
                int idx = sentencementions[ei].getParse().getSpan().getStart();
                JSONObject ann = wlif.newAnnotation(view, "Markable","m"+idx);
                JSONArray targets = new JSONArray();
                targets.put("tok" + idx);
                wlif.setFeature(ann, "targets", targets);
                wlif.setFeature(ann, "ENTITY_MENTION_TYPE",  sentencementions[ei].getParse().getEntityType());
            }
            documentmentions.addAll(Arrays.asList(sentencementions));
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//        ann = wlif.newAnnotation(view, "Markable","m1");
//        targets = new JSONArray();
//        targets.put("tok2");
//        wlif.setFeature(ann, "targets", targets);
//        wlif.setFeature(ann, "ENTITY_MENTION_TYPE", "PRONOUN");
//        ann = wlif.newAnnotation(view, "Coreference","coref0");
//        JSONArray mentions = new JSONArray();
//        mentions.put("m0");
//        mentions.put("m1");
//        wlif.setFeature(ann, "mentions", mentions);
//        wlif.setFeature(ann, "representative", "m0");

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        if (documentmentions.size() > 0) {
            // this was for treebank linker, but I'm using DefaultLinker....
            DiscourseEntity[] entities = linker.getEntities(documentmentions.toArray(new Mention[documentmentions.size()]));
            System.out.println("\nNow displaying all discourse entities::");
            for(DiscourseEntity ent : entities) {
                Iterator<MentionContext> entMentions = ent.getMentions();
                String mentionString = "";
                JSONObject ann = null;
                JSONArray mentions = new JSONArray();
                while(entMentions.hasNext()) {
                    Mention men = entMentions.next();
                    mentions.put("m" + men.getSpan().getStart());
                    if(mentionString.equals("")) {
                        mentionString = men.toString();
                        ann = wlif.newAnnotation(view, "Coreference","coref" +men.getId());
                    } else {
                        mentionString = mentionString + " :: " + men.toString();
                    }
                }
                System.out.println("\tMention set:: [ " + mentionString + " ]");
                if(mentions.length() > 1)
                    ann.put("mentions", mentions);
            }

            System.out.println("\n\nNow printing out the named entities from mention sets::");
            for(DiscourseEntity ent : entities) {
                Iterator<MentionContext> entMentions = ent.getMentions();
                while(entMentions.hasNext()) {
                    Mention men = entMentions.next();
                    if(men.getNameType() != null) {
                        System.out.println("\t[" + men.toString() + "]");
                    }
                }
            }
        }
        return wlif;
    }
}
