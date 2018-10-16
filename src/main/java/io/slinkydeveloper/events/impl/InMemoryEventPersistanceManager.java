package io.slinkydeveloper.events.impl;

import io.slinkydeveloper.events.Event;
import io.slinkydeveloper.events.EventPersistanceManager;
import io.vertx.core.Future;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryEventPersistanceManager implements EventPersistanceManager {

  Map<String, Event> eventsMap;

  public InMemoryEventPersistanceManager() {
    this.eventsMap = new ConcurrentHashMap<>();
  }

  @Override
  public Future<Event> addEvent(Event event) {
    String eventId = this.generateEventId();
    event.setId(eventId);
    this.eventsMap.put(eventId, event);
    return Future.succeededFuture(event);
  }

  @Override
  public Future<Event> getEvent(String eventId) {
    return Future.succeededFuture(this.eventsMap.get(eventId));
  }

  @Override
  public Future<Void> deleteEvent(String eventId) {
    this.eventsMap.remove(eventId);
    return Future.succeededFuture();
  }

  @Override
  public Future<Event> updateEvent(Event event) {
    this.eventsMap.replace(event.getId(), event);
    return Future.succeededFuture(event);
  }

  @Override
  public Future<List<Event>> getPendingEvents() {
    return Future.succeededFuture(
        this.eventsMap.values().stream().filter(e -> e.getCreationDateTime() != null && e.getTriggerDateTime() == null && e.getCompletionDateTime() == null).collect(Collectors.toList())
    );
  }

  @Override
  public Future<List<Event>> getRunningEvents() {
    return Future.succeededFuture(
        this.eventsMap.values().stream().filter(e -> e.getCreationDateTime() != null && e.getTriggerDateTime() != null && e.getCompletionDateTime() == null).collect(Collectors.toList())
    );
  }

  @Override
  public Future<List<Event>> getCompletedEvents() {
    return Future.succeededFuture(
        this.eventsMap.values().stream().filter(e -> e.getCreationDateTime() != null && e.getTriggerDateTime() != null && e.getCompletionDateTime() != null).collect(Collectors.toList())
    );
  }

  @Override
  public Future<List<Event>> getAllEvents() {
    return Future.succeededFuture(
        new ArrayList<>(this.eventsMap.values())
    );
  }

  @Override
  public Future<List<Event>> cleanEventsCompletedBefore(ZonedDateTime before) {
    return this.getCompletedEvents().map(events -> {
      List<Event> eventsToRemove = events.stream().filter(e -> before.compareTo(ZonedDateTime.parse(e.getCompletionDateTime())) <= 0).collect(Collectors.toList());
      eventsToRemove.forEach(e -> this.deleteEvent(e.getId()));
      return eventsToRemove;
    });
  }

  private String generateEventId() {
    return UUID.randomUUID().toString();
  }
}
