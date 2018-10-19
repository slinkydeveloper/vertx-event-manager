package io.slinkydeveloper.events.persistance.mongodb;

import io.slinkydeveloper.events.impl.BaseEventManagerIntegrationTest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

public class MongoDbEventManagerIntegrationTest extends BaseEventManagerIntegrationTest<MongoDbEventPersistenceManager> {

  MongoDbRunner runner;
  MongoClient client;

  @Override
  public Future<MongoDbEventPersistenceManager> loadPersistenceManager(Vertx vertx) throws Exception {
    this.runner = new MongoDbRunner();
    this.runner.startMongo();
    this.client = MongoClient.createShared(
        vertx,
        runner.getConfig()
    );
    Future<MongoDbEventPersistenceManager> fut = Future.future();
    this.client.createCollection("eventManager", ar -> {
      if (ar.succeeded()) {
        fut.complete(new MongoDbEventPersistenceManager(client, "eventManager"));
      } else {
        fut.fail(ar.cause());
      }
    });
    return fut;
  }

  @Override
  public void wipePersistence(MongoDbEventPersistenceManager persistance, VertxTestContext testContext, Checkpoint completeCheck) {
    this.client.remove("eventManager", new JsonObject(), testContext.succeeding(v -> completeCheck.flag()));
  }

  @AfterAll
  public void stopDb() {
    this.runner.stopMongo();
  }
}
