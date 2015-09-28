package de.werft.tools.sources;

import java.io.InputStream;

import de.werft.tools.general.OntologyConstants;
import de.werft.tools.importer.general.OntologyConnector;
import de.werft.tools.importer.general.TripleStoreConnector;

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
