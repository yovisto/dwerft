package de.dwerft.lpdc.exporter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;

public class LockitExporter extends RdfExporter {
	
	private String outputPath;
	
	public LockitExporter(File rdfInput, String outputPath) throws IOException {
		super(rdfInput);
		this.outputPath = outputPath;
	}
	
	
	@Override
	public void export() {
		
			writeSceneCSV(getScenesAsXML(""));
	}

	/**
	 * Restores a ResultSet from a given XML and writes a CSV file containing selected variables
	 * 
	 * TODO 
	 * Lockit importer only reads a certain (unknown encoding)
	 * Add missing variables
	 * 
	 * @param sceneXML
	 */
	private void writeSceneCSV(String sceneXML) {
		
		try {
			FileWriter writer = new FileWriter(outputPath);
			
			ResultSet sceneResults = ResultSetFactory.fromXML(sceneXML);
			
			while (sceneResults.hasNext()) {
				QuerySolution scene = sceneResults.next();
				
				writer.append(getResourceOrLiteralValue(scene, "sceneNumbers") + ";");
				
				//Merge environment info to one variable
				String intExt = getResourceOrLiteralValue(scene, "interiorExteriors");
				String dayNight = getResourceOrLiteralValue(scene, "dayTimes");
				writer.append(createLockitIAT(intExt, dayNight) + ";");
				
				writer.append(getResourceOrLiteralValue(scene, "estimatedTimes") + ";");
				
				////////////////////
				// TODO - Wenn man den kompletten Header nimmt, f�hrt das zu merkw�rdgem Erzeugen von Schaupl�tzen bei Lockit, deswegen erstmal der Split
				// TODO - Es gibt auch ein Problem mit dem Encoding, das Leerzeichen direkt nach dem Bindestrich ist irgendein Sonderzeichen
				String lit = getResourceOrLiteralValue(scene, "sceneHeaders");
				String[] split = lit.split("�-");				
				writer.append(split[1].trim() + ";");
				/////////////////
				writer.append(getResourceOrLiteralValue(scene, "sceneDescriptions") + ";");				
				writer.append("\n");
			}
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Generates the formatted String required by Lockit
	 * The String contains intExt and dayNight information
	 * 
	 * @param intExt
	 * @param dayNight
	 * @return
	 */
	private String createLockitIAT(String intExt, String dayNight) {
		
		StringBuilder sb = new StringBuilder();
		
		if (intExt.matches("int(ext)?"))
			sb.append("I/");
		if (intExt.matches("(int)?ext"))
			sb.append("A/");
		
		if (dayNight.equalsIgnoreCase("day"))
			sb.append("T");
		else
			sb.append("N");
		
		return sb.toString();
	}
}
