package io.slinkydeveloper.events;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.VertxGen;

@VertxGen
public enum EventState {
  PENDING,
  RUNNING,
  COMPLETED,
  ERROR,
  STARVING
}
