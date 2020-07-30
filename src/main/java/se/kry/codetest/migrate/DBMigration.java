package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import se.kry.codetest.DBConnector;

public class DBMigration {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    DBConnector connector = new DBConnector(vertx);
//    connector.query("DROP TABLE service").setHandler(done -> {
//        if(done.succeeded()){
//          System.out.println("completed db migrations");
//       } else {
//          done.cause().printStackTrace();
//        }
//        vertx.close(shutdown -> {
//          System.exit(0);
//        });
//      });
    
    connector.query("CREATE TABLE IF NOT EXISTS service (name VARCHAR(128) primary key not null,value VARCHAR(128))").setHandler(done -> {
      if(done.succeeded()){
        System.out.println("completed db migrations");
      } else {
        done.cause().printStackTrace();
      }
      vertx.close(shutdown -> {
        System.exit(0);
      });
    });
  }
}
