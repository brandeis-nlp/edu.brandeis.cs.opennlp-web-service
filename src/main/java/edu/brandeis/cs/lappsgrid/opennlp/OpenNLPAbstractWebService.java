package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.Version;
import opennlp.tools.coref.DefaultLinker;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import opennlp.tools.util.model.BaseModel;
import org.anc.io.UTF8Reader;
import org.lappsgrid.api.WebService;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by shicq on 3/6/14.
 */

public abstract class OpenNLPAbstractWebService implements WebService {
    protected static final Map<Class, String> registModelMap = new HashMap<Class, String>();
    protected static final Map<String, BaseModel> models = new HashMap<String, BaseModel>();
    protected static final Properties prop = new Properties();
    protected static final Logger logger = LoggerFactory.getLogger(OpenNLPAbstractWebService.class);
    public static final String PropFileName = "opennlp-web-service.properties";

    public static final String TOKEN_ID = "tok_";
    public static final String POS_ID = "pos_";
    public static final String SENT_ID = "sent_";
    public static final String CONSTITUENT_ID = "cs_";
    public static final String PS_ID = "ps_";
    public static final String DEPENDENCY_ID = "dep_";
    public static final String DS_ID = "ds_";
    public static final String MENTION_ID = "m_";
    public static final String COREF_ID = "coref_";
    public static final String NE_ID = "ne_";

    static {
        registModelMap.put(Tokenizer.class, "Tokenizer");
        registModelMap.put(Splitter.class, "Sentence-Detector");
        registModelMap.put(NamedEntityRecognizer.class, "Name-Finder");
        registModelMap.put(Parser.class, "Parser");
//        registModelMap.put(Coreference.class, "Coreference");
        registModelMap.put(POSTagger.class, "Part-of-Speech-Tagger");
    }

    public String getVersion() {
        return Version.getVersion();
    }

    protected void init() throws OpenNLPWebServiceException {
        logger.info("init(): Creating OpenNLP ...");
        InputStream stream = this.getClass().getResourceAsStream("/" + PropFileName);
        if (stream == null) {
            logger.error("init(): fail to open \""+PropFileName+"\".");
            throw new OpenNLPWebServiceException("init(): fail to open \""+PropFileName+"\".");
        }
        try {
            logger.info("init(): load opennlp-web-service.properties.");
            prop.load(stream);
            stream.close();
        } catch (IOException e) {
            logger.error("init(): fail to load \""+PropFileName+"\".");
            throw new OpenNLPWebServiceException("init(): fail to load \""+PropFileName+"\".");
        }
    }

