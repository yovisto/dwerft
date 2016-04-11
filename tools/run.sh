#!/bin/sh
#
# Run the dwerft tools
#

echo "Running dwerft tools"

args=$*
mvn clean compile exec:java -Dexec.mainClass="de.werft.tools.general.DwerftTools" -Dexec.args="$args"
