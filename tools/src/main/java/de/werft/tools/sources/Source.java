package de.werft.tools.sources;

import java.io.InputStream;


/**
 * The Interface Source.
 * The interfaces is designed to be a two way connection to a single tool.
 * Each class implementing this interfaces holds the contract that it recives or imports
 * data from that tool, also as exporting data to that tool.
 * <br>
 * There are classes that break this rule to show something on the terminal and gets nothing.<br>
 * Please the the individual classes for details.
 */
public interface Source {
	
	/**
	 * Provides an input stream from a tool. a.k.a import.
	 *
	 * @param source the source
	 * @return the input stream or null if we can't construct a working stream.
	 */
	public InputStream get(String source);
	
	/**
	 * Sends the results to a tool. a.k.a. export
	 *
	 * @param content the content
	 * @return true, if successful
	 */
	public boolean send(String content);
}
