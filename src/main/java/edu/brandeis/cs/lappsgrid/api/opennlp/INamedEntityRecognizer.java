package edu.brandeis.cs.lappsgrid.api.opennlp;

import opennlp.tools.util.Span;

/**
 * <i>INamedEntityRecognizer.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> <a href="http://opennlp.apache.org/documentation/manual/opennlp.html#tools.namefind.recognition">Named Entity Recognition</a>
 * <p> 
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public interface INamedEntityRecognizer  {
	/**
	 *  Generates name tags for the given sequence, typically a sentence, returning token spans for any identified names.
	 * 
	 * @see opennlp.tools.namefind.TokenNameFinder
	 */
	public Span[] find(String[] tokens) ;
}
