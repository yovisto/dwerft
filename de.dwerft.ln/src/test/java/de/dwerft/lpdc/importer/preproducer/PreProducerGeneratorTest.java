package de.dwerft.lpdc.importer.preproducer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import de.dwerft.lpdc.importer.general.Mapping;
import de.dwerft.lpdc.importer.general.Mapping.MappingAction;
import de.dwerft.lpdc.sources.PreproducerSource;


public class PreProducerGeneratorTest {

	private String owl = "file:ontology/dwerft-ontology.owl";

	private String format = "RDF/XML";
	
	private List<String> ignore = Arrays.asList("file");
	
	//@Test
	public void testGenerateInfo() throws MalformedURLException, IOException {
		// a simple example how to call the generator und use it
		PreProducerGenerator ppg = new PreProducerGenerator(owl, format, ignore, getMappings(), 2);
		ppg.generate(getStream("info"));
		
		Model m = ppg.getGeneratedModel();
		m.write(System.out, "TTL");
	}
	
	//@Test
	public void testGenerateCharacters() throws MalformedURLException, IOException {
		// a simple example how to call the generator und use it
		PreProducerGenerator ppg = new PreProducerGenerator(owl, format, ignore, getMappings(), 3);
		ppg.generate(getStream("listCharacters"));
		
		Model m = ppg.getGeneratedModel();
		m.write(System.out, "TTL");
	}
	
	//@Test
	public void testGenerateCrew() throws MalformedURLException, IOException {
		// a simple example how to call the generator und use it
		PreProducerGenerator ppg = new PreProducerGenerator(owl, format, ignore, getMappings(), 3);
		ppg.generate(getStream("listCrew"));
		
		Model m = ppg.getGeneratedModel();
		m.write(System.out, "TTL");
	}
	
	//@Test
	public void testGenerateScenes() throws MalformedURLException, IOException {
		// a simple example how to call the generator und use it
		PreProducerGenerator ppg = new PreProducerGenerator(owl, format, ignore, getMappings(), 3);
		ppg.generate(getStream("listScenes"));
		
		Model m = ppg.getGeneratedModel();
		m.write(System.out, "TTL");
	}
	
	//@Test
	public void testGenerateSchedule() throws MalformedURLException, IOException {
		// a simple example how to call the generator und use it
		PreProducerGenerator ppg = new PreProducerGenerator(owl, format, ignore, getMappings(), 3);
		ppg.generate(getStream("listSchedule"));
		
		Model m = ppg.getGeneratedModel();
		m.write(System.out, "TTL");
	}
	
	//@Test
	public void testGenerateDecorations() throws MalformedURLException, IOException {
		// a simple example how to call the generator und use it
		PreProducerGenerator ppg = new PreProducerGenerator(owl, format, ignore, getMappings(), 3);
		ppg.generate(getStream("listDecorations"));
		
		Model m = ppg.getGeneratedModel();
		m.write(System.out, "TTL");
	}
	
	
	@Test
	public void testAllMethods() throws MalformedURLException, IOException {
		// a simple example how to call the generator und use it
		PreProducerGenerator ppg = new PreProducerGenerator(owl, format, ignore, getMappings(), 2);
		ppg.generate(getStream("info"));
		ppg.setNodesToDrop(3);
		ppg.generate(getStream("listScenes"));
		ppg.generate(getStream("listDecorations"));
		ppg.generate(getStream("listCharacters"));
		ppg.generate(getStream("listExtras"));
		ppg.generate(getStream("listSchedule"));
		ppg.generate(getStream("listCrew"));
		
		Model m = ppg.getGeneratedModel();
		
		// Find the project and add a reference from DWERFT resource
		Resource dwerft = m.createResource("http://filmontology.org/resource/DWERFT");
		ResIterator subIterator = m.listSubjects();		
		while(subIterator.hasNext()) {
			Resource next = subIterator.next();
			if (next.getURI().contains("Project")) {
				Property dctPart = m.createProperty("http://purl.org/dc/terms/hasPart");
				dwerft.addProperty(dctPart, next);
			}
		}
		
		m.write(System.out, "TTL");
		writeRDFToFile(m, System.getProperty("java.io.tmpdir") + "/filmontology_example.ttl");
	}
	
