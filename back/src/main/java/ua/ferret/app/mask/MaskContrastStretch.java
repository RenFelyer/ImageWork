package ua.ferret.app.mask;

import static ua.ferret.app.zutils.ColorUtils.*;

import java.awt.image.BufferedImage;

import io.vertx.core.json.JsonObject;
import ua.ferret.app.zutils.MaskInstance;

public class MaskContrastStretch implements MaskInstance {

	private static final String MAX_VALUE_NAME = "m_c_s_MAX";
	private static final String MIN_VALUE_NAME = "m_c_s_MIN";

	@Override
	public void execute(BufferedImage image, JsonObject config) {
		var max = Integer.valueOf(config.getString(MAX_VALUE_NAME, "255"));
		var min = Integer.valueOf(config.getString(MIN_VALUE_NAME, "0"));

		for (var x = 0; x < image.getWidth(); x++)
			for (var y = 0; y < image.getHeight(); y++) {
				var pixel = getBrightness(getRGB(image.getRGB(x, y)));
				pixel = (int) ((pixel - min) * 255.0 / (max - min));
				image.setRGB(x, y, getPixel(pixel));
			}
	}

	@Override
	public JsonObject requirements(JsonObject config) {
		var max = new JsonObject();
		max.put("value", config.getValue(MAX_VALUE_NAME, 255));
		max.put("name", MAX_VALUE_NAME);
		max.put("step", 1);
		max.put("max", 255);
		max.put("min", 0);
		var min = new JsonObject();
		min.put("value", config.getValue(MIN_VALUE_NAME, 0));
		min.put("name", MIN_VALUE_NAME);
		min.put("step", 1);
		min.put("max", 255);
		min.put("min", 0);
		return JsonObject.of("max", max, "min", min);
	}
}
