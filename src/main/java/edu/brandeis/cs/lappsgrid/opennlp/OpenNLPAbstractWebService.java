package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.api.opennlp.IVersion;
import opennlp.tools.coref.DefaultLinker;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.*;
import opennlp.tools.parser.Parser;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.anc.lapps.serialization.Container;
import org.lappsgrid.api.Data;
import org.lappsgrid.api.LappsException;
import org.lappsgrid.api.WebService;
import org.lappsgrid.discriminator.DiscriminatorRegistry;
import org.lappsgrid.discriminator.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Created by shicq on 3/6/14.
 */
public abstract class OpenNLPAbstractWebService implements WebService , IVersion {
    protected static final Logger logger = LoggerFactory.getLogger(OpenNLPAbstractWebService.class);
    protected Properties prop = new Properties();

    protected static void putFeature(Map mapFeature, String key, Object obj) {
        if (key != null && obj != null) {
            mapFeature.put(key, obj.toString());
        }
    }

    public static final Container getContainer(Data input) throws LappsException
    {

        String discriminatorstr = input.getDiscriminator();
        long type = DiscriminatorRegistry.get(discriminatorstr);

        if (type == Types.ERROR) {
            // Data objects with an ERROR discriminator should not be
            // passed in.
            throw new LappsException(input.getPayload());
        }
        else if (type == Types.TEXT) {
            Container container = new Container();
            container.setText(input.getPayload());
            return container;
        }
        else if (type == Types.JSON) {
            return new Container(input.getPayload());
        }
        String typeName = DiscriminatorRegistry.getUri(type);
        throw new LappsException("Unexpected Data object type: " + typeName);
    }




    protected void init() throws OpenNLPWebServiceException {
        logger.info("init(): load OpenNLP Properties  ...");
        InputStream stream = this.getClass().getResourceAsStream("/" + "opennlp-web-service.properties");
        if (stream == null) {
            logger.error("init(): fail to open \"opennlp-web-service.properties\".");
            throw new OpenNLPWebServiceException("init(): fail to open \"opennlp-web-service.properties\".");
        }
        try {
            prop.load(stream);
            stream.close();
        } catch (IOException e) {
            logger.error("init(): fail to load \"opennlp-web-service.properties\".");
            throw new OpenNLPWebServiceException("init(): fail to load \"opennlp-web-service.properties\".");
        }
    }


