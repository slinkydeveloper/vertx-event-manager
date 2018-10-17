package io.slinkdeveloper.events.persistance.mongodb;

import io.slinkydeveloper.events.persistance.BaseEventPersistanceManager;
import io.slinkydeveloper.events.persistance.inmemory.InMemoryEventPersistanceManager;
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
public class MongoDbEventPersistanceManagerTest extends BaseEventPersistanceManager {

  MongoClient client;

  @BeforeAll
  public void setup(Vertx vertx, VertxTestContext testContext) {
    this.client = MongoClient.createShared(
        vertx,
        new JsonObject().put("db_name", "test").put("connection_string", "mongodb://127.0.0.1:27017")
    );
    this.persistance = new MongoDbEventPersistanceManager(client, "eventManager");
    testContext.completeNow();
  }

  @AfterEach
  public void wipeDb(Vertx vertx, VertxTestContext testContext) {
    this.client.remove("eventManager", new JsonObject(), testContext.succeeding(v -> testContext.completeNow()));
  }

}
