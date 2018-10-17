package io.slinkydeveloper.events.impl;

import io.slinkydeveloper.events.Event;
import io.slinkydeveloper.events.EventLogicManager;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class EventLogicManagerTest {

  @Test
  public void testEventLogicManager(VertxTestContext test) {
    EventLogicManager eventLogicManager = EventLogicManager.create();
    eventLogicManager.addEventType("blablaEvent", ev -> {
      assertEquals("superEvent", ev.getId());
      assertEquals("blablaEvent", ev.getEventType());
      return Future.succeededFuture(new JsonObject().put("blaKey", "blaValue"));
    });
    eventLogicManager
        .runEvent(Event.createRunningEvent(ZonedDateTime.now().minusHours(1), ZonedDateTime.now(), "blablaEvent",  new JsonObject()).setId("superEvent"))
        .setHandler(test.succeeding(resultData -> {
          assertEquals(new JsonObject().put("blaKey", "blaValue"), resultData);
          test.completeNow();
        }));
  }

}