    protected Parse createSentenceParse(final String sentenceText, final Span[] sentenceTokens) {
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

    /**
     * loadSentenceModel("Sentence-Detector")
     * @param modelName
     * @return
     */
    protected SentenceDetectorME loadSentenceDetector(String modelName) throws  OpenNLPWebServiceException {
        SentenceDetectorME sentenceDetector = null;
        // default English
        String sentenceModel = prop.getProperty(modelName, "en-sent.bin");
        logger.info("init(): load " + sentenceModel);
        InputStream stream = this.getClass().getResourceAsStream("/" + sentenceModel);
        if (stream == null) {
            logger.error("init(): fail to open SENTENCE MODEl \""+sentenceModel+"\".");
            throw new OpenNLPWebServiceException("init(): fail to open SENTENCE MODEl \""+sentenceModel+"\".");
        }
        logger.info("init(): load SENTENCE MODEl \""+sentenceModel+"\"");
        try {
            try {
                SentenceModel model = new SentenceModel(stream);
                sentenceDetector = new SentenceDetectorME(model);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            logger.error("init(): fail to load SENTENCE MODEl \""+sentenceModel+"\".");
            throw new OpenNLPWebServiceException("init(): fail to load SENTENCE MODEl \""+sentenceModel+"\".");
        }
        return sentenceDetector;
    }

    protected TokenizerME loadTokenizer(String modelName)  throws  OpenNLPWebServiceException {
        TokenizerME tokenizer = null;
        // default English
        String tokenModel = prop.getProperty(modelName, "en-token.bin");

        logger.info("init(): load " + tokenModel);
        InputStream stream = this.getClass().getResourceAsStream("/" + tokenModel);
        if (stream == null) {
            logger.error("init(): fail to open TOKEN MODEl \""+tokenModel+"\".");
            throw new OpenNLPWebServiceException("init(): fail to open TOKEN MODEl \""+tokenModel+"\".");
        }
        logger.info("init(): load TOKEN MODEl \""+tokenModel+"\"");
        try {
            try {
                TokenizerModel model = new TokenizerModel(stream);
                tokenizer = new TokenizerME(model);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            logger.error("init(): fail to load TOKEN MODEl \""+tokenModel+"\".");
            throw new OpenNLPWebServiceException("init(): fail to load TOKEN MODEl \""+tokenModel+"\".");
        }
        logger.info("init(): Creating OpenNLP Tokenizer!");
        return tokenizer;
    }

    protected TokenNameFinder loadTokenNameFinder(String modelName)  throws  OpenNLPWebServiceException {
        TokenNameFinder nameFinder;
        InputStream stream = this.getClass().getResourceAsStream("/" + modelName);
        if (stream == null) {
            logger.error("load(): fail to open NER MODEL \"" + modelName
                    + "\".");
            throw new OpenNLPWebServiceException(
                    "load(): fail to open NER MODEL \"" + modelName + "\".");
        }
        logger.info("load(): load NER MODEL \"" + modelName + "\"");

        try {
            try {
                TokenNameFinderModel model = new TokenNameFinderModel(stream);
                nameFinder = new NameFinderME(model);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            logger.error("load(): fail to load NER MODEL \"" + modelName
                    + "\".");
            throw new OpenNLPWebServiceException(
                    "load(): fail to load NER MODEL \"" + modelName + "\".");
        }
        return nameFinder;
    }

    protected static final String [] NERTags = new String[]{"Person", "Date", "Location", "Organization"};

    protected Map<String, TokenNameFinder> loadTokenNameFinders(String modelName) throws  OpenNLPWebServiceException  {
        Map<String, TokenNameFinder> nameFinders = new HashMap<String,TokenNameFinder> ();
        String nerModels = prop.getProperty(modelName,
                "en-ner-person.bin");
        logger.info("init(): load opennlp-web-service.properties.");
        for (String nerModel : nerModels.split(":")) {
            logger.info("init(): load " + nerModel + " ...");
            if (nerModel.trim().length() > 0) {
                TokenNameFinder nameFinder = loadTokenNameFinder(nerModel);
                if (nameFinder != null){
                    String lowNerModel = nerModel.toLowerCase();
                    String nerModelTag = "Unknown";
                    for (String tag: NERTags) {
                        if(lowNerModel.contains(tag.toLowerCase())) {
                            nerModelTag = tag;
                            nameFinders.put(nerModelTag,nameFinder);
                        }
                    }
                }
            }
        }
        logger.info("init(): Creating OpenNLP NamedEntityRecognizer!");
        return nameFinders;
    }


    protected opennlp.tools.parser.Parser loadParser(String modelName) throws  OpenNLPWebServiceException {
        opennlp.tools.parser.Parser parser = null;
        // default English
        String parserModel = prop.getProperty(modelName,
                "en-parser-chunking.bin");

        logger.info("init(): load opennlp-web-service.properties.");
        InputStream stream = this.getClass().getResourceAsStream("/" + parserModel);
        if (stream == null) {
            logger.error("init(): fail to open PARSER MODEl \"" + parserModel
                    + "\".");
            throw new OpenNLPWebServiceException(
                    "init(): fail to open PARSER MODEl \"" + parserModel + "\".");
        }

        logger.info("init(): load PARSER MODEl \"" + parserModel + "\"");

        try {
            try {
                ParserModel model = new ParserModel(stream);
                parser = ParserFactory.create(model);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            logger.error("init(): fail to load PARSER MODEl \"" + parserModel
                    + "\".");
            throw new OpenNLPWebServiceException(
                    "init(): fail to load PARSER MODEl \"" + parserModel + "\".");
        }

        logger.info("init(): Creating OpenNLP Parser!");
        return parser;
    }

    protected Linker loadCoRefLinker(String modelName) throws  OpenNLPWebServiceException  {
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
        String linkerModel = prop.getProperty(modelName, "coref");
        logger.info("init(): load opennlp-web-service.properties.");
        try {
            linker = new DefaultLinker(this.getClass().getResource("/" + linkerModel).getPath(), LinkerMode.TEST);
        } catch (IOException e) {
            logger.error("init(): fail to load Linker \"" + linkerModel
                    + "\".");
            throw new OpenNLPWebServiceException(
                    "init(): fail to load Linker \"" + linkerModel + "\".");
        }
        logger.info("init(): Creating OpenNLP coreference!");
        return linker;
    }

}
