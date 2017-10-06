#!/bin/sh

# execute all steps for the ada project in a row



mvn clean compile exec:java -Dexec.mainClass="de.werft.tools.general.DwerftTools" -Dexec.args="convert -u http://ada.filmontology.org/ontology/ drive/ada_types.tsv ada_types.ttl drive/ada_types.rml.ttl"
mvn exec:java -Dexec.mainClass="de.werft.tools.general.DwerftTools" -Dexec.args="convert -u http://ada.filmontology.org/ontology/ drive/ada_level.tsv ada_level.ttl drive/ada_level.rml.ttl "
mvn exec:java -Dexec.mainClass="de.werft.tools.general.DwerftTools" -Dexec.args="convert -u http://ada.filmontology.org/ontology/ drive/ada_values.tsv ada_values.ttl drive/ada_values.rml.ttl"
mvn exec:java -Dexec.mainClass="de.werft.tools.general.DwerftTools" -Dexec.args="merge ada_owl.ttl ada_level.ttl ada_types.ttl ada_values.ttl drive/ada_ontology.owl"
