#!/bin/sh

echo "Preparing runtime..."

cd "${BASH_SOURCE%/*}"

if [ -e DwerftConfig.properties ]
then
    echo "Using custom config file"
else
    echo "No config file found, using defaults"
    cp src/main/resources/DwerftConfig.properties .
fi

echo "Running dwerft tools"

args=$*

mvn clean compile
mvn exec:java -Dexec.mainClass="de.werft.tools.general.DwerftTools" -Dexec.args="$args"
