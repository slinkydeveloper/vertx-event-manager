package io.slinkydeveloper.events;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import java.util.List;

@ProxyGen
public interface EventManager {

  void registerEvent(Event event, Handler<AsyncResult<String>> resultHandler);
  void getEvent(String eventId, Handler<AsyncResult<Event>> resultHandler);
  void unregisterEvent(String eventId, Handler<AsyncResult<Void>> resultHandler);
  void getEventsFilteredByState(EventState state, Handler<AsyncResult<List<Event>>> resultHandler);
  void cleanEventsCompletedBefore(String zonedDateTimeBefore, Handler<AsyncResult<List<Event>>> resultHandler);
  @ProxyIgnore void start(Handler<AsyncResult<Void>> resultHandler);
  @ProxyIgnore void stop();

  static EventManager createProxy(Vertx vertx, String address, DeliveryOptions options) {
    return new EventManagerVertxEBProxy(vertx, address, options);
  }

}
