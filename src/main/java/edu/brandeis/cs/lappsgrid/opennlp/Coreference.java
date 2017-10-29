package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.coref.DefaultLinker;
import opennlp.tools.coref.DiscourseEntity;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.coref.mention.DefaultParse;
import opennlp.tools.coref.mention.Mention;
import opennlp.tools.coref.mention.MentionContext;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.vocabulary.Features;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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
    private Linker corefLinker;

    private SentenceDetector sentDetector;
    private opennlp.tools.tokenize.Tokenizer tokenizer;
    private List<TokenNameFinder> nameFinders;
    private opennlp.tools.parser.Parser parser;

    public Coreference() throws OpenNLPWebServiceException {
        if (corefLinker == null)
            loadAnnotators();
    }

    @Override
    protected void loadAnnotators() throws OpenNLPWebServiceException {

        corefLinker = loadCoRefLinker();
        super.loadTokenizerModel();
        super.loadSentenceModel();
        super.loadParserModel();
        super.loadNameFinderModels();
        tokenizer = new TokenizerME(tokenizerModel);
        sentDetector = new SentenceDetectorME(sentenceDetectorModel);
        parser = ParserFactory.create(parserModel);
        nameFinders = new ArrayList<>();
        for (TokenNameFinderModel model : nameFinderModels) {
            nameFinders.add(new NameFinderME(model));
        }
    }

    private Linker loadCoRefLinker() throws  OpenNLPWebServiceException  {
        String corefModelResPath = MODELS.getProperty(
                MODEL_PROP_KEY_MAP.get(getClass()),
                DEFAULT_MODEL_RES_FILE_MAP.get(getClass()));
        try {

            logger.info("Setting a system property for `WNSEARCHDIR`. " +
                    "This is required to run opennlp coreference resolution.");
            System.setProperty("WNSEARCHDIR", MODELS.getProperty("Wordnet", "/wordnet.3.1.dict"));

            logger.info(String.format("Opening a binary model for %s", "COREF"));
            String corefModelDirectory = new File(getClass().getResource(corefModelResPath).toURI()).getAbsolutePath();
            return new DefaultLinker(corefModelDirectory, LinkerMode.TEST);
        } catch (URISyntaxException | IOException e) {
            throw super.modelFails("COREF", corefModelResPath, e);
        }
    }

    @Override
    public String execute(Container container) throws OpenNLPWebServiceException {
        String text = container.getText();
        View view = container.newView();
        view.addContains(Uri.TOKEN,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "tokenizer:opennlp");
        view.addContains(Uri.COREF,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "coreference:opennlp");
        view.addContains(Uri.MARKABLE,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "markable:opennlp");

        // get sentences
        Span[] sentSpans = sentDetector.sentPosDetect(text);
        List<Mention> mentions = new ArrayList<>();
        List<Parse> parses = new ArrayList<>();
        int mentionIdx = 0;
        int sentIdx = 0;
        for (Span sentSpan : sentSpans) {

        }
        // krim for each sentence
        for(int sentIdx = 0 ; sentIdx < sentSpans.length ; sentIdx++) {
            Span sentSpan = sentSpans[sentIdx];
            String sentText = text.substring(sentSpan.getStart(), sentSpan.getEnd());
            Span[] sentTokens = tokenizer.tokenizePos(sentText);

            // create a parse tree of the sentence, based on the terminal tokens
            Parse sentParse = parser.parse(createTerminalNodes(sentText, sentTokens));
            Parse[] posNodes = sentParse.getTagNodes(); // pos-tag span array

            // create empty array for tag node string values (word tokens)
            String[] tokens = getWordTokens(view, sentIdx, sentSpan, sentText, posNodes);

            /*
            // Now pass the work token array to ner
            for (String nerType:nameFinders.keySet())  {
                Span[] neSpans  = nameFinders.get(nerType).find(tokens);
                // with an input Parse object we can generate token parses as above
                // process each name token
                for (Span neSpan : neSpans) {
                    Parse startToken = posNodes[neSpan.getStart()];
                    Parse endToken = posNodes[neSpan.getEnd()];
                    Parse commonParent = startToken.getCommonParent(endToken);
                    if (commonParent != null) {
                        Span wholeNeSpan = new Span(startToken.getSpan().getStart(), endToken.getSpan().getEnd());
                        if (wholeNeSpan.equals(commonParent.getSpan())) {
                            commonParent.insert(new Parse(commonParent.getText(), wholeNeSpan, nerType, 1.0, endToken.getHeadIndex()));
                        } else {
                            Parse[] children = commonParent.getChildren();
                            boolean crossingKids = false;
                            for (Parse child : children) {
                                if (wholeNeSpan.crosses(child.getSpan()))
                                    crossingKids = true;
                            }
                            if (crossingKids) {
                                if (commonParent.getType().equals("NP")) {
                                    Parse[] grandChildren = children[0].getChildren();
                                    if (grandChildren.length > 1 && wholeNeSpan.contains(grandChildren[grandChildren.length - 1].getSpan())) {
                                        commonParent.insert(new Parse(commonParent.getText(), commonParent.getSpan(), nerType, 1.0, commonParent.getHeadIndex()));
                                    }
                                }
                            } else {
                                commonParent.insert(new Parse(commonParent.getText(), wholeNeSpan, nerType, 1.0, endToken.getHeadIndex()));
                            }
                        }
                    }
                }
            }
            */

            System.out.println("\nSentence#" + sentIdx + " parse after POS & NER tag:");
            sentParse.show();
            // add to list of parses
            parses.add(sentParse);
            // now wrap the parsed sentence result in a DefaultParse object, so it can be used in coref
            DefaultParse sentParseInd = new DefaultParse(sentParse, sentIdx);
            // get all mentions in the parsed sentence
            Mention[] sentMentions = corefLinker.getMentionFinder().getMentions(sentParseInd);

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
                JsonObj ann = container.newAnnotation(view);
//                String id = "m_"+start+"_"+end;
                men.setId(mIdx++);
                container.setId(ann, "m" + men.getId());
                container.setType(ann, "http://vocab.lappsgrid.org/Markable");
                container.setStart(ann, start);
                container.setEnd(ann,  end);
                JsonArr targets = new JsonArr();
                for(opennlp.tools.coref.mention.Parse token : men.getParse().getTokens()) {
                    targets.put("tok_"+sentIdx+"_" + token.getSpan().getStart());
                }
                container.setFeature(ann, "targets", targets);
                container.setFeature(ann, "words", men.getParse().toString());
                container.setFeature(ann, "sentenceIndex", sentIdx);
                // json.setFeature(ann, "ENTITY_MENTION_TYPE", sentMentions[mIdx].getParse().getEntityType());
            }
            mentions.addAll(Arrays.asList(sentMentions));
        }
        int cntCof = 0;
        if (mentions.size() > 0) {
            // this was for treebank linker, but I'm using DefaultLinker....
            DiscourseEntity[] entities = corefLinker.getEntities(mentions.toArray(new Mention[mentions.size()]));
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
                    JsonObj ann =  container.newAnnotation(view, Uri.COREF);
                    container.setId(ann, "coref"+cntCof++);
                    container.setFeature(ann, "mentions", mentions);
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
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    private String[] getWordTokens(View view, int sentIdx, Span sentSpan, String sentText, Parse[] posNodes) {
        String[] tokens = new String[posNodes.length];

        // get word tokens, getting ready to pass to ner
        for (int posIdx = 0; posIdx < posNodes.length; posIdx ++) {
            Span posSpan = posNodes[posIdx].getSpan();
            int posStart = posSpan.getStart();
            int posEnd = posSpan.getEnd();
            tokens[posIdx] = sentText.substring(posStart, posEnd);
            // krim: isn't posNodes[i].getText() a token text?
//                tokens[i] = posNodes[i].getText().substring(posStart, posEnd);
            String id = "tok_" + sentIdx + "_" + posIdx;
            Annotation ann = view.newAnnotation(id, Uri.TOKEN,
                    sentSpan.getStart() + posStart, sentSpan.getStart() + posEnd);
            ann.addFeature("word", tokens[posIdx]);
            // krim: why do we need POS tags in LIF?
            ann.addFeature(Features.Token.POS, posNodes[posIdx].getType());
//                container.setFeature(tokAnn,"pos", posNodes[posIdx].getType());
        }
        return tokens;
    }

    private Parse createTerminalNodes(final String sentenceText, final Span[] sentenceTokens) {
        Parse sentParse = new Parse(sentenceText, new Span(0, sentenceText.length()), AbstractBottomUpParser.INC_NODE, 1, 0);
        for (int i = 0; i < sentenceTokens.length; i++) {
            int tokenStart = sentenceTokens[i].getStart();
            int tokenEnd = sentenceTokens[i].getEnd();

            // flesh out the parse with token sub-parses
            sentParse.insert(new Parse(sentenceText, new Span(tokenStart, tokenEnd),
                    AbstractBottomUpParser.TOK_NODE, 1, i));
        }
        return sentParse;
    }
}
