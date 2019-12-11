package edu.brandeis.lapps.opennlp;

import edu.brandeis.lapps.BrandeisService;
import edu.brandeis.lapps.BrandeisServiceException;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.model.BaseModel;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
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

public abstract class OpenNLPAbstractWebService extends BrandeisService {
    protected static final Properties MODELS = new Properties();
    protected static final Logger logger = LoggerFactory.getLogger(OpenNLPAbstractWebService.class);
    public static final String MODEL_PROP_FILENAME = "/models.properties";
    protected static final String PRODUCER_ALIAS = "opennlp";

    // NOTE: models can be static, but the actual NameFinders cannot be static,
    // because they are not thread safe.
    static final List<TokenNameFinderModel> nameFinderModels = new LinkedList<>();
    static SentenceModel sentenceDetectorModel;
    static POSModel posModel;
    static ParserModel parserModel;
    static TokenizerModel tokenizerModel;

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
        DEFAULT_MODEL_RES_FILE_MAP.put(NamedEntityRecognizer.class, "/en-ner-person.bin:/en-ner-location.bin:/en-ner-organization.bin:/en-ner-date.bin");
        DEFAULT_MODEL_RES_FILE_MAP.put(Parser.class, "/en-parser-chunking.bin");
//        DEFAULT_MODEL_RES_FILE_MAP.put(Coreference.class, "/coref");
        DEFAULT_MODEL_RES_FILE_MAP.put(POSTagger.class, "/en-pos-maxent.bin");
    }

    private void loadModelPaths() throws BrandeisServiceException {
        if (MODELS.size() == 0) {
            logger.info("Finding paths of pre-trained models.");
            InputStream stream = this.getClass().getResourceAsStream(MODEL_PROP_FILENAME);
            if (stream == null) {
                logger.error("Fail to open \"" + MODEL_PROP_FILENAME + "\".");
                throw new BrandeisServiceException("fail to open \"" + MODEL_PROP_FILENAME + "\".");
            }
            try {
                logger.info("loading " + MODEL_PROP_FILENAME);
                MODELS.load(stream);
                stream.close();
            } catch (IOException e) {
                logger.error("Fail to load \"" + MODEL_PROP_FILENAME + "\".");
                throw new BrandeisServiceException("fail to load \"" + MODEL_PROP_FILENAME + "\".");
            }
        } else {
            logger.info("Found cache of the paths of pre-trained models, contains " + MODELS.size() + " items.");
        }
    }

    protected OpenNLPAbstractWebService() throws BrandeisServiceException {
        this.loadModelPaths();
    }

    protected abstract void loadAnnotators() throws BrandeisServiceException;

    protected BrandeisServiceException modelFails(String modelName, String modelResName, Throwable e) {
        String error = String.format("Failed to open %s MODEL \"%s\".", modelName, modelResName);
        logger.error(error);
        logger.error(e.toString());
        return new BrandeisServiceException(error);
    }

    void loadSentenceModel() throws BrandeisServiceException {
        if (sentenceDetectorModel == null) {
            String sentenceModelResPath = MODELS.getProperty(
                    MODEL_PROP_KEY_MAP.get(Splitter.class),
                    DEFAULT_MODEL_RES_FILE_MAP.get(Splitter.class));
            sentenceDetectorModel =(SentenceModel) loadBinaryModel(
                    "SENTENCE", sentenceModelResPath, SentenceModel.class);
        }
    }

    void loadPOSModel() throws BrandeisServiceException {
        if (posModel == null) {
            String posModelResPath = MODELS.getProperty(
                    MODEL_PROP_KEY_MAP.get(POSTagger.class),
                    DEFAULT_MODEL_RES_FILE_MAP.get(POSTagger.class));
            posModel = (POSModel) loadBinaryModel(
                    "POSTAGGER", posModelResPath, POSModel.class);
        }
    }

    void loadParserModel() throws BrandeisServiceException {
        if (parserModel == null) {
            String syntacticModelResPath = MODELS.getProperty(
                    MODEL_PROP_KEY_MAP.get(Parser.class),
                    DEFAULT_MODEL_RES_FILE_MAP.get(Parser.class));
            parserModel = (ParserModel) loadBinaryModel(
                    "PARSER", syntacticModelResPath, ParserModel.class);
        }
    }

    void loadTokenizerModel() throws BrandeisServiceException {
        if (tokenizerModel == null) {
            String tokenModelResPath = MODELS.getProperty(
                    MODEL_PROP_KEY_MAP.get(Tokenizer.class),
                    DEFAULT_MODEL_RES_FILE_MAP.get(Tokenizer.class));
            tokenizerModel = (TokenizerModel) loadBinaryModel(
                    "TOKEN", tokenModelResPath, TokenizerModel.class);
        }
    }

    void loadNameFinderModels() throws BrandeisServiceException {
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

    BaseModel loadBinaryModel(String modelName, String modelResPath, Class modelClass) throws BrandeisServiceException {
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

    String getTokenText(Annotation token, String fullText) {
        String tokenText;
        if (token.getFeatures().containsKey("word")) {
            tokenText = token.getFeature("word");
        } else {
            tokenText = fullText.substring(token.getStart().intValue(),  token.getEnd().intValue());
        }
        return tokenText;
    }

    protected abstract String execute(Container in) throws BrandeisServiceException;

    protected abstract ServiceMetadata loadMetadata();

    @Override
    protected ServiceMetadata setDefaultMetadata() {
        ServiceMetadata metadata = super.setDefaultMetadata();
        IOSpecification required = new IOSpecification();
        required.addLanguage("en");
        required.setEncoding("UTF-8");
        required.addFormat(Discriminators.Uri.LIF);
        metadata.setRequires(required);

        IOSpecification produces = new IOSpecification();
        produces.addLanguage("en");
        produces.setEncoding("UTF-8");
        produces.addFormat(Discriminators.Uri.LIF);
        metadata.setProduces(produces);

        return metadata;

    }
}
