package io.slinkydeveloper.events.impl;

import io.slinkydeveloper.events.Event;
import io.slinkydeveloper.events.EventPersistanceManager;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;
import java.util.function.Function;

@ExtendWith(VertxExtension.class)
public abstract class BaseEventPersistanceManager {

  EventPersistanceManager persistance;

  @BeforeEach
  abstract void before(Vertx vertx, VertxTestContext testContext);

  @Test
  void addEvent(VertxTestContext test) {
    Event event = Event.createPendingEvent(
        ZonedDateTime.now(),
        "superEvent",
        new JsonObject().put("someData", "someValue")
    );
    persistance.addEvent(event).setHandler(test.succeeding(newEvent -> {
      assertNotNull(newEvent.getId());
      assertNull(newEvent.getTriggerDateTime());
      assertNull(newEvent.getCompletionDateTime());
      assertEquals(event.getCreationDateTime(), newEvent.getCreationDateTime());
      assertNull(newEvent.getEventResult());
      assertEquals(event.getEventType(), newEvent.getEventType());
      assertEquals(event.getEventData(), newEvent.getEventData());
      test.completeNow();
    }));
  }

  @Test
  void getEvent(VertxTestContext test) {
    Event event = Event.createCompletedEvent(
        ZonedDateTime.now(),
        ZonedDateTime.now().plusHours(1),
        ZonedDateTime.now().plusHours(2),
        "superEvent",
        new JsonObject().put("someData", "someValue"),
        new JsonObject().put("someResults", "someValue")
    );
    persistance
        .addEvent(event)
        .recover(this.assertNotFail(test))
        .compose(pushedEvent -> persistance.getEvent(pushedEvent.getId()))
        .recover(this.assertNotFail(test))
        .compose(retrievedEvent -> {
          assertEquals(event.getCompletionDateTime(), retrievedEvent.getCompletionDateTime());
          assertEquals(event.getCreationDateTime(), retrievedEvent.getCreationDateTime());
          assertEquals(event.getTriggerDateTime(), retrievedEvent.getTriggerDateTime());
          assertEquals(event.getEventData(), retrievedEvent.getEventData());
          assertEquals(event.getEventType(), retrievedEvent.getEventType());
          assertEquals(event.getEventResult(), retrievedEvent.getEventResult());
          return this.completeNow(test);
        });
  }

  @Test
  void deleteEvent(VertxTestContext test) {
    Event event = Event.createCompletedEvent(
        ZonedDateTime.now(),
        ZonedDateTime.now().plusHours(1),
        ZonedDateTime.now().plusHours(2),
        "superEvent",
        new JsonObject().put("someData", "someValue"),
        new JsonObject().put("someResults", "someValue")
    );
    persistance.addEvent(event).setHandler(test.succeeding(pushedEvent ->
        persistance.deleteEvent(pushedEvent.getId()).setHandler(test.succeeding(res ->
            persistance.getEvent(pushedEvent.getId()).setHandler(test.succeeding(retrievedEvent -> {
              assertNull(retrievedEvent);
              test.completeNow();
            }))
        ))
    ));
  }

  @Test
  void updateEvent(VertxTestContext test) {
    JsonObject oldEventResult = new JsonObject().put("someResults", "someValue");
    JsonObject newEventResult = oldEventResult.copy().put("newResults", "newValue");
    Event event = Event.createCompletedEvent(
        ZonedDateTime.now(),
        ZonedDateTime.now().plusHours(1),
        ZonedDateTime.now().plusHours(2),
        "superEvent",
        new JsonObject().put("someData", "someValue"),
        oldEventResult
    );
    persistance.addEvent(event).setHandler(test.succeeding(pushedEvent ->
      persistance.updateEvent(pushedEvent.setEventResult(newEventResult)).setHandler(test.succeeding(res ->
          persistance.getEvent(pushedEvent.getId()).setHandler(test.succeeding(retrievedEvent -> {
            assertEquals(newEventResult, retrievedEvent.getEventResult());
            test.completeNow();
          }))
      ))
    ));
  }

