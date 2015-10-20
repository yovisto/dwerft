package de.werft.tools.importer.general;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * The xml to rdf converter.
 *
 * This class takes first the ontology file and its format.
 * Second the mapping from xml to rdf. For the mapping format see README.md
 *
 */
public abstract class AbstractXMLtoRDFconverter {
	
	/** The Logger. */
	private static final Logger L = Logger.getLogger(AbstractXMLtoRDFconverter.class.getName());
	
	protected OntologyConnector ontConn;
	protected XMLProcessor xmlProc;
	protected Mapper mapper;
	protected RdfProcessor rdfProc;

	/**
	 * The XMLtoRDFconverter is the controller of the OntologyConnector, XMLProcessor,
	 * Mapper, and RdfProcessor.
	 * 
	 * Specific XML converters inherit from this class and implement specific conversion
	 * routines that are execute before and after the general conversion process based
	 * on the mapping definitions.
	 * 
	 * @param ontologyFileName Filename of the OWL ontology to be used.
	 * @param ontologyFormat Format, e.g., RDF/XML, of the ontology file.
	 * @param mappingsFilename Filename of the mappings definitions.
	 */
	public AbstractXMLtoRDFconverter(String ontologyFileName, String ontologyFormat, String mappingsFilename) {
		ontConn = new OntologyConnector(ontologyFileName, ontologyFormat);
		mapper = new Mapper(mappingsFilename);
		rdfProc = new RdfProcessor(ontConn);
	}

	/**
	 * this methods starts the conversion.
	 *
	 * @param is the xml input stream
	 */
	public void convert(InputStream is) {
		xmlProc = new XMLProcessor(is);
		processingBeforeConvert();
		
		Node node;		
		while((node = xmlProc.nextElement()) != null) {
			List<MappingDefinition> mappingsForNode = mapper.getMappingsForNode(node);
			for (MappingDefinition mappingDefinition : mappingsForNode) {
				rdfProc.createRDF(node, mappingDefinition);
			}
		}
		
		processingAfterConvert();
	}
	
	public Model getGeneratedModel() {
		return rdfProc.getGeneratedModel();
	}


	/**
	 * A helper method for writing the result int o a file.
	 *
	 * @param filename the resulting filename and directory
	 * @param format the output format e.g. TTL
	 */
	public void writeRdfToFile(String filename, String format) {
		OutputStream out;
		try {
			out = new FileOutputStream(filename);
			getGeneratedModel().write(out, format);
			out.close();
			L.info("Turtle file written: " + filename);
		} catch (IOException e) {
			L.error("Failed writing turtle file " + filename + ": " + e);
		}
	}

	/**
	 * A helper method for writing results as ttl file.
	 *
	 * @param filename name and directory of the result file
	 */
	public void writeRdfToFile(String filename) {
		writeRdfToFile(filename, "TTL");
	}
	
	public void writeRdfToConsole() {
		getGeneratedModel().write(System.out, "TTL");
	}

    /**
     * Override this hook to make some adjustments before
     * we start the conversion.
     */
    public abstract void processingBeforeConvert();

    /**
     * Override the hook to make some clean ups after the conversion.
     */
	public abstract void processingAfterConvert();

}
