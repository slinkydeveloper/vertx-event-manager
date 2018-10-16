package io.slinkydeveloper.events.impl;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class InMemoryEventPersistanceManagerTest extends BaseEventPersistanceManager {
  @Override
  @BeforeEach
  void before(Vertx vertx, VertxTestContext testContext) {
    this.persistance = new InMemoryEventPersistanceManager();
    testContext.completeNow();
  }
}
