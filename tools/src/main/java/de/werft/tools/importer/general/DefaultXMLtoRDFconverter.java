package de.werft.tools.importer.general;

public class DefaultXMLtoRDFconverter extends AbstractXMLtoRDFconverter {

	public DefaultXMLtoRDFconverter(String ontologyFileName,
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