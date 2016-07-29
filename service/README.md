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

The configuration is done under `src/main/resources/Service*`. 
The triple store enpoint and the Tailr URI, user and repository is defined
in `ServiceConfig.properties`, where the access keys are defined in
`ServiceKeys.properties` (Keys are not provided). 

### API
After that the API is reachable under [localhost:8080/api](localhost:8080/api).
A full documentation is given via swagger json and yaml. These can be found under 
[localhost:8080/api/swagger.json](localhost:8080/api/swagger.json) and 
[localhost:8080/api/swagger.yaml](localhost:8080/api/swagger.yaml).
   
The API consists of one action at the moment.   

* `/upload` - Is a PUT request accepting `application/octet-stream`  
    + `?key=` - Is required for uploading the provided byte stream to tailr
    + `&graph=` - Is optional for storing the model under a different graph in the triple store
    + `&level=` - Is optional and decides the update strategy for uploading the model to the triple store.
        * `0` - Level 0 indicates that the uploaded rdf model is a negative set. Thus means all triples from
            the model are removed from the triple store.
        * `1` - Level 1 let SPARQL handle the merging and the provided model will be uploaded as is.
            This means all triples are uploaded as new to the triple store and the store decides what to do.
        * `2` (default) - Level 2 indicates a full merge. Since tailr responses the new and deleted triples, we do two
            update request. the first removes all old triples from the triple store and the second adds all new
            triples.
    + `&lang` - is optional and specifies the rdf format (see [Apache Jena](https://jena.apache.org/documentation/io/rdf-input.html) for 
        more Information). Default is `ttl`.  
*   Possible Responses are:
    + 200 - Ok, indicating that the upload and storage request succeeds.
    + 204 - No Content, indicating that there is no uploaded content.
    + 206 - Not Acceptable, indicating that the provided file is not valid rdf.
    + 306 - Not Modified, indicating that Tailr is not reachable or returns error messages. The process stops there and returns.
    + 400 - Bad Request, indicating that the parameters provided are not valid or empty.

### Remarks
Here are some remarks and examples.  

An example for a Java client can be found under `src/test/de/werft/MyResourceTest.java`.  


Remarks:  
- Due to the Jena implementation there is no feedback if an upload was successfully.  
