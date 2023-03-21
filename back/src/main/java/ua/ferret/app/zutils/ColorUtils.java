package ua.ferret.app.zutils;

public final class ColorUtils {

	private ColorUtils() {
	}

	public static int[] getRGB(int pixel) {
		return new int[] { (pixel >> 16) & 0xFF, (pixel >> 8) & 0xFF, (pixel >> 0) & 0xFF };
	}

	public static int getPixel(int color) {
		return getPixel(new int[] { color, color, color });
	}

	public static int getPixel(int[] rgb) {
		return ((255 & 0xFF) << 24) | ((rgb[0] & 0xFF) << 16) | ((rgb[1] & 0xFF) << 8) | ((rgb[2] & 0xFF) << 0);
	}

	public static int getBrightness(int[] rgb) {
		return (int) (0.33 * rgb[0] + 0.56 * rgb[1] + 0.11 * rgb[2]);
	}

}
