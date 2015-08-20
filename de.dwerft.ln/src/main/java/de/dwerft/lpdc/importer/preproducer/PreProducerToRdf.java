package de.dwerft.lpdc.importer.preproducer;

import java.util.Set;

import de.dwerft.lpdc.importer.general.MappingDefinition;
import de.dwerft.lpdc.importer.general.XMLtoRDFconverter;

public class PreProducerToRdf extends XMLtoRDFconverter {

	public PreProducerToRdf(String ontologyFileName,
			String ontologyFormat, Set<MappingDefinition> mappings) {
		super(ontologyFileName, ontologyFormat, mappings);
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
