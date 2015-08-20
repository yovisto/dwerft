package de.dwerft.lpdc.importer.preproducer;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

import de.dwerft.lpdc.general.OntologyConstants;
import de.dwerft.lpdc.importer.general.MappingDefinition;
import de.dwerft.lpdc.importer.general.MappingDefinition.ContentSource;
import de.dwerft.lpdc.importer.general.MappingDefinition.TargetPropertyType;
import de.dwerft.lpdc.sources.PreproducerSource;

public class PreProducerToRdfTest {


	@Test
	public void testConverter() {
		
		Set<MappingDefinition> mappings = new HashSet<MappingDefinition>();
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project", null, null,
						null, null, OntologyConstants.ONTOLOGY_NAMESPACE+"Project", null, null));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project",null, null,
						ContentSource.ATTRIBUTE, "projectid",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Project", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"identifierPreProducer",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:title", null, null,
						ContentSource.TEXT_CONTENT, null,
						OntologyConstants.ONTOLOGY_NAMESPACE+"Project", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"title",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:episode", null, null,
						null, null, OntologyConstants.ONTOLOGY_NAMESPACE+"Episode", null, null));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:episode", null, null,
						ContentSource.ATTRIBUTE, "id",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Episode", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"identifierPreProducer",
						TargetPropertyType.DATATYPE_PROPERTY));

		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:episode/prp:framerate", null, null,
						ContentSource.TEXT_CONTENT, null,
						OntologyConstants.ONTOLOGY_NAMESPACE+"Episode", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"frameRate",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:episode/prp:ratio", null, null,
						ContentSource.TEXT_CONTENT, null,
						OntologyConstants.ONTOLOGY_NAMESPACE+"Episode", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"shootingRatio",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:episode/prp:length-in-sec", null, null,
						ContentSource.TEXT_CONTENT, null,
						OntologyConstants.ONTOLOGY_NAMESPACE+"Episode", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"duration",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:episode/material", null, null,
						ContentSource.TEXT_CONTENT, null,
						OntologyConstants.ONTOLOGY_NAMESPACE+"Episode", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"format",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:episode/aspect", null, null,
						ContentSource.TEXT_CONTENT, null,
						OntologyConstants.ONTOLOGY_NAMESPACE+"Episode", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"aspectRatio",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:episode", null, null,
						ContentSource.CONTAINMENT, "prp:project",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Episode", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"hasEpisode",
						TargetPropertyType.OBJECT_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/company", null, null,
						null, null, OntologyConstants.ONTOLOGY_NAMESPACE+"Company", null, null));

		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/company", null, null,
						ContentSource.CONTAINMENT, "prp:project",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Company", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"hasCompany",
						TargetPropertyType.OBJECT_PROPERTY));

		mappings.add(
				new MappingDefinition("/root/return/prp:project/company/name", null, null,
						ContentSource.TEXT_CONTENT, null,
						OntologyConstants.ONTOLOGY_NAMESPACE+"Company", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"name",
						TargetPropertyType.DATATYPE_PROPERTY));

		mappings.add(
				new MappingDefinition("/root/return/prp:project/company/prp:adress", null, null,
						null, null, OntologyConstants.ONTOLOGY_NAMESPACE+"Address", null, null));

		mappings.add(
				new MappingDefinition("/root/return/prp:project/company/prp:adress", null, null,
						ContentSource.CONTAINMENT, "company",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Company", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"address",
						TargetPropertyType.OBJECT_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/company/name", null, null,
						ContentSource.TEXT_CONTENT, null,
						OntologyConstants.ONTOLOGY_NAMESPACE+"Company", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"name",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:character", null, null,
						null, null, OntologyConstants.ONTOLOGY_NAMESPACE+"Character", null, null));
	
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:character", null, null,
						ContentSource.ATTRIBUTE, "id",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Character", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"identifierPreProducer",
						TargetPropertyType.DATATYPE_PROPERTY));	
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:character/number", null, null,
						ContentSource.TEXT_CONTENT, null,
						OntologyConstants.ONTOLOGY_NAMESPACE+"Character", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"characterNumber",
						TargetPropertyType.DATATYPE_PROPERTY));	
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:character/name", null, null,
						ContentSource.TEXT_CONTENT, null,
						OntologyConstants.ONTOLOGY_NAMESPACE+"Character", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"name",
						TargetPropertyType.DATATYPE_PROPERTY));	
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:character/prp:cast", null, null,
						null, null, OntologyConstants.ONTOLOGY_NAMESPACE+"Cast", null, null));
	
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:character/prp:cast/name", null, null,
						ContentSource.TEXT_CONTENT, null,
						OntologyConstants.ONTOLOGY_NAMESPACE+"Cast", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"name",
						TargetPropertyType.DATATYPE_PROPERTY));		

		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:character/prp:cast", null, null,
						ContentSource.CONTAINMENT, "prp:character",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Character", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"characterCast",
						TargetPropertyType.OBJECT_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:character/prp:cast", null, null,
						ContentSource.CONTAINMENT, "prp:project",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Project", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"hasCast",
						TargetPropertyType.OBJECT_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/root/return/prp:project/prp:character/prp:cast/prp:sex", null, null,
						ContentSource.TEXT_CONTENT, null,
						OntologyConstants.ONTOLOGY_NAMESPACE+"Cast", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"sex",
						TargetPropertyType.DATATYPE_PROPERTY));		
		
		
		PreproducerSource pps = new PreproducerSource(new File("src/main/resource/config.properties"));
		
		PreProducerToRdf pprdf = new PreProducerToRdf(OntologyConstants.ONTOLOGY_FILE,
				OntologyConstants.ONTOLOGY_FORMAT, mappings);
		
		pprdf.convert(pps.get("info"));
		pprdf.convert(pps.get("listCharacters"));
		
		Model generatedModel = pprdf.getGeneratedModel();
		
		generatedModel.write(System.out, "TTL");

		
	}
}
