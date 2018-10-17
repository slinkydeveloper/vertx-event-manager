package io.slinkydeveloper.events;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonObject;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Different Event
 *
 */
@DataObject(generateConverter = true)
public class Event {

  private String id;
  private ZonedDateTime creationDateTime;
  private ZonedDateTime triggerDateTime;
  private ZonedDateTime completionDateTime;
  private String eventType;
  private JsonObject eventData;
  private JsonObject eventResult;
  private Throwable eventError;
  private EventState state;

  public Event(){}

  public Event(ZonedDateTime creationDateTime, ZonedDateTime triggerDateTime, ZonedDateTime completionDateTime, String eventType, JsonObject eventData, JsonObject eventResult, Throwable eventError, EventState state) {
    this.creationDateTime = creationDateTime;
    this.triggerDateTime = triggerDateTime;
    this.completionDateTime = completionDateTime;
    this.eventType = eventType;
    this.eventData = eventData;
    this.eventResult = eventResult;
    this.eventError = eventError;
    this.state = state;
  }

  public Event(Event other) {
    this.creationDateTime = other.creationDateTime;
    this.triggerDateTime = other.triggerDateTime;
    this.completionDateTime = other.completionDateTime;
    this.eventType = other.eventType;
    this.eventData = other.eventData;
    this.eventResult = other.eventResult;
    this.eventError = other.eventError;
    this.state = other.state;
  }

  public Event(JsonObject object) {
    this();
    EventConverter.fromJson(object, this);
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    EventConverter.toJson(this, jsonObject);
    return jsonObject;
  }

  public String getId() {
    return id;
  }

  @Fluent
  public Event setId(String id) {
    this.id = id;
    return this;
  }

  public String getCreationDateTime() {
    return creationDateTime.toString();
  }

  @Fluent
  public Event setCreationDateTime(String creationDateTime) {
    this.creationDateTime = ZonedDateTime.parse(creationDateTime);
    return this;
  }

  public String getTriggerDateTime() {
    return triggerDateTime.toString();
  }

  @Fluent
  public Event setTriggerDateTime(String triggerDateTime) {
    this.triggerDateTime = ZonedDateTime.parse(triggerDateTime);
    return this;
  }

  @Nullable
  public String getCompletionDateTime() {
    if (completionDateTime == null) return null;
    return completionDateTime.toString();
  }

  @Fluent
  public Event setCompletionDateTime(String completionDateTime) {
    this.completionDateTime = ZonedDateTime.parse(completionDateTime);
    return this;
  }

  public String getEventType() {
    return eventType;
  }

  @Fluent
  public Event setEventType(String eventType) {
    this.eventType = eventType;
    return this;
  }

  public JsonObject getEventData() {
    return eventData;
  }

  @Fluent
  public Event setEventData(JsonObject eventData) {
    this.eventData = eventData;
    return this;
  }

  @Nullable
  public JsonObject getEventResult() {
    return eventResult;
  }

  @Fluent
  public Event setEventResult(JsonObject eventResult) {
    this.eventResult = eventResult;
    return this;
  }

  public Throwable getEventError() {
    return eventError;
  }

  @Fluent
  public Event setEventError(Throwable eventError) {
    this.eventError = eventError;
    return this;
  }

  public EventState getState() {
    return state;
  }

  @Fluent
  public Event setState(EventState state) {
    this.state = state;
    return this;
  }

  public Event copy() {
    return new Event(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Event event = (Event) o;
    return Objects.equals(getId(), event.getId()) &&
        Objects.equals(getCreationDateTime(), event.getCreationDateTime()) &&
        Objects.equals(getTriggerDateTime(), event.getTriggerDateTime()) &&
        Objects.equals(getCompletionDateTime(), event.getCompletionDateTime()) &&
        Objects.equals(getEventType(), event.getEventType()) &&
        Objects.equals(getEventData(), event.getEventData()) &&
        Objects.equals(getEventResult(), event.getEventResult()) &&
        getState() == event.getState();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getCreationDateTime(), getTriggerDateTime(), getCompletionDateTime(), getEventType(), getEventData(), getEventResult(), getState());
  }

  public static Event createPendingEvent(ZonedDateTime creationDateTime, ZonedDateTime triggerDateTime, String eventType, JsonObject eventData) {
    return new Event(creationDateTime, triggerDateTime, null, eventType, eventData, null, null, EventState.PENDING);
  }

  public static Event createRunningEvent(ZonedDateTime creationDateTime, ZonedDateTime triggerDateTime, String eventType, JsonObject eventData) {
    return new Event(creationDateTime, triggerDateTime, null, eventType, eventData, null, null, EventState.RUNNING);
  }

  public static Event createCompletedEvent(ZonedDateTime creationDateTime, ZonedDateTime triggerDateTime, ZonedDateTime completionDateTime, String eventType, JsonObject eventData, JsonObject eventResult) {
    return new Event(creationDateTime, triggerDateTime, completionDateTime, eventType, eventData, eventResult, null,  EventState.COMPLETED);
  }

}
