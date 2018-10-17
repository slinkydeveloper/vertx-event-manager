package io.slinkydeveloper.events.impl;

import io.slinkydeveloper.events.*;
import io.slinkydeveloper.events.logic.EventLogicManager;
import io.slinkydeveloper.events.persistence.inmemory.InMemoryEventPersistenceManager;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class EventManagerTest {

  EventManagerImpl eventManager;
  InMemoryEventPersistenceManager persistance;
  EventLogicManager logicManager;

  @BeforeEach
  void before(Vertx vertx, VertxTestContext testContext) {
    this.persistance = new InMemoryEventPersistenceManager();
    this.logicManager = EventLogicManager.create();
    this.eventManager = new EventManagerImpl(vertx, persistance, logicManager);
    testContext.completeNow();
  }

  @Test
  public void registerAndRunEventTest(Vertx vertx, VertxTestContext test) {
    this.eventManager.start(test.succeeding((v) -> {
      Checkpoint eventHandlerCheckpoint = test.strictCheckpoint();
      Checkpoint eventCreationCheckpoint = test.strictCheckpoint();
      Checkpoint retrieveResult = test.strictCheckpoint();

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
        test.verify(() -> assertTrue(this.persistance.getEventsMap().containsKey(eventId)));
        test.verify(() -> assertEquals(EventState.PENDING, this.persistance.getEventsMap().get(eventId).getState()));
        eventCreationCheckpoint.flag();
        vertx.setTimer(1000, id -> {
          this.eventManager.getEvent(eventId, test.succeeding(res -> {
            test.verify(() -> assertEquals(new JsonObject().put("key", "result"), res.getEventResult()));
            test.verify(() -> assertEquals(new JsonObject().put("key", "result"), this.persistance.getEventsMap().get(eventId).getEventResult()));
            test.verify(() -> assertEquals(EventState.COMPLETED, this.persistance.getEventsMap().get(eventId).getState()));
            retrieveResult.flag();
          }));
        });
      }));
    }));
  }

  @Test
  public void registerEventThatFailAndRunEventTest(Vertx vertx, VertxTestContext test) {
    this.eventManager.start(test.succeeding((v) -> {
      Checkpoint eventHandlerCheckpoint = test.strictCheckpoint();
      Checkpoint eventCreationCheckpoint = test.strictCheckpoint();
      Checkpoint retrieveResult = test.strictCheckpoint();

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
        test.verify(() -> assertTrue(this.persistance.getEventsMap().containsKey(eventId)));
        test.verify(() -> assertEquals(EventState.PENDING, this.persistance.getEventsMap().get(eventId).getState()));
        eventCreationCheckpoint.flag();
        vertx.setTimer(1000, id -> {
          this.eventManager.getEvent(eventId, test.succeeding(res -> {
            test.verify(() -> assertEquals("Sbam", this.persistance.getEventsMap().get(eventId).getEventError().getMessage()));
            test.verify(() -> assertEquals("Sbam", res.getEventError().getMessage()));
            test.verify(() -> assertEquals(EventState.ERROR, res.getState()));
            retrieveResult.flag();
          }));
        });
      }));
    }));
  }

  @Test
  public void registerAndUnregisterEventTest(Vertx vertx, VertxTestContext test) {
    this.eventManager.start(test.succeeding((v) -> {
      Checkpoint eventCreationCheckpoint = test.strictCheckpoint();
      Checkpoint deleteEvent = test.strictCheckpoint();
      Checkpoint afterDelete = test.strictCheckpoint();

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
        test.verify(() -> assertTrue(this.persistance.getEventsMap().containsKey(eventId)));
        test.verify(() -> assertEquals(EventState.PENDING, this.persistance.getEventsMap().get(eventId).getState()));
        eventCreationCheckpoint.flag();
        this.eventManager.unregisterEvent(eventId, test.succeeding(r -> {
          test.verify(() -> assertFalse(this.persistance.getEventsMap().containsKey(eventId)));
          test.verify(() -> assertFalse(this.eventManager.timersId.containsKey(eventId)));
          deleteEvent.flag();
          vertx.setTimer(2000, id -> {
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
      Checkpoint eventsCheck = test.strictCheckpoint();

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
            test.verify(() -> assertEquals(1, this.eventManager.timersId.size()));
            vertx.setTimer(1000, l -> {
              test.verify(() -> assertEquals(EventState.STARVING, this.persistance.getEventsMap().get(baitEventAdded.getId()).getState()));
              test.verify(() -> assertEquals(EventState.COMPLETED, this.persistance.getEventsMap().get(event.getId()).getState()));
              eventsCheck.flag();
            });
          }));
        }));
      }));
    }));
  }

}
