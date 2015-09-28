package de.werft.tools.importer.preproducer;

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

import de.werft.tools.importer.general.Mapping;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import de.werft.tools.sources.PreproducerSource;


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
		mapping.add(Mapping.createMapping("company", Mapping.MappingAction.LINK, "", "project", "hasCompany"));
		
		
		/* address mapping */
		mapping.add(Mapping.createMapping("adress", Mapping.MappingAction.LINK, "company", "company", "address"));
		mapping.add(Mapping.createMapping("adress", Mapping.MappingAction.MAP, "address"));
		mapping.add(Mapping.createMapping("street", Mapping.MappingAction.MAP, "streetName"));
		mapping.add(Mapping.createMapping("country", Mapping.MappingAction.MAP, "countryName"));
		mapping.add(Mapping.createMapping("city", Mapping.MappingAction.MAP, "cityName"));
		
		/* id mapping */
		mapping.add(Mapping.createMapping("id", Mapping.MappingAction.MAP, "identifierPreProducer"));
		mapping.add(Mapping.createMapping("projectid", Mapping.MappingAction.MAP,"identifierPreProducer"));
		mapping.add(Mapping.createMapping("rel", Mapping.MappingAction.MAP, "identifierPreProducer"));
		mapping.add(Mapping.createMapping("ref", Mapping.MappingAction.MAP, "identifierPreProducer"));
		
		/* episode mapping */
		mapping.add(Mapping.createMapping("episode", Mapping.MappingAction.LINK, "", "project", "hasEpisode"));
		mapping.add(Mapping.createMapping("ratio", Mapping.MappingAction.MAP, "shootingRatio"));
		mapping.add(Mapping.createMapping("aspect", Mapping.MappingAction.MAP, "aspectRatio"));
		mapping.add(Mapping.createMapping("length-in-sec", Mapping.MappingAction.MAP, "duration"));
		mapping.add(Mapping.createMapping("material", Mapping.MappingAction.MAP, "format"));
		
		/* character mapping */
		mapping.add(Mapping.createMapping("cast", Mapping.MappingAction.LINK, "character", "project", "hasCast"));
		mapping.add(Mapping.createMapping("cast", Mapping.MappingAction.LINK, "character", "character", "characterCast"));
		mapping.add(Mapping.createMapping("character", Mapping.MappingAction.LINK, "project", "project", "hasCharacter"));
		mapping.add(Mapping.createMapping("character", Mapping.MappingAction.LINK, "scene", "scene", "sceneCharacter"));
		mapping.add(Mapping.createMapping("number", Mapping.MappingAction.CONTEXTMAP, "character", "characterNumber"));
		
		/* crew mapping */
		mapping.add(Mapping.createMapping("function", Mapping.MappingAction.LINK, "", "project", "hasCrew"));
		mapping.add(Mapping.createMapping("function-group", Mapping.MappingAction.LINK, "", "project", "hasCrewDepartment"));
		mapping.add(Mapping.createMapping("function", Mapping.MappingAction.LINK, "", "crewDepartment", "hasCrewMember"));
		mapping.add(Mapping.createMapping("function", Mapping.MappingAction.MAP, "crew"));
		mapping.add(Mapping.createMapping("function-group", Mapping.MappingAction.MAP, "crewDepartment"));
		mapping.add(Mapping.createMapping("code", Mapping.MappingAction.MAP, "departmentCode"));
		
		/* extra mapping */
		mapping.add(Mapping.createMapping("cast", Mapping.MappingAction.LINK, "extras", "extra", "characterCast"));
		mapping.add(Mapping.createMapping("extrascast", Mapping.MappingAction.LINK, "", "extra", "characterCast"));
		mapping.add(Mapping.createMapping("extras", Mapping.MappingAction.LINK, "scene", "scene", "sceneExtra"));
		mapping.add(Mapping.createMapping("extrascast", Mapping.MappingAction.MAP, "cast"));
		mapping.add(Mapping.createMapping("extras", Mapping.MappingAction.MAP, "extra"));
		
		/* scene mapping */
		mapping.add(Mapping.createMapping("scene-group", Mapping.MappingAction.MAP, "sceneGroup"));
		mapping.add(Mapping.createMapping("scene-group", Mapping.MappingAction.LINK, "", "episode", "hasSceneGroup"));
		mapping.add(Mapping.createMapping("scene", Mapping.MappingAction.LINK, "scene-group", "sceneGroup", "hasScene"));
		mapping.add(Mapping.createMapping("insert", Mapping.MappingAction.MAP, "sceneInserted"));
		mapping.add(Mapping.createMapping("number", Mapping.MappingAction.MAP, "sceneNumber"));
		mapping.add(Mapping.createMapping("head", Mapping.MappingAction.MAP, "sceneHeader"));
		mapping.add(Mapping.createMapping("intext", Mapping.MappingAction.MAP, "interiorExterior"));
		mapping.add(Mapping.createMapping("daynight", Mapping.MappingAction.MAP, "dayTime"));
		mapping.add(Mapping.createMapping("shots", Mapping.MappingAction.MAP, "sceneShots"));
		mapping.add(Mapping.createMapping("prestop", Mapping.MappingAction.MAP, "estimatedTime"));
		mapping.add(Mapping.createMapping("storyday", Mapping.MappingAction.MAP, "storyDay"));
		mapping.add(Mapping.createMapping("timelevel", Mapping.MappingAction.MAP, "timeLevel"));
		mapping.add(Mapping.createMapping("storytime", Mapping.MappingAction.MAP, "storyTime"));
		mapping.add(Mapping.createMapping("pagefrom", Mapping.MappingAction.MAP, "pageStart"));
		mapping.add(Mapping.createMapping("pageto", Mapping.MappingAction.MAP, "pageEnd"));
		mapping.add(Mapping.createMapping("description", Mapping.MappingAction.MAP, "sceneDescription"));
		mapping.add(Mapping.createMapping("decoration", Mapping.MappingAction.LINK, "scene", "scene", "sceneSet"));
		mapping.add(Mapping.createMapping("formattedscript", Mapping.MappingAction.CONVERT, "sceneContent"));
		
		/* schedule mapping */
		mapping.add(Mapping.createMapping("shooting-board", Mapping.MappingAction.MAP, "shootingSchedule"));
		mapping.add(Mapping.createMapping("shooting-board", Mapping.MappingAction.LINK, "project", "project", "hasShootingSchedule"));
		mapping.add(Mapping.createMapping("shooting-day", Mapping.MappingAction.MAP, "shootingDay"));
		mapping.add(Mapping.createMapping("shooting-day", Mapping.MappingAction.LINK, "shooting-board", "shootingSchedule", "hasShootingDay"));
		mapping.add(Mapping.createMapping("scene", Mapping.MappingAction.LINK, "shooting-day", "shootingDay", "shootingDayScene"));
		mapping.add(Mapping.createMapping("mode", Mapping.MappingAction.MAP, "shootingDayMode"));
		mapping.add(Mapping.createMapping("night", Mapping.MappingAction.MAP, "shootingDayNight"));
		
		/* decoration mapping */
		mapping.add(Mapping.createMapping("decoration", Mapping.MappingAction.MAP, "set"));
		mapping.add(Mapping.createMapping("typecode", Mapping.MappingAction.MAP, "setType"));
		mapping.add(Mapping.createMapping("decoration", Mapping.MappingAction.LINK, "project", "project", "hasSet"));
		mapping.add(Mapping.createMapping("location", Mapping.MappingAction.LINK, "decoration", "set", "setLocation"));
		mapping.add(Mapping.createMapping("adress", Mapping.MappingAction.LINK, "decoration", "set", "address", true));
		mapping.add(Mapping.createMapping("emergency", Mapping.MappingAction.LINK, "decoration", "set", "hasEmergency", true));
		mapping.add(Mapping.createMapping("label", Mapping.MappingAction.MAP, "name"));
		mapping.add(Mapping.createMapping("facility", Mapping.MappingAction.LINK, "decoration", "set", "hasFacility", true));
		mapping.add(Mapping.createMapping("type", Mapping.MappingAction.MAP, "facilityType"));
		
		/* hacking the mapping system */
		mapping.add(Mapping.createMapping("facility", Mapping.MappingAction.CONTEXTMAP, "location", "name"));
		mapping.add(Mapping.createMapping("emergency", Mapping.MappingAction.CONTEXTMAP, "location", "name"));
		mapping.add(Mapping.createMapping("name", Mapping.MappingAction.CONTEXTMAP, "location", "name"));
		
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
