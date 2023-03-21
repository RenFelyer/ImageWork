package ua.ferret.app.verticle;

import static ua.ferret.app.AppEnvironment.IMAGES_PATH;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import ua.ferret.app.zutils.RestUtils;

@Component
public class RestVerticle extends AbstractVerticle {

	private static Logger log = LoggerFactory.getLogger(RestVerticle.class);
	private @Autowired ApplicationContext context;

	@Override
	public void start(Promise<Void> promise) throws Exception {
		var options = new HttpServerOptions();
		options.setCompressionSupported(true);
		options.setUseAlpn(true);

		var router = Router.router(vertx);
		router.route().handler(RestUtils.disableCors());
		router.route("/eventbus/*").subRouter(RestUtils.createBridge(vertx));

		router.route("/info").handler(this::info);
		router.post("/upload").handler(this::upload);
		router.route("/selected").handler(this::selected);
		router.route("/load/:filename").handler(this::load);
		router.route("/shutdown").handler(this::shutdown);

		vertx.createHttpServer(options).requestHandler(router).listen(8393).onSuccess(http -> {
			log.info("{} launched on the port: {}", getClass().getSimpleName(), http.actualPort());
			promise.complete();
		}).onFailure(promise::fail);
	}

	private void info(RoutingContext ctx) {
		var image = vertx.eventBus().request("image.info", "").map(Message::body);
		var affine = vertx.eventBus().request("affine.info", "").map(Message::body);
		var buffer = vertx.eventBus().request("buffer.info", "").map(Message::body);
		CompositeFuture.all(image, affine, buffer).onSuccess(res -> {
			var json = new JsonObject();
			json.put("image", res.resultAt(0));
			json.put("affine", res.resultAt(1));
			json.put("buffer", res.resultAt(2));
			ctx.end(json.toBuffer());
		}).onFailure(ctx::fail);
	}

	private void upload(RoutingContext ctx) {
		var req = ctx.request();
		req.setExpectMultipart(true);
		req.uploadHandler(upload -> {
			var loadname = upload.filename();
			var extension = loadname.substring(loadname.lastIndexOf("."));

			if (!(extension.endsWith(".png") || extension.endsWith(".jpg"))) {
				ctx.fail(new IllegalArgumentException("Only files with .png and .jpg extensions are acceptable"));
				return;
			}

			var filename = UUID.randomUUID() + extension;
			upload.streamToFileSystem(IMAGES_PATH + filename);
			upload.exceptionHandler(ctx::fail);
			upload.endHandler(some -> {
				vertx.eventBus().publish("image.upload", filename);
				ctx.end();
			});
		});
	}

	private void selected(RoutingContext ctx) {
		vertx.eventBus().<Buffer>request("buffer.image", "").map(Message::body).onSuccess(ctx::end)
				.onFailure(ctx::fail);
	}

	private void load(RoutingContext ctx) {
		vertx.fileSystem().readFile(IMAGES_PATH + ctx.pathParam("filename")).onSuccess(ctx::end).onFailure(ctx::fail);
	}

	private void shutdown(RoutingContext ctx) {
		SpringApplication.exit(context);
		ctx.end("Servert shutdown");
	}
}
