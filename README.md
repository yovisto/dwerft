# DWERFT Linked Production Data Cloud


## Introduction

#### Semantic Web

#### RDF

#### Apache Jena

#### SPARQL Syntax


## So what's the point of this project?

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

If you'd like to try this out for yourself, start of by cloning the git repository to a destination of your choice. Since this is a Maven project, get Maven over [here](https://maven.apache.org/) if you haven't already done so.
Once these two steps are complete, run `mvn clean install` and you're good to go.

## Structure

If you have never worked with Apache Jena before, a good start would be to take a look at the `SparqlExample` found in the package `examples`. It is a demonstration of how to issue requests to a known end point.

The package `tools` contains sample code showing ways of how to transform various data formats like XML into valid RDF and vice versa. As this involves a few steps, each package is dedicated to one task. As with all conversions, one of the main problems is creating an exhaustive mapping. Conveniently, this has already been done for you. For now, mappings exist for Dramaqueen and PreProducer. 

For the following example, let's say our aim is to store a script that has been written using Dramaqueen in the LPDC and later export all the information to XML readable by PreProducer. For each step there is a test within the `test` package, which should help comprehend the way the tasks are processed in detail.

#### Step 1: Dramaqueen -> RDF

So firstly, the challenge is to convert a Dramaqueen script to valid RDF. In order to achieve this, we simply feed our source, destination and mappings file to the `DramaqueenToRdf` converter in the package `importer.dramaqueen`. It will traverse the whole script and convert all attributes into RDF, making use of the mapping along the way. Make sure to take a look at the resulting file, it will give you an understanding of what RDF looks like.

#### Step 2: RDF -> triple store

So now that we have proper RDF, we still need to get this into the triple store mentioned earlier on. All classes responsible for uploading content to various APIs can be found in the package `sources` and implement the same interface. This interface consists of merely two methods, `get` and `send`. In our case, we'd send our RDF file to the triple store by utilizing the `send` method in the applicable class.

#### Step 3: Triple store -> PreProducer XML

Exporting RDF back to tool-specific formats basically boils down to issuing the right queries and generating valid XML. The abstract class `RdfExporter` found in the package `exporter` provides some handy methods for extracting information from the triple store. It is vital however, that the correct ontology file is provided and the queries are free of errors. The `PreproducerExporter` extends the aformentioned abstract class and builds XML in a recursive manner. Again, take a look at the generated XML.

#### Step 4: PreProducer XML -> PreProducer Tool

Lastly, the XML file needs to be sent to the PreProducer API. As we learned before, the package `sources` contains all classes required for this kind of task. Since we can't publish any credentials you'll need your own. Simply add these to the `config.template` hidden in the `resources` folder and pass said file to the constructor of the `PreproducerSource`. Calling the method `send` will now upload the previously generated XML to Preproducer, where all the applicable data originally contained in the Dramaqueen script can now be viewed.
