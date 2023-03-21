package ua.ferret.app.zutils;

import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.OPTIONS;
import static io.vertx.core.http.HttpMethod.POST;

import io.vertx.core.Vertx;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

public final class RestUtils {

	private RestUtils() {}

	public static CorsHandler disableCors() {
		return CorsHandler.create().allowedMethod(GET).allowedMethod(POST).allowedMethod(OPTIONS).allowCredentials(true)
				.allowedHeader("Access-Control-Allow-Method").allowedHeader("Access-Control-Allow-Origin")
				.allowedHeader("Access-Control-Allow-Credentials").allowedHeader("Content-Type");
	}

	public static Router createBridge(Vertx vertx) {
		SockJSBridgeOptions options = new SockJSBridgeOptions();
		options.addInboundPermitted(new PermittedOptions().setAddressRegex(".*"));
		options.addOutboundPermitted(new PermittedOptions().setAddressRegex(".*"));
		return SockJSHandler.create(vertx).bridge(options);
	}

}
