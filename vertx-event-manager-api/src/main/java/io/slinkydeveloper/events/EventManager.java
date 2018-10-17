package io.slinkydeveloper.events;

import io.vertx.codegen.annotations.ProxyGen;
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

  static EventManager createProxy(Vertx vertx, String address, DeliveryOptions options) {
    return new EventManagerVertxEBProxy(vertx, address, options);
  }

}
