package edu.brandeis.lapps.opennlp;

import edu.brandeis.lapps.BrandeisServiceException;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.tokenize.TokenizerME;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Parser extends AbstractOpennlpWrapper {
    private static String[] MODEL_NAME = new String[]{
            DEFAULT_MODEL_RES_FILE_MAP.get(Tokenizer.class),    // for tokenization
            DEFAULT_MODEL_RES_FILE_MAP.get(POSTagger.class),
            DEFAULT_MODEL_RES_FILE_MAP.get(Parser.class)        // for parsing
    };
    private static String TOOL_DESCRIPTION = String.format("This service is a wrapper around Apache OpenNLP %s " +
                    "providing an English constituent parser service. Internally it uses public OpenNLP-1.5 models " +
                    "(available at http://opennlp.sourceforge.net/models-1.5/), in particular, \"%s\" is used.",
                    getWrappeeVersion(), Arrays.toString(MODEL_NAME));
    protected static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private opennlp.tools.tokenize.Tokenizer tokenizer;
    private opennlp.tools.parser.Parser parser;

    protected Parser() throws BrandeisServiceException {
        loadAnnotators();
    }

    @Override
    synchronized protected void loadAnnotators() throws BrandeisServiceException {
        super.loadParserModel();
        parser = ParserFactory.create(parserModel);
        super.loadTokenizerModel();
        tokenizer = new TokenizerME(tokenizerModel);
    }

    private String buildPennString(Parse[] parses) {
        StringBuffer builder = new StringBuffer();
        for (Parse parse : parses) {
            parse.show(builder);
            builder.append("\n");
        }
        return builder.toString();
    }


    /* parse() is only used in test suite to see the parsing results */
    public String parse(String sentence) {
        StringBuffer builder = new StringBuffer();
        Parse[] parses = ParserTool.parseLine(sentence, parser, 1);
        System.out.println(" parses.length = " + parses.length);
        for (Parse parse : parses) {
            parse.show(builder);
            builder.append("\n");
        }
        return builder.toString();
    }


    @Override
    public String processPayload(Container container) throws BrandeisServiceException {
        logger.info("Executing");
        String txt = container.getText();
        List<View> sentViews = container.findViewsThatContain(Uri.SENTENCE);

        // throw exception here, the outer execute method will wrap it into a LEDS
        if (sentViews.size() == 0) {
            throw new BrandeisServiceException(unmetRequirements(Uri.SENTENCE));
        }
        View sentView = sentViews.get(sentViews.size() - 1);
        List<Annotation> sentAnns = sentView.getAnnotations();

        View view = container.newView();
        setUpContainsMetadata(view, PRODUCER_ALIAS);

        for (int sid = 0; sid < sentAnns.size(); sid++) {
            // for each sentence
            Annotation sentAnn = sentAnns.get(sid);
            String sentText = getTokenText(sentAnn, txt);
            String[] tokens = tokenizer.tokenize(sentText);
            Parse[] parses = ParserTool.parseLine(String.join(" ", tokens), parser, 1);

            Annotation ps = view.newAnnotation(PS_ID + sid, Uri.PHRASE_STRUCTURE,
                    sentAnn.getStart(), sentAnn.getEnd());
            ps.addFeature("sentence", sentText);
            ps.addFeature("penntree", buildPennString(parses));
            List<String> tokenIds = new LinkedList<>();
            List<String> constituentIds = new LinkedList<>();
            for (Parse parse : parses) {
                findConstituents(parse, tokenIds, constituentIds, sid, view, null);
            }
            ps.addFeature(Features.PhraseStructure.CONSTITUENTS, constituentIds);
        }
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }


    protected String findConstituents(Parse parse, List<String> tokenIds, List<String> constituentIds, int sentId, View view, String parentId) {
        boolean isLeaf = parse.getType().equals(AbstractBottomUpParser.TOK_NODE);
        if (isLeaf) {
            String tid = TOKEN_ID + sentId + "_" + tokenIds.size();
            tokenIds.add(tid);
            Annotation tokenAnn = view.newAnnotation(tid, Uri.TOKEN, parse.getSpan().getStart(), parse.getSpan().getEnd());
            tokenAnn.addFeature(Features.Token.WORD, parse.getCoveredText());
            return tid;
        } else {
            String cid = CONSTITUENT_ID + sentId + "_" + constituentIds.size();
            constituentIds.add(cid);
            Annotation constituentAnn = view.newAnnotation(cid, Uri.CONSTITUENT, -1, -1);
            constituentAnn.setLabel(parse.getLabel());
            constituentAnn.addFeature(Features.Constituent.LABEL, parse.getType());
            List<String> children = new LinkedList<>();
            for (Parse child : parse.getChildren()) {
                String childId = findConstituents(child, tokenIds, constituentIds, sentId, view, cid);
                children.add(childId);
            }
            constituentAnn.addFeature("children", children);
            if (parentId != null) {
                constituentAnn.addFeature(Features.Constituent.PARENT, parentId);
            }
            return cid;
        }
    }

    public ServiceMetadata loadMetadata() {
        ServiceMetadata meta = setDefaultMetadata();
        meta.setDescription(TOOL_DESCRIPTION);
        meta.getRequires().addAnnotation(Uri.SENTENCE);
        meta.getProduces().addAnnotation(Uri.TOKEN);
        meta.getProduces().addAnnotation(Uri.CONSTITUENT);
        meta.getProduces().addAnnotation(Uri.PHRASE_STRUCTURE);
        meta.getProduces().addTagSet(Uri.PHRASE_STRUCTURE, Uri.TAGS_CAT_PENNTB);
        return meta;

    }
}

