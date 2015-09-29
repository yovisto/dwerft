# DWERFT Import/Export tools


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

The package `tools` contains sample code showing ways of how to transform various data formats like XML into valid RDF and vice versa. As this involves a few steps, each package is dedicated to one task. Let's say your aim is to store a script that has been written using Dramaqueen in the LPDC and later export all the information 
