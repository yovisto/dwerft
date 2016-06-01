package de.werft.tools.sources;

import java.io.InputStream;


/**
 * The class defines a way of recieving data from somewhere.
 * This can be a file a API or a archive.
 *
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
