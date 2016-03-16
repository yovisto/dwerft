# DWERFT Linked Production Data Cloud

**Table of Contents**
- [Introduction](#introduction)
  - [Semantic Web](#semantic-web)
  - [Resource Description Framework](#resource-description-framework)
  - [Apache Jena](#apache-jena)
  - [SPARQL](#sparql-syntax)
- [Project details](#project-details)
  - [Scope](#scope)
  - [Linked Production Data Cloud](#linked-production-data-cloud)
  - [Tools](#tools)
- [Setting up the LPDC framework](#setting-up-the-lpdc-framework)
  - [Prepackaged version](#prepackaged-version)
  - [Manual setup](#manual-setup)
  - [Valid arguments](#valid-arguments)
- [Package structure](#package-structure)
  - [Mapping](#mapping)
- [Sample workflow](#sample-workflow)

## Introduction

*Note: All text snippets in the introduction are taken from [Wikipedia](https://en.wikipedia.org/wiki/Main_Page).*

#### Semantic Web

The Semantic Web is an extension of the Web through standards by the World Wide Web Consortium (W3C). The standards promote common data formats and exchange protocols on the Web, most fundamentally the Resource Description Framework (RDF).

According to the W3C, "The Semantic Web provides a common framework that allows data to be shared and reused across application, enterprise, and community boundaries". The term was coined by Tim Berners-Lee for a web of data that can be processed by machines.

[Read more](https://en.wikipedia.org/wiki/Semantic_Web)

#### Resource Description Framework

The RDF data model is similar to classical conceptual modeling approaches such as entity–relationship or class diagrams, as it is based upon the idea of making statements about resources (in particular web resources) in the form of subject–predicate–object expressions. These expressions are known as triples in RDF terminology. The subject denotes the resource, and the predicate denotes traits or aspects of the resource and expresses a relationship between the subject and the object. For example, one way to represent the notion "The sky has the color blue" in RDF is as the triple: a subject denoting "the sky", a predicate denoting "has", and an object denoting "the color blue". Therefore, RDF swaps object for subject that would be used in the classical notation of an entity–attribute–value model within object-oriented design; Entity (sky), attribute (color) and value (blue). RDF is an abstract model with several serialization formats (i.e., file formats), and so the particular way in which a resource or triple is encoded varies from format to format.

[Read more](https://en.wikipedia.org/wiki/Resource_Description_Framework)

#### Apache Jena

Apache Jena is an open source Semantic Web framework for Java. It provides an API to extract data from and write to RDF graphs. The graphs are represented as an abstract "model". A model can be sourced with data from files, databases, URLs or a combination of these. A Model can also be queried through SPARQL 1.1.

[Read more](https://en.wikipedia.org/wiki/Jena_(framework))

[Jena Project Page](https://jena.apache.org/index.html)

#### SPARQL Syntax

SPARQL [..] is an RDF query language, that is, a semantic query language for databases, able to retrieve and manipulate data stored in Resource Description Framework (RDF) format. It was made a standard by the RDF Data Access Working Group (DAWG) of the World Wide Web Consortium, and is recognized as one of the key technologies of the semantic web.

[Read more](https://en.wikipedia.org/wiki/SPARQL)

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

## Setting up the LPDC framework

#### Prepackaged version

This might be the easiest way to try the lpdc tools.
We provide a prepackaged version [here](https://github.com/yovisto/dwerft/releases).  
You only need a current Java version (1.8) to run the jar file.

The structure is as following:
```
config/ - contains DwerftConfig.properties
examples/ - some examples to try the tool
ontology/ - provides the ontology file
mapping/ provides different mappings
```
To use it, run:
  ```
  java -jar dwerft-tools.jar <your arguments>
  ```
For the available arguments see further down this readme.

#### Manual setup

Manually setting up the framework requires Apache Maven. So start of by getting Maven over [here](https://maven.apache.org/) if you haven't already done so. Next, clone the git repository to a destination of your choice.
  
  ```
  mkdir ~/dwerft_lpdc
  git clone https://github.com/yovisto/dwerft.git ~/dwerft_lpdc
  ```

If you plan on using an operation which utilizes the Preproducer API, make sure to provide valid credentials. Simply copy the template we provide to the `tools` directory, and with an editor of your choice add your credentials. Adjust other settings to your needs. 
  
  ```
  cd ~/dwerft_lpdc/tools
  cp src/main/resources/DwerftConfig.properties .
  vim DwerftConfig.properties
  ```

Irrespective of whether you added credentials or not, you can now execute the lpdc project by running the run script provided. The various arguments available are listed in the next section.
  
  ```
  ./run.sh <your arguments>
  ```
  
The framework comes with a selection of available operations available for instant usage via the `run.sh`, which are listed below. However, due to the nature of the dwerft tool package not all conversions are currently supported.

  - DramaQueen XML to RDF
  - PreProducer API to RDF
  - Custom XML to RDF
  - CSV/ALE to XML/RDF

#### Valid arguments

All available options are listed below and can also be viewed by using the `-help` option. Please note that querying the Preproducer API requires valid credentials.

 - `-convert` : Converts from one format to another. We determine the right conversion from the file extensions.
    + Usage: `<input> <output> <mapping>`
    +  valid input files are 
      * `*.dq` for dramaqueen (needs a rdf output file); 
      * `*.(csv|ale)` for csv (converts without output file to xml, otherwise to rdf with a mapping file)
      * `*.xml` for generic conversion (needs rdf output file and a mapping)
      *  no input file for preproducer (needs a rdf output file)
    + valid output files are `*.(rdf|ttl|nt)`
 - `-upload` : Uploads a rdf file to SPARQL endpoint (currently not supported).
 - `-print`: Prints the RDF output to console. Does not replace writing RDF to file.
 - `-format`: Specify an RDF output format. Available options are Turtle ('ttl'), N-Triples ('nt'), and TriG ('trig'). Default is Turtle.

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

*Note: While the framework does support numerous conversions, unfortunately not all are invokable via the CLI as mentioned above. The former are marked with an exlamation mark (!) in the workflow described below.*

For the following example, let's say our aim is to store a script that has been written using Dramaqueen in the LPDC, edit the script using Preproducer, and finally view the project in LockitNetwork. All interfaces work by storing generated RDF in the lpdc and generating tool specific XML. 

1. **(!) Dramaqueen to RDF:** So firstly, the challenge is to convert a Dramaqueen script to valid RDF. In order to achieve this, we simply feed our source, destination and mappings file to the `DramaqueenToRdf` converter in the package `importer.dramaqueen`. It will traverse the whole script and convert all attributes into RDF, making use of the mapping along the way. Make sure to take a look at the resulting file, it will give you an understanding of what RDF looks like.

2. **RDF to triple store:** Now that we have proper RDF, we still need to get this into the triple store mentioned earlier on. All classes responsible for uploading content to various APIs can be found in the package `sources` and implement the same interface. This interface consists of merely two methods, `get` and `send`. In our case, we'd send our RDF file to the triple store by utilizing the `send` method in the applicable class.

3. **Triple store to PreProducer Tool:** Exporting RDF back to tool-specific formats basically boils down to issuing the right queries and generating valid XML. The abstract class `RdfExporter` found in the package `exporter` provides some handy methods for extracting information from the triple store. It is vital however, that the correct ontology file is provided and the queries are free of errors. The `PreproducerExporter` extends the aformentioned abstract class and builds XML in a recursive manner. Again, take a look at the generated XML. Now, the XML file needs to be sent to the PreProducer API. As we learned before, the package `sources` contains all classes required for this kind of task. Since we can't publish any credentials you'll need your own. Simply add these to the `DwerftConfig.properties` file hidden in the `resources` folder and pass said file to the constructor of the `PreproducerSource`. Calling the method `send` will now upload the previously generated XML to Preproducer. You can now log on to your PreProducer account and review your upload. The data you sent will require confirmation. Your script is now viewable in the PreProducer frontend. 

4. **(!) PreProducer API to triple store:** Assuming extensive project editing has taken place using PreProducer, it is now time to update the lpdc with the latest data. As mentioned before, this requires valid credentials. Firstly, the data from PreProducer has to be converted to RDF. The class `PreProducerToRdf` resembles an extension to the basic `AbstractXMLToRDFConverter`. In order for all linking operations to work, the PreProducer API methods must be queried in a special order, which is conveniently stored in the `PreProducerToRdf` class and accessible by a getter method. Once the RDF file has been generated, it can now be uploaded to the triple store. This step is not available for use with the CLI. However, there is a `TripleStoreSource` class within the `sources` package, which provides a `send` method for uploading the previously generated RDF file to the lpdc.

5. **Triple store to LockitNetwork:** This is probably the easiest of all steps. Log in to your LockitNetwork account, click the import button, and finally the `DWERFT` tab. Now, simply copy the project`s URI into the given field and enjoy editing the project data from within LockitNetwork. 
