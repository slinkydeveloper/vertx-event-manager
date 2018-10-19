# Vert.x Event Manager Example

This project is composed by three verticles:

* [EventManagerVerticle](src/main/java/EventManagerVerticle.java): defines the events handling logic, instantiates the `EventManager` and mounts the service on the event bus
* [EventSenderVerticle](src/main/java/EventSenderVerticle.java): Periodically spawns random `Event` instances and sends it to the `EventManager`
* [EventManagerAdministratorVerticle](src/main/java/EventManagerAdministratorVerticle.java): Periodically cleanup old events

## Run

Just run `mvn package` and then run the fat jar under `target`