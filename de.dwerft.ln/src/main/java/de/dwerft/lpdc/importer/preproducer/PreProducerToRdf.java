package de.dwerft.lpdc.importer.preproducer;

import de.dwerft.lpdc.importer.general.XMLtoRDFconverter;

public class PreProducerToRdf extends XMLtoRDFconverter {

	public PreProducerToRdf(String ontologyFileName, String ontologyFormat,
			String mappingsFilename) {
		super(ontologyFileName, ontologyFormat, mappingsFilename);
	}

	@Override
	public void processingBeforeConvert() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processingAfterConvert() {
		// TODO Auto-generated method stub

	}

}
