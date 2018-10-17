package io.slinkydeveloper.events;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

import java.util.List;

@ProxyGen
public interface EventManagerAdmin extends EventManager {

  // I need to override this methods to trigger service proxy gen
  void registerEvent(Event event, Handler<AsyncResult<String>> resultHandler);
  void getEvent(String eventId, Handler<AsyncResult<Event>> resultHandler);
  void unregisterEvent(String eventId, Handler<AsyncResult<Void>> resultHandler);
  void getEventsFilteredByState(EventState state, Handler<AsyncResult<List<Event>>> resultHandler);
  void start(Handler<AsyncResult<Void>> resultHandler);
  void stop();

  static EventManagerAdmin createProxy(Vertx vertx, String address, DeliveryOptions options) {
    return new EventManagerAdminVertxEBProxy(vertx, address, options);
  }
}
