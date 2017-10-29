package edu.brandeis.cs.lappsgrid.opennlp;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

/**
 * <i>Tokenizer.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p><a href="http://opennlp.sourceforge.net/models-1.5/">Models for 1.5 series</a>
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public class Tokenizer extends OpenNLPAbstractWebService {

    private opennlp.tools.tokenize.Tokenizer tokenizer;

    public Tokenizer() throws OpenNLPWebServiceException {
        loadAnnotators();
        this.metadata = loadMetadata();
    }

    @Override
    protected void loadAnnotators() throws OpenNLPWebServiceException {
        super.loadTokenizerModel();
        tokenizer = new TokenizerME(tokenizerModel);
    }

    public String[] tokenize(String s) {
        String tokens[] = tokenizer.tokenize(s);
        return tokens;
    }

    public Span[] tokenizePos(String s) {
        Span [] boundaries = tokenizer.tokenizePos(s);
        return boundaries;
    }

    @Override
    public String execute(Container container) throws OpenNLPWebServiceException {
        logger.info("Executing");
        String txt = container.getText();
        View view = container.newView();
        view.addContains(Uri.TOKEN,
                String.format("%s:%s", this.getClass().getName(), getVersion()),
                "tokenizer:opennlp");
        Span[] spans = tokenizePos(txt);
        int count = 0;
        for (Span span : spans) {
            int start = span.getStart();
            int end = span.getEnd();
            Annotation ann = view.newAnnotation(TOKEN_ID + count++,
                    Uri.TOKEN, start, end);
            ann.getFeatures().put("word", txt.substring(start, end));
        }
        Data<Container> data = new Data<>(Uri.LIF, container);
        return Serializer.toJson(data);
    }

    public String loadMetadata() {
    	ServiceMetadata meta = new ServiceMetadata();
    	meta.setName(this.getClass().getName());
    	meta.setDescription("tokenizer:opennlp");
    	meta.setVersion(getVersion());
    	meta.setVendor("http://www.cs.brandeis.edu/");
    	meta.setLicense(Uri.APACHE2);

    	IOSpecification requires = new IOSpecification();
    	requires.setEncoding("UTF-8");
    	requires.addLanguage("en");
    	requires.addFormat(Uri.LAPPS);

    	IOSpecification produces = new IOSpecification();
    	produces.setEncoding("UTF-8");
    	produces.addLanguage("en");
    	produces.addAnnotation(Uri.TOKEN);
    	produces.addFormat(Uri.LAPPS);

    	meta.setRequires(requires);
    	meta.setProduces(produces);
    	Data<ServiceMetadata> data = new Data<> (Uri.META, meta);
    	return data.asPrettyJson();
    }
}