  @Test
  void getAllEvents(VertxTestContext test) {
    Event event1 = Event.createPendingEvent(
        ZonedDateTime.now(),
        "superEventOfFirstType",
        new JsonObject().put("someData", "someValue")
    );
    Event event2 = Event.createPendingEvent(
        ZonedDateTime.now(),
        "superEventOfSecondType",
        new JsonObject().put("someData", "someValue")
    );
    Event event3 = Event.createPendingEvent(
        ZonedDateTime.now(),
        "superEventOfThirdType",
        new JsonObject().put("someData", "someValue")
    );
    CompositeFuture.all(
        persistance.addEvent(event1),
        persistance.addEvent(event2),
        persistance.addEvent(event3)
    ).recover(this.assertNotFail(test))
        .compose(res -> persistance.getAllEvents())
        .recover(this.assertNotFail(test))
        .compose(events -> {
          assertTrue(events.stream().anyMatch(e -> "superEventOfFirstType".equals(e.getEventType())));
          assertTrue(events.stream().anyMatch(e -> "superEventOfSecondType".equals(e.getEventType())));
          assertTrue(events.stream().anyMatch(e -> "superEventOfThirdType".equals(e.getEventType())));
          return this.completeNow(test);
        });
  }

  @Test
  void getPendingEvents(VertxTestContext test) {
    Event event1 = Event.createPendingEvent( // Pending event
        ZonedDateTime.now(),
        "superEventOfFirstType",
        new JsonObject().put("someData", "someValue")
    );
    Event event2 = Event.createRunningEvent( // Running event
        ZonedDateTime.now().minusHours(1),
        ZonedDateTime.now(),
        "superEventOfSecondType",
        new JsonObject().put("someData", "someValue")
    );
    Event event3 = Event.createCompletedEvent( // Completed event
        ZonedDateTime.now().minusHours(2),
        ZonedDateTime.now().minusHours(1),
        ZonedDateTime.now().minusMinutes(30),
        "superEventOfThirdType",
        new JsonObject().put("someData", "someValue"),
        new JsonObject()
    );
    CompositeFuture.all(
        persistance.addEvent(event1),
        persistance.addEvent(event2),
        persistance.addEvent(event3)
    ).recover(this.assertNotFail(test))
        .compose(res -> persistance.getPendingEvents())
        .recover(this.assertNotFail(test))
        .compose(events -> {
          assertTrue(events.stream().anyMatch(e -> "superEventOfFirstType".equals(e.getEventType())));
          assertTrue(events.stream().noneMatch(e -> "superEventOfSecondType".equals(e.getEventType())));
          assertTrue(events.stream().noneMatch(e -> "superEventOfThirdType".equals(e.getEventType())));
          return this.completeNow(test);
        });
  }

  @Test
  void getRunningEvents(VertxTestContext test) {
    Event event1 = Event.createPendingEvent( // Pending event
        ZonedDateTime.now(),
        "superEventOfFirstType",
        new JsonObject().put("someData", "someValue")
    );
    Event event2 = Event.createRunningEvent( // Running event
        ZonedDateTime.now().minusHours(1),
        ZonedDateTime.now(),
        "superEventOfSecondType",
        new JsonObject().put("someData", "someValue")
    );
    Event event3 = Event.createCompletedEvent( // Completed event
        ZonedDateTime.now().minusHours(2),
        ZonedDateTime.now().minusHours(1),
        ZonedDateTime.now().minusMinutes(30),
        "superEventOfThirdType",
        new JsonObject().put("someData", "someValue"),
        new JsonObject()
    );
    CompositeFuture.all(
        persistance.addEvent(event1),
        persistance.addEvent(event2),
        persistance.addEvent(event3)
    ).recover(this.assertNotFail(test))
        .compose(res -> persistance.getRunningEvents())
        .recover(this.assertNotFail(test))
        .compose(events -> {
          assertTrue(events.stream().noneMatch(e -> "superEventOfFirstType".equals(e.getEventType())));
          assertTrue(events.stream().anyMatch(e -> "superEventOfSecondType".equals(e.getEventType())));
          assertTrue(events.stream().noneMatch(e -> "superEventOfThirdType".equals(e.getEventType())));
          return this.completeNow(test);
        });
  }

