package ua.ferret.app.zutils;

import java.awt.image.BufferedImage;

import io.vertx.core.json.JsonObject;

public interface MaskInstance {

	public static final MaskInstance MASK_NONE = (image, config) -> {
	};

	void execute(BufferedImage image, JsonObject header);
	
	default JsonObject requirements(JsonObject config) {
		return JsonObject.of();
	}

}
