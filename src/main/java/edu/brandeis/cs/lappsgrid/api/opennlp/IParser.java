package edu.brandeis.cs.lappsgrid.api.opennlp;

/**
 * <i>IParser.java</i> Language Application Grids (<b>LAPPS</b>)
 * <p> 
 * <p> <a href="http://opennlp.apache.org/documentation/manual/opennlp.html#tools.parser">Parser</a>
 * <p> 
 * <p> 
 *
 * @author Chunqi Shi ( <i>shicq@cs.brandeis.edu</i> )<br>Nov 20, 2013<br>
 * 
 */
public interface IParser{
	public String parse(String sentence);
}
