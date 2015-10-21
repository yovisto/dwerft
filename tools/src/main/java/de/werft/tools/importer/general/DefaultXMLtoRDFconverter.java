package de.werft.tools.importer.general;

import java.io.InputStream;

public class DefaultXMLtoRDFconverter extends AbstractXMLtoRDFconverter {

	public DefaultXMLtoRDFconverter(InputStream ontologyFileName,
			String ontologyFormat, String mappingsFilename) {
		super(ontologyFileName, ontologyFormat, mappingsFilename);
	}

	@Override
	public void processingBeforeConvert() {
	}

	@Override
	public void processingAfterConvert() {
	}

}
