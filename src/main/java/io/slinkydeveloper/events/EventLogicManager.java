package io.slinkydeveloper.events;

import io.slinkydeveloper.events.impl.EventLogicManagerImpl;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.Future;
import java.util.function.Function;

public interface EventLogicManager {

  @Fluent EventLogicManager addEventType(String eventType, Function<Event, Future<JsonObject>> eventLogic);
  Future<JsonObject> runEvent(Event event);

  static EventLogicManager create() {
    return new EventLogicManagerImpl();
  }

}
