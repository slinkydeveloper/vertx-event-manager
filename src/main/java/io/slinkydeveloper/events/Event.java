package io.slinkydeveloper.events;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.json.JsonObject;

import java.time.ZonedDateTime;
import java.util.Objects;

@DataObject(generateConverter = true)
public class Event {

  String id;
  ZonedDateTime creationDateTime;
  ZonedDateTime triggerDateTime;
  ZonedDateTime completionDateTime;
  String eventType;
  JsonObject eventData;
  JsonObject eventResult;

  public Event(){}

  protected Event(ZonedDateTime creationDateTime, ZonedDateTime triggerDateTime, ZonedDateTime completionDateTime, String eventType, JsonObject eventData, JsonObject eventResult) {
    this.creationDateTime = creationDateTime;
    this.triggerDateTime = triggerDateTime;
    this.completionDateTime = completionDateTime;
    this.eventType = eventType;
    this.eventData = eventData;
    this.eventResult = eventResult;
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

  @Nullable
  public String getCreationDateTime() {
    if (creationDateTime == null) return null;
    return creationDateTime.toString();
  }

  @Fluent
  public Event setCreationDateTime(String creationDateTime) {
    this.creationDateTime = ZonedDateTime.parse(creationDateTime);
    return this;
  }

  @Nullable
  public String getTriggerDateTime() {
    if (triggerDateTime == null) return null;
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
        Objects.equals(getEventResult(), event.getEventResult());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getCreationDateTime(), getTriggerDateTime(), getCompletionDateTime(), getEventType(), getEventData(), getEventResult());
  }

  public static Event createPendingEvent(ZonedDateTime creationDateTime, String eventType, JsonObject eventData) {
    return new Event(creationDateTime, null, null, eventType, eventData, null);
  }

  public static Event createRunningEvent(ZonedDateTime creationDateTime, ZonedDateTime triggerDateTime, String eventType, JsonObject eventData) {
    return new Event(creationDateTime, triggerDateTime, null, eventType, eventData, null);
  }

  public static Event createCompletedEvent(ZonedDateTime creationDateTime, ZonedDateTime triggerDateTime, ZonedDateTime completionDateTime, String eventType, JsonObject eventData, JsonObject eventResult) {
    return new Event(creationDateTime, triggerDateTime, completionDateTime, eventType, eventData, eventResult);
  }

}
