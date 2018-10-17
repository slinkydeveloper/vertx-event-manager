package io.slinkydeveloper.events.logic;

import io.slinkydeveloper.events.Event;
import io.slinkydeveloper.events.logic.impl.EventLogicManagerImpl;
import io.vertx.core.json.JsonObject;

import io.vertx.core.Future;
import java.util.function.Function;

public interface EventLogicManager {

  EventLogicManager addEventType(String eventType, Function<Event, Future<JsonObject>> eventLogic);
  Future<JsonObject> runEvent(Event event);

  static EventLogicManager create() {
    return new EventLogicManagerImpl();
  }

}
