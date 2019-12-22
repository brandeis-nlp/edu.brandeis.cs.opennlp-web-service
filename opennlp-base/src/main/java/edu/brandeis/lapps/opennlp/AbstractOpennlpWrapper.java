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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class AbstractOpennlpWrapper extends BrandeisService {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractOpennlpWrapper.class);
    protected static final String PRODUCER_ALIAS = "opennlp";

    // NOTE: models can be static, but the actual NameFinders cannot be static,
    // because they are not thread safe.
    static final List<TokenNameFinderModel> nameFinderModels = new LinkedList<>();
    static SentenceModel sentenceDetectorModel;
    static POSModel posModel;
    static ParserModel parserModel;
    static TokenizerModel tokenizerModel;

    protected static final Map<Class, String> DEFAULT_MODEL_RES_FILE_MAP = new HashMap<>();
    static {
        DEFAULT_MODEL_RES_FILE_MAP.put(Tokenizer.class, "en-token.bin");
        DEFAULT_MODEL_RES_FILE_MAP.put(Splitter.class, "en-sent.bin");
        DEFAULT_MODEL_RES_FILE_MAP.put(NamedEntityRecognizer.class, "en-ner-person.bin:en-ner-location.bin:en-ner-organization.bin:en-ner-date.bin");
        DEFAULT_MODEL_RES_FILE_MAP.put(Parser.class, "en-parser-chunking.bin");
//        DEFAULT_MODEL_RES_FILE_MAP.put(Coreference.class, "/coref");
        DEFAULT_MODEL_RES_FILE_MAP.put(POSTagger.class, "en-pos-maxent.bin");
    }

    protected AbstractOpennlpWrapper() {
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
            String sentenceModelResPath = DEFAULT_MODEL_RES_FILE_MAP.get(Splitter.class);
            sentenceDetectorModel =(SentenceModel) loadBinaryModel(
                    "SENTENCE", sentenceModelResPath, SentenceModel.class);
        }
    }

    void loadPOSModel() throws BrandeisServiceException {
        if (posModel == null) {
            String posModelResPath = DEFAULT_MODEL_RES_FILE_MAP.get(POSTagger.class);
            posModel = (POSModel) loadBinaryModel(
                    "POSTAGGER", posModelResPath, POSModel.class);
        }
    }

    void loadParserModel() throws BrandeisServiceException {
        if (parserModel == null) {
            String syntacticModelResPath = DEFAULT_MODEL_RES_FILE_MAP.get(Parser.class);
            parserModel = (ParserModel) loadBinaryModel(
                    "PARSER", syntacticModelResPath, ParserModel.class);
        }
    }

    void loadTokenizerModel() throws BrandeisServiceException {
        if (tokenizerModel == null) {
            String tokenModelResPath = DEFAULT_MODEL_RES_FILE_MAP.get(Tokenizer.class);
            tokenizerModel = (TokenizerModel) loadBinaryModel(
                    "TOKEN", tokenModelResPath, TokenizerModel.class);
        }
    }

    void loadNameFinderModels() throws BrandeisServiceException {
        if (nameFinderModels.size() == 0) {
            String[] neModelsResPaths = DEFAULT_MODEL_RES_FILE_MAP.get(NamedEntityRecognizer.class).split(":");
            for (String neModelResPath : neModelsResPaths) {
                if (neModelResPath.trim().length() > 0) {
                    nameFinderModels.add((TokenNameFinderModel) loadBinaryModel(
                            "NER", neModelResPath, TokenNameFinderModel.class));
                }
            }
        }
    }

    BaseModel loadBinaryModel(String modelName, String modelResPath, Class<? extends BaseModel> modelClass) throws BrandeisServiceException {

        if (! modelResPath.startsWith("/")) {
            modelResPath = "/" + modelResPath;
        }
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

    protected abstract ServiceMetadata loadMetadata();

    @Override
    protected ServiceMetadata setDefaultMetadata() {
        ServiceMetadata metadata = super.setDefaultMetadata();
        metadata.setLicense(Discriminators.Uri.APACHE2);
        metadata.setLicenseDesc("This service provides an interface to a Apache OpenNLP tool, which is originally licensed under Apache License, Version 2.0 . For more information, please visit `the Apache License website <https://www.apache.org/licenses/LICENSE-2.0>`_. ");
        metadata.setAllow(Discriminators.Uri.ALL);

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
