package ua.ferret.app.mask;

import static ua.ferret.app.zutils.ColorUtils.getBrightness;
import static ua.ferret.app.zutils.ColorUtils.getPixel;
import static ua.ferret.app.zutils.ColorUtils.getRGB;

import java.awt.image.BufferedImage;

import io.vertx.core.json.JsonObject;
import ua.ferret.app.zutils.MaskInstance;

public class MaskNonlinearContrastStretch implements MaskInstance {

	private static final String R_VALUE_NAME = "m_n_c_s_R";
	private static final String S_VALUE_NAME = "m_n_c_s_S";

	@Override
	public void execute(BufferedImage image, JsonObject config) {
		var r = Double.valueOf(config.getString(R_VALUE_NAME, "0.9"));
		var S = Integer.valueOf(config.getString(S_VALUE_NAME, "50"));

		var max = Integer.MIN_VALUE;
		var min = Integer.MAX_VALUE;

		for (var x = 0; x < image.getWidth(); x++)
			for (var y = 0; y < image.getHeight(); y++) {
				var pixel = getBrightness(getRGB(image.getRGB(x, y)));
				max = Math.max(pixel, max);
				min = Math.min(pixel, min);
			}
		
		for (var x = 0; x < image.getWidth(); x++)
			for (var y = 0; y < image.getHeight(); y++) {
				var pixel = getBrightness(getRGB(image.getRGB(x, y)));
				pixel = (int) (255 * Math.pow((double) (pixel - min) / (max - min), r));
				image.setRGB(x, y, getPixel(Math.max(Math.min(pixel + S, 255), 0)));
			}

	}
	
	@Override
	public JsonObject requirements(JsonObject config) {
		var R = new JsonObject();
		R.put("value", config.getValue(R_VALUE_NAME, 1));
		R.put("name", R_VALUE_NAME);
		R.put("step", 0.01);
		R.put("max", 6);
		R.put("min", 0);
		var S = new JsonObject();
		S.put("value", config.getValue(S_VALUE_NAME, 0));
		S.put("name", S_VALUE_NAME);
		S.put("step", 1);
		S.put("max", 255);
		S.put("min", 0);
		return JsonObject.of("r", R, "S", S);
	}

}
