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

import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.LifException;
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
        loadAnnotators();
        this.metadata = loadMetadata();
    }

    @Override
    protected void loadAnnotators() throws OpenNLPWebServiceException {

        corefLinker = loadCoRefLinker();
//        super.loadTokenizerModel();
//        super.loadSentenceModel();
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

        List<View> tokenViews = container.findViewsThatContain(Uri.TOKEN);
        if (tokenViews.size() == 0) {
            throw new OpenNLPWebServiceException(String.format(
                    "Wrong Input: CANNOT find %s within previous annotations",
                    Uri.TOKEN));
        }
        View tokenView = tokenViews.get(tokenViews.size() - 1);
        List<Annotation> tokenAnns = tokenView.getAnnotations();

        List<View> sentViews = container.findViewsThatContain(Uri.SENTENCE);
        if (sentViews.size() == 0) {
            throw new OpenNLPWebServiceException(String.format(
                    "Wrong Input: CANNOT find %s within previous annotations",
                    Uri.SENTENCE));
        }
        List<Annotation> sentAnns = sentViews.get(sentViews.size() - 1).getAnnotations();

        View view = null;
        try {
            view = container.newView();
        } catch (LifException ignored) {
            // newView() throws an error when the given ID already exists
            // , which never be the case when not passing (ID gets generated)
        }
        view.addContains(Uri.COREF,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "coreference:opennlp");
        view.addContains(Uri.MARKABLE,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "markable:opennlp");

        List<Mention> allMentions = new ArrayList<>();
        Map<Annotation, List<Annotation>> sentsAndTokens = nestTokenUnderSents(sentAnns, tokenAnns);

        // from http://blog.dpdearing.com/2012/11/making-coreference-resolution-with-opennlp-1-5-0-your-bitch/
        int sentIdx = 0;
        for (Annotation sent : sentAnns) {
            String sentText = text.substring(sent.getStart().intValue(), sent.getEnd().intValue());
            // TODO: 10/29/17 experiment with ParseTool.parseLine results
            Parse parse = parseSent(sentText, sentsAndTokens.get(sent));
            DefaultParse parseWrapper = new DefaultParse(parse, sentIdx);

            // run linker to get mentions first
            // Parse objects are used later to get chains
            Mention[] mentionsInSent = corefLinker.getMentionFinder().getMentions(parseWrapper);
            for (Mention mention : mentionsInSent) {
                if (mention.getParse() == null) {
                    Parse mentionParse = new Parse(sentText, mention.getSpan(), "NML", 1, 0);
                    parse.insert(mentionParse);
                    mention.setParse(new DefaultParse(mentionParse, sentIdx));
                }

                // now add all mentions as LIF annotations
                int mentionStart = (int) (sent.getStart() + mention.getSpan().getStart());
                int mentionEnd = (int) (sent.getStart() + mention.getSpan().getEnd());
                Annotation mentionAnn = view.newAnnotation(
                        MENTION_ID + mention.getId(), Uri.MARKABLE, mentionStart, mentionEnd);
                mentionAnn.addFeature("words", text.substring(mentionStart, mentionEnd));
                ArrayList<String> targets = new ArrayList<>();
                for (Annotation tokenAnn : sentsAndTokens.get(sent)) {
                    if (tokenAnn.getStart() >= mentionStart) {
                        targets.add(String.format("%s:%s", tokenView.getId(), tokenAnn.getId()));
                    }
                    if (tokenAnn.getEnd() > mentionEnd) {
                        break;
                    }
                }
                mentionAnn.addFeature(Features.Markable.TARGETS, targets);
            }
            allMentions.addAll(Arrays.asList(mentionsInSent));

        }

        int corefId = 0;
        if (allMentions.size() > 0) {
            DiscourseEntity[] chains = corefLinker.getEntities(allMentions.toArray(new Mention[0]));

            for (DiscourseEntity chain : chains) {
                List<String> chainedMentions = new LinkedList<>();
                // opennlp coref does not support getRepresentativeMention() or equivalent
                // so we'll just take the first one as the representative one
                String representativeMentionId = null;
                for (Iterator<MentionContext> chainsIter = chain.getMentions(); chainsIter.hasNext(); ) {
                    Mention mention = chainsIter.next();
                    if (representativeMentionId == null) {
                        representativeMentionId = Integer.toString(mention.getId());
                    }
                    chainedMentions.add((Integer.toString(mention.getId())));
                }
                Annotation corefAnn = view.newAnnotation(COREF_ID + corefId++, Uri.COREF);
                corefAnn.addFeature(Features.Coreference.REPRESENTATIVE, representativeMentionId);
                corefAnn.addFeature(Features.Coreference.MENTIONS, chainedMentions);
            }
        }
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    /**
     * Expects sents and tokens are sorted by the order (ascending)
     * @param sents
     * @param tokens
     * @return
     */
    private Map<Annotation, List<Annotation>> nestTokenUnderSents(List<Annotation> sents, List<Annotation> tokens) {
        int tokenIdx = 0;
        Map<Annotation, List<Annotation>> nested = new HashMap<>();
        for (Annotation sent : sents) {
            List<Annotation> nestedTokens = new LinkedList<>();
            while (tokens.get(tokenIdx).getEnd() <= sent.getEnd()) {
                nestedTokens.add(tokens.get(tokenIdx));
                tokenIdx++;
            }
            nested.put(sent, nestedTokens);
        }
        return nested;
    }

    private Parse parseSent(String sentenceText, Collection<Annotation> tokenAnns) {
        // see http://blog.dpdearing.com/2011/12/how-to-use-the-opennlp-1-5-0-parser/
        Parse p = new Parse(sentenceText,
                new Span(0, sentenceText.length()),
                AbstractBottomUpParser.INC_NODE,
                1, 0);

        int tokenIdx = 0;
        for (Annotation token : tokenAnns) {
            p.insert(new Parse(sentenceText,
                    new Span(token.getStart().intValue(), token.getEnd().intValue()),
                    AbstractBottomUpParser.TOK_NODE,
                    0, tokenIdx++));
        }
        return this.parser.parse(p);
    }

    private String loadMetadata() {
        ServiceMetadata meta = new ServiceMetadata();
        meta.setName(this.getClass().getName());
        meta.setDescription("corefernce:opennlp");
        meta.setVersion(getVersion());
        meta.setVendor("http://www.cs.brandeis.edu/");
        meta.setLicense(Uri.APACHE2);

        IOSpecification requires = new IOSpecification();
        requires.setEncoding("UTF-8");
        requires.addLanguage("en");
        requires.addFormat(Uri.LAPPS);
        requires.addAnnotation(Uri.TOKEN);
        requires.addAnnotation(Uri.SENTENCE);

        IOSpecification produces = new IOSpecification();
        produces.setEncoding("UTF-8");
        produces.addLanguage("en");
        produces.addFormat(Uri.LAPPS);
        produces.addAnnotation(Uri.MARKABLE);
        produces.addAnnotation(Uri.COREF);

        meta.setRequires(requires);
        meta.setProduces(produces);
        Data<ServiceMetadata> data = new Data<>(Uri.META, meta);
        return data.asPrettyJson();
    }
}
