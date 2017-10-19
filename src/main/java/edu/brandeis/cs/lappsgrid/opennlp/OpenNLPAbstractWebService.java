package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.coref.DefaultLinker;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
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

import java.io.File;
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
//        MODEL_PROP_KEY_MAP.put(Coreference.class, "Coreference");
        MODEL_PROP_KEY_MAP.put(POSTagger.class, "Part-of-Speech-Tagger");
    }

    protected static final Map<Class, String> DEFAULT_MODEL_RES_FILE_MAP = new HashMap<>();
    static {
        DEFAULT_MODEL_RES_FILE_MAP.put(Tokenizer.class, "/en-token.bin");
        DEFAULT_MODEL_RES_FILE_MAP.put(Splitter.class, "/en-sent.bin");
        DEFAULT_MODEL_RES_FILE_MAP.put(NamedEntityRecognizer.class, "/en-ner-person.bin:en-ner-location.bin:en-ner-organization.bin:en-ner-date.bin");
        DEFAULT_MODEL_RES_FILE_MAP.put(Parser.class, "/en-parser-chunking.bin");
//        DEFAULT_MODEL_RES_FILE_MAP.put(Coreference.class, "/coref");
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

    protected void loadModels() throws OpenNLPWebServiceException {
        this.loadModelPaths();
    }

    private OpenNLPWebServiceException modelFails(String modelName, String modelResName, Throwable e) {
        String error = String.format("Failed to open %s MODEL \"%s\".", modelName, modelResName);
        logger.error(error);
        logger.error(e.toString());
        return new OpenNLPWebServiceException(error);
    }


    BaseModel loadBinaryModel(String modelName, String modelResPath, Class modelClass) throws OpenNLPWebServiceException {

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

    protected Linker loadCoRefLinker(String corefPropKey) throws  OpenNLPWebServiceException  {
        Linker linker = null;
        logger.info("Creating OpenNLP Coreference ...");
        try {
            String wordnetPath = new File(new File(
                    this.getClass().getResource("/wordnet").toURI()), "3.1/dict").getAbsolutePath();
            System.setProperty("WNSEARCHDIR", wordnetPath);
        }catch(Exception e){
            throw new OpenNLPWebServiceException("Load wordnet 3.1 Error. ");
        }
        // default English
        String model = MODELS.getProperty(corefPropKey, "coref");
        logger.info("Opening " + model);
        try {
            linker = new DefaultLinker( new File(
                    this.getClass().getResource("/" + model).toURI()).getAbsolutePath(),
                    LinkerMode.TEST);
        } catch (Exception e) {
            logger.error("fail to load Linker \"" + model
                    + "\".");
            throw new OpenNLPWebServiceException(
                    "fail to load Linker \"" + model + "\".");
        }
        logger.info("Creating OpenNLP coreference!");
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