  @Test
  void getCompletedEvents(VertxTestContext test) {
    Event event1 = Event.createPendingEvent( // Pending event
        ZonedDateTime.now(),
        "superEventOfFirstType",
        new JsonObject().put("someData", "someValue")
    );
    Event event2 = Event.createRunningEvent( // Running event
        ZonedDateTime.now().minusHours(1),
        ZonedDateTime.now(),
        "superEventOfSecondType",
        new JsonObject().put("someData", "someValue")
    );
    Event event3 = Event.createCompletedEvent( // Completed event
        ZonedDateTime.now().minusHours(2),
        ZonedDateTime.now().minusHours(1),
        ZonedDateTime.now().minusMinutes(30),
        "superEventOfThirdType",
        new JsonObject().put("someData", "someValue"),
        new JsonObject()
    );
    CompositeFuture.all(
        persistance.addEvent(event1),
        persistance.addEvent(event2),
        persistance.addEvent(event3)
    ).recover(this.assertNotFail(test))
        .compose(res -> persistance.getCompletedEvents())
        .recover(this.assertNotFail(test))
        .compose(events -> {
          assertTrue(events.stream().noneMatch(e -> "superEventOfFirstType".equals(e.getEventType())));
          assertTrue(events.stream().noneMatch(e -> "superEventOfSecondType".equals(e.getEventType())));
          assertTrue(events.stream().anyMatch(e -> "superEventOfThirdType".equals(e.getEventType())));
          return this.completeNow(test);
        });
  }

  @Test
  void cleanEventsCompletedBefore(VertxTestContext test) {
    Event event1 = Event.createPendingEvent( // Pending event
        ZonedDateTime.now(),
        "superEventOfFirstType",
        new JsonObject().put("someData", "someValue")
    );
    Event event2 = Event.createCompletedEvent( // Completed event that must be removed
        ZonedDateTime.now().minusHours(2),
        ZonedDateTime.now().minusHours(1),
        ZonedDateTime.now().minusMinutes(40),
        "superEventOfSecondType",
        new JsonObject().put("someData", "someValue"),
        null
    );
    Event event3 = Event.createCompletedEvent( // Completed event that must remain
        ZonedDateTime.now().minusHours(2),
        ZonedDateTime.now().minusHours(1),
        ZonedDateTime.now().minusMinutes(20),
        "superEventOfThirdType",
        new JsonObject().put("someData", "someValue"),
        new JsonObject()
    );
    CompositeFuture.all(
        persistance.addEvent(event1),
        persistance.addEvent(event2),
        persistance.addEvent(event3)
    ).recover(this.assertNotFail(test))
        .compose(res -> persistance.cleanEventsCompletedBefore(ZonedDateTime.now().minusMinutes(30)))
        .recover(this.assertNotFail(test))
        .compose(res -> persistance.getAllEvents())
        .recover(this.assertNotFail(test))
        .compose(events -> {
          assertTrue(events.stream().anyMatch(e -> "superEventOfFirstType".equals(e.getEventType())));
          assertTrue(events.stream().noneMatch(e -> "superEventOfSecondType".equals(e.getEventType())));
          assertTrue(events.stream().anyMatch(e -> "superEventOfThirdType".equals(e.getEventType())));
          return this.completeNow(test);
        });
  }

  private <T> Function<Throwable, Future<T>> assertNotFail(VertxTestContext test) {
    return res -> {
      test.failNow(res);
      return Future.failedFuture(res);
    };
  }

  private Future<Void> completeNow(VertxTestContext test) {
    test.completeNow();
    return Future.succeededFuture();
  }

}