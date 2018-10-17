package io.slinkydeveloper.events.impl;

import io.slinkydeveloper.events.*;
import io.slinkydeveloper.events.logic.EventLogicManager;
import io.slinkydeveloper.events.persistence.EventPersistenceManager;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseEventManagerIntegrationTest<T extends EventPersistenceManager> {

  EventManagerImpl eventManager;
  T persistance;
  EventLogicManager logicManager;

  public abstract T loadPersistenceManager(Vertx vertx);
  public abstract void wipePersistence(T persistance, VertxTestContext testContext);

  @BeforeAll
  void before(Vertx vertx, VertxTestContext testContext) {
    this.persistance = loadPersistenceManager(vertx);
    this.logicManager = EventLogicManager.create();
    this.eventManager = new EventManagerImpl(vertx, persistance, logicManager);
    wipePersistence(this.persistance, testContext);
  }

  @AfterEach
  void wipe(VertxTestContext testContext) {
    this.eventManager.stop();
    wipePersistence(persistance, testContext);
  }

  @Test
  public void registerAndRunEventTest(Vertx vertx, VertxTestContext test) {
    this.eventManager.start(test.succeeding((v) -> {
      Checkpoint eventHandlerCheckpoint = test.strictCheckpoint();
      Checkpoint eventCreationCheckpoint = test.strictCheckpoint();
      Checkpoint eventQueryAfterCompleted = test.strictCheckpoint();
      Checkpoint eventQueryBeforeCompleted = test.strictCheckpoint();

      this.logicManager.addEventType("anEvent", e -> {
        test.verify(() -> assertEquals(EventState.RUNNING, e.getState()));
        eventHandlerCheckpoint.flag();
        return Future.succeededFuture(e.getEventData().copy().put("key", "result"));
      });

      Event event = Event.createPendingEvent(
          ZonedDateTime.now(),
          ZonedDateTime.now().plus(Duration.ofMillis(500)),
          "anEvent",
          new JsonObject().put("key", "data")
      );

      this.eventManager.registerEvent(event, test.succeeding(eventId -> {
        test.verify(() -> assertNotNull(eventId));
        eventCreationCheckpoint.flag();
        this.eventManager.getEvent(eventId, test.succeeding(res1 -> {
          test.verify(() -> assertEquals(EventState.PENDING, res1.getState()));
          eventQueryBeforeCompleted.flag();
          vertx.setTimer(1000, id -> {
            this.eventManager.getEvent(eventId, test.succeeding(res2 -> {
              test.verify(() -> assertEquals(new JsonObject().put("key", "result"), res2.getEventResult()));
              test.verify(() -> assertEquals(EventState.COMPLETED, res2.getState()));
              eventQueryAfterCompleted.flag();
            }));
          });
        }));
      }));
    }));
  }

  @Test
  public void registerEventThatFailAndRunEventTest(Vertx vertx, VertxTestContext test) {
    this.eventManager.start(test.succeeding((v) -> {
      Checkpoint eventHandlerCheckpoint = test.strictCheckpoint();
      Checkpoint eventCreationCheckpoint = test.strictCheckpoint();
      Checkpoint eventQueryAfterCompleted = test.strictCheckpoint();
      Checkpoint eventQueryBeforeCompleted = test.strictCheckpoint();

      this.logicManager.addEventType("anEvent", e -> {
        eventHandlerCheckpoint.flag();
        throw new IllegalStateException("Sbam");
      });

      Event event = Event.createPendingEvent(
          ZonedDateTime.now(),
          ZonedDateTime.now().plus(Duration.ofMillis(100)),
          "anEvent",
          new JsonObject().put("key", "data")
      );

      this.eventManager.registerEvent(event, test.succeeding(eventId -> {
        test.verify(() -> assertNotNull(eventId));
        eventCreationCheckpoint.flag();
        this.eventManager.getEvent(eventId, test.succeeding(res1 -> {
          test.verify(() -> assertEquals(EventState.PENDING, res1.getState()));
          eventQueryBeforeCompleted.flag();
          vertx.setTimer(1000, id -> {
            this.eventManager.getEvent(eventId, test.succeeding(res2 -> {
              test.verify(() -> assertEquals("Sbam", res2.getEventError().getMessage()));
              test.verify(() -> assertEquals(EventState.ERROR, res2.getState()));
              eventQueryAfterCompleted.flag();
            }));
          });
        }));
      }));
    }));
  }

  @Test
  public void registerAndUnregisterEventTest(Vertx vertx, VertxTestContext test) {
    this.eventManager.start(test.succeeding((v) -> {
      Checkpoint eventCreationCheckpoint = test.strictCheckpoint();
      Checkpoint deleteEvent = test.strictCheckpoint();
      Checkpoint afterDelete = test.strictCheckpoint();
      Checkpoint afterDeleteQuery = test.strictCheckpoint();

      this.logicManager.addEventType("anEvent", e -> {
        test.failNow(new IllegalStateException("Must never be called"));
        return Future.succeededFuture();
      });

      Event event = Event.createPendingEvent(
          ZonedDateTime.now(),
          ZonedDateTime.now().plus(Duration.ofSeconds(1000)),
          "anEvent",
          new JsonObject().put("key", "data")
      );

      this.eventManager.registerEvent(event, test.succeeding(eventId -> {
        test.verify(() -> assertNotNull(eventId));
        eventCreationCheckpoint.flag();
        this.eventManager.unregisterEvent(eventId, test.succeeding(r -> {
          test.verify(() -> assertFalse(this.eventManager.getTimersId().containsKey(eventId)));
          deleteEvent.flag();
          this.eventManager.getEvent(eventId, test.succeeding(e -> {
            test.verify(() -> assertNull(e));
            afterDeleteQuery.flag();
          }));
          vertx.setTimer(2000, id -> { // Wait to check if the event is triggered
            afterDelete.flag();
          });
        }));
      }));
    }));
  }

  @Test
  public void startAndStopEventManagerTest(Vertx vertx, VertxTestContext test) {
    this.eventManager.start(test.succeeding((v) -> {
      Checkpoint eventHandler = test.strictCheckpoint();
      Checkpoint eventRegister = test.strictCheckpoint();
      Checkpoint restart = test.strictCheckpoint();
      Checkpoint starvingEventCheck = test.strictCheckpoint();
      Checkpoint completedEventCheck = test.strictCheckpoint();

      this.logicManager.addEventType("anEvent", e -> {
        test.verify(() -> assertEquals(EventState.RUNNING, e.getState()));
        eventHandler.flag();
        return Future.succeededFuture(e.getEventData().copy().put("key", "result"));
      });

      Event event = Event.createPendingEvent(
          ZonedDateTime.now(),
          ZonedDateTime.now().plus(Duration.ofMillis(500)),
          "anEvent",
          new JsonObject().put("key", "data")
      );

      Event baitEvent = Event.createRunningEvent(ZonedDateTime.now().minusHours(1), ZonedDateTime.now(), "anEvent", new JsonObject());

      this.eventManager.registerEvent(event, test.succeeding(eventId -> {
        eventRegister.flag();
        this.eventManager.stop();
        this.persistance.addEvent(baitEvent).setHandler(test.succeeding(baitEventAdded -> {
          this.eventManager.start(test.succeeding(v2 -> {
            restart.flag();
            test.verify(() -> assertEquals(1, this.eventManager.getTimersId().size()));
            vertx.setTimer(1000, l -> {
              this.persistance.getEvent(event.getId()).setHandler(test.succeeding(e -> {
                test.verify(() -> assertEquals(EventState.COMPLETED, e.getState()));
                completedEventCheck.flag();
              }));
              this.persistance.getEvent(baitEventAdded.getId()).setHandler(test.succeeding(e -> {
                test.verify(() -> assertEquals(EventState.STARVING, e.getState()));
                starvingEventCheck.flag();
              }));
            });
          }));
        }));
      }));
    }));
  }

}
