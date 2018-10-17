package io.slinkydeveloper.events.logic.impl;

import io.slinkydeveloper.events.Event;
import io.slinkydeveloper.events.logic.EventLogicManager;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import io.vertx.core.Future;
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
    if (!this.eventLogicsMap.containsKey(event.getEventType()))
      return Future.failedFuture(new IllegalStateException("EventLogicManager doesn't contain the event type " + event.getEventType()));
    try {
      return this.eventLogicsMap.get(event.getEventType()).apply(event);
    } catch (Throwable e) {
      return Future.failedFuture(e);
    }
  }
}
