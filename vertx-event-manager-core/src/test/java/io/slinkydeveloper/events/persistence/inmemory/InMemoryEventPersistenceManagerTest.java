package io.slinkydeveloper.events.persistence.inmemory;

import io.slinkydeveloper.events.persistence.BaseEventPersistenceManager;
import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
@Timeout(value = 100, timeUnit = TimeUnit.MILLISECONDS)
public class InMemoryEventPersistenceManagerTest extends BaseEventPersistenceManager {

  @BeforeEach
  public void before(Vertx vertx, VertxTestContext testContext) {
    this.persistance = new InMemoryEventPersistenceManager();
    testContext.completeNow();
  }
}
