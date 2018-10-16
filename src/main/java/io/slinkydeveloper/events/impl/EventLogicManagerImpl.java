package io.slinkydeveloper.events.impl;

import io.slinkydeveloper.events.Event;
import io.slinkydeveloper.events.EventLogicManager;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;

@VertxGen
public class EventLogicManagerImpl implements EventLogicManager {

  Map<String, Function<Event, Future<JsonObject>>> eventLogicsMap;

  public EventLogicManagerImpl() {
    this.eventLogicsMap = new HashMap<>();
  }

  @Override
  @Fluent
  public EventLogicManager addEventType(String eventType, Function<Event, Future<JsonObject>> eventLogic) {
    this.eventLogicsMap.put(eventType, eventLogic);
    return this;
  }

  @Override
  public Future<JsonObject> runEvent(Event event) {
    return this.eventLogicsMap.get(event.getEventType()).apply(event);
  }

}
