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
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.AnonId;
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

    @Override
    public void run() {
        super.run();
        logger.debug("Linking ale file " + file);
        
        System.out.println(file);

        try {
            TailrClient client = getClient();
            Repository repo = new Repository(config.getTailrUser(), config.getTailrRepo());

            try {
                Memento m = client.getLatestMemento(repo, key);               
                Graph g = m.resolve();                
                Model scriptModel = ModelFactory.createModelForGraph(g);
                
                Model aleModel = ModelFactory.createDefaultModel();
                RDFDataMgr.read(aleModel, new FileInputStream(file), RDFLanguages.nameToLang(lang));
                
                
                
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
	            
            	Map<String,String> sceneIndex = new HashMap<String, String>();
            	
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
	                
	                Property slateProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/slate");
	                Statement slateStat = clip.getProperty(slateProp);
	                String slateNum = slateStat.getString();
	                
	                Property takeProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/take");
	                Statement takeStat = clip.getProperty(takeProp);
	                String takeNum = takeStat.getString();

	                Property descProp = aleModel.getProperty("http://filmontology.org/ontology/2.0/description");
	                Statement descStat = clip.getProperty(descProp);
	                String shotDescription = "";
	                
	                if (descStat != null) {
	                	shotDescription = descStat.getString();
	                }

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

            } catch (IOException e) {
                /* key not found */
                logger.info("Ne previous version found. Just upload the file as usual.");
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

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
