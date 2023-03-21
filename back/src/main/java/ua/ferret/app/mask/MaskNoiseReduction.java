package ua.ferret.app.mask;

import static ua.ferret.app.zutils.ColorUtils.getBrightness;
import static ua.ferret.app.zutils.ColorUtils.getPixel;
import static ua.ferret.app.zutils.ColorUtils.getRGB;

import java.awt.image.BufferedImage;

import io.vertx.core.json.JsonObject;
import ua.ferret.app.zutils.MaskInstance;

public class MaskNoiseReduction implements MaskInstance {

	private static final String NAME_VALUE = "m_n_r_x";

	@Override
	public void execute(BufferedImage image, JsonObject config) {
		var mask = new int[9];
		var temp = new int[image.getWidth()][image.getHeight()];

		for (var i = 0; i < mask.length; i++)
			mask[i] = Integer.valueOf(config.getString(NAME_VALUE + i, "0"));

		for (var x = 0; x < image.getWidth(); x++)
			for (var y = 0; y < image.getHeight(); y++)
				temp[x][y] = getConvolution(image, x, y, mask);

		for (var x = 0; x < image.getWidth(); x++)
			for (var y = 0; y < image.getHeight(); y++)
				image.setRGB(x, y, getPixel(temp[x][y]));
	}

	private static int getConvolution(BufferedImage image, int x, int y, int[] mask) {
		var sum = 0;
		var del = 0;

		for (var i = -1; i <= 1; i++)
			for (var j = -1; j <= 1; j++) {
				var v = x + i;
				var h = y + 1;

				if (v < 0 || v >= image.getWidth() || h < 0 || h >= image.getHeight())
					continue;

				var index = (i + 1) * 3 + (j + 1);
				sum += mask[index] * getBrightness(getRGB(image.getRGB(v, h)));
				del += mask[index];
			}

		return Math.min(255, sum / Math.max(del, 1));
	}

	@Override
	public JsonObject requirements(JsonObject config) {
		return JsonObject.of(//
				"x11", cell(0, 0, config), "x12", cell(1, 0, config), "x13", cell(2, 0, config), //
				"x21", cell(3, 0, config), "x22", cell(4, 0, config), "x23", cell(5, 0, config), //
				"x31", cell(6, 0, config), "x32", cell(7, 0, config), "x33", cell(8, 0, config)//
		);
	}

	private JsonObject cell(int index, int defaultValue, JsonObject config) {
		var json = new JsonObject();
		json.put("value", config.getValue(NAME_VALUE + index, defaultValue));
		json.put("name", NAME_VALUE + index);
		json.put("step", 1);
		json.put("max", 9);
		json.put("min", 0);
		return json;
	}

}
