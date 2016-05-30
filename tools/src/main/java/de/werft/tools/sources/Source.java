package de.werft.tools.sources;

import java.io.InputStream;


/**
 * The Interface Source.
 * The interfaces is designed to be a two way connection to a single tool.
 * Each class implementing this interfaces holds the contract that it receives or imports
 * data from that tool, also as exporting data to that tool.
 * <br>
 * There are classes that break this rule to show something on the terminal and gets nothing.
 * <br>
 */
public interface Source {

	/**
	 * Provides an input stream from a tool. a.k.a import.
	 *
	 * @param source e.g. api method name or else.
	 * @return the input stream or null if we can't construct a working stream.
	 */
	InputStream get(String source);
}
