package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.coref.DefaultLinker;
import opennlp.tools.coref.Linker;
import opennlp.tools.coref.LinkerMode;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import opennlp.tools.util.model.BaseModel;
import org.anc.io.UTF8Reader;
import edu.brandeis.cs.lappsgrid.Version;
import org.lappsgrid.api.WebService;
import org.lappsgrid.discriminator.Discriminators;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

/**
 * Created by shicq on 3/6/14.
 */

public abstract class OpenNLPAbstractWebService implements WebService {
    protected static final Map<Class, String> registModelMap = new HashMap<Class, String>();
    protected static final Map<String, BaseModel> models = new HashMap<String, BaseModel>();
    protected static final Properties prop = new Properties();
    protected static final Logger logger = LoggerFactory.getLogger(OpenNLPAbstractWebService.class);
    public static final String PropFileName = "opennlp-web-service.properties";

    static {
        registModelMap.put(Tokenizer.class, "Tokenizer");
        registModelMap.put(Splitter.class, "Sentence-Detector");
        registModelMap.put(NamedEntityRecognizer.class, "Name-Finder");
        registModelMap.put(Parser.class, "Parser");
        registModelMap.put(Coreference.class, "Coreference");
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

//        for(String name:prop.stringPropertyNames()) {
//            if (models.get(name) != null) {
//                continue;
//            }
//
//            stream = this.getClass().getResourceAsStream("/" + name);
//            if (stream == null) {
//                logger.error("init(): fail to open MODEl \""+name+"\".");
//                throw new OpenNLPWebServiceException("init(): fail to open MODEl \""+name+"\".");
//            }
//
//            logger.info("init(): load MODEl \""+name+"\"");
//
//            try {
//                try {
//                    if (name == "Tokenizer")
//                        models.put(name, new TokenizerModel(stream));
//                    if (name == "Sentence-Detector")
//                        models.put(name, new SentenceModel(stream));
//                    if (name == "Name-Finder")
//                        models.put(name, new TokenNameFinderModel(stream));
//                    if (name == "Parser")
//                        models.put(name, new ParserModel(stream));
//                    if (name == "Coreference")
//                        models.put(name, new TokenizerModel(stream));
//                } finally {
//                    stream.close();
//                }
//            } catch (IOException e) {
//                logger.error("init(): fail to load MODEl \""+name+"\".");
//                throw new OpenNLPWebServiceException("init(): fail to load MODEl \""+name+"\".");
//            }
//        }
//        logger.info("init(): Creating OpenNLP!");
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


    protected POSTaggerME loadPOSTagger(String modelName) throws OpenNLPWebServiceException {
        POSTaggerME postagger;

        String taggerModel = prop.getProperty(modelName);
        InputStream stream = this.getClass().getResourceAsStream("/" + taggerModel);
        if (stream == null) {
            logger.error("init(): fail to open POSTAGGER MODEl \""+taggerModel+"\".");
            throw new OpenNLPWebServiceException("init(): fail to open POSTAGGER MODEl \""+taggerModel+"\".");
        }

        logger.info("init(): load POSTAGGER MODEl \""+taggerModel+"\"");

        try {
            try {
                POSModel model = new POSModel(stream);
                postagger = new POSTaggerME(model);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            logger.error("init(): fail to load POSTAGGER MODEl \""+modelName+"\".");
            throw new OpenNLPWebServiceException("init(): fail to load POSTAGGER MODEl \""+modelName+"\".");
        }

        logger.info("init(): Creating OpenNLP POSTagger!");
        return postagger;
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
        Data data = Serializer.parse(input, Data.class);
        if (data == null) {
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

    public abstract String execute(Container in) throws OpenNLPWebServiceException;
}
