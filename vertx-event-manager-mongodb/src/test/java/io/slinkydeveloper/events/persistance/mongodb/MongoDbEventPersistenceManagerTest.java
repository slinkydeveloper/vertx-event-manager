package io.slinkydeveloper.events.persistance.mongodb;

import io.slinkydeveloper.events.persistence.BaseEventPersistenceManager;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
@Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MongoDbEventPersistenceManagerTest extends BaseEventPersistenceManager {

  MongoDbRunner runner;
  MongoClient client;

  @BeforeAll
  public void setup(Vertx vertx, VertxTestContext testContext) throws Exception {
    this.runner = new MongoDbRunner();
    this.runner.startMongo();
    this.client = MongoClient.createShared(
        vertx,
        this.runner.getConfig()
    );
    this.client.createCollection("eventManager", testContext.succeeding(ar -> {
      this.persistance = new MongoDbEventPersistenceManager(client, "eventManager");
      testContext.completeNow();
    }));
  }

  @AfterEach
  public void wipeDb(Vertx vertx, VertxTestContext testContext) {
    this.client.remove("eventManager", new JsonObject(), testContext.succeeding(v -> testContext.completeNow()));
  }

  @AfterAll
  public void stopDb() {
    this.runner.stopMongo();
  }

}
