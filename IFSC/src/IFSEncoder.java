import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class IFSEncoder {

	@SuppressWarnings("unused")
	public static void Encode(String inputFile, int size) throws IOException {
		File imageFile = new File(inputFile);
		BufferedImage image = ImageIO.read(imageFile);
		int rangeBlockCount = image.getHeight() * image.getWidth() / (size * size);
		int domainBlockCount = (image.getHeight() + 1 - 2 * size) * (image.getWidth() + 1 - 2 * size);
		int[] comparison = new int[5];

	}

	public static double[] regression(int[][] F, int[][] R) {
		int a = 0, a2 = 0, b = 0, b2 = 0, ab = 0;
		int n = F.length * F[0].length;
		int g = 0;
		double s = 0;
		double ms = 0;
		for (int i = 0; i < F.length; i++) {
			for (int j = 0; j < F[0].length; j++) {
				a += F[i][j];
				b += R[i][j];
				ab += F[i][j] * R[i][j];
				a2 += F[i][j] * F[i][j];
				b2 += R[i][j] * R[i][j];
			}
		}
		s = (((double) n * ab - a * b) / (n * a2 - a * a));
		g = (int) (((double) b - s * a) / (n));
		ms = ((double) b2 + s * (s * a2 - 2 * ab + 2 * g * a) + g * (n * g - 2 * b)) / (n);
		double[] result = { s, g, Math.sqrt(ms) };
		return result;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
