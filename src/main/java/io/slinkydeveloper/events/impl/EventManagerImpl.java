package io.slinkydeveloper.events.impl;

import io.slinkydeveloper.events.Event;
import io.slinkydeveloper.events.EventLogicManager;
import io.slinkydeveloper.events.EventManager;
import io.slinkydeveloper.events.EventPersistanceManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

public class EventManagerImpl implements EventManager {

  private Vertx vertx;
  private EventPersistanceManager persistance;
  private EventLogicManager logicManager;

  public EventManagerImpl(Vertx vertx, EventPersistanceManager persistance, EventLogicManager logicManager) {
    this.vertx = vertx;
    this.persistance = persistance;
    this.logicManager = logicManager;
  }

  private void initMap() {

  }

  @Override
  public void registerEvent(Event event, Handler<AsyncResult<String>> resultHandler) {
    resultHandler.handle(persistance.addEvent(event).map(Event::getId));
  }

  @Override
  public void getEventResult(String eventId, Handler<AsyncResult<JsonObject>> resultHandler) {
    resultHandler.handle(persistance.getEvent(eventId).map(Event::getEventResult));
  }

  @Override
  public void unregisterEvent(String eventId, Handler<AsyncResult<Void>> resultHandler) {

  }

  @Override
  public void getPendingEvents(Handler<AsyncResult<List<Event>>> resultHandler) {
    resultHandler.handle(persistance.getPendingEvents());
  }

  @Override
  public void getCompletedEvents(Handler<AsyncResult<List<Event>>> resultHandler) {
    resultHandler.handle(persistance.getCompletedEvents());
  }
}
