package ua.ferret.app.verticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

@Component
public class PageVerticle extends AbstractVerticle {

	private static Logger log = LoggerFactory.getLogger(PageVerticle.class);

	@Override
	public void start(Promise<Void> promise) throws Exception {
		var router = Router.router(vertx);

		router.get().handler(StaticHandler.create("webroot"));
		router.get().handler(this::rerouteToIndex);


		vertx.createHttpServer().requestHandler(router).listen(8080).onSuccess(http -> {
			log.info("{} launched on the port: {}", getClass().getSimpleName(), http.actualPort());
			promise.complete();
		}).onFailure(promise::fail);
	}

	private void rerouteToIndex(RoutingContext rc) {
		if (!"/".equals(rc.normalizedPath())) {
			rc.reroute("/");
		} else {
			rc.next();
		}
	}

}
