import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

public class IFSEncoder {

	@SuppressWarnings("unused")
	public static void Encode(String inputFile, String outputFile, int size) throws IOException {
		File imageFile = new File(inputFile);
		BufferedImage image = ImageIO.read(imageFile);
		OutputStream outputStream = new FileOutputStream(outputFile);
		outputStream.write(image.getHeight());
		outputStream.write(image.getWidth());
		int rangeBlockCount = image.getHeight() * image.getWidth() / (size * size);
		int domainBlockCount = (image.getHeight() + 1 - 2 * size) * (image.getWidth() + 1 - 2 * size);
		int r = 0, s = 0;
		double[] temp = new double[6];
		byte[] code = new byte[5];
		for (int i = 0; i < rangeBlockCount; i++) {
			r = i / (image.getWidth() + 1 - size);
			s = i % (image.getWidth() + 1 - size);
			temp = IFSEncoder.selectBestDomain(image, s * size, r * size, size);
			int position = (s / (2 * size)) * (r / (2 * size));
			code[0] = (byte) ((((int) temp[5]) & 0x7) << 5 + ((Math.round(temp[2] * 31)) & 0x1F));
			code[1] = (byte) ((((int) temp[3]) & 0x7F) << 1 + (position & 0x1));
			position >>= 1;
			code[2] = (byte) (position & 0xFF);
			position >>= 8;
			code[3] = (byte) (position & 0xFF);
			position >>= 8;
			code[4] = (byte) (position & 0xFF);
			position >>= 8;
			code[5] = (byte) (position & 0xFF);
			outputStream.write(code);
		}
		outputStream.close();
	}

	public static double[] selectBestDomain(BufferedImage image, int rx, int ry, int size) {
		short[][] R = CMap.imageToArray(image, rx, ry, rx + size, ry + size);
		short[][] D;
		double[] comparison = new double[] { -1, -1, 0, 0, Double.MAX_VALUE, -1 };
		double[] temp;
		int r = 0, s = 0;
		for (int i = 0; i < (image.getHeight() + 1 - 2 * size) * (image.getWidth() + 1 - 2 * size); i++) {
			r = i / (image.getHeight() + 1 - 2 * size);
			s = i % (image.getHeight() + 1 - 2 * size);
			D = CMap.subsample(CMap.imageToArray(image, s, r, s + 2 * size, r + 2 * size));
			for (int j = 0; j < 8; j++) {
				D = CMap.permute(D, j);
				temp = IFSEncoder.regression(D, R);
				if (temp[2] < comparison[4]) {
					comparison[0] = s;
					comparison[1] = r;
					comparison[2] = temp[0];
					comparison[3] = temp[1];
					comparison[4] = temp[2];
					comparison[5] = j;
				}
			}
		}

		return comparison;
	}

	public static double[] regression(short[][] F, short[][] R) {
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

	}

}
