package se.kry.codetest;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class BackgroundPoller {
	
	private String update = "UPDATE SERVICE SET STATUS = 'OK' WHERE NAME=?";
	
	private DBConnector connector;

  public Future<List<String>> pollServices(DBConnector connector,Map<String, JsonObject> services) {
    System.out.println("Inside Pollservices");
    
    //Poll each service and then update the status column
    services.forEach((key,value) -> checkService(key,value,connector));
    return Future.failedFuture("TODO");
    
  }

private void checkService(String key,JsonObject json,DBConnector connector) {
	

	try {
        URL url = new URL(json.getString("url"));
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(2000);
        conn.connect();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK||conn.getResponseCode()==301||conn.getResponseCode()==302) {
        	//Update the status of the URL to OK
        	json.put("status","OK");
        	connector.query("UPDATE service set value = ? where name = ?",new JsonArray().add(json).add(key)).setHandler(res -> {
    	        if (res.succeeded()) {
    	           System.out.println("Status updated to OK");
    	            }
    	            
    	         else {
    	        	//Handle this properly later
    	        	
    	            System.out.println("Failed to update the status to OK");
    	            
    	        }
    	    });
        }
        else {
        	json.put("status","FAIL");
        	connector.query("UPDATE service set value = ? where name = ?",new JsonArray().add(json).add(key)).setHandler(res -> {
    	        if (res.succeeded()) {
    	           System.out.println("Status updated to FAIL");
    	            }
    	            
    	         else {
    	        	//Handle this properly later
    	            System.out.println("Failed to update the status to fail");
    	            
    	        }
    	    });
        }
        
    } 
	catch (Exception e) {
       e.printStackTrace();
        //Code to set the status to FAIL
       json.put("status","FAIL");
   	connector.query("UPDATE service set value = ? where name = ?",new JsonArray().add(json).add(key)).setHandler(res -> {
	        if (res.succeeded()) {
	           System.out.println("Status updated to FAIL");
	            }
	            
	         else {
	        	//Handle this properly later
	            System.out.println("Failed to update the status to fail");
	            
	        }
	    });
    }
	finally {
		System.out.println("INSIDE FINALLY");
	}

    System.out.println(" Done");
    
}
}