	private HashSet<Mapping> getMappings() {
		HashSet<Mapping> mapping = new HashSet<Mapping>();
		/* general link mappings */
		mapping.add(Mapping.createMapping("company", MappingAction.LINK, "", "project", "hasCompany"));
		
		
		/* address mapping */
		mapping.add(Mapping.createMapping("adress", MappingAction.LINK, "company", "company", "address"));
		mapping.add(Mapping.createMapping("adress", MappingAction.MAP, "address"));
		mapping.add(Mapping.createMapping("street", MappingAction.MAP, "streetName"));
		mapping.add(Mapping.createMapping("country", MappingAction.MAP, "countryName"));
		mapping.add(Mapping.createMapping("city", MappingAction.MAP, "cityName"));
		
		/* id mapping */
		mapping.add(Mapping.createMapping("id", MappingAction.MAP, "identifierPreProducer"));
		mapping.add(Mapping.createMapping("projectid", MappingAction.MAP,"identifierPreProducer"));
		mapping.add(Mapping.createMapping("rel", MappingAction.MAP, "identifierPreProducer"));
		mapping.add(Mapping.createMapping("ref", MappingAction.MAP, "identifierPreProducer"));
		
		/* episode mapping */
		mapping.add(Mapping.createMapping("episode", MappingAction.LINK, "", "project", "hasEpisode"));
		mapping.add(Mapping.createMapping("ratio", MappingAction.MAP, "shootingRatio"));
		mapping.add(Mapping.createMapping("aspect", MappingAction.MAP, "aspectRatio"));
		mapping.add(Mapping.createMapping("length-in-sec", MappingAction.MAP, "duration"));
		mapping.add(Mapping.createMapping("material", MappingAction.MAP, "format"));
		
		/* character mapping */
		mapping.add(Mapping.createMapping("cast", MappingAction.LINK, "character", "project", "hasCast"));
		mapping.add(Mapping.createMapping("cast", MappingAction.LINK, "character", "character", "characterCast"));
		mapping.add(Mapping.createMapping("character", MappingAction.LINK, "project", "project", "hasCharacter"));
		mapping.add(Mapping.createMapping("character", MappingAction.LINK, "scene", "scene", "sceneCharacter"));
		mapping.add(Mapping.createMapping("number", MappingAction.CONTEXTMAP, "character", "characterNumber"));
		
		/* crew mapping */
		mapping.add(Mapping.createMapping("function", MappingAction.LINK, "", "project", "hasCrew"));
		mapping.add(Mapping.createMapping("function-group", MappingAction.LINK, "", "project", "hasCrewDepartment"));
		mapping.add(Mapping.createMapping("function", MappingAction.LINK, "", "crewDepartment", "hasCrewMember"));
		mapping.add(Mapping.createMapping("function", MappingAction.MAP, "crew"));
		mapping.add(Mapping.createMapping("function-group", MappingAction.MAP, "crewDepartment"));
		mapping.add(Mapping.createMapping("code", MappingAction.MAP, "departmentCode"));
		
		/* extra mapping */
		mapping.add(Mapping.createMapping("cast", MappingAction.LINK, "extras", "extra", "characterCast"));
		mapping.add(Mapping.createMapping("extrascast", MappingAction.LINK, "", "extra", "characterCast"));
		mapping.add(Mapping.createMapping("extras", MappingAction.LINK, "scene", "scene", "sceneExtra"));
		mapping.add(Mapping.createMapping("extrascast", MappingAction.MAP, "cast"));
		mapping.add(Mapping.createMapping("extras", MappingAction.MAP, "extra"));
		
		/* scene mapping */
		mapping.add(Mapping.createMapping("scene-group", MappingAction.MAP, "sceneGroup"));
		mapping.add(Mapping.createMapping("scene-group", MappingAction.LINK, "", "episode", "hasSceneGroup"));
		mapping.add(Mapping.createMapping("scene", MappingAction.LINK, "scene-group", "sceneGroup", "hasScene"));
		mapping.add(Mapping.createMapping("insert", MappingAction.MAP, "sceneInserted"));
		mapping.add(Mapping.createMapping("number", MappingAction.MAP, "sceneNumber"));
		mapping.add(Mapping.createMapping("head", MappingAction.MAP, "sceneHeader"));
		mapping.add(Mapping.createMapping("intext", MappingAction.MAP, "interiorExterior"));
		mapping.add(Mapping.createMapping("daynight", MappingAction.MAP, "dayTime"));
		mapping.add(Mapping.createMapping("shots", MappingAction.MAP, "sceneShots"));
		mapping.add(Mapping.createMapping("prestop", MappingAction.MAP, "estimatedTime"));
		mapping.add(Mapping.createMapping("storyday", MappingAction.MAP, "storyDay"));
		mapping.add(Mapping.createMapping("timelevel", MappingAction.MAP, "timeLevel"));
		mapping.add(Mapping.createMapping("storytime", MappingAction.MAP, "storyTime"));
		mapping.add(Mapping.createMapping("pagefrom", MappingAction.MAP, "pageStart"));
		mapping.add(Mapping.createMapping("pageto", MappingAction.MAP, "pageEnd"));
		mapping.add(Mapping.createMapping("description", MappingAction.MAP, "sceneDescription"));
		mapping.add(Mapping.createMapping("decoration", MappingAction.LINK, "scene", "scene", "sceneSet"));
		mapping.add(Mapping.createMapping("formattedscript", MappingAction.CONVERT, "sceneContent"));
		
		/* schedule mapping */
		mapping.add(Mapping.createMapping("shooting-board", MappingAction.MAP, "shootingSchedule"));
		mapping.add(Mapping.createMapping("shooting-board", MappingAction.LINK, "project", "project", "hasShootingSchedule"));
		mapping.add(Mapping.createMapping("shooting-day", MappingAction.MAP, "shootingDay"));
		mapping.add(Mapping.createMapping("shooting-day", MappingAction.LINK, "shooting-board", "shootingSchedule", "hasShootingDay"));
		mapping.add(Mapping.createMapping("scene", MappingAction.LINK, "shooting-day", "shootingDay", "shootingDayScene"));
		mapping.add(Mapping.createMapping("mode", MappingAction.MAP, "shootingDayMode"));
		mapping.add(Mapping.createMapping("night", MappingAction.MAP, "shootingDayNight"));
		
		/* decoration mapping */
		mapping.add(Mapping.createMapping("decoration", MappingAction.MAP, "set"));
		mapping.add(Mapping.createMapping("typecode", MappingAction.MAP, "setType"));
		mapping.add(Mapping.createMapping("decoration", MappingAction.LINK, "project", "project", "hasSet"));
		mapping.add(Mapping.createMapping("location", MappingAction.LINK, "decoration", "set", "setLocation"));
		mapping.add(Mapping.createMapping("adress", MappingAction.LINK, "decoration", "set", "address", true));
		mapping.add(Mapping.createMapping("emergency", MappingAction.LINK, "decoration", "set", "hasEmergency", true));
		mapping.add(Mapping.createMapping("label", MappingAction.MAP, "name"));
		mapping.add(Mapping.createMapping("facility", MappingAction.LINK, "decoration", "set", "hasFacility", true));
		mapping.add(Mapping.createMapping("type", MappingAction.MAP, "facilityType"));
		
		/* hacking the mapping system */
		mapping.add(Mapping.createMapping("facility", MappingAction.CONTEXTMAP, "location", "name"));
		mapping.add(Mapping.createMapping("emergency", MappingAction.CONTEXTMAP, "location", "name"));
		mapping.add(Mapping.createMapping("name", MappingAction.CONTEXTMAP, "location", "name"));
		
		return mapping;
	}
	
	private BufferedInputStream getStream(String methodName) throws MalformedURLException, IOException {
		return new PreproducerSource(new File("src/main/resource/config.properties")).get(methodName);
	}
	
	private void writeRDFToFile(Model m, String output) {
		OutputStream out;
		try {
			out = new FileOutputStream(output);
			m.write(out, "TTL");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
