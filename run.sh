#!/bin/sh

echo "running dwerft tools"
args=$*
mvn clean compile
mvn exec:java -Dexec.mainClass="de.werft.tools.general.DwerftTools" -Dexec.args="$args"
