## Upload Service

This document describes the upload web service provided
used by the dwerft tools. The upload service decouples the 
conversion of different kinds of formats into rdf and the
uploading and revision handling for a triple store.


This service is intended to be a restful web service that can either
be deployed within a servlet container like Tomcat or to run from
the command line using jetty. 

For revision handling [tailr](tailr.s16a.org) is used, since it
allows storing of arbitrary RDF files and generating deltas between
two files. For uploading RDF data to a triple store SPARQL Update 1.1 is
used. 

The API is described below as well as some remarks on special
cases and background functionality.

### Running
Checkout the latest version from Github and run with  
 `mvn jetty:run` or `mvn jetty:run -Djetty.port=8080`

### API
After that the API is reachable under [localhost:8080/api](localhost:8080/api).
A full documentation is given via swagger json and yaml. These can be found under 
[localhost:8080/api/swagger.json](localhost:8080/api/swagger.json) and 
[localhost:8080/api/swagger.yaml](localhost:8080/api/swagger.yaml).


### Remarks

