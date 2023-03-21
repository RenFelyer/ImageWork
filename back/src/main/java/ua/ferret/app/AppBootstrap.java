package ua.ferret.app;

import static ua.ferret.app.AppEnvironment.CONFIG_PATH;
import static ua.ferret.app.AppEnvironment.IMAGES_PATH;

import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@Component
public class AppBootstrap {

	@EventListener({ ContextStartedEvent.class, ContextRefreshedEvent.class })
	void started(ApplicationContextEvent event) {
		var ctx = event.getApplicationContext();
		var vertx = ctx.getBean(Vertx.class);

		vertx.fileSystem().mkdirs(CONFIG_PATH);
		vertx.fileSystem().mkdirs(IMAGES_PATH);

		for (var verticle : ctx.getBeanProvider(Verticle.class)) {
			var options = new DeploymentOptions();
			options.setClassLoader(ctx.getClassLoader());
			options.setConfig(loadConfig(vertx, getPath(verticle)));
			vertx.deployVerticle(verticle, options);
		}
	}

	@EventListener({ ContextStoppedEvent.class, ContextClosedEvent.class })
	void stopped(ApplicationContextEvent event) {
		var ctx = event.getApplicationContext();
		var vertx = ctx.getBean(Vertx.class);
		for (var verticle : ctx.getBeanProvider(AbstractVerticle.class))
			vertx.fileSystem().writeFileBlocking(getPath(verticle), verticle.config().toBuffer());
		vertx.close();
	}

	private static JsonObject loadConfig(Vertx vertx, String path) {
		var temp = new JsonObject();
		if(vertx.fileSystem().existsBlocking(path))
			temp = new JsonObject(vertx.fileSystem().readFileBlocking(path)); 
		return temp;
	}

	private static String getPath(Verticle verticle) {
		return CONFIG_PATH + verticle.getClass().getSimpleName().toLowerCase().replace("verticle", "") + ".json";
	}

}
