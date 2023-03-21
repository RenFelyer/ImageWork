package ua.ferret.app.verticle;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static ua.ferret.app.AppEnvironment.IMAGES_PATH;
import static ua.ferret.app.zutils.ColorUtils.getBrightness;
import static ua.ferret.app.zutils.ColorUtils.getRGB;
import static ua.ferret.app.zutils.MaskInstance.MASK_NONE;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import ua.ferret.app.mask.MaskContrastStretch;
import ua.ferret.app.mask.MaskGray;
import ua.ferret.app.mask.MaskImageFilter;
import ua.ferret.app.mask.MaskNoiseReduction;
import ua.ferret.app.mask.MaskNonlinearContrastStretch;
import ua.ferret.app.mask.MaskRobertsCross;
import ua.ferret.app.zutils.ImageUtils;
import ua.ferret.app.zutils.MaskInstance;

@Component
public class BufferVerticle extends AbstractVerticle {

	private static Logger log = LoggerFactory.getLogger(BufferVerticle.class);

	private Map<String, MaskInstance> mask = Map.of(//
			"Gray", new MaskGray(), //
			"Contrast Stretch", new MaskContrastStretch(),						// 3
			"Non-linear Contrast Stretch", new MaskNonlinearContrastStretch(),	// 3
			"Noise Reduction", new MaskNoiseReduction(),						// 4
			"Roberts Filter", new MaskRobertsCross(),							// 5
			"Image Filter", new MaskImageFilter()							// 6-7
			
	);

	private BufferedImage original;
	private BufferedImage transformed;

	@Override
	public void start() throws Exception {
		vertx.eventBus().consumer("buffer.info").handler(this::info);
		vertx.eventBus().consumer("buffer.image").handler(this::image);

		vertx.eventBus().<String>consumer("buffer.selected").handler(this::selected);
		vertx.eventBus().<String>consumer("buffer.transformation").handler(this::transformation);
		log.info("{} launched with mask: {}", getClass().getSimpleName(), mask.keySet());
	}

	private void info(Message<Object> message) {
		message.reply(getData());
	}

	private void image(Message<Object> message) {
		ImageUtils.imageToBuffer(transformed).onSuccess(message::reply).onFailure(error -> {
			log.warn("Error when converting a file to bits", error);
			message.fail(404, error.getMessage());
		});
	}

	private void selected(Message<String> message) {
		var path = IMAGES_PATH + message.body();
		vertx.fileSystem().readFile(path).compose(ImageUtils::bufferToImage).onSuccess(image -> {
			transformed = new BufferedImage((original = image).getWidth(), image.getHeight(), TYPE_INT_RGB);
			vertx.eventBus().publish("buffer.transformation", config().getString("mask", "Base"));
		}).onFailure(error -> {
			log.warn("Image could not be uploaded", error);
			message.fail(404, error.getMessage());
		});
	}

	private void transformation(Message<String> message) {
		transformed.setData(original.getData());
		var data = message.body();
		var headers = message.headers();
		var pixels = ((DataBufferInt) transformed.getRaster().getDataBuffer()).getData();

		config().put("mask", data);
		headers.forEach((name, value) -> config().put(name, value));
		mask.getOrDefault(data, MASK_NONE).execute(transformed, config());
		vertx.eventBus().publish("buffer.update.image", "");

		CompositeFuture.all(getHistogram(pixels), getMeanIntensity(pixels)).onSuccess(res0 -> {
			context.putLocal("histogram", res0.<Integer[]>resultAt(0));
			context.putLocal("meanIntensity", res0.<Double>resultAt(1));
			CompositeFuture.all(getContrast(pixels), getEntropy(pixels.length)).onSuccess(res1 -> {
				context.putLocal("contrast", res1.<Double>resultAt(0));
				context.putLocal("entropy", res1.<Double>resultAt(1));
				vertx.eventBus().publish("buffer.update.info", getData());
			}).onFailure(error -> {
				message.fail(404, error.getMessage());
				log.warn("Unable to calculate Contrast and Entropy", error);
			});
		}).onFailure(error -> {
			message.fail(404, error.getMessage());
			log.warn("Unable to calculate Histogram and Mean Intensity", error);
		});
	}

	public Future<Integer[]> getHistogram(int[] pixels) {
		return vertx.<int[]>executeBlocking(promise -> {
			int[] histogram = new int[256];
			for (int i = 0; i < pixels.length; i++)
				histogram[getBrightness(getRGB(pixels[i]))]++;
			promise.complete(histogram);
		}, false).compose(ImageUtils::intToInteger);
	}

	public Future<Double> getMeanIntensity(int[] pixels) {
		return vertx.executeBlocking(promise -> {
			long meanIntensity = 0L;
			for (int i = 0; i < pixels.length; i++) {
				int r = (pixels[i] >> 16) & 0xFF;
				int g = (pixels[i] >> 8) & 0xFF;
				int b = (pixels[i] >> 0) & 0xFF;
				meanIntensity += r + g + b;
			}
			promise.complete(meanIntensity / (pixels.length * 3.0));
		}, false);
	}

	public Future<Double> getContrast(int[] pixels) {
		final double meanIntensity = context.<Double>getLocal("meanIntensity");
		return vertx.executeBlocking(promise -> {
			double contrast = 0L;
			for (int i = 0; i < pixels.length; i++) {
				int r = (pixels[i] >> 16) & 0xFF;
				int g = (pixels[i] >> 8) & 0xFF;
				int b = (pixels[i] >> 0) & 0xFF;
				double deviation = (r + g + b) / 3 - meanIntensity;
				contrast += deviation * deviation;
			}
			promise.complete(contrast / (pixels.length) - 1);
		}, false);
	}

	public Future<Double> getEntropy(double totalPixels) {
		final var histogram = context.<Integer[]>getLocal("histogram");
		return vertx.executeBlocking(promise -> {
			double entropy = 0;
			for (int i = 0; i < histogram.length; i++) {
				var probabilities = histogram[i] / totalPixels;
				entropy -= probabilities > 0 ? probabilities * (Math.log(probabilities) / Math.log(2)) : 0;
			}
			promise.complete(entropy);
		}, false);
	}

	private JsonObject getData() {
		var hist = context.<Integer[]>getLocal("histogram");
		
		var json = new JsonObject();
		json.put("currentMask", config().getString("mask", "Base"));
		json.put("histogram", hist == null ? List.of() : List.of(hist));
		json.put("meanIntensity", context.<Double>getLocal("meanIntensity"));
		json.put("contrast", context.<Double>getLocal("contrast"));
		json.put("entropy", context.<Double>getLocal("entropy"));

		var trans = new JsonObject();
		for (var name : mask.keySet())
			trans.put(name, mask.getOrDefault(name, MASK_NONE).requirements(config()));

		json.put("mask", trans);
		return json;
	}
}
