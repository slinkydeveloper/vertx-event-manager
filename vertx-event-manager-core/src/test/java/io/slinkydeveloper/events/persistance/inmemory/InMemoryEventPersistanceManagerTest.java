package io.slinkydeveloper.events.persistance.inmemory;

import io.slinkydeveloper.events.persistance.BaseEventPersistanceManager;
import io.slinkydeveloper.events.persistance.inmemory.InMemoryEventPersistanceManager;
import io.vertx.core.Vertx;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

@ExtendWith(VertxExtension.class)
@Timeout(value = 100, timeUnit = TimeUnit.MILLISECONDS)
public class InMemoryEventPersistanceManagerTest extends BaseEventPersistanceManager {
  @Override
  @BeforeEach
  public void before(Vertx vertx, VertxTestContext testContext) {
    this.persistance = new InMemoryEventPersistanceManager();
    testContext.completeNow();
  }
}
