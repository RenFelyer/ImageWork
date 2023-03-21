package ua.ferret.app.verticle;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

@Component
public class AffineVerticle extends AbstractVerticle {

	private static Logger log = LoggerFactory.getLogger(AffineVerticle.class);
	private final Map<String, Object> params = Map.of("kx", 0, "ky", 0, "a", 0, "sx", 1, "sy", 1);

	@Override
	public void start() throws Exception {
		vertx.eventBus().consumer("affine.info").handler(this::info);
		vertx.eventBus().consumer("affine.update").handler(this::update);
		log.info("{} launched with params: {}", getClass().getSimpleName(), config());
	}

	private void update(Message<Object> message) {
		var type = message.headers().get("type");
		var data = message.body().toString();
		if (params.containsKey(type))
			config().put(type, data.isBlank() || data.endsWith(".") || data.endsWith(",") ? 0 : data);
	}

	private void info(Message<Object> message) {
		var json = new JsonObject();
		params.forEach((key, value) -> json.put(key, config().getValue(key, value)));
		message.reply(json);
	}

}
