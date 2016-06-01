package de.werft.tools.importer.general;

import com.hp.hpl.jena.rdf.model.Model;
import de.werft.tools.sources.AbstractSource;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * The xml to rdf converter.
 *
 * This class takes first the ontology file and its format.
 * Second the mapping from xml to rdf. For the mapping format see README.md
 *
 */
public abstract class AbstractXMLtoRDFconverter implements Converter<Model> {
	
	protected final Logger L = Logger.getLogger(this.getClass().getName());
	
	protected OntologyConnector ontConn;
	protected XMLProcessor xmlProc;
	protected Mapper mapper;
	protected RdfProcessor rdfProc;
    private Converter<File> preConverter;

	/**
	 * The XMLtoRDFconverter is the controller of the OntologyConnector, XMLProcessor,
	 * Mapper, and RdfProcessor.
	 * 
	 * Specific XML converters inherit from this class and implement specific conversion
	 * routines that are execute before and after the general conversion process based
	 * on the mapping definitions.
	 *  @param ontologyFileName Filename of the OWL ontology to be used.
	 * @param ontologyFormat Format, e.g., RDF/XML, of the ontology file.
     * @param mappingsFilename Filename of the mappings definitions.
     */
	public AbstractXMLtoRDFconverter(InputStream ontologyFileName, String ontologyFormat, String mappingsFilename) {
		ontConn = new OntologyConnector(ontologyFileName, ontologyFormat);
		mapper = new Mapper(mappingsFilename);
		rdfProc = new RdfProcessor(ontConn);
	}

    public void setPreConverter(Converter<File> preConverter) {
        this.preConverter = preConverter;
    }

	/**
	 * This methods starts the conversion.
	 *
	 * @param input the xml input stream
	 */
	public void convert(String input) throws IOException {
        String file = input;
        if (preConverter != null) {
            preConverter.convert(input);
            file = preConverter.getResult().getAbsolutePath();
        }

		xmlProc = getInputProcessor(file);
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

    protected XMLProcessor getInputProcessor(String input) {
        return new XMLProcessor(new AbstractSource().get(input));
    }
	
	public Model getResult() {
		return rdfProc.getGeneratedModel();
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
