#!/bin/sh

# execute all steps for the ada project in a row

# set path of ada-dev project
ADA=~/Entwicklung/repos/AdA-dev/code/ontology_converter

# create io directories
mkdir -p ada_ont/
mkdir -p ada_out/

# pull recent changes (hopes for no local recent changes)
DWERFT=`pwd`
cd $ADA && git pull
cp ./* $DWERFT/ada_ont
cd $DWERFT

mvn clean compile exec:java -Dexec.mainClass="de.werft.tools.general.DwerftTools" -Dexec.args="convert -u http://ada.filmontology.org/ontology/ ada_in/ada_types.tsv ada_out/ada_types.ttl ada_ont/ada_types.rml.ttl"
mvn exec:java -Dexec.mainClass="de.werft.tools.general.DwerftTools" -Dexec.args="convert -u http://ada.filmontology.org/ontology/ ada_in/ada_level.tsv ada_out/ada_level.ttl ada_ont/ada_level.rml.ttl "
mvn exec:java -Dexec.mainClass="de.werft.tools.general.DwerftTools" -Dexec.args="convert -u http://ada.filmontology.org/ontology/ ada_in/ada_values.tsv ada_out/ada_values.ttl ada_ont/ada_values.rml.ttl"
mvn exec:java -Dexec.mainClass="de.werft.tools.general.DwerftTools" -Dexec.args="merge -f rdf/xml ada_out/ada.owl ada_out/ada_level.ttl ada_out/ada_types.ttl ada_out/ada_values.ttl ada_ont/ada_basic_ontology.owl"

DATE=`date -I`
sed -i "s/modified>.*<\/dcterms/modified>$DATE<\/dcterms/" ada_out/ada_owl.ttl 

DATE=`date +%Y/%m`
sed -i "s:ada.filmontology.org/ontology/:ada.filmontology.org/ontology/$DATE/:" ada_out/ada_owl.ttl 
sed -i "s:ada.filmontology.org/resource/:ada.filmontology.org/resource/$DATE/:" ada_out/ada_owl.ttl 
