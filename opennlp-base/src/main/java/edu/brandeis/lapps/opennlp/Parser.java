package edu.brandeis.lapps.opennlp;

import edu.brandeis.lapps.BrandeisServiceException;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.ParserFactory;
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

import java.util.LinkedList;
import java.util.List;

public class Parser extends AbstractOpennlpWrapper {
    private static String TOOL_DESCRIPTION = "This service is a wrapper around Apache OpenNLP 1.5.3 providing an English constituent parser service." +
            "\nInternally it uses public OpenNLP-1.5 models (available at http://opennlp.sourceforge.net/models-1.5/), in particular, \n" +
            "\"/en-parser-chunking.bin\" is used. ";
    protected static final Logger logger = LoggerFactory.getLogger(Parser.class);

    private opennlp.tools.parser.Parser parser;

    protected Parser() throws BrandeisServiceException {
        loadAnnotators();
    }

    @Override
    synchronized protected void loadAnnotators() throws BrandeisServiceException {
        super.loadParserModel();
        parser = ParserFactory.create(parserModel);
    }

    private String buildPennString(Parse parses[]) {
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
        Parse parses[] = ParserTool.parseLine(sentence, parser, 1);
        System.out.println(" parses.length = " + parses.length);
        for (int pi = 0, pn = parses.length; pi < pn; pi++) {
            parses[pi].show(builder);
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
            Parse parses[] = ParserTool.parseLine(sentText, parser, 1);

            Annotation ps = view.newAnnotation(PS_ID + sid, Uri.PHRASE_STRUCTURE,
                    sentAnn.getStart(), sentAnn.getEnd());
            ps.addFeature("sentence", sentText);
            ps.addFeature("penntree", buildPennString(parses));
            List<String> constituentIds = new LinkedList<>();
            // TODO: 2/2/2018 leaves are not grounded to token annotations
            for (Parse parse : parses) {
                findConstituents(parse, constituentIds, sid, view);
            }
            ps.getFeatures().put(Features.PhraseStructure.CONSTITUENTS, constituentIds);
        }
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }


    protected String findConstituents(Parse parse, List<String> constituentIds, int sentId, View view) {
        String cid = CONSTITUENT_ID + sentId + "_" + constituentIds.size();
        constituentIds.add(cid);

        Annotation constituentAnn = view.newAnnotation(cid, Uri.CONSTITUENT, -1, -1);

        if (!parse.getType().equals(AbstractBottomUpParser.TOK_NODE)) {
            constituentAnn.setLabel(parse.getType());
        } else {
            constituentAnn.setLabel(parse.getCoveredText());
        }
        if (parse.getChildren().length > 0) {
            List<String> children = new LinkedList<>();
            for (Parse child : parse.getChildren()) {
                String childId = findConstituents(child, constituentIds, sentId, view);
                children.add(childId);
            }
            constituentAnn.getFeatures().put("children", children.toString());
        }
        return cid;
    }

    public ServiceMetadata loadMetadata() {
        ServiceMetadata meta = setDefaultMetadata();
        meta.setDescription(TOOL_DESCRIPTION);
        meta.getRequires().addAnnotation(Uri.SENTENCE);
        meta.getProduces().addAnnotation(Uri.CONSTITUENT);
        meta.getProduces().addAnnotation(Uri.PHRASE_STRUCTURE);
        return meta;

    }
}

