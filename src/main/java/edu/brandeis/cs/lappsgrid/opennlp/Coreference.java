package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.Version;
import org.lappsgrid.discriminator.Discriminator;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.json.LIFJsonSerialization;
import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention;
import opennlp.tools.coref.mention.MentionContext;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.parser.Parse;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.util.Span;
import org.lappsgrid.serialization.json.JsonArr;
import org.lappsgrid.serialization.json.JsonObj;
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
    private static SentenceDetector sentDetector;
    private static Linker linker;
    private static TokenizerME tokenizer;
    private static Map<String,TokenNameFinder> nameFinders;
    private static  opennlp.tools.parser.Parser parser;

    public Coreference() throws OpenNLPWebServiceException {
        if (linker == null)
		    init();
	}
    
	protected void init() throws OpenNLPWebServiceException {
        super.init();
        linker = super.loadCoRefLinker("Coreference");
        sentDetector = this.loadSentenceDetector("Sentence-Detector");
        tokenizer = this.loadTokenizer("Tokenizer");
        nameFinders = this.loadTokenNameFinders("Name-Finder");
        parser = this.loadParser("Parser");
    }
//
//    public Object coRef(String text)throws OpenNLPWebServiceException {
//        LIFJsonSerialization wlif = new LIFJsonSerialization();
//        wlif.setText(text);
//        JsonObj view = wlif.newView();
//        wlif.newContains(view, Discriminators.Uri.COREF, "opennlp:token",
//                this.getClass().getName() + ":" + Version.getVersion());
//        wlif.newContains(view, "http://vocab.lappsgrid.org/Markable", "opennlp:markable",
//                this.getClass().getName() + ":" + Version.getVersion());
//        wlif.newContains(view, Discriminators.Uri.TOKEN, "opennlp:coreference",
//                this.getClass().getName() + ":" + Version.getVersion());
//        // get modelling resources
//        final SentenceDetectorME sentDetector = this.loadSentenceDetector("Sentence-Detector");
//        final TokenizerME tokenizer = this.loadTokenizer("Tokenizer");
//        final  Map<String,TokenNameFinder>  nameFinders = this.loadTokenNameFinders("Name-Finder");
//        final opennlp.tools.parser.Parser parser = this.loadParser("Parser");
//        final Linker linker = this.loadCoRefLinker("Coreference");
//       // get sentences
//        Span[] sentSpans = sentDetector.sentPosDetect(text);
//
//        List<Mention> documentmentions = new ArrayList<Mention>();
//        List<Parse> parses = new ArrayList<Parse>();
//
//        for(int sentNum = 0 ; sentNum < sentSpans.length ; sentNum++) {
//            String sentenceText = text.substring(sentSpans[sentNum].getStart() , sentSpans[sentNum].getEnd());
//            int sentStart = sentSpans[sentNum].getStart();
//            Span[] sentTokens = tokenizer.tokenizePos(sentenceText);
//            // create a parse tree for the sentence, based on the tokens
//            Parse sentParse = parser.parse(this.createSentenceParse(sentenceText, sentTokens));
//
//            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
//            // needs to be after the POS tagger :)
//            Parse[] tagNodes = sentParse.getTagNodes();
//            // create empty array for tag node string values - word tokens
//            String[] tokens = new String[tagNodes.length];
//            // get the tag node word token values, ready to pass to name finder
//            for (int i = 0; i < tagNodes.length; i ++) {
//                Span span = tagNodes[i].getSpan();
//                int start = span.getStart();
//                int end = span.getEnd();
//                tokens[i] = tagNodes[i].getText().substring(start, end);
//                wlif.newAnnotation(view, "Token","tok"+ start , start, end);
//            }
//            // Now pass the String[] of the input parse word tokens to the name finder
//            for (String nerType:nameFinders.keySet())  {
//                Span[] nameSpans  = nameFinders.get(nerType).find(tokens);
//                // with an input Parse object we can generate token parses as above
//                Parse[] tokenParses = sentParse.getTagNodes();
//                // process each name token
//                for(int nameTokenIdx = 0; nameTokenIdx < nameSpans.length; nameTokenIdx++) {
//                    Span nameTokenSpan = nameSpans[nameTokenIdx];
//                    Parse startToken = tokenParses[nameTokenSpan.getStart()];
//                    Parse endToken = tokenParses[nameTokenSpan.getEnd()];
//                    Parse commonParent = startToken.getCommonParent(endToken);
//                    if (commonParent != null) {
//                        Span nameSpan = new Span(startToken.getSpan().getStart(),endToken.getSpan().getEnd());
//                        if (nameSpan.equals(commonParent.getSpan())) {
//                            commonParent.insert(new Parse(commonParent.getText(),nameSpan,nerType,1.0,endToken.getHeadIndex()));
//                        } else {
//                            Parse[] kids = commonParent.getChildren();
//                            boolean crossingKids = false;
//                            for (int ki = 0, kn = kids.length; ki < kn; ki++) {
//                                if (nameSpan.crosses(kids[ki].getSpan()))
//                                    crossingKids = true;
//                            }
//                            if (crossingKids) {
//                                if (commonParent.getType().equals("NP")) {
//                                    Parse[] grandKids = kids[0].getChildren();
//                                    if (grandKids.length > 1 && nameSpan.contains(grandKids[grandKids.length-1].getSpan())) {
//                                        commonParent.insert(new Parse(commonParent.getText(),commonParent.getSpan(),nerType,1.0,commonParent.getHeadIndex()));
//                                    }
//                                }
//                            }else {
//                                commonParent.insert(new Parse(commonParent.getText(),nameSpan,nerType,1.0,endToken.getHeadIndex()));
//                            }
//                        }
//                    } // if
//                } // for
//            }
//            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//            System.out.println("\nSentence#" + (sentNum+1) + " parse after POS & NER tag:");
//            sentParse.show();
//
//            // add to list of parses
//            parses.add(sentParse);
//            // now wrap the parsed sentence result in a DefaultParse object, so it can be used in coref
//            DefaultParse sentParseInd = new DefaultParse(sentParse, sentNum);
//
//            // get all mentions in the parsed sentence
//            Mention[] sentencementions = linker.getMentionFinder().getMentions(sentParseInd);
//
//            // Copy & paste from TreebankLinker source code.. edited for var name changes
//            //construct new parses for mentions which don't have constituents.
//            for (int ei=0,en=sentencementions.length;ei<en;ei++) {
//                if (sentencementions[ei].getParse() == null) {
////                    //not sure how to get head index, but its not used at this point.
//                    Parse snp = new Parse(sentParse.getText(),sentencementions[ei].getSpan(),"NML",1.0,0);
//                    sentParse.insert(snp);
//                    sentencementions[ei].setParse(new DefaultParse(snp, sentNum));
//                }
//                int idx = sentencementions[ei].getParse().getSpan().getStart();
//                JsonObj ann = wlif.newAnnotation(view, "Markable","m"+idx);
//                JsonArr targets = new JsonArr();
//                targets.put("tok" + idx);
//                wlif.setFeature(ann, "targets", targets);
//                wlif.setFeature(ann, "ENTITY_MENTION_TYPE",  sentencementions[ei].getParse().getEntityType());
//            }
//            documentmentions.addAll(Arrays.asList(sentencementions));
//        }
//
//        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
////
////        ann = wlif.newAnnotation(view, "Markable","m1");
////        targets = new JSONArray();
////        targets.put("tok2");
////        wlif.setFeature(ann, "targets", targets);
////        wlif.setFeature(ann, "ENTITY_MENTION_TYPE", "PRONOUN");
////        ann = wlif.newAnnotation(view, "Coreference","coref0");
////        JSONArray mentions = new JSONArray();
////        mentions.put("m0");
////        mentions.put("m1");
////        wlif.setFeature(ann, "mentions", mentions);
////        wlif.setFeature(ann, "representative", "m0");
//
//        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//        if (documentmentions.size() > 0) {
//            // this was for treebank linker, but I'm using DefaultLinker....
//            DiscourseEntity[] entities = linker.getEntities(documentmentions.toArray(new Mention[documentmentions.size()]));
//            System.out.println("\nNow displaying all discourse entities::");
//            for(DiscourseEntity ent : entities) {
//                Iterator<MentionContext> entMentions = ent.getMentions();
//                String mentionString = "";
//                JsonObj ann = null;
//                JsonArr mentions = new JsonArr();
//                while(entMentions.hasNext()) {
//                    Mention men = entMentions.next();
//                    mentions.put("m" + men.getSpan().getStart());
//                    if(mentionString.equals("")) {
//                        mentionString = men.toString();
//                        ann = wlif.newAnnotation(view, "Coreference","coref" +men.getId());
//                    } else {
//                        mentionString = mentionString + " :: " + men.toString();
//                    }
//                }
//                System.out.println("\tMention set:: [ " + mentionString + " ]");
//                if(mentions.length() > 1)
//                    ann.put("mentions", mentions);
//            }
//
//            System.out.println("\n\nNow printing out the named entities from mention sets::");
//            for(DiscourseEntity ent : entities) {
//                Iterator<MentionContext> entMentions = ent.getMentions();
//                while(entMentions.hasNext()) {
//                    Mention men = entMentions.next();
//                    if(men.getNameType() != null) {
//                        System.out.println("\t[" + men.toString() + "]");
//                    }
//                }
//            }
//        }
//        return wlif;
//    }

    @Override
    public String execute(LIFJsonSerialization json) throws OpenNLPWebServiceException {
        String txt = json.getText();
        json.setText(txt);
        JsonObj view = json.newView();
        json.newContains(view, Discriminators.Uri.COREF, "opennlp:token",
                this.getClass().getName() + ":" + Version.getVersion());
        json.newContains(view, "http://vocab.lappsgrid.org/Markable", "opennlp:markable",
                this.getClass().getName() + ":" + Version.getVersion());
        json.newContains(view, Discriminators.Uri.TOKEN, "opennlp:coreference",
                this.getClass().getName() + ":" + Version.getVersion());
        // get sentences
        Span[] sentSpans = sentDetector.sentPosDetect(txt);
        List<Mention> docMentions = new ArrayList<Mention>();
        List<Parse> docParses = new ArrayList<Parse>();
        int cntMention = 0;
        for(int sentIdx = 0 ; sentIdx < sentSpans.length ; sentIdx++) {
            String sentenceText = txt.substring(sentSpans[sentIdx].getStart(), sentSpans[sentIdx].getEnd());
            int sentStart = sentSpans[sentIdx].getStart();
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
                String id = "tok_"+sentIdx+"_"+ start;
                JsonObj tokAnn = json.newAnnotation(view, Discriminators.Uri.TOKEN, id, sentStart+start, sentStart+end);
                json.setWord(tokAnn, tokens[i]);
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

            System.out.println("\nSentence#" + sentIdx + " parse after POS & NER tag:");
            sentParse.show();
            // add to list of parses
            docParses.add(sentParse);
            // now wrap the parsed sentence result in a DefaultParse object, so it can be used in coref
            DefaultParse sentParseInd = new DefaultParse(sentParse, sentIdx);
            // get all mentions in the parsed sentence
            Mention[] sentMentions = linker.getMentionFinder().getMentions(sentParseInd);

            // Copy & paste from TreebankLinker source code.. edited for var name changes
            //construct new parses for mentions which don't have constituents.
            for (int mIdx = 0; mIdx < sentMentions.length; mIdx++) {
                Mention men = sentMentions[mIdx];
                if (men.getParse() == null) {
//                    //not sure how to get head index, but its not used at this point.
                    Parse snp = new Parse(sentParse.getText(),men.getSpan(),"NML",1.0,0);
                    sentParse.insert(snp);
                    men.setParse(new DefaultParse(snp, sentIdx));
                }
                int start = sentStart + men.getSpan().getStart();
                int end =  sentStart + men.getSpan().getEnd();
                JsonObj ann = json.newAnnotation(view);
//                String id = "m_"+start+"_"+end;
                men.setId(cntMention++);
                json.setId(ann,"m"+men.getId());
                json.setType(ann, "http://vocab.lappsgrid.org/Markable");
                json.setStart(ann, start);
                json.setEnd(ann,  end);
                JsonArr targets = new JsonArr();
                for(opennlp.tools.coref.mention.Parse token : men.getParse().getTokens()) {
                    targets.put("tok_"+sentIdx+"_" + token.getSpan().getStart());
                }
                json.setFeature(ann, "targets", targets);
                json.setFeature(ann, "words", men.getParse().toString());
                // json.setFeature(ann, "ENTITY_MENTION_TYPE", sentMentions[mIdx].getParse().getEntityType());
            }
            docMentions.addAll(Arrays.asList(sentMentions));
        }
        int cntCof = 0;
        if (docMentions.size() > 0) {
            // this was for treebank linker, but I'm using DefaultLinker....
            DiscourseEntity[] entities = linker.getEntities(docMentions.toArray(new Mention[docMentions.size()]));
            System.out.println("\nNow displaying all discourse entities::");
            for(DiscourseEntity ent : entities) {
                Iterator<MentionContext> entMentions = ent.getMentions();
                if(!entMentions.hasNext())
                    continue;
                String mentionString = "";
                JsonArr mentions = new JsonArr();
                while(entMentions.hasNext()) {
                    Mention men = entMentions.next();
//                    System.out.println("men="+men.getClass());
                    mentions.put("m" + men.getId());
                    mentionString = mentionString + " || " + men.toString();
                }
                System.out.println("\tMention set:: [ " + mentionString + " ]");
                if(mentions.length() > 1){
                    JsonObj ann =  json.newAnnotation(view, Discriminators.Uri.COREF);
                    json.setId(ann, "coref"+cntCof++);
                    json.setFeature(ann, "mentions", mentions);
                }
            }

            System.out.println("\n\nNow printing out the named entities from mention sets::");
            for(DiscourseEntity ent : entities) {
                Iterator<MentionContext> entMentions = ent.getMentions();
                while(entMentions.hasNext()) {
                    Mention men = entMentions.next();
                    if(men.getNameType() != null) {
                        System.out.println("\t[" +men.getNameType()+"::"+ men.toString() + "]");
                    }
                }
            }
        }
        return json.toString();
    }
}
