Dwerft Service API 0.1
======================

This API provides an uniform interface to the Dwerft project server.
The service provides access to change, get or delete existing data on the
SPARQL backend.  
Therefore we try to provide a lean and neat API.

### General Considertions ###

The API should provide several features for all types of request.  

* First it should response with correct HTTP status codes most of the time.  
* The default output should be RDF/JSON but you can choce to switch to RDF/XML
    with the `?output=xml` flag.  
* There should be a pretty print and compressed output. 
    The default should be compressed output and `?pretty=true` should response
    with a pretty printed output.  
* Acces is provided unter the url part `/api/`  
* Updates should return besides the correct Header informations also
  the updated subgraph.

### API ###

* `/api/query` provides acces to various informations.
  As an admin you can use `?free=$query` to send a query without limitations.
  Use converting with the `?convert=$filetype` option (filetype should be xml).
      * `/api/query/xml` is a shortcut for `/api/query/?convert=xml`
* `/api/update` provides access to update machanism.
  These methods accept xml and json and turtle for rdf as input, to choose use the correct media types.
  Use converting to rdf with the `?convert=$filetype` option (where filetype is mostly xml).
      * `/api/update/xml` is a shortcut for `/api/update?convert=xml`
* `/api/delete` provides a delete mechanism.
  
