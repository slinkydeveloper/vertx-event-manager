import io.slinkydeveloper.events.Event;
import io.slinkydeveloper.events.EventManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.stream.Collectors;

public class EventManagerAdministratorVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(EventManagerAdministratorVerticle.class);

  private final Handler<AsyncResult<String>> registrationCallback = ar -> {
    if (ar.failed()) {
      log.error("Error during event registration: " + ar.cause());
    } else {
      log.info("Event id " + ar.result());
    }
  };

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    // Instantiate the event manager admin proxy
    EventManager eventManager = EventManager.createProxy(vertx, "events_manager.myapp", new DeliveryOptions());

    vertx.setPeriodic(2000, l -> {
      eventManager.cleanEventsCompletedBefore(ZonedDateTime.now().minus(Duration.ofSeconds(2)).toString(), ar -> {
        if (ar.failed()) {
          log.error("Error during event cleanup: " + ar.cause());
        } else {
          log.info("Cleaned from db events " + ar.result().stream().map(Event::getId).collect(Collectors.joining(", ")));
        }
      });
    });
  }
}
