package io.slinkydeveloper.events.persistance;

import io.slinkydeveloper.events.Event;
import io.slinkydeveloper.events.EventState;
import io.vertx.core.Future;

import java.time.ZonedDateTime;
import java.util.List;

public interface EventPersistanceManager {

  Future<Event> addEvent(Event event);
  Future<Event> getEvent(String eventId);
  Future<Void> deleteEvent(String eventId);
  Future<Event> updateEvent(Event event);
  Future<List<Event>> getEventsFilteredByState(EventState state);
  Future<List<Event>> getAllEvents();
  Future<List<Event>> cleanEventsCompletedBefore(ZonedDateTime before);

}
