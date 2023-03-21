package ua.ferret.app.zutils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import ua.ferret.app.verticle.BufferVerticle;

public final class ImageUtils {
	private static final Logger log = LoggerFactory.getLogger(BufferVerticle.class);

	private ImageUtils() {
	}

	public static boolean hasImage(String filename) {
		return StringUtils.hasLength(filename) && (filename.endsWith(".jpg") || filename.endsWith(".png"));
	}

	public static Future<BufferedImage> bufferToImage(Buffer buffer) {
		try {
			var inputStream = new ByteArrayInputStream(buffer.getBytes());
			return Future.succeededFuture(ImageIO.read(inputStream));
		} catch (IOException ex) {
			return Future.failedFuture(ex);
		}
	}

	public static Future<Buffer> imageToBuffer(BufferedImage image) {
		try {
			if (image == null)
				return Future.succeededFuture(Buffer.buffer());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", baos);
			return Future.succeededFuture(Buffer.buffer(baos.toByteArray()));
		} catch (IOException ex) {
			log.warn("Transformed image not found", ex);
			return Future.failedFuture(ex);
		}
	}

	public static Future<Integer[]> intToInteger(int[] pixels) {
		var arr = new Integer[pixels.length];
		for (int i = 0; i < pixels.length; i++)
			arr[i] = pixels[i];
		return Future.succeededFuture(arr);
	}
}
