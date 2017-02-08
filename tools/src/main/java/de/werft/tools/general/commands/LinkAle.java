package de.werft.tools.general.commands;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;

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

    private Resource getResourceWithCertainPropertyValue(Model model, Resource resource, String property, String propValue) {
    	return null;
    }
    
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
            Map<String,String> scriptIndex = new HashMap<String, String>();
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
            	System.out.println(ep);
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
            
            System.out.println(sceneIndex);
            
            

                        
            
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
            

//            Map<String,Map<String,Map<String,Map<String,String>>>> clipIndex = new HashMap<String, Map<String,Map<String,Map<String,String>>>>();

        	Map<String,Resource> shotIndex = new HashMap<String, Resource>();
        	Map<String,Resource> takeIndex = new HashMap<String, Resource>();

            Property aleTypeProp = aleModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            Resource shotClass = aleModel.getResource("http://filmontology.org/ontology/2.0/Shot");
            Resource takeClass = aleModel.getResource("http://filmontology.org/ontology/2.0/Take");
            Property shotNumberProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/shotNumber");
            Property takeNumberProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/takeNumber");
            Property episodeNumberProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/episodeNumber");
            Property sceneNumProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/sceneNumber");
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
//					System.out.println(episodeNumber+" "+sceneNumber+" "+shotNumber+" "+takeNumber+" "+shotDescription+" "+clip.getURI());
					
					String sceneUri = sceneIndex.get(episodeNumber+"-"+sceneNumber);
					if (sceneUri == null) {
	                	logger.warn("No matching scene (Episode "+episodeNumber+" - Scene "+sceneNumber+" found for clip "+clip.getURI());
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
            name = name + "-linked.ttl";
            
            aleModel.setNsPrefix("for", "http://filmontology.org/resource/");
            aleModel.setNsPrefix("foo", "http://filmontology.org/ontology/2.0/");

            RDFDataMgr.write(new FileOutputStream(name), aleModel, RDFLanguages.TTL);

            
//            while (clipsShotIt.hasNext()) {
//				Resource resource = (Resource) clipsShotIt.next();
//				
//			}
//            
//            while (clipsIt.hasNext()) {
//				Resource clip = (Resource) clipsIt.next();
//				
//			}
//
//            
//                
                
 /*               
                
                Set<Resource> targetScripts = new HashSet<Resource>();
                Resource matchedScript = null;
                Resource linkScript = null;
                
                Property typeProp = scriptModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
                Resource scriptClass = scriptModel.getResource("http://filmontology.org/ontology/2.0/Script");
                ResIterator it = scriptModel.listResourcesWithProperty(typeProp, scriptClass);
                
                if (!it.hasNext()) {
                	logger.error("No script found in RDF data under tailr key "+key);
                	return;
                }
                
                
                while (it.hasNext()) {
                	targetScripts.add( (Resource) it.next());
                }
                
                logger.info("Scripts found under tailr key "+key+": "+targetScripts);

                Property clipsShotProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/clipsShot");
                ResIterator it2 = aleModel.listResourcesWithProperty(clipsShotProp);
                    
                if (!it2.hasNext()) {
                	logger.error("No script found in RDF data of file "+file);
                	return;
                }
                
				Resource resource = (Resource) it2.next();
				
				for (Resource ts : targetScripts) {
					if (resource.getURI().equals(ts.getURI())) {
						matchedScript = resource;
						linkScript = ts;
					}
				}

	            if (matchedScript == null) {
	            	logger.error("No matching script found under tailr key "+key+" and file "+file);
	            	return;
	            } 
                    	
                logger.info("Matched script for linking: "+matchedScript.getURI());
	            
//            	Map<String,String> sceneIndex = new HashMap<String, String>();
            	
                Property sceneProp = scriptModel.getProperty("http://filmontology.org/ontology/2.0/hasScene");
                StmtIterator listProperties = linkScript.listProperties(sceneProp);
                while (listProperties.hasNext()) {
					Statement statement = (Statement) listProperties.next();
					Resource scene = (Resource) statement.getObject();
	                Property sceneNumProp = scriptModel.getProperty("http://filmontology.org/ontology/2.0/sceneNumber");
	                Statement numStat = scene.getProperty(sceneNumProp);	                
	                String sceneNumber = numStat.getString();
	                sceneIndex.put(sceneNumber,numStat.getSubject().getURI());
                }
                
                if (sceneIndex.size() == 0) {
	            	logger.error("No scenes found for script "+linkScript.getURI());
	            	return;
                }
                
                logger.info("Scenes found: "+sceneIndex.size());

            	Map<String,Resource> shotIndex = new HashMap<String, Resource>();
            	Map<String,Resource> takeIndex = new HashMap<String, Resource>();

                StmtIterator listProperties2 = matchedScript.listProperties(clipsShotProp);
                
                while (listProperties2.hasNext()) {
                	
                    Property aleTypeProp = aleModel.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
                    Resource shotClass = aleModel.getResource("http://filmontology.org/ontology/2.0/Shot");
                    Resource takeClass = aleModel.getResource("http://filmontology.org/ontology/2.0/Take");
	                Property shotNumberProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/shotNumber");
	                Property takeNumberProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/takeNumber");
	                Property hasClipProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/hasClip");
	                Property hasTakeProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/hasTake");
	                Property nameProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/name");

                	
					Statement statement = (Statement) listProperties2.next();
					Resource clip = (Resource) statement.getObject();
	                Property sceneNumProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/sceneNumber");
	                Statement numStat = clip.getProperty(sceneNumProp);
	                String clipSceneNumber = numStat.getString();
	                String targetSceneUri = sceneIndex.get(clipSceneNumber);
	                
	                String slateNum = getPropertyValue(aleModel, clip, "http://filmontology.org/ontology/2.0/slate");
	                String takeNum = getPropertyValue(aleModel, clip, "http://filmontology.org/ontology/2.0/take");
	                String shotDescription = getPropertyValue(aleModel, clip, "http://filmontology.org/ontology/2.0/description");
	                

	                Property hasShotProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/hasShot");
	                
	                if (targetSceneUri == null) {
	                	logger.warn("No matching scene found for clip "+clip.getURI());
	                } else {
	                	logger.info("Scene "+clipSceneNumber+" - Shot "+slateNum+" - Take "+takeNum+" : "+clip.getURI()+" matched with "+targetSceneUri);

	                	Resource shot = shotIndex.get(clipSceneNumber+"-"+slateNum);
	                	if (shot == null) {
		                	UUID uuid = UUID.randomUUID();
		                	shot = aleModel.getResource("http://filmontology.org/resource/Shot/"+uuid.toString());
		                	shotIndex.put(clipSceneNumber+"-"+slateNum, shot);
		                	shot.addProperty(typeProp, shotClass);
		                	shot.addProperty(shotNumberProp, slateNum);
		                	if (!"".equals(shotDescription)) {
		                		shot.addProperty(nameProp, shotDescription);
		                	}
		                	
		                	Resource targetScene = aleModel.getResource(targetSceneUri);
		                	targetScene.addProperty(hasShotProp, shot);
	                	}

	                	UUID uuid = UUID.randomUUID();
	                	Resource take = aleModel.getResource("http://filmontology.org/resource/Take/"+uuid.toString());
	                	take.addProperty(typeProp, takeClass);
	                	take.addProperty(takeNumberProp, takeNum);	          
	                	take.addProperty(hasClipProp, clip);
	                	shot.addProperty(hasTakeProp, take);
	                }
				}
                
                
                String name = StringUtils.removeEnd(file, ".nt");
                name = name + "-linked.nt";
                
                aleModel.setNsPrefix("for", "http://filmontology.org/resource/");
                aleModel.setNsPrefix("foo", "http://filmontology.org/ontology/2.0/");

                RDFDataMgr.write(new FileOutputStream(name), aleModel, RDFLanguages.NTRIPLES);

                
                */ 
                
//                System.out.println(sceneIndex);


                

                
//                String triples = convertMementoToNtTriples(m);
//                StringReader sr = new StringReader(triples);
//                Model model = ModelFactory.createDefaultModel();
//
//                
//                System.out.println(m.toString());
//                
                                
//                StringReader sr = new StringReader(s)
//                
//                Model model = ModelFactory.createDefaultModel();
//                RDFDataMgr.read(model,
//
//                
//                Graph g = m.resolve();
//                Model model = ModelFactory.createModelForGraph(g);
                
                
//                Property typeProp = model.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
//                Resource scriptClass = model.getResource("http://filmontology.org/ontology/2.0/Script");                
//                ResIterator it = model.listResourcesWithProperty(typeProp, scriptClass);
//                
//                while (it.hasNext()) {
//					Resource resource = (Resource) it.next();
//					System.out.println(resource);
//				}
                
                
                
//                String old = convertMementoToNtTriples(m);
//                String input = convertToNtTriples(new FileInputStream(file), RDFLanguages.nameToLang(lang));


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

    /* check if an inputstream is valid rdf */
    private String convertToNtTriples(InputStream stream, Lang format) {
        StringWriter writer = new StringWriter();
        try {
            Model m = ModelFactory.createDefaultModel();
            RDFDataMgr.read(m, stream, format);
            RDFDataMgr.write(writer, m, Lang.NT);
        } catch (RiotException e) {
            logger.error("Failed to convert the input to n-triples.", e);
        }
        return writer.toString();
    }

    /* check if an inputstream is valid rdf */
    private String convertMementoToNtTriples(Memento m) {
        StringWriter writer = new StringWriter();
        try {
            Graph g = m.resolve();
            RDFDataMgr.write(writer, g, Lang.NT);
        } catch (RiotException | UnsupportedEncodingException | URISyntaxException e) {
            logger.error("Failed to convert the input to n-triples.", e);
        } catch (IOException e) {
            logger.error("Unable to resolve memento.", e);
        }
        return writer.toString();
    }

    private void merge(String old, String input) {
        Map<String, Set<String>> file1 = new HashMap<>();
        Map<String, Set<String>> file2 = new HashMap<>();
        String merged = "";

        String[] split = old.split("\n");
        for (String s : split) {
            String[] words = s.split(" ");

            String rest = "";
            for (int i = 2; i < words.length; i++) {
                rest = rest + words[i] + " ";
            }

            String subpred = words[0] + " " + words[1];

            Set<String> set = file1.get(subpred);
            if (set == null) {
                set = new HashSet<>();
                file1.put(subpred, set);
            }
            set.add(rest);
        }

        split = input.split("\n");
        for (String s : split) {
            String[] words = s.split(" ");

            String rest = "";
            for (int i = 2; i < words.length; i++) {
                rest = rest + words[i] + " ";
            }

            String subpred = words[0] + " " + words[1];

            Set<String> set = file2.get(subpred);
            if (set == null) {
                set = new HashSet<>();
                file2.put(subpred, set);
            }
            set.add(rest);
        }

        Set<String> keys = new HashSet<>();
        keys.addAll(file1.keySet());
        keys.addAll(file2.keySet());

        for (String key : keys) {
            Set<String> dq = file1.get(key);
            Set<String> pp = file2.get(key);

            if (dq == null) {
                for (String s : pp) {
                    merged = merged + key+" "+s+"\n";
                }
            }

            if (pp == null) {
                for (String s : dq) {
                    merged = merged + key+" "+s+"\n";
                }
            }

            if (dq != null && pp != null) {
//				String[] split = key.split(" ");
//				if (datProps.contains(split[1])) {
//
//				}

                for (String s : pp) {
                    merged = merged + key+" "+s+"\n";
                }
            }
        }

        try {
            String name = StringUtils.removeEnd(file, "." + lang);
            name = name + "-merged.nt";
            Files.write(Paths.get(name), merged.getBytes("UTF8"));
        } catch (IOException e) {
            logger.error("Failed to write merged file.", e);
        }
    }

}
