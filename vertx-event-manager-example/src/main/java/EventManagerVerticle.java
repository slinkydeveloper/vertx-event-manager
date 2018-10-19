import io.slinkydeveloper.events.EventManager;
import io.slinkydeveloper.events.persistance.mongodb.MongoDbEventPersistenceManager;
import io.slinkydeveloper.events.impl.EventManagerImpl;
import io.slinkydeveloper.events.logic.EventLogicManager;
import io.slinkydeveloper.events.persistence.EventPersistenceManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceBinder;

import java.time.ZonedDateTime;

public class EventManagerVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(EventManagerVerticle.class);

  EventManagerImpl eventManager;
  MessageConsumer<JsonObject> eventManagerMessageConsumer;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    // Initialize the mongodb persistence manager
    MongoClient mongoClient = MongoClient.createShared(
        vertx,
        new JsonObject().put("db_name", "test").put("connection_string", "mongodb://127.0.0.1:27017")
    );
    EventPersistenceManager persistenceManager = new MongoDbEventPersistenceManager(mongoClient, "eventManager");

    // Initialize logic manager and put inside the event handlers
    EventLogicManager logicManager = EventLogicManager.create();
    logicManager.addEventType("removeUser", e -> {
      log.info("Removing user " + e.getEventData().getString("userId") + " from event id " + e.getId());
      // Your "remove user" logic
      return Future.succeededFuture(new JsonObject().put("removedAt", ZonedDateTime.now().toString()));
    });

    // Initialize event manager
    eventManager = new EventManagerImpl(vertx, persistenceManager, logicManager);

    // Now mount the event manager service on event bus
    ServiceBinder binder = new ServiceBinder(vertx);
    this.eventManagerMessageConsumer = binder
        .setAddress("events_manager.myapp")
        .register(EventManager.class, eventManager);

    // Now start the event manager
    eventManager.start(v -> {
      startFuture.complete();
    });
  }

  @Override
  public void stop(Future<Void> stopFuture) throws Exception {
    eventManager.stop();
    eventManagerMessageConsumer.unregister(stopFuture.completer());
  }
}
