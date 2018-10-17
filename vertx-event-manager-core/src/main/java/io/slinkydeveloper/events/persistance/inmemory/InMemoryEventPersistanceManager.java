package io.slinkydeveloper.events.persistance.inmemory;

import io.slinkydeveloper.events.Event;
import io.slinkydeveloper.events.persistance.EventPersistanceManager;
import io.slinkydeveloper.events.EventState;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryEventPersistanceManager implements EventPersistanceManager {

  protected Map<String, Event> eventsMap;

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
  public Future<List<Event>> getEventsFilteredByState(EventState state) {
    Objects.requireNonNull(state);
    return Future.succeededFuture(
        this.eventsMap.values().stream().filter(e -> e.getState().equals(state)).collect(Collectors.toList())
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
    return this.getEventsFilteredByState(EventState.COMPLETED).compose(events -> {
      List<Event> eventsToRemove = events.stream().filter(e -> before.compareTo(ZonedDateTime.parse(e.getCompletionDateTime())) >= 0).collect(Collectors.toList());
      return CompositeFuture.all(
          eventsToRemove.stream().map(e -> this.deleteEvent(e.getId())).collect(Collectors.toList())
      ).map(eventsToRemove);
    });
  }

  public Map<String, Event> getEventsMap() {
    return eventsMap;
  }

  private String generateEventId() {
    return UUID.randomUUID().toString();
  }
}
