package de.werft.tools.general.commands;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Path;
import com.github.rvesse.airline.annotations.restrictions.Required;

import de.hpi.rdf.tailrapi.Memento;
import de.hpi.rdf.tailrapi.Repository;
import de.hpi.rdf.tailrapi.TailrClient;
import de.werft.tools.general.DwerftTools;

/**
 * 
 */
@Command(name = "linkale", description = "Links converted clip metadata to the appropriate scenes and output as file.")
public class LinkAle extends DwerftTools {

    @Arguments(description = "Creates a link file based on a converted ALE file and the tailr key under which the script is stored.")
    @Required
    @Path(mustExist = true)
    private String file = "";

    @Option(name = {"-k", "--key"}, description = "Provide the key name from tailr where the script can be found.")
    @Required
    private String key = "";

    @Option(name = {"-f", "--format"}, description = "Provide a language definition such as ttl, ntm, jsonld")
    private String lang = "ttl";

    @Option(name = {"-p", "--private"}, description = "If the tailr repository is private.")
    private boolean isPrivate = false;

    private Set<String> getResourcesWithCertainProperty(Model model, Resource resource, String property) {
    	Set<String> result = new HashSet<String>();
    	
        Property prop = model.getProperty(property);
        StmtIterator listProperties = resource.listProperties(prop);
        while (listProperties.hasNext()) {
			Statement statement = (Statement) listProperties.next();
			RDFNode object = statement.getObject();
			
			if (object instanceof Resource) {
				result.add(((Resource)object).getURI());
			}
		}
        
    	return result;
    }

    
    private Set<String> getResourcesOfCertainType(Model model, String type) {
    	Set<String> result = new HashSet<String>();
    	
        Property typeProp = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
        Resource typeClass = model.getResource(type);
        
        ResIterator it = model.listResourcesWithProperty(typeProp, typeClass);
        while (it.hasNext()) {
			Resource resource = (Resource) it.next();
			result.add(resource.getURI());
		}
        
    	return result;
    }


    
    @Override
    public void run() {
        super.run();
        logger.info("Linking ale file " + file);
        
        try {
            TailrClient client = getClient();
            Repository repo = new Repository(config.getTailrUser(), config.getTailrRepo());

            Memento m = client.getLatestMemento(repo, key);               
            Graph g = m.resolve();                
            Model scriptModel = ModelFactory.createModelForGraph(g);
            
            Model aleModel = ModelFactory.createDefaultModel();
            RDFDataMgr.read(aleModel, new FileInputStream(file), RDFLanguages.nameToLang(lang));
            
            String production = "";
            Map<String,String> episodeIndex = new HashMap<String, String>();
            Map<String,String> sceneIndex = new HashMap<String, String>();
            
            
            Set<String> prod = getResourcesOfCertainType(scriptModel, "http://filmontology.org/ontology/2.0/Production");
            prod.addAll(getResourcesOfCertainType(scriptModel, "http://filmontology.org/ontology/2.0/IndividualProduction"));
            prod.addAll(getResourcesOfCertainType(scriptModel, "http://filmontology.org/ontology/2.0/SeriesProduction"));
            
            if (prod.size() != 1) {
            	logger.error("No productions or multiple productions found under tailr key "+key+"\n"+prod);
            	return;
            }
            
            production = prod.iterator().next();
            
            System.out.println(production);
            
            Set<String> episodes = getResourcesWithCertainProperty(scriptModel, scriptModel.getResource(production), "http://filmontology.org/ontology/2.0/hasEpisode");
            for (String ep : episodes) {
            	Resource epRes = scriptModel.getResource(ep);
            	String epNum = getPropertyValue(scriptModel, epRes, "http://filmontology.org/ontology/2.0/episodeNumber");
            	episodeIndex.put(epNum, epRes.getURI());
			}
            
            if (episodeIndex.size() == 0) {
            	episodeIndex.put("1", production);
            }
            
            for (String epNum : episodeIndex.keySet()) {
            	Resource ep = scriptModel.getResource(episodeIndex.get(epNum));
            	Set<String> scripts = getResourcesWithCertainProperty(scriptModel, ep, "http://filmontology.org/ontology/2.0/hasScript");
            	Resource script = scriptModel.getResource(scripts.iterator().next());       	
            	
            	Set<String> scenes = getResourcesWithCertainProperty(scriptModel, script, "http://filmontology.org/ontology/2.0/hasScene");
            	for (String sc : scenes) {
                	Resource scRes = scriptModel.getResource(sc);
                	String scNum = getPropertyValue(scriptModel, scRes, "http://filmontology.org/ontology/2.0/sceneNumber"); 		
					sceneIndex.put(epNum+"-"+scNum, scRes.getURI());
				}
			}
            
            if (sceneIndex.size() == 0) {
            	logger.error("No scenes found under tailr key "+key+"\n"+episodeIndex);
            	return;
            }
            
            
            Property clipsShotProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/clipsShot");
            ResIterator clipsShotIt = aleModel.listResourcesWithProperty(clipsShotProp);
            
            if (!clipsShotIt.hasNext()) {
            	logger.error("No linked clips (clipsShot) found in file "+file);
            	return;
            }
            
            Resource aleProd = clipsShotIt.next();
            
            if (!aleProd.getURI().equals(production)) {
            	logger.error("Production of tailr key and file do not match "+production+" - "+aleProd.getURI());
            	return;
            }
            
        	Map<String,Resource> shotIndex = new HashMap<String, Resource>();

            Property aleTypeProp = aleModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            Resource shotClass = aleModel.getResource("http://filmontology.org/ontology/2.0/Shot");
            Resource takeClass = aleModel.getResource("http://filmontology.org/ontology/2.0/Take");
            Property shotNumberProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/shotNumber");
            Property takeNumberProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/takeNumber");
            Property hasClipProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/hasClip");
            Property hasTakeProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/hasTake");
            Property nameProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/name");
            Property hasShotProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/hasShot");

            Set<String> clipsShot = getResourcesWithCertainProperty(aleModel, aleProd, "http://filmontology.org/ontology/2.0/clipsShot");
            
            for (String clipUri : clipsShot) {
            	Resource clip = aleModel.getResource(clipUri);
				String episodeNumber = getPropertyValue(aleModel, clip, "http://filmontology.org/ontology/2.0/clipEpisodeNumber");
				String sceneNumber = getPropertyValue(aleModel, clip, "http://filmontology.org/ontology/2.0/sceneNumber");
				String shotNumber = getPropertyValue(aleModel, clip, "http://filmontology.org/ontology/2.0/slate");
				String takeNumber = getPropertyValue(aleModel, clip, "http://filmontology.org/ontology/2.0/take");
                String shotDescription = getPropertyValue(aleModel, clip, "http://filmontology.org/ontology/2.0/description");
				
				
				if (episodeNumber != null && sceneNumber != null && shotNumber != null && takeNumber != null ) {
					String sceneUri = sceneIndex.get(episodeNumber+"-"+sceneNumber);
					if (sceneUri == null) {
	                	logger.warn("No matching scene (Episode "+episodeNumber+" - Scene "+sceneNumber+") found for clip "+clip.getURI());
	                	continue;
					} else {
	                	logger.info("Episode "+episodeNumber+" - Scene "+sceneNumber+" - Shot "+shotNumber+" - Take "+takeNumber+" : "+clip.getURI()+" matched with "+sceneUri);
					}

                	Resource shot = shotIndex.get(episodeNumber+"-"+sceneNumber+"-"+shotNumber);
                	if (shot == null) {
	                	UUID uuid = UUID.randomUUID();
	                	shot = aleModel.getResource("http://filmontology.org/resource/Shot/"+uuid.toString());
	                	shotIndex.put(episodeNumber+"-"+sceneNumber+"-"+shotNumber, shot);
	                	shot.addProperty(aleTypeProp, shotClass);
	                	shot.addProperty(shotNumberProp, shotNumber);
	                	if (shotDescription != null && !"".equals(shotDescription)) {
	                		shot.addProperty(nameProp, shotDescription);
	                	}
	                	
	                	Resource targetScene = aleModel.getResource(sceneUri);
	                	targetScene.addProperty(hasShotProp, shot);
                	}

                	UUID uuid = UUID.randomUUID();
                	Resource take = aleModel.getResource("http://filmontology.org/resource/Take/"+uuid.toString());
                	take.addProperty(aleTypeProp, takeClass);
                	take.addProperty(takeNumberProp, takeNumber);	          
                	take.addProperty(hasClipProp, clip);
                	shot.addProperty(hasTakeProp, take);
					
				}

			}
            
            
            String name = StringUtils.removeEnd(file, "." + lang);
            name = name + "-linked."+lang;
            
            aleModel.setNsPrefix("for", "http://filmontology.org/resource/");
            aleModel.setNsPrefix("foo", "http://filmontology.org/ontology/2.0/");

            RDFDataMgr.write(new FileOutputStream(name), aleModel, RDFLanguages.TTL);

            logger.info("Linked file written: "+name);

        } catch (URISyntaxException | IOException e) {
            logger.error("No version found under tailr key "+key+" ."+e);
            e.printStackTrace();
        }

    }
    
    private String getPropertyValue(Model model, Resource resource, String propertyName) {
        Property prop = model.getProperty(propertyName);
        Statement stat = resource.getProperty(prop);
        if (stat == null) {
        	return null;
        }

    	return stat.getString();    	
    }
    
    /* build a new tailr client */
    private TailrClient getClient() throws URISyntaxException {
        return TailrClient.getInstance(config.getTailrBase(), config.getTailrUser(), config.getTailrToken(), isPrivate);
    }

}
