package io.slinkydeveloper.events.persistence.inmemory;

import io.slinkydeveloper.events.impl.BaseEventManagerIntegrationTest;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxTestContext;

public class InMemoryEventManagerIntegrationTest extends BaseEventManagerIntegrationTest<InMemoryEventPersistenceManager> {

  @Override
  public Future<InMemoryEventPersistenceManager> loadPersistenceManager(Vertx vertx) throws Exception {
    return Future.succeededFuture(new InMemoryEventPersistenceManager());
  }

  @Override
  public void wipePersistence(InMemoryEventPersistenceManager persistance, VertxTestContext testContext, Checkpoint persistenceWipedCheck) {
    persistance.getEventsMap().clear();
    persistenceWipedCheck.flag();
  }
}
