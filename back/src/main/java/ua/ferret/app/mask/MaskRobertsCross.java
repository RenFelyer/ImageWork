package ua.ferret.app.mask;

import static ua.ferret.app.zutils.ColorUtils.getPixel;
import static ua.ferret.app.zutils.ColorUtils.getRGB;

import java.awt.image.BufferedImage;

import io.vertx.core.json.JsonObject;
import ua.ferret.app.zutils.MaskInstance;

public class MaskRobertsCross implements MaskInstance {

	@Override
	public void execute(BufferedImage image, JsonObject header) {
		int width = image.getWidth() - 1;
		int height = image.getHeight() - 1;

		var kernelX = new int[][] { { 1, 0 }, { 0, -1 } };
		var kernelY = new int[][] { { 0, 1 }, { -1, 0 } };
		var magnitude = new int[width][height][3];

		// Apply the kernel to each pixel in the image
		for (var x = 0; x < width; x++)
			for (var y = 0; y < height; y++) {
				// Compute the gradient magnitude for this pixel
				var gx = getConvolution(image, x, y, kernelX);
				var gy = getConvolution(image, x, y, kernelY);
				magnitude[x][y] = getMagnitude(gx, gy);
			}

		for (var x = 0; x < width; x++)
			for (var y = 0; y < height; y++)
				image.setRGB(x, y, getPixel(magnitude[x][y]));
	}

	private static int[] getConvolution(BufferedImage image, int x, int y, int[][] kernel) {
		var result = new int[3];

		for (int i = 0; i < kernel.length; i++) {
			for (int j = 0; j < kernel[i].length; j++) {
				var px = x + i;
				var py = y + j;

				if (px < 0 || px >= image.getWidth() || py < 0 || py >= image.getHeight())
					continue;

				for (int k = 0; k < result.length; k++) {
					result[k] += (kernel[i][j] * getRGB(image.getRGB(px, py))[k]);
				}
			}
		}

		return result;
	}

	public static int[] getMagnitude(int[] gx, int[] gy) {
		var magnitude = new int[3];
		for (int k = 0; k < magnitude.length; k++)
			magnitude[k] = (int) Math.min(255, Math.sqrt(gx[k] * gx[k] + gy[k] * gy[k]));
		return magnitude;
	}
}
