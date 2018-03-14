package edu.brandeis.lapps.opennlp;

import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.model.BaseModel;
import org.lappsgrid.api.WebService;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by shicq on 3/6/14.
 */

public abstract class OpenNLPAbstractWebService implements WebService {
    protected static final Properties MODELS = new Properties();
    protected static final Logger logger = LoggerFactory.getLogger(OpenNLPAbstractWebService.class);
    public static final String MODEL_PROP_FILENAME = "/models.properties";

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
    // NOTE: models can be static, but the actual NameFinders cannot be static,
    // because they are not thread safe.
    static final List<TokenNameFinderModel> nameFinderModels = new LinkedList<>();
    static SentenceModel sentenceDetectorModel;
    static POSModel posModel;
    static ParserModel parserModel;
    static TokenizerModel tokenizerModel;

    protected String metadata;

    protected static final Map<Class, String> MODEL_PROP_KEY_MAP = new HashMap<>();
    static {
        MODEL_PROP_KEY_MAP.put(Tokenizer.class, "Tokenizer");
        MODEL_PROP_KEY_MAP.put(Splitter.class, "Sentence-Detector");
        MODEL_PROP_KEY_MAP.put(NamedEntityRecognizer.class, "Name-Finder");
        MODEL_PROP_KEY_MAP.put(Parser.class, "Parser");
        MODEL_PROP_KEY_MAP.put(Coreference.class, "Coreference");
        MODEL_PROP_KEY_MAP.put(POSTagger.class, "Part-of-Speech-Tagger");
    }

    protected static final Map<Class, String> DEFAULT_MODEL_RES_FILE_MAP = new HashMap<>();
    static {
        DEFAULT_MODEL_RES_FILE_MAP.put(Tokenizer.class, "/en-token.bin");
        DEFAULT_MODEL_RES_FILE_MAP.put(Splitter.class, "/en-sent.bin");
        DEFAULT_MODEL_RES_FILE_MAP.put(NamedEntityRecognizer.class, "/en-ner-person.bin:/en-ner-location.bin:/en-ner-organization.bin:/en-ner-date.bin");
        DEFAULT_MODEL_RES_FILE_MAP.put(Parser.class, "/en-parser-chunking.bin");
        DEFAULT_MODEL_RES_FILE_MAP.put(Coreference.class, "/coref");
        DEFAULT_MODEL_RES_FILE_MAP.put(POSTagger.class, "/en-pos-maxent.bin");
    }

