package de.werft.tools.sources;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class AbstractSource implements Source {

	private final static Logger L = LogManager.getLogger(AbstractSource.class);

	@Override
	public InputStream get(String source) {
		try {
			return new FileInputStream(source);
		} catch (FileNotFoundException e) {
			L.error(source + " is not reachable. " + e.getMessage());
		}
		return null;
	}

	@Override
	public boolean send(String content) {
		// TODO Auto-generated method stub
		return false;
	}	
}
