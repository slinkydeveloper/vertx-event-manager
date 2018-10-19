import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class Starter {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(EventManagerVerticle.class, new DeploymentOptions().setMultiThreaded(false), ar -> {
      if (ar.succeeded()) {
        vertx.deployVerticle(EventSenderVerticle.class, new DeploymentOptions());
        vertx.deployVerticle(EventManagerAdministratorVerticle.class, new DeploymentOptions());
      }
      else System.err.println(ar.cause().toString());
    });
  }

}