    public String getVersion() {
        String path = "/version.properties";
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            logger.error("version.properties file not found, version is UNKNOWN.");
            return "UNKNOWN";
        }
        Properties properties = new Properties();
        try {
            properties.load(stream);
            stream.close();
            return (String) properties.get("version");
        } catch (IOException e) {
            logger.error("error loading version.properties, version is UNKNOWN.");
            return "UNKNOWN";
        }
    }

    private void loadModelPaths() throws OpenNLPWebServiceException {
        if (MODELS.size() == 0) {
            logger.info("Finding paths of pre-trained models.");
            InputStream stream = this.getClass().getResourceAsStream(MODEL_PROP_FILENAME);
            if (stream == null) {
                logger.error("Fail to open \"" + MODEL_PROP_FILENAME + "\".");
                throw new OpenNLPWebServiceException("fail to open \"" + MODEL_PROP_FILENAME + "\".");
            }
            try {
                logger.info("loading " + MODEL_PROP_FILENAME);
                MODELS.load(stream);
                stream.close();
            } catch (IOException e) {
                logger.error("Fail to load \"" + MODEL_PROP_FILENAME + "\".");
                throw new OpenNLPWebServiceException("fail to load \"" + MODEL_PROP_FILENAME + "\".");
            }
        } else {
            logger.info("Found cache of the paths of pre-trained models, contains " + MODELS.size() + " items.");
        }
    }

    protected OpenNLPAbstractWebService() throws OpenNLPWebServiceException {
        this.loadModelPaths();
    }

    protected abstract void loadAnnotators() throws OpenNLPWebServiceException;

    protected OpenNLPWebServiceException modelFails(String modelName, String modelResName, Throwable e) {
        String error = String.format("Failed to open %s MODEL \"%s\".", modelName, modelResName);
        logger.error(error);
        logger.error(e.toString());
        return new OpenNLPWebServiceException(error);
    }

    void loadSentenceModel() throws OpenNLPWebServiceException {
        if (sentenceDetectorModel == null) {
            String sentenceModelResPath = MODELS.getProperty(
                    MODEL_PROP_KEY_MAP.get(Splitter.class),
                    DEFAULT_MODEL_RES_FILE_MAP.get(Splitter.class));
            sentenceDetectorModel =(SentenceModel) loadBinaryModel(
                    "SENTENCE", sentenceModelResPath, SentenceModel.class);
        }
    }

    void loadPOSModel() throws OpenNLPWebServiceException {
        if (posModel == null) {
            String posModelResPath = MODELS.getProperty(
                    MODEL_PROP_KEY_MAP.get(POSTagger.class),
                    DEFAULT_MODEL_RES_FILE_MAP.get(POSTagger.class));
            posModel = (POSModel) loadBinaryModel(
                    "POSTAGGER", posModelResPath, POSModel.class);
        }
    }

    void loadParserModel() throws OpenNLPWebServiceException {
        if (parserModel == null) {
            String syntacticModelResPath = MODELS.getProperty(
                    MODEL_PROP_KEY_MAP.get(Parser.class),
                    DEFAULT_MODEL_RES_FILE_MAP.get(Parser.class));
            parserModel = (ParserModel) loadBinaryModel(
                    "PARSER", syntacticModelResPath, ParserModel.class);
        }
    }

    void loadTokenizerModel() throws OpenNLPWebServiceException {
        if (tokenizerModel == null) {
            String tokenModelResPath = MODELS.getProperty(
                    MODEL_PROP_KEY_MAP.get(Tokenizer.class),
                    DEFAULT_MODEL_RES_FILE_MAP.get(Tokenizer.class));
            tokenizerModel = (TokenizerModel) loadBinaryModel(
                    "TOKEN", tokenModelResPath, TokenizerModel.class);
        }
    }

    void loadNameFinderModels() throws OpenNLPWebServiceException {
        if (nameFinderModels.size() == 0) {
            String[] neModelsResPaths = MODELS.getProperty(
                    MODEL_PROP_KEY_MAP.get(NamedEntityRecognizer.class),
                    DEFAULT_MODEL_RES_FILE_MAP.get(NamedEntityRecognizer.class)).split(":");
            Arrays.toString(neModelsResPaths);
            for (String neModelResPath : neModelsResPaths) {
                if (neModelResPath.trim().length() > 0) {
                    nameFinderModels.add((TokenNameFinderModel) loadBinaryModel(
                            "NER", neModelResPath, TokenNameFinderModel.class));
                }
            }
        }
    }

    BaseModel loadBinaryModel(String modelName, String modelResPath, Class modelClass) throws OpenNLPWebServiceException {
        this.loadModelPaths();

        logger.info(String.format("Opening a binary model for %s: %s", modelName, modelResPath));
        InputStream stream = this.getClass().getResourceAsStream(modelResPath);
        if (stream == null) {
            throw modelFails(modelName, modelResPath, new FileNotFoundException());
        }

        logger.info(String.format("Loading the model for %s: %s", modelName, modelResPath));

        try {
            try {
                Constructor<? extends BaseModel> constructor = modelClass.getConstructor(InputStream.class);
                return constructor.newInstance(stream);
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
                throw modelFails(modelName, modelResPath, e);
            } catch (NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw modelFails(modelName, modelResPath, e);
        }
        return null;
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
