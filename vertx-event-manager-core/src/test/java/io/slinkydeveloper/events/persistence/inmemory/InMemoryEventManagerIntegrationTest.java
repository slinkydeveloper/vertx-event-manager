package io.slinkydeveloper.events.persistence.inmemory;

import io.slinkydeveloper.events.impl.BaseEventManagerIntegrationTest;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;

public class InMemoryEventManagerIntegrationTest extends BaseEventManagerIntegrationTest<InMemoryEventPersistenceManager> {
  @Override
  public InMemoryEventPersistenceManager loadPersistenceManager(Vertx vertx) {
    return new InMemoryEventPersistenceManager();
  }

  @Override
  public void wipePersistence(InMemoryEventPersistenceManager persistence, VertxTestContext testContext) {
    persistence.getEventsMap().clear();
    testContext.completeNow();
  }
}
