package io.slinkdeveloper.events.persistance.mongodb;

import io.slinkydeveloper.events.Event;
import io.slinkydeveloper.events.EventState;
import io.slinkydeveloper.events.persistance.EventPersistanceManager;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MongoDbEventPersistanceManager implements EventPersistanceManager {

  private MongoClient client;
  private String collectionName;

  public MongoDbEventPersistanceManager(MongoClient client, String collectionName) {
    this.client = client;
    this.collectionName = collectionName;
  }

  @Override
  public Future<Event> addEvent(Event event) {
    Future<String> res = Future.future();
    client.save(this.collectionName, event.toJson(), res.completer());
    return res.map(eventId -> event.copy().setId(eventId));
  }

  @Override
  public Future<Event> getEvent(String eventId) {
    Future<JsonObject> res = Future.future();
    client.findOne(this.collectionName, createIdQuery(eventId), null, res.completer());
    return res.map(jo -> (jo != null) ? new Event(jo) : null);
  }

  @Override
  public Future<Void> deleteEvent(String eventId) {
    Future<Void> res = Future.future();
    client.remove(this.collectionName, createIdQuery(eventId), res.completer());
    return res;
  }

  @Override
  public Future<Event> updateEvent(Event event) {
    Future<JsonObject> res = Future.future();
    client.findOneAndReplace(this.collectionName, createIdQuery(event.getId()), event.toJson(), res.completer());
    return res.map(this::buildEventFromJsonObject);
  }

  @Override
  public Future<List<Event>> getEventsFilteredByState(EventState state) {
    Future<List<JsonObject>> res = Future.future();
    client.find(this.collectionName, new JsonObject().put("state", state.name()), res.completer());
    return res.map(l -> l.stream().map(this::buildEventFromJsonObject).collect(Collectors.toList()));
  }

  @Override
  public Future<List<Event>> getAllEvents() {
    Future<List<JsonObject>> res = Future.future();
    client.find(this.collectionName, new JsonObject(), res.completer());
    return res.map(l -> l.stream().map(this::buildEventFromJsonObject).collect(Collectors.toList()));
  }

  @Override
  public Future<List<Event>> cleanEventsCompletedBefore(ZonedDateTime before) {
    return CompositeFuture
        .all(getEventsFilteredByState(EventState.COMPLETED), getEventsFilteredByState(EventState.ERROR))
        .compose(cf -> {
          List<Event> toRemove = Stream.concat(
              ((List<Event>)cf.resultAt(0)).stream(),
              ((List<Event>)cf.resultAt(1)).stream()
          ).filter(e -> before.compareTo(ZonedDateTime.parse(e.getCompletionDateTime())) >= 0).collect(Collectors.toList());
          JsonObject query = new JsonObject().put("_id", new JsonObject().put("$in", new JsonArray(
              toRemove.stream().map(Event::getId).collect(Collectors.toList())
          )));
          Future<Void> res = Future.future();
          client.remove(this.collectionName, query, res.completer());
          return res.map(toRemove);
        });
  }

  private Event buildEventFromJsonObject(JsonObject obj) {
    return new Event(obj).setId(obj.getString("_id"));
  }

  private JsonObject createIdQuery(String eventId) {
    return new JsonObject().put("_id", eventId);
  }
}
