package se.kry.codetest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle {

  private HashMap<String, JsonObject> services = new HashMap<>();
  //TODO use this
  private DBConnector connector;
  private BackgroundPoller poller = new BackgroundPoller();

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx);
    Router router = Router.router(vertx);
    Set<String> allowedHeaders = new HashSet<>();
    allowedHeaders.add("x-requested-with");
    allowedHeaders.add("Access-Control-Allow-Origin");
    allowedHeaders.add("origin");
    allowedHeaders.add("Content-Type");
    allowedHeaders.add("accept");
    allowedHeaders.add("X-PINGARUNER");

    Set<HttpMethod> allowedMethods = new HashSet<>();
    allowedMethods.add(HttpMethod.GET);
    allowedMethods.add(HttpMethod.POST);
    allowedMethods.add(HttpMethod.OPTIONS);
    /*
     * these methods aren't necessary for this sample, 
     * but you may need them for your projects
     */
    allowedMethods.add(HttpMethod.DELETE);
    allowedMethods.add(HttpMethod.PATCH);
    allowedMethods.add(HttpMethod.PUT);
    router.route().handler(BodyHandler.create());
    router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods));
    //services.put("https://www.kry.se", "UNKNOWN"); -- > Replaced this with services queried from the database.
    /*Querying the services from the database start
     */
    
    getServices();
    setRoutes(router);
            /*Querying the services from the database end
             */
    vertx.setPeriodic(1000*10, timerId -> poller.pollServices(connector,services));
    
    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8080, result -> {
          if (result.succeeded()) {
            System.out.println("KRY code test service started");
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        });
  }

  private void getServices() {
	  /*Querying the services from the database start
	     */
	    
	    connector.query("SELECT * FROM service").setHandler(res -> {
	        if (res.succeeded()) {
	            List<JsonObject> retrievedServices = res.result().getRows();

	            
	            
	            for (JsonObject service: retrievedServices) {
	                String name = service.getString("name");
	                String value = service.getString("value");
	                JsonObject jsonObject = new JsonObject(value);
	                services.put(name,jsonObject);
	                
	            }
	            
	        } else {
	        	//Handle this properly later
	            System.out.println("Retrieval failed: " + res.cause());
	            
	        }
	    });
	            /*Querying the services from the database end
	             */
  }
  private void setRoutes(Router router){
	 
    router.route("/*").handler(StaticHandler.create());
    router.get("/service").handler(req -> {
      getServices();//Call to Database and populate the services
      List<JsonObject> jsonServices = services
          .entrySet()
          .stream()
          .map(service ->
              new JsonObject()
                  .put("name", service.getKey())
                  .put("value", service.getValue()))
          .collect(Collectors.toList());
      req.response().setStatusCode(200)
          .putHeader("content-type", "application/json")
          .end(new JsonArray(jsonServices).encode());
    });
    
    
    router.post("/service/").handler(this::addService);
    
    router.delete("/service/:name").handler(this::removeService);
    
 
  }
  
  private void addService(RoutingContext routingContext) {
	  JsonObject json = routingContext.getBodyAsJson();
	  String name = json.getString("name");
      json.remove("name");
      LocalDateTime lt = LocalDateTime.now(); 
      json.put("date",lt.toString());
      json.put("status", "UNKNOWN");
      
     

      connector.query("INSERT INTO service (name,value) VALUES (?,?)", new JsonArray().add(name).add(json.toString())).setHandler(complete -> {
          if (complete.succeeded()) {
              System.out.println("Service added: " + name);
          } else {
              System.out.println("Unable to save the service to the db: " + complete.cause());
          }
      });
	  routingContext.response()
	      .setStatusCode(201)
	      .putHeader("content-type", "application/json; charset=utf-8")
	      .end(Json.encodePrettily(json));
	}
  

 /*Method to remove the service*/
  
  
  
  private void removeService(RoutingContext routingContext) {
	  String name = routingContext.request().getParam("name");
	  
	  try {
			connector.query("DELETE FROM service WHERE name = ?", new JsonArray().add(name)).setHandler(res -> {
		        if (res.succeeded()) {
		            System.out.println("Service deleted " + name);
		            //Delete from the services too
		            services.remove(name);
		           
		        } else {
		        	routingContext.response().setStatusCode(400).end("Deletion failed");
		        }
		    });
		}
		catch(Exception e) {
			e.printStackTrace();
			routingContext.response().setStatusCode(400).end("Deletion Failed");
		}
	      
	 
	  routingContext.response().setStatusCode(204).end("Deleted");
	}
  /*
   * Get the name and url of the service and add it in the DB
   * To be done: Validations
   */
  

 
}



