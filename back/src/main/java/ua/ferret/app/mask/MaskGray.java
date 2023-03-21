package ua.ferret.app.mask;

import java.awt.image.BufferedImage;

import io.vertx.core.json.JsonObject;
import static ua.ferret.app.zutils.ColorUtils.*;
import ua.ferret.app.zutils.MaskInstance;

public class MaskGray implements MaskInstance {

	@Override
	public void execute(BufferedImage image, JsonObject header) {
		for(var x = 0; x < image.getWidth(); x++)
			for(var y = 0; y < image.getHeight(); y++)
				image.setRGB(x, y, getPixel(getBrightness(getRGB(image.getRGB(x, y)))));
	}

}
