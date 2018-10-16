package io.slinkydeveloper.events;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

@ProxyGen
public interface EventManager {

  void registerEvent(Event event, Handler<AsyncResult<String>> resultHandler);
  void getEventResult(String eventId, Handler<AsyncResult<JsonObject>> resultHandler);
  void unregisterEvent(String eventId, Handler<AsyncResult<Void>> resultHandler);
  void getPendingEvents(Handler<AsyncResult<List<Event>>> resultHandler);
  void getCompletedEvents(Handler<AsyncResult<List<Event>>> resultHandler);

}
