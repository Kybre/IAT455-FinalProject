import java.awt.Color;
import java.awt.image.BufferedImage;

public class Functions {
	protected static int getRed(int pixel) {
		return (pixel >>> 16) & 0xFF;
	}

	protected static int getGreen(int pixel) {
		return (pixel >>> 8) & 0xFF;
	}

	protected static int getBlue(int pixel) {
		return pixel & 0xFF;
	}
}