    protected Parse createTerminalNodes(final String sentenceText, final Span[] sentenceTokens) {
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

    private OpenNLPWebServiceException logAndThrowError(String serviceName, String modelResName) {
        String error = String.format("load(): fail to open %s MODEL \"%s\".", serviceName, modelResName);
        logger.error(error);
        return new OpenNLPWebServiceException(error);
    }

    /* used to be loadTokenNameFinders(), but NameFinders cannot be static (and shared among threads) */
    protected List<TokenNameFinderModel> loadTokenNameFinderModels(String nerPropKey) throws  OpenNLPWebServiceException  {
        List<TokenNameFinderModel> nameFinderModels = new LinkedList<>();
        String nerModelResources = prop.getProperty(nerPropKey, "en-ner-person.bin");
        System.out.println(prop.keySet());
        logger.info("init(): load opennlp-web-service.properties.");
        for (String nerModelResName : nerModelResources.split(":")) {
            logger.info("init(): load " + nerModelResName + " ...");
            if (nerModelResName.trim().length() > 0) {
                nameFinderModels.add(loadTokenNameFinderModel(nerModelResName));
            }
        }
        return nameFinderModels;
    }

    protected TokenNameFinderModel loadTokenNameFinderModel(String modelResName) throws OpenNLPWebServiceException {
        TokenNameFinderModel model;
        InputStream stream = this.getClass().getResourceAsStream("/" + modelResName);
        if (stream == null) {
            throw logAndThrowError("NER", modelResName);
        }
        logger.info("load(): load NER MODEL \"" + modelResName + "\"");
        try {
            try {
                model = new TokenNameFinderModel(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw logAndThrowError("NER", modelResName);
        }
        return model;
    }

    protected SentenceModel loadSentenceModel(String splitPropKey) throws  OpenNLPWebServiceException {
        // default English
        String sentenceModel = prop.getProperty(splitPropKey, "en-sent.bin");
        logger.info("init(): load " + sentenceModel);
        InputStream stream = this.getClass().getResourceAsStream("/" + sentenceModel);
        if (stream == null) {
            throw logAndThrowError("SENTENCE", sentenceModel);
        }
        logger.info("init(): load SENTENCE MODEl \""+sentenceModel+"\"");
        try {
            try {
                return new SentenceModel(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw logAndThrowError("SENTENCE", sentenceModel);
        }
    }

    protected TokenizerModel loadTokenizerModel(String tokPropKey)  throws  OpenNLPWebServiceException {
        String tokenModel = prop.getProperty(tokPropKey, "en-token.bin");

        logger.info("init(): load " + tokenModel);
        InputStream stream = this.getClass().getResourceAsStream("/" + tokenModel);
        if (stream == null) {
            throw logAndThrowError("TOKEN", tokenModel);
        }
        logger.info("init(): load TOKEN MODEl \""+tokenModel+"\"");
        try {
            try {
                return new TokenizerModel(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw logAndThrowError("TOKEN", tokenModel);
        }
    }

    protected POSModel loadPOSModel(String posPropKey) throws OpenNLPWebServiceException {

        String taggerModel = prop.getProperty(posPropKey);
        InputStream stream = this.getClass().getResourceAsStream("/" + taggerModel);
        if (stream == null) {
            throw logAndThrowError("POSTAGGER", taggerModel);
        }

        logger.info("init(): load POSTAGGER MODEl \""+taggerModel+"\"");

        try {
            try {
                return new POSModel(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw logAndThrowError("POSTAGGER", taggerModel);
        }
    }

    protected ParserModel loadParserModel(String pspPropKey) throws  OpenNLPWebServiceException {
        String parserModel = prop.getProperty(pspPropKey, "en-parser-chunking.bin");

        logger.info("init(): load opennlp-web-service.properties.");
        InputStream stream = this.getClass().getResourceAsStream("/" + parserModel);
        if (stream == null) {
            throw logAndThrowError("PARSER", parserModel);
        }
        logger.info("init(): load PARSER MODEl \"" + parserModel + "\"");

        try {
            try {
                return new ParserModel(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw logAndThrowError("PARSER", parserModel);
        }
    }

    protected Linker loadCoRefLinker(String corefPropKey) throws  OpenNLPWebServiceException  {
        Linker linker = null;
        logger.info("init(): Creating OpenNLP Coreference ...");
        try {
            String wordnetPath = new File(new File(
                    this.getClass().getResource("/wordnet").toURI()), "3.1/dict").getAbsolutePath();
            System.setProperty("WNSEARCHDIR", wordnetPath);
        }catch(Exception e){
            throw new OpenNLPWebServiceException("Load wordnet 3.1 Error. ");
        }
        // default English
        String linkerModel = prop.getProperty(corefPropKey, "coref");
        logger.info("init(): load opennlp-web-service.properties.");
        try {
            linker = new DefaultLinker( new File(
                    this.getClass().getResource("/" + linkerModel).toURI()).getAbsolutePath(),
                    LinkerMode.TEST);
        } catch (Exception e) {
            logger.error("init(): fail to load Linker \"" + linkerModel
                    + "\".");
            throw new OpenNLPWebServiceException(
                    "init(): fail to load Linker \"" + linkerModel + "\".");
        }
        logger.info("init(): Creating OpenNLP coreference!");
        return linker;
    }

    @Override
    public String execute(String input) {

        if (input == null) return null;

        input = input.trim();
        // in case of Json
        Data data;
        if(input.startsWith("{") && input.endsWith("}")) {
            data = Serializer.parse(input, Data.class);
        } else {
            // when json parse failed
            data = new Data();
            data.setDiscriminator(Discriminators.Uri.TEXT);
            data.setPayload(input);
        }

        final String discriminator = data.getDiscriminator();
        Container container;

        switch (discriminator) {
            case Discriminators.Uri.ERROR:
                return input;
            case Discriminators.Uri.JSON_LD:
            case Discriminators.Uri.LIF:
                container = new Container((Map) data.getPayload());
                break;
            case Discriminators.Uri.TEXT:
                container = new Container();
                container.setText((String) data.getPayload());
                container.setLanguage("en");
                break;
            default:
                String message = String.format
                        ("Unsupported discriminator type: %s", discriminator);
                return new Data<>(Discriminators.Uri.ERROR, message).asJson();
        }

        try {
            // TODO: 12/4/2016 this will be redundant when @context stuff sorted out
            container.setContext(Container.REMOTE_CONTEXT);
            return execute(container);
        } catch (Throwable th) {
            th.printStackTrace();
            String message =
                    String.format("Error processing input: %s", th.toString());
            return new Data<>(Discriminators.Uri.ERROR, message).asJson();
        }
    }

    @Override
    public String getMetadata() {

        String metadata;
        // get caller name using reflection
        String serviceName = this.getClass().getName();
        String resName = "/metadata/"+ serviceName +".json";
        logger.info("load resources:" + resName);

        InputStream inputStream = this.getClass().getResourceAsStream(resName);

        if (inputStream == null) {
            String message = "Unable to load metadata file for " + this.getClass().getName();
            logger.error(message);
            metadata = (new Data<>(Discriminators.Uri.ERROR, message)).asPrettyJson();
        } else {
            UTF8Reader reader;
            try {
                reader = new UTF8Reader(inputStream);
                Scanner s = new Scanner(reader).useDelimiter("\\A");
                String metadataText = s.hasNext() ? s.next() : "";
                metadata = (new Data<>(Discriminators.Uri.META,
                        Serializer.parse(metadataText, ServiceMetadata.class))).asPrettyJson();
                reader.close();
            } catch (Exception e) {
                String message = "Unable to parse metadata json for " + this.getClass().getName();
                logger.error(message, e);
                metadata = (new Data<>(Discriminators.Uri.ERROR, message)).asPrettyJson();
            }
        }
        return metadata;
    }

    protected String getTokenText(Annotation token, String fullText) {
        String tokenText;
        if (token.getFeatures().containsKey("word")) {
            tokenText = token.getFeature("word");
        } else {
            tokenText = fullText.substring(token.getStart().intValue(),  token.getEnd().intValue());
        }
        return tokenText;
    }

    public abstract String execute(Container in) throws OpenNLPWebServiceException;
}
