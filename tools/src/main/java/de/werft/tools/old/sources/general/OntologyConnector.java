package de.werft.tools.old.sources.general;

import de.werft.tools.general.OntologyConstants;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * The OntologyConnector is responsible for retrieving ontology model elements based on the names and prefixes.
 * 
 * @author hagt
 *
 */
public class OntologyConnector {
	
	private OntModel ontologyModel;

	/**
	 * Creates a new OntologyConnector and reads the ontology from an URL.
	 *  @param url
	 * @param format
     */
	public OntologyConnector(InputStream url, String format) {
		this.ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		ontologyModel.read(url,format);
	}
	
	/**
	 * Gets the ontology class or null if the class uri does not exist.
	 *
	 * @param uri The URI of the ontology class
	 *            
	 * @return the ontology class
	 */
	public OntClass getOntologyClass(String uri) {
		ExtendedIterator<OntClass> classes = ontologyModel.listClasses();
        while (classes.hasNext()) {
            OntClass next = classes.next();
            if (uri.equals(next.getURI())) {
                return next;
            }
        }
        return null;
    }
	
	/**
	 * Gets a ontology datatype property.
	 *
	 * @param uri The URI of the datatype property
	 * @return The ontology datatype property
	 */
	public DatatypeProperty getOntologyDatatypeProperty(String uri) {
		ExtendedIterator<DatatypeProperty> props = ontologyModel.listDatatypeProperties();
        while (props.hasNext()) {
            DatatypeProperty next = props.next();
            if (uri.equals(next.getURI())) {
                return next;
            }
        }
        return null;
    }
	
	/**
	 * Gets a ontology datatype property.
	 * 
	 * @param prefix	Prefix of the namespace
	 * @param propertyName	Name of the property
	 * @return The ontology datatype property
	 */
	public DatatypeProperty getOntologyDatatypeProperty(String prefix, String propertyName) {
		return getOntologyDatatypeProperty(ontologyModel.getNsPrefixURI(prefix)+propertyName);
	}
	
	/**
	 * Get a ontology property
	 * 
	 * @param prefix	Prefix of the namespace
	 * @param propertyName	Name of the property
	 * @return The ontology property
	 */
	public Property getProperty(String prefix, String propertyName) {
		return ontologyModel.getProperty(ontologyModel.getNsPrefixURI(prefix), propertyName);
	}
	
	
	/**
	 * Gets a ontology object property from the ontology model.
	 *
	 * @param uri The URI of the object property
	 * @return the ontology object property
	 */
	
	public ObjectProperty getOntologyObjectProperty(String uri) {
		ExtendedIterator<ObjectProperty> props = ontologyModel.listObjectProperties();
        while (props.hasNext()) {
            ObjectProperty next = props.next();
            if (uri.equals(next.getURI())) {
                return next;
            }
        }
        return null;
    }
	
	/**
	 * Gets a ontology object property from the ontology model.
	 * 
	 * @param prefix	Prefix of the namespace
	 * @param propertyName	Name of the property
	 * @return The ontology object property
	 */
	public ObjectProperty getOntologyObjectProperty(String prefix, String propertyName) {
		return getOntologyObjectProperty(ontologyModel.getNsPrefixURI(prefix)+propertyName);
	}
	
	/**
	 * Retrieves the set of declared properties in the ontology model
	 * for the specific ontology class.
	 * @param uri The URI of the ontology class
	 * @return A set of property names
	 */
	public Set<Property> getDeclaredProperties(String uri) {
		Set<Property> result = new HashSet<Property>();
		
		OntClass ontClass = ontologyModel.getOntClass(uri);
		if (ontClass != null) {
			ExtendedIterator<OntProperty> props = ontClass.listDeclaredProperties();
			while (props.hasNext()) {
				OntProperty prop = (OntProperty) props.next();
				if (prop.getURI().startsWith(OntologyConstants.ONTOLOGY_NAMESPACE)) {
					result.add(prop);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Gets the ontology model.
	 * 
	 * @return The ontology model.
	 */
	public OntModel getOntologyModel() {
		return ontologyModel;
	}
}
