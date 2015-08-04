package de.dwerft.lpdcimport.general;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * 
 * Like JIT compiler this class is the abstract form of a generation tool
 * for reading a input stream of some structured source like xml or csv and 
 * generating a rdf file based on a given ontology. As a result you 
 * get the generated {@link Model}.
 * This class provides a backend for parser in terms of providing methods
 * to read things from the original ontology and write to the generation model.
 * 
 * //TODO provide validation for the model, maybe use rdfunit or our own impl
 * 
**/
public abstract class RdfGenerator {
	
	/** The ontology model. */
	protected OntModel ontologyModel;
	
	/** The generated model. */
	protected Model generatedModel;
	
	/**
	 * Instantiates a new rdf generator.
	 *
	 * @param owl
	 *            a {@link File} from which we read the ontology
	 * @param format
	 *            the owl file format
	 * @param propertyMapping
	 *            provides a mapping between the xml model and the ontology
	 */
	public RdfGenerator(String owl, String format) {
		this.ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		loadOntModel(owl, format);
	}
	
	/* read the ontology we use and create a default model for generation */
	private void loadOntModel(String owl, String format) {
		ontologyModel.read(owl, format);
		generatedModel = ModelFactory.createDefaultModel();
		generatedModel.setNsPrefixes(ontologyModel.getNsPrefixMap());
		//ontologyModel.write(System.out, "TTL");
	}
	
	/**
	 * Gets the ontology class or null if the class name does not exist.
	 *
	 * @param name
	 *            the name
	 * @return the ontology class
	 */
	protected Optional<OntClass> getOntologyClass(String name) {
		ExtendedIterator<OntClass> classes = ontologyModel.listClasses();
		return classes.toList().stream().filter(o -> o.getLocalName().equalsIgnoreCase(name)).findFirst();
	}
	
	/**
	 * Gets the ontology object property.
	 *
	 * @param name
	 *            the name
	 * @return the ontology object property
	 */
	protected Optional<ObjectProperty> getOntologyObjectProperty(String name) {
		ExtendedIterator<ObjectProperty> classes = ontologyModel.listObjectProperties();
		return classes.toList().stream().filter(o -> o.getLocalName().equalsIgnoreCase(name)).findFirst();
	}
	
	/**
	 * Gets the ontology datatype property.
	 *
	 * @param name
	 *            the name
	 * @return the ontology datatype property
	 */
	protected Optional<DatatypeProperty> getOntologyDatatypeProperty(String name) {
		ExtendedIterator<DatatypeProperty> classes = ontologyModel.listDatatypeProperties();
		return classes.toList().stream().filter(o -> o.getLocalName().equalsIgnoreCase(name)).findFirst();
	}	
	
	/**
	 * The property is required for adding rdf:type to their class
	 * 
	 * @param prefix
	 * @param name
	 * @return the property, or null if non was found
	 */
	protected Property getRdfProperty(String prefix, String name) {
		String propPrefix = ontologyModel.getNsPrefixURI(prefix);
		return propPrefix != null ? ontologyModel.getProperty(propPrefix, name) : null;
	}
	
	/**
	 * Sets the property.
	 *
	 * @param name the name of the literal
	 * @param value the literal value
	 * @param r the resource that binds the literal
	 */
	protected void setProperty(String name, String value, Resource r) {
		/* check if we have a literal property */
		Optional<DatatypeProperty> dp = getOntologyDatatypeProperty(name);
		if (dp.isPresent()) {
			Literal l = generatedModel.createTypedLiteral(convertStringToAppropiateObject(value));
			r.addLiteral(generatedModel.getProperty(dp.get().getURI()), l);
		}
		
		/* we have a link to another class */
		Optional<ObjectProperty> op = getOntologyObjectProperty(name);
		if (op.isPresent()) {
			r.addProperty(op.get(), generatedModel.createResource(value));
		}
	}
	
	/**
	 * Since the values of xml tags are only read as Strings, the real
	 * type must be determined in order to use the correct rdf data types
	 * This methods does so.
	 * e.g.: A value of "true" would return a Boolean true
	 * 
	 * Supported datatypes:
	 * Boolean, Integer, Float, String
	 * 
	 * @param value the String containing a value
	 * @return an Object of the correct type
	 */
	private Object convertStringToAppropiateObject(String value) {
		Object o = value;
		
		//Boolean
		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
			o = Boolean.parseBoolean(value);
		}
		//Integer
		try {
			if (value.matches("-?[0-9]+")) {
				o = Integer.parseInt(value);
			}
		} catch (NumberFormatException nfe) { }
				
		//Float
		if (value.matches("-?[0-9]\\.[0-9]+")) {
			o = Float.parseFloat(value);
		}
		
		//If the input is neither numeric nor Boolean it is returned as a String
		return o;
	}
	
	/* returns the resource that satisfies the literal or null */
	public Resource getResourceThatSatisfiesLiteral(Property p, String literal) {
		ExtendedIterator<Resource> itr = generatedModel.listResourcesWithProperty(p).filterKeep(new Filter<Resource>() {
			
			@Override
			public boolean accept(Resource arg0) {
				return arg0.hasLiteral(p, literal);
			}
		});		
		/* we want the first one */
		return itr.hasNext() ? itr.next() : null;
	}
	
	
	/**
	 * Generate a {@link Model} from the input stream.
	 *
	 * @param stream
	 *            the data input stream
	 */
	public abstract void generate(InputStream stream);

	
	/**
	 * Gets the generated model.
	 *
	 * @return the generated model
	 */
	public Model getGeneratedModel() {
		return generatedModel;
	}
	
	/**
	 * Serializes an RDF model into a file with turtle syntax
	 * @param m
	 * 			the RDF model
	 * @param outputPath
	 * 			path to the file name
	 */
	public void writeRDFToFile(Model m, String outputPath) {
		OutputStream out;
		try {
			out = new FileOutputStream(outputPath);
			m.write(out, "TTL");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
