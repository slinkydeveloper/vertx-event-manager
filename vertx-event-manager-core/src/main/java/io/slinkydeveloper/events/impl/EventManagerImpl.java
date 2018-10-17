package io.slinkydeveloper.events.impl;

import io.slinkydeveloper.events.*;
import io.slinkydeveloper.events.logic.EventLogicManager;
import io.slinkydeveloper.events.persistence.EventPersistenceManager;
import io.vertx.core.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventManagerImpl implements EventManagerAdmin {

  private final static Logger log = LoggerFactory.getLogger(EventManagerImpl.class);

  private Vertx vertx;
  private EventPersistenceManager persistance;
  private EventLogicManager logicManager;

  final protected Map<String, Long> timersId;

  private AtomicBoolean isRunning;

  public EventManagerImpl(Vertx vertx, EventPersistenceManager persistence, EventLogicManager logicManager) {
    this.vertx = vertx;
    this.persistance = persistence;
    this.logicManager = logicManager;

    this.timersId = new ConcurrentHashMap<>();
    this.isRunning = new AtomicBoolean(false);
  }

  @Override
  public void registerEvent(Event event, Handler<AsyncResult<String>> resultHandler) {
    if (!isRunning.get()) throw new IllegalStateException("EventManager is not running");
    persistance.addEvent(event).setHandler(ar -> {
      if (ar.succeeded()) {
        String eventId = ar.result().getId();
        if (isPast(ZonedDateTime.parse(ar.result().getTriggerDateTime()))) {
          this.runEvent(eventId);
        } else {
          this.startTimer(ar.result());
        }
        resultHandler.handle(Future.succeededFuture(eventId));
      } else {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void getEvent(String eventId, Handler<AsyncResult<Event>> resultHandler) {
    resultHandler.handle(persistance.getEvent(eventId));
  }

  @Override
  public void unregisterEvent(String eventId, Handler<AsyncResult<Void>> resultHandler) {
    if (!isRunning.get()) throw new IllegalStateException("EventManager is not running");
    stopTimer(eventId);
    resultHandler.handle(persistance.deleteEvent(eventId));
  }

  @Override
  public void getEventsFilteredByState(EventState state, Handler<AsyncResult<List<Event>>> resultHandler) {
    resultHandler.handle(persistance.getEventsFilteredByState(state));
  }

  @Override
  public void start(Handler<AsyncResult<Void>> resultHandler) {
    if (isRunning.get()) throw new IllegalStateException("EventManager is already running");
    CompositeFuture.all(
        persistance.getEventsFilteredByState(EventState.PENDING),
        persistance.getEventsFilteredByState(EventState.RUNNING)
    ).setHandler(cf -> {
      // Set timers for pending events and start past events
      ((List<Event>)cf.result().resultAt(0)).forEach(e -> {
        if (isPast(ZonedDateTime.parse(e.getTriggerDateTime()))) {
          this.runEvent(e.getId());
        } else {
          this.startTimer(e);
        }
      });

      // Set starving events state
      ((List<Event>)cf.result().resultAt(1)).forEach(e -> {
        this.persistance.updateEvent(e.setState(EventState.STARVING));
        log.info("Set STARVING status for event " + e.getId());
      });

      this.isRunning.set(true);
      resultHandler.handle(Future.succeededFuture());
    });
  }

  @Override
  public void stop() {
    if (!isRunning.get()) throw new IllegalStateException("EventManager is not running");
    this.isRunning.set(false);
    for (String eventId : this.timersId.keySet()) {
      stopTimer(eventId);
    }
  }

  private void startTimer(Event event) {
    final String eventId = event.getId();
    long timerId = vertx.setTimer(
        zonedDateTimeDifference(ZonedDateTime.now(), ZonedDateTime.parse(event.getTriggerDateTime()), ChronoUnit.MILLIS),
        l -> {
          synchronized (this.timersId) {
            this.timersId.remove(eventId);
          }
          runEvent(eventId);
        });
    synchronized (this.timersId) {
      this.timersId.put(event.getId(), timerId);
    }
  }

  private void stopTimer(String eventId) {
    long timerId = this.timersId.get(eventId);
    vertx.cancelTimer(timerId);
    synchronized (this.timersId) {
      this.timersId.remove(eventId);
    }
  }

  private void runEvent(String eventId) {
    this.persistance
        .getEvent(eventId)
        .compose(e -> this.persistance.updateEvent(e.setState(EventState.RUNNING)))
        .setHandler(ar -> {
          final Event event = ar.result();
          if (ar.succeeded()) {
            this.logicManager.runEvent(event).setHandler(runAr -> {
              if (runAr.succeeded()) {
                this.persistance.updateEvent(
                    event.setState(EventState.COMPLETED)
                        .setCompletionDateTime(ZonedDateTime.now().toString())
                        .setEventResult(runAr.result()));
              } else {
                this.persistance.updateEvent(
                    event
                        .setState(EventState.ERROR)
                        .setCompletionDateTime(ZonedDateTime.now().toString())
                        .setEventError(runAr.cause()));
              }
            });
          } else {
            log.error("Cannot update event " + eventId + ": " + ar.cause());
          }
        });
  }

  private boolean isPast(ZonedDateTime d) {
    return ZonedDateTime.now().compareTo(d) >= 0;
  }

  private long zonedDateTimeDifference(ZonedDateTime d1, ZonedDateTime d2, ChronoUnit unit){
    return unit.between(d1, d2);
  }
}
