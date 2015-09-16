package de.dwerft.lpdc.sources;

import java.io.InputStream;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

import de.dwerft.lpdc.general.OntologyConstants;
import de.dwerft.lpdc.importer.general.OntologyConnector;
import de.dwerft.lpdc.importer.general.TripleStoreConnector;

public class TripleStoreSource implements Source {

	@Override
	public InputStream get(String source) {
		// TODO Auto-generated method stub
		return null;
	}

	// use as content the ttl file name
	@Override
	public boolean send(String content) {
		OntologyConnector ontoConn = new OntologyConnector(OntologyConstants.ONTOLOGY_FILE, OntologyConstants.ONTOLOGY_FORMAT);
		TripleStoreConnector conn = new TripleStoreConnector(ontoConn, OntologyConstants.SPARQL_ENDPOINT);
		conn.updateEndpoint(content);
		return true;
	}

}
