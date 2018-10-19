package io.slinkydeveloper.events.persistance.mongodb;

import io.slinkydeveloper.events.impl.BaseEventManagerIntegrationTest;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.junit5.VertxTestContext;

public class MongoDbEventManagerIntegrationTest extends BaseEventManagerIntegrationTest<MongoDbEventPersistenceManager> {

  MongoClient client;

  @Override
  public MongoDbEventPersistenceManager loadPersistenceManager(Vertx vertx) {
    this.client = MongoClient.createShared(
        vertx,
        new JsonObject().put("db_name", "test").put("connection_string", "mongodb://127.0.0.1:27017")
    );
    return new MongoDbEventPersistenceManager(client, "eventManager");
  }

  @Override
  public void wipePersistence(MongoDbEventPersistenceManager persistance, VertxTestContext testContext) {
    this.client.remove("eventManager", new JsonObject(), testContext.succeeding(v -> testContext.completeNow()));
  }
}
