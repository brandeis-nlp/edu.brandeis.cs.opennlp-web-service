package edu.brandeis.cs.lappsgrid.opennlp;

import edu.brandeis.cs.lappsgrid.opennlp.api.IPOSTagger;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.util.Sequence;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

import java.util.List;

/**
 * <i>POSTagger.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p><a href="http://opennlp.sourceforge.net/models-1.5/">Models for 1.5 series</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 *
 */
public class POSTagger extends OpenNLPAbstractWebService implements IPOSTagger  {

    private static POSModel posModel;
    private opennlp.tools.postag.POSTagger postagger;


    public POSTagger() throws OpenNLPWebServiceException {
        loadModels();
        this.metadata = loadMetadata();
    }

    @Override
    synchronized protected void loadModels() throws OpenNLPWebServiceException {
        super.loadModels();
        if (posModel == null) {
            posModel = loadPOSModel(registModelMap.get(getClass()));
        }
        postagger = new POSTaggerME(posModel);
    }

    @Override
    public String[] tag(String[] sentence) {
        if (postagger == null) {
            try {
                loadModels();
            } catch (OpenNLPWebServiceException e) {
                throw new RuntimeException("tokenize(): Fail to initialize POSTagger", e);
            }
        }
        String tags[] = postagger.tag(sentence);
        return tags;
    }


    @Override
    public Sequence[] topKSequences(String[] sentence) {
        if (postagger == null) {
            try {
                loadModels();
            } catch (OpenNLPWebServiceException e) {
                throw new RuntimeException("tokenize(): Fail to initialize POSTagger", e);
            }
        }
        Sequence tags[] = postagger.topKSequences(sentence);
        return tags;
    }

    @Override
    public String execute(Container container) throws OpenNLPWebServiceException {
        logger.info("execute(): Execute OpenNLP tokenizer ...");
        String txt = container.getText();

        List<View> tokenViews = container.findViewsThatContain(Uri.TOKEN);
        if (tokenViews.size() == 0) {
            throw new OpenNLPWebServiceException(String.format(
                    "Wrong Input: CANNOT find %s within previous annotations",
                    Uri.TOKEN));
        }
        List<Annotation> tokenAnns = tokenViews.get(tokenViews.size() - 1).getAnnotations();

        View view = container.newView();
        view.addContains(Uri.POS,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "tagger:opennlp");

        int count = 0;
        if (tokenAnns == null || tokenAnns.size() == 0) {
            // is word.
            if (txt.matches("[a-zA-Z]+")) {
                String [] tags = tag(new String []{txt});
                for(int i = 0; i < tags.length; i++) {
                    Annotation ann = view.newAnnotation(POS_ID + count++,
                            Uri.POS, 0, txt.length());
                    ann.addFeature(Features.Token.POS, tags[i]);
                }
            } else {
                throw new OpenNLPWebServiceException(String.format(
                        "Wrong Input: CANNOT find %s within previous annotations",
                        Uri.TOKEN));
            }
        } else {
            String [] tokens = new String [tokenAnns.size()];
            for (int i = 0; i < tokenAnns.size(); i ++) {
                tokens[i] = getTokenText(tokenAnns.get(i), txt);
            }
            String [] tags = tag(tokens);
            for(int i = 0; i < tags.length; i++) {
                Annotation ann =  view.newAnnotation(POS_ID + count++, Uri.POS,
                        tokenAnns.get(i).getStart(), tokenAnns.get(i).getEnd());
                ann.addFeature(Features.Token.POS, tags[i]);
            }
        }
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }
    
    public String loadMetadata() {
    	ServiceMetadata meta = new ServiceMetadata();
    	meta.setName(this.getClass().getName());
    	meta.setDescription("tagger:opennlp");
    	meta.setVersion(getVersion());
    	meta.setVendor("http://www.cs.brandeis.edu/");
    	meta.setLicense(Uri.APACHE2);
    	
    	IOSpecification requires = new IOSpecification();
    	requires.setEncoding("UTF-8");
    	requires.addLanguage("en");
    	requires.addFormat(Uri.LAPPS);
    	requires.addAnnotation(Uri.TOKEN);
    	
    	IOSpecification produces = new IOSpecification();
    	produces.setEncoding("UTF-8");
    	produces.addLanguage("en");
    	produces.addFormat(Uri.LAPPS);
    	produces.addAnnotation(Uri.POS);
    	
    	meta.setRequires(requires);
    	meta.setProduces(produces);
    	Data<ServiceMetadata> data = new Data<> (Uri.META, meta);
    	return data.asPrettyJson();
    }
    
    public String getMetadata() {
    	return this.metadata;
    }
    
}
