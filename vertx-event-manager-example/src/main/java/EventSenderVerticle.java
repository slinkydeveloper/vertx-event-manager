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

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Random;

public class EventSenderVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(EventSenderVerticle.class);

  private final Handler<AsyncResult<String>> registrationCallback = ar -> {
    if (ar.failed()) {
      log.error("Error during event registration: " + ar.cause());
    } else {
      log.info("Event id " + ar.result());
    }
  };

  private String generateRandomString() {
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = 10;
    Random random = new Random();
    StringBuilder buffer = new StringBuilder(targetStringLength);
    for (int i = 0; i < targetStringLength; i++) {
      int randomLimitedInt = leftLimit + (int)
          (random.nextFloat() * (rightLimit - leftLimit + 1));
      buffer.append((char) randomLimitedInt);
    }
    return buffer.toString();
  }

  private Event generateEvent(ZonedDateTime triggerDateTime) {
    return Event.createPendingEvent(
        ZonedDateTime.now(),
        triggerDateTime,
        "removeUser",
        new JsonObject().put("userId", generateRandomString())
    );
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    // Instantiate the event manager proxy
    EventManager eventManager = EventManager.createProxy(vertx, "events_manager.myapp", new DeliveryOptions());

    vertx.setPeriodic(400, l -> {
      eventManager.registerEvent(generateEvent(ZonedDateTime.now().plus(Duration.ofMillis(500))), registrationCallback);
      eventManager.registerEvent(generateEvent(ZonedDateTime.now().plus(Duration.ofMillis(800))), registrationCallback);
    });
  }
}
