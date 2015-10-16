# DWERFT Linked Production Data Cloud

**Table of Contents**
- [Introduction](#introduction)
  - [Semantic Web](#semantic-web)
  - [RDF](#rdf)
  - [Apache Jena](#apache-jena)
  - [SPARQL](#sparql-syntax)
- [Project details](#project-details)
  - [Scope](#scope)
  - [Linked Production Data Cloud](#linked-production-data-cloud)
  - [Tools](#tools)
- [Installation](#installation)
- [Package structure](#package-structure)
  - [Mapping](#mapping)
- [Sample workflow](#sample-workflow)

## Introduction

#### Semantic Web

#### RDF

#### Apache Jena

#### SPARQL Syntax


## Project details

#### Scope

This project intends to provide samples demonstrating how to connect various tools to the linked production data cloud (lpdc). The tools in question are all used for film making. The aim is to extract as much meta data from the tools and store it in the lpdc. Next, this meta data is to be exported from the lpdc back to the tools, selecting only the data required by each.

#### Linked Production Data Cloud

The lpdc stores all data using a custom ontology which is still subject to changes and a triple store. It supports SPARQL queries.

#### Tools

The tools which have been integrated into the project thus far are as follows. We are hoping there will be more as the project progresses.

* [Dramaqueen](http://dramaqueen.info/)
* [Preproducer](http://www.preproducer.com/)
* [LockitNetwork](http://lockitnetwork.com/)

## Installation

If you'd like to try this out for yourself, start of by cloning the git repository to a destination of your choice.
  ```
  mkdir ~/dwerft_lpdc
  git clone https://github.com/yovisto/dwerft.git ~/dwerft_lpdc
  cd ~/dwerft_lpdc
  ```
Since this is a Maven project, get Maven over [here](https://maven.apache.org/) if you haven't already done so.
Once these two steps are complete, run 

```
mvn -Dmaven.test.skip=true clean install
cd tools
mvn exec:java -Dexec.mainClass="de.werft.tools.general.DwerftTools" -Dexec.args="*arguments of your choice*"
```

and you're good to go. Due to the nature of the dwerft tool package not all conversions are currently supported.
The following conversions are valid for use with the -c argument. Please note that querying the Preproducer API requires valid credentials.
 - Generating XML from RDF
    - triple store -> preproducer (-c ts prp -o /path/to/output)
    - triple store -> dramaqueen (-c ts dq -o /path/to/output)
 - Generating RDF from XML 
    - dramaqueen -> RDF (-c dq ts -i /path/to/dramaqueenXML -o /path/to/output)
 - Generating RDF from preproducer tool.
	  		preproducer -> RDF (-c prp ts -o /path/to/output)
Other valid arguments are the following:
 - -p : prints results to console
 - -h : prints the help page

## Package structure

If you have never worked with Apache Jena before, a good start would be to take a look at the `SparqlExample` found in the package `examples`. It is a demonstration of how to issue requests to a known end point.

The package `tools` contains sample code showing ways of how to transform various data formats like XML into valid RDF and vice versa. As this involves a few steps, each package is dedicated to one task. As with all conversions, one of the main problems is creating an exhaustive mapping. Conveniently, this has already been done for you. For now, mappings exist for Dramaqueen and PreProducer. 

`tools.exporter`: This package contains the abstract class 'RdfExporter', which provides helper methods useful when querying the triple store. As of now, two implementations of said class exist. `PreproducerExporter` generates an XML file and `LockitExporter` a CSV file.

`tools.general`: The file `DwerftTools` contains a main method, required for running the DWerft Suite with arguments. `OntologyConstants` is simply a collection of immutable values, like the triple store URL.

`tools.importer.general`: All classes responsible for mapping operations and parsing of XML files can be found in here. Import in this context means converting XML to valid RDF. It is planned to expand the import to upload the generated RDF directly into the triple store.

`tools.sources`:Finally, the sources package contains an interface used for communicating with the APIs of the tools and implementations for Dramaqueen, PreProducer and the triple store. 

#### Mapping

The DWERFT tools utilize a sophisticated mapping structure to ensure that as much XML can be converted to the LPDC and vice versa. New mappings can easily be written using the template in the `resource` folder of the project. Let's take a look at the sample mapping for preproducer XML below:

```
map77.xmlNodePath=/root/return/prp:project/prp:episode/prp:scene-group/prp:scene/prp:daynight
map77.conditionalAttributeName=
map77.conditionalAttributeValue=
map77.contentSource=TEXT_CONTENT
map77.contentElementName=
map77.targetOntologyClass=http://filmontology.org/ontology/1.0/Scene
map77.targetOntologyProperty=http://filmontology.org/ontology/1.0/dayTime
map77.targetPropertyType=DATATYPE_PROPERTY
```



## Sample workflow

For the following example, let's say our aim is to store a script that has been written using Dramaqueen in the LPDC and later export all the information to XML readable by PreProducer. For each step there is a test within the `test` package, which should help comprehend the way the tasks are processed in detail.

1. **Dramaqueen to RDF:** So firstly, the challenge is to convert a Dramaqueen script to valid RDF. In order to achieve this, we simply feed our source, destination and mappings file to the `DramaqueenToRdf` converter in the package `importer.dramaqueen`. It will traverse the whole script and convert all attributes into RDF, making use of the mapping along the way. Make sure to take a look at the resulting file, it will give you an understanding of what RDF looks like.

2. **RDF to triple store:** Now that we have proper RDF, we still need to get this into the triple store mentioned earlier on. All classes responsible for uploading content to various APIs can be found in the package `sources` and implement the same interface. This interface consists of merely two methods, `get` and `send`. In our case, we'd send our RDF file to the triple store by utilizing the `send` method in the applicable class.

3. **Triple store to PreProducer XML:** Exporting RDF back to tool-specific formats basically boils down to issuing the right queries and generating valid XML. The abstract class `RdfExporter` found in the package `exporter` provides some handy methods for extracting information from the triple store. It is vital however, that the correct ontology file is provided and the queries are free of errors. The `PreproducerExporter` extends the aformentioned abstract class and builds XML in a recursive manner. Again, take a look at the generated XML.

4. **PreProducer XML to PreProducer Tool:** Lastly, the XML file needs to be sent to the PreProducer API. As we learned before, the package `sources` contains all classes required for this kind of task. Since we can't publish any credentials you'll need your own. Simply add these to the `config.template` hidden in the `resources` folder and pass said file to the constructor of the `PreproducerSource`. Calling the method `send` will now upload the previously generated XML to Preproducer, where all the applicable data originally contained in the Dramaqueen script can now be viewed.
