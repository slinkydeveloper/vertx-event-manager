package io.slinkydeveloper.events.persistance.mongodb;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.vertx.core.json.JsonObject;

public class MongoDbRunner {

  private MongodExecutable exe;

  public void startMongo() throws Exception {
    Version.Main version = Version.Main.PRODUCTION;
    int port = 27018;
    System.out.println("Starting Mongo " + version + " on port " + port);
    IMongodConfig config = new MongodConfigBuilder().
        version(version).
        net(new Net(port, Network.localhostIsIPv6())).
        build();
    exe = MongodStarter.getDefaultInstance().prepare(config);
    exe.start();
  }

  public void stopMongo() {
    if (exe != null) {
      exe.stop();
    }
  }

  public JsonObject getConfig() {
    JsonObject config = new JsonObject();
    config.put("connection_string", "mongodb://localhost:27018");
    config.put("db_name", "test");
    return config;
  }

}
