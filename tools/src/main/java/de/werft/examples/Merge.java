package de.werft.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class Merge {

	public static void main(String[] args) {

		/*
        OntModel ontologyModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        
        InputStream is = null;
        
        try {
            is = new BufferedInputStream(new FileInputStream("ontology/dwerft-ontology_v2.owl"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ontologyModel.read(is, "RDF/XML");
        
        Set<String> objProps = new HashSet<String>();
        Set<String> datProps = new HashSet<String>();
        
        ExtendedIterator<ObjectProperty> objIt = ontologyModel.listObjectProperties();
        while (objIt.hasNext()) {
			ObjectProperty objectProperty = (ObjectProperty) objIt.next();
			objProps.add(objectProperty.getURI());
		}
                
        ExtendedIterator<DatatypeProperty> datIt = ontologyModel.listDatatypeProperties();
        while (datIt.hasNext()) {
			DatatypeProperty datatypeProperty = (DatatypeProperty) datIt.next();
			datProps.add(datatypeProperty.getURI());
		}
        
        System.out.println(datProps);
        */
        
		Map<String, Set<String>> file1 = new HashMap<String, Set<String>>();
		Map<String, Set<String>> file2 = new HashMap<String, Set<String>>();
		
		String merged = "";
		
		try {
			Stream<String> lines = Files.lines(Paths.get("v2_testdreh_dq_v2.n3"));
			Iterator<String> iterator = lines.iterator();
			while (iterator.hasNext()) {
				String line = iterator.next();
				String[] split = line.split(" ");
				
				String rest ="";
				for (int i = 2; i < split.length; i++) {
					rest = rest +split[i]+" "; 
				}
				
				String subpred = split[0]+" "+split[1];
				
				Set<String> set = file1.get(subpred);
				if (set == null) {
					set = new HashSet<String>();
					file1.put(subpred, set);
				}
				set.add(rest);
			}
			lines.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			Stream<String> lines = Files.lines(Paths.get("v2_testdreh_pp.n3"));
			Iterator<String> iterator = lines.iterator();
			while (iterator.hasNext()) {
				String line = iterator.next();
				String[] split = line.split(" ");
				
				String rest ="";
				for (int i = 2; i < split.length; i++) {
					rest = rest +split[i]+" "; 
				}
				
				String subpred = split[0]+" "+split[1];
				
				Set<String> set = file2.get(subpred);
				if (set == null) {
					set = new HashSet<String>();
					file2.put(subpred, set);
				}
				set.add(rest);
			}
			lines.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Set<String> keys = new HashSet<String>();
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
			Files.write(Paths.get("./v2_testdreh_merged.n3"), merged.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
