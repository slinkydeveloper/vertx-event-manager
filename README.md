# Vert.x Event Manager

Vert.x Event Manager is a lightweight event manager/scheduler with customizable persistence layer. Inspired to [agenda](https://github.com/agenda/agenda)

## Features

* Job scheduling
* Mount the event manager as Vert.x Event Bus service to access from local/remote verticles
* MongoDB & in memory persistence
* Custom persistence just implementing an interface

## Usage

Clone this repo and run `mvn install`. Then import:

* `vertx-event-manager-api`: If you just need to access remotely to the event manager
* `vertx-event-manager-core`: If you need to instantiate the event manager
* `vertx-event-manager-mongodb`: If you need mongo db persistence

For a complete example look at [vertx-event-manager-example](vertx-event-manager-example/README.md)

## Limitations

* Only one instance can run at time and connect to the database to avoid multiple time events triggering.

## Planned features

* Lock system to support multiple instances
* Command line
* Periodic jobs
* SQL Persistence

## License

MIT License

Copyright (c) 2018 Francesco Guardiani