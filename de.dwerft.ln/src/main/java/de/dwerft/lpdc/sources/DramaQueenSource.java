package de.dwerft.lpdc.sources;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class DramaQueenSource implements Source {

	/**
	 * Creates an input stream of the given file name. Assumes that the project file is compressed.
	 * 
	 * @param source File name of the dramaqueen project
	 * @return The input stream
	 */
	@Override
	public InputStream get(String source) {
		return get(source, true);
	}

	/**
	 * Creates an input stream of the given file name. Assumes that the project file is compressed.
	 * 
	 * @param source File name of the dramaqueen project
	 * @param compressed Whether the file is compressed or not
	 * @return The input stream
	 */
	public InputStream get(String source, boolean compressed) {
		
		try {
			if (compressed) {
				ZipFile zip = new ZipFile(source);
				Enumeration<? extends ZipEntry> entries = zip.entries();

				while (entries.hasMoreElements()) {
					ZipEntry ze = entries.nextElement();
					if ("document.xml".equals(ze.getName())) {
						return zip.getInputStream(ze);
					}
				}
			} else {
				return new FileInputStream(source);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean send(String content) {
		return false;
	}

}
