package de.dwerft.lpdc.importer.dramaqueen;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

import de.dwerft.lpdc.general.OntologyConstants;
import de.dwerft.lpdc.importer.general.MappingDefinition;
import de.dwerft.lpdc.importer.general.MappingDefinition.ContentSource;
import de.dwerft.lpdc.importer.general.MappingDefinition.TargetPropertyType;
import de.dwerft.lpdc.sources.DramaQueenSource;

public class DramaqueenToRdfTest {
	
	private static final String dqFile = "examples/Hansel_Gretel_de.dq";

	@Test
	public void testConverter() {
		
		Set<MappingDefinition> mappings = new HashSet<MappingDefinition>();
		
	
		mappings.add(
				new MappingDefinition("/ScriptDocument", null, null,
						null, null, OntologyConstants.ONTOLOGY_NAMESPACE+"Project", null, null));
		mappings.add(
				new MappingDefinition("/ScriptDocument",
						"id", null, ContentSource.ATTRIBUTE, "id",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Project", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"identifierDramaQueen",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		
		mappings.add(
				new MappingDefinition("/ScriptDocument/characters/Character", null, null,
						null, null, OntologyConstants.ONTOLOGY_NAMESPACE+"Character", null, null));
		
		mappings.add(
				new MappingDefinition("/ScriptDocument/characters/Character", "id", null,
						ContentSource.ATTRIBUTE, "id",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Character", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"identifierDramaQueen",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/ScriptDocument/characters/Character/Property",
						"id", "0", ContentSource.ATTRIBUTE, "value",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Character", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"name",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/ScriptDocument/characters/Character/Property",
						"id", "181", ContentSource.ATTRIBUTE, "value",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Character", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"fullName",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		
		
		mappings.add(
				new MappingDefinition("/ScriptDocument/characters/Character",
						null, null,
						ContentSource.CONTAINMENT, "ScriptDocument",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Project", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"hasCharacter",
						TargetPropertyType.OBJECT_PROPERTY));
		

		
		mappings.add(
				new MappingDefinition("/ScriptDocument/locations/Location",
						null, null,
						null, null, OntologyConstants.ONTOLOGY_NAMESPACE+"Set", null, null));

		
		mappings.add(
				new MappingDefinition("/ScriptDocument/locations/Location",
						"id",null,
						ContentSource.ATTRIBUTE, "id",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Set", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"identifierDramaQueen",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		
		
		
		
		
		
		
		mappings.add(
				new MappingDefinition("/ScriptDocument/sequences/Step", null, null,
						null, null, OntologyConstants.ONTOLOGY_NAMESPACE+"SceneGroup", null, null));
		
		
		mappings.add(
				new MappingDefinition("/ScriptDocument/sequences/Step/children/Scene/children/Frame",
						null, null,
						null, null, OntologyConstants.ONTOLOGY_NAMESPACE+"Scene", null, null));

		
		mappings.add(
				new MappingDefinition("/ScriptDocument/sequences/Step/children/Scene/children/Frame",
						"id",null,
						ContentSource.ATTRIBUTE, "id",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Scene", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"identifierDramaQueen",
						TargetPropertyType.DATATYPE_PROPERTY));
		
		mappings.add(
				new MappingDefinition("/ScriptDocument/sequences/Step/children/Scene/children/Frame",
						null, null,
						ContentSource.CONTAINMENT, "Step",
						OntologyConstants.ONTOLOGY_NAMESPACE+"SceneGroup", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"hasScene",
						TargetPropertyType.OBJECT_PROPERTY));
		
		
		mappings.add(
				new MappingDefinition("/ScriptDocument/sequences/Step/children/Scene/children/Frame/Property",
						"id", "168", ContentSource.ATTRIBUTE, "value",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Frame", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"interiorExterior",
						TargetPropertyType.DATATYPE_PROPERTY));

		mappings.add(
				new MappingDefinition("/ScriptDocument/sequences/Step/children/Scene/children/Frame/Property",
						"id", "10", ContentSource.REFERENCE, "value",
						OntologyConstants.ONTOLOGY_NAMESPACE+"Frame", 
						OntologyConstants.ONTOLOGY_NAMESPACE+"sceneSet",
						TargetPropertyType.OBJECT_PROPERTY));
		
		

		DramaQueenSource dqsoure = new DramaQueenSource();
		InputStream inputStream = dqsoure.get(dqFile);
		
		
		DramaqueenToRdf dqrdf = new DramaqueenToRdf(OntologyConstants.ONTOLOGY_FILE, OntologyConstants.ONTOLOGY_FORMAT, mappings);
		
		dqrdf.convert(inputStream);
		
		Model generatedModel = dqrdf.getGeneratedModel();


		generatedModel.write(System.out, "TTL");
	}

}
